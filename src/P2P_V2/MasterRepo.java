package P2P_V2;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The master Repo thread. Handle a client. And wait for his instructions.
 * It also handle the directory informations. 
 * If a client register to him -> Update the directory. 
 * If a client leave -> Update too. 
 * Gettting a file means -> send him the list of servers that have this file.
 * The master send also the disponible files from the directory to the client.
 * @author k1nd0ne
 *
 */
public class MasterRepo extends Thread {
	private Directory directory;
	private String message;
	private String path, port;
	private Socket sockService;
	private String timeStamp;

	public MasterRepo(Socket sockService, Directory directory) throws IOException {
		this.message = "";
		this.sockService = sockService;
		this.directory = directory;
	}

	/**
	 * @param sockService
	 * @return received message.
	 */
	public String ReceiveMessage(Socket sockService) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sockService.getInputStream()));
			// Lit une ligne de caractères depuix le flux, et donc la reçoit du client
			String messageRcv = reader.readLine();
			// System.out.println(messageRcv);
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * @param sockService
	 * @param message     Send message to the specified socket.
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
	 * 
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
				this.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
				System.out
						.println("--" + timeStamp + "--" + "[Master Repository] " + user + " attempt to authenticate.");
				// System.out.println("Server-PI : Pass => " + pass);
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
		this.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		if (sessionStatus == false) {

			System.out.println("--" + timeStamp + "--" + "[Master Repository] Could not authentifiate"
					+ sockService.getInetAddress() + ":" + (sockService.getPort() + 1));
			SendMessage(sockService, "530"); // Sending error : Authentication failure.
		} else {
			System.out.println("--" + timeStamp + "--" + "[Master Repository] Authentication success for "
					+ sockService.getInetAddress() + ":" + (sockService.getPort() + 1));
			SendMessage(sockService, "230"); // Sending success : authentication ok.
		}

		/********************************************************/
		/* MESSAGE HANDLER PHASE */
		/********************************************************/
		while (sessionStatus) {
			this.timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			
			//Wait for a message
			this.message = ReceiveMessage(sockService);
			if (message == null) { // The client is leaving we need to unregister him from the directory
				System.out.println("--" + timeStamp + "--" + "[Master Repository] " + sockService.getInetAddress() + ":"
						+ (sockService.getPort() + 1) + " disconnected.");
				directory.removeServer(
						sockService.getInetAddress().toString().substring(1) + ":" + (sockService.getPort() + 1));
				directory.printDirectory();
				sessionStatus = false;
			}
			
			// A Server want to say that he is sharing a part of a file.
			else if (message.startsWith("REGISTER:")) {
				System.out.println("--" + timeStamp + "--" + "[Master Repository] " + sockService.getInetAddress() + ":"
						+ sockService.getPort() + " is writting to the directory.");

				try {
					ObjectInputStream is = new ObjectInputStream(sockService.getInputStream());
					ArrayList<Register> reglist = (ArrayList<Register>) is.readObject();
					for (Register reg : reglist) {
						directory.addServer(sockService.getInetAddress().toString().substring(1), Integer.toString(reg.getPort()), reg.getFileName());
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				this.directory.printDirectory();
				
				
				// A client wants to retrive the serverList for a file
			} else if (message.startsWith("GET:")) {
				try {
					String filename = message.split(":")[1];
					ArrayList<String> serverList = directory.getServersFromFileName(filename);
					if(serverList.size() == 0) {
						System.out.println("TAILLE : " + serverList.size());
						SendMessage(sockService, "500");
					}
					else {
						SendMessage(sockService, "300");
					}
					ObjectOutputStream os = new ObjectOutputStream(sockService.getOutputStream());
					os.writeObject(serverList);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// A client wan't to see the content of the database
			else if (message.startsWith("LS:")) {

				SendMessage(sockService, "300");
				ArrayList<String> fileList = directory.getFileList();
				String fileListToSend = "";
				for (String file : fileList) {
					fileListToSend += file + ":";
				}
				SendMessage(sockService, fileListToSend);
			}

			// Client doesn't know what he wants and neither do we.
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
