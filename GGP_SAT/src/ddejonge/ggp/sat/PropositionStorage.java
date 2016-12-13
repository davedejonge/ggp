package ddejonge.ggp.sat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlSentence;

import ddejonge.ggp.sat.logic.Proposition;

/**
 * Data structure to store Proposition objects.<br/>
 * Makes sure that you don't create two or more objects representing the same GdlSentence.<br/>
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class PropositionStorage {

	//STATIC FIELDS
	
	
	//FIELDS
	/**Stores all Proposition objects, indexed by their IDs.*/
	private ArrayList<Proposition> propositions = new ArrayList<Proposition>();
	

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
	Proposition add(GdlSentence atom){
		
		Proposition correspondingProp = getProposition(atom);
		
		if(correspondingProp == null){
			Proposition newProp = new Proposition(atom, propositions.size()+1);
			propositions.add(newProp);
			return newProp;
		}else{
			return correspondingProp;
		}
		
		
		
	}
	
	public Proposition getProposition(int id){
		return propositions.get(id-1);
	}
	
	public Proposition getProposition(GdlSentence sentence){
		for(Proposition prop : propositions) {
			if(prop != null && prop.equalsSentence(sentence)){
				return prop;
			}
		}
		
		return null;
	}
	
	public List<Proposition> toList(){
		return Collections.unmodifiableList(propositions);
	}
	
	public int size(){
		return propositions.size();
	}
}
