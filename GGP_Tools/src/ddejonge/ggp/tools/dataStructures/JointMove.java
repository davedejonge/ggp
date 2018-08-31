package ddejonge.ggp.tools.dataStructures;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.tools.NotImplementedException;


public class JointMove implements Serializable, List<Move> {

	private static final long serialVersionUID = 1L;
	
	
	Move[] array;
	int size = 0; //the number of elements in the list. Is not the same as the length of the array!
	
	public JointMove(int numPlayers){
		array = new Move[numPlayers];
	}
	
	
	public JointMove(Move... array){
		this.array = Arrays.copyOf(array, array.length);
		size = array.length;
	}
	
	public JointMove(Collection<? extends Move> c){
		array = new Move[c.size()];
		size = c.size();
		
		int i=0;
		for(Move move : c){
			array[i] = move;
			i++;
		}
	}
	
	
	@Override
	public boolean add(Move e) {
		array[size] = e;
		size++;
		return true;
	}

	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public void add(int index, Move element) {
		throw new RuntimeException("JointMove.add() Error! Not implemented.");
	}

	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean addAll(Collection<? extends Move> c) {
		throw new RuntimeException("JointMove.addAll() Error!  Not implemented.");
	}

	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Move> c) {
		throw new RuntimeException("JointMove.addAll() Error! Not implemented.");
	}

	@Override
	public void clear() {
		Arrays.fill(array,null);
		size = 0;
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean contains(Object o) {
			throw new RuntimeException("JointMove.contains() Error! Not implemented.");
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("JointMove.containsAll() Error! Not implemented.");
	}

	@Override
	public Move get(int index) {
		return array[index];
	}

	public int getNumPlayers(){
		return this.array.length;
	}
	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public int indexOf(Object o) {
		throw new RuntimeException("JointMove.indexOf() Error! Not implemented.");
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Iterator<Move> iterator() {
		return new MoveListIterator(this);
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public int lastIndexOf(Object o) {
		throw new RuntimeException("JointMove.lastIndexOf() Error! Not implemented.");
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public ListIterator<Move> listIterator() {
		throw new RuntimeException("JointMove.listIterator() Error! Not implemented.");
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public ListIterator<Move> listIterator(int index) {
		throw new RuntimeException("JointMove.listIterator() Error! Not implemented.");
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("JointMove.remove() Error! Not implemented.");
	}

	@Override
	public Move remove(int index) {
		
		Move removedElement =  array[index];
		array[index] = null;
		
		if(removedElement != null){
			size--;
		}
		
		return removedElement;
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("JointMove.removeAll() Error! Not implemented.");
	}

	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("JointMove.retainAll() Error! Not implemented.");
	}

	@Override
	public Move set(int index, Move element) {
		
		Move previousElement = array[index];
		array[index] = element;
		
		if(previousElement == null){
			size++;
		}
		
		
		
		return previousElement;
	}

	@Override
	public int size() {
		return size;
	}

	
	
	/**
	 * THIS METHOD IS NOT IMPLEMENTED!
	 * @param c
	 * @return
	 */
	@Override
	public List<Move> subList(int fromIndex, int toIndex) {
		throw new RuntimeException("JointMove.subList() Error! Not implemented.");
	}

	@Override
	public Object[] toArray() {
		return Arrays.copyOf(array, array.length);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return (T[])Arrays.copyOf(array, array.length);
	}
	
	public String toString(){
		return Arrays.toString(array);
	}
	
	public boolean equals(Object object){
		
		if( ! (object instanceof JointMove)){
			return false;
		}
		JointMove other = (JointMove)object;
		
		for (int i = 0; i < array.length; i++) {
			if( ! array[i].equals(other.array[i])){
				return false;
			}
		}
		
		return true;
	}
	
	class MoveListIterator implements Iterator<Move>{

		int cursor = 0;
		JointMove moveList;
		
		public MoveListIterator(JointMove moveList) {
			
			if(moveList.size < moveList.array.length){
				throw new RuntimeException("MoveList.MoveListIterator.MoveListIterator() Error! Iterating over MoveList is only allowed if the MoveList has been filled completely.");
			}
			
			this.moveList = moveList;
		}
		
		@Override
		public boolean hasNext() {
			return cursor < moveList.size();
		}

		@Override
		public Move next() {
			return moveList.get(cursor++);
		}

		@Override
		public void remove() {
			throw new RuntimeException("JointMove.MoveListIterator.remove() Error! Not implemented.");
		}
		
	}

}
