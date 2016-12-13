package ddejonge.ggp.asp;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;

import ddejonge.asp.ASPRunner;
import ddejonge.asp.Result;

public class ASPProver {
	
	
	List<GdlSentence> groundedBasePropositions;
	
	//Stores the game description as a list of ASP rules.
	List<String> basicAspRules;
	
	
	
	
	//CONSTRUCTOR
	public ASPProver(List<Gdl> gameDescription, List<GdlSentence> groundedBasePropositions, List<GdlSentence> groundedDoesPropositions){
		
		//Remove disjunctions from the description, because they cause errors.
		gameDescription = DeORer.run(gameDescription);
		
		this.groundedBasePropositions = groundedBasePropositions;
		
		//Convert the game description into a set of ASP rules.
		
		//create a list to store the ASP rules in.
		basicAspRules = new ArrayList<String>(gameDescription.size());
		
		for(Gdl gdl : gameDescription){
			
			if(gdl instanceof GdlRule){
				GdlRule rule = (GdlRule)gdl;
				
				GdlConstant type = rule.getHead().getName();
				
				// remove base, init and input rules.
				if(type.equals(GdlPool.BASE) || type.equals(GdlPool.INIT) || type.equals(GdlPool.INPUT) || type.equals(GdlPool.TERMINAL)|| type.equals(GdlPool.GOAL)){
					continue;
				}
				
				String string = GDL2ASPConverter.toAspString(rule.getHead());
				string += " :- ";
				string += GDL2ASPConverter.toAspString(rule.getBody());
				string += ".";
				basicAspRules.add(string);
				
			}else if(gdl instanceof GdlSentence){
				
				GdlConstant type = ((GdlSentence)gdl).getName();
				
				// remove base, init and input rules.
				if(type.equals(GdlPool.BASE) || type.equals(GdlPool.INIT) || type.equals(GdlPool.INPUT)){
					continue;
				}
				
				String string = GDL2ASPConverter.toAspString(gdl);
				string += ".";
				basicAspRules.add(string);
			}
			
		}
		
		basicAspRules.addAll(getActionRestrictions(groundedDoesPropositions));
		
		//Makes sure that any state can be realized.
		basicAspRules.addAll(getTrueProps());
		
		
	}
	
	
	private ArrayList<String> getActionRestrictions(List<GdlSentence> groundedDoesPropositions){
		
		ArrayList<String> actionRestrictions = new ArrayList<String>();
		
		//add isAction rules.
		HashSet<String> actions = new HashSet<String>();
		for(GdlSentence groundedDoesProp : groundedDoesPropositions){
			String s = GDL2ASPConverter.toAspString(groundedDoesProp.getBody().get(1));
			String s2 = "isAction(" + s + ").";
			
			
			if(! actions.contains(s2)){
				actionRestrictions.add(s2);
			}
			
			actions.add(s2);
		}
		
		actionRestrictions.add("1 {does(R,A) : isAction(A)} 1 :- role(R).");
		actionRestrictions.add(":- does(R,A), not legal(R,A)."); //should this be default negation or classical negation?
		
		return actionRestrictions;
	}
	
	//The returned strings represent the fact that any truth-assignment to the base propositions is possible.
	private ArrayList<String> getTrueProps(){
		
		ArrayList<String> strings = new ArrayList<String>();
		
		strings.add("{ true(P) : tdom(P)}.");
		
		
		for (GdlSentence sentence : groundedBasePropositions) {
			
			if(sentence.getName().equals(GdlPool.TRUE)){
				strings.add("tdom(P).".replace("P", GDL2ASPConverter.toAspString(sentence.get(0))));
			}
			
		}
		
		return strings;
		
	}
	
	
	
	//MAYBE IT'S BETTER TO MOVE THESE TWO METHODS TO MutexDetector
	
