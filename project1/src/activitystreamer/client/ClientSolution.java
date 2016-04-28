package activitystreamer.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import activitystreamer.message.*;
import activitystreamer.util.Settings;

public class ClientSolution extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSolution clientSolution;
	private TextFrame textFrame;
	private boolean term;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader inreader;
    private PrintWriter outwriter;
    private boolean open = false;
    private Socket socket;
	
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
		term = false;
		
        initiateConnection();

		if(Settings.getSecret() != null)
        {
        	login();
        }
        else
        {
        	Settings.setSecret(Settings.nextSecret());
        	register();
        }
		
		// start the client's thread
        start();
	}
	
	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj)
	{
		ActivityMessage message = new ActivityMessage(Settings.getUsername(),
													  Settings.getSecret(),
													  activityObj.toJSONString()
													  );
		log.info(message.messageToString());
		writeMsg(message.messageToString());
	}
	
	// called by the gui when the user clicks disconnect
	public void disconnect()
	{
		LogoutMessage message = new LogoutMessage();
		writeMsg(message.messageToString());
		closeCon();
		textFrame.setVisible(false);
		textFrame.dispose();

		
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
	
	public boolean writeMsg(String msg) 
    {
        if(open){
            outwriter.println(msg);
            outwriter.flush();
            return true;    
        }
        return false;
    }
    
    public void closeCon()
    {
        if(open)
        {
            log.info("closing connection "+Settings.socketAddress(socket));
            try 
            {
                inreader.close();
                out.close();
                in.close();
                outwriter.close();
            } 
            catch (IOException e) 
            {
                // already closed?
                log.error("received exception closing the connection "+Settings.socketAddress(socket)+": "+e);
            }
        }
    }

	public boolean process(String msg)
	{
		Message incomingMessage;
		Message error;
		
		
		log.info(msg);
		Map<String,String> mapMsg = Message.stringToMap(msg);
		switch(Message.incomingMessageType(mapMsg))
		{
			case "":
				error = new InvalidMessage("the received message contained a blank command");
                writeMsg(error.messageToString());
                return true;
            case "LOGIN_FAILED":
            	incomingMessage = new LoginFailedMessage(mapMsg);
            	log.error(((LoginFailedMessage) incomingMessage).getInfo());
            	return true;

            case "LOGIN_SUCCEED":
            	log.debug("login successful");
            	return false;
            	
            case "REGISTER_FAILED":
            	incomingMessage = new RegisterFailedMessage(mapMsg);
            	log.error(((RegisterFailedMessage) incomingMessage).getInfo());
            	return true;
            	
            case "REGISTER_SUCCESS":
            	incomingMessage = new RegisterSuccessMessage(mapMsg);
            	log.debug(((RegisterSuccessMessage) incomingMessage).getInfo());
            	login();
            	return false;
            	
            case "ACTIVITY_BROADCAST":
            	incomingMessage = new ActivityBroadcastMessage(mapMsg);
            	textFrame.setOutputText(((ActivityBroadcastMessage) incomingMessage).getActivityObject());
            	return false;
            	
            case "INVALID_MESSAGE":
            	incomingMessage = new InvalidMessage(mapMsg);
            	log.error(((InvalidMessage) incomingMessage).getInfo());
            	return true;
            	
            case "AUTHENTICATE_FAIL":
            	incomingMessage = new AuthenticateFailMessage(mapMsg);
            	log.error(((AuthenticateFailMessage) incomingMessage).getInfo());
            	return true;

            case "REDIRECT":
            	incomingMessage = new RedirectMessage(mapMsg);
            	log.debug("redirecting to " + ((RedirectMessage) incomingMessage).getHostname() +":"+ ((RedirectMessage) incomingMessage).getPort());
            	Settings.setRemoteHostname(((RedirectMessage) incomingMessage).getHostname());
            	Settings.setRemotePort(((RedirectMessage) incomingMessage).getPort());
            	clientSolution = new ClientSolution();
            	return true;

        }
		return false;
	}
	
	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized void outgoingConnection(Socket s) throws IOException
	{
		log.debug("outgoing connection: "+Settings.socketAddress(s));
		socket = s;
		in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        inreader = new BufferedReader( new InputStreamReader(in));
        outwriter = new PrintWriter(out, true);
        open = true;
        
	}
	

	// the client's run method, to receive messages
	@Override
	public void run()
	{
		try 
        {
            String data;
            while(!term && (data = inreader.readLine())!=null)
            {
                term=process(data);
            }
            log.debug("connection closed to "+Settings.socketAddress(socket));
            disconnect();
      
        } 
        catch (IOException e) 
        {
            log.error("connection " + Settings.socketAddress(socket)
                      + " closed with exception: " + e
                     );
            disconnect();
        }
		open = false;
		textFrame.dispose();
	}

	private void login()
	{
		LoginMessage message = new LoginMessage(Settings.getUsername(),Settings.getSecret());
		writeMsg(message.messageToString());
	}
	
	private void register()
	{
		RegisterMessage message = new RegisterMessage(Settings.getUsername(),Settings.getSecret());
		writeMsg(message.messageToString());
	}

	/*
	 * additional methods
	 */
	
}
