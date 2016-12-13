package ddejonge.ggp.prover.mutex;

public class SubsumptionEdge{
	
	SubsumptionVertex from;
	SubsumptionVertex to;

	//Constructor is not public, because it should only be called from Graph.
	SubsumptionEdge(SubsumptionVertex from, SubsumptionVertex to){
		this.from = from;
		this.to = to;
	}
	
	public SubsumptionVertex getFrom(){
		return from;
	}
	
	public SubsumptionVertex getTo(){
		return to;
	}
	
}
