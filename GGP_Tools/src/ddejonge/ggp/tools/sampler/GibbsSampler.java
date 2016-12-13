package ddejonge.ggp.tools.sampler;

import java.util.Arrays;

public class GibbsSampler extends RandomSampler{

	
	double weights[];
	double maxWeight;
	final double maxUnnormalizedProbability;
	final double inverseTemperature;
	
	//table with pre-calculated values. NOTE: Assumes maxUnnormalizedProbability is never changed!
	double[] gibbsValues = new double[101];
	
	
	public GibbsSampler(double maxUnnormalizedProbability, double maxWeight){
		
		Arrays.fill(gibbsValues, -1.0);
		
		this.maxUnnormalizedProbability = maxUnnormalizedProbability;
		this.maxWeight = maxWeight;
		
		//e^100t = 3.0
		this.inverseTemperature = Math.log(maxUnnormalizedProbability) / maxWeight;
	}
	
	public void setWeights(double[] weights){
		this.weights = weights;
	}
	
	
	@Override
	public int getRandomIndex(int maxIndex){
		
		double[] unnormalizedProbs = new double[weights.length];
		double sum = 0.0;
		
		for (int i = 0; i < weights.length; i++) {
			unnormalizedProbs[i] = getUnnormalizedGibbsProbability(weights[i]);
			sum += unnormalizedProbs[i];
		}
		
		return getRandomIndex(unnormalizedProbs, sum);
	}
	
	/**
	 * Returns 1.0 if the given weight is 0.0
	 * Returns maxUnnormalizedProbability  if the given weight is maxWeight
	 * 
	 * @param weight A value between 0.0 and maxWeight
	 * @param maxWeight
	 * @param maxUnnormalizedProbability
	 * @return
	 */
	double getUnnormalizedGibbsProbability(double weight){
		
		
		int index = (int)Math.floor(weight);
		if( gibbsValues[index] < 0.0){
			gibbsValues[index] = Math.exp(this.inverseTemperature * weight);
		}
		
		
		return gibbsValues[index];
	}

}
