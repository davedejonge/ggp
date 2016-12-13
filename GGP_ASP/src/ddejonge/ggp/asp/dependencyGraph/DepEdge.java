package ddejonge.ggp.asp.dependencyGraph;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;
import org.ggp.base.util.prover.aima.substitution.Substitution;

import ddejonge.ggp.tools.graph.Edge;


public class DepEdge extends Edge{

	List<GdlRule> rules;
	Substitution sub;
	
	String stringRep = null;
	
	
	public DepEdge(Node from, Node to) {
		super(from, to);
	}
	
	void setRule(GdlRule rule){
		if(this.rules == null){
			rules = new ArrayList<GdlRule>();
		}
		
		if(!rules.contains(rule)){
			rules.add(rule);
		}
	}
	
	void setSubstitution(Substitution sub){
		this.sub = sub;
	}
	
	
	public void cleanUp(){
		super.cleanUp();
		if(this.rules != null){
			this.rules.clear();
		}
		this.sub = null;
		this.stringRep = null;
	}
	
	@Override
	public String toString(){
		
		if(stringRep == null){

			StringBuilder sb = new StringBuilder();
			sb.append(getFrom().toString());
			sb.append(" ---> ");
			sb.append(getTo().toString());
			sb.append("  :  ");
			
			if(this.rules == null){
				sb.append(sub.toString());
			}else{
				sb.append(rules.toString());
			}
			
			stringRep = sb.toString();
		}
		
		return stringRep;
	}
	
	@Override
	public boolean equals(Object object){
		
		if( ! (object instanceof DepEdge)){
			return false;
		}
		
		DepEdge otherEdge = (DepEdge)object;
		
		if( ! this.getFrom().equals(otherEdge.getFrom())){
			return false;
		}
		
		if( ! this.getTo().equals(otherEdge.getTo())){
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode(){
		return this.getFrom().hashCode() ^ this.getTo().hashCode();
	}
	
}
