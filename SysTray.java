import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.Icon;


public class SysTray {
	public static boolean working = false;
    static MenuItem openDir = new MenuItem("Open Anon Box Folder");
    static MenuItem fileCount = new MenuItem("size: " + Driver.settings.downloaded + "MB");
    static MenuItem threadCount = new MenuItem("files: " + Driver.boardOptions.getThreadSize());
	static Image getImage(int work) throws HeadlessException {
		
		Icon defaultIcon = null;
		switch(work) {
			case 0: defaultIcon = Driver.resources.normal; break;
			case 1: defaultIcon = Driver.resources.normal2; break;
			case 2: defaultIcon = Driver.resources.normal3; break;
		}
	    Image img = new BufferedImage(defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	    defaultIcon.paintIcon(new Panel(), img.getGraphics(), 0, 0);
	        
	    return img;
	}

	
static PopupMenu createPopupMenu() throws  HeadlessException {
        PopupMenu menu = new PopupMenu();
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	System.exit(0);
	        }
        });
        openDir.addActionListener(new ActionListener() {    	
	        public void actionPerformed(ActionEvent e) {
	        	
	        	Driver.addmsg ("\nOpening directory");
	        	File f = new File(Driver.settings.getDirectory());
	        	Desktop desktop = null; 
	        	if (!Desktop.isDesktopSupported()) {
	        		Driver.addmsg ("\nCant open directory");
	        	} else {
		        	try {
		        		 desktop = Desktop.getDesktop();	
		        		desktop.open(f);
		        	} catch (IOException j){ }
	        	}
	        }
        });
        menu.add(openDir);
        menu.add(fileCount);
        menu.add(threadCount);
        menu.add(exit);
return menu;
}


}



