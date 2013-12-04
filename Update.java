import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.MessageDigest;


public class Update implements Serializable {
		/*
		 * Update
		 * checks for new versions and displays a changelog
		 */

	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double currentVersion;
	public String currentMD5;
	
	   public Update() {
	   }
	   
	   public boolean getVer()
		{
		   String result = "";
		   String url = "";
		   String site = "";
		   if (Driver.settings.api) {
			   site =  Driver.settings.getSite();
		   } else {
			   site = "samgb.com";
		   }
		   if (!Driver.settings.api) return false; //no api support
	       try {
	    	   url = "http://" + site + "/anonbox.php?ver";
	           Socket s = new Socket(Driver.settings.getSocket(), 80);
	           BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	           PrintWriter socketOut = new PrintWriter(s.getOutputStream());
	           socketOut.println("GET " + url);
	           socketOut.println();
	           socketOut.flush();

	           result = in.readLine();
	           currentVersion = Double.parseDouble(result);
	           if (currentVersion > Driver.version) {
	        	   Driver.addmsg("\nDownloading update " + currentVersion);
	        	   downloadUpdate();
	        	   return true;
	           }
	           return false;
	       } catch (Exception e){
	    	if (Driver.settings.isDebug()) Driver.addmsg("\nUpdate exception @ " + url + "\nUpdate returns:" + result);
	       	return false;
	       }     
	  }
	  //gets the latest version's md5 checksum
	  public boolean getMD5()
		{	   
		  String url = "";
		  String site = "";
		   if (Driver.settings.api) {
			   site =  Driver.settings.getSite();
		   } else {
			   site = "darkchan.com";
		   }
	       try {
	    	   url = "http://" + site + "/anonbox.php?md5";
	           Socket s = new Socket(Driver.settings.getSocket(), 80);
	           BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	           PrintWriter socketOut = new PrintWriter(s.getOutputStream());
	           socketOut.println("GET " + url);
	           socketOut.println();
	           socketOut.flush();
	           
	           this.currentMD5 = in.readLine();
	           return true;
	       } catch (Exception e){
	    	if (Driver.settings.isDebug()) Driver.addmsg("\nMD5 exception @ " + url + "\nMD5 returns:" + this.currentMD5);
	       }
	       return false;
	  }
	  public void downloadUpdate()
		{
			if (currentVersion == 0 || currentVersion <= Driver.version) return;

			String site = "";
			if (Driver.settings.api) {
				   site =  Driver.settings.getSite();
			} else {
				   site = "darkchan.com";
			}
			   
			Driver.viewing = 4;
	      	File file=new File("Anon Box" + currentVersion + ".exe");
	      	if (file.exists()) {
	      		Driver.addmsg("\nNew Version already downloaded, please use that instead.");
	      		return;
	      	}
	      	
	 		Driver.trayWorking(1);
	 		String signature = "";
			try {
				String url = "http://" + site + "/anonbox" + currentVersion; //get threads for api
		    	if (Driver.settings.isDebug()) Driver.addmsg("\n(debug) Updating @ " + url);
		    	Socket s = new Socket(Driver.settings.getSocket(), 80);
		        InputStream in = s.getInputStream();
		        PrintWriter socketOut = new PrintWriter(s.getOutputStream());
		        socketOut.println("GET " + url);
		        socketOut.println();
		        socketOut.flush();
		        OutputStream out = new FileOutputStream(file);

		        byte[] buffer=new byte[1024];
		        int readData;
		        
		        MessageDigest md5 = MessageDigest.getInstance("MD5");
		        
		        while((readData=in.read(buffer))!=-1){
		        	out.write(buffer,0,readData);
		        	md5.update(buffer,0,readData);
		        }
		        
		        out.close();
		        in.close();
	        	signature = new BigInteger(1,md5.digest()).toString(16);
			} catch (Exception e){
	        	Driver.addmsg("\nCould not download update " + e);
	        	Driver.trayWorking(0);
	        	return;
	        }
			
			Driver.addmsg("\nSuccessfully updated to version " + currentVersion + "\nMD5 checksum: " + signature);
			
			this.getMD5();
			if (!this.currentMD5.equals(signature)) {
				Driver.addmsg("\nMD5 signature doesn't match, proceed at own risk.");
				return;
			}
			
			Driver.trayWorking(0);
			String current = Driver.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			
			try {
				current = URLDecoder.decode(current, "UTF-8");
			} catch (UnsupportedEncodingException e1) { }
			File f = new File(current);
			if (f.exists()) {
				Driver.addmsg("\nPreparing to delete old files...");
				Driver.settings.setOldF(f);
				Driver.settings.serial();
			} 
			Desktop d = null;
			if (Desktop.isDesktopSupported() && file.exists()) {
				d = Desktop.getDesktop();
				try {
					Driver.addmsg("\nOpening new file");
					d.open(file);
				} catch (IOException e) { Driver.addmsg(e.toString()); }
			}
		}
	  
	  
}
