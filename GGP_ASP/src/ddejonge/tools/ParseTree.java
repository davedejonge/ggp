package ddejonge.tools;

public class ParseTree {

	//STATIC FIELDS

	//FIELDS
	char startChildren = '(';
	char endChildren = ')';
	char siblingSeparator = ',';
	
	ParseNode root;

	//CONSTRUCTORS
	public ParseTree(String stringToParse){
		parse(stringToParse);
	}

	ParseTree(String stringToParse, char startChildren, char endChildren, char siblingSeparator){
		this.startChildren = startChildren;
		this.endChildren = endChildren;
		this.siblingSeparator = siblingSeparator;
		
		parse(stringToParse);
	}
	
	
	//METHODS
	//e.g. does(robot,move(1,2,3), 2)
	private void parse(String functionString){
		
		functionString = functionString.trim();
		
		char[] chars = functionString.toCharArray();
		
		root = new ParseNode();
		root.startIndex = 0;
		
		ParseNode currentNode = root;
		
		for(int i=0; i<chars.length; i++){
			if(chars[i] == startChildren){
				//create a new child, set its startindex.
				
				currentNode.setEndIndex(i);
				currentNode.setLabel(functionString);
				
				ParseNode newChild = new ParseNode();
				newChild.setStartIndex(i+1);
				
				currentNode.addChild(newChild);
				
				currentNode = newChild;
				
			}else if(chars[i] == endChildren){
				//set the endIndex of the current child, move back to parent
				
				if(currentNode.getLabel() == null){
					currentNode.setEndIndex(i);
					currentNode.setLabel(functionString);
				}
				
				currentNode = currentNode.getParent();
				
			}else if(chars[i] == siblingSeparator){
				//set the endindex of the current child, create a new child, set its startindex.
				
				if(currentNode.getLabel() == null){
					currentNode.setEndIndex(i);
					currentNode.setLabel(functionString);
				}//if the comma comes directly after a ')' then the endindex of the previous sibling was already set.
				
				ParseNode newSibling = new ParseNode();
				newSibling.setStartIndex(i+1);
				
				currentNode.getParent().addChild(newSibling);
				
				currentNode = newSibling;
			}
		}
		
		
		//this happens if there is no '(' or ')' or ',' in the functionString
		if(currentNode.getLabel() == null){
			currentNode.setEndIndex(chars.length);
			currentNode.setLabel(functionString);
		}
		
		return;
	}

	//GETTERS AND SETTERS
	public ParseNode getRoot(){
		return root;
	}

	//STATIC METHODS
}
