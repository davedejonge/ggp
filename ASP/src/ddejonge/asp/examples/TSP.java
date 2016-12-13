package ddejonge.asp.examples;

import ddejonge.asp.ASPRunner;
import ddejonge.asp.Result;

public class TSP {

	public static void main(String[] args){
		
		
		String content = getTspInstance();
		
		
		Result result = ASPRunner.findOptimalModel(content, true, true);
		
		System.out.println();
		System.out.println("***");
		System.out.println(result.toString());
	}
	
	public static String getTspInstance(){
		
		StringBuilder sb = new StringBuilder();
		
		//1. PROBLEM INSTANCE	
		
		//create 6 nodes.
		sb.append("node(1..6)."); 
		sb.append(System.lineSeparator());
		
		//Create edges:
		sb.append("edge(1, (2;3;4)). "); //three edges, coming out of node 1, going into 2,3, and 4 respectively.
		sb.append("edge(2, (4;5;6)). "); 
		sb.append("edge(3, (1;4;5)). "); 
		sb.append("edge(4, (1;2)). "); 
		sb.append("edge(5, (3;4;6)). "); 
		sb.append("edge(6, (2;3;5)). "); 
		sb.append(System.lineSeparator());
		
		//Set edge weights:
		sb.append("cost(1,2,2). "); 
		sb.append("cost(1,3,3). "); 
		sb.append("cost(1,4,1). "); 
		sb.append("cost(2,4,2). "); 
		sb.append("cost(2,5,2). "); 
		sb.append("cost(2,6,4). "); 
		sb.append("cost(3,1,3). "); 
		sb.append("cost(3,4,2). "); 
		sb.append("cost(3,5,2). "); 
		sb.append("cost(4,1,1). "); 
		sb.append("cost(4,2,2). "); 
		sb.append("cost(5,3,2). "); 
		sb.append("cost(5,4,2). "); 
		sb.append("cost(5,6,1). "); 
		sb.append("cost(6,2,4). "); 
		sb.append("cost(6,3,3). "); 
		sb.append("cost(6,5,1). "); 
		sb.append(System.lineSeparator());
		
		
		// 2. PROBLEM 'Generate'
		//	each node must have exactly 1 outgoing edge and exactly 1 incoming edge.
		sb.append("{ cycle(X,Y) : edge(X,Y) } = 1 :- node(X). "); 
		sb.append(System.lineSeparator());
		sb.append("{ cycle(X,Y) : edge(X,Y) } = 1 :- node(Y). "); 
		sb.append(System.lineSeparator());
		
		// 3. PROBLEM 'Define'
		// any node connected to node 1 via a sequence of edges is 'reached'.
		sb.append("reached(Y) :- cycle(1,Y). "); 
		sb.append(System.lineSeparator());
		sb.append("reached(Y) :- cycle(X,Y), reached(X). "); 
		sb.append(System.lineSeparator());
		
		// 4. PROBLEM 'Test'
		//	every node must be reached.
		sb.append(":- node(Y), not reached(Y). "); 
		sb.append(System.lineSeparator());
		
		// 5. PROBLEM 'optimize'
		sb.append("#minimize { C,X,Y : cycle(X,Y), cost(X,Y,C) }. "); 
		
		// 6. DISPLAY
		sb.append("#show cycle/2. "); 
		
		return sb.toString();
		
	}
}
