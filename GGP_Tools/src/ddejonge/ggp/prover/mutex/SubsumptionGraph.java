package ddejonge.ggp.prover.mutex;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlSentence;

/**
 * A graph in which each vertex is labeled with a (non-ground) GdlSentence.<br/>
 * There is an edge from vertex A to a vertex B if B is labeled with a GdlSentence that is more general then the label of A. <br/>
 * e.g. if A is labeled with true(cell(1,?Y, b))  and B is labeled with true(cell(?X0, ?y, b))<br/>
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class SubsumptionGraph{

	List<SubsumptionVertex> vertices;
	List<SubsumptionEdge> edges;
	
	//CONSTRUCTOR
	public SubsumptionGraph(){
		this.vertices = new ArrayList<SubsumptionVertex>();
		this.edges = new ArrayList<SubsumptionEdge>();
	}
	
	
	public void add(MutexCandidate sentence){
		
		if(this.getVertex(sentence) == null){
			this.vertices.add(new SubsumptionVertex(sentence));
		}
		
	}
	
	public void remove(MutexCandidate sentence){
		
		SubsumptionVertex vertexToRemove = this.getVertex(sentence);
		if(vertexToRemove == null){
			return;
		}
		
		for(SubsumptionEdge edge : vertexToRemove.getOutgoingEdges()){
			edge.to.getIncomingEdges().remove(edge);
		}
		vertexToRemove.getOutgoingEdges().clear();
		
		for(SubsumptionEdge edge : vertexToRemove.getIncomingEdges()){
			edge.from.getOutgoingEdges().remove(edge);
		}
		vertexToRemove.getIncomingEdges().clear();
		
		this.vertices.remove(vertexToRemove);
		
	}
	
	public void setSubsumes(MutexCandidate from, MutexCandidate to){

		this.add(from);
		this.add(to);
		
		SubsumptionVertex fromVertex = getVertex(from);
		SubsumptionVertex toVertex = getVertex(to);
		
		this.setEdge(fromVertex, toVertex);
		
	}
	
	void addVertex(SubsumptionVertex vertex){
		this.vertices.add(vertex);
	}
	
	
	void setEdge(SubsumptionVertex from, SubsumptionVertex to){
		
		for(SubsumptionEdge outGoingEdge : from.getOutgoingEdges()){
			if(outGoingEdge.to.getLabel().equals(to.getLabel())){
				return;
			}
		}
		
		SubsumptionEdge edge = new SubsumptionEdge(from, to);
		
		edges.add(edge);
		from.addOutgoingEdge(edge);
		to.addIncomingEdge(edge);
	}
	
	public SubsumptionVertex getVertex(MutexCandidate candidate){
		
		for (SubsumptionVertex vertex : vertices) {
			if(vertex.getLabel().equals(candidate)){
				return vertex;
			}
		}
		
		return null;
	}
	
	public List<SubsumptionVertex> getVertices(){
		return this.vertices;
	}
}
