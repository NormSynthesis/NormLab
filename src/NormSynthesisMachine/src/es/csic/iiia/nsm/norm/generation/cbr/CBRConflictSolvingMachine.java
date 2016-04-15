package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.sensing.View;
import es.csic.iiia.nsm.sensing.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * A norm generation machine that uses Case Based Reasoning (CBR) to generates
 * norms from conflicts. Each generated norm is aimed at regulating a given
 * conflict, trying to prevent it from arising again in the future
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Goal
 * @see Conflict
 */
public class CBRConflictSolvingMachine {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private Random random;
	private CaseBase caseBase;									// the case base

	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param nsm the norm synthesis machine
	 * @param normReasoner 
	 */
	public CBRConflictSolvingMachine(NormativeNetwork normativeNetwork, 
			NormReasoner normReasoner, NormSynthesisStrategy strategy,
			Random random, NormSynthesisMetrics nsMetrics) {

		this.random = random;
		this.caseBase = new CaseBase();
	}


	public List<Long> getConflictSource(Conflict conflict) {
		CaseDescription cDesc = new CaseDescription(conflict);
		Case cse = caseBase.searchForSimilarCase(cDesc);
		CaseSolution sol;

		/* No similar case has been found -> Solve case by generating
		 * a random solution*/
		if(cse != null) {
			sol = this.adaptBestSolution(cse, conflict);
		}
		/* A similar case has been found -> Adapt the best solution
		 * of the found similar case*/
		else {
			sol = this.solveCase(cDesc);
		}
		return sol.getBlamedAgentsIds();
	}


	/**
	 * Creates a new random {@code CaseSolution} to solve the conflict
	 * described in the {@code CaseDescription}. The reasoning employed
	 * to generate the random solution is based on the principle:
	 * if we prohibit some of the actions that the conflicting agents
	 * performed in the situation previous to the conflict, then maybe
	 * we can avoid the conflict to happen again.
	 * Therefore, the random solution generation works as follows:
	 * <ol>
	 * <li>	it retrieves the conflicting view, namely the {@code View}
	 * 			that contains the conflict;
	 * <li>	it chooses an agent that is in conflict in the conflicting
	 * 			{@code View};
	 * <li>	it retrieves the conflict source, namely the {@code View}
	 * 			that describes the situation previous to the conflict;
	 * <li>	it retrieves the action that the chosen agent performed 
	 * 			in the transition from the conflict source to the
	 * 			conflicting {@code View}; 
	 * <li>	it generates a {@code CaseSolution} that prohibits the
	 * 			agent to perform the retrieved action in the situation
	 * 			described by the conflict source {@code View}; and 
	 * <li>	since agents cannot understand case solutions, the generated
	 * 			{@code CaseSolution} is translated to norms that will be
	 * 			given to the agents.
	 * 
	 * @param 	cDesc the {@code CaseDescription} that describes
	 * 					the conflicting situation
	 * @param 	dmFunctions the domain functions, which are used to 
	 * 					translate case solutions to norms
	 * @param 	goal the goal with respect to the conflict has been detected
	 * @return the new {@code CaseSolution} for the case description
	 */
	private CaseSolution solveCase(CaseDescription cDesc) {
		ViewTransition conflictSource = cDesc.getConflictSource();
		View sourceView = conflictSource.getView(-1);
		CaseSolution sol = new CaseSolution();
		
		/* Randomly choose the agent responsible for the conflict */
		List<Long> conflictingAgents = cDesc.getConflictingAgents();
		List<Long> blamedAgents = new ArrayList<Long>();
		blamedAgents.add(conflictingAgents.get(
				random.nextInt(conflictingAgents.size())));

		/* For each responsible agent, generate a norm */
		for(Long agentId : blamedAgents) {

			/* Check if the agent existed in the scenario in the
			 * previous tick (not new entry) */
			boolean agentExistedInPreviousTick =
					sourceView.getAgentIds().contains(agentId);

			if(agentExistedInPreviousTick) {
				sol.addBlamedAgent(sourceView, agentId);
			}
		}
		return sol;	
	}

	/**
	 * Adapts the best solution of a given case, to solve a given {@code conflict}
	 * 
	 * @param cse the case from which to adapt its best solution
	 * @param conflict the conflict to solve
	 * @return a new {@code CaseSolution} aimed to solve
	 * 					the given {@code conflict}
	 */
	private CaseSolution adaptBestSolution(Case cse, Conflict conflict) {
		//		List<Norm> norms = new ArrayList<Norm>();

		// TODO
		return null;
	}

}
