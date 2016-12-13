package ddejonge.ggp.sat.mutex;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.propnet.factory.flattener.PropNetFlattener;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.prover.mutex.MutexCandidate;
import ddejonge.ggp.prover.mutex.MutexProver;
import ddejonge.ggp.sat.logic.Clause;
import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;
import ddejonge.ggp.tools.logic.DistinctRemover;

public class Test {

	public static void main(String[] args) throws GdlFormatException, SymbolFormatException {
		
		//List<Gdl> rules = GameParser.file2rules(SystemInfo.GAMES_FOLDER + "connectFour\\connectFour.kif");
		List<Gdl> rules = GameParser.file2rules(SystemInfo.GAMES_FOLDER + "ticTacToe\\ticTacToe.kif");
		//List<Gdl> rules = GameParser.file2rules("testGame\\testGame.kif");
		
		//Generate a PropnetStateMachine
		PropnetStateMachine stateMachine = new PropnetStateMachine();
		stateMachine.initialize(rules);
		
		List<Role> roles = stateMachine.getRoles();
		
		//Remove any ORs
		rules = DeORer.run(rules);
		
		//Get grounded rules.
		PropNetFlattener flattener = new PropNetFlattener(rules);
		List<GdlRule> groundedRules = flattener.flatten();
		
		//Remove any Distincts.
		groundedRules = DistinctRemover.removeDistincts(groundedRules);
		
		
		System.out.println();
		for (GdlRule gdlRule : groundedRules) {
			System.out.println(gdlRule);
		}
		System.out.println();
		
		SATMutexProver satMutexProver = new SATMutexProver(groundedRules, stateMachine.getGroundedBasePropositions(), stateMachine.getGroundedDoesPropositions(), roles);

		List<MutexCandidate> mutexCandidates = new ArrayList<>();
		GdlRelation representant = (GdlRelation)GdlFactory.create("(true (control ?Y0))");
		MutexCandidate candidate = new MutexCandidate(representant, stateMachine.getGroundedBasePropositions());
		mutexCandidates.add(candidate);
		
		satMutexProver.proveMutex(mutexCandidates, candidate, true);
		
		System.out.println("weak mutex: " + candidate.isMutex);
		System.out.println("strong mutex: " + candidate.isStrongMutex);
		/*
		//let's prove: if the cell is currently blank, then in the next state it can't be blank as well.
		GdlRelation control_x = (GdlRelation)GdlFactory.create("(true (control xplayer))");
		GdlRelation control_o = (GdlRelation)GdlFactory.create("(true (control oplayer))");
		
		GdlRelation true_x = (GdlRelation)GdlFactory.create("(true (cell 1 1 x))");
		GdlRelation next_o = (GdlRelation)GdlFactory.create("(next (cell 1 1 o))");
		
		Proposition prop_control_x = satMutexProver.satProver.getSatDescription().gdlSentence2proposition(control_x);
		Proposition prop_control_o = satMutexProver.satProver.getSatDescription().gdlSentence2proposition(control_o);

		Proposition prop_true_blank = satMutexProver.satProver.getSatDescription().gdlSentence2proposition(true_x);
		Proposition prop_next_blank = satMutexProver.satProver.getSatDescription().gdlSentence2proposition(next_o);
		
		ArrayList<Clause> clausesToProve = new ArrayList<>();
		clausesToProve.add(new Clause(prop_true_blank, true));
		clausesToProve.add(new Clause(prop_next_blank, true));
		clausesToProve.add(new Clause(prop_control_x, true));
		clausesToProve.add(new Clause(prop_control_o, false));
		
		boolean satisfiable = satMutexProver.satProver.isSatisfiable(clausesToProve);
		
		System.out.println(satisfiable);*/
		
		/*
		GdlConstant one = GdlPool.getConstant("1");
		GdlVariable y1 = GdlPool.getVariable("?Y1");
		GdlConstant black = GdlPool.getConstant("black");
		GdlFunction cell = GdlPool.getFunction(GdlPool.getConstant("cell"), new GdlTerm[]{one, y1, black});
		GdlSentence representant = GdlPool.getRelation(GdlPool.TRUE, new GdlTerm[]{cell});

		MutexCandidate mc = new MutexCandidate(representant, stateMachine.getGroundedBasePropositions());
		
		prover.proveMutex(new ArrayList<MutexCandidate>(), mc, false);*/

	}

	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
