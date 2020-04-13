package P2P;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

public class MainMasterRepo {
/**
 * Split a file into 4K part files.
 * @param filepath
 * @throws IOException
 */
	public static void splitFile(String filepath) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(filepath, "r");
		long numSplits = 10; // from user input, extract it from args
		long sourceSize = raf.length();
		long bytesPerSplit = sourceSize / numSplits;
		long remainingBytes = sourceSize % numSplits;

		int maxReadBufferSize = 4 * 1024; // 4KB
		for (int destIx = 1; destIx <= numSplits; destIx++) {
			BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(filepath+".part" + destIx));
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
			BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(filepath+".part" + (numSplits + 1)));
			readWrite(raf, bw, remainingBytes);
			bw.close();
		}
		raf.close();
	}
/**
 * ReadWrite function for a given file.
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
	
	
	
	public static void main(String[] args) throws IOException {		
		// Server Loop//
		ServerSocket socket = new ServerSocket(2121); // we define our own FTP port to not interfere with the real one.
		Directory dir = new Directory();
		/*
		splitFile("./src/mydb/DM.pdf");
		splitFile("./src/mydb/fichier.rtf");
		splitFile("./src/mydb/image.jpg");
		*/
		// Service Socket Creation and Accept//
		while (true) {
			System.out.println("Master Repository : Listening...");
			Socket sockService;
			sockService = socket.accept();
			MasterRepo handler = new MasterRepo(sockService,dir);
			System.out.println("Master Repository : Starting interaction with " + sockService.getInetAddress() + " : "
					+ sockService.getPort());
			handler.start();
		}

	}

}