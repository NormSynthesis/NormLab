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
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
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
public class TrafficNSExample4_NSStrategy implements NormSynthesisStrategy {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormSynthesisMachine nsm;
	protected NormSynthesisSettings nsmSettings; 
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;

	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predicatesDomains;
	protected Monitor monitor;
	protected TrafficNSExample4_NSOperators operators;
	protected List<ViewTransition> viewTransitions;
	protected Random random;
	
	protected CBRConflictSolvingMachine cbrMachine;
	protected Map<Goal,List<Conflict>> conflicts;
	protected Set<Norm> createdNorms;
	protected List<Norm> activatedNorms;
	protected List<Norm> normAdditions;
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
	public TrafficNSExample4_NSStrategy(NormSynthesisMachine nsm,
			NormSynthesisMetrics nsMetrics) {

		this.nsm = nsm;
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.dmFunctions = nsm.getDomainFunctions();
		this.predicatesDomains = this.nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.monitor = nsm.getMonitor();
		this.random = new Random();
		
		this.normReasoner = new NormReasoner(this.nsmSettings.getSystemGoals(), 
				this.predicatesDomains, this.dmFunctions, nsMetrics);

		this.conflicts = new HashMap<Goal, List<Conflict>>();
		this.operators = new TrafficNSExample4_NSOperators(
				this, normReasoner, nsm, nsMetrics);
		
		this.cbrMachine = new CBRConflictSolvingMachine(this.normativeNetwork,
				normReasoner, this, new Random(), nsMetrics);
		
		this.viewTransitions = new ArrayList<ViewTransition>();
		this.createdNorms = new HashSet<Norm>();
		this.activatedNorms = new ArrayList<Norm>();
		this.normAdditions = new ArrayList<Norm>();
		this.normDeactivations = new ArrayList<Norm>();
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

		/*-------------------
		 *  Norm generation
		 *-------------------*/

		this.normGeneration();
		
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

	/**
	 * 
	 */
	@Override
  public void setup(NormSynthesisMachine nsm) {}
}