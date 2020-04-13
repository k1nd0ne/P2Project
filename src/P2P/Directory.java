package P2P;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 * Directory class, bind part of a file to a list of servers.
 * @author k1nd0ne
 *
 */
public class Directory implements Serializable{
	private HashMap<String, ArrayList<String>> directory;
	
	public Directory() {
		directory = new HashMap<String, ArrayList<String>>();
	}
/**
 * Add a server:port to the directory for a filename.
 * @param server
 * @param port
 * @param nameAndBlock
 */
	public void addServer(String server, String port, String nameAndBlock) {
		if (directory.containsKey(nameAndBlock)) {
			Collection<ArrayList<String>> sls = this.directory.values();
			for (ArrayList<String> patate : sls) {
				if (!patate.contains(server + ":" + port)) {
					patate.add(server + ":" + port);
				}
			}
		} else {
			directory.put(nameAndBlock, new ArrayList<String>());
			Collection<ArrayList<String>> sls = this.directory.values();
			
			for (ArrayList<String> patate : sls) {
				if (!patate.contains(server + ":" + port)) {
					patate.add(server + ":" + port);
				}
			}
		}
	}
/**
 * Check if a server:port exist in the directory.
 * @param server
 * @return
 */
	public boolean serverExist(String server) {
		Collection<ArrayList<String>> sls = this.directory.values();
		for (ArrayList<String> patate : sls) {
			if (patate.contains(server))
				return true;
		}
		return false;
	}

	/**
	 * Display the directory content.
	 */
	public void printDirectory() {	
		System.out.println("Listing Directory available files : ");
		for (Map.Entry direntry : directory.entrySet()) {
			System.out.println(direntry.getKey() + "->");
			for (String line : (ArrayList<String>) direntry.getValue()) {
				System.out.println(line);
			}
		}
	}
	/**
	 * Return an arraylist of servers that are in the possession of the filename.
	 * @param filename
	 * @return
	 */
	public ArrayList<String> getServersFromFileName(String filename){
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		for (Map.Entry direntry : directory.entrySet()) {
			System.out.println(direntry.getKey() + "->");
			if(direntry.getKey().toString().startsWith(filename)) {
				for (String line : (ArrayList<String>) direntry.getValue()) {
					if(!res.contains(line)) {
						res.add(direntry.getKey().toString()+":"+line);
					}
				}
			}
			
		}
		return res;
	}
	
}
