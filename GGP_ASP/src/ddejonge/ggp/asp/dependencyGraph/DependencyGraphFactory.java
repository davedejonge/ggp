package ddejonge.ggp.asp.dependencyGraph;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;


public class DependencyGraphFactory {
	
	
	//store all rules for which we have already generated the relevant nodes and edges,
	// in order to prevent infinite recursion.
	static ArrayList<GdlRule> handledRules = new ArrayList<GdlRule>();
	
	public static DependencyGraph constructGraph(List<Gdl> gameDescription){
		
		DependencyGraph graph = new DependencyGraph();
		
		for (Gdl gdl : gameDescription) {
			
			if(gdl instanceof GdlRule){
				handleRule(graph, gameDescription, (GdlRule)gdl);
			}else if(gdl instanceof GdlSentence){
				findOrCreateNode(graph, (GdlSentence)gdl);
			}
			
		}
		
		return graph;
	}

	
	
	static void handleRule(DependencyGraph graph, List<Gdl> gameDescription, GdlRule rule){
		
		if(handledRules.contains(rule)){
			return;
		}
		handledRules.add(rule);
		
		//get the node corresponding to the head.
		GdlSentence head = rule.getHead();
		
		Node headNode = findOrCreateNode(graph, head);
		
		//get all nodes corresponding to the body sentences.
		List<GdlSentence> bodySentences = getBodySentences(rule);
		List<Node> bodyNodes = findOrCreateNodes(graph, bodySentences);
		
		
		for (Node bodyNode : bodyNodes) {
			setEdge(graph, headNode, rule, bodyNode);
		}
		
		//Now, for each body sentence, try to unify it with the head of any other rule.
		for(GdlSentence bodySentence : bodySentences){
			handleBodySentence(graph, gameDescription, bodySentence);
		}
	}
	
	static void handleBodySentence(DependencyGraph graph, List<Gdl> gameDescription, GdlSentence bodySentence){
		
		for(Gdl gdl : gameDescription){
			
			GdlSentence head = getHead(gdl);
			
			if(head.equals(bodySentence)){
				continue;
			}
			
			Substitution sub = Unifier.unify(head, bodySentence);
			if(sub != null){
				
				Node bodyNode = findOrCreateNode(graph, bodySentence);
				Node headNode = findOrCreateNode(graph, head);
				
				setEdge(graph, bodyNode, sub, headNode);
				
				if(gdl instanceof GdlRule){
					handleRule(graph, gameDescription, (GdlRule)gdl);
				}
				
				//if gdl is a fact then the recursion stops.
			}
			
		}
	}
	
	
	static List<GdlSentence> getBodySentences(GdlRule rule){
		
		List<GdlSentence> sentences = new ArrayList<GdlSentence>();
		
		for(GdlLiteral literal : rule.getBody()){
			sentences.addAll(extractSentences(literal));
		}
		
		return sentences;
	}
	
	static List<GdlSentence> extractSentences(GdlLiteral formula){
		
		List<GdlSentence> sentences = new ArrayList<GdlSentence>();
		
		/*if(formula instanceof GdlAnd){
			throw new NotImplementedException();
			
		}else*/ if(formula instanceof GdlOr){
			GdlOr or = (GdlOr) formula;
			
			for (int i = 0; i < or.arity(); i++) {
				sentences.addAll(extractSentences(or.get(i)));
			}
			
			
		}else if(formula instanceof GdlNot){
			GdlNot not = (GdlNot) formula;
			
			sentences.addAll(extractSentences(not.getBody()));
			
		}else if(formula instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) formula;
			
			sentences.add(sentence);
			
		}else if(formula instanceof GdlDistinct){
			
			//nothing needs to happen here.
			
		}else{
			throw new RuntimeException("DependencyGraphFactory.extractSentences() Error! " + formula.getClass().getName());
		}
		
		return sentences;
	}
	
	
	static GdlSentence getHead(Gdl gdl){
		
		if(gdl instanceof GdlRule){
			return ((GdlRule) gdl).getHead();
		}else if(gdl instanceof GdlSentence){
			return (GdlSentence)gdl;
		}else{
			throw new RuntimeException("DependencyGraphFactory.getHead() Error! unhandled class " + gdl.getClass().getName());
		}
		
	}
	
	
	
	//CREATE AND RETRIEVE NODES
	
	static Node findOrCreateNode(DependencyGraph graph, GdlSentence sentence){
		
		Node node = graph.findNode(sentence);
		
		if(node == null){
			node = new Node(sentence, graph.numNodes++);
			graph.getVertices().add(node);
		}
		
		return node;
	}
	
	static List<Node> findOrCreateNodes(DependencyGraph graph, List<GdlSentence> sentences){
		
		List<Node> nodesToReturn = new ArrayList<Node>();
		
		for (GdlSentence sentence : sentences) {
			Node node = findOrCreateNode(graph, sentence);
			nodesToReturn.add(node);
		}
		
		return nodesToReturn;
	}
	
	
	//CREATE AND RETRIEVE EGES
	
	static void setEdge(DependencyGraph graph, Node bodyNode, Substitution sub, Node headNode){
		
		DepEdge edge = findOrCreateEdge(graph, bodyNode, headNode);
		edge.setSubstitution(sub);
	}
	
	static void setEdge(DependencyGraph graph, Node headNode, GdlRule rule, Node bodyNode){
		
		DepEdge edge = findOrCreateEdge(graph, headNode, bodyNode);
		edge.setRule(rule);

	}

	
	static DepEdge findOrCreateEdge(DependencyGraph graph, Node fromNode, Node toNode){
		
		//check if there already is an edge:
		DepEdge edge = graph.findEdge(fromNode, toNode);
		
		if(edge == null){
			
			edge = new DepEdge(fromNode, toNode);
			
			fromNode.addOutgoingEdge(edge);

			toNode.addIncomingEdge(edge);
			
			graph.getEdges().add(edge);
		}
		
		return edge;
	}
	

	
}
