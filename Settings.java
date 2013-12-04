import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.URLDecoder;

public class Settings implements Serializable
{
   /**
	 * Saves the basic settings to file and reads it at startup for concurrency
	 */
	
	private static final long serialVersionUID = -805483592994115788L; 
	private String board;      // board name
	private int boardId;      // board id
	private int maxThreads; //number of threads to hold at any time, 0 = infinite
	private String site; //the site to grab from
	
	private String directory; //directory to store threads in
	private boolean fullscreen;
	private boolean debug; //show more info/calls
	private boolean oneFolder; //store all threads in the directory
	private boolean alert; //when we add a new file, show popup
	
	private File oldf; //for when we update to a new version
	
	public int downloaded;
	public int uploaded; 
	
	private boolean onStartup; //run anonbox when the user loads his computer
	private boolean wallpaperMode;
	
	public String threadTag;
	public boolean api; //api calls if T, F for brute force
	public Settings()
	{
	   //default values
	   this.directory = "C:\\anon box";
	   this.site = "boards.4chan.org";
	   this.board = "b";
	   this.api = false;
	   
	   this.oneFolder = true;
	   this.debug = false;
	   this.maxThreads = 0;
	   
	   this.onStartup = true;
	}
   
   public Settings(String b, boolean fullscreen)
   {
      this.board = b;
      this.fullscreen = fullscreen;
   }
   
   public void setScreen(boolean fullscreen)
   {
      this.fullscreen = fullscreen;
   }
   public void setWallpaper(Boolean b)
   {
	   this.wallpaperMode = b;
	   if (!b) {
		   Driver.wallpaper_set = 0;
	   }
   }
   public Boolean getWallpaper()
   {
	   return this.wallpaperMode;
   }
   public void setBoard(String b)
   {
      this.board = b;
   }
   public void setDirectory(String d)
   {
      this.directory = d;
   }
   public void setBoardId(int id) 
   {
	   this.boardId = id;
   }
   public void setStartup(Boolean b) 
   {
	   this.onStartup = b;
	   if (b) { bootLoad(); }
   }
   public Boolean getStartup()
   {
	   return this.onStartup;
   }
   public boolean fullscreen()
   {
      return fullscreen;
   }
   
   public String getBoard()
   {
      return board;
   }
   public String getDirectory()
   {
      return directory;
   }
   public String getSite()
   {
	  return site; //usually "darkchan.com"
   }
   public String getSocket()
   {
	 if (site.equals("127.0.0.1/")) {
		 return "localhost";
	 } else {
		 return site;
	 }
   }
   public int getBoardId() 
   {
	   return this.boardId;
   }
   
   void bootLoad() { //create a symb link in the users autostart folder
	   try {
		   String path = Driver.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		   String decodedPath = URLDecoder.decode(path, "UTF-8");
		   String startupFolder = System.getProperty("user.home");
		   String script = "Set sh = CreateObject(\"WScript.Shell\")"
			   + "\nSet shortcut = sh.CreateShortcut(\"" + startupFolder + "\\Start Menu\\Programs\\Startup\\anonbox.lnk\")"
			   + "\nshortcut.TargetPath = \"" + decodedPath.substring(1,decodedPath.length()) + "anonbox.exe\""
			   + "\nshortcut.Save";

			   File file = new File("c:/temp.vbs");
			   FileOutputStream fo = new FileOutputStream(file);
			   fo.write(script.getBytes());
			   fo.close();
			   //Runtime.getRuntime().exec("wscript.exe D:/temp/crear-acceso-directo.vbs");
			   Runtime.getRuntime().exec("wscript.exe " + file.getAbsolutePath() );
		} catch (Exception x) {
		    System.out.println(x);
		}
   }
   
   //verify's the board ID
   public boolean verify () 
   {
	   int status = 0;
	   try {
	   	  
      	  String url = "http://" + this.getSite() + "/api?api=verifyBoard&b=" + this.getBoard();
      	  Driver.addmsg("\nVerifying board: " + this.getBoard() + ", site: " + this.getSite());
          Socket s = new Socket(this.getSocket(), 80);
          s.setSoTimeout(5000);
          BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
          PrintWriter socketOut = new PrintWriter(s.getOutputStream());
          socketOut.println("GET " + url + " HTTP/1.1");
          socketOut.println("Host: " + Driver.settings.getSite());
          socketOut.println("User-Agent: Mozilla/5.0 (Windows NT 5.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
          socketOut.println("Connection: close");
          socketOut.println();
          socketOut.flush();

          String line;
          String result = in.readLine();
          status = Integer.parseInt(result.substring(9, 12));
          Driver.addmsg("\t" + status);
          
          while ((line = in.readLine()) != null){
              result += line + "\n";               
          }
          
          if (result.equals("false") || result.equals("")) {
        	  if (Driver.settings.api) Driver.viewing = 4;
        	  Driver.addmsg("\n-Invalid board");
        	  return false;
          } else {
        	this.api = true;
	        String[] parse = result.split("board=\""); //sift out the board name
	        String[] parse2 = parse[1].split("\" ID=\""); //sift out the board ID
	        String[] parse3 = parse2[1].split("\""); //sift out the board ID
	        
	        this.setBoardId(Integer.parseInt(parse3[0])); //board ID
	        this.setBoard(parse2[0]);
	        
        	Driver.addmsg("\n-Verified board " + parse2[0]);
          	return true;
          }
      } catch (Exception e){
    	Driver.addmsg("\tforcing index");
    	this.api = false;
    	Driver.settingsBrute.readXML();
    	if (Driver.settingsBrute.threadIdentifier.equals("")) {
    		Driver.addmsg("Could not find a ruleset for site: " + Driver.settings.getSite() +
    				"\nPlease ensure sites.xml in your anonbox folder contains the site");
    		Driver.state = 4;
    	}
      	return false;
      }   
   }
   
   //serializes settings
   public void serial() 
   {
 	  try {  //Settings
               FileOutputStream outStream =  new FileOutputStream("Settings.dat");
               ObjectOutputStream objectOutputFile =  new ObjectOutputStream(outStream);
               objectOutputFile.writeObject(this);
               objectOutputFile.close();
       } catch (IOException e) {
             	  e.printStackTrace();
       }
   }

	public String savepath(long id, String file) { //where we save files
		if (oneFolder) {
			return Driver.settings.getDirectory() + "\\" + id + "_" + file;
		} else {
			return Driver.settings.getDirectory() + "\\" + id + "\\" + file;
		}
	}

	public void clearUpdate() {
	    //load update settings (clear old files)
		if (oldf == null) return;
	   	try { 	   		
	   		Driver.addmsg("\nChecking for old files...");
	        if (this.oldf.exists()) {
	        	if (oldf.delete()) Driver.addmsg("\t y");
	        }
	        oldf = null;
	        this.serial();
	   	} catch (Exception e) { Driver.addmsg("\t " + e);}
	   	Driver.addmsg("\t done");
	}
/*
 * GETTERS & SETTERS
 */
public void setDebug(boolean debug) {
	this.debug = debug;
}

public boolean isDebug() {
	return debug;
}

public void setMaxThreads(int maxThreads) {
	this.maxThreads = maxThreads;
}

public int getMaxThreads() {
	return maxThreads;
}

public void setSite(String s) {
	this.site = s;
}

public void setOneFolder(boolean oneFolder) {
	this.oneFolder = oneFolder;
}

public boolean isOneFolder() {
	return oneFolder;
}

public void setAlert(boolean alert) {
	this.alert = alert;
}
public void setOldF(File f) {
	this.oldf = f;
}
public boolean isAlert() {
	return alert;
}
}
