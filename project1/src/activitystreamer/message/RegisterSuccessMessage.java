package activitystreamer.message;

public class RegisterSuccessMessage extends Message 
{
    private static String COMMAND = "REGISTER_SUCCESS";
    private static String[] keys = ["command", "info"];

    RegisterSuccessMessage()
    {
        super();
        message.put("info", "");
    }

    RegisterSuccessMessage(String stringMessage)
    {
        super(stringMessage);
    }


}