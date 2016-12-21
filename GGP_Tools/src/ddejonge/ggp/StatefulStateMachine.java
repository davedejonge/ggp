package ddejonge.ggp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.tools.dataStructures.JointMove;

/**
 * Represents a StateMachine that keeps a current state.<br/>
 * Extends the StateMachine class with a number of methods that allow you to query the properties of the current state of the machine,
 * so that you don't have to pass a MachineState object again for each new query. This may allow you to optimize the efficiency of your implementation.
 * 
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public abstract class StatefulStateMachine extends StateMachine{

	
	/**
	 * Sets the current state of the StateMachine.
	 * @param state
	 */
	public abstract void setState(MachineState state);
	
	/**
	 * Returns the goal value of the given role, in the current state of the StateMachine.
	 * @param role
	 * @param checkGoalDefinition if set to false this method will simply return the first goal value it finds, without checking if there are any other goal values defined.
	 * @return
	 */
	public abstract int getGoal(Role role, boolean checkGoalDefinition) throws GoalDefinitionException;
	
	/**
	 * Returns true if the current state of the StateMachine is terminal.
	 */
	public abstract boolean isTerminal();


	/**
	 * Returns the current state of the StateMachine.
	 * @return
	 */
	public abstract MachineState getState();
	

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

	
	@Override
    public List<List<Move>> getLegalJointMoves(MachineState state) throws MoveDefinitionException{
		this.setState(state);
		return getLegalJointMoves();
    }
	
	public List<List<Move>> getLegalJointMoves() throws MoveDefinitionException{
        
		//This implementation is simply copied from StateMachine.getLegalJointMoves(MachineState state). 
		// Except that we have replaced getLegalMoves(state, role) by getLegalMoves(role).
		
		List<List<Move>> legals = new ArrayList<List<Move>>();
        for (Role role : getRoles()) {
            legals.add(getLegalMoves(role));
        }

        List<List<Move>> crossProduct = new ArrayList<List<Move>>();
        crossProductLegalMoves(legals, crossProduct, new LinkedList<Move>());

        return crossProduct;
	}
	
	@Override
	public List<List<Move>> getLegalJointMoves(MachineState state, Role role, Move move) throws MoveDefinitionException{
		this.setState(state);
		return getLegalJointMoves(role, move);
	}
	
    public List<List<Move>> getLegalJointMoves(Role role, Move move) throws MoveDefinitionException
    {
    	
		//This implementation is simply copied from StateMachine.getLegalJointMoves(Role role, Move move). 
		// Except that we have replaced getLegalMoves(state, role) by getLegalMoves(role).
    	
        List<List<Move>> legals = new ArrayList<List<Move>>();
        for (Role r : getRoles()) {
            if (r.equals(role)) {
                List<Move> m = new ArrayList<Move>();
                m.add(move);
                legals.add(m);
            } else {
                legals.add(getLegalMoves(r));
            }
        }

        List<List<Move>> crossProduct = new ArrayList<List<Move>>();
        crossProductLegalMoves(legals, crossProduct, new LinkedList<Move>());

        return crossProduct;
    }
    
    @Override
    public List<MachineState> getNextStates(MachineState state) throws MoveDefinitionException, TransitionDefinitionException
    {
        this.setState(state);
        return this.getNextStates();
    }
    
    public List<MachineState> getNextStates() throws MoveDefinitionException, TransitionDefinitionException
    {
        List<MachineState> nextStates = new ArrayList<MachineState>();
        for (List<Move> move : getLegalJointMoves()) {
            nextStates.add(getNextState(move));
        }

        return nextStates;
    }
    
    @Override
    public Map<Move, List<MachineState>> getNextStates(MachineState state, Role role) throws MoveDefinitionException, TransitionDefinitionException
    {
    	this.setState(state);
        return getNextStates(role);
    }
    
    public Map<Move, List<MachineState>> getNextStates(Role role) throws MoveDefinitionException, TransitionDefinitionException
    {
        Map<Move, List<MachineState>> nextStates = new HashMap<Move, List<MachineState>>();
        Map<Role, Integer> roleIndices = getRoleIndices();
        for (List<Move> moves : getLegalJointMoves()) {
            Move move = moves.get(roleIndices.get(role));
            if (!nextStates.containsKey(move)) {
                nextStates.put(move, new ArrayList<MachineState>());
            }
            nextStates.get(move).add(getNextState(moves));
        }

        return nextStates;
    }
    
    @Override
    public List<Integer> getGoals(MachineState state) throws GoalDefinitionException {
        this.setState(state);
        return this.getGoals();
    }
    
    public List<Integer> getGoals() throws GoalDefinitionException {
        List<Integer> theGoals = new ArrayList<Integer>();
        for (Role r : getRoles()) {
            theGoals.add(getGoal(r, false));
        }
        return theGoals;
    }

    @Override
    public List<Move> getRandomJointMove(MachineState state) throws MoveDefinitionException
    {
        this.setState(state);
        return getRandomJointMove();
    }

    public List<Move> getRandomJointMove() throws MoveDefinitionException
    {
        List<Move> random = new ArrayList<Move>();
        for (Role role : getRoles()) {
            random.add(getRandomMove(role));
        }

        return random;
    }
    
    @Override
    public List<Move> getRandomJointMove(MachineState state, Role role, Move move) throws MoveDefinitionException
    {
        this.setState(state);
        return getRandomJointMove(role, move);
    }

    public List<Move> getRandomJointMove(Role role, Move move) throws MoveDefinitionException
    {
        List<Move> random = new ArrayList<Move>();
        for (Role r : getRoles()) {
            if (r.equals(role)) {
                random.add(move);
            } else {
                random.add(getRandomMove(r));
            }
        }

        return random;
    }


    @Override
    public Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException
    {
        this.setState(state);
        return getRandomMove(role);
    }
    
    public Move getRandomMove(Role role) throws MoveDefinitionException
    {
        List<Move> legals = getLegalMoves(role);
        return legals.get(new Random().nextInt(legals.size()));
    }

    @Override
    public MachineState getRandomNextState(MachineState state) throws MoveDefinitionException, TransitionDefinitionException
    {
        this.setState(state);
        return getRandomNextState();
    }

    public MachineState getRandomNextState() throws MoveDefinitionException, TransitionDefinitionException
    {
        List<Move> random = getRandomJointMove();
        return getNextState(random);
    }
    
    @Override
    public MachineState getRandomNextState(MachineState state, Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException
    {
        this.setState(state);
        return getRandomNextState(role, move);
    }
    
    public MachineState getRandomNextState(Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException
    {
        List<Move> random = getRandomJointMove(role, move);
        return getNextState(random);
    }

    @Override
    public MachineState performDepthCharge(MachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
    	 this.setState(state);
    	 return performDepthCharge(theDepth);
    }
    
    public MachineState performDepthCharge(final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
       
    	int nDepth = 0;
        while(!isTerminal()) {
            nDepth++;
            /*state = getNextStateDestructively(state, getRandomJointMove(state));*/
            MachineState nextState = getNextState(getRandomJointMove());
            this.setState(nextState);
        }
        if(theDepth != null)
            theDepth[0] = nDepth;
        return getState();
    }

    @Override
    public void getAverageDiscountedScoresFromRepeatedDepthCharges(final MachineState state, final double[] avgScores, final double[] avgDepth, final double discountFactor, final int repetitions) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
    	this.setState(state);
    	getAverageDiscountedScoresFromRepeatedDepthCharges(avgScores, avgDepth, discountFactor, repetitions);
    }
    
    public void getAverageDiscountedScoresFromRepeatedDepthCharges(final double[] avgScores, final double[] avgDepth, final double discountFactor, final int repetitions) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
    	
    	MachineState stateForCharge = getState();
    	
    	avgDepth[0] = 0;
    	for (int j = 0; j < avgScores.length; j++) {
    		avgScores[j] = 0;
    	}
    	final int[] depth = new int[1];
    	for (int i = 0; i < repetitions; i++) {
    		this.setState(stateForCharge);
    		performDepthCharge(depth);
    		avgDepth[0] += depth[0];
    		final double accumulatedDiscountFactor = Math.pow(discountFactor, depth[0]);
    		for (int j = 0; j < avgScores.length; j++) {
    			avgScores[j] += getGoal(getRoles().get(j), false) * accumulatedDiscountFactor;
    		}
    	}
    	avgDepth[0] /= repetitions;
    	for (int j = 0; j < avgScores.length; j++) {
    		avgScores[j] /= repetitions;
    	}
    }



}
