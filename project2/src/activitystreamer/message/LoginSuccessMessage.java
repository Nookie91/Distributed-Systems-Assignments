package activitystreamer.message;

import activitystreamer.server.Connection;

public class LoginSuccessMessage extends Message 
{
    private final static String command = "LOGIN_SUCCESS";
    private String info;

    public LoginSuccessMessage(String username)
    {
        super(command);
        this.info = "logged in as user " + username;
    }

    public String getInfo()
    {
        return info;
    }

        public boolean checkFields(Connection con)
    {
        InvalidMessage error;
        if(getInfo().equals("logged in as user "))
        {
            error = new InvalidMessage("the received message did not contain a username");
            con.writeMsg(error.messageToString());
            return true;
        }
        return false;
    }
}