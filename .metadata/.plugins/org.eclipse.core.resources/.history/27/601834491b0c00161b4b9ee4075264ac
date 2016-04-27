package activitystreamer.message;

public class InvalidMessage extends Message 
{
    private static String COMMAND = "INVALID_MESSAGE";
    private static String[] keys = ["command", "info"];

    Message(String info)
    {
        super();
        message.put("info",info);
    }

    Message(String stringMessage)
    {
        super(stringMessage);
    }

    public String getInfo()
    {
        return message.getString("info");
    }


}