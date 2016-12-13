package ddejonge.ggp.propnet;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.Utils;


/**
 * This class just shows the basic use of the PropnetStateMachine.
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class Propnet_Example {

	public static void main(String[] args) {
		
		//Load the rules of some game.
		List<Gdl> rules = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\games\\breakthrough\\breakthrough.kif");
	
		//Create a PropnetStateMachine object.
		PropnetStateMachine propnetStateMachine = new PropnetStateMachine();
		
		//Initialize it with the rules of the game.
		propnetStateMachine.initialize(rules);
		
		//get the initial state of that game.
		MachineState propNetInitialState = propnetStateMachine.getInitialState();
		
		//Get the roles defined for the game.
		List<Role> roles = propnetStateMachine.getRoles();
		
		
		//Pick a random legal move for each player.
		List<Move> jointMove = new ArrayList<>();
		for(Role role : roles){
			
			//Get all legal moves for the current role.
			List<Move> legalMoves = null;
			try {
				legalMoves = propnetStateMachine.getLegalMoves(propNetInitialState, role);
			} catch (MoveDefinitionException e) {
				e.printStackTrace();
			}
			
			//pick one randomly.
			Move randomMove = Utils.getRandomObjectFromList(legalMoves);
			
			//add it to the joint move.
			jointMove.add(randomMove);
		}
		

		//Get the next state, resulting from the randomly chosen actions of the players.		
		MachineState newState = null;
		try {
			newState = propnetStateMachine.getNextState(jointMove);
		} catch (TransitionDefinitionException e) {
			e.printStackTrace();
		}
		
		//test if the current state is terminal:
		boolean currentStateIsTerminal = propnetStateMachine.isTerminal(newState);
		
		//If it is terminal, then get the goal values for each player and print them out.
		if(currentStateIsTerminal){
			System.out.println("Current state is terminal!");
			
			for(Role role : roles){
				
				int goal = -1;
				try {
					goal = propnetStateMachine.getGoal(newState, role);
				} catch (GoalDefinitionException e) {
					// This happens if the rules do not define a goal value for the given role in the given state.
					// However, for a correctly described game the goal values must always be defined for any terminal state.
					e.printStackTrace();
				}
				
				System.out.println("" + role + " finished with goal value: " + goal);
			}
		}
		
	}
	
	
}
