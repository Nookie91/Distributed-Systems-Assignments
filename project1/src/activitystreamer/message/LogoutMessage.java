package activitystreamer.message;

import java.util.Map;

public class LogoutMessage extends Message 
{
    private static String COMMAND = "LOGOUT";
    private static String[] keys = {"command"};

    public LogoutMessage()
    {
        super(COMMAND);
    }

    public LogoutMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }


}