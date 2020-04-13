package P2P_WEB;

import java.io.Serializable;
/**
 * Register Class, contains one server information about one part of one file. 
 * This object is used to be stored in an arrayList an then send to the remote client.
 * Class is easy to understand without any more comments
 * @author k1nd0ne
 *
 */
public class Register implements Serializable{

	private String filename;  
	private String server;
	private int port;
	
	public Register() {
		this.filename = "";
		this.server = "";
		this.port = 0;
	}
	/**
	 * Set the filename
	 * @param fn
	 */
	public void setfileName(String fn) {
		this.filename = fn;
		
	}
	
	/**
	 * Set the server
	 * @param s
	 */
	public void setServer(String s) {
		this.server = s;
	}
	
	/*
	 * Set the port no
	 */
	public void setPort(int p) {
		this.port = p;
	}
	
	/**
	 * Getter for the filename
	 * @return
	 */
	public String getFileName() {
		return this.filename;
	}
	/**
	 * getter for server name 
	 * @return
	 */
	public String getServer() {
		return this.server;
	}
	/**
	 * getter for port
	 * @return
	 */
	public int getPort() {
		return this.port;
	}
	/*
	 * print to check things
	 */
	public void print() {
		System.out.println(server + ":" + port);
	}
}
