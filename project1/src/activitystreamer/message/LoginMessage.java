package activitystreamer.message;

import java.util.Map;

import activitystreamer.server.Connection;

public class LoginMessage extends Message 
{
    private static String COMMAND = "LOGIN";
    private static String[] keys = {"command", "username", "secret"};

    public LoginMessage(String username, String secret)
    {
        super();
        message.put("command", COMMAND);
        message.put("username", username);
        message.put("secret", secret);
    }

    public LoginMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }

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
            		InvalidMessage error = new InvalidMessage("the received message did not contain a" + key);
                    con.writeMsg(error.messageToString());
                    return true;            		
            	}
            }
        }
        return false;
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
}