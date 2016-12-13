package ddejonge.ggp.asp.dependencyGraph;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;

import ddejonge.ggp.tools.graph.Vertex;


class Node extends Vertex{

	
	int id;
	GdlSentence sentence;
	
	Node(GdlSentence sentence, int id){
		this.sentence = sentence;
		this.id = id;
	}
	
	@Override
	public void cleanUp(){
		super.cleanUp();
		this.sentence = null;
	}

	@Override
	public String toString(){
		return this.sentence.toString();
	}
	
	@Override
	public boolean equals(Object otherNode){
		
		if( ! (otherNode instanceof Node)){
			return false;
		}
		
		return this.sentence.equals(((Node)otherNode).sentence);
	}
	
	@Override
	public int hashCode(){
		return this.sentence.hashCode();
	}
	
	
}
