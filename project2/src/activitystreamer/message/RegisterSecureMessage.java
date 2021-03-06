package activitystreamer.message;

import activitystreamer.server.Connection;


// Register as normal but if server supports SSL flag that the clients
// does as well
public class RegisterSecureMessage extends Message
{
    private final static String command = "REGISTER";
    private String username;
    private String secret;
    private Boolean secure = false;

    public RegisterSecureMessage(String username, String secret, Boolean secure)
    {
        super(command);
        this.username = username;
        this.secret = secret;
        this.secure = secure;
    }
    
    public RegisterSecureMessage(String username, String secret)
    {
        super(command);
        this.username = username;
        this.secret = secret;
        this.secure = false;
    }


    public String getUsername()
    {
        return username;
    }

    public String getSecret()
    {
        return secret;
    }

    public Boolean getSecure()
    {
    	if(secure == null)
    	{
    		return false;
    	}
        return secure;
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

//        if(getSecure() == null)
//        {
//             error = new InvalidMessage("the received message did not contain a secure field");
//             con.writeMsg(error.messageToString());
//             return true;
//        }

        return false;
    }

}
