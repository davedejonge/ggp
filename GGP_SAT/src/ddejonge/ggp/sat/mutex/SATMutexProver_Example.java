package ddejonge.ggp.sat.mutex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.propnet.factory.flattener.PropNetFlattener;
import org.ggp.base.util.statemachine.Role;

import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.prover.mutex.MutexCandidate;
import ddejonge.ggp.prover.mutex.MutexDetector_OLD;
import ddejonge.ggp.prover.mutex.MutexDetector;
import ddejonge.ggp.prover.mutex.MutexProver;
import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;
import ddejonge.ggp.tools.logic.DistinctRemover;
import ddejonge.ggp.tools.logic.Mutex;

public class SATMutexProver_Example {
	
	public static void main(String[] args) {
		
		//load some game...
		//List<Gdl> rules = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\breakthrough\\breakthrough.kif");
		//List<Gdl> rules = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\games\\cephalopodMicro\\cephalopodMicro.kif");
		List<Gdl> rules = GameParser.file2rules(SystemInfo.GAMES_FOLDER + "ticTacToe\\ticTacToe.kif");
		//List<Gdl> rules = GameParser.file2rules(SystemInfo.GAMES_FOLDER + "connectFour\\connectFour.kif");
		
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
		
		
		SATMutexProver prover = new SATMutexProver(groundedRules, stateMachine.getGroundedBasePropositions(), stateMachine.getGroundedDoesPropositions(), roles);
		
		System.out.println("Determining candidates heuristically...");
		long l1 = System.currentTimeMillis();
		Set<GdlSentence> allGroundedBasePropositions = new HashSet<>(stateMachine.getGroundedBasePropositions());
		List<MutexCandidate> mutexCandidates = MutexDetector.getMutexCandidates(stateMachine, allGroundedBasePropositions, false, true, System.currentTimeMillis() + 4_000);
		long l2 = System.currentTimeMillis();
		
		int numCandidatesFound = mutexCandidates.size();
		
		System.out.println("proving...");
		//Use the given prover to see if any of the candidates really represents a mutex.
		List<MutexCandidate> provedMutexCandidates = MutexDetector.detectMutexesFormally(prover, mutexCandidates);
		long l3 = System.currentTimeMillis();
		
		//Convert to mutex objects.
		List<Mutex> mutexes = MutexDetector.convertToMutexObjects(provedMutexCandidates, stateMachine.getGroundedBasePropositions());
		
		
		System.out.println();
		if(mutexes.size() > 0){
			System.out.println("Mutex objects:");
			for(Mutex mutex : mutexes){
				System.out.println(mutex.getValues());
			}
		}
		
		System.out.println();
		System.out.println("Number of times the prover was called: " + prover.getNumProverCalled());
		System.out.println("Number of candidates found: " + numCandidatesFound);
		System.out.println("Number of mutexes found: " + mutexes.size());
		System.out.println("Finding candidates heuristically: " + (l2 - l1) + " ms.");
		//System.out.println("Converting to ASP program: " + ASPMutexProver.sw1.getTimeString());
		System.out.println("Proving: " + (l3 - l2) + " ms.");
		System.out.println("Total time: " + (l3 - l1) + " ms.");
		
	}

}
