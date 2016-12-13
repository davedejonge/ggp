package ddejonge.ggp.asp.puzzleSolver;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.asp.ASP2GDLConverter;

public class SolutionParser {

	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS

	//STATIC METHODS
	
	//e.g. does(robot,move(1,1,3,2),1) does(robot,move(3,2,2,4),2) does(robot,move(2,4,4,5),3) does(robot,move(4,5,6,4),4) does(robot,move(6,4,5,2),5) does(robot,move(5,2,3,1),6) does(robot,move(3,1,1,2),7) does(robot,move(1,2,3,3),8) does(robot,move(3,3,2,5),9) does(robot,move(2,5,1,3),10) does(robot,move(1,3,2,1),11) does(robot,move(2,1,4,2),12) does(robot,move(4,2,5,4),13) does(robot,move(5,4,3,5),14) does(robot,move(3,5,1,4),15) does(robot,move(1,4,2,2),16) does(robot,move(2,2,4,1),17) does(robot,move(4,1,6,2),18) does(robot,move(6,2,4,3),19) does(robot,move(4,3,5,1),20) does(robot,move(5,1,6,3),21) does(robot,move(6,3,5,5),22) does(robot,move(5,5,3,4),23) does(robot,move(3,4,1,5),24) does(robot,move(1,5,2,3),25) does(robot,move(2,3,4,4),26) does(robot,move(4,4,6,5),27) does(robot,move(6,5,5,3),28) does(robot,move(5,3,6,1),29) does(robot,move(6,1,4,2),30)
	public static Move[] parseMoves(String solutionString){
		
		//assumes the given string consists only of does propositions.
		
		List<String> moveStrings = new ArrayList<String>();
		
		int i1 = solutionString.indexOf("does(", 0);
		int i2;
		while(true){
			
			i2 = solutionString.indexOf("does(", i1 + 1);
			
			if(i2 == -1){
				moveStrings.add(solutionString.substring(i1, solutionString.length()));
				break;
			}
			
			moveStrings.add(solutionString.substring(i1, i2).trim());
			
			i1 = i2;
		}
		
		Move[] moves = new Move[moveStrings.size()];
		for (String moveString : moveStrings) {
			
			String[] args = getArguments(moveString);
			
			//the first argument is the role.
			// the second argument is the move
			// the third argument is the time
			
			GdlTerm moveTerm = ASP2GDLConverter.parseTerm(args[1]);
			int index = Integer.parseInt(args[2]) - 1;
			
			moves[index] = new Move(moveTerm);
			
			/*moves[index] = getMoveByString(allPosibleMoves, args[1]);*/
		}
		
		return moves;
	}
	
	
	//e.g. 
	//if given  does(robot,move(1,1,3,2),1)
	// returns {robot, move(1,1,3,2) , 1}
	static String[] getArguments(String moveString){
		
		String[] args = new String[3];
		
		moveString = moveString.substring(5, moveString.length()-1); //e.g. robot,move(1,1,3,2),1
		int firstCommaIndex = moveString.indexOf(",");
		int lastCommaIndex = moveString.lastIndexOf(",");
		
		args[0] = moveString.substring(0, firstCommaIndex);
		args[1] = moveString.substring(firstCommaIndex+1, lastCommaIndex);
		args[2] = moveString.substring(lastCommaIndex+1, moveString.length());
		
		return args;
		
	}
	
	
}
