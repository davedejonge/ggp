package ddejonge.ggp.mcts;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.mcts.MCTSGraph;
import ddejonge.ggp.mcts.MCTSNode;
import ddejonge.ggp.tools.dataStructures.JointMove;
import ddejonge.ggp.tools.dataStructures.Pair;

/**
 * This class collects a number of methods to explore the tree, which are not used in a normal MCTS algorithm.
 * They are mainly used for negotiations.
 * 
 * @author Dave de Jonge
 *
 */
public class TreeExplorer {

	//STATIC FIELDS

	//FIELDS
	ArrayList<List<MCTSEdge>> openList;
	MCTSGraph graph;
	int numPlayers;
	
	/**
	 * The algorithms in this class will ignore any node that has been explored less times than this threshold.
	 */
	int minNumRollouts;
	
	
	
	//CONSTRUCTORS
	public TreeExplorer(MCTSGraph graph, int numPlayers, int minNumRollouts) {
		this.graph = graph;
		this.openList = new ArrayList<List<MCTSEdge>>();
		
		this.numPlayers = numPlayers;
		this.minNumRollouts = minNumRollouts;
		
		List<MCTSEdge> emptyList = new ArrayList<MCTSEdge>(0);
		
		openList.add(emptyList);
		
	}
	
	//METHODS
	public boolean hasNext(){
		return ! this.openList.isEmpty();
	}
	
	public Pair<List<JointMove>, float[]> getNext(){
		
		if(openList.isEmpty()){
			return null;
		}
		
		//Get the last branch on the openList.
		List<MCTSEdge> nextBranch = openList.remove(openList.size()-1);
		
		MCTSNode leafNode;
		if(nextBranch.size() == 0){ //this happens only if nextBranch is the very first element put on the list.
			leafNode = graph.getRoot(); 
			
		}else{		
			//Get the leaf node of that branch.
			leafNode = nextBranch.get(nextBranch.size() - 1).getTo();
		}
		
		//For each child of that leaf node, put its branch on the open list.
		addChildEdges(nextBranch, leafNode);
		
		
		if(leafNode instanceof IntermediateNode){
			return getNext();
		}
		
		List<JointMove> jointMoves = edges2jointMoves(nextBranch);
		
		//Return the branch.
		return new Pair<List<JointMove>, float[]>(jointMoves, leafNode.averageGoal);
	}
	
	private void addChildEdges(List<MCTSEdge> nextBranch, MCTSNode leafNode){
		
		if(leafNode.getOutgoingEdges() == null){
			return;
		}
		
		//For each child of that leaf node, put its branch on the open list.
		for(MCTSEdge childEdge : leafNode.getOutgoingEdges()){
			
			//if the child node has not been evaluated yet, then we can skip it.
			if(childEdge.getTo().averageGoal == null){
				continue;
			}
			
			//skip this node if it hasn't been visited enough times.
			if(childEdge.getTo().getRolloutCount() < minNumRollouts){
				continue;
			}
			
			List<MCTSEdge> newBranch = new ArrayList<MCTSEdge>(nextBranch.size() + 1);
			newBranch.addAll(nextBranch);
			newBranch.add(childEdge);
			
			openList.add(newBranch);
		}
	}
	
	private List<JointMove> edges2jointMoves(List<MCTSEdge> edges){
		
		List<JointMove> jointMoves = new ArrayList<JointMove>();
		
		JointMove jointMove = null;
		
		for(int i=0; i<edges.size(); i++){
			
			if(i % numPlayers == 0){
				
				if(jointMove != null && (jointMove.get(0) == null || jointMove.get(1) == null)){
					throw new RuntimeException("TreeExplorer.edges2jointMoves() Error! incomplete joint move!");
				}
				
				jointMove = new JointMove(numPlayers);
				jointMoves.add(jointMove);
			}
			
			MCTSEdge edge = edges.get(i);
			
			int roleIndex = graph.playerIndex2roleIndex(graph.getNextPlayerIndex(edge.getFrom()));
			
			jointMove.set(roleIndex, edge.move);
		}
		
		
		return jointMoves;
	}
	
	
	
