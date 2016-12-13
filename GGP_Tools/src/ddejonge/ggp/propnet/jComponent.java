package ddejonge.ggp.propnet;


public abstract class jComponent{

	final static int AND = 1;
	final static int OR = 2;
	final static int NOT = 3;
	
	/*
	final static int NEXT = 4;
	final static int LEGAL = 5;
	final static int TERMINAL = 6;
	final static int GOAL = 4;
	
	final static int DOES = 7;
	final static int TRUE = 8;
	final static int OTHER = 9;
	*/
	
	
	boolean currentValue = false;
	
	abstract void update(int index);
	
	
	abstract int addInput(jComponent input);
	
	abstract void addOutput(jComponent output, int index);
	
	abstract void calculateValue();
}
