package ddejonge.ggp.mcts2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.tools.Utils;
import ddejonge.ggp.tools.dataStructures.ArrayOfLists;
import ddejonge.ggp.tools.dataStructures.JointMove;
import ddejonge.ggp.tools.dataStructures.MoveCollector;
import ddejonge.ggp.tools.graph.Edge;
import ddejonge.ggp.tools.graph.Graph;
import ddejonge.ggp.tools.visual.Monitor;
import ddejonge.ggp.tools.zobrist.TranspositionTable;

class MCTS_Graph extends Graph<MCTS_Node, MCTS_Edge>{

	//STATIC FIELDS

	//FIELDS
	MCTS_Params params;
	
	
	Random random = new Random();
	
	//Related to the game:
	int numPlayers;
	final public int myRoleIndex;
	int opponentRoleIndex;
	StateMachine stateMachine;
	
	
	//Related to the algorithm:
	

	
	MCTS_StateNode root = null;
	TranspositionTable<MCTS_StateNode> transpoTable;
	
	
	
	/**
	 * The number of times a leaf node must have been visited before children will be generated.
	 */
	int expansionThreshold;
	
	//This object stores for each player which actions were selected during each rollout.
	private MoveCollector moveCollector;
	 
	
	/**
	 * stores all nodes of a given depth together. This makes it easy to remove those nodes from the graph that can no longer be reached from the root node.
	 */
	ArrayOfLists<MCTS_Node> depth2nodesAtDepth = new ArrayOfLists<MCTS_Node>(100, 1000, true);
	
	//For debugging
	public Monitor monitor;
	public int numRolloutsPerformed = 0;

	private int numNodesInTree;
	

	

	//CONSTRUCTORS
	public MCTS_Graph(MCTS_Params params, StateMachine stateMachine, Role myRole, Monitor monitor){
		
		
		
		
		this.params = params;
		this.expansionThreshold = params.INITIAL_EXPAND_THRESHOLD; //the expansionThreshold may be updated later.
		
		
		this.transpoTable = new TranspositionTable<MCTS_StateNode>();
		
		this.stateMachine = stateMachine;
		/*if( ! (stateMachine instanceof PropnetStateMachine)){
			throw new RuntimeException("MCTS_ProductSearch_Negotiator.metaGame() Error! Current implementation can't handle non-propnet statemachine");
		}*/
		
		this.numPlayers = stateMachine.getRoles().size();
		this.moveCollector = new MoveCollectorImpl(numPlayers);
		
		this.myRoleIndex = this.stateMachine.getRoleIndices().get(myRole);
		
		if(myRoleIndex == 0) {
			this.opponentRoleIndex = 1;
		}else if(myRoleIndex == 1) {
			this.opponentRoleIndex = 0;
		}else {
			throw new RuntimeException("MCNS_Graph.MCNS_Graph() Error! " + myRoleIndex);
		}
		
		this.monitor = monitor;
		
	}

	//METHODS
	
	
	/**
	 * This method is called at the beginning of each turn, to find the node in the graph that  represents the new state of the game.
	 * @param root
	 * @param state The state is only used in case we cannot find any node that represents the given state. In that case we can create a new node with that state.
	 * @param lastMoves
	 * @return
	 */
	public void findNewRoot(MachineState state, List<GdlTerm> lastMoves){
		
		//Find the new root node in the tree.
		MCTS_Node loopNode = root;
		for(int pIndex = 0; pIndex<numPlayers; pIndex++){
			int rIndex = playerIndex2roleIndex(pIndex);
			
			Move moveToFind = this.stateMachine.getMoveFromTerm(lastMoves.get(rIndex));
			
			
			//find the child of the current node that is labeled with this move.
			boolean found = false;
			if(loopNode.getOutgoingEdges() != null){
				for(Edge e : loopNode.getOutgoingEdges()){
					MCTS_Edge edge = (MCTS_Edge)e;
					if(edge.getMove().equals(moveToFind)){
						loopNode = edge.getTo();
						found = true;
						break;
					}
				}
			}

			if(!found){
				loopNode = null;
				System.out.println(this.getClass().getSimpleName() + ".findNewRoot() Warning! The joint move that was played is not represented in the tree: " + lastMoves);
				/*Utils.printStackTrace();*/
				
				break;
			}
			// This may happen if we haven't even managed to completely explore the search tree until
			// the next turn. In that case the players may have played a joint move that is not 
			// represented in the tree.
			// We should then simply create a new root with the current state.
		}
		
		MCTS_StateNode newRoot;
		if(loopNode == null){
			newRoot = new MCTS_StateNode(state, false); //TODO: I'm not sure if we can really just put 'false' here for the isTerminal parameter.
			newRoot.setDepth(root.getDepth() + numPlayers);
			depth2nodesAtDepth.add(root.getDepth() + numPlayers, newRoot);
		
		}else{
			newRoot = (MCTS_StateNode)loopNode; //if loopNode is not a StateNode then something went wrong.
		}
		

		
		this.root = newRoot;
	}
	
