package ServerPackage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// Socket constructor w/ input & output
public class SocketManager {
	public Socket sock = null;
	public DataInputStream input = null;
	public DataOutputStream output = null;
	String name = null;

	public SocketManager(Socket socket) throws IOException {
		try {
			sock = socket;
			input = new DataInputStream(sock.getInputStream());
			output = new DataOutputStream(sock.getOutputStream());
			name = sock.getLocalAddress().getHostName();
		} catch (IOException err) {err.printStackTrace();}
	}

	//Safe thread accessible way to read data
	synchronized public DataInputStream getInput() {return input;}

	//Safe thread accessible way to write data
	synchronized public DataOutputStream getOutput() {return output;}

	//Method that closes inputs, outputs and the socket
	public void close() {
		try {
			input.close();
			output.close();
			sock.close();
		} catch (IOException err) {err.printStackTrace();}
	}

	//Safe thread accessible way to set the name
	synchronized public void setName(String val) {name = val;}

	//Safe thread accessible way to get the name
	synchronized public String getName() {return name;}

	//Safe thread accessible way to get the IP address
	synchronized public String ip() {return sock.getInetAddress().getHostAddress();}

	//Safe thread accessible way to get port Number
	synchronized public String port() {return Integer.toString(sock.getPort());}
}