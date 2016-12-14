package ddejonge.ggp.sat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;

import ddejonge.ggp.sat.logic.CNF;
import ddejonge.ggp.sat.logic.Clause;
import ddejonge.ggp.sat.logic.DNF;
import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.sat.logic.SimpleConjunction;
import ddejonge.ggp.sat.logic.XOR;
import ddejonge.ggp.tools.logic.LogicUtils;

public class SATUtils {
	

	public static DNF xor2dnf(XOR xor){
		
		DNF dnf = new DNF();
		
		for(Proposition prop : xor){
			
			//Create a conjunction in which the current proposition is true, and all other propositions are false.
			SimpleConjunction conjunction = new SimpleConjunction();
			
			//add the current proposition as a positive one.
			conjunction.addLiteral(prop, true);
			
			//add all other propositions as a negative one.
			for(Proposition prop2 : xor){
				if(prop == prop2){
					continue;
				}
				
				conjunction.addLiteral(prop2, false);
			}
			
			dnf.addConjunction(conjunction);
		}
		
		return dnf;
	}
	
	/**
	 * PropositionStorage is necessary, because this method creates new Propositions.
	 * @param dnf
	 * @param propositions
	 * @return
	 */
	public static CNF dnf2cnf(DNF dnf, PropositionStorage propositions){
		
		//For each conjunction C_i in DNF, introduce a new propositional variable q_i
		// replace each literal p_ij in C_i by the disjunction ~q_i OR p_ij
		
		//take the conjunction of the disjunctions (~q_i OR p_ij) over all i,j 
		// conjunct this with the disjunction  (q_1 OR q_2 ... OR q_n) 
		
		CNF cnf = new CNF();
		Clause auxiliaryClause = new Clause();  //(q_1 OR q_2 ... OR q_n) 
		
		for (SimpleConjunction conjunction : dnf) {
			
			GdlProposition _q_i = GdlPool.getProposition(GdlPool.getConstant("Z"+propositions.size()));
			Proposition q_i = propositions.add(_q_i);
			auxiliaryClause.addPositiveLiteral(q_i);
			
			for(Proposition p_ij : conjunction.getPositiveAtoms()){
				Clause newClause = new Clause();
				newClause.addNegativeLiteral(q_i);
				newClause.addPositiveLiteral(p_ij);
				cnf.add(newClause);
			}
			for(Proposition p_ij : conjunction.getNegativeAtoms()){
				Clause newClause = new Clause();
				newClause.addNegativeLiteral(q_i);
				newClause.addNegativeLiteral(p_ij);
				cnf.add(newClause);
			}
		}
		cnf.add(auxiliaryClause);
		
		return cnf;
		
	}
	
	
	/**
	 * Given the CNF  {{a,b} , {c,d}}  and the literal p <br/>
	 * returns the CNF {{a,b,p} , {c,d,p}}
	 * @param cnf
	 * @param p
	 * @return
	 */
	static CNF disjunctCNFwithLiteral(CNF cnf, boolean positive, Proposition p){
		
		CNF newCnf = new CNF();
		for (Clause clause  : cnf) {
			
			Clause newClause = new Clause(clause.getPositiveAtoms(), clause.getNegativeAtoms());
			
			newClause.addLiteral(p, positive);
			newCnf.add(newClause);
			
		}		
		
		return newCnf;
	}

	/**
	 * Returns a CNF that is equivalent with the negation of the given DNF.
	 * @param negatedDnf
	 * @return
	 */
	public static CNF negatedDnf2cnf(DNF negatedDnf){
		
		CNF cnf = new CNF();
		for (SimpleConjunction conj : negatedDnf) {
			
			//reverse positive and negative atoms.
			Clause clause = new Clause(conj.getNegativeAtoms(), conj.getPositiveAtoms());
			cnf.add(clause);
		}
		
		return cnf;
	}

	
	public static Set<GdlSentence> extractAtoms(List<GdlRule> groundedDescription){
		
		Set<GdlSentence> allAtoms = new HashSet<GdlSentence>();
		
		//Extract all atoms from the rules.
		for (GdlRule gdlRule : groundedDescription) {
			extractAtoms(gdlRule, allAtoms);
		}
		
		return allAtoms;
	}
	
	static void extractAtoms(GdlRule rule, Set<GdlSentence> allAtoms){
		
		GdlSentence head = rule.getHead();
		allAtoms.add(head);
		
		for(GdlLiteral bodyElem : rule.getBody()){
			extractAtoms(bodyElem, allAtoms);
		}
	}
	
	static void extractAtoms(GdlLiteral formula, Set<GdlSentence> allAtoms){
		
		if(formula instanceof GdlOr){
			GdlOr or = (GdlOr) formula;
			
			for (int i = 0; i < or.arity(); i++) {
				extractAtoms(or.get(i), allAtoms);
			}
			
			
		}else if(formula instanceof GdlNot){
			GdlNot not = (GdlNot) formula;
			
			extractAtoms(not.getBody(), allAtoms);
			
		}else if(formula instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) formula;
			
			allAtoms.add(sentence);
			
		}else if(formula instanceof GdlDistinct){
			
			//We are assuming the rules are grounded. Therefore any Distinct can be removed.
			
		}else{
			throw new RuntimeException("GDL2CNF.extractAtoms() Error! " + formula.getClass().getName());
		}
		
		
	}
	
	
	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
