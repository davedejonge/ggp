package ddejonge.ggp.sat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import ddejonge.ggp.sat.logic.CNF;
import ddejonge.ggp.sat.logic.Clause;
import ddejonge.ggp.sat.logic.DNF;
import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.sat.logic.SATFormula;
import ddejonge.ggp.sat.logic.SimpleConjunction;
import ddejonge.ggp.sat.logic.XOR;
import ddejonge.ggp.tools.dataStructures.ArraySet;

public class SATProver_OLD {

	//STATIC FIELDS

	//FIELDS
	private SATDescription satDescription;
	
	//NOTE: the fact that we keep the hypothesis, the state, and the moves as fields in this class, (rather than only use them as local parameters of the methods),
	// allows us to prove several hypotheses for the same state and moves, without having to re-convert the state and moves to Clauses every time.

	/**Some hypothesis you would like to prove.*/
	private GdlSentence hypothesis; //
	
	/** The game state for which you want to prove the hypothesis.*/
	private MachineState state;
	
	/** The moves of the players for which you want to prove the hypothesis.*/
	private List<Move> moves = new ArrayList<>(4);	
	
	
	//These are the hypothesis, the state, and the moves, translated to Clauses:
	private Clause hypothesisAsClause; //represents the NEGATION of the hypothesis.
	private List<Clause> stateAsClauses = new ArrayList<>();
	private List<Clause> movesAsClauses = new ArrayList<>();
	
	//these values are reset to false after every prove.
	// in order to call prove() again, you first must set the state, moves, and hypothesis again.
	private boolean stateSet = false;
	private boolean movesSet = false;
	private boolean hypothesisSet = false;
	
	//CONSTRUCTORS
	public SATProver_OLD(List<GdlRule> groundedDescription, List<GdlSentence> groundedBasePropositions, List<GdlSentence> groundedDoesPropositions, List<Role> roles){
		satDescription = new SATDescription(groundedDescription, roles);
	}
	
	public SATProver_OLD(SATDescription satDescription){
		this.satDescription = satDescription;
	}

	
	
	
	
	//METHODS
	void setState(MachineState state){
		
		stateSet = true;
		
		//If the given state is null, then use the state that was already set.
		if(this.state == state){
			return;
		}
		
		this.state = state;
		this.stateAsClauses.clear();
		addToClauses(state, stateAsClauses);
	}
	
	void keepState(){
		stateSet = true;
	}
	
	void clearState(){
		stateSet = true;
		state = null;
		stateAsClauses.clear();
	}
	
	void setMoves(List<Move> moves){
		
		movesSet = true;
		
		//If the given moves are null, then use the moves that were already set.
		if(moves == null || this.moves.equals(moves)){
			return;
		}
		
		this.moves.clear();
		this.moves.addAll(moves);
		
		//first clear the list of moves as clauses.
		movesAsClauses.clear();
		
		//then fill it again with the new moves.
		addToClauses(moves, movesAsClauses);
	}
	
	void clearMoves(){
		movesSet = true;
		moves.clear();
		movesAsClauses.clear();
		
		adsfj;
		
		//THIs is causing problems!
		//If moves are irrelevant we simply clear the moves. However, there is also a rule that says that
		// every player must always make exactly one move. Therefore, the set of clauses is always unsatisfiable, and every proof will return true.
		
	}
	
	void keepMoves(){
		movesSet = true;
	}
	
	void setHypothesis(GdlSentence hypothesis){
		
		hypothesisSet = true;
		
		//If the given hypothesis is null, then use the hypothesis that was already set.
		if(hypothesis == null || hypothesis == this.hypothesis){
			return;
		}
		
		this.hypothesis = hypothesis;
		Proposition prop = GDL2SATConverter.toSAT(this.satDescription, hypothesis);
		this.hypothesisAsClause = new Clause(prop, false);
	}
	
	void keepHypothesis(){
		hypothesisSet = true;
	}
	
	
	/** 
	 * Converts the given state into a list of clauses and adds them to the given list.
	 * @param state
	 * @param clauses
	 */
	private void addToClauses(MachineState state, List<Clause> clauses){
		
		if(state == null){
			return;
		}
		
		for(Proposition trueProp: satDescription.getPropositionsOfType(GdlPool.TRUE)){
			
			Clause clause;
			if(state.getContents().contains(trueProp.getGdlSentence())){
				clause = new Clause(trueProp, true);
			}else{
				clause = new Clause(trueProp, false);
			}
			
			clauses.add(clause);
		}
	}
	
	/**
	 * Converts the given moves into clause objects and adds them to the list of clauses.
	 * @param moves
	 * @param clauses
	 */
	private void addToClauses(List<Move> moves, List<Clause> clauses){
		
		if(moves == null){
			return;
		}
		
		//convert to does propositions
		for(int i=0; i<moves.size(); i++){
			
			Role role = satDescription.roles.get(i);
			GdlConstant roleConstant = role.getName();
			
			Move move = moves.get(i);
			GdlTerm moveTerm = move.getContents();
			
			GdlRelation doesRelation = GdlPool.getRelation(GdlPool.DOES, new GdlTerm[]{roleConstant, moveTerm});
			
			Proposition doesProposition = GDL2SATConverter.toSAT(satDescription, doesRelation);
			
			Clause clause = new Clause(doesProposition, true);
			clauses.add(clause);
		}
		
	}
	
	
	private void addToClauses(GdlSentence hypothesis, List<Clause> clauses){
		Proposition hyp = GDL2SATConverter.toSAT(satDescription, hypothesis);
		Clause clause = new Clause(hyp, false);
		clauses.add(clause);
	}
	
	
	
