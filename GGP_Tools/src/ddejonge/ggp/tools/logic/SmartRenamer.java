package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlVariable;
import org.ggp.base.util.prover.aima.renamer.VariableRenamer;

import ddejonge.ggp.NotImplementedException;

public class SmartRenamer {

	
	//Makes sure that variables in different disjuncts have the same name, so that we can easily 
	// determine that they are equal.
	
	/**
	 * 
	 * @param formula This formula cannot be part of a larger formula!
	 */
	public static GdlLiteral rename(GdlLiteral formula){
		
		VariableRenamer simpleRenamer = new VariableRenamer();
		
		if(formula instanceof GdlAnd){
			GdlAnd and = (GdlAnd) formula;
			
			//rename and collect all variables in the first conjunct.
			//in the second conjunct, rename variables, however, variables that also appear in any earlier
			// conjunct may not be changed.
			// we can't rename x to y if y already appears in any of the earlier conjuncts.
			
			ArrayList<GdlVariable> usedVars = new ArrayList<GdlVariable>();
			
			for (int i = 0; i < and.arity(); i++) {
				rename(and.get(i), usedVars, simpleRenamer);
			}
			
			throw new RuntimeException("SmartRenamer.rename() Error! not implemented.");
			
		}else if(formula instanceof GdlOr){
			GdlOr or = (GdlOr) formula;
			
			Disjunction disjunction = new Disjunction();
			for (int i = 0; i < or.arity(); i++) {
				disjunction.add(rename(formula));
			}
			
			return disjunction.getLiteral();
			
		}else if(formula instanceof GdlNot){
			GdlNot not = (GdlNot) formula;
			
			GdlLiteral renamedBody = rename(not.getBody());
			
			return GdlPool.getNot(renamedBody);
			
			
		}else if(formula instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) formula;
			
			throw new RuntimeException("SmartRenamer.rename() Error! not implemented. GdlSentence");
			
		}else if(formula instanceof GdlProposition){
			GdlProposition proposition = (GdlProposition) formula;
			
			throw new RuntimeException("SmartRenamer.rename() Error! not implemented. GdlProposition");
			
		}else if(formula instanceof GdlRelation){
			GdlRelation relation = (GdlRelation) formula;
			
			throw new RuntimeException("SmartRenamer.rename() Error! not implemented. GdlRelation");
			
		}else if(formula instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) formula;
			
			throw new RuntimeException("SmartRenamer.rename() Error! not implemented. GdlDistinct");
			
		}else{
			throw new RuntimeException("SmartRenamer.simplify() Error! " + formula.getClass().getName());
		}
		
	}
	
	
	
}
