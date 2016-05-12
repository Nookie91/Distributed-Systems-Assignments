package activitystreamer.message;

import org.json.simple.JSONObject;

import activitystreamer.server.Connection;

public class ActivityBroadcastMessage extends Message 
{
    private final static String command = "ACTIVITY_BROADCAST";
    private JSONObject activity;
    private String username;

    public ActivityBroadcastMessage(JSONObject activity, String username)
    {

    	super(command);
        this.activity = activity;
        this.username = username;
        this.activity.put("authenticated_user",username);
    }

    public JSONObject getActivity()
    {
        return activity;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean checkFields(Connection con)
     {
        InvalidMessage error;
        if(getUsername() == null)
        {
            error = new InvalidMessage("the received message did not contain a username");
            con.writeMsg(error.messageToString());
            return true;
        }
        if(getActivity() == null)
        {
            error = new InvalidMessage("the received message did not contain an activity");
            con.writeMsg(error.messageToString());
            return true;
        }
        return false;
     }

     
}