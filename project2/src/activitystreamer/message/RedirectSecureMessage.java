package activitystreamer.message;

import activitystreamer.server.Connection;

public class RedirectSecureMessage extends Message
{
    private final static String command = "REDIRECT";
    private String hostname;
    private Integer port;
    private Boolean secure;

    public RedirectSecureMessage(String hostname, Integer port, Boolean secure)
    {
        super(command);
        this.hostname = hostname;
        this.port = port;
        this.secure = secure;
    }

    public String getHostname()
    {
        return hostname;
    }

    public Integer getPort()
    {
        return port;
    }

    public Boolean getSecure()
    {
        return secure;
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
