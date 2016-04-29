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
    	int lastcomma;
    	StringBuilder msg = new StringBuilder(message);
    	msg.deleteCharAt(msg.length() - 1);
    	msg.deleteCharAt(0);
    	
    	while(msg.indexOf(":") != -1)
    	{
    		lastcomma = msg.indexOf(",");
    		if(lastcomma == -1)
    		{
    			if(msg.indexOf("{") != -1)
    			{
    				map.put(msg.substring(0,msg.indexOf(":")).replace("\"", ""),
            				msg.substring(msg.indexOf("{"),msg.lastIndexOf("}")+1));
                    msg.replace(0,msg.lastIndexOf("}")+2,"");
    			}
    			else
    			{
    				map.put(msg.substring(0,msg.indexOf(":")).replace("\"", ""), 
            				msg.substring(msg.indexOf(":") + 1).replace("\"", ""));
            		msg.replace(0,msg.length(),"");
    			}
    			
    		}
    		else if(msg.indexOf("{") == -1 || lastcomma < msg.indexOf("{"))
        	{
        		map.put(msg.substring(0,msg.indexOf(":")).replace("\"", ""), 
        				msg.substring(msg.indexOf(":") + 1,lastcomma).replace("\"", ""));
        		msg.replace(0,lastcomma+1,"");
        			
        	}
        	else
        	{
        		map.put(msg.substring(0,msg.indexOf(":")).replace("\"", ""),
        				msg.substring(msg.indexOf("{"),msg.lastIndexOf("}")+1));
                msg.replace(0,msg.lastIndexOf("}")+2,"");
        		
        	}
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