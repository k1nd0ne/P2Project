package FTP;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;

//TODO : REPONSES FTP : 1 ok 3 pas ok

/* Le SERVER-PI est chargé d'écouter les commandes provenant d'un USER-PI sur le canal de contrôle
 * sur un port donné, d'établir la connexion pour le canal de contrôle, de recevoir sur celui-ci 
 * les commandes FTP de l'USER-PI, d'y répondre et de piloter le SERVER-DTP 
 */
public class ServerPI {

	private String message;
	private ServerSocket socketPI; // This will be the listening socket for the server PI.
	private ServerDTP serverDTP; // The server PI delivers instruction to the server DTP to manage the file
									// transfers.
	private String path, port;

	/**
	 * Class constructor creating the server-PI listener on port 2121
	 */
	public ServerPI() {
		this.message = "";
		this.path = "/"; // Default path.
		try {
			this.socketPI = new ServerSocket(2121); // we define our own FTP port to not interfere with the real one.
		} catch (IOException e) {
			System.err.println("Server-PI : Cannot create socket : " + e.getMessage());
		}
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
			System.out.println(messageRcv);
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

	/*
	 * Launch the Server-PI routine : Listen for a client, accept it and wait for
	 * Authentication,Path, ClientDTP port, and wait for instructions (LS, RETR,
	 * PWD).
	 */
	public void start() throws IOException {
		Socket sockService;
		boolean sessionStatus = false;

		// Server Loop//
		while (true) {

			// Service Socket Creation and Accept//
			System.out.println("Server-PI : Listening...");
			try {
				sockService = socketPI.accept();
			} catch (IOException ioe) {
				System.out.println("Server-PI : Accept error : " + ioe.getMessage());
				break;
			}
			System.out.println("Server-PI : starting interaction with " + sockService.getInetAddress() + " : "
					+ sockService.getPort());

			// TCP Connection successfull//
			String message = ReceiveMessage(sockService);
			if (message != null) {
				sessionStatus = Authentifiate(message);
			} else {
				SendMessage(sockService, "400"); // Sending error : Syntax
			}
			// Check de l'authentification
			if (sessionStatus == false) {
				System.out.println("Server-PI : could not authentifiate" + sockService.getInetAddress());
				SendMessage(sockService, "530"); // Sending error : Authentication failure.
			} else {
				System.out.println("Server-PI : OK");
				SendMessage(sockService, "230"); // Sending success : authentication ok waiting now for the path
			}

			if (sessionStatus == true) {
				// At this point, the client is authentication, but we need to know the PATH so
				// we receive again//
				System.out.println("Waiting for PATH...");
				message = ReceiveMessage(sockService);
				if (message != null) {
					if (message.startsWith("PATH:")) {
						if (!new File(this.path = message.split(":")[1]).exists()) {
							SendMessage(sockService, "550"); // Syntax error : Path unknown
						}
						System.out.println("Server-PI : PATH =>" + this.path);
						SendMessage(sockService, "310"); // Syntax error : Path unknown

					} else {
						SendMessage(sockService, "500"); // Syntax error : No Path = disconnect
						sessionStatus = false;
					}
				}
			}
			// Server-DTP Port number//
			if (sessionStatus == true) {
				message = ReceiveMessage(sockService);
				if (message.contains("PORT:")) {
					this.port = message.split(":")[1];
					Pattern pattern = Pattern.compile(
							"^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$");
					Matcher matcher = pattern.matcher(port);
					if (matcher.find()) {
						sessionStatus = true;
						SendMessage(sockService, "300"); // Sending success :
					} else {
						sessionStatus = false;
						SendMessage(sockService, "500"); // Syntax error : No Port = disconnect
					}
				}
			}
			// Main loop : Listen for Service command (RETR,STOR,QUIT...)
			while (sessionStatus == true) {

				if (message == null) {
					sessionStatus = false;
				}

				message = ReceiveMessage(sockService);

				try {
					// This is the actions taken if we receive the Retrive file instruction
					if (message.startsWith("RETR:")) {
						String filename = message.split(":")[1];
						System.out.println("filename : " + filename);
						filename = this.path + "/" + filename;
						System.out.println("filename : " + filename);
						if (!new File(filename).exists()) {
							SendMessage(sockService, "550"); // Syntax error : Path unknown
						} else {
							serverDTP = new ServerDTP(sockService.getInetAddress(), port);
							serverDTP.SendFile(filename);
						}
					}

					// This is the actions taken if we receive the ls instruction
					if (message.startsWith("LS:")) {
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

					if (message.startsWith("PATH:")) {
						String tempPath = "";
						if (message.length() <= 5) {
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

				} catch (Exception e) {

				}

			}
		}
	}

}
