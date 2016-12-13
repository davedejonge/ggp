package ddejonge.ggp.propnet;

import java.util.ArrayList;
import java.util.List;

public class jConnective extends jComponent{

	//Connectives must have at least 1 input, each of which can be either a Proposition or a Connective.
	//Connectives must have exactly 1 output, which can be either a Connective or a Proposition
	
	static int generated = 0;
	public int id;
	
	//FIELDS
	int type;
	
	private List<jComponent> inputs;
	private int numTrue;
	
	protected List<jComponent> outputs;
	protected List<Integer> indices;   //maps the index of the output to the index that this Proposition has as the input of that output.
	
	
	
	//CONSTRUCTOR
	public jConnective(int type){
		
		this.id = generated++;
		
		outputs = new ArrayList<jComponent>();
		indices = new ArrayList<Integer>();
		
		this.type = type;
		inputs = new ArrayList<jComponent>();
	}
	
	
	int addInput(jComponent input){
		this.inputs.add(input);
		
		this.calculateValue();
		
		return this.inputs.size()-1;
		
		
	}
	
	void addOutput(jComponent output, int index){
		
		if(output instanceof jConstant){
			throw new RuntimeException("jConnective.addOutput() Error! Constant cannot be an output");
		}
		
		outputs.add(output);
		indices.add(index);
		
		/*output.calculateValue();*/
	}
	
	
	//METHODS
	
	//This method is called when the value of the input with the given index has changed.
	@Override
	void update(int inputIndex){
		
		boolean newInputValue = inputs.get(inputIndex).currentValue;
		
		if(newInputValue){
			numTrue++;
		}else{
			numTrue--;
		}
		
		boolean newOutputValue = false;
		
		
		if(this.type == AND){
			newOutputValue = (numTrue == inputs.size());
		}else if(this.type == OR){
			newOutputValue = (numTrue > 0);
		}else if(this.type == NOT){
			newOutputValue = !newInputValue;
		}else{
			throw new RuntimeException("Connective.update() Error! unknown type: " + this.type);
		}
		
		if(newOutputValue != currentValue){
			currentValue = newOutputValue;
			
			for (int i = 0; i < outputs.size(); i++) {
				
				jComponent output = outputs.get(i);
				output.update(indices.get(i));
			}
			
		}
		
		
	}
	
	@Override
	void calculateValue(){
		
		this.numTrue = 0;
		for(jComponent input : inputs){
			if(input.currentValue){
				numTrue++;
			}
		}
		
		boolean newOutputValue;
		if(this.type == AND){
			newOutputValue = (numTrue == inputs.size());
		}else if(this.type == OR){
			newOutputValue = (numTrue > 0);
		}else if(this.type == NOT){
			newOutputValue = (numTrue == 0);
		}else{
			throw new RuntimeException("Connective.update() Error! unknown type: " + this.type);
		}
		
		
		if(newOutputValue != currentValue){
			currentValue = newOutputValue;
			
			for (int i = 0; i < outputs.size(); i++) {
				
				jComponent output = outputs.get(i);
				output.update(indices.get(i));
			}
		}
	}
	
	public String toString(){
		
		if(this.type == AND){
			return "AND " + this.inputs;
		}else if(this.type == OR){
			return "OR " + this.inputs;
		}else if(this.type == NOT){
			return "NOT " + this.inputs;
		}else{
			return "unknown type: " + this.type;
		}
		
		
	}
	
	
	
}
