package ddejonge.ggp.sat.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;

public class Clause implements SATFormula{

	//STATIC FIELDS

	//FIELDS
	Set<Proposition> positiveAtoms;
	Set<Proposition> negativeAtoms;
	
	private VecInt vecInt = null;
	
	//CONSTRUCTORS
	public Clause(){
		this.positiveAtoms = new HashSet<>();
		this.negativeAtoms = new HashSet<>();
	}
	
	/**
	 * Constructor to create a singleton clause
	 * @param atom
	 * @param positive
	 */
	public Clause(Proposition atom, boolean positive){
		this.positiveAtoms = new HashSet<>();
		this.negativeAtoms = new HashSet<>();
		
		if(positive){
			positiveAtoms.add(atom);
		}else{
			negativeAtoms.add(atom);
		}
	}
	
	public Clause(Set<Proposition> positiveAtoms, Set<Proposition> negativeAtoms){
		
		this.positiveAtoms = new HashSet<>(positiveAtoms);
		this.negativeAtoms = new HashSet<>(negativeAtoms);
	}
	
	private void initArray(){
		
		int[] array = new int[positiveAtoms.size() + negativeAtoms.size()];
		int i=0;
		for (Proposition positiveAtom : positiveAtoms) {
			array[i] = positiveAtom.id;
			i++;
		}
		for (Proposition negativeAtom : negativeAtoms) {
			array[i] = -negativeAtom.id;
			i++;
		}
		
		vecInt = new VecInt(array);
	}
	
	//METHODS

	//GETTERS AND SETTERS
	public Set<Proposition> getPositiveAtoms(){
		return Collections.unmodifiableSet(positiveAtoms);
	}
	
	public Set<Proposition> getNegativeAtoms(){
		return Collections.unmodifiableSet(negativeAtoms);
	}
	
	
	public void addPositiveLiteral(Proposition atom){
		addLiteral(atom, true);
	}
	
	public void addNegativeLiteral(Proposition atom){
		addLiteral(atom, false);
	}
	
	public void addLiteral(Proposition atom, boolean positive){
		if(positive){
			this.positiveAtoms.add(atom);
		}else{
			this.negativeAtoms.add(atom);
		}
		
		//Instead of updating the Sat4j representation of this clause we simply set it to null.
		//This will make sure that it is recalculated as soon as we need it.
		vecInt = null;
	}

	public boolean isEmpty(){
		return positiveAtoms.isEmpty() && negativeAtoms.isEmpty();
	}
	
	/**
	 * Returns the SAT4J representation of this clause.
	 * @return
	 */
	public VecInt getVecInt(){
		
		if(vecInt == null){
			initArray();
		}
		
		return this.vecInt;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(Proposition positiveAtom : positiveAtoms){
			
			if(!first){
				sb.append(" OR ");
			}
			first = false;
			
			sb.append(positiveAtom);
			
		}
		for(Proposition negativeAtom : negativeAtoms){
			if(!first){
				sb.append(" OR ");
			}
			first = false;
			
			sb.append("~" + negativeAtom);
		}
		
		return sb.toString();
	}
	/*
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for(Proposition prop : positiveAtoms){
			if(!first){
				sb.append(",");
			}
			first = false;
			sb.append(prop);
		}
		for(Proposition prop : positiveAtoms){
			if(!first){
				sb.append(",");
			}
			first = false;
			sb.append("~" + prop);
		}
		sb.append("}");
		return sb.toString();
	}*/
}
