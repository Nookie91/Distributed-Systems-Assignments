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
		con.writeMsg(message.toString());
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

		Message incomingMessage;
		Message error, reply, broadcast;
		
		ArrayList<Connection> sentLockRequests;
		
		
		Map<String,String> mapMsg = Message.stringToMap(msg);

		switch(Message.incomingMessageType(con,msg))
		{
			case "":
				error = new InvalidMessage("the received message contained a blank command");
                con.writeMsg(error.toString());
                return true;

            case "AUTHENTICATE":
            	incomingMessage = new AuthenticateMessage(mapMsg);
            	if(incomingMessage.checkFields(con))
            	{
            		return true;
            	}

            	if(((AuthenticateMessage) incomingMessage).doesSecretMatch(Settings.getSecret()))
            	{
            		
            		connectionType.put(con,ConnectionType.SERVER);
            		connectionStatus.put(con,"");
            		return false;
            	}
            	else
            	{
            		error = new AuthenticateFailMessage("the supplied secret is incorrect:" + ((AuthenticateMessage) incomingMessage).getSecret());
	                con.writeMsg(error.toString());
	                return true;
            	}

            case "AUTHENTICATION_FAIL":
            	return true;

            case "LOGIN":
            	incomingMessage = new LoginMessage(mapMsg);
            	if(incomingMessage.checkFields(con))
            	{
            		return true;
            	}

            	if(checkLogin(((LoginMessage) incomingMessage).getUsername(),((LoginMessage) incomingMessage).getSecret(),con))
            	{
            		connectionStatus.put(con,((LoginMessage) incomingMessage).getUsername());
            		connectionType.put(con, ConnectionType.CLIENT);
            		incrementUsers();
	            	reply = new LoginSuccessMessage(((LoginMessage) incomingMessage).getUsername());
					con.writeMsg(reply.toString());
					return false;
            	}
            	else
            	{
            		reply = new LoginFailedMessage();
					con.writeMsg(reply.toString());
					return true;
            	}
            	            	
            case "LOGOUT":
            	return true;

            case "ACTIVITY_MESSAGE":
            	if(connectionStatus.get(con).equals(null))
            	{
            		error = new AuthenticateFailMessage("currently no user logged in");
	                con.writeMsg(error.toString());
	                return true;
            	}

            	incomingMessage = new ActivityMessage(mapMsg);
            	if(incomingMessage.checkFields(con))
            	{
            		return true;
            	}
            	if(!((ActivityMessage) incomingMessage).getUsername().equals("anomynous"))
            	{
            		if(((ActivityMessage) incomingMessage).getUsername().equals(connectionStatus.get(con)) &&
            			checkLogin(((ActivityMessage) incomingMessage).getUsername(),((ActivityMessage) incomingMessage).getSecret(),con))
            		{
            			broadcast = new ActivityBroadcastMessage(((ActivityMessage) incomingMessage).getActivity(),
            													 ((ActivityMessage) incomingMessage).getUsername()
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
            			error = new AuthenticateFailMessage("username/secret do not match logged in user");
		                con.writeMsg(error.toString());
		                return true;	
            		}
            	}
            	else
            	{
            		broadcast = new ActivityBroadcastMessage(((ActivityMessage) incomingMessage).getActivity(),
            												 ((ActivityMessage) incomingMessage).getUsername()
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
            	if(connectionType.get(con) == ConnectionType.SERVER &&
            	   connectionStatus.get(con).equals(""))
            	{
            		boolean newServer = true;
            		incomingMessage = new ServerAnnounceMessage(mapMsg);
            		if(incomingMessage.checkFields(con))
	            	{
	            		return true;
	            	}
            		for(AuthorisedServer server:servers)
            		{
            			if(server.getID().equals(((ServerAnnounceMessage) incomingMessage).getID()))
            			{
            				server.updateLoad(((ServerAnnounceMessage) incomingMessage).getLoad());
            				newServer = false;
            				break;
            			}
            		}
            		if(newServer == true)
            		{
            			servers.add(new AuthorisedServer(((ServerAnnounceMessage) incomingMessage).getID(),
						            					 ((ServerAnnounceMessage) incomingMessage).getLoad(),
						            					 ((ServerAnnounceMessage) incomingMessage).getHostname(),
						            					 ((ServerAnnounceMessage) incomingMessage).getPort())
            											);
            		}
            		for(Connection connection: getConnections())
					{
						if(connectionType.get(connection) == ConnectionType.SERVER &&
							!connection.equals(con))
						{
							connection.writeMsg(incomingMessage.toString());
						}
					}
					return false;	
            	}
            	else
            	{
            		error = new InvalidMessage("server is not authenticated");
	                con.writeMsg(error.toString());
	                return true;
            	}
            case "ACTIVITY_BROADCAST":
            	if(connectionType.get(con) == ConnectionType.SERVER && 
            		connectionStatus.get(con).equals(""))
            	{
            		incomingMessage = new ActivityBroadcastMessage(mapMsg);
            		if(incomingMessage.checkFields(con))
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
            		error = new InvalidMessage("server is not authenticated");
	                con.writeMsg(error.toString());
	                return true;
            	}

            case "REGISTER":
            	incomingMessage = new RegisterMessage(mapMsg);
            	if(incomingMessage.checkFields(con))
            	{
            		return true;
            	}
            	if(connectionStatus.get(con).equals(null))
            	{
            		error = new InvalidMessage("Cannot Register: Already logged in.");
	                con.writeMsg(error.toString());
	                return true;
            	}
            	else if(userDatabase.containsKey(((RegisterMessage) incomingMessage).getUsername()))
            	{
            		reply = new RegisterFailedMessage(((RegisterMessage) incomingMessage).getUsername());
            		con.writeMsg(reply.toString());
            		return true;
            	}
            	else
            	{
            		userDatabase.put(((RegisterMessage) incomingMessage).getUsername(),((RegisterMessage) incomingMessage).getSecret());
            		LockRequestMessage lockRequest = new LockRequestMessage(((RegisterMessage) incomingMessage).getUsername(),
            																((RegisterMessage) incomingMessage).getSecret()
            																);
            		sentLockRequests = new ArrayList<Connection>();
            		currentRegisterRequests.put(((RegisterMessage) incomingMessage).getUsername(),con);
            		for(Connection connection: getConnections())
					{
						if(connectionType.get(connection) == ConnectionType.SERVER)
						{
							connection.writeMsg(lockRequest.toString());
							sentLockRequests.add(connection);
						}
					}
            		currentLockRequests.put(((RegisterMessage) incomingMessage).getUsername(),sentLockRequests);
            		return false;
            	}
            case "LOCK_REQUEST":
            	incomingMessage = new LockRequestMessage(mapMsg);
            	if(incomingMessage.checkFields(con))
            	{
            		return true;
            	}
            	if(userDatabase.containsKey(((LockRequestMessage) incomingMessage).getUsername()) &&
            		!userDatabase.get(((LockRequestMessage) incomingMessage).getUsername()).equals(((LockRequestMessage) incomingMessage).getSecret()))
            	{
            		reply = new LockDeniedMessage(((LockRequestMessage) incomingMessage).getUsername(),((LockRequestMessage) incomingMessage).getSecret());
            		con.writeMsg(reply.toString());
            		return false;
            	}
            	else
            	{
            		userDatabase.put(((LockRequestMessage) incomingMessage).getUsername(),((LockRequestMessage) incomingMessage).getSecret());
            		returnLockRequests.put(((LockRequestMessage) incomingMessage).getUsername(),con);
            		sentLockRequests = new ArrayList<Connection>();
            		for(Connection connection: getConnections())
					{
						if(connectionType.get(connection) == ConnectionType.SERVER &&
							!connection.equals(con))
						{
							connection.writeMsg(incomingMessage.toString());
							sentLockRequests.add(connection);
						}
					}
					if(sentLockRequests.isEmpty())
					{
						reply = new LockAllowedMessage(((LockRequestMessage) incomingMessage).getUsername(),
													 ((LockRequestMessage) incomingMessage).getSecret(),
            									     serverID
            										);
						con.writeMsg(reply.toString());
					}
					else
					{
						currentLockRequests.put(((LockRequestMessage) incomingMessage).getUsername(),sentLockRequests);
					}
					
					return false;
            	}
            case "LOCK_DENIED":
            	incomingMessage = new LockDeniedMessage(mapMsg);
            	sentLockRequests = new ArrayList<Connection>();
            	if(incomingMessage.checkFields(con))
            	{
            		return true;
            	}

            	if(userDatabase.containsKey(((LockDeniedMessage) incomingMessage).getUsername()) &&
            		userDatabase.get(((LockDeniedMessage) incomingMessage).getUsername()).equals(((LockDeniedMessage) incomingMessage).getSecret()))
            	{
            		userDatabase.remove(((LockDeniedMessage) incomingMessage).getUsername());
            	}
            	for(Connection connection: getConnections())
				{
					if(connectionType.get(connection) == ConnectionType.SERVER &&
						!connection.equals(con))
					{
						connection.writeMsg(incomingMessage.toString());
						sentLockRequests.add(connection);
					}
				}
				if(currentLockRequests.containsKey(((LockDeniedMessage) incomingMessage).getUsername()))
				{
					currentLockRequests.remove(((LockDeniedMessage) incomingMessage).getUsername());
				}
				if(returnLockRequests.containsKey(((LockDeniedMessage) incomingMessage).getUsername()))
				{				
					returnLockRequests.remove(((LockDeniedMessage) incomingMessage).getUsername());
				}
				if(currentRegisterRequests.containsKey(((LockDeniedMessage) incomingMessage).getUsername()))
				{
					reply = new RegisterFailedMessage(((LockDeniedMessage) incomingMessage).getUsername());
					currentRegisterRequests.get(((LockDeniedMessage) incomingMessage).getUsername()).writeMsg(reply.toString());
					currentRegisterRequests.get(((LockDeniedMessage) incomingMessage).getUsername()).closeCon();
					currentRegisterRequests.remove(((LockDeniedMessage) incomingMessage).getUsername());
				}
				return false;

			case "LOCK_ALLOWED":
				

		}
		return false;
	}

	private boolean checkLogin(String username, String secret,Connection con)
	{
		if(userDatabase.containsKey(username))
		{
			if(secret.equals(userDatabase.get(username)))
			{
				return true;
			}
			else
			{
				return false;
			}	
		}
		else if(username.equals("anomynous")) 
		{
			return true;
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
				con.writeMsg(message.toString());
			}
		}
	}
}
