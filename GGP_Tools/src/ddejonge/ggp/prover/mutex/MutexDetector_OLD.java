package ddejonge.ggp.prover.mutex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;
import org.ggp.base.util.prover.aima.substituter.Substituter;
import org.ggp.base.util.prover.aima.substitution.Substitution;
import org.ggp.base.util.prover.aima.unifier.Unifier;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

import ddejonge.ggp.propnet.PropnetStateMachine;
import ddejonge.ggp.propnet.heuristics.Heuristics;
import ddejonge.ggp.tools.Stopwatch;
import ddejonge.ggp.tools.dataStructures.JointMove;
import ddejonge.ggp.tools.logic.LogicUtils;
import ddejonge.ggp.tools.logic.Mutex;

public class MutexDetector_OLD {

	//public final static char replacementChar = '#';
	
	//static final GdlConstant replacementTerm = GdlPool.getConstant("" + replacementChar);
	

	public static List<GdlSentence> getMutexCandidates(PropnetStateMachine stateMachine, List<GdlSentence> allGroudedBasePropositions, long deadlineHeuristicSearch){
		
		//Create a list of non-ground sentences that represent potential mutexes.
		// e.g. the GdlSentence true(cell(1,1,?y))  represents the mutex {true(cell(1,1,X)) ,  true(cell(1,1,O)) ,  true(cell(1,1,b))}
		ArrayList<GdlSentence> candidates = new ArrayList<GdlSentence>(100);
		
		//Also create a list of non-ground sentences for which we are sure that they are not mutexes.
		ArrayList<GdlSentence> rejected = new ArrayList<GdlSentence>(100);
		
		//Now fill these two lists
		//First try to filter out some of the possible mutexes by randomly generating states and looking if any potential mutex is violated.
		// Note however, that this step is important, because the returned candidates are correct at least for the initial state, and this is
		//  a necessary condition for the prover to work.
		detectHeuristically(stateMachine, deadlineHeuristicSearch, candidates, rejected);
		
		
		// for each candidate, create all generalizations, but remove those that are also a generalization of any rejected candidate.
		// e.g. if we have a candidate true(cell(1,1,?y))  then    true(cell(?X,1,?y)) is a generalization.
		//   the interpretation of this generalized candidate is that for EACH value x of ?X  true(cell(x,1,?Y))  is a mutex.
		//   i.e. true(cell(?X,1,?Y))  is a function from the domain of ?X  to the domain of ?Y
		SubsumptionGraph subsGraph = getGenearlizedCandidates(candidates, rejected);
		
		//Create a new list of candidates, which also includes all the generalized ones.
		ArrayList<GdlSentence> newCandidates = new ArrayList<GdlSentence>();
		for(SubsumptionVertex vertex : subsGraph.getVertices()){
			if(vertex.getOutgoingEdges().size() == 0){
				newCandidates.add(vertex.getLabel());
			}
		}
		
		return newCandidates;
	}
	
	public static List<Mutex> proveCandidates(MutexProver prover, List<GdlSentence> candidates, List<GdlSentence> allGroudedBasePropositions){
		
		//Use clingo to see if any of the candidates really represents a mutex.
		List<GdlSentence> provedMutexCandidates = detectMutexesFormally(prover, candidates);
		
		//Convert the proved candidates into Mutex objects.
		List<Mutex> mutexes = new ArrayList<>(provedMutexCandidates.size());
		for (GdlSentence candidate : provedMutexCandidates) {
			
			List<GdlVariable> inputVars = new ArrayList<>();
			List<GdlVariable> outputVars = new ArrayList<>();
			
			fillVariableLists(candidate,  inputVars, outputVars);
			
			//first only instantiate the input values.
			Set<GdlSentence> partiallyInstantiated = LogicUtils.getPartialInstantiations(candidate, inputVars, allGroudedBasePropositions);
			
			//for each partially instantiated atom, create a mutex object.
			for(GdlSentence atomWithOnlyOutputVars : partiallyInstantiated){
				
				//get all instantiations.
				Set<GdlSentence> values = LogicUtils.getInstantiations(atomWithOnlyOutputVars, allGroudedBasePropositions);
				mutexes.add(new Mutex(atomWithOnlyOutputVars, values, false));
			}
	

			
		}
		
		//Return the mutexes
		return mutexes;
		
	}
	
