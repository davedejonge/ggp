package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;



public class Conjunction{

	ArrayList<GdlLiteral> conjuncts;
	
	
	public Conjunction(){
		conjuncts = new ArrayList<GdlLiteral>(16);
	}
	
	public Conjunction(int capacity){
		conjuncts = new ArrayList<GdlLiteral>(capacity);
	}
	
	//COPY CONSTRUCTOR
	public Conjunction(Conjunction original){
		conjuncts = new ArrayList<GdlLiteral>(original.conjuncts);
	}
	
	public void add(GdlLiteral literalToAdd){
		addToConjuncts(conjuncts, literalToAdd);
	}
	
	public void addAll(List<GdlLiteral> literalsToAdd){
		for(int i=0;i<literalsToAdd.size(); i++){
			addToConjuncts(conjuncts, literalsToAdd.get(i));
		}
	}
	
	
	public int size(){
		return conjuncts.size();
	}
	
	public GdlLiteral get(int index){
		return conjuncts.get(index);
	}
	
	public GdlLiteral set(int index, GdlLiteral literal){
		return conjuncts.set(index, literal);
	}
	
	public GdlLiteral remove(int index){
		return conjuncts.remove(index);
	}
	
	public void clear(){
		conjuncts.clear();
	}
	
	public GdlAnd getAnd(){
		
		if( ! MutexStorage.test(this)){
			
			this.conjuncts.clear();
			this.conjuncts.add(LogicUtils.FALSUM);
		}
		
		return AndPool.getAnd(conjuncts);
	}
	
	public GdlLiteral getLiteral(){
		
		if(conjuncts.size() == 0){
			return LogicUtils.VERUM;
		}
		
		if(conjuncts.size() == 1){
			return conjuncts.get(0);
		}
		
		if( ! MutexStorage.test(this)){
			return LogicUtils.FALSUM;
		}
		
		return AndPool.getAnd(conjuncts);
		
	}
	
	
	public static void addToConjuncts(List<GdlLiteral> conjuncts, GdlLiteral literalToAdd){
		
		//If the list of conjuncts contains the FALSUM then it doesn't make sense to add anything to it.
		// we only look at the first element of the conjuncts-list because we assume that if it contains the 
		// falsum then it should be the only element of this list anyway.
		if(conjuncts.size()>0 && conjuncts.get(0) instanceof GdlOr && ((GdlOr)conjuncts.get(0)).arity() == 0 ){
			return;
		}
		
		if(literalToAdd == null){
			return;
		}
		
		
		
		//Adding VERUM to a conjunction doesn't have any effect.
		// However, we don't have to check this explicitly, because it is already taken care of
		// because it is a special case of  'literalToAdd instanceof GdlAnd'
		
		
		/*
		// Apply the rule   a ^ (a v b) == a  
		// 1. If we have a ^ b ^ c, and we try to add (a v e) then we don't need to add it.
		if(literalToAdd instanceof GdlOr){
			
			GdlOr or = (GdlOr)literalToAdd;
			
			for(int i=0; i<conjuncts.size(); i++){
				for(int j=0; j<or.arity(); j++){
					if(conjuncts.get(i).equals(or.get(j))){
						return;
					}
				}
			}
		}
		
		// 2. If we have (a v e) ^ b ^ c, and we try to add a then we don't need to add it.
		for(int i=0; i<conjuncts.size(); i++){
			
			if(conjuncts.get(i) instanceof GdlOr){
				
				GdlOr or = (GdlOr)conjuncts.get(i);
				
				for(int j=0; j<or.arity(); j++){
					if(literalToAdd.equals(or.get(j))){
						
						conjuncts.set(i, literalToAdd);
						
						return;
					}
				}
			}
			
		}*/
		
		
		if(literalToAdd instanceof GdlAnd){
			GdlAnd innerAnd = (GdlAnd)literalToAdd;
			
			for(int j=0; j<innerAnd.arity(); j++){
				addToConjuncts(conjuncts, innerAnd.get(j));
			}
			
		}else if(literalToAdd instanceof GdlOr && ((GdlOr)literalToAdd).arity() == 0){
			//adding a FALSUM to an AND results in the entire AND to be false,
			//so we return an AND that only contains the FALSUM.
			
			conjuncts.clear();
			conjuncts.add(literalToAdd);
			
		}else if(literalToAdd instanceof GdlOr && ((GdlOr)literalToAdd).arity() == 1){
			
			//if there is only one value we may as well add that value itself, rather than the GdlOr object.
			addToConjuncts(conjuncts, ((GdlOr)literalToAdd).get(0));
		
		}else{
			
			boolean alreadyPresent = false;
			for(int i=0; i<conjuncts.size(); i++){
				
				GdlLiteral conjunct = conjuncts.get(i);
				
				//check if the added literal is consistent with the conjunct.
				int verification = verifyConjunction(conjunct, literalToAdd);
				
				if(verification == ARE_FALSE){
					conjuncts.clear();
					conjuncts.add(LogicUtils.FALSUM);
					return;
				}else if(verification == ARE_EQUAL){
					alreadyPresent = true;
				}
				
			}
			
			if(!alreadyPresent){
				conjuncts.add(literalToAdd);
			}
		}
		
	}
	
