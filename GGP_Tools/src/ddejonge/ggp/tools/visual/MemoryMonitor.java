package ddejonge.ggp.tools.visual;

public class MemoryMonitor {

	
	Runtime runtime = null;
	
	public MemoryMonitor(){
		this.runtime = Runtime.getRuntime();
	}
	
	/**
	 * Returns the number of MegaBytes of used memory.
	 * 
	 * @return
	 */
	public int getUsedMemoryMB(){
		return (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1e6);
	}
	
	
	public String getUsedMemoryString(){
		return "Used memory: " + getUsedMemoryMB() + " MB";
	}
	
	public void printUsedMemory(String prefix){
		System.out.println(prefix + "Used memory: " + getUsedMemoryMB() + " MB");
	}
	
	
	public void printMemory(){
		
		long max = runtime.maxMemory(); 	//the maximum amount of memory the heap may occupy.
		long total = runtime.totalMemory(); //current heap size
		long free = runtime.freeMemory(); 	//current heap size minus used memory.
		
		long used = total - free;
		long realFree = max - used;
		
		System.out.println("Max Heap size: " + max / (1000 * 1000) + " MB");
		System.out.println("Used memory: " + used / (1000 * 1000) + " MB");
		System.out.println("Free memory: " + realFree / (1000 * 1000) + " MB");
	}
	
}
