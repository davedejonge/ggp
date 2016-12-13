package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;


/**
 * Represents a set of base propositions, such that always exactly one of them must be true in any state.
 * @author Dave de Jonge
 *
 */
public class Mutex {
	
	//STATIC FIELDS
	public final static String inputVarName = "?X";
	public final static String outputVarName = "?Y";
	
	/**
	 * ummy value to represent that none of the values is true.
	 */
	public final static GdlSentence NONE = GdlPool.getProposition(GdlPool.getConstant("NONE"));

	
	//FIELDS
	public final boolean exactlyOneMustBeTrue; //if this is false it is possible that none of the propositions are true.
	
	/**
	 * The non-ground atom that represents this mutex. It should not contain any input vars and at least one output var. e.g. true(cell(1,1,?y))
	 */
	final GdlSentence nonGroundRepresentation;
	
	/**
	 * Can be non-ground, but the variables must all be input vars.
	 */
	List<GdlSentence> values; 
	
	
	//CONSTRUCTOR
	/**
	 * 
	 * @param nonGroundRepresentation The non-ground atom that represents this mutex. It should not contain any input vars and at least one output var. e.g. true(cell(1,1,?y))
	 * @param values The ground propositions that are the possible values of this mutex.
	 * @param exactlyOneMustBeTrue
	 */
	public Mutex(GdlSentence nonGroundRepresentation, Set<GdlSentence> values, boolean exactlyOneMustBeTrue){
		this.values = new ArrayList<GdlSentence>(values);
		this.exactlyOneMustBeTrue = exactlyOneMustBeTrue;
		
		this.nonGroundRepresentation = nonGroundRepresentation;
		
		if(!exactlyOneMustBeTrue && !values.contains(NONE)){
			this.values.add(NONE);
		}
	}
	
	
	//GETTERS AND SETTERS
	public List<GdlSentence> getValues(){
		return Collections.unmodifiableList(values);
	}
	
	public String toString(){
		return nonGroundRepresentation.toString();
	}
}
