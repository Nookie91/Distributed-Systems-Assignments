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
    	String temp =  JSONObject.toJSONString(activity);
    	temp = temp.replace("\"{", "{").replace("}\"", "}");
    	temp = temp.replace("\\", "");

        try 
        {
			return (JSONObject) parser.parse(temp);
		} 
        catch (ParseException e) 
		{
			return null;
		}
    }

    public String messageToString()
    {
    	String msg = JSONObject.toJSONString(message);
    	msg = msg.replace("\"{", "{").replace("}\"", "}");
    	msg = msg.replace("\\", "");
        return msg;
    }
    
    private void processActivity(String username)
    {
    	Map<String,String> activity = stringToMap(getActivity());
    	String temp;
        activity.put("authenticated_user",username);
        temp = JSONObject.toJSONString(activity);
        temp = temp.replace("\"{", "{").replace("}\"", "}");
    	temp = temp.replace("\\", "");
        message.put("activity", temp);
    }
    
    public String[] getKeys()
    {
    	return keys;
    }
}