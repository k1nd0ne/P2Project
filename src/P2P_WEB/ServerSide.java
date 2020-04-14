package P2P_WEB;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * The MainServerSide is the server side of the client it is waiting for an
 * other client application to ask for a file. It's interacting with the master
 * repository. It is also handling file registration for the client.
 * 
 * @author k1nd0ne
 *
 */
public class ServerSide extends Thread {
	private String request;
	private ArrayList<Register> regList; // A list of directory entries.
	private ArrayList<String> directory; // A list of servers coming from the directory.
	private String dbPath;
	private Socket RepoSock;
	private Socket sockService;
	private int clientSocketPort;
	private Timer timer;
	private JPanel jp; // To update graphics components (When an user get files from us)
	private String masterAddr; // The master repository address is useful to get information about the penality
								// to apply.
	private int penality;

	/**
	 * Constructor
	 * 
	 * @param dbPath
	 * @param sock
	 * @param display
	 * @param ClientSockPort
	 */
	public ServerSide(String dbPath, Socket sock, int ClientSockPort, JPanel panel, String addr) {
		this.dbPath = dbPath + "/parts/";
		this.RepoSock = sock;
		this.regList = new ArrayList<Register>();
		this.directory = new ArrayList<String>();
		this.clientSocketPort = ClientSockPort;
		this.jp = panel;
		this.masterAddr = addr;
		this.penality = 0;
	}

	/**
	 * Main Loop. Create the server socket to send files to other clients. Also
	 * register to the Master Repository every 60 seconds. If a new file is
	 * downloaded. It will be split into parts and shared.
	 */
	public void run() {
		JList list = new JList<String>(new DefaultListModel<String>());
		list.setBackground(Color.DARK_GRAY);
		list.setBounds(535, 103, 314, 251);
		jp.add(list);
		try {
			ServerSocket socket = new ServerSocket(this.clientSocketPort + 1);

			// This timer refresh the registration every 60 seconds
			this.timer = new Timer();
			timer.schedule(new RefreshRegistration(dbPath, socket, regList, RepoSock), 0, 60000);

			// While the user is not disconnecting we wait for an download event.
			while (!interrupted()) {
				sockService = socket.accept();
				informRatio();
				UploadThread handler = new UploadThread(sockService, dbPath, list, penality);
				handler.start();
			}
			this.timer.purge();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR: " + e.getMessage());

		}
	}

	/**
	 * This method is used to compute the penalty to apply to the client.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void informRatio() throws UnknownHostException, IOException {
		Socket masterSocket = new Socket(masterAddr, 2121);
		SendMessage("USER:" + "user" + ":PASS:" + "password", masterSocket);
		String resp = ReceiveMessage(masterSocket);
		if (resp.contentEquals("230")) {
			SendMessage("INFO:1", masterSocket);
			resp = ReceiveMessage(masterSocket);
			this.penality = Integer.parseInt(resp);
		}
		return;
	}

	/**
	 * Send a message to the remote sock
	 * 
	 * @param message
	 * @param sock
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
	}

	/**
	 * Receive message from a client.
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
			System.out.println("Distributed_repo : Erreur de Receive : " + ioe.getMessage());
		}
		return null;
	}
}
