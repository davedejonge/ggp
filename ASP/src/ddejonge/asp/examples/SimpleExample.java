package ddejonge.asp.examples;

import java.io.File;
import java.util.ArrayList;

import ddejonge.asp.ASPRunner;
import ddejonge.asp.Result;
import ddejonge.asp.tools.FileIO;

public class SimpleExample {

	public static void main(String[] args) {
	
		
		//1.Create a very simple problem.
		ArrayList<String> content = new ArrayList<>();
		content.add("a :- not b.");
		content.add("b :- not a.");
		content.add("c.");
		content.add(":- c, not b.");
		
		Result result = ASPRunner.findModels(content,0, true, Integer.MAX_VALUE);
		
		System.out.println();
		System.out.println("***");
		System.out.println("Satisfiable: " + result.satisfiable);
		System.out.println("Num. Models: " + result.numModels);
	}
	
	

	/*
	public static boolean isSatsifiable(ArrayList<String> program){
		return getNumberOfModels(program) > 0;
	}
	

	public static int getNumberOfModels(ArrayList<String> program){
		
	}*/
	
	
	public static void getAllModels(ArrayList<String> program){
		
	}
	
	
	
	
}
