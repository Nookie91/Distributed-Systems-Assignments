package activitystreamer.message;

public class LogoutMessage extends Message 
{
    private final static String command = "LOGOUT";

    public LogoutMessage()
    {
        super(command);
    }
    
}