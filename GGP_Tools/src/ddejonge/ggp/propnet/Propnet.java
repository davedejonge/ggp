package ddejonge.ggp.propnet;





import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.And;
import org.ggp.base.util.propnet.architecture.components.Constant;
import org.ggp.base.util.propnet.architecture.components.Not;
import org.ggp.base.util.propnet.architecture.components.Or;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.architecture.components.Transition;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

import ddejonge.ggp.tools.dataStructures.ArrayOfLists;



public class Propnet {
	

	//FIELDS
	
	List<GdlTerm> roleNames = new ArrayList<GdlTerm>(); 
	
	//Maps each GdlSentence to its corresponding component.
	HashMap<GdlSentence, jProposition> gdlSentence2Prop = new HashMap<GdlSentence, jProposition>();
	
	HashMap<String, jProposition> string2doesPropositions = new HashMap<>();
	ArrayOfLists<jProposition> roleIndex2doesPropositions = new ArrayOfLists<jProposition>(5, 100, true);
	
	jProposition[] currentlySatisfiedDoesPropositions;
	
	jProposition terminalProposition;
	
	List<List<jProposition>> roleIndex2goalPropositions = new ArrayList<List<jProposition>>();
	/*HashMap<GdlTerm, List<jProposition>> roleName2goalPropositions = new HashMap<GdlTerm, List<jProposition>>();*/
	
	List<List<jProposition>> roleIndex2legalPropositions = new ArrayList<List<jProposition>>();
	/*HashMap<GdlTerm, List<jProposition>> roleName2legalPropositions = new HashMap<GdlTerm, List<jProposition>>();*/
	
	ArrayList<jProposition> nextPropositions = new ArrayList<jProposition>();
	boolean[] nextState;
	
	ArrayList<jProposition> basePropositions = new ArrayList<jProposition>();
	
	Set<GdlSentence> initialState = new HashSet<GdlSentence>();
	
	//List of base propositions for which there is no corresponding NEXT proposition.
	// These propositions can only be true in the initial state.
	ArrayList<jProposition> basePropositionsWithNoNext = new ArrayList<jProposition>();
	
