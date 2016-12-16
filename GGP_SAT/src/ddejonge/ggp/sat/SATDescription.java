package ddejonge.ggp.sat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.Role;

import ddejonge.ggp.sat.logic.CNF;
import ddejonge.ggp.sat.logic.Clause;
import ddejonge.ggp.sat.logic.DNF;
import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.sat.logic.SimpleConjunction;
import ddejonge.ggp.sat.logic.XOR;
import ddejonge.ggp.tools.dataStructures.ArraySet;

public class SATDescription {

	//STATIC FIELDS

	//FIELDS
	/**Stores all Proposition objects, indexed by their IDs.*/
	PropositionStorage propositionStorage = new PropositionStorage();
	
	//e.g. maps GOAL to the list of proposition objects corresponding to grounded GOAL atoms.
	HashMap<GdlConstant, List<Proposition>> sentenceNames2propositions = new HashMap<>();
	
	
	/**List of restrictions imposed by the rules of the game.*/
	List<Clause> gameRules = new ArrayList<>();
	
	/**Represents the rule that you must always perform exactly one action.*/
	List<Clause> actionRestrictions; 
	
	/**Represents the rule that you can only perform an action if that action is legal.*/
	List<Clause> legalRestrictions; 
	
	/**Represents the rule that a proposition can only be true if it is in the head of some rule.*/
	List<Clause> nonProduceableRestrictions;
	
	List<Clause> allRulesAndRestrictions;
	List<Clause> generalRulesAndRestrictions;
	
	List<Role> roles = new ArrayList<>();
	
	
	/**List of all Proposition objects that represent a grounded GDl relation of the form true(...)*/
	/*List<Proposition> trueProps = new ArrayList<>();*/
	
	Proposition terminal;
	
	/**Maps the name of each role to the list of Does-propositions for that role.*/
	HashMap<String, List<Proposition>> roleNames2doesPropositions = new HashMap<>();
	
	/** Maps each Does Proposition to its corresponding Legal proposition.
	 *  e.g.  does(xplayer, mark(1,1,x))  -->   legal(xplayer, mark(1,1,x))
	 * */
	HashMap<Proposition, Proposition> doesProp2legalProp = new HashMap<>();
	
	public HashMap<Proposition, Proposition> trueProp2NextProp = new HashMap<>();
	
	
	//CONSTRUCTORS
	public SATDescription(List<GdlRule> groundedDescription, List<Role> roles){
		
		this.roles = roles;
		
		//Fill the proposition storage.
		producePropositionObjects(groundedDescription);
		
		
		//Fill the table that maps relation names to proposition objects.
		for(Proposition proposition : propositionStorage.toList()){
			
			GdlConstant relationName = proposition.getGdlSentence().getName(); //e.g. GOAL
			
			List<Proposition> list = sentenceNames2propositions.get(relationName);
			if(list == null){
				list = new ArrayList<>();
				sentenceNames2propositions.put(relationName, list);
			}
			
			list.add(proposition);
		}
		
		
		
		
		//Generate rules to ensure that propositions can only be true if they appear in the head of some rule.
		initProducabilityRestrictions(groundedDescription);
		
		//Add action restrictions
		initActionRestrictions();
		
		
		//add legal restrictions
		// does(A, x) -->  legal(A,x)
		// ~does(A,x) OR legal(A,x)
		initLegalRestrictions();
		
		
		initGameRules(groundedDescription);
		
		
		ArrayList<Clause> _generalRulesAndRestrictions = new ArrayList<>();
		_generalRulesAndRestrictions.addAll(gameRules);
		_generalRulesAndRestrictions.addAll(nonProduceableRestrictions);
		generalRulesAndRestrictions = Collections.unmodifiableList(_generalRulesAndRestrictions);
		
		ArrayList<Clause> _allRulesAndRestrictions = new ArrayList<>();
		_allRulesAndRestrictions.addAll(gameRules);
		_allRulesAndRestrictions.addAll(legalRestrictions);
		_allRulesAndRestrictions.addAll(actionRestrictions);
		_allRulesAndRestrictions.addAll(nonProduceableRestrictions);
		allRulesAndRestrictions = Collections.unmodifiableList(_allRulesAndRestrictions);
	}
	
	
	
