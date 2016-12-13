package ddejonge.ggp.asp;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.*;


public class GDL2ASPConverter {
	
	/**
	 * Converts all rules in the given list into ASP rules. <br/>
	 * Each of the returned string will end with a period.
	 * 
	 * @param rules
	 * @return
	 */
	public static List<String> toAspStrings(List<Gdl> rules){
		
		ArrayList<String> aspStrings = new ArrayList<String>(rules.size());
		
		for(Gdl rule : rules){
			
			String aspString = toAspString(rule);
			
			if(!aspString.endsWith(".")){
				aspString += ".";
			}
			
			aspStrings.add(aspString);
		}
		
		return aspStrings;
	}
	
	
	
	
	public static String toAspString(Gdl gdl){
		
		if(gdl instanceof GdlRule){
			GdlRule rule = (GdlRule)gdl;
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(GDL2ASPConverter.toAspString(rule.getHead()));
			sb.append(" :- ");
			sb.append(GDL2ASPConverter.toAspString(rule.getBody()));
			sb.append(".");
			
			return sb.toString();
			
		}else if(gdl instanceof GdlOr){
			GdlOr or = (GdlOr) gdl;
			
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean first = true;
			for (int i = 0; i < or.arity(); i++) {
				if(!first){
					sb.append("; ");
				}
				sb.append(toAspString(or.get(i)));
				first = false;
			}
			sb.append("}");

			return sb.toString();
			
		}else if(gdl instanceof GdlNot){
			GdlNot not = (GdlNot) gdl;
			
			StringBuilder sb = new StringBuilder();
			sb.append("not ");
			sb.append(toAspString(not.getBody()));
			return sb.toString();
			
		}else if(gdl instanceof GdlProposition){
			GdlProposition proposition = (GdlProposition) gdl;
			
			return proposition.toString();
			
		}else if(gdl instanceof GdlRelation){
			GdlRelation relation = (GdlRelation) gdl;
			
			StringBuilder sb = new StringBuilder();
			sb.append(relation.getName().toString());
			sb.append("(");
			boolean first = true;
			for(GdlTerm term : relation.getBody()){
				if(!first){
					sb.append(",");
				}
				sb.append(toAspString(term));
				first = false;
			}
			sb.append(")");
			
			return sb.toString();
			
		}else if(gdl instanceof GdlDistinct){
			GdlDistinct distinct = (GdlDistinct) gdl;
			
			StringBuilder sb = new StringBuilder();
			sb.append(toAspString(distinct.getArg1()));
			sb.append(" != ");
			sb.append(toAspString(distinct.getArg2()));

			return sb.toString();
			
		}else if(gdl instanceof GdlFunction){
			GdlFunction function = (GdlFunction)gdl;
			
			StringBuilder sb = new StringBuilder();
			sb.append(function.getName().toString());
			sb.append("(");
			boolean first = true;
			for(GdlTerm term : function.getBody()){
				if(!first){
					sb.append(",");
				}
				sb.append(toAspString(term));
				first = false;
				
			}
			sb.append(")");
			
			return sb.toString();
			
			
		}else if(gdl instanceof GdlConstant){ 
			
			//Make sure the first character is lower case.
			String s= gdl.toString();
			
			char firstChar = s.charAt(0);
			if(Character.isUpperCase(firstChar)){
				firstChar = Character.toLowerCase(firstChar);
				s = firstChar + s.substring(1);
			}
			
			return s;
			
		}else if(gdl instanceof GdlVariable){ 
			
			String s= gdl.toString();
			
			if(s.startsWith("?")){
				s = s.substring(1);
			}
			
			
			//Make sure the first character is upper case.
			char firstChar = s.charAt(0);
			if(Character.isLowerCase(firstChar)){
				firstChar = Character.toUpperCase(firstChar);
				s = firstChar + s.substring(1);
			}
			
			return s;
			
		}else{
			throw new RuntimeException("GdlRule2AspString.toAspString() Error! " + gdl.getClass().getName());
		}
	}
	
	
	public static String toAspString(List<GdlLiteral> ruleBody){
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i = 0; i < ruleBody.size(); i++) {
			if(!first){
				sb.append(", ");
			}
			sb.append(toAspString(ruleBody.get(i)));
			first = false;
		}

		return sb.toString();
		
	}
	
	
	public static String getSequenceString(List<? extends Object> objects){
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;  i<objects.size(); i++){
			
			Object object = objects.get(i);
			if(i!=0){
				sb.append(", ");
			}
			if(object instanceof Gdl){
				sb.append(GDL2ASPConverter.toAspString((Gdl)object));
			}else{			
				sb.append(object.toString());
			}
		}
		
		return sb.toString();
	}
	

}
