package ddejonge.ggp.mcts;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.mcts.heuristics.rave.RaveCalculator;
import ddejonge.ggp.tools.NotImplementedException;
import ddejonge.ggp.tools.graph.Edge;
import ddejonge.ggp.tools.visual.ViewableTreeNode;

public class MCTSEdge extends Edge implements ViewableTreeNode{
	
	Move move;
	
	/**Is set to true when its to-Node has a higher bound then the lower bound of any of its siblings.*/
	boolean pruned = false; 
	
	//the number of times this node has been selected when descending the tree, looking for the best leaf. 
	private int selectionCount = 0;
	
	/**The number of times a rollout has been performed through this edge. Is used to calculate the uct value.*/
	private int rolloutCount = 0;
	
	float uctValue = 1000f;
	
	public MCTSEdge(MCTSNode from, Move move, MCTSNode to) {
		super(from, to);
		this.move = move;
	}
	
	
	/**
	 * 
	 * @param roleIndex The index of the player that is making the move
	 * @return
	 */
	float getHeuristic(int roleIndex){
		return getHeuristic(roleIndex, null);
	}

	float getHeuristic(int roleIndex, RaveCalculator raveCalculator){
		
		float returnVal = uctValue;
		
		if(this.getTo().averageGoal == null){
			return uctValue;
		}
		
		if(raveCalculator == null || this.getTo().raveAverageGoal == null){

			returnVal += this.getTo().averageGoal[roleIndex];
			
		}else{
			
			double raveParameter = raveCalculator.getRaveParameter(selectionCount);
			
			returnVal += raveParameter * (this.getTo().raveAverageGoal[roleIndex]);	
			returnVal += (1-raveParameter) * (this.getTo().averageGoal[roleIndex] );
			
			//TODO: I am not sure whether this should be selectionCount or rolloutCount
			throw new NotImplementedException();
			
		}
		
		return returnVal;
		
	}
	
	
	public MCTSNode getFrom() {
		return (MCTSNode)super.getFrom();
	}


	public Move getMove() {
		return move;
	}


	public MCTSNode getTo() {
		return (MCTSNode)super.getTo();
	}

	public int getRolloutCount(){
		return this.rolloutCount;
	}
	
	void increaseRolloutCount(){
		this.rolloutCount++;
	}
	
	public int getSelectionCount() {
		return selectionCount;
	}

	void increaseSelectionCount() {
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

	public float getUctValue() {
		return uctValue;
	}


	public String toString(){
		
		return "Move: " + move + " pruned: " + pruned + " uct value: " + this.uctValue;
	}


	@Override
	public List<MCTSNode> getChildren() {
		
		ArrayList<MCTSNode> children = new ArrayList<MCTSNode>(1);
		children.add(getTo());
		return children;
	}
	

}
