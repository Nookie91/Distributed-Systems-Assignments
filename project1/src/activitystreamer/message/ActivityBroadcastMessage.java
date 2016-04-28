package activitystreamer.message;

import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    public JSONObject getActivityObject()
    {
    	Map<String,String> activity = stringToMap(getActivity());
    	JSONParser parser = new JSONParser();
        
        try 
        {
			return (JSONObject) parser.parse(JSONObject.toJSONString(activity));
		} 
        catch (ParseException e) 
		{
			return null;
		}
    }

    private void processActivity(String username)
    {
    	Map<String,String> activity = stringToMap(getActivity());
       
        activity.put("authenticated_user",username);
        message.put("activity", JSONObject.toJSONString(activity));
    }
    
    public String[] getKeys()
    {
    	return keys;
    }
}