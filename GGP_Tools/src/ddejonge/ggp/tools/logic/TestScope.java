package ddejonge.ggp.tools.logic;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;

public class TestScope {

	public static void main(String[] args) {

		//Generate a formula
		// p(x) ^ not q(y)
		
		GdlConstant p = GdlPool.getConstant("p");
		GdlConstant q = GdlPool.getConstant("q");
		GdlVariable x = GdlPool.getVariable("x");
		GdlVariable y = GdlPool.getVariable("y");
		
		GdlRelation p_x = GdlPool.getRelation(p,new GdlTerm[] {x});
		GdlRelation q_y = GdlPool.getRelation(q,new GdlTerm[] {x});
		
		GdlNot nq_y = GdlPool.getNot(q_y);
		
		GdlAnd phi = AndPool.getAnd(new GdlLiteral[]{p_x, nq_y});
		
		GdlLiteral scope_x = LogicUtils.getScope(phi, x);
		GdlLiteral scope_y = LogicUtils.getScope(phi, y);
		
		System.out.println("Scope x: " + scope_x);
		System.out.println("Scope y: " + scope_y);
	}

}
