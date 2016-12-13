package ddejonge.ggp.propnet;



import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import ddejonge.ggp.tools.GameParser;




public class TestPropnet {

	
	public static void main(String[] args) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

			//Collect all files we can find.
			List<File> gameDescriptionFiles = collectAllDescriptionFiles();
			
			for(File gameDescriptionFile : gameDescriptionFiles){
				
				System.out.println();
				System.out.println("testing game: " + gameDescriptionFile.getName());
				boolean success = testGame(gameDescriptionFile.getAbsolutePath());
				
				if(!success){
					return;
				}
			}
		
			System.out.println("ALL GAMES FINISHED SUCCESFULLY!");
		
	}
	
	
	public static boolean testGame(String filePath) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		
		Random random = new Random();
		
		//Load the rules of TIC-TAC-TOE
		List<Gdl> rules = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\games\\breakthrough\\breakthrough.kif");
	
		
		ProverStateMachine proverStateMachine = new ProverStateMachine();
		PropnetStateMachine propnetStateMachine = new PropnetStateMachine();
		
	
		proverStateMachine.initialize(rules);
		propnetStateMachine.initialize(rules);
		
		//get the initial state!
		MachineState proverInitialState = proverStateMachine.getInitialState();
		MachineState propNetInitialState = propnetStateMachine.getInitialState();
		
		if( ! proverInitialState.equals(propNetInitialState)){
			System.out.println("Error!");
			System.out.println("proverInitialState " + proverInitialState);
			System.out.println("propNetInitialState " + propNetInitialState);
			return false;
		}
		
		//create some joint move.
		List<Move> jointMove;
		
		MachineState state = proverInitialState;
		for(int i=0; i<100; i++){
			
			System.out.println("iteration: " + i);
			
			jointMove = new ArrayList<Move>();
			
			for(Role role : propnetStateMachine.getRoles()){
				
				List<Move> proverLegalMoves = proverStateMachine.getLegalMoves(state, role);
				List<Move> propnetLegalMoves = propnetStateMachine.getLegalMoves(state, role);
				
				if(! testEquality(proverLegalMoves, propnetLegalMoves)){
					System.out.println("Error! ");
					System.out.println("state: " + state);
					System.out.println("proverLegalMoves " + role + " " + proverLegalMoves);
					System.out.println("propnetLegalMoves " + role + " " + propnetLegalMoves);
					
					return false;
				}
				
				int r = random.nextInt(proverLegalMoves.size());
				jointMove.add(proverLegalMoves.get(r));
				
			}
			
			MachineState proverNextState = proverStateMachine.getNextState(state, jointMove);
			MachineState propnetNextState = propnetStateMachine.getNextState(state, jointMove);
			
			if(! proverNextState.equals(propnetNextState)){
				System.out.println("Error!");
				System.out.println("joint move: " + jointMove);
				System.out.println("proverNextState " + proverNextState);
				System.out.println("propnetNextState " + propnetNextState);
				return false;
			}
			
			state = proverNextState;
			
			//check terminal
			boolean proverIsTerminal = proverStateMachine.isTerminal(state);
			boolean propNetIsTerminal = propnetStateMachine.isTerminal(state);
			
			if(proverIsTerminal != propNetIsTerminal){
				System.out.println("Error!");
				System.out.println("proverIsTerminal " + proverIsTerminal);
				System.out.println("propNetIsTerminal " + propNetIsTerminal);
				return false;
			}
			
			
			//check goals
			if(proverIsTerminal){
				for(Role role : propnetStateMachine.getRoles()){
					
					int proverGoal = proverStateMachine.getGoal(state, role);
					int propnetGoal = propnetStateMachine.getGoal(state, role);
					
					if(proverGoal != propnetGoal){
						System.out.println("Error!");
						System.out.println("proverGoal " + proverGoal);
						System.out.println("propnetGoal " + propnetGoal);
						return false;
					}
				}
				
				//return anyway, because we can't continue to play after a terminal state.
				System.out.println("finished! (reached a terminal state)");
				return true;
			}
			
			
		}
		
		
		
		System.out.println("finished!");
		return true;
		
	}
	
	
	static List<File> collectAllDescriptionFiles(){
		
		List<File> gameDescriptionFiles = new ArrayList<File>();
		
		File root1 = new File("C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Players\\games\\");
		
		gameDescriptionFiles.addAll(collectAllDescriptionFiles(root1));
		
		File root2 = new File("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\");
		
		gameDescriptionFiles.addAll(collectAllDescriptionFiles(root2));
		
		
		return gameDescriptionFiles;
	}
	
	
	static List<File> collectAllDescriptionFiles(File root){
		
		List<File> gameDescriptionFiles = new ArrayList<File>();
		
		List<File> directories = new ArrayList<File>();
		
		directories.add(root);
		
		while(directories.size() > 0){
			File directory = directories.remove(directories.size()-1);
			for(File sub : directory.listFiles()){
				
				if(sub.isDirectory()){
					directories.add(sub);
				}else if(sub.getName().endsWith(".kif")){
					gameDescriptionFiles.add(sub);
				}
				
			}
		}
		
		return gameDescriptionFiles;
	}
	
	static boolean testEquality(List<Move> moves1, List<Move> moves2){
		
		if(moves1.size() != moves2.size()){
			return false;
		}
		
		for(Move move : moves1){
			if( ! moves2.contains(move)){
				return false;
			}
		}
		
		for(Move move : moves2){
			if( ! moves1.contains(move)){
				return false;
			}
		}
		
		
		return true;
	}
	
	
	
	
	
	

	
}
