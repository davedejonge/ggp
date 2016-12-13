package ddejonge.ggp.prover.mutex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlVariable;

import ddejonge.ggp.tools.dataStructures.ArraySet;
import ddejonge.ggp.tools.logic.LogicUtils;

/**
 * Given some candidate true(cell(?x0 ?x1, ?y)

   A mutex candidate can be in one of 6 states, determined by the answers to the following two questions:
	Q1 is it a mutex? 			yes, no, dk
	Q2 is it a strong mutex? 	yes, no, dk
	
	The following answers are possible:
	(Q1, Q2) \in { (yes, yes) (yes, no)  (yes, dk)  (no, no)  (dk, no) (dk, dk) }
	
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class MutexCandidate {

	//STATIC FIELDS

	//FIELDS
	private GdlSentence representant;
	public Boolean isMutex; //the value null represents "don't know"
	public Boolean isStrongMutex; //the value null represents "don't know"
	
	private List<GdlVariable> inputVars;
	private List<GdlVariable> outputVars;
	
	private ArraySet<GdlSentence> values;
	
	//CONSTRUCTORS
	/**
	 * Call this constructor if the representant does not have input vars.
	 * @param representant
	 */
	public MutexCandidate(GdlSentence representant, Collection<GdlSentence> allGroundedBasePropositions){
		this.representant = representant;
		this.values = new ArraySet<>();
		this.values.addAll(LogicUtils.getInstantiations(representant, allGroundedBasePropositions));
		
		
		this.inputVars = new ArrayList<GdlVariable>();
		this.outputVars = new ArrayList<GdlVariable>();
		MutexDetector.fillVariableLists(representant, inputVars, outputVars);
		
		
		this.isMutex = null;
		this.isStrongMutex = null;
	}
	
	/**
	 * Call this constructor if the representant has input vars.
	 * @param representant
	 */
	MutexCandidate(GdlSentence representant){
		this.representant = representant;
		this.values = null;
		
		this.inputVars = new ArrayList<GdlVariable>();
		this.outputVars = new ArrayList<GdlVariable>();
		MutexDetector.fillVariableLists(representant, inputVars, outputVars);
		
		this.isMutex = null;
		this.isStrongMutex = null;
	}

	//METHODS

	public GdlSentence getRepresentant() {
		return representant;
	}

	public List<GdlVariable> getInputVars() {
		return Collections.unmodifiableList(inputVars);
	}

	public List<GdlVariable> getOutputVars() {
		return Collections.unmodifiableList(outputVars);
	}

	public Set<GdlSentence> getValues() {
		return Collections.unmodifiableSet(values);
	}



	//GETTERS AND SETTERS
	@Override
	public boolean equals(Object other){
		if(other instanceof MutexCandidate){
			return this.representant.equals(((MutexCandidate)other).representant);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return this.representant.hashCode();
	}
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append(this.representant);
		sb.append(" mutex: " + isMutex);
		sb.append(" strong mutex: " + isStrongMutex);
		return sb.toString();
	}
}
