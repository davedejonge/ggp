package ddejonge.ggp.mcts2;

class MCTS_Params {

	//STATIC FIELDS

	//FIELDS
	
	/**
	 * The number of times a leaf node must have been visited before children will be generated. This is only the initial value. 
	 */
	final public int INITIAL_EXPAND_THRESHOLD = 1;
	
	final public boolean USE_CLEANUP = true;
	
	final public boolean RANDOMIZE_LEGAL_ACTIONS = false;
	

	
	/**
	 * If true, the algorithm will add an exploration term to the search heuristic.
	 */
	final public boolean USE_UCT_TERM = true;
	
	final public float UCT_CONSTANT = 40f; 
	
	
	/**
	 * the fraction of the metaGame phase assigned to initially search for the Subgame Perfect Equilibrium.
	 */
	final public double initialSearchTime = 0.9; 
	
	/**
	 * If set to true, this algorithm will not use the true opponent utility, but instead will assume the opponents
	 * utility will equal 100 minus this agent's own utility.
	 * 
	 * This is useful in games where the true opponent's utility is unknown.
	 */
	public boolean makeZeroSumAssumption = false;
	
	/**
	 * The maximum goal value that a player can achieve. This is necessary to initialize the upper bounds of the nodes and is also used when making the
	 * zero-sum assumption.
	 */
	final public int MAX_GOAL_VALUE = 100;
	
	/**
	 * If the tree expand function picks the same leaf node this number of times in a row then it will stop.
	 */
	final public int SAME_LEAF_THRESHOLD = 100;
	
	final public int MAX_NEW_NODES = 100_000;


}
