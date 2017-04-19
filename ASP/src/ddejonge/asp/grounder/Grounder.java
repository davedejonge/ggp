package ddejonge.asp.grounder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ddejonge.asp.ASPGobbler;
import ddejonge.asp.ASPRunner;
import ddejonge.asp.Constants;
import ddejonge.asp.Result;
import ddejonge.asp.examples.TSP;
import ddejonge.asp.tools.FileIO;

public class Grounder {

	
	static int counter;
	

	public static List<String> ground(String pathToGringo, String originalProgram){
		List<String> prgrm = new ArrayList<>();
		prgrm.add(originalProgram);
		return ground(pathToGringo, prgrm);
	}
	
	
	public static List<String> ground(String pathToGringo, List<String> originalProgram){
		
		
		//** 1. write the original program to file.
		long l1 = System.currentTimeMillis();
		
		String folderName = "temp";
		String fileName = System.currentTimeMillis() + "_" + counter + "_tempFile.lp";
		counter++;
		File tempFile = FileIO.stringsToFile(folderName, fileName, originalProgram, false);
		
		long l2 = System.currentTimeMillis();
		System.out.println("step 2 (write asp strings to file) finished in " + (l2-l1) + " ms.");
		
		
		
		
		//** 2. feed the file to GRINGO
		l1 = System.currentTimeMillis();
		
		String[] cmd = new String[]{pathToGringo, "-t", "--keep-facts", tempFile.getAbsolutePath()};
		List<String> result = exec(cmd, "gringo", false);
		
		l2 = System.currentTimeMillis();
		System.out.println("step 3 (calling gringo) finished in " + (l2-l1) + " ms.");
		
		
		return result;
	}
	
	
	private static List<String> exec(String[] cmdArray, String name, boolean print){
		
		Process process = null; 
		List<String> result = null;
		
		try{
			process = Runtime.getRuntime().exec(cmdArray);
			
			GrounderGobbler errorGobbler = new GrounderGobbler(process.getErrorStream(), true);            
			GrounderGobbler outputGobbler = new GrounderGobbler(process.getInputStream(), print);
	        errorGobbler.start();
	        outputGobbler.start();
        
	        result = outputGobbler.waitForResult();
	        
	        
		}catch (Throwable e) {
			e.printStackTrace();
		}	
		
		
        
		return result;
		
	}
	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
