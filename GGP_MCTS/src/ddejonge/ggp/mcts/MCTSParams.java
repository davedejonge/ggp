package ddejonge.ggp.mcts;

import java.io.Serializable;

import ddejonge.ggp.tools.sampler.RandomSampler;

/**
 * A class to hold all parameters of the MCTS algorithm that can be tweaked.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class MCTSParams implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	
	//PARAMETERS FOR DEBUGGING
	
	//if set to true the graph will store a copy of itself to hard disk after every turn.
	public boolean SERIALIZE = false;

	/**
	 * Generally should be true, however for debugging it is sometimes easier if it's false.
	 */
	public boolean RANDOMIZE_LEGAL_ACTIONS = true; 
	
	
	//BASIC PARAMETERS
	
	public boolean USE_PROPNET = true;
	
	public boolean USE_ASP_FOR_SINGLE_PLAYER_GAMES = true;
	
	/**
	 * If this value is set to, for example, 5, it means that a new child is only created for a leaf node if at least 5 rollouts have  
	 * been performed from this leaf node.
	 */
	public int EXPAND_THRESHOLD = 1;
	
	public float UCT_CONSTANT = 40f; 
	
	public boolean USE_TRANSPOSITION_TABLE = true;

	public boolean USE_CLEANUP = true;
	
	/**Stop expanding the search tree when there are more than this amount of nodes in the tree.
	 * Setting this field to a lower value can be useful if you run out of memory.
	 * */
	public int MAX_NODES_IN_TREE = Integer.MAX_VALUE;
	
	/**If set to true, the state which is stored in each node is deleted as soon as we have generated children for that node.*/
	public boolean DELETE_STATE_AFTER_EXPANSION = true;

	public boolean USE_BRANCH_AND_BOUND = true;
	
	//PARAMETERS FOR MAST
	public boolean USE_MAST = false;
	public double MAST_MAX_UNNORMALIZED_PROB = 4.0;
	public double MAST_LINEAR_OFFSET = 50.0;
	public int MAST_SAMPLING_METHOD = RandomSampler.GIBBS;
	
	
	//PARAMETERS FOR RAVE
	public boolean USE_RAVE = false;
	public double RAVE_EQUIVALENCE_PARAMETER = 500.0;
	
	
}
