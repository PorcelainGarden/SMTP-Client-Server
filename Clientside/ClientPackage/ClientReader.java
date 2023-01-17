package ClientPackage;
import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientReader implements Runnable {
	Socket cwSocket = null;
	String msgServer = "";
	boolean isRunning = true;
	private StateManager sm;
	String err = "Something went wrong, please try again"; //Writing this here so I won't have to write it every single time...
	public ClientReader(Socket inputSock, StateManager state) {
		cwSocket = inputSock;
		sm = state;
	}
	
	//Reads data from the input stream of the cwSocket socket object and stores it in the msgServer variable,
	//then check current state of client and performs different actions based on the state
	public void run() {
		try {
			while (isRunning) {
				DataInputStream dataIn = new DataInputStream(cwSocket.getInputStream());
				msgServer = dataIn.readUTF();
				System.out.println(msgServer);
				if (sm.getState() == State.HELO) {
					if (msgServer.contains("250")) {
						sm.setState(State.A_HELO);
					} else {System.err.println(err);}
				} else if (sm.getState() == State.MAIL) {
					if (msgServer.contains("250")) {
						sm.setState(State.A_MAIL);
					} else {System.err.println(err);}
				} else if (sm.getState() == State.RCPT) {
					if (msgServer.contains("250")) {} //So it doesn't print out an error, just checks it if it has it
					 else {System.err.println(err);}
				} else if (sm.getState() == State.DATA) {
					if (msgServer.contains("354")) {
						sm.setState(State.A_DATA);
					} else {System.err.println(err );}
				} else if (sm.getState() == State.MSG) {
					if (msgServer.contains("250")) {
						sm.setState(State.A_MSG);
					} else {System.err.println(err);}
				}
				if (msgServer.contains("221")) {
					isRunning = false;
					System.out.println("Client Closing...");
				}
			}
		} catch (SocketException err) {System.err.println("Server terminated the connection");} 
		  catch (Exception err) {
			System.err.println("err in Reader--> " + err.getMessage());
			err.printStackTrace();
		}
	}
}