	/**
	 * Fills a list of candidate Mutexes by randomly exploring the state space. Also fills a list of rejected candidates, i.e. candidate mutexes that have been proven to be not a mutex.
	 * @param stateMachine
	 * @param deadline
	 * @param candidates
	 * @param rejected
	 */
	public static void detectHeuristically(PropnetStateMachine stateMachine, long deadline, ArrayList<GdlSentence> candidates, ArrayList<GdlSentence> rejected){
		
		List<Role> roles = stateMachine.getRoles();
		List<GdlTerm> roleTerms;
		roleTerms = new ArrayList<GdlTerm>(roles.size());
		for(Role role : roles){
			roleTerms.add(role.getName());
		}
		
		MachineState initialState = stateMachine.getInitialState();
		
		//generate a random state. analyze all its base propositions.
		// if we find any candidate for which two or more base propositions are satisfied,
		// then we can discard it.
		long lastPrinted = 0;
		int numRollouts=0;
		while(true){
			
			long time = System.currentTimeMillis();
			if(time > deadline){
				break;
			}
			
			numRollouts++;
			
			/*
			if(time > lastPrinted + 1000){
				System.out.println(numRollouts + " rollouts. time to go: " + (deadline - time)/1000);
				lastPrinted = time;
			}*/
			
			
			// *1. Perform a rollout
			
			//  1a. Reset the stateMachine to the initial state 
			stateMachine.setState(initialState);
			
			//  1b. Try to find and reject candidate mutexes from the initial state.
			analyzeState(initialState, roleTerms, candidates, rejected);
			
			// 1c. Generate new states until we find a terminal state.
	    	while( ! stateMachine.isTerminal(null)) {
	        	
	    		//randomly generate new state.
	    		JointMove jointMove = stateMachine.getRandomJointMove((Heuristics)null);
	    		stateMachine.setActions(jointMove);
	    		stateMachine.setNextStateAsCurrentState();
	    		MachineState state = stateMachine.getCurrentState();
	    		
	    		//Try to find and reject candidate mutexes from the current state.
	    		analyzeState(state, roleTerms, candidates, rejected);
	        }
			
		}
		
		/*
		int i=0;
		Collections.sort(this.candidates);
		for(String candidateVarID : this.candidates){
			i++;
			System.out.println(i + ". " + candidateVarID);
		}
		System.out.println("rejected: " + rejected.size());
		for(String candidateVarID : this.rejected){
			System.out.println("REJECTED: " + candidateVarID);
		}*/
		
		return;
	}
	
	/**
	 * For the given state, tries to find new candidate mutexes.
	 * Next, it tests whether any of these new candidates or any of the given candidates are 
	 * violated by the given state. 
	 * 
	 * @param state
	 * @param roleTerms
	 * @param candidates
	 * @param rejected
	 */
	private static void analyzeState(MachineState state, List<GdlTerm> roleTerms, ArrayList<GdlSentence> candidates, ArrayList<GdlSentence> rejected){
		
		//This map stores for every candidate mutex, which value it takes in the current state.
		//If a candidate mutex takes two different values in the same state, then it is not a mutex.
		HashMap<String, GdlSentence> candidate2instance = new HashMap<String, GdlSentence>();
		
		for(GdlSentence baseProp : state.getContents()){
			
			//Generate potential mutexes of which the current baseProposition could form part. Each candidate is a non-ground atom with exactly one variable.
			List<GdlSentence> newCandidates = getCompatibleCandidates(baseProp, roleTerms);
			
			for(GdlSentence newCandidate : newCandidates){
				
				//if the current new candidate hadn't been discovered yet, (and hasn't been rejected), then add it to the list of candidates.
				if(!candidates.contains(newCandidate) && !rejected.contains(newCandidate)){
					candidates.add(newCandidate);
				}
				
				
				//now check if we already have found a base proposition for the current new candidate. 
				GdlSentence inst = candidate2instance.get(newCandidate.toString());
				
				if(inst == null){
					
					//if not, then store the current base proposition as an instantiation of the candidate.
					candidate2instance.put(newCandidate.toString(), baseProp);
					
				}else if(inst.equals(baseProp)){
					
					// nothing needs to happen. (I think this can never happen)
					
				}else{
					
					//We have discovered two different ground base propositions in the current state that satisfy the same candidate mutex.
					// Therefore, the candidate can be rejected.
					//Remove the current candidate mutex from the candidates list.
					candidates.remove(newCandidate);
					if(!rejected.contains(newCandidate)){
						rejected.add(newCandidate);
					}
				}
				
				
			}
			
		}
		
		
	}
	
