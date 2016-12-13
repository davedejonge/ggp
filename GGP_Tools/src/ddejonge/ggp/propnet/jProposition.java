package ddejonge.ggp.propnet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlSentence;

public class jProposition extends jComponent {
	
	//Propositions can have at most 1 input, which must be a Connective
	//Propositions can have any number of outputs, which must be Connectives
	
	static int generated = 0;
	int id;
	
	//FIELDS
	GdlSentence gdlSentence;
	GdlConstant type;
	
	jComponent input; //Do we really need this field??
	
	protected List<jComponent> outputs;
	protected List<Integer> indices;  //maps the index of each output to the index that this Proposition has as the input of that output.
	
	
	int goalValue = -1; //only for GOAL Propositions.
	
	GdlSentence trueSentence = null; //only for NEXT Propositions. this is the same sentence, but with next replaced by true.
	jProposition correspondingBaseProposition = null;
	
	jProposition correspondingDoesProposition = null; //only for LEGAL propositions.
	
	
	boolean tempValue;
	
	//CONSTRUCTOR
	jProposition(GdlSentence gdlSentence){
		this.gdlSentence = gdlSentence;
		
		this.id = generated++;
		
		outputs = new ArrayList<jComponent>();
		indices = new ArrayList<Integer>();
		
		if(gdlSentence != null){ //this should only happen if this object is a jConstant.
			this.type = this.gdlSentence.getName();
			
			if(this.type.equals(GdlPool.GOAL)){
				goalValue = Integer.parseInt(gdlSentence.get(1).toString());
			}
			if(this.type.equals(GdlPool.NEXT)){
				trueSentence = GdlPool.getRelation(GdlPool.TRUE, this.gdlSentence.getBody());
			}
		}
	}
	
	
	//METHODS
	
	@Override
	void calculateValue(){
		this.setValue(input.currentValue);
	}
	
	void setValue(boolean newValue){
		
		if(newValue != currentValue){
			this.update(0);
		}
		
	}
	
	void setTempValue(boolean newTempValue){
		this.tempValue = newTempValue;
	}
	
	boolean getTempValue(){
		return tempValue;
	}
	
	void update(){
		this.update(0); // A jProposition can only have 1 input, so the index of the input to update is always 0.
	}
	
	//This method should only be called if the value of the input proposition has changed!
	@Override
	void update(int index){
		//the index parameter is not used since Proposition objects only have 1 input.
		
		this.currentValue = !this.currentValue;
		
		for (int i = 0; i < outputs.size(); i++) {
			
			jComponent output = outputs.get(i);
			output.update(indices.get(i));
		}
	}
	

	
	
	
	@Override
	int addInput(jComponent input){
		
		this.input = input;
		this.setValue(input.currentValue);
		
		return 0;
	}
	
	@Override
	void addOutput(jComponent output, int index){
		
		if(output instanceof jConstant){
			throw new RuntimeException("jProposition.addOutput() Error! Constant cannot be an output");
		}
		
		if(gdlSentence != null){ //can be null if this object is of type Constant.
			if(gdlSentence.getName().equals(GdlPool.NEXT) || /*gdlSentence.getName().equals(GdlPool.LEGAL) ||*/ gdlSentence.getName().equals(GdlPool.TERMINAL) || gdlSentence.getName().equals(GdlPool.GOAL)){
				throw new RuntimeException("Proposition.addOutput() Error! this type of proposition cannot have an output. " + gdlSentence.getName());
			}
		}
		
		outputs.add(output);
		indices.add(index);
	}
	
	

	
	public String toString(){
		return this.gdlSentence.toString() + " " + this.currentValue;
	}
	
}
