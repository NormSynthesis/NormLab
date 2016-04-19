package es.csic.iiia.nsm.norm.generation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableToAgentContext;
import es.csic.iiia.nsm.norm.generation.cbr.CBRConflictSolvingMachine;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.sensing.Monitor;
import es.csic.iiia.nsm.sensing.View;
import es.csic.iiia.nsm.sensing.ViewTransition;
import es.csic.iiia.nsm.strategy.GenericOperators;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormGenerator {

	/**
	 * 
	 * @author Javier Morales (jmorales@iiia.csic.es)
	 *
	 */
	public enum Approach {
		Reactive, Deliberative
	}
	
	protected Monitor monitor;
	protected CBRConflictSolvingMachine cbrMachine;
	protected NormativeNetwork normativeNetwork;
	protected NormReasoner normReasoner;
	protected GenericOperators operators;
	protected NormSynthesisSettings nsmSettings;
	protected DomainFunctions dmFunctions;
	protected Set<Norm> createdNorms;
	protected NormSynthesisMetrics nsMetrics;
	protected Random random;
	protected NormGeneraliser.Approach nGeneralisationApproach;
	protected NormGenerator.Approach nGenerationApproach;
	
	/**
	 * Constructor
	 */
	public NormGenerator(NormSynthesisSettings nsmSettings, Monitor monitor, 
			NormReasoner normReasoner, DomainFunctions dmFunctions,
			GenericOperators operators, NormativeNetwork normativeNetwork,
			CBRConflictSolvingMachine cbrMachine, Random random,
			NormSynthesisMetrics nsMetrics, NormGeneraliser.Approach nGenApproach) {

		this.nsmSettings = nsmSettings;
		this.nsMetrics = nsMetrics;
		this.monitor = monitor;
		this.normativeNetwork = normativeNetwork;
		this.dmFunctions = dmFunctions;
		this.operators = operators;
		this.normReasoner = normReasoner;
		this.cbrMachine = cbrMachine;
		this.random = random;
		this.nGeneralisationApproach = nGenApproach;
		this.createdNorms = new HashSet<Norm>();
		
		this.nGenerationApproach = nsmSettings.getNormGenerationApproach();
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> step(List<ViewTransition> viewTransitions,
			Map<Goal,List<Conflict>> conflicts) {
		
		List<Norm> activatedNorms = new ArrayList<Norm>();
		
		/* Collect perceptions from the MAS */
		this.getMASPerceptions(viewTransitions);

		/* Detect conflicts within MAS perceptions */
		this.detectConflicts(viewTransitions, conflicts);

		/* Create norms from detected conflicts */
		this.createdNorms = this.regulateConflicts(conflicts);

		/* Process created norms */
		for(Norm norm : createdNorms)	{
			this.processCreatedNorm(norm, activatedNorms);
		}
		
		return activatedNorms;
	}

	/**
	 * @param norm
	 * @param activatedNorms 
	 */
  private void processCreatedNorm(Norm norm, List<Norm> activatedNorms) {
  	
  	SetOfPredicatesWithTerms precondition = norm.getPrecondition();
		NormModality modality = norm.getModality();
		EnvironmentAgentAction action = norm.getAction();
		boolean normExists = normativeNetwork.containsNormWith(precondition,
				modality, action);
	
		/* Add the norm to the normative network only if either norm generation
		 * is deliberative, or it is reactive but the norm does not exist 
		 * in the normative network (i.e., first addition) */
		if(nGenerationApproach == Approach.Deliberative || !normExists) {
			this.operators.add(norm);
		}
		else {
			norm = normativeNetwork.retrieveNorm(norm);
		}
		
		/* If norm generation is reactive, activate the norm
		 * and reset its utility. Otherwise, hibernate it */
		if(this.nGenerationApproach == NormGenerator.Approach.Reactive) {
			this.operators.activate(norm);
			this.operators.reset(norm);
			
			/* This is necessary to exploit norm synergies (when norm generation
			 * is reactive) in order to deactivate norms that are substitutable 
			 * with norms that have been activated during norm generation */
			activatedNorms.add(norm);
		}
		else {
			this.operators.hibernate(norm);
		}
  }

	/**
	 * Creates norms to regulate each conflict from a list of detected conflicts
	 * 
	 * @param conflicts detected conflicts
	 * @param createdNorms norms created to regulate conflicts 
	 */
	private Set<Norm> regulateConflicts(Map<Goal, List<Conflict>> conflicts) {
		HashSet<Norm> createdNorms = new HashSet<Norm>();
		Set<Norm> normsForConflict;
		
		for(Goal goal : conflicts.keySet()) {
			for(Conflict conflict : conflicts.get(goal)) {
				normsForConflict = this.regulateConflict(conflict, goal);
				createdNorms.addAll(normsForConflict);
			}
		}
		return createdNorms;
	}

	/**
	 * Creates norms to regulate a detected conflict
	 * 
	 * @param conflicts a detected conflict
	 * @return a set of norms created to regulate a conflict 
	 */
	protected Set<Norm> regulateConflict(Conflict conflict, Goal goal) {
		Set<Norm> createdNorms = new HashSet<Norm>();
		List<Long> blamedAgents = new ArrayList<Long>();
		View sourceView = conflict.getConflictSource().getView(-1);
		List<EnvironmentAgentAction> agActions;
		EnvironmentAgentContext agContext;
		EnvironmentAgentAction agAction;

		/* Get a list of those agents that are considered as responsible 
		 * for the conflict (requires domain knowledge) */
		blamedAgents = this.cbrMachine.getConflictSource(conflict);

		/* Create a norm that regulates the behavior of each agent
		 * blamed for the conflict in the context previous to the conflict */
		for(Long blamedAgentId : blamedAgents) {

			/* Get agent's context and action */
			agContext = this.dmFunctions.agentContext(blamedAgentId, sourceView);
			agActions = this.dmFunctions.agentAction(blamedAgentId, 
					conflict.getConflictSource());
			agAction = agActions.get(random.nextInt(agActions.size()));

			/* Check that the agent has a valid context */
			if(agContext == null) {
				continue;
			}
			
			/* Check that no norms applied to the agent in the state
			 * previous to the state containing the conflict */
			if(this.normsAppliedToAgentContext(agContext)) {
				continue;
			}
			
			/* Create a new norm prohibiting that action in that context */
			SetOfPredicatesWithTerms precondition = agContext.getDescription();
			if(!precondition.isEmpty()) {
				Norm norm = new Norm(precondition, NormModality.Prohibition, 
						agAction, goal);
				
				createdNorms.add(norm);
				
				/* Update metrics */
				this.nsMetrics.newNonRegulatedConflictsSolvedThisTick();
			}
		}
		return createdNorms;
	}

	/**
	 * Returns <tt>true</tt> if any (represented) norm applies to the 
	 * agent context received as a parameter
	 * 
	 * @param agContext an agent context
	 * @return <tt>true</tt> if some norm (represented) applies to the 
	 * 				 agent context received as a parameter
	 */
	private boolean normsAppliedToAgentContext(EnvironmentAgentContext agContext) {
		NormsApplicableToAgentContext normsApplicable = 
				this.normReasoner.getNormsApplicable(agContext.getDescription());

		for(Norm n : normsApplicable.getApplicableNorms()) {
			NetworkNodeState nState = this.normativeNetwork.getState(n);
			boolean isHibernated = nState == NetworkNodeState.Hibernated;
			boolean isRepresented = this.normativeNetwork.isRepresented(n);
			boolean isSubstituted = nState == NetworkNodeState.Substituted;
			
			if(isRepresented || isHibernated || isSubstituted) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves MAS perceptions from the monitors that perceive the scenario
	 * 
	 * @return a {@code List} of the monitor perceptions, where each perception
	 *  				is a view transition from t-1 to t
	 */
	protected void getMASPerceptions(List<ViewTransition> viewTransitions) {
		this.monitor.getPerceptions(viewTransitions);
	}

	/**
	 * Given a list of view transitions (from t-1 to t), this method
	 * returns a list of conflicts with respect to each goal of the system
	 * 
	 * @param viewTransitions the list of perceptions of each sensor
	 */
	protected Map<Goal, List<Conflict>> detectConflicts(
			List<ViewTransition> viewTransitions, 
			Map<Goal,List<Conflict>> conflicts) {

		conflicts.clear();

		/* Conflict detection is computed in terms of a goal */
		for(Goal goal : this.nsmSettings.getSystemGoals())		{
			List<Conflict> goalConflicts = new ArrayList<Conflict>();

			for(ViewTransition vTrans : viewTransitions) {
				goalConflicts.addAll(dmFunctions.getConflicts(goal, vTrans));
			}  	
			conflicts.put(goal, goalConflicts);
		}
		return conflicts;
	}
}
