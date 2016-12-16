package ddejonge.ggp;



import java.io.File;
import java.util.List;
import java.util.Random;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.exception.MetaGamingException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.tools.OutOfTimeException;
import ddejonge.ggp.tools.visual.Monitor;

public abstract class BasicPlayer extends MyStateMachineGamer{
	
	
	//STATIC FIELDS
	public static final int DEFAULT_PORT = 9147;
	
	/*public boolean USE_PROPNET_FOR_MULTIPLAYER_GAMES = true;
	public boolean USE_PROPNET_FOR_SINGLEPLAYER_GAMES = true;
	*/
	
	//FIELDS
	
	final int MONITOR_X_LOCATION = 300;
	final int MONITOR_Y_LOCATION = 100;
	final int MONITOR_WIDTH = 400;
	final int DATA_MONITOR_HEIGHT = 150;
	final int CONSOLE_HEIGHT = 450;
	
	public Monitor monitor = new Monitor(this.getName(), MONITOR_X_LOCATION, MONITOR_Y_LOCATION, MONITOR_WIDTH, DATA_MONITOR_HEIGHT, CONSOLE_HEIGHT);
	
//	protected StateMachine stateMachine;
	
	/**
	 * The number of moves that have so far been made during the current game.
	 */
	protected int numMovesMade;
	Move selectedMove = null;
	
	protected int numPlayers;
	protected Role myRole;
	protected int myRoleIndex;
	
	
	//CONSTRUCTORS
	protected BasicPlayer(boolean enableMonitor){
		
		if(enableMonitor){
			initMonitor();
		}
	}
	
	protected BasicPlayer(){

		initMonitor();
    	
	}
	
	void initMonitor(){
    	
		System.setOut(this.monitor.getPrintStream());
    	System.setErr(this.monitor.getPrintStream());
    	
    	monitor.setProperty("Role:");
    	monitor.setProperty("Round:");
    	monitor.setProperty("Time:");
    	monitor.setProperty("Status:");
    	monitor.setMemoryProperty();
    	
    	monitor.setConsoleFileName(this.getName() + " console.log");
    	monitor.setDataFileName(this.getName() + " data.log");
    	
    	monitor.setValue("Round:", "-");
    	monitor.setValue("Status:", "Started at port " + DEFAULT_PORT);
	}
	
	@Override
	public void metaGame(long timeout) throws MetaGamingException
	{
		
		long l1 = System.currentTimeMillis();
		
		//Clear the monitor for a new game. By overriding metaGame we are able to clear the console before the state machine is initialized.
		this.monitor.clearConsole();
		selectedMove = null;
		
		System.out.println("BasicPlayer.metaGame() my name is: " + this.getName());
		System.out.println("BasicPlayer.metaGame() timeout: " + (timeout - l1));
		
		super.metaGame(timeout);
		
		GamerLogger.startFileLogging(getMatch(), this.getName());
		
		//NOTE: this method only works because we are extending MyStateMachineGame which is an adapted version of the StateMachineGame in the GGP Base.
		//	in the original GGP Base the method metaGame() is declared 'final', so it can't be overridden.
		//  I have removed the 'final' keyword, and added the method newMetaGame();
		//  Also, I have changed the field stateMahchine from private to protected.
		
		
		System.out.println("BasicPlayer.metaGame() number of players: " + this.stateMachine.getRoles().size());
		System.out.println();
		
	}
	
	
	/**
	 * This method is called at the beginning of each new game.
	 * @param initialState
	 */
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		//This method is called from StateMachineGamer.metaGame().
		// it is in that method, before calling this method, that the state machine is initialized.
		
		
		
		this.monitor.setLogFolderPath("log" + File.pathSeparator + Monitor.getDateString());
		
		this.monitor.setValue("Round:", 0, true);
		
		this.numMovesMade = 0;
		this.stateMachine = getStateMachine();
		
		//set my role.
		this.myRole = getRole();
		this.myRoleIndex = this.stateMachine.getRoleIndices().get(myRole);
		
