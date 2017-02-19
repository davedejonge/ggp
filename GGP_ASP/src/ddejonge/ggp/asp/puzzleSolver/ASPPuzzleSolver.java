package ddejonge.ggp.asp.puzzleSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.statemachine.Move;

import ddejonge.asp.ASPRunner;
import ddejonge.asp.Result;
import ddejonge.ggp.asp.GDL2ASPConverter;
import ddejonge.ggp.asp.dependencyGraph.DependencyGraph;
import ddejonge.ggp.asp.dependencyGraph.DependencyGraphFactory;
import ddejonge.ggp.tools.GameParser;

public class ASPPuzzleSolver {

	//STATIC FIELDS
	
	
	
	public static void main(String[] args) {
		
		//List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\knightsTour\\knightsTour.kif");
		//List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\bandl3\\bandl3_player.kif");
		//List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\sudokuGrade6H\\sudoku.kif");
		List<Gdl> description = GameParser.file2rules("C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\8puzzle\\rulesheet.kif");
		
		List<Gdl> newDescription = DeORer.run(description);
		
		ASPPuzzleSolver solver = new ASPPuzzleSolver(newDescription);
		//solver.printAllRules();
		
		System.out.println("ASPPuzzleSolver.main() searching for solution...");
		boolean solutionFound = solver.findSolution(System.currentTimeMillis() + 30_000);
		
		if(solutionFound){
			System.out.println("solution found!");
			System.out.println(Arrays.toString(solver.solution));
		}else{
			System.out.println("no solution found.");
		}
		
	}
	
	

	//FIELDS
	ArrayList<GdlConstant> gdlKeyWords;
	
	Move[] solution = null;

	List<String> temporalizedRules;
	List<String> moveDomainRules;
	List<String> solverRules;
	
	DependencyGraph dependencyGraph;
	
	//CONSTRUCTORS
	public ASPPuzzleSolver(List<Gdl> description){
		
		gdlKeyWords = new ArrayList<GdlConstant>();
		gdlKeyWords.add(GdlPool.BASE);
		gdlKeyWords.add(GdlPool.DOES);
		gdlKeyWords.add(GdlPool.GOAL);
		gdlKeyWords.add(GdlPool.INIT);
		gdlKeyWords.add(GdlPool.INPUT);
		gdlKeyWords.add(GdlPool.LEGAL);
		gdlKeyWords.add(GdlPool.NEXT);
		gdlKeyWords.add(GdlPool.ROLE);
		gdlKeyWords.add(GdlPool.TERMINAL);
		gdlKeyWords.add(GdlPool.TRUE);
		
		//Remove disjunctions from the description, because they cause errors.
		description = DeORer.run(description);
		
		System.out.println("ASPPuzzleSolver.ASPPuzzleSolver() generating dependency graph...");
		dependencyGraph = DependencyGraphFactory.constructGraph(description);
		
		//Get temporalized versions of the original rules.
		System.out.println("ASPPuzzleSolver.ASPPuzzleSolver() temporalizing rules...");
		temporalizedRules = getTemporalizedRules(description);
		
		
		
		//Get the possible moves.
		/*
		System.out.println("ASPPuzzleSolver.ASPPuzzleSolver() determining moves (constructing graph)...");
		DomainGraph graph = new DomainGraph(description);
		System.out.println("ASPPuzzleSolver.ASPPuzzleSolver() determining moves...");
		
		List<GdlTerm> moves = graph.getArgumentDomain(GdlPool.DOES.toString(), 2, 1);
		//moves = getKnightsTourMoves();
		
		System.out.println("ASPPuzzleSolver.ASPPuzzleSolver() constructing move domain rules...");*/
		String roleName = getRoleName(description); //Get the role name.
		this.moveDomainRules = getMoveDomainRules(description, roleName);
		
		//Add rules for the ASP solver.
		System.out.println("ASPPuzzleSolver.ASPPuzzleSolver() adding solver rules...");
		int targetGoal = 100; //TODO: get the correct target goal.
		solverRules = getSolverRules(roleName, targetGoal);
		
		//printAllRules();
		//findSolution(10000);
		
	}
	
	
	
