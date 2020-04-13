package P2P_V2;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * The main Master repository to launch the MasterRepo Class.
 * Create the socket and wait for a client. Then handle him in a new thread.
 * @author k1nd0ne
 *
 */
public class MainMasterRepo {

	public static void main(String[] args) throws IOException {
		// Server Loop//
		ServerSocket socket = new ServerSocket(2121); // we define our own FTP port to not interfere with the real one.
		Directory dir = new Directory();
		// Service Socket Creation and Accept//
		while (true) {
			System.out.println("[Master Repository] Server is up and running.");
			Socket sockService;
			sockService = socket.accept();
			MasterRepo handler = new MasterRepo(sockService, dir);
			System.out.println("[Master Repository] Client connected [" + sockService.getInetAddress() + ":"
					+ sockService.getPort()+"]");
			handler.start();
		}

	}

}