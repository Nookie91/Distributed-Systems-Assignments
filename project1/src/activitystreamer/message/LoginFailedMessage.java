package activitystreamer.message;

public class oginFailedLMessage extends Message 
{
    private static String COMMAND = "LOGIN_FAILED";
    private static String[] keys = ["command", "info"];

    Message()
    {
        super();
        message.put("info", "attempt to login with wrong secret")
    }

    Message(String stringMessage)
    {
        super(stringMessage);
    }


}