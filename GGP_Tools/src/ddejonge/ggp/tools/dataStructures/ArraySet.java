package ddejonge.ggp.tools.dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An ArrayList that checks that you don't add the same element twice.<br/>
 * This has the advantage that it implements Set, as well as that you can iterate over it with a numerical iterator.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 * @param <T>
 */
public class ArraySet<T> implements Set<T>{

	

	//STATIC FIELDS

	//FIELDS
	ArrayList<T> backingList;
	
	
	//CONSTRUCTORS
	public ArraySet(){
		backingList = new ArrayList<>();
	}
	
	public ArraySet(int capacity){
		backingList = new ArrayList<>(capacity);
	}

	//METHODS
	
	
	@Override
	public boolean add(T e) {
		
		if(backingList.contains(e)){
			return false;
		}
		
		return backingList.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		
		boolean returnVal = false;
		
		for(T elem : c){
			
			if(add(elem)){
				returnVal = true;
			}
		}
		
		return returnVal;
	}

	@Override
	public void clear() {
		backingList.clear();
	}

	@Override
	public boolean contains(Object o) {
		return backingList.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return backingList.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return backingList.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return backingList.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return backingList.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return backingList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return backingList.retainAll(c);
	}

	@Override
	public int size() {
		return backingList.size();
	}

	@Override
	public Object[] toArray() {
		return backingList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return backingList.toArray(a);
	}
	
	
	public String toString(){
		return backingList.toString();
	}

	public List<T> subList(int i, int size) {
		return Collections.unmodifiableList(backingList.subList(i, size));
	}

	public List<T> toList(){
		return Collections.unmodifiableList(backingList);
	}

}
