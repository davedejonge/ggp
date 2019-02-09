package ddejonge.ggp.mcts2;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.tools.graph.Edge;


class MCTS_Edge extends Edge {
	
	//STATIC FIELDS

	//FIELDS
	private Move move;
	
	/**Is set to true when its to-Node has a higher bound than the lower bound of any of its siblings.*/
	boolean pruned = false; 
	
	//the number of times this node has been selected when descending the tree, looking for the best leaf. 
	private int selectionCount = 0;
	
	private int rolloutCount = 0;
	
	/**UCT value of this edge, including normalization constant.*/
	private float uctValue = 1000f; 
	
	//CONSTRUCTORS
	protected MCTS_Edge(MCTS_Node from, Move move, MCTS_Node to) {
		super(from, to);
		this.move = move;
	}

	//METHODS
	/**
	 * Returns the sum of the UCT value of this edge with the average goal of the child node for the given role.
	 * 
	 * @param roleIndex The index of the player that is making the move
	 * @return
	 */
	float getHeuristic(int roleIndex){
		
		float[] avGoals = this.getTo().getAverageGoals();
		
		float returnVal = uctValue; 
		if(avGoals != null) {
			returnVal += avGoals[roleIndex];
		}
		
		return returnVal;
		
		

		
	}
	
	public float getUctValue(){
		return uctValue;
	}
	
	
	//GETTERS AND SETTERS
	public Move getMove() {
		return move;
	}
	
	public int getRolloutCount(){
		return this.rolloutCount;
	}
	
	public void increaseRolloutCount(){
		this.rolloutCount++;
	}
	
	public int getSelectionCount() {
		return selectionCount;
	}

	public void increaseSelectionCount() {
		this.selectionCount++;
	}

	
	void updateUCTValue(float uctConstant){
		
		int parentNodeRolloutCount = this.getFrom().getRolloutCount();
		
		if(parentNodeRolloutCount == 0 || this.getRolloutCount() == 0){ //children that haven't been visited yet have maximum uct value.
			this.uctValue = 1000f;
		}else{
			this.uctValue = uctConstant * UCT.calculate(parentNodeRolloutCount, this.getRolloutCount());
			//Note that for the second argument we need the rolloutCount of the Edge rather than of the Node.
			// This is because the child node may have been visited via one of its other parents.
			// IDEA: it would be better if the first argument were in fact the sum of the rolloutCounts of its children.
			//  in theory this should already be the case, but in practice it isn't because an edge may be added later, so the new parent
			//  wasn't updated in earlier rollouts that went via the child.
			
			/*if(this.uctValue == 0.0){
				System.out.println("uctConstant " + uctConstant);
				System.out.println("parentNodeRolloutCount " + parentNodeRolloutCount);
				System.out.println("this.getRolloutCount() " + this.getRolloutCount());
				System.out.println("calculation: " + UCT.calculate(parentNodeRolloutCount, this.getRolloutCount()));
				throw new RuntimeException("NegoMCTS_Edge.updateUCTValue() Error!");
			}*/
			
		}
	}
	
	
	@Override
	public MCTS_Node getTo(){
		return (MCTS_Node)super.getTo();
	}
	
	@Override
	public MCTS_Node getFrom(){
		return (MCTS_Node)super.getFrom();
	}
	
	
	public String toString(){
		return super.getFrom().toString() + " " + this.move + " " + super.getTo().toString();
	}

	//STATIC METHODS
}
