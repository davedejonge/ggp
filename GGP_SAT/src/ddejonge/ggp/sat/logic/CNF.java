package ddejonge.ggp.sat.logic;

import java.util.List;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class CNF extends ArrayList<Clause> implements SATFormula{

	//STATIC FIELDS
	
	//STATIC METHODS

	//FIELDS
	
	//CONSTRUCTORS
	public CNF(){
		super();
	}
	
	public CNF(List<Clause> clauses){
		super(clauses);
	}
	

	//METHODS

	//GETTERS AND SETTERS
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for (Clause clause : this) {
			
			if(!first){
				sb.append(" AND ");
			}
			first = false;
			
			sb.append("(");
			sb.append(clause);
			sb.append(")");
		}
		
		return sb.toString();
	}

	
}
