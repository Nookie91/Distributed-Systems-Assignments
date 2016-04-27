package activitystreamer.message;

public class ActivityBroadcastMessage extends Message 
{
    private static String COMMAND = "ACTIVITY_BROADCAST";
    private static String[] keys = ["command", "activity"];

    ActivityBroadcastMessage(String activity, String username)
    {
        super();
        message.put("activity",activity);
        processActivity(username);
    }

    ActivityBroadcastMessage(String stringMessage)
    {
        super(stringMessage);
    }

    public String getActivity()
    {
        return message.getString("activity");
    }

    private void processActivity(String username)
    {
        JSONobject activity = new JSONobject(message.getString("activity"));
        activity.put("authenticated_user",username);
        message.put("activity", activity.toString());
    }
}