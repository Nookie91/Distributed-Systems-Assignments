package activitystreamer.message;

import activitystreamer.server.Connection;

public class LockDeniedMessage extends Message 
{
    private final static String command = "LOCK_DENIED";
    private String username;
    private String secret;

    public LockDeniedMessage(String username, String secret)
    {
        super(command);
        this.username = username;
        this.secret = secret;
    }
    
    public String getUsername()
    {
        return username;
    }    

    public String getSecret()
    {
        return secret;
    }

    public boolean checkFields(Connection con)
    {
        InvalidMessage error;
        if(getUsername() == null)
        {
            error = new InvalidMessage("the received message did not contain a username");
            con.writeMsg(error.messageToString());
            return true;
        }

        if(getSecret() == null)
        {
             error = new InvalidMessage("the received message did not contain a secret");
             con.writeMsg(error.messageToString());
             return true;
        }
             
        return false;
    }
}