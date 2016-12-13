package ddejonge.ggp.tools.dataStructures;

import java.util.ArrayList;


public class ArrayOfLists<T> {
	
	
	Object[] array;
	int defaultListSize;
	boolean autoResize = false;
	
	public ArrayOfLists(int arraySize, int defaultListSize, boolean autoResize){		
		array = new Object[arraySize];
		this.defaultListSize = defaultListSize;
		this.autoResize = autoResize;
	}
	
	/**
	 * 
	 * @param arraySize The number of lists in this table.
	 * @param defaultListSize The initial capacity of each list in this table.
	 */
	public ArrayOfLists(int arraySize, int defaultListSize){		
		array = new Object[arraySize];
		this.defaultListSize = defaultListSize;
	}
	
	
	
	public void add(int index, T object){
		
		if(index >= array.length && autoResize){
			
			Object[] newArray = new Object[index+1];
			for(int i=0; i<array.length; i++){
				newArray[i] = array[i];
			}
			
			array = newArray;
			
		}
		
		
		if(array[index] == null){
			array[index] = new ArrayList<T>(defaultListSize);
		}
		((ArrayList<T>)array[index]).add(object);
	}
	
	
	/**
	 * Puts the given list at the given index.
	 * Overwrites any existing list at that index. 
	 * Does not make a copy of the given list.
	 * @param index
	 * @param list
	 */
	public void setList(int index, ArrayList<T> list){
		
		if(index >= array.length && autoResize){
			
			Object[] newArray = new Object[index+1];
			for(int i=0; i<array.length; i++){
				newArray[i] = array[i];
			}
			
			array = newArray;
			
		}
		
		array[index] = list;
	}
	
	public T get(int row, int col){
		return ((ArrayList<T>)array[row]).get(col);
	}
	
	public T remove(int row, int col){
		if(array[row] == null || ((ArrayList<T>)array[row]).size() == 0){
			return null;
		}
		return ((ArrayList<T>)array[row]).remove(col);
	}
	
	public ArrayList<T> get(int row){
		return (ArrayList<T>)array[row];
	}
	
	/**
	 * Clears all the lists in this table.
	 */
	public void clear(){
		
		for(int i=0; i<array.length; i++){
			if(array[i] != null){
				((ArrayList<T>)array[i]).clear();
			}
			
		}
	}
	
	public int getListSize(int index){
		
		if(array[index] == null){
			return 0;
		}
		
		return ((ArrayList<T>)array[index]).size();
	}
	
	public int getArraySize(){
		return this.array.length;
	}

}
