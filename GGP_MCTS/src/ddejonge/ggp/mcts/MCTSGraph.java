package ddejonge.ggp.mcts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.DefaultEditorKit.CutAction;

import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.mcts.heuristics.MoveCollectorImpl;
import ddejonge.ggp.mcts.heuristics.mast.MastTable;
import ddejonge.ggp.mcts.heuristics.rave.RaveCalculator;
import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.propnet.heuristics.Heuristics;
import ddejonge.ggp.propnet.heuristics.MoveCollector;
import ddejonge.ggp.propnet.heuristics.Rollout;
import ddejonge.ggp.tools.NotImplementedException;
import ddejonge.ggp.tools.Utils;
import ddejonge.ggp.tools.dataStructures.ArrayOfLists;
import ddejonge.ggp.tools.dataStructures.JointMove;
import ddejonge.ggp.tools.dataStructures.Pair;
import ddejonge.ggp.tools.graph.Graph;
import ddejonge.ggp.tools.visual.Monitor;
import ddejonge.ggp.tools.zobrist.TranspositionTable;

public class MCTSGraph extends Graph<MCTSNode, MCTSEdge>{
	
	
	//FIELDS
	
	//Paramaters of the algorithm
	protected MCTSParams params;
	
	//this field is initialized in the constructor with the value of params.EXPAND_THRESHOLD.
	// The reason for this is that in this way we can later change the threshold by changing this field without having to 
	// change the Params object. The Params object can then be re-used for the next game without reseting any values.
	int expansionThreshold;
	
	//stores all nodes of a given depth together. This makes it easy to remove those nodes from the graph
	// that can no longer be reached from the root node.
	ArrayOfLists<MCTSNode> depth2nodesAtDepth = new ArrayOfLists<MCTSNode>(100, 1000, true);
	
	protected StateNode root;
	transient TranspositionTable<MCTSNode> transpoTable;
	
	//transient MastTable mastTable;
	protected transient Heuristics heuristicsObject;
	protected transient RaveCalculator raveCalculator;
	
	protected transient StateMachine stateMachine;
	protected int numPlayers;
	int myRoleIndex;
	int myPlayerIndex = 0;
	
	//if this set to true it will pick a random move in case several moves are equivalent.
	transient Monitor monitor;
	protected int numRollOutsPerformed;
	//Node originalRoot;
	
	//this is not the same as the number of nodes generated:
	// We first create a new node, 
	//  then we check whether an equivalent node is already in the graph, 
	//  and only if this is not the case then we add the new node to the graph.
	private int numNodesInTree;
	
	ArrayList<Rollout> collectedRollouts;
	
	
	//CONSTRUCTOR
	public MCTSGraph(MCTSParams params, StateMachine stateMachine, Role myRole, Monitor monitor) {
		
		this.params = params;
		this.expansionThreshold = params.EXPAND_THRESHOLD;
		
		if(params.USE_TRANSPOSITION_TABLE){
			transpoTable = new TranspositionTable<MCTSNode>();
		}
		
		root = null;
		MCTSNode.numGenerated = 0;
		MCTSEdge.edgesGenerated = 0;
		
		this.stateMachine = stateMachine;
		this.numPlayers = stateMachine.getRoles().size();
		
		this.myRoleIndex = this.stateMachine.getRoleIndices().get(myRole);
		
		this.monitor = monitor;
		
		if(params.USE_MAST){
			heuristicsObject = new MastTable(stateMachine.getRoles(), params);
		}
		
		if(params.USE_RAVE){
			this.raveCalculator = new RaveCalculator(params);
		}else{
			this.raveCalculator = null;
		}
		
	}
	
	public void setHeuristics(Heuristics heuristics){
		this.heuristicsObject = heuristics;
	}
	

	
	/**
	 * This method is called at the beginning of each turn, to find the node in the graph that represents the new state of the game.
	 * @param root
	 * @param state The state is only used in case we cannot find any node that represents the given state. In that case we can create a new node with that state.
	 * @param lastMoves
	 * @return
	 */
	public void findNewRoot(MachineState state, List<GdlTerm> lastMoves){
		
		//Find the new root node in the tree.
		MCTSNode loopNode = root;
		for(int pIndex = 0; pIndex<numPlayers; pIndex++){
			int rIndex = playerIndex2roleIndex(pIndex);
			
			Move moveToFind = this.stateMachine.getMoveFromTerm(lastMoves.get(rIndex));
			
			
			//find the child of the current node that is labeled with this move.
			boolean found = false;
			if(loopNode.getOutgoingEdges() != null){
				for(MCTSEdge edge : loopNode.getOutgoingEdges()){
					if(edge.move.equals(moveToFind)){
						loopNode = edge.getTo();
						found = true;
						break;
					}
				}
			}

			if(!found){
				loopNode = null;
				System.out.println(this.getClass().getSimpleName() + ".findNewRoot() Warning! The joint move that was played is not represented in the tree: " + lastMoves);
				Utils.printStackTrace();
				
				break;
			}
			// This may happen if we haven't even managed to completely explore the search tree until
			// the next turn. In that case they players may have played a joint move that is not 
			// represented in the tree.
			// We should then simply create a new root with the current state.
		}
		
		StateNode newRoot;
		if(loopNode == null){
			boolean isTerminal = stateMachine.isTerminal(state);
			newRoot = new StateNode(state, isTerminal);
			depth2nodesAtDepth.add(root.getDepth() + numPlayers, newRoot);
		}else{
			newRoot = (StateNode)loopNode; //if loopNode is not a StateNode then something went wrong.
		}
		
		this.root = newRoot;
	}
	
	
	
	
	
	/**
	 * Creates a new root node with the given state and expands from there.
	 */
	public void expand(MachineState rootState, int maxNodesInGraph, long deadline) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		if(root == null){
			boolean isTerminal = stateMachine.isTerminal(rootState);
			root = new StateNode(rootState, isTerminal);
			depth2nodesAtDepth.add(0, root);
		}else{
			throw new NotImplementedException();
		}
	
