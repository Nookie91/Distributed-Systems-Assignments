package activitystreamer.message;

public class RegisterFailedeMessage extends Message 
{
    private static String COMMAND = "REGISTER_FAILED";
    private static String[] keys = ["command", "info"];

    RegisterFailedeMessage(String username)
    {
        super();
        message.put("info", username + " is already registered with the system");
    }

    RegisterFailedeMessage(String stringMessage)
    {
        super(stringMessage);
    }


}