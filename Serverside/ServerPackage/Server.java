package ServerPackage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    int portNumber = 13716;
    ServerSocket serverSock = null;
    ArrayList<SocketManager> clients = null;
    DataManager dm;
    FileWriter file;
    BufferedWriter writeFile;
    
    public static void main(String[] args){
    	System.out.print("Please type in the port number the server has to act on: ");    	
    	Scanner scan = new Scanner(System.in);
    	int port = scan.nextInt();
    	Server server = new Server(port);      	
    	scan.close();
    }

    public Server (int port) {
		if (port > 2048) {
			portNumber = port;
		} else {System.err.println("Port number too low, defaulting to 13716");}      
        try{
            serverSock = new ServerSocket(portNumber);
            clients = new ArrayList<SocketManager>();           
            dm = new DataManager();            
            File file = new File ("message.xml");            
            while (true){
                System.out.println("Awaiting client...");
                Socket sock = serverSock.accept();              
                SocketManager sm = new SocketManager(sock);                
                synchronized(clients) {
                	clients.add(sm);   
                	System.out.println("Client Connected");
                }
                ServerConnectionHandler sch = new ServerConnectionHandler(clients, dm, file, sm, true);
                Thread schThread = new Thread(sch);
                schThread.start();
            }
        }
        catch (Exception err){System.err.println("Error: " + err.getMessage());}
    }   
}