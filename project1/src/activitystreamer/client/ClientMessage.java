package activitystreamer.client;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;

public class ClientMessage 
{
    private Map<String, String> messageContents;
    public JSONObject message;
    
    ClientMessage()
    {
        blankMessage();
    }

    public String messageString()
    {
        message = new JSONObject(messageContents);
        return message.toString();
    }

    public void blankMessage()
    {
        messageContents = new HashMap<String, String>();
        message = new JSONObject(messageContents);
    }

    public void authenticateMessage(String secret)
    {
        messageContents.put("command", "AUTHENTICATE");
        messageContents.put("secret", secret);
    }

    public void authenticateFailMessage(String secret)
    {
        messageContents.put("command", "AUTHENTICATION_FAIL");
        messageContents.put("info","the supplied secret is incorrect: " + secret);
    }

    public void loginMessage(String username, String secret)
    {
        messageContents.put("command", "LOGIN");
        messageContents.put("username",username);
        messageContents.put("secret", secret);
    }    

    public void loginSuccessMessage(String username)
    {
        messageContents.put("command", "LOGIN_SUCCESS");
        messageContents.put("info", "logged in as user " + username)
    }

    public void redirectMessage(String hostname, String port)
    {
        messageContents.put("command", "REDIRECT");
        messageContents.put("hostname", hostname);
        messageContents.put("port", port);
    }

    public void logoutMessage()
    {
        messageContents.put("command", "LOGOUT");
    }

    public void activityMessage(String username, String secret, JSONObject activityObject)
    {
        messageContents.put("command", "ACTIVITY_MESSAGE");
        messageContents.put("username", username);
        messageContents.put("secret", secret);
        messageContents.put("activity", activityObject.toString());
    }

    public void serverAnnounceMessage(String id, String load, String hostname, String port)
    {
        messageContents.put("command", "SERVER_ANNOUNCE");
        messageContents.put("id", id)
        messageContents.put("load", load);
        messageContents.put("hostname", hostname);
        messageContents.put("port", port);
    }

    public void activityBroadcastMessage(JSONObject activityObject)
    {
        messageContents.put("command", "ACTIVITY_BROADCAST");
        messageContents.put("activity", activityObject.toString());
    }

    public void registerMessage(String username, String secret)
    {
        messageContents.put("command", "REGISTER");
        messageContents.put("username",username);
        messageContents.put("secret", secret);
    }

    public void registerFailedMessage(String username)
    {
        messageContents.put("command", "REGISTER_FAILED");
        messageContents.put("info", username + " is already registered with the system");
    }

    public void registerSuccessMessage(String username)
    {
        messageContents.put("command", "REGISTER_SUCCESS");
        messageContents.put("info", "register success for " + username);
    }

    public void lockRequestMessage(String username, String secret)
    {
        messageContents.put("command", "LOCK_REQUEST");
        messageContents.put("username", username);
        messageContents.put("secret", secret);
    }

    public void lockDeniedMessage(String username, String secret)
    {
        messageContents.put("command", "LOCK_DENIED");
        messageContents.put("username", username);
        messageContents.put("secret", secret);
    }

    public void lockAllowedMessage(String username, String secret, String serverSecret)
    {
        messageContents.put("command", "LOCK_ALLOWED");
        messageContents.put("username", username);
        messageContents.put("secret", secret);
        messageContents.put("server", serverSecret);
    }    

}