	//METHODS
	
	
	//temporary field, only necessary for initialization.
	HashMap<Component, jComponent> comp2comp = new HashMap<Component, jComponent>();
	
	
	public Propnet(){
		
		
	}
	
	
	
	
	public void init(PropNet _propNet, List<Role> roles){
		
		for (int i = 0; i < roles.size(); i++) {
			this.roleNames.add(roles.get(i).getName());
			
			roleIndex2goalPropositions.add(null);
			roleIndex2legalPropositions.add(null);
		}
		
		currentlySatisfiedDoesPropositions = new jProposition[roleNames.size()];
		
		
		//TODO: we should actually clear all the fields of this object here,
		// just in case the init() method gets called more than once.
		
		
		//1. First just create all components without connecting them
		for(Component _comp : _propNet.getComponents()){
			
			jComponent jComp;
			
			if(_comp instanceof Proposition){
				
				Proposition _proposition = (Proposition)_comp;
				GdlSentence gdlSentence = _proposition.getName();
				
				//System.out.println("Propnet.init() Proposition: " + gdlSentence);
				
				jComp = new jProposition(gdlSentence);
			
				gdlSentence2Prop.put(gdlSentence, (jProposition)jComp);
				
				handleSpecialSentences(gdlSentence, (jProposition)jComp);
				
			}else if(_comp instanceof And){
				
				jComp = new jConnective(jConnective.AND);
				
			}else if(_comp instanceof Or){
				
				jComp = new jConnective(jConnective.OR);
			
			}else if(_comp instanceof Not){
				
				jComp = new jConnective(jConnective.NOT);
				
			}else if(_comp instanceof Constant){
				
				jComp = new jConstant(_comp.getValue());
				
			}else if(_comp instanceof Transition){
				
				// Create a NEXT proposition for the output of this Transition.
				// For some reason, when calling getName() on the input of a Transition you do not get the NEXT proposition, as you would expect.
				// therefore, instead we need to call getName() on the output of hte Transition and convert it to a NEXT proposition.
				
				GdlSentence trueProposition =  ((Proposition)_comp.getOutputs().iterator().next()).getName();
				GdlSentence nextProposition = GdlPool.getRelation(GdlPool.NEXT, trueProposition.getBody());
				
				jComp = new jProposition(nextProposition);
				
				if(! nextPropositions.contains(jComp)){
					nextPropositions.add((jProposition)jComp);
					gdlSentence2Prop.put(nextProposition, (jProposition)jComp);
				}
				
				/*
				System.out.println("Propnet.init() Transition outputs: ");
				for(Component component : _comp.getOutputs()){
					if(component instanceof Proposition){
						System.out.println("  " + ((Proposition)component).getName());
					}else{
						System.out.println("  " + component.getClass().getSimpleName());
					}
				}*/
				
			
			}else{
				throw new RuntimeException("TestPropnetFactory.PropNet2PropNetConverter() Error! " + _comp.getClass().getName());
			}
			
			comp2comp.put(_comp, jComp);
			
		}
		
		
		//2. For all NEXT propositions: set their corresponding TRUE proposition and sentence
		basePropositionsWithNoNext.addAll(basePropositions); //first add all base propositions and then remove those for which there is a corresponding NEXT.
		for(jProposition nextProposition : this.nextPropositions){
			
			jProposition correspondingBaseProposition = gdlSentence2Prop.get(nextProposition.trueSentence);
			nextProposition.correspondingBaseProposition = correspondingBaseProposition;
			
			basePropositionsWithNoNext.remove(correspondingBaseProposition);
		}
		
		this.nextState = new boolean[this.nextPropositions.size()];
		
		
		//3. For all LEGAL propositions: set their corresponding DOES propositions.
		// also fill a list of base propositions for which there is no corresponding next proposition.
		for(int i=0; i<roleNames.size(); i++){
		
			GdlTerm roleName = this.roleNames.get(i);
			
			for(jProposition legalProposition : roleIndex2legalPropositions.get(i)){
				
				GdlRelation doesRelation = GdlPool.getRelation(GdlPool.DOES, legalProposition.gdlSentence.getBody());
				jProposition doesProposition = this.gdlSentence2Prop.get(doesRelation);
				
				if(doesProposition == null){
					// this happens for example with noop. 
					// there is a rule stating it is legal to do noop,
					// but there is no rule with does(xplayer, noop) in the body.
					
					doesProposition = new jProposition(doesRelation);
					gdlSentence2Prop.put(doesRelation, doesProposition);
					
					storeDoesProposition(doesRelation, doesProposition);
					
				}
				
				legalProposition.correspondingDoesProposition = doesProposition;
				
			}
		}
		
		//4. Now make the right connections.
		for(Component _comp : _propNet.getComponents()){
			
			
			// if _comp is a Transition then the corresponding jComp object is a NEXT Proposition.
			/*
			if(_comp instanceof Transition){
				continue;
			}*/
			
			
			jComponent jComp = comp2comp.get(_comp);
			
			for(Component _input : _comp.getInputs()){
				
				
				if(_input instanceof Transition){
					continue;
				}
				
				jComponent jInput = comp2comp.get(_input);
				
				
				if(jInput == null){
					System.out.println("Propnet.init() _input.class: " + _input.getClass().getSimpleName());
					System.out.println("Propnet.init() _input: " + _input);
					System.out.println("Propnet.init() jInput: null");
				}
				
				int index = jComp.addInput(jInput);
				jInput.addOutput(jComp, index);
				
			}
		}
		
		
		//5. Now make sure that the propositions are initialized correctly
		for(jProposition jProp : gdlSentence2Prop.values()){
			GdlSentence gdlSentence = jProp.gdlSentence;
			
			//Note we loop over all propositions rather than only the base propositions.
			// This is because non-base propositions obviously also need to be set.
			
			//Constants don't need their value to be set.
			if(jProp instanceof jConstant){
				continue;
			}
			
			//We only need to set the values of those propositions that do not have inputs.
			if(jProp.input != null){
				continue;
			}
			
			//I suppose that the only propositions left should be either TRUE or DOES propositions.
			/*if( ( ! jProp.gdlSentence.getName().equals(GdlPool.TRUE)) &&  ( ! jProp.gdlSentence.getName().equals(GdlPool.DOES))){
				System.out.println("Propnet.init() WARNGING: we are setting the value of the following proposition: " + jProp.gdlSentence);
			}*/
			
			if(this.initialState.contains(gdlSentence)){
				jProp.setValue(true);
			}else{
				jProp.setValue(false);
			}
			
		}
		
	}
	
