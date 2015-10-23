package uta.proxyserver.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Display;

import uta.proxyserver.controller.*;
import uta.proxyserver.gui.Home;

public class ProxyServer extends Thread {
	ArrayList<String> httpRequest;	
	int temp=1;
	String permHost="";
	int port;
	static String cachedHost;
	Socket serverSocket=null;			
	Socket clientSocket=null;
	public HashMap <String,String> cache = new HashMap<String,String>();
	public ProxyServer() 
	{	
		httpRequest = new ArrayList<String>(10);
		readCacheMap();
	}
	
	
	public ProxyServer(Socket socket) 
	{
		super("ProxyThread");
        clientSocket = socket;
        readCacheMap();
	}
	
	//function used to write to log file
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

	//used to reach cache
	public void readCacheMap()
	{
		FileReader file1 = null;
		try {
			file1 = new FileReader("C:\\ProxyServer\\Cache\\cache.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {

		    BufferedReader reader = new BufferedReader(file1);
		    String line = null;
		    String[] temp;
//		    line=reader.readLine();
		    while ((line = reader.readLine()) != null) {
		    	
		        temp = line.split("\t");
		        if(!cache.containsKey(temp[0]))
		        	cache.put(temp[0],temp[1]);
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}	
	}
	
	//read HTTP request
	private String readHttpRequest(BufferedReader clientToProxy) throws Exception
	{		
		String clientData;				
		while((clientData=clientToProxy.readLine())!=null)
		{
			if(clientData.length()==0)break;
			httpRequest.add(clientData);
//			System.out.println("clientData: "+clientData);
		}
		System.out.println("HR: "+httpRequest.size());
		return getHost();
	}
	
	//getHost Name
	private String getHost() throws Exception
	{
		String host=null;
		if(httpRequest.size()!=0)
		{
			String firstLine[]=httpRequest.get(0).split(" ");
			String secondLine[]=httpRequest.get(1).split(" ");
			if(firstLine[0].equals("GET"))
			{
				for(int i=0;i<firstLine.length;i++)
					System.out.println("firstLine["+i+"]"+firstLine[i]);
				firstLine[1]=firstLine[1].substring(1,firstLine[1].length());
				int slashIndex=-1,ampIndex=-1;
				slashIndex=firstLine[1].indexOf('/');
				ampIndex=firstLine[1].indexOf('&');
				int hostLastIndex;
				
				if(slashIndex==-1&&ampIndex==-1)
				{
					host=firstLine[1].replaceFirst("/", "");
					firstLine[1]="/";
					permHost=host;
				}
				else if(slashIndex!=-1&&ampIndex!=-1)
				{
					hostLastIndex=(slashIndex<ampIndex)?slashIndex:ampIndex;
					host=firstLine[1].substring(0,hostLastIndex);
					firstLine[1]=firstLine[1].substring(hostLastIndex,firstLine[1].length());
				}			
				else
				{
					hostLastIndex=(slashIndex>ampIndex)?slashIndex:ampIndex;
					host=firstLine[1].substring(0,hostLastIndex);
					firstLine[1]=firstLine[1].substring(hostLastIndex,firstLine[1].length());
				}
				
							
				replaceInRequest(firstLine,0);
				
				secondLine[1]=host;
				replaceInRequest(secondLine,1);
			}
			else
			{
				
				WriteToFile("Not a 'GET' request.");
				Display.getDefault().syncExec(new Runnable() {
				    public void run() {
				        Home.getInstance().txtText.append("\nNot a 'GET' request.");
				    }
				});
				System.out.println("Not a 'Get' request");
				host=null;
			}		
			return host;
		}
		else 
			return null;
	}
	
	//internal processing, used to manuplate requests
	private void replaceInRequest(String[] arr,int pos)
	{
		String replacement="";
		for(String arrEntry:arr)
		{
			replacement+=arrEntry+" ";
		}
		replacement=replacement.substring(0,replacement.length()-1);
		httpRequest.remove(pos);
		httpRequest.add(pos,replacement);
	}
	
	//send httpRequest
	private void sendHttpRequest(BufferedWriter proxyToServer) throws IOException
	{
		for(String requestLine:httpRequest)
		{					
			if(requestLine.contains("GET"))
			{
				
				String[] temp = requestLine.split(" ");
				cachedHost=temp[1];
				if(cache.containsKey(permHost+cachedHost))
				{
					
					WriteToFile("File found in cache: '"+permHost+cachedHost.substring(0,cachedHost.length()-1)+"'. Reading Response from cache.");
					WriteToFile("Response found in the following file: '"+cache.get(permHost+cachedHost)+"'");
					
					Display.getDefault().syncExec(new Runnable() {
					    public void run() {
					        Home.getInstance().txtText.append("\n\nFile found in cache: '"+permHost+cachedHost.substring(0,cachedHost.length()-1)+"'. Reading Response from cache.\n");
					        Home.getInstance().txtText.append("Response found in the following file: '"+cache.get(permHost+cachedHost)+"'\n\n");
					    }
					});
				}
			}
			proxyToServer.write(requestLine+"\r\n");	
			
			WriteToFile(requestLine);
			
			Display.getDefault().syncExec(new Runnable() {
			    public void run() {
			        Home.getInstance().txtText.append("\n"+requestLine);
			    }
			});
			System.out.println(requestLine);					
		}
		proxyToServer.write("\r\n");
		proxyToServer.flush();
		System.out.println("\n");
	}
	
	//thread
	public void run() 
	{
		
		
		int clientNo=1;
		
		
		BufferedWriter proxyToServer=null;
		BufferedInputStream serverToProxy=null;
		BufferedReader clientToProxy=null;			
		BufferedOutputStream proxyToClient=null;
		
		try {
//			while(true)
//			{
				ProxyServer proxyServer = new ProxyServer();
				
				temp=clientNo;
				clientNo++;
				
				WriteToFile("New Connection accepted.");
				
				Display.getDefault().syncExec(new Runnable() {
				    public void run() {
				        Home.getInstance().txtText.append("\nNew Connection accepted.");
				    }
				});
				System.out.println("Connection accepted, serving client "+clientNo);
				
				proxyToClient=new BufferedOutputStream(clientSocket.getOutputStream());				
				clientToProxy=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				
				WriteToFile("Reading http request...");
				
				Display.getDefault().syncExec(new Runnable() {
				    public void run() {
				        Home.getInstance().txtText.append("\nReading http request...");
				    }
				});
				System.out.println("Reading http request ");
				String host=proxyServer.readHttpRequest(clientToProxy);
				UrlValidator urlValidator = new UrlValidator();
				if(host!=null && urlValidator.isValid("http://"+host) )
				{
					permHost=host;
					try
					{
//						System.out.println(host+"<--host");
					serverSocket= new Socket(host,80);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					if(serverSocket==null)
					{
						WriteToFile("Host Not Found: "+host);
					
						Display.getDefault().syncExec(new Runnable() {
						    public void run() {
						        Home.getInstance().txtText.append("\nHost Not Found: "+host+"\n\n");
						    }
						});
					}
					proxyToServer= new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
					serverToProxy = new BufferedInputStream(serverSocket.getInputStream());
					@SuppressWarnings("unused")
					BufferedReader responseReader= new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
					
					WriteToFile("Sending Request to server...");
					
					Display.getDefault().syncExec(new Runnable() {
					    public void run() {
					        Home.getInstance().txtText.append("\nSending Request to server...");
					    }
					});
					System.out.println("Sending Request to server... \n\n");
					proxyServer.sendHttpRequest(proxyToServer);
					
					WriteToFile("Reading response from server...");
					
					Display.getDefault().syncExec(new Runnable() {
					    public void run() {
					        Home.getInstance().txtText.append("\nReading response from server...\n\n");
					    }
					});
					System.out.println("Reading response from server... \n\n\n");
					byte[] buffer = new byte[32767];
					int len = serverToProxy.read(buffer);
					
					PrintWriter writer = null;
					OutputStream ostream = null;
					if(len>0)
					{
						readCacheMap();
						int filename=cache.size()+1;
						if(!cache.containsKey(host+cachedHost))
						{
							writer = new PrintWriter(new BufferedWriter(new FileWriter("C:\\ProxyServer\\Cache\\cache.txt", true)));
							writer.println(host+cachedHost+"\t"+filename+".dat");
							System.out.println("CachedHost: "+host+cachedHost);
							ostream = new FileOutputStream("C:\\ProxyServer\\Cache\\"+filename+".dat");
							writer.close();
						}
					}
					WriteToFile("Host Address: "+host+cachedHost);
					Timestamp startTime=new Timestamp(System.currentTimeMillis());
					WriteToFile("Byte Length: "+len);
					while (len != -1) {
					    proxyToClient.write(buffer, 0, len);
					    if(ostream!=null)
					    	ostream.write(buffer, 0, len);
					    WriteToFile("Respone Received: \n"+new String(buffer, "UTF-8"));
					    if(len<32767)break;
					    len = serverToProxy.read(buffer);		
					    
					}
					if(ostream!=null)
						ostream.close();
					Timestamp endTime=new Timestamp(System.currentTimeMillis());
					long a = startTime.getTime();
					long b =endTime.getTime();
					WriteToFile("Time Taken: "+(b-a)+" ms");
					proxyToClient.flush();		
					serverSocket.close();			
					//clientSocket.close();
					proxyToServer.close();
					serverToProxy.close();
					clientToProxy.close();				
					proxyToClient.close();	
				}
				else
				{
					if(host!=null && !urlValidator.isValid("http://"+host))
					{
						
						WriteToFile("Not a Valid Hostname: "+host);
						Display.getDefault().syncExec(new Runnable() {
						    public void run() {
						        Home.getInstance().txtText.append("\nNot a Valid Hostname: "+host+"\n\n");
						    }
						});
						System.out.println("Host NULL");
					}
					else
					{
						
						WriteToFile("Invalid Host: "+permHost+" (Could not resolve Hostname due to Redirects)");
						
						Display.getDefault().syncExec(new Runnable() {
						    public void run() {
						        Home.getInstance().txtText.append("\nInvalid Host: "+permHost+" (Could not resolve Hostname due to Redirects)\n\n");
						    }
						});
						System.out.println("Host NULL");
					}
				}
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
//		finally
//		{
//			try {
//				
//				//clientSocket.close();
//				if(serverSocket !=null )
//					serverSocket.close();
//				if(serverToProxy !=null)
//					serverToProxy.close();
//				if(clientToProxy !=null)				
//					clientToProxy.close();				
//				if(proxyToClient !=null)
//					proxyToClient.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
	}

}