	private void initProducabilityRestrictions(List<GdlRule> groundedDescription){
		
		ArraySet<Proposition> produceablePropositions = new ArraySet<Proposition>();
		for(GdlRule gdlRule : groundedDescription){
			GdlSentence head = gdlRule.getHead();
			
			
			if(head.getName().equals(GdlPool.BASE) || head.getName().equals(GdlPool.INPUT)){
				continue; //skip these proposition because they are not stored in the propositionStorage, which causes gdlSentence2proposition to throw an exception.
			}
			
			produceablePropositions.add(GDL2SATConverter.toSAT(this, head));
			
			//True and Does propositions never appear in the head of a rule. However, they clearly must be produceable.
			if(head.getName().equals(GdlPool.INIT)){
				
				// A true-proposition is produceable if there is a rule with the corresponding init-prop as its head.
				GdlSentence trueProp = GdlPool.getRelation(GdlPool.TRUE, head.getBody());
				produceablePropositions.add(GDL2SATConverter.toSAT(this, trueProp));
				
			}else if(head.getName().equals(GdlPool.NEXT)){
				
				// A true-proposition is produceable if there is a rule with the corresponding next-prop as its head.
				GdlSentence trueProp = GdlPool.getRelation(GdlPool.TRUE, head.getBody());
				produceablePropositions.add(GDL2SATConverter.toSAT(this, trueProp));
				
			}else if(head.getName().equals(GdlPool.LEGAL)){
				
				// a does-proposition is produceable if and only if there is a rule with the corresponding legal-prop in the head.
				GdlSentence doesProp = GdlPool.getRelation(GdlPool.DOES, head.getBody());
				produceablePropositions.add(GDL2SATConverter.toSAT(this, doesProp));
				
			}
			
		}
		
		
		ArrayList<Proposition> nonProduceablePropositions = new ArrayList<Proposition>(propositionStorage.toList()); 
		nonProduceablePropositions.removeAll(produceablePropositions);
		
		//For each proposition p that does not appear in the head of any rule: add the singleton clause ~p
		nonProduceableRestrictions = new ArrayList<>(nonProduceablePropositions.size());
		for(Proposition nonProduceableProposition: nonProduceablePropositions){
			Clause clause = new Clause(nonProduceableProposition, false);
			nonProduceableRestrictions.add(clause);
		}

	}
	
	
	private void initActionRestrictions(){
		
		this.actionRestrictions = new ArrayList<>();
		for(Role role : roles){
			List<Proposition> doesPropositions = roleNames2doesPropositions.get(role.toString());
			
			XOR xor = new XOR(doesPropositions);
			DNF restrictions = SATUtils.xor2dnf(xor);
			CNF restrictionsAsCNF = SATUtils.dnf2cnf(restrictions, propositionStorage);
			actionRestrictions.addAll(restrictionsAsCNF);
		}
	}
	
	private void _initActionRestrictions(){
		
		
		this.actionRestrictions = new ArrayList<>();
		for(Role role : roles){
			List<Proposition> doesPropositions = roleNames2doesPropositions.get(role.toString());
			
			//If role A has three possible actions:  do(A, x)   do(A,y)   do(A, z)
			// then this DNF will be of the form:
			//  (do(A, x) AND  ~do(A,y) AND  ~do(A, z))    OR  (~do(A, x) AND  do(A,y) AND  ~do(A, z))    OR     (~do(A, x) AND  ~do(A,y) AND  do(A, z))
			// meaning that role A must always perform exactly one action.
			DNF restrictions = new DNF();
			
			for(Proposition trueDoesProp : doesPropositions){
				
				//Create a conjunction in which the current proposition is true, and all other does-propositions of this role are false.
				SimpleConjunction conjunction = new SimpleConjunction();
				
				//add the current proposition as a positive one.
				conjunction.addLiteral(trueDoesProp, true);
				
				//add all other does propositions as a negative one.
				for(Proposition falseDoesProp : doesPropositions){
					if(falseDoesProp == trueDoesProp){
						continue;
					}
					conjunction.addLiteral(falseDoesProp, false);
				}
				
				restrictions.addConjunction(conjunction);
			}
			
			CNF restrictionsAsCNF = SATUtils.dnf2cnf(restrictions, propositionStorage);
			actionRestrictions.addAll(restrictionsAsCNF);
		}
		
	}
	
	private void initLegalRestrictions(){
		
		legalRestrictions = new ArrayList<>();
		for(Role role : roles){
			List<Proposition> doesPropositions = roleNames2doesPropositions.get(role.toString());
			
			for(Proposition doesProp : doesPropositions){
				
				Clause legalRestriction = new Clause();
				
				legalRestriction.addLiteral(doesProp, false);
				
				Proposition legalProp = doesProp2legalProp.get(doesProp);
				legalRestriction.addLiteral(legalProp, true);
				
				legalRestrictions.add(legalRestriction);				
			}
		
		}
	}
	