	/**
	 * Expand from the initial state.
	 * 
	 * Keeps expanding until either the deadline is reached, or the maximum number of nodes in the tree has been reached.
	 *  
	 * 
	 * @throws GoalDefinitionException 
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	public void expandTreeRegularMCTS(int maxNodesInTree, long deadline, long deadlineToDisplay) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		expandTreeRegularMCTS(this.stateMachine.getInitialState(), maxNodesInTree, deadline, deadlineToDisplay);
	}
	
	/**
	 * If root is null, creates an entirely new root node and starts expanding from there.
	 * 
	 */
	private void expandTreeRegularMCTS(MachineState rootState, int maxNodesInTree, long deadline, long deadlineToDisplay) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		if(root == null){
			root = new MCTS_StateNode(rootState, false); //TODO: I'm not sure if we can really just put 'false' here for the isTerminal parameter.
			depth2nodesAtDepth.add(0, root);
		}
	
		int numMovesMade = 0;
		expandTreeRegularMCTS(root, numMovesMade, maxNodesInTree, deadline, deadlineToDisplay);
	}
	
	/**
	 * First tries to find the node that represents the given state, then it makes that node the new root, and 
	 * starts expanding the tree from there.
	 * 
	 * Keeps expanding until either the deadline is reached, or the maximum number of nodes in the tree has been reached.
	 * 
	 * 
	 * 
	 * @param currentState
	 * @throws GoalDefinitionException 
	 * @throws MoveDefinitionException 
	 * @throws TransitionDefinitionException 
	 */
	public void expandTreeRegularMCTS(MachineState currentState, List<GdlTerm> lastMoves, int numMovesMade, int maxNodesInTree, long deadline, long deadlineToDisplay) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		//First, find the corresponding node and make it the new root.
		findNewRoot(currentState, lastMoves);
		
		//Dispose all nodes of depth lower than the root (note: nodes with LOWER depth are the nodes ABOVE the root, i.e. HIGHER up in the tree).
		// (in principle we could also discard all siblings of the root, but this is technically a bit complicated.)
		if(params.USE_CLEANUP && root.getDepth() > 1){
			cleanUpTillDepth(root.getDepth());
			this.transpoTable.cleanUp(numMovesMade);
		}
		
		expandTreeRegularMCTS(this.getRoot(), numMovesMade, maxNodesInTree, deadline, deadlineToDisplay);
	}
	
	public int sameLeafCounter;
	int productSearchCounter = 0;
	public int loopCounter;
	
	/**
	 * Keep expanding the subtree under the given node, until either time runs out, or the maximum number of nodes in the tree has reached.
	 *
	 * This is mostly an ordinary MCTS, except that after each rollout we store it as a potential deal, and every time we generate children we
	 * update the pareto frontiers of each ancestor of the expanded node.
	 * 
	 * @param subtreeRoot
	 * @param numMovesMade
	 * @param maxNodesInTree
	 * @param timeout
	 * @param deadlineToDisplay
	 * @param confirmedDeal
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 * @throws GoalDefinitionException
	 */
	public void expandTreeRegularMCTS(MCTS_Node subtreeRoot, int numMovesMade,  int maxNodesInTree, long timeout, long deadlineToDisplay) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		//NOTE: if updateParetoFrontiers is true then storeDeals must also be true.
		
		//FOR DEBUGGING:
		long timeToGo = timeout - System.currentTimeMillis();
		
		long lastUpdate = 0;
		
		// Just for debugging. During the search, it is set to the largest depth of any leaf node visited
		int searchDepth = -1;
		
		//is increased if the leaf returned is same leaf as the previous iteration.
		// is reset to null otherwise.
		sameLeafCounter = 0;
		MCTS_Node previousLeaf = null;
		
		//just for debugging
		loopCounter = 0;
		
		
		
		while(System.currentTimeMillis() < timeout && numNodesInTree < maxNodesInTree){
			
			
			loopCounter++;
			
			if(moveCollector != null){
				moveCollector.clear();
			}
			
			if(root == null){
				throw new RuntimeException("NegoMCTS_Graph.expand() Error! root == null");
			}
			
			if(subtreeRoot.isFullyExplored()) {
				System.out.println("MCNS_Graph.expandTreeRegularMCTS() fULLY EXPLORED " + loopCounter);
				break;
			}
			
			
			
			//1. *** Start at the subtree root and select the best leaf to explore next.
			// Note that we are using the ordinary MCTS procedure to select the best leaf under the given subtreeRoot.
			// However, that subtreeRoot itself has been selected based on the target heuristics
			ArrayList<MCTS_Edge> branchToBestLeaf = selectBestLeaf(subtreeRoot, true);
			MCTS_Node leaf;
			if(branchToBestLeaf.size() == 0){
				leaf = subtreeRoot;
			}else{
				leaf = branchToBestLeaf.get(branchToBestLeaf.size()-1).getTo();
			}
			
			
			int movesMadeAtLeaf = leaf.getDepth() / numPlayers;
			
			//DEBUG CODE:
			if(numMovesMade > movesMadeAtLeaf){
				System.out.println("numMovesMade " + numMovesMade);
				System.out.println("movesMadeAtLeaf " + movesMadeAtLeaf);
				System.out.println("leaf.getDepth() " + leaf.getDepth());
				System.out.println("root.getDepth() " + root.getDepth());
				throw new RuntimeException("NegoMCTS_Graph.expand() Error!");
			}
			
			//FOR DEBUGGING:
			MachineState leafState = leaf.getState();
			if(leafState == null){
				if(leaf == root){
					System.out.println("NegoMCTS_Graph.expand() leaf == root");
				}
				throw new RuntimeException("NegoMCTS_Graph.rollout() Error! leaf.state == null leaf.depth == " + leaf.getDepth() + " leaf.class.getName() == " + leaf.getClass().getSimpleName());
			}
			
			//FOR DEBUGGING:
			if(leaf.getDepth() - root.getDepth() > searchDepth){
				searchDepth = leaf.getDepth() - root.getDepth();
			}
			
			//1b. ** Count how often we select the same leaf in a row. If more than a certain threshold the search can stop.
			if(previousLeaf != null && leaf == previousLeaf){
				sameLeafCounter++;
			}else{
				previousLeaf = leaf;
				sameLeafCounter = 0;
			}
			if(sameLeafCounter > params.SAME_LEAF_THRESHOLD){
				
				StringBuilder sb = new StringBuilder();
				for(MCTS_Edge edge : branchToBestLeaf) {
					sb.append(edge.getMove());
					sb.append(" ");
				}
				
				if(this.monitor != null) {
					this.monitor.printlnLogFile("MCNS_Graph.expand() sameLeafCounter > " + params.SAME_LEAF_THRESHOLD + " " + sb.toString());
				}
				//System.out.println("NegoMCTS_Graph.expand() sameLeafCounter > 100");
				break;
			}
			
			
			
			//2b. ** Check if the leaf is a terminal state.
			boolean leafIsTerminal = leaf.isTerminal();
			

			//2 ***PERFORM ROLLOUT FROM THE LEAF (unless the leaf is terminal)
			
			int[] goals;
			if(leafIsTerminal){
				
				//get the goal values of the terminal state.
				goals = getGoalsFromState(leaf.getState());
				if(params.makeZeroSumAssumption) {
					MCTS_Utils.applyZeroSumAssumption(goals, myRoleIndex, params.MAX_GOAL_VALUE);
				}
				

			}else {
				
				
				// 2b. **PERFORM ROLLOUT 
				// (if the leaf is in fact the root node, then we can skip this and generate children straight away).
				
				numRolloutsPerformed++;
				
				//apply rollout.
				goals = this.rollout(leaf, moveCollector, 1000);
				
				//in games where the true opponent's utility is unknown we can make the assumption that it is a zero-sum game.
				if(params.makeZeroSumAssumption) {
					MCTS_Utils.applyZeroSumAssumption(goals, myRoleIndex, params.MAX_GOAL_VALUE);
				}
				
				
			}
			
			
			//3. *** Update values of the leaf and all its ancestors (is called both for terminal and non-terminal leafs).
			this.updateBranch(leaf, branchToBestLeaf, goals);
			
			if(leafIsTerminal) {
				updateFullyExplored(leaf); //Needs to be called AFTER updateBranch, otherwise the MCTS algorithm will terminate before having updated the ancestors of this terminal node.
			}
			
			//4. *** If the leaf has been visited enough times, CREATE CHILDREN
			if((leaf.getRolloutCount() >= expansionThreshold || leaf == root) && !leafIsTerminal){
			
				
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
				
				
				for(Move move : legalMoves){
					
					MCTS_Node child;
					if(nextPlayerIndex == numPlayers -1){ //the next node must be a StateNode.
						
						//Get a list of moves in the branch from the previous StateNode to the current leaf, and add the move for the new child node to it.
						JointMove jointMove;
						if(numPlayers == 1){
							jointMove = new JointMove(numPlayers);
							jointMove.add(move);
						}else{
							jointMove = getPartialJointMove((MCTS_IntermediateNode)leaf);
							jointMove.set(rIndex, move);
						}

						//Use this joint move to create the new MachineState object for the new StateNode.
						MachineState newState = stateMachine.getNextState(state, jointMove);
						boolean isTerminal = stateMachine.isTerminal(newState);
						
						MCTS_StateNode newNode = new MCTS_StateNode(newState, isTerminal);
						
						
						//Store the new node in the transposition table, or retrieve an existing node from the table if it already contains one							
						// for the new state.
						child = transpoTable.retrieve(newState, newNode, numMovesMade);
						
						//if there was no node equivalent to newNode already in the table then we will really add a new node to the graph.
						// (otherwise we are just adding a new edge between two existing nodes)
						if(child == newNode){
							depth2nodesAtDepth.add(leaf.getDepth() + 1, child);
						}
						
						
					}else{
						child = new MCTS_IntermediateNode(leaf.getState());
						depth2nodesAtDepth.add(leaf.getDepth() + 1, child);
					}
					
					
					addChild(leaf, move, child, rIndex);
				}
				
				
				
			}
			
			
			//Check the time.
			long currentTime = System.currentTimeMillis();
			if(currentTime > timeout){
				//System.out.println("NegoMCTS_Graph.expand() currentTime > timeout");
				break;
			}
			if(currentTime - lastUpdate > 250){
				lastUpdate = currentTime;
				if(monitor != null){
					monitor.setValue("Time:", deadlineToDisplay - currentTime, false);
					monitor.setValue("Nodes generated:", numNodesInTree, false);
					monitor.setValue("Edges generated:", Edge.edgesGenerated, true);
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
					}
				}

			}
			
		}
		
	}
	

	
	/**
	 * Returns a branch starting at the given subTreeRoot and ending at the 'best' leaf as determined by the regular MCTS heuristics.
	 * @param subTreeRoot
	 * @param expanding
	 * @return
	 */
	ArrayList<MCTS_Edge> selectBestLeaf(MCTS_Node subTreeRoot, boolean expanding){
		ArrayList<MCTS_Edge> branch = new ArrayList<MCTS_Edge>();
		
		MCTS_Node node = subTreeRoot;
		
		while(node.hasChildren()){
			MCTS_Edge edgeToAdd = selectBestEdge(node, expanding);
			
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
	
	/**
	 * Pick the 'best' edge to explore next, using ordinary MCTS.
	 * @param node
	 * @param expanding
	 * @return
	 */
	private MCTS_Edge selectBestEdge(MCTS_Node node, boolean expanding){ 
		
		int roleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		
		
		
		//find the highest lower bound among the children.
		// I think this is not necessary, because we have already set the boolean field 'pruned' of the edge when calculating the bounds.
		MCTS_Edge edgeWithHighestLowBound = null;
		int highestLowBound = -1;
		for(MCTS_Edge edge : node.getOutgoingEdges()){
			if(edge.getTo().lowerBounds != null){
				if(edge.getTo().lowerBounds[roleIndex] > highestLowBound){
					highestLowBound = edge.getTo().lowerBounds[roleIndex];
					edgeWithHighestLowBound = edge;
				}
			}
		}
		
		MCTS_Edge bestEdge = null;
		for(MCTS_Edge edge : node.getOutgoingEdges()){
			
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
			throw new RuntimeException("NegoMCTS_Graph.findBestLeaf() Error! bestEdge.to == null");
		}
		
		//increase the edge's selection count. (only if this method is called while expanding the tree).
		if(expanding){
			bestEdge.increaseSelectionCount();
		}
		
		return bestEdge;
	}
	
	


	
	
	void addChild(MCTS_Node parent, Move move, MCTS_Node child, int roleIndex){
		
		MCTS_Edge edge = new MCTS_Edge(parent, move, child);
		this.setEdge(edge);
		child.setDepth(parent.getDepth() + 1);
		
		numNodesInTree++;
		
	}
	
	
	
	
	/**
	 * Completely cleans up this graph.
	 */
	public void cleanUp(){
		
		int maxDepth = depth2nodesAtDepth.getArraySize();
		cleanUpTillDepth(maxDepth);
		if(transpoTable != null){
			this.transpoTable.cleanUp(maxDepth);
		}
		this.root = null;
		
		this.stateMachine = null; 
		//we only set this pointer to null, but we don't clean up the stateMachine, because the stateMachine was passed to this class via the Constructor,
		// therefore the stateMachine may still be required externally.
		
	}
	
	/**
	 * Is called after findNewRoot()
	 */
	public void cleanUpUnreachableNodes(){
		
		if(this.getRoot().getDepth() > 1){
			this.cleanUpTillDepth(this.getRoot().getDepth());
			int numMovesMade = this.getRoot().getDepth() / numPlayers;
			this.transpoTable.cleanUp(numMovesMade);
		}
	}
	
	
	void cleanUpTillDepth(int depth){
		
		for (int i = 0; i < depth && i<depth2nodesAtDepth.getArraySize(); i++) {
			List<MCTS_Node> disposableNodes = depth2nodesAtDepth.get(i);
			if(disposableNodes != null){
				for(MCTS_Node node : disposableNodes){
					this.removeVertex(node);
				}
				disposableNodes.clear();
				depth2nodesAtDepth.setList(i, null);
			}
		}

		
	}
	
	/**
	 * Given the highest lower bound among its siblings, determines whether the given node can be pruned or not.<br/>
	 * A sibling is pruned if its upper bound for the given role is STRICTLY lower than the given highestLowBound.
	 * @param node
	 * @param highestLowBound
	 * @param roleIndex
	 * @return
	 */
	public boolean canBePruned(MCTS_Node node, int highestLowBound, int roleIndex){
		
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
	
	protected JointMove getPartialJointMove(MCTS_IntermediateNode node){
		
		JointMove jointMove = new JointMove(numPlayers);
		
		//Fill this list with the chosen moves until this leaf node.
		MCTS_Node loopNode = node;
		int pIndex = getPlayerIndex(node);
		
		while(loopNode instanceof MCTS_IntermediateNode){
			MCTS_Edge incomingEdge = ((MCTS_IntermediateNode)loopNode).getIncomingEdge();
			
			int rIndex = playerIndex2roleIndex(pIndex);
			
			jointMove.set(rIndex, incomingEdge.getMove());
			
			loopNode = incomingEdge.getFrom();
			pIndex--;
		}
		
		return jointMove;
	}
	
	
	/**
	 * 
	 * @param leaf
	 * @param moveCollector
	 * @param maxLength The maximum length that a rollout can have. Will throw an exception if it gets too long. Currently this is just for debugging purposes, we need to implement a better solution.
	 * @return
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 * @throws GoalDefinitionException
	 */
	protected int[] rollout(MCTS_Node leaf, MoveCollector moveCollector, int maxLength) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		
		int[] goals;
		
		//Extract the state form the leaf
		MachineState leafState = leaf.getState();
		if(leafState == null){
			throw new RuntimeException("NegoMCTS_Graph.rollout() Error! leaf.state == null" );
		}
		
		
		if(stateMachine instanceof PropnetStateMachine){
			
			PropnetStateMachine propnetStateMachine = (PropnetStateMachine)this.stateMachine;
			propnetStateMachine.setState(leafState);
			
			
			if(leaf instanceof MCTS_IntermediateNode){

				MCTS_IntermediateNode intermediateNode = (MCTS_IntermediateNode)leaf;
				
				//determine the moves that lead from the last state node to this leaf node.
				JointMove jointMove = getPartialJointMove(intermediateNode);
				
				//Fill the partial joint move with random moves until it is full.
				fillPartialJointMove(jointMove, moveCollector);
				
				//Create the next state.
				try{
					//propnetStateMachine.setState(leafState);
					propnetStateMachine.setActions(jointMove);
					propnetStateMachine.setNextStateAsCurrentState();
				
				}catch(NullPointerException e){
					
					System.out.println(jointMove);
					throw e;
				}
			}
			
			//TODO: check the max rollout length
			
			goals = ((PropnetStateMachine)stateMachine).performDepthCharge(moveCollector, null);
			

			
		}else {
		
			//TODO: I'm not sure what happens if the leaf is an intermediate node.
			
			MachineState state = leafState;
			
			int rolloutLength = 0;
			
			//generate joint moves until we reach a terminal state.
	        while(!stateMachine.isTerminal(state)) {
	        	
	        	List<Move> moves = stateMachine.getRandomJointMove(state);
	            state = stateMachine.getNextStateDestructively(state, moves);
	            
	            //Store each joint move.
	            moveCollector.add(new JointMove(moves));
	            rolloutLength++;
	            
	            if(rolloutLength > maxLength) {
	            	throw new RuntimeException("MCNS_Graph.rollout() Error! rolloutLength: " + rolloutLength);
	            }
	            
	        }
	        
	        goals = getGoalsFromState(state);

		}
		

		return goals;
	}
	
	/**
	 * Fills the given partial joint with random moves until it is full.
	 * 
	 * @param jointMove
	 * @param moveCollector
	 * @throws MoveDefinitionException
	 */
	protected void fillPartialJointMove(JointMove jointMove, MoveCollector moveCollector) throws MoveDefinitionException{
		
		//Fill the list with random moves until it is full.
		while(jointMove.size() < numPlayers){
			
			int pIndex = jointMove.size();
			int rIndex = playerIndex2roleIndex(pIndex);
			
			List<Move> legalMoves = getLegalMovesByIndex(null, rIndex);
			
			Move move = Utils.getRandomObjectFromList(legalMoves);
			
			jointMove.set(rIndex, move);

		}
		
		/*
		if(Verifier.noopCounter(jointMove) != 1){
			throw new RuntimeException(this.getClass().getSimpleName() + ".fillPartialJointMove() Error! " + jointMove);
		}*/
		
		if(moveCollector != null){
			moveCollector.add(jointMove);
		}
	}
	
	
	public Move findBestMove(){
		return getBestChildEdge(this.root).getMove();
	}
	
	MCTS_Node getBestChildNode(MCTS_Node node){
		return getBestChildEdge(node).getTo();
	}
	
	/**
	 * This method is used to select the best move to make.
	 * @param node
	 * @return
	 */
	MCTS_Edge getBestChildEdge(MCTS_Node node){
		
		//determine the best exact node (based on its utility value).
		//determine the best non-exact node (based on visit count).
		// pick the one of these two with highest utility value. 
		
		MCTS_Edge bestExactEdge = null;
		float highestValue = 0.0f;
		
		MCTS_Edge bestNonExactEdge = null;
		
		if(node == null){
			System.out.println("NegoMCTS_Graph.getBestChildEdge() WARNING: node == null");
			return null;
		}

		if(node.getOutgoingEdges() == null){
			System.out.println("NegoMCTS_Graph.getBestChildEdge() node.outgoingEdges == null");
			return null;
		}
		
		for(MCTS_Edge childEdge : node.getOutgoingEdges()){
			MCTS_Node childNode = childEdge.getTo();
			
			if(childEdge.pruned){
				continue;
			}
			
			//TODO: instead of exactness, isn't it enough here to check that the bounds for ME are equal?
			//  (exact means that the bounds for ALL players are equal)
			if(childNode.isExact()){
				
				if(bestExactEdge == null || childNode.getAverageGoal(myRoleIndex) > highestValue){
					bestExactEdge = childEdge;
					highestValue = childNode.getAverageGoal(myRoleIndex);
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
			throw new RuntimeException("NegoMCTS_Graph.getBestChildEdge() bestExactEdge.to == null");
		}else if(bestNonExactEdge.getTo() == null){
			throw new RuntimeException("NegoMCTS_Graph.getBestChildEdge() bestNonExactEdge.to == null");

		
		}else if(bestExactEdge.getTo().getAverageGoals() == null){
			return bestNonExactEdge;
		}else if(bestNonExactEdge.getTo().getAverageGoals() == null){
			return bestExactEdge;	
			
		}else if(bestExactEdge.getTo().getAverageGoal(myRoleIndex) >= bestNonExactEdge.getTo().getAverageGoal(myRoleIndex)){
			return bestExactEdge;
		}else{
			return bestNonExactEdge;
		}
	}

	
	
	
	
	

	

	//GETTERS AND SETTERS
	
	/**
	 * Returns the index of the player that made the move leading to this node.
	 * @param node
	 * @return
	 */
	int getPlayerIndex(MCTS_Node node){
		
		if(node.getDepth() == 0){
			throw new RuntimeException("MCTSPlayer.getPlayerIndex() Error! you can't call this method on root node.");
		}
		
		//Each node stores a move, except the root node.
		// if the depth is 1, it stores the move of player 0 (me).
		// if the depth is 2, it stroes the move of player 1.
		// etc...
		
		return (node.getDepth()-1) % numPlayers;
	}
	
	/**
	 * Returns the index of the player that is to make the next move.
	 * Note that the playerIndex of myself is always 0.
	 * @param node
	 * @return
	 */
	public int getNextPlayerIndex(MCTS_Node node){
		return node.getDepth() % numPlayers;
	}
	
	/**
	 * Returns the index of the player making the move.
	 * @param edge
	 * @return
	 */
	public int getPlayerIndex(MCTS_Edge edge){
		return getNextPlayerIndex(edge.getFrom());
	}
	
	/**
	 * The roleIndex is the index that the server has assigned to each player. <br/>
	 * We need to use this index in order to create joint moves correctly.<br/>
	 * <br/> 
	 * However, our algorithm assumes that our role always comes first in the tree. <br/>
	 * Therefore, the algorithm assigns a playerIndex to each role. <br/>
	 * The playerIndex for our role is always 0. The rest of the roles are simply ordered
	 * according to their roleIndex. <br/>
	 * <br/>
	 * 
	 * Suppose there are 6 players: a, b, c, d, e, f,<br/> 
	 * and that I am player d.<br/>
	 * <br/>
	 * player a has roleIndex 0, player b has roleIndex 1, etc...<br/>
	 * player d has playerIndex 0, player a has playerIndex 1, etc...<br/>
	 * <br/>
	 * 0, 1, 2, 3, 4, 5 <br/>
	 * a, b, c, d, e, f <br/>
	 * d, a, b, c, e, f <br/>
	 * 
	 * @param playerIndex
	 * @return
	 */
	public int playerIndex2roleIndex(int playerIndex){
		
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
	 * Loops backwards from the last node and edge back to the root and for each node and edge on this path it will call a number of update functions.
	 * <br/>
	 * For each node it will update its average rollout values, its rollout count, its lower bounds and its upper bounds.<br/>
	 * For each edge it will update its rollout count and its UCT value.<br/>
	 * <br/>
	 * Is called after selecting a leaf and performing a rollout on that leaf (or directly after selecting the leaf if it was a terminal leaf). 
	 * 
	 * @param leaf
	 * @param branch
	 * @param newGoals
	 */
	void updateBranch(MCTS_Node leaf, ArrayList<MCTS_Edge> branch, int[] newGoals){
		
		//Note: I think branch can be empty, in which case leaf is the subtree root.
		// For this reason we need to pass the leaf to this method separately.
		
		leaf.increaseRolloutCount();
		int depth = leaf.getDepth()+1;
		
		//Loop backwards from the end of the branch towards the subtree root.
		for (int i = branch.size()-1; i >=0; i--) {
			MCTS_Edge currentEdge = branch.get(i);
			MCTS_Node node = currentEdge.getTo();
			
			//Consistency check
			if(node.getDepth() != depth - 1){
				throw new RuntimeException("NegoMCTS_Graph.update() depth should increase by one! node.getDepth() " + node.getDepth() + " depth " + depth);
			}
			depth = node.getDepth();
			
			
			if(i == branch.size()-1){
				
				//recalculate the average rollout values of this node.
				updateLeafNode(node, newGoals);
				
				if(node.isTerminal()){
					//set the upper- and lower bounds of the terminal node equal to its goals.
					setTerminalBounds(node, newGoals);
				}else{
					//recalculate the upper and lower bounds of the node.
					updateBounds(node);
				}
				
			}else{
				
				//recalculate the average rollout values of this node.
				updateNonLeafNode(node, newGoals);
				
				//recalculate the upper and lower bounds of the node.
				updateBounds(node);
				
				//DEBUGGING CODE:
				if(node.isExact() && node.getAverageGoals() == null) {
					
					int numChildren;
					if(node.getOutgoingEdges() == null) {
						numChildren = 0;
					}else {
						numChildren = node.getOutgoingEdges().size();
						for(MCTS_Edge edge : node.getOutgoingEdges()) {
							System.err.println("child is terminal : " + edge.getTo().isTerminal());
							System.err.println("child is exact : " + edge.getTo().isExact());
						}
					}
					
					System.err.println("Num children: " + numChildren);
					System.err.println("node.depth " + node.getDepth());
					System.err.println("node.isTerminal " + node.isTerminal());
					System.err.println("node.upperBounds " + Arrays.toString(node.upperBounds));
					System.err.println("node.lowerBounds " + Arrays.toString(node.lowerBounds));
					throw new RuntimeException("MCNS_Graph.updateBounds() Error! ");
				}
			}
			
			// Update the edge's rolloutCount.
			currentEdge.increaseRolloutCount();
			
			//Update the rolloutCount of the parent node.
			currentEdge.getFrom().increaseRolloutCount();
			
			//update the UCT values of the siblings of this edge of this node.
			for(MCTS_Edge siblingEdge : currentEdge.getFrom().getOutgoingEdges()){
				siblingEdge.updateUCTValue(params.UCT_CONSTANT);
			}
			
		}
		
		//Finally also update the root node of the subtree that we are exploring..
		MCTS_Node subtreeRoot;
		if(branch.size() == 0){
			subtreeRoot =  leaf;
		}else{
			subtreeRoot = branch.get(0).getFrom();
			updateNonLeafNode(subtreeRoot, newGoals);
		}
		
		updateBounds(subtreeRoot);
			
	}
	
	/**
	 * Sets the field 'fullyExplored' of the given node to 'true'  either if all nodes in the subtree underneath this node have been generated. <br/>
	 * Is called after a terminal node has been selected and its ancestors have been updated.
	 * @param node
	 */
	void updateFullyExplored(MCTS_Node node) {
		
		boolean allChildrenFullyExplored;
		
		if(node.isTerminal()) {
			
			allChildrenFullyExplored = true;
			node.setFullyExplored(true); //Not really necessary, because it is directly set to true when the terminal node is created.
		
		}else if(node.hasOutgoingEdges()) {
			
			allChildrenFullyExplored = true;
			for(MCTS_Edge outEdge : node.getOutgoingEdges()) {
				MCTS_Node child = outEdge.getTo(); 
				if(!child.isFullyExplored()) {
					allChildrenFullyExplored = false;
					break;
				}
			}
			
		}else {
			allChildrenFullyExplored = false; //the node is non-terminal, but its children haven't been generated yet.
			//(I don't think this can actually happen, but it doesn't hurt). 
		}
		
		
		if(allChildrenFullyExplored) {
			
			//also mark the current node also as 'fully explored'
			node.setFullyExplored(true);
			
			//now check if its ancestors are fully explored too.
			if(node.getIncomingEdges() != null) {
				for(MCTS_Edge edge : node.getIncomingEdges()) {
					MCTS_Node parent = edge.getFrom();
					
					updateFullyExplored(parent);
					
				}
			}
			
		}
		
		
	}
	
	

	


	

	
	
	
	

	/**
	 * Sets the upper- and lower-bounds of a terminal node equal to its goal values.
	 * 
	 * Is called from update(), which is every time after selecting a leaf node and performing a rollout from that leaf node.
	 * 
	 * @param terminalNode
	 * @param goals
	 */
	void setTerminalBounds(MCTS_Node terminalNode, int[] goals){
		
		if(!terminalNode.isTerminal()) {
			throw new RuntimeException("MCNS_Graph.setTerminalBounds() Error! given node is not terminal!");
		}
		
		
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
		
		terminalNode.setExact(true);
	}
	
	
	
	/**
	 * Is called from update(), which in turn is called after every rollout.
	 * 
	 * Update the lower and upper bounds of this node, based on the bounds of its children. 
	 * This will allow us to prune nodes and to skip the exploration of nodes for which we have already determined their exact values.
	 * 
	 * @param node
	 */
	void updateBounds(MCTS_Node node){
		
		//TODO: this code can be optimized, if we also pass the child node for which  
		// the bounds have changed.
		
		if(node.upperBounds == null){
			node.upperBounds = new byte[numPlayers];
		}
		if(node.lowerBounds == null){
			node.lowerBounds = new byte[numPlayers];
		}
		
		//initialize the upper bounds with the maximum achievable goal value.
		if(node.getOutgoingEdges() == null){
			Arrays.fill(node.upperBounds, (byte)params.MAX_GOAL_VALUE);
			return;
		}
		
		
		//First, determine the highest lowerbound among all children of the current node.
		//  this will be used next to prune children.
		
		//find the highest lower bound for the player that is to move next.
		int nextRoleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		int highestLowBound = -1;
		for(MCTS_Edge edge : node.getOutgoingEdges()){
			if(edge == null){
				System.out.println("NegoMCTS_Graph.updateBounds() WARNING!! edge == null");
				continue;
			}
			if(edge.getTo() == null){
				System.out.println("NegoMCTS_Graph.updateBounds() WARNING!! edge.to == null");
				continue;
			}
			if(edge.getTo().lowerBounds != null && edge.getTo().lowerBounds[nextRoleIndex] > highestLowBound){
				highestLowBound = edge.getTo().lowerBounds[nextRoleIndex];
			}
		}
		
		//A node is pruned if the upper bound for the active player is lower than the highestLowBound.
		// Or, if the upper bound is equal, and the lower bound is strictly lower.
		//loop over children.
		for (int chIndex = 0; chIndex < node.getOutgoingEdges().size(); chIndex++) {
			MCTS_Edge childEdge = node.getOutgoingEdges().get(chIndex);
			
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
			MCTS_Edge childEdge = node.getOutgoingEdges().get(chIndex);
			MCTS_Node childNode = node.getOutgoingEdges().get(chIndex).getTo();
			
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
			MCTS_Edge childEdge = node.getOutgoingEdges().get(i);
			MCTS_Node child = node.getOutgoingEdges().get(i).getTo();
			
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
		
		
		
		//Check if for every player the upper bound of this node is equal to the lower bound.
		// If yes, then the node is labeled as 'exact'.
		boolean exact = true;
		for(int j=0; j<node.lowerBounds.length; j++){
			
			
			//the bounds are more precise then the average values, because the average values
			// are the result of many calculations, which cause rounding errors.
			//Therefore, if the average value of a player is not between its bounds, correct this.
			if(node.getAverageGoals() != null) {
				if(node.getAverageGoal(j) > node.upperBounds[j]){
					node.setAverageGoal(j, node.upperBounds[j]);
				}else if(node.getAverageGoal(j) < node.lowerBounds[j]){
					node.setAverageGoal(j, node.lowerBounds[j]);
				}
			}
			
			
			if(node.lowerBounds[j] != node.upperBounds[j]){
				exact = false;
			}
		}
		
		//If the averageGoals haven't been set yet, then set them equal to the bounds.
		if(exact && node.getAverageGoals() == null) {
			for(int i=0; i<node.lowerBounds.length; i++) {
				node.setAverageGoal(i, node.lowerBounds[i]);
			}
		}
		node.setExact(exact);
				
	}
	
	
	/**
	 * Called right after a rollout has been performed from the given leaf node.
	 * 
	 * Recalculates the average rollout value of this node.
	 * 
	 * @param node
	 * @param newGoals
	 */
	void updateLeafNode(MCTS_Node node, int[] newGoals){
		
		if(node.isTerminal()){
				
			if(node.getAverageGoals() != null){
				
				//for terminal nodes we don't need to update the average goals. (unless they haven't been set yet).
				// If we do update the average goals, we cause rounding errors.
				
				//Consistency check: check that the average goals that are already stored in the terminal node are equal to the given goals.
				for (int i = 0; i < newGoals.length; i++) {
					if(Math.abs(newGoals[i] - node.getAverageGoal(i)) > 0.1){
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
	 * Is called from update() which is called after every rollout.
	 * 
	 * @param node
	 * @param newGoals
	 */
	protected void updateNonLeafNode(MCTS_Node node, int[] newGoals){
		
		//TODO: REMOVE DEBUG
		if(node.isTerminal()){
			throw new RuntimeException("NegoMCTS_Graph.updateNonLeafNode() Error!");
		}
			
		//1. Calculate the weighted sum over the non-exact nodes.

		float[] weightedSumNonExactNodes = new float[numPlayers];
		int totalWeight = 0;
		for(MCTS_Edge childEdge : node.getOutgoingEdges()){
			
			if(childEdge.getTo().isExact()){
				continue;
			}
			
			int weight = childEdge.getSelectionCount();
			if(weight == 0){
				continue;
			}
			
			totalWeight += weight;
			
			MCTS_Node childNode = childEdge.getTo();
			for(int roleIndex=0; roleIndex<numPlayers;  roleIndex++){
				
				if(childNode.getAverageGoals() != null){ //if childNode hasn't been explored its weight is 0 anyway, so we can safely skip it.
					weightedSumNonExactNodes[roleIndex] += weight * childNode.getAverageGoal(roleIndex);
				}
			}
			
		}
		
		if(totalWeight == 0){ //all children are exact (or never selected).
			return;
		}
		
		//divide by totalWeight to obtain the weighted average.
		for(int roleIndex=0; roleIndex<numPlayers;  roleIndex++){
			weightedSumNonExactNodes[roleIndex] /= totalWeight;
		}
		
		//2 Take the max-min value of the exact nodes and the average over the non-exact nodes.
		
		float[] maxMinValues = weightedSumNonExactNodes;
		int nextRoleIndex = playerIndex2roleIndex(getNextPlayerIndex(node));
		
		for(MCTS_Edge childEdge : node.getOutgoingEdges()){
			MCTS_Node child = childEdge.getTo();
			
			if(child.isExact()){
				if(child.getAverageGoal(nextRoleIndex) > maxMinValues[nextRoleIndex]){  //TODO: here the algorithm sometimes crashes because child.averageGoals == null.
					for(int i=0; i<numPlayers; i++){
						maxMinValues = child.getAverageGoals();
					}
				}
			}
			
			//Q: how can a node be marked as 'exact' while it does not have its average goals set?
			//Normally, average goals are set in the method updateLeafNode, which is called after a rollout has been performed.
			
		}
		
		node.setAverageGoals(maxMinValues);
		
	}
	
	
	
	
	
	public MCTS_StateNode getRoot() {
		return this.root;
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
	
	public int getNumNodesInTree() {
		return numNodesInTree;
	}
	
	//METHODS THAT DEPEND ON THE TYPE OF STATEMACHINE USED
	int[] getGoalsFromState(MachineState machineState) throws GoalDefinitionException{
		
		int[] goals;
		
		if(stateMachine instanceof PropnetStateMachine){
			
			((PropnetStateMachine)stateMachine).setState(machineState);
			goals = ((PropnetStateMachine)stateMachine).getGoalsAsArray(); //this method is only defined for PropnetStateMachine.
		
		}else{
			goals = new int[this.numPlayers];
			
			List<Integer> goalsList = stateMachine.getGoals(machineState);
			
			for (int i = 0; i < goals.length; i++) {
				goals[i] = goalsList.get(i);
			}
			
			
		}
		

		return goals;
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

	//STATIC METHODS
}