		int numMovesMade = 0;
		expand(numMovesMade, maxNodesInGraph, deadline);
	}
	

	
	/**
	 * Finds the node corresponding to the given state in the tree, sets that node as the new root, and continues expanding.<br/>
	 * Should be called at the beginning of a new turn.
	 * 
	 * @param currentState
	 * @throws GoalDefinitionException 
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	public void expand(MachineState currentState, List<GdlTerm> lastMoves, int numMovesMade, int maxNodesInGraph, long deadline) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		//First, find the corresponding node and make it the new root.
		findNewRoot(currentState, lastMoves);
		
		//Dispose all nodes of depth lower than the root.
		// (in principle we could also discard all siblings of the root, but this is technically a bit complicated.)
		if(params.USE_CLEANUP && root.getDepth() > 1){
			cleanUpTillDepth(root.getDepth());
			this.transpoTable.cleanUp(numMovesMade);
		}
		
		expand(numMovesMade, maxNodesInGraph, deadline);
	}
	
	
	

	
	
	/**
	 * Continues expanding the  tree from the existing root.
	 */
	public void expand(int numMovesMade, int maxNodesInGraph, long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		long lastUpdate = 0;
		
		//Just for debugging.  Counts how many rollouts have been performed during the current turn.
		numRollOutsPerformed = 0;
		
		// Just for debugging. During the search, it is set to the largest depth of any leaf node visited
		int searchDepth = -1;
		
		//is increased if the leaf returned is same leaf as the previous iteration.
		// is reset to null otherwise.
		int sameLeafCounter = 0;
		MCTSNode previousLeaf = null;
		
		//This object stores for each player which actions were selected during the node selection and rollout.
		// This is then used to update the MAST table.
		MoveCollectorImpl moveCollector = null;
		if(params.USE_MAST || params.USE_RAVE){
			moveCollector = new MoveCollectorImpl(numPlayers);
		}
		
		//just for debugging
		int loopCounter = 0;
		
		while(System.currentTimeMillis() < timeout && numNodesInTree < maxNodesInGraph){
			
			loopCounter++;
			
			if(moveCollector != null){
				moveCollector.clear();
			}
			
			if(root == null){
				throw new RuntimeException("MCTSGraph.expand() Error! root == null");
			}
			
			//start at the root and select the best leaf to explore next.
			ArrayList<MCTSEdge> branchToBestLeaf = findBestLeaf(root, moveCollector, raveCalculator, true);
			MCTSNode leaf;
			if(branchToBestLeaf.size() == 0){
				leaf = root;
			}else{
				leaf = branchToBestLeaf.get(branchToBestLeaf.size()-1).getTo();
			}
			 
			
			//FOR DEBUGGING:
			MachineState leafState = leaf.getState();
			if(leafState == null){
				throw new RuntimeException("MCTSGraph.rollout() Error! leaf.state == null" );
			}
			
			//FOR DEBUGGING:
			if(leaf.getDepth() - root.getDepth() > searchDepth){
				searchDepth = leaf.getDepth() - root.getDepth();
			}
			
			//Count how often we select the same leaf in a row. If more than a certain threshold the search can stop.
			if(previousLeaf != null && leaf == previousLeaf){
				sameLeafCounter++;
			}else{
				previousLeaf = leaf;
				sameLeafCounter = 0;
			}
			
			if(sameLeafCounter > 100){
				
				/*
				System.out.println("MCTSGraph.expand() Stopping tree search, because the same leaf node has been selected more than 100 times. leaf.depth == " + leaf.getDepth());
				
				if(leaf == root){
					System.out.println("MCTSGraph.expand() leaf == root");
				}
				*/
				break;
			}

			//check if the leaf is a terminal state.
			boolean leafIsTerminal = leaf.isTerminal();
			
			if(leafIsTerminal){
				
				//get the goal values of the terminal state.
				int[] goals = getGoalsFromState(leaf.getState());
				
				update(leaf, branchToBestLeaf, goals, numMovesMade, moveCollector);
				
				/*
				//first just update the nodes of this node.
				updateSingleNode(leaf, goals, false);
				setTerminalBounds(leaf, goals);
				
				//next, update the values of the leaf and all its ancestors.
				if(leaf.incomingEdges != null){
					for(MCTSEdge incomingEdge : leaf.incomingEdges){
						MCTSNode parentNode = incomingEdge.from;
						
						update(parentNode, goals, numMovesMade, moveCollector);
					}
				}
				 */
			
			
			
			}else if(leaf != root){  //***PERFORM ROLLOUT FROM THE LEAF
				//If the leaf is in fact the root node, then we can skip this and generate children straight away.
				
				//apply rollout.
				int[] goals = rollout(leaf, moveCollector);
				
				//update values of the leaf and all its ancestors.
				update(leaf, branchToBestLeaf, goals, numMovesMade, moveCollector);
				
				
				//update the MAST Table
				if(this.heuristicsObject != null){
					this.heuristicsObject.updateAfterRollOut(moveCollector, null, goals);
				}
			}
			
			if(System.currentTimeMillis() > timeout){
				break;
			}
			
			//If the leaf has been visited enough times, create children.
			if((leaf.getRolloutCount() > expansionThreshold || leaf == root) && !leafIsTerminal){
			
				//Determine the role of the next player to make a move.
				int nextPlayerIndex = getNextPlayerIndex(leaf);
				int rIndex = playerIndex2roleIndex(nextPlayerIndex);
				Role role = stateMachine.getRoles().get(rIndex);
				
				//Determine the state in which this player makes the move.
				MachineState state = leaf.getState();
					
				//Note: there is some redundancy here, because the legal moves after the leaf node are also
				// generated inside the rollout function.
				//TODO: improve this.
				
				List<Move> legalMoves = this.stateMachine.getLegalMoves(state, role);
				if(params.RANDOMIZE_LEGAL_ACTIONS){
					Utils.shuffle(legalMoves);
				}
				
				//For some reason this doesn't work!!
				/*MoveList jointMove = null;
				if(nextPlayerIndex == numPlayers -1){
					jointMove = getPartialJointMove((IntermediateNode)leaf);
				}*/
				
				
				for(Move move : legalMoves){
					
					MCTSNode child;
					if(nextPlayerIndex == numPlayers -1){ //the next node must be a StateNode.
						
						//Get a list of moves in the branch from the previous StateNode to the current leaf, and add the move for the new child node to it.
						JointMove jointMove;
						if(numPlayers == 1){
							jointMove = new JointMove(numPlayers);
							jointMove.add(move);
						}else{
							jointMove = getPartialJointMove((IntermediateNode)leaf);
							jointMove.set(rIndex, move);
						}

						//Use this joint move to create the new MachineState object for the new StateNode.
						MachineState newState = stateMachine.getNextState(state, jointMove);
						boolean isTerminal = stateMachine.isTerminal(newState);
						
						MCTSNode newNode = new StateNode(newState, isTerminal);
						
						//Store the new node in the transposition table, or retrieve an existing node from the table if it already contains one							
						// for the new state.
						child = transpoTable.retrieve(newState, newNode, numMovesMade);
						
						//if there was no node equivalent to newNode already in the table then we will really add a new node to the graph.
						// (otherwise we are just adding a new edge between two existing nodes)
						if(child == newNode){
							depth2nodesAtDepth.add(leaf.getDepth() + 1, child);
						}
						
						
					}else{
						
						child = new IntermediateNode(leaf.getState());
						depth2nodesAtDepth.add(leaf.getDepth() + 1, child);
					}
					
					addChild(leaf, move, child);
				}
					
				//now that the leaf node has been expanded it doesn't need its state field anymore.
				// (except if we are using decision tree learning).
				if(params.DELETE_STATE_AFTER_EXPANSION && leaf.getOutgoingEdges() != null && leaf.getOutgoingEdges().size() > 0){
					leaf.deleteState();
				}
				
			
			}
			
			
			//Check the time.
			long currentTime = System.currentTimeMillis();
			if(currentTime > timeout){
				break;
			}
			if(currentTime - lastUpdate > 250){
				lastUpdate = currentTime;
				if(monitor != null){
					monitor.setValue("Time:", timeout - currentTime, false);
					monitor.setValue("Nodes generated:", numNodesInTree, false);
					monitor.setValue("Edges generated:", MCTSEdge.edgesGenerated, true);
				}
			}
			
		}
		
		/*
		System.out.println("loopCounter: " + loopCounter);
		System.out.println("Num rollouts performed: " + numRollOutsPerformed);
		System.out.println("deepest search depth: " + searchDepth + " plies.");*/
	}
	
	
	


	
	


	ArrayList<MCTSEdge> findBestLeaf(MCTSNode subTreeRoot, MoveCollector moveCollector, RaveCalculator raveCalculator, boolean expanding){
		
		
		if(moveCollector != null){
			throw new RuntimeException("MCTSGraph.findBestLeaf() Error! moveCollector != null is not implemented.");
		}
		if(raveCalculator != null){
			throw new RuntimeException("MCTSGraph.findBestLeaf() Error! raveCalculator != null is not implemented.");
		}
		
		
		ArrayList<MCTSEdge> branch = new ArrayList<MCTSEdge>();
		
		MCTSNode node = subTreeRoot;
		
		while(node.hasChildren()){
			MCTSEdge edgeToAdd = findBestEdge(node, expanding);
			
			//If we are not in expand mode we are only interested in the values of the best node, rather than the node itself.
			// Therefore, if the best leaf hasn't been explored yet (and therefore doesn't have any values) we return its parent.
			if( (!expanding) && edgeToAdd.getTo().getAverageGoals() == null){
				break;
			}
			
			
			branch.add(edgeToAdd);
			node = edgeToAdd.getTo();
		}

		
		return branch;
	}
	

	MCTSEdge findBestEdge(MCTSNode node, boolean expanding){
		
		int roleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		
		
		
		//find the highest lower bound among the children.
		// I think this is not necessary, because we have already set the boolean field 'pruned' of the edge when calculating the bounds.
		MCTSEdge edgeWithHighestLowBound = null;
		int highestLowBound = -1;
		for(MCTSEdge edge : node.getOutgoingEdges()){
			if(edge.getTo().lowerBounds != null){
				if(edge.getTo().lowerBounds[roleIndex] > highestLowBound){
					highestLowBound = edge.getTo().lowerBounds[roleIndex];
					edgeWithHighestLowBound = edge;
				}
			}
		}
		
		MCTSEdge bestEdge = null;
		for(MCTSEdge edge : node.getOutgoingEdges()){
			
			//Skip this child if it can be pruned.
			if(canBePruned(edge.getTo(), highestLowBound, roleIndex)){
				edge.pruned = true;
				continue;
			}
			
			
			//Skip this child if its values have already been determined exactly.
			// EDIT: this yields problems, because the values of the current node's ancestors will not be updated correctly.
			// EDIT2: on the other hand, it is necessary for games like dollar auction to explore the tree exhaustively.
			if(edge.getTo().isExact()){
				continue;
			}
			
			//bestEdge can be null if for all children of the current node the lowerbounds are null.
			if(bestEdge == null || edge.getHeuristic(roleIndex) > bestEdge.getHeuristic(roleIndex)){
				bestEdge = edge;
			}
		}
		
		//if the node with the highest lowBound is exact then it can happen that all nodes are either pruned, or are exact.
		if(bestEdge == null){
			bestEdge = edgeWithHighestLowBound;
		}
		
		if(bestEdge.getTo() == null){
			throw new RuntimeException("NegoMCTS_Graph.findBestLeaf() Error! bestEdge.getTo() == null");
		}
		
		//increase the edge's selction count. (only if this method is called while expanding the tree).
		if(expanding){
			bestEdge.increaseSelectionCount();
		}
		
		return bestEdge;
	}
	
	void addChild(MCTSNode parent, Move move, MCTSNode child){
		
		MCTSEdge edge = new MCTSEdge(parent, move, child);
		this.setEdge(edge);
		child.setDepth(parent.getDepth() + 1);
		
		numNodesInTree++;
		
	}
	
	/**
	 * Recursive algorithm that returns the 'best' leaf node to explore next.
	 * 
	 * Returns null if for every child of the node we have that either it is pruned, or the exact value has already been determined.
	 * In that case the tree search has finished.
	 * 
	 * @param node
	 * @param moveSelection
	 * @param raveCalculator Can be null if RAVE is not applied.
	 * @param expanding is true when this method is called while expanding the tree. Is false if we are calling this method just because
	 * we are interested in the currently best leaf node without the intention of expanding the tree.
	 * @return
	 */
	/*
	MCTSNode findBestLeaf(MCTSNode node, MoveCollectorImpl moveSelection, RaveCalculator raveCalculator, boolean expanding){
		
		if(expanding){
			node.visitCount++;
		}
		
		//if we have found a leaf node then return it.
		if(node.outgoingEdges == null || node.outgoingEdges.size() == 0){
			return node; 
		}
		
		int roleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		
		MCTSEdge bestEdge = null;
		
		//find the highest lower bound among the children.
		// I think this is not necessary, because we have already set the boolean field 'pruned' of the edge when calculating the bounds.
		int highestLowBound = -1;
		if(params.USE_BRANCH_AND_BOUND){
			for(MCTSEdge edge : node.outgoingEdges){
				if(edge.getTo().lowerBounds != null){
					if(edge.getTo().lowerBounds[roleIndex] > highestLowBound){
						highestLowBound = edge.getTo().lowerBounds[roleIndex];
						bestEdge = edge;
					}
				}
			}
		}
		//
		//if(bestEdge == null){
			//bestEdge = node.outgoingEdges.get(0);
		//}
		
		
		for(MCTSEdge edge : node.outgoingEdges){
			
			//Skip this child if it can be pruned.
			if(params.USE_BRANCH_AND_BOUND){
				
				if(canBePruned(edge.getTo(), highestLowBound, roleIndex)){
					edge.pruned = true;
					continue;
				}
				
			}
			
			//Skip this child if its values have already been determined exactly.
			// EDIT: this yields problems, because the values of the current node's ancestors will not be updated correctly.
			
			//if(edge.getTo().isExact){
				//continue;
			//}
			
			//bestEdge can be null if for all children of the current node the lowerbounds are null.
			if(bestEdge == null || edge.getHeuristic(roleIndex, raveCalculator) > bestEdge.getHeuristic(roleIndex, raveCalculator)){
				bestEdge = edge;
			}
		}
		
		
		
		if(bestEdge == null){
			throw new RuntimeException("MCTSGraph.findBestLeaf() Error!");
			
			
			
			//The case that bestEdge == null should only happen if we have already found the theoretically optimal strategy for the current node.
			//node.isExact = true;
			//return null;
		}
		if(bestEdge.getTo() == null){
			throw new RuntimeException("MCTSGraph.findBestLeaf() Error! bestEdge.getTo() == null");
		}
		
		//update the edge's UCT value (only if this method is called while expanding the tree).
		if(expanding){
			bestEdge.visitCount++;
			bestEdge.uctValue = params.UCT_CONSTANT * UCT.calculate(node.visitCount, bestEdge.visitCount);
		}
		
		
		if(moveSelection != null){
			moveSelection.add(roleIndex, bestEdge.move);
		}
		
		//If we are not in expand mode we are only interested in the values of the best node, rather than the node itself.
		// Therefore, if the best leaf hasn't been explored yet (and therefore doesn't have any values) we return its parent.
		if( (!expanding) && bestEdge.getTo().getAverageGoals() == null){
			return node;
		}
		
		MCTSNode bestLeaf = findBestLeaf(bestEdge.getTo(), moveSelection, raveCalculator, expanding);
		
		
		//if(bestLeaf == null){
			//This happens if we have already found the theoretically optimal strategy for the node bestEdge.getTo()
			//Therefore we need to call findBestLeaf again, on the current node. Note that this time however its child bestEdge.getTo() will
			// be marked as exact, so it will return a different node.
			//return findBestLeaf(node, moveSelection, raveCalculator, expanding);
		//}
		
		
		return bestLeaf;
	}
	*/
	
	/**
	 * Given the highest lower bound among its siblings, determines whether the given node can be pruned or not.
	 * @param node
	 * @param highestLowBound
	 * @param roleIndex
	 * @return
	 */
	public boolean canBePruned(MCTSNode node, int highestLowBound, int roleIndex){
		
		if(node.upperBounds == null){
			return false;
		}
		
		if(node.upperBounds[roleIndex] < highestLowBound){
			return true;
		}
		
		if(node.upperBounds[roleIndex] == highestLowBound){
			if(node.lowerBounds[roleIndex] < highestLowBound){
				return true;
			}
		}
		
		//TODO: remove the pruned node and its children entirely?
		
		return false;
	}
	
	protected JointMove getPartialJointMove(IntermediateNode node){
		
		JointMove jointMove = new JointMove(numPlayers);
		
		//Fill this list with the chosen moves until this leaf node.
		MCTSNode loopNode = node;
		int pIndex = getPlayerIndex(node);
		while(loopNode instanceof IntermediateNode){
			MCTSEdge incomingEdge = ((IntermediateNode)loopNode).getIncomingEdge();
			
			int rIndex = playerIndex2roleIndex(pIndex);
			
			jointMove.set(rIndex, incomingEdge.move);
			
			loopNode = incomingEdge.getFrom();
			pIndex--;
		}
		
		return jointMove;
	}
	
	
	
	protected int[] rollout(MCTSNode leaf, MoveCollectorImpl moveCollector) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		this.numRollOutsPerformed++;
		
		//Extract the state form the leaf
		MachineState leafState = leaf.getState();
		if(leafState == null){
			throw new RuntimeException("MCTSGraph.rollout() Error! leaf.state == null" );
		}
		
		if( ! (stateMachine instanceof PropnetStateMachine)){
			
			MachineState terminalState = stateMachine.performDepthCharge(leafState, new int[1]);
			List<Integer> goalsList = stateMachine.getGoals(terminalState);
			int[] goals = new int[goalsList.size()];
			
			for (int i = 0; i < goals.length; i++) {
				goals[i] = goalsList.get(i);
			}
			return goals;
		}
		
		PropnetStateMachine propnetStateMachine = (PropnetStateMachine)this.stateMachine;
		
		propnetStateMachine.setState(leafState);
		
		if(leaf instanceof StateNode){
			
		
		}else if(leaf instanceof IntermediateNode){
		
			IntermediateNode intermediateNode = (IntermediateNode)leaf;
			
			//determine the moves that lead from the last state node to this leaf node.
			JointMove jointMove = getPartialJointMove(intermediateNode);
			
			//Fill the partial joint move with random moves until it is full.
			fillPartialJointMove(jointMove, moveCollector);
			
			//Create the next state.
			try{
				propnetStateMachine.setState(leafState);
				propnetStateMachine.setActions(jointMove);
				propnetStateMachine.setNextStateAsCurrentState();
			
			}catch(NullPointerException e){
				
				System.out.println(jointMove);
				throw e;
			}
		}else{
			throw new RuntimeException("MCTSGraph.rollout() Error! unknown class: " + leaf.getClass().getSimpleName());
		}
		
		return propnetRollout(moveCollector);
		
	}
	
	/**
	 * Fills the given partial joint with random moves until it is full.
	 * 
	 * @param jointMove
	 * @param moveCollector
	 * @throws MoveDefinitionException
	 */
	protected void fillPartialJointMove(JointMove jointMove, MoveCollectorImpl moveCollector) throws MoveDefinitionException{
		
		//Fill the list with random moves until it is full.
		while(jointMove.size() < numPlayers){
			
			int pIndex = jointMove.size();
			int rIndex = playerIndex2roleIndex(pIndex);
			
			

			
			List<Move> legalMoves = getLegalMovesByIndex(null, rIndex);
			
			
			if(legalMoves.size() == 0){
				//TODO: remove debug
				MachineState currentState = ((PropnetStateMachine)this.stateMachine).getCurrentState();
				boolean term = this.stateMachine.isTerminal(currentState);
				System.out.println("current state size: " + currentState.getContents().size() + " terminal: " + term);
				
			}
			
			
			Move move = null;
			
			if(heuristicsObject == null){
				move = (Move)Utils.getRandomObjectFromList(legalMoves);
			}else{
				int moveIndex = heuristicsObject.getRandomMoveIndexInRollOut(rIndex, legalMoves);
				move = legalMoves.get(moveIndex);
			}
			
			jointMove.set(rIndex, move);
			if(moveCollector != null){
				moveCollector.add(rIndex, move);
			}
		}
	}
	
	private int[] propnetRollout(MoveCollectorImpl moveSelection) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		//perform rollout.
		
		int[] goals = ((PropnetStateMachine)stateMachine).performDepthCharge(moveSelection, heuristicsObject);
		
		if(moveSelection != null){
			moveSelection.getRollOut().setGoals(goals);
		}
		
		
		this.numRollOutsPerformed++;
		return goals;
	}
	
	
	
	public Move findBestMove(){
		return getBestChildEdge(this.root).move;
	}
	
	public MCTSNode getBestChildNode(MCTSNode node){
		return getBestChildEdge(node).getTo();
	}
	
	/**
	 * Called in order to find the best move to make after the tree finished expanding.
	 * @param node
	 * @return
	 */
	public MCTSEdge getBestChildEdge(MCTSNode node){
		
		//determine the best exact node (based on its utility value).
		//determine the best non-exact node (based on visit count).
		// pick the one of these two with highest utility value. 
		
		MCTSEdge bestExactEdge = null;
		float highestValue = 0.0f;
		
		MCTSEdge bestNonExactEdge = null;
		
		if(node == null){
			System.out.println("MCTSGraph.getBestChildEdge() WARNING: node == null");
			return null;
		}

		if(node.getOutgoingEdges() == null){
			System.out.println("MCTSGraph.getBestChildEdge() node.outgoingEdges == null");
			return null;
		}
		
		for(MCTSEdge childEdge : node.getOutgoingEdges()){
			MCTSNode childNode = childEdge.getTo();
			
			if(childEdge.pruned){
				continue;
			}
			
			//TODO: instead of exactness, isn't it enough here to check that the bounds for ME are equal?
			//  (exact means that the bounds for ALL players are equal)
			if(childNode.isExact()){
				
				if(bestExactEdge == null || childNode.averageGoal[myRoleIndex] > highestValue){
					bestExactEdge = childEdge;
					highestValue = childNode.averageGoal[myRoleIndex];
				}
				
			}else{
				
				if(bestNonExactEdge == null || childEdge.getSelectionCount() > bestNonExactEdge.getSelectionCount()){
					bestNonExactEdge = childEdge;
				}
			}
		}
		
		if(bestExactEdge == null){
			return bestNonExactEdge;
		}else if(bestNonExactEdge == null){
			return bestExactEdge;
		
			
		}else if(bestExactEdge.getTo() == null){
			throw new RuntimeException("MCTSGraph.getBestChildEdge() bestExactEdge.getTo() == null");
		}else if(bestNonExactEdge.getTo() == null){
			throw new RuntimeException("MCTSGraph.getBestChildEdge() bestNonExactEdge.getTo() == null");

		
		}else if(bestExactEdge.getTo().averageGoal == null){
			return bestNonExactEdge;
		}else if(bestNonExactEdge.getTo().averageGoal == null){
			return bestExactEdge;	
			
		}else if(bestExactEdge.getTo().averageGoal[myRoleIndex] >= bestNonExactEdge.getTo().averageGoal[myRoleIndex]){
			return bestExactEdge;
		}else{
			return bestNonExactEdge;
		}
	}

	

	
	
	
	/**
	 * Returns the index of the player that made the move that leads to this node.
	 * @param node
	 * @return
	 */
	int getPlayerIndex(MCTSNode node){
		
		if(node.getDepth() == 0){
			throw new RuntimeException("MCTSPlayer.getPlayerIndex() Error! you can't call this method on root node.");
		}
		
		//Each node stores a move, except the root node.
		// if the depth is 1, it stores the move of player 0 (me).
		// if the depth is 2, it stroes the move of player 1.
		// etc...
		
		return (node.getDepth()-1) % numPlayers;
	}
	
	public StateNode getRoot() {
		return root;
	}

	public Heuristics getHeuristicsObject() {
		return heuristicsObject;
	}

	/**
	 * Returns the index of the player that is to make the next move.
	 * Note that the playerIndex of myself is always 0.
	 * @param node
	 * @return
	 */
	public int getNextPlayerIndex(MCTSNode node){
		return node.getDepth() % numPlayers;
	}
	
	/**
	 * Returns the index of the player making the move.
	 * @param edge
	 * @return
	 */
	int getPlayerIndex(MCTSEdge edge){
		return getNextPlayerIndex(edge.getFrom());
	}
	
	/**
	 * The roleIndex is the index that the server has assigned to each player. 
	 * We need to use this index in order to create joint moves correctly.
	 * 
	 * However, our algorithm assumes that our role always comes first in the tree.
	 * Therefore, the algorithm assigns a playerIndex to each role.
	 * The playerIndex for our role is always 0. The rest of the roles are simply ordered
	 * according to their roleIndex.
	 * 
	 * 
	 * Suppose there are 6 players: a, b, c, d, e, f, 
	 * and that I am player d.
	 * 
	 * player a has roleIndex 0, player b has roleIndex 1, etc...
	 * player d has playerIndex 0, player a has playerIndex 1, etc...
	 * 
	 * 0, 1, 2, 3, 4, 5
	 * a, b, c, d, e, f
	 * d, a, b, c, e, f

	 * 
	 * @param playerIndex
	 * @return
	 */
	protected int playerIndex2roleIndex(int playerIndex){
		
		if(playerIndex == 0){
			return myRoleIndex;
		}else if(playerIndex <= myRoleIndex){
			//e.g. if playerIndex is 2, then the role is b,
			//  and then the roleIndex is 1.
			return playerIndex - 1;
		}else{
			return playerIndex;
		}
		
	}
	
	/**
	 * Updates the nodes involved in the rollout.
	 * Is called from expand() after each rollout.
	 * 
	 * 
	 * @param node A node representing a non-terminal state
	 * @param newGoals
	 * @param updateCounter
	 * @param numMovesMade
	 */
	void update(MCTSNode leaf, ArrayList<MCTSEdge> branch, int[] newGoals, int numMovesMade, MoveCollector selectedMoves){
		
		/*
		if(node == null){
			return;
		}
		*/
		
		
		leaf.increaseRolloutCount();
		int depth = leaf.getDepth()+1;
		
		for (int i = branch.size()-1; i >=0; i--) {
			MCTSEdge currentEdge = branch.get(i);
			MCTSNode node = currentEdge.getTo();
			
			depth = node.getDepth();
			
			
			if(i == branch.size()-1){
				
				updateLeafNode(node, newGoals);
				
				if(node.isTerminal()){
					setTerminalBounds(node, newGoals);
				}else{
					updateBounds(node);
				}
				
			}else{
				updateNonLeafNode(node, newGoals);
				updateBounds(node);
			}
			
			
			//if we are using RAVE, update siblings.
			if(params.USE_RAVE){
				
				for(MCTSEdge siblingEdge : currentEdge.getFrom().getOutgoingEdges()){
					
					if(siblingEdge == currentEdge){
						continue;
					}
					
					//Check if the action of this edge is in the rollout path.
					int roleIndex = playerIndex2roleIndex(getPlayerIndex(siblingEdge));
					if(selectedMoves.getMoves(roleIndex).contains(siblingEdge.move)){
						
						//if yes, then update the sibling.
						
						//TODO: call update on the sibling node
						/*
						 * MCTSNode sibling = siblingEdge.getTo();
						 * updateSingleNode(sibling, newGoals, true);
						 * */
						
						throw new NotImplementedException();
					}
				}
			}
			
			
			// Update the edge's rolloutCount.
			currentEdge.increaseRolloutCount();
			
			//Update the rolloutCount of the parent node.
			currentEdge.getFrom().increaseRolloutCount();
			
			//update the UCT values of the siblings of this edge of this node.
			for(MCTSEdge siblingEdge : currentEdge.getFrom().getOutgoingEdges()){
				siblingEdge.updateUCTValue(params.UCT_CONSTANT);
			}
			
		}
			
		
		//Finally also update the root node.
		MCTSNode rootNode;
		if(branch.size() == 0){
			rootNode =  leaf;
		}else{
			rootNode = branch.get(0).getFrom();
			updateNonLeafNode(rootNode, newGoals);
		}
		
		updateBounds(rootNode);
		
		
	}
	
	/**
	 * Called right after a rollout has been performed from the given leaf node.
	 * @param node
	 * @param newGoals
	 */
	void updateLeafNode(MCTSNode node, int[] newGoals){
		
		if(node.isTerminal()){
				
			if(node.getAverageGoals() != null){
				
				//for terminal nodes we don't need to update the average goals. (unless they haven't been set yet).
				// If we do update the average goals, we cause rounding errors.
				
				//Consistency check.
				for (int i = 0; i < newGoals.length; i++) {
					if(Math.abs(newGoals[i] - node.getAverageGoals()[i]) > 0.1){
						System.out.println("newGoals " + Arrays.toString(newGoals));
						System.out.println("averageGoals " + Arrays.toString(node.getAverageGoals()));
						throw new RuntimeException("NegoMCTS_Graph.updateLeafNode() Error!");
					}
				}

				return;
			}
		}
		
		if(node.getAverageGoals() == null){
			for(int i=0; i<numPlayers; i++){
				node.setAverageGoal(i, 0.0f);
			}
		}
		
		int rolloutCount = node.getRolloutCount();
		
		for(int roleIndex=0; roleIndex<numPlayers;  roleIndex++){
			
			//update the average goal value for current player.
			float sum = node.getAverageGoal(roleIndex) * ((float)(rolloutCount-1));
			sum += (float)newGoals[roleIndex];
			node.setAverageGoal(roleIndex,  sum/ ((float)rolloutCount));
		}
		
		
	}
	
	/**
	 * Updates the average goals of a node after a rollout.
	 * 
	 * @param node
	 * @param newGoals
	 */
	protected void updateNonLeafNode(MCTSNode node, int[] newGoals){
		
		//TODO: REMOVE DEBUG
		if(node.isTerminal()){
			throw new RuntimeException(this.getClass().getSimpleName() + ".updateNonLeafNode() Error!");
		}
			
		//1. Calculate the weighted sum over the non-exact nodes.

		float[] weightedSumNonExactNodes = new float[numPlayers];
		int totalWeight = 0;
		for(MCTSEdge childEdge : node.getOutgoingEdges()){
			
			if(childEdge.getTo().isExact()){
				continue;
			}
			
			int weight = childEdge.getSelectionCount();
			if(weight == 0){
				continue;
			}
			
			totalWeight += weight;
			
			MCTSNode childNode = childEdge.getTo();
			for(int roleIndex=0; roleIndex<numPlayers;  roleIndex++){
				
				if(childNode.getAverageGoals() != null){ //if childNode hasn't been explored its weight is 0 anyway, so we can safely skip it.
					weightedSumNonExactNodes[roleIndex] += weight * childNode.getAverageGoal(roleIndex);
				}
			}
			
		}
		
		if(totalWeight == 0){
			return;
		}
		
		//divide by totalWeight to obtain the weighted average.
		for(int roleIndex=0; roleIndex<numPlayers;  roleIndex++){
			weightedSumNonExactNodes[roleIndex] /= totalWeight;
		}
		
		//2 Take the max-min value of the exact nodes, and the average over the non-exact nodes.
		
		float[] maxMinValues = weightedSumNonExactNodes;
		int nextRoleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		
		for(MCTSEdge childEdge : node.getOutgoingEdges()){
			MCTSNode child = childEdge.getTo();
			
			if(child.isExact()){
				if(child.getAverageGoal(nextRoleIndex) > maxMinValues[nextRoleIndex]){
					for(int i=0; i<numPlayers; i++){
						maxMinValues = child.getAverageGoals();
					}
				}
			}
			
		}
		
		node.setAverageGoals(maxMinValues);
		
	}
	
	/**
	 * Sets the bounds of a terminal node.
	 * @param terminalNode a node that represents a terminal state.
	 * @param goals
	 */
	void setTerminalBounds(MCTSNode terminalNode, int[] goals){
		
		if(terminalNode.upperBounds == null){
			terminalNode.upperBounds = new byte[numPlayers];
		}
		if(terminalNode.lowerBounds == null){
			terminalNode.lowerBounds = new byte[numPlayers];
		}
		
		for (int i = 0; i < goals.length; i++) {
			terminalNode.upperBounds[i] = (byte) goals[i];
			terminalNode.lowerBounds[i] = (byte) goals[i];
		}
		
		terminalNode.isExact = true;
	}
	
	
	/**
	 * 
	 * @param node A node representing a non-terminal state
	 */
	void updateBounds(MCTSNode node){
		//is called from update(), which in turn is called after every rollout.
		
		//TODO: this code can be optimized, if we also pass the child node for which  
		// the bounds have changed.
		
		if(node.upperBounds == null){
			node.upperBounds = new byte[numPlayers];
		}
		if(node.lowerBounds == null){
			node.lowerBounds = new byte[numPlayers];
		}
		
		if(node.getOutgoingEdges() == null){
			Arrays.fill(node.upperBounds, (byte)100);
			return;
		}
		
		
		//First, determine the highest lowerbound among all children of the current node.
		//  this will be used next to prune children.
		
		//find the highest lower bound for the player that is to move next.
		int nextRoleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		node.nextRoleIndex = nextRoleIndex;
		int highestLowBound = -1;
		for(MCTSEdge edge : node.getOutgoingEdges()){
			if(edge == null){
				System.out.println("MCTSGraph.updateBounds() WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!! edge == null");
				continue;
			}
			if(edge.getTo() == null){
				System.out.println("MCTSGraph.updateBounds() WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!! edge.getTo() == null");
				continue;
			}
			if(edge.getTo().lowerBounds != null && edge.getTo().lowerBounds[nextRoleIndex] > highestLowBound){
				highestLowBound = edge.getTo().lowerBounds[nextRoleIndex];
			}
		}
		node.highestLowBound = highestLowBound;
		
		//A node is pruned if the upper bound for the active player is lower than the highestLowBound.
		// Or, if the upper bound is equal, and the lower bound is strictly lower
		//loop over children.
		for (int chIndex = 0; chIndex < node.getOutgoingEdges().size(); chIndex++) {
			MCTSEdge childEdge = node.getOutgoingEdges().get(chIndex);
			
			if(canBePruned(childEdge.getTo(), highestLowBound, nextRoleIndex)){
				childEdge.pruned = true;
			}
			
		}
		
		
		// NEXT UPDATE THE UPPER BOUNDS.
		// for each player the upper bound is the max over all non-pruned children.
		
		//reset the current upper bounds.
		Arrays.fill(node.upperBounds, (byte)0);
		
		//loop over children.
		for (int chIndex = 0; chIndex < node.getOutgoingEdges().size(); chIndex++) {
			MCTSEdge childEdge = node.getOutgoingEdges().get(chIndex);
			MCTSNode childNode = node.getOutgoingEdges().get(chIndex).getTo();
			
			//if ANY child does not have upper bounds, then all upper bounds of this node
			// must be set equal to 100.
			if(childNode.upperBounds == null){
				Arrays.fill(node.upperBounds, (byte)100);
				break;
			}
			
			if(childEdge.pruned){
				continue;
			}
			
			
			//update all upper bounds according to the current child.
			for(int rlIndex=0; rlIndex<childNode.upperBounds.length; rlIndex++){
						
				if(childNode.upperBounds[rlIndex] > node.upperBounds[rlIndex]){
					node.upperBounds[rlIndex] = childNode.upperBounds[rlIndex];
				}
						
			}
		}
		
		
		//NEXT UPDATE THE LOWER BOUNDS
		//for the current player, the lower bound is the max over all children.
		// for the other players the lower bound is the min over all children.
		Arrays.fill(node.lowerBounds, (byte)100);  //for all other players initialize to the maximum value.
		node.lowerBounds[nextRoleIndex] = 0; //for the next player, initialize to 0.
		
		
		
		
		//loop over children.
		for (int i = 0; i < node.getOutgoingEdges().size(); i++) {
			MCTSEdge childEdge = node.getOutgoingEdges().get(i);
			MCTSNode child = node.getOutgoingEdges().get(i).getTo();
			
			if(childEdge.pruned){
				continue;
			}
			
			for(int j=0; j<node.lowerBounds.length; j++){
					
				if(j == nextRoleIndex){
						
					if(child.lowerBounds == null){
						continue;
					}
						
					if(child.lowerBounds[j] > node.lowerBounds[j]){
						node.lowerBounds[j] = child.lowerBounds[j];
					}
					
				}else{
					
					if(child.lowerBounds == null){
						node.lowerBounds[j] = 0;
					
					}else{
					
						if(child.lowerBounds[j] < node.lowerBounds[j]){
							node.lowerBounds[j] = child.lowerBounds[j];
						}
					}
				}
					
			}
			
		}
		
		
		//Check if for every player the upper bound is equal to the lower bound.
		// If yes, then the node is labeled as 'exact'.
		node.isExact = true;
		for(int j=0; j<node.lowerBounds.length; j++){
			
			//the bounds are more precise then the average values, because the average values
			// are the result of many calculations, which cause rounding errors.
			//Therefore, if the average value of a player is not between its bounds, correct this.
			if(node.averageGoal[j] > node.upperBounds[j]){
				node.averageGoal[j] = node.upperBounds[j];
			}else if(node.averageGoal[j] < node.lowerBounds[j]){
				node.averageGoal[j] = node.lowerBounds[j];
			}
			
			if(node.lowerBounds[j] != node.upperBounds[j]){
				node.isExact = false;
			}
		}
		
		

		
		
	}
	
	/**
	 * Updates the rave values of the node.
	 * 
	 * @param node
	 * @param newGoals
	 * @param updateRave
	 */
	protected void updateRave(MCTSNode node, int[] newGoals){
		
			
		if(node.raveAverageGoal == null){
			node.raveAverageGoal = new float[numPlayers];
		}
		
		node.raveUpdateCount++;
		
		float[] arrayToUpdate = node.raveAverageGoal;
		int updateCount = node.raveUpdateCount;
		
		
		
		for(int roleIndex=0; roleIndex<numPlayers;  roleIndex++){
			
			float sum = arrayToUpdate[roleIndex] * ((float)(updateCount-1));
			sum += newGoals[roleIndex];
			arrayToUpdate[roleIndex] = sum/ ((float)updateCount);
		}
	}
	
	
	
	/**
	 * Completely cleans up this graph. <br/>
	 * Is called at the end of the game.
	 */
	public void cleanUp(){
		
		int maxDepth = depth2nodesAtDepth.getArraySize();
		cleanUpTillDepth(maxDepth);
		if(transpoTable != null){
			this.transpoTable.cleanUp(maxDepth);
		}
		this.root = null;
		
		this.heuristicsObject = null;
		this.raveCalculator = null;
		this.stateMachine = null; 
		//we only set this pointer to null, but we don't clean up the stateMachine, because the stateMachine was passed to this class via the Constructor,
		// therefore the stateMachine may still be required externally.
		
		if(this.collectedRollouts != null){
			this.collectedRollouts.clear();
		}
		
	}
	
	/**
	 * Is called from expand() at the beginning of each turn.
	 * Is also called from cleanUp()
	 * @param depth
	 */
	private void cleanUpTillDepth(int depth){
		
		for (int i = 0; i < depth && i<depth2nodesAtDepth.getArraySize(); i++) {
			List<MCTSNode> disposableNodes = depth2nodesAtDepth.get(i);
			if(disposableNodes != null){
				for(MCTSNode node : disposableNodes){
					this.removeVertex(node);
				}
				disposableNodes.clear();
				depth2nodesAtDepth.setList(i, null);
			}
		}
		
	}
	
	void serialize(String logFolderPath, String fileName){
		
		File logFolder = new File(logFolderPath);
		logFolder.mkdirs();
		
		File file = new File(logFolder, fileName);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try(
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		){
			
			objectOutputStream.writeObject(this);
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**Returns the list of rollouts collected during the expansion of the tree.
	 * WARNING: does not return a copy, but rather the list itself.
	 * */
	public List<Rollout> getCollectedRollouts(){
		return this.collectedRollouts;
	}
	
	public void clearCollectedRollouts(){
		this.collectedRollouts.clear();
	}
	
	/**
	 * Increases the expansion threshold with the given amount. <br/>
	 * The given value may be negative, in order to decrease the expansion threshold.
	 * @param increase
	 */
	public void increaseExpansionThreshold(int increase){
		this.expansionThreshold += increase;
	}

	public int getExpansionThreshold(){
		return this.expansionThreshold;
	}
	
	
	//METHODS THAT DEPEND ON THE TYPE OF STATEMACHINE USED
	int[] getGoalsFromState(MachineState machineState) throws GoalDefinitionException{
		
		if(stateMachine instanceof PropnetStateMachine){
			
			((PropnetStateMachine)stateMachine).setState(machineState);
			return ((PropnetStateMachine)stateMachine).getGoals();
		
		}else{
			int[] goals = new int[this.numPlayers];
			
			List<Integer> goalsList = stateMachine.getGoals(machineState);
			
			for (int i = 0; i < goals.length; i++) {
				goals[i] = goalsList.get(i);
			}
			
			return goals;
		}
		
		
	}
	
	/**
	 * If the stateMachine is a propnetStateMachine, then state can be null.
	 * @param state
	 * @param roleIndex
	 * @return
	 * @throws MoveDefinitionException
	 */
	protected List<Move> getLegalMovesByIndex(MachineState state, int roleIndex) throws MoveDefinitionException{
		
		if(stateMachine instanceof PropnetStateMachine){
			return ((PropnetStateMachine)stateMachine).getLegalMoves(roleIndex);
		}else{
			return stateMachine.getLegalMoves(state, stateMachine.getRoles().get(roleIndex));
		}
	}
	

}
