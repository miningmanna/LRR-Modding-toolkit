package de.mm.lrrmod.bmp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import de.mm.lrrmod.LRRModTool;

public class BMPReindexTool extends LRRModTool {
	
	private static final long serialVersionUID = 1957382216525055527L;
	
	private BMPReindexWindow window;
	
	public BMPReindexTool() {
		this.name = "BMPReIndex Tool";
		
		window = new BMPReindexWindow(null);
		
		BoxLayout layoutMgr = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layoutMgr);
		
		setPreferredSize(new Dimension(200, 200));
		
		add(Box.createRigidArea(new Dimension(200, 30)));
		
		JButton openButton = new JButton("Open");
		openButton.setAlignmentX(0.5f);
		add(openButton);
		
		add(Box.createRigidArea(new Dimension(200, 5)));
		
		JButton saveButton = new JButton("Save");
		saveButton.setAlignmentX(0.5f);
		add(saveButton);
		
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int res = chooser.showOpenDialog(BMPReindexTool.this);
				if(res != JFileChooser.APPROVE_OPTION)
					return;
				
				BufferedImage img = null;
				try {
					img = ImageIO.read(chooser.getSelectedFile());
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
				
				window.setImage(img);
				window.setVisible(true);
				
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int res = chooser.showSaveDialog(BMPReindexTool.this);
				if(res != JFileChooser.APPROVE_OPTION)
					return;
				
				File file = chooser.getSelectedFile();
				if(!file.getName().toUpperCase().endsWith(".BMP"))
					file = new File(file.getAbsolutePath() + ".bmp");
				
				try {
					FileOutputStream out = new FileOutputStream(file);
					window.saveRearrangedImage(out);
					out.close();
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
				
			}
		});
		
	}
	
	@Override
	public void destroy() {
		
	}

}
