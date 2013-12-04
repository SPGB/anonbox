import java.awt.Toolkit;
import java.io.InputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;


public class resor {
	//relative pathing
	public resor() {
		
	}
	Icon normal = new ImageIcon((Toolkit.getDefaultToolkit().getImage(getClass().getResource("/bulb.gif"))));
	Icon normal2 = new ImageIcon((Toolkit.getDefaultToolkit().getImage(getClass().getResource("/bulbDownload.png"))));
	Icon normal3 = new ImageIcon((Toolkit.getDefaultToolkit().getImage(getClass().getResource("/bulbWork.png"))));
	InputStream xml = getClass().getResourceAsStream("default.xml");
}
