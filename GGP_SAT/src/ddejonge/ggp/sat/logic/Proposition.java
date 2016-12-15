package ddejonge.ggp.sat.logic;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.sat4j.core.VecInt;

public class Proposition implements SATFormula, Comparable<Proposition>{

	//STATIC FIELDS

	//FIELDS
	private GdlSentence gdlProp;
	int id;
	
	private VecInt vecInt;
	
	//CONSTRUCTORS
	public Proposition(GdlSentence gdlProp, int id) {
		this.gdlProp = gdlProp;
		this.id = id;
		
		vecInt = new VecInt(new int[]{id});
	}

	//METHODS

	//GETTERS AND SETTERS
	
	/**
	 * Returns the SAT4J representation of this proposition as a unit clause.
	 * @return
	 */
	public VecInt getVecInt(){
		return this.vecInt;
	}
	
		
	public String toString(){
		return getGdlSentence().toString();
	}
	
	
	public boolean equalsSentence(GdlSentence sentence){
		return this.getGdlSentence().equals(sentence);
	}
	
	public boolean equals(Object object){
		if(object instanceof Proposition){
			return this.id == ((Proposition)object).id;
		}
		return false;
	}
	
	
	public int hashcode(){
		return this.id;
	}

	@Override
	public int compareTo(Proposition o) {
		return this.id - o.id;
	}

	public GdlSentence getGdlSentence() {
		return gdlProp;
	}

}
