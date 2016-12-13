package ddejonge.ggp.tools.logic;

import org.ggp.base.util.prover.aima.substitution.Substitution;

public class SubstitutionContainer {

	Substitution substitution;
	
	public void add(Substitution sub){
		
		if(sub == null){
			return;
		}
		
		if(substitution == null){
			substitution = sub;
		}else{
			substitution = substitution.compose(sub);
		}
		
	}
	
	void clear(){
		substitution = null;
	}
	
	public Substitution get(){
		return substitution;
	}
	
	public String toString(){
		
		if(substitution == null){
			return null;
		}
		
		return substitution.toString();
	}
}
