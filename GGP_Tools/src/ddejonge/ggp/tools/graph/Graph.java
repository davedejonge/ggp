package ddejonge.ggp.tools.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Graph<V extends Vertex, E extends Edge>{

	/*List<V> vertices;
	List<E> edges;*/
	
	//CONSTRUCTOR
	public Graph(){
		/*this.vertices = new ArrayList<V>();
		this.edges = new ArrayList<E>();*/
	}
	
	/*
	public void addVertex(V vertex){
		if(!vertices.contains(vertex)){
			this.vertices.add(vertex);
		}
	}
	*/
	
	/**
	 * Adds the edge to the graph, and makes sure that the 'from' and 'to' vertices have this edge in their
	 * respective lists of incoming/outgoing edges.
	 * @param edge
	 */
	public void setEdge(E edge){
		
		/*
		if(!edges.contains(edge)){
			edges.add(edge);
			edge.getFrom().addOutgoingEdge(edge);
			edge.getTo().addIncomingEdge(edge);
		}*/
		
		edge.getFrom().addOutgoingEdge(edge);
		edge.getTo().addIncomingEdge(edge);
	}
	
	/*
	public List<V> getVertices(){
		return Collections.unmodifiableList(this.vertices);
	}
	
	public List<E> getEdges(){
		return Collections.unmodifiableList(this.edges);
	}
	*/
	
	
	public void removeVertex(V vertex){
		
		/*vertices.remove(vertex);
		edges.removeAll(vertex.getIncomingEdges());
		edges.removeAll(vertex.getOutgoingEdges());*/
		
		if(vertex.getIncomingEdges() != null){
			for(int i=0; i<vertex.getIncomingEdges().size(); i++){
				
				Edge linkToParent = vertex.getIncomingEdges().get(i);
				Vertex parent = linkToParent.getFrom();
				parent.getOutgoingEdges().remove(linkToParent);
				
				linkToParent.cleanUp_private();
			}
		}
		
		if(vertex.getOutgoingEdges() != null){
			for(int i=0; i<vertex.getOutgoingEdges().size(); i++){
				
				Edge linkToChild = vertex.getOutgoingEdges().get(i);
				Vertex child = linkToChild.getTo();
				/*child.getIncomingEdges().remove(linkToChild);*/
				child.removeIncomingEdge(linkToChild);
				
				linkToChild.cleanUp_private();
			}
		}
		
		vertex.cleanUp_private();
	}
	
	public void removeEdge(E edge){
		
		//remove the edge from its source's list of outgoing edges.
		/*edge.getFrom().getOutgoingEdges().remove(edge);*/
		edge.getFrom().removeOutgoingEdge(edge);
		
		//remove the edge from its target's list of incoming edges.
		/*edge.getTo().getIncomingEdges().remove(edge);*/
		edge.getTo().removeIncomingEdge(edge);
	}
	
	/*
	public void cleanUp(){
		
		
		if(vertices == null){
			return;
		}
		
		
		//we need to make a copy of the list of vertices, so that we can loop over the copy, without getting into trouble
		// because removeVertex() tries to modify the list of  vertices.
		List<V> copyOfVertices = new ArrayList<V>(vertices);
		
		//we can directly clear these lists so that removeVertex() has less work to do.
		vertices.clear();
		if(edges != null){
			edges.clear();
		}
		
		for(V vertex : copyOfVertices){
			removeVertex(vertex); //Note that removing all vertices automatically also removes all edges.
		}
		copyOfVertices.clear();
		
	}*/

}
