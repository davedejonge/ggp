package ddejonge.ggp.asp.dependencyGraph;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.prover.aima.substituter.Substituter;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;

import ddejonge.ggp.tools.graph.Edge;
import ddejonge.ggp.tools.graph.Graph;
import ddejonge.ggp.tools.graph.Vertex;



/**
 * Represents a graph in which each node corresponds to a (non-ground) atom of the game description.<br/>
 * There is an edge from Node A to Node B if one of these two cases holds:<br/>
 * 	- There is a rule r such that sentence A is the head of r and one of the subgoals of r is sentence B. 
 *  - Sentence A appears in the body of some rule r1, and sentence B appears in the head of some rule r2, and there is a substitution that unifies these two sentences.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class DependencyGraph extends Graph<Node, DepEdge>{

	
	int numNodes = 0;
	
	List<Node> nodes = new ArrayList<Node>();
	List<DepEdge> edges = new ArrayList<DepEdge>();
	
	Node findNode(GdlSentence sentence){
		
		for (Node node : this.nodes) {
			if(node.sentence.equals(sentence)){
				return node;
			}
		}
		
		return null;
	}

	DepEdge findEdge(Node headNode, Node bodyNode){
		
		for (DepEdge edge : this.edges) {
			if(edge.getFrom().equals(headNode) && edge.getTo().equals(bodyNode)){
				return edge;
			}
		}
		
		return null;
	}
	
	
	@Override
	public void setEdge(DepEdge edge){
		this.edges.add(edge);
		
		if(!this.nodes.contains(edge.getFrom())){
			this.nodes.add((Node)edge.getFrom());
		}
		
		if(!this.nodes.contains(edge.getTo())){
			this.nodes.add((Node)edge.getTo());
		}
		
		super.setEdge(edge);
	}
	
	@Override
	public void removeEdge(DepEdge edge){
		this.edges.remove(edge);
		super.removeEdge(edge);
	}
	
	@Override
	public void removeVertex(Node vertex){
		this.nodes.remove(vertex);
		super.removeVertex(vertex);
	}
	
	
	public boolean dependsOn(GdlLiteral formula, GdlConstant relationName){
		List<GdlConstant> relationNames = new ArrayList<GdlConstant>(1);
		relationNames.add(relationName);
		return dependsOn(formula, relationNames);
	}
	
	public boolean dependsOn(GdlLiteral formula, List<GdlConstant> relationNames){
		
		if(formula instanceof GdlOr){
			GdlOr or = (GdlOr) formula;
			
			for (int i = 0; i < or.arity(); i++) {
				if(dependsOn(or.get(i), relationNames)){
					return true;
				}
			}
			
			return false;
			
		}else if(formula instanceof GdlNot){
			GdlNot not = (GdlNot) formula;
			
			return dependsOn(not.getBody(), relationNames);
			
			
		}else if(formula instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) formula;
			
			return sentenceDependsOn(sentence, relationNames);
			
		}else if(formula instanceof GdlDistinct){
			return false;
			
		}else{
			throw new RuntimeException(this.getClass().getName() + ".simplify() Error! " + formula.getClass().getName());
		}
		
	}
	
	//NOT USED
	/*
	private boolean sentenceDependsOn(GdlSentence sentence, GdlConstant relationName){
		List<GdlConstant> relationNames = new ArrayList<GdlConstant>(1);
		relationNames.add(relationName);
		return dependsOn(sentence, relationNames);
	}*/
	
	private boolean sentenceDependsOn(GdlSentence sentence, List<GdlConstant> relationNames){
		
		Node node = findNode(sentence);
		
		NodeSet visitedNodes = new NodeSet(this.numNodes);
		Substitution sub = new Substitution();
		
		return dependsOn(node, relationNames, visitedNodes, sub);
		
	}
	
	
	
	
	private boolean dependsOn(Node node, List<GdlConstant> relationNames, NodeSet visitedNodes, Substitution sub){
		
		//prevent cycles.
		if(visitedNodes.contains(node)){
			return false;
		}
		visitedNodes.add(node);
		
		if(relationNames.contains(node.sentence.getName())){
			return true;
		}
		
		if(node.getOutgoingEdges() != null){
		
			GdlSentence substitutedSentence = null;
			
			for(Edge outEdge : node.getOutgoingEdges()){
				DepEdge outgoingEdge = (DepEdge)outEdge;
				Substitution newSub = sub;
				
				//if the outgoing edge has a substitution, then we need to check whether this substitution is 
				// compatible with the substitution that was passed as a parameter.
				if(outgoingEdge.sub != null){
					
					if(substitutedSentence == null){
						//apply the current substitution to the current node.
						substitutedSentence = Substituter.substitute(node.sentence, sub);
					}
					
					newSub = Unifier.unify(substitutedSentence, ((Node)outgoingEdge.getTo()).sentence);
					
					if(newSub == null){
						continue;
					}
				}
				
				if(dependsOn((Node)outgoingEdge.getTo(), relationNames, visitedNodes, newSub)){
					return true;
				}
			}
		}
		
		return false;
	}

	public List<DepEdge> getEdges() {
		return this.edges;
	}

	public List<Node> getVertices() {
		return this.nodes;
	}
	
	public void cleanUp(){
		
		
		if(nodes == null){
			return;
		}
		
		
		//we need to make a copy of the list of vertices, so that we can loop over the copy, without getting into trouble
		// because removeVertex() tries to modify the list of  vertices.
		List<Node> copyOfVertices = new ArrayList<Node>(nodes);
		
		//we can directly clear these lists so that removeVertex() has less work to do.
		nodes.clear();
		if(edges != null){
			edges.clear();
		}
		
		for(Node vertex : copyOfVertices){
			removeVertex(vertex); //Note that removing all vertices automatically also removes all edges.
		}
		copyOfVertices.clear();
		
	}
	
	

}
