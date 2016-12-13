package ddejonge.ggp.mcts.heuristics.mast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

import ddejonge.ggp.mcts.MCTSParams;
import ddejonge.ggp.mcts.heuristics.MoveCollectorImpl;
import ddejonge.ggp.propnet.heuristics.Heuristics;
import ddejonge.ggp.propnet.heuristics.MoveCollector;
import ddejonge.ggp.tools.AverageValueCalculator;
import ddejonge.ggp.tools.sampler.GibbsSampler;
import ddejonge.ggp.tools.sampler.RandomSampler;
import ddejonge.ggp.tools.sampler.UniformSampler;

public class MastTable extends Heuristics{


	
	ArrayList<IndividualMastTable> individualTables;
	
	final int samplingMethod;
	RandomSampler sampler;
	int numPlayers;
	List<Role> roles;
	
	public MastTable(List<Role> roles, MCTSParams params){
		
		this.roles = roles;
		this.numPlayers = roles.size();
		
		individualTables = new ArrayList<IndividualMastTable>(numPlayers);
		
		for (int i = 0; i <numPlayers; i++) {
			individualTables.add(new IndividualMastTable());
		}
		
		this.samplingMethod = params.MAST_SAMPLING_METHOD;
		
		if(this.samplingMethod == RandomSampler.GIBBS){
			
			sampler = new GibbsSampler(params.MAST_MAX_UNNORMALIZED_PROB, 100.0);
		
		}else if(this.samplingMethod == RandomSampler.UNIFORM){
			
			sampler = new UniformSampler();
		
		}else{
			throw new RuntimeException("IndividualMastTable() Error! unknown sampling method: " + this.samplingMethod);
		}
		
	}
	
	
	@Override
	public Object updateAfterRollOut(MoveCollector selectedMoves, MachineState terminalState, int[] goals) {
		
		for (int roleIndex = 0; roleIndex < numPlayers; roleIndex++) {
			
			for(Move move : selectedMoves.getMoves(roleIndex)){
				this.update(roleIndex, move, goals[roleIndex]);
			}
		}
		
		return null;
	}
	
	public void update(int roleIndex, Move action, int newValue){
		individualTables.get(roleIndex).update(action, newValue);
	}

	
	@Override
	public int getRandomMoveIndexInRollOut(int roleIndex, List<? extends Object> legalMoves){
		
		IndividualMastTable table = individualTables.get(roleIndex);
		
		if(this.sampler instanceof GibbsSampler){
			
			double[] weights = new double[legalMoves.size()];
			
			for (int i = 0; i< weights.length; i++) {
				
				Move move = (Move)legalMoves.get(i);
				
				AverageValueCalculator avc = table.action2value.get(move);
				
				if(avc == null){
					return i;
				}
				
				weights[i] = avc.getAverage();
				
			}
			
			((GibbsSampler)sampler).setWeights(weights);
		}
		
		return this.sampler.getRandomIndex(legalMoves.size());
	}
	
	
	public String toString(){
		
		String s = "";
		
		for (int j = 0; j <individualTables.size(); j++) {
			s += roles.get(j).getName().toString() + System.lineSeparator();
			IndividualMastTable imt = individualTables.get(j);
			s += imt.toString() + System.lineSeparator();
		}
		
		return s;
	}



	
}
