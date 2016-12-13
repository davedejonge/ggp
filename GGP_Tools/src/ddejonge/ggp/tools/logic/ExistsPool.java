package ddejonge.ggp.tools.logic;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlVariable;

import ddejonge.ggp.tools.Pair;

public class ExistsPool {

	private static final ConcurrentMap<Pair<List<GdlVariable>, GdlLiteral>, Exists> existsPool = new ConcurrentHashMap<Pair<List<GdlVariable>, GdlLiteral>, Exists>();
	
	
	public static void drainPool() {
		existsPool.clear();
	}
	
	/** 
	 * If the pool does not have a mapping for the given key, adds a mapping from key to value
	 * to the pool.
	 *
	 * Note that even if you've checked to make sure that the pool doesn't contain the key,
	 * you still shouldn't assume that this method actually inserts the given value, since
	 * this class is accessed by multiple threads simultaneously.
	 *
	 * @return the value mapped to by key in the pool
	 */
	private static <K,V> V addToPool(K key, V value, ConcurrentMap<K, V> pool) {
		V prevValue = pool.putIfAbsent(key, value);
		if(prevValue == null)
			return value;
		else
			return prevValue;
	}
	

	public static Exists getExists(List<GdlVariable> variables, GdlLiteral body)
	{
		
		Pair<List<GdlVariable>, GdlLiteral> pair = new Pair<List<GdlVariable>, GdlLiteral>(variables, body);
		
		Exists ret = existsPool.get(pair);
		if(ret == null) {
		    Exists ex = new Exists(variables, body);
			ret = addToPool(pair, ex, existsPool);
		}

		return ret;
	}
	
	
}
