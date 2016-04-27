package activitystreamer.message;

import org.json.simple.JSONObject;

import activitystreamer.server.Connection;

import java.util.HashMap;
import java.util.Map;

public abstract class Message
{
    Map<String,String> message;
    
    String COMMAND;

    Message(String command)
    {
        message = new HashMap<String,String>();
        message.put("command", command);
    }

    Message(Map<String, String> stringMessage)
    {
    	message = stringMessage;
    	

    }
    
    public static Map<String, String> stringToMap(String message)
    {
    	Map<String,String> map = new HashMap<String,String>();
    	String[] pairs = message.split(",");
    	for (int i=0;i<pairs.length;i++) 
    	{
    	    String pair = pairs[i];
    	    String[] keyValue = pair.split(":");
    	    map.put(keyValue[0], keyValue[1]);
    	}
    	
    	return map;
    }

    public static String incomingMessageType(Connection con, String stringMessage)
    {
        Map<String,String> incomingMessage = new HashMap<String,String>();
        InvalidMessage error;
        incomingMessage = stringToMap(stringMessage);
        String messageType;
        if(incomingMessage.containsKey("command"))
        {
            messageType = incomingMessage.get("command");
        }
        else
        {
            error = new InvalidMessage("the received message did not contain a command");
            con.writeMsg(error.toString());
            return "";   
        }
        return messageType;
    }

    public String messageToString()
    {
        return JSONObject.toJSONString(message);
    }

    public boolean checkFields(Connection con)
    {
    	InvalidMessage error;
        for(String key: getKeys())
        {
            if(!message.containsKey(key))
            {
                error = new InvalidMessage("the received message did not contain a" + key);
            	con.writeMsg(error.toString());
                return true;
            }
        }
        return false;
    }
    
    public abstract String[] getKeys();
    
}