	private void initGameRules(List<GdlRule> groundedDescription){
		
		//Convert the rules into DNF
		HashMap<Proposition, DNF> head2bodies = new HashMap<>();
		for(GdlRule gdlRule : groundedDescription){
			
			GdlConstant type = gdlRule.getHead().getName();
			if(type.equals(GdlPool.BASE) || type.equals(GdlPool.INPUT)){
				continue;
			}
			//do not skip INIT rules, because we need them to determine the initial state.
			
			Proposition head = GDL2SATConverter.toSAT(this, gdlRule.getHead());
			
			if(gdlRule.getBody().isEmpty()){
				
				//directly create a clause and add it to the gameRules.
				Clause clause = new Clause(head, true);
				gameRules.add(clause);
				
				continue;
			}
			
			
			//get all bodies already stored in the map
			DNF dnf = head2bodies.get(head);
			if(dnf == null){
				dnf = new DNF();
				head2bodies.put(head, dnf);
			}
			
			//add the body of the current rule.
			SimpleConjunction conjunctionToAdd = body2simpleConjunction(gdlRule.getBody());
			dnf.addConjunction(conjunctionToAdd);
			
		}
	
		
		//Convert the DNF rules into CNF.
		
		// we now have:
		//  p <--> DNF
		//  p --> DNF    AND   DNF --> p
		//  ~p OR DNF    AND   ~DNF OR p
		// note that (~p OR DNF) is itself a DNF, so it can be converted directly into CNF.
		// furthermore, ~DNF can be easily converted into CNF, and  CNF OR p can also be easily converted into CNF
		
		
		for(Proposition head : head2bodies.keySet()){
			
			DNF bodies = head2bodies.get(head);
			
			//First handle ~p OR DNF
			DNF dnf1 = new DNF(bodies);
			
			SimpleConjunction notP = new SimpleConjunction();
			notP.addLiteral(head, false);
			dnf1.addConjunction(notP);
			
			//convert dnf1 to CNF
			CNF cnf1 = SATUtils.dnf2cnf(dnf1, propositionStorage);
			
			//add each clause in the CNF to the total CNF
			gameRules.addAll(cnf1);
			
			//Second, handle ~DNF OR p
			CNF cnf2 = SATUtils.negatedDnf2cnf(bodies);
			cnf2 = SATUtils.disjunctCNFwithLiteral(cnf2, true, head);
			gameRules.addAll(cnf2);
			
		}
	}
	
	
	public SimpleConjunction body2simpleConjunction(List<GdlLiteral> ruleBody){
		
		SimpleConjunction conjunction = new SimpleConjunction();
		
		for(GdlLiteral lit : ruleBody){
			
			if(lit instanceof GdlNot){
				
				GdlLiteral prop = ((GdlNot) lit).getBody();
				Proposition atom = GDL2SATConverter.toSAT(this, (GdlSentence)prop);
				conjunction.addLiteral(atom, false);
			
			}else if(lit instanceof GdlSentence){
				
				Proposition atom = GDL2SATConverter.toSAT(this, (GdlSentence)lit);
				conjunction.addLiteral(atom, true);
			
			}else{
				throw new RuntimeException("SatDescription.body2simpleConjunction() Error! " + lit.getClass().getSimpleName());
			}
		}
		
		return conjunction;
	}
	
