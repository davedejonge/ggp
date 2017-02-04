package ddejonge.ggp.tools.logic;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.prover.aima.substituter.Substituter;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;



public class LogicUtils {

	public static void main(String[] args) {
		
		GdlLiteral a = GdlPool.getProposition(GdlPool.getConstant("a"));
		GdlLiteral b = GdlPool.getProposition(GdlPool.getConstant("b"));
		GdlLiteral c = GdlPool.getProposition(GdlPool.getConstant("c"));
		GdlLiteral e = GdlPool.getProposition(GdlPool.getConstant("e"));
		
		GdlLiteral d1 = GdlPool.getRelation(_DOES,new GdlTerm[] {GdlPool.getConstant("d1")});
		GdlLiteral d2 = GdlPool.getRelation(_DOES,new GdlTerm[] {GdlPool.getConstant("d2")});
		GdlLiteral d3 = GdlPool.getRelation(_DOES,new GdlTerm[] {GdlPool.getConstant("d3")});
		GdlLiteral d4 = GdlPool.getRelation(_DOES,new GdlTerm[] {GdlPool.getConstant("d4")});
		
		GdlLiteral X1 = GdlPool.getOr(new GdlLiteral[] {a, b});
		GdlLiteral X2 = GdlPool.getOr(new GdlLiteral[] {c, e});
		
		GdlOr or1 = GdlPool.getOr(new GdlLiteral[] {X1, d1});
		GdlOr or2 = GdlPool.getOr(new GdlLiteral[] {X2, d2});
		
		GdlAnd and = AndPool.getAnd(new GdlLiteral[]{or1, or2});
		
		//GdlNot not = GdlPool.getNot(or);
		
		System.out.println("BEFORE:");
		System.out.println(and.toString());
		System.out.println();
		System.out.println("AFTER:");
		
		GdlOr normalized = bringToDoesForm(and, 1000);
		System.out.println(normalized);
		System.out.println();
		
		
		
		
	}
	

	
	public static GdlLiteral VERUM = AndPool.getAnd(new ArrayList<GdlLiteral>(0)); //the empty AND represents TRUE
	public static GdlLiteral FALSUM = GdlPool.getOr(new ArrayList<GdlLiteral>(0));  //the empty OR represents FALSE
	
	public static final GdlConstant _DOES = GdlPool.getConstant("_does");

	
	//TODO: remove this debug code!!
	static GdlFunction cont_x = GdlPool.getFunction(GdlPool.getConstant("control"),new GdlTerm[]{GdlPool.getConstant("xplayer")});
	static GdlFunction cont_o = GdlPool.getFunction(GdlPool.getConstant("control"),new GdlTerm[]{GdlPool.getConstant("oplayer")});
	static GdlLiteral control_x = GdlPool.getRelation(GdlPool.TRUE, new GdlTerm[]{cont_x}); 
	static GdlLiteral control_o = GdlPool.getRelation(GdlPool.TRUE, new GdlTerm[]{cont_o});
	
	public static Set<GdlSentence> getInstantiations(GdlSentence nonGroundedSentence, Collection<GdlSentence> groundedBasePropositions){
		
		Set<GdlSentence> instantiations = new HashSet<>();
		
		for(GdlSentence groundedProposition : groundedBasePropositions){
			
			Substitution sub = Unifier.unify(nonGroundedSentence, groundedProposition);
			
			if(sub != null){
				instantiations.add(groundedProposition);
			}
			
		}
		
		return instantiations;
	}
	
	public static Set<GdlSentence> getPartialInstantiations(GdlSentence nonGroundedSentence, List<GdlVariable> variables, List<GdlSentence> groundedBasePropositions){
	
		//Create a list containing the sentences that still need to be instantiated.
		Set<GdlSentence> toInstantiate = new HashSet<>();
		toInstantiate.add(nonGroundedSentence);
		
		Set<GdlSentence> partialInstantiations = new HashSet<>();
		for(GdlVariable variable : variables){
			
			//for each atom to instantiate, put all its instantiations in the list of partialInstantiations.
			partialInstantiations.clear();
			for(GdlSentence sentnece : toInstantiate){
				partialInstantiations.addAll(getPartialInstantiations(sentnece, variable, groundedBasePropositions));
			}
			
			//remove the sentences that have been instantiated from the list, and fill it with the results of those instantiations,
			// so that we can continue with those for the next variable.
			toInstantiate.clear();
			toInstantiate.addAll(partialInstantiations);
		}
		
		//Note: if we return partialInstantiations instead of toInstantiate, it goes wrong when the list of inputvars is empty.
		return toInstantiate;
	}
	
	/**
	 * Replaces all occurrences of the given var in the given non-ground sentence.
	 * @param nonGroundedSentence
	 * @param groundedBasePropositions
	 * @return
	 */
	public static List<GdlSentence> getPartialInstantiations(GdlSentence nonGroundedSentence, GdlVariable variable, List<GdlSentence> groundedBasePropositions){
		
		List<GdlSentence> partialInstantiations = new ArrayList<>();
		
		for(GdlSentence groundedProposition : groundedBasePropositions){
			
			//Get the substitution that unifies the non-ground atom with the ground atom.
			Substitution fullSubstitution = Unifier.unify(nonGroundedSentence, groundedProposition);
			
			if(fullSubstitution != null){
				
				//If they can indeed be unified, then get the term that substitutes the given variable.
				GdlTerm substitutionTerm = fullSubstitution.get(variable);
			
				//Create a new Substitution object that only substitutes the given variable.
				Substitution partialSubstition = new Substitution();
				partialSubstition.put(variable, substitutionTerm);
			
				//Apply the partial substitution.
				GdlSentence partialInstantiation = Substituter.substitute(nonGroundedSentence, partialSubstition);
			
				//Add it to the list, if it isn't already in there.
				if(!partialInstantiations.contains(partialInstantiation)){
					partialInstantiations.add(partialInstantiation);
				}
			}
			
		}
		
		return partialInstantiations;
	}
	
