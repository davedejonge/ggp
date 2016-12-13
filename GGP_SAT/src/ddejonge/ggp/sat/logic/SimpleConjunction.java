package ddejonge.ggp.sat.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a conjunction of literals, e.g.   p ^ q ^ ~s. <br/>
 * Note that it cannot contain any disjunctions or nested conjunctions.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class SimpleConjunction implements SATFormula{

	Set<Proposition> positiveAtoms;
	Set<Proposition> negativeAtoms;
	
	//CONSTRUCTORS
	public SimpleConjunction(){
		this.positiveAtoms = new HashSet<>();
		this.negativeAtoms = new HashSet<>();
	}
	//METHODS

	//GETTERS AND SETTERS
	public Set<Proposition> getPositiveAtoms(){
		return Collections.unmodifiableSet(positiveAtoms);
	}
	
	public Set<Proposition> getNegativeAtoms(){
		return Collections.unmodifiableSet(negativeAtoms);
	}

	
	public void addLiteral(Proposition atom, boolean positive){
		
		if(positive){
			positiveAtoms.add(atom);
		}else{
			negativeAtoms.add(atom);
		}
		
	}
	
	public boolean isEmpty(){
		return positiveAtoms.isEmpty() && negativeAtoms.isEmpty();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(Proposition positiveAtom : positiveAtoms){
			
			if(!first){
				sb.append(" AND ");
			}
			first = false;
			
			sb.append(positiveAtom);
			
		}
		for(Proposition negativeAtom : negativeAtoms){
			if(!first){
				sb.append(" AND ");
			}
			first = false;
			
			sb.append("~" + negativeAtom);
		}
		
		return sb.toString();
	}
}
