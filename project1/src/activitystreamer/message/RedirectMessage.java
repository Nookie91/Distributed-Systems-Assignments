package activitystreamer.message;

import java.util.Map;

public class RedirectMessage extends Message 
{
    private static String COMMAND = "REDIRECT";
    private static String[] keys = {"command", "hostname", "port"};

    public RedirectMessage(String hostname, int port)
    {
        super(COMMAND);
        message.put("hostname", hostname);
        message.put("port",Integer.toString(port));
    }

    public RedirectMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }
    
    public String[] getKeys()
    {
    	return keys;
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