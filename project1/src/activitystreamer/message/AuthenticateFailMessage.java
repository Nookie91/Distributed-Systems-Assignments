package activitystreamer.message;

import java.util.Map;

public class AuthenticateFailMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE_FAIL";
    private static String[] keys = {"command", "info"};

    public AuthenticateFailMessage(String secret)
    {
        super();
        message.put("command", COMMAND);
        message.put("info", secret);
    }

    public AuthenticateFailMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }


}