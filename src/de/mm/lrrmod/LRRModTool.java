package de.mm.lrrmod;

import javax.swing.JPanel;

public abstract class LRRModTool extends JPanel {
	
	protected String name;
	
	public LRRModTool() {
		super();
		name = "";
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void hide();
	public abstract void show();
	
}
