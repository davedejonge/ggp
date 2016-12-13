package ddejonge.asp.examples;

import ddejonge.asp.ASPRunner;
import ddejonge.asp.Result;

public class TestNestedFunctions {

	
	public static void main(String[] args){
		
		
		String content = getInstance();
		
		
		Result result = ASPRunner.findOptimalModel(content, true, true);
		
		System.out.println();
		System.out.println("***");
		System.out.println(result.toString());
	}
	
	static String getInstance(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("p(f(f(f(1)))).");
		sb.append("p(X) :- p(f(X)).");
		
		return sb.toString();
		
	}
	
}
