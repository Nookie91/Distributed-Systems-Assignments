package activitystreamer.message;

import activitystreamer.server.Connection;

public class RedirectMessage extends Message 
{
    private final static String command = "REDIRECT";
    private String hostname;
    private Integer port;

    public RedirectMessage(String hostname, Integer port)
    {
        super(command);
        this.hostname = hostname;
        this.port = port;
    }
    
    public String getHostname()
    {
        return hostname;
    }

    public Integer getPort()
    {
        return port;
    }

    public boolean checkFields(Connection con)
    {
        InvalidMessage error;
        if(getHostname() == null)
        {
            error = new InvalidMessage("the received message did not contain a hostname");
            con.writeMsg(error.messageToString());
            return true;
        }
        if(getPort() == null)
        {
            error = new InvalidMessage("the received message did not contain a port");
            con.writeMsg(error.messageToString());
            return true;
        }
        return false;
    }
}