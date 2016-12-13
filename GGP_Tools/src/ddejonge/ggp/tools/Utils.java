package ddejonge.ggp.tools;

import java.util.List;
import java.util.Random;

public class Utils {
	
	public static Random random = new Random();

	/**
	 * Returns true if the given string represents an integer. e.g. "10".
	 * @param s
	 * @return
	 */
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	private static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	
	public static <T> T getRandomObjectFromList(List<? extends T> list){
		int i = random.nextInt(list.size());
		return list.get(i);
	}
	
	
	/**
	 * Randomly shuffles the elements in a list.
	 * @param list
	 */
	public static <T> void shuffle(List<T> list){
		
		for(int i=list.size()-1; i>=1; i--){
			//if list has size 10, then i runs from 9 till 1
			
			//in the first iteration we have i = 9 and 0 <= j <= 9
			//in the last iteration we have i=1 and 0 <= j <= 1
			int j= random.nextInt(i+1); 
			
			T temp = list.get(i);
			list.set(i, list.get(j));
			list.set(j, temp);
			
		}
		
		return;
	}
	
	/**
	 * Rounds off a number to the given number of digits.
	 * @param number
	 * @param numDigits
	 * @return
	 */
	public static double round(double number, int numDigits){
		
		if(numDigits < 0){
			throw new IllegalArgumentException("Number of digits bust be greater than or equal to 0.");
		}
		
		int powerOfTen = 1;
		for(int i=0; i<numDigits; i++){
			powerOfTen *= 10;
		}
		
		double returnVal = number * powerOfTen;
		
		returnVal = (double)Math.round(returnVal);
		
		returnVal = returnVal / ((double)powerOfTen);
		
		return returnVal;
		
	}
	
	
	/**
	 * Given an amount of milliseconds, returns a string that represents that time.
	 * @param timeSpanMillis
	 * @return
	 */
	public static String getTimeSpanString(long timeSpanMillis){
		
		if(timeSpanMillis == 0){
			return "0 ms.";
		}
		
		if(timeSpanMillis < 0){
			throw new RuntimeException("Utils.getTimeSpanString() Error! timeSpanMillis == " + timeSpanMillis + " Must be greater than 0. ");
		}
		
		long millisPerSec = 1000;
		long millisPerMin = 60 * millisPerSec;
		long millisPerHour = 60 * millisPerMin;
		long millisPerDay = 24 * millisPerHour;
		long millisPerYear = 356 * millisPerDay;
		
		String s = "";
		
		long years = timeSpanMillis / millisPerYear;
		timeSpanMillis -= years * millisPerYear;
		if(years > 0){
			s += years + "yr ";
		}
		
		long days = timeSpanMillis / millisPerDay;
		timeSpanMillis -= days * millisPerDay;
		if(days > 0){
			s += days + "d ";
		}
		
		long hours = timeSpanMillis / millisPerHour;
		timeSpanMillis -= hours * millisPerHour;
		if(hours > 0){
			s += hours + "hr ";
		}
		
		long minutes = timeSpanMillis / millisPerMin;
		timeSpanMillis -= minutes * millisPerMin;
		if(minutes > 0){
			s += minutes + "min ";
		}
		
		long seconds = timeSpanMillis / millisPerSec;
		timeSpanMillis -= seconds * millisPerSec;
		if(seconds > 0){
			s += seconds + "sec ";
		}
		
		if(timeSpanMillis > 0){
			s += timeSpanMillis + "ms";
		}
		
		return s;
	}
	
	public static void printStackTrace(){
		
		StackTraceElement[] array = Thread.currentThread().getStackTrace();
		for (int i = 2; i < array.length; i++) {
			System.out.println(array[i].toString());
		}
	}
	
}
