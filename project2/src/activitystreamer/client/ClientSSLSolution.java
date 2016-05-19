package activitystreamer.client;

import java.io.BufferedReader;

import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.*;

import javax.net.ssl.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import activitystreamer.message.*;
import activitystreamer.util.Settings;

public class ClientSSLSolution extends Thread {

	// Logging initialistion
	private static final Logger log = LogManager.getLogger();
	// To store the singleton object.
	private static ClientSSLSolution clientSolution;
	// The GUI interface
	private TextFrame textFrame;
	// Variables used in the socket and connection to server.
	private boolean term;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader inreader;
    private PrintWriter outwriter;
    private boolean open = false;
    SSLSocketFactory factory;
    private SSLSocket sslSocket;


	// this is a singleton object
	public static ClientSSLSolution getInstance()
	{
	if(clientSolution==null)
		{
			clientSolution = new ClientSSLSolution();
		}
		return clientSolution;
	}

	// constructor for client.
	public ClientSSLSolution()
	{
		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		term = false;
		
		// Message msg;
		factory = (SSLSocketFactory) SSLSocketFactory.getDefault();


		
		
		// Open the connection handled by the Settings.
        initiateConnection();

        // Login in if a secret was provided in arguments, otherwise
        // register a new user.
		if(Settings.getSecret() != null || Settings.getUsername().equals("anonymous"))
		{
			login();
		}
		else
		{
			Settings.setSecret(Settings.nextSecret());
			register();
		}

		// start the client's thread for reading incoming messages.
       start();
	}

	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj)
	{
		ActivityMessage message = new ActivityMessage(Settings.getUsername(),
													  Settings.getSecret(),
													  activityObj
													  );
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

	// make a connection to a server stored in the Settings.
	private void initiateConnection()
	{
		try
		{
			System.out.println("made it here");
			sslSocket = (SSLSocket)factory.createSocket(Settings.getRemoteHostname(),Settings.getRemotePort());
			String[] cipher = {"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA"};
			sslSocket.setEnabledCipherSuites(cipher);//sslSocket.getEnabledCipherSuites());
			
			sslSocket.setUseClientMode(true);
			
			outgoingConnection();
		}
		catch (IOException e)
		{
			log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
			System.exit(-1);
		}
		
	}

	// write a message to the server
	public boolean writeMsg(String msg)
    {
        if(open){
            outwriter.println(msg);
            outwriter.flush();
            return true;
        }
        return false;
    }

    // Close the connection to the server.
    public void closeCon()
    {
        if(open)
        {
            log.info("closing connection "+Settings.socketAddress(sslSocket));
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
                log.error("received exception closing the connection "+Settings.socketAddress(sslSocket)+": "+e);
            }
        }
    }

    // Process an incoming message from the server.
	public boolean process(String msg)
	{
		Gson gson = new Gson();
		Message incomingMessage;
		Message error;
		// get message object from JSONObject to determine command
		incomingMessage = gson.fromJson(msg,Message.class);



		// get command type and act accordingly.
		switch(incomingMessage.getCommand())
		{
			case "":
				error = new InvalidMessage("the received message contained a blank command");
	            writeMsg(error.messageToString());
	            return true;
           case "LOGIN_FAILED":
	           	incomingMessage = gson.fromJson(msg,LoginFailedMessage.class);
	           	log.error(((LoginFailedMessage) incomingMessage).getInfo());
	           	return true;

           case "LOGIN_SUCCESS":
	           	log.debug("login successful");
	           	return false;

           case "REGISTER_FAILED":
	           	incomingMessage = gson.fromJson(msg,RegisterFailedMessage.class);
	           	log.error(((RegisterFailedMessage) incomingMessage).getInfo());
	           	return true;

           case "REGISTER_SUCCESS":
	           	incomingMessage = gson.fromJson(msg,RegisterSuccessMessage.class);
	           	log.debug(((RegisterSuccessMessage) incomingMessage).getInfo());
	           	login();
	           	return false;

           case "ACTIVITY_BROADCAST":
	           	incomingMessage = gson.fromJson(msg,ActivityBroadcastMessage.class);
	           	if(((ActivityBroadcastMessage) incomingMessage).getActivity() == null)
		        {
		            error = new InvalidMessage("the received message did not contain a JSON activity");
		            writeMsg(error.messageToString());
		            return true;
		        }
	           	textFrame.setOutputText(((ActivityBroadcastMessage) incomingMessage).getActivity());
	           	return false;

           case "INVALID_MESSAGE":
	           	incomingMessage = gson.fromJson(msg,InvalidMessage.class);
	           	log.error(((InvalidMessage) incomingMessage).getInfo());
	           	return true;

           case "AUTHENTICATE_FAIL":
	           	incomingMessage = gson.fromJson(msg,AuthenticateFailMessage.class);
	           	log.error(((AuthenticateFailMessage) incomingMessage).getInfo());
	           	return true;

           case "REDIRECT":
	           	incomingMessage = gson.fromJson(msg,RedirectMessage.class);
	           	if(((RedirectMessage) incomingMessage).getHostname() == null)
		        {
		            error = new InvalidMessage("the received message did not contain a hostname");
		            writeMsg(error.messageToString());
		            return true;
		        }
		        if(((RedirectMessage) incomingMessage).getPort() == null)
		        {
		            error = new InvalidMessage("the received message did not contain a port");
		            writeMsg(error.messageToString());
		            return true;
		        }
	           	log.debug("redirecting to " + ((RedirectMessage) incomingMessage).getHostname() +":"+ ((RedirectMessage) incomingMessage).getPort());
	           	Settings.setRemoteHostname(((RedirectMessage) incomingMessage).getHostname());
	           	Settings.setRemotePort(((RedirectMessage) incomingMessage).getPort());
	           	clientSolution = new ClientSSLSolution();
	           	return true;
	        default:
	        	log.error("unrecognised command");
	        	error = new InvalidMessage("unrecognised command");
	            writeMsg(error.messageToString());
	        	return true;

        }
	}



	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized void outgoingConnection() throws IOException
	{
		log.debug("outgoing connection: "+Settings.socketAddress(sslSocket));
		in = new DataInputStream(sslSocket.getInputStream());
        out = new DataOutputStream(sslSocket.getOutputStream());
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
            log.debug("connection closed to "+Settings.socketAddress(sslSocket));
            disconnect();

        }
        catch (IOException e)
        {
            log.error("connection " + Settings.socketAddress(sslSocket)
                      + " closed with exception: " + e
                     );
            disconnect();
        }
		open = false;
		textFrame.dispose();
	}

	// log in to server with Settings
	private void login()
	{
		LoginMessage message = new LoginMessage(Settings.getUsername(),Settings.getSecret());
		writeMsg(message.messageToString());
	}

	// register with server using Settings
	private void register()
	{
		RegisterMessage message = new RegisterMessage(Settings.getUsername(),Settings.getSecret());
		writeMsg(message.messageToString());
	}
}
