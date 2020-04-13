package P2P_V2;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimerTask;
/**
 * This class is the registration thread that occur every 60 seconds.
 * @author k1nd0ne
 *
 */
public class RefreshRegistration extends TimerTask{
	/**
	 * Get the ls return.
	 * @param path
	 * @return
	 */
	public static String getFileList(String path) {
		String res = "";
		try {
			String lscmd = "ls " + path;
			Process p = Runtime.getRuntime().exec(lscmd);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				// System.out.println(line);
				res += line + ":";
				line = reader.readLine();
			}
		} catch (IOException e1) {
			System.out.println("Pblm found1.");
		} catch (InterruptedException e2) {
			System.out.println("Pblm found2.");
		}
		return res;
	}
	
	
	/**
	 * Send a message to the remote sock.
	 * 
	 * @param message
	 */
	public void SendMessage(String message, Socket sock) {
		PrintStream pStream;
		try {
			pStream = new PrintStream(sock.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// écrit une ligne de caractères sur le flux, et donc l’envoie au client choisie

	}
	
	private String request;
	private String dbPath;
	private ServerSocket socket;
	private ArrayList<Register> regList;
	private Socket RepoSock;
	public RefreshRegistration(String dbPath,ServerSocket socket,ArrayList<Register> regList,Socket RepoSock) throws IOException {
		this.dbPath = dbPath;
		this.socket = socket; 
		this.regList = regList; 
		this.RepoSock = RepoSock;
		this.request = "REGISTER:";
		
	}


	/**
	 * This is considered as a thread. It is just the register process. 
	 * We Get the loot content. Split the files. And share the informations.
	 */
	public void run() {
		
		//List the file in the DB directory and split them into 4kb part files.
				for(String file : getFileList("./src/loot/").split(":")) {
					if(!file.contains(".part")) {
						try {
							splitFile(file);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}
					}
				}
		SendMessage("REGISTER:", RepoSock);
		String[] fileList = getFileList(dbPath).split(":");
		for (String filename : fileList) {
			if(filename.contains(".part")) {
				Register reg = new Register();
				reg.setfileName(filename);
				reg.setServer("127.0.0.1"); //Temporary the master will bind the address. Why ? Because i can't find a way to get local IP adress...
				reg.setPort(socket.getLocalPort());
				regList.add(reg);
			}
		}
		
		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(RepoSock.getOutputStream());
			os.writeObject(regList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	/**
	 * Split a file into 10 parts of 4KB.
	 * If the file is too large, then, more parts of 4KB.
	 * @param filepath
	 * @throws IOException
	 */
	public static void splitFile(String filepath) throws IOException {
		String fileBak = filepath;
		filepath = "./src/loot/"+filepath;
		File file = new File(filepath);
		if(file.isDirectory() == false) {
		RandomAccessFile raf = new RandomAccessFile(filepath, "r");
		long numSplits = 10; // from user input, extract it from args
		long sourceSize = raf.length();
		long bytesPerSplit = sourceSize / numSplits;
		long remainingBytes = sourceSize % numSplits;

		int maxReadBufferSize = 4 * 1024; // 4KO
		for (int destIx = 1; destIx <= numSplits; destIx++) {
			BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("./src/loot/parts/"+fileBak+".part" + destIx));
			if (bytesPerSplit > maxReadBufferSize) {
				long numReads = bytesPerSplit / maxReadBufferSize;
				long numRemainingRead = bytesPerSplit % maxReadBufferSize;
				for (int i = 0; i < numReads; i++) {
					readWrite(raf, bw, maxReadBufferSize);
				}
				if (numRemainingRead > 0) {
					readWrite(raf, bw, numRemainingRead);
				}
			} else {
				readWrite(raf, bw, bytesPerSplit);
			}
			bw.close();
		}
		if (remainingBytes > 0) {
			BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("./src/loot/parts/"+fileBak+".part" + (numSplits + 1)));
			readWrite(raf, bw, remainingBytes);
			bw.close();
		}
		raf.close();
		}
	}

	/**
	 * Read and write into a file.
	 * @param raf
	 * @param bw
	 * @param numBytes
	 * @throws IOException
	 */
	public static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
		byte[] buf = new byte[(int) numBytes];
		int val = raf.read(buf);
		if (val != -1) {
			bw.write(buf);
		}
	}
}
