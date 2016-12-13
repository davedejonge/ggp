package ddejonge.ggp.tools.randomStateGenerator;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.tools.Utils;

public class RandomStateGenerator {

	//STATIC FIELDS

	//STATIC METHODS
	public static MachineState generate(List<Gdl> description){
		StateMachine stateMachine = new PropnetStateMachine();
		stateMachine.initialize(description);
		
		return generate(stateMachine);
	}
	
	
	public static MachineState generate(StateMachine stateMachine){
		
		try {
			
			MachineState state = stateMachine.getInitialState();
			
			ArrayList<MachineState> states = new ArrayList<>();
	        while(!stateMachine.isTerminal(state)) {
	            state = stateMachine.getNextState(state, stateMachine.getRandomJointMove(state));
	            states.add(state);
	        }

			return Utils.getRandomObjectFromList(states);
		
		} catch (TransitionDefinitionException | MoveDefinitionException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	

}