	//METHODS
	//Note: we may use the DomainGraph instead of using this method.
	String getRoleName(List<Gdl> description){
		
		for(Gdl gdl : description){
			
			if(gdl instanceof GdlRule){
				
				GdlRule rule = (GdlRule)gdl;
				
				if(rule.getHead().getName().equals(GdlPool.ROLE)){
					//TODO: handle this case.
					// unlikely that this happens, but to be sure.
					throw new RuntimeException("ASPPuzzleSolver.getRoleName() Error! we currently cannot yet handle roles defined in the head of a rule.");
				}
				
			
			}else if(gdl instanceof GdlRelation){
				GdlRelation fact = (GdlRelation)gdl;
				
				if(fact.getName().equals(GdlPool.ROLE)){
					return fact.getBody().get(0).toString();
				}
				
			}else if(gdl instanceof GdlProposition){
				continue;
			}
			
		}
		
		throw new RuntimeException("ASPPuzzleSolver.getRoleName() Error! Role not found!");
	}
	
	
	List<String> getTemporalizedRules(List<Gdl> description){
		
		List<String> tempRules = new ArrayList<String>(description.size());
		
		for(Gdl gdl : description){
			
			if(gdl instanceof GdlRule){
				GdlRule rule = (GdlRule)gdl;
				
				if(rule.getHead().getName().equals(GdlPool.BASE) || rule.getHead().getName().equals(GdlPool.INPUT)){
					continue;
				}
				
				tempRules.add(temporalizeRule(rule));
				
			}else if(gdl instanceof GdlSentence){
				GdlSentence fact = (GdlSentence)gdl;
				
				tempRules.add(temporalizeFact(fact));
				
			}else{
				throw new RuntimeException("ASPPuzzleSolver.ASPPuzzleSolver() Error!");
			}
		}
		
		return tempRules;
	}

	
	String temporalizeRule(GdlRule rule){
		
		GdlSentence head = rule.getHead();
		
		String headArgument = "T";
		String bodyArgument = "T";
		
		if(head.getName().equals(GdlPool.INIT)){
			
			headArgument = "1";
			bodyArgument = "1";
		
		}else if(head.getName().equals(GdlPool.NEXT)){
			
			headArgument = "T+1";
			bodyArgument = "T";
			
		}else if(head.getName().equals(GdlPool.ROLE)){
			
			//note that since we are assuming the game is a single-player game it is unlikely that there
			// will be any rule with Role in the head. However, we do take the possibility into account, 
			// just to be sure.
			
			headArgument = null;
			bodyArgument = null;
		}
		
		boolean[] temporalized = new boolean[1];
		String s = temporalizeSentence(head, headArgument, temporalized);
		
		boolean hasUnsafeElement = temporalized[0];
		boolean hasSafeBodyElement = false;
		
		s += " :- ";
		boolean first = true;
		for(GdlLiteral subGoal : rule.getBody()){
			if(!first){
				s += ", ";
			}
			first = false;
			
			
			temporalized = new boolean[1];
			s +=  temporalizeLiteral(subGoal, bodyArgument, temporalized);
			
			if(temporalized[0]){
				if(subGoal instanceof GdlNot){
					hasUnsafeElement = true;
				}else{
					hasSafeBodyElement = true;
				}
			}
			
		}
		
		//in case of NEXT we should always add the time_domain predicate, because otherwise Clingo may
		// create an infinite set of propositions.
		if((hasUnsafeElement && !hasSafeBodyElement) || head.getName().equals(GdlPool.NEXT)){
			s += ", ";
			s += "time_domain(T)";
		}
		
		
		/*
		if(head.getName().equals(GdlPool.NEXT)){
			s += ", ";
			s += "time_domain(T)";
		}*/
		
		s += ".";
		
		return s;
	}
	
