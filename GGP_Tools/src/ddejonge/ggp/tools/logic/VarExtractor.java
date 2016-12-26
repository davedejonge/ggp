package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;

public class VarExtractor {

	//STATIC FIELDS

	//STATIC METHODS
	
	/**
	 * Returns a list that contains the GdlVariables exactly in the order in which they appear in the given literal.
	 * This means the returned list may contain the same variable more than once.
	 * @param literal
	 * @return
	 */
	public static List<GdlVariable> extractVariableList(GdlLiteral literal){
		
		ArrayList<GdlVariable> returnList = new ArrayList<>();
		
		extractVariableList(literal, returnList);
		
		return returnList;
	}
	
	public static void extractVariableList(GdlLiteral literal, List<GdlVariable> listToFill){
		
		if(literal instanceof GdlOr){
			GdlOr or = (GdlOr) literal;
			
			for (int i = 0; i < or.arity(); i++) {
				extractVariableList(or.get(i), listToFill);
			}
			
			
		}else if(literal instanceof GdlNot){
			GdlNot not = (GdlNot) literal;
			
			extractVariableList(not.getBody(), listToFill);
			
		}else if(literal instanceof GdlProposition){
			GdlProposition proposition = (GdlProposition) literal;
			
			return;
			
		}else if(literal instanceof GdlRelation){
			GdlRelation relation = (GdlRelation) literal;
			
			for (int i = 0; i < relation.arity(); i++) {
				extractVariableList(relation.get(i), listToFill);
			}
			
		}else if(literal instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) literal;
			
			extractVariableList(distinct.getArg1(), listToFill);
			extractVariableList(distinct.getArg2(), listToFill);
			
			return;
			
		}else{
			throw new RuntimeException("VarExtractor.extractVariables() Error! " + literal.getClass().getName());
		}
	}
	
	public static void extractVariableList(GdlTerm term, List<GdlVariable> listToFill){
		
		if(term instanceof GdlVariable){
			
			listToFill.add((GdlVariable)term);
		
		}else if(term instanceof GdlConstant){
			
			return;
			
		}else if(term instanceof GdlFunction){
		
			GdlFunction function = (GdlFunction)term;
			
			for(GdlTerm innerTerm : function.getBody()){
				extractVariableList(innerTerm, listToFill);
			}
			
		}else{
			throw new RuntimeException("VarExtractor.extractVariableList() Error! unknown class: " + term.getClass().getSimpleName());
		}
	}
	
	/**
	 * Fills the given set with the variables that appear in the given literal.
	 * @param literal
	 * @param setToFill
	 */
	public static void extractVariables(GdlLiteral literal, Set<GdlVariable> setToFill){
		
		if(literal instanceof GdlOr){
			GdlOr or = (GdlOr) literal;
			
			for (int i = 0; i < or.arity(); i++) {
				extractVariables(or.get(i), setToFill);
			}
			
			
		}else if(literal instanceof GdlNot){
			GdlNot not = (GdlNot) literal;
			
			extractVariables(not.getBody(), setToFill);
			
		}else if(literal instanceof GdlProposition){
			GdlProposition proposition = (GdlProposition) literal;
			
			return;
			
		}else if(literal instanceof GdlRelation){
			GdlRelation relation = (GdlRelation) literal;
			
			for (int i = 0; i < relation.arity(); i++) {
				extractVariables(relation.get(i), setToFill);
			}
			
		}else if(literal instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) literal;
			
			extractVariables(distinct.getArg1(), setToFill);
			extractVariables(distinct.getArg2(), setToFill);
			
			return;
			
		}else{
			throw new RuntimeException("VarExtractor.extractVariables() Error! " + literal.getClass().getName());
		}
	}
	
	
	public static void extractVariables(GdlTerm term, Set<GdlVariable> setToFill){
		
		if(term instanceof GdlVariable){
			
			setToFill.add((GdlVariable)term);
		
		}else if(term instanceof GdlConstant){
			
			return;
			
		}else if(term instanceof GdlFunction){
		
			GdlFunction function = (GdlFunction)term;
			
			for(GdlTerm innerTerm : function.getBody()){
				extractVariables(innerTerm, setToFill);
			}
			
		}else{
			throw new RuntimeException("VarExtractor.extractVariables() Error! unknown class: " + term.getClass().getSimpleName());
		}
	}

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
