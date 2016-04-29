package activitystreamer.message;

import activitystreamer.server.Connection;

public class ServerAnnounceMessage extends Message 
{
    private final static String command = "SERVER_ANNOUNCE";
    private String id;
    private String hostname;
    private Integer load;
    private Integer port;

    public ServerAnnounceMessage(String id, Integer load, String hostname, Integer port)
    {
        super(command);
        this.id = id;
        this.port = port;
        this.hostname = hostname;
        this.load = load;
    }
    
    public String getID()
    {
        return id;
    }

    public Integer getLoad()
    {
        return load;
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
        if(getLoad() == null)
        {
            error = new InvalidMessage("the received message did not contain a load");
            con.writeMsg(error.messageToString());
            return true;
        } 
        if(getID() == null)
        {
            error = new InvalidMessage("the received message did not contain an ID");
            con.writeMsg(error.messageToString());
            return true;
        } 
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