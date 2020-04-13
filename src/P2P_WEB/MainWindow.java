package P2P_WEB;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.ListSelectionModel;

/**
 * This class represent the client interface. 
 * @author k1nd0ne
 *
 */
public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField loginField;
	private JPasswordField passwordField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 872, 496);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		/*Main Panel Creation*/
		JPanel panel = new JPanel();
		panel.setBounds(6, 103, 709, -98);
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		/*This textField reprensent the server address field*/
		textField = new JTextField();
		textField.setBounds(331, 6, 169, 26);
		textField.setText("127.0.0.1");
		contentPane.add(textField);
		textField.setColumns(10);
		
		/*This label indicate the client that he need to enter the server address*/
		JLabel lblPpServer = new JLabel("P2P Server Address : ");
		lblPpServer.setBounds(200, 11, 131, 16);
		lblPpServer.setForeground(Color.WHITE);
		contentPane.add(lblPpServer);

		/*The connection status progress bar*/
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(305, 40, 244, 20);
		contentPane.add(progressBar);
		
		/*This label is as a message information label it can be changed to inform the client of an error or action*/
		JLabel connectLabel = new JLabel("");
		connectLabel.setBounds(657, 6, 215, 16);
		connectLabel.setForeground(Color.GREEN);
		contentPane.add(connectLabel);
		
		/*This is the login field*/
		loginField = new JTextField();
		loginField.setBounds(57, 6, 131, 26);
		loginField.setText("user");
		contentPane.add(loginField);
		loginField.setColumns(10);
		
		/*Indicate to the user that this is the login field*/
		JLabel lblLogin = new JLabel("Login");
		lblLogin.setBounds(6, 11, 101, 16);
		lblLogin.setForeground(Color.WHITE);
		contentPane.add(lblLogin);
		
		/*This is the password indicator/field */
		JLabel lblPass = new JLabel("Pass");
		lblPass.setBounds(6, 43, 61, 16);
		lblPass.setForeground(Color.WHITE);
		contentPane.add(lblPass);
		passwordField = new JPasswordField();
		passwordField.setBounds(57, 38, 131, 26);
		contentPane.add(passwordField);
		
		
		/*This is the connect button*/
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(188, 35, 117, 29);
		contentPane.add(btnConnect);
		
		progressBar.setVisible(false);
		
		
		/*When the user click on the connect button, 
		 * We check if all the field are not empty.
		 * Then we launch the client Thread -> Connection to the master repository and unlock user interface.
		 * We also inform the user of what is happening in the background (Authentification etc...).
		 */
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(textField.getText().isEmpty() || passwordField.getPassword().toString().isEmpty() || loginField.getText().isEmpty() ) {
					connectLabel.setText("Please provide a value for all the field.");
					connectLabel.setForeground(Color.RED);
				}
				else {
					progressBar.setVisible(true);
					btnConnect.setText("Connecting...");
					btnConnect.setEnabled(false);
					textField.setEnabled(false);
					passwordField.setEnabled(false);
					loginField.setEnabled(false);
					progressBar.setValue(20);
					/*Client thread creation and launch*/
					try {
						Client threadClient = new Client(loginField.getText(),textField.getText(),
								contentPane, connectLabel, progressBar, loginField, passwordField, btnConnect, textField);
						threadClient.start();
						
					} catch (IOException e1) { //something went wrong when connecting to the master repo (bad name etc..)
						progressBar.setVisible(false);
						connectLabel.setText("Error. Connection Refused");
						connectLabel.setForeground(Color.RED);
						e1.printStackTrace();
						btnConnect.setText("Connect");
						btnConnect.setEnabled(true);
						textField.setEnabled(true);
						textField.setEnabled(true);
						loginField.setEnabled(true);
						passwordField.setEnabled(true);
						contentPane.validate();
						contentPane.repaint();
					}
					
				}
			}
		});
	}
}