	/**
	 * Returns the domain of the given variable in the given non-ground atom.
	 * @param nonGroundedSentence
	 * @param variable
	 * @param groundedBasePropositions
	 * @return
	 */
	public static List<GdlTerm> getDomain(GdlSentence nonGroundedSentence, GdlVariable variable, List<GdlSentence> groundedBasePropositions){
		
		
		List<GdlTerm> domainTerms = new ArrayList<>();
		
		for(GdlSentence groundedProposition : groundedBasePropositions){
			
			Substitution sub = Unifier.unify(nonGroundedSentence, groundedProposition);
			
			if(sub != null){
				
				GdlTerm subsitutedTerm = sub.get(variable);
				
				domainTerms.add(subsitutedTerm);
			}
			
		}
		
		return domainTerms;
		
	}
	
	public static GdlLiteral negate(GdlLiteral literal){
		if(literal instanceof GdlNot){
			return ((GdlNot) literal).getBody();
		}else{
			return GdlPool.getNot(literal);
		}
	}
	
	
	public static GdlOr bringToDoesForm(GdlLiteral literal, long timeout){
		
		if(literal instanceof GdlOr){
			
			Disjunction disjunction = new Disjunction();
			
			GdlOr or = (GdlOr)literal;
			for (int i = 0; i < or.arity(); i++) {
				
				if(dependsOnDoes(or.get(i))){
					
					//bring the disjunct to normal form and then add it to the disjunction
					disjunction.add(bringToDoesForm(or.get(i), timeout));
					
				}else{
					
					//add directly to disjuncts.
					disjunction.add(or.get(i));
				}
			}
			
			/*
			if(disjuncts.size() == 1){
				return disjuncts.get(0);
			}*/
			
			return disjunction.getOr();
			
		}else if(literal instanceof GdlNot){
			
			GdlLiteral body = ((GdlNot)literal).getBody();
			
			if( ! dependsOnDoes(body)){
				
				//Create an OR that only contains this literal.
				List<GdlLiteral> disjuncts = new ArrayList<>();
				disjuncts.add(literal);
				return GdlPool.getOr(disjuncts);
				
			}
			
			
			// 1. first bring the body to normal form.. (the body is now a disjunction of conjunctions)
			// 2. then apply DeMorgan to bring the negation back in (the full formula is now a conjunction of negated conjunctions)
			// 3. apply DeMorgan to each conjunct (now it's a conjunction of disjunctions)
			// 4. distribute AND over OR (now it's a disjunction, which will automatically be in the correct form because of step 1).
			
			//1.
			GdlOr correctForm = bringToDoesForm(body, timeout);
			
			//2.
			GdlAnd and = applyDeMorgan(correctForm);
			
			//3. 
			List<GdlLiteral> newConjunction = new ArrayList<>();
			for(int i=0; i<and.arity(); i++){
				
				GdlNot innerNot = (GdlNot)and.get(i);
				newConjunction.add(applyDeMorgan((GdlAnd)innerNot.getBody()));
			}
			GdlAnd newAnd = AndPool.getAnd(newConjunction);
			
			//4.
			return distributeSpecial(newAnd);
			
		}else if(literal instanceof GdlAnd){
			
			// 1. make sure that all conjuncts that depend on Does are in the correct form.
			// 2. distribute AND over OR.
			
			Conjunction conjunction = new Conjunction();
			
			GdlAnd and = (GdlAnd)literal;
			
			for (int i = 0; i < and.arity(); i++) {
				
				GdlLiteral conjunct = and.get(i);
				
				if(dependsOnDoes(conjunct)){
					conjunct = bringToDoesForm(conjunct, timeout);
				}
				
				conjunction.add(conjunct);
			}


			
			GdlAnd normalizedAnd = conjunction.getAnd();
			
			//Distribute...
			return distributeSpecial(normalizedAnd);
			
			
		}else{
			
			// A Sentence or a Distinct is already in the correct form.
			
			return GdlPool.getOr(new GdlLiteral[] {literal});
			
		}
		
		
	}
	
	/**
	 * Determines whether the given literal contains a DOES relation.
	 * @param literal
	 * @return
	 */
	public static boolean dependsOnDoes(GdlLiteral literal){
		
		if(literal instanceof GdlRelation){
			if(((GdlRelation)literal).getName().equals(_DOES)){
				return true;
			}else{
				return false;
			}
			
		}else if(literal instanceof GdlNot){
			
			return dependsOnDoes(((GdlNot)literal).getBody());
			
		}else if(literal instanceof GdlAnd){
			
			GdlAnd and = (GdlAnd)literal;
			for (int i = 0; i < and.arity(); i++) {
				if(dependsOnDoes(and.get(i))){
					return true;
				}
			}
			
			return false;
			
		}else if(literal instanceof GdlOr){
			
			GdlOr or = (GdlOr)literal;
			for (int i = 0; i < or.arity(); i++) {
				if(dependsOnDoes(or.get(i))){
					return true;
				}
			}
			
			return false;
			
		}else{
			return false;
		}
		
		
	}
	
