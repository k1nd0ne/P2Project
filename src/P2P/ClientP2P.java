package P2P;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
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
import java.util.Scanner;

/**
 * The ClientP2P class is used to connect to the master repository and make
 * actions. Such as retrive a file, print available files and help. Then if a
 * get is intiated, the the client_transfer threads are triggered.
 * 
 * @author k1nd0ne
 *
 */
public class ClientP2P {

	private Socket sock;
	private String username;
	private String password;
	private boolean sessionStatus;
	private String resp;
	private ArrayList<String> directory;

	/**
	 * The ClientP2P constructor.
	 * 
	 * @param username
	 * @param password
	 */
	public ClientP2P(String username, String password) {
		this.username = username;
		this.password = password;
		this.sessionStatus = false;
		this.directory = null;
	}

	/**
	 * Send a message to the remote masterRepository.
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

	}

	/**
	 * Send a message to a specific socket.
	 * 
	 * @param message
	 * @param sock
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
	 * Receive message from the master Repo
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
			System.out.println("Client-PI : Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Client's routine. authenticate, connects, wait for instructions.
	 * 
	 * @throws InterruptedException
	 */
	public void start() throws InterruptedException {

		/*********************************************************************/
		/* CONNECTION AN AUTHENTICATION PART */
		/*********************************************************************/
		try {
			this.sock = new Socket("127.0.0.1", 2121); // For now the server address is not a variable.
		} catch (IOException e) {
			System.out.println("ClientP2P : Erreur lors de la création du socket Client : " + e.getMessage());
		}

		SendMessage("USER:" + this.username + ":PASS:" + this.password);
		this.resp = ReceiveMessage(this.sock);
		if (resp.contentEquals("230")) {
			System.out.println("230 User logged in, proceed.");
			this.sessionStatus = true;
		} else {
			System.out.println("530 Authentication failure.");
			return;
		}

		/*********************************************************************/
		/* MAIN LOOP */
		/*********************************************************************/
		System.out.println("Type 'help' to see disponible commands.");
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		String command = "";
		while (sessionStatus == true) {
			System.out.print(username + ">");
			command = myObj.nextLine(); // Read user input

			if (command.contentEquals("")) {

			}

			else if (command.equals("help")) {
				System.out.println("-----------Available commands--------");
				System.out.println("help : show this section.");
				System.out.println("dir : show db content");
				System.out.println("list : show available servers");
				System.out.println("get [filename] : retrive the file named 'filename' ");
				System.out.println("exit : leave.");
			}

			/*********************************************************************/
			/* DATABASE CONTENT REQUEST */
			/*********************************************************************/
			else if (command.equals("dir")) {
				SendMessage("LS:");
				this.resp = ReceiveMessage(sock);
				if (this.resp.equals("300")) {
					System.out.println("Waiting for database...");
					String dirRes[] = ReceiveMessage(sock).split(":");
					System.out.println("----------------------------_AVAILABLE FILES_---------------------------");
					for (String line : dirRes) {
						System.out.println(line);
					}
				} else {
					System.out.println("Can't find database, please try later.");
				}

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
					String path = "./src/loot/";
					
					//Threads creation.
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
										Integer.parseInt(infoTemp[2]));
								ThreadList.add(t);
								size++;
							}

						}
						//Threads execution
						for (Client_Transfer t : ThreadList) {
							t.start();
						}
						for (Client_Transfer t : ThreadList) {
							if (t.isAlive()) {
								t.join();
							}
						}
						System.out.println("Transfer Finished. Reforming file...");
						
						//Reforming the file.
						int i = 1;
						String tempPath = "./src/loot/" + file + ".part" + i;
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
							//System.out.println("Reading " + tempPath);
							OutputStream fileOutputStream = new FileOutputStream("./src/loot/" + file, true);
							InputStream fileInputStream = new FileInputStream(tempPath);
							int byteRead;
							while ((byteRead = fileInputStream.read(buffer)) > 0) {
								fileOutputStream.write(buffer, 0, byteRead);
							}
							fileOutputStream.close();
							fileInputStream.close();
							f = new File(tempPath);
							f.delete();
							i++;
							tempPath = "./src/loot/" + file + ".part" + i;
						}
						System.out.println("File downloaded. Check FTP-P2P/src/loot");

					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("Wrong command.");
				}
			}

			else if (command.equals("exit")) {
				this.sessionStatus = false;
				System.out.println("Bye.");
				System.exit(0);
			}
		}
	}

}
