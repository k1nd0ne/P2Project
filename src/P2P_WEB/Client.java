package P2P_WEB;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 
 * @author k1nd0ne The client class is composed of 2 parts : Client side
 *         (Interaction with the user). Server side (Handle other client
 *         connected to us (typically when they download one of our files).
 * 
 *         The client class is composed of the informations entered in the UI,
 *         as well as new graphic components to display when the connection is
 *         successful.
 */
public class Client extends Thread {

	private String request;
	private ArrayList<Register> regList; // Register -> an object representing an entry in the directory
	private ArrayList<String> directory;// Will be used to get a list of servers having a file.
	private Socket sock;
	private String username;
	private String password;
	private String resp;
	private String dbPath;
	private ClientServerSide distrib; // The server part of the client.

	/* Graphic components declaration */
	private JPanel jp;
	private JLabel connectLabel;
	private JProgressBar progressBar;
	private JTextField serverField, loginField;
	private JPasswordField passwordField;
	private JButton connectButton;
	private JList<String> list; // Represent the list of available file in the all P2P network.
	private JList<String> list_1; // Represent the list of files that WE share
	private JLabel lblSharedFiles;
	private JLabel lblAvailableFiles;
	private JProgressBar progressBar_1; // The downloading progressbar

	/**
	 * The client constructor is binding all the attribute (Heritage of the graphics
	 * component/information from the mainWindow), (New graphics component
	 * initialization)
	 * 
	 * @param username
	 * @param addr
	 * @param jp
	 * @param cl
	 * @param progressBar
	 * @param login
	 * @param passwordField
	 * @param bt
	 * @param server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client(String username, String addr, JPanel jp, JLabel cl, JProgressBar progressBar, JTextField login,
			JPasswordField passwordField, JButton bt, JTextField server) throws UnknownHostException, IOException {
		this.sock = new Socket(addr, 2121);
		this.username = username;
		this.password = passwordField.getText();
		this.resp = "";
		this.regList = new ArrayList<Register>();
		this.directory = new ArrayList<String>();
		this.connectLabel = cl;
		this.progressBar = progressBar;
		// Interface binding
		this.jp = jp;
		this.serverField = server;
		this.loginField = login;
		this.passwordField = passwordField;
		this.connectButton = bt;
		this.list = new JList<String>(new DefaultListModel<String>());
		this.list_1 = new JList<String>(new DefaultListModel<String>());
		this.lblSharedFiles = new JLabel("Shared files");
		this.lblAvailableFiles = new JLabel("Available files");
		this.progressBar_1 = new JProgressBar();

	}

	/**
	 * Reset some of the UI components
	 */
	public void resetAll() {
		this.serverField.setEnabled(true);
		this.loginField.setEnabled(true);
		this.passwordField.setEnabled(true);
		this.connectButton.setEnabled(true);
		this.connectButton.setText("Connect");
	}

	/**
	 * Send a message to the remote master repo
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
	 * Send a message to the remote sock
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
			String messageRcv = reader.readLine();
			return messageRcv;
		} catch (IOException ioe) {
			System.out.println("Client: Erreur de lecture : " + ioe.getMessage());
		}
		return null;
	}

	/**
	 * Ask the master repository the list of shared files in the whole P2P network.
	 */
	public void refreshDispFiles() {
		SendMessage("LS:");
		String dirRes[] = ReceiveMessage(sock).split(":");
		for (String line : dirRes) {
			((DefaultListModel) list.getModel()).addElement("-" + line);
		}
	}

	/**
	 * This routine is loading and displaying the graphics components.
	 */
	public void run() {

		/********************************************************/
		/* AUTHENTICATION TO THE MASTER REPOSITORY */
		/********************************************************/
		SendMessage("USER:" + this.username + ":PASS:" + this.password);
		this.resp = ReceiveMessage(this.sock);
		if (resp.contentEquals("230")) {
			System.out.println("230 User logged in, proceed.");
			this.connectLabel.setText("Authentication success.");
			this.connectLabel.setForeground(Color.GREEN);
			this.progressBar.setValue(40);
		} else {
			System.out.println("530 Authentication failure.");
			connectLabel.setText("Authentication failure.");
			resetAll();
			this.connectLabel.setForeground(Color.RED);
			this.progressBar.setValue(0);

			return;

		}

		try {
			sleep(200);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* Setting up download directory. */
		this.connectLabel.setText("Checking directory...");
		this.connectLabel.setForeground(Color.ORANGE);
		this.progressBar.setValue(60);

		this.dbPath = "./src/loot";

		if (!new File(this.dbPath).exists()) {
			this.connectLabel.setText("Can't setup loot. see README");
			this.connectLabel.setForeground(Color.RED);
			this.progressBar.setValue(0);
			return;
		}

		try {
			sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* Creating the server side of the client application */

		this.connectLabel.setText("Creating server side socket...");
		this.connectLabel.setForeground(Color.ORANGE);
		this.progressBar.setValue(80);

		try {
			sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/********************************************************/
		/* SERVER LAUNCH AND REGISTRATION TO THE DIRECTORY SERV */
		/********************************************************/
		try {
			this.distrib = new ClientServerSide(this.dbPath, this.sock, this.sock.getLocalPort(), jp,
					this.serverField.getText());
			distrib.start();
			this.connectLabel.setText("Connected");
			this.connectLabel.setForeground(Color.GREEN);
			this.progressBar.setValue(100);
			sleep(500);
		} catch (Exception e) {
			this.connectLabel.setText("Can't create server side.");
			this.connectLabel.setForeground(Color.RED);
			this.progressBar.setValue(0);
			System.out.println(e.getMessage());
			return; // Can't create server side = exit
		}

		/********************************************************/
		/* GRAPHIC COMPONENTS ACCESS AND DISPLAY */
		/********************************************************/
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBounds(213, 110, 302, 353);
		jp.add(list);

		list_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list_1.setBackground(Color.WHITE);
		list_1.setBounds(6, 110, 182, 353);

		jp.add(list_1);

		JLabel lblSharedFiles = new JLabel("Shared files : ");
		lblSharedFiles.setForeground(Color.WHITE);
		lblSharedFiles.setBounds(6, 85, 88, 16);
		jp.add(lblSharedFiles);

		JButton btnNewButton = new JButton("Stop sharing");
		btnNewButton.setBounds(85, 80, 111, 26);
		jp.add(btnNewButton);

		JLabel lblAvailableFiles = new JLabel("Available files :");
		lblAvailableFiles.setForeground(Color.WHITE);
		lblAvailableFiles.setBounds(213, 85, 101, 16);
		jp.add(lblAvailableFiles);

		JLabel lblDownloadInProgress = new JLabel("Download in progress...");
		lblDownloadInProgress.setForeground(Color.ORANGE);
		lblDownloadInProgress.setBounds(642, 410, 183, 16);
		lblDownloadInProgress.setVisible(false);
		jp.add(lblDownloadInProgress);

		JButton btnRefresh = new JButton("refresh");
		btnRefresh.setBounds(398, 80, 117, 29);
		jp.add(btnRefresh);

		
		
		/*Download button, when pressed retrieve the file selected in the available file list see actions performed later*/
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(522, 406, 180, 62);
		btnDownload.setVisible(false);
		jp.add(btnDownload);

		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((DefaultListModel) list.getModel()).removeAllElements();
				refreshDispFiles();
				btnDownload.setVisible(false);
			}

		});

		/*If the user doesn't want to share a file anymore*/
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// We move the file he shared before in the not_shared directory
				String filePath = ((String) list_1.getSelectedValuesList().get(0)).substring(1); // remove the '-' at
																									// the beginning
				String filename = filePath.split("/")[3];
				new File(filePath).renameTo(new File("./src/loot/not_shared/" + filename));

				// Then remove the parts concerned
				int i = 1;
				while (true) {
					File f = new File("./src/loot/parts/" + filename + ".part" + i);
					if (f.exists()) {
						f.delete();
					} else {
						break;
					}
					i++;
				}
				// Inform the master repository that we don't share this file anymore
				SendMessage("STOPSHARE:" + filename);

				// Refresh graphics elements
				((DefaultListModel) list_1.getModel()).removeAllElements();
				((DefaultListModel) list.getModel()).removeAllElements();
				refreshSharedFiles();
				refreshDispFiles();
				btnDownload.setVisible(false);
			}

		});

		/*Adding the progress bar to the UI */
		progressBar_1.setBounds(542, 422, 324, 20);
		progressBar_1.setVisible(false);
		jp.add(progressBar_1);
		
		/*The disconnect button end the thread*/
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(749, 445, 117, 29);

		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				progressBar.setValue(0);
				connectLabel.setText(" ");
				resetAll();
				jp.remove(list);
				jp.remove(list_1);
				jp.remove(lblSharedFiles);
				jp.remove(btnNewButton);
				jp.remove(lblAvailableFiles);
				jp.remove(btnRefresh);
				jp.remove(btnDownload);
				jp.remove(progressBar_1);
				jp.remove(btnDisconnect);
				jp.validate();
				jp.repaint();
				try {
					sock.close();
					distrib.interrupt(); //kill the server side. 
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		jp.add(btnDisconnect);

		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnDownload.setVisible(true);
			}
		});

		/*Display what are the files we share and what can we download*/
		refreshSharedFiles();
		refreshDispFiles();

		/*********************************************************************/
		/* Download a file ! */
		/*********************************************************************/
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String file = list.getSelectedValuesList().get(0).substring(1); // Only one item can be download at the
				if (new File("./src/loot/" + file).exists()) {
					connectLabel.setText("File already downloaded.");
					connectLabel.setForeground(Color.RED);
				} //else {
					DownloadThread dl = new DownloadThread(file, sock, connectButton, progressBar_1, btnDownload,
							lblDownloadInProgress, list_1);
					dl.start();
				//}

			}

		});

	}

	/**
	 * refresh the shared file directory in the UI
	 */
	private void refreshSharedFiles() {
		String sharedfile = getFileList();
		for (String f : sharedfile.split(":")) {
			((DefaultListModel) list_1.getModel()).addElement("-" + f);
		}

		jp.validate();
		jp.repaint();
	}

	/**
	 * Get the ls return.
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
