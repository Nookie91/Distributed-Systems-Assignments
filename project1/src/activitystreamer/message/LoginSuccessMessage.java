package activitystreamer.message;

import java.util.Map;

public class LoginSuccessMessage extends Message 
{
    private static String COMMAND = "LOGIN_SUCCESS";
    private static String[] keys = {"command", "info"};

    public LoginSuccessMessage(String username)
    {
        super();
        message.put("command", COMMAND);
        message.put("info", "logged in as user " + username);
    }

    public LoginSuccessMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }


}