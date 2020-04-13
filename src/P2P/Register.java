package P2P;

import java.io.Serializable;
/**
 * Register Class, contains one server info.
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
	
	public void setfileName(String fn) {
		this.filename = fn;
		
	}
	public void setServer(String s) {
		this.server = s;
	}
	
	public void setPort(int p) {
		this.port = p;
	}
	
	public String getFileName() {
		return this.filename;
	}
	public String getServer() {
		return this.server;
	}
	
	public int getPort() {
		return this.port;
	}
}
