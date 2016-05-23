package activitystreamer.message;

import activitystreamer.server.Connection;

public class AuthenticateSecureMessage extends Message 
{
    private final static String command = "AUTHENTICATE";
    private String secret;
    private Boolean secure;

    public AuthenticateSecureMessage(String secret, Boolean secure)
    {
        super(command);
        this.secret = secret;
        this.secure = secure;        
    }
    
    public AuthenticateSecureMessage(String secret)
    {
        super(command);
        this.secret = secret;
        this.secure = false;        
    }

    public String getSecret()
    {
        return secret;
    }
    
    public Boolean getSecure()
    {
    	if (secure == null)
    	{
    		return false;
    	}
    	return secure;
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