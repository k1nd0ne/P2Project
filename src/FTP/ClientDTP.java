package FTP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientDTP extends Thread{
	private ServerSocket socketDTP; //This will be the listening socket for the server DTP.
	private Socket sockService;
	private int port;
	private String actualName;
	private DataInputStream inputStream;
	
	
	/**
	 * Class Constructor for ClientDTP, create a serversocket with a random disponible port.
	 */
	public ClientDTP() {
		try {
			this.socketDTP = new ServerSocket(0); //we define our own FTP port to not interfere with the real one.
			this.port = socketDTP.getLocalPort();
		} catch (IOException e) { 
			System.err.println("Client-DTPs : Cannot create socket : " + e.getMessage());
		} 
	}
	/**
	 * run methods is used to accept a connection with the serverDTP and Receive a file.
	 */
	public void run(){
		Socket sockService = null;
		try{
			
			sockService = socketDTP.accept();
			this.inputStream = new DataInputStream(sockService.getInputStream());
		}
		catch(IOException ioe) {
			System.out.println("Client-DTP : Accept error : " + ioe.getMessage());
			//break;
		}
		System.out.println("Client-DTP : starting interaction with " + sockService.getInetAddress() + " : " + sockService.getPort());
				try {
					System.out.println("SENDING TRUE");
					ReceiveFile();		
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	
	/**
	 * Set the filename variable.
	 * @param name
	 */
	public void setFileName(String name) {
		this.actualName = name;
	}
	/**
	 * 
	 * @return the ClientDTP port number.
	 */
	public int getDTPort() {
		return this.port;
	}
	
	/**
	 * Receive the file named "actualName"
	 * @throws IOException
	 */
	public void ReceiveFile() throws IOException {
		System.out.println("Receving file "+actualName);
		OutputStream fileOutputStream = new FileOutputStream(actualName);
		 byte[] buffer = new byte[16 * 1024];
	        int byteRead;
	        while((byteRead = this.inputStream.read(buffer))>0){
	        	fileOutputStream.write(buffer,0,byteRead);
	        }
	}

}
