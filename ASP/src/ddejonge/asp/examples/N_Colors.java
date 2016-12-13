package ddejonge.asp.examples;

import ddejonge.asp.ASPRunner;
import ddejonge.asp.Result;

public class N_Colors {
	
	public static void main(String[] args){
		
		int numColors = 3;
		
		String content = n_colors(numColors);
		
		int numSolutions = 0; //0 actually means that we want ALL solutions.
		
		Result result = ASPRunner.findModels(content, numSolutions, true, Integer.MAX_VALUE);
		
		System.out.println();
		System.out.println("***");
		System.out.println(result.toString());
	}
	
	
	
	/**
	 * Defines the N-Colors problem in ASP.
	 * @param numColors
	 * @return
	 */
	public static String n_colors(int numColors){
		
		StringBuilder sb = new StringBuilder();
		
		//1. PROBLEM INSTANCE	
		
		//create 6 nodes.
		sb.append("node(1..6)."); 
		sb.append(System.lineSeparator());
		
		//Create edges:
		sb.append("edge(1, (2;3;4)). "); //three edges, from 1, to 2,3, and 4 respectively.
		sb.append("edge(2, (4;5;6)). "); 
		sb.append("edge(3, (1;4;5)). "); 
		sb.append("edge(4, (1;2)). "); 
		sb.append("edge(5, (3;4;6)). "); 
		sb.append("edge(6, (2;3;5)). "); 
		sb.append(System.lineSeparator());
		
		// 2. PROBLEM 'Generate'
		//	every node must have exactly 1 color:
		String s = "{ color(X,1..#n) } = 1 :- node(X). ";
		s = s.replace("#n", "" + numColors);
		sb.append(s);  
		sb.append(System.lineSeparator());
		
		
		// 3. PROBLEM 'Define'
		
		// 4. PROBLEM 'Test'
		//	if x and y are neighbors, then they cannot have the same color.
		sb.append(":- edge(X,Y), color(X,C), color(Y,C). ");  
		sb.append(System.lineSeparator());
		
		
		//only output the 'color' predicates
		sb.append("#show color/2.");  
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
	
	
}
