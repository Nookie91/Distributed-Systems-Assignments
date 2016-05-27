package activitystreamer.server;

// Class used to store information on broadcasted servers.
public class AuthorisedServer
{
    private String id;
    private int load;
    private int serverLoad;
    private String hostname;
    private int port;
    private int distance;

    
    AuthorisedServer(String id, int load, String hostname, int port, int serverLoad, int distance)
    {
    	this.id = id;
        this.load = load;
        this.hostname = hostname;
        this.port = port;
        this.serverLoad = serverLoad;
        this.distance = distance;
    }

    public String getID()
    {
        return id;
    }

    public int getLoad()
    {
        return load;
    }

    public void updateLoad(int load)
    {
        this.load = load;
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }
    
    public int getServerLoad()
    {
    	return serverLoad;
    }
    
    public void updateServerLoad(int serverLoad)
    {
    	this.serverLoad = serverLoad;
    }
    
    public Integer getDistance()
    {
    	return distance;
    }
}