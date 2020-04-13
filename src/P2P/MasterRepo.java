package P2P;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import FTP.ServerDTP;

/**
 * The Master repository is olding the directory and distribute it to the
 * clients.
 * 
 * @author k1nd0ne
 *
 */
public class MasterRepo extends Thread {
	private Directory directory;
	private String message;
	private String path, port;
	private Socket sockService;

	public MasterRepo(Socket sockService, Directory directory) throws IOException {
		this.message = "";
		this.path = "./src/mydb"; // Default path.
		this.sockService = sockService;
		this.directory = directory;
	}

	/**
	 * @param sockService
	 * @return receive message.
	 */
	public String ReceiveMessage(Socket sockService) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sockService.getInputStream()));
			// Lit une ligne de caractères depuix le flux, et donc la reçoit du client
			String messageRcv = reader.readLine();
			System.out.println(messageRcv);
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Send message to the specified socket.
	 * 
	 * @param sockService
	 * @param message
	 */
	public void SendMessage(Socket sockService, String message) {
		try {
			PrintStream pStream = new PrintStream(sockService.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Authentication function.
	 * @param message
	 * @return true if user and pass are correct using the check method.
	 * @throws IOException
	 */
	public boolean Authentifiate(String message) throws IOException {
		if (message.contains("USER") && message.contains("PASS")) {
			try {
				String info[] = message.split(":");
				String user = info[1];
				String pass = info[3];
				System.out.println("Server-PI : User =>" + user);
				System.out.println("Server-PI : Pass => " + pass);
				return Check(user, pass);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param user
	 * @param pass
	 * @return boolean, true if creds are correct.
	 * @throws IOException
	 */
	public boolean Check(String user, String pass) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("./src/FTP/passwd.txt"));
		try {
			String line = br.readLine();
			while (line != null) {
				String info[] = line.split(":");
				if (user.equals(info[0])) {
					if (pass.equals(info[1])) {
						return true;
					}
				}
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		return false;
	}

	/**
	 * Ls function converted to our messages format
	 * 
	 * @param path
	 * @return
	 */
	public String getFileList(String path) {
		String res = "";
		try {
			String lscmd = "ls " + path;
			Process p = Runtime.getRuntime().exec(lscmd);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				// System.out.println(line);
				res += line + ":";
				line = reader.readLine();
			}
		} catch (IOException e1) {
			System.out.println("Pblm found1.");
		} catch (InterruptedException e2) {
			System.out.println("Pblm found2.");
		}
		return res;
	}

	public void run() {

		/********************************************************/
		/* AUTHENTICATION PHASE */
		/********************************************************/
		boolean sessionStatus = true;

		// TCP Connection successfull//
		message = ReceiveMessage(sockService);
		if (message != null) {
			try {
				sessionStatus = Authentifiate(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			SendMessage(sockService, "400"); // Sending error : Syntax
		}
		// Check de l'authentification
		if (sessionStatus == false) {
			System.out.println("Server-PI : could not authentifiate" + sockService.getInetAddress());
			SendMessage(sockService, "530"); // Sending error : Authentication failure.
		} else {
			System.out.println("Server-PI : OK");
			SendMessage(sockService, "230"); // Sending success : authentication ok.
		}

		/********************************************************/
		/* MESSAGE HANDLER PHASE */
		/********************************************************/
		while (sessionStatus) {
			this.message = ReceiveMessage(sockService);
			if (message == null) {
				sessionStatus = false;
			}
			// The user is changing database/dir.
			else if (this.message.startsWith("PATH:")) {
				String tempPath = "";
				if (this.message.length() <= 5) {
					tempPath = "~/";
				} else {
					if (!new File(tempPath = message.split(":")[1]).exists()) {
						if (!new File((tempPath = this.path + "/" + message.split(":")[1])).exists()) {
							SendMessage(sockService, "550"); // Syntax error : Path unknown
						} else {
							this.path = tempPath;
							System.out.println("Server-PI : PATH =>" + this.path);
							SendMessage(sockService, "310");
						}
					} else {
						this.path = tempPath;
						System.out.println("Server-PI : PATH =>" + this.path);
						SendMessage(sockService, "310");
					}

				}
			}
			// A Server want to say that he is sharing a part of a file.
			else if (message.startsWith("REGISTER:")) {
				System.out.println("A server is registering...");
				try {
					ObjectInputStream is = new ObjectInputStream(sockService.getInputStream());
					ArrayList<Register> reglist = (ArrayList<Register>) is.readObject();
					for (Register reg : reglist) {
						directory.addServer(reg.getServer(), Integer.toString(reg.getPort()), reg.getFileName());
					}
				} catch (Exception e) {
					SendMessage(sockService, "550");
				}
				// A client wants to retrieve the serverList for a file
			} else if (message.startsWith("GET:")) {
				try {
					String filename = message.split(":")[1];
					ObjectOutputStream os = new ObjectOutputStream(sockService.getOutputStream());
					os.writeObject(directory.getServersFromFileName(filename));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// A client wan't to see the content of the database
			else if (message.startsWith("LS:")) {
				if (message.length() > 3) {
					String tempPath = message.split(":")[1];
					if (!new File(tempPath).exists()) {
						SendMessage(sockService, "550");
					} else {
						SendMessage(sockService, "300");
						SendMessage(sockService, getFileList(tempPath));
					}

				} else {
					SendMessage(sockService, "300");
					SendMessage(sockService, getFileList(this.path));
				}
			}

			else {
				SendMessage(sockService, "500"); // Syntax error
			}
		}

		try {
			sockService.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
