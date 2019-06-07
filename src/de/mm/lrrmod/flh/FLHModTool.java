package de.mm.lrrmod.flh;

import javax.swing.JButton;

import de.mm.lrrmod.LRRModTool;

public class FLHModTool extends LRRModTool {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7537319353930785086L;
	
	public FLHModTool() {
		name = "FLH Tool";
		
		add(new JButton("SUCK MY ASS"));
	}
	
	@Override
	public void destroy() {
		// TODO: make sure FLHPreviwer is properly disposed.
	}
	
}
