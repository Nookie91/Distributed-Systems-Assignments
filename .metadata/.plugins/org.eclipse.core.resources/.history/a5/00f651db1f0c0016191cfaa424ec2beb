package activitystreamer.message;

public class LockRequestMessage extends Message 
{
    private static String COMMAND = "LOCK_REQUEST";
    private static String[] keys = ["command", "username", "secret"];

    LockRequestMessage(String username, String secret)
    {
        super();
        message.put("username",username);
        message.put("secret",secret);
    }

    LockRequestMessage(String stringMessage)
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