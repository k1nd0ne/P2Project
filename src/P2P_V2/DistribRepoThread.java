package P2P_V2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;

/**
 * The Distributed repo Thread is the server part of the client.
 * It pull informations about the master repo. 
 * And register to it.
 * @author k1nd0ne
 *
 */
public class DistribRepoThread extends Thread{
	
	/**
	 * Send a message to the remote serverPI
	 * 
	 * @param message
	 */
	public void SendMessage(String message, Socket sock) {
		PrintStream pStream;
		try {
			pStream = new PrintStream(sock.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// écrit une ligne de caractères sur le flux, et donc l’envoie au client choisie

	}
	
	
	

	
	
	private String request;
	private ArrayList<Register> regList; 
	private ArrayList<String> directory;
	private String dbPath;
	private Socket RepoSock; 
	private Display display;
	private Socket sockService;
	private int clientSocketPort;
	
	/**
	 * Constructor
	 * @param dbPath
	 * @param sock
	 * @param display
	 * @param ClientSockPort
	 */
	public DistribRepoThread(String dbPath,Socket sock,Display display,int ClientSockPort) {
		this.dbPath = dbPath+"/parts/";
		this.RepoSock = sock;
		this.regList = new ArrayList<Register>();
		this.directory = new ArrayList<String>();
		this.display = display;
		this.clientSocketPort = ClientSockPort; 
	}
	
	/**
	 * Main Loop.
	 * Create the server socket to send files to other clients.
	 * Also regsiter to the Master Repository every 60 seconds. If a new file is downloaded. It will be split into parts and shared.
	 */
	public void run() {
		try {
			ServerSocket socket = new ServerSocket(this.clientSocketPort+1);
			
			Timer timer = new Timer();
			timer.schedule(new RefreshRegistration(dbPath, socket, regList,RepoSock), 0, 60000);
			
			display.DisplayStatus();
			display.clientListDisplay();
			while (true) {	
				
				sockService = socket.accept();
				display.addClient(sockService.getInetAddress() + ":" + sockService.getPort());
				synchronized (display) {
					display.clientListDisplay();
				}
				
				DistributedRepo handler = new DistributedRepo(sockService,dbPath,display);
				handler.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERREUR : "+e.getMessage());
		} // we define our own FTP port to not interfere with the real one if it's running.
	}
}
