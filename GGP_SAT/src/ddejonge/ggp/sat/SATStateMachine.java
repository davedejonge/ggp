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
import ddejonge.ggp.tools.logic.LogicUtils;

public class SATStateMachine extends StateMachine{
	
	
	//STATIC FIELDS

	//FIELDS
	SATDescription satDescription;
	SATProver satProver;
	List<Role> roles;	
	
	MachineState initialState = null;
	HashMap<String, GdlRelation[]> roles2goalPropositions = new HashMap<String, GdlRelation[]>();
	
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

		for(Role role : roles){
			roles2goalPropositions.put(role.toString(), new GdlRelation[101]);
		}
		
		//Fill the arrays with goal propositions.
		for (GdlSentence gdlSentence : allAtoms) {
			if(gdlSentence.getName().equals(GdlPool.GOAL)){
				
				GdlConstant roleConstant = (GdlConstant)gdlSentence.get(0);
				String roleName = roleConstant.toString();
				
				GdlRelation[] array = roles2goalPropositions.get(roleName);
				
				GdlConstant goalConstant = (GdlConstant) gdlSentence.get(1);
				int goal = Integer.parseInt(goalConstant.toString());
				
				array[goal] = (GdlRelation) gdlSentence;
			}
		}

		
		
		//2. convert the description into a set of clauses.
		satDescription = new SATDescription(flatDescription, roles);
		
		satProver = new SATProver(satDescription);
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		
		satProver.setState(state);
		
		GdlSentence[] goalPropositions = roles2goalPropositions.get(role.toString());
		
		int goalValue = -1;
		for(int i=0; i<=100; i++){
			
			satProver.setHypothesis(goalPropositions[i]);
			
			if(satProver.prove()){
				
				if(goalValue == -1){
					goalValue = i;
				}else{
					throw new GoalDefinitionException(state, role);
				}
			}
		}
		
		if(goalValue == -1){
			throw new GoalDefinitionException(state, role);
		}
		
		return goalValue;
	}

	@Override
	public boolean isTerminal(MachineState state) {
		
		satProver.setState(state);
		satProver.setHypothesis(GdlPool.getProposition(GdlPool.TERMINAL));
		return satProver.prove();
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
				
				satProver.setHypothesis(initSentence);
				
				if(this.satProver.prove()){
					
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
		
		satProver.setState(state);
		
		List<Move> legalMoves = new ArrayList<>();
		for(Proposition doesProp : satDescription.roleNames2doesPropositions.get(role)){
			
			satProver.setHypothesis(doesProp.getGdlSentence());
			
			if(satProver.prove()){
				GdlTerm action = doesProp.getGdlSentence().get(1);
				legalMoves.add(new Move(action));
			}
		}
		
		return legalMoves;
		
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {

		satProver.setState(state);
		satProver.setMoves(moves);
		
		Set<GdlSentence> nextTrueSentences = new ArraySet<>();
		
		List<Proposition> nextPropositions = this.satDescription.getPropositionsOfType(GdlPool.NEXT);
		for (Proposition nextProposition : nextPropositions) {
			
			GdlSentence nextSentence = nextProposition.getGdlSentence();
			
			satProver.setHypothesis(nextSentence);
			
			if(this.satProver.prove()){
				
				GdlSentence initiallyTrueSentence = GdlPool.getRelation(GdlPool.TRUE, nextSentence.getBody());
				nextTrueSentences.add(initiallyTrueSentence);
			}
		}
		
		return new MachineState(nextTrueSentences);
		
	}
	
	

}
