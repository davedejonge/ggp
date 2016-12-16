package ddejonge.ggp.sat.mutex;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.Role;

import ddejonge.ggp.prover.mutex.MutexCandidate;
import ddejonge.ggp.prover.mutex.MutexProver;
import ddejonge.ggp.sat.GDL2SATConverter;
import ddejonge.ggp.sat.SATDescription;
import ddejonge.ggp.sat.SATProver;
import ddejonge.ggp.sat.SATUtils;
import ddejonge.ggp.sat.logic.CNF;
import ddejonge.ggp.sat.logic.Clause;
import ddejonge.ggp.sat.logic.DNF;
import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.sat.logic.SimpleConjunction;
import ddejonge.ggp.sat.logic.XOR;
import ddejonge.ggp.tools.dataStructures.UnionCollection;

public class SATMutexProver implements MutexProver{

	//STATIC FIELDS

	//FIELDS
	SATProver satProver; //Note: it is better to have a field of type SATProver rather than to extend the SATProver class. This is so that other provers may use the same SATProver object.
	SATDescription satDescription;
	private int numProverCalled = 0;
	
	
	//CONSTRUCTORS
	public SATMutexProver(List<GdlRule> groundedRules, ArrayList<GdlSentence> groundedBasePropositions, ArrayList<GdlSentence> groundedDoesPropositions, List<Role> roles) {
		SATDescription satDescription = new SATDescription(groundedRules, roles);
		satProver = new SATProver(satDescription);
	}

	//METHODS
	
	@Override
	public boolean proveMutex(List<MutexCandidate> allCandidates, MutexCandidate candidateMutex, boolean proveStrong) {
		
		
		//1. Create the SAT rules that are necessary to exclude mutexes already found.
		List<Clause> knownMutexRules = getKnownMutexRules(satProver, allCandidates, candidateMutex); 
		
		
		// In ASP, we would use the following rules:
		//
		// hyp :- 0 { true(cell(1,1,Y2)) : domY2(Y2) } 1.
		// :- not hyp.
		// t :- 0 { next(cell(1,1,Y2)) : domY2(Y2) } 1.
		// :- t.
		
		//In SAT this becomes:
		// xor(  true(cell(1,1,X)) ,  true(cell(1,1,O))  , true(cell(1,1,O)) )
		// not xor (   next(cell(1,1,X)) ,  next(cell(1,1,O))  , next(cell(1,1,O)) )
		
		//We are testing: if in the current round no more than 1 of the values is true, is it possible that in the next round
		// more than one value is true?
		
		//2. Create list of clauses stating that the candidateMutex is satisfied in the current round.
		// 2a. first convert the candidate mutex to a DNF 
		DNF currentRoundDNF = getMutexDNF(candidateMutex, false);
		CNF currentRoundCNF = SATUtils.dnf2cnf(currentRoundDNF, satDescription.getPropositionStorage());
		
		//3. Create list of clauses stating that the candidateMutex is not satisfied in the next round
		DNF nextRoundDNF = getMutexDNF(candidateMutex, true);
		CNF nextRoundCNF = SATUtils.negatedDnf2cnf(nextRoundDNF);
		
		UnionCollection<Clause> clausesToProve = new UnionCollection<>();
		clausesToProve.addAll(knownMutexRules);
		clausesToProve.addAll(currentRoundCNF);
		clausesToProve.addAll(nextRoundCNF);
		
		clausesToProve.addAll(satDescription.getAllRulesAndRestrictions());
		
		int numVars = satDescription.getNumPropositions();
		
		numProverCalled++;
		Boolean satisfiable = SATProver.isSatisfiable(clausesToProve, numVars);
		
		//if SAT4J failed, then return false, because indeed nothing has been proved.
		if(satisfiable == null){
			return false;
		}
		
		boolean proofSuccessful = false;
		
		if(satisfiable != null && !satisfiable){
			candidateMutex.isMutex = true;
			if(proveStrong){
				candidateMutex.isStrongMutex = true;
			}
			proofSuccessful = true;
		}
		//NOTE: if the result is negative, this is not yet a proof that the candidate is not a mutex.
		
		return proofSuccessful;
			
		
	}

