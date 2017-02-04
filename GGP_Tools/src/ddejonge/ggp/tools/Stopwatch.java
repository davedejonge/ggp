package ddejonge.ggp.tools;
import java.util.Date;


/**
 * 
 * 
 * 
 * 
 * @author Dave de Jonge
 *
 */
public class Stopwatch {

	//STATIC STOPWATCH
	
	private static long l1;
	private static long l2;
	private static long totalTimeStatic = 0;
	
	public static void startStatic(){
		/*l1 = (new Date()).getTime();*/
		l1 = System.currentTimeMillis();
	}
	
	public static void stopStatic(){
		/*l2 = (new Date()).getTime();*/
		l2 = System.currentTimeMillis();
		totalTimeStatic += (l2-l1);
	}
	
	public static int getTimeStatic(){
		return (int) totalTimeStatic;
	}
	
	public static void resetStatic(){
		totalTimeStatic = 0;
	}
	
	
	
	
	//NON-STATIC STOPWATCH
	
	private long l3 = 0;
	private long l4 = 0;
	private long totalTime = 0;
	
	
	/**
	 * Makes the stopwatch to start to run, or to continue running after it has been stopped.
	 */
	public void start(){
		/*l3 = (new Date()).getTime();*/
		l3 = System.currentTimeMillis();
	}
	
	/**
<<<<<<< HEAD
	 * Pauses running, but does not reset the time to zero.
=======
	 * Stops the time, but does not reset the time to zero.
>>>>>>> branch 'master' of https://github.com/davedejonge/ggp.git
	 * Calling <code>start()</code> after <code>stop()</code> causes the stopwatch to continue running from where it had been stopped.
	 * 
	 */
	public void stop(){
		/*l4 = (new Date()).getTime();*/
		l4 = System.currentTimeMillis();
		totalTime += (l4-l3);
	}
	
	/**
	 * Sets the stopwatch back to zero.
	 */
	public void reset(){
		totalTime = 0;
	}
	
	/**
	 * Returns the time.<p>
	 * The time is the sum of all the intervals between start() and stop() since the last reset, or since the creation of the object.
	 * 
	 * @return The sum of all the intervals between start() and stop() since the last reset, or since the creation of the object.
	 */
	public int getTime(){
		return (int) (totalTime);
	}
	
	/**
	 * Returns the time as a string.
	 * @return
	 */
	public String getTimeString(){
		return "" + totalTime + " ms.";
	}
	
}
