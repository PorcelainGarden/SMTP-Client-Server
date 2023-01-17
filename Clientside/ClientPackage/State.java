package ClientPackage;

//Basic SMTP States as listed in SMTP standards
public enum State {
	NONE, HELO, MAIL, RCPT, DATA, QUIT, MSG, A_HELO, A_MAIL, A_RCPT, A_DATA, A_MSG, B_RCPT, E_MSG
}
