package ddejonge.ggp.tools.dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * This collection allows you to put the elements of a number of collections together in one collection, without having to copy all their elements.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 * @param <T>
 */
public class UnionCollection<T> implements Collection<T>{
	//This class implements Collection rather than just Iterable, because we may want to pass it to a function
	// that expects a collection, because it makes a call to addAll().


	List<Collection<? extends T>> listOfCollections = new ArrayList<>();
	
	
	@Override
	public boolean add(T e) {
		return listOfCollections.add(Collections.singleton(e));
	}


	@Override
	public boolean addAll(Collection<? extends T> c) {
		return listOfCollections.add(c);
	}


	@Override
	public void clear() {
		listOfCollections.clear();
	}

	@Override
	public boolean contains(Object o) {

		for(Collection<? extends T> col : listOfCollections){
			if(col.contains(o)){
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * THIS METHOD IS NOT SUPPORTED!
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}



	@Override
	public boolean isEmpty() {
		for(Collection<? extends T> col : listOfCollections){
			if(!col.isEmpty()){
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Iterator<T> iterator() {
		return new UnionIterator();
		
	}

	
	private class UnionIterator implements Iterator<T>{

		
		
		Iterator<Collection<? extends T>> outerIterator = listOfCollections.iterator();
		Iterator<? extends T> innerIterator;
		
		@Override
		public boolean hasNext() {

			while(innerIterator == null || !innerIterator.hasNext()){
				
				if(!outerIterator.hasNext()){
					return false;
				}
				
				Collection<? extends T> nextCollection = outerIterator.next();
				innerIterator = nextCollection.iterator();
				//this new innerIterator may represent an empty list, so therefore we have put this in a while-loop.
			}
				
			return true;
			
		}

		@Override
		public T next() {

			if(!hasNext()){ 
				// We are calling hasNext() here not only to check whether there is a next element, 
				// but also to make sure the right innerIterator is set.
				throw new NoSuchElementException();
			}
			
			return innerIterator.next();
			
		}
		
	}

	/**
	 * THIS METHOD IS NOT SUPPORTED!
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * THIS METHOD IS NOT SUPPORTED!
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * THIS METHOD IS NOT SUPPORTED!
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
		
		
	}


	@Override
	public int size() {
		
		int size = 0;
		
		for(Collection<? extends T> col : listOfCollections){
			size += col.size();
		}
		
		return size;
	}

	/**
	 * THIS METHOD IS NOT SUPPORTED!
	 */
	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	/**
	 * THIS METHOD IS NOT SUPPORTED!
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}



	
	

}
