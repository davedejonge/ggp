package ddejonge.asp.grounder;

import java.util.List;

import ddejonge.asp.Constants;
import ddejonge.asp.examples.TSP;

public class Grounder_Example {

	
	public static void main(String[] args) {
		String content = TSP.getTspInstance();
		
		long l1 = System.currentTimeMillis();
		List<String> groundedProgram = Grounder.ground(Constants.CLINGO_LOCATION, content);
		long l2 = System.currentTimeMillis();
		
		for(String s : groundedProgram){
			System.out.println(s);
		}
		
		System.out.println("finished in " + (l2-l1) + " ms.");
	}
	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