		this.monitor.setLogFolderPath("log" + File.separator + Monitor.getDateString() + "_" + myRole);
		
		
    	//monitor.setLocation(MONITOR_X_LOCATION + myRoleIndex * (MONITOR_WIDTH + 100) , MONITOR_Y_LOCATION);
    	 
		
		this.numPlayers = stateMachine.getRoles().size();
		
		
		this.monitor.setValue("Role:", myRole);
		this.monitor.setValue("Time:", "?", true);
	}
	
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}
	
	
	
	
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		
		long start = System.currentTimeMillis();
		
		this.monitor.setValue("Round:", numMovesMade+1, true);
		
		//get the current state
		MachineState currentState = getCurrentState();
		

		
		//Check that the last move made according to the server equals selectedMove.
		if(selectedMove != null){
			List<GdlTerm> lastMoves = this.getMatch().getMostRecentMoves();
			if( ! selectedMove.getContents().equals(lastMoves.get(myRoleIndex))){
				System.out.println("BasicPlayer.stateMachineSelectMove() WARNING! LAST MOVE FAILED!");
				System.out.println("My last move: " + selectedMove);
				System.out.println("Moves according to server: " + lastMoves);
			}
		}
		
		selectedMove = null;
		
		try{
			selectedMove = findBestMove(currentState, timeout);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		this.numMovesMade++;
		
		this.monitor.writeLogFile();
		this.monitor.writeDataFile();
		
		List<Move> myLegalMoves = getStateMachine().getLegalMoves(getCurrentState(), this.myRole);
		
		long stop = System.currentTimeMillis();
		
		notifyObservers(new GamerSelectedMoveEvent(myLegalMoves, selectedMove, stop - start));
		
		return selectedMove;
	}

	public abstract Move findBestMove(MachineState currentState, long timeout) throws Exception;
	

	
	
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	
	// This is the default Sample Panel
	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
	
	@Override
	public void stateMachineStop() {
		System.out.println("BasicPlayer.stateMachineStop() The game has ended!");
		
		try {
			
			int goal = this.getStateMachine().getGoal(this.getCurrentState(), this.myRole);
			this.monitor.println("MyGNGNegotiator.gameFinished() Finished with goal value: " + goal);
		} catch (GoalDefinitionException e) {
			e.printStackTrace();
		}
		
		try {
			this.monitor.writeLogFile();
			this.monitor.writeDataFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stateMachineAbort() {
		System.out.println("BasicPlayer.stateMachineStop() The game has been aborted!");

	}

	int lastPrintedTime = Integer.MAX_VALUE;
	
	public void checkTime(long timeout){
		checkTime(timeout, System.currentTimeMillis());
	}
	
	public void checkTime(long timeout, long currentTime){
		
		int timeToGo = (int) (timeout - currentTime);
		
		if(timeToGo < 0){
			throw new OutOfTimeException();
		}
		
		
		int secondsToGo = timeToGo / 1000;
		
		if(secondsToGo < lastPrintedTime){
			
			//print time...
			this.monitor.setValue("Time:", secondsToGo);
			/*try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}*/
			
			lastPrintedTime = secondsToGo;
		}
	}


	
	
	
	/**
	 * Assuming the current game is a two-player turn-taking game, this returns the next state.
	 * 
	 * @param stateMachine
	 * @param state
	 * @param role
	 * @param move
	 * @return
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 */
	protected static MachineState getNextState(StateMachine stateMachine, MachineState state, Role role, Move move) throws MoveDefinitionException, TransitionDefinitionException{

		//Get all possible joint moves that are consistent with the given move.
		List<List<Move>> jointMoves = stateMachine.getLegalJointMoves(state, role, move);

		if(jointMoves.size() > 1){
			throw new RuntimeException("getNextState() Error! this method should only be called for games with no simultaneous moves. " + jointMoves);
		}
		
		//There should be only one such joint move, so take that one.
		List<Move> jointMove = jointMoves.get(0);

		//generate the next state
		MachineState nextState = stateMachine.getNextState(state, jointMove);


		return nextState;

	}
	

}
