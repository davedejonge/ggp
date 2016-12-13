package ddejonge.ggp.asp.grounder;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;

import ddejonge.asp.Constants;
import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;

public class GDL_ASP_Grounder_Example {

	
	public static void main(String[] args) {
		
		List<Gdl> rules = GameParser.file2rules(SystemInfo.GAMES_FOLDER + "hex\\hex.kif");
		String pathToGringo = Constants.GRINGO_LOCATION;
		
		long l1 = System.currentTimeMillis();
		List<Gdl> groundRules = GDL_ASP_Grounder.ground(pathToGringo, rules);
		
		System.out.println("number of ground rules: " + groundRules.size());
		
		PropnetStateMachine stateMachine1 = new PropnetStateMachine();
		stateMachine1.initialize(groundRules);
		long l2 = System.currentTimeMillis();
		System.out.println("generating propnet1 finished  in " + (l2-l1) + " ms.");
		
		/*
		long l3 = System.currentTimeMillis();
		PropNetFlattener flattener = new PropNetFlattener(rules);
		flattener.flatten();
		long l4 = System.currentTimeMillis();
		System.out.println("propnet flattener finished in " + (l4-l3) + " ms.");
		*/
		
		long l5 = System.currentTimeMillis();
		PropnetStateMachine stateMachine2 = new PropnetStateMachine();
		stateMachine2.initialize(rules);
		long l6 = System.currentTimeMillis();
		System.out.println("generating propnet2 finished in " + (l6-l5) + " ms.");
	}
	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
