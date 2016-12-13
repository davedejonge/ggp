package ddejonge.ggp.mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.statemachine.MachineState;

import ddejonge.ggp.tools.graph.Vertex;
import ddejonge.ggp.tools.visual.ViewableTreeNode;


public abstract class MCTSNode extends Vertex implements ViewableTreeNode{

	//STATIC FIELDS
	
	//STATIC METHODS
	public static int getNodesGenerated() {
		return numGenerated;
	}
	
	//FIELDS
	private int depth;
	
	private MachineState state;//if this node is an intermediate node then this field is the state of the first StateNode above this node.
	
	
	int raveUpdateCount = 0;
	
	/**Counts how many times the averageGoals array has been updated.
	// When a rollout is performed starting at leaf node n, then this is updated only for the nodes in the selected branch from the root to n.
	// This means that it is not updated for nodes in a different branch from the root to n. */
	private int rolloutCount = 0;  
	
	/**
	 * Is set to true if and only if for each player the upper bound equals the lower bound.<br/>
	 * This is used to determine the best move to make. Normally, the best move is determined by the visit count,
	 * but if one of the children of the root has higher averageGoals, and the value is exact, then it is better to
	 * rely on that.
	 */
	boolean isExact = false; 
	
	//this value is our average goal value. The average is taken over each visit to this node.
	float[] averageGoal;
	float[] raveAverageGoal;
	
	//I'm using bytes instead of ints just to save memory. However, I'm not sure it really does save memory.
	byte[] upperBounds;
	byte[] lowerBounds;
	
	
	//TODO: remove debug:
	public int nextRoleIndex = -1;
	public int  highestLowBound = -1;
	
	//CONSTRUCTORS
	MCTSNode(MachineState state){
		this.id = numGenerated++;
		this.state = state;
	}
	
	/*
	public void addChild(MCTSNode childNode, Move move){
		
		//update the child node.
		childNode.depth = this.depth + 1;
		
		MCTSEdge edge = new MCTSEdge(this, move, childNode);
		if(this.outgoingEdges == null){
			this.outgoingEdges = new ArrayList<MCTSEdge>();
		}
		this.outgoingEdges.add(edge);
		
		if(childNode instanceof StateNode){
			
			if(childNode.incomingEdges == null){
				childNode.incomingEdges = new ArrayList<MCTSEdge>(4); //give it a small capacity, because we usually don't find many equivalent states.
			}
			childNode.incomingEdges.add(edge);
			
			//for StateNodes the state field is set in the constructor.
			
		}else if(childNode instanceof IntermediateNode){
			
			//for IntermediateNodes the state field is copied from its parent.
			childNode.state = this.state;
			
			//((IntermediateNode)childNode).incomingEdge = edge;
			
			childNode.incomingEdges = new ArrayList<MCTSEdge>(1); //for IntermediateNodes this list can only have size 1.
			childNode.incomingEdges.add(edge);
			
		
		}else{
			throw new RuntimeException("IntermediateNode.addChild() Error! unknown class: " + childNode.getClass().getName());
		}
		
		
		
	}*/
	
	
	/**
	 * Sets all pointers to its incoming and outgoing edges to null.
	 * Sets all pointers in these edges that point to this node to null.
	 */
	/*
	public void disconnect(){
		
		if(this.incomingEdges != null){
		
			for(int i=0; i<this.incomingEdges.size(); i++){
				MCTSEdge incomingEdge = incomingEdges.get(i);
				incomingEdge.to = null;
			}
			this.incomingEdges = null;
		}
		
		if(outgoingEdges != null){
			for(int i=0; i<this.outgoingEdges.size(); i++){
				MCTSEdge outgoingEdge = outgoingEdges.get(i);
				outgoingEdge.from = null;
			}
			this.outgoingEdges = null;
		}
		
		this.averageGoal = null;
		this.raveAverageGoal = null;
		
		this.upperBounds = null;
		this.lowerBounds = null;
		
		this.state = null;
		
	}
	*/
	
	
	
	//GETTERS AND SETTERS
	
	public boolean hasChildren(){
		return this.hasOutgoingEdges();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MCTSEdge> getOutgoingEdges() {
		return (List<MCTSEdge>)super.getOutgoingEdges();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MCTSEdge> getIncomingEdges() {
		return (List<MCTSEdge>)super.getIncomingEdges();
	}
	
	public int getRolloutCount(){
		return this.rolloutCount;
	}
	
	/**
	 * Is called after a rollout, on every node in the selected branch.
	 */
	void increaseRolloutCount(){
		this.rolloutCount++;
	}
	
	
	public MachineState getState(){
		return state;
	}
	
	public abstract boolean isTerminal();
	
	/**
	 * Returns true if for each player the upper bound is equal to the lower bound. This means that the utility values of this
	 * node for each player have been determined exactly, and no further exploration of this node is necessary.
	 * @return
	 */
	public boolean isExact() {
		return isExact;
	}

	public int getDepth() {
		return depth;
	}

	void setDepth(int depth) {
		this.depth = depth;
	}

	void deleteState(){
		this.state = null;
	}
	
	@Override
	//public List<? extends ViewableTreeNode> getChildren() {
	public List<MCTSNode> getChildren() {	
	
		ArrayList<MCTSNode> children = new ArrayList<MCTSNode>();
		
		if(this.getOutgoingEdges() != null){
			for(MCTSEdge edge: this.getOutgoingEdges()){
				children.add(edge.getTo());
			}
		}

		return children;
	}
	
	/**
	 * Returns a copy of the average rollout scores. <br/>
	 * Returns null if no rollout has yet been performed from this node.
	 * @return
	 */
	public float[] getAverageGoals(){
		
		if(averageGoal == null){
			return null;
		}
		
		return Arrays.copyOf(averageGoal, 2);
	}

	/**
	 * Returns the average rollout score of the player with the given roleindex. <br/>
	 * Returns null if no rollout has yet been performed from this node.
	 * @return
	 */
	public float getAverageGoal(int roleIndex){
		return this.averageGoal[roleIndex];
	}
	
	void setAverageGoals(float[] averageGoals){
		for(int i=0; i<averageGoals.length; i++){
			this.setAverageGoal(i, averageGoals[i]);
		}
	}
	
	
	void setAverageGoal(int roleIndex, float value){
		if(this.averageGoal == null){
			this.averageGoal = new float[2];
		}
		this.averageGoal[roleIndex] = value;
	}
	
	public String getBoundsString(){
		return "upperBounds: " + Arrays.toString(this.upperBounds) + " lowerBounds: " + Arrays.toString(this.lowerBounds);
	}
	
	
	

}
