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

public class MutexDetector {

	//public final static char replacementChar = '#';
	
	//static final GdlConstant replacementTerm = GdlPool.getConstant("" + replacementChar);
	

	public static List<MutexCandidate> getMutexCandidates(PropnetStateMachine stateMachine, Set<GdlSentence> allGroudedBasePropositions, boolean allowGeneralizedMutexes, boolean applyRandomSearch, long deadlineHeuristicSearch){
		
		//Create a list of non-ground sentences that represent potential mutexes.
		// e.g. the GdlSentence true(cell(1,1,?y))  represents the mutex {true(cell(1,1,X)) ,  true(cell(1,1,O)) ,  true(cell(1,1,b))}
		Set<MutexCandidate> candidates = generateAllCandidates(allGroudedBasePropositions);
		
		
		List<Role> roles = stateMachine.getRoles();
		List<GdlTerm> roleTerms;
		roleTerms = new ArrayList<GdlTerm>(roles.size());
		for(Role role : roles){
			roleTerms.add(role.getName());
		}
		
		if(applyRandomSearch){
			
			// Try to filter out some of the possible mutexes by randomly generating states and looking if any potential mutex is violated.
			// Note however, that this step is important, because the returned candidates are correct at least for the initial state, and this is
			//  a necessary condition for the prover to work.
			rejectHeuristically(stateMachine, roleTerms, deadlineHeuristicSearch, candidates);
			
		}else{
			
			//Only analyze the initial state.
			MachineState initialState = stateMachine.getInitialState();
			analyzeState(initialState, roleTerms, candidates);
		}
		

		
		ArrayList<MutexCandidate> newCandidates = new ArrayList<MutexCandidate>();
		
		if(!allowGeneralizedMutexes){
			
			
			for (MutexCandidate candidate : candidates) {
				if(candidate.isMutex == null || candidate.isMutex){ //skip rejected candidates.
					newCandidates.add(candidate);
				}
			}		
			
			return newCandidates;
		}
		
		
		// for each candidate, create all generalizations, but remove those that are also a generalization of any rejected candidate.
		// e.g. if we have a candidate true(cell(1,1,?y))  then    true(cell(?X,1,?y)) is a generalization.
		//   the interpretation of this generalized candidate is that for EACH value x of ?X  true(cell(x,1,?Y))  is a mutex.
		//   i.e. true(cell(?X,1,?Y))  is a function from the domain of ?X  to the domain of ?Y
		SubsumptionGraph subsGraph = getGenearlizedCandidates(candidates);
		
		//Create a new list of candidates, that only contains the most general ones.
		
		for(SubsumptionVertex vertex : subsGraph.getVertices()){
			if(vertex.getOutgoingEdges().size() == 0){
				newCandidates.add(vertex.getLabel());
			}
		}
		
		return newCandidates;
	}
	
	
	
