import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class BruteSettings {
	String threadIdentifier;
	String lookfor;
	String urlAppend;
	String tail;
	
	String beforeBody;
	String afterBody;
	String beforeFile;
	String afterFile;
	String urlPrepend;
	public BruteSettings () {
		
	}

	public void readXML() {
	        XPath xpath = XPathFactory.newInstance().newXPath();
	        Document doc;
			try {
				File f = new File(Driver.settings.getDirectory() + "\\sites.xml");
				String site = "";
				doc = null;
				if (f.exists()) {
					doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Driver.settings.getDirectory() + "\\sites.xml");
					if (doc != null)
						site = xpath.evaluate("//SITE" + Driver.settings.getSite(), doc);
				}
				if (!site.equals("")) { //if site is not empty
					//thread
					this.threadIdentifier = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//thread/threadidentifier/text()", doc);
					this.lookfor = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//thread/needle/text()", doc);
					this.tail = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//thread/tail/text()", doc);
					this.urlAppend = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//thread/urlappend/text()", doc);
					this.beforeBody = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//thread/beforeBody/text()", doc);
					this.afterBody = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//thread/afterBody/text()", doc);
					
					//file
					this.beforeFile = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//file/beforefile/text()", doc);
					this.afterFile = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//file/afterfile/text()", doc);
					this.urlPrepend = xpath.evaluate("//sites//SITE" + Driver.settings.getSite() + "//file/urlprepend/text()", doc);
				} else {
					Driver.addmsg("\nXML for sites -> SITES" + Driver.settings.getSite() + " not found at " + Driver.settings.getDirectory() + "\\sites.xml");
					if (Driver.settings.getSite().equals("7chan.org")) {
						this.threadIdentifier = "res/";
						this.lookfor = "Reply</a>";
						this.tail = ".html";
						this.urlAppend = ".html";
						
						this.beforeBody = "<p class=\"message\">";
						this.afterBody = "</p>";
						
				    	this.beforeFile = "target=\"_blank\" href=\"";
				    	this.afterFile = "\"";
				    	this.urlPrepend = "";
					} else if (Driver.settings.getSite().equals("boards.420chan.org")) {
						this.threadIdentifier = "res/";
						this.lookfor = ".php\"> ";
						this.tail = ".php";
						this.urlAppend = ".php";
				    	
						this.beforeBody = "<blockquote";
						this.afterBody = "</blockquote>";
						
				    	this.beforeFile = "href=\"/" + Driver.settings.getBoard() + "/src/";
				    	this.afterFile = "\"";
				    	this.urlPrepend = "http://boards.420chan.org/" + Driver.settings.getBoard() + "/src/";
					} else { //4chan
						this.threadIdentifier = "res/";
						this.lookfor = "Reply</a>";
						this.tail = "";
						this.urlAppend = "";
						
						this.beforeBody = "<blockquote>";
						this.afterBody = "</blockquote>";
						
				    	this.beforeFile = "images.4chan.org";
				    	this.afterFile = "\"";
				    	this.urlPrepend = "http://images.4chan.org";
					} 
				}
			} catch (Exception e) {
				Driver.addmsg("\n\t X " + e.toString());
			}
	        
	}

	//give the thread a home
	public void addXML () {
		File f = new File(Driver.settings.getDirectory() + "\\sites.xml");
		try{
			if(!f.exists() && f.createNewFile())
				Driver.addmsg("\n\t + sites.xml");
			    InputStream source = Driver.resources.xml;
			    OutputStream destination = new FileOutputStream(f);
			    try {
			    	int read = 0;
			    	byte[] bytes = new byte[1024];
			     
			    	while ((read = source.read(bytes)) != -1) {
			    		destination.write(bytes, 0, read);
			    	} 
			    }
			    catch(Exception e) {
			    	e.printStackTrace();
			    } finally {
			    	source.close();
			        destination.close();
			    }

		}catch(Exception e){
			Driver.addmsg("\n\t X sites.xml");
		} 
	}
}
 