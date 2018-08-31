package ddejonge.ggp.tools.dataStructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ggp.base.util.statemachine.Move;


public class Rollout {

	List<JointMove> jointMoveSequence = new ArrayList<>();
	JointMove incompleteJointMove = null;
	
	int[] goals;
	int numPlayers;
	
	public Rollout(int numPlayers){
		this.numPlayers = numPlayers;
	}
	
	/**Is called during rollout, every time a new joint move is chosen.*/
	public void add(JointMove jointMove){
		jointMoveSequence.add(jointMove);
	}
	
	
	
	/**Is called when descending the tree to find best leaf.*/
	public void add(int roleIndex, Move move){

		if(incompleteJointMove == null){
			incompleteJointMove = new JointMove(numPlayers);
		}
		
		//First add the move to the incomplete joint move.
		incompleteJointMove.set(roleIndex, move);
		
		//If the incompleteJointMove becomes complete, add it to the joint move sequence, and reset the pointer to null.
		if(incompleteJointMove.size() == numPlayers){
			
			jointMoveSequence.add(incompleteJointMove);
			incompleteJointMove = null;
		}
		
	}
	
	

	
	public List<JointMove> getJointMoveSequence(){
		return Collections.unmodifiableList(this.jointMoveSequence);
		//return new ArrayList<JointMove>(this.jointMoveSequence);
	}
	
	public void clear(){
		this.jointMoveSequence.clear();
		this.incompleteJointMove = null;
		this.goals = null;
	}
	
	/**
	 * Returns the number of single player moves in this collection (not the number of joint moves!)
	 * @return
	 */
	public int size(){
		
		int numMoves = 0;
		if(incompleteJointMove != null) {
			numMoves += this.incompleteJointMove.size();
		}
		
		numMoves += numPlayers*this.jointMoveSequence.size();
		
		return numMoves;
	}
	
	
	
	public void setGoals(int[] utilities){
		this.goals = Arrays.copyOf(utilities, numPlayers);
	}
	
	public int[] getGoals(){
		return Arrays.copyOf(goals, numPlayers);
	}
}
