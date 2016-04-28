package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import activitystreamer.message.*;
import activitystreamer.util.Settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ControlSolution extends Control 
{
	private static final Logger log = LogManager.getLogger();
	
	/*
	 * additional variables as needed
	 */
	
	public static enum ConnectionType {CLIENT,SERVER,UNDEFINED};
	private static String serverID;	

	private static int numberOfLoggedInUsers;
	private static Map<String, String> userDatabase;
	private static ArrayList<AuthorisedServer> servers;

	private static Map<String,Connection> currentRegisterRequests;
	private static Map<String,Connection> returnLockRequests;
	private static Map<String,ArrayList<Connection>> currentLockRequests;
	private static Map<Connection,ConnectionType> connectionType;
	private static Map<Connection, String> connectionStatus;

	// since control and its subclasses are singleton, we get the singleton this way
	public static ControlSolution getInstance() 
	{
		if(control==null)
		{
			control=new ControlSolution();
		} 
		return (ControlSolution) control;
	}
	
	public ControlSolution() 
	{
		super();
		/*
		 * Do some further initialization here if necessary
		 */
		numberOfLoggedInUsers = 0;
		userDatabase = new HashMap<String, String>();
		servers = new ArrayList<AuthorisedServer>();

		currentRegisterRequests = new HashMap<String,Connection>();
		returnLockRequests = new HashMap<String,Connection>();
		currentLockRequests = new HashMap<String,ArrayList<Connection>>();
		connectionType = new HashMap<Connection,ConnectionType>();
		connectionStatus = new HashMap<Connection,String>();
		
		serverID = Settings.nextSecret();
		
		// check if we should initiate a connection and do so if necessary
		initiateConnection();
		// start the server's activity loop
		// it will call doActivity every few seconds
		start();
	}
	
	
	/*
	 * a new incoming connection
	 */
	@Override
	public Connection incomingConnection(Socket s) throws IOException
	{
		Connection con = super.incomingConnection(s);
		/*
		 * do additional things here
		 */
		System.out.println(s);
		connectionType.put(con, ConnectionType.UNDEFINED);
		connectionStatus.put(con,null);
		return con;
	}
	
	/*
	 * a new outgoing connection
	 */
	@Override
	public Connection outgoingConnection(Socket s) throws IOException
	{
		Message message;
		Connection con = super.outgoingConnection(s);
		/*
		 * do additional things here
		 */
		
		message = new AuthenticateMessage(Settings.getSecret());
		con.writeMsg(message.messageToString());
		connectionType.put(con, ConnectionType.SERVER);
		connectionStatus.put(con,"");
		
		return con;
	}
	
	
	/*
	 * the connection has been closed
	 */
	@Override
	public void connectionClosed(Connection con)
	{
		super.connectionClosed(con);
		/*
		 * do additional things here
		 */

		connectionType.remove(con);
		connectionStatus.remove(con);
        decrementUsers();
	}
	
	
	/*
	 * process incoming msg, from connection con
	 * return true if the connection should be closed, false otherwise
	 */
	@Override
	public synchronized boolean process(Connection con,String msg)
	{
		/*
		 * do additional work here
		 * return true/false as appropriate
		 */

		InvalidMessage error;
		
		log.info(msg);
		Map<String,String> mapMsg = Message.stringToMap(msg);

		switch(Message.incomingMessageType(con,mapMsg))
		{
			case "":
				error = new InvalidMessage("the received message contained a blank command");
                con.writeMsg(error.messageToString());
                log.error("the received message contained a blank command");
                return true;

            case "AUTHENTICATE":
            	return authenticate(con,mapMsg);

            case "AUTHENTICATION_FAIL":
            	return true;

            case "LOGIN":
            	return login(con,mapMsg);
            	            	
            case "LOGOUT":
            	return true;

            case "ACTIVITY_MESSAGE":
            	return activityMessage(con,mapMsg);

            case "SERVER_ANNOUNCE":
            	return serverAnnounce(con,mapMsg);

            case "ACTIVITY_BROADCAST":
            	return activityBroadcast(con,mapMsg);

            case "REGISTER":
            	return register(con,mapMsg);

            case "LOCK_REQUEST":
            	return lockRequest(con,mapMsg);
            	
            case "LOCK_DENIED":
            	return lockDenied(con,mapMsg);

			case "LOCK_ALLOWED":
				return lockAllowed(con,mapMsg);
			default:
                return true;
		}
	}

	private boolean checkLogin(String username, String secret,Connection con)
	{
		if(username.equals("anonymous")) 
		{
			return true;
		}
		else if(userDatabase.containsKey(username))
		{
			if(secret == null)
			{
				return false;
			}
			else if(secret.equals(userDatabase.get(username)))
			{
				return true;
			}
			else
			{
				return false;
			}	
		}
		else if(connectionStatus.get(con).equals(null))
		{
			return false;
		}
		else
		{
			return true;	
		}
	}

	
	
	/*
	 * Called once every few seconds
	 * Return true if server should shut down, false otherwise
	 */
	@Override
	public boolean doActivity()
	{
		/*
		 * do additional work here
		 * return true/false as appropriate
		 */

		serverBroadcast();
		
		return false;
	}
	
		/*
	 * Other methods as needed
	 */

	private void incrementUsers()
	{
		numberOfLoggedInUsers ++;
	}

	private void decrementUsers()
	{
		numberOfLoggedInUsers --;
	}

	private void serverBroadcast()
	{
		ServerAnnounceMessage message = new ServerAnnounceMessage(serverID,
																  numberOfLoggedInUsers,
																  Settings.getLocalHostname(), 
																  Settings.getLocalPort()
																 );
		for(Connection con: getConnections())
		{
			if(connectionType.get(con) == ConnectionType.SERVER)
			{
				con.writeMsg(message.messageToString());
			}
		}
	}

    private boolean serverAnnounce(Connection con, Map<String,String> mapMsg)
    {
        ServerAnnounceMessage msg = new ServerAnnounceMessage(mapMsg);;
        InvalidMessage errorMsg;
        if(connectionType.get(con) == ConnectionType.SERVER 
           && connectionStatus.get(con).equals("")
          )
        {
            boolean newServer = true;
            
            if(msg.checkFields(con))
            {
                return true;
            }
            for(AuthorisedServer server:servers)
            {
                if(server.getID().equals(msg.getID()))
                {
                    server.updateLoad(msg.getLoad());
                    newServer = false;
                    break;
                }
            }
            if(newServer == true)
            {
                servers.add(new AuthorisedServer(msg.getID(), 
                                                 msg.getLoad(),
                                                 msg.getHostname(),
                                                 msg.getPort()
                                                )
                            );
            }
            for(Connection connection: getConnections())
            {
                if(connectionType.get(connection) == ConnectionType.SERVER 
                   && !connection.equals(con)
                  )
                {
                    connection.writeMsg(msg.messageToString());
                }
            }
            return false;   
        }
        else
        {
            errorMsg = new InvalidMessage("server is not authenticated");
            con.writeMsg(errorMsg.messageToString());
            log.error("Server :" + msg.getID() + " is ");
            return true;
        }
    }

    private boolean activityBroadcast(Connection con, Map<String,String> mapMsg)
    {   
        ActivityBroadcastMessage msg = new ActivityBroadcastMessage(mapMsg);
        InvalidMessage errorMsg;
        if(connectionType.get(con) == ConnectionType.SERVER  
           && connectionStatus.get(con).equals("")
          )
        {
            if(msg.checkFields(con))
            {
                return true;
            }

            for(Connection connection: getConnections())
            {
                if(!connection.equals(con))
                {
                    connection.writeMsg(msg.messageToString());
                }
            }
            return false;   
        }
        else
        {
            errorMsg = new InvalidMessage("server is not authenticated");
            con.writeMsg(errorMsg.messageToString());
            return true;
        }
    }

    private boolean authenticate(Connection con, Map<String,String> mapMsg)
    {
        AuthenticateMessage msg = new AuthenticateMessage(mapMsg);
        AuthenticateFailMessage errorMsg;

        if(msg.checkFields(con))
        {
            return true;
        }

        if(msg.doesSecretMatch(Settings.getSecret()))
        {
            connectionType.put(con,ConnectionType.SERVER);
            connectionStatus.put(con,"");
            return false;
        }
        else
        {
            errorMsg = new AuthenticateFailMessage("the server secret is incorrect: " 
                                                + msg.getSecret()
                                               );
            con.writeMsg(errorMsg.messageToString());
            log.debug("the server secret is incorrect: " + msg.getSecret());
            return true;
        }
    }

    private boolean login(Connection con, Map<String,String> mapMsg)
    {
        LoginMessage msg = new LoginMessage(mapMsg);
        Message replyMsg;


        for(AuthorisedServer server: servers)
        {
            if(numberOfLoggedInUsers >= server.getLoad() + 2)
            {
                replyMsg = new RedirectMessage(server.getHostname(),
                                               server.getPort()
                                              );
                con.writeMsg(replyMsg.messageToString());
                log.info("Redirecting to Hostname: " + server.getHostname() 
                                         + " Port: " + server.getPort());
                return true;
            }
        }

        if(msg.checkFields(con))
        {
            return true;
        }

        if(checkLogin(msg.getUsername(),msg.getSecret(),con))
        {
            connectionStatus.put(con,msg.getUsername());
            connectionType.put(con, ConnectionType.CLIENT);
            incrementUsers();

            replyMsg = new LoginSuccessMessage(msg.getUsername());
            con.writeMsg(replyMsg.messageToString());
            log.info("Login Success - Hostname: " + msg.getUsername());
            return false;
        }
        else
        {
            replyMsg = new LoginFailedMessage();
            con.writeMsg(replyMsg.messageToString());
            log.info("Login Failed");
            return true;
        }    
    }

    private boolean activityMessage(Connection con, Map<String,String> mapMsg)
    {
        ActivityMessage msg = new ActivityMessage(mapMsg);
        ActivityBroadcastMessage broadcast;
        Message errorMsg;

        if(connectionStatus.get(con) == null)
        {
            errorMsg = new AuthenticateFailMessage("currently no user logged in");
            con.writeMsg(errorMsg.messageToString());
            log.debug("Activity message requires logged in user");
            return true;
        }

        if(msg.checkFields(con))
        {
            return true;
        }

        if(!msg.getUsername().equals("anonymous"))
        {
            if(msg.getUsername().equals(connectionStatus.get(con)) 
               && checkLogin(msg.getUsername(),msg.getSecret(),con)
              )
            {
                broadcast = new ActivityBroadcastMessage(msg.getActivity(),
                                                         msg.getUsername()
                                                        );
                for(Connection connection:getConnections())
                {
                    connection.writeMsg(broadcast.messageToString());
                    log.info("broadcasting activity object");
                }
                return false;
            }
            else
            {
                errorMsg = new AuthenticateFailMessage("username/secret do not match logged in user");
                con.writeMsg(errorMsg.messageToString());
                return true;    
            }
        }
        else
        {
            broadcast = new ActivityBroadcastMessage(msg.getActivity(),
                                                     msg.getUsername()
                                                     );
            for(Connection connection:getConnections())
            {
                connection.writeMsg(broadcast.messageToString());
                log.info("broadcasting activity object");
            }
            return false;
        }
    }

    private boolean register(Connection con, Map<String,String> mapMsg)
    {    
        RegisterMessage msg = new RegisterMessage(mapMsg);
        LockRequestMessage lockRequest;
        InvalidMessage errorMsg;
        Message replyMsg;
        ArrayList<Connection> sentLockRequests;

        if(msg.checkFields(con))
        {
            return true;
        }

        if(connectionStatus.get(con) != null)
        {
            errorMsg = new InvalidMessage("Cannot Register: Already logged in.");
            con.writeMsg(errorMsg.messageToString());
            log.error("Cannot Register: Already logged in.");
            return true;
        }
        else if(userDatabase.containsKey(msg.getUsername()))
        {
            replyMsg = new RegisterFailedMessage(msg.getUsername());
            con.writeMsg(replyMsg.messageToString());
            log.debug("register failed. username already registered");
            return true;
        }
        else
        {
            userDatabase.put(msg.getUsername(),msg.getSecret());
            lockRequest = new LockRequestMessage(msg.getUsername(),
                                                 msg.getSecret()
                                                );
            sentLockRequests = new ArrayList<Connection>();
            
            for(Connection connection: getConnections())
            {
                if(connectionType.get(connection) == ConnectionType.SERVER)
                {
                    connection.writeMsg(lockRequest.messageToString());
                    sentLockRequests.add(connection);
                }
            }

            if(sentLockRequests.isEmpty())
            {
                replyMsg = new RegisterSuccessMessage(msg.getUsername());
                con.writeMsg(replyMsg.messageToString());
                log.debug("register success for: " + msg.getUsername());
            }
            else
            {
                currentRegisterRequests.put(msg.getUsername(),con);
                currentLockRequests.put(msg.getUsername(),sentLockRequests);
            }
            
            return false;
        }
    }

    private boolean lockRequest(Connection con, Map<String,String> mapMsg)
    {    
        LockRequestMessage msg = new LockRequestMessage(mapMsg);
        InvalidMessage errorMsg;
        Message replyMsg;
        ArrayList<Connection> sentLockRequests;

        if(connectionType.get(con) == ConnectionType.SERVER 
           && connectionStatus.get(con).equals("")
          )
        {
            if(msg.checkFields(con))
            {
                return true;
            }
            if(userDatabase.containsKey(msg.getUsername()))
            {
                replyMsg = new LockDeniedMessage(msg.getUsername(),msg.getSecret());
                con.writeMsg(replyMsg.messageToString());
                log.debug("denying lock for: " + msg.getUsername() + " " + msg.getSecret());
                return false;
            }
            else
            {
                userDatabase.put(msg.getUsername(),msg.getSecret());
                returnLockRequests.put(msg.getUsername(),con);
                sentLockRequests = new ArrayList<Connection>();
                for(Connection connection: getConnections())
                {
                    if(connectionType.get(connection) == ConnectionType.SERVER 
                       && !connection.equals(con)
                      )
                    {
                        connection.writeMsg(msg.messageToString());
                        sentLockRequests.add(connection);
                    }
                }

                if(sentLockRequests.isEmpty())
                {
                    replyMsg = new LockAllowedMessage(msg.getUsername(),
                                                   msg.getSecret(),
                                                   serverID
                                                  );
                    con.writeMsg(replyMsg.messageToString());
                }
                else
                {
                    currentLockRequests.put(msg.getUsername(),sentLockRequests);
                }
                
                return false;
            }
        }
        else
        {
            errorMsg = new InvalidMessage("server is not authenticated");
            con.writeMsg(errorMsg.messageToString());
            return true;
        }
    }

    private boolean lockDenied(Connection con, Map<String,String> mapMsg)
    {    
        LockDeniedMessage msg = new LockDeniedMessage(mapMsg);
        InvalidMessage errorMsg;
        Message replyMsg;
        ArrayList<Connection> sentLockRequests;

        if(connectionType.get(con) == ConnectionType.SERVER 
           && connectionStatus.get(con).equals("")
          )

        {
            sentLockRequests = new ArrayList<Connection>();

            if(msg.checkFields(con))
            {
                return true;
            }

            if(userDatabase.containsKey(msg.getUsername()) 
               && userDatabase.get(msg.getUsername()).equals(msg.getSecret())
              )
            {
                userDatabase.remove(msg.getUsername());
            }

            for(Connection connection: getConnections())
            {
                if(connectionType.get(connection) == ConnectionType.SERVER
                   && !connection.equals(con)
                  )
                {
                    connection.writeMsg(msg.messageToString());
                    sentLockRequests.add(connection);
                }
            }

            if(currentLockRequests.containsKey(msg.getUsername()))
            {
                currentLockRequests.remove(msg.getUsername());
            }

            if(returnLockRequests.containsKey(msg.getUsername()))
            {                
                returnLockRequests.remove(msg.getUsername());
            }

            if(currentRegisterRequests.containsKey(msg.getUsername()))
            {
                replyMsg = new RegisterFailedMessage(msg.getUsername());
                currentRegisterRequests.get(msg.getUsername()).writeMsg(replyMsg.messageToString());
                currentRegisterRequests.get(msg.getUsername()).closeCon();
                currentRegisterRequests.remove(msg.getUsername());
            }

            return false;
        }
        else
        {
            errorMsg = new InvalidMessage("server is not authenticated");
            con.writeMsg(errorMsg.messageToString());
            return true;
        }
    }

    private boolean lockAllowed(Connection con, Map<String,String> mapMsg)
    {    
        LockAllowedMessage msg = new LockAllowedMessage(mapMsg);
        InvalidMessage errorMsg;
        Message replyMsg;
        ArrayList<Connection> sentLockRequests;

        if(connectionType.get(con) == ConnectionType.SERVER 
           && connectionStatus.get(con).equals("")
          )
        {      
            sentLockRequests = currentLockRequests.get(msg.getUsername());
            sentLockRequests.remove(con);

            if(sentLockRequests.isEmpty())
            {
                currentLockRequests.remove(msg.getUsername());
                if(currentRegisterRequests.containsKey(msg.getUsername()))
                {
                    replyMsg = new RegisterSuccessMessage(msg.getUsername());
                    currentRegisterRequests.get(msg.getUsername()).writeMsg(replyMsg.messageToString());
                    currentRegisterRequests.remove(msg.getUsername());
                }
                else
                {
                    replyMsg = new LockAllowedMessage(msg.getUsername(),
                                                      msg.getSecret(),
                                                      serverID
                                                     );
                    returnLockRequests.get(msg.getUsername()).writeMsg(replyMsg.messageToString());
                }
                returnLockRequests.remove(msg.getUsername());
            }
            
            currentLockRequests.put(msg.getUsername(), sentLockRequests);
            return false;
        }
        else
        {
            errorMsg = new InvalidMessage("server is not authenticated");
            con.writeMsg(errorMsg.messageToString());
            return true;
        }
    }
}
