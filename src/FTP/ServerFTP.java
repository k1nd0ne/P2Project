package FTP;

import java.io.IOException;

public class ServerFTP {
	/**
	 * Main Class for the server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		ServerPI serverPI = new ServerPI();
		try {
			serverPI.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
