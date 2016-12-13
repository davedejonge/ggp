package ddejonge.ggp.asp;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;

import ddejonge.tools.ParseNode;
import ddejonge.tools.ParseTree;

public class ASP2GDLConverter {

	public static GdlRule parseRule(String stringToParse){
		
		String[] split = stringToParse.split(":-");
		
		String headString = split[0];
		GdlSentence head = parseSentence(headString);
		
		String bodyString = split[1];
		bodyString = "body(" + bodyString +")";
		ParseTree parseTree = new ParseTree(bodyString);
		
		List<GdlLiteral> body = new ArrayList<>();
		for(ParseNode child : parseTree.getRoot().getChildren()){
			
			if(child.getLabel().contains("!=")){
				System.out.println("WARNING 'distinct' is not handled.");
			}else{
			
				GdlLiteral sentence = (GdlLiteral)nodeToTerm(child, true);
				body.add(sentence);
			}
		}
		
		
		GdlRule rule = GdlPool.getRule(head, body);
		
		return rule;
	}
	
	public static GdlSentence parseSentence(String stringToParse){
		
		ParseTree parseTree = new ParseTree(stringToParse);
		
		return (GdlSentence)nodeToTerm(parseTree.getRoot(), true);
		
	}

	public static GdlTerm parseTerm(String stringToParse){
		
		ParseTree parseTree = new ParseTree(stringToParse);
		
		return (GdlTerm)nodeToTerm(parseTree.getRoot(), false);
	}
	
	
	private static Gdl nodeToTerm(ParseNode node, boolean expectingLiteral){
		
		if(node.getChildren() == null || node.getChildren().size() == 0){
			
			if(expectingLiteral){
				return GdlPool.getProposition(GdlPool.getConstant(node.getLabel()));
			}else if(node.getLabel().startsWith("?")){
				//TODO: THIS IS WRONG! In ASP variables start with a capital.
				return GdlPool.getVariable(node.getLabel());
			}else{
				return GdlPool.getConstant(node.getLabel());
			}
			
		}
		
		
		GdlTerm[] body = new GdlTerm[node.getChildren().size()];
		for (int i = 0; i < body.length; i++) {
			body[i] = (GdlTerm)nodeToTerm(node.getChildren().get(i), false);
		}
		
		
		
		if(expectingLiteral){
			
			String relationName = node.getLabel();
			boolean isNot = false;
			if(relationName.startsWith("not ")){
				relationName = relationName.replace("not ", "");
				isNot = true;
			}
			
			GdlRelation relation = GdlPool.getRelation(GdlPool.getConstant(relationName), body);
			
			if(isNot){
				
				GdlNot not = GdlPool.getNot(relation);
				return not;
				
			}else{
				return relation;
			}
			
			
		}else{
			return GdlPool.getFunction(GdlPool.getConstant(node.getLabel()), body);
		}
	}
	
}
