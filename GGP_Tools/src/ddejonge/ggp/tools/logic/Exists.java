package ddejonge.ggp.tools.logic;

import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlVariable;

public class Exists extends GdlLiteral{

	GdlLiteral body;
	List<GdlVariable> variables;

	public Exists(List<GdlVariable> variables, GdlLiteral body){
		this.variables = variables;
		this.body = body;
	}
	
	
	@Override
	public boolean isGround() {
		return body.isGround();
	}

	@Override
	public String toString() {
		
		String s= "E" + variables.toString() + "(";
		s += body.toString();
		s +=")";
		
		return s;
	}
	
}
