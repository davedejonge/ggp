package ddejonge.ggp.sat;

import java.util.List;

import org.ggp.base.util.gdl.grammar.*;

import ddejonge.ggp.sat.logic.Clause;
import ddejonge.ggp.sat.logic.Proposition;
import ddejonge.ggp.sat.logic.SATFormula;

public class GDL2SATConverter {

	
	public static Proposition toSAT(SATDescription satDescription, GdlSentence sentence){
		
		Proposition prop = satDescription.propositionStorage.getProposition(sentence);
		
		if(prop == null){
			throw new RuntimeException("SatDescription.gdlSentence2proposition() Error! " + sentence + " not found!");
		}
		
		return prop;
	}
	

	
	
	//STATIC FIELDS

	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
