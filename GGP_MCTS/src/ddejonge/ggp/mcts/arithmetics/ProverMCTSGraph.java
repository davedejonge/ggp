package ddejonge.ggp.mcts.arithmetics;

import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.mcts.IntermediateNode;
import ddejonge.ggp.mcts.MCTSGraph;
import ddejonge.ggp.mcts.MCTSNode;
import ddejonge.ggp.mcts.MCTSParams;
import ddejonge.ggp.mcts.StateNode;
import ddejonge.ggp.mcts.heuristics.MoveCollectorImpl;
import ddejonge.ggp.tools.dataStructures.JointMove;
import ddejonge.ggp.tools.visual.Monitor;

public class ProverMCTSGraph extends MCTSGraph{

	public ProverMCTSGraph(MCTSParams params, StateMachine stateMachine, Role myRole, Monitor monitor) {
		super(params, stateMachine, myRole, monitor);
	}

	@Override
	public int[] rollout(MCTSNode leaf, MoveCollectorImpl moveCollector) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
	
		MachineState leafState = leaf.getState();
		if(leafState == null){
			throw new RuntimeException("MCTSGraph.rollout() Error! leaf.state == null" );
		}
		
		//The state from which we should start the rollout.
		MachineState startState;
		
		if(leaf instanceof StateNode){
		
			startState = leafState;
		
		}else{
		
			IntermediateNode intermediateNode = (IntermediateNode)leaf;
			
			//determine the moves that lead from the last state node to this leaf node.
			JointMove jointMove = getPartialJointMove(intermediateNode);
			
			//Fill the partial joint move with random moves until it is full.
			fillPartialJointMove(jointMove, moveCollector);
			
			//Create the next state.
			startState = stateMachine.getNextState(leafState, jointMove);
			
		}
		
		return performRolloutFromState(startState, moveCollector);
	}
	
	
	int[] performRolloutFromState(MachineState state, MoveCollectorImpl moveCollector) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		//Perform rollout.
    	while(!stateMachine.isTerminal(state)) {
    		
    		if(heuristicsObject != null && heuristicsObject.appliesEarlyCutoff){
    			int[] goals = heuristicsObject.testEarlyCutOff(state, moveCollector);
    			if(goals != null){
    				return goals;
    			}
    		}
    		
    		List<Move> jointMove = stateMachine.getRandomJointMove(state);
    		
            state = stateMachine.getNextStateDestructively(state, jointMove);
            
        	if(moveCollector != null){
        		moveCollector.add(new JointMove(jointMove));
        	}
            
        }
    	
    	//Get goals
		List<Integer> goalList = stateMachine.getGoals(state);
		int goals[] = new int[goalList.size()];
		for (int i = 0; i < goalList.size(); i++) {
			goals[i] = goalList.get(i);
		}
		
		if(moveCollector != null){
			moveCollector.getRollOut().setGoals(goals);
		}
		
		
		this.numRollOutsPerformed++;
		
		return goals;
	}
	
}
