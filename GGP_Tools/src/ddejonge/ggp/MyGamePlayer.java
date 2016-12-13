package ddejonge.ggp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.event.PlayerDroppedPacketEvent;
import org.ggp.base.player.event.PlayerReceivedMessageEvent;
import org.ggp.base.player.event.PlayerSentMessageEvent;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.statemachine.random.RandomGamer;
import org.ggp.base.player.request.factory.RequestFactory;
import org.ggp.base.player.request.grammar.AbortRequest;
import org.ggp.base.player.request.grammar.InfoRequest;
import org.ggp.base.player.request.grammar.PlayRequest;
import org.ggp.base.player.request.grammar.Request;
import org.ggp.base.util.http.HttpReader;
import org.ggp.base.util.http.HttpWriter;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.observer.Event;
import org.ggp.base.util.observer.Observer;
import org.ggp.base.util.observer.Subject;

/**
 * Adaptation of the GamePlayer class in the GGP Base. 
 * 
 * It adds the following functionality to the original class:
 * 
 * Counts how many INFO messages have been received after the last PLAY message. If this passes a threshold,
 * the game is aborted, because apparently something went wrong (e.g. a PLAY message from the game manager got lost) .
 * This is necessary, because if we don't abort the match the player continues to wait for the PLAY message, while
 * the Game Manager continues sending INFO messages.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public final class MyGamePlayer extends Thread implements Subject
{
    private final int port;
    private final Gamer gamer;
    private ServerSocket listener;
    private final List<Observer> observers;
    
    final int MAX_INFO_MESSAGES = 10;
    int numInfoMessagesAfterPlayMessage = 0;

    public MyGamePlayer(int port, Gamer gamer) throws IOException
    {
        observers = new ArrayList<Observer>();
        listener = null;

        while(listener == null) {
            try {
                listener = new ServerSocket(port);
            } catch (IOException ex) {
                listener = null;
                port++;
                System.err.println("Failed to start gamer on port: " + (port-1) + " trying port " + port);
            }
        }

        this.port = port;
        this.gamer = gamer;
    }

	@Override
	public void addObserver(Observer observer)
	{
		observers.add(observer);
	}

	@Override
	public void notifyObservers(Event event)
	{
		for (Observer observer : observers)
		{
			observer.observe(event);
		}
	}

	public final int getGamerPort() {
	    return port;
	}

	public final Gamer getGamer() {
	    return gamer;
	}

	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			try
			{
				Socket connection = listener.accept();
				String in = HttpReader.readAsServer(connection);
				if (in.length() == 0) {
				    throw new IOException("Empty message received.");
				}

				notifyObservers(new PlayerReceivedMessageEvent(in));
				GamerLogger.log("GamePlayer", "[Received at " + System.currentTimeMillis() + "] " + in, GamerLogger.LOG_LEVEL_DATA_DUMP);

				Request request = new RequestFactory().create(gamer, in);
				String out = request.process(System.currentTimeMillis());
				
				HttpWriter.writeAsServer(connection, out);
				connection.close();
				notifyObservers(new PlayerSentMessageEvent(out));
				GamerLogger.log("GamePlayer", "[Sent at " + System.currentTimeMillis() + "] " + out, GamerLogger.LOG_LEVEL_DATA_DUMP);
				
				
				
				
				//Abort the match if we have received 10 info messages since the last play message.
				if(gamer.getMatch() != null){
					
					if(request instanceof PlayRequest){
						
						numInfoMessagesAfterPlayMessage = 0;
					
					}else if(request instanceof InfoRequest){
						
						numInfoMessagesAfterPlayMessage++;
					
						if(numInfoMessagesAfterPlayMessage >= MAX_INFO_MESSAGES){
							
							numInfoMessagesAfterPlayMessage = 0;
							System.out.println("MyGamePlayer.run() ABORTING MATCH!");
							
							//The code to abort a match is inside the method AbortRequest.process(), so we manually need 
							// to create an AbortRequest object to execute this code. A bit ugly, but I don't see a better
							// solution.
							
							AbortRequest abortRequest = new AbortRequest(gamer, gamer.getMatch().getMatchId());
							abortRequest.process(System.currentTimeMillis());
							
							//Returns the String 'aborted' which is normally send back to the Game Manager, but in this case
							// we simply ignore the returned string, because the Game Manager isn't expecting an abort anyway.
						}
					}
				
				}


			}
			catch (Exception e)
			{
				notifyObservers(new PlayerDroppedPacketEvent());
			}
		}
	}

	// Simple main function that starts a RandomGamer on a specified port.
	// It might make sense to factor this out into a separate app sometime,
	// so that the GamePlayer class doesn't have to import RandomGamer.
	public static void main(String[] args)
	{
		if (args.length != 1) {
			System.err.println("Usage: GamePlayer <port>");
			System.exit(1);
		}

		try {
			MyGamePlayer player = new MyGamePlayer(Integer.valueOf(args[0]), new RandomGamer());
			player.run();
		} catch (NumberFormatException e) {
			System.err.println("Illegal port number: " + args[0]);
			e.printStackTrace();
			System.exit(2);
		} catch (IOException e) {
			System.err.println("IO Exception: " + e);
			e.printStackTrace();
			System.exit(3);
		}
	}
}