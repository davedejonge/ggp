package ddejonge.ggp.mcts2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.tools.dataStructures.JointMove;
import ddejonge.ggp.tools.dataStructures.MoveCollector;

class MoveCollectorImpl extends MoveCollector{
	
	
	private ArrayList<Set<Move>> roleIndex2selectedMoves;
	
	
	public MoveCollectorImpl(int numPlayers){
		super(numPlayers);
		
		//Create a set of moves for each player.
		roleIndex2selectedMoves = new ArrayList<Set<Move>>(numPlayers);
		for (int i = 0; i < numPlayers; i++) {
			roleIndex2selectedMoves.add(new HashSet<Move>());
		}
		
	}
	
	/**Is called during rollout, every time a new joint move is chosen.*/
	@Override
	public void add(JointMove jointMove){
		for (int i = 0; i < roleIndex2selectedMoves.size(); i++) {
			
			Set<Move> moveSet = roleIndex2selectedMoves.get(i);
			moveSet.add(jointMove.get(i));
		}
		
		super.add(jointMove);
	}
	
	
	
	
	/**Is called when descending the tree to find best leaf.*/
	@Override
	public void add(int roleIndex, Move move){
		roleIndex2selectedMoves.get(roleIndex).add(move);
		
		super.add(roleIndex, move);
	}
	
	@Override
	public Set<Move> getMoves(int roleIndex){
		return this.roleIndex2selectedMoves.get(roleIndex);
	}
	
	@Override
	public void clear(){
		for (int i = 0; i < roleIndex2selectedMoves.size(); i++) {
			roleIndex2selectedMoves.get(i).clear();
		}
		
		super.clear();
	}

	
	

}
