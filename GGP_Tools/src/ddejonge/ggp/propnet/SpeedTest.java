package ddejonge.ggp.propnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import ddejonge.ggp.tools.GameParser;



public class SpeedTest {
	
	public static void main(String[] args) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		
		//Load the rules of TIC-TAC-TOE
		List<Gdl> rules = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\games\\breakthrough\\breakthrough.kif");
		
		
		//1. FIRST test the prover state machine
		ProverStateMachine proverStateMachine = new ProverStateMachine();
		proverStateMachine.initialize(rules);
		
		int time = 60*1000;
		
		int numRollouts = runSpeedTest(proverStateMachine, time);
		
		System.out.println("Number of rollouts: " + numRollouts + "     (" + totalGoals + ")");
		
		
		//1. FIRST test the propnet state machine
		PropnetStateMachine propnetStateMachine = new PropnetStateMachine();
		propnetStateMachine.initialize(rules);
		
		numRollouts = runSpeedTest(propnetStateMachine, time);
		
		System.out.println("Number of rollouts: " + numRollouts + "     (" + totalGoals + ")");
		
		
	}
	
	static int totalGoals;
	
	static int runSpeedTest(ProverStateMachine stateMachine, int time) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		
		Random random = new Random();
		
		MachineState inititalState = stateMachine.getInitialState();
		MachineState state = inititalState;
		
		List<Role> roles = stateMachine.getRoles();
		List<Move> jointMove;
		
		
		int numRollouts = 0;
		
		long deadline = System.currentTimeMillis() + time;
		while(System.currentTimeMillis() < deadline){
			
			if(stateMachine.isTerminal(state)){
				
				totalGoals += stateMachine.getGoal(state, roles.get(0));
				totalGoals += stateMachine.getGoal(state, roles.get(1));
				
				numRollouts++;
				
				state = inititalState;
			
			}else{
				
				
				jointMove = new ArrayList<Move>();
				
				for(Role role : roles){
					
					List<Move> legalMoves = stateMachine.getLegalMoves(state, role);
				
					int r = random.nextInt(legalMoves.size());
					jointMove.add(legalMoves.get(r));
				}
				
				state = stateMachine.getNextState(state, jointMove);
				
			}
			
			
		}
		
		return numRollouts;
		
		
	}
	
	static int runSpeedTest(PropnetStateMachine stateMachine, int time) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		
		MachineState intitalState = stateMachine.getInitialState();
		
		List<Role> roles = stateMachine.getRoles();
		
		
		int numRollouts = 0;
		
		long deadline = System.currentTimeMillis() + time;
		while(System.currentTimeMillis() < deadline){
			
			
			if(stateMachine.isTerminal()){
				
				totalGoals += stateMachine.getGoal(roles.get(0), false);
				totalGoals += stateMachine.getGoal(roles.get(1), false);
				
				numRollouts++;
				
				stateMachine.setState(intitalState);
			
			}else{
				
				stateMachine.setRandomActions();
				
				stateMachine.setNextStateAsCurrentState();
			}
			
			
		}
		
		return numRollouts;
		
		
	}

}
