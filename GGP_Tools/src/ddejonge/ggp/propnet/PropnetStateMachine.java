package ddejonge.ggp.propnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.propnet.factory.flattener.PropNetFlattener;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.propnet.heuristics.Heuristics;
import ddejonge.ggp.propnet.heuristics.MoveCollector;
import ddejonge.ggp.tools.Utils;
import ddejonge.ggp.tools.dataStructures.JointMove;


public class PropnetStateMachine extends StateMachine{

	
	Propnet propnet = new Propnet();
	List<Role> roles;	
	
	
	@Override
	public void initialize(List<Gdl> description) {

		roles = Role.computeRoles(description); //This method makes sure the roles are determined in the correct order!
		/*for (int i = 0; i < roles.size(); i++) {
			propnet.roleNames.add(roles.get(i).getName());
		}*/
		
		
		try {
			
			long l1 = System.currentTimeMillis();
			PropNet _propNet = OptimizingPropNetFactory.create(description);
			long l2 = System.currentTimeMillis();
			System.out.println("PropnetStateMachine.initialize() finished generating initial propnet in " + (l2-l1) + " ms.");
			System.out.println("PropnetStateMachine.initialize() Now converting propnet");
			
			long l3 = System.currentTimeMillis();
			propnet.init(_propNet, roles);
			long l4 = System.currentTimeMillis();
			System.out.println("PropnetStateMachine.initialize() finished converting propnet in " + (l4-l3) + " ms.");
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	
	
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		
		if(state != null){
			propnet.setState(state.getContents());
		}
		return getGoal(role, false);
	}
	
	public int[] getGoals() throws GoalDefinitionException{
		
		int[] goals = new int[this.roles.size()];
		for (int i = 0; i < goals.length; i++) {
			goals[i] = getGoal(i, false);
		}
		
		return goals;
	}
	
	public int getGoal(Role role, boolean safely) throws GoalDefinitionException {
		return getGoal(this.roles.indexOf(role), safely);
	}
		
	/**
	 * If 'safely' is set to false it directly returns a goal value as soon as it finds one.
	 * If 'safely' is set to true it first checks that there is no other goal value and throws an
	 * Exception if it does finds another goal value.
	 * @param roleIndex
	 * @param safely
	 * @return
	 * @throws GoalDefinitionException
	 */
	int getGoal(int roleIndex, boolean safely) throws GoalDefinitionException {	
		
		List<jProposition> goalPropositionsOfRole = propnet.roleIndex2goalPropositions.get(roleIndex);
		jProposition trueGoalProp = null;
		
		
		for(jProposition goalProp : goalPropositionsOfRole){
			if(goalProp.currentValue){
				
				if(!safely){
					return goalProp.goalValue;
				}
				
				if(trueGoalProp == null){
					
					trueGoalProp = goalProp;
					
				}else{
					throw new GoalDefinitionException(new MachineState(propnet.getCurrentState()), roles.get(roleIndex));
				}
			}
		}
		
		if(trueGoalProp == null){
			throw new GoalDefinitionException(new MachineState(propnet.getCurrentState()), roles.get(roleIndex));
		}
		
		return trueGoalProp.goalValue;
	}

	/**
	 * If the given state is null, then this method returns true if and only if the current state of the propnet is terminal.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		
		if(state != null){
			propnet.setState(state.getContents());
		}
		return isTerminal();
		
		
	}
	
	public boolean isTerminal() {
		return propnet.terminalProposition.currentValue;
	}
	

	@Override
	public List<Role> getRoles() {
		return roles;
	}

	@Override
	public MachineState getInitialState() {
		return new MachineState(propnet.getInitialState());
	}

	/**
	 * Returns a list of PropNetMoves
	 * 
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
		if(state != null){
			propnet.setState(state.getContents());
		}
		return getLegalMoves(role);
	}
	
	/**
	 * Returns a list of PropNetMoves
	 * 
	 * @param role
	 * @return
	 * @throws MoveDefinitionException
	 */
	List<Move> getLegalMoves(Role role) throws MoveDefinitionException {
		return getLegalMoves(roles.indexOf(role));
	}
	
	/**
	 * 
	 * Returns a list of PropNetMoves
	 * 
	 * @param roleIndex
	 * @return
	 * @throws MoveDefinitionException
	 */
	public List<Move> getLegalMoves(int roleIndex) throws MoveDefinitionException {
		List<jProposition> legalPropositionsOfRole = propnet.roleIndex2legalPropositions.get(roleIndex);
		
		/*	
		if(legalPropositionsOfRole == null){
			System.out.println("role.getName() " + role.getName());
			System.out.println(propnet.roleName2legalPropositions);
		}*/
		
		List<Move> legalMoves = new ArrayList<Move>();
		
		for(jProposition legalProposition : legalPropositionsOfRole){
			if(legalProposition.currentValue){
				legalMoves.add(new PropnetMove(legalProposition.correspondingDoesProposition));
			}
		}
			
		return legalMoves;
	}
	
	List<jProposition> getLegalMovesAsPropositions(Role role) throws MoveDefinitionException {
		return getLegalMovesAsPropositions(roles.indexOf(role));
	}
	
	List<jProposition> getLegalMovesAsPropositions(int roleIndex) throws MoveDefinitionException {
		
		List<jProposition> legalPropositionsOfRole = propnet.roleIndex2legalPropositions.get(roleIndex);
			
		if(legalPropositionsOfRole == null){
			
			/*
			System.out.println("role.getName() " + role.getName());
			System.out.println(propnet.roleName2legalPropositions);*/
			
			throw new MoveDefinitionException(new MachineState(propnet.getCurrentState()), roles.get(roleIndex));

		}
		
		List<jProposition> satisfiedLegalPropositionsOfRole = new ArrayList<jProposition>();
		
		for(jProposition legalProposition : legalPropositionsOfRole){
			if(legalProposition.currentValue){
				satisfiedLegalPropositionsOfRole.add(legalProposition);
			}
		}
			
		return satisfiedLegalPropositionsOfRole;
	}
	
	

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {
		
		//TODO: when are we supposed to throw the TransitionDefinitionException?
		
		
		if(state != null){
			propnet.setState(state.getContents());
		}
		return getNextState(moves);
	}
	
	
	/**
	 * Returns the next state without actually setting it as the new state of the propnet.
	 * @return
	 */
	MachineState getNextState(List<Move> moves) throws TransitionDefinitionException {
		
		//TODO: when are we supposed to throw the TransitionDefinitionException?
		
		return new MachineState(propnet.getNextState(moves));
	}
	
	
	public void setState(MachineState state){
		propnet.setState(state.getContents());
	}

	/**
	 * Sets the action propositions without changing the current state.
	 * @param jointMove
	 */
	public void setActions(List<Move> jointMove){
		propnet.setActions(jointMove);
	}
	
	
	Random random = new Random();
	
	public void setRandomActions() throws MoveDefinitionException{
		
		List<jProposition> jointMove = new ArrayList<jProposition>();
		
		for(Role role : roles){
			
			List<Move> legalMoves = this.getLegalMoves(role);
			int randomIndex = random.nextInt(legalMoves.size());
			PropnetMove randomMove = (PropnetMove) legalMoves.get(randomIndex);
			
			
			jointMove.add(randomMove.getPropnetComponent());
			
		}
		
		propnet.setActionPropositions(jointMove);
	}
	
	
	public void setNextStateAsCurrentState(){
		this.propnet.setNextStateAsCurrentState();
	}
	
	public MachineState getCurrentState(){
		return new MachineState(propnet.getCurrentState());
	}
	
	
    public int[] performDepthCharge(MachineState state, MoveCollector moveSelection, Heuristics heuristicsObject) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
    	propnet.setState(state.getContents());
    	
    	return performDepthCharge(moveSelection, heuristicsObject);
    }
	
    
    /**
     * Performs a depth charge from the current state and fills the given list with all the states it encounters.<br/>
     * Make sure current state of the StateMachine is set with the state from which you want the depth charge to start!
     * @param states
     * @return
     * @throws GoalDefinitionException 
     */
    public int[] performDepthCharge(List<MachineState> states) throws GoalDefinitionException{
    	
    	states.add(this.getCurrentState());
    	
    	while( ! this.isTerminal()) {
    		
    		JointMove jointMove = this.getRandomJointMove((Heuristics)null);
    		propnet.setActions(jointMove);
    		
        	this.setNextStateAsCurrentState();
        	
        	if(states != null){
        		states.add(this.getCurrentState());
        	}

        }
    	
    	return this.getGoals();
    }
    
