package P2P_WEB;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JProgressBar;
/**
 * The client transfer class handle ONE file transfer from one remote client to our filesystem.
 * @author k1nd0ne
 *
 */
public class TransferThread extends Thread {
	private Socket sock;
	private String filename;
	private String server;
	private int port;
	private String lootPath;
	private JProgressBar Dprogress;
	private int progressActu;
	
	
	/**
	 * Client_Transfer constructor, take the distributed repository information and filename that it wants to retrieve from it.
	 * Also update the download progress bar.
	 * @param filename
	 * @param server
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TransferThread(String filename, String server, int port,JProgressBar progress, int size) throws UnknownHostException, IOException {
		this.sock = new Socket(server,port);
		this.filename = filename;
		this.server = server;
		this.port = port;
		this.lootPath = "./src/loot";
		this.Dprogress = progress;
		this.progressActu = 100/size; 
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
	 * The run method. It download the given filename.
	 */
	public synchronized void run() {
		
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
			System.out.println("Client socket creation error " + e.getMessage());
		}
		
		this.Dprogress.setValue(Dprogress.getValue()+ progressActu);
		try {
			this.sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
