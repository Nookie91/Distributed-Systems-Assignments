package activitystreamer.message;

import org.json.simple.JSONObject;

import activitystreamer.server.Connection;

public class ActivityMessage extends Message 
{
    private final static String command = "ACTIVITY_MESSAGE";
    private String username;
    private String secret;
    private JSONObject activity;

    public ActivityMessage(String username, String secret, JSONObject activity)
    {
    	super(command);
        this.username = username;
        this.secret = secret;
        this.activity = activity;
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
        if(getUsername().equals("anonymous"))
        {
            if(getSecret() == null)
            {
                 error = new InvalidMessage("the received message did not contain a secret");
                 con.writeMsg(error.messageToString());
                 return true;
            }
            
        } 
        if(getActivity() == null)
        {
            error = new InvalidMessage("the received message did not contain a JSON activity");
            con.writeMsg(error.messageToString());
            return true;
        }
        return false;
    }
    


    public String getUsername()
    {
        return username;
    }

    public String getSecret()
    {
        return secret;
    }

    public JSONObject getActivity()
    {
        return activity;
    }
}