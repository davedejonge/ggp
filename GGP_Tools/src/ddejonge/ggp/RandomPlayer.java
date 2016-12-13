package ddejonge.ggp;

import java.io.IOException;
import java.util.List;

import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.tools.Utils;

public class RandomPlayer extends BasicPlayer {

	public static void main(String[] args) throws IOException {
    	
		//segetCapturesFromMovest port number
    	int port = (args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT);

    	//create instance of the player.
    	Gamer gamer = new RandomPlayer();
        
       	//create instance of the player.
        GamePlayer player = new GamePlayer(port, gamer);

        //start it.
        player.start();

	}
	
	RandomPlayer(){
		this.monitor.enable(false);
	}
	
	@Override
	public Move findBestMove(MachineState currentState, long timeout)
			throws Exception {


		List<Move> legalMoves = this.stateMachine.getLegalMoves(currentState, myRole);
		Move randomMove = (Move) Utils.getRandomObjectFromList(legalMoves);
		
		return randomMove;
	}

}
