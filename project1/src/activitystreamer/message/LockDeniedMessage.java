package activitystreamer.message;

import java.util.Map;

public class LockDeniedMessage extends Message 
{
    private static String COMMAND = "LOCK_DENIED";
    private static String[] keys = {"command", "username", "secret"};

    public LockDeniedMessage(String username, String secret)
    {
        super();
        message.put("command", COMMAND);
        message.put("username",username);
        message.put("secret",secret);
    }

    public LockDeniedMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }

    public String getUsername()
    {
        return message.get("username");
    }    

    public String getSecret()
    {
        return message.get("secret");
    }

}