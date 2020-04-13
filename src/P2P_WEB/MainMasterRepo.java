package P2P_WEB;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * The main Master repository.
 * Create the socket and wait for a client. Then handle him in a new thread.
 * @author k1nd0ne
 *
 */
public class MainMasterRepo {
	
	
	public static void main(String[] args) throws IOException {
		/*Creating unique objects */
		WebInteraction WI = new WebInteraction(); //To share the master repository info with the web portal
		RestrainDirectory restrain = new RestrainDirectory(); //The restrain directory contains the number of file downloaded by clients to help with the penalities
		Directory dir = new Directory(); //The Directory that contains all the informations on shared files.
		
		
		ServerSocket socket = new ServerSocket(2121); // we define our own FTP port to not interfere with the real one.
		
		/*Master repository main loop */
		while (true) {
			System.out.println("[Master Repository] Server is up and running.");
			Socket sockService;
			sockService = socket.accept();
			MasterRepo handler = new MasterRepo(sockService, dir,WI,restrain);
			System.out.println("[Master Repository] Client connected [" + sockService.getInetAddress() + ":"
					+ sockService.getPort()+"]");
			handler.start();
		}

	}

}