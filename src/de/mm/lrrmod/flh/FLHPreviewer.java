package de.mm.lrrmod.flh;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

public class FLHPreviewer {
	
	private JFrame frame;
	private Thread thread;
	private Runnable tWorker;
	private boolean autoDisplay;
	
	private ArrayList<BufferedImage> imgs;
	private int fps;
	private int frameIndex;
	
	public FLHPreviewer(ArrayList<BufferedImage> images) {
		this.imgs = images;
		tWorker = new Runnable() {
			@Override
			public void run() {
				
				while(autoDisplay) {
					
					try {
						Thread.sleep(1000/fps);
						
					} catch(Exception e) {}
					
				}
				
			}
		};
	}
	
	private static class ImagePanel extends JFrame {
		
		private BufferedImage img;
		
		public ImagePanel(BufferedImage img) {
			this.img = img;
		}
		
		public void setImage(BufferedImage img) {
			this.img = img;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if(img != null)
				g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
		}
		
		@Override
		public Dimension getPreferredSize() {
			if(img != null)
				return new Dimension(img.getWidth(), img.getHeight());
			else
				return super.getPreferredSize();
		}
		
	}
	
}
