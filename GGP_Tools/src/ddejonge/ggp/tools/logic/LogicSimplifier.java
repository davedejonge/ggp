package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;

public class LogicSimplifier {
	
	
	/**
	 * 
	 * @param original
	 * @param falsePredicates A list of predicates that are known to be false (e.g. terminal conditions)
	 * @return
	 */
	public static GdlLiteral simplify(GdlLiteral original,  Set<? extends GdlLiteral> truePredicates, Set<? extends GdlLiteral> falsePredicates){
		
		if(truePredicates != null && truePredicates.contains(original)){
			return LogicUtils.VERUM;
		}
		
		if(falsePredicates != null && falsePredicates.contains(original)){
			return LogicUtils.FALSUM;
		}
		
		if(original instanceof GdlAnd){
			GdlAnd and = (GdlAnd) original;
			
			return simplifyAnd(and, truePredicates, falsePredicates);
			
		}else if(original instanceof GdlOr){
			GdlOr or = (GdlOr) original;
			
			return simplifyOr(or, truePredicates, falsePredicates);
			
		}else if(original instanceof GdlNot){
			GdlNot not = (GdlNot) original;
			
			return simplifyNot(not, truePredicates, falsePredicates);
			
		}else if(original instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) original;
			
			return sentence;
			
		}else if(original instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) original;
			
