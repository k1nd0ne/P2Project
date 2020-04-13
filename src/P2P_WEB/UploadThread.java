package P2P_WEB;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * This class is used as a thread that handle a client. It is use to send a file
 * to hims. 
 * @author k1nd0ne
 *
 */
public class UploadThread extends Thread {
	private String message;
	private Socket socketService;
	private String filename;
	private String path;
	private JList list;
	private int timer;

	public UploadThread(Socket sockService, String path, JList list, int penality) throws IOException {
		this.socketService = sockService;
		this.path = path;
		this.list = list;
		this.timer = penality;
	}

	/**
	 * Send message to the given socket.
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
	 * Receive message from the client.
	 * 
	 * @param sockService
	 * @return
	 */
	public String ReceiveMessage(Socket sockService) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sockService.getInputStream()));
			String messageRcv = reader.readLine();
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Distributed_repo : Erreur de Receive : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Send a file to the remote socket.
	 * Send is done sending multiple block of 4KB. 
	 * @param filename
	 * @param socket
	 * @throws IOException
	 */
	public void SendFile(String filename, Socket socket) throws IOException {
		InputStream fileInputStream = new FileInputStream(filename);
		DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
		byte[] buffer = new byte[4 * 1024];
		int byteRead;
		while ((byteRead = fileInputStream.read(buffer)) > 0) {
			try {
				sleep(this.timer); //We apply the penalty here to regulate the rate.
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			outputStream.write(buffer, 0, byteRead);
		}

	}

	/**
	 * The main purpose of this Thread is to send a file, and display the information to the user on the UI.
	 */
	public synchronized void run() { //The synchronized here is because we update the list in the UI from multiple thread. 
		message = ReceiveMessage(this.socketService);
		if (message == null) {
			return;
		}
		if (message.startsWith("RETR:")) {

			String file = message.split(":")[1];

			this.filename = this.path + "/" + file;

			File f = new File(filename);

			
			if (f.exists()) {
				String elem = "Peer " + socketService.getInetAddress() + " is downloading " + file.split(".part")[0]+ "...";
				if (!((DefaultListModel) list.getModel()).contains(elem)) {
					((DefaultListModel) list.getModel()).addElement(elem); //Update the UI.
				}
				
				try {
					SendFile(filename, socketService);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				((DefaultListModel) list.getModel()).removeElement(elem); //update the UI.
			}
		}

		try {
			this.socketService.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
