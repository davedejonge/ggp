package ddejonge.asp.grounder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ddejonge.asp.Result;

class GrounderGobbler extends Thread{

    InputStream inputStream;
    boolean print;
    
	boolean finished = false;
	List<String> groundedProgram;
    
	GrounderGobbler(InputStream is, boolean print){
        
    	this.inputStream = is;
        this.print = print;
    }
    
	GrounderGobbler(InputStream is){
    	   	
        this.inputStream = is;
        this.print = false;
    }
    
    public void run()
    {
    	
    	groundedProgram = new ArrayList<>();
    	finished = false;
    	
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)))
        {

        	String line=null;
            while ( (line = br.readLine()) != null ){
                
            	groundedProgram.add(line);
            	
            	if(print){
            		System.out.println(line);
            	}
            
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
        
        finished = true;
    }
    
    public List<String> waitForResult(){
        
    	while(! this.finished){
        	
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
        }
    	
    	return this.groundedProgram;
    }
    
}