    public int[] performDepthCharge(MoveCollector moveSelection, Heuristics heuristicsObject) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        
    	while( ! this.isTerminal()) {
        	
    		
    		if(heuristicsObject != null && heuristicsObject.appliesEarlyCutoff){
    			MachineState currentState = this.getCurrentState();
    			int[] goals = heuristicsObject.testEarlyCutOff(currentState, moveSelection);
    			if(goals != null){
    				return goals;
    			}
    		}
    		
    		JointMove jointMove = this.getRandomJointMove(heuristicsObject);
    		propnet.setActions(jointMove);
    		
        	//this.setActions(jointMove);
        	this.setNextStateAsCurrentState();
        	
        	if(moveSelection != null){
        		moveSelection.add(jointMove);
        	}
        	

        }
    	
        /*return this.getCurrentState();*/
		return this.getGoals();
    }
    
    
   public JointMove getRandomJointMove(Heuristics heuristicsObject){
	   
	   int numPlayers = this.getRoles().size();
       
	   JointMove jointMove = new JointMove(numPlayers);
        for (int roleIndex = 0; roleIndex<numPlayers; roleIndex++) {
        	
        	try {
        		
        		List<Move> legalMoves = this.getLegalMoves(this.getRoles().get(roleIndex));
        		Move randomMove;
        		
        		if(heuristicsObject != null){
					int moveIndex = heuristicsObject.getRandomMoveIndexInRollOut(roleIndex, legalMoves);
					randomMove = legalMoves.get(moveIndex);
				}else{
					randomMove = (Move) Utils.getRandomObjectFromList(legalMoves);
				}
				
				jointMove.set(roleIndex, randomMove);
				
			} catch (MoveDefinitionException e) {
				e.printStackTrace();
			}
        	
        	
        }

        return jointMove;
    }
	
   ArrayList<GdlSentence> groundedBasePropositions = null;
   
	public ArrayList<GdlSentence> getGroundedBasePropositions(){
		
		if(groundedBasePropositions == null){
			
			groundedBasePropositions = new ArrayList<GdlSentence>(propnet.basePropositions.size());
			
			for(jProposition prop : propnet.basePropositions){
				groundedBasePropositions.add(prop.gdlSentence);
			}
		}
		
		return groundedBasePropositions;
	}
	
	ArrayList<GdlSentence> groundedDoesPropositions = null;
	
	public ArrayList<GdlSentence> getGroundedDoesPropositions(){
		
		if(groundedDoesPropositions == null){
			
			groundedDoesPropositions = new ArrayList<GdlSentence>();
			
			for(int i=0; i<this.getRoles().size(); i++){
				for(jProposition prop : propnet.roleIndex2doesPropositions.get(i)){
					groundedDoesPropositions.add(prop.gdlSentence);
				}
				
			}
			
		}
		
		
		return groundedDoesPropositions;
	}
}
