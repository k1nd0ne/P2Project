package P2P_V2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author k1nd0ne The client class is mixing up the Distributed and ClientP2P
 *         Class into one.
 */
public class Client extends Thread {

	private String request;
	private ArrayList<Register> regList;
	private ArrayList<String> directory;
	private Socket sock;
	private String username;
	private String password;
	private String resp;
	private String dbPath;
	private Display display;
	private boolean sessionStatus;

	/**
	 * Client constructor.
	 * 
	 * @param username
	 * @param password
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client(String username, String password) throws UnknownHostException, IOException {
		String addr = "";
		Scanner myObj = new Scanner(System.in);
		System.out.println("Enter the repository IP [default : 127.0.0.1] : ");
		addr = myObj.nextLine();
		if(!addr.matches("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")) {
			addr = "127.0.0.1";
			System.out.println("Using default");
		}
		else {
			System.out.println("Using ip : " + addr);
		}
		this.sock = new Socket(addr, 2121);
		this.username = username;
		this.password = password;
		this.resp = "";
		this.regList = new ArrayList<Register>();
		this.directory = new ArrayList<String>();
		this.display = new Display();
		this.sessionStatus = false;
	}

	/**
	 * Send a message to the remote serverPI
	 * 
	 * @param message
	 */
	public void SendMessage(String message) {
		PrintStream pStream;
		try {
			pStream = new PrintStream(sock.getOutputStream());
			pStream.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// écrit une ligne de caractères sur le flux, et donc l’envoie au client

	}

	/**
	 * Send a message to the remote serverPI
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

	/**
	 * Receive message from server
	 * 
	 * @param sockService
	 * @return
	 */
	public String ReceiveMessage(Socket sockService) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(sockService.getInputStream()));
			// Lit une ligne de caractères depuix le flux, et donc la reçoit du client
			String messageRcv = reader.readLine();
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Client: Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * The startClient method launch the client preconfiguration and main loop.
	 * 
	 * @throws InterruptedException
	 */
	public void startClient() throws InterruptedException {

		/********************************************************/
		/* AUTHENTICATION TO THE MASTER REPOSITORY */
		/********************************************************/
		this.sleep(1000);
		display.DisplayStatus();
		SendMessage("USER:" + this.username + ":PASS:" + this.password);
		this.resp = ReceiveMessage(this.sock);
		if (resp.contentEquals("230")) {
			System.out.println("230 User logged in, proceed.");
			display.validate(1);
			this.sessionStatus = true;
		} else {
			System.out.println("530 Authentication failure.");
			this.display.deny(1);
			sleep(1000);
			this.display.DisplayStatus();
			return;

		}

		// Setting up download directory.
		this.display.DisplayStatus();
		this.sleep(1000);
		this.dbPath = "./src/loot";

		if (!new File(this.dbPath).exists()) {
			display.deny(2);
			this.sleep(1000);
			display.DisplayStatus();
			return;
		}

		display.validate(2);
		sleep(1000);
		display.DisplayStatus();

		/********************************************************/
		/* SERVER LAUNCH AND REGISTRATION TO THE DIRECTORY SERV */
		/********************************************************/
		try {
			DistribRepoThread distrib = new DistribRepoThread(this.dbPath, this.sock, this.display,
					this.sock.getLocalPort());
			distrib.start();
			display.validate(4);
			sleep(1000);
		} catch (Exception e) {
			display.deny(4);
			System.out.println(e.getMessage());
		}

		/*********************************************************************/
		/* MAIN CLIENT LOOP */
		/*********************************************************************/
		System.out.println("Type 'help' to see disponible commands.");
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		String command = "";
		while (sessionStatus == true) {

			display.clientListDisplay();

			command = myObj.nextLine(); // Read user input

			if (command.contentEquals("")) {

			}

			else if (command.equals("help")) {
				display.addToDisplay("-----------Available commands--------");
				display.addToDisplay("help : show this section.");
				display.addToDisplay("dir : show db content");
				display.addToDisplay("get [filename] : retrive the file named 'filename' ");
				display.addToDisplay("clear : clean the console");
				display.addToDisplay("exit : leave.");
				display.clientListDisplay();
			}

			else if (command.equals("clear")) {
				SendMessage("LS:");
				display.clear();
			}

			/*********************************************************************/
			/* DATABASE CONTENT REQUEST */
			/*********************************************************************/
			else if (command.equals("dir")) {
				SendMessage("LS:");
				this.resp = ReceiveMessage(sock);
				if (this.resp.equals("300")) {
					this.resp = "";
					display.clear();
					display.addToDisplay("[-] Waiting for database...");
					display.clientListDisplay();
					sleep(1000);
					String dirRes[] = ReceiveMessage(sock).split(":");
					display.clear();
					display.addToDisplay("[ok] Here is your result : ");
					for (String line : dirRes) {
						display.addToDisplay("-" + line);
					}
					display.addToDisplay("\n");
				} else {
					display.addToDisplay("[error] Can't find database, please try later.");
				}
				display.clientListDisplay();
			} else if (command.equals("list")) {
				System.out.println("nothing yet");
			}
			/*********************************************************************/
			/* GET FILE REQUEST */
			/*********************************************************************/

			else if (command.startsWith("get")) {
				if (command.length() > 3) {
					String file = command.split(" ")[1];
					String request = "GET:" + file;
					String[] infoTemp;
					SendMessage(request);
					this.resp = ReceiveMessage(sock);
					if (!resp.contains("300")) {
						display.addToDisplay("Cannot find your file");
						display.clientListDisplay();
					} else {
						String path = "./src/loot/parts/";

						// Threads creation.
						ArrayList<Client_Transfer> ThreadList = new ArrayList<Client_Transfer>();
						try {
							ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
							this.directory = (ArrayList<String>) is.readObject();
							int size = 0;
							String message = "";
							int count = 0;
							for (String line : this.directory) {
								infoTemp = line.split(":");
								if (!new File(path + infoTemp[1]).exists()) {
									Client_Transfer t = new Client_Transfer(infoTemp[0], infoTemp[1],
											Integer.parseInt(infoTemp[2]), this.display);
									ThreadList.add(t);
									size++;
								}

							}
							this.display.clear();
							// Threads execution
							for (Client_Transfer t : ThreadList) {
								t.start();
							}
							for (Client_Transfer t : ThreadList) {
								if (t.isAlive()) {
									t.join();
								}
							}
							// Transfer Finished. Reforming file
							int i = 1;
							String tempPath = "./src/loot/parts/" + file + ".part" + i;
							System.out.println(tempPath);
							File f = new File("./src/loot/" + file);
							if (f.exists()) {
								f.delete();
							}
							int total = 0;
							byte[] buffer = new byte[4 * 1024];
							while (true) {
								if (!new File(tempPath).exists()) {
									break;
								}
								// System.out.println("Reading " + tempPath);
								OutputStream fileOutputStream = new FileOutputStream("./src/loot/" + file, true);
								InputStream fileInputStream = new FileInputStream(tempPath);
								int byteRead;
								while ((byteRead = fileInputStream.read(buffer)) > 0) {
									fileOutputStream.write(buffer, 0, byteRead);
								}
								fileOutputStream.close();
								fileInputStream.close();
								tempPath = "./src/loot/" + file + ".part" + i;
								f = new File(tempPath);
								f.delete();
								i++;
								tempPath = "./src/loot/parts/" + file + ".part" + i;
							}

						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					display.addToDisplay("Wrong command.");

				}
			}

			else if (command.equals("exit")) {
				this.sessionStatus = false;
				System.out.println("Bye.");
				System.exit(0);
			} else {
				display.addToDisplay("Wrong command.");
				display.clientListDisplay();
			}
		}

	}

	/**
	 * Get the ls return.
	 * 
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

}
