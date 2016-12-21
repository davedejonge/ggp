package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;

public class MutexStorage {
	
	

	//map each ground atom to a list of ground atoms that are mutually exclusive.
	// e.g.   cell(1,1,x) -->  { cell(1,1,o) ,  cell(1,1,b) }
	private static HashMap<GdlLiteral, Mutex> map = new HashMap<GdlLiteral, Mutex>();

	
	
	private static ArrayList<GdlLiteral> mutexValues = new ArrayList<GdlLiteral>();
	private static ArrayList<Mutex> mutexes = new ArrayList<Mutex>();
	
	public static boolean test(Conjunction conjunction){
		
		if(map.isEmpty()){
			return true;
		}
		
		mutexValues.clear();
		mutexes.clear();
		
		
		for(int i=0; i<conjunction.size(); i++){
			
			GdlLiteral conjunct = conjunction.get(i);
			
			if( ! conjunct.isGround()){
				continue;
			}
			
			Mutex mutex = map.get(conjunct);
			
			if(mutex == null){
				continue;
			}
			
			int index = mutexes.indexOf(mutex);
			
			if(index != -1 && ! mutexValues.get(index).equals(conjunct)){
				//We have found a variable that is already represented, but by a different conjunct.
				// This means we have two conjuncts which are two different values of the same variable, which is illegal.
				
				return false;
				
			}
			
			
			mutexes.add(mutex);
			mutexValues.add(conjunct);
			
		}
		
		return true;
		
	}
	
	
	public static void addMutex(GdlSentence nonGroundRepresentation, Set<GdlSentence> values, boolean exactlyOneMustBeTrue){
		
		Mutex mutex = new Mutex(nonGroundRepresentation, values, exactlyOneMustBeTrue);
		
		for(GdlSentence value : values){
			map.put(value, mutex);
		}
		
		
	}
	
	
}
