package activitystreamer.server;

public class AuthorisedServer
{
    private String id;
    private int load;
    private String hostname;
    private int port;

    AuthorisedServer(String id, int load, String hostname, int port)
    {
        this.id = id;
        this.load = load;
        this.hostname = hostname;
        this.port = port;
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
}