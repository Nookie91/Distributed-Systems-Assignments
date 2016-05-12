package activitystreamer.message;

import activitystreamer.server.Connection;

public class AuthenticateMessage extends Message 
{
    private final static String command = "AUTHENTICATE";
    private String secret;

    public AuthenticateMessage(String secret)
    {
        super(command);
        this.secret = secret;
        
    }

    public String getSecret()
    {
        return secret;
    }

    public boolean doesSecretMatch(String secret)
    {
    	if(secret == null && this.secret == null)
    	{
    		return true;
    	}
    	else if(secret == null || this.secret == null)
    	{
    		return false;
    	}
    	else if(secret.equals(this.secret))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

        public boolean checkFields(Connection con)
    {
        return false;
    }

}