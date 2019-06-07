package de.mm.lrrmod;

import javax.swing.JPanel;

public abstract class LRRModTool extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String name;
	
	public LRRModTool() {
		super();
		name = "";
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void destroy();
	
}