	/**
	 * Returns null if the given formula does not depend on var.
	 * Otherwise, returns the 'highest' formula in which the var appears.
	 * 
	 * If everything is okay this should never return a GdlNot.
	 * Either the scope is the body of the GdlNot, or the variable is also contained
	 * in a sibling of the not.
	 * 
	 * i.e. 
	 * if   psi = not(phi(x))   			then it will return phi(x)
	 * if 	psi = not(phi(x)) && phi'(x)    then it will return psi.
	 * 
	 * Note that this implies we assume that formulas of the form
	 *  Ex( not(phi(x)) ) never occur.
	 *  we only allow:				not( Ex(phi(x) ) 
	 *  or: 						Ex( not(phi(x)) &&  not(phi'(x))  )
	 *  or:							Ex( not(phi(x)) ||  not(phi'(x))  )
	 * 
	 * 
	 * @param formula
	 * @param var
	 * @return
	 */
	public static GdlLiteral getScope(GdlLiteral formula,  GdlVariable var){
		
		if(formula instanceof GdlAnd){
			
			GdlAnd and = (GdlAnd) formula;
			return getScopeAND(and, var);
			
		}else if(formula instanceof GdlOr){
			
			GdlOr or = (GdlOr) formula;
			return getScopeOR(or, var);
		
		}else if(formula instanceof GdlNot){
			
			GdlNot not = (GdlNot) formula;
			return getScope(not.getBody(), var);
			
			
		}else if(formula instanceof GdlProposition){
			
			return null; // a proposition cannot contain a variable.
			
		}else if(formula instanceof GdlRelation){
			
			GdlRelation relation = (GdlRelation) formula;
			
			if(dependsOnVar(relation, var)){
				return relation;
			}else{
				return null;
			}
		
		}else if(formula instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) formula;
			
			if(dependsOnVar(distinct.getArg1(), var) || dependsOnVar(distinct.getArg2(), var)){
				return distinct;
			}else{
				return null;
			}
			
		}else{
			throw new RuntimeException("LogicUtils.getScope() Error! unknown class: " + formula.getClass().getName());
		}
	}
	
	
	public static GdlLiteral getScopeOR(GdlOr or, GdlVariable var){
		
		GdlLiteral scope = null;
		
		for (int i = 0; i < or.arity(); i++) {
			
			GdlLiteral conjunct = getScope(or.get(i), var);
			
			if(conjunct != null){
				
				if(scope == null){
					scope = conjunct;
				}else{
					//if we have found a second disjunct in which the variable appears, then return the entire formula.
					return or; 
				}
			}
		}
		
		return scope;
	}
	
	public static GdlLiteral getScopeAND(GdlAnd and, GdlVariable var){
		
		GdlLiteral scope = null;
		
		for (int i = 0; i < and.arity(); i++) {
			
			GdlLiteral conjunct = getScope(and.get(i), var);
			
			if(conjunct != null){
				
				if(scope == null){
					scope = conjunct;
				}else{
					//if we have found a second conjunct in which the variable appears, then return the entire formula.
					return and;
				}
			}
		}
		
		return scope;
	}
	
	
	public static boolean dependsOnVar(GdlLiteral formula, GdlVariable var){
		
		if(formula instanceof GdlAnd){
			GdlAnd and = (GdlAnd) formula;
			
			for (int i = 0; i < and.arity(); i++) {
				if(dependsOnVar(and.get(i), var)){
					return true;
				}
			}
			return false;
			
		}else if(formula instanceof GdlOr){
			GdlOr or = (GdlOr) formula;
			
			for (int i = 0; i < or.arity(); i++) {
				if(dependsOnVar(or.get(i), var)){
					return true;
				}
			}
			return false;
			
		}else if(formula instanceof GdlNot){
			GdlNot not = (GdlNot) formula;
			
			return dependsOnVar(not.getBody(), var);
			
		}else if(formula instanceof GdlProposition){
			return false;
			
		}else if(formula instanceof GdlRelation){
			GdlRelation relation = (GdlRelation) formula;
			
			for(int i=0; i<relation.arity(); i++){
				if(dependsOnVar(relation.get(i), var)){
					return true;
				}
			}
			return false;
			
		}else if(formula instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) formula;
			
			return dependsOnVar(distinct.getArg1(), var) || dependsOnVar(distinct.getArg2(), var);
			
		}else{
			throw new RuntimeException("LogicUtils.dependsOnVar() Error! " + formula.getClass().getName());
		}
	}
	
	
	public static boolean dependsOnVar(GdlTerm term, GdlVariable var){
		
		if(term instanceof GdlFunction){
			GdlFunction function = (GdlFunction)term;
			
			for (int i = 0; i < function.arity(); i++) {
				if(dependsOnVar(function.get(i), var)){
					return true;
				}
			}
			return false;
			
		}else if(term instanceof GdlVariable){
			
			return term.equals(var);
			
		}else if(term instanceof GdlConstant){
			return false;
		}else{
			throw new RuntimeException("LogicUtils.dependsOnVar() Error! " + term.getClass().getName());
		}
		
	}
	
	public static GdlOr applyDeMorgan(GdlAnd and){
		
		Disjunction disjunction = new Disjunction(and.arity());
		
		for(int i=0; i<and.arity(); i++){
			disjunction.add(GdlPool.getNot(and.get(i)));
		}
		
		return disjunction.getOr();
		
	}
	
	public static GdlAnd applyDeMorgan(GdlOr or){
		
		Conjunction conjuncts = new Conjunction(2*or.arity());
		
		for(int i=0; i<or.arity(); i++){
			conjuncts.add(GdlPool.getNot(or.get(i)));
		}
		
		return conjuncts.getAnd();
		
	}
	
	
	static GdlOr distributeSpecial(GdlAnd and){
		
		if(and.arity() == 0){
			return GdlPool.getOr(new GdlLiteral[]{and});
		}
		
		//TODO: set capacities.
		List<DistributeNode> openList = new ArrayList<DistributeNode>(10*1000);
		openList.add(new DistributeNode());
		List<Conjunction> finalSolution = new ArrayList<Conjunction>(10*1000);
		
		
		while(openList.size() > 0){
			
			DistributeNode node = openList.remove(openList.size()-1);
			int index = node.numAdded;
			
			if(and.get(index) instanceof GdlOr){
				GdlOr or = (GdlOr) and.get(index);
				
				Disjunction independentDisjuncts =  new Disjunction();
				
				for(int i=0; i<or.arity(); i++){
					
					GdlLiteral literal = or.get(i);
					
					if(dependsOnDoes(literal)){
						
						DistributeNode newNode = new DistributeNode(node);
						newNode.addAndIncreaseCounter(literal);
						
						//If we now have a full conjunction then we can add it to the final solution.
						//Otherwise we put the new Node on the open list.
						if(newNode.numAdded < and.arity()){
							openList.add(newNode);
						}else{
							finalSolution.add(newNode.conjunction);
						}
						
					}else{
						independentDisjuncts.add(literal);
					}
				}
				
				if(independentDisjuncts.size() > 0){
					
					DistributeNode newNode = new DistributeNode(node);
					newNode.addAndIncreaseCounter(independentDisjuncts.getOr());
					
					//If we now have a full conjunction then we can add it to the final solution.
					//Otherwise we put the new Node on the open list.
					if(newNode.numAdded < and.arity()){
						openList.add(newNode);
					}else{
						finalSolution.add(newNode.conjunction);
					}
				}

				
			}else{
				
				//Make a copy of the current node.
				DistributeNode newNode = new DistributeNode(node);
				
				//Extend it with the current literal.
				newNode.addAndIncreaseCounter(and.get(index));
				
				//If we now have a full conjunction then we can add it to the final solution.
				//Otherwise we put the new Node on the open list.
				if(newNode.numAdded < and.arity()){
					openList.add(newNode);
				}else{
					finalSolution.add(newNode.conjunction);
				}
				
			}
			
		}
		
		//Convert the solution list into an OR containing ANDs
		Disjunction disjuncts = new Disjunction(2*finalSolution.size());
		
		for(int i=0; i<finalSolution.size(); i++){
			GdlAnd and2 = finalSolution.get(i).getAnd();
			disjuncts.add(and2);
		}
		
		return disjuncts.getOr();
	}
	
	/**
	 * Given a conjunction of disjunctions this function returns an equivalent disjunction of conjunctions.
	 * (The given conjunction does not necessarily have to contain only disjunctions)
	 * @param and
	 * @return
	 */
	public static GdlOr distributeAndOr(GdlAnd and){
		
		//TODO: set capacities.
		List<DistributeNode> openList = new ArrayList<DistributeNode>(10*1000);
		openList.add(new DistributeNode());
		List<Conjunction> finalSolution = new ArrayList<Conjunction>(10*1000);
		
		
		while(openList.size() > 0){
			
			DistributeNode node = openList.remove(openList.size()-1);
			int index = node.numAdded;
			
			if(and.get(index) instanceof GdlOr){
				GdlOr or = (GdlOr) and.get(index);
				
				for(int i=0; i<or.arity(); i++){
					
					GdlLiteral literal = or.get(i);
					
					//Make a copy of the current node.
					DistributeNode newNode = new DistributeNode(node);
					
					//Extend it with the current literal.
					newNode.addAndIncreaseCounter(literal);
					
					//If we now have a full conjunction then we can add it to the final solution.
					//Otherwise we put the new Node on the open list.
					if(newNode.numAdded < and.arity()){
						openList.add(newNode);
					}else{
						finalSolution.add(newNode.conjunction);
					}
					
					
				}
			}else{
				
				//Make a copy of the current node.
				DistributeNode newNode = new DistributeNode(node);
				
				//Extend it with the current literal.
				newNode.addAndIncreaseCounter(and.get(index));
				
				//If we now have a full conjunction then we can add it to the final solution.
				//Otherwise we put the new Node on the open list.
				if(newNode.numAdded < and.arity()){
					openList.add(newNode);
				}else{
					finalSolution.add(newNode.conjunction);
				}
				
			}
			
		}
		
		//Convert the solution list into an OR containing ANDs
		Disjunction disjuncts = new Disjunction(finalSolution.size());
		
		for(int i=0; i<finalSolution.size(); i++){
			GdlAnd and2 = finalSolution.get(i).getAnd();
			disjuncts.add(and2);
		}
		
		return disjuncts.getOr();
	}
	
	
	static class DistributeNode{
		
		//FIELDS
		Conjunction conjunction;
		int numAdded;
		
		//DEFAULT CONSTRUCTOR
		DistributeNode(){
			this.conjunction = new Conjunction();
			this.numAdded = 0;
		}
		
		//COPY CONSTRUCTOR
		DistributeNode(DistributeNode node){
			this.conjunction = new Conjunction(node.conjunction);
			numAdded = node.numAdded;
		}
		
		void addAndIncreaseCounter(GdlLiteral literal){
			
			conjunction.add(literal);
			numAdded++;
		}
		
		void add(GdlLiteral literal){
			conjunction.add(literal);
		}
		
		void increaseCounter(){
			numAdded++;
		}
		
	}
	
	/**
	 * Returns true if the conjuncts of or1 are a subset of the conjuncts of or0.
	 * (assuming or0.arity() > or1.arity()) 
	 * @param or0
	 * @param or1
	 * @return
	 */
	static boolean containsDisjunction(GdlOr or0, GdlOr or1){
		
		for(int k=0; k<or1.arity(); k++){
			if( ! containsElement(or0, or1.get(k))){
				return false;
			}
		}
			
		return true;
		
	}
	
	static boolean containsConjunction(GdlAnd and0, GdlAnd and1){
		
		for(int k=0; k<and1.arity(); k++){
			if( ! containsElement(and0, and1.get(k))){
				return false;
			}
		}
			
		return true;
		
	}
	
	
	public static boolean containsElement(GdlOr or, GdlLiteral lit2){
		
		for(int i=0; i<or.arity(); i++){
			
			if(or.get(i).equals(lit2)){
				return true;
			}
		}
			
		return false;
		
	}
	
	static boolean containsElement(GdlAnd and, GdlLiteral lit2){
		
		for(int i=0; i<and.arity(); i++){
			
			if(and.get(i).equals(lit2)){
				return true;
			}
		}
			
		return false;
		
	}
	
	
	

	
	public static List<GdlRule> removeOrs(List<GdlRule> rules){
		
		//copy the list into a list of GDL objects.
		List<Gdl> gdls = new ArrayList<>(rules);
		
		//Remove the ORs
		List<Gdl> newDescription = DeORer.run(gdls);
		
		//copy the new rules into a list of GdlRule objects.
		List<GdlRule> newRules = new ArrayList<>();
		for (Gdl gdl : newDescription) {
			newRules.add((GdlRule)gdl);
		}
		
		//return the list.
		return newRules;
	}
	
	public static List<GdlRule> removeDistincts(List<GdlRule> groundedRules){
		
		List<GdlRule> newRules = new ArrayList<>();
		
		for (GdlRule gdlRule : groundedRules) {
			List<GdlLiteral> body = gdlRule.getBody();
			
			List<GdlLiteral> newBody = new ArrayList<>(body.size());
			
			boolean okay = true;
			
			for(GdlLiteral literal : body){
				
				if((literal instanceof GdlDistinct)){
					
					GdlDistinct distinct = (GdlDistinct)literal;
					
					if(distinct.getArg1().equals(distinct.getArg2())){
						//the literal evaluates to FALSE, so the current rule can be ignored.
						okay = false;
						break;
					}
					
				}else{
					newBody.add(literal);
				}
				

			}
			
			if(okay){
				GdlRule newRule = GdlPool.getRule(gdlRule.getHead(), newBody);
				newRules.add(newRule);
			}

		}
		
		return newRules;
		
	}
	
	
	

	
	
	/*
	List<GdlLiteral> simplifyConjunction(List<GdlLiteral> conjunction){
		
		//First check if the first and second conjunct have something in common.
		GdlLiteral element0 = conjunction.get(0);
		GdlLiteral element1 = conjunction.get(1);
		
		GdlLiteral commonElement = null;		
		
		//WARNING: we are assuming that a conjunction cannot contain any GdlAnd elements.
		
		if(element0 instanceof GdlOr && element1 instanceof GdlOr){
			
			GdlOr or1 = (GdlOr)element1;
			
			for(int i=0; i<or1.arity(); i++){
				if(containsElement((GdlOr)element0, or1.get(i))){
					commonElement = or1.get(i);
					break;
				}
			}
			
		}else if(element0 instanceof GdlOr && ! (element1 instanceof GdlOr)){
			
			if(containsElement((GdlOr)element0, element1)){
				commonElement = element1;
			}
		
		}else if( ! (element0 instanceof GdlOr) &&  element1 instanceof GdlOr){
			
			if(containsElement((GdlOr)element1, element0)){
				commonElement = element0;
			}
		
		}else if( ! (element0 instanceof GdlOr) && ! (element1 instanceof GdlOr)){
			
			if(element0.equals(element1)){
				commonElement = element0;
			}
			
		}
		
		if(commonElement == null){
			return conjunction;
		}

		
		//Now check if the common element also appears in any other conjunct.
		boolean appearsEverywhere = true;
		for(int i=2; i<conjunction.size(); i++){
			
			GdlLiteral elem = conjunction.get(i);
			
			if(elem instanceof GdlOr){
				if( ! containsElement((GdlOr)elem, commonElement)){
					appearsEverywhere = false;
					break;
				}
			}else{
				
				if(! elem.equals(commonElement)){
					appearsEverywhere = false;
					break;
				}
			}
			
		}
		
	}
	*/
	

	

	

	
	/**
	 * Removes any disjunct of the given disjunction if it is a special case of another disjunct.
	 * 
	 * @param or
	 * @return
	 */
	public static GdlOr removeSpecialCases(GdlOr or){
		
		boolean[] toRemove = new boolean[or.arity()];
		int numToRemove = 0;
		
		for(int i=0; i<or.arity(); i++){
			for(int j=0; j<or.arity(); j++){
				if(i == j){
					continue;
				}
				
				if(toRemove[j]){
					//We have already determined that the j-th conjunct must be removed in an earlier iteration.
					continue;
				}
				
				if(specialCaseOf(or.get(i), or.get(j))){
					toRemove[i] = true;
					numToRemove++;
				}
				
				
			}
		}
		
		//if nothing can be removed then we can return the original object. 
		if(numToRemove == 0){
			return or;
		}
		
		//Now create a new OR with only the elements that are not removed.
		Disjunction newDisjuncts = new Disjunction(or.arity());
		for(int i=0; i<or.arity(); i++){
			
			if(!toRemove[i]){
				newDisjuncts.add(or.get(i));
			}
		}
		
		return newDisjuncts.getOr();
	}
	
	
	
	/**
	 * Returns true if lit0 is a special case of lit1. 
	 * That is, if lit0 can be obtained by substituting some variables in lit1.
	 * However, it may return false even if it is possible.
	 * 
	 * @param lit0
	 * @param lit1
	 * @return
	 */
	public static boolean specialCaseOf(GdlLiteral lit0, GdlLiteral lit1){
		return specialCaseOf(lit0, lit1, new SubstitutionContainer());
	}
	
	public static boolean specialCaseOf(GdlLiteral lit0, GdlLiteral lit1, SubstitutionContainer sc){
		
		if(lit1 == null){
			throw new RuntimeException("LogicUtils.specialCaseOf() Error! lit1 == null (1)");
		}
		
		if(sc.get() != null){
			lit1 = MySubstituter.substitute(lit1, sc.get());
		}
		
		if(lit0 == null){
			throw new RuntimeException("LogicUtils.specialCaseOf() Error! lit0 == null");
		}
		if(lit1 == null){
			throw new RuntimeException("LogicUtils.specialCaseOf() Error! lit1 == null (2) " + sc.get());
		}
		
		//The substitution above may alter the type of the literal (it may return FALSUM or VERUM when given a Distinct).
		// so therefore we need to check the type after the substitution. 
		if( ! lit0.getClass().equals(lit1.getClass())){
			return false;
		}
		
		
		if(lit0.equals(lit1)){
			return true;
		}
		
		if(lit0 instanceof GdlAnd){
			GdlAnd and0 = (GdlAnd) lit0;
			GdlAnd and1 = (GdlAnd) lit1;
			
			if(and0.arity() != and1.arity()){
				return false;
			}
			
			//NOTE: this only returns true if the conjuncts have the same order in both ANDs.
			//therefore, this can be improved.
			for (int i = 0; i < and0.arity(); i++) {
				if( ! specialCaseOf(and0.get(i), and1.get(i), sc)){
					return false;
				}
			}
			
			return true;
			
		}else if(lit0 instanceof GdlOr){
			GdlOr or0 = (GdlOr) lit0;
			GdlOr or1 = (GdlOr) lit1;
			
			if(or0.arity() != or1.arity()){
				return false;
			}
			
			//NOTE: this only returns true if the disjuncts have the same order in both ORs.
			//therefore, this can be improved.
			for (int i = 0; i < or0.arity(); i++) {
				if( ! specialCaseOf(or0.get(i), or0.get(i), sc)){
					return false;
				}
			}
			
			return true;
			
		}else if(lit0 instanceof GdlNot){
			GdlNot not0 = (GdlNot) lit0;
			GdlNot not1 = (GdlNot) lit1;
			
			return specialCaseOf(not0.getBody(), not1.getBody(), sc);
			
		}else if(lit0 instanceof GdlSentence){
			GdlSentence sentence0 = (GdlSentence) lit0;
			GdlSentence sentence1 = (GdlSentence) lit1;
			
			Substitution theta = Unifier.unify(sentence0, sentence1);
			if(theta == null){
				return false;
			}
			if(sentence0.equals(MySubstituter.substitute(sentence0, theta))){
				sc.add(theta);
				return true;
			}
			return false;
			
			
		}else if(lit0 instanceof GdlDistinct){
			GdlDistinct distinct0 = (GdlDistinct) lit0;
			GdlDistinct distinct1 = (GdlDistinct) lit1;
			
			boolean firstIsVar = true;
			boolean secondIsVar = true;
			
			//check the first argument of both Distincts.
			if(distinct1.getArg1() instanceof GdlConstant){
				
				firstIsVar = false;
				
				if( ! distinct1.getArg1().equals(distinct0.getArg1())){
					return false;
				}
			}
			
			//check the second argument of both Distincts.
			if(distinct1.getArg2() instanceof GdlConstant){
				
				secondIsVar = false;
				
				if( ! distinct1.getArg2().equals(distinct0.getArg2())){
					return false;
				}
			}
			
			
			//Now we still need to make sure that the variables are consistent with one another.
			
			if( firstIsVar && secondIsVar){   // d(?x1, ?x2) d(?x3, ?x4)
				
				GdlVariable x1 = (GdlVariable)distinct0.getArg1();
				GdlVariable x2 = (GdlVariable)distinct0.getArg2();
				GdlVariable x3 = (GdlVariable)distinct1.getArg1();
				GdlVariable x4 = (GdlVariable)distinct1.getArg2();
				
				//Now there are three possible cases:
				//1)  d(?x1, ?x1) d(?x3, ?x3)  //true
				//2)  d(?x1, ?x2) d(?x3, ?x3)  //false
				//3)  d(?x1, ?x1) d(?x3, ?x4)  //true
				
				//1) and 2)
				if(x3.equals(x4)){
					
					if(x1.equals(x2)){
						Substitution theta = new Substitution();
						theta.put(x3, x1);
						sc.add(theta);
						return true;
					}
					
					return false;
					
				}
				
				//3)
				Substitution theta = new Substitution();
				theta.put(x3, x1);
				theta.put(x4, x1);
				sc.add(theta);
				return true;
				
			}else{
				
				Substitution theta;
				
				
				if(firstIsVar){				// d(?x1, c4)  d(?x3, c4)
					
					theta = new Substitution();
					GdlVariable x1 = (GdlVariable)distinct0.getArg1();
					GdlVariable x3 = (GdlVariable)distinct1.getArg1();
					theta.put(x3, x1);
					sc.add(theta);
				
				}else if(secondIsVar){		// d(c1, ?x2)  d(c1, ?x4)
					
					theta = new Substitution();
					GdlVariable x2 = (GdlVariable)distinct0.getArg2();
					GdlVariable x4 = (GdlVariable)distinct1.getArg2();
					theta.put(x4, x2);
					sc.add(theta);
				}
				
				
				
				// d(c1, c2)   d(c1, c2)

				return true;
			}

			
			
		}else{
			throw new RuntimeException("LogicSimplifier.simplify() Error! " + lit0.getClass().getName());
		}
		
	}
	
	public static GdlLiteral bringToNNF(GdlLiteral original){
		
		GdlLiteral newLiteral;
		
		if(original instanceof GdlNot){
			
			GdlLiteral body = ((GdlNot)original).getBody();
			
			if(body instanceof GdlAnd){
				
				newLiteral = LogicUtils.applyDeMorgan((GdlAnd)body);
				return bringToNNF(newLiteral);
				
			}else if(body instanceof GdlOr){
				
				newLiteral = LogicUtils.applyDeMorgan((GdlOr)body);
				return bringToNNF(newLiteral);
				
			}else{

				return original;
				
			}
		}else if(original instanceof GdlOr){
			
			GdlOr or = (GdlOr)original;
			Disjunction disjuncts = new Disjunction(or.arity());
			
			for(int i=0; i<or.arity(); i++){
				disjuncts.add(bringToNNF(or.get(i)));
			}
			
			return disjuncts.getOr();
		
		}else if(original instanceof GdlAnd){
			
			GdlAnd and = (GdlAnd)original;
			Conjunction conjuncts = new Conjunction(and.arity());
			
			//First, bring each conjunct to DNF
			for(int i=0; i<and.arity(); i++){
				conjuncts.add(bringToNNF(and.get(i)));
			}
			
			return conjuncts.getAnd();
			
		}else{
			
			return original;
		}
	}
	
	
	public static GdlLiteral bringToDNF(GdlLiteral original){
		
		if(original instanceof GdlNot){
			
			GdlLiteral body = ((GdlNot)original).getBody();
			
			//We assume the original is already in negation normal form, so the body of the NOT must be a GdlSentence.
			if( (body instanceof GdlSentence) || (body instanceof GdlDistinct) ){
				return original;
			}
			
			throw new RuntimeException("BackwardProver.bringToDNF() Error! the specified NOT object does not contain a GdlSentence: " + original.toString());
			
			
		}else if(original instanceof GdlOr){
			
			Disjunction disjuncts = new Disjunction(((GdlOr) original).arity());
			
			for(int i=0; i<((GdlOr)original).arity(); i++){
				disjuncts.add(bringToDNF(((GdlOr) original).get(i)));
			}
			
			return disjuncts.getOr();
			
		}else if(original instanceof GdlDistinct){
			
			//GdlDistinct can only contain terms, so nothing needs to be replaced.
			return original; 
			
		}else if(original instanceof GdlSentence){
			
			return original; 
			
		}else if(original instanceof GdlAnd){
			
			GdlAnd and = (GdlAnd)original;
			Conjunction newConjunction = new Conjunction(and.arity());
			
			//First, bring each conjunct to DNF
			for(int i=0; i<and.arity(); i++){
				newConjunction.add(bringToDNF(and.get(i)));
			}

			//Then, flip the conjuncts and the disjuncts.
			return LogicUtils.distributeAndOr(newConjunction.getAnd());
			
		}else{
			throw new RuntimeException("BackwardProver5.bringToDNF() Error! unknown class " + original.getClass().getName());
		}
		
	}
	
	
	
	public static GdlLiteral replaceDoesWithLegal(GdlLiteral original){
		
		GdlLiteral newLiteral = replaceRelationName(original, GdlPool.DOES, GdlPool.LEGAL);
		newLiteral = replaceRelationName(original, GdlPool.getConstant("_does"), GdlPool.LEGAL);
		
		return newLiteral;
	}
	
	
	public static GdlLiteral replaceRelationName(GdlLiteral original, GdlConstant relationNameToRemove, GdlConstant newRelationName){
		
		if(original instanceof GdlNot){
			
			GdlLiteral body = ((GdlNot)original).getBody();
			
			GdlLiteral newBody = replaceRelationName(body, relationNameToRemove, newRelationName);
			
			if(newBody == null){
				return null;
			}
			
			return GdlPool.getNot(newBody);
			
		}else if(original instanceof GdlOr){
			
			Disjunction disjuncts = new Disjunction(((GdlOr) original).arity());
			
			for(int i=0; i<((GdlOr)original).arity(); i++){
				disjuncts.add(replaceRelationName(((GdlOr) original).get(i), relationNameToRemove, newRelationName));
			}
			
			return disjuncts.getOr();
			
		}else if(original instanceof GdlDistinct){
			
			//GdlDistinct can only contain terms, so nothing needs to be replaced.
			return original; 
			
		}else if(original instanceof GdlSentence){
			
			GdlConstant relationName = ((GdlRelation) original).getName();
			
			
			if(relationName.equals(relationNameToRemove)){
				
				GdlRelation newRelation = GdlPool.getRelation(newRelationName, ((GdlSentence) original).getBody());
				return newRelation;
			
			}else{
				
				return original;
			}
			
		}else if(original instanceof GdlAnd){
			
			Conjunction conjuncts = new Conjunction(((GdlAnd) original).arity());
			
			for(int i=0; i<((GdlAnd)original).arity(); i++){
				conjuncts.add(replaceRelationName(((GdlAnd) original).get(i), relationNameToRemove, newRelationName));
			}
			
			return conjuncts.getAnd();
			
		}else{
			throw new RuntimeException("BackwardProver.replaceTrueWithNext() Error! unknown class " + original.getClass().getName());
		}
	}
	
	
	/**
	 * Creates a copy of the given original, but replaces every occurrence of 'true' by 'next
	 *
	 * @param original
	 * @return
	 */
	public static GdlLiteral replaceTrueWithNext(GdlLiteral original){
		
		
		if(original instanceof GdlNot){
			
			GdlLiteral body = ((GdlNot)original).getBody();
			
			GdlLiteral newBody = replaceTrueWithNext(body);
			
			if(newBody == null){
				return null;
			}
			
			return GdlPool.getNot(newBody);
			
		}else if(original instanceof GdlOr){
			
			Disjunction disjuncts = new Disjunction(((GdlOr) original).arity());
			
			for(int i=0; i<((GdlOr)original).arity(); i++){
				disjuncts.add(replaceTrueWithNext(((GdlOr) original).get(i)));
			}
			
			return disjuncts.getOr();
			
		}else if(original instanceof GdlDistinct){
			
			//GdlDistinct can only contain terms, so nothing needs to be replaced.
			return original; 
			
		}else if(original instanceof GdlSentence){
			
			GdlConstant relationName = ((GdlRelation) original).getName();
			
			
			if(relationName.equals(GdlPool.TRUE)){
				
				GdlRelation newRelation = GdlPool.getRelation(GdlPool.NEXT, ((GdlSentence) original).getBody());
				return newRelation;
			
			}else if(relationName.equals(GdlPool.DOES) || relationName.equals(LogicUtils._DOES)){

				throw new RuntimeException("BackwardProver.replaceTrueWithNext() Error! " + relationName);
				
			}else{
				
				return original;
			}
			
		}else if(original instanceof GdlAnd){
			
			Conjunction conjuncts = new Conjunction(((GdlAnd) original).arity());
			
			for(int i=0; i<((GdlAnd)original).arity(); i++){
				conjuncts.add(replaceTrueWithNext(((GdlAnd) original).get(i)));
			}
			
			return conjuncts.getAnd();
			
		}else{
			throw new RuntimeException("BackwardProver.replaceTrueWithNext() Error! unknown class " + original.getClass().getName());
		}
		
	}
	
	public static String createString(GdlLiteral literal){
		return createString(literal, 0);
	}
	
	private static String createString(GdlLiteral literal, int depth){
		
		String s;
		
		String indentation = "";
		for(int i=0; i<depth; i++){
			indentation += "    ";
		}
		
		if(literal instanceof GdlOr){
			
			s = indentation + "( or" + System.lineSeparator();
			for(int i=0; i<((GdlOr)literal).arity(); i++){
				s += createString(((GdlOr)literal).get(i), depth+1) + System.lineSeparator();
			}
			s += indentation + ")";
		
		}else if(literal instanceof GdlAnd){
			
			s = indentation + "( and" + System.lineSeparator();
			for(int i=0; i<((GdlAnd)literal).arity(); i++){
				s += createString(((GdlAnd)literal).get(i), depth+1) + System.lineSeparator();
			}
			s += indentation + ")";
			
		}else if(literal instanceof GdlNot){
			
			s = indentation + "( not" + System.lineSeparator();
			s += createString(((GdlNot)literal).getBody(), depth+1) + System.lineSeparator();
			s += indentation + ")";
			
		}else {
			s = indentation + literal.toString();
		}
		
		return s;
	}
	
	
}
