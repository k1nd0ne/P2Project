package P2P;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import P2P_V2.Display;

/**
 * This class is used as a thread that treat a client. It is use to send a file
 * to hims.
 * 
 * @author k1nd0ne
 *
 */
public class DistributedRepo extends Thread {
	private String message;
	private Socket socketService;
	private String filename;
	private String path;
	private Display display;

	public DistributedRepo(Socket sockService, String path) throws IOException {
		this.socketService = sockService;
		this.path = path;
	}

	/**
	 * Send message throught the given socket.
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
		// écrit une ligne de caractères sur le flux, et donc l’envoie au client

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
			// Lit une ligne de caractères depuix le flux, et donc la reçoit du client
			String messageRcv = reader.readLine();
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Distributed_repo : Erreur de Receive : " + ioe.getMessage());
		}
		return null;
	}

	public void SendFile(String filename,Socket socket) throws IOException {
		System.out.println("Sending file : " + filename);
		InputStream fileInputStream = new FileInputStream(filename);
		DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
		byte[] buffer = new byte[4 * 1024];
		int byteRead;
		while ((byteRead = fileInputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, byteRead);
		}
		
	}
	/**
	 * Recive a file.
	 */
	public void run() {
		System.out.println("WAITING FOR MESSAGE");
		message = ReceiveMessage(this.socketService);
		System.out.println("OK");
		if (message == null) {
			System.out.println("Bye.");
		}
		if (message.startsWith("RETR:")) {
			System.out.println(message);
			this.filename = this.path + "/" + message.split(":")[1];
			System.out.println("filename : " + filename);
			if (new File(filename).exists()) {
				try {
					SendFile(filename,socketService);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		System.out.println("THREAD FINISHED");
		try {
			this.socketService.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
}
