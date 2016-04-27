package activitystreamer.message;

import java.util.Map;

public class InvalidMessage extends Message 
{
    private static String COMMAND = "INVALID_MESSAGE";
    private static String[] keys = {"command", "info"};

    public InvalidMessage(String info)
    {
        super();
        message.put("command", COMMAND);
        message.put("info",info);
    }

    public InvalidMessage(Map<String,String> stringMessage)
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