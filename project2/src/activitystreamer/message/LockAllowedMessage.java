package activitystreamer.message;
import activitystreamer.server.Connection;

public class LockAllowedMessage extends Message 
{
    private final static String command = "LOCK_ALLOWED";
    private String username;
    private String secret;
    private String server;

    public LockAllowedMessage(String username, String secret, String server)
    {
        super(command);
        this.username = username;
        this.secret = secret;
        this.server = server;
    }
    

    public String getUsername()
    {
        return username;
    }    

    public String getSecret()
    {
        return secret;
    }

    public String getServer()
    {
        return server;
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
        if(getServer() == null)
        {
            error = new InvalidMessage("the received message did not contain a server");
            con.writeMsg(error.messageToString());
            return true;
        }
        return false;
    }
}