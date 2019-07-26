package de.mm.lrrmod.flh;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.Thread.State;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class FLHPreviewer {
	
	private JFrame frame;
	private ImagePanel imgPanel;
	private OnRedrawListener onRedraw;
	
	private Thread thread;
	private Runnable tWorker;
	private boolean autoDisplay;
	
	private ArrayList<BufferedImage> imgs;
	private int fps;
	private int frameIndex;
	
	public FLHPreviewer(ArrayList<BufferedImage> images) {
		this.imgs = images;
		this.fps = 25;
		tWorker = new Runnable() {
			@Override
			public void run() {
				try {
					while(autoDisplay) {
						
						Thread.sleep(1000/fps);
						
						if(imgs != null) {
							frameIndex++;
							frameIndex %= imgs.size();
							imgPanel.setImage(imgs.get(frameIndex));
							
							if(onRedraw != null)
								onRedraw.onRedraw(frameIndex);
						} else {
							frameIndex = 0;
						}
						
						frame.repaint();
						
					}
				} catch(Exception e) {}
			}
		};
		
		BufferedImage img = null;
		if(imgs != null)
			if(imgs.size() > 0)
				img = imgs.get(0);
		
		imgPanel = new ImagePanel(img);
		frame = new JFrame("Preview");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.add(imgPanel);
		frame.pack();
		
	}
	
	public void setFrame(int frameIndex) {
		
		stop();
		
		if(imgs == null)
			return;
		
		if(frameIndex < 0)
			this.frameIndex = 0;
		else if(frameIndex >= imgs.size())
			this.frameIndex = imgs.size()-1;
		else
			this.frameIndex = frameIndex;
		
		imgPanel.setImage(imgs.get(frameIndex));
		if(onRedraw != null)
			onRedraw.onRedraw(frameIndex);
		frame.repaint();
		
	}
	
	public int getFrame() {
		return frameIndex;
	}
	
	public void setFPS(int fps) {
		if(fps < 0)
			return;
		else if(fps == 0)
			stop();
		else
		{
			this.fps = fps;
			start();
		}
	}
	
	public void start() {
		
		if(thread == null || thread.getState() != State.NEW) {
			if(thread != null)
				thread.interrupt();
			thread = new Thread(tWorker);
			thread.setDaemon(true);
		}
		
		autoDisplay = true;
		thread.start();
	}
	
	public void stop() {
		autoDisplay = false;
	}
	
	public int getFrameCount() {
		if(imgs != null)
			return imgs.size();
		return 0;
	}

	public void destroy() {
		try {
			stop();
			thread.interrupt();
			frame.dispose();
		} catch(Exception e) {}
		frame.dispose();
	}
	
	public void setOnRedrawListener(OnRedrawListener listener) {
		this.onRedraw = listener;
	}
	
	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}
	
	public boolean isRunning() {
		return autoDisplay;
	}

	public void setImages(ArrayList<BufferedImage> frames) {
		this.imgs = frames;
		BufferedImage img = null;
		if(imgs != null)
			if(imgs.size() > 0)
				img = imgs.get(0);
		
		imgPanel.setImage(img);
		frame.pack();
	}

	private static class ImagePanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7029869425107664585L;
		
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
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, getSize().width, getSize().height);
			if(img != null) {
				float imgAspect = (float) img.getHeight()/img.getWidth();
				float panelAspect = getSize().height / getSize().width;
				
				int width = (int) (imgAspect >= panelAspect
						? getSize().height/imgAspect
						: getSize().width);
				int height = (int) (imgAspect < panelAspect
						? imgAspect*getSize().width
						: getSize().height);
				
				int x = imgAspect >= panelAspect
						? (getSize().width-width)/2
						: 0;
				int y = imgAspect < panelAspect
						? (getSize().height-height)/2
						: 0;
				
				g.drawImage(img, x, y, width, height, null);
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			if(img != null)
				return new Dimension(img.getWidth(), img.getHeight());
			else
				return super.getPreferredSize();
		}
		
	}
	
	public static interface OnRedrawListener {
		public void onRedraw(int frameNum);
	}
	
}