	/**
	 * Returns a list of non-ground atoms which represent the potential mutexes of which the given ground atom may form part.<br/>
	 * e.g. true(cell(1,1,?x)) represents the mutex {true(cell,1,1,X) , true(cell,1,1,O), true(cell,1,1,b)}<br/>
	 * <br/>
	 * Currently, the returned list will only contain atoms with exactly 1 variable.
	 * 
	 * 
	 * @param groundedBaseProposition
	 * @param roleTerms
	 * @return
	 */
	private static List<GdlSentence> getCompatibleCandidates(GdlSentence groundedBaseProposition, List<GdlTerm> roleTerms){
		
		ArrayList<GdlSentence> candidateVarIDs = new ArrayList<GdlSentence>();
		
		//get the body
		GdlTerm body = groundedBaseProposition.getBody().get(0);  //e.g. cell(1,1,b)
		
		if(body instanceof GdlFunction){
			GdlFunction function = (GdlFunction)body;
			
			List<GdlTerm> innerTerms = function.getBody(); //e.g. {1, 1, b}
			
			//check which of the terms represent roles (I think we need this code in case we allow more than one var.)
			/*int roleMask = 0;
			for(int i=0; i<innerTerms.size(); i++){
				if(roleTerms.contains(innerTerms.get(i))){
					roleMask = roleMask | (1<<i);
				}
			}*/
			
			//For each inner term, create a new non-ground term by replacing that inner term with a variable.
			for(int i=0; i<innerTerms.size(); i++){
				
				//I think this code was meant for the feature detector.
				/*
				if(roleTerms.contains(innerTerms.get(i))){
					continue;
				}*/
				
				List<GdlTerm> newInnerTerms = new ArrayList<GdlTerm>(innerTerms);
				
				newInnerTerms.set(i, GdlPool.getVariable(Mutex.outputVarName + i)); //e.g. {1, 1, ?Y1}
				GdlFunction newFunction = GdlPool.getFunction(function.getName(), newInnerTerms); 
				GdlSentence newSentence = GdlPool.getRelation(groundedBaseProposition.getName(), new GdlTerm[]{newFunction});
				
				candidateVarIDs.add(newSentence);
			}
			
			
			
		}else{
			
			/// ... ???
		}
		
		return candidateVarIDs;
		
	}
	
	
	public static SubsumptionGraph getGenearlizedCandidates(ArrayList<GdlSentence> candidates, ArrayList<GdlSentence> rejected){
		
		SubsumptionGraph subsumptionGraph = new SubsumptionGraph();
		
		//create an open list.
		List<GdlSentence> toGeneralize = new ArrayList<GdlSentence>(candidates);
		
		while(toGeneralize.size() > 0) {
			
			//remove the next candidate to generalize from the open list.
			GdlSentence candidate = toGeneralize.remove(toGeneralize.size()-1);
			
			//make sure a vertex representing this candidate is in the graph.
			subsumptionGraph.add(candidate);
			
			//create all direct generalizations (sentences for which exactly one constant is replaced by a variable):
			List<GdlSentence> generalizations = generalize(candidate);
			
			//Add them to the open list, so that they can be generalized again (i.e. replace another constant with a variable).
			toGeneralize.addAll(generalizations);
			
			//Also add them to the graph.
			for(GdlSentence gen : generalizations){
				subsumptionGraph.add(gen);
				subsumptionGraph.setSubsumes(candidate, gen);
			}
			
		}
		
		
		//Now remove all vertices for which there exists a path from a rejected vertex.
		// e.g. if we have found a counter example for the candidate true(cell(?Y, 1, b))  then all generalizations, such as
		//   true(cell(?Y, ?X1, b)) are also violated.
		List<GdlSentence> toRemove = new ArrayList<GdlSentence>(rejected);
		while(toRemove.size() > 0) {
			GdlSentence candidateToRemove = toRemove.remove(toRemove.size()-1);
			
			List<GdlSentence> generalizations = generalize(candidateToRemove);
			toRemove.addAll(generalizations);
			
			subsumptionGraph.remove(candidateToRemove);
			
		}
		
		return subsumptionGraph;
		
	}
	
