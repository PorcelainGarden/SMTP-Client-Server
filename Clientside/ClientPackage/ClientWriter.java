package ClientPackage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ClientWriter implements Runnable {
	String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
	String dmnNameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
	String emailAddressRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	Socket cwSocket = null;
	State wState = State.NONE;
	State sState = State.NONE;
	String answr;
	String dmn;
	StateManager sm;
	Date date;
	String mailFrom;
	ArrayList<String> mailTo = new ArrayList<String>();
	ArrayList<String> fullmsg = new ArrayList<String>();
	String sbjct;
	String msg = "";

	boolean isRunning = true;

	//A constructor that initializes the cwSocket and sm fields with the given Socket and StateManager objects, respectively.
	public ClientWriter(Socket outputSock, StateManager state) {
		cwSocket = outputSock;
		sm = state;
	}

	//Infinite loop that calls the UserInput() method while the isRunning field is true.
	public void run() {
		try {
			while (isRunning) {UserInput();}
		} catch (Exception err) {
			System.err.println("Writer Error: " + err.getMessage());
			err.printStackTrace();
		}
	}

	//A method that prompts the user for input and processes the input according to the current state of the client.
	public void UserInput() {
		Scanner scan = new Scanner(System.in);
		if (wState == State.NONE) {
			System.out.println("Send an email? Yes/No");
			answr = scan.nextLine();
			if (answr.toLowerCase().equals("yes") || answr.toLowerCase().equals("y")) {
				wState = State.HELO;
			} else if (answr.toLowerCase().equals("no") || answr.toLowerCase().equals("n")) {
				System.out.println("No selected. Connection closed...");
				scan.close();				
				sState = State.QUIT;
				wState = State.QUIT;
				sm.setState(State.QUIT);				
				SendEmail();
			} else {System.err.println("Unrecognized response. Use yes/no to answer.");}
		} else if (wState == State.HELO) {
			System.out.println("What is the desired domain you want to send the email from?");
			answr = scan.nextLine();
			if (answr.matches(ipAddressRegex) || answr.matches(dmnNameRegex)) {
				dmn = answr;
				System.out.println("Domain set to: " + dmn);
				wState = State.MAIL;
			} else {System.err.println("Invalid Domain address, try again.");}
		} else if (wState == State.MAIL) {
			System.out.println("Sender's email address: ");
			answr = scan.nextLine();
			if (answr.matches(emailAddressRegex)) {
				mailFrom = answr;
				System.out.println("Sender's email address : " + mailFrom);
				wState = State.RCPT;
			} else {System.err.println("Invalid Email address, try again.");}
		} else if (wState == State.RCPT) {
			System.out.println("Recipient's email address: ");
			answr = scan.nextLine();
			if (answr.matches(emailAddressRegex)) {
				mailTo.add(answr);
				System.out.println("Recipient's email address: " + mailTo);
				while (wState != State.DATA) {
					System.out.println("Send email to more people? Yes/No");
					answr = scan.nextLine();
					if (answr.toLowerCase().equals("yes") || answr.toLowerCase().equals("y")) {
						System.out.println("Other recipient's email:  ");
						answr = scan.nextLine();
						if (answr.matches(emailAddressRegex)) {
							mailTo.add(answr);
							System.out.println("Recipients emails:  " + mailTo);
						} else {System.err.println("Invalid Email address, try again.");}
					} else if (answr.toLowerCase().equals("no") || answr.toLowerCase().equals("n")) {
						wState = State.DATA;
					} else {System.err.println("Unrecognized response. Use yes/no to answer.");}
				}
			} else {System.err.println("Invalid Email address, try again.");}
		} else if (wState == State.DATA) {
			System.out.println("Email subject: ");
			answr = scan.nextLine();
			System.out.println("The subject of the email is ' " + answr + " '. Is this correct? Yes/No");
			sbjct = answr;
			answr = scan.nextLine();
			if (answr.toLowerCase().equals("yes") || answr.toLowerCase().equals("y")) {
				System.out.println(sbjct + " is the email's subject.");
				System.out.println("Type the body of the email. Finish by typing 'FINISHMESSAGE'");
				answr = scan.nextLine();
				while (!answr.toUpperCase().equals("FINISHMESSAGE")) {
					if (answr.equals(".")) {answr = "..";}
					msg = msg + " " + answr;
					answr = scan.nextLine();
				}
				System.out.println("Email saved...");
				wState = State.MSG;
			} else if (answr.toLowerCase().equals("no") || answr.toLowerCase().equals("n")) {System.out.println("Type again.");
			} else {System.err.println("Unrecognized response. Use yes/no to answer.");}
		} else if (wState == State.MSG) {
			date = new Date();
			Showmsg();
			System.out.println("Send the email? Yes/No");
			answr = scan.nextLine();
			if (answr.toLowerCase().equals("yes") || answr.toLowerCase().equals("y")) {
				System.out.println("Sending your email...");
				while (isRunning) {
					SendEmail();					
					if (sm.getState() == State.E_MSG) {break;}
				}				
				System.out.println("Closing Client...");				
				scan.close();					
				sState = State.QUIT;
				wState = State.QUIT;
				sm.setState(State.QUIT);					
				SendEmail();				
			} else if (answr.toLowerCase().equals("no") || answr.toLowerCase().equals("n")) {
				System.out.println("No selected. \n Closing Client...");
				scan.close();					
				sState = State.QUIT;
				wState = State.QUIT;
				sm.setState(State.QUIT);					
				SendEmail();
			} else {System.err.println("Unrecognized response. Use yes/no to answer.");}
		}
	}

	//Display an email message to the user and add it to the fullmsg field.
	public void Showmsg() {
		System.out.println("  !.!  --- EMAIL START ---  !.!  \n "
				+ "Date: " + date + "\n"
				+ "Sender: " + mailFrom + "\n"
				+ "Recipient: " + mailTo + "\n"
				+ "Subject: " + sbjct + "\n" 
				+ msg + "\n"
				+ "  !.!  --- EMAIL   END ---  !.!  ");

		fullmsg.add("Date: " + date + "\n"
				+ "Sender: " + mailFrom + "\n"
				+ "Recipient: " + mailTo + "\n"
				+ "Subject: " + sbjct + "\n"
				+ msg);
	}

	//Method that sends an email message over a socket connection using the Simple Mail Transfer Protocol (SMTP).
	public void SendEmail() {
		try {
			DataOutputStream dataOut = new DataOutputStream(cwSocket.getOutputStream());
			while (isRunning) {
				sState = sm.getState();
				if (sState == State.NONE) {
					dataOut.writeUTF("HELO " + dmn);
					dataOut.flush();
					sm.setState(State.HELO);
				} else if (sState == State.A_HELO) {
					dataOut.writeUTF("MAIL FROM:<" + mailFrom + ">");
					dataOut.flush();
					sm.setState(State.MAIL);
				} else if (sState == State.A_MAIL) {
					for (int i = 0; i < mailTo.size(); i++) {
						dataOut.writeUTF("RCPT TO:<" + mailTo.get(i) + ">");
						dataOut.flush();
						sm.setState(State.RCPT);
					}
					sm.setState(State.A_RCPT);
				} else if (sState == State.A_RCPT) {
					dataOut.writeUTF("DATA");
					dataOut.flush();
					sm.setState(State.DATA);
				} else if (sState == State.A_DATA) {
					for (int ii = 0; ii < fullmsg.size(); ii++) {
						dataOut.writeUTF(fullmsg.get(ii));
						dataOut.flush();
					}
					dataOut.writeUTF(".");
					dataOut.flush();
					sm.setState(State.MSG);
				} else if (sState == State.A_MSG) {
					System.out.println("Email delivered successfuly.");					
					sm.setState(State.E_MSG);
					break;					
				} else if (sState == State.QUIT) {
					dataOut.writeUTF("QUIT");
					dataOut.flush();					
					isRunning = false;
				}
			}
		} catch (SocketException err) {System.err.println("Connection terminated by Server...");
		} catch (IOException err) {System.err.println("Reader Error: " + err.getMessage());}
	}
}
