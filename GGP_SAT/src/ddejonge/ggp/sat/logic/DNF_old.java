package ddejonge.ggp.sat.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DNF_old implements SATFormula{

	//STATIC FIELDS

	//FIELDS
	List<SimpleConjunction> conjunctions;
	
	//CONSTRUCTORS
	public DNF_old(){
		this.conjunctions = new ArrayList<SimpleConjunction>();
	}

	public DNF_old(DNF_old original){
		this.conjunctions = new ArrayList<SimpleConjunction>();
		this.conjunctions.addAll(original.conjunctions);
	}
	
	//METHODS

	//GETTERS AND SETTERS
	public void addConjunction(SimpleConjunction conjunction){
		this.conjunctions.add(conjunction);
	}
	
	public List<SimpleConjunction> getConjunctions(){
		return Collections.unmodifiableList(this.conjunctions);
	}

	public int size(){
		return conjunctions.size();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for (SimpleConjunction simpleConjunction : conjunctions) {
			
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
