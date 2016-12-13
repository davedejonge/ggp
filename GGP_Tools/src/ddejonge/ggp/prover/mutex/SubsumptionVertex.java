package ddejonge.ggp.prover.mutex;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlSentence;

public class SubsumptionVertex {
	
	private List<SubsumptionEdge> incomingEdges = new ArrayList<SubsumptionEdge>();
	private List<SubsumptionEdge> outgoingEdges = new ArrayList<SubsumptionEdge>();
	
	private MutexCandidate label;
	
	public SubsumptionVertex(MutexCandidate label){
		this.label = label;
	}
	
	void addOutgoingEdge(SubsumptionEdge edge){
		if(this.outgoingEdges == null){
			this.outgoingEdges = new ArrayList<SubsumptionEdge>();
		}
		
		this.outgoingEdges.add(edge);
	}
	
	void addIncomingEdge(SubsumptionEdge edge){
		if(this.incomingEdges == null){
			this.incomingEdges = new ArrayList<SubsumptionEdge>();
		}
		
		this.incomingEdges.add(edge);
	}
	
	
	
	//GETTERS AND SETTERS
	public MutexCandidate getLabel(){
		return this.label;
	}
	
	public List<SubsumptionEdge> getIncomingEdges(){
		return new ArrayList<SubsumptionEdge>(incomingEdges);
	}

	public List<SubsumptionEdge> getOutgoingEdges(){
		return new ArrayList<SubsumptionEdge>(outgoingEdges);
	}
	
	public String toString(){
		return this.label.toString();
	}
}
