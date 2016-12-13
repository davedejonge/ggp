package ddejonge.ggp.tools.dataStructures;

import java.util.Arrays;

/**
 * Alternative to HashMap that uses arrays and indices to retrieve objects rather than hashCodes.
 * @author Dave de Jonge, Western Sydney University
 *
 * @param <K>
 * @param <V>
 */
public class ArrayMap<K, V>{
	
	Object[] keyArray;
	Object[] valueArray;
	
	int nextIndex = 0;
	
	public ArrayMap(int capacity){
		
		keyArray = new Object[capacity];
		valueArray = new Object[capacity];
	}
	
	
	public void put(K key, V value){
		
		int index = getKeyIndex(key);
		
		
		if(index == -1){//The key wasn't present yet. Add the value to the end of the array
			index = nextIndex;
			nextIndex++;
			
			if(index >= keyArray.length){
				resizeArrays();
			}
			
		}
		
		keyArray[index] = key;
		valueArray[index] = value;
	}
	
	private int getKeyIndex(K key){
		
		for (int i = 0; i < keyArray.length; i++) {
			if(keyArray[i] != null && keyArray[i].equals(key)){
				return i;
			}
		}
		
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public V get(K key){
		
		int index = getKeyIndex(key);
		
		if(index == -1){
			return null;
		}
		
		return (V) valueArray[index];
		
	}
	
	public void clear(){
		Arrays.fill(keyArray, null);
		Arrays.fill(valueArray, null);
		nextIndex = 0;
	}
	
	
	private void resizeArrays(){
		
		keyArray = Arrays.copyOf(keyArray, 2*keyArray.length);
		valueArray = Arrays.copyOf(valueArray, 2*valueArray.length);
		
	}

	public String toString(){
		String s = Arrays.toString(this.keyArray); 
		s += System.lineSeparator();
		s += Arrays.toString(this.valueArray);
		return s;
	}
}
