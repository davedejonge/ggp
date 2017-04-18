package ddejonge.ggp.tools.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;

public class AtomExtractor {

	//STATIC FIELDS

	//STATIC METHODS
	public static Set<GdlSentence> extractAtoms(List<Gdl> description){
		
		Set<GdlSentence> setToFill = new HashSet<GdlSentence>();
		
		extractAtoms(description, setToFill);
		
		return setToFill;
	}
	
	public static Set<GdlSentence> extractAtoms(GdlRule rule){
		
		Set<GdlSentence> setToFill = new HashSet<GdlSentence>();
		
		extractAtoms(rule, setToFill);
		
		return setToFill;
	}
	
	
	public static Set<GdlSentence> extractAtoms(GdlLiteral formula){
		
		Set<GdlSentence> setToFill = new HashSet<GdlSentence>();
		
		extractAtoms(formula, setToFill);
		
		return setToFill;
	}
	
	
	
	static void extractAtoms(List<Gdl> rules, Set<GdlSentence> setToFill){
		
		for(Gdl gdl : rules){
			if(gdl instanceof GdlRule){
				extractAtoms((GdlRule)gdl, setToFill);
			}else if(gdl instanceof GdlSentence){
				extractAtoms((GdlSentence)gdl, setToFill);
			}else{
				throw new RuntimeException("AtomExtractor.extractAtoms() Error! unhandled class: " + gdl.getClass().getName());
			}
		}
		
		
	}
	
	
	static void extractAtoms(GdlRule rule, Set<GdlSentence> setToFill){
		
		GdlSentence head = rule.getHead();
		extractAtoms(head, setToFill);

		for(GdlLiteral bodyElement : rule.getBody()){
			extractAtoms(bodyElement, setToFill);
		}
		
	}
	
	
	static void extractAtoms(GdlLiteral literal, Set<GdlSentence> setToFill){
		
		
		if(literal instanceof GdlAnd){
			GdlAnd and = (GdlAnd) literal;
			
			for (int i = 0; i < and.arity(); i++) {
				extractAtoms(and.get(i), setToFill);
			}
			
		}else if(literal instanceof GdlOr){
			GdlOr or = (GdlOr) literal;
			
			for (int i = 0; i < or.arity(); i++) {
				extractAtoms(or.get(i), setToFill);
			}
			
		}else if(literal instanceof GdlNot){
			GdlNot not = (GdlNot) literal;
			
			extractAtoms(not.getBody(), setToFill);
			
		}else if(literal instanceof GdlSentence){
			GdlSentence sentence = (GdlSentence) literal;
			
			setToFill.add(sentence);
			
		}else if(literal instanceof GdlDistinct){
			
			//ignore this.
			
		}else{
			throw new RuntimeException("AtomExtractor.extractAtoms() Error! " + literal.getClass().getName());
		}	
		
		
	}

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
