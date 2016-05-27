package activitystreamer.client;

import java.io.BufferedReader;

import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.ssl.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import activitystreamer.message.*;
import activitystreamer.util.Settings;

public class ClientSolution extends Thread {

	// Logging initialistion
	private static final Logger log = LogManager.getLogger();
	// To store the singleton object.
	private static ClientSolution clientSolution;
	// The GUI interface
	private TextFrame textFrame;
	// Variables used in the socket and connection to server.
	private boolean term;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader inreader;
    private PrintWriter outwriter;
    private boolean open = false;
    private Socket socket;
    SSLSocketFactory factory;
    private SSLSocket sslSocket;


	// this is a singleton object
	public static ClientSolution getInstance()
	{
		if(clientSolution==null)
		{
			clientSolution = new ClientSolution();
		}
		return clientSolution;
	}

	// constructor for client.
	public ClientSolution()
	{
		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		term = false;

		// Open the connection handled by the Settings.
        initiateBaseConnection();

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

    public ClientSolution(Boolean ssl)
	{
		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		term = false;

		factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		// Open the connection handled by the Settings.
        initiateBaseConnection();

        // Login in if a secret was provided in arguments, otherwise
        // register a new user.
		login_register();

		// start the client's thread for reading incoming messages.
       start();
	}

    private void login_register()
    {
        if(Settings.getSecret() != null || Settings.getUsername().equals("anonymous"))
        {
            login();
        }
        else
        {
            Settings.setSecret(Settings.nextSecret());
            register();
        }
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
	}

	// make a connection to a server stored in the Settings.
	private void initiateBaseConnection()
	{
		try
		{
			socket = new Socket(Settings.getRemoteHostname(),Settings.getRemotePort());
			outgoingBaseConnection();
		}
		catch (IOException e)
		{
			log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
			System.exit(-1);
		}
	}

    private void initiateSSLConnection()
	{
		try
		{
            factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			sslSocket = (SSLSocket)factory.createSocket(Settings.getRemoteHostname(),Settings.getRemotePort());
			sslSocket.setEnabledCipherSuites(sslSocket.getEnabledCipherSuites());
			sslSocket.setUseClientMode(true);
			outgoingSSLConnection();
		}
		catch (IOException e)
		{
			log.error("failed to make secure connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
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

    // Process an incoming message from the server.
	public boolean process(String msg)
	{
		Gson gson = new Gson();
		Message incomingMessage;
		Message error;
		// get message object from JSONObject to determine command
		try
				{
					incomingMessage = gson.fromJson(msg,Message.class);
				}
				catch(Exception e)
				{
					log.error("Message recieved is not a Json Object");
					return true;
				}

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
	           	incomingMessage = gson.fromJson(msg,RedirectSecureMessage.class);
	           	if(((RedirectSecureMessage) incomingMessage).getHostname() == null)
		        {
		            error = new InvalidMessage("the received message did not contain a hostname");
		            writeMsg(error.messageToString());
		            return true;
		        }
		        if(((RedirectSecureMessage) incomingMessage).getPort() == null)
		        {
		            error = new InvalidMessage("the received message did not contain a port");
		            writeMsg(error.messageToString());
		            return true;
		        }
	           	log.debug("redirecting to " + ((RedirectSecureMessage) incomingMessage).getHostname() +":"+ ((RedirectSecureMessage) incomingMessage).getPort());
	           	Settings.setRemoteHostname(((RedirectSecureMessage) incomingMessage).getHostname());
	           	Settings.setRemotePort(((RedirectSecureMessage) incomingMessage).getPort());
	           	closeCon();
                if (((RedirectSecureMessage) incomingMessage).getSecure() == true)
                {
                    initiateSSLConnection();
                }
                else
                {
                    initiateBaseConnection();
                }
                login_register();
	           	return false;

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
	public synchronized void outgoingBaseConnection() throws IOException
	{
		log.debug("outgoing connection: "+Settings.socketAddress(socket));
		in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        inreader = new BufferedReader( new InputStreamReader(in));
        outwriter = new PrintWriter(out, true);
        open = true;
	}

    public synchronized void outgoingSSLConnection() throws IOException
	{
		log.debug("outgoing secure connection: "+Settings.socketAddress(sslSocket));
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

	// log in to server with Settings
	private void login()
	{
		LoginSecureMessage message = new LoginSecureMessage(Settings.getUsername(),Settings.getSecret(),true);
		writeMsg(message.messageToString());
	}

	// register with server using Settings
	private void register()
	{
		RegisterSecureMessage message = new RegisterSecureMessage(Settings.getUsername(),Settings.getSecret(),true);
		writeMsg(message.messageToString());
	}
}
