package ddejonge.asp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ddejonge.asp.tools.FileIO;



public class ASPRunner {
	
	
	
	//STATIC FIELDS
	
	/**
	 * This is just to make sure that each temporary file has a unique file name.
	 */
	private static int counter;
	
	
	
	//STATIC METHODS
	
	public static Result findModels(String program, int numModels, boolean print, long timeout){
	
		ArrayList<String> list = new ArrayList<String>();
		list.add(program);
		return findModels(list, numModels, print, timeout);
		
	}

	
	/**
	 * 
	 * @param program
	 * @param numModels The maximum number of models to be computed, where 0 means 'compute all answer sets'
	 * @return
	 */
	public static Result findModels(ArrayList<String> program, int numModels, boolean print, long timeout){
		
		String fileName = System.currentTimeMillis() + "_" + counter + "_tempFile.lp";
		counter++;
		
		File tempFile = FileIO.stringsToFile("temp", fileName, program, false);
		
		return findModels(tempFile, numModels, print, timeout);
	}
	
	public static Result findModels(File file, int numModels, boolean print, long timeout){
		
		//calculate maximum time
		int timeLimit = (int)(timeout - System.currentTimeMillis())/1000; 
		
		if(timeLimit == 0){
			return null;
		}
		
		String[] cmd = new String[]{Constants.CLINGO_LOCATION,  "--time-limit=" + timeLimit, file.getAbsolutePath(), "" + numModels};
		
		Result result = ASPRunner.exec(cmd, "clingo", print);
		
		return result;
	}
	
	
	
	
	
	
	public static Result findOptimalModel(String program, boolean findAll, boolean print){
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(program);
		
		return findOptimalModel(list, findAll, print);
	}
	
	public static Result findOptimalModel(ArrayList<String> program, boolean findAll, boolean print){
		File tempFile = FileIO.stringsToFile("temp", "tempFile.lp", program, false);
		
		return findOptimalModel(tempFile, findAll, print);
	}
	
	public static Result findOptimalModel(File file, boolean findAll, boolean print){
		
		String[] cmd;
		
		//--quiet=1 means that the clingo should only output the optimal models.
		if(findAll){
			cmd = new String[]{Constants.CLINGO_LOCATION,  "--quiet=1",  "--opt-mode=optN", file.getAbsolutePath()};
		}else{
			cmd = new String[]{Constants.CLINGO_LOCATION,  "--quiet=1", file.getAbsolutePath()};
		}
		
		Result result = ASPRunner.exec(cmd, "clingo", print);
		
		return result;
		
	}
	
	
	
	
	private static Result exec(String[] cmdArray, String name, boolean print){
		
		Process process = null; 
		Result result = null;
		
		try{
			process = Runtime.getRuntime().exec(cmdArray);
			
	        ASPGobbler errorGobbler = new ASPGobbler(process.getErrorStream(), true);            
	        ASPGobbler outputGobbler = new ASPGobbler(process.getInputStream(), print);
	        errorGobbler.start();
	        outputGobbler.start();
        
	        result = outputGobbler.waitForResult();
	        
	        
		}catch (Throwable e) {
			e.printStackTrace();
		}	
		
		
        
		return result;
		
	}
	
	
	
	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
