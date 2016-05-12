package activitystreamer.message;

import activitystreamer.server.Connection;

public class AuthenticateFailMessage extends Message 
{

    private final static String command = "AUTHENTICATE_FAIL";
    private String info;

    public AuthenticateFailMessage(String info)
    {
        super(command);
        this.info = info;
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