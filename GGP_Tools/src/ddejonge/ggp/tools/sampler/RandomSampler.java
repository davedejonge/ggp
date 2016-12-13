package ddejonge.ggp.tools.sampler;

import java.util.List;
import java.util.Random;

public abstract class RandomSampler {
	
	public final static int UNIFORM = 0;
	public final static int GIBBS = 1;
	
	
	Random random = new Random();

	public abstract int getRandomIndex(int bound);
	
	public Object getRandomObject(List<? extends Object> list){
		
		return list.get(getRandomIndex(list.size()));
		
	}
	
	
	/**
	 * Picks a random index, where each index has an unnormalized probability according to the given array.
	 * @param unnormalizedProbs
	 * @return
	 */
	public int getRandomIndex(double[] unnormalizedProbs){
		
		double sum = 0.0;
		for (int i = 0; i < unnormalizedProbs.length; i++) {
			sum += unnormalizedProbs[i];
		}
		
		return getRandomIndex(unnormalizedProbs, sum);
	}
	
	int getRandomIndex(double[] unnormalizedProbs, double sum){
		
		double r = random.nextDouble() * sum;
		
		double cumulative = 0.0;
		
		for (int i = 0; i < unnormalizedProbs.length; i++) {
			
			cumulative += unnormalizedProbs[i];
			
			if(r<cumulative){
				return i;
			}
			
		}
		
		//return the last index.
		return unnormalizedProbs.length-1;
	}
	
}
