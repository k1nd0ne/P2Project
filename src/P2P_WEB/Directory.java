package P2P_WEB;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * The directory store the content of the disponible file and give the
 * associated client which as it.
 * 
 * @author k1nd0ne
 *
 */
public class Directory{
	private HashMap<String, ArrayList<String>> directory;
	private ArrayList<String> fileList;
	private ArrayList<String> tempValues;

	/**
	 * Constructor, init hashmap and arrays.
	 */
	public Directory() {
		directory = new HashMap<String, ArrayList<String>>();
		this.fileList = new ArrayList<String>();
		this.tempValues = new ArrayList<String>();
	}

	/**
	 * Add a a client to the List. (See getServersFromFileName)
	 * 
	 * @param file
	 */
	public void addToFileList(String file) {
		if (!fileList.contains(file)) {
			fileList.add(file);
		}
	}

	/**
	 * Return the list of servers that have a particular file. (See
	 * getServersFromFileName)
	 * 
	 * @return
	 */
	public ArrayList<String> getFileList() {
		return this.fileList;
	}

	/**
	 * Add a server for the file block name.
	 * 
	 * @param server
	 * @param port
	 * @param nameAndBlock
	 */
	public int addServer(String server, String port, String nameAndBlock) {
		int nbFile = 0;
		if (directory.containsKey(nameAndBlock)) {
			Collection<ArrayList<String>> sls = this.directory.values();
			for (ArrayList<String> patate : sls) {
				if (!patate.contains(server + ":" + port)) {
					patate.add(server + ":" + port);
					nbFile++;
				}
			}
		} else {
			String[] parts = nameAndBlock.split(Pattern.quote("."));
			addToFileList(parts[0] + "." + parts[1]);
			directory.put(nameAndBlock, new ArrayList<String>());
			Collection<ArrayList<String>> sls = this.directory.values();
			for (ArrayList<String> patate : sls) {
				if (!patate.contains(server + ":" + port)) {
					patate.add(server + ":" + port);
					nbFile++;
				}
			}
		}

		return nbFile;

	}

	/**
	 * Check if a client exist
	 * 
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
	 * Get All the Client that have the filename into a list and return it. (We take
	 * the client radomly).
	 * 
	 * @param filename
	 * @return
	 */
	public ArrayList<String> getServersFromFileName(String filename) {
		int i = 0;
		Random rand = new Random();

		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		for (Map.Entry direntry : directory.entrySet()) {

			if (direntry.getKey().toString().startsWith(filename)) {

				this.tempValues = (ArrayList<String>) direntry.getValue();
				i = rand.nextInt(tempValues.size());
				String line = tempValues.get(i);
				if (!res.contains(line)) {
					res.add(direntry.getKey().toString() + ":" + line);
				}
			}

		}
		return res;
	}

	/**
	 * Remove the client from the directory.
	 * 
	 * @param serverAndPortn
	 */
	public int removeServer(String serverAndPortn) {
		int nbfile = 0;
		Collection<ArrayList<String>> sls = this.directory.values();
		for (ArrayList<String> patate : sls) {
			if (patate.contains(serverAndPortn)) {
				patate.remove(serverAndPortn);
				nbfile++;
			}
		}
		return nbfile;
	}

	/**
	 * Get the number of files shared by a client
	 * 
	 * @param client
	 * @return Integer
	 */
	public Integer getNumberFileShared(String client) {
		Integer res = 0;
		for (Map.Entry direntry : directory.entrySet()) {
			for (String line : (ArrayList<String>) direntry.getValue()) {
				if (line.contains(client)) {
					res++;
				}
			}
		}
		return res;
	}

	/**
	 * Remove a server from a specific filename key
	 * 
	 * @param filename
	 * @param string
	 * return number of file removed (For the Web UI update) 
	 */
	public int removeFileForServer(String filename, String client) {
		String actualFile;
		int res = 0;
		for (Map.Entry direntry : directory.entrySet()) {
			actualFile = (String) direntry.getKey();
			if (actualFile.contains(filename)) {
				((ArrayList<String>) direntry.getValue()).remove(client);
				res++;
			}
		}
		return res; 
	}
}
