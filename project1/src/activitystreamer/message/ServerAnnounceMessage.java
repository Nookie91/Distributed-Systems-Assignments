package activitystreamer.message;

import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;

public class ServerAnnounceMessage extends Message 
{
    private static String COMMAND = "SERVER_ANNOUNCE";
    private static String[] keys = {"command", "id", "load", "hostname", "port"};

    public ServerAnnounceMessage(String id, int load, String hostname, int port)
    {
        super();
        message.put("command", COMMAND);
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
    
    public String messageToString()
    {
    	Map<String,String> msg1 = new HashMap<String,String>();
    	Map<String,Integer> msg2 = new HashMap<String,Integer>();
    	
    	msg1.put("command",message.get("command"));
    	msg1.put("id",message.get("id"));
    	msg1.put("hostname",message.get("hostname"));
    	
    	msg2.put("load",getLoad());
    	msg2.put("port",getPort());
    	
    	String msg = JSONObject.toJSONString(msg1) + JSONObject.toJSONString(msg2);
    	msg = msg.replace("}{", ",");
        return msg;
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