	public static List<GdlTerm> getInnerTerms(GdlSentence sentence){
		return ((GdlFunction) sentence.getBody().get(0)).getBody();
	}
	
	/**
	 * Returns a list that contains GdlSentences for which exactly one constant has been replaced by an inputVar. 
	 * For example, if the given sentence is true(cell(1, ?Y, b)) then this method returns the list
	 *  { true(cell(?X0, ?Y, b)) , true(cell(1, ?Y, ?X2)) }
	 * @param sentence
	 * @return
	 */
	public static List<GdlSentence> generalize(GdlSentence sentence){
		
		List<GdlSentence> generalizedSentences = new ArrayList<GdlSentence>();
		
		
		List<GdlTerm> innerTerms = getInnerTerms(sentence); //e.g. {1, ?Y, b}
		
		
		for (int j = 0; j < innerTerms.size(); j++) {
			
			GdlTerm term = innerTerms.get(j);
					
			if(term instanceof GdlVariable){
				continue;
			}
			if(term instanceof GdlFunction){
				throw new RuntimeException("MutexDetector.generalize() Error! inner term is function!");
			}
			
			List<GdlTerm> generalizedCandidateInnerTerms = new ArrayList<GdlTerm>(innerTerms); //e.g. {?X0, ?Y, b}
			generalizedCandidateInnerTerms.set(j, GdlPool.getVariable(Mutex.inputVarName + j));
			
			GdlTerm generalizedCandidateBody = GdlPool.getFunction(((GdlFunction) sentence.getBody().get(0)).getName(), generalizedCandidateInnerTerms);
			//e.g. cell(?X0, ?Y, b)
			
			GdlSentence generalizedCandidate = GdlPool.getRelation(sentence.getName(), new GdlTerm[]{generalizedCandidateBody});
			//e.g. true(cell(?X0, ?Y, b))
			
			generalizedSentences.add(generalizedCandidate);
		}
		
		return generalizedSentences;
		
		/*
		int outputVarMask = 0;
		for(int i=0; i<innerTerms.size(); i++){
			if(innerTerms.get(i) instanceof GdlVariable){
				outputVarMask = outputVarMask | (1<<i);
			}
		}
		
		for(int subsetIndex = 1; subsetIndex< (1 << innerTerms.size()); subsetIndex++){
			
			//skip subsets that contain an output variable.
			if( (outputVarMask & subsetIndex) != 0){
				continue;
			}
			
			List<GdlTerm> generalizedCandidateInnerTerms = new ArrayList<GdlTerm>(); //e.g. {?X0, ?Y, b}
			for(int i=0; i<innerTerms.size(); i++){
				if( (subsetIndex & (1<<i)) == 0){
					generalizedCandidateInnerTerms.add(innerTerms.get(i));
				}else{
					generalizedCandidateInnerTerms.add(GdlPool.getVariable(inputVarName + i));
				}
			}
			
			GdlTerm generalizedCandidateBody = GdlPool.getFunction(((GdlFunction) sentence.getBody().get(0)).getName(), generalizedCandidateInnerTerms);
			//e.g. cell(?X0, ?Y, b)
			
			GdlSentence generalizedCandidate = GdlPool.getRelation(sentence.getName(), new GdlTerm[]{generalizedCandidateBody});
			//e.g. true(cell(?X0, ?Y, b))

			generalizedSentences.add(generalizedCandidate);
		}
		
		return generalizedSentences;
		*/
	}
	
