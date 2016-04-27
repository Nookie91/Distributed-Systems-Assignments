package activitystreamer.message;

import java.util.Map;

public class RegisterMessage extends Message 
{
    private static String COMMAND = "REGISTER";
    private static String[] keys = {"command", "username", "secret"};

    public RegisterMessage(String username, String secret)
    {
        super();
        message.put("command", COMMAND);
        message.put("username",username);
        message.put("secret",secret);
    }

    public RegisterMessage(Map<String,String> stringMessage)
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