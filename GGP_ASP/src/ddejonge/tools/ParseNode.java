package ddejonge.tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ParseNode {

	//STATIC FIELDS
	
	
	//FIELDS
	ParseNode parent;
	ArrayList<ParseNode> children;
	private String label;
	
	int startIndex;
	int endIndex;

	//CONSTRUCTORS
	ParseNode(){
	}
	
	ParseNode(String label){
		this.label = label;
	}

	//METHODS
	void addChild(ParseNode child){
		if(this.children == null){
			children = new ArrayList<ParseNode>();
		}
		children.add(child);
		
		child.parent = this;
	}

	//GETTERS AND SETTERS
	void setStartIndex(int startIndex){
		this.startIndex = startIndex;
	}

	void setEndIndex(int endIndex){
		this.endIndex = endIndex;
	}
	
	
	ParseNode getParent(){
		return this.parent;
	}
	
	public String toString(){
		return this.getLabel();
	}

	void setLabel(String functionString){
		this.label = functionString.substring(startIndex, endIndex);
	}
	
	public String getLabel() {
		return label;
	}

	public List<ParseNode> getChildren() {
		if(children == null){
			return null;
		}
		return Collections.unmodifiableList(children);
	}
	
	//STATIC METHODS
}