			return simplifyDistinct(distinct);
			
		}else{
			throw new RuntimeException("LogicSimplifier.simplify() Error! " + original.getClass().getName());
		}
		
		
	}
	
	private static GdlLiteral simplifyAnd(GdlAnd and, Set<? extends GdlLiteral> truePredicates, Set<? extends GdlLiteral> falsePredicates){
		
		Conjunction conjunction = new Conjunction(and.arity());
		
		for (int i = 0; i < and.arity(); i++) {
			
			//first simplify the conjunct.
			GdlLiteral simplifiedConjunct = simplify(and.get(i), truePredicates, falsePredicates);
			
			//then add it to the conjunction
			conjunction.add(simplifiedConjunct);
		}
		
		if(conjunction.size() == 1){
			return conjunction.get(0);
		}
		
		//If the conjunction is of the form (t1 V t2 V t3) ^ (t1 V t2)
		//Then it can be simplified to (t1 V t2)
		simplifyConjunction(conjunction);
		
		return conjunction.getAnd();
		
	}
	
	private static GdlLiteral simplifyOr(GdlOr or, Set<? extends GdlLiteral> truePredicates, Set<? extends GdlLiteral> falsePredicates){
		
		Disjunction disjunction = new Disjunction(or.arity());
		
		
		for (int i = 0; i < or.arity(); i++) {
			GdlLiteral simplifiedDisjunct = simplify(or.get(i), truePredicates, falsePredicates);
			disjunction.add(simplifiedDisjunct);
		}
		
		if(disjunction.size() == 1){
			return disjunction.get(0);
		}
		
		//If the disjunction is of the form (t1 ^ t2 ^ t3) v (t1 ^ t2)
		//Then it can be simplified to (t1 ^ t2)
		simplifyDisjunction(disjunction);
		
		return disjunction.getOr();
		
	}
	
	private static GdlLiteral simplifyNot(GdlNot not, Set<? extends GdlLiteral> truePredicates, Set<? extends GdlLiteral> falsePredicates){
		
		GdlLiteral body = not.getBody();
		
		GdlLiteral simplifiedBody = simplify(body, truePredicates, falsePredicates);
		
		//Apply the law ~(~x) = x
		if(simplifiedBody instanceof GdlNot){
			GdlLiteral bodyOfBody = ((GdlNot)simplifiedBody).getBody();
			return bodyOfBody;
		}
		
		
		//apply the law ~T = F
		if(simplifiedBody.equals(LogicUtils.VERUM)){
			return LogicUtils.FALSUM;
		}
		
		//apply the law ~F = T
		if(simplifiedBody.equals(LogicUtils.FALSUM)){
			return LogicUtils.VERUM;
		}
		
		return GdlPool.getNot(simplifiedBody);
		
	}
	
	
	private static GdlLiteral simplifyDistinct(GdlDistinct distinct){
		
		if(distinct.isGround()){
			if(distinct.getArg1().equals(distinct.getArg2())){
				return LogicUtils.FALSUM;
			}else{
				return LogicUtils.VERUM;
			}
		}
		
		return distinct;
		
		
	}
	
	
	
	/**
	 * If 'strong' is 'true' then we assume that the variables in the disjuncts are not related.
	 * @param disjunction
	 * @param strong
	 */
	public static void simplifyDisjunction(Disjunction disjunction){
		
		//test if any of the disjuncts is a subset of any other disjunct.
		//If so, then the containing disjunct can be removed.
		//
		// E.g.  					a v b v (t1 ^ t2 ^ t3) v (t1 ^ t2)
		// can be simplified to  	a v b v (t1 ^ t2)
		//
		// Also 					a v b v (a ^ c)
		// becomes 					a v b
		
		boolean[] toRemove = new boolean[disjunction.size()];
		int numToRemove = 0;
		
		for(int i=0; i<disjunction.size(); i++){
			for(int j=0; j<disjunction.size(); j++){
				if(i == j){
					continue;
				}
				
				if(toRemove[j]){
					//We have already determined that the j-th conjunct must be removed in an earlier iteration.
					continue;
				}
				
				GdlLiteral element0 = disjunction.get(j); //The j-th element is the potential container.
				
				//Check if the j-th element is an AND
				if( ! (element0 instanceof GdlAnd)){
					continue;
				}
				GdlAnd and0 = (GdlAnd)element0;
				
				
				boolean contains = false;  			//will be set to true in the case (a ^ b ^ c) v (a ^ b)
				
				//Check if the i-th element is an OR
				GdlLiteral element1 = disjunction.get(i);
				if(element1 instanceof GdlAnd){
					
					GdlAnd and1 = (GdlAnd)element1;
					
					if(and1.arity() > and0.arity()){
						continue;
					}
					
					contains = LogicUtils.containsConjunction(and0, and1);
					
				}else{
					
					if(LogicUtils.containsElement((GdlAnd)element0, element1)){
						contains = true;
					}
					
				}
				
				if(contains){
					
					//Mark element0 as an element that can be removed.
					toRemove[j] = true;
					numToRemove++;
					
				}
				
			}
		}
		
		//Now remove those elements that have been marked to be removed.
		if(numToRemove > 0){
			
			//Otherwise, construct a new object.
			ArrayList<GdlLiteral> newDisjuncts = new ArrayList<GdlLiteral>(disjunction.size());
			for(int i=0; i<disjunction.size(); i++){
				
				if(!toRemove[i]){
					newDisjuncts.add(disjunction.get(i));
				}
			}
			
			disjunction.clear();
			disjunction.addAll(newDisjuncts);
		}

	}
	
	
	/**
	 * 	If any of the conjuncts is a subset of any other conjunct
	 *	 then the containing conjunct can be removed.
	 *	
	 *	E.g.  					a ^ b  ^ (t1 V t2 V t3) ^ (t1 V t2) <br/>
	 *	can be simplified to  	a ^ b  ^ (t1 V t2)					<br/>
	 *																<br/>
	 *	Also 					a ^ b ^ (a V c)						<br/>
	 *	becomes 				a ^ b							
	 * 
	 * 
	 * @param conjunction
	 */
	static void simplifyConjunction(Conjunction conjunction){
		
		//First we collect all negated disjunctions and put them together into one negated disjunction.
		//
		// e.g. (a v b) ^ ~(c v d) ^ ~(d v e)  becomes:
		// 		(a v b) ^ ~(c v d v e)
		//
		// Then we can apply the following rule:
		//   (a v b v c) ^  ~(a v d)  ==  (b v c) ^  ~(a v d)
		//
		
		Disjunction negativeDisjuncts = new Disjunction();
		for(int j=0; j<conjunction.size(); j++){
			
			GdlLiteral conjunct = conjunction.get(j);
			
			if(conjunct instanceof GdlNot){
				if(((GdlNot)conjunct).getBody() instanceof GdlOr){
					
					GdlOr dis = (GdlOr)((GdlNot)conjunct).getBody();
					for (int i = 0; i < dis.arity(); i++) {
						negativeDisjuncts.add(dis.get(i));
					}
					conjunction.remove(j);
					j--;
				}
			}			
		}
		
		//Put all elements that appear in a negated disjunction into one disjunction.
		GdlOr negativeDisjunction = negativeDisjuncts.getOr();
		
		if(negativeDisjuncts.size() > 0){
			//and put its negation back into the conjunction
			conjunction.add(GdlPool.getNot(negativeDisjunction));
		}
		
		
		//Now check whether any of the disjunctions inside the conjunction contains any other disjunction in the conjunction.
		// e.g check if the conjunction is of the form (t1 V t2 V t3) ^ (t1 V t2)
		boolean[] toRemove = new boolean[conjunction.size()];
		int numToRemove = 0;
		
		for(int i=0; i<conjunction.size(); i++){
			for(int j=0; j<conjunction.size(); j++){
				if(i == j){
					continue;
				}
				
				if(toRemove[j]){
					//We have already determined that the j-th conjunct must be removed in an earlier iteration.
					continue;
				}
				
				//Check if the j-th element is an OR
				GdlLiteral element0 = conjunction.get(j); //The j-th element is the potential container.
				if( ! (element0 instanceof GdlOr)){
					continue;
				}
				GdlOr or0 = (GdlOr)element0;
				
				
				boolean contains = false;  			//will be set to true in the case (a v b v c) ^ (a v b)
				
				//Check if the i-th element is an OR
				GdlLiteral element1 = conjunction.get(i);
				if(element1 instanceof GdlOr){
					
					GdlOr or1 = (GdlOr)element1;
					
					if(or1.arity() > or0.arity()){
						continue;
					}
					
					contains = LogicUtils.containsDisjunction(or0, or1);
					
				}else{
					
					if(LogicUtils.containsElement((GdlOr)element0, element1)){
						contains = true;
					}
					
				}
				
				if(contains){
					
					//Mark element0 as an element that can be removed.
					toRemove[j] = true;
					numToRemove++;
					
				}
				
			}
		}
		
		//Now remove those elements that have been marked to be removed.
		if(numToRemove > 0){
			
			//Otherwise, construct a new object.
			ArrayList<GdlLiteral> newConjuncts = new ArrayList<GdlLiteral>(conjunction.size());
			for(int i=0; i<conjunction.size(); i++){
				
				if(!toRemove[i]){
					newConjuncts.add(conjunction.get(i));
				}
			}
			
			conjunction.clear();
			conjunction.addAll(newConjuncts);
		}
		

		
		//Finally, test if the negated disjunction contains any of the positive disjunctions, or vice versa.
		
		if(negativeDisjunction.arity() == 0){
			return;
		}
		
		
		GdlOr[] replacements = new GdlOr[conjunction.size()];
		
		for (int i = 0; i < conjunction.size(); i++) {
			
			if( ! (conjunction.get(i) instanceof GdlOr)){
				continue;
			}
				
			GdlOr positiveDisjunction = (GdlOr)conjunction.get(i);
			
			
			
			
			
			ArrayList<GdlLiteral> cleanDisjunction = new ArrayList<GdlLiteral>();
			
			for(int k=0; k<positiveDisjunction.arity(); k++){
				
				GdlLiteral elm = positiveDisjunction.get(k);
				
				if( ! LogicUtils.containsElement(negativeDisjunction, elm)){
					cleanDisjunction.add(elm);
				}
				
			}
			
			//if the clean disjunction is empty, then it is a falsum, which makes
			//the entire conjunction false, so we can stop directly.
			if(cleanDisjunction.size() == 0){
				conjunction.clear();
				conjunction.add(LogicUtils.FALSUM);
				return;
			}
			
			GdlOr replacement = GdlPool.getOr(cleanDisjunction);
			replacements[i] = replacement;
			
			
			/*
			
			if(positiveDisjunction.arity() > negativeDisjunction.arity()){
				
				if(containsDisjunction(positiveDisjunction, negativeDisjunction)){
					//Apply:   (a v b v c v d) ^  ~(a v b)  ==  (c v d) ^ ~(a v b)
					
					ArrayList<GdlLiteral> cleanDisjunction = new ArrayList<GdlLiteral>();
					
					for(int k=0; k<positiveDisjunction.arity(); k++){
						
						GdlLiteral elm = positiveDisjunction.get(k);
						
						if( ! containsElement(negativeDisjunction, elm)){
							cleanDisjunction.add(elm);
						}
						
					}
					
					GdlOr replacement = GdlPool.getOr(cleanDisjunction);
					replacements[i] = replacement;
					
				}
				
			}else{
				
				if(containsDisjunction(negativeDisjunction, positiveDisjunction)){
					
					//Apply:   ~(a v b v c) ^ (a v b)  ==  F
					conjunction.clear();
					conjunction.add(FALSUM);
					return;
				}
			}
			*/
		}
		
		for (int i = 0; i < replacements.length; i++) {
			
			if(replacements[i] != null){
				conjunction.set(i, replacements[i]);
			}
		}
		

	}
	
	
	//The method below is currently not used. 
	//Moreover, note that the version of simplifyConjunction() above, which takes a list as argument
	// is more advanced.
	/*
	public static GdlAnd simplifyConjunction(GdlAnd conjunction){
		
		
		//test if any of the conjuncts is a subset of any other conjunct.
		//If so, then the containing conjunct can be removed.
		//
		// E.g.  					a ^ b  ^ (t1 V t2 V t3) ^ (t1 V t2)
		// can be simplified to  	a ^ b  ^ (t1 V t2)
		//
		// Also 					a ^ b ^ (a V c)
		// becomes 					a ^ b
		
		boolean[] toRemove = new boolean[conjunction.arity()];
		int numToRemove = 0;
		
		for(int i=0; i<conjunction.arity(); i++){
			for(int j=0; j<conjunction.arity(); j++){
				if(i == j){
					continue;
				}
				
				if(toRemove[j]){
					//We have already determined that the j-th conjunct must be removed in an earlier iteration.
					continue;
				}
				
				boolean contains = false;
				
				GdlLiteral element0 = conjunction.get(j); //The j-th element is the potential container.

				if( ! (element0 instanceof GdlOr)){
					continue;
				}
				
				GdlOr or0 = (GdlOr)element0;
				
				GdlLiteral element1 = conjunction.get(i);
				
				if(element1 instanceof GdlOr){
					
					GdlOr or1 = (GdlOr)element1;
					
					if(or1.arity() > or0.arity()){
						continue;
					}
					
					contains = true;
					for(int k=0; k<or1.arity(); k++){
						if( ! LogicUtils.containsElement(or0, or1.get(k))){
							contains = false;
							break;
						}
					}
					
				}else{
					
					if(LogicUtils.containsElement((GdlOr)element0, element1)){
						contains = true;
					}
					
				}
				
				if(contains){
					
					//Now, the container can be removed.
					toRemove[j] = true;
					numToRemove++;
				}
				
			}
		}
		
		
		//If no conjunct can be removed then return the original object.
		if(numToRemove == 0){
			return conjunction;
		}
		
		//Otherwise, construct a new object.
		ArrayList<GdlLiteral> newConjuncts = new ArrayList<GdlLiteral>(conjunction.arity());
		for(int i=0; i<conjunction.arity(); i++){
			
			if(!toRemove[i]){
				newConjuncts.add(conjunction.get(i));
			}
			
		}
		
		return AndPool.getAnd(newConjuncts);
	}
	*/
	
}
