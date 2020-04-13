package P2P_V2;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * The client transfer class handle filetransfer from one Distributed repo to a client.
 * @author k1nd0ne
 *
 */
public class Client_Transfer extends Thread {
	private Socket sock;
	private String filename;
	private String server;
	private int port;
	private String lootPath;
	private Display display;

	/**
	 * Client_Transfer constructor, take the distributed repository information and filename that it wants to retreive from it.
	 * @param filename
	 * @param server
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client_Transfer(String filename, String server, int port,Display disp) throws UnknownHostException, IOException {
		this.sock = new Socket(server,port);
		this.filename = filename;
		this.server = server;
		this.port = port;
		this.lootPath = "./src/loot";
		this.display = disp;
		display.addTransfer(filename + "<----" +server+":"+port);
		display.clientListDisplay();
	}

	/**
	 * Send a message to the remote serverPI
	 * @param message
	 */
	public void SendMessage(String message) {
		PrintStream pStream;
		try {
			pStream = new PrintStream(this.sock.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * The run method. It send the file to the clientP2P.
	 */
	public void run() {
		try {
			SendMessage("RETR:" + filename);
			DataInputStream inputStream = new DataInputStream(this.sock.getInputStream());
			OutputStream fileOutputStream = new FileOutputStream(lootPath+"/"+filename);
			byte[] buffer = new byte[4 * 1024];
			int byteRead;
			while ((byteRead = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, byteRead);
			}
		} catch (IOException e) {
			System.out.println("ClientP2P : Erreur lors de la création du socket Client : " + e.getMessage());
		}
		
		try {
			sleep(2000);
			display.removeTransfer(filename + "<----" +server+":"+port);
			display.clientListDisplay();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
