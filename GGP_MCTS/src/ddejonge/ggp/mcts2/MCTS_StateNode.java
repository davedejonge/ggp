package ddejonge.ggp.mcts2;

import java.util.Arrays;

import org.ggp.base.util.statemachine.MachineState;



class MCTS_StateNode extends MCTS_Node{

	//STATIC FIELDS

	//FIELDS
	final boolean isTerminal;
	
	
	//CONSTRUCTORS
	public MCTS_StateNode(MachineState state, boolean isTerminal){
		super(state);
		this.isTerminal = isTerminal;
		if(isTerminal) {
			this.setFullyExplored(true);
		}
		
	}
	
	//METHODS
	
	
	//GETTERS AND SETTERS

	
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append(" av. goals: " + Arrays.toString(getAverageGoals()));
		sb.append(" lb: " + Arrays.toString(lowerBounds));
		sb.append(" ub: " + Arrays.toString(upperBounds));
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
	
	
	@Override
	protected void cleanUp(){
		super.cleanUp();
	}


	//STATIC METHODS
}
