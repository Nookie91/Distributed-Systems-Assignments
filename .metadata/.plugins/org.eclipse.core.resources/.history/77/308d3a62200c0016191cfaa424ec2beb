package activitystreamer.message;

public class LoginSuccessMessage extends Message 
{
    private static String COMMAND = "LOGIN_SUCCESS";
    private static String[] keys = ["command", "info"];

    Message(String username)
    {
        super();
        message.put("info", "logged in as user " + username);
    }

    Message(String stringMessage)
    {
        super(stringMessage);
    }


}