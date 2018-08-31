package ddejonge.ggp.propnet.heuristics;

import java.util.Set;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.tools.dataStructures.JointMove;

public interface MoveCollector {

	public void add(JointMove jointMove);
	
	public void add(int roleIndex, Move move);
	
	public Set<Move> getMoves(int roleIndex);
	
	public void clear();
	
	public Rollout getRollOut();
	
	public int size();
	
}
