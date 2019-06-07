package de.mm.lrrmod;

import java.lang.reflect.Constructor;

public class LRRModToolkit {
	
	
	public LRRModToolkit() {
		// TODO Auto-generated constructor stub
	}
	
	public LRRModToolkit(int asd) {
		
	}
	
	public static void main(String[] args) {
		
		Class<LRRModToolkit> cls = LRRModToolkit.class;
		
		Constructor<?>[] constructors = cls.getConstructors();
		
		for(Constructor<?> c : constructors) {
			
			System.out.println(c.getParameterCount());
			
		}
		
	}
	
}
