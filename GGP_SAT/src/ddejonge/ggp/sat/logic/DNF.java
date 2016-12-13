package ddejonge.ggp.sat.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DNF extends ArrayList<SimpleConjunction> implements SATFormula{

	//STATIC FIELDS

	//FIELDS
	
	//CONSTRUCTORS
	public DNF(){
		super();
	}

	public DNF(DNF original){
		super(original);
	}
	
	//METHODS

	//GETTERS AND SETTERS
	public void addConjunction(SimpleConjunction conjunction){
		this.add(conjunction);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for (SimpleConjunction simpleConjunction : this) {
			
			if(!first){
				sb.append(" OR ");
			}
			first = false;
			
			sb.append("(");
			sb.append(simpleConjunction);
			sb.append(")");
		}
		
		return sb.toString();
	}
}
