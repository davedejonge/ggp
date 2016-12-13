package ddejonge.ggp.tools.logic;

import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlLiteral;

public class GdlAnd extends GdlLiteral {

	private final List<GdlLiteral> conjuncts;
	private transient Boolean ground;

	public GdlAnd(List<GdlLiteral> conjuncts){
		this.conjuncts = conjuncts;
		ground = null;
	}
	

	public int arity()
	{
		return conjuncts.size();
	}

	private boolean computeGround()
	{
		for (GdlLiteral literal : conjuncts)
		{
			if (!literal.isGround())
			{
				return false;
			}
		}

		return true;
	}

	public GdlLiteral get(int index)
	{
		return conjuncts.get(index);
	}

	@Override
	public boolean isGround()
	{
		if (ground == null)
		{
			ground = computeGround();
		}

		return ground;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("( and ");
		for (GdlLiteral literal : conjuncts)
		{
			sb.append(literal + " ");
		}
		sb.append(")");

		return sb.toString();
	}


}
