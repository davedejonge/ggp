package ddejonge.ggp.sat.logic;

import java.util.List;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class CNF_old implements SATFormula{

	//STATIC FIELDS

	//FIELDS
	List<Clause> clauses;
	
	//CONSTRUCTORS
	public CNF_old(){
		this.clauses = new ArrayList<Clause>();
	}
	
	public CNF_old(List<Clause> clauses){
		this.clauses = new ArrayList<Clause>(clauses);
	}
	

	//METHODS

	//GETTERS AND SETTERS
	public List<Clause> getClauses(){
		return Collections.unmodifiableList(clauses);
	}

	public void add(Clause clause) {
		this.clauses.add(clause);
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for (Clause clause : clauses) {
			
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

	//STATIC METHODS
}
