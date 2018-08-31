package ddejonge.ggp.propnet.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.tools.dataStructures.JointMove;


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
	
	
	public void setGoals(int[] utilities){
		this.goals = Arrays.copyOf(utilities, numPlayers);
	}
	
	public int[] getGoals(){
		return Arrays.copyOf(goals, numPlayers);
	}
	
	public List<JointMove> getJointMoveSequence(){
		return new ArrayList<JointMove>(this.jointMoveSequence);
	}
	
	public void clear(){
		this.jointMoveSequence.clear();
		this.incompleteJointMove = null;
		this.goals = null;
	}
	
	
}
