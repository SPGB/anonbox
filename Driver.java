import java.awt.*;


import java.lang.Thread;
import java.util.ArrayList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
  * ANON BOX 
  * anonymous folder sync
  * @author: avonwodahs
  * @VERSION: BETA
  * Please join the development at https://github.com/Darkchan/Anon-Box
  **/

//main class
public class Driver implements gui
{
    protected static final Double version = 0.24;
	//some vars
	static boolean updating = false;
	static boolean options = false;
    static boolean showLogo = true;
    
    static Settings settings = new Settings(); //current settings
    static Boards boardOptions = new Boards(); //board
    static BruteSettings settingsBrute = new BruteSettings();
	public static resor resources = new resor();
	
    static options opt = new options();
    static Update upt = new Update();
    static int viewing = 0; //what is currently focused (options = 3, dont check threads when >1) 
   
    static boolean minimized = false; //if the program is in tray

    static String[] msg = new String[20];
    static int msgPointer = 0;
    
    static int state = 1; //0 is connection problem, 2 is up to date
    
    static String UrlThreadTail = ""; 
    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static BufferedImage wallpaper = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_4BYTE_ABGR);
    static long wallpaper_set = 0;
    static long last_mouse_movement = System.currentTimeMillis();
    static ArrayList<String> wallpaper_queue = new ArrayList<String>(); //threads we deleted/remove
    static Point mouseDownCompCoords;
   public static void main(String[] args) throws IOException
   {
	  //version
      
      //welcome message
      textArea.setFont(new Font("helvetica", Font.TRUETYPE_FONT, 14));
      addmsg("Welcome to Anonbox (v. " + version + ")\n");
      textArea.setEditable(false);         
      //set up the settings/load them from settings.dat and last.dat
      ObjectInputStream objectInputFile = null;
      try { // settings
    	  FileInputStream inStream = new FileInputStream("Settings.dat");
    	  objectInputFile = new ObjectInputStream(inStream);
    	  settings = (Settings) objectInputFile.readObject();
    	  SysTray.fileCount.setLabel("size: " + (Driver.settings.downloaded / 1024 / 1024) + "MB");
    	  SysTray.threadCount.setLabel("files: " + Driver.boardOptions.getThreadSize());
      } catch (Exception e) { 
    	  //if we can't load the settings we will assume its a new user
    	  addmsg("\nIt looks like this is your first time! \n\tSettings up the default settings now...");
    	  settings = new Settings();
    	  settings.bootLoad();
    	  addmsg("\nThe default anon box folder is: " + settings.getDirectory());
    	  addmsg("\nYou can customize anonbox to use any imageboard by generating an xml and customizing it");
    	  addmsg("\nPlease click options at the top to ensure everything is correct before we sync.");
    	  viewing = 4; //dont start loading threads yet
	    } finally {
	    	 if (objectInputFile != null) objectInputFile.close();
	    }
    
	    //load our boad settings
	   	try { 
	   		settingsBrute.readXML();
	        FileInputStream inStream = new FileInputStream("last.dat");
	        objectInputFile = new ObjectInputStream(inStream);
	        boardOptions = (Boards) objectInputFile.readObject();
	        settings.verify();
	   	} catch (Exception e) {
	   		if (settings.isDebug()) addmsg("\n\tCould not load last board");
	   		viewing = 4; //dont start loading threads yet
	    } finally {
	    	 if (objectInputFile != null) objectInputFile.close();
	    	 if (settings.isDebug()) addmsg("\nCurrent threads tracked: " + boardOptions);
	    	 //compare number of files to file count
	    	 boardOptions.verify();
	    }
	    
	    opt.settings();
	
	    new Thread(new Runnable() {
	    	public void run() {
	    		if (settings.getBoard() != null) addmsg("\nLoaded settings for board " + settings.getBoard());
	    		try{
	    			File file=new File(settings.getDirectory());
	            		if (!file.exists()) {
	            			if(file.mkdir())
	            				addmsg("\n Set up directory (" + settings.getDirectory() + ")");
	            			else
	            				addmsg("\n Could not set up directory (" + settings.getDirectory()  + ")");
	            		}
	            		Thread.sleep(3000);
	            		if (Driver.upt.getVer()) {
	            			addmsg("\nClosing...");
	            			Thread.sleep(2000);
	            			System.exit(0);
	            		} else {
	            			Thread.sleep(2000);
	            			settings.clearUpdate();
	            		}
	            		while(true) {
	            			Thread.sleep(3000);
	            			if (Driver.settings.getWallpaper()) {
	            				wallpaper_process_queue();
	            			}
	            		}
	    		} catch(Exception e) { e.printStackTrace(); } 
	    		if (settings.isDebug()) infoText.setText("\nDEBUG mode is enabled");
	      }
	    }).start();
	    
	      //icon
	      Image icon = Toolkit.getDefaultToolkit().getImage("bulb.gif");
	      frame.setIconImage(icon);
	      
	      //check board
	      new Thread(new Runnable() {
	    	  public void run() {
 				{
 					while(true) {
	 					try {
	 						int sleepTime = (boardOptions.getThreadSize() + 1) * 300;
	 						if (sleepTime > 15000) sleepTime = 15000;
							if (viewing < 3) boardOptions.list();
							panel.repaint();
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
 					}
 				}
	    	  }
	      }).start();

		//systray
		tIcon.setPopupMenu(SysTray.createPopupMenu());
		frame.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowIconified(WindowEvent e) {
	        	frame.setVisible(false);
	            minimized = true;
	            try {
	                	SystemTray.getSystemTray().add(tIcon);
	            } catch (AWTException e1) {
	            	e1.printStackTrace();
	            }
	        }
		});
		
        tIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Driver.frame.setVisible(true);
                minimized = false;
                Driver.frame.setExtendedState(Frame.NORMAL);
                SystemTray.getSystemTray().remove(tIcon);
            }
         });
        
          showLogo = false;	   
          
	      topPanel.setVisible(true);
	      panel.setVisible(true);
	      optPanel.setVisible(false);
	      	      
	      textArea.setMargin(new Insets(10,10,10,10));  
	      textArea.repaint();
	      textArea.setOpaque(false);
	      textScroll.setOpaque(false);
	      topPanel.setOpaque(false);
	      topPanel.setPreferredSize(new Dimension(150, 150));
	      optPanel.setPreferredSize(new Dimension(900, 275));
	      frame.setResizable( false );
	      frame.setBackground(bColor);
	      optPanel.setBackground(bColor);
	      frame.removeNotify(); //remove border
	      frame.setLocation(screenSize.width / 2 - 350,screenSize.height / 2 - 250); //set 100px margin
	      frame.setUndecorated(true); //remove title bar
	      panel.setBounds(new Rectangle(0, 0, frame.getWidth(), frame.getHeight()));
	      textScroll.setBorder(null);
	      frame.add(panel);
	      panel.add(topPanel, BorderLayout.NORTH);
	      panel.add(optPanel, BorderLayout.SOUTH);
	      frame.setPreferredSize(new Dimension(700, 500));
	     
	      textArea.setLineWrap(true); //word wrap
	      textArea.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()-400));
	      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      if (settings.fullscreen()) {
	    	  frame.setUndecorated(true);
	      }
	      frame.pack();
	      frame.setTitle("Anon Box");
	      frame.setIconImage(SysTray.getImage(0));
	      frame.setVisible(true);
	      //fullscreen adjust
			if (settings.fullscreen()) {
	  			fullscreen();
	  		}
	  		frame.addMouseListener(new MouseListener(){
	  			public void mouseReleased(MouseEvent e) {
						mouseDownCompCoords = null;
					}
					public void mousePressed(MouseEvent e) {
						mouseDownCompCoords = e.getPoint();
					}
					public void mouseExited(MouseEvent e) {
					}
					public void mouseEntered(MouseEvent e) {
					}
					public void mouseClicked(MouseEvent e) {
					}
			});
			frame.addMouseMotionListener(new MouseMotionListener(){
			public void mouseMoved(MouseEvent e) {
				last_mouse_movement = System.currentTimeMillis();
				if (!topPanel.isVisible()) {
							topPanel.setVisible(true);
							panel.repaint();
				}
				}
				public void mouseDragged(MouseEvent e) {
					Point currCoords = e.getLocationOnScreen();
					frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
				}
			});
   }
      
 
  public static void fullscreen () {
	  //full screen mode
	  
	  frame.setBounds(0,0,screenSize.width, screenSize.height);
  }
  
  public static void clearmsg() {
	  //clears all of the messages on screen
	  msgPointer=0;
  }
  
  public static void addmsg(String s) {
	  //add a message
	  if (state == 0 && !settings.isDebug()) {
		  trayWorking(0);
		  s = "Connection problem detected...";
	  }
	  if (state == 2 && !settings.isDebug()) {
		  Driver.infoText.setText("Up to date");
	  }
	 if (msgPointer < 20) {
		 msg[msgPointer] = s;
		 msgPointer++;
		 textArea.append(s);
	 } else {
		 if (msg[msgPointer-1].equals(s)) return;
		 if (s.subSequence(0, 1).equals("\t")) {
			 msg[msgPointer-1] = msg[msgPointer-1] + "  " + s;
			 return;
		 }
		 textArea.setText(msg[0]);
		 for (int i = 1; i < 20; i++) {
			 msg[i-1] = msg[i];
			 textArea.append(msg[i]);
		 }
		 msg[msgPointer-1] = s;
	 }
	 panel.repaint();
	 topPanel.setVisible((last_mouse_movement > System.currentTimeMillis() - 3000));
  }
  public static void wallpaper_process_queue() {
	  if (Driver.wallpaper_queue.isEmpty()) return;
	  String path = Driver.wallpaper_queue.get(0);
	  Driver.wallpaper_queue.remove(0);
	  try {
			Driver.wallpaper = ImageIO.read(new File(path));
	  } catch (IOException e) {
			Driver.addmsg("\nError, can't update wallpaper");
			e.printStackTrace();
	  }
	  Driver.wallpaper_set = System.currentTimeMillis();
	  panel.repaint();
  }
  public static void trayWorking (int i) {  
	  //i = 0 for normal, i = 1 for download, i = 2 for upload
	  switch (i) {
	  	case 0:  tIcon.setToolTip("Anon box"); tIcon.setImage(SysTray.getImage(0)); break;
	  	case 1:  tIcon.setToolTip("Anon box - Downloading"); tIcon.setImage(SysTray.getImage(1)); break;
	  	case 2:  
	  		tIcon.setToolTip("Anon box - Uploading");
	  		TrayIcon t[] = SystemTray.getSystemTray().getTrayIcons(); 
	  		if (t.length != 0) t[0].displayMessage("Uploading", "uploading a file", TrayIcon.MessageType.INFO); tIcon.setImage(SysTray.getImage(2));
	  		break;
	  }
	 
  }
  
  }