	// if vars == {?X0. ?X1, ?X2}  this method returns {domX0(?X0), domX1(?X1), domX2(?X2)}
	public static List<String> getDomainStrings(List<GdlVariable> vars){
		
		List<String> domainStrings = new ArrayList<String>(vars.size());
		
		for(GdlVariable var : vars){
			domainStrings.add(getDomainString(var, var));
		}
		
		return domainStrings;
	}
	
	// e.g. if var == ?X0  and term == 3
	// then this returns the string domX0(3)
	public static String getDomainString(GdlVariable var, GdlTerm term){
		
		/*
		String varString = var.toString(); //e.g. ?X0
		if(varString.startsWith("?")){
			varString = varString.substring(1);  //e.g. X0
		}*/
		
		return "dom" + GDL2ASPConverter.toAspString(var) + "(" + GDL2ASPConverter.toAspString(term) + ")";  //e.g. domX0(2)
	}
	
	
	
	

	
	
	

	
	
	public boolean proveTurnTaking(List<Gdl> rules, boolean print){
		
		
		ArrayList<String> inductionStep = new ArrayList<String>();
		
		
		//1. create a copy of each rule with legal in the head, and replace legal with next_legal
		//   do the same for all rules on which legal depends.
		List<Gdl> relevantRules = CollectDependentRules.collect(rules, GdlPool.LEGAL);
		
		//Prepend all relation names in these rules with 'next_'.
		List<Gdl> nextRules = NextRules.convertToNextRules(relevantRules);
		
		//Convert these rules to ASP strings.
		List<String> aspNextRules = GDL2ASPConverter.toAspStrings(nextRules);
		
		//Add them to the induction step rules.
		inductionStep.addAll(aspNextRules);
		
		
		/*
		//TODO: remove these lines!!
		inductionStep.add(":- true(control(xplayer)), true(control(oplayer)).");
		inductionStep.add("legal(R, noop2) :- role(R)");
		inductionStep.add("next_legal(R, noop2) :- role(R)");*/
		
		
		//2. generate the induction step
		inductionStep.add("active(R) :- role(R), {legal(R,A) : isAction(A)}>1.");
		inductionStep.add("one_active :- {active(R):role(R)} = 1.");
		inductionStep.add(":- not one_active.");
		
		inductionStep.add("next_active(R) :- role(R), {next_legal(R,A) : isAction(A)}>1.");
		inductionStep.add("next_one_active :- {next_active(R):role(R)} = 1.");
		inductionStep.add(" :- next_one_active.");
		
		
		return prove(inductionStep, print);
		
	}
	
	
	/**
	 * Returns true if the given list of rules together with the basic rules form an UNsatisfiable program, returns false if it is satisfiable.<br/>
	 * Returns null if Clingo failed because of some error.
	 * @param rules
	 * @param print
	 * @return
	 */
	public Boolean prove(ArrayList<String> rules, boolean print){
		
		
		ArrayList<String> aspRules = new ArrayList<String>(basicAspRules.size() + rules.size() + 2);
		aspRules.addAll(basicAspRules);
		
		
		// Note: we assume we already know that the hypothesis holds for the initial state,
		// so we only need to prove the induction step.
		aspRules.addAll(rules);
		
		aspRules.add("#show true/1.");
		aspRules.add("#show does/2.");
		
		/*
		for (String aspRule : aspRules) {
			System.out.println(aspRule);
		}
		*/
		
		long deadline = System.currentTimeMillis() + 60*1000; //set the deadline to 1 min.
		
		//System.out.println("***");
		//System.out.println();
		//long l1 = System.currentTimeMillis();
		Result result = ASPRunner.findModels(aspRules, 1, print, deadline);
		//long l2 = System.currentTimeMillis();
		//System.out.println("finished in " + (l2-l1) + " ms.");
		
		if(result.satisfiable == null){
			return null;
		}
		
		if(result.satisfiable){
			return false;
		}else{
			return true;
		}
		
	}
	
	
	
	
	public List<GdlSentence> getGroundedBasePropositions() {
		return groundedBasePropositions;
	}


	
	

}
