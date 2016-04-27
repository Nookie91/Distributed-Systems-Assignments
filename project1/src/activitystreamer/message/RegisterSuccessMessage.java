package activitystreamer.message;

import java.util.Map;

public class RegisterSuccessMessage extends Message 
{
    private static String COMMAND = "REGISTER_SUCCESS";
    private static String[] keys = {"command", "info"};

    public RegisterSuccessMessage(String username)
    {
        super();
        message.put("command", COMMAND);
        message.put("info", "register success for " + username);
    }

    public RegisterSuccessMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }


}