	/**
	 * Returns an approximation of the values of the Nash equilibrium of the subgame following the given moves.
	 * 
	 * 
	 * @return
	 */
	public float[] getCurrentEquilibriumValues(List<JointMove> jointMoves){
		
		MCTSNode node = getApproximateNode(jointMoves).getLeft();
		
		return getCurrentEquilibriumValues(node);
	}
	
	/**
	 * Returns an approximation of the values of the Nash equilibrium.
	 * 
	 * @return
	 */
	public float[] getCurrentEquilibriumValues(MCTSNode subtreeRoot){
		
		MCTSNode loopNode = subtreeRoot;
		
		while(loopNode.getOutgoingEdges() != null){
			
			MCTSNode child = getChildWithHighestGoal(loopNode);
			
			if(child == null || child.getRolloutCount() < minNumRollouts){
				return loopNode.averageGoal;
			}
			
			loopNode = child;
		}
		
		return loopNode.averageGoal;
		
		//The average value of the first best child is not very accurate because it takes a long time 
		// to converge to the actual equilibrium value.
		// A better estimate is to take the best leaf node and use its values.
		
	}
	
	/**
	 * Returns the node corresponding to the given JointMoves.<br/>
	 * Returns null if there is no such node.
	 * @param jointMoves
	 * @return
	 */
	public MCTSNode getNode(List<JointMove> jointMoves){
		
		Pair<MCTSNode, Boolean> pair = getApproximateNode(jointMoves);
		
		if(pair.getRight()){
			return pair.getLeft();
		}else{
			return null;
		}
	}
	
	/**
	 * Returns the node corresponding to the given JointMoves.<br/>
	 * If the tree does not contain such node, it returns its closest ancestor.
	 * 
	 * The returned boolean value indicates whether the returned node corresponds exactly to the given joint moves or not.
	 * 
	 * @param graph
	 * @param jointMoves
	 * @return
	 */
	
	public Pair<MCTSNode, Boolean> getApproximateNode(List<JointMove> jointMoves){
		
		MCTSNode node = graph.getRoot();
		for(JointMove jointMove : jointMoves){
			
			int numPlayers = jointMove.getNumPlayers();
			
			for(int playerIndex=0; playerIndex<numPlayers; playerIndex++){
				int roleIndex = graph.playerIndex2roleIndex(playerIndex);
				
				if(node.getOutgoingEdges() == null){
					return new Pair<MCTSNode, Boolean>(node, false);
				}
				
				boolean nodeFound = false;
				for(MCTSEdge edge : node.getOutgoingEdges()){
					if(edge.move.equals(jointMove.get(roleIndex))){
						if( edge.getTo().averageGoal != null &&  edge.getTo().getRolloutCount() >= minNumRollouts){
							node = edge.getTo();
							nodeFound = true;
						}
						break;
					}
				}
				
				if( ! nodeFound){
					return new Pair<MCTSNode, Boolean>(node, false);
				}
			}
		}
		
		return new Pair<MCTSNode, Boolean>(node, true);
	}
	
	/**
	 * Returns the node for which the average goal value for the active player is highest.<br/>
	 * Returns null if none of the node's children has its average goals initialized.
	 * @param node
	 * @return
	 */
	MCTSNode getChildWithHighestGoal(MCTSNode node){
		
		int nextRoleIndex = graph.playerIndex2roleIndex(graph.getNextPlayerIndex(node));
		float highestGoal = -1;
		MCTSNode bestChild = null;
		
		for(MCTSEdge childEdge : node.getOutgoingEdges()){
			MCTSNode childNode = childEdge.getTo();
			
			if(childNode.averageGoal == null){
				continue;
			}
			
			if(childNode.averageGoal[nextRoleIndex] > highestGoal){
				bestChild = childNode;
				highestGoal = childNode.averageGoal[nextRoleIndex];
			}
		}
		
		return bestChild;
	}
	

	//GETTERS AND SETTERS

	//STATIC METHODS
}
