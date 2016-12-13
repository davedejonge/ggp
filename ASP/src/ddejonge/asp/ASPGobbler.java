package ddejonge.asp;

import java.io.*;

/**
 * Reads from the given inputstream and redirects it to the given output stream.
 * 
 * 
 * @author http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 *
 */
public class ASPGobbler extends Thread
{
    InputStream inputStream;
    boolean print;
    
	boolean finished = false;
	Result result;
    
    ASPGobbler(InputStream is, boolean print){
        
    	this.inputStream = is;
        this.print = print;
    }
    
    ASPGobbler(InputStream is){
    	   	
        this.inputStream = is;
        this.print = false;
    }
    
    public void run()
    {
    	
    	result = new Result();
    	finished = false;
    	
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
        {

        	String line=null;
            while ( (line = br.readLine()) != null ){
                
            	if(line.startsWith("Answer")){
            		//System.out.println(line);
            		line = br.readLine();
            		result.solutions.add(line);
            	}
            	
            	if(line.startsWith("Models")){
            		result.numModels = line.split(":")[1].trim();
            	}
            	
            	if(line.startsWith("SATISFIABLE")){
            		result.satisfiable = true;
            	}else if(line.startsWith("UNSATISFIABLE")){
            		result.satisfiable = false;
            	}else if(line.startsWith("UNKNOWN")){
            		result.satisfiable = null;
            	}
            	
            	if(line.startsWith("Optimization")){
            		result.optimalValue = Integer.parseInt(line.split(":")[1].trim());
            	}
            	
            	if(print){
            		System.out.println(line);
            	}
            
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
        
        finished = true;
    }
    
    public Result waitForResult(){
        
    	while(! this.finished){
        	
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
        }
    	
    	return this.result;
    }
    
    
}