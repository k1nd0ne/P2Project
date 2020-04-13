package FTP;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerDTP{
	
	private Socket client;
	private DataOutputStream outputStream;
	
	/*
	 * Class Constructor : Connect to the remote ClientDTP server.
	 */
	public ServerDTP(InetAddress hostname,String port) throws NumberFormatException, UnknownHostException, IOException {
		this.client = new Socket(hostname,Integer.parseInt(port));
		System.out.println("Server-DTP : Connection to "+ hostname);
		
	}
	/**
	 * Send file to the clientDTP
	 * @param filename
	 * @throws IOException
	 */
	public void SendFile(String filename) throws IOException {
		System.out.println("Sending file : "+filename);
		InputStream fileInputStream = new FileInputStream(filename);
        byte[] buffer = new byte[16 * 1024];
        int byteRead;
        while((byteRead = fileInputStream.read(buffer))>0) {
        	this.outputStream.write(buffer,0,byteRead);
        }
	}
	
}
