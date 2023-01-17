package ServerPackage;
import java.util.ArrayList;

//Simple data structure that represents a mail message. 
public class MailMessage {
	public String from;
	public ArrayList<String> toList;
	public ArrayList<String> msg;

	public MailMessage() {
		toList = new ArrayList<String>();
		msg = new ArrayList<String>();
	}
}