	private List<Clause> getKnownMutexRules(SATProver satDescription, List<MutexCandidate> allCandidates, MutexCandidate candidateMutex) {
		
		
		ArrayList<Clause> knownMutexRules = new ArrayList<Clause>();
		
		if(allCandidates == null){
			return knownMutexRules;
		}
		
		
		//for each already known mutex, add the restriction that none of them can be violated.
		for(MutexCandidate otherMutex : allCandidates){
			
			//skip the mutex we are currently trying to prove.
			if(otherMutex == candidateMutex){
				continue;
			}
			
			//Skip those candidates for which we know that they are not a mutex, or for which we don't know it yet.
			if(otherMutex.isMutex == null || !otherMutex.isMutex){
				continue;
			}
			
			List<Clause> mutexClauses = getMutexClauses(otherMutex);
			knownMutexRules.addAll(mutexClauses);
		}
		
		return knownMutexRules;
		
	}
	
	
	List<Clause> getMutexClauses(MutexCandidate candidate){
		
		DNF mutexDNF = getMutexDNF(candidate, false);
		
		CNF mutexCNF = SATUtils.dnf2cnf(mutexDNF, satDescription.getPropositionStorage());
		
		List<Clause> mutexClauses = new ArrayList<>();
		mutexClauses.addAll(mutexCNF);
		
		return mutexClauses;
	}
	
	public DNF getMutexDNF(MutexCandidate candidate, boolean replaceTrueWithNext){
		
		List<Proposition> candidatePropositions = new ArrayList<>(candidate.getValues().size());
		for(GdlSentence valueSentence : candidate.getValues()){
			Proposition prop = GDL2SATConverter.toSAT(satDescription, valueSentence);
			if(replaceTrueWithNext){
				prop = satDescription.trueProp2NextProp.get(prop);
			}
			candidatePropositions.add(prop);
		}
		
		XOR xor = new XOR(candidatePropositions);
		DNF mutexDNF = SATUtils.xor2dnf(xor);
		
		/*
		for(GdlSentence valueSentence : candidate.getValues()){
			
			Proposition trueProp = satProver.gdlSentence2proposition(valueSentence);
			
			Proposition prop = trueProp;
			if(replaceTrueWithNext){
				prop = satProver.trueProp2NextProp.get(trueProp);
			}
			
			//Create a conjunction in which the current proposition is true, and all other propositions of this candidate are false.
			SimpleConjunction conjunction = new SimpleConjunction();
			
			//add the current proposition as a positive one.
			conjunction.addAtom(true, prop);
			
			//add all other propositions as a negative one.
			for(GdlSentence valueSentence2 : candidate.getValues()){
				if(valueSentence == valueSentence2){
					continue;
				}
				
				Proposition prop2 = satProver.gdlSentence2proposition(valueSentence2);
				if(replaceTrueWithNext){
					prop2 = satProver.trueProp2NextProp.get(prop2);
				}
				
				conjunction.addAtom(false, prop2);
			}
			
			mutexDNF.addConjunction(conjunction);
		}*/
		
		
		//If the mutex may be a weak mutex, then also add a conjunction to represent the possibility that all atoms are false.
		if(candidate.isStrongMutex == null || !candidate.isStrongMutex){
			
			SimpleConjunction conjunction = new SimpleConjunction();
			
			for(Proposition prop : candidatePropositions){
				conjunction.addLiteral(prop, false);
			}
			
			/*
			for(GdlSentence valueSentence : candidate.getValues()){
				Proposition falseProp = satProver.gdlSentence2proposition(valueSentence);
				if(replaceTrueWithNext){
					falseProp = satProver.trueProp2NextProp.get(falseProp);
				}
				conjunction.addAtom(false, falseProp);
			}*/
			
			mutexDNF.addConjunction(conjunction);
		}
		
		return mutexDNF;
	}


	//GETTERS AND SETTERS
	
	public int getNumProverCalled(){
		return numProverCalled;
	}
}
