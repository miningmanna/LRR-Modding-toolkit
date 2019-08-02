package de.mm.lrrmod.bmp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BMPReindexWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4716821876573307841L;
	
	private static final int DEF_CELLSIZE = 20;
	
	int sel1, sel2;
	int cellSize;
	byte[] buffer;
	Color[] palette;
	JPanel drawPanel;
	
	public BMPReindexWindow(BufferedImage img, int cellSize) {
		super("BMP reindex");
		palette = new Color[256];
		
		this.cellSize = cellSize > 0 ? cellSize : DEF_CELLSIZE;
		
		setImage(img);
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		drawPanel = new JPanel() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -8882607585788021341L;
			
			@Override
			public void paint(Graphics _g) {
				Graphics2D g = (Graphics2D) _g;
				Dimension size = getSize();
				
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, size.width, size.height);
				
				for(int i = 0; i < 256; i++) {
					int x = i%16;
					int y = Math.floorDiv(i, 16);
					
					if(palette[i] != null)
						g.setColor(palette[i]);
					else
						g.setColor(Color.BLACK);
					
					g.fillRect(1+x*(cellSize+1), 1+y*(cellSize+1), cellSize, cellSize);
					
					if(i == sel1) {
						g.setColor(Color.CYAN);
						g.drawLine(x*(cellSize+1), y*(cellSize+1), x*(cellSize+1)+(cellSize/2), y*(cellSize+1));
						g.drawLine(x*(cellSize+1), y*(cellSize+1), x*(cellSize+1), y*(cellSize+1)+(cellSize/2));
						g.drawLine((x+1)*(cellSize+1), (y+1)*(cellSize+1), (x+1)*(cellSize+1)-(cellSize/2), (y+1)*(cellSize+1));
						g.drawLine((x+1)*(cellSize+1), (y+1)*(cellSize+1), (x+1)*(cellSize+1), (y+1)*(cellSize+1)-(cellSize/2));
					}
					if(i == sel2) {
						g.setColor(Color.MAGENTA);
						g.drawLine((x+1)*(cellSize+1), y*(cellSize+1), (x+1)*(cellSize+1)-(cellSize/2), y*(cellSize+1));
						g.drawLine((x+1)*(cellSize+1), y*(cellSize+1), (x+1)*(cellSize+1), y*(cellSize+1)+(cellSize/2));
						g.drawLine((x)*(cellSize+1), (y+1)*(cellSize+1), (x)*(cellSize+1)+(cellSize/2), (y+1)*(cellSize+1));
						g.drawLine((x)*(cellSize+1), (y+1)*(cellSize+1), (x)*(cellSize+1), (y+1)*(cellSize+1)-(cellSize/2));
					}
				}
				
			}
			
		};
		
		drawPanel.setPreferredSize(new Dimension((cellSize+1)*16+1,(cellSize+1)*16+1));
		
		drawPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				switch (e.getButton()) {
				case 1:
					sel1 = getIndex(e.getX(), e.getY());
					break;
				case 3:
					sel2 = getIndex(e.getX(), e.getY());
					break;
				case 2:
					if(buffer != null)
						switchColors(sel1, sel2);
				default:
					break;
				}
				BMPReindexWindow.this.repaint();
			}
		});
		add(drawPanel);
		pack();
		
	}
	
	private void switchColors(int i1, int i2) {
		
		Color ctemp = palette[i1];
		palette[i1] = palette[i2];
		palette[i2] = ctemp;
		
		int imgOff = 54;
		byte[] colorTemp = new byte[4];
		System.arraycopy(buffer, imgOff+i1*4, colorTemp, 0, 4);
		System.arraycopy(buffer, imgOff+i2*4, buffer, imgOff+i1*4, 4);
		System.arraycopy(colorTemp, 0, buffer, imgOff+i2*4, 4);
		
		for(int i = 0x0436; i < buffer.length; i++) {
			if((buffer[i] & 0x00FF) == i1)
				buffer[i] = (byte) i2;
			else if((buffer[i] & 0x00FF) == i2)
				buffer[i] = (byte) i1;
		}
		
	}
		
	private int getIndex(int x, int y) {
		System.out.println("Converting: " + x + ", " + y);
		return clamp(Math.floorDiv(y, (cellSize+1)), 15)*16+clamp(Math.floorDiv(x, (cellSize+1)), 15);
	}
	
	private int clamp(int val, int max) {
		if(val > max)
			return max;
		return val;
	}
	
	public BMPReindexWindow(BufferedImage img) {
		this(img, DEF_CELLSIZE);
	}
	
	public void saveRearrangedImage(OutputStream out) throws IOException {
		out.write(buffer);
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
	public void setImage(BufferedImage img) {
		if(img == null)
			return;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "bmp", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer = out.toByteArray();
		
		// Fix palette offset
		
		try {
			ByteArrayOutputStream newBuffOut = new ByteArrayOutputStream();
			int oldOff = getIntLE(buffer, 10);
			buffer[10] = 0x36;
			buffer[11] = 0x04;
			buffer[12] = 0;
			buffer[13] = 0;
			newBuffOut.write(buffer, 0, oldOff); // Write all data till pixel data
			byte[] empty = new byte[0x0436-oldOff];
			newBuffOut.write(empty);
			newBuffOut.write(buffer, oldOff, buffer.length-oldOff);
			newBuffOut.close();
			buffer = newBuffOut.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Failed to fix pixel data offset");
			return;
		}
		
		int imgOff = 54;
		for(int i = 0; i < 256; i++) {
			int r = 0x00FF & buffer[imgOff+i*4+2];
			int g = 0x00FF & buffer[imgOff+i*4+1];
			int b = 0x00FF & buffer[imgOff+i*4+0];
			palette[i] = new Color(r, g, b);
		}
	}
	
}
