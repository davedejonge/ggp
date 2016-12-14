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
		
		if(state == null){
			satProver.keepState();
		}else{
			satProver.setState(state);
		}
		
		GdlSentence[] goalPropositions = roles2goalPropositions.get(role.toString());
		
		int goalValue = -1;
		for(int i=0; i<=100; i++){
			
			if(goalPropositions[i] == null){
				continue;
			}
			
			satProver.keepState();	 //state has already been set at the beginning of this method.
			satProver.clearMoves();  //moves are irrelevant for the goal.
			satProver.setHypothesis(goalPropositions[i]);
			
			Boolean result = satProver.prove();
			
			if(result != null && result){
				
				if(goalValue == -1){
					goalValue = i;
				}else{
					//If the goal value had already been set before, then it means there are 2 different goal values for this state.
					System.out.println("Previous goal value: " + goalValue);
					System.out.println("New goal value: " + i);
					
					/*System.out.println();
					System.out.println("state:");
					System.out.println(satProver.getStateClauses());
					System.out.println();*/
					
					GdlSentence true11X = GdlPool.getRelation(GdlPool.TRUE, new GdlTerm[] {GdlPool.getFunction(GdlPool.getConstant("cell"), new GdlTerm[]{GdlPool.getConstant("1"), GdlPool.getConstant("1"), GdlPool.getConstant("x")})});
					GdlSentence true11O = GdlPool.getRelation(GdlPool.TRUE, new GdlTerm[] {GdlPool.getFunction(GdlPool.getConstant("cell"), new GdlTerm[]{GdlPool.getConstant("1"), GdlPool.getConstant("1"), GdlPool.getConstant("o")})});
					GdlSentence true11b = GdlPool.getRelation(GdlPool.TRUE, new GdlTerm[] {GdlPool.getFunction(GdlPool.getConstant("cell"), new GdlTerm[]{GdlPool.getConstant("1"), GdlPool.getConstant("1"), GdlPool.getConstant("b")})});
					
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(true11X);
					System.out.println(true11X.toString() + ": " + satProver.prove());
					
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(true11O);
					System.out.println(true11O.toString() + ": " + satProver.prove());
					
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(true11b);
					System.out.println(true11b.toString() + ": " + satProver.prove());
					
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(GdlPool.getProposition(GdlPool.getConstant("open")));
					System.out.println("open: " + satProver.prove());
					
					GdlRelation lineX = GdlPool.getRelation(GdlPool.getConstant("line"), new GdlTerm[]{GdlPool.getConstant("x")});
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(lineX);
					System.out.println("lineX: " + satProver.prove());
					
					GdlRelation lineO = GdlPool.getRelation(GdlPool.getConstant("line"), new GdlTerm[]{GdlPool.getConstant("o")});
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(lineO);
					System.out.println("lineO: " + satProver.prove());
					
					for(int j=1; j<=3; j++){
							GdlRelation row = GdlPool.getRelation(GdlPool.getConstant("row"), new GdlTerm[]{GdlPool.getConstant(""+j), GdlPool.getConstant("x")});
							satProver.keepState();	 //state has already been set at the beginning of this method.
							satProver.clearMoves();  //moves are irrelevant for the goal.
							satProver.setHypothesis(row);
							System.out.println("row_" + j + "_X" + satProver.prove());
							
							GdlRelation col = GdlPool.getRelation(GdlPool.getConstant("column"), new GdlTerm[]{GdlPool.getConstant(""+j), GdlPool.getConstant("x")});
							satProver.keepState();	 //state has already been set at the beginning of this method.
							satProver.clearMoves();  //moves are irrelevant for the goal.
							satProver.setHypothesis(col);
							System.out.println("col_" + j + "_X" + satProver.prove());
					}
					
					GdlRelation diaX = GdlPool.getRelation(GdlPool.getConstant("diagonal"), new GdlTerm[]{GdlPool.getConstant("x")});
					satProver.keepState();	 //state has already been set at the beginning of this method.
					satProver.clearMoves();  //moves are irrelevant for the goal.
					satProver.setHypothesis(diaX);
					System.out.println("diaX" + satProver.prove());
					
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
		
		if(state == null){
			satProver.keepState();
		}else{
			satProver.setState(state);
		}
		satProver.clearMoves(); //moves are irrelevant.
		satProver.setHypothesis(GdlPool.getProposition(GdlPool.TERMINAL));
		
		Boolean result = satProver.prove();
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
				
				satProver.clearState(); //the current state of the prover is irrelevant.
				satProver.clearMoves(); //moves are clearly irrelevant.
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
		
		if(state == null){
			satProver.keepState();
		}else{
			satProver.setState(state);
		}
		
		List<Move> legalMoves = new ArrayList<>();
		List<Proposition> actionPropositionsOfrole = satDescription.roleNames2doesPropositions.get(role.toString());
		for(Proposition doesProp : actionPropositionsOfrole){
			
			//Convert actionProposition to legal proposition.
			Proposition legalProp = satDescription.doesProp2legalProp.get(doesProp);
			
			satProver.keepState(); //state has already been set at the beginning of this method.
			satProver.clearMoves(); //moves are irrelevant.
			satProver.setHypothesis(legalProp.getGdlSentence());
			
			if(satProver.prove()){
				GdlTerm action = legalProp.getGdlSentence().get(1);
				legalMoves.add(new Move(action));
			}
		}
		
		return legalMoves;
		
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException {

		if(state == null){
			satProver.keepState();
		}else{
			satProver.setState(state);
		}
		
		
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
			
			satProver.keepState(); //state has already been set at the beginning of this method.
			satProver.keepMoves(); //moves have already been set at the beginning of this method.
			satProver.setHypothesis(nextSentence);
			
			if(this.satProver.prove()){
				
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
