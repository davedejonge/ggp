package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;

public class Disjunction{

	private ArrayList<GdlLiteral> disjuncts;
	
	public Disjunction(){
		disjuncts = new ArrayList<GdlLiteral>(16);
	}
	
	public Disjunction(int capacity){
		disjuncts = new ArrayList<GdlLiteral>(capacity);
	}
	
	public void add(GdlLiteral literalToAdd){
		addToDisjuncts(disjuncts, literalToAdd);
	}
	
	public void addAll(ArrayList<GdlLiteral> literalsToAdd){
		for(int i=0;i<literalsToAdd.size(); i++){
			addToDisjuncts(disjuncts, literalsToAdd.get(i));
		}
	}
	
	public GdlOr getOr(){
		return GdlPool.getOr(disjuncts);
	}
	
	public GdlLiteral getLiteral(){
		
		if(disjuncts.size() == 0){
			return LogicUtils.FALSUM;
		}else if(disjuncts.size() == 1){
			return disjuncts.get(0);
		}
		
		return this.getOr();
	}
	
	public int size(){
		return disjuncts.size();
	}
	
	
	public GdlLiteral get(int index){
		return disjuncts.get(index);
	}
	
	public void clear(){
		disjuncts.clear();
	}
	
	
	public static void addToDisjuncts(List<GdlLiteral> disjuncts, GdlLiteral literalToAdd){
		
		//If the list of disjuncts contains the VERUM then it doesn't make sense to add anything to it.
		if(disjuncts.size() > 0 && disjuncts.get(0) instanceof GdlAnd && ((GdlAnd)disjuncts.get(0)).arity() == 0 ){
			return;
		}
		
		if(literalToAdd == null){
			return;
		}
		
		//Adding FALSUM to a disjunction doesn't have any effect.
		// However, we don't have to check this explicitly, because it is already taken care of
		// because it is a special case of  'literalToAdd instanceof GdlOr'
		
		if(literalToAdd instanceof GdlOr){
			GdlOr innerOr = (GdlOr)literalToAdd;
			
			for(int j=0; j<innerOr.arity(); j++){
				addToDisjuncts(disjuncts, innerOr.get(j));
			}
		}else if(literalToAdd instanceof GdlAnd && ((GdlAnd)literalToAdd).arity() == 0){
			
			//adding a VERUM to an OR results in the entire OR to be false.
			//so we return an OR that only contains the VERUM.
			disjuncts.clear();
			disjuncts.add(literalToAdd);
		
		}else if(literalToAdd instanceof GdlAnd && ((GdlAnd)literalToAdd).arity() == 1){
			
			//if there is only one value we may as well add that value itself, rather than the GdlAnd object.
			addToDisjuncts(disjuncts, ((GdlAnd)literalToAdd).get(0));
		
		}else{
			
			//Make sure that the literal to add isn't already in this disjunction.
			for(int i=0; i<disjuncts.size(); i++){
				
				GdlLiteral disjunct = disjuncts.get(i);
				
				if(disjunct.equals(literalToAdd)){
					return;
				}
				
			}
			
			disjuncts.add(literalToAdd);
		}
	}
	
	@Override
	public String toString(){
		return this.disjuncts.toString();
		
	}
}