	/**
	 * Returns true if the current hypothesis can be proved in the current state and assuming the players make the current moves. <br/>
	 * Returns false if the given hypothesis is proved to be false, and returns null if we can neither prove nor disprove it. <br/>
	 * <br/>
	 * State and moves are allowed to be null in case they are not relevant.
	 * @param state
	 * @param moves
	 * @param hypothesis
	 * @return
	 */
	public Boolean prove(){
		
		//Check that the caller has properly set everything.
		if(!stateSet){
			throw new RuntimeException("SATProver.prove() Error! state has not been set. Always call either setState() or keepState() or clearState() before calling prove().");
		}
		if(!movesSet){
			throw new RuntimeException("SATProver.prove() Error! moves have not been set. Always call either setMoves() or keepMoves() or clearMoves() before calling prove().");
		}
		if(!hypothesisSet){
			throw new RuntimeException("SATProver.prove() Error! hypothesis has not been set. Always call either setHypothesis or keepHypothesis before calling prove().");
		}
		
		//set these flags back to false so that we force the user to reset the state, moves, and hypothesis the next time.
		stateSet = false;
		movesSet = false;
		hypothesisSet = false;
		
		ArrayList<Clause> allClauses = new ArrayList<>();
		allClauses.addAll(satDescription.gameRules);
		allClauses.addAll(satDescription.legalRestrictions);
		allClauses.addAll(satDescription.actionRestrictions);
		allClauses.addAll(satDescription.nonProduceableRestrictions);
		
		allClauses.addAll(stateAsClauses);
		allClauses.addAll(movesAsClauses);
		allClauses.add(hypothesisAsClause);
		
		//TODO: remove debug code:
		/*
		System.out.println("SATProver.prove() check Printing goal clauses!");
		for(Clause clause : allClauses){
			for(Proposition prop : clause.getPositiveAtoms()){
				if(prop.getGdlSentence().getName().equals(GdlPool.GOAL)){
					System.out.println(clause);
					break;
				}
			}
			for(Proposition prop : clause.getNegativeAtoms()){
				if(prop.getGdlSentence().getName().equals(GdlPool.GOAL)){
					System.out.println(clause);
					break;
				}
			}
		}*/
		
		//Now check if the list of clauses is satisfiable.
		Boolean isSatisfiable = isSatisfiable(allClauses, satDescription.propositionStorage.size());
		
		if(isSatisfiable == null){
			return null;
		}
		
		return !isSatisfiable;
	}
	
	
	/**
	 * Tests whether the given list of clauses is satisfiable in combination with the SATDescription of this prover.
	 * @param clauses
	 * @return
	 */
	/*public Boolean isSatisfiable(List<Clause> clauses){
		
		ArrayList<Clause> allClauses = new ArrayList<>();
		allClauses.addAll(satDescription.gameRules);
		allClauses.addAll(satDescription.legalRestrictions);
		allClauses.addAll(satDescription.actionRestrictions);
		allClauses.addAll(satDescription.nonProduceableRestrictions);
		allClauses.addAll(clauses);
		
		return isSatisfiable(allClauses, satDescription.propositionStorage.size());
	}*/
	
	/**
	 * Returns null if it fails to either prove or disprove satisfiability.
	 * @param allClauses
	 * @param numVars
	 * @return
	 */
	public static Boolean isSatisfiable(Iterable<Clause> allClauses, int numVars){
		
		ISolver solver = SolverFactory.newDefault();
		solver.newVar(numVars);
		
		//1. Define the problem.
		try {
		
			for (Clause clause : allClauses) {
				solver.addClause(clause.getVecInt());
			}
			
		} catch (ContradictionException e) {
			return false;
		}
		
		//2. Solve the problem.
		
		//Switch to Problem interface
		IProblem problem = solver;
		try {
			
			return problem.isSatisfiable();
			
		} catch (TimeoutException e) {
			return null;
		}
		
	}

	public static Boolean _isSatisfiable(List<? extends SATFormula> formulas, int numVars){
		
		
		ISolver solver = SolverFactory.newDefault();
		solver.newVar(numVars);
		
		//1. Define the problem.
		try {
		
			for (SATFormula formula : formulas) {
				
				if(formula instanceof Clause){
					
					solver.addClause(((Clause)formula).getVecInt());
				
				}else if(formula instanceof CNF){
					
					for(Clause clause : (CNF)formula){
						solver.addClause(clause.getVecInt());
					}
				
				}else if(formula instanceof Proposition){
					
					solver.addClause(((Proposition)formula).getVecInt());
					
				}else if(formula instanceof SimpleConjunction){

					
					throw new RuntimeException("SATProver._isSatisfiable() Error! Cannot handle SimpleConjunctions");
					
				}else if(formula instanceof XOR){
					
					throw new RuntimeException("SATProver._isSatisfiable() Error! Cannot handle XORs");
					
				}else if (formula instanceof DNF){
					
					throw new RuntimeException("SATProver._isSatisfiable() Error! Cannot handle DNFs");
					
				}else{
					
					throw new RuntimeException("SATProver._isSatisfiable() Error! unknown class: " + formula.getClass().getSimpleName());
					
				}
				
			}
			
		} catch (ContradictionException e) {
			return false;
		}
		
		//2. Solve the problem.
		
		//Switch to Problem interface
		IProblem problem = solver;
		try {
			
			return problem.isSatisfiable();
			
		} catch (TimeoutException e) {
			return null;
		}
	}

	
	

	
	
	
	
	//GETTERS AND SETTERS
	public List<Clause> getStateClauses(){
		return Collections.unmodifiableList(this.stateAsClauses);
	}

	
}
