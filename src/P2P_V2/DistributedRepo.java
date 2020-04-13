package P2P_V2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * This class is used as a thread that treat a client. It is use to send a file
 * to hims.
 * This Thread takes the display class in argument to add content information dynamicaly.
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

	public DistributedRepo(Socket sockService, String path, Display display) throws IOException {
		this.socketService = sockService;
		this.path = path;
		this.display = display;
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

	public void SendFile(String filename, Socket socket) throws IOException {
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
		message = ReceiveMessage(this.socketService);
		if (message == null) {
			display.removeHost(socketService.getInetAddress() + ":" + socketService.getPort());
			return;
		}
		if (message.startsWith("RETR:")) {
			this.filename = this.path + "/" + message.split(":")[1];

			if (new File(filename).exists()) {
				try {
					display.addTransfer(
							filename + "-->" + socketService.getInetAddress() + ":" + socketService.getPort());
					display.clientListDisplay();
					SendFile(filename, socketService);
					try {
						sleep(2000);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					display.removeTransfer(
							filename + "-->" + socketService.getInetAddress() + ":" + socketService.getPort());
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		try {
			sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		display.removeHost(socketService.getInetAddress() + ":" + socketService.getPort());
		// System.out.println("THREAD FINISHED");
		try {
			this.socketService.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
