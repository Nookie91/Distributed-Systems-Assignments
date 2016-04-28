package activitystreamer.message;

import java.util.Map;

public class AuthenticateFailMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE_FAIL";
    private static String[] keys = {"command", "info"};

    public AuthenticateFailMessage(String info)
    {
        super();
        message.put("command", COMMAND);
        message.put("info", info);
    }

    public AuthenticateFailMessage(Map<String,String> stringMessage)
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