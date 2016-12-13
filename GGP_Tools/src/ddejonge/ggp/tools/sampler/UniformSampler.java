package ddejonge.ggp.tools.sampler;

public class UniformSampler extends RandomSampler{

	@Override
	public int getRandomIndex(int maxIndex) {
		return this.random.nextInt(maxIndex);
	}

}
