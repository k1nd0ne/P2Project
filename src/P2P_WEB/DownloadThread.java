package P2P_WEB;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
/**
 * This is the download Thread class.
 * Each file part download is done in one Thread (Download_Thread).
 * There is a limit of 11 parts per file shared so there is at most 11 thread launch per download. 
 * @author k1nd0ne
 *
 */
public class DownloadThread extends Thread {
	private String filename;
	private ArrayList<String> directory; 
	private JButton connectButton;
	private Socket sock; 
	private JProgressBar progressBar_1;
	private JButton btndownload;
	private JLabel lblDownloadInProgress; 
	private JList shared; 
	/**
	 * Send a message to the remote sock
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
	}

	/**
	 * This constructor got some of the client's UI components (progress bar, informations...)
	 * @param filename
	 * @param sock
	 * @param connectedButton
	 * @param progressBar
	 * @param dlb
	 * @param lblDIP
	 * @param shared
	 */
	public DownloadThread(String filename,Socket sock,JButton connectedButton,JProgressBar progressBar,JButton dlb,JLabel lblDIP,JList shared) {
		this.filename = filename;
		this.connectButton = connectedButton; 
		this.sock = sock;
		this.progressBar_1 = progressBar; 
		this.progressBar_1.setVisible(true);
		this.btndownload = dlb;
		this.lblDownloadInProgress = lblDIP;
		this.shared = shared; 
	}
	
	/**
	 * This is the download method. 
	 * It is getting the list of host that have the file we request. 
	 * Then, it is launch threads for each parts
	 * Next,it is reforming the file.
	 * Finally, the file parts downloaded are shared and shared file list is refreshed.
	 * @param file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private synchronized void download(String file) throws IOException, ClassNotFoundException, InterruptedException {
		lblDownloadInProgress.setVisible(true);
		String request = "GET:" + file;
		String[] infoTemp;
		SendMessage(request,sock);
			String path = "./src/loot/parts/";

			// Threads creation. (Reminder : 11 Thread at most ! All files are splitted in 11 parts at most.
			ArrayList<Client_Transfer> ThreadList = new ArrayList<Client_Transfer>();
				ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
				this.directory = (ArrayList<String>) is.readObject();
				if(this.directory.isEmpty()) {
					connectButton.setText("Error no peer available.");
					connectButton.setForeground(Color.RED);
					return;
				}
				int size = 0;
				String message = "";
				int count = 0;
				
				
				
				/*Thread Launch, downloading each parts*/
				for (String line : this.directory) {
					infoTemp = line.split(":");
					if (!new File(path + infoTemp[1]).exists()) {
						Client_Transfer t = new Client_Transfer(infoTemp[0], infoTemp[1],
								Integer.parseInt(infoTemp[2]),this.progressBar_1,this.directory.size());
						ThreadList.add(t);
						size++;
					}

				}
				// Threads execution
				for (Client_Transfer t : ThreadList) {
					t.start();
				}
				
				//We wait until all the threads are finished before reforming the file.
				for (Client_Transfer t : ThreadList) {
					if (t.isAlive()) {
						try {
							t.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			
				// Transfer Finished. Reforming files and moving the parts to the directory that is used to share the files.
				File f = new File("./src/loot/" + file);
				if(f.exists()) {
					f.delete();
				}
				
				int i = 1;
				String tempPath = "./src/loot/" + file + ".part" + i;
				System.out.println(tempPath);
				
				int total = 0;
				byte[] buffer = new byte[4 * 1024];
				while (true) {
					try {
					OutputStream fileOutputStream = new FileOutputStream("./src/loot/" + file, true);
					InputStream fileInputStream = new FileInputStream(tempPath);
					int byteRead;
					while ((byteRead = fileInputStream.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, byteRead);
						System.out.println("writing " + file);
						
					}

					new File(tempPath).renameTo(new File("./src/loot/parts/" + file + ".part"+ i)); 
					System.out.println("Removing "+tempPath);
					i++;
					tempPath = "./src/loot/" + file + ".part" + i;
					}
					catch(FileNotFoundException e) {
						break;
					}
					
				}
				
	}
	
	//This what happens behind/front of the scene when the user click on the download button 
	public synchronized void run() {
		btndownload.setVisible(false); //Disable the Download button
		try {
			download(filename);
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Reset the visual components after download.
		btndownload.setVisible(true); 
		progressBar_1.setValue(0);
		progressBar_1.setVisible(false);
		lblDownloadInProgress.setVisible(false);
		String sharedfile = getFileList();
		((DefaultListModel) shared.getModel()).removeAllElements();
		for (String f : sharedfile.split(":")) {
			((DefaultListModel) shared.getModel()).addElement("-" + f);
		}
		
	}
	

	/**
	 * Get the ls return (without directories).
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileList() {
		String res = "";
		try {
			String lscmd = "find ./src/loot -maxdepth 1 -type f -not -name 't_*'";
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
}
