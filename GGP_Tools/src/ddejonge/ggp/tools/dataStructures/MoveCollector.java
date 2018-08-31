package ddejonge.ggp.tools.dataStructures;

import java.util.List;
import java.util.Set;

import org.ggp.base.util.statemachine.Move;

public abstract class MoveCollector {

	//This class wraps around a Rollout object. The idea is that the implementation of this abstract class may provide
	// a method to obtain all moves made by one specific player.
	
	Rollout rollout;
	
	public MoveCollector(int numPlayers){
		this.rollout = new Rollout(numPlayers);
	}
	
	/**Is called during rollout, every time a new joint move is chosen.*/
	public void add(JointMove jointMove) {
		this.rollout.add(jointMove);
	}
	
	/**Is called when descending the tree to find best leaf.*/
	public void add(int roleIndex, Move move) {
		this.rollout.add(roleIndex, move);
	}
	
	public List<JointMove> getJointMoveSequence(){
		return this.rollout.getJointMoveSequence();
	}
	
	/**
	 * Must return the set of moves made by one agent.<br/>
	 * This is required for the implementation of RAVE and MAST.
	 * 
	 * @param roleIndex
	 * @return
	 */
	public abstract Set<Move> getMoves(int roleIndex);
	
	public void clear(){
		this.rollout.clear();
	}
	
	public Rollout getRollOut(){
		return this.rollout;
	}
	
	/**
	 * Returns the number of single player moves in this collection (not the number of joint moves!)
	 * @return
	 */
	public int size(){
		return this.rollout.size();
	}
	
	
	public String toString() {
		return this.rollout.getJointMoveSequence().toString();
	}
}
