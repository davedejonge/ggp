package ddejonge.ggp.tools.logic.unification;

import java.util.ArrayList;
import java.util.List;

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


public class MySubstituter {

	public static GdlLiteral substitute(GdlLiteral literal, MySubstitution theta)
	{
		return substituteLiteral(literal, theta);
	}

	public static GdlSentence substitute(GdlSentence sentence, MySubstitution theta)
	{
		return substituteSentence(sentence, theta);
	}

	public static GdlRule substitute(GdlRule rule, MySubstitution theta)
	{
		return substituteRule(rule, theta);
	}

	private static GdlConstant substituteConstant(GdlConstant constant, MySubstitution theta)
	{
		return constant;
	}

	private static GdlDistinct substituteDistinct(GdlDistinct distinct, MySubstitution theta)
	{
		if (distinct.isGround())
		{
			return distinct;
		}
		else
		{
			GdlTerm arg1 = substituteTerm(distinct.getArg1(), theta);
			GdlTerm arg2 = substituteTerm(distinct.getArg2(), theta);

			return GdlPool.getDistinct(arg1, arg2);
		}
	}

	private static GdlFunction substituteFunction(GdlFunction function, MySubstitution theta)
	{
		if (function.isGround())
		{
			return function;
		}
		else
		{
			GdlConstant name = substituteConstant(function.getName(), theta);

			List<GdlTerm> body = new ArrayList<GdlTerm>();
			for (int i = 0; i < function.arity(); i++)
			{
				body.add(substituteTerm(function.get(i), theta));
			}

			return GdlPool.getFunction(name, body);
		}
	}

	private static GdlLiteral substituteLiteral(GdlLiteral literal, MySubstitution theta)
	{
		if (literal instanceof GdlDistinct)
		{
			return substituteDistinct((GdlDistinct) literal, theta);
		}
		else if (literal instanceof GdlNot)
		{
			return substituteNot((GdlNot) literal, theta);
		}
		else if (literal instanceof GdlOr)
		{
			return substituteOr((GdlOr) literal, theta);
		}
		else
		{
			return substituteSentence((GdlSentence) literal, theta);
		}
	}

	private static GdlNot substituteNot(GdlNot not, MySubstitution theta)
	{
		if (not.isGround())
		{
			return not;
		}
		else
		{
			GdlLiteral body = substituteLiteral(not.getBody(), theta);
			return GdlPool.getNot(body);
		}
	}

	private static GdlOr substituteOr(GdlOr or, MySubstitution theta)
	{
		if (or.isGround())
		{
			return or;
		}
		else
		{
			List<GdlLiteral> disjuncts = new ArrayList<GdlLiteral>();
			for (int i = 0; i < or.arity(); i++)
			{
				disjuncts.add(substituteLiteral(or.get(i), theta));
			}

			return GdlPool.getOr(disjuncts);
		}
	}

	private static GdlProposition substituteProposition(GdlProposition proposition, MySubstitution theta)
	{
		return proposition;
	}

	private static GdlRelation substituteRelation(GdlRelation relation, MySubstitution theta)
	{
		if (relation.isGround())
		{
			return relation;
		}
		else
		{
			GdlConstant name = substituteConstant(relation.getName(), theta);

			List<GdlTerm> body = new ArrayList<GdlTerm>();
			for (int i = 0; i < relation.arity(); i++)
			{
				body.add(substituteTerm(relation.get(i), theta));
			}

			return GdlPool.getRelation(name, body);
		}
	}

	private static GdlSentence substituteSentence(GdlSentence sentence, MySubstitution theta)
	{
		if (sentence instanceof GdlProposition)
		{
			return substituteProposition((GdlProposition) sentence, theta);
		}
		else
		{
			return substituteRelation((GdlRelation) sentence, theta);
		}
	}

	private static GdlTerm substituteTerm(GdlTerm term, MySubstitution theta)
	{
		if (term instanceof GdlConstant)
		{
			return substituteConstant((GdlConstant) term, theta);
		}
		else if (term instanceof GdlVariable)
		{
			return substituteVariable((GdlVariable) term, theta);
		}
		else
		{
			return substituteFunction((GdlFunction) term, theta);
		}
	}

	private static GdlTerm substituteVariable(GdlVariable variable, MySubstitution theta)
	{
		if (!theta.contains(variable))
		{
			return variable;
		}
		else
		{
			GdlTerm result = theta.get(variable);
			GdlTerm betterResult = null;

			while (!(betterResult = substituteTerm(result, theta)).equals(result))
			{
				result = betterResult;
			}

			theta.put(variable, result);
			return result;
		}
	}

	private static GdlRule substituteRule(GdlRule rule, MySubstitution theta)
	{
		GdlSentence head = substitute(rule.getHead(), theta);

		List<GdlLiteral> body = new ArrayList<GdlLiteral>();
		for ( GdlLiteral literal : rule.getBody() )
		{
			body.add(substituteLiteral(literal, theta));
		}

		return GdlPool.getRule(head, body);
	}
}
