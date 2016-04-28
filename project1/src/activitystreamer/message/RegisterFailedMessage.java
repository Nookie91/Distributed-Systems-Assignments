package activitystreamer.message;

import java.util.Map;

public class RegisterFailedMessage extends Message 
{
    private static String COMMAND = "REGISTER_FAILED";
    private static String[] keys = {"command", "info"};

    public RegisterFailedMessage(String username)
    {
        super();
        message.put("command", COMMAND);
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

    public String getInfo()
    {
        return message.get("info");
    }


}