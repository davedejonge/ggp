package ddejonge.ggp.mcts;

import java.util.Arrays;

public class UCT {
	
	// For each node we store a visit count N, a rolloutCount, and an array of average goal values.
	// 
	// - The visit count counts how often the node has been selected (so it is updated before the rollout).
	// - The rollout count counts how often a rollout has been made from a leaf underneath this node.
	//   (it is updated after the rollout)
	// - The average goal array stores for each role the average of the goal it received over all rollouts,
	//    that started at a leaf node underneath this node.	(it is updated after the rollout)
	//
	//	Each edge stores a visit count and a uctValue.
	// -The visit count counts how often the edge has been selected (so it is updated before the rollout).
	// - The uctValue depends on its visit count and on the visit count of its from-node.
	//		(it is updated before the rollout)
	//  
	// When selecting the next leaf node we base this on the heuristic of the edges.
	//  the heuristic of an edge is the sum of the uctValue of the edge and the average goal value of its to-node,
	//  for the player who makes the move of that edge.
	// 
	// When selecting the best move to make, we simply use the visit count of the
	// outgoing edges of the root node.
	//
	// Updating after a rollout:
	//  - increase the rolloutCounts of all nodes on any path to the leaf node.
	//  - update the average goal values of the leaf node straightforwardly.
	//  - for its parents: recalculate the average goals by taking the weighted average of
	//    all children of that parent, weighted by the visit counts of the outgoing edges of 
	//    that parent.

	/**
	 * Holds the possible values of the numerator of the UCT formula.
	 */
	private static float[] table1 = new float[10*1000];
	
	/**
	 * Holds the possible values of the denominator of the UCT formula.
	 */
	private static float[] table2 = new float[1000];
	
	final static float PARENT_MIN = calculateNumerator(2);
	final static float CHILD_MIN = 0.5f; 
    
	public static float calculate(int parentCounter, int childCounter){
		
		if(childCounter < 1){
			throw new RuntimeException("UCT.calculate() Error! childCounter must be greater than 0. parentCounter == " + parentCounter + " childCounter == " + childCounter);
		}
		if(parentCounter < 1){
			throw new RuntimeException("UCT.calculate() Error! parentCounter must be greater than 0. parentCounter == " + parentCounter + " childCounter == " + childCounter);
		}
		if(parentCounter < childCounter){
			throw new RuntimeException("UCT.calculate() Error! parentCounter must be strictly greater than childCounter. parentCounter == " + parentCounter + " childCounter == " + childCounter);
		}
		
		if(parentCounter == 1){ //in this case both the parentCounter and the childCounter are both exactly 1. Indeed the value should be 0, because it means none of the siblings of this child have been visited, so they have preference over this child.
			return 0.0f;
		}
		
		if(parentCounter >= table1.length){
			resize1();
		}
		
		if(childCounter >= table2.length){
			resize2();
		}
		
		 //if the table holds a value that is lower than the lowest possible value it means
		// that this table entry hasn't been calculated yet.
		if(table1[parentCounter] < PARENT_MIN){
			table1[parentCounter] = calculateNumerator(parentCounter);
		}	
		
		 //if the table holds a value that is lower than the lowest possible value it means
		// that this table entry hasn't been calculated yet.
		if(table2[childCounter] < CHILD_MIN){ 
			
			double cc = (double)childCounter;
			table2[childCounter] = (float)Math.sqrt(cc);

		}
		
		return table1[parentCounter] / table2[childCounter];
		
	}
	
	private static float calculateNumerator(int parentCounter){
		double pc = (double)parentCounter;
		return (float)Math.sqrt(Math.log(pc));
	}
	
	private static void resize1(){
		table1 = Arrays.copyOf(table1, 2*table1.length);
	}
	
	private static void resize2(){
		table2 = Arrays.copyOf(table2, 2*table2.length);
	}
}
