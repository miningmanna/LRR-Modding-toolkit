package de.mm.lrrmod;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import de.mm.lrrmod.bmp.BMPReindexTool;
import de.mm.lrrmod.flh.FLHModTool;

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
	
	public LRRModToolkit addTool(LRRModTool tool) {
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
			.addTool(new BMPReindexTool())
		.run();
		
	}
	
}
