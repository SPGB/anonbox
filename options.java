import java.awt.Color;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class options {
    public void settings() {
    //settings - OPTIONS
    JButton save = new JButton("Save");
    save.setBackground(Driver.bColor);
    save.setBorderPainted(false);
    save.setFocusPainted(false);
    save.setForeground(Driver.aColor);
    
    final JButton clear = new JButton("Clear");
    clear.setBackground(Driver.bColor);
    clear.setBorderPainted(false);
    clear.setFocusPainted(false);
    clear.setForeground(Driver.aColor);
    clear.setMnemonic('C');
    
    final JButton xml = new JButton("Generate override xml");
    xml.setBackground(Driver.bColor);
    xml.setBorderPainted(false);
    xml.setFocusPainted(false);
    xml.setForeground(Driver.aColor);
    xml.setMnemonic('x');
    
    //add some top buttons, users love those
    final JButton opt = new JButton("Options");
    opt.setBackground(Driver.bColor);
    opt.setBorderPainted(false);
    opt.setFocusPainted(false);
    opt.setForeground(Driver.aColor);
    opt.setMnemonic('O');
    
   
    final JButton exit = new JButton("Exit");
    exit.setBackground(Driver.bColor);
    exit.setBorderPainted(false);
    exit.setFocusPainted(false);
    exit.setForeground(Driver.aColor);
    exit.setMnemonic('O');
    
    
    Driver.optPanel.setPreferredSize(new Dimension(250, 400));
    final JCheckBox windowMode = new JCheckBox("Full Screen" , false);
    windowMode.setBackground(Driver.bColor);
    windowMode.setForeground(Color.white);
    
    final JCheckBox debugMode = new JCheckBox("Debug Mode" , false);
    debugMode.setBackground(Driver.bColor);
    debugMode.setForeground(Color.white);
    
    final JCheckBox oneFolder = new JCheckBox("One Folder" , false);
    oneFolder.setBackground(Driver.bColor);
    oneFolder.setForeground(Color.white);
    
    final JCheckBox alert = new JCheckBox("Alert on update" , false);
    alert.setBackground(Driver.bColor);
    alert.setForeground(Color.white);
    
    final JCheckBox startup = new JCheckBox("run on boot" , false);
    startup.setBackground(Driver.bColor);
    startup.setForeground(Color.white);
    
    final JCheckBox wallpaper = new JCheckBox("wallpaper mode" , false);
    wallpaper.setBackground(Driver.bColor);
    wallpaper.setForeground(Color.white);
    
    final JTextField board  = new JTextField("Board");
    final JLabel labelBoard = new JLabel("Board ID:");
    labelBoard.setForeground(Color.white);
    
    board.setColumns(10);
    board.setToolTipText("Board");
    board.setBounds(0, 0, 100, 20);
    
    final JLabel labelDir = new JLabel("directory path:");
    labelDir.setForeground(Color.white);
    
    final JTextField dir = new JTextField();
    dir.setToolTipText("Directory");
    dir.setColumns(12);
    
    final JLabel labelTag = new JLabel("Only threads containing:");
    labelTag.setForeground(Color.white);
    
    final JTextField tag = new JTextField();
    tag.setToolTipText("Onlt the threads containing this word");
    tag.setColumns(15);
    
    final JLabel labelSite = new JLabel("site: ");
    labelSite.setForeground(Color.white);
    
    final JTextField site = new JTextField();
    site.setToolTipText("Site");
    site.setColumns(15);
    
    final JLabel labelThreads = new JLabel("Maximum threads: ");
    labelThreads.setForeground(Color.white);
    
    final JTextField threads = new JTextField();
    threads.setToolTipText("Max Threads");
    threads.setColumns(5);
    
    
    //save settings
    save.addActionListener(new
	         ActionListener()
	         {
	            public void actionPerformed(ActionEvent event)
	            {	

	            	//Driver.panel.setVisible(false);
	            	Driver.optPanel.setVisible(false);
	            	
	            	Driver.state = 1;
	            	Driver.textArea.setText("Saved Settings");
	            	Driver.clearmsg();
	            	
	            	if (Driver.viewing == 4) { Driver.addmsg("\n You are now ready to go! minimize this window if you want and we will keep working from the tray.ß"); } 
	            	Driver.viewing = 1;
	            	
	            	if (windowMode.isSelected()) {
	            		Driver.fullscreen();
	            	} else {
	            		Driver.frame.setSize(700, 500); 
	            	}
	            	Driver.settings.setScreen(windowMode.isSelected());

	            	if (debugMode.isSelected() != Driver.settings.isDebug()) {
	            		Driver.addmsg("\nSwitching debug mode.");
	            		Driver.settings.setDebug(debugMode.isSelected());
	            	}
	            	
	            	if (wallpaper.isSelected() != Driver.settings.getWallpaper()) {
	            		Driver.addmsg("\nToggling wallpaper mode");
	            		Driver.settings.setWallpaper(wallpaper.isSelected());
	            	}
	            	if (startup.isSelected() != Driver.settings.getStartup()) {
	            		Driver.addmsg("\nToggling startup");
	            		Driver.settings.setStartup(startup.isSelected());
	            	}
	            	
	            	if (oneFolder.isSelected() != Driver.settings.isOneFolder()) {
	            		Driver.addmsg("\nSwitching one folder mode.");
	            		Driver.boardOptions.clear();
	            		Driver.settings.setOneFolder(oneFolder.isSelected());
	            	}
	            	
	            	if (alert.isSelected() != Driver.settings.isAlert()) {
	            		Driver.addmsg("\nSwitching alert mode!");
	            		Driver.settings.setAlert(alert.isSelected());
	            	}
	            	
	            	if (!site.getText().equals(Driver.settings.getSite())) {
	            		Driver.addmsg("\n- updated site");
	            		Driver.boardOptions.clear();	
	            		Driver.settings.setSite(site.getText().trim());
	            		Driver.settings.verify();
	            		Driver.settingsBrute.readXML();
	            	}
	            	
	            	if (!board.getText().equals(Integer.toString(Driver.settings.getBoardId()))) {
	            		Driver.addmsg("\n- updated board (" + Driver.settings.getBoard() + ") => (" + board.getText() +  ")");
	  	            	Driver.settings.setBoard(board.getText().trim());
	            	}
	            	
	            	Driver.settings.setDirectory(dir.getText());  	  
	            	Driver.settings.threadTag = tag.getText();
	            	Driver.settings.setMaxThreads(Integer.parseInt(threads.getText()));
	            	Driver.settings.serial(); //serialize data
	            }
	         });
    
    /*clear
     * viewing: -
     */
    clear.addActionListener(new
	         ActionListener()
	         {
	            public void actionPerformed(ActionEvent event)
	            {	
	            	Driver.boardOptions.clear();
	    }
	});
    
    /*clear
     * viewing: -
     */
    xml.addActionListener(new
	         ActionListener()
	         {
	            public void actionPerformed(ActionEvent event)
	            {	
	            	Driver.addmsg("\nGenerated overriding xml, any settings changed here will be what we use for parsing" + 
	            			"\nUse this to add support for new *chans. :)");
	            	Driver.settingsBrute.addXML();
	    }
	});

	
	
	
    /*options
     * viewing: 3
     */
    opt.addActionListener(new
	         ActionListener()
	         {
	            public void actionPerformed(ActionEvent event)
	            {
	            	if (Driver.viewing != 3) {
	            		Driver.clearmsg();
	            		int size = Driver.settings.downloaded; 
	            		String size_unit = "KB";
	            		if (size > 1024) { size = size / 1024; size_unit = "MB"; }
	            		if (size > 1024) { size = size / 1024; size_unit = "GB"; }
	            		Driver.textArea.setText("\nDownloaded: " + size + size_unit
	            				+ "\nUploaded: " + Driver.settings.uploaded/1024/1024 + "mb" 
	            				+ "\nThreads: " + Driver.boardOptions.getThreadSize()
	            				+ "\nSupported sites: \n\t boards.4chan.org \n\t darkchan.com \n\t 7chan.org \n\t boards.420chan.org");		
	            		Driver.viewing = 3;
	            		
	            		//booleans
	            		windowMode.setSelected(Driver.settings.fullscreen());
		            	debugMode.setSelected(Driver.settings.isDebug());
		            	oneFolder.setSelected(Driver.settings.isOneFolder());
		            	alert.setSelected(Driver.settings.isAlert());
		            	startup.setSelected(Driver.settings.getStartup());
		            	wallpaper.setSelected(Driver.settings.getWallpaper());
		            	if (Driver.settings.api) {
		            		board.setText("" + Driver.settings.getBoardId());
		            	} else {
		            		board.setText("" + Driver.settings.getBoard());	
		            	}
		            	dir.setText(Driver.settings.getDirectory());
		            	site.setText(Driver.settings.getSite());
		            	threads.setText(Integer.toString(Driver.settings.getMaxThreads()));
		            	//Driver.panel.setVisible(false);
		            	Driver.optPanel.setVisible(true); 	
	            	} else {
	            		Driver.viewing = 1;
	            		//Driver.panel.setVisible(false);
	            		Driver.optPanel.setVisible(false); 	
	            		//Driver.textArea.setText("Anon Box (v. " + Driver.version + ")");
	            		Driver.clearmsg();
	            	}
	            }
	         });
    
    //exit button
    exit.addActionListener(new
  	         ActionListener()
  	         {
  	            public void actionPerformed(ActionEvent event)
  	            {
  	            	Driver.settings.serial();
  	            	Driver.boardOptions.serial();
  	                System.exit(0); //calling the method is a must
  	            }
  	         });

    Driver.optPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    
    c.gridx = 0; c.gridy = 0;
    Driver.optPanel.add(labelBoard, c);
    c.gridx = 1; c.gridy = 0; 
    Driver.optPanel.add(board, c); //for current board
    
    c.gridx = 0; c.gridy = 1;
    Driver.optPanel.add(labelDir, c);
    c.gridx = 1; c.gridy = 1;
    Driver.optPanel.add(dir, c); //for directory
   
    c.gridx = 0; c.gridy = 2;
    Driver.optPanel.add(labelSite, c);
    c.gridx = 1; c.gridy = 2;
    Driver.optPanel.add(site, c);
    
    c.gridx = 0; c.gridy = 3;
    Driver.optPanel.add(labelThreads, c);
    c.gridx = 1; c.gridy = 3;
    Driver.optPanel.add(threads, c);
    
    c.gridx = 0; c.gridy = 4;
    Driver.optPanel.add(labelTag, c);
    c.gridx = 1; c.gridy = 4;
    Driver.optPanel.add(tag, c);
    
    //booleans
    c.gridx = 0; c.gridy = 5;
    Driver.optPanel.add(windowMode, c); //for windowed mode
    c.gridx = 1; c.gridy = 5;
    Driver.optPanel.add(debugMode, c);
    c.gridx = 0; c.gridy = 6;
    Driver.optPanel.add(oneFolder, c);
    c.gridx = 1; c.gridy = 6;
    Driver.optPanel.add(alert, c);
    c.gridx = 0; c.gridy = 7;
    Driver.optPanel.add(startup, c);
    c.gridx = 1; c.gridy = 7;
    Driver.optPanel.add(wallpaper, c);
    
    //buttons
    c.gridx = 0; c.gridy = 8;
    Driver.optPanel.add(clear, c); //for directory
    c.gridx = 1; c.gridy = 8;
    Driver.optPanel.add(save, c);
    
    c.gridx = 0; c.gridy = 9;
    c.gridwidth = 2;
    Driver.optPanel.add(xml, c); //for directory
    
    Driver.topPanel.add(opt);
    Driver.topPanel.add(exit);
    }
}
