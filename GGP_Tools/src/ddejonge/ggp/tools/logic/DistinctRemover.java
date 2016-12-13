package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRule;

/**
 * Removes all rules that have a non-satisfied distinct.
 * For each rule, remove any satisfied distincts.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class DistinctRemover {

	public static List<GdlRule> removeDistincts(List<GdlRule> groundedRules){
		
		List<GdlRule> cleanList = new ArrayList<GdlRule>();
		
		for(GdlRule rule : groundedRules){
			
			if(!rule.isGround()){
				throw new RuntimeException("DistinctRemover.removeDistincts() Error! rules are not ground!");
			}
			
			GdlRule cleanRule = getCleanRule(rule);
			
			if(cleanRule != null){
				cleanList.add(cleanRule);
			}
			
		}
		
		return cleanList;
	}
	
	
	/**
	 * Returns a rule with all GdlDistinct objects removed. <br/>
	 * Returns null if the original rule contained a non-satisfied distinct (i.e. of the form a!=a).
	 * @param rule
	 * @return
	 */
	private static GdlRule getCleanRule(GdlRule rule){
		
		List<GdlLiteral> cleanBody = new ArrayList<>(rule.getBody().size());
		
		for(GdlLiteral literal : rule.getBody()){
			
			if(literal instanceof GdlDistinct){
				
				if(((GdlDistinct) literal).getArg1().equals(((GdlDistinct) literal).getArg2())){
					return null;
				}
				
			}else{
				cleanBody.add(literal);
			}
			
		}
		
		return GdlPool.getRule(rule.getHead(), cleanBody);
		
	}
	
	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
