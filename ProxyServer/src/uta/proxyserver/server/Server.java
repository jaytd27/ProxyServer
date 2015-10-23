package uta.proxyserver.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.sql.Timestamp;

import org.eclipse.swt.widgets.Display;

import uta.proxyserver.gui.Home;
import uta.proxyserver.gui.NumberInputDialog;

public class Server extends Thread {
	boolean listening = true;
	int port1=0;
	ServerSocket proxySocket=null;
	public void WriteToFile(String print)
	{
		PrintWriter logger = null;
		try {
			logger = new PrintWriter(new BufferedWriter(new FileWriter("C:\\ProxyServer\\Logs\\logs.txt", true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.println("["+new Timestamp(System.currentTimeMillis())+"]: "+print);
		logger.close();	
	}
	
	public void run() {
		
		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    	
		    	//input port for running the server
		    	@SuppressWarnings("static-access")
				NumberInputDialog dialog = new NumberInputDialog(Home.getInstance().shlMaverickProxyServer);
		    	port1=dialog.open();
		    }
		});
		
		
		int port=0;
		
		if(port1>0)
			port=port1;
		else 
		{
			WriteToFile("Invalid Port or Port not specified. Defaulting to Port: 8080");
			
			
			Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			    	Home.getInstance().txtText.append("\nInvalid Port or Port not specified. Defaulting to Port: 8080\n");
			    }
			});
			
			port=8080;
		}										
	    
		int proxyPort=port;
		
		try {
			//starting server on the port specified or default port 8080
			proxySocket = new ServerSocket(proxyPort);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		WriteToFile("Server started, Listening on Port: "+proxyPort);

		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    	Home.getInstance().txtText.append("\nServer started, Listening on Port: "+proxyPort+"\n\n");
		    }
		});
		
		Thread server = null;
		
		WriteToFile("Waiting for clients...");
		
		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    	Home.getInstance().txtText.append("\nWaiting for clients...");
		    }
		});
		

		System.out.println("Waiting for clients...");
		while(listening)
		{
			try {
				//Accept incomming connection for further processing
				server = new ProxyServer(proxySocket.accept());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			server.start();
	//		if(proxySocket!=null)				
	//		{
	//			if(!proxySocket.isClosed())
	//				try {
	//					proxySocket.close();
	//				} catch (IOException e1) {
	//					// TODO Auto-generated catch block
	//					e1.printStackTrace();
	//				}
	//		}
		}	
	}
}