	/**
	 * Extracts all ground atoms from the grounded game description and converts them into Proposition objects.
	 * @param groundedDescription
	 */
	private void producePropositionObjects(List<GdlRule> groundedDescription){
		
		//1.Extract all atoms from the description.
		Set<GdlSentence> allAtoms = SATUtils.extractAtoms(groundedDescription);
		
		
		Set<GdlSentence> truePropositions = new ArraySet<>();
		List<GdlSentence> inputPropositions = new ArrayList<>();
		
		//2.Convert them into Proposition objects
		int id = 1;
		for(GdlSentence atom : allAtoms){

			if(atom instanceof GdlRelation){
				GdlRelation relation = (GdlRelation)atom;
				
				//Create a new proposition object.
				Proposition prop = propositionStorage.add(atom);
				
				
				//Convert INIT, BASE and NEXT to TRUE propositions.
				// Note: if everything is okay we only really need to do this for the BASE propositions, but just to be sure we also convert the others.
				// Also, store INPUT and TERMINAL propositions separately.
				if(relation.getName().equals(GdlPool.BASE)){
					
					GdlRelation trueProp = GdlPool.getRelation(GdlPool.TRUE, atom.getBody());
					truePropositions.add(trueProp);
				
				}else if(relation.getName().equals(GdlPool.INIT)){
					
					GdlRelation trueProp = GdlPool.getRelation(GdlPool.TRUE, atom.getBody());
					truePropositions.add(trueProp);
				
				}else if(relation.getName().equals(GdlPool.NEXT)){
					
					GdlRelation trueProp = GdlPool.getRelation(GdlPool.TRUE, atom.getBody());
					truePropositions.add(trueProp);
					
				}else if(relation.getName().equals(GdlPool.TRUE)){
					
					truePropositions.add(atom);
				
				}else if(relation.getName().equals(GdlPool.INPUT)){
					inputPropositions.add(atom);
				
				}else if(relation.getName().equals(GdlPool.TERMINAL)){
					
					this.terminal = prop;
				}
			
			}else if(atom instanceof GdlProposition){
				
				//Create a new proposition object.
				propositionStorage.add(atom);
			
			}else{
				
				throw new RuntimeException("SATDescription.producePropositionObjects() Error! unknown class: " + atom.getClass().getSimpleName());
			}
			
			

		}
		
		//Make sure we have exactly one True proposition for each Base proposition.
		for(GdlSentence trueSentence : truePropositions){
			
			//convert it.
			Proposition trueProp = propositionStorage.add(trueSentence);
			/*trueProps.add(trueProp);*/
			
			GdlRelation nextSenctence = GdlPool.getRelation(GdlPool.NEXT, trueSentence.getBody());
			Proposition nextProp = propositionStorage.add(nextSenctence);
			
			trueProp2NextProp.put(trueProp, nextProp);
		}
		
		//Make sure we have exactly one Does proposition and one Legal proposition for each Input proposition.
		for(GdlSentence inputProp : inputPropositions){
			
			//1. Get the corresponding does prop.
			GdlRelation doesRelation = GdlPool.getRelation(GdlPool.DOES, inputProp.getBody());
			Proposition doesProp = propositionStorage.add(doesRelation);
			
			//   1b. Also store the does prop in a hashmap.
			
			//get the roleName to use as the key in the hashmap.
			String roleName = inputProp.getBody().get(0).toString();
			
			List<Proposition> doesPropositions = roleNames2doesPropositions.get(roleName);
			if(doesPropositions == null){
				doesPropositions = new ArrayList<>();
				roleNames2doesPropositions.put(roleName, doesPropositions);
			}
			doesPropositions.add(doesProp);
			
			
			//2. Get the corresponding legal prop.
			GdlRelation legalRelation = GdlPool.getRelation(GdlPool.LEGAL, inputProp.getBody());
			Proposition legalProp = propositionStorage.add(legalRelation);
			
			//	2b. Also store it in a list.
			doesProp2legalProp.put(doesProp, legalProp);
		}
	}
	
	
	//METHODS

	//GETTERS AND SETTERS
	/**
	 * Returns a list of clauses that represent the rules of the game, but not the restrictions regarding to the legality of moves
	 * or the restriction that each player must make exactly one move. 
	 * @return
	 */
	public List<Clause> getGeneralRulesAndRestrictions(){
		return generalRulesAndRestrictions;
	}
	
	/**
	 * Returns a list of clauses that represent the rules of the game, together with a number of rules
	 * that hold for all games that are defined in GDL (e.g. every player must make exactly one move in every round).
	 * These are necessary for inductive proofs.
	 * @return
	 */
	public List<Clause> getAllRulesAndRestrictions(){
		return allRulesAndRestrictions;
	}
	

	
	
	public int getNumPropositions(){
		return this.propositionStorage.size();
	}
	
	/**
	 * Returns a list of all Proposition objects corresponding the given type.<br/>
	 * For example, if type is GdlPool.GOAL it will return all Goal Propositions.
	 * @param name
	 * @return
	 */
	public List<Proposition> getPropositionsOfType(GdlConstant type){
		return this.sentenceNames2propositions.get(type);
	}

	public ArrayList<Proposition> getPropositionList(){
		return new ArrayList<Proposition>(propositionStorage.toList());
		//can't use an UnmodifiableList here, because the return type must be an ArrayList.
	}
	
	public List<Proposition> getDoesPropositionsByRoleName(String roleName){
		return this.roleNames2doesPropositions.get(roleName);
	}
	
	public PropositionStorage getPropositionStorage(){
		return propositionStorage;
	}
}
