package ddejonge.ggp.tools.dataStructures;

import java.util.ArrayList;

public class UnionCollection_Example {

	public static void main(String[] args) {
		
		ArrayList<String> list1 = new ArrayList<>();
		list1.add("A");
		list1.add("B");
		list1.add("C");
		
		ArrayList<String> list2 = new ArrayList<>();
		list2.add("D");
		list2.add("E");
		list2.add("F");
		
		ArrayList<String> list3 = new ArrayList<>();
		list3.add("G");
		list3.add("H");
		list3.add("I");
		
		
		UnionCollection<String> unionList = new UnionCollection<>();
		unionList.addAll(list1);
		unionList.addAll(list2);
		unionList.addAll(list3);
		
		for (String string : unionList) {
			System.out.println(string);
		}
	}
}
