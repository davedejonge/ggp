package ddejonge.tools;

import java.util.ArrayList;
import java.util.List;

public class ParseTree_Example {

	//STATIC FIELDS
	
	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS

	//STATIC METHODS
	public static void main(String[] args) {
		
		String s = "does(robot,move(1,2,3), 2)";
		
		ParseTree parseTree = new ParseTree(s);
		
		System.out.println(parseTree.getRoot().getLabel());
		
		for(ParseNode node : parseTree.getRoot().children){
			System.out.println(node.getLabel());
		}
		
	}
	

}
