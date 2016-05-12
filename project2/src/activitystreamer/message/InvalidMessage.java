package activitystreamer.message;

import activitystreamer.server.Connection;

public class InvalidMessage extends Message 
{
    private final static String command = "INVALID_MESSAGE";
    private String info;

    public InvalidMessage(String info)
    {
        super(command);
        this.info = info;
    }
    
    public String getInfo()
    {
        return info;
    }

    public boolean checkFields(Connection con)
    {
        InvalidMessage error;
        if(getInfo() == null)
        {
            error = new InvalidMessage("the received message did not contain any info");
            con.writeMsg(error.messageToString());
            return true;
        }
        
        return false;
    }

}