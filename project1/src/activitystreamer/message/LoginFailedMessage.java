package activitystreamer.message;

import activitystreamer.server.Connection;

public class LoginFailedMessage extends Message 
{
    private final static String command = "LOGIN_FAILED";
    private String info;

    public LoginFailedMessage()
    {
        super(command);
        this.info = "attempt to login with wrong secret";
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