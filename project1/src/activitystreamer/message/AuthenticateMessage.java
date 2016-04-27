package activitystreamer.message;

import java.util.Map;

public class AuthenticateMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE";
    private static String[] keys = {"command", "secret"};

    public AuthenticateMessage(String secret)
    {
        super(COMMAND);
        message.put("secret",secret);
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
        if(secret.equals(message.get("secret")))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}