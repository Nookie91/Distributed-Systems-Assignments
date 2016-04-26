package activitystreamer.message;

public class RedirectMessage extends Message 
{
    private static String COMMAND = "REDIRECT";
    private static String[] keys = ["command", "hostname", "port"];

    RedirectMessage(String hostname, int port)
    {
        super();
        message.put("hostname", hostname);
        message.put("port",port);
    }

    RedirectMessage(String stringMessage)
    {
        super(stringMessage);
    }

    public String getHostname()
    {
        return message.getString("hostname");
    }

    public int getPort()
    {
        return message.getInt("port");
    }
}