	public static ArrayList<GdlSentence> _getGenearlizedCandidates(ArrayList<GdlSentence> candidates, ArrayList<GdlSentence> rejected){
		
		
		ArrayList<GdlSentence> generalizedCandidates = new ArrayList<GdlSentence>();
		for(GdlSentence candidate : candidates){ 
			//the candidates in this list only have output vars.
			// for each subset of its constants, replace it by input vars.
			
			//candidate should be a TRUE sententence. e.g. true(cell(1,?Y,b))
			
			
			//get the body
			GdlTerm body = candidate.getBody().get(0);  //e.g. cell(1,?Y,b)
			
			if(body instanceof GdlFunction){
				GdlFunction function = (GdlFunction)body;
				
				List<GdlTerm> innerTerms = function.getBody(); //e.g. {1, ?Y, b}
				
				int outputVarMask = 0;
				for(int i=0; i<innerTerms.size(); i++){
					if(innerTerms.get(i) instanceof GdlVariable){
						outputVarMask = outputVarMask | (1<<i);
					}
				}
				
				for(int subsetIndex = 1; subsetIndex< (1 << innerTerms.size()); subsetIndex++){
					
					//skip subsets that contain an output variable.
					if( (outputVarMask & subsetIndex) != 0){
						continue;
					}
					
					List<GdlTerm> generalizedCandidateInnerTerms = new ArrayList<GdlTerm>(); //e.g. {?X0, ?Y, b}
					for(int i=0; i<innerTerms.size(); i++){
						if( (subsetIndex & (1<<i)) == 0){
							generalizedCandidateInnerTerms.add(innerTerms.get(i));
						}else{
							generalizedCandidateInnerTerms.add(GdlPool.getVariable(Mutex.inputVarName + i));
						}
					}
					
					GdlTerm generalizedCandidateBody = GdlPool.getFunction(function.getName(), generalizedCandidateInnerTerms);
					//e.g. cell(?X0, ?Y, b)
					
					GdlSentence generalizedCandidate = GdlPool.getRelation(candidate.getName(), new GdlTerm[]{generalizedCandidateBody});
					//e.g. true(cell(?X0, ?Y, b))
					
					
					
					
					boolean generalizesRejectedCandidate = false;
					for(GdlSentence rejectedCandidate : rejected){
						
						
						if( ! rejectedCandidate.getName().equals(generalizedCandidate.getName())){
							continue;
						}
						
						List<GdlTerm> rejectedInnerTerms = ((GdlFunction) rejectedCandidate.getBody().get(0)).getBody();
						
						if(generalizedCandidateInnerTerms.size() != rejectedInnerTerms.size()){
							continue;
						}
						
						boolean generalizes = true;
						for (int i = 0; i < generalizedCandidateInnerTerms.size(); i++) {
							
							GdlTerm genTerm = generalizedCandidateInnerTerms.get(i);
							GdlTerm rejTerm = rejectedInnerTerms.get(i);
							
							if(rejTerm instanceof GdlConstant && genTerm instanceof GdlConstant){
								
								if( ! rejTerm.equals(genTerm)){
									generalizes = false;
									break;
								}
								
							}
							
							if(rejTerm instanceof GdlVariable && genTerm instanceof GdlConstant){
								generalizes = false;
								break;
							}
							
							if(rejTerm instanceof GdlConstant && genTerm instanceof GdlVariable){
								//nothing happens
							}
							
							if(rejTerm instanceof GdlVariable && genTerm instanceof GdlVariable){
								
								if( ! rejTerm.equals(genTerm)){
									generalizes = false;
									break;
								}
								
							}
						}
						
						if( generalizes){
							generalizesRejectedCandidate = true;
							break;
						}
						
					}
						
					if(!generalizesRejectedCandidate && ! generalizedCandidates.contains(generalizedCandidate)){
						generalizedCandidates.add(generalizedCandidate);
					}
				}
				
				
				
			}else{
				// ... ???
			}
			
			
		}
		
		
		return generalizedCandidates;
		
	}
	
	
	public static List<GdlSentence> detectMutexesFormally(MutexProver prover, List<GdlSentence> candidates){
		
		//list to store the Mutex objects when a candidate is indeed proved to be a mutex.
		List<GdlSentence> proved = new ArrayList<GdlSentence>();
		
		//List to store the candidates for which we can't immediately prove it's a mutex.
		ArrayList<GdlSentence> rejectedCandidates = new ArrayList<GdlSentence>(candidates.size());
		
		while(candidates.size() > 0){
			
			GdlSentence candidate = candidates.remove(candidates.size()-1);
			boolean isMutex = prover.proveMutex(proved, candidate);
			
			if(isMutex){
				
				//if we have proved it is a mutex, then add it to the list of mutexes
				proved.add(candidate);
				
				//all rejected candidates may now be reconsidered, since the new mutex may influence the proof.
				candidates.addAll(0, rejectedCandidates);
				rejectedCandidates.clear();
				
				
			}else{
				
				rejectedCandidates.add(candidate);
				
			}
			
			//TODO: also check for mutexes for which exactly one of the propositions must hold.
		}
		
		return proved;
	}

	

	
	

	
	

	
	
