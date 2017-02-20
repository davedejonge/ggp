package ddejonge.ggp.asp.puzzleSolver;

import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.transforms.DeORer;

import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;

public class ASPPuzzleSolver_Example {

	//STATIC FIELDS
	private static String PUZZLE_NAME = "8puzzle";
	private static int SEARCH_TIME = 60_000; 			//maximum time to search for a solution, in milliseconds.
	
	// Find a solution with this maximum number of steps.
	// Note: if you set this value very high it may take a long time to find the solution. However, if you set it too low, there simply may not be any solution.
	private static int MAX_SOLUTION_LENGTH = 30; 		
	
	//STATIC METHODS
	public static void main(String[] args) {
		
		//List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\knightsTour\\knightsTour.kif");
		//List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\bandl3\\bandl3_player.kif");
		//List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\sudokuGrade6H\\sudoku.kif");
		//List<Gdl> description = GameParser.file2rules(SystemInfo.GAMES_FOLDER + "8puzzle\\rulesheet.kif");
		
		List<Gdl> description =  GameParser.getRulesFromGameFolder(SystemInfo.GAMES_FOLDER + PUZZLE_NAME);
		
		//sanitize the description by removing any occurance of the keyword OR.
		List<Gdl> newDescription = DeORer.run(description);
		
		ASPPuzzleSolver solver = new ASPPuzzleSolver(newDescription);
		//solver.printAllRules();
		
		System.out.println("ASPPuzzleSolver.main() searching for solution...");
		long l1 = System.currentTimeMillis();
		boolean solutionFound = solver.findSolutionLimitedSteps(MAX_SOLUTION_LENGTH, System.currentTimeMillis() + SEARCH_TIME);
		long l2 = System.currentTimeMillis();
		
		if(solutionFound){
			
			System.out.println();
			System.out.println("*************");
			System.out.println("SOLUTON FOUND!");
			System.out.println("num steps: " + solver.solution.length);
			System.out.println("found in " + (l2 - l1) + " ms.");
			System.out.println(Arrays.toString(solver.solution));
		}else{
			System.out.println("no solution found.");
		}
		
	}
	
	
	
	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
