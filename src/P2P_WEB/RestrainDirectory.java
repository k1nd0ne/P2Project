package P2P_WEB;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a directory storing the number of bytes a client is downloading.
 * @author k1nd0ne
 *
 */
public class RestrainDirectory {
	private HashMap<String, Integer> directory;
	
	
	public RestrainDirectory() {
		this.directory = new HashMap<String, Integer>();
	}
	
	public void addFile(String client,Integer nbFile) {
		Integer res = nbFile + this.directory.get(client);
		this.directory.put(client,res);
	}
	
	public void createClient(String client) {
		if(!this.directory.containsKey(client)) {
		this.directory.put(client, new Integer(0));
		}
	}
	
	public Integer getFileUploaded(String client) {
		return this.directory.get(client);
	}
}