	public static List<Mutex> convertToMutexObjects(List<MutexCandidate> candidates, List<GdlSentence> allGroudedBasePropositions){
		
		//Convert the proved candidates into Mutex objects.
		List<Mutex> mutexes = new ArrayList<>(candidates.size());
		for (MutexCandidate candidate : candidates) {
			
			if(candidate.isMutex == null || !candidate.isMutex){
				continue;
			}
			
			//fillVariableLists(candidate.getRepresentant(),  candidate.getInputVars(), candidate.getOutputVars());
			
			//first only instantiate the input values.
			Set<GdlSentence> partiallyInstantiated = LogicUtils.getPartialInstantiations(candidate.getRepresentant(), candidate.getInputVars(), allGroudedBasePropositions);
			
			//for each partially instantiated atom, create a mutex object.
			for(GdlSentence atomWithOnlyOutputVars : partiallyInstantiated){
				
				//get all instantiations.
				Set<GdlSentence> values = LogicUtils.getInstantiations(atomWithOnlyOutputVars, allGroudedBasePropositions);
				mutexes.add(new Mutex(atomWithOnlyOutputVars, values, candidate.isStrongMutex));
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
	public static void rejectHeuristically(PropnetStateMachine stateMachine, List<GdlTerm> roleTerms, long deadline, Set<MutexCandidate> candidates){
		
		
		MachineState initialState = stateMachine.getInitialState();
		
		int numRolloutsWithNoNewRejections = 0;
		
		//generate a random state. analyze all its base propositions.
		// if we find any candidate for which two or more base propositions are satisfied,
		// then we can discard it.
		while(true){
			
			long time = System.currentTimeMillis();
			if(time > deadline){
				break;
			}
			
			// *1. Perform a rollout
			
			//  1a. Reset the stateMachine to the initial state 
			stateMachine.setState(initialState);
			
			//  1b. Try to find and reject candidate mutexes from the initial state.
			int numNewRejections = analyzeState(initialState, roleTerms, candidates);
			
			// 1c. Generate new states until we find a terminal state.
	    	while( ! stateMachine.isTerminal(null)) {
	        	
	    		//randomly generate new state.
	    		JointMove jointMove = stateMachine.getRandomJointMove((Heuristics)null);
	    		stateMachine.setActions(jointMove);
	    		stateMachine.setNextStateAsCurrentState();
	    		MachineState state = stateMachine.getState();
	    		
	    		//Try to find and reject candidate mutexes from the current state.
	    		numNewRejections += analyzeState(state, roleTerms, candidates);
	        }
			
	    	
			if(numNewRejections == 0){
				numRolloutsWithNoNewRejections++;
			}else{
				numRolloutsWithNoNewRejections = 0;
			}
			
			//If we have generated 4 consecutive rollouts without any new rejected candidates, then return.
			if(numRolloutsWithNoNewRejections == 4){
				return;
			}
		}
		
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
	 * @return The number of candidates that have been disproved by this function call.
	 */
	private static int analyzeState(MachineState state, List<GdlTerm> roleTerms, Set<MutexCandidate> candidates){
		
		///NOTE: the given candidates do not have input vars.
		
		//Count for how many candidates the status is changed. This number can be used to determine whether it is useful to continue generating new states to disprove more candidates.
		int numNewRejections = 0;
		
		for(MutexCandidate candidate : candidates){
			
			//Get the instantiations of this candidate.
			Set<GdlSentence> intersection = getIntersection(candidate.getValues(), state.getContents());
			
			if(intersection.size() == 0){
				
				if(candidate.isStrongMutex == null){
					numNewRejections++;
				}
				
				candidate.isStrongMutex = false;
				
			}else if(intersection.size() > 1){
				
				if(candidate.isMutex == null || candidate.isStrongMutex == null){
					numNewRejections++;
				}
				
				candidate.isMutex = false;
				candidate.isStrongMutex = false;
			}
			
		}
		
		return numNewRejections;
	}
	
	
	static <T> Set<T> getIntersection(Set<T> set1, Set<T> set2){
		Set<T> intersection = new HashSet<T>(set1); // use the copy constructor
		intersection.retainAll(set2);
		return intersection;
	}
	
	
	private static Set<MutexCandidate> generateAllCandidates(Set<GdlSentence> allGroundedBasePropositions){
		
		Set<MutexCandidate> allCandidates = new HashSet<>();
		for(GdlSentence groundedBaseProposition : allGroundedBasePropositions){
			allCandidates.addAll(getCompatibleCandidates(groundedBaseProposition, allGroundedBasePropositions));
		}
		
		return allCandidates;
	}
	
	
	/**
	 * Returns a set of potential mutexes of which the given ground atom may form part. <br/>
	 * e.g. given the proposition true(cell(1,1,O))
	 * it will return the mutex candidates corresponding to the non-ground atoms
	 * true(cell(?Y,1,O))  true(cell(1,?Y,O))   and  true(cell(1,1,?Y))
	 * 
	 * Currently, the returned set will only contain atoms with exactly 1 output variable.
	 * 
	 * @param groundedBaseProposition
	 * @return
	 */
	private static Set<MutexCandidate> getCompatibleCandidates(GdlSentence groundedBaseProposition, Collection<GdlSentence> allGroundedBasePropositions){
		
		Set<MutexCandidate> candidates = new HashSet<MutexCandidate>();
		
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
				
				candidates.add(new MutexCandidate(newSentence, allGroundedBasePropositions));
			}
			
			
			
		}else{
			
			/// ... ???
		}
		
		return candidates;
		
	}
	

	
	
	
	
	public static SubsumptionGraph getGenearlizedCandidates(Set<MutexCandidate> candidates){
		
		SubsumptionGraph subsumptionGraph = new SubsumptionGraph();
		
		//create an open list.
		List<MutexCandidate> toGeneralize = new ArrayList<MutexCandidate>(candidates);
		
		while(toGeneralize.size() > 0) {
			
			//remove the next candidate to generalize from the open list.
			MutexCandidate candidate = toGeneralize.remove(toGeneralize.size()-1);
			
			/*
			//some of the candidates have already been rejected empirically. We can skip those.
			if(candidate.isMutex != null && !candidate.isMutex){
				continue;
			}*/
			
			//make sure a vertex representing this candidate is in the graph.
			subsumptionGraph.add(candidate);
			
			//create all direct generalizations (sentences for which exactly one constant is replaced by a variable):
			List<MutexCandidate> generalizations = generalize(candidate);
			
			//Add them to the open list, so that they can be generalized again (i.e. replace another constant with a variable).
			toGeneralize.addAll(generalizations);
			
			//Also add them to the graph.
			for(MutexCandidate gen : generalizations){
				subsumptionGraph.add(gen);
				subsumptionGraph.setSubsumes(candidate, gen);
			}
			
		}
		
		
		//NOTE: we first need to add ALL the candidates to the graph, including those that have already been rejected, and
		// then remove the rejected ones, as well as their generalizations. The reason is that an atom C may be a generalization
		// of two different atoms A and B. If we first discover that A is rejected then we do not add C, then we discover B, but
		// we do not reject B, and then add C because it is a generalization.
		
	
		
		//Now, for every candidate that has already empirically been proved not be a mutex add it to a list of rejected candidates, and do the same for all its generalizations.
		// e.g. if we have found a counter example for the candidate true(cell(?Y, 1, b))  then all generalizations, such as
		//   true(cell(?Y, ?X1, b)) are also violated.
		List<MutexCandidate> rejected = new ArrayList<>();
		for(MutexCandidate candidate : candidates){
			if(candidate.isMutex != null && !candidate.isMutex){
				rejectCandidate(candidate, subsumptionGraph, rejected);
			}
		}
		
		//Now remove all vertices corresponding to rejected candidates.
		for(MutexCandidate rejectedCandidate : rejected){
			subsumptionGraph.remove(rejectedCandidate);
		}
		
		//Finally, for all candidates that haven't been rejected, but for which we know they are not strong mutexes,
		// make sure that all their generalizations are also set as not-strong.
		for(MutexCandidate candidate : candidates){
			
			//skip those candidates that have already been rejected completely.
			if(candidate.isMutex != null && !candidate.isMutex){
				continue;
			}
					
					
			if(candidate.isStrongMutex != null && !candidate.isStrongMutex){
				setNotStrong(candidate, subsumptionGraph);
			}
		}
		
		
		return subsumptionGraph;
		
	}
	
	/**
	 * Adds the given candidate to the rejected list, and then recursively does the same for all its generalizations.
	 * @param candidate
	 * @param rejected
	 */
	public static void rejectCandidate(MutexCandidate candidate, SubsumptionGraph subsumptionGraph, List<MutexCandidate> rejected){
		
		candidate.isMutex = false;
		candidate.isStrongMutex = false;
		rejected.add(candidate);
		
		SubsumptionVertex currentVertex = subsumptionGraph.getVertex(candidate);
		for(SubsumptionEdge edge : currentVertex.getOutgoingEdges()){
			MutexCandidate child = edge.getTo().getLabel(); 
			
			rejectCandidate(child, subsumptionGraph, rejected);
		}
	}
	
	/**
	 * Sets the isStrongMutex field of the given candidate to false and recursively does the same for all its generalizations.
	 * @param candidate
	 * @param subsumptionGraph
	 */
	public static void setNotStrong(MutexCandidate candidate, SubsumptionGraph subsumptionGraph){
		candidate.isStrongMutex = false;
		
		SubsumptionVertex currentVertex = subsumptionGraph.getVertex(candidate);
		for(SubsumptionEdge edge : currentVertex.getOutgoingEdges()){
			MutexCandidate child = edge.getTo().getLabel(); 
			
			setNotStrong(child, subsumptionGraph);
		}
	}
	
	
	
	
	public static List<GdlTerm> getInnerTerms(GdlSentence sentence){
		return ((GdlFunction) sentence.getBody().get(0)).getBody();
	}
	
	/**
	 * Returns a list that contains MutexCandidates that are equal to the given candidate, except that exactly one constant 
	 * has been replaced by an inputVar. 
	 * For example, if the atom of the given candidate is true(cell(1, ?Y, b)) then this method returns the list
	 *  { true(cell(?X0, ?Y, b)) , true(cell(1, ?Y, ?X2)) }
	 * @param sentence
	 * @return
	 */
	public static List<MutexCandidate> generalize(MutexCandidate candidate){
		
		List<MutexCandidate> generalizedCandidates = new ArrayList<MutexCandidate>();
		
		GdlSentence sentence = candidate.getRepresentant();
		
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
			
			GdlSentence generalizedCandidateSentence = GdlPool.getRelation(sentence.getName(), new GdlTerm[]{generalizedCandidateBody});
			//e.g. true(cell(?X0, ?Y, b))
			
			MutexCandidate generalizedCandidate = new MutexCandidate(generalizedCandidateSentence);
			
			/*
			//If we already know the current candidate is not a strong mutex, then its generalizations certainly aren't either.
			if(candidate.isStrongMutex != null && !candidate.isStrongMutex){
				generalizedCandidate.isStrongMutex = false;
			}*/
			
			generalizedCandidates.add(generalizedCandidate);
		}
		
		return generalizedCandidates;
		
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
	
	
	public static List<MutexCandidate> detectMutexesFormally(MutexProver prover, List<MutexCandidate> candidates){
		
		//list to store the Mutex objects for which we know exactly whether it is a mutex or not and whether it is a strong mutex or not.
		List<MutexCandidate> completelyDetermined = new ArrayList<MutexCandidate>();
		
		List<MutexCandidate> temporarilyDiscarded = new ArrayList<>(candidates.size());
		
		
		// Reorder the list, such that any candidates for which the GdlRelation has only one argument come first.
		// This is, because many games have a 'control' relation and many mutexes depend on this.
		List<MutexCandidate> potentialControls = new ArrayList<>();
		for(MutexCandidate candidate : candidates){
			GdlSentence representant = candidate.getRepresentant(); //e.g. true(control(xplayer))
			
			GdlTerm term = representant.getBody().get(0); //e.g. control(xplayer)
			if(term instanceof GdlFunction){
				int bodySize = ((GdlFunction) term).getBody().size(); //e.g. {xplayer}
				
				if(bodySize == 1){
					potentialControls.add(candidate);
				}
			}
		}
		//Move the potential controls to the end of the list (because those are tested first).
		List<MutexCandidate> openList = new ArrayList<>(candidates);
		openList.removeAll(potentialControls);
		openList.addAll(potentialControls);
		
		while(openList.size() > 0){
			
			//get the next candidate from the list.
			MutexCandidate candidate = openList.remove(openList.size()-1);
			
			//If we have already proved that the current candidate is a weak mutex, then try to prove that it is a strong mutex.
			boolean proveStrongly = false;
			if(candidate.isMutex != null && candidate.isMutex == true){
				proveStrongly = true;
			}
			
			if(candidate.isMutex != null && candidate.isStrongMutex != null){
				throw new RuntimeException("MutexDetector2.detectMutexesFormally() Error! the current mutexCandidate is already completely determined!");
			}
			
			boolean result = prover.proveMutex(candidates, candidate, proveStrongly);
			
			if(candidate.isMutex != null && candidate.isStrongMutex != null){
				completelyDetermined.add(candidate);
			}else{
				temporarilyDiscarded.add(candidate);
			}
			
			if(result){
				
				//all rejected candidates may now be reconsidered, since the new result may influence the proof.
				openList.addAll(0, temporarilyDiscarded);
				temporarilyDiscarded.clear();
			}
			
		}
		
		return completelyDetermined;
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
