package ddejonge.ggp.mcts2;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;

import ddejonge.ggp.tools.graph.Edge;
import ddejonge.ggp.tools.graph.Vertex;
import ddejonge.ggp.tools.visual.ViewableTreeNode;


class MCTS_Node extends Vertex implements ViewableTreeNode{

	//STATIC FIELDS

	//FIELDS
	private int depth; 
	private MachineState state;
	


	/**Counts how many times the averageGoals array has been updated.
	// When a rollout is performed starting at leaf node n, then this is updated only for the nodes in the selected branch from the root to n.
	// This means that it is not updated for nodes in a different branch from the root to n. */
	private int rolloutCount = 0;  
	
	
	//In order to calculate the UCT value of a node, we need to use the rolloutCount.
	// For any non-leaf node its average goal values should be a weighted average over its children,
	// where the weight of each child is determined by the selection count of the Edge leading to that child.
	
	//TODO: make sure that the average goal values are indeed calculated using the selectionCount
	// PROBLEM: how do we take those rollouts into account that were performed when the node didn't have children yet?
	


	
	//this value is our average goal value. The average is taken over each visit to this node.
	private float[] averageGoals;
	

	
	//I'm using bytes instead of ints just to save memory. However, I'm not sure it really does save memory.
	byte[] upperBounds;
	byte[] lowerBounds;
	
	

	/**
	 * Is set to true if and only if for each player the upper bound equals the lower bound. <br/>
	 * This is used to determine the best move to make. Normally, the best move is determined by the visit count,
	 * but if one of the children of the root has higher averageGoals, and the value is exact, then it is better to
	 * rely on that.
	 */
	private boolean exact = false;
	
	/**
	 * Is set to true if all terminal states underneath this node have been explored. <br/>
	 * Note that this is much stronger than isExact, because isExact uses max-min reasoning, i.e. it ignores nodes that will not be chosen by a rational player. 
	 */
	private boolean fullyExplored = false;
	
	//CONSTRUCTORS
	MCTS_Node(MachineState state){
		this.id = numGenerated++;
		this.state = state;
	}

	//METHODS

	//GETTERS AND SETTERS
	
	public MachineState getState(){
		return state;
	}
	
	public int getDepth(){
		return this.depth;
	}
	
	void setDepth(int depth){
		this.depth = depth;
	}
	
	
	public int getRolloutCount(){
		return this.rolloutCount;
	}
	
	/**
	 * Is called after a rollout, on every node in the selected branch.
	 */
	public void increaseRolloutCount(){
		this.rolloutCount++;
	}
	

	

	public float[] getAverageGoals(){
		return this.averageGoals;
	}
	
	public float getAverageGoal(int roleIndex){
		return this.averageGoals[roleIndex];
	}
	void setAverageGoal(int roleIndex, float value){
		if(this.averageGoals == null){
			this.averageGoals = new float[2];
		}
		this.averageGoals[roleIndex] = value;
	}
	
	void setAverageGoals(float[] averageGoals){
		
		if(averageGoals == null) {
			throw new RuntimeException("MCNS_Node.setAverageGoals() Error! ");
		}
		
		
		for(int i=0; i<averageGoals.length; i++){
			this.setAverageGoal(i, averageGoals[i]);
		}
	}
	
	

	
	public boolean isExact() {
		return this.exact;
	}
	

	/**
	 * This field is set to true in the following cases:<br/>
	 * 1. The node is a terminal node (in that case this method is called after selecting the node, and setting its upper and lower bounds. <br/>
	 * 2. For each player the upper- and lower-bounds of are equal. This is checked after calling updateBounds().
	 */
	void setExact(boolean exact) {
		this.exact = exact;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MCTS_Edge> getIncomingEdges() {
		return (List<MCTS_Edge>)super.getIncomingEdges();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MCTS_Edge> getOutgoingEdges() {
		return (List<MCTS_Edge>)super.getOutgoingEdges();
	}
	
	public boolean hasChildren(){
		List<? extends Edge> outgoungEdges = super.getOutgoingEdges();
		return (outgoungEdges != null) && (!outgoungEdges.isEmpty()); 
	}
	
	
	/**
	 * Is set to true if all terminal states underneath this node have been explored. <br/>
	 * Note that this is much stronger than isExact, because isExact uses max-min reasoning, i.e. it ignores nodes that will not be chosen by a rational player. 
	 */
	void setFullyExplored(boolean subtreeFullyExplored) {
		this.fullyExplored = subtreeFullyExplored;
	}
	
	
	/**
	 * Returns true if all terminal states underneath this node have been explored. <br/>
	 * Note that this is much stronger than isExact, because isExact uses max-min reasoning, i.e. it ignores nodes that will not be chosen by a rational player. 
	 */
	public boolean isFullyExplored() {
		return this.fullyExplored;
	}
	
	
	@Override
	protected void cleanUp(){
		super.cleanUp();
		this.state = null;
		this.averageGoals = null;
		this.upperBounds = null;
		this.lowerBounds = null;
		

	}

	@Override
	public List<? extends ViewableTreeNode> getChildren() {
		
		ArrayList<MCTS_Node> children = new ArrayList<MCTS_Node>();
		
		if(this.getOutgoingEdges() != null){
			for(MCTS_Edge edge: this.getOutgoingEdges()){
				children.add(edge.getTo());
			}
		}

		return children;
		
	}
	
	public boolean isTerminal(){
		
		if(this instanceof MCTS_StateNode){
			return ((MCTS_StateNode)this).isTerminal;
		}
		
		return false;
	}
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append(" av. goals: " + Arrays.toString(averageGoals));
		sb.append(" rollouts: " + rolloutCount);
		if(this.isExact()){
			sb.append("*");
		}
		return sb.toString();
	}

	
	//TODO: at work, I think we need to synchronize the GGP_TOOLS project, so that these methods are actually in the Vertex class.
	public void setVisitMarker(int marker) {
		this.setMark(marker);
	}
	
	public int getVisitMarker() {
		return this.getMark();
	}
	
}
