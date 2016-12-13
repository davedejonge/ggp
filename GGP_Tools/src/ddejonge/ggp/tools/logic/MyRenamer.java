package ddejonge.ggp.tools.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlFunction;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlOr;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.gdl.grammar.GdlVariable;

public class MyRenamer {

	private int counter;
	Map<GdlVariable, GdlVariable> renamings;

	public MyRenamer(){
		counter = 0;
		this.renamings = new HashMap<GdlVariable, GdlVariable>();
	}
	
	
	public GdlLiteral rename(GdlLiteral formula){
		return renameLiteral(formula);
	}
	
	private GdlLiteral renameLiteral(GdlLiteral literal)
	{
		
		if(literal instanceof GdlAnd){
			return renameAnd((GdlAnd) literal);
		}
		else if (literal instanceof GdlOr)
		{
			return renameOr((GdlOr) literal);
		}
		if (literal instanceof GdlDistinct)
		{
			return renameDistinct((GdlDistinct) literal);
		}
		else if (literal instanceof GdlNot)
		{
			return renameNot((GdlNot) literal);
		}
		
		else if (literal instanceof GdlSentence)
		{
			return renameSentence((GdlSentence) literal);
		}else{
			throw new RuntimeException("MyRenamer.renameLiteral() Error! unknown class: " + literal.getClass().getName());
		}
	}
	
	private GdlLiteral renameAnd(GdlAnd and)
	{
		if (and.isGround())
		{
			return and;
		}
		else
		{
			ArrayList<GdlLiteral> conjunction = new ArrayList<GdlLiteral>(and.arity());
			for (int i = 0; i < and.arity(); i++)
			{
				conjunction.add(renameLiteral(and.get(i)));
			}

			return AndPool.getAnd(conjunction);
		}
	}
	
	private GdlLiteral renameOr(GdlOr or)
	{
		if (or.isGround())
		{
			return or;
		}
		else
		{
			ArrayList<GdlLiteral> disjunction = new ArrayList<GdlLiteral>(or.arity());
			for (int i = 0; i < or.arity(); i++)
			{
				disjunction.add(renameLiteral(or.get(i)));
			}

			return GdlPool.getOr(disjunction);
		}
	}
	

	private GdlConstant renameConstant(GdlConstant constant)
	{
		return constant;
	}

	private GdlDistinct renameDistinct(GdlDistinct distinct)
	{
		if (distinct.isGround())
		{
			return distinct;
		}
		else
		{
			GdlTerm arg1 = renameTerm(distinct.getArg1());
			GdlTerm arg2 = renameTerm(distinct.getArg2());

			return GdlPool.getDistinct(arg1, arg2);
		}
	}

	private GdlFunction renameFunction(GdlFunction function)
	{
		if (function.isGround())
		{
			return function;
		}
		else
		{
			GdlConstant name = renameConstant(function.getName());

			List<GdlTerm> body = new ArrayList<GdlTerm>();
			for (int i = 0; i < function.arity(); i++)
			{
				body.add(renameTerm(function.get(i)));
			}

			return GdlPool.getFunction(name, body);
		}
	}



	private GdlNot renameNot(GdlNot not)
	{
		if (not.isGround())
		{
			return not;
		}
		else
		{
			GdlLiteral body = renameLiteral(not.getBody());
			return GdlPool.getNot(body);
		}
	}



	private GdlProposition renameProposition(GdlProposition proposition)
	{
		return proposition;
	}

	private GdlRelation renameRelation(GdlRelation relation)
	{
		if (relation.isGround())
		{
			return relation;
		}
		else
		{
			GdlConstant name = renameConstant(relation.getName());

			List<GdlTerm> body = new ArrayList<GdlTerm>();
			for (int i = 0; i < relation.arity(); i++)
			{
				body.add(renameTerm(relation.get(i)));
			}

			return GdlPool.getRelation(name, body);
		}
	}



	private GdlSentence renameSentence(GdlSentence sentence)
	{
		if (sentence instanceof GdlProposition)
		{
			return renameProposition((GdlProposition) sentence);
		}
		else
		{
			return renameRelation((GdlRelation) sentence);
		}
	}

	private GdlTerm renameTerm(GdlTerm term)
	{
		if (term instanceof GdlConstant)
		{
			return renameConstant((GdlConstant) term);
		}
		else if (term instanceof GdlVariable)
		{
			return renameVariable((GdlVariable) term);
		}
		else
		{
			return renameFunction((GdlFunction) term);
		}
	}

	private GdlVariable renameVariable(GdlVariable variable)
	{
		if (!renamings.containsKey(variable))
		{
			GdlVariable newName = GdlPool.getVariable("?X" + (counter++));
			renamings.put(variable, newName);
		}

		return renamings.get(variable);
	}

}
