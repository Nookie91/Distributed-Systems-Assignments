package activitystreamer.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.util.Settings;

public class SSLListener extends Thread
{
	private static final Logger log = LogManager.getLogger();
	private SSLServerSocket serverSocket=null;
	private boolean term = false;
	private int portnum;
    SSLServerSocketFactory factory;
    private static final String ALGORITHM = "SSL";

	public SSLListener() throws IOException
	{
		try 
		{
			SSLContext context = SSLContext.getInstance(ALGORITHM);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			// Oracle's default kind of key store
			KeyStore ks = KeyStore.getInstance("JKS");

			char[] password = "admin123".toCharArray();
			ks.load(new FileInputStream("/home/nookie/Documents/COMP90015/project2/bin/activitystreamer/server.jks"), password);
			kmf.init(ks, password);
			context.init(kmf.getKeyManagers(), null, null);
			Arrays.fill(password, '0');
			portnum = Settings.getLocalSecurePort(); // keep our own copy in case it changes later
	        factory = context.getServerSocketFactory();
			serverSocket = (SSLServerSocket) factory.createServerSocket(portnum);

			String[] cipher = {"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA"};

			serverSocket.setEnabledCipherSuites(cipher);
			serverSocket.setNeedClientAuth(true);
			start();
		} 
		catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) 
		{
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void run()
	{
		log.info("listening for new connections on "+portnum);
		while(!term)
		{
			SSLSocket clientSocket;
			try
			{
				clientSocket = (SSLSocket) serverSocket.accept();
				ControlSolution.getInstance().incomingConnection(clientSocket);
				System.out.println("Starting handshaking...");
		        try {
		        	clientSocket.startHandshake();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error(e);
				}
			}
			catch (IOException e)
			{
				log.info("received exception, shutting down");
				term=true;
			}
		}
	}

	public void setTerm(boolean term)
	{
		this.term = term;
		if(term) interrupt();
	}


}
