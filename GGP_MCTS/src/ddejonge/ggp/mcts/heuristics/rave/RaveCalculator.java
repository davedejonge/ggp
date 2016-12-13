package ddejonge.ggp.mcts.heuristics.rave;

import java.util.Arrays;

import ddejonge.ggp.mcts.MCTSParams;

public class RaveCalculator {
	
	
	final double k;
	
	double[] preCalculatedValues = new double[10000];
	
	
	public RaveCalculator(MCTSParams params){
		this.k = params.RAVE_EQUIVALENCE_PARAMETER;
		
		Arrays.fill(preCalculatedValues, -1.0);
	}
	
	public double getRaveParameter(int visitCount){
		
		
		//if the array turns out to be too short, we make a copy with double the length.
		if(visitCount >= preCalculatedValues.length){
			preCalculatedValues = Arrays.copyOf(preCalculatedValues, 2*preCalculatedValues.length);
		}
		
		if(preCalculatedValues[visitCount] < 0.0){
			preCalculatedValues[visitCount] = Math.sqrt(k/ (3 * visitCount + k));
		}
		
		return preCalculatedValues[visitCount];
		
	}
	

}
