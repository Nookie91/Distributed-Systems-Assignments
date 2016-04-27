package activitystreamer.client;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import activitystreamer.util.Settings;

public class ClientSolution extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSolution clientSolution;
	private TextFrame textFrame;
	private ClientConnection connection;
	
	/*
	 * additional variables
	 */
	
	// this is a singleton object
	public static ClientSolution getInstance()
	{
		if(clientSolution==null)
		{
			clientSolution = new ClientSolution();
		}
		return clientSolution;
	}
	
	public ClientSolution()
	{
		/*
		 * some additional initialization
		 */

		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		initiateConnection();
		// start the client's thread
		start();
	}
	
	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj)
	{
		
	}
	
	// called by the gui when the user clicks disconnect
	public void disconnect()
	{
		textFrame.setVisible(false);

		c.closecon();
		/*
		 * other things to do
		 */
	}
	
	private void initiateConnection()
	{
		// make a connection to a server
		try 
		{
			outgoingConnection(new Socket(Settings.getRemoteHostname(),Settings.getRemotePort()));
		} 
		catch (IOException e) 
		{
			log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
			System.exit(-1);
		}
		
	}
	
	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized void outgoingConnection(Socket s) throws IOException
	{
		log.debug("outgoing connection: "+Settings.socketAddress(s));
		connection = new ClientSolution(s);
	}
	

	// the client's run method, to receive messages
	@Override
	public void run()
	{
		
	}

	private Boolean login()
	{
		clientMessage.blankMessage();
		clientMessage.loginMessage(Settings.getUsername(),Settings.getSecret());
		connection.sendMsg(clientMessage.messageString());
	}

	/*
	 * additional methods
	 */
	
}
