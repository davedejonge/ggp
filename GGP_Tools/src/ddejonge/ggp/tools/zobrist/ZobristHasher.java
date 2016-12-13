package ddejonge.ggp.tools.zobrist;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlSentence;

public class ZobristHasher {

	Random random = new Random();
	HashMap<GdlSentence, Long> map = new HashMap<>(4096);
	
	//TODO: instead of mapping sentences directly to bitstrings, we can alternatively
	// map the relation-symbols, function-symbols and constants to bitstrings.
	
	long calculate(Set<GdlSentence> currentState){
		
		long hash = 0;
		
		for(GdlSentence sentence : currentState){
			long bitString = getBitString(sentence);
			
			hash ^= bitString;
		}
		
		return hash;
	}
	
	
	
	long getBitString(GdlSentence sentence){
		
		Long val = map.get(sentence);
		if(val == null){
			val = random.nextLong();
			map.put(sentence, val);
		}
		
		return val;
	}
	
	
	
}
