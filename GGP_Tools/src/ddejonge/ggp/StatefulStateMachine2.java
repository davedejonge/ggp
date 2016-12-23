package ddejonge.ggp;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public abstract class StatefulStateMachine2 extends StateMachine{

	MachineState currentState;
	
	/**
	 * Returns the goal value of the given role, in the current state of the StateMachine.
	 * @param role
	 * @return
	 * @throws GoalDefinitionException 
	 */
	public int getGoal(Role role) throws GoalDefinitionException{
		return this.getGoal(currentState, role);
	}
	
	/**
	 * Returns true if the current state of the StateMachine is terminal.
	 */
	public boolean isTerminal(){
		return this.isTerminal(currentState);
	}


	/**
	 * Returns the current state of the StateMachine.
	 * @return
	 */
	public abstract MachineState getCurrentState();
	
	/**
	 * Sets the current state of the StateMachine.
	 * @param state
	 */
	public abstract void setCurrentState(MachineState state);

	/**
	 * Returns the legal moves of the given role, in the current state of the StateMachine.
	 * @param role
	 * @return
	 */
	public abstract List<Move> getLegalMoves(Role role) throws MoveDefinitionException;

	/**
	 * Returns the next state, if the given moves are made in the current state of the StateMachine.
	 * @param moves
	 * @return
	 */
	public abstract MachineState getNextState(List<Move> moves) throws TransitionDefinitionException;

	public abstract void setNextStateAsCurrentState(List<Move> moves);
	
	//STATIC FIELDS

	//STATIC METHODS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
