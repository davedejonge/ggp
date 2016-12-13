package ddejonge.ggp.mcts;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;


public class StateNode extends MCTSNode{

	private boolean isTerminal;
	
	
	public StateNode(MachineState state, boolean isTerminal){
		super(state);
		this.isTerminal = isTerminal;
	}
	
	public boolean isTerminal(){
		return this.isTerminal;
	}
	
	public String toString(){
		
		
		
		String s= "";
		s += "ID: " + this.id;
		if(this.isExact){ 
			s+="*";
		}
		if(this.getState() != null){
			s += " state: " + this.getState().toString();
		}
		s += " depth: " + this.getDepth() + System.lineSeparator();
		s += " rolloutCount: " + this.getRolloutCount() + System.lineSeparator();
		s += " averageGoals: " + Arrays.toString(averageGoal) + System.lineSeparator();
		s += " upperBounds: " + Arrays.toString(this.upperBounds) + System.lineSeparator();
		s += " lowerBounds: " + Arrays.toString(this.lowerBounds) + System.lineSeparator();
		s += " nextRoleIndex: " + this.nextRoleIndex + System.lineSeparator();
		s += " highestLowBound: " + this.highestLowBound + System.lineSeparator();
		
		return s;
	}

}
