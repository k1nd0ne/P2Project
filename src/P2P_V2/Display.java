package P2P_V2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TimerTask;
/**
 * The display class is a thread that display informations dynamicaly. For example, if a client is connected to an other,
 * then it will be added to the client list. Same if one is leaving. It also manage the command prompt display, color managment and step following.
 * @author k1nd0ne
 *
 */
public class Display{
	private String authInfo = "[-] Authentication verification";
	private String setLocalDir = "[-] Setting up download directory";
	private String initRegistering = "[-] Registering to master repository";
	private ArrayList<String> clientList;
	private ArrayList<String> transferList;
	private ArrayList<String> buffer;
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	
	/**
	 * Init all list. 
	 * The client list represent a client connected to us.
	 * The Transfer list represent what we download OR send from/to an other client.
	 * The buffer store any messages and display them when refreshed.
	 */
	public Display() {
		this.clientList = new ArrayList<String>();
		this.transferList = new ArrayList<String>();
		this.buffer = new ArrayList<String>();
		buffer.add("");
	}

	/**
	 * DisplayStatus informations : Step validation.
	 */
	public void DisplayStatus() {

		synchronized (System.out) {
			
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.print(" .oPYo. .oPYo.  .oPYo.    .oPYo.      .oo o   o \n" + 
					" 8    8     `8  8    8    8   `8     .P 8 `b d' \n" + 
					"o8YooP'    oP' o8YooP'   o8YooP'    .P  8  `b'  \n" + 
					" 8      .oP'    8         8   `b   oPooo8   8   \n" + 
					" 8      8'      8         8    8  .P    8   8   \n" + 
					" 8      8ooooo  8         8oooP' .P     8   8   \n" + 
					":..:::::.......:..::::::::......:..:::::..::..::\n" + 
					"::::::::::::::::::::::::::::::::::::::::::::::::\n" + 
					"::::::::::::::::::::::::::::::::::::::::::::::::\n");
			System.out.println(authInfo);
			System.out.println(setLocalDir);
			System.out.println(initRegistering);
			System.out.print("\n");
			System.out.print("\n");
			System.out.print("\n");
		}

	}

	/**
	 * When the user type clear in the command line, the bufferised messages are cleaned.
	 */
	public synchronized void clear() {
		this.buffer.clear();
		this.buffer.add("");
	}

	/**
	 * Simply add a client to the clientList.
	 * @param client
	 */
	public synchronized void addClient(String client) {
		clientList.add(client);
	}
	
	/**
	 * Add a transfer information to the trasnfer list.
	 * @param client
	 */

	public synchronized void addTransfer(String client) {
		transferList.add(client);
	}

	/**
	 * Remove a host from the client list. Then Refresh the display.
	 * @param name
	 */
	public synchronized void removeHost(String name) {
		for (int i = 0; i < clientList.size(); i++) {
			if (name.equals(clientList.get(i))) {
				clientList.remove(i);
			}
		}
		DisplayStatus();
		clientListDisplay();
	}
	
	/**
	 * Remove a Transfer information from the transfer list. Then refresh
	 * @param name
	 */

	public synchronized void removeTransfer(String name) {
		for (int i = 0; i < transferList.size(); i++) {
			if (name.equals(transferList.get(i))) {
				transferList.remove(i);
			}
		}
		DisplayStatus();
		clientListDisplay();
	}

	/**
	 * The Main  Display function that display messages,clients,transfers,prompt information.
	 */
	public synchronized void clientListDisplay() {
		DisplayStatus();

		synchronized (System.out) {
			System.out.println(
					"----------------------------------P2P Clients Information---------------------------------");

			switch (clientList.size()) {

			case 0:
				System.out.println("\n\n\n\n");
				break;
			case 1:
				System.out.println("\n\n\n");
				break;
			case 2:
				System.out.println("\n\n");
				break;
			case 3:
				System.out.println("\n");
				break;
			}

			for (String c : clientList) {
				System.out.print(ANSI_CYAN);
				System.out.println(c);
				System.out.print(ANSI_WHITE);

			}

			System.out.println(
					"------------------------------------P2P File Transfer-------------------------------------");

			switch (transferList.size()) {

			case 0:
				System.out.println("\n\n\n\n");
				break;
			case 1:
				System.out.println("\n\n\n");
				break;
			case 2:
				System.out.println("\n\n");
				break;
			case 3:
				System.out.println("\n");
				break;
			}

			for (String c : transferList) {
				if (c.contains("-->")) {
					System.out.print(ANSI_PURPLE);
					System.out.println(c);
					System.out.print(ANSI_WHITE);
				} else {
					System.out.print(ANSI_GREEN);
					System.out.println(c);
					System.out.print(ANSI_WHITE);
				}

			}
			System.out.println(
					"--------------------------------User command interaction----------------------------------\n");

		}

		for (String message : buffer) {
			System.out.println(message);
		}
		System.out.print("$>");
	}

	/**
	 * Add a message to the buffer. 
	 * @param res
	 */
	public synchronized void addToDisplay(String res) {
		buffer.add(res);
	}

	/**
	 * Validate a step -> Change color and print ok.
	 * @param step
	 */
	public void validate(int step) {
		switch (step) {
		case 1:
			this.authInfo = "[" + ANSI_GREEN + "ok" + ANSI_WHITE + "] Authentication verification";
			break;
		case 2:
			this.setLocalDir = "[" + ANSI_GREEN + "ok" + ANSI_WHITE + "] Setting up download directory";
			break;
		case 4:
			this.initRegistering = "[" + ANSI_GREEN + "ok" + ANSI_WHITE + "] Registering to master repository";
			break;
		default:
			break;
		}
	}

	/**
	 * Deny a step -> Red color and error message.
	 * @param step
	 */
	public void deny(int step) {
		switch (step) {
		case 1:
			this.authInfo = "[" + ANSI_RED + "error" + ANSI_WHITE + "] Authentication verification";
			break;
		case 2:
			this.setLocalDir = "[" + ANSI_RED + "error" + ANSI_WHITE + "] Setting up download directory";
			break;
		case 4:
			this.initRegistering = "[" + ANSI_RED + "error" + ANSI_WHITE + "] Registering to master repository";
			break;
		default:
			break;
		}
	}
}
