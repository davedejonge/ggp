package ddejonge.ggp.asp;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;

import ddejonge.ggp.tools.NotImplementedException;

public class NextRules {
	
	
	/**
	 * Creates a copy of each given rule but prepends every relation name with next_
	 * @param rules
	 */
	public static List<Gdl> convertToNextRules(List<Gdl> rules){
		
		ArrayList<Gdl> newRules = new ArrayList<Gdl>();
		
		for (Gdl gdl : rules) {
			if(gdl instanceof GdlRule){
				newRules.add(convertRule((GdlRule)gdl));
			}else if(gdl instanceof GdlSentence){
				newRules.add(convertSentence((GdlSentence)gdl));
			}else{
				throw new RuntimeException("NextRules.convertRule() Error! unhandled class: " + gdl.getClass().getName());
			}
		}
		
		return newRules;
		
	}

	
	public static GdlRule convertRule(GdlRule rule){
		
		GdlSentence newHead = convertSentence(rule.getHead());
		
		GdlLiteral[] newBody = new GdlLiteral[rule.getBody().size()];
		for(int i=0; i<rule.getBody().size(); i++){
			
			GdlLiteral literal = rule.getBody().get(i);
			newBody[i] = convertLiteral(literal);
		}
		
		return GdlPool.getRule(newHead, newBody);
		
	}
	
	/**
	 * Creates a copy of the given relation but prepends its name with next_
	 * @param rules
	 */	
	public static GdlSentence convertSentence(GdlSentence sentence){
		
		
		GdlConstant sentenceName = sentence.getName();
		
		GdlConstant newSentenceName;
		if(sentenceName.equals(GdlPool.TRUE)){
			newSentenceName = GdlPool.NEXT;
		}else if(sentenceName.equals(GdlPool.DOES) || sentenceName.equals(GdlPool.NEXT)){
			
			throw new RuntimeException("NextRules.convertSentence() Error! something went wrong: " + sentence);
		}else{
		
			newSentenceName = GdlPool.getConstant("next_" + sentenceName.toString());
		}
		
		GdlSentence newSentence;
		if(sentence instanceof GdlProposition){
			newSentence = GdlPool.getProposition(newSentenceName);
		}else if(sentence instanceof GdlRelation){
			newSentence = GdlPool.getRelation(newSentenceName, sentence.getBody());
		}else{
			throw new RuntimeException("NextRules.convertRule() Error! unhandled class: " + sentence.getClass().getName());
		}
		
		return newSentence;
		
	}
	
	public static GdlLiteral convertLiteral(GdlLiteral literal){
		
		
		/*if(literal instanceof GdlAnd){
			GdlAnd and = (GdlAnd) literal;
			
			throw new NotImplementedException();
			
		}else */if(literal instanceof GdlOr){
			GdlOr or = (GdlOr) literal;
			
			GdlLiteral[] newDisjuncts = new GdlLiteral[or.arity()];
			
			for (int i = 0; i < or.arity(); i++) {
				newDisjuncts[i] = convertLiteral(or.get(i));
			}
			
			return GdlPool.getOr(newDisjuncts);
			
		}else if(literal instanceof GdlNot){
			GdlNot not = (GdlNot) literal;
			
			return GdlPool.getNot(convertLiteral(not.getBody()));
			
		}else if(literal instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) literal;
			
			return convertSentence(sentence);
			
		}else if(literal instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) literal;
			
			return distinct;
			
		}else{
			throw new RuntimeException("NextRules.convertLiteral() Error! " + literal.getClass().getName());
		}
	}
	
	
}
