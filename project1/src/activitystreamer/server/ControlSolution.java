package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ControlSolution extends Control 
{
	private static final Logger log = LogManager.getLogger();
	
	/*
	 * additional variables as needed
	 */
	

	private static String serverID;	

	private static int numberOfLoggedInUsers;
	private static Map<String, String> userDatabase;
	private static ArrayList<AuthorisedServer> servers;

	private static Map<String,Connection> currentRegisterRequests;
	private static Map<String,Connection> returnLockRequests;
	private static Map<String,ArrayList<Connection>> currentLockRequests;

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
		isConnectionAuthenticated = false;
		loggedInUser = null;
		numberOfLoggedInUsers = 0;
		userDatabase = new HashMap<String, String>();
		servers = new ArrayList<AuthorisedServer>();

		currentRegisterRequests = new HashMap<String,Connection>();
		returnLockRequests = new HashMap<String,Connection>();
		currentLockRequests = new HashMap<String,ArrayList<Connection>>();

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
		
		return con;
	}
	
	/*
	 * a new outgoing connection
	 */
	@Override
	public Connection outgoingConnection(Socket s) throws IOException
	{
		Connection con = super.outgoingConnection(s);
		/*
		 * do additional things here
		 */
		
		AuthenticateMessage message = new AuthenticateMessage(Settings.getSecret());
		con.writeMsg(message.toString());
		
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

		if(loggedInUser != null)
		{
			loggedInUser = null;
            decrementUsers();
		}
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

		Message incomingMessage();

		switch(Message.incomingMessageType(msg))
		{
			case "":
				InvalidMessage error = new InvalidMessage("the received message contained a blank command");
                con.writeMsg(error.toString());
                return true;

            case "AUTHENTICATE":
            	incomingMessage = new AuthenticateMessage(msg);
            	if(incomingMessage.checkFields())
            	{
            		return true;
            	}

            	if(incomingMessage.doesSecretMatch(Settings.getSecret()))
            	{
            		con.setIsConnectionAuthenticated(true);
            		con.setConnectionType(Connection.ConnectionType.SERVER);
            		return false;
            	}
            	else
            	{
            		AuthenticationFail error = new AuthenticationFail("the supplied secret is incorrect:" + incomingMessage.getSecret());
	                con.writeMsg(error.toString());
	                return true;
            	}
            	return false;

            case "AUTHENTICATION_FAIL":
            	return true;

            case "LOGIN":
            	incomingMessage = new LoginMessage(msg);
            	if(incomingMessage.checkFields())
            	{
            		return true;
            	}

            	if(checkLogin(message.getUsername(),message.getSecret()))
            	{
            		con.setLoggedInUser(message.getUsername());
            		con.setConnectionType(Connection.ConnectionType.CLIENT);
            		incrementUsers();
	            	LoginSuccessMessage reply = new LoginSuccessMessage(incomingMessage.getUsername());
					con.writeMsg(reply.toString());
					return false;
            	}
            	else
            	{
            		LoginFailedMessage reply = new LoginFailedMessage();
					con.writeMsg(reply.toString());
					return true;
            	}
            	            	
            case "LOGOUT":
            	return true;

            case "ACTIVITY_MESSAGE":
            	if(con.getLoggedInUser() == null)
            	{
            		AuthenticationFail error = new AuthenticationFail("currently no user logged in");
	                con.writeMsg(error.toString());
	                return true;
            	}

            	incomingMessage = new ActivityMessage(msg);
            	if(incomingMessage.checkFields())
            	{
            		return true;
            	}
            	if(!incomingMessage.getUsername().equalsTo("anomynous"))
            	{
            		if(incomingConnection.getUsername.equalsTo(con.getLoggedInUser()) &&
            			checkLogin(incomingMessage.getUsername(),incomingMessage.getSecret()))
            		{
            			ActivityBroadCastMessage broadcast = new ActivityBroadCastMessage(incomingMessage.getActivity()
            																			  incomingMessage.getUsername()
            																			 );
            			for(Connection connection:getConnections())
            			{
            				if(!connection.equals(con))
            				{
            					connection.writeMsg(broadcast.toString());
            				}
            			}
            			return false;
            		}
            		else
            		{
            			AuthenticationFail error = new AuthenticationFail("username/secret do not match logged in user");
		                con.writeMsg(error.toString());
		                return true;	
            		}
            	}
            	else
            	{
            		ActivityBroadcastMessage broadcast = new ActivityBroadcastMessage(incomingMessage.getActivity()
            																			  incomingMessage.getUsername()
            																			 );
        			for(Connection connection:getConnections())
        			{
        				if(!connection.equals(con))
        				{
        					connection.writeMsg(broadcast.toString());
        				}
        			}
        			return false;
            	}

            case "SERVER_ANNOUNCE":
            	if(con.isConnectionAuthenticated())
            	{
            		boolean newServer = true;
            		incomingMessage = new ServerAnnouncetMessage(msg);
            		if(incomingMessage.checkFields())
	            	{
	            		return true;
	            	}
            		for(AuthorisedServer server:servers)
            		{
            			if(server.getID().equalsTo(incomingMessage.getID()))
            			{
            				server.updateLoad(incomingMessage.getLoad);
            				newServer = false;
            				break;
            			}
            		}
            		if(newServer == true)
            		{
            			servers.add(new AuthorisedServer(incomingMessage.getID(),
            											 incomingMessage.getLoad(),
            											 incomingMessage.getHostname(),
            											 incomingMessage.getPort()));
            		}
            		for(Connection connection: getConnections())
					{
						if(connection.isConnectionServer() &&
							!connection.equals(con))
						{
							connection.writeMsg(incomingMessage.toString());
						}
					}
					return false;	
            	}
            	else
            	{
            		InvalidMessage error = new InvalidMessage("server is not authenticated");
	                con.writeMsg(error.toString());
	                return true;
            	}
            case "ACTIVITY_BROADCAST":
            	if(con.isConnectionAuthenticated())
            	{
            		incomingMessage = new ActivityBroadcastMessage(msg);
            		if(incomingMessage.checkFields())
	            	{
	            		return true;
	            	}

            		for(Connection connection: getConnections())
					{
						if(!connection.equals(con))
						{
							connection.writeMsg(incomingMessage.toString());
						}
					}
					return false;	
            	}
            	else
            	{
            		InvalidMessage error = new InvalidMessage("server is not authenticated");
	                con.writeMsg(error.toString());
	                return true;
            	}

            case "REGISTER":
            	incomingMessage = new RegisterMessage(msg);
            	if(incomingMessage.checkFields())
            	{
            		return true;
            	}
            	if(con.getLoggedInUser != null)
            	{
            		InvalidMessage error = new InvalidMessage("Cannot Register: Already logged in.");
	                con.writeMsg(error.toString());
	                return true;
            	}
            	else if(userDatabase.containsKey(incomingMessage.getUsername()))
            	{
            		RegisterFailedMessage reply = new RegisterFailedMessage(incomingMessage.getUsername());
            		con.writeMsg(reply.toString());
            		return true;
            	}
            	else
            	{
            		userDatabase.put(incomingMessage.getUsername(),incomingMessage.getSecret());
            		LockRequestMessage lockRequest = new LockRequestMessage(incomingMessage.getUsername(),
            																incomingMessage.getSecret()
            																);
            		ArrayList<Connection> sentLockRequests = new ArrayList<connection>();
            		currentRegisterRequests.put(incomingMessage.getUsername(),con);
            		for(Connection connection: getConnections())
					{
						if(connection.isConnectionServer())
						{
							connection.writeMsg(lockRequest.toString());
							sentLockRequests.add(connection);
						}
					}
            		currentLockRequests.put(incomingMessage.getUsername(),sentLockRequests);
            		return false;
            	}
            case "LOCK_REQUEST":
            	incomingMessage = new LockRequestMessage(msg);
            	if(incomingMessage.checkFields())
            	{
            		return true;
            	}
            	if(userDatabase.containsKey(incomingMessage.getUsername()) &&
            		!userDatabase.get(incomingMessage.getUsername()).equalsTo(incomingMessage.getSecret()))
            	{
            		LockDeniedMessage reply = new LockDeniedMessage(incomingMessage.getUsername(),incomingMessage.getSecret());
            		con.writeMsg(reply.toString());
            		return false;
            	}
            	else
            	{
            		userDatabase.put(incomingMessage.getUsername(),incomingMessage.getSecret());
            		returnLockRequests.add(incomingMessage.getUsername(),con);
            		ArrayList<Connection> sentLockRequests = new ArrayList<connection>();
            		for(Connection connection: getConnections())
					{
						if(connection.isConnectionServer() &&
							!connection.equals(con))
						{
							connection.writeMsg(incomingMessage.toString());
							sentLockRequests.add(connection);
						}
					}
					if(sentLockRequests.isEmpty())
					{
						LockAllowMessage reply = new LockAllowMessage(incomingMessage.getUsername(),
            														  incomingMessage.getSecret(),
            														  serverID
            														  );
						con.writeMsg(reply.toString());
					}
					else
					{
						currentLockRequests.put(incomingMessage.getUsername(),sentLockRequests);
					}
					
					return false;
            	}
            case "LOCK_DENIED":
            	incomingMessage = LockDeniedMessage(msg);
            	if(incomingMessage.checkFields())
            	{
            		return true;
            	}

            	if(userDatabase.containsKey(incomingMessage.getUsername()) &&
            		userDatabase.get(incomingMessage.getUsername()).equalsTo(incomingMessage.getSecret()))
            	{
            		userDatabase.remove(incomingMessage.getUsername());
            	}
            	for(Connection connection: getConnections())
				{
					if(connection.isConnectionServer() &&
						!connection.equals(con))
					{
						connection.writeMsg(incomingMessage.toString());
						sentLockRequests.add(connection);
					}
				}
				if(currentLockRequests.containsKey(incomingMessage.getUsername()))
				{
					currentLockRequests.remove(incomingMessage.getUsername());
				}
				if(returnLockRequests.containsKey(incomingMessage.getUsername()))
				{				
					returnLockRequests.remove(incomingMessage.getUsername());
				}
				if(currentRegisterRequests.containsKey(incomingMessage.getUsername()))
				{
					RegisterFailedMessage reply = new RegisterFailedMessage(incomingMessage.getUsername());
					currentRegisterRequests.get(incomingMessage.getUsername()).writeMsg(reply);
					currentRegisterRequests.get(incomingMessage.getUsername()).closeCon();
					currentRegisterRequests.remove(incomingMessage.getUsername())
				}
				return false;

			case "LOCK_ALLOWED":
				




            	



		}
		return false;
	}

	private boolean checkLogin(String username, String secret)
	{
		if(userDatabase.containsKey(username))
		{
			if(secret.equalsTo(userDatabase.get(username)) == 0)
			{
				return true;
			}
			else
			{
				return false;
			}	
		}
		else if(username.equalsTo("anomynous")) 
		{
			return true;
		}
		else if(isUserLoggedIn == true)
		{
			return true;
		}
		else
		{
			return false;	
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
			if(con.isConnectionServer())
			{
				con.writeMsg(message.toString());
			}
		}
	}
}
