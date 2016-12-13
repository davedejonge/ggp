package ddejonge.ggp.mcts.player;

import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.Gamer;

import ddejonge.ggp.mcts.MCTSParams;


public class MCTSPlayer_Example {

	public static void main(String[] args) throws Exception	{

    	//port number
    	int port = 9147;
    	
    	//Create an object to hold the parameters for our algorithm. This object will be initialized with default values.
    	MCTSParams params = new MCTSParams();
    	
    	//Override some of the default parameters...
		params.EXPAND_THRESHOLD = 1;
		
    	//create instance of the player.
    	Gamer gamer = new MCTSPlayer(params, "MCTSPlayer");
        
       	//wrap it in a GamePlayer object.
        GamePlayer player = new GamePlayer(port, gamer);
        
        //start it.
        player.start();
    }
}
