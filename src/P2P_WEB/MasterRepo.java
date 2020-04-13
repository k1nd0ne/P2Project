package P2P_WEB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author k1nd0ne
 *
 *         The master Repo thread. Handle a client. It wait for his
 *         instructions. It also update the directory informations. If a client
 *         register to him -> Update the directory. If a client leave -> Update
 *         too. Getting a file -> send him the list of servers that have this
 *         file. (not all the directory !) The master send also available files
 *         from the directory to the client.
 */
public class MasterRepo extends Thread {
	private Directory directory;
	private RestrainDirectory restrain;
	private String message;
	private String path, port;
	private Socket sockService;
	private String timeStamp;
	private WebInteraction WI;

	/**
	 * Constructor. The main master repository give a copy of it's directory, web
	 * manager, and restrain directory (no duplicate).
	 * 
	 * @param sockService
	 * @param directory
	 * @param WI
	 * @param restrainDir
	 * @throws IOException
	 */
	public MasterRepo(Socket sockService, Directory directory, WebInteraction WI, RestrainDirectory restrainDir)
			throws IOException {
		this.message = "";
		this.sockService = sockService;
		this.restrain = restrainDir;
		this.directory = directory;
		this.WI = WI;
		this.WI.updateGraphic(new SimpleDateFormat("yyyy/MM/dd/HH/mm").format(Calendar.getInstance().getTime()).toString());
	}

