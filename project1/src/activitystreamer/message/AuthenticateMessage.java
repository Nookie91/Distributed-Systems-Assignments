package activitystreamer.message;

import java.util.Map;

public class AuthenticateMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE";
    private static String[] keys = {"command", "secret"};

    public AuthenticateMessage(String secret)
    {
        super();
        message.put("command", COMMAND);
        if(secret != null)
        {
            message.put("secret",secret);
        }
        else
        {
            message.put("secret","");
        }
        
    }

    public AuthenticateMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }

    public String getSecret()
    {
        return message.get("secret");
    }

    public boolean doesSecretMatch(String secret)
    {
    	if(secret == null &&
    			message.get("secret").equals(""))
    	{
    		return true;
    	}
    	else if(secret == null)
    	{
    		return false;
    	}
    	else if(secret.equals(message.get("secret")))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}