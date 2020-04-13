package FTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientPI {
	private Socket sock;
	private int port;
	private String username;
	private String password;
	private String path;
	private String resp;
	private ClientDTP clientDTP;
	private boolean sessionStatus;

	/**
	 * Create ClientPI socket using the following parameter.
	 * 
	 * @param username
	 * @param password
	 * @param path
	 */
	ClientPI(String username, String password, String path, int port) {
		this.port = port;
		this.username = username;
		this.password = password;
		this.path = path;
		this.resp = "";
		this.sessionStatus = false;
		try {
			this.sock = new Socket("127.0.0.1", port); // For now the server address is not a variable.
		} catch (IOException e) {
			System.out.println("ClientPI : Erreur lors de la création du socket Client : " + e.getMessage());
		}

	}

	/**
	 * Send a message to the remote serverPI
	 * 
	 * @param message
	 */
	public void SendMessage(String message) {
		PrintStream pStream;
		try {
			pStream = new PrintStream(sock.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// écrit une ligne de caractères sur le flux, et donc l’envoie au client

	}

	/**
	 * Receive message from the serverPI
	 * 
	 * @param sockService
	 * @return
	 */
	public String ReceiveMessage(Socket sockService) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sockService.getInputStream()));
			// Lit une ligne de caractères depuix le flux, et donc la reçoit du client
			String messageRcv = reader.readLine();
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Client-PI : Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Authenticate, send ClientDTP port
	 */
	public void start() {

		// Send authentification information
		SendMessage("USER:" + this.username + ":PASS:" + this.password);
		this.resp = ReceiveMessage(this.sock);
		if (resp.contentEquals("230")) {
			System.out.println("230 User logged in, proceed.\nUsing binary mode for file transfer.");
		} else {
			System.out.println("530 Authentication failure.");
			return;
		}
		// Send Path
		SendMessage("PATH:" + this.path);

		ReceiveMessage(this.sock);
		// Create Client DTP with a random disponible port, and send the port to the FTP
		// server.

		clientDTP = new ClientDTP();
		clientDTP.start();
		SendMessage("PORT:" + clientDTP.getDTPort());
		ReceiveMessage(this.sock);
		this.sessionStatus = true;
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		System.out.println("Type 'help' to see disponible commands.");
		String command = "";
		while (sessionStatus == true) {
			System.out.print("ftp>");
			command = myObj.nextLine(); // Read user input
			gstCommand(command);
		}
	}

	/**
	 * Manage commands entered by the client.
	 * 
	 * @param command
	 */
	public void gstCommand(String command) {
		if (command.equals("help")) {
			System.out.println("ls : The one you know ! Just print the content of the current directory");
			System.out.println("cd : Move between dirs !");
			System.out.println("pwd : Where am I ? ");
			System.out.println("get : Get a file");
			System.out.println("quit : Leave this place forever ! ");
			System.out.println("list : list available server");
		}

		else if (command.equals("pwd")) {
			System.out.println(this.path);
		}

		else if (command.startsWith("get")) {
			try {
				String filename = command.split(" ")[1];
				this.path = this.path + "/" + filename;
				System.out.println("Getting file " + filename + "...");
				clientDTP.setFileName("./src/loot/" + filename);
				SendMessage("RETR:" + filename);
				clientDTP.ReceiveFile();

			} catch (Exception e) {
				System.out.println("Unknown command '" + command + "'.");
			}

		}

		else if (command.startsWith("ls")) {
			String array[] = command.split(" ");
			if (array.length > 1) {
				SendMessage("LS:" + array[1]);
			} else {
				SendMessage("LS:");
			}
			String resp = ReceiveMessage(this.sock);
			if (resp.equals("550")) {
				System.out.println("Requested action not taken. File unavailable (e.g., file not found, no access).");
			} else {
				System.out.println(resp + " Waiting for ls result...");
				String lsResult[] = ReceiveMessage(this.sock).split(":");
				for (String string : lsResult) {
					System.out.print(string + "\t");
				}
				System.out.println("\n");
			}
		}

		else if (command.startsWith("cd")) {
			System.out.println("CD DETECTED");
			String tempPath = "";
			String array[] = command.split(" ");
			if (array.length > 1) {
				SendMessage("PATH:" + array[1]);
				tempPath = array[1];
			} else {
				SendMessage("PATH:~/");
				tempPath = "~/";
			}

			String resp = ReceiveMessage(this.sock);

			if (resp.equals("550")) {
				System.out.println("Requested action not taken. File unavailable (e.g., file not found, no access).");
			} else {
				this.path = tempPath;
			}
		}

		else if (command.equals("quit")) {
			this.sessionStatus = false;
			System.out.println("Bye.");
			System.exit(0);

		}

		else if (command.equals("list")) {
			System.out.println("Here is the Server list : ");
		}

		else {
			System.out.println("Unknown command '" + command + "'.");
		}

	}

}
