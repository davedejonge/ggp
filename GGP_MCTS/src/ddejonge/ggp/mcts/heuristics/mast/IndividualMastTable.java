package ddejonge.ggp.mcts.heuristics.mast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.util.statemachine.Move;

import ddejonge.ggp.mcts.MCTSParams;
import ddejonge.ggp.tools.AverageValueCalculator;
import ddejonge.ggp.tools.sampler.GibbsSampler;
import ddejonge.ggp.tools.sampler.RandomSampler;
import ddejonge.ggp.tools.sampler.UniformSampler;

class IndividualMastTable {
	
	//PARAMETERS
	HashMap<Move, AverageValueCalculator> action2value = new HashMap<Move, AverageValueCalculator>(1000);
	
	
	void update(Move action, double newValue){
		
		AverageValueCalculator avc = action2value.get(action);
		
		if(avc == null){
			avc = new AverageValueCalculator();
			action2value.put(action, avc);
		}
		
		avc.update(newValue);
		
	}
	
	
	public String toString(){
		
		StringBuilder stringBuilder = new StringBuilder();
		
		
		List<Move> orderedMoves = new ArrayList<Move>();
		for(Move move : this.action2value.keySet()){
			
			if(orderedMoves.size() == 0){
				orderedMoves.add(move);
			}else{
				
				boolean inserted = false;
				
				for (int i = 0; i < orderedMoves.size(); i++) {
					
					double moveToInsertValue = this.action2value.get(move).getAverage();
					
					double moveAtIndexValue = this.action2value.get(orderedMoves.get(i)).getAverage();
					
					if(moveToInsertValue > moveAtIndexValue){
						orderedMoves.add(i, move);
						inserted = true;
						break;
					}
				}
				
				if(!inserted){
					orderedMoves.add(move);
				}
				
			}
			
			
			
		}
		for(Move move : orderedMoves){
			
			AverageValueCalculator avc = this.action2value.get(move);
			
			stringBuilder.append(move);
			stringBuilder.append(": ");
			stringBuilder.append(avc.getAverage());
			stringBuilder.append(System.lineSeparator());
			
		}
		
		return stringBuilder.toString();
	}

	
}
