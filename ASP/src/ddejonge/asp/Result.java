package ddejonge.asp;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.MediaSize.Other;

public class Result {
	
	public String numModels;
	
	/**
	 * If Clingo finishes correctly, this value will be set to 'true' or 'false'. However, if Clingo fails because of some error, this value will remain null.
	 */
	public Boolean satisfiable = null; 
	
	public List<String> solutions = new ArrayList<String>();
	public Integer optimalValue = null;
	
	
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		if(optimalValue != null){
			sb.append("Optimal value: " + optimalValue);
			sb.append(System.lineSeparator());
		}else{
			sb.append("Satisfiable: " + this.satisfiable);
			sb.append(System.lineSeparator());
		}
		
		

		/*sb.append("Number of Models: " + this.numModels);
		sb.append(System.lineSeparator());*/
		
		for(String solution : solutions){
			sb.append(solution);
			sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}
	

}
