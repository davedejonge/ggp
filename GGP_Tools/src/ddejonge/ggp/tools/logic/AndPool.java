package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ggp.base.util.gdl.grammar.GdlLiteral;

public class AndPool {
	
	private static final ConcurrentMap<List<GdlLiteral>, GdlAnd> andPool = new ConcurrentHashMap<List<GdlLiteral>, GdlAnd>();
	
	
	public static void drainPool() {
		andPool.clear();
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
	
	
	
	public static GdlAnd getAnd(GdlLiteral[] conjuncts)
	{
		return getAnd(Arrays.asList(conjuncts));
	}

	public static GdlAnd getAnd(List<GdlLiteral> conjuncts)
	{
		GdlAnd ret = andPool.get(conjuncts);
		if(ret == null) {
		    conjuncts = getImmutableCopy(conjuncts);
			ret = addToPool(conjuncts, new GdlAnd(conjuncts), andPool);
		}

		return ret;
	}
	
	private static <T> List<T> getImmutableCopy(List<T> list) {
	    return Collections.unmodifiableList(new ArrayList<T>(list));
	}
}
