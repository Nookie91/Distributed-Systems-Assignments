package activitystreamer.message;

public class AuthenticateFailMessage extends Message 
{
    private static String COMMAND = "AUTHENTICATE_FAIL";
    private static String[] keys = ["command", "info"];

    AuthenticateFailMessage(String secret)
    {
        super();
        message.put("info", secret);
    }

    AuthenticateFailMessage(String stringMessage)
    {
        super(stringMessage);
    }


}