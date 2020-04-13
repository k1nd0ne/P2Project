package P2P;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the main distributed repository. It is first annoncing itself to the Master Repository.
 * Secondly it is waiting for client. We suppose that the
 * server as it own path were it store file from the Directory server database.
 * In the futur, the server will also be a client so it will download and update
 * his own directory information every X sec.
 * 
 * @author k1nd0ne
 *
 */
public class MainDistributedRepo {
	public static String getFileList(String path) {
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
		return res+".";
	}
	
	public static void SendMessage(String message,Socket sock) {
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
	public static String ReceiveMessage(Socket sockService) {
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

	public static void main(String[] args) throws UnknownHostException, IOException {
		//VARIABLES DECLARATION : 
		Scanner myScan = new Scanner(System.in); // Create a Scanner object
		ServerSocket socket;
		String request = ""; 
		Socket sock;
		int port = 2121; 
		String username = "server";
		String password = "server";
		String resp = "";
		ArrayList<Register> regList = new ArrayList<Register>();
		
		/********************************************************/
		/* CONFIG PHASE */
		/********************************************************/
		sock = new Socket("localhost", port); // For now the server address is not a variable.
		SendMessage("USER:" + username + ":PASS:" + password,sock);
		resp = ReceiveMessage(sock);
		if (resp.contentEquals("230")) {
			System.out.println("230 User logged in, proceed.\n");
		} else {
			System.out.println("530 Authentication failure.");
			return;
		}
		
		System.out.println("*** P2P Server configuration ***");
		String dbPath = "";
		System.out.print("Please enter your database path : ");
		dbPath = myScan.nextLine(); // Read user input
		
		
		
		/********************************************************/
		/* 	SERVER LAUNCH AND REGISTRATION TO THE DIR SERV 		*/
		/********************************************************/
		try {
			socket = new ServerSocket(0);
			request = "REGISTER:";
			String[] fileList = getFileList(dbPath).split(":");
			for (String filename : fileList) {
				Register reg = new Register();
				reg.setfileName(filename);
				reg.setServer("127.0.0.1");
				reg.setPort(socket.getLocalPort());
				regList.add(reg);
			}
			SendMessage(request, sock);
			ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
			os.writeObject(regList);
			
			while (true) {
				System.out.println("ServerP2P_DIR : Listening...");
				Socket sockService;
				sockService = socket.accept();
				DistributedRepo handler = new DistributedRepo(sockService,dbPath);
				System.out.println("ServerP2P_DIR : Starting interaction with " + sockService.getInetAddress() + " : "
						+ sockService.getPort());
				handler.start();
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERREUR : "+e.getMessage());
		} // we define our own FTP port to not interfere with the real one.

	}

}
