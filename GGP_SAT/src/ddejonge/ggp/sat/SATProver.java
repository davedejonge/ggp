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
import ddejonge.ggp.tools.dataStructures.UnionCollection;

public class SATProver {
	
	
	//Possible improvements:
	// - in isSatisfiable() we are creating a new Solver every time and copying all the clauses to that solver.
	//   maybe this can be done more efficiently.
	// - addToClauses(List<Move> moves, List<Clause> clauses) can be highly optimized.
	// - maybe addToClauses(MachineState state, List<Clause> clauses) as well.
	
	
	
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
	private List<Clause> negatedHypothesisAsClauseList = new ArrayList<>(1); //a singleton list that represents the NEGATION of the hypothesis.
	private List<Clause> stateAsClauses = new ArrayList<>();
	private List<Clause> movesAsClauses = new ArrayList<>();
	
	
	//The collection of clauses to feed to the prover.
	private Collection<Clause> clausesToProve = new UnionCollection<>();
	
	//CONSTRUCTORS
	public SATProver(List<GdlRule> groundedDescription, List<GdlSentence> groundedBasePropositions, List<GdlSentence> groundedDoesPropositions, List<Role> roles){
		satDescription = new SATDescription(groundedDescription, roles);
	}
	
	public SATProver(SATDescription satDescription){
		this.satDescription = satDescription;
	}

	
	
	
	
	//METHODS
	
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
	
	
	
	private void setState(MachineState state){
		
		//If the given state is null then we use the previously set state.
		if(state == null){
			
			//if both the given state, and the previous state are null, then something is wrong.
			if(this.state == null){
				throw new RuntimeException("SATProver.proveStateProperty() Error! state is not given!");
			}
			
			return;
		}
		
		//set the state.
		this.state = state;
		this.stateAsClauses.clear();
		addToClauses(state, stateAsClauses);
	}
	
	void setMoves(List<Move> moves){
		
		//If the given moves are null, then use the moves that were already set.
		if(moves == null){
			
			if(this.moves == null || this.moves.isEmpty()){
				throw new RuntimeException("SATProver.setMoves() Error! moves are not given!");
			}
			
			return;
		}
		
		this.moves.clear();
		this.moves.addAll(moves);
		
		//first clear the list of moves as clauses.
		movesAsClauses.clear();
		
		//then fill it again with the new moves.
		addToClauses(moves, movesAsClauses);
	}
	
	private void setHypothesis(GdlSentence hypothesis){
		
		//if both the given hypothesis, and the previous hypothesis are null, then something is wrong.
		if(hypothesis == null && this.hypothesis == null){
			throw new RuntimeException("SATProver.proveStateProperty() Error! hypothesis is not given!");
		}
		
		//If the given hypothesis is null, then use the hypothesis that was already set.
		if(hypothesis == null || hypothesis.equals(this.hypothesis)){
			return;
		}
		
		this.hypothesis = hypothesis;
		Proposition prop = GDL2SATConverter.toSAT(this.satDescription, hypothesis);
		Clause negatedHypothesis = new Clause(prop, false);
		
		this.negatedHypothesisAsClauseList.clear();
		this.negatedHypothesisAsClauseList.add(negatedHypothesis);
	}
	
	
	Boolean proveGeneralHypothesis(GdlSentence hypothesis){
		
		//Set the current hypothesis.
		setHypothesis(hypothesis);
		
		//Collect the rules of the game as clauses.
		clausesToProve.clear();
		clausesToProve.addAll(satDescription.getGeneralRulesAndRestrictions());
		
		//Add the hypothesis as clause.
		clausesToProve.addAll(negatedHypothesisAsClauseList);
		
		//Now check if the list of clauses is satisfiable.
		Boolean isSatisfiable = isSatisfiable(clausesToProve, satDescription.propositionStorage.size());
		
		if(isSatisfiable == null){
			return null;
		}
		
		return !isSatisfiable;
	}
	
	
	Boolean proveStateProperty(MachineState state, GdlSentence hypothesis){
		
		//Set the current state.
		setState(state);
		
		//Set the current hypothesis.
		setHypothesis(hypothesis);
		
		//Collect the rules of the game as clauses.
		clausesToProve.clear();
		clausesToProve.addAll(satDescription.getGeneralRulesAndRestrictions());
		
		//Add the state and the hypothesis as clauses.
		clausesToProve.addAll(stateAsClauses);
		clausesToProve.addAll(negatedHypothesisAsClauseList);
		
		//Now check if the list of clauses is satisfiable.
		Boolean isSatisfiable = isSatisfiable(clausesToProve, satDescription.propositionStorage.size());
		
		if(isSatisfiable == null){
			return null;
		}
		
		return !isSatisfiable;
	}
	
	Boolean proveStateActionProperty(MachineState state, List<Move> moves, GdlSentence hypothesis){
		
		//Set the current state.
		setState(state);
		
		//Set the moves
		setMoves(moves);
		
		//Set the current hypothesis.
		setHypothesis(hypothesis);
		
		//Collect the rules of the game as clauses.
		clausesToProve.clear();
		clausesToProve.addAll(satDescription.getAllRulesAndRestrictions());
		
		//Add the state and the hypothesis as clauses.
		clausesToProve.addAll(stateAsClauses);
		clausesToProve.addAll(negatedHypothesisAsClauseList);
		clausesToProve.addAll(movesAsClauses);
		
		//Now check if the list of clauses is satisfiable.
		Boolean isSatisfiable = isSatisfiable(clausesToProve, satDescription.propositionStorage.size());
		
		if(isSatisfiable == null){
			return null;
		}
		
		return !isSatisfiable;
	}
	
	
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