	private static GdlVariable getOutputVar(GdlSentence sentence){
		
		List<GdlVariable> outputVars = new ArrayList<>(1);
		fillVariableLists(sentence,  new ArrayList<>(), outputVars);
		
		if(outputVars.size() > 1){
			throw new RuntimeException("MutexDetector.getOutputVar() Error! expected only one output var. " + sentence + " outputVars: " + outputVars);
		}
		if(outputVars.isEmpty()){
			throw new RuntimeException("MutexDetector.getOutputVar() Error! no output var detected. " + sentence);
		}
		
		GdlVariable outputVar = outputVars.get(0);
		
		return outputVar;
	}
	
	/**
	 * Extracts all variables from the given sentence and puts them in either of the two given lists.
	 * @param sentence
	 * @param inputVars
	 * @param outputVars
	 */
	public static void fillVariableLists(GdlSentence sentence, List<GdlVariable> inputVars, List<GdlVariable> outputVars){
		
		 for(GdlTerm term : sentence.getBody()){
			 fillVariableLists(term, inputVars, outputVars);
		 }
			
	 }
	
	/**
	 * If the given term is a variable then this method will put it in one of the two given lists, based on its name.
	 * @param term
	 * @param inputVariables
	 * @param outputVariables
	 */
	private static void fillVariableLists(GdlTerm term, List<GdlVariable> inputVariables, List<GdlVariable> outputVariables){
		 
		if(term instanceof GdlVariable){
			
			if(term.toString().startsWith(Mutex.inputVarName)){
				if(!inputVariables.contains(term)){
					inputVariables.add((GdlVariable)term);
				}
			}else if(term.toString().startsWith(Mutex.outputVarName)){
				if(!outputVariables.contains(term)){
					outputVariables.add((GdlVariable)term);
				}
			}else{
				throw new RuntimeException("Prover.getOutputVariables() Error! All variables must either start with ?X or ?Y. " + term);
			}
			
		 }else if(term instanceof GdlConstant){
			
			 //nothing needs to happen here.
		 
		 }else if(term instanceof GdlFunction){
			 GdlFunction function = (GdlFunction)term;
			 
			 for(GdlTerm innerTerm : function.getBody()){
				 fillVariableLists(innerTerm, inputVariables, outputVariables);
			 }
			 
		 }else{
			 throw new RuntimeException("Prover.getOutputVariables() Error! unhandled class: " + term.getClass().getName());
		 }
		 
		 
	}
}