	void handleSpecialSentences(GdlSentence sentence, jProposition proposition){
		
		GdlTerm sentenceName = sentence.getName();
		
		if(sentenceName.equals(GdlPool.TERMINAL)){
			
			this.terminalProposition = proposition;
		
		}else if(sentenceName.equals(GdlPool.DOES)){
			
			storeDoesProposition((GdlRelation)sentence, proposition);
			
		}else if(sentenceName.equals(GdlPool.INIT)){
			
			if( ! sentence.getBody().isEmpty()){ //for some unclear reason this happens.
				
				GdlRelation trueRel = GdlPool.getRelation(GdlPool.TRUE, sentence.getBody());
				this.initialState.add(trueRel);
			}
			

		
		}else if(sentenceName.equals(GdlPool.NEXT)){
			
			if(! nextPropositions.contains(proposition)){
				nextPropositions.add(proposition);
			}
		
		}else if(sentenceName.equals(GdlPool.TRUE)){
			
			if(! basePropositions.contains(proposition)){
				basePropositions.add(proposition);
			}
		
		}else if(sentenceName.equals(GdlPool.GOAL)){
			
			GdlTerm roleName = sentence.get(0);
			
			int roleIndex = this.roleNames.indexOf(roleName);
			if(roleIndex == -1){
				System.out.println("WARNING: we have found the following goal propsotion: " + sentence + " while the roles are: " + this.roleNames);
				//This happens in connect5 because it also generates GOAL sentences for the "role" 'blank'.
				return;
				
			}
			
			
			List<jProposition> goalProps = roleIndex2goalPropositions.get(roleIndex);
			if(goalProps == null){
				goalProps = new ArrayList<jProposition>();
				roleIndex2goalPropositions.set(roleIndex, goalProps);
			}
			
			if(! goalProps.contains(proposition)){
				goalProps.add(proposition);
			}
			
		}else if(sentenceName.equals(GdlPool.LEGAL)){
			
			GdlTerm roleName = sentence.get(0);
			
			int roleIndex = this.roleNames.indexOf(roleName);
			
			List<jProposition> legalProps = roleIndex2legalPropositions.get(roleIndex);
			if(legalProps == null){
				legalProps = new ArrayList<jProposition>();
				roleIndex2legalPropositions.set(roleIndex, legalProps);
			}
			
			if(! legalProps.contains(proposition)){
				legalProps.add(proposition);
			}
			
		}
	}
	
	void storeDoesProposition(GdlRelation doesRelation, jProposition proposition){
		
		GdlTerm roleName = doesRelation.getBody().get(0);
		GdlTerm actionTerm = doesRelation.getBody().get(1);
		
		String roleName_plus_actionTerm = roleName.toString() + actionTerm.toString();
		
		this.string2doesPropositions.put(roleName_plus_actionTerm, proposition);
		
		int roleIndex = this.roleNames.indexOf(roleName);
		
		this.roleIndex2doesPropositions.add(roleIndex, proposition);
		
	}
	
	
	
	
	
	
	Set<GdlSentence> getInitialState(){
		return this.initialState;
	}
			
	
	
	public void setState(Set<GdlSentence> newState){
		
		for (int i = 0; i < basePropositions.size(); i++) {
			
			jProposition proposition = this.basePropositions.get(i);
			
			if(newState.contains(proposition.gdlSentence)){
				proposition.setValue(true);
			}else{
				proposition.setValue(false);
			}
			
		}
		
	}
	
	

	
	/**
	 * Sets the action propositions without changing the current state.
	 * @param jointMove
	 */
	void setActions(List<Move> jointMove){

		//Convert the list of moves to a list of Proposition Components.
		List<jProposition> propList = listOfMoves2ListOfPropositions(jointMove);
		
		setActionPropositions(propList);
		
	}
	
	
	
	List<jProposition> propositionList = new ArrayList<jProposition>();
	
	
	static boolean alreadyPrinted = false;
	
	private List<jProposition> listOfMoves2ListOfPropositions(List<Move> jointMove){
		
		//Set the DOES propositions.
		propositionList.clear();
		
		for (int j = 0; j < roleNames.size(); j++) {
			
			Move move = jointMove.get(j);
			
			jProposition proposition;
			if(move instanceof PropnetMove){
				proposition =  ((PropnetMove)move).getPropnetComponent();
			
				if(proposition == null){
					throw new RuntimeException("Propnet.listOfMoves2ListOfPropositions() Error! " +jointMove);
				}
				
			}else{
			
				
				GdlTerm roleName = roleNames.get(j);
				GdlTerm actionTerm = move.getContents();
				
				String roleName_plus_actionTerm = roleName.toString() + actionTerm.toString();
				
				proposition = string2doesPropositions.get(roleName_plus_actionTerm);
				
				if(proposition == null){
					
					if(!alreadyPrinted){
						ArrayList<String> alphabeticallyOrdered = new ArrayList<String>(string2doesPropositions.keySet());
						Collections.sort(alphabeticallyOrdered);
						System.out.println();
						for(String string : alphabeticallyOrdered){
							System.out.println(string);
						}
						System.out.println("roleName_plus_actionTerm " + roleName_plus_actionTerm);
						alreadyPrinted = true;
					}
					
					
					
					throw new RuntimeException("Propnet.listOfMoves2ListOfPropositions() Error!");
				}
			}
			
			propositionList.add(proposition);
			
		}
		
		return propositionList;
	}
	
	
	
