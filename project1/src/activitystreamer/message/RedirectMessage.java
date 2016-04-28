package activitystreamer.message;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class RedirectMessage extends Message 
{
    private static String COMMAND = "REDIRECT";
    private static String[] keys = {"command", "hostname", "port"};

    public RedirectMessage(String hostname, int port)
    {
        super();
        message.put("command", COMMAND);
        message.put("hostname", hostname);
        message.put("port",Integer.toString(port));
    }

    public String messageToString()
    {
    	Map<String,String> msg1 = new HashMap<String,String>();
    	Map<String,Integer> msg2 = new HashMap<String,Integer>();
    	
    	msg1.put("command",message.get("command"));;
    	msg1.put("hostname",message.get("hostname"));
    	
    	msg2.put("port",getPort());
    	
    	String msg = JSONObject.toJSONString(msg1) + JSONObject.toJSONString(msg2);
    	msg = msg.replace("}{", ",");
        return msg;
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