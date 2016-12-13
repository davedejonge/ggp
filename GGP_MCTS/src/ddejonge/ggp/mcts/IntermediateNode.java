package ddejonge.ggp.mcts;

import java.util.Arrays;

import org.ggp.base.util.statemachine.MachineState;




public class IntermediateNode extends MCTSNode{
	
	IntermediateNode(MachineState state) {
		super(state);
	}
	
	public boolean isTerminal(){
		return false;
	}
	
	public String toString(){
		
		String s= "";
		s += "ID: " + this.id;
		if(this.isExact){ 
			s+="*";
		}
		s += " depth: " + this.getDepth() + System.lineSeparator();
		//s += "move: " + move + System.lineSeparator();	
		s += " rolloutCount: " + getRolloutCount() + System.lineSeparator();
		s += " averageGoals: " + Arrays.toString(averageGoal) + System.lineSeparator();
		s += " upperBounds: " + Arrays.toString(this.upperBounds) + System.lineSeparator();
		s += " lowerBounds: " + Arrays.toString(this.lowerBounds) + System.lineSeparator();
		s += " nextRoleIndex: " + this.nextRoleIndex + System.lineSeparator();
		s += " highestLowBound: " + this.highestLowBound + System.lineSeparator();
		//s += "uctValue: " + uctValue + System.lineSeparator();;
		
		return s;
	}

	/*
	@Override
	void disconnect(){
		super.disconnect();
		
		this.incomingEdge = null;
	}*/
	
	MCTSEdge getIncomingEdge(){
		if(this.getIncomingEdges() == null){
			return null;
		}
		
		return this.getIncomingEdges().get(0);
	}
	
}
