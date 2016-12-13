package ddejonge.ggp.propnet;

public class jConstant extends jProposition{
	
	
	
	jConstant(boolean value){
		super(null);
		this.currentValue = value;
	}
	
	@Override
	void update(int index){
		throw new RuntimeException("jConstant.update() Error! Can't update a constant");
	}
	
	@Override
	int addInput(jComponent input){
		throw new RuntimeException("jConstant.addInput() Error! Can't add an input to a constant.");
	}
	
	@Override
	void setValue(boolean newValue) {
		throw new RuntimeException("jConstant.update() Error! Can't set the value of a constant");
	}
	
	@Override
	void calculateValue(){
		
	}

}
