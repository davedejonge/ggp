package ddejonge.ggp.tools;

public class AverageValueCalculator {
	
	private  double sum = 0.0;
	private int counter = 0;
	
	private double average = 0.0;
	
	public void update(double newValue){
		sum += newValue;
		counter++;
		
		average = sum / counter;
	}
	
	public double getAverage(){
		return average;
	}

}
