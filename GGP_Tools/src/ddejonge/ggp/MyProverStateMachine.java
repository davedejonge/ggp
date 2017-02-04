package ddejonge.ggp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.prover.Prover;
import org.ggp.base.util.prover.aima.AimaProver;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;
import org.ggp.base.util.statemachine.implementation.prover.result.ProverResultParser;


/**
 * This is a copy of the ProverStateMachine, with the only difference that it the method
 * getGoal() returns 0 when the goal is not defined.
 * 
 * 
 * @author dave
 *
 */
public class MyProverStateMachine extends StateMachine {

	private MachineState initialState;
	private Prover prover;
	private List<Role> roles;

	/**
	 * Initialize must be called before using the StateMachine
	 */
	public MyProverStateMachine()
	{

	}

	@Override
	public void initialize(List<Gdl> description)
	{
		prover = new AimaProver(description);
		roles = computeRoles(description);
		initialState = computeInitialState();
	}

    /**
     * This code is adapted by Dave de Jonge, from the code in class Role.
     * The difference is that this method also works if the roles are encoded as GdlRule objects.
     * 
     * Compute all of the roles in a game, in the correct order.
     * <p>
     * Order matters, because a joint move is defined as an ordered list
     * of moves, in which the order determines which player took which of
     * the moves. This function will give an ordered list in which the roles
     * have that correct order.
     */
    public static List<Role> computeRoles(List<? extends Gdl> description)
    {
        List<Role> roles = new ArrayList<Role>();
        for (Gdl gdl : description) {
            
        	GdlSentence head;
        	
        	if (gdl instanceof GdlRelation) {
                
                head = (GdlRelation) gdl;
                
            }else if(gdl instanceof GdlRule){
            	
            	GdlRule rule = (GdlRule)gdl;
            	head = rule.getHead();
            	
            }else{
            	throw new RuntimeException("MyProverStateMachine.computeRoles() Error! " + gdl.getClass().getName());
            }
            
            
            if (head.getName().getValue().equals("role")) {
                roles.add(new Role((GdlConstant) head.get(0)));
            }
        }
        return roles;
    }
	
	
	private MachineState computeInitialState()
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getInitQuery(), new HashSet<GdlSentence>());
		return new ProverResultParser().toState(results);
	}

	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getGoalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() != 1)
		{
			return 0;
			
			/*
		    GamerLogger.logError("StateMachine", "Got goal results of size: " + results.size() + " when expecting size one.");
			throw new GoalDefinitionException(state, role);
			*/
		}

		try
		{
			GdlRelation relation = (GdlRelation) results.iterator().next();
			GdlConstant constant = (GdlConstant) relation.get(1);

			return Integer.parseInt(constant.toString());
		}
		catch (Exception e)
		{
			throw new GoalDefinitionException(state, role);
		}
	}

	@Override
	public MachineState getInitialState()
	{
		return initialState;
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role) throws MoveDefinitionException
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getLegalQuery(role), ProverQueryBuilder.getContext(state));

		if (results.size() == 0)
		{
			throw new MoveDefinitionException(state, role);
		}

		return new ProverResultParser().toMoves(results);
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves) throws TransitionDefinitionException
	{
		Set<GdlSentence> results = prover.askAll(ProverQueryBuilder.getNextQuery(), ProverQueryBuilder.getContext(state, getRoles(), moves));

		for (GdlSentence sentence : results)
		{
			if (!sentence.isGround())
			{
				throw new TransitionDefinitionException(state, moves);
			}
		}

		return new ProverResultParser().toState(results);
	}

	@Override
	public List<Role> getRoles()
	{
		return roles;
	}

	@Override
	public boolean isTerminal(MachineState state)
	{
		return prover.prove(ProverQueryBuilder.getTerminalQuery(), ProverQueryBuilder.getContext(state));
	}
	
	
	
	
	
	
	
	
}
