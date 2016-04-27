package activitystreamer.message;

import java.util.Map;

public class RegisterFailedMessage extends Message 
{
    private static String COMMAND = "REGISTER_FAILED";
    private static String[] keys = {"command", "info"};

    public RegisterFailedMessage(String username)
    {
        super(COMMAND);
        message.put("info", username + " is already registered with the system");
    }

    public RegisterFailedMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }


}