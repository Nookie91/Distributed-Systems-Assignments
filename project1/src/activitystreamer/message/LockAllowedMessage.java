package activitystreamer.message;

import java.util.Map;

public class LockAllowedMessage extends Message 
{
    private static String COMMAND = "LOCK_ALLOWED";
    private static String[] keys = {"command", "username", "secret", "server"};

    public LockAllowedMessage(String username, String secret, String server)
    {
        super(COMMAND);
        message.put("username",username);
        message.put("secret",secret);
        message.put("server",server);
    }

    public LockAllowedMessage(Map<String,String> stringMessage)
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

    public String getServer()
    {
        return message.get("server");
    }
}