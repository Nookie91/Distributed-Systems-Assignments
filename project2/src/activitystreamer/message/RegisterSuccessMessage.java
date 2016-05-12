package activitystreamer.message;

import activitystreamer.server.Connection;

public class RegisterSuccessMessage extends Message 
{
    private final static String command = "REGISTER_SUCCESS";
    private String info;

    public RegisterSuccessMessage(String username)
    {
        super(command);
        this.info = "register success for " + username;
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