import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class Boards implements Serializable {
	   /**
	 * Boards keeps our current board loaded and keeps track of our thread list
	 */
		private static final long serialVersionUID = 1L;
		
		ArrayList<Thread> threadList = new ArrayList<Thread>(); //our threads
		ArrayList<Long> blackList = new ArrayList<Long>(); //threads we deleted/remove
		
		ArrayList<Long> tagList = new ArrayList<Long>(); //threads we deleted/remove
		private String bruteForceUrl;
		private int last_page_indexed = 0;
		public String printList() {
			return threadList.toString();
		}
		public String toString() {
			return threadList.size() + " threads";
		}
		public int getThreadSize() {
			if (threadList == null) return 1;
			return threadList.size();
		}

		 //removes all of our threads
		public void clear() {
			Driver.addmsg("\nClearing " + threadList.size() + " threads");
			this.bruteForceUrl = null;
			this.tagList = null;
			int j = threadList.size();
			for(int i = 0; i < j; i++) {
				threadList.get(0).remove();
			}
			threadList.clear();
			blackList.clear();
			Driver.boardOptions.serial();
			Driver.msg = new String[20];
			Driver.msgPointer = 0;
			Driver.wallpaper_queue.clear();
		}
		
		//remove one thread
		public void removeThread(Thread t) {
			if (blackList == null) { blackList =  new ArrayList<Long>(); }
			blackList.add(t.getId());
			threadList.remove(t);
		}
		
		//creates a new thread and adds it to our list
		public boolean addThread(long thread) { 
			boolean contains = false;
			if (blackList == null) { blackList =  new ArrayList<Long>(); }
			if (blackList.contains(thread)) { if (Driver.settings.isDebug()) Driver.addmsg("\n\t(debug) rejecting blacklisted thread " + thread); return false; }
			for(Thread i: threadList) {
				if (i.getId() == thread) {
					contains = true;
				}
			}
			if (!contains) { //add new thread
				Thread t = new Thread(thread);
				threadList.add(t);
				if (Driver.settings.isAlert()) {
					Driver.tIcon.displayMessage("New Thread", Driver.settings.getSite() + "/" + thread, TrayIcon.MessageType.INFO);
				}
				if (!Driver.settings.isOneFolder()) t.addDir();
				t.updateCount();
				for (int i = 1; i < t.getCount(); i++) {
					t.grabImage(i);
				}
				if (Driver.settings.getMaxThreads() != 0 && threadList.size() > Driver.settings.getMaxThreads()) {
					Driver.addmsg("\nToo many threads, removing " + threadList.get(0));
					threadList.get(0).remove();
				}
				return true;
			} else { //check existing
				for (int i = 0; i < threadList.size(); i++) {
					threadList.get(i).updateCount();
				}
			}
			return false;
		}
		
		//get a list of current threads on the server-side board
	   public void list()
	 	{	   
		   String line = ""; 
	        try {
	        	if (Driver.settings.api) {
		        	String url = "http://" + Driver.settings.getSite() + "/api?api=listBoard&short&b=" + Driver.settings.getBoardId(); //get threads for api        	
		            Socket s = new Socket(Driver.settings.getSocket(), 80);
		            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		            PrintWriter socketOut = new PrintWriter(s.getOutputStream());
		            socketOut.println("GET " + url);
		            socketOut.println();
		            socketOut.flush();

		            boolean result = false;
		            
		            while ((line = in.readLine()) != null){
		            	 try
		            	   {
		            		 if (line.equals("false")) { Driver.addmsg("\n\tCurrently empty board"); return;} 
		            		 Driver.addmsg("\n\tchecking " + Integer.parseInt( line ));
		            		 if (this.addThread(Integer.parseInt( line ))) {
		            			Driver.state = 1;
		            			result = true; 
		            		 }
		            	   }
		            	   catch( Exception e )
		            	   {
		            		  Driver.state = 0;
		            		  if (Driver.settings.isDebug()) Driver.infoText.setText(e.toString());
		            	   }	
		            }
		            if (!result) this.verify();
		            return;
	        	}
	        	String url;
	        	last_page_indexed++;
	        	if (last_page_indexed > 10) last_page_indexed = 0;
	        	if (bruteForceUrl == null) {
	        		url = "http://" + Driver.settings.getSite() + "/" + Driver.settings.getBoard() + "/" + last_page_indexed + Driver.settingsBrute.urlAppend;
	        	} else {
	        		url = bruteForceUrl + last_page_indexed;
	        	}    	
	        	Driver.addmsg("\nIndexing page " + last_page_indexed + " @ " + url);
	        	BufferedReader in = null;
	        	Socket s = null;
	        	PrintWriter socketOut  = null;;
	        	try {
			        	s = new Socket(Driver.settings.getSite(), 80);
			            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			            socketOut = new PrintWriter(s.getOutputStream());
			            socketOut.println("GET " + url + " HTTP/1.1");
			            socketOut.println("Host: " + Driver.settings.getSite());
			            socketOut.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			            socketOut.println("Connection: keep-alive");
			            socketOut.println("User-Agent: Mozilla/5.0 (Windows NT 5.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1");
			            socketOut.println();
			            socketOut.flush();
	        	} catch (Exception e) {
	        			Driver.addmsg("\t socket error while scraping: " + e.getLocalizedMessage());
	        			e.printStackTrace();
	        			return;
	        	}
		        line = in.readLine();
		        int status = Integer.parseInt(line.substring(9, 12));
		        Driver.addmsg("\t" + status);
		            
		        if (status == 404) return; //cant find
		        if (status == 403) { Driver.addmsg(" -> x\n error messgae: forbidden" + url); return; } //cant find
		        ArrayList<Long> temp = new ArrayList<Long>(); //for no api
		        int count = 0;

		        while (line != null){
		        	if (status == 302) {
		            			Driver.addmsg(" -> x\n please ensure this is correct: " + url);
		            			break;
		            }
			        if (status == 301) {
			        	if (line.contains("Location:")) {
			        		String[] location = line.split("Location:"); //sift out the board name
				            Driver.addmsg(" -> " + location[1]);
				            bruteForceUrl = location[1];
				           return;
			           }
			        }
			        else if (line.contains(Driver.settingsBrute.lookfor)){
			            	String[] marray = line.split(Driver.settingsBrute.lookfor);
			            	int thread = 0;
			            	if (marray[0] != null) {
			            		String[] marray2 = marray[0].split(Driver.settingsBrute.threadIdentifier);
			            		if (marray2.length > 1) {
			            			count++;
			            			if (count > 10) break; //cap of 10 threads to add at a time
				            			if (marray2[1].contains("#")) {
				            				String[] marray3 = marray2[1].split("#");
				            				try {
				            					long temp_number = Long.parseLong(marray3[0].replaceAll( "[^\\d]", "" ));
					            				temp.add(temp_number);
				            				} catch (Exception e) {
				            					if (Driver.settings.isDebug()) {
				            						Driver.addmsg("\n(debug) parse error, " + e.getMessage());
				            						e.printStackTrace();
				            					}
				            				}
				            			} else if (marray2[1].contains(".")) {
				            				String[] marray3 = marray2[1].split("[.]");
				            				temp.add(Long.parseLong(marray3[0].replaceAll( "[^\\d]", "" )));
				            			} else {
					            			try {
					            				long temp_number = Long.parseLong(marray2[1].replaceAll( "[^\\d]", "" ));
					            				temp.add(temp_number);
					            			} catch (Exception e) {
					            				Driver.addmsg("\n(debug) parse int error for " + marray2[1]);
					            			}
				            			}
			            		}
			            	}
			            	if (thread != 0) {
			            		Driver.addmsg("\n(debug) found thread id " + thread);
			            	}
		            	}
		            	line = in.readLine();
		            }

		            s.close();
		            in.close();
		            socketOut.close();
		            
		            int j = temp.size();
		            Driver.addmsg("\n\tIndexing returns " + j + " threads");
		            for (int i = 0; i < j; i++) {
		            	if (Driver.viewing > 2) {
		            		Driver.addmsg("\n Stopping indexing");
		            		break;		            		
		            	}
		            	this.addThread(temp.get(i));
		            }
	        } catch (Exception e){
	        	e.printStackTrace();
	        	Driver.state = 0; //indicates connection problem
	        	if (Driver.settings.isDebug()) Driver.infoText.setText(e.toString());
	        }     
	   }
	   
	   //check the integrity of our threads and their children (files)
	   public void verify() {
		   if (Driver.settings.isDebug()) Driver.addmsg("\nVerifying");
		   int count = 0;
		   boolean changes = false;
		   for(Thread i: threadList) {
			   if (i.validate()) changes = true;
			   count += i.getCount();
			}
		   	if (changes) {
		   		Driver.state = 1;
	   		} else {
	   			Driver.state = 2;
	   		}
		   if (Driver.settings.isDebug()) Driver.addmsg("\nVerify complete (" + count + " files)");
	   }
	   
	   //save our board
	   public void serial() 
	   {
	 	  try { //boards
	           FileOutputStream outStream =  new FileOutputStream("last.dat");
	           ObjectOutputStream objectOutputFile = new ObjectOutputStream(outStream);
	           objectOutputFile.writeObject(this);
	           objectOutputFile.close();
	 	  } catch (IOException e) {
	         	  e.printStackTrace();
	   	  }
	 	  Driver.addmsg("\nSaved!");
	   }
}
