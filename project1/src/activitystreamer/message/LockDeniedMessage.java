package activitystreamer.message;

public class LockDeniedMessage extends Message 
{
    private static String COMMAND = "LOCK_DENIED";
    private static String[] keys = ["command", "username", "secret"];

    LockDeniedMessage(String username, String secret)
    {
        super();
        message.put("username",username);
        message.put("secret",secret);
    }

    LockDeniedMessage(String stringMessage)
    {
        super(stringMessage);
    }

    public String getUsername()
    {
        return message.getString("username");
    }    

    public String getSecret()
    {
        return message.getString("secret");
    }

}