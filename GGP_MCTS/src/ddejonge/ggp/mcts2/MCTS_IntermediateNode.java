package ddejonge.ggp.mcts2;

import java.util.Arrays;

import org.ggp.base.util.statemachine.MachineState;


class MCTS_IntermediateNode extends MCTS_Node{

	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS
	public MCTS_IntermediateNode(MachineState state) {
		super(state);
	}

	//METHODS

	//GETTERS AND SETTERS
	MCTS_Edge getIncomingEdge(){
		
		if(this.getIncomingEdges() == null){
			return null;
		}
		
		return (MCTS_Edge)this.getIncomingEdges().get(0);
	}
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append("Intermediate Node");
		sb.append(" av. goals: " + Arrays.toString(getAverageGoals()));
		sb.append(" rollouts: " + getRolloutCount());
		
		int numChildren = 0;
		if(this.hasOutgoingEdges()) {
			numChildren = this.getOutgoingEdges().size();
		}
		sb.append(" numChildren: " + numChildren);
		sb.append(" depth: " + getDepth());
		if(this.isTerminal()) {
			sb.append("--t");
		}
		if(this.isExact()){
			sb.append("*");
		}
		return sb.toString();
	}
	
	//STATIC METHODS
}
