package activitystreamer.message;

public class RegisterMessage extends Message 
{
    private static String COMMAND = "REGISTER";
    private static String[] keys = ["command", "username", "secret"];

    RegisterMessage(String username, String secret)
    {
        super();
        message.put("username",username);
        message.put("secret",secret);
    }

    RegisterMessage(String stringMessage)
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