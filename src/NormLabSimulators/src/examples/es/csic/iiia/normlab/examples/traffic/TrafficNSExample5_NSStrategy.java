package es.csic.iiia.normlab.examples.traffic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableToAgentContext;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.cbr.CBRConflictSolvingMachine;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.sensing.Monitor;
import es.csic.iiia.nsm.sensing.View;
import es.csic.iiia.nsm.sensing.ViewTransition;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * The SIMON norm synthesis strategy
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class TrafficNSExample5_NSStrategy implements NormSynthesisStrategy {

	/**
	 * Generalisation mode of SIMON. Mode <tt>Shallow<tt> sets SIMON to perform
	 * shallow generalisations, while mode <tt>Deep<tt> sets SIMON to perform
	 * deep generalisations
	 * 
	 * @author "Javier Morales (jmorales@iiia.csic.es)"
	 */
	public enum GeneralisationMode {
		Deep, Shallow;
	}

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormSynthesisMachine nsm;
	protected NormSynthesisSettings nsmSettings; 
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormSynthesisMetrics metrics;

	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predicatesDomains;
	protected Monitor monitor;

	protected TrafficNSExample5_UtilityFunction utilityFunction;	
	protected TrafficNSExample5_NSOperators operators;
	protected CBRConflictSolvingMachine cbrMachine;
	protected Random random;
	
	protected Map<Goal,List<Conflict>> conflicts;
	protected Map<Goal,Map<ViewTransition, NormComplianceOutcomes>> normCompliance;
	protected Map<ViewTransition, NormsApplicableInView> normApplicability;
	protected List<ViewTransition> viewTransitions;

	protected Set<Norm> createdNorms;
	protected List<Norm> activatedNorms;
	protected List<Norm> 	normAdditions;
	protected List<Norm> normDeactivations;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	nsm the norm synthesis machine
	 * @param 	genMode the SIMON generalisation mode
	 */
	public void setup(NormSynthesisMachine nsm) {

		this.nsm = nsm;
		this.metrics = nsm.getNormSynthesisMetrics();
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.dmFunctions = nsm.getDomainFunctions();
		this.predicatesDomains = this.nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.monitor = nsm.getMonitor();
		this.random = new Random();
		
		this.normReasoner = new NormReasoner(this.nsmSettings.getSystemGoals(), 
				this.predicatesDomains, this.dmFunctions, metrics);

		this.utilityFunction = new TrafficNSExample5_UtilityFunction();
		this.conflicts = new HashMap<Goal, List<Conflict>>();
		this.operators = new TrafficNSExample5_NSOperators(
				this, normReasoner, nsm, metrics);

		this.cbrMachine = new CBRConflictSolvingMachine(this.normativeNetwork,
				normReasoner, this, new Random(), metrics);
		
		this.normApplicability = new HashMap<ViewTransition, 
				NormsApplicableInView>();

		this.normCompliance = new HashMap<Goal,
				Map<ViewTransition,NormComplianceOutcomes>>();

		this.viewTransitions = new ArrayList<ViewTransition>();
		this.createdNorms = new HashSet<Norm>();
		this.activatedNorms = new ArrayList<Norm>();
		this.normAdditions = new ArrayList<Norm>();
		this.normDeactivations = new ArrayList<Norm>();

		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normCompliance.put(goal, new HashMap<ViewTransition,
					NormComplianceOutcomes>());
		}
	}

	/**
	 * Executes IRON's strategy and outputs the resulting normative system.
	 * The norm synthesis cycle consists in three steps:
	 * <ol>
	 * <li> Norm generation. Generates norms for each detected conflict.
	 * 			Generated norms are aimed to avoid detected conflicts in the future;
	 * <li> Norm evaluation. Evaluates norms in terms of their effectiveness
	 * 			and necessity, based on the outcome of their compliances and 
	 * 			infringements, respectively; and
	 * <li> Norm refinement. Generalises norms which utilities are over
	 * 			generalisation thresholds, and specialises norms which utilities
	 * 			are under specialisation thresholds. Norm generalisations can be
	 * 			performed in Shallow or Deep mode.
	 * 
	 * @return the normative system resulting from the norm synthesis cycle
	 */
	public NormativeSystem execute() {	
		this.normAdditions.clear();
		this.normDeactivations.clear();
		this.createdNorms.clear();
		this.activatedNorms.clear();

		this.normGeneration();
		
		this.normEvaluation();

		/* Return the current normative system */
		return normativeNetwork.getNormativeSystem();
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * Executes the norm generation phase
	 */
	private void normGeneration() {
		
		/* Collect perceptions from the MAS */
		this.getMASPerceptions(viewTransitions);

		/* Detect conflicts within MAS perceptions */
		this.detectConflicts(viewTransitions);

		/* Create norms from detected conflicts */
		this.createdNorms = this.regulateConflicts(conflicts);

		/* Add norms to the normative network */
		for(Norm norm : createdNorms)	{
			this.operators.add(norm);
			this.operators.activate(norm);
		}
	}
	
	/**
	 * Executes the norm evaluation phase
	 */
	private void normEvaluation() {

		/* Compute norm applicability */
		this.normApplicability = this.normApplicability(viewTransitions);

		/* Detect norm applicability and compliance */
		this.normCompliance(this.normApplicability);

		/* Update utilities and performances */
		this.updateUtilitiesAndPerformances(this.normCompliance);
	}

	/**
	 * Calls scenario monitors to perceive agents interactions
	 * 
	 * @return a {@code List} of the monitor perceptions, where each perception
	 *  				is a view transition from t-1 to t
	 */
	private void getMASPerceptions(List<ViewTransition> viewTransitions) {
		this.monitor.getPerceptions(viewTransitions);
	}

	/**
	 * Given a list of view transitions (from t-1 to t), this method
	 * returns a list of conflicts with respect to each goal of the system
	 * 
	 * @param viewTransitions the list of perceptions of each sensor
	 */
	protected Map<Goal, List<Conflict>> detectConflicts(
			List<ViewTransition> viewTransitions) {
		this.conflicts.clear();

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
			}
		}
		return createdNorms;
	}
	
	/**
	 * Returns <tt>true</tt> if some norm (represented) applies to the 
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
			if(this.normativeNetwork.isRepresented(n) || 
					this.normativeNetwork.getState(n) == NetworkNodeState.Candidate) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Computes the norm applicability perceived by each sensor.
	 * (recall that each view transition is perceived by a particular sensor)
	 * 
	 * @param vTransitions the list of perceptions of each sensor
	 * @return a map containing the norms that are applicable to each
	 * agent in each view transition
	 */
	protected Map<ViewTransition, NormsApplicableInView> normApplicability(
			List<ViewTransition> vTransitions)	{

		/* Clear norm applicability from previous tick */
		this.normApplicability.clear();

		/* Get applicable norms of each viewTransition (of each sensor) */
		for(ViewTransition vTrans : vTransitions) {
			NormsApplicableInView normApplicability;
			normApplicability = this.normReasoner.getNormsApplicable(vTrans);
			this.normApplicability.put(vTrans, normApplicability);
		}
		return this.normApplicability;
	}

	/**
	 * Computes norms' compliance based on the norms that were applicable 
	 * to each agent in the previous time step (norm applicability)
	 * and the actions that agents performed in the transition from the 
	 * previous to the current time step
	 * 
	 * @param normApplicability norms that were applicable in the previous tick
	 */
	protected void normCompliance(Map<ViewTransition,
			NormsApplicableInView> normApplicability) {

		/* Check norm compliance in the view in terms of each system goal */
		for(Goal goal : this.nsmSettings.getSystemGoals()) {

			/* Clear norm compliance of previous tick */
			this.normCompliance.get(goal).clear();

			/* Evaluate norm compliance and conflicts in each 
			 * view transition with respect to each system goal */
			for(ViewTransition vTrans : normApplicability.keySet()) {
				NormsApplicableInView vNormAppl = normApplicability.get(vTrans);

				/* If there is no applicable norm in the view, continue */
				if(vNormAppl.isEmpty()) {
					continue;
				}
				NormComplianceOutcomes nCompliance = this.normReasoner.
						checkNormComplianceAndOutcomes(vNormAppl, goal);

				this.normCompliance.get(goal).put(vTrans, nCompliance);
			}
		}
	}

	/**
	 * Updates norm utilities and performances based on
	 * their norm compliance in the current time step
	 * 
	 * @param normCompliance the norm compliance in the current time step
	 */
	protected void	updateUtilitiesAndPerformances(
			Map<Goal, Map<ViewTransition,NormComplianceOutcomes>> normCompliance) {

		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			for(ViewTransition vTrans : normCompliance.get(goal).keySet()) {
				for(EvaluationCriteria dim : this.nsm.getNormEvaluationDimensions())	{
					this.utilityFunction.evaluate(dim, goal, 
							normCompliance.get(goal).get(vTrans), normativeNetwork);	
				}
			}
		}
	}

	/**
	 * 
	 * @param norm
	 */
	public void normCreated(Norm norm) {
		this.createdNorms.add(norm);
		this.normAdditions.add(norm);
	}

	/**
	 * 
	 * @param norm
	 */
	public void normActivated(Norm norm) {
		this.activatedNorms.add(norm);
		this.normAdditions.add(norm);
	}

	/**
	 * 
	 * @param norm
	 */
	public void normDeactivated(Norm norm) {
		this.normDeactivations.add(norm);
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNewAdditionsToNormativeSystem() {
		return this.normAdditions;
	}

	/**
	 * 
	 */
	@Override
  public void addDefaultNormativeSystem(List<Norm> defaultNorms) {}

//	/**
//	 * 
//	 */
//	@Override
//  public void setup(NormSynthesisMachine nsm) {}
}