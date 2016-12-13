package ddejonge.ggp.asp;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;

import ddejonge.ggp.tools.NotImplementedException;


public class CollectDependentRules {

	
	public static List<Gdl> collect(List<Gdl> description, GdlConstant relationName){
		
		List<Gdl> subsetToFill = new ArrayList<Gdl>();
		
		for(Gdl gdl : description){
			
			GdlSentence ruleHead = getRuleHead(gdl);
			
			if(ruleHead.getName().equals(relationName)){
				collect(description, subsetToFill, ruleHead);
			}
			
		}
		
		
		return subsetToFill;
	}
	
	/**
	 * Collect all rules that the given atom depends on.
	 * @param description
	 * @param headToUnifyWith
	 * @return
	 */
	public static List<Gdl> collect(List<Gdl> description, GdlSentence atom){
		
		List<Gdl> subsetToFill = new ArrayList<Gdl>();
		
		collect(description, subsetToFill, atom);
		
		return subsetToFill;
	}
	
	private static void collect(List<Gdl> description, List<Gdl> subsetToFill, GdlLiteral formula){
		
		/*if(formula instanceof GdlAnd){
			
			throw new NotImplementedException();
			
		}else */if(formula instanceof GdlOr){
			GdlOr or = (GdlOr) formula;
			
			for (int i = 0; i < or.arity(); i++) {
				collect(description, subsetToFill, or.get(i));
			}
			
			
		}else if(formula instanceof GdlNot){
			GdlNot not = (GdlNot) formula;
			
			collect(description, subsetToFill, not.getBody());
			
		}else if(formula instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) formula;
			
			collectSentence(description, subsetToFill, sentence);
			
		}else if(formula instanceof GdlDistinct){
			return;
			
		}else{
			throw new RuntimeException("CollectDependentRules.collect() Error! " + formula.getClass().getName());
		}
		
		
		
		
		
		
		
		

	}
	
	
	private static void collectSentence(List<Gdl> description, List<Gdl> subsetToFill, GdlSentence sentence){
		
		
		if(sentence.getName().equals(GdlPool.TRUE) || sentence.getName().equals(GdlPool.DOES)){
			return;
		}
		
		//Find any rule of which the head unifies with the given atom.
		for(Gdl gdl : description){
			
			
			GdlSentence ruleHead = getRuleHead(gdl);
			
			Substitution sub = Unifier.unify(ruleHead, sentence);
			if(sub == null){
				continue;
			}
					
			//add this rule to the subset.
			if(!subsetToFill.contains(gdl)){
				subsetToFill.add(gdl);
				
				if(gdl instanceof GdlRule){
					for(GdlLiteral bodyElement : ((GdlRule) gdl).getBody()){
						collect(description, subsetToFill, bodyElement);
					}
						
				}
			}
					

			
		}
	}
	
	private static GdlSentence  getRuleHead(Gdl gdl){
		
		GdlSentence ruleHead;
		
		if(gdl instanceof GdlRule){
			
			ruleHead = ((GdlRule) gdl).getHead();
		
		}else if(gdl instanceof GdlSentence){
			
			ruleHead = (GdlSentence)gdl;
			
		}else{
			throw new RuntimeException("CollectDependentRules.collectSentence() Error! unhandled class: "+ gdl.getClass().getName());
		}
		
		return ruleHead;
		
	}
	
	
}
