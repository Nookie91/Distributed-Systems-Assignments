package activitystreamer.message;

import java.util.HashMap;
import java.util.Map;

public class ActivityBroadcastMessage extends Message 
{
    private static String COMMAND = "ACTIVITY_BROADCAST";
    private static String[] keys = {"command", "activity"};

    public ActivityBroadcastMessage(String activity, String username)
    {
        super();
        message.put("command", COMMAND);
        message.put("activity",activity);
        processActivity(username);
    }

    public ActivityBroadcastMessage(Map<String,String> stringMessage)
    {
        super(stringMessage);
    }

    public String getActivity()
    {
        return message.get("activity");
    }

    private void processActivity(String username)
    {
    	Map<String,String> activity = new HashMap<String,String>();
    	String[] pairs = message.get("activity").split(",");
    	for (int i=0;i<pairs.length;i++) 
    	{
    	    String pair = pairs[i];
    	    String[] keyValue = pair.split(":");
    	    activity.put(keyValue[0], keyValue[1]);
    	}
       
        activity.put("authenticated_user",username);
        message.put("activity", activity.toString());
    }
    
    public String[] getKeys()
    {
    	return keys;
    }
}