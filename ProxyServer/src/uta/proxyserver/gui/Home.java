package uta.proxyserver.gui;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import uta.proxyserver.server.*;


public class Home {
	public static Shell shlMaverickProxyServer;
	public Text txtText;
	private static Home instance;
		  
	private Home(){
		
		
	    
	    File CacheDir = new File("C:\\ProxyServer\\Cache\\");
	    if (!CacheDir.exists()) {
	    	CacheDir.mkdirs();
	    }
	    File CacheFile = new File("C:\\ProxyServer\\Cache\\cache.txt");
	    if (!CacheFile.exists()) {
	    	try {
				CacheFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    }
	    File LogDir = new File("C:\\ProxyServer\\Logs\\");
	    if (!LogDir.exists()) {
	    	LogDir.mkdirs();
	    }
	    File LogFile = new File("C:\\ProxyServer\\Logs\\logs.txt");
	    if (!LogFile.exists()) {
	    	try {
				LogFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    }
		shlMaverickProxyServer = new Shell();
		shlMaverickProxyServer.setModified(true);
		shlMaverickProxyServer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		shlMaverickProxyServer.setMinimumSize(new Point(1024, 568));
		shlMaverickProxyServer.setSize(931, 453);
		
		shlMaverickProxyServer.setText("Maverick Proxy Server");
		
		Menu menu = new Menu(shlMaverickProxyServer, SWT.BAR);
		shlMaverickProxyServer.setMenuBar(menu);
		
		MenuItem menuFile = new MenuItem(menu, SWT.CASCADE);
		menuFile.setText("Menu");
		
		Menu popupMenu_File = new Menu(menuFile);
		menuFile.setMenu(popupMenu_File);
		
		MenuItem mntmExit = new MenuItem(popupMenu_File, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		mntmExit.setText("&Exit");
		
		MenuItem menuHelp = new MenuItem(menu, SWT.NONE);
		menuHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}
		});
		menuHelp.setText("Help");
		
		Composite composite = new Composite(shlMaverickProxyServer, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		composite.setBounds(4, 4, 1000, 75);
		
		Button btnOpenCache = new Button(composite, SWT.FLAT | SWT.CENTER);
		btnOpenCache.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					@SuppressWarnings("unused")
					Process p = new ProcessBuilder("explorer.exe", "/select,C:\\ProxyServer\\Cache\\cache.txt").start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		});
		btnOpenCache.setText("Open Cache");
		btnOpenCache.setBounds(10, 0, 320, 75);
		
		Button btnOpenLogs = new Button(composite, SWT.FLAT);
		btnOpenLogs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					@SuppressWarnings("unused")
					Process p = new ProcessBuilder("explorer.exe", "/select,C:\\ProxyServer\\Logs\\logs.txt").start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnOpenLogs.setText("Open Logs");
		btnOpenLogs.setBounds(340, 0, 320, 75);
		
		Button btnExit = new Button(composite, SWT.FLAT);
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		btnExit.setText("Exit");
		btnExit.setBounds(670, 0, 320, 75);
		
		Thread server = new Server();
		server.start();
		
		txtText = new Text(shlMaverickProxyServer, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		txtText.setBounds(15, 85, 978, 414);
		
	}
	   
	public static Home getInstance(){
        if(instance == null){
            instance = new Home();
        }
        return instance;
    }
	
	public static void main(String[] args) {
		try {
			
			
		    Display display = Display.getDefault();
			@SuppressWarnings("unused")
			Home window = Home.getInstance();
			
			shlMaverickProxyServer.open();
			shlMaverickProxyServer.layout();
			
			while (!shlMaverickProxyServer.isDisposed()) {
				if (!display.readAndDispatch()) {
					
					display.sleep();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	      
	}

	static String stripExtension (String str) {
        // Handle null case specially.

        if (str == null) return null;

        // Get position of last '.'.

        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.

        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.

        return str.substring(0, pos);
    }

}
