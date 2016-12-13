package ddejonge.ggp.sat.logic;

import java.util.ArrayList;
import java.util.List;

public class XOR extends ArrayList<Proposition> implements SATFormula {

	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS
	public XOR(List<Proposition> propositions){
		super(propositions);
	}
	
	//METHODS

	//GETTERS AND SETTERS
	
	public String toString(){
		return "XOR(" + this.toString() + ")";
	}
}
