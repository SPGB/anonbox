import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.TrayIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public interface gui {
	//settings some vars
	TrayIcon tIcon = new TrayIcon(SysTray.getImage(0),
            "Anon Box", SysTray.createPopupMenu());
	TrayIcon tIconWork = new TrayIcon(SysTray.getImage(1),
            "Anon Box (Working)", SysTray.createPopupMenu());	
    //colors
    int intValue = Integer.parseInt( "ededed",16);
    Color aColor = new Color( intValue );
    int intValue2 = Integer.parseInt( "882222",16);
    Color bColor = new Color( intValue2 );
    

    
   // define panels and frame
  JFrame frame = new JFrame();
  JPanel panel = new JPanel() {
  	  /**
		 * the toppanel contains the clouds and has to be redrawn constantly
		 */
		private static final long serialVersionUID = 1L;
		
		{setOpaque(false);}
	  	    public void paintComponent (Graphics g) {
	  	    	Graphics2D g2 = (Graphics2D) g;
	  	    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	  	    	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
	  	    	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	  	    	g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); 
	  	    	g2.setColor(Driver.bColor);
	  	    	g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
	  	    	if (Driver.wallpaper_set != 0 && Driver.settings.getWallpaper()) {
	  	    		g2.drawImage(Driver.wallpaper, 0, 0, panel.getWidth(), panel.getHeight(), this);
	  	    	}
	  	    	if (Driver.wallpaper_set == 0 || Driver.topPanel.isVisible()) {
	  	    		if (Driver.settings.getWallpaper()) g2.drawString(Driver.wallpaper_queue.size() + "", 10, 20);
		  	    	g2.setColor(Driver.aColor);
		  	    	for (int i = 0; i < Driver.msg.length; i++) {
		  	    		if (Driver.msg[i] != null) g2.drawString(Driver.msg[i], 30, 50 + (i * 20));
		  	    	}
	  	    	}
	  	    }
	  	  public void update (Graphics g)
		  	{
		  	   // initialize buffer
		  	   Image offscreen = createImage (this.getSize().width, this.getSize().height);
		  	   Graphics dbg = offscreen.getGraphics ();
				// clear screen in background
		  	    dbg.setColor (getBackground ());
		  	    dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);

		  	    // draw elements in background
		  	    dbg.setColor (getForeground());
		  	    paint (dbg);

		  	    // draw image on the screen
		  	    g.drawImage (offscreen, 0, 0, this);

		  	} 
  };
  JTextArea infoText = new JTextArea();
  JPanel optPanel = new JPanel();
  JTextArea textArea = new JTextArea();
  JScrollPane textScroll = new JScrollPane(textArea);
  JPanel topPanel = new JPanel();
  
  
}
