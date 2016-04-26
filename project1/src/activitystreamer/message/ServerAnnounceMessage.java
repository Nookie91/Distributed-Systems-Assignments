package activitystreamer.message;

public class ServerAnnounceMessage extends Message 
{
    private static String COMMAND = "SERVER_ANNOUNCE";
    private static String[] keys = ["command", "id", "load", "hostname", "port"];

    Message(String id, int load, String hostname, int port)
    {
        super();
        message.put("id",id);
        message.put("load",load);
        message.put("hostname",hostname);
        message.put("port",port);
    }

    Message(String stringMessage)
    {
        super(stringMessage);
    }

    public String getID()
    {
        return message.getString("id");
    }

    public int getLoad()
    {
        return message.getInt("load");
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