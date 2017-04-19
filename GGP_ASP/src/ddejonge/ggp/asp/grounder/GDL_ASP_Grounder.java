package ddejonge.ggp.asp.grounder;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.propnet.factory.flattener.PropNetFlattener;

import ddejonge.asp.grounder.Grounder;
import ddejonge.ggp.asp.ASP2GDLConverter;
import ddejonge.ggp.asp.GDL2ASPConverter;
import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;

public class GDL_ASP_Grounder {

	
	//STATIC FIELDS
	public static List<Gdl> ground(String pathToGringo, List<Gdl> rules){
		
		long l1 = System.currentTimeMillis();
		
		rules = DeORer.run(rules);
		
		//convert GDL to ASP
		List<String> aspRules = GDL2ASPConverter.toAspStrings(rules);
		
		/*
		System.out.println();
		System.out.println("ASP RULES:");
		for (String s : aspRules) {
			System.out.println(s);
		}*/
		
		
		//We need to add some additional rules to make sure that GRINGO knows that NEXT and TRUE are connected to each other.
		String additonalRule1 = "true(X) :- next(X).";
		String additonalRule2 = "true(X) :- init(X).";
		String additonalRule3 = "does(X,Y) :- legal(X,Y).";
		aspRules.add(additonalRule1);
		aspRules.add(additonalRule2);
		aspRules.add(additonalRule3);
		
		long l2 = System.currentTimeMillis();
		System.out.println("step 1 (converting GDL to ASP) finished in " + (l2-l1) + " ms.");
		
		//apply GRINGO
		List<String> groundedASPRules = Grounder.ground(pathToGringo, aspRules);
		
		/*
		System.out.println();
		System.out.println("GROUNDED ASP RULES:");
		for (String groundedASPRule : groundedASPRules) {
			System.out.println(groundedASPRule);
		}*/
		
		
		
		
		
		//convert the base propositions to GDLRelations.
		l1 = System.currentTimeMillis();
		List<Gdl> groundedGdlRules = new ArrayList<>();
		for(String groundedASPRule : groundedASPRules){
			
			//remove the period at the end of the rule.
			String aspSentenceString = groundedASPRule.replace(".", ""); 
			
			if(aspSentenceString.contains(":-")){
				
				GdlRule rule = ASP2GDLConverter.parseRule(aspSentenceString);
				
				//Remove the additional rules. Obviously a real GDL rule cannot have TRUE or DOES in its head,
				// therefore such a rule is an additional rule that we have added for the grounding process only.
				if(rule.getHead().getName().equals(GdlPool.TRUE) || rule.getHead().getName().equals(GdlPool.DOES)){
					continue;
				}
				
				groundedGdlRules.add(rule);
			
			}else{
			
				//convert the string into a GdlSentence
				GdlSentence atom = ASP2GDLConverter.parseSentence(aspSentenceString);
				
				
				//Remove the additional rules. Obviously a real GDL rule cannot have TRUE or DOES in its head,
				// therefore such a rule is an additional rule that we have added for the grounding process only.
				if(atom.getName().equals(GdlPool.TRUE) || atom.getName().equals(GdlPool.DOES)){
					continue;
				}
				
				
				groundedGdlRules.add(atom);
			}
		}
		
		l2 = System.currentTimeMillis();
		
		System.out.println("step 4 (converting ASP back to GDL) finished in " + (l2-l1) + " ms.");
		System.out.println(groundedGdlRules.size() + " grounded GDL rules created.");
		/*
		System.out.println();
		System.out.println("GROUNDED GDL RULES:");
		for (Gdl groundedGdlRule : groundedGdlRules) {
			System.out.println(groundedGdlRule);
		}
		*/
		
		// There are still some  errors in the implementation of this grounder!
		adsf;
		
		return groundedGdlRules;
		
	}
	
	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