	String temporalizeFact(GdlSentence fact){
		
		String temporalArgument = null;
		
		if(fact.getName().equals(GdlPool.INIT)){
			temporalArgument = "1";
		}
		
		return temporalizeLiteral(fact, temporalArgument, new boolean[1]) + ".";
	}
	
	
	String temporalizeLiteral(GdlLiteral literal, String temporalArgument, boolean[] temporalized){
		
		
		if(literal instanceof GdlOr){
			GdlOr or = (GdlOr) literal;
			
			//WARNING! For some reason this is causing errors!
			// Instead, we are using the DeORer to remove disjunctions, so this code should never be executed.
			
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean first = true;
			for (int i = 0; i < or.arity(); i++) {
				if(!first){
					sb.append("; ");
				}
				first = false;
				
				
				boolean[] temp = new boolean[1];
				sb.append(temporalizeLiteral(or.get(i), temporalArgument, temp));
				
				//if one of the disjuncts is temporalized, then the disjunction is temporalized.
				if(temp[0]){
					temporalized[0] = true;
				}
			}
			sb.append("}");
			
			
			return sb.toString();
			
		}else if(literal instanceof GdlNot){
			GdlNot not = (GdlNot) literal;
			
			String s = "not ";
			s += temporalizeLiteral(not.getBody(), temporalArgument, temporalized);
			
			return s;
			
		}else if(literal instanceof GdlSentence){
			return temporalizeSentence((GdlSentence)literal, temporalArgument, temporalized);
			
		}else if(literal instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) literal;
			
			/*
			String s = convertToAspString(distinct.getArg1());
			s += " != ";
			s += convertToAspString(distinct.getArg2());
			*/
			
			String s = "not ";
			s += convertToAspString(distinct.getArg1());
			s += " = ";
			s += convertToAspString(distinct.getArg2());
			
			temporalized[0] = false;
			
			return s;
			
		}else{
			throw new RuntimeException(this.getClass().getName() + ".simplify() Error! " + literal.getClass().getName());
		}
	}
	
	
	String temporalizeSentence(GdlSentence sentence, String temporalArgument, boolean[] temporalized){
		
		
		// A predicate does not need a temporal argument if:
		//1. is not among the GDL keywords, and
		//2. does not depend on true or does in the original game description.
		if( ! dependencyGraph.dependsOn(sentence, GdlPool.TRUE) && ! dependencyGraph.dependsOn(sentence, GdlPool.DOES) && !gdlKeyWords.contains(sentence.getName())){
			temporalArgument = null;
		}
		
		
		if(sentence instanceof GdlProposition){
			return temporalizeProposition((GdlProposition)sentence, temporalArgument, temporalized);
		}else if(sentence instanceof GdlRelation){
			return temporalizeRelation((GdlRelation)sentence, temporalArgument, temporalized);
		}else{
			throw new RuntimeException("ASPPuzzleSolver.temporalizeSentence() Error! unhandled class: " + sentence.getClass().getName());
		}
	}
	
	String temporalizeProposition(GdlProposition proposition, String temporalArgument, boolean[] temporalized){
		
		String relationName = proposition.getName().toString();
		
		if(temporalArgument == null){
			temporalized[0] = false;
			return relationName;
		}else{
			if(temporalArgument.equals("1")){
				temporalized[0] = false;
			}else{
				temporalized[0] = true;
			}
			return relationName + "(" + temporalArgument + ")";
		}
		
		
	}
	
	
	String temporalizeRelation(GdlRelation relation, String temporalArgument, boolean[] temporalized){
		
		String string = "";
		
		String relationName = relation.getName().toString();
		if(relationName.equals(GdlPool.INIT.toString()) || relationName.equals(GdlPool.TRUE.toString()) || relationName.equals(GdlPool.NEXT.toString()) ){
			relationName = "holds";
		}
		
		string = relationName + "(";
		
		
		String comma = ", ";
		
		List<GdlTerm> body = relation.getBody();
		for(GdlTerm term : body){
			string += convertToAspString(term);
			string += comma;
		}
		
		if(temporalArgument == null){
			//no extra argument needs to be added, and therefore we can remove the last comma.
			string = string.substring(0, string.length()-comma.length());
			
			temporalized[0] = false;
			
		}else{
			string += temporalArgument;
			
			if(temporalArgument.equals("1")){
				temporalized[0] = false;
			}else{
				temporalized[0] = true;
			}
		}
		
		
		string += ")";
		
		return string;
	}
	
	String convertToAspString(GdlTerm term){
		
		if(term instanceof GdlFunction){
			
			String s = ((GdlFunction)term).getName().toString();
			s += "(";
			boolean first = true;
			for(GdlTerm bodyTerm : ((GdlFunction)term).getBody()){
				if(!first){
					s+= ", ";
				}
				first = false;
				s += convertToAspString(bodyTerm);
			}
			
			s += ")";
			return s;
			
		}else if(term instanceof GdlConstant){
			return term.toString();
		}else if(term instanceof GdlVariable){
			
			String s = term.toString().replace("?", "");
			s = s.toUpperCase();
			
			//we have already reserved T as our temporal variable, so we need to give this variable another name.
			if(s.equals("T")){
				s = "TT";
			}
			
			return s;
			
		}else{
			throw new RuntimeException("ASPPuzzleSolver.temporalizeTerm() Error! unhandled class: " + term.getClass().getSimpleName());
		}
		
	}
	
	
	List<String> getMoveDomainRules(List<Gdl> description, String roleName){
		
		List<String> moveDomainRules = new ArrayList<String>();
		
		for(Gdl gdl : description){
			
			if(gdl instanceof GdlRule){
				GdlRule rule = (GdlRule)gdl;
				
				if(rule.getHead().getName().equals(GdlPool.INPUT)){
					moveDomainRules.add(GDL2ASPConverter.toAspString(rule));
				}
			}
				
		}
			
		moveDomainRules.add("move_domain(M) :- input(" + roleName +", M).");
		
		
		return moveDomainRules;
	}
	
	List<String> getTimeDomainRules(int maxNumActions){
		
		//if we want a solution consisting of 10 actions, then the states need to be numbered 1 till 11.
		// 1 being the initial state, and 11 being the terminal state.
		
		
		List<String> timeDomainRules = new ArrayList<String>(maxNumActions+1);
		for(int i=1; i<=maxNumActions+1; i++){
			timeDomainRules.add("time_domain(" + i + ").");
		}
		//timeDomainRules.add("time_domain(1.." + (maxNumActions+1) + ").");
		
		return timeDomainRules;
	}
	
	
	List<String> getSolverRules(String roleName, int targetGoal){
		
		List<String> solverRules = new ArrayList<String>();
		
		//in each round that the game is not yet terminated the player must choose exactly one move.
		solverRules.add("1 {does(#r, M, T) : move_domain(M) } 1 :- not terminated(T), time_domain(T).");
		//time_domain must be added here, otherwise it's an unsafe rule.
		
		//if the game is in a terminal state then all next rounds will also be terminal.
		solverRules.add("terminated(T) :- terminal(T).");
		solverRules.add("terminated(T+1) :- terminated(T), time_domain(T).");
		//we've added time_domain here to prevent Clingo from generating an infinite set of propositions.
		
		//the player can only make legal actions.
		solverRules.add(":- does(#r,M,T), not legal(#r,M,T).");
		
		//The game must reach a terminal state.
		solverRules.add(":- 0 {terminated(T) : time_domain(T)} 0.");
		
		//in the terminal state, the goal value must be gmax.
		// note that the 'terminal' state is the first 'terminated' state.
		solverRules.add(":- terminated(T), not terminated(T-1), not goal(#r,gmax,T).");
		solverRules.add(":- terminated(1), not goal(#r,gmax,1).");  //this is the special case that the game is directly terminated in the first round.

		
		//Replace #r by role name.
		//and replace gmax by the target goal.
		for (int i = 0; i < solverRules.size(); i++) {
			
			String s = solverRules.get(i);
			
			s = s.replace("#r", roleName);
			s = s.replace("gmax", "" + targetGoal);
			
			solverRules.set(i, s);
			
		}
		

		return solverRules;
	}
	
	
	
	/**
	 * Tries to find a solution.
	 * 
	 * @return Returns true if a solution is found and returns false otherwise.
	 */
	public boolean findSolution(long timeout){
		
		int maxNumActions = 1;
		
		while(System.currentTimeMillis() < timeout - 1000){
			//Note: if there is less then 1 sec to go, then we can't start Clingo because we can't set a deadline smaller then 1 sec.
			// passing a deadline of 0 sec. would cause Clingo to ignore the deadline altogether, which we certainly don't want.
			
			System.out.println("ASPPuzzleSolver.findSolution() time to go: " + (timeout - System.currentTimeMillis()));
			boolean solutionFound = findSolutionLimitedSteps(maxNumActions, timeout);
			if(solutionFound){
				System.out.println("ASPPuzzleSolver.findSolution() finished. time left: " + (timeout - System.currentTimeMillis()));
				return true;
			}
			
			maxNumActions+=5;
		}
		
		System.out.println("ASPPuzzleSolver.findSolution() finished.. time left: " + (timeout - System.currentTimeMillis()));
		
		return false;
	}
	
	public boolean findSolutionLimitedSteps(int maxNumActions, long timeout){
		
		System.out.println("ASPPuzzleSolver.findSolutionLimitedSteps() searching for solution with maximally " + maxNumActions + " actions.");
		
		ArrayList<String> allAspRules = new ArrayList<String>();
		allAspRules.addAll(temporalizedRules);
		allAspRules.addAll(moveDomainRules);
		allAspRules.addAll(solverRules);
		
		List<String> timeDomainRules = getTimeDomainRules(maxNumActions);
		allAspRules.addAll(timeDomainRules);
		
		/*allAspRules.add("#show holds/2.");
		allAspRules.add("#show doesOne/4.");
		//allAspRules.add("#show open/1.");
		allAspRules.add("#show terminated/1.");
		allAspRules.add("#show goal/3.");*/
		allAspRules.add("#show does/3."); //remember that does has 3 arguments, because it is temporalized.
		
		boolean print = false;
		Result result = ASPRunner.findModels(allAspRules, 1, print, timeout);
		
		if(result == null){
			return false;
		}
		
		
		
		if(result.satisfiable){
			
			String solutionString = result.solutions.get(0);
			
			
			
			
			
			System.out.println("ASPPuzzleSolver.findSolutionLimitedSteps() " + solutionString);
			//this.solution = convertStringToSolution(solutionString);
			
			this.solution = SolutionParser.parseMoves(solutionString);
			
			return true;
		}else{
			return false;
		}
		
	}
	
	//GETTERS AND SETTERS
	public boolean hasSolution(){
		return solution != null;
	}
	
	//List<Move> convertStringToSolution(String solutionString){
	//	
	//}
	
	
	/**
	 * Returns null if no solution has been found yet. The calling function should then decide how to pick a move.
	 * e.g. pick a random move.
	 * 
	 * @param numMovesMade
	 * @return
	 */
	public Move getMove(int numMovesMade){
		
		if(solution == null){
			return null;
		}else{
			return solution[numMovesMade];
		}
		
	}
	
	
	public Move[] getSolution(){
		return this.solution;
	}

	
	
	public void cleanUp(){
		if(dependencyGraph != null){
			this.dependencyGraph.cleanUp();
		}
		if(gdlKeyWords != null){
			this.gdlKeyWords.clear();
		}
		if(moveDomainRules != null){
			this.moveDomainRules.clear();
		}
		if(solution != null){
			Arrays.fill(solution, null);
			this.solution = null;
		}
		if(solverRules != null){
			this.solverRules.clear();
		}
		if(temporalizedRules != null){
			this.temporalizedRules.clear();
		}
	}
	
	public void printAllRules(){
		for(String s : this.temporalizedRules){
			System.out.println(s);
		}
		
		/*
		for(String s : this.moveDomainRules){
			System.out.println(s);
		}*/
		System.out.println("moves: " + moveDomainRules.size());
		for(String s : this.solverRules){
			System.out.println(s);
		}
		
		/*
		List<String> timeDomainRules = getTimeDomainRules(50);
		for(String s : timeDomainRules){
			System.out.println(s);
		}*/
	}
	
	
	
	//STATIC METHODS
}
