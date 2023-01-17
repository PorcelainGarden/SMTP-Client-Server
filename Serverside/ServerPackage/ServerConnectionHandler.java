package ServerPackage;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ClientPackage.State;

public class ServerConnectionHandler implements Runnable {
	SocketManager autos = null;
	ArrayList<SocketManager> clients = null;
	boolean verbose = false;
	State currentState = State.NONE;
	DataManager dm;
	File file;

	public ServerConnectionHandler(ArrayList<SocketManager> l, DataManager dm, File file, SocketManager inSoc,
			boolean v) {
		autos = inSoc;
		clients = l;
		verbose = v;
		this.dm = dm;
		this.file = file;
	}
	
	//initializes a connection with the client. 
	public void run() {
		try {
			Response(220);
			autos.output.flush();
			while (clients.size() > 0) {
				String message = autos.input.readUTF();
				System.out.println("--> " + message);
				currentState = parse(message, autos);
				System.out.println(currentState);
			}
		} catch (SocketException exception) {
			System.out.println("Client terminated the connection");
		} catch (Exception err) {
			err.printStackTrace();
			System.err.println("ServerHandler Error: " + err.getMessage());
		}
	}

	//Method that has a switch containing error codes.
	public void Response(int Code) {
		try {switch (Code) {
			case 214:
				autos.output.writeUTF("214 | Command sequence order: HELO, MAIL > RCPT > DATA > '.'. QUIT & NOOP can be used inbetween.");
				break;
			case 220:
				autos.output.writeUTF("SMTP service ready.");
				break;
			case 221:
				autos.output.writeUTF("221 | " + clients.get(0).getName() + " transmission channel closed by service...");
				break;
			case 250:
				autos.output.writeUTF("250 | Mail Action OK.");
			case 354:
				autos.output.writeUTF("354 | E-mail input start. Finish with <CRLF>.<CRLF>'.");
				break;
			case 500:
				autos.output.writeUTF("500 | Server couldn't recognize the command because of a syntax error.");
				break;
			case 501:
				autos.output.writeUTF("501 | Syntax error found in command parameters or arguments.");
				break;
			case 502:
				autos.output.writeUTF("502 | Command not implemented.");
				break;
			case 503:
				autos.output.writeUTF("503 | Server had bad sequence of commands.");
				break;
			case 504:
				autos.output.writeUTF("504 | Command parameter not implemented.");
				break;
			default:
				autos.output.writeUTF("Unknown response code error");
				}
			} catch (Exception err) {System.err.println("Error: " + err.getMessage());}
		}

