package P2P_WEB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
/**
 * The web interaction class is use to write informations about the 
 * master repository into files to be read later by the JavaScript interpreter.
 * @author k1nd0ne
 *
 */
public class WebInteraction {
	private int clientCount;
	private int fileCount;
	
	
	public WebInteraction() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter("./Web/ServerConsol/clientCount");
		writer.print("Client : " + 0);
		writer.close();
		
		
		writer = new PrintWriter("./Web/ServerConsol/fileCount");
		writer.print("File : " + 0);
		writer.close();
		
		writer = new PrintWriter("./Web/ServerConsol/db");
		writer.print(" ");
		writer.close();
		
		writer = new PrintWriter("./Web/ServerConsol/graph");
		writer.print(" ");
		writer.close();
	}
	/**
	 * Write the db file list for the web interface
	 * @param db
	 * @throws IOException
	 */
	public void WriteDBContent(ArrayList<String> db) throws IOException {
		PrintWriter writer = new PrintWriter("./Web/ServerConsol/db");
		for(String f:db) {
			writer.println(f+"\n");
		}
		writer.close();
	}
	
	/**
	 * Write File Count for the web interface
	 * @param i
	 * @throws IOException
	 */
	public void WriteFileCount() throws IOException {

		PrintWriter writer = new PrintWriter("./Web/ServerConsol/fileCount");
		writer.print("File : " + this.fileCount);
		writer.close();
	}
	
	/**
	 * Write the client count for the WI
	 * @throws IOException
	 */
	public void WriteClientCount() throws IOException {
		PrintWriter writer = new PrintWriter("./Web/ServerConsol/clientCount");
		writer.print("Client : " + this.clientCount);
		writer.close();
	}
	/**
	 * Update the graphics file when a client is connecting
	 * @throws FileNotFoundException 
	 */
	public void updateGraphic(String timestamp) throws FileNotFoundException {
		String[] cutted = timestamp.split("/");
		String line = "{ x: new Date("+ cutted[0] + ","+ cutted[1] + "-1," + cutted[2] + "," + cutted[3] + "," + cutted[4] + ",0), y:"+this.clientCount+ " },";
		PrintWriter writer = new PrintWriter(new FileOutputStream(new File("./Web/ServerConsol/graph"), true));
		writer.append(line);
		writer.close();
	}

	//Useful methods//
	
	public void removeFileCount(int i) {
		this.fileCount -= i;
	}
	
	public void AddFileCount(int i ) {
		this.fileCount += i;
	}
	
	
	public void addClient() {
		this.clientCount++;
	}
	
	public void rmClient() {
		this.clientCount--;
	}
}
