package de.mm.lrrmod;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import de.mm.lrrmod.flh.FLHModTool;
import de.mm.lrrmod.flh.FLHPreviewer;

public class LRRModToolkit {
	
	private JFrame frame;
	private ArrayList<LRRModTool> tools;
	private JTabbedPane toolTabs;
	
	public LRRModToolkit() {
		tools = new ArrayList<>();
		frame = new JFrame("LRR Modding toolkit");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		toolTabs = new JTabbedPane(JTabbedPane.LEFT);
		frame.add(toolTabs);
		
	}
	
	public LRRModToolkit addTool(FLHModTool tool) {
		if(tool != null) {
			tools.add(tool);
			toolTabs.addTab(tool.getName(), tool);
			frame.repaint();
		}
		return this;
	}
	
	public LRRModToolkit run() {
		
		frame.pack();
		frame.setVisible(true);
		frame.repaint();
		
		return this;
	}
	
	public static void main(String[] args) {
		
		new LRRModToolkit()
			.addTool(new FLHModTool())
		.run();
		
	}
	
}
