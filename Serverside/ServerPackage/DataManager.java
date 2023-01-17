package ServerPackage;
import java.util.ArrayList;

//Simple data structure that manages lists from MailMessage
public class DataManager {
	public ArrayList<MailMessage> allMail;
	public MailMessage currentMessage;
	public DataManager() {allMail = new ArrayList<MailMessage>();
	}
}
