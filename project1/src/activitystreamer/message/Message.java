package activitystreamer.message;

import org.json.simple.JSONObject;

import activitystreamer.server.Connection;

import java.util.HashMap;
import java.util.Map;

public abstract class Message
{
    Map<String,String> message;
    
    String COMMAND;

    Message()
    {
        message = new HashMap<String,String>();
    }

    Message(Map<String, String> stringMessage)
    {
    	message = stringMessage;
    	

    }
    
    public static Map<String, String> stringToMap(String message)
    {
    	Map<String,String> map = new HashMap<String,String>();
    	StringBuilder msg = new StringBuilder(message);
    	msg.deleteCharAt(msg.length() - 1);
    	msg.deleteCharAt(0);
    	message = msg.toString();
    	String[] pairs = message.split(",");
    	for (int i=0;i<pairs.length;i++) 
    	{
    	    String pair = pairs[i];
    	    String[] keyValue = pair.split(":");
    	    System.out.println(keyValue[0] + "   " + keyValue[1]);
    	    map.put(keyValue[0].replace("\"", ""), keyValue[1].replace("\"", ""));
    	}
    	
    	return map;
    }

    public static String incomingMessageType(Connection con, Map<String,String> message)
    {
        InvalidMessage error;
        
        String messageType;
        if(message.containsKey("command"))
        {
            messageType = message.get("command");
        }
        else
        {
            error = new InvalidMessage("the received message did not contain a command!");
            con.writeMsg(error.messageToString());
            return "";   
        }
        return messageType;
    }

    public static String incomingMessageType(Map<String,String> message)
    {
        
        String messageType;
        if(message.containsKey("command"))
        {
            messageType = message.get("command");
        }
        else
        {
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
                error = new InvalidMessage("the received message did not contain a " + key);
            	con.writeMsg(error.messageToString());
                return true;
            }
        }
        return false;
    }
    
    public abstract String[] getKeys();
    
}