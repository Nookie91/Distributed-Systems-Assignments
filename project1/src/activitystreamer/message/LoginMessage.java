package activitystreamer.message;

import java.util.Map;

import activitystreamer.server.Connection;

public class LoginMessage extends Message 
{
    private final static String command = "LOGIN";
    private String username;
    private String secret;

    public LoginMessage(String username, String secret)
    {
        super(command);
        this.username = username;
        this.secret = secret;
    }
    public LoginMessage(String username)
    {
        super(command);
        this.username = username;
        this.secret = null;
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
        if(getUsername().equals("anonymous"))
        {
            if(getSecret() == null)
            {
            	 error = new InvalidMessage("the received message did not contain a secret");
                 con.writeMsg(error.messageToString());
                 return true;
            }
            
        } 
        return false;
    }

    public String getUsername()
    {
        return username;
    }    

    public String getSecret()
    {
        return secret;
    }
}