package activitystreamer.message;

public class LoginMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE";
    private static String[] keys = ["command", "username", "secret"];

    LoginMessage(String username, String secret)
    {
        super();
        message.put("username", username);
        message.put("secret", secret);
    }

    LoginMessage(String stringMessage)
    {
        super(stringMessage);
    }

    @override
    public boolean checkFields()
    {
        for(String key in keys)
        {
            try
            {
                if(key.equalTo("secret") == 0)
                {
                    if(message.getString("username").equalTo("anomynous") == 0)
                    {
                        continue;
                    }
                }
            }
            catch(JSONException e)
            {
                InvalidMessage error = new InvalidMessage("the received message did not contain a username");
                error.sendMessage();
                return true;
            }
            
            try
            {
                message.getString(key);
            }
            catch(JSONException e)
            {
                InvalidMessage error = new InvalidMessage("the received message did not contain a" + key);
                error.sendMessage();
                return true;
            }
        }
        return false;
    }

    public String getUsername()
    {
        return message.getString("username");
    }

    public String getSecret()
    {
        if(message.getString("username").equalTo("anomynous") == 0)
        {
            return "";
        }
        return message.getString("secret");
    }
}