package ddejonge.ggp.tools.zobrist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;

public class TranspositionTable_Example {

	//STATIC FIELDS

	//STATIC METHODS
	public static void main(String[] args) {
		
		//example how to use the table.
		//In this example, we use Strings instead of MCTS TreeNodes.
		
		
		//Create a list of base propositions (normally are given in the game description, but here we create them ourselves).
		List<GdlSentence> basePropositions = new ArrayList<GdlSentence>();
		for(int i=0; i<35; i++){
			String name;
			if(i<10){
				name = "p0" + i;
			}else{
				name = "p" + i;
			}
			GdlProposition p = GdlPool.getProposition(GdlPool.getConstant(name));
			basePropositions.add(p);
		}
		
		
		//create the table
		TranspositionTable<String> table = new TranspositionTable<String>();
		
		//Create some states:
		Set<GdlSentence> set1 = new HashSet<GdlSentence>();
		set1.add(basePropositions.get(1));
		set1.add(basePropositions.get(2));
		set1.add(basePropositions.get(3));
		MachineState state1 = new MachineState(set1);
		
		Set<GdlSentence> set2 = new HashSet<GdlSentence>();
		set2.add(basePropositions.get(1));
		set2.add(basePropositions.get(2));
		MachineState state2 = new MachineState(set2);
		
		Set<GdlSentence> set3 = new HashSet<GdlSentence>();
		set3.add(basePropositions.get(2));
		set3.add(basePropositions.get(3));
		MachineState state3 = new MachineState(set3);
		
		
		//store the first 'node'
		table.retrieve(state1, "n123", 0); //the string n123 represents the state in which p1 p2 and p3 are all true.
		
		//store the second 'node'
		table.retrieve(state2, "n12", 0);
		
		//store the third 'node'
		table.retrieve(state3, "n23", 0);
		
		//Now try to store a new node with state1.
		String retrievedNode = (String) table.retrieve(state1, "n321", 0); //the string n321 is a new string, but that represents the same state as n123.
		System.out.println("retrieved node: " + retrievedNode);
		
		//Now try to store yet another new node with state1.
		retrievedNode = (String) table.retrieve(state1, "n132", 0);
		System.out.println("retrieved node: " + retrievedNode);
		
		//Now try to store a new node with state3.
		retrievedNode = (String) table.retrieve(state3, "n32", 0);
		System.out.println("retrieved node: " + retrievedNode);
		
		//Now try to store a new node with state2.
		retrievedNode = (String) table.retrieve(state2, "n21", 0);
		System.out.println("retrieved node: " + retrievedNode);
	}


	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
