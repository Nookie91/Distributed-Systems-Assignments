package activitystreamer.message;

import java.util.Map;

public class LoginFailedMessage extends Message 
{
    private static String COMMAND = "LOGIN_FAILED";
    private static String[] keys = {"command", "info"};

    public LoginFailedMessage()
    {
        super();
        message.put("command", COMMAND);
        message.put("info", "attempt to login with wrong secret");
    }

    public LoginFailedMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }

    public String getInfo()
    {
        return message.get("info");
    }


}