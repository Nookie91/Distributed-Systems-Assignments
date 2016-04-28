package activitystreamer.message;

import java.util.Map;

import org.json.simple.JSONObject;

import activitystreamer.server.Connection;

public class ActivityMessage extends Message 
{
    private static String COMMAND = "ACTIVITY_MESSAGE";
    private static String[] keys = {"command", "username", "secret", "activity"};

    public ActivityMessage(String username, String secret, String activity)
    {
    	super();
        message.put("command", COMMAND);
        message.put("username",username);
        message.put("secret",secret);
        message.put("activity",activity);

    }

    public ActivityMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }


    @Override
    public boolean checkFields(Connection con)
    {
        for(String key: keys)
        {
            if(key.equals("secret") &&
            	message.containsKey("username")&&
            	message.get("username").equals("anomynous"))
            {
                    continue;
            }
            else
            {
            	if(!message.containsKey(key))
            	{
            		InvalidMessage error = new InvalidMessage("the received message did not contain a " + key);
                    con.writeMsg(error.messageToString());
                    return true;            		
            	}
            }
        }
        return false;
    }
    
    public String messageToString()
    {
    	String msg = JSONObject.toJSONString(message);
    	msg = msg.replace("\"{", "{").replace("}\"", "}");
    	msg = msg.replace("\\\"", "\"").replace("\"\\", "\"");
        return msg;
    }

    public String getUsername()
    {
        return message.get("username");
    }

    public String getSecret()
    {
        if(message.get("username").equals("anomynous"))
        {
            return "";
        }
        return message.get("secret");
    }

    public String getActivity()
    {
        return message.get("activity");
    }
}