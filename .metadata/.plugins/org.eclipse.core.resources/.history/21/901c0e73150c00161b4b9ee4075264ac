package activitystreamer.message;

import org.json.simple.JSONObject;

public abstract class Message
{
    JSONObject message;
    private abstract static String[] keys;

    Message()
    {
        message = new JSONObject();
        message.put("command", COMMAND);
    }

    Message(String stringMessage)
    {
        message = new JSONObject(stringMessage);
    }

    public static String incomingMessageType(String stringMessage)
    {
        JSONObject incomingMessage = new JSONObject(stringMessage);
        String messageType;
        try
        {
            messageType = incomingMessage.get("command");
        }
        catch(JSONException e)
        {
            InvalidMessage error = new InvalidMessage("the received message did not contain a command");
            error.send();
            return "";   
        }
        return messageType;
    }

    public String messageToString()
    {
        return message.toString();
    }

    public boolean checkFields()
    {
        for(String key in keys)
        {
            try
            {
                message.getString(key);
            }
            catch(JSONException e)
            {
                InvalidMessage error = new InvalidMessage("the received message did not contain a" + key);
                error.sendMessage();
                return true;
            }
        }
        return false;
    }

    public void sendMessage()
    {

    }
}