	//saves message to the xml file
	public void SaveMessage() {
		Document doc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Element root;
			if (file.exists()) {
				doc = db.parse("message.xml");
				root = doc.getDocumentElement();
			} else {
				doc = db.newDocument();
				root = doc.createElement("ALLMESSAGES");
				doc.appendChild(root);
			}

			Element message = doc.createElement("MAIL");
			root.appendChild(message);
			Element from = doc.createElement("FROM");
			from.appendChild(doc.createTextNode(dm.currentMessage.from.toString()));
			message.appendChild(from);
			for (int i = 0; i < dm.currentMessage.toList.size(); i++) {
				Element to = doc.createElement("TO");
				to.appendChild(doc.createTextNode(dm.currentMessage.toList.get(i).toString()));
				message.appendChild(to);
			}
			Element text = doc.createElement("TXT");
			text.appendChild(doc.createTextNode(dm.currentMessage.msg.toString()));
			message.appendChild(text);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource src = new DOMSource(doc);
			StreamResult rslt = new StreamResult(new File("msgs.xml"));
			transformer.transform(src, rslt);
		} catch (Exception err) {System.err.println("Error: " + err.getMessage());}
	}

	//Processes a message received by the server from a client and returns the new state of the server.
	private State parse(String message, SocketManager sm) {
		String[] components = message.split(" ");
		String emailAdressRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		String domainNameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

		try {
			if (components.length > 0) {
				if (components[0].toUpperCase().equals("HELO")) {
					if (currentState == State.NONE || currentState == State.QUIT) {
						if (components.length > 1) {
							if ((components[1].matches(ipAddressRegex) || components[1].matches(domainNameRegex))
									&& components[1].length() < 65) {
								autos.output.writeUTF("250 " + components[1]);
							} else {Response(501);}
						} else {Response(250);}
						currentState = State.HELO;
					} else {Response(503);}
				} else if (components[0].toUpperCase().contains("MAIL")) {
					if (currentState == State.HELO || currentState == State.MSG) {
						dm.currentMessage = new MailMessage();
						String[] parts = components[1].split(":");
						if (parts[0].toUpperCase().equals("FROM")) {
							String firstAngleBracket = parts[1].substring(0, 1);
							String secondAngleBracket = parts[1].substring(parts[1].length() - 1, parts[1].length());
							if (firstAngleBracket.equals("<") && secondAngleBracket.equals(">")) {								
								String email = parts[1].substring(1, parts[1].length() - 1);
								if (email.matches(emailAdressRegex)) {
									dm.currentMessage.from = email;
									Response(250);
									currentState = State.MAIL;
								} else {Response(501);}
							} else {Response(501);}
						} else {Response(501);}
					} else {Response(503);}
				} else if (components[0].toUpperCase().contains("RCPT")) {
					if (currentState == State.MAIL || currentState == State.RCPT) {
						String[] parts = components[1].split(":");
						if (parts[0].toUpperCase().equals("TO")) {							
							String firstAngleBracket = parts[1].substring(0, 1);
							String secondAngleBracket = parts[1].substring(parts[1].length() - 1, parts[1].length());
							if (firstAngleBracket.equals("<") && secondAngleBracket.equals(">")) {								
								String rcpt = parts[1].substring(1, parts[1].length() - 1);								
								if (rcpt.matches(emailAdressRegex)) {
									dm.currentMessage.toList.add(rcpt);
									Response(250);
									currentState = State.RCPT;
								} else {Response(501);}								
							} else {Response(501);}							
						} else {Response(501);}
					} else {Response(503);}
				} else if (components[0].toUpperCase().equals("DATA")) {
					if (currentState == State.RCPT) {
						if (components.length == 1) {
							Response(354);
							currentState = State.DATA;
						} else {Response(501);}
					} else {Response(503);}
				} else if (components[0].equals(".")) {
					if (currentState == State.DATA) {
						if (components.length == 1) {
							Response(250);
							currentState = State.MSG;
							dm.allMail.add(dm.currentMessage);
							SaveMessage();
							autos.output.writeUTF("250 | Accepted message delivery.");
						} else {
							dm.currentMessage.msg.add(message);
							currentState = State.DATA;
						}
					} else {Response(503);}
				} else if (components[0].toUpperCase().equals("QUIT")) {
					if (components.length == 1) {
						Response(221);
						clients.get(0).close();
						clients.remove(0);
						currentState = State.QUIT;
					} else {Response(501);}
				} else if (components[0].toUpperCase().equals("RSET")) {
					if (currentState == State.MAIL || currentState == State.RCPT || currentState == State.DATA) {
						dm.currentMessage = new MailMessage();
						Response(250);
					}
				} else if (components[0].toUpperCase().equals("NOOP")) {Response(250);}
				 else if (components[0].toUpperCase().equals("HELP")) {
					if (components.length == 1) {Response(214);}
					 else {Response(504);}
				} else if (components[0].toUpperCase().equals("EXPN")) {Response(502);}
				  else if (components[0].toUpperCase().equals("VRFY")) {Response(502);}
				  else {
					if (currentState == State.DATA) {
						if (message.contains("..")) {
							String newNewMessage = message.replace("..", ".");
							dm.currentMessage.msg.add(newNewMessage);
						}else {dm.currentMessage.msg.add(message);}
						currentState = State.DATA;
					} else {Response(500);}
				}
			}
			autos.output.flush();
		} catch (IOException err) {System.err.println("Error: " + err.getMessage());}
		return currentState;
	}
}