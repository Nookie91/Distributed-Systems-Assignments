package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.*;

import activitystreamer.message.*;
import activitystreamer.util.Settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;



public class ControlSolution extends Control
{
	private static final Logger log = LogManager.getLogger();

	public static enum ConnectionType {CLIENT,SERVER,UNSECURED,SECURED};
	private static String serverID;

	private static int numberOfLoggedInUsers;
	private static Map<String, String> userDatabase;
	private static ArrayList<AuthorisedServer> servers;

	private static Map<String,Connection> currentRegisterRequests;
	private static Map<String,Connection> returnLockRequests;
	private static Map<String,ArrayList<Connection>> currentLockRequests;
	private static Map<Connection,ConnectionType> connectionType;
	private static Map<Connection, String> connectionStatus;
	Gson gson;



    private static SSLListener sslListener;

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

        try
		{
			sslListener = new SSLListener();
		}
		catch (IOException e1)
		{
			log.fatal("failed to startup a ssl listening thread: "+e1);
			System.exit(-1);
		}

		numberOfLoggedInUsers = 0;
		userDatabase = new HashMap<String, String>();
		servers = new ArrayList<AuthorisedServer>();

		currentRegisterRequests = new HashMap<String,Connection>();
		returnLockRequests = new HashMap<String,Connection>();
		currentLockRequests = new HashMap<String,ArrayList<Connection>>();
		connectionType = new HashMap<Connection,ConnectionType>();
		connectionStatus = new HashMap<Connection,String>();
		gson = new Gson();
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
		System.out.println();
		System.out.println(s);
		System.out.println();
		connectionType.put(con, ConnectionType.UNSECURED);
		connectionStatus.put(con,null);
		return con;
	}

	@Override
	public Connection incomingConnection(SSLSocket s) throws IOException
	{
		Connection con = super.incomingConnection(s);
		/*
		 * do additional things here
		 */
		System.out.println();
		System.out.println(s);
		System.out.println();
		connectionType.put(con, ConnectionType.SECURED);
		connectionStatus.put(con,null);
		return con;
	}

	public void initiateSSLConnection()
	{
		// make a connection to another server if remote hostname is supplied
		if(Settings.getRemoteHostname()!=null)
		{
			try
			{
				SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				System.out.println("made it here");
				SSLSocket sslSocket = (SSLSocket)factory.createSocket(Settings.getRemoteHostname(),Settings.getRemotePort());
				String[] cipher = {"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA"};
				sslSocket.setEnabledCipherSuites(cipher);//sslSocket.getEnabledCipherSuites());

				sslSocket.setUseClientMode(true);

				outgoingConnection(sslSocket);
			}
			catch (IOException e)
			{
				log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
				System.exit(-1);
			}
		}
	}

	/*
	 * a new outgoing connection. connects to server passed in cmdline
	 */
	@Override
	public Connection outgoingConnection(Socket s) throws IOException
	{
		Message message;
		Connection con = super.outgoingConnection(s);

		message = new AuthenticateSecureMessage(Settings.getSecret(),true);
		con.writeMsg(message.messageToString());
		connectionType.put(con, ConnectionType.SERVER);
		connectionStatus.put(con,"");

		return con;
	}

	@Override
	public Connection outgoingConnection(SSLSocket s) throws IOException
	{
		Message message;
		Connection con = super.outgoingConnection(s);

		message = new AuthenticateSecureMessage(Settings.getSecret(),true);
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
		InvalidMessage error;

		Message message;
		message = gson.fromJson(msg,Message.class);

		switch(message.getCommand())
		{
			case "":
				error = new InvalidMessage("the received message contained a blank command");
                con.writeMsg(error.messageToString());
                log.error("the received message contained a blank command");
                return true;

            case "AUTHENTICATE":
            	return authenticate(con,msg);

            case "AUTHENTICATION_FAIL":
            	log.debug("Incorrect server secret provided.");
            	return true;

            case "LOGIN":
            	return login(con,msg);

            case "LOGOUT":
            	return true;

            case "INVALID_MESSAGE":
            	return true;

            case "ACTIVITY_MESSAGE":
            	return activityMessage(con,msg);

            case "SERVER_ANNOUNCE":
            	return serverAnnounce(con,msg);

            case "ACTIVITY_BROADCAST":
            	return activityBroadcast(con,msg);

            case "REGISTER":
            	return register(con,msg);

            case "LOCK_REQUEST":
            	return lockRequest(con,msg);

            case "LOCK_DENIED":
            	return lockDenied(con,msg);

			case "LOCK_ALLOWED":
				return lockAllowed(con,msg);

			case "REDIRECT":
				return redirect(con,msg);
			default:
                return true;
		}
	}

    // check credentials of client.
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
		else if(connectionStatus.get(con) == null)
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
		serverBroadcast();

		return false;
	}

	private void incrementUsers()
	{
		numberOfLoggedInUsers ++;
	}

	private void decrementUsers()
	{
		numberOfLoggedInUsers --;
	}


	private boolean redirect(Connection con,String mapMsg)
	{
		RedirectSecureMessage message = gson.fromJson(mapMsg, RedirectSecureMessage.class);
		Settings.setRemotePort(message.getPort());
		initiateSSLConnection();
		return true;
	}
	
    // broadcast server to other servers.
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

    // if server is authenticated, add/update its stored information and then
    // boradcast to all other servers along the chain.
    private boolean serverAnnounce(Connection con, String mapMsg)
    {
        ServerAnnounceMessage msg = gson.fromJson(mapMsg,ServerAnnounceMessage.class);
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

    // if server is authenticated, broadcast activity to all other connections
    // but the one who sent it.
    private boolean activityBroadcast(Connection con, String mapMsg)
    {
        ActivityBroadcastMessage msg = gson.fromJson(mapMsg,ActivityBroadcastMessage.class);
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

    // if server passes the correct secret, add it to the list of authenticated
    // servers.
    private boolean authenticate(Connection con, String mapMsg)
    {
        AuthenticateSecureMessage msg = gson.fromJson(mapMsg,AuthenticateSecureMessage.class);
        AuthenticateFailMessage errorMsg;

        if(msg.checkFields(con))
        {
            return true;
        }
        
        if (msg.getSecure() == true && connectionType.get(con) == ConnectionType.UNSECURED)
        {
            Message replyMsg = new RedirectSecureMessage(Settings.getLocalHostname(),
            							   Settings.getLocalSecurePort(),
                                           true
                                          );
            con.writeMsg(replyMsg.messageToString());
            log.info("Redirecting to Hostname: " + Settings.getLocalHostname()
                                     + " Port: " + Settings.getLocalSecurePort());
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

    // check the load on other servers, redirect the client if the laod
    // difference is greater than 1. otherwise, check client credentials
    // adding them to the list of logged in users if they are correct.
    private boolean login(Connection con, String mapMsg)
    {
        LoginSecureMessage msg = gson.fromJson(mapMsg,LoginSecureMessage.class);
        Message replyMsg;
        System.out.println(mapMsg);

        for(AuthorisedServer server: servers)
        {
            if(numberOfLoggedInUsers >= server.getLoad() + 2)
            {
                replyMsg = new RedirectSecureMessage(server.getHostname(),
                                               server.getPort(),
                                               false
                                              );
                con.writeMsg(replyMsg.messageToString());
                log.info("Redirecting to Hostname: " + server.getHostname()
                                         + " Port: " + server.getPort());
                return true;
            }
        }
        log.debug(msg.getSecure());
        log.debug(connectionType.get(con));
        if (msg.getSecure() == true && connectionType.get(con) == ConnectionType.UNSECURED)
        {
            replyMsg = new RedirectSecureMessage(Settings.getLocalHostname(),
            							   Settings.getLocalSecurePort(),
                                           true
                                          );
            con.writeMsg(replyMsg.messageToString());
            log.info("Redirecting to Hostname: " + Settings.getLocalHostname()
                                     + " Port: " + Settings.getLocalSecurePort());
            return true;
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

    // if the user is logged in, add the required field onto the JSONObject,
    // then broadcast to all connections.
    private boolean activityMessage(Connection con, String mapMsg)
    {
        ActivityMessage msg = gson.fromJson(mapMsg,ActivityMessage.class);
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

    // check if client is already logged in or registered with server.
    // otherwise send out a lock request to all other servers.
    // if no other servers are connected, send a succeed.
    private boolean register(Connection con, String mapMsg)
    {
        RegisterSecureMessage msg = gson.fromJson(mapMsg,RegisterSecureMessage.class);
        LockRequestMessage lockRequest;
        InvalidMessage errorMsg;
        Message replyMsg;
        ArrayList<Connection> sentLockRequests;

        if(msg.checkFields(con))
        {
            return true;
        }

        if (msg.getSecure() == true && connectionType.get(con) == ConnectionType.UNSECURED)
        {
            replyMsg = new RedirectSecureMessage(Settings.getLocalHostname(),
            									 Settings.getLocalSecurePort(),
	                                             true
	                                            );
            con.writeMsg(replyMsg.messageToString());
            log.info("Redirecting to Hostname: " + Settings.getLocalHostname()
                                     + " Port: " + Settings.getLocalSecurePort());
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

    // check if lock should be denied by this server, otherwise send lock
    // requests to other servers. If no other servers send a lock accept.
    private boolean lockRequest(Connection con, String mapMsg)
    {
        LockRequestMessage msg = gson.fromJson(mapMsg,LockRequestMessage.class);
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

    // remove username/secret pair if in database, send denied to all other
    // connections. if its the server that sent the original lock request, send
    // register denied to client.
    private boolean lockDenied(Connection con, String mapMsg)
    {
        LockDeniedMessage msg = gson.fromJson(mapMsg,LockDeniedMessage.class);
        InvalidMessage errorMsg;
        Message replyMsg;
        log.debug("got lock denied from: " + con);
        if(connectionType.get(con) == ConnectionType.SERVER
           && connectionStatus.get(con).equals("")
          )

        {
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

    // send lock request up the line, if its the server that sent original
    // request, send register succeed.
    private boolean lockAllowed(Connection con, String mapMsg)
    {
        LockAllowedMessage msg = gson.fromJson(mapMsg,LockAllowedMessage.class);
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
