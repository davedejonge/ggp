package ddejonge.ggp.tools.zobrist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;


/**
 * This is a Table that maps game states to objects of class V. 
 * Normally, this is used in Monte Carlo Tree Search in order to retrieve a tree node that represents a given state.
 * 
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 * @param <V>
 */
public class TranspositionTable<V> {
	
	
	//FIELDS
	ZobristHasher hasher = new ZobristHasher();
	List<PointerNode<V>> roots; //we store a separate tree for each round of the game.
	
	public TranspositionTable(){
		roots = new ArrayList<PointerNode<V>>(500);
	}
	
	/**
	 * Removes all nodes representing states for the previous rounds.
	 * @param numMovesMade
	 */
	public void cleanUp(int numMovesMade){
		
		for(int i=0; i<numMovesMade; i++){
			
			if(i >= roots.size()){
				break;
			}
			
			PointerNode<V> pointerNode = roots.get(i);
			
			if(pointerNode != null){
				pointerNode.clear();
			}
			
		}
	}
	
	/**
	 * This method is used both to store an new Value object, or to retrieve an existing Value object from the table.
	 * If the given state was already present in the table then the given Value object will not be stored, and the existing Value object will be returned.
	 * If the given state was not present in the table, then it will simply return the given Value object.
	 * 
	 * 
	 * 
	 * @param state
	 * @param value
	 * @param numMovesMade
	 * @return
	 */
	public V retrieve(MachineState state, V value, int numMovesMade){
		
		//make sure that roots.size() is strictly larger than numMovesMade.
		while(roots.size() <= numMovesMade){
			roots.add(null);
		}
		
		//get the root of the tree corresponding with the round of the game.
		PointerNode<V> root;
		
		root = roots.get(numMovesMade);
		if(root == null){
			root = new PointerNode<V>();
			roots.set(numMovesMade, root);
		}
		
		long hashValue = hasher.calculate(state.getContents());
		
		return root.retrieve(value, hashValue);
		
	}

}
