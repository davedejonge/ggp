package ddejonge.ggp.mcts.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import ddejonge.ggp.BasicPlayer;
import ddejonge.ggp.mcts.MCTSGraph;
import ddejonge.ggp.mcts.MCTSNode;
import ddejonge.ggp.mcts.MCTSParams;
import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.prover.mutex.MutexDetector_OLD;
import ddejonge.ggp.tools.Stopwatch;
import ddejonge.ggp.tools.Utils;
import ddejonge.ggp.tools.dataStructures.Pair;
import ddejonge.ggp.tools.logic.Mutex;
import ddejonge.ggp.tools.visual.TreeViewer;

/**
 * Monte Carlo Search over a directed graph.
 * 
 * Note: for now we will only store state nodes in the transposition table.
 * that is: nodes for which the depth is a multiple of n, if there are n players.
 * 
 * @author Dave de Jonge
 *
 */
public class MCTSPlayer extends BasicPlayer {
	
	//How can we make sure that we do not create cycles?
	// 
	// - option 1: only consider two states equivalent if they also appear in the same round of the game 
	//		(i.e. after the same number of moves, i.e. if they have the same depth in the graph)
	//   in many games this restriction will not make much difference, because they have a round-counter 
	//   anyway.
	//
	// We have to adapt the transposition table to only recognize a state not only by the base propositions
	// but also by the depth in the graph.
	
	
	String name;
	MCTSParams params;

	/**
	 * The search tree. (or actually it is a graph rather than a tree)
	 */
	MCTSGraph graph;
	
	Move myLastMove;
	
	
	
	
	//CONSTRUCTOR
	MCTSPlayer(MCTSParams params, String name){
		
		this.monitor.setProperty("StateNodes generated:");
		this.monitor.setProperty("Edges generated:");
		
		this.params = params;
		
		this.name = name;
		
		/*this.USE_PROPNET_FOR_MULTIPLAYER_GAMES = params.USE_PROPNET;
		this.USE_PROPNET_FOR_SINGLEPLAYER_GAMES = params.USE_PROPNET;*/
		
		this.monitor.setTitle(this.getName());
		
	}
	
	

	
	
	
	
	/**
	 * This method is called at the beginning of each new game.
	 * @param timeout
	 */
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		
		try{ 
			
			//Call the super class method to do some basic initialization.
			super.stateMachineMetaGame(timeout);
			
			//Construct the search tree.
			graph = new MCTSGraph(params, stateMachine, myRole, monitor);
			
			//Start expanding the MCTS tree until the meta game phase is over.
			System.out.println();
			this.monitor.println("MCTSPlayer.stateMachineMetaGame() expanding the tree during meta game phase...");
			graph.expand(this.stateMachine.getInitialState(), params.MAX_NODES_IN_TREE, timeout - 500);
			

		}catch(Throwable e){
			e.printStackTrace();
		}

	}
	
	
	@Override
	public Move findBestMove(MachineState currentState, long timeout) throws Exception {
		
		Move chosenMove;
		try{
			
			
			System.out.println();
			System.out.println("numMovesMade: " + numMovesMade);
			
			//Check memory and do something if too much memory is used.
			if(monitor.getUsedMemoryMB() > 1000){
				
				if(getStateMachine() instanceof CachedStateMachine){
					((CachedStateMachine)getStateMachine()).prune();
				}
				System.gc(); //Calling the Garbage collector is bad practice. We should find a better way to deal with memory issues.
				graph.increaseExpansionThreshold(1);
				System.out.println("MCTSPlayer.findBestMove() increasing expand threshold to: " + graph.getExpansionThreshold());
				
			}

		
			//If everything is okay, the current state cannot be terminal. Here we check that this is indeed the case
			if(this.stateMachine.isTerminal(currentState)){
				System.out.println("MCTSPlayer.findBestMove() CURRENT STATE IS TERMINAL! " + currentState);
			}
			
			
			if(numMovesMade == 0){
				
				graph.expand(numMovesMade, params.MAX_NODES_IN_TREE, timeout - 1000);
				
			}else{
				
				List<GdlTerm> lastMoves = this.getMatch().getMostRecentMoves();
				System.out.println("MCTSPlayer.findBestMove() last moves made: " + lastMoves);
				graph.expand(currentState, lastMoves, numMovesMade, params.MAX_NODES_IN_TREE, timeout-1000);
			}
			
			
			//Determine the best move to make.
			chosenMove = graph.findBestMove();
			System.out.println("MCTSPlayer.findBestMove() move chosen: " + chosenMove);
			
			
		}catch(Throwable e){
			
			e.printStackTrace();
			
			//If anything goes wrong, just pick a a random legal move.
			System.out.println("ERROR! MAKING RANDOM MOVE...");
			chosenMove = Utils.getRandomObjectFromList(this.stateMachine.getLegalMoves(currentState, getRole()));
			
			
		}

		//write the log file to disk.
		this.monitor.writeLogFile();
		
		
		//Return our move to make.
		return chosenMove;
				

	}
	
	@Override
	public void stateMachineStop() {
		
		if(graph != null){
			this.graph.cleanUp();
		}
		
		super.stateMachineStop();
	}
	
	@Override
	public String getName() {
		
		if(this.name == null){
			return getClass().getSimpleName();
		}
		
		return name;
	}







	@Override
	public StateMachine getInitialStateMachine() {
		return new PropnetStateMachine();
	}
	
	
}
