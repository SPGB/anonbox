import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.activation.MimetypesFileTypeMap;


public class Thread implements Serializable {
	/*
	 * for each thread and it's children (files)
	 */
	
	private static final long serialVersionUID = 1L;
	private long id;
	private int fileCount;
	private ArrayList<String> files = new ArrayList<String>();
	
	private ArrayList<String> temp = new ArrayList<String>(); //for no api
	
	public Thread(long i) {
		this.id = i;
	}
	
	public String toString() {
		return Long.toString(this.id);
	}
	
	public long getId() {
		return this.id;
	}
	
	//ensure our children don't have downs
	public boolean validate() {
		boolean changes = false;
		if (files == null || files.size() == 0) { files =  new ArrayList<String>(); fileCount = 0; return false; }
		int j = files.size();
		for (int i = 1; i < j; i++) {
          	File file=new File(Driver.settings.savepath(id, files.get(i)));
          	if (!file.exists()) {
          		if (downloadImage(files.get(i))) {
          			Driver.addmsg("\n reup: " + files.get(i));
          			changes = true;
          		} else {
          			Driver.addmsg("\n couldn't grab " + files.get(i));
          			files.remove(files.get(i));
          			return false;
          		}
          	}
		}
		if (files.size() != fileCount) {
			if (Driver.settings.isDebug()) Driver.addmsg("\n\tSize mismatch array: " + files.size() + " and #: " + fileCount);
			fileCount = files.size();
		}
		if (!Driver.settings.isOneFolder()){ //as long as its split into folders
			File f = new File(Driver.settings.getDirectory() + "\\" + this.getId());
			try{
				File f2[] = f.listFiles();
				if (f2 != null) {
					for (File f3 : f2) {
						if (!f3.getName().equals("Thumbs.db")) {
							if (files == null || !files.contains(f3.getName())) {
								upload(f3.getName());
								changes = true;
							}	
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				return changes;
			} 
		}
		return changes;
	}
	
	//give the thread a home
	public void addDir () {
		File f = new File(Driver.settings.getDirectory() + "\\" + this.getId());
		try{
			if(!f.exists() && f.mkdir())
				Driver.addmsg("\n\t +" + this.id + " (" + this.getCount() + ")");
		}catch(Exception e){
			System.out.println(e);
		} 
	}
	
	//remove that home!
	public boolean remove() {	
		int count = 0;
		if (Driver.settings.isOneFolder()) {
			for (String i : files) {
				File f = new File(Driver.settings.getDirectory() + "\\" + i);
				if (f.delete()) {
					count++;
				} else {
					Driver.addmsg("\n\t could not remove " + f.getName());
				}
			}
		} else {
			File f = new File(Driver.settings.getDirectory() + "\\" + id);
			try{
				File f2[] = f.listFiles();
				int j = f2.length; 
				for (int i = 0; i < j; i++) {
					if (f2[i].delete()) {
						count++;
					} else {
						Driver.addmsg("\n\t could not remove " + f2[i].getName());
					}
				}
				if(f.delete()) {
					Driver.addmsg("\n\t -" + this.id + " (" + this.getCount() + ") -" + count + " files");
				} else {
					Driver.addmsg("\n\t couldn't delete " + this);
					if (!f.canExecute()) Driver.addmsg("\n\t cant execute ");
					if (!f.canRead()) Driver.addmsg("\n\t cant read ");
					if (!f.canWrite()) Driver.addmsg("\n\t cant write ");
				}
			}catch(Exception e){
				return false;
			} 
		}
		Driver.boardOptions.removeThread(this);
		return true;
	}
	
	//uploads a file (filename: inp) to the server
	public boolean upload(String inp) {
		
		if (!Driver.settings.api) return false; //not supported
		
		File f = new File(Driver.settings.savepath(id, inp));
		File f2 = null;
		String f2Name = null;
		String f1Name = f.getName();
		try {
			if(!f.exists()) return false;
		} catch(Exception e){
			e.printStackTrace();
		} 
        try{
        	Driver.trayWorking(2);
    		URLConnection conn = null; 
    		OutputStream os = null; 
    		InputStream is = null; 
    		String CrLf = "\r\n"; 
        	URL url = new URL("http://" + Driver.settings.getSite() + "/api?api=upload&id=" + this.id); 
        	
        	conn = url.openConnection(); 
        	conn.setDoOutput(true); 
        	
        	//open file
        	FileInputStream imgIs = new FileInputStream(f);
        	byte []imgData = new byte[imgIs.available()]; 
        	if (imgIs.available() <= 1) {
        		if (Driver.settings.isDebug()) Driver.addmsg("\n\tBlank file");
        		return false; 
        	}
        	
        	//get mime
        	MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        	String mimeType = mimeTypesMap.getContentType(f);
        	
        	imgIs.read(imgData); 
        	
        	Driver.addmsg("\nAttempting upload (" + mimeType + ")"); 
        	String message1 = "-----------------------------4664151417711" + CrLf +
        	"Content-Disposition: form-data; name=\"submitted\"" + CrLf + CrLf + 
        	"TRUE" + CrLf +
        	"-----------------------------4664151417711" + CrLf +
        	"Content-Disposition: form-data; name=\"file[]\"; filename=\"" + f.getName() + "\"" + CrLf +
        	"Content-Type: image/jpeg" + CrLf + CrLf; 

        	//file is inserted here
        	
        	String message2 = CrLf + "-----------------------------4664151417711--" + CrLf; 
        	
        	conn.setRequestProperty("POST", url + " HTTP1/1"); 
        	conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------4664151417711"); 
        	// might not need to specify the content-length when sending chunked data. 
        	conn.setRequestProperty("Content-Length", String.valueOf((message1.length() + message2.length() + imgData.length))); 

        	os = conn.getOutputStream();  
        	os.write(message1.getBytes()); 
        	
        	// SEND THE IMAGE 
        	int index = 0; int size = 1024; 
        	do{ 
	        	//System.out.println("write:" + index); 
	        	if((index+size)>imgData.length){ 
	        		size = imgData.length - index; 
	        	} 
	        	os.write(imgData, index, size); 
	        	index+=size; 
        	} while(index<imgData.length); 
        	Driver.settings.uploaded += index;
        	Driver.addmsg("\n\tSent " + index/1024 + " kb"); ; 

        	os.write(message2.getBytes()); 
        	os.flush(); 
        	
        	is = conn.getInputStream(); 
	
        	char buff = 512; 
        	int len; 
        	byte []data = new byte[buff]; 
        	String line = "";
        	do{ //read
        		len = is.read(data); 
        		if(len > 0 && line != null){ 
        			line += new String(data, 0, len);
        		} 
        	} while(len>0); 
        	if (!line.equals("")) {
		        String[] parse = line.split("!file!"); //sift out the board name
		        //System.out.println(parse.length);
		        if (parse.length < 2) {
		        	Driver.addmsg("\nCouldn't upload(1) (" + line + ")");
		        	return false;
		        }
		        else if (!parse[1].equals("")) {
					f2Name = parse[1];
					f2 = new File(Driver.settings.savepath(id, f2Name));
				} else {
					Driver.addmsg("\nCouldn't upload(2) (" + line + ")");
				}
        	}
			imgIs.close();
			os.close(); 
			is.close(); 
        	}catch(Exception e){ 
        		Driver.trayWorking(0);
        		e.printStackTrace(); 
        	}finally{       		        		
        		if (f2 != null) {	
    				fileCount++;	
    				files.add(f2Name);
    				if (f.renameTo(f2)) {
    					Driver.addmsg("\nSuccessfully uploaded " + f1Name);
    					Driver.trayWorking(0);
    					Driver.boardOptions.serial(); //save
    				}
        		}
        	} 
        return true;
   }
	
	//check the number of files we should have
	public void updateCount() {
			if (Driver.settings.api) {
			        try {
			        	String url = "http://" + Driver.settings.getSite() + "/api?api=countfiles&id=" + this.id; //get threads for api
			        	//System.out.println(url);
			        	Socket s = new Socket(Driver.settings.getSocket(), 80);
			            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			            PrintWriter socketOut = new PrintWriter(s.getOutputStream());
			            socketOut.println("GET " + url);
			            socketOut.println();
			            socketOut.flush();
	
			            String line = in.readLine();
		            	 try {
		            		if (Integer.parseInt( line ) == 0 ) {
		            			Driver.addmsg("\n\t empty thread (" + this.id + ")");
		            			this.remove();
		            			return;
		            		}
		            		if (Driver.settings.isDebug()) Driver.addmsg("\n\t (debug) comparing " + this.id + "(have " + this.fileCount + ", found " + line + ")");
		 					if ( this.fileCount < Integer.parseInt( line )) {
								Driver.addmsg("\n\t request file grab on " + this.id + " (found " + line + " files, have " + this.fileCount + ") ");
								 this.fileCount = Integer.parseInt( line );
								 grabImage(this.getCount());
							}
		            	 } catch( Exception e ) {
		            		  return;
		            	 }
			        } catch (Exception e){
			        	return;
			        }    
			} else { //brute force				
				try {
	        		String url;
	        		
	        		url = "http://" + Driver.settings.getSite() + "/" + Driver.settings.getBoard() + "/res/" + this + Driver.settingsBrute.tail; //get threads for api        	
	        		if (Driver.settings.isDebug()) Driver.addmsg("\nChecking " + this.id + " @ " + url);
	        		
		        	Socket s = new Socket(Driver.settings.getSite(), 80);
		            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		            PrintWriter socketOut = new PrintWriter(s.getOutputStream());
		            socketOut.println("GET " + url + " HTTP/1.1");
		            socketOut.println("Host: " + Driver.settings.getSite());
		            socketOut.println("User-Agent: Mozilla/5.0 (Windows NT 5.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
		            socketOut.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		            socketOut.println("Connection: close");
		            socketOut.println();
		            socketOut.flush();
	
		            String line = in.readLine();
		            int status = Integer.parseInt(line.substring(9, 12));
		            if (status != 200) Driver.addmsg("\t " + status);
		            if (status == 301) {
		            	Driver.addmsg("\t" + "redirect");
		            }
		            if (status == 404) { 
		            	Driver.addmsg("\n blacklisting bad url: " + url);
		            	Driver.boardOptions.blackList.add(this.id);
		            	remove();
		            	return;
		            }
		            if (status == 403) { 
		            	Driver.addmsg("\t forbidden");
		            	return;
		            }
		            int count = 0;
		            temp.clear();
		            
		            boolean requireTag = false; String requireHappy = "";
		            if (!Driver.settings.threadTag.equals("")) requireTag = true;
		            
		            while (line != null && !line.contains("File Only]")){
		            	if (requireTag && line.contains(Driver.settingsBrute.beforeBody)) {
		            		String marray[] = line.split(Driver.settingsBrute.afterBody);
		            		String marray2[] = marray[0].split(Driver.settingsBrute.beforeBody);
		            		if (marray2[1].contains(Driver.settings.threadTag)) {
		            			requireHappy = marray2[1];
		            		}
		            	}
		            	if (line.contains(Driver.settingsBrute.beforeFile)) {	
		            		String marray[] = line.split(Driver.settingsBrute.beforeFile); //split by images.4chan.org
		            		if (marray.length > 1) {
			            		String marray2[] = marray[1].split(Driver.settingsBrute.afterFile); //split by "
			            		marray2[0] = Driver.settingsBrute.urlPrepend + marray2[0]; 
			            		temp.add(marray2[0]);
			            		count++;
			            		//System.out.println("Found: " + marray2[0]);
		            		}
		            	}
		            	line = in.readLine();
		            }
		            s.close();
		            in.close();
		            socketOut.close();
		            if (requireTag) { //if we require a tag to be found on the page
		            	if (requireHappy.equals("")) {
		            		Driver.addmsg("\n\tTag not found :(");
		            		Driver.boardOptions.removeThread(this);
		            		return;
		            	} else {
		            		if (Driver.boardOptions.tagList == null || !Driver.boardOptions.tagList.contains(this.id)) {
			            		if (Driver.boardOptions.tagList == null) Driver.boardOptions.tagList = new ArrayList<Long>();
		            			Driver.boardOptions.tagList.add(this.id);
			            		Driver.addmsg("\n\t Tag found! \t Found: " + count);
			            		File tagfile = new File(Driver.settings.savepath(this.id, "tag_ " + Driver.settings.threadTag + ".html"));
			            		if (!tagfile.exists()) tagfile.createNewFile();
			            		
			            		FileWriter ryt=new FileWriter(tagfile, true);
			            		BufferedWriter out=new BufferedWriter(ryt);
			            		out.append("<br /><b>" + this.id + "</b>" + requireHappy + "\n");
			            		out.close();
		            		}
		            	}
		            } else {
		            	if (Driver.settings.isDebug()) Driver.addmsg("\t " + count);
		            }
		            if ( this.fileCount < count) {
						Driver.addmsg("\n\t request file grab on " + this.id + " (found " + count + " files, have " + this.fileCount + ") ");
						int j = temp.size();
						for (int i = 0; i < j; i++) {
							Driver.addmsg("\nGetting file " + temp.get(i) + " - " + i + " of " + j);
							downloadImage(temp.get(i));
						}
						this.fileCount = count;
					} else {
						if (Driver.settings.isDebug()) Driver.addmsg("\tof  " + this.fileCount);	
					}
				} catch (Exception e) {
					if (Driver.settings.isDebug()) Driver.infoText.setText(e.toString());
					Driver.state = 0;
				}
				if (Driver.state == 0) Driver.state = 1;
	        }
		}
	
	//if we have less then the amount of files, grab the new one
	public void grabImage(int index) {	
		if (!Driver.settings.api) { return; } //no api so this won't work
		Driver.addmsg("\n\t checking (grab) " + this.getId() + ", index " + index);
		String result = "";
        try {
        	boolean found = false;
        	//get file location
        	String url = "http://" + Driver.settings.getSite() + "/api?api=getfile&id=" + this.id; //get threads for api
        	Socket s = new Socket(Driver.settings.getSocket(), 80);
            BufferedReader in0 = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter socketOut = new PrintWriter(s.getOutputStream());
            socketOut.println("GET " + url);
            socketOut.println();
            socketOut.flush();
            result = in0.readLine();
            while (result != null) {
            	if (result == "0" || result.equals("")) {
            		found = false;
            	} else  if (files == null || !files.contains(result)) { 
                	downloadImage(result);
                	found = true;
                } else {
                	found = true;
                }
            	result = in0.readLine();
            }
            in0.close();
            s.close();
            socketOut.close();
            
        	if (!found) { 
        		this.remove();
        		return;
        	}            
        } catch (Exception e){
        	Driver.addmsg("\nERROR(2) " + e);
        }
	}
	
	//download the grabbed file
	public boolean downloadImage(String url) {
		if (url == "") return false;
		//System.out.println(url + " -> download");
		String[] marray = url.split("/");
		String result = marray[marray.length - 1];
		//System.out.println(result + " -> file name");
		if (url.substring(0,1).equals("/")) { url = "http:" + url; }
		//if (url.substring(url.length() - 3,3) != ".jpg") {
		//	System.out.println(url.substring(url.length()));
		//}
		
		/*
		if (!Driver.settings.api) { //no api
			
    		String marray[] = result.split("src/");
    		if (marray.length == 1) {
    			Driver.addmsg("\tBad link detected: \"" + result + "\"");
    			return false;
    		}
    		result = marray[1];
    		if (!url.contains(Driver.settings.getSite()) && !Driver.settings.getSite().contains("4chan") ) {
    			url = "http://" + Driver.settings.getSite() + url;
    		}
    		
		} */
		if (files.contains(result)) { Driver.addmsg("already have this file - " + result); return false; }
		
      	File file=new File(Driver.settings.savepath(this.id, result));
      	if (file.exists()) {
      		Driver.addmsg("\n already exists (" + result + ")");
      		files.add(result);
      		return true;
      	}
      	
      	Driver.state = 1;
 		Driver.trayWorking(1);
 		if (Driver.settings.isAlert()) {
 			TrayIcon t[] = SystemTray.getSystemTray().getTrayIcons();
 			if (t.length > 0) t[0].displayMessage("Downloading", "Downloading file" + result, TrayIcon.MessageType.INFO);
 			
		}
      	if (!Driver.settings.isOneFolder()) this.addDir(); //ensure directory exists
		try {
			files.add(result); //so we don't dl it twice
			if (url.equals("")) url = "http://" + Driver.settings.getSite() + "/uploads/" + result; //get threads for api
	    	Socket s = new Socket(Driver.settings.getSocket(), 80);
	        InputStream in = s.getInputStream();
	        PrintWriter socketOut = new PrintWriter(s.getOutputStream());
            socketOut.println("GET " + url + " HTTP/1.1");
            socketOut.println("Host: images.4chan.org");
            socketOut.println("Connection: close");
            socketOut.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            socketOut.println("User-Agent: Mozilla/5.0 (Windows NT 5.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
	        socketOut.println();
	        socketOut.flush();
	        OutputStream out = new FileOutputStream(Driver.settings.savepath(this.id, result));

	        byte[] buffer=new byte[1024];
	        int readData;
	        int index = 0;
	        int size = 0;

	        //strip headers
	        byte[] line = new byte[1];
	        in.read(line, 0, 1);	
	        while (line != null) { //strip headers, eoh will have \r\n only (2)
	        	if ((char) line[0] == '\n') { 
	        		if (size == 2) break;
	        		size = 0;
	        	}
	        	size++;
	        	in.read(line, 0, 1);	
	        }
	        while((readData=in.read(buffer))!=-1){
	        	out.write(buffer,0,readData);
	        	buffer=new byte[1024];
	        	if (Driver.msgPointer > 1) Driver.msg[Driver.msgPointer-1] = this.id + ") Downloading " + result + ", " + index/1024 + "kb";
	        	if (Driver.topPanel.isVisible()) Driver.panel.repaint();
	        	index += readData;
	        }
	        Driver.msg[Driver.msgPointer-1] = this.id + ") Downloading " + result + ", done";
	        if (Driver.settings.isDebug()) Driver.addmsg("\tdone");
	        Driver.settings.downloaded += index;
	        SysTray.fileCount.setLabel("size: " + (Driver.settings.downloaded / 1024 / 1024) + "MB");
	        SysTray.threadCount.setLabel("files: " + Driver.boardOptions.getThreadSize());
	        out.close();
	        in.close();
	        s.close();
	        if (Driver.settings.getWallpaper()) {
	        	updateWallpaper(file.getAbsolutePath());
	        }
		} catch (Exception e){
        	Driver.state = 0;
        	if (Driver.settings.isDebug()) Driver.msg[Driver.msgPointer-1] = e.toString();
        	files.remove(result);
        	fileCount--;
        	Driver.trayWorking(0);
        	e.printStackTrace();
        	return false;
        }	
		
		Driver.addmsg("\nNew file in " + this + " (" + result + ")");
		Driver.trayWorking(0);
        return true;
	}
	private void updateWallpaper(String path) {
		//updates the drawpane to the last image
		try {
			Driver.wallpaper_set = System.currentTimeMillis();
			Driver.wallpaper_queue.add(path);
		} catch( Exception e) {
			System.out.println(e);
		}
	}
	public int getCount() {
		return this.fileCount;
	}
	
	public void setCount(int c) {
		this.fileCount = c;
	}
	
}
