package ddejonge.ggp.propnet.heuristics;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.MachineState;


public abstract class Heuristics {

	Random random = new Random();
	
	/**Is called after every rollout. Can be used to update the internal state of this object. */
	public Object updateAfterRollOut(MoveCollector selectedMoves, MachineState terminalState, int[] goals){
		return null;
	}
	
	
	public int getRandomMoveIndexInRollOut(int roleIndex, List<? extends Object> legalMoves){
		return random.nextInt(legalMoves.size());
	}
	
	public boolean appliesEarlyCutoff = false;
	
	/**
	 * Returns goals if the given state satisfies some criterion for early cutoff.
	 * otherwise returns null
	 * @param state
	 * @return
	 */
	public int[] testEarlyCutOff(MachineState state, MoveCollector moveSelection){
		return null;
	}
	
	
}
