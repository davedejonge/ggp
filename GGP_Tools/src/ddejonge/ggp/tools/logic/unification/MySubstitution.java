package ddejonge.ggp.tools.logic.unification;

import java.util.ArrayList;

import org.ggp.base.util.gdl.grammar.*;

public class MySubstitution {
	
	//FIELDS
	ArrayList<GdlVariable> variables;
	ArrayList<GdlTerm> terms;
	
	
	
	
	//CONSTRUCTORS
	public MySubstitution(){
		this.variables = new ArrayList<GdlVariable>();
		this.terms = new ArrayList<GdlTerm>();
	}
	
	MySubstitution(int capacity){
		this.variables = new ArrayList<GdlVariable>(capacity);
		this.terms = new ArrayList<GdlTerm>(capacity);
	} 
	
	
	
	
	
	
	//GETTERS AND SETTERS
	boolean contains(GdlVariable var){
		return variables.contains(var);
	}

	GdlTerm get(GdlVariable var){
		
		for (int i = 0; i < variables.size(); i++) {
			if(variables.get(i).equals(var)){
				return terms.get(i);
			}
			
		}
		
		return null;
	}
	
	void put(GdlVariable var, GdlTerm term){
		
		for (int i = 0; i < variables.size(); i++) {
			if(variables.get(i).equals(var)){
				terms.set(i, term);
				return;
			}
			
		}
		
		variables.add(var);
		terms.add(term);
		
	}
	
	public MySubstitution compose(MySubstitution thetaPrime){
		
		MySubstitution result = new MySubstitution(this.variables.size() + thetaPrime.variables.size());
		
		result.variables.addAll(this.variables);
		result.terms.addAll(this.terms);
		
		for (int i = 0; i < thetaPrime.variables.size(); i++) {
			put(thetaPrime.variables.get(i), thetaPrime.terms.get(i));
		}
		
		//Note: the following code doesn't work correctly, because it may cause the
		// the same variable to appear twice:
		// result.variables.addAll(thetaPrime.variables);
		// result.terms.addAll(thetaPrime.terms);
		
		
		return result;
	}
}