	final static int ARE_FALSE = 1;			// the combination is always false. e.g.  x && !x
	final static int ARE_COMPATIBLE = 2;	// they are compatible. 			e.g.  x && y
	final static int ARE_EQUAL = 3;			// they are equal. 					e.g.  x && x
	final static int ARE_TRUE = 4;			// the combination is always true. 	e.g.  x || !x
	final static int UNCHECKED = 5;
	
	/**
	 * When calling this method, subs should be an empty list.
	 * If a substitution is required then it will be added to that list.
	 * 
	 * @param literal1
	 * @param literal2
	 * @param subs
	 * @return
	 */
	static int verifyConjunction(GdlLiteral literal1, GdlLiteral literal2){
		
		if(literal1.equals(literal2)){
			return ARE_EQUAL;
		}
		
		if(literal1 instanceof GdlSentence && literal2 instanceof GdlNot){
			return verifyCon((GdlSentence)literal1, (GdlNot)literal2);
		}
		if(literal2 instanceof GdlSentence && literal1 instanceof GdlNot){
			return verifyCon((GdlSentence)literal2, (GdlNot)literal1);
		}
		if(literal1 instanceof GdlRelation && literal2 instanceof GdlRelation){
			return verifyCon((GdlRelation)literal1, (GdlRelation)literal2);
		}
		
		return UNCHECKED;
	}
	
	/**
	 * Returns false if the provided arguments are x and ~x respectively 
	 * 
	 * @param sentence
	 * @param not
	 * @return
	 */
	static int verifyCon(GdlSentence sentence, GdlNot not){
		
		GdlLiteral body = not.getBody();
		if(body instanceof GdlSentence){
			if(sentence.equals(body)){
				return ARE_FALSE;
			}
		}
		
		return ARE_COMPATIBLE;
	}
	
	static int verifyCon(GdlRelation relation1, GdlRelation relation2){
		
		//TODO: In fact, we should check the combination of all Should_Do relations in the conjunction.
		//because each pair may be unifiable but the combination of all of them not.
		
		//Note that substituting the relation by a unified relation is not an option,
		// because the variables in the relation may also occur in other relations outside the conjunction.
		
		if(relation1.getName().equals(LogicUtils._DOES) && relation2.getName().equals(GdlPool.DOES) ){
			
			Substitution theta = Unifier.unify(relation1, relation2);
			
			if(theta == null){
				return ARE_FALSE;
			}else{
				return ARE_COMPATIBLE;
			}
			
		}else{
			return UNCHECKED;
		}
	}
	
	@Override
	public String toString(){
		return this.conjuncts.toString();
		
	}
}
