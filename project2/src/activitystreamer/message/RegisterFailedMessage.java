package activitystreamer.message;

import activitystreamer.server.Connection;


public class RegisterFailedMessage extends Message 
{
    private final static String command = "REGISTER_FAILED";
    private String info;

    public RegisterFailedMessage(String username)
    {
        super(command);
        this.info =  username + " is already registered with the system";
    }

    public String getInfo()
    {
        return info;
    }

    public boolean checkFields(Connection con)
	{
    	InvalidMessage error;
	    if(getInfo() == null)
	    {
	        error = new InvalidMessage("the received message did not contain any info");
	        con.writeMsg(error.messageToString());
	        return true;
	    }
	    return false;
	}

}