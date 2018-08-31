package ddejonge.ggp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class that provides a method to find the intersection of a list of lists.
 * @author Dave de Jonge
 *
 */
public class Intersection {



	//STATIC METHODS
	public static <T> List<T> getIntersection(List<? extends List<T>> listOfLists){
		
		List<T> intersection = new ArrayList<T>();
		
		//simply loop over the first list and check for each element whether it is contained in the other lists.
		List<T> firstList = listOfLists.get(0);
		for(T object : firstList){
			
			boolean isContainedInOthers = true;
			for(int i=1; i<listOfLists.size(); i++){
				if( ! listOfLists.get(i).contains(object)){
					isContainedInOthers = false;
					break;
				}
			}
			
			if(isContainedInOthers){
				intersection.add(object);
			}
		}
		
		return intersection;
		
	}
	
	public static <T extends Comparable<T>> List<T> getIntersection2(List<? extends List<T>> listOfLists){
		
		for (int i = 0; i < listOfLists.size(); i++) {
			Collections.sort(listOfLists.get(i));
		}
		
		throw new RuntimeException("Intersection.getIntersection2() Error! Not implemented.");
		
	}
}
