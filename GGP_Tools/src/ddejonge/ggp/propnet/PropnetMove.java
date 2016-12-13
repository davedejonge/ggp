package ddejonge.ggp.propnet;

import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.Move;



public class PropnetMove extends Move {
	
	private transient jProposition propnetComponent;

	public PropnetMove(jProposition propnetComponent) {
		
		//the given proponetComponent must be of type DOES.
		//does(xplayer, mark(1,1,x))
		
		super(((GdlRelation)propnetComponent.gdlSentence).get(1));
		
		this.propnetComponent = propnetComponent;
	}

	public jProposition getPropnetComponent() {
		return propnetComponent;
	}
	
	/*
	public String toString(){
		return super.toString() + " " + propnetComponent;
	}*/
}
