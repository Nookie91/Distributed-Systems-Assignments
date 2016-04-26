package activitystreamer.message;

public class AuthenticateMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE";
    private static String[] keys = ["command", "secret"];

    AuthenticateMessage(String secret)
    {
        super();
        message.put("secret",secret);
    }

    AuthenticateMessage(String stringMessage)
    {
        super(stringMessage);
    }

    public String getSecret()
    {
        return message.getString("secret");
    }

    public boolean doesSecretMatch(String secret)
    {
        if(secret.compareTo(message.getString("secret")) == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}