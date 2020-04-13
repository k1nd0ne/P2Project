package P2P_V2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * The directory store the content of the disponible file and give the associated client which as it.
 * @author k1nd0ne
 *
 */
public class Directory implements Serializable {
	private HashMap<String, ArrayList<String>> directory;
	private ArrayList<String> fileList;

	/**
	 * Constructor, init hashmap and arrays. 
	 */
	public Directory() {
		directory = new HashMap<String, ArrayList<String>>();
		this.fileList = new ArrayList<String>();
	}

	
	/**
	 * Add a a client to the List. (See getServersFromFileName)
	 * @param file
	 */
	public void addToFileList(String file) {
		if (!fileList.contains(file)) {
			fileList.add(file);
		}
	}

	/**
	 * Return the list of servers that have a particular file. (See getServersFromFileName)
	 * @return
	 */
	public ArrayList<String> getFileList() {
		return this.fileList;
	}

	/**
	 * Add a server for the file block name.
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
			String[] parts = nameAndBlock.split(Pattern.quote("."));
			addToFileList(parts[0]+"."+parts[1]);
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
	 * Check if a client exist
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
	 * Print the hashmap -> The directory.
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
	 * Get All the Client that have the filename into a list and return it.
	 * @param filename
	 * @return
	 */
	public ArrayList<String> getServersFromFileName(String filename) {
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		for (Map.Entry direntry : directory.entrySet()) {
			//System.out.println(direntry.getKey() + "->");
			if (direntry.getKey().toString().startsWith(filename)) {
				for (String line : (ArrayList<String>) direntry.getValue()) {
					if (!res.contains(line)) {
						res.add(direntry.getKey().toString() + ":" + line);
					}
				}
			}

		}
		return res;
	}
	
	/**
	 * Remove the client from the directory.
	 * @param serverAndPortn
	 */
	public void removeServer(String serverAndPortn) {
			System.out.println(serverAndPortn + " AS TO BE REMOVED");
			Collection<ArrayList<String>> sls = this.directory.values();
			for (ArrayList<String> patate : sls) {
				if(patate.contains(serverAndPortn)) {
					patate.remove(serverAndPortn);
				}	
			}
			
		
	}

}