	/**
	 * Receive a message from the connected client
	 * 
	 * @param sockService
	 * @return received message.
	 */
	public String ReceiveMessage(Socket sockService) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sockService.getInputStream()));
			// Lit une ligne de caractères depuix le flux, et donc la reçoit du client
			String messageRcv = reader.readLine();
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Send message to a client.
	 * 
	 * @param sockService
	 * @param message
	 */
	public void SendMessage(Socket sockService, String message) {
		try {
			PrintStream pStream = new PrintStream(sockService.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Authentication method. Used to check if the user has an account.
	 * 
	 * @param message
	 * @return true if user and pass are correct using the check method.
	 * @throws IOException
	 */
	public boolean Authentifiate(String message) throws IOException {
		if (message.contains("USER") && message.contains("PASS")) {
			try {
				String info[] = message.split(":");
				String user = info[1];
				String pass = info[3];
				this.timeStamp = new SimpleDateFormat("yyyy/MM/dd/HH:mm").format(Calendar.getInstance().getTime());
				System.out
						.println("--" + timeStamp + "--" + "[Master Repository] " + user + " attempt to authenticate.");
				// System.out.println("Server-PI : Pass => " + pass);
				return Check(user, pass);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * This Check function is used in the upper methods to validate authentication
	 * with the passwd.txt file.
	 * 
	 * @param user
	 * @param pass
	 * @return boolean, true if creds are correct.
	 * @throws IOException
	 */
	public boolean Check(String user, String pass) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("./src/FTP/passwd.txt"));
		try {
			String line = br.readLine();
			while (line != null) {
				String info[] = line.split(":");
				if (user.equals(info[0])) {
					if (pass.equals(info[1])) {
						return true;
					}
				}
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		return false;
	}

	/**
	 * This is the main threat routine engaged for every client of the master repo.
	 */
	public void run() {
		/********************************************************/
		/* AUTHENTICATION PHASE */
		/********************************************************/
		boolean sessionStatus = true;

		message = ReceiveMessage(sockService);
		if (message != null) {
			try {
				sessionStatus = Authentifiate(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		} else {
			SendMessage(sockService, "400"); // Sending error : Syntax
		}
		this.timeStamp = new SimpleDateFormat("yyyy/MM/dd/HH:mm").format(Calendar.getInstance().getTime());
		if (sessionStatus == false) {

			System.out.println("--" + timeStamp + "--" + "[Master Repository] Could not authentifiate"
					+ sockService.getInetAddress() + ":" + (sockService.getPort() + 1));
			SendMessage(sockService, "530"); // Sending error : Authentication failure.
		} else {
			System.out.println("--" + timeStamp + "--" + "[Master Repository] Authentication success for "
					+ sockService.getInetAddress() + ":" + (sockService.getPort() + 1));
			SendMessage(sockService, "230"); // Sending success : authentication ok.

			/* Update the Web interface with new client. */
			try {
				this.WI.addClient();
				this.WI.WriteClientCount();
			} catch (IOException e) {

			}

		}

		/********************************************************/
		/* MESSAGE HANDLING PHASE */
		/********************************************************/
		while (sessionStatus) {
			this.timeStamp = new SimpleDateFormat("yyyy/MM/dd/HH:mm").format(Calendar.getInstance().getTime());

			/* Wait for a message */

			this.message = ReceiveMessage(sockService);

			/* The client is leaving */
			if (message == null) {

				/* First we update the web UI */
				try {
					this.WI.rmClient();
					this.WI.WriteClientCount();
					try {
						this.WI.updateGraphic(new SimpleDateFormat("yyyy/MM/dd/HH/mm").format(Calendar.getInstance().getTime()).toString());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}

				/* Then we remove the client form the directory */
				System.out.println("--" + timeStamp + "--" + "[Master Repository] " + sockService.getInetAddress() + ":"
						+ (sockService.getPort() + 1) + " disconnected.");
				int fileCount = directory.removeServer(
						sockService.getInetAddress().toString().substring(1) + ":" + (sockService.getPort() + 1));

				/* Next, we update the number of files shared on the web UI */
				this.WI.removeFileCount(fileCount);
				try {

					this.WI.WriteFileCount();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				/* Finally, cut the while loop */
				sessionStatus = false;
			}

			/* A client want to register a file he his sharing */
			else if (message.startsWith("REGISTER:")) {
				System.out.println("--" + timeStamp + "--" + "[Master Repository] " + sockService.getInetAddress() + ":"
						+ sockService.getPort() + " is writting to the directory.");

				/* We open the discussion and wait for a list of file to be registered */
				try {
					ObjectInputStream is = new ObjectInputStream(sockService.getInputStream());
					ArrayList<Register> reglist = (ArrayList<Register>) is.readObject();
					int fc = 0;
					for (Register reg : reglist) {
						fc = directory.addServer(sockService.getInetAddress().toString().substring(1),
								Integer.toString(reg.getPort()), reg.getFileName());
						this.WI.AddFileCount(fc); // Updating the Web UI for file count.
					}
					this.WI.WriteFileCount(); // Updating the Web UI for file count.
					ArrayList<String> fileList = directory.getFileList();
					this.WI.WriteDBContent(fileList); // Updating the Web UI for file shared.
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

				/*
				 * If the client nerver showed up before we add it to the restriction directory
				 */
				restrain.createClient(sockService.getInetAddress().toString().substring(1));

				/* A client was sharing a file and don't share it anymore */
			} else if (message.startsWith("STOPSHARE:")) {
				try {
					String filename = message.split(":")[1];
					int fileCount = this.directory.removeFileForServer(filename,
							sockService.getInetAddress().toString().substring(1) + ":" + (sockService.getPort() + 1));
					this.WI.removeFileCount(fileCount); //Update Web UI
				} catch (Exception e) {
					e.printStackTrace();
				}

				/* A client want to download a file so he need to know who got it */
			} else if (message.startsWith("GET:")) {
				try {
					String filename = message.split(":")[1];
					ArrayList<String> serverList = directory.getServersFromFileName(filename);
					ObjectOutputStream os = new ObjectOutputStream(sockService.getOutputStream());
					os.writeObject(serverList);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			/* A client wan't to see the content of the database : what is shared ? */
			else if (message.startsWith("LS:")) {
				ArrayList<String> fileList = directory.getFileList();
				String fileListToSend = "";
				for (String file : fileList) {
					fileListToSend += file + ":";
				}
				SendMessage(sockService, fileListToSend);
			}

			/*
			 * A client is sharing a file to an other one, he is asking the master to know
			 * if there is a penality to apply.
			 */
			else if (message.startsWith("INFO:")) {

				Integer nbFile = Integer.parseInt(message.split(":")[1]);
				String client = sockService.getInetAddress().toString().substring(1);

				System.out.println("--" + timeStamp + "--" + "[Master Repository] " + sockService.getInetAddress() + ":"
						+ sockService.getPort() + " policy update for " + client + " + " + nbFile + " file downloaded");

				restrain.createClient(client);
				restrain.addFile(client, nbFile);

				Integer numberFileShared = this.directory.getNumberFileShared(client);
				Integer numberFileDownload = restrain.getFileUploaded(client);

				/*
				 * The penality is applied if a client download 2 times the total number of file
				 * he is sharing
				 */
				if (numberFileDownload > 2 * numberFileShared) {
					SendMessage(sockService, "5000"); // 500 millisecond ban between each block sent.
					System.out.println("--" + timeStamp + "--" + "[Master Repository] " + sockService.getInetAddress()
							+ ":" + sockService.getPort() + " policy update for " + client
							+ " -> download penality applied");
				} else {
					SendMessage(sockService, "0"); // no ban client is respecting our policy.
				}
			}

			/* Client doesn't know what he wants and neither do we. */
			else {
				SendMessage(sockService, "500"); // Syntax error
			}
		}

		// END WHILE//

		try {
			sockService.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
