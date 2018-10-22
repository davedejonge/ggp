package ddejonge.ggp.tools.randomStateGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;
import ddejonge.ggp.tools.Utils;


/**
 * Generates a random sequence of joint moves and the corresponding states for a given game.<br/>
 * Is useful for testing whether a certain game was specified in GDL correctly.
 * 
 * @author Dave de Jonge
 *
 */
public class RandomStateGenerator {

	//STATIC FIELDS

	
	
	//STATIC METHODS
	public static void main(String[] args) {
		
		//String gameDescriptionPath = SystemInfo.JAVA_PROJECTS_FOLDER + "GGP\\ggp-base-master\\games\\game\\8puzzle\\rulesheet.kif";
		
		String gameDescriptionPath = SystemInfo.JAVA_PROJECTS_FOLDER + "GNG\\GGP_Negotiations\\negoGames\\genius_linear\\anac 2013\\Acquisition\\Acquisition.kif";
		List<Gdl> description = GameParser.file2rules(gameDescriptionPath);
	
		asdñjf;
		//TODO: insert reservation values into the generated kif files.
		
		StateMachine stateMachine = new ProverStateMachine();
		
		RandomStateGenerator generator = new RandomStateGenerator(description, stateMachine);
		
		for(int i=0; i<10; i++) {
		
			generator.generate();
			
			List<List<Move>> jointMoveSequence = generator.getJointMoveSequence();
			
			for (List<Move> jointMove : jointMoveSequence) {
				System.out.println(jointMove);
			}
			System.out.println(generator.getGoals());
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}
		
	}
	
	
	public static MachineState getRandomState(List<Gdl> description, StateMachine stateMachine){
		
		List<MachineState> stateSequence = RandomStateGenerator.getRandomStateSequence(description, stateMachine);
		return Utils.getRandomObjectFromList(stateSequence);
	}
	
	public static List<MachineState> getRandomStateSequence(List<Gdl> description, StateMachine stateMachine){
		
		RandomStateGenerator generator = new RandomStateGenerator(description, stateMachine);
		generator.generate();
		
		return generator.getStateSequence();
	}
	
	public static List<List<Move>> getRandomMoveSequence(List<Gdl> description, StateMachine stateMachine){
		
		RandomStateGenerator generator = new RandomStateGenerator(description, stateMachine);
		generator.generate();
		
		return generator.getJointMoveSequence();
	}
	
	
	
	

	//FIELDS
	private StateMachine stateMachine;
	
	private List<List<Move>> jointMoveSequence;
	private List<MachineState> stateSequence;
	private List<Integer> goals;
	
	
	//CONSTRUCTOR
	RandomStateGenerator(List<Gdl> description, StateMachine stateMachine){
		this.stateMachine = stateMachine;
		stateMachine.initialize(description);
	}
	
	
	
	
	//METHODS
	
	/**
	 * Generate a new random sequence of joint moves. Will overwrite results from a previous call to this method.
	 */
	public void generate() {
		
		jointMoveSequence = new ArrayList<>();
		stateSequence = new ArrayList<>();
		
		try {
			
			MachineState state = stateMachine.getInitialState();
			
	        while(!stateMachine.isTerminal(state)) {
	        	
	        	List<Move> jointMove = stateMachine.getRandomJointMove(state);
	        	jointMoveSequence.add(Collections.unmodifiableList(jointMove));
	        	
	            state = stateMachine.getNextState(state, jointMove);
	            stateSequence.add(state);
	        }

	        
	        goals = this.stateMachine.getGoals(state);
		
		} catch (TransitionDefinitionException | MoveDefinitionException e) {
			e.printStackTrace();
		} catch (GoalDefinitionException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	//GETTERS
	
	public List<List<Move>> getJointMoveSequence(){
		return Collections.unmodifiableList(jointMoveSequence);
	}
	
	public List<MachineState> getStateSequence(){
		return Collections.unmodifiableList(stateSequence);
	}
	
	public List<Integer> getGoals() {
		return Collections.unmodifiableList(goals);
	}
}
