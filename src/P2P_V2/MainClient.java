package P2P_V2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MainClient {
	/**
	 * This main Client class is going to be the client and server.
	 * This just flush the screen, wait for authentication infos, and launch the Client Class.
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		
		Scanner myObj = new Scanner(System.in); // Create a Scanner object
		System.out.print(" .oPYo. .oPYo.  .oPYo.    .oPYo.      .oo o   o \n" + 
				" 8    8     `8  8    8    8   `8     .P 8 `b d' \n" + 
				"o8YooP'    oP' o8YooP'   o8YooP'    .P  8  `b'  \n" + 
				" 8      .oP'    8         8   `b   oPooo8   8   \n" + 
				" 8      8'      8         8    8  .P    8   8   \n" + 
				" 8      8ooooo  8         8oooP' .P     8   8   \n" + 
				":..:::::.......:..::::::::......:..:::::..::..::\n" + 
				"::::::::::::::::::::::::::::::::::::::::::::::::\n" + 
				"::::::::::::::::::::::::::::::::::::::::::::::::\n\n");
		System.out.println("Please Sign In into your account.");
		
		System.out.print("Username>");
  	  	String username = myObj.nextLine();  // Read user input
  	  
  	  	System.out.print("Password>");
  	  	String password = myObj.nextLine();  // Read user input
		
  	  	try {
  	  		Client c = new Client(username,password);
  	  		c.startClient();
  	  	}
  	  	catch(Exception e) {
  	  		System.out.println("Directory server is down please try again later.");
  	  	}
  	  	 
	}

}
