package ddejonge.ggp.mcts2;

import java.util.Arrays;

import ddejonge.ggp.tools.Utils;

class MCTS_Utils {

	//STATIC FIELDS

	//STATIC METHODS
	public static void applyZeroSumAssumption(int[] goals, int myRoleIndex, int maxUtility) {
		
		int myGoal = goals[myRoleIndex];
		int opponentGoal = maxUtility - myGoal;
		
		Arrays.fill(goals, opponentGoal);
		goals[myRoleIndex] = myGoal;
	}
	
	public static void applyZeroSumAssumption(float[] goals, int myRoleIndex, int maxUtility) {
		
		float myGoal = goals[myRoleIndex];
		float opponentGoal = maxUtility - myGoal;
		
		Arrays.fill(goals, opponentGoal);
		goals[myRoleIndex] = myGoal;
	}
	
	public static String getUtilityString(float[] utilities, boolean opponentUtiltiyVisible, int myRoleIndex) {
		
		StringBuilder sb = new StringBuilder(); 
				
		sb.append("[");
		for(int i=0; i<utilities.length; i++) {
			
			
			if(i != 0) {
				sb.append(",");
			}
			
			if(i != myRoleIndex && !opponentUtiltiyVisible) {
				sb.append("?");
			}else {
				
				sb.append(Math.round(Utils.round(utilities[i], 0)));
			}
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	//FIELDS

	//CONSTRUCTORS

	//METHODS

	//GETTERS AND SETTERS
}
