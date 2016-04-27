package activitystreamer.message;

public class LockAllowedMessage extends Message 
{
    private static String COMMAND = "LOCK_ALLOWED";
    private static String[] keys = ["command", "username", "secret", "server"];

    LockAllowedMessage(String username, String secret, String server)
    {
        super();
        message.put("username",username);
        message.put("secret",secret);
        message.put("server",server);
    }

    LockAllowedMessage(String stringMessage)
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

    public String getServer()
    {
        return message.getString("server");
    }
}