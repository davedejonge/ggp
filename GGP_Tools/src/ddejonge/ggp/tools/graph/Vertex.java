package ddejonge.ggp.tools.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Vertex{
	
	private List<Edge> incomingEdges;
	private List<Edge> outgoingEdges;
	
	public static int numGenerated;
	protected int id;
	
	//can be used for various purposes, e.g. to set flags for certain algorithms.
	private int mark = 0;
	
	public void addOutgoingEdge(Edge edge){
		if(this.outgoingEdges == null){
			this.outgoingEdges = new ArrayList<Edge>();
		}
		
		this.outgoingEdges.add(edge);
	}
	
	public void addIncomingEdge(Edge edge){
		if(this.incomingEdges == null){
			this.incomingEdges = new ArrayList<Edge>();
		}
		
		this.incomingEdges.add(edge);
	}
	
	
	
	//GETTERS AND SETTERS
	public void setMark(int mark){
		this.mark = mark;
	}
	
	public int getMark(){
		return mark;
	}
	
	public int getId(){
		return this.id;
	}
	
	public List<? extends Edge> getIncomingEdges(){
		
		if(incomingEdges == null){
			return null;
		}
		return Collections.unmodifiableList(incomingEdges);
	}
	
	
	public boolean hasOutgoingEdges(){
		return this.outgoingEdges != null && !this.outgoingEdges.isEmpty();
	}

	/**
	 * May return null if there are no outgoing edges.
	 * @return
	 */
	public List<? extends Edge> getOutgoingEdges(){
		if(outgoingEdges == null){
			return null;
		}
		return Collections.unmodifiableList(outgoingEdges);
	}
	
	
	void removeIncomingEdge(Edge incomingEdge){
		this.incomingEdges.remove(incomingEdge);
	}
	
	void removeOutgoingEdge(Edge outgoingEdge){
		this.outgoingEdges.remove(outgoingEdge);
	}
	
	void cleanUp_private(){
		//should only be called from graph.
		
		if(incomingEdges != null){
			this.incomingEdges.clear();
		}
		if(outgoingEdges != null){
			this.outgoingEdges.clear();
		}
		
		cleanUp();
	}
	
	protected void cleanUp(){
		//can be overridden
	}
	
}
