package FTP;

import java.util.Scanner;

public class ClientFTP {
	/**
	 * Main class for Client FTP.
	 * @param args
	 */
	public static void main(String[] args) {
	    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
	    System.out.println("--------FTP-P2P---------");
	    	  System.out.print("Username>");
	    	  String username = myObj.nextLine();  // Read user input
	    	  
	    	  System.out.print("Password>");
	    	  String password = myObj.nextLine();  // Read user input
	  	      System.out.println("Connecting to 127.0.0.1:2121");
	  	      
	  	      ClientPI p1 = new ClientPI(username,password,"./src/myhome",2121);
	 		  p1.start();
	 		  
	 		  
	    
	    
	}

}
