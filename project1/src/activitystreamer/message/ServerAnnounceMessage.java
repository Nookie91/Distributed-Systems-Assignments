package activitystreamer.message;

import java.util.Map;

public class ServerAnnounceMessage extends Message 
{
    private static String COMMAND = "SERVER_ANNOUNCE";
    private static String[] keys = {"command", "id", "load", "hostname", "port"};

    public ServerAnnounceMessage(String id, int load, String hostname, int port)
    {
        super(COMMAND);
        message.put("id",id);
        message.put("load",Integer.toString(load));
        message.put("hostname",hostname);
        message.put("port",Integer.toString(port));
    }

    public ServerAnnounceMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }

    public String getID()
    {
        return message.get("id");
    }

    public int getLoad()
    {
        return Integer.parseInt(message.get("load"));
    }

    public String getHostname()
    {
        return message.get("hostname");
    }

    public int getPort()
    {
        return Integer.parseInt(message.get("port"));
    }
}