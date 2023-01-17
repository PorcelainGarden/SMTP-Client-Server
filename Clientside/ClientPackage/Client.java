package ClientPackage;
import java.net.*;
import java.util.Scanner;

public class Client {
	private StateManager sm = new StateManager();
	
	//Creates a client that connects to a server at a specific IP address and port number. 
	public Client(String serverIP, int portNo) {
		try {
			Socket sock = new Socket(serverIP, portNo);
			ClientReader clientRead = new ClientReader(sock, sm);
			Thread clientReadThread = new Thread(clientRead);
			clientReadThread.start();
			ClientWriter clientWrite = new ClientWriter(sock, sm);
			Thread clientWriteThread = new Thread(clientWrite);
			clientWriteThread.start();
		} catch (Exception err) {System.err.println("No connection has been made, " + err);}
	}

	//Client-side entry point for the program.
	public static void main(String[] args) {
		Scanner scan = null;
		try {
			scan = new Scanner(System.in);
			System.out.print("Please type in the port number: ");
			int portNo = scan.nextInt();
			System.out.print("Please type in the server IP: ");
			String serverIP = scan.next();
			Client client = new Client(serverIP, portNo);
		} catch (Exception err) { scan.close();}
	}
}