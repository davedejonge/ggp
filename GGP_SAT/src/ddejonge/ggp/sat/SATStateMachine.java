package ddejonge.ggp.sat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.propnet.factory.flattener.PropNetFlattener;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.sat4j.reader.GroupedCNFReader;

import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.tools.NotImplementedException;
import ddejonge.ggp.tools.dataStructures.ArraySet;
import ddejonge.ggp.tools.dataStructures.Pair;
import ddejonge.ggp.tools.logic.LogicUtils;

public class SATStateMachine extends StateMachine{
	
	
	//STATIC FIELDS

	//FIELDS
	SATDescription satDescription;
	SATProver satProver;
	List<Role> roles;	
	
	MachineState initialState = null;
	/*HashMap<String, GdlRelation[]> roles2goalPropositions = new HashMap<String, GdlRelation[]>();*/
	
	/**
	 * Maps each role to a list of GOAL propositions. Each GOAL proposition is represented by a pair, consisting
	 * of the GDLRelation and the goal value.
	 */
	HashMap<String, List<Pair<GdlRelation, Integer>>> roles2goalPropositions = new HashMap<>();
	
	MachineState currentState;
	
	
	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
	
	
	
	@Override
	public void initialize(List<Gdl> description) {
		
		roles = Role.computeRoles(description); //This method makes sure the roles are determined in the correct order!
		roles = Collections.unmodifiableList(roles);
		
		
		//1. ground the description.
		PropNetFlattener pf = new PropNetFlattener(description);
		List<GdlRule> flatDescription = pf.flatten();
		
		flatDescription = LogicUtils.removeOrs(flatDescription);
		flatDescription = LogicUtils.removeDistincts(flatDescription);
		
		
		Set<GdlSentence> allAtoms = SATUtils.extractAtoms(flatDescription);

		/*for(Role role : roles){
			roles2goalPropositions.put(role.toString(), new GdlRelation[101]);
		}*/
		
		//Fill the arrays with goal propositions.
		for (GdlSentence gdlSentence : allAtoms) {
			if(gdlSentence.getName().equals(GdlPool.GOAL)){
				
				GdlConstant roleConstant = (GdlConstant)gdlSentence.get(0);
				String roleName = roleConstant.toString();
				
				List<Pair<GdlRelation, Integer>> goalPropsOfRole = roles2goalPropositions.get(roleName);
				if(goalPropsOfRole == null){
					goalPropsOfRole = new ArrayList<>();
					roles2goalPropositions.put(roleName, goalPropsOfRole);
				}
				
				/*GdlRelation[] array = roles2goalPropositions.get(roleName);*/
				
				GdlConstant goalConstant = (GdlConstant) gdlSentence.get(1);
				int goal = Integer.parseInt(goalConstant.toString());
				
				goalPropsOfRole.add(new Pair<GdlRelation, Integer>((GdlRelation)gdlSentence, goal));
				
				/*array[goal] = (GdlRelation) gdlSentence;*/
			}
		}

		
		
		//2. convert the description into a set of clauses.
		satDescription = new SATDescription(flatDescription, roles);
		
		satProver = new SATProver(satDescription);
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		
		/*GdlSentence[] goalPropositions = roles2goalPropositions.get(role.toString());*/
		List<Pair<GdlRelation, Integer>> goalPropsOfRole = roles2goalPropositions.get(role.toString());
		
		int goalValue = -1;
		for(Pair<GdlRelation, Integer> goalPair : goalPropsOfRole){
			GdlRelation goalProp = goalPair.getLeft();
			
			Boolean result = satProver.proveStateProperty(state, goalProp);
			
			if(result != null && result){
				
				if(goalValue == -1){
					goalValue = goalPair.getRight();
				}else{
					//If the goal value had already been set before, then it means there are 2 different goal values for this state.
					System.out.println("Previous goal value: " + goalValue);
					System.out.println("New goal value: " + goalPair.getRight());
					throw new GoalDefinitionException(state, role);
				}
			}
		}
		
		
		/*
		for(int i=0; i<=100; i++){
			
			if(goalPropositions[i] == null){
				continue;
			}
			
			Boolean result = satProver.proveStateProperty(state, goalPropositions[i]);
			
			if(result != null && result){
				
				if(goalValue == -1){
					goalValue = i;
				}else{
					//If the goal value had already been set before, then it means there are 2 different goal values for this state.
					System.out.println("Previous goal value: " + goalValue);
					System.out.println("New goal value: " + i);
					throw new GoalDefinitionException(state, role);
				}
			}
		}*/
		
		if(goalValue == -1){
			throw new GoalDefinitionException(state, role);
		}
		
		return goalValue;
	}

	@Override
	public boolean isTerminal(MachineState state) {
		
		Boolean result = satProver.proveStateProperty(state, GdlPool.getProposition(GdlPool.TERMINAL));
		if(result == null){
			throw new RuntimeException("SATStateMachine.isTerminal() Error! isTerminal cannot be determined.");
		}
		
		return result;
	}
	
	


	@Override
	public List<Role> getRoles() {
		return roles;
	}

	@Override
	public MachineState getInitialState() {
		
		if(initialState == null){
		
			Set<GdlSentence> initiallyTrueSentences = new ArraySet<>();
			
			List<Proposition> initPropositions = this.satDescription.getPropositionsOfType(GdlPool.INIT);
			for (Proposition initProposition : initPropositions) {
				
				GdlSentence initSentence = initProposition.getGdlSentence();
				
				
				if(this.satProver.proveGeneralHypothesis(initSentence)){
					
					GdlSentence initiallyTrueSentence = GdlPool.getRelation(GdlPool.TRUE, initSentence.getBody());
					initiallyTrueSentences.add(initiallyTrueSentence);
				}
			}
			
			initialState = new MachineState(initiallyTrueSentences);
		}
		
		
		
		return initialState;
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException {
		
		List<Move> legalMoves = new ArrayList<>();
		List<Proposition> actionPropositionsOfrole = satDescription.roleNames2doesPropositions.get(role.toString());
		for(Proposition doesProp : actionPropositionsOfrole){
			
			//Convert actionProposition to legal proposition.
			Proposition legalProp = satDescription.doesProp2legalProp.get(doesProp);
			
			if(satProver.proveStateProperty(state, legalProp.getGdlSentence())){
				GdlTerm action = legalProp.getGdlSentence().get(1);
				legalMoves.add(new Move(action));
			}
		}
		
		return legalMoves;
		
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {

		//I don't think we would ever call getNextState twice in a row with the same state and moves.
		//Also, I don't think we would ever call this method with a new state, but with the same moves.
		if(moves == null){
			throw new RuntimeException("SATStateMachine.getNextState() Error! moves should not be null!");
		}
		satProver.setMoves(moves);
		
		Set<GdlSentence> nextTrueSentences = new ArraySet<>();
		
		List<Proposition> nextPropositions = this.satDescription.getPropositionsOfType(GdlPool.NEXT);
		for (Proposition nextProposition : nextPropositions) {
			
			GdlSentence nextSentence = nextProposition.getGdlSentence();
			
			if(this.satProver.proveStateActionProperty(state, moves, nextSentence)){
				
				GdlSentence initiallyTrueSentence = GdlPool.getRelation(GdlPool.TRUE, nextSentence.getBody());
				nextTrueSentences.add(initiallyTrueSentence);
			}
		}
		
		//TODO: remove debug
		/*if(nextTrueSentences.isEmpty()){
			System.out.println("SATStateMachine.getNextState() check");
		}*/
		
		return new MachineState(nextTrueSentences);
		
	}
	
	

}
