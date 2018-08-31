package ddejonge.ggp.tools;

import java.util.Random;

public class StaticRandom {
	
	public static Random random = new Random();
	
	public static int nextInt() {
		return random.nextInt();
	}
	
	public static int nextInt(int n){
		return random.nextInt(n);
	}
	
	public static long nextLong(){
		return random.nextLong();
	}
	
	public static double nextDouble() {
		return random.nextDouble();
	}
	
	public static float nextFloat() {
		return random.nextFloat();
	}
	
	public static boolean nextBoolean() {
		return random.nextBoolean();
	}
	

	
	public static String nextString(int numChars){
		
		String s = "";
		
		for(int i=0; i<numChars; i++){
			char c = (char)random.nextInt(256);
			s+= c;
		}
		
		return s;
		
	}

}
