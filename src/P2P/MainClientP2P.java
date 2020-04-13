package P2P;

import java.util.Scanner;
/**
 * Main Client. To execute in a terminal. 
 * @author k1nd0ne
 *
 */
public class MainClientP2P {
	
	public static void main(String[] args) throws InterruptedException {
		boolean sessionStatus = false;
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		System.out.println("***** The P2P Bay ****");
		System.out.println("Please Sign In to access the files");
		
		
		System.out.print("Username>");
  	  	String username = myObj.nextLine();  // Read user input
  	  
  	  	System.out.print("Password>");
  	  	String password = myObj.nextLine();  // Read user input
	    System.out.println("Connecting to 127.0.0.1:2121");
		ClientP2P c1 = new ClientP2P(username,password);
		c1.start();
	}
}
