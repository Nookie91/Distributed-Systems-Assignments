package activitystreamer.message;

import activitystreamer.server.Connection;

public class ServerAnnounceMessage extends Message 
{
    private final static String command = "SERVER_ANNOUNCE";
    private String id;
    private String hostname;
    private Integer load;
    private Integer port;
    private Integer serverLoad;
    private Integer distance;
    

    public ServerAnnounceMessage(String id, Integer load, String hostname, Integer port)
    {
        super(command);
        this.id = id;
        this.port = port;
        this.hostname = hostname;
        this.load = load;
        this.serverLoad = -1;
        this.distance = -1;
    }
    
    public ServerAnnounceMessage(String id, Integer load, String hostname, Integer port, Integer serverLoad, Integer distance)
    {
        super(command);
        this.id = id;
        this.port = port;
        this.hostname = hostname;
        this.load = load;
        this.serverLoad = serverLoad;
        this.distance = distance;
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
    
    public Integer getServerLoad()
    {
    	if (serverLoad == null)
    	{
    		serverLoad = -1;
    	}
    	return serverLoad;
    }
    
    public void incrementDistance()
    {
    	if(distance == null || distance == -1)
    	{
    		distance = -1;
    	}
    	else
    	{
    		distance ++;
    	}
    }
    
    public Integer getDistance()
    {
    	return distance;
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