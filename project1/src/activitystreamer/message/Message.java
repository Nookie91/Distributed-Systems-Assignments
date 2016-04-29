package activitystreamer.message;

import com.google.gson.Gson;

import activitystreamer.server.Connection;

public class Message
{   
    private String command;

    public Message(String command)
    {
        this.command = command;
    }


    public String getCommand()
    {
    	if(command == null)
    	{
    		return "";
    	}
    	else
    	{
    		return command;
    	}
    }
    
    public String messageToString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    public boolean checkFields(Connection con)
    {
        return false;
    }    
}