	/**
	 * Sets the action propositions without changing the current state.
	 * @param jointMove
	 */
	void setActionPropositions(List<jProposition> jointMove){
		
		for(int roleIndex = 0; roleIndex<roleNames.size(); roleIndex++){
			
			jProposition currentAction = currentlySatisfiedDoesPropositions[roleIndex];
			jProposition newAction = jointMove.get(roleIndex);
			
			if( currentAction != newAction){
				if(currentAction != null){
					currentAction.setValue(false);
				}
				newAction.setValue(true);
				currentlySatisfiedDoesPropositions[roleIndex] = newAction;
			}
		}
		
		/*
		//OLD implementation:
		
		for(jProposition doesProposition : currentlySatisfiedDoesPropositions){
			if(!jointMove.contains(doesProposition)){
				doesProposition.setValue(false);
			}
		}
		
		for(jProposition doesProposition : jointMove){
			if(!currentlySatisfiedDoesPropositions.contains(doesProposition)){
				doesProposition.setValue(true);
			}
		}
		
		currentlySatisfiedDoesPropositions.clear();
		currentlySatisfiedDoesPropositions.addAll(jointMove);
		*/
		
		
		//OLDER implementation:
		/*
		for (int i = 0; i < jointMove.size(); i++) {
			
			Proposition satisfiedProposition = jointMove.get(i);
			
			for(Proposition doesProposition : this.roleIndex2doesPropositions.get(i)){
				
				if(satisfiedProposition == doesProposition){
					doesProposition.setValue(true);
				}else{
					doesProposition.setValue(false);
				}
				
			}
		}
		*/
		
		
		//even OLDER implementation:
		/*
		Collection<Proposition> allDoesPropositions = this.string2doesPropositions.values();
		for(Proposition doesProposition : allDoesPropositions){
			
			if(jointMove.contains(doesProposition)){
				doesProposition.setValue(true);
			}else{
				doesProposition.setValue(false);
			}
			
		}*/
		
	}
	
	
	/**
	 * Sets the NEXT state as the current state.
	 */
	void setNextStateAsCurrentState(){
		
		//Note that we have to do this in two steps:
		//  first copy the truth-values of the next-propositions into an array.
		//  and then use those values to set the values of the base propositions.
		// We cannot do this in one step, because setting the value of a base proposition may alter the
		// the value of some next proposition.
		
		for (int i = 0; i < nextPropositions.size(); i++) {
			jProposition nextProposition = this.nextPropositions.get(i);
			
			nextState[i] = nextProposition.currentValue;
		}
		
		for (int i = 0; i < nextPropositions.size(); i++) {
			jProposition nextProposition = this.nextPropositions.get(i);
			
			jProposition baseProposition = nextProposition.correspondingBaseProposition;
			
			if(baseProposition != null){
				baseProposition.setValue(nextState[i]);
				
				//it may happen that a certain proposition does occur as NEXT but not as TRUE.
				//e.g with step counters. In that case we don't have to bother setting its value to TRUE since
				// it will not affect the state anyway.
			}
			
			
		}
		
		
		//some base propositions do not have any corresponding next propositions, so they need to be set to false.
		for(jProposition baseProposition : basePropositionsWithNoNext){
			baseProposition.setValue(false);
		}
	}
	
	
	
	//WARNING! THE ORDER OF THE MOVES IN jointMove MUST CORRESPOND WITH THE ORDER OF THE ROLES IN ROLENAMES!
	Set<GdlSentence> getNextState(Set<GdlSentence> currentState, List<Move> jointMove){
		
		//Set the base propositions
		this.setState(currentState);
		return getNextState(jointMove);
	}
	
	
	/**
	 * Returns the next state without actually setting it as the new state of the propnet.
	 * @return
	 */
	Set<GdlSentence> getNextState(List<Move> jointMove){
		
		this.setActions(jointMove);
		
		return getNextState();

	}
	
	/**
	 * Returns the next state without actually setting it as the new state of the propnet.
	 * @return
	 */
	Set<GdlSentence> getNextState(){
		
		Set<GdlSentence> nextState = new HashSet<GdlSentence>();
		
		for(jProposition nextProposition : nextPropositions){
			if(nextProposition.currentValue){
				nextState.add(nextProposition.trueSentence);
			}
		}
		
		return nextState;
	}
	

	
	Set<GdlSentence> getCurrentState(){
		
		Set<GdlSentence> currentState = new HashSet<GdlSentence>(2*this.basePropositions.size());
		
		for(jProposition baseProposition : this.basePropositions){
			if(baseProposition.currentValue){
				currentState.add(baseProposition.gdlSentence);
			}
		}
		
		return currentState;
	}
	
	

	
	
}
