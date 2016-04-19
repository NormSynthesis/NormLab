/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.evaluation.group.JointContext;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupOutcomes;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupUtilityFunction;
import es.csic.iiia.nsm.norm.evaluation.group.RL_NormGroupUtilityFunction;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.sensing.View;
import es.csic.iiia.nsm.sensing.ViewTransition;
import es.csic.iiia.nsm.strategy.GenericOperators;

/**
 * Class to evaluate norms after agents fulfill and infringe them. This
 * class contains all the necessary methods to compute norm applicability,
 * norm compliance, and to evaluate norms based on their compliance outcomes.
 * To this aim it employs a utility function that evaluates a norm in terms
 * of some evaluation criteria. This function, which must be provided as a
 * parameter in the class' constructor, may variate depending on the synthesis
 * strategy that is to be executed.
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormEvaluator {

	/**
	 * Mechanism employed to evaluate norms. There are two:
	 * 
	 * <ol>
	 * <li> With reinforcement learning and Bollinger Bands. This is the mechanism
	 * originally employed by IRON and SIMON. It evaluates norms by computing 
	 * rewards and cumulating them with a RL formula. Then, it computes the
	 * performance ranges of norms and their Bollinger Bands, which allow to 
	 * highlight long-term trends. Norms are then preserved and discarded based
	 * on these Bollinger Bands.
	 *  
	 * <li> With a q-period moving average. This is the mechanism originally
	 * employed by LION and DESMON. It requires less evidence than the above
	 * mechanism to evaluate norms. It evaluates norms by computing rewards, and 
	 * cumulates these rewards by means of a q-period moving average that highlights
	 * long-term trends. No reinforcement learning nor Bollinger Bands are employed 
	 * to make decisions abiout the preservation or discarding of norms.   
	 * </ol>
	 * 
	 * @author Javi
	 *
	 */
	public enum Mechanism {
		BollingerBands, MovingAverage;
	}

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected List<EvaluationCriteria> evalCriteria;	// A set of evaluation criteria 
	protected NormSynthesisSettings nsmSettings;			// The settings of the NSM
	protected NormReasoner normReasoner;							// A norms' reasoner
	protected DomainFunctions dmFunctions;						// To assess norm applicability
	protected NormativeNetwork normativeNetwork;			// A normative network
	protected NormGroupNetwork normGroupNetwork;			// To evaluate norm groups
	protected NormSynthesisMetrics nsMetrics;					// The NSM's metrics
	protected NormUtilityFunction normUtilityFunction;		// To evaluate norms
	protected NormGroupUtilityFunction nGroupUtilityFunction;		// To evaluate norms
	protected GenericOperators operators;							// Normative network operators

	/* To keep track of those norms that are negatively evaluated */
	protected Map<Norm, List<SetOfPredicatesWithTerms>> negRewardedNorms;
	
	protected boolean pursueLiberality;								// Exploit norm synergies?

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor of the class. This class contains all the necessary methods
	 * to compute norms' applicability, norms' compliance, and to evaluate norms
	 * based on their compliance outcomes To this aim it employs a utility function
	 * that evaluates a norm in terms of some evaluation criteria. 
	 * 
	 * @param evalCriteria a set of evaluation criteria to evaluate norms
	 * @param nsmSettings the settings of the NSM
	 * @param dmFunctions	domain functions, employed to compute norm applicability 
	 * 				and norm compliance
	 * @param normativeNetwork a normative network 
	 * @param nGroupNetwork a network of norm groups
	 * @param normReasoner a reasoner to compute norm applicability
	 * @param utilityFunction a function to evaluate norms in terms of some
	 * 				evaluation criteria
	 * @param operators a set of normative network operators
	 * @param nsMetrics the metrics of the NSM
	 */
	public NormEvaluator(List<EvaluationCriteria> evalCriteria, 
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			NormativeNetwork normativeNetwork, NormGroupNetwork nGroupNetwork,
			NormReasoner normReasoner, GenericOperators operators,
			NormSynthesisMetrics nsMetrics, NormEvaluator.Mechanism nEvMechanism) {

		this.evalCriteria = evalCriteria;
		this.nsmSettings = nsmSettings;
		this.dmFunctions = dmFunctions;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = nGroupNetwork;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.nsMetrics = nsMetrics;
		
		this.pursueLiberality = nsmSettings.pursueLiberality();
		this.negRewardedNorms = new HashMap<Norm,List<SetOfPredicatesWithTerms>>();

		/* Create norm evaluation functions */
		if(nEvMechanism == NormEvaluator.Mechanism.BollingerBands) {
			this.normUtilityFunction = new RL_NormUtilityFunction(nsMetrics, 
					nsmSettings.getNormEvaluationLearningRate());
		}
		else {
			this.normUtilityFunction = new QMA_NormUtilityFunction(nsMetrics);	
		}

		/* Create norm group evaluation functions */
		this.nGroupUtilityFunction = new RL_NormGroupUtilityFunction(nsMetrics,	0.1);
	}

	/**
	 * Performs a complete cycle of norm evaluation: 
	 * <ol>
	 * <li> Computation of norm applicability. During this step, the norm evaluator
	 * 			detects which norms apply to each agent in each view transition.
	 * <li> Detection of norm compliance. Computes whether agents have fulfilled or
	 * 			infringed those norms that applied to them, and whether conflicts arose
	 * 			after these fulfillments and infringements.
	 * <li> Detection of norm group compliance. The same as norm compliance, but by
	 * 			considering norms that are jointly fulfilled and infringed.
	 * <li>	Norm evaluation. Evaluates norms in terms of some evaluation criteria.
	 * 			With this aim it employs the information gathered during the detection of
	 * 			norm compliance.  
	 * </ol>
	 * 
	 * @param viewTransitions a view transition 
	 * @param normApplicability to keep track of norms' applicability
	 * @param normCompliance to keep track of norms' compliance
	 * @param normGroupCompliance to keep track of norm groups' compliance
	 */
	public void step(List<ViewTransition> viewTransitions, 
			Map<ViewTransition, NormsApplicableInView> normApplicability, 
			Map<Goal, Map<ViewTransition, NormComplianceOutcomes>> normCompliance, 
			Map<Goal, NormGroupOutcomes> normGroupCompliance) {

		/* Compute norm applicability */
		this.normApplicability(viewTransitions, normApplicability);

		/* Detect norm compliance */
		this.normCompliance(normApplicability, normCompliance);
		
		/* Detect norm group compliance to exploit norm synergies
		 * (only if liberality is pursued) */
		if(this.pursueLiberality) {
			this.normGroupCompliance(normCompliance, normGroupCompliance);
		}

		/* Update utilities and performances of norms and norm groups */
		this.updateUtilitiesAndPerformances(normCompliance, normGroupCompliance);
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * Computes the norm applicability perceived by each sensor.
	 * (recall that each view transition is perceived by a particular sensor)
	 * 
	 * @param vTransitions the list of perceptions of each sensor
	 * @return a map containing the norms that are applicable to each
	 * agent in each view transition
	 */
	protected void normApplicability(List<ViewTransition> vTransitions, 
			Map<ViewTransition, NormsApplicableInView> normApplicability)	{

		/* Clear norm applicability from previous tick */
		normApplicability.clear();

		/* Get applicable norms of each viewTransition (of each sensor) */
		for(ViewTransition vTrans : vTransitions) {
			NormsApplicableInView normsAppInView;
			normsAppInView = this.normReasoner.getNormsApplicable(vTrans);
			normApplicability.put(vTrans, normsAppInView);
		}
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
			NormsApplicableInView> normApplicability,
			Map<Goal, Map<ViewTransition, NormComplianceOutcomes>> normCompliance) {

		/* Check norm compliance in the view in terms of each system goal */
		for(Goal goal : this.nsmSettings.getSystemGoals()) {

			/* Clear norm compliance of previous tick */
			normCompliance.get(goal).clear();

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

				normCompliance.get(goal).put(vTrans, nCompliance);
			}
		}
	}

	/**
	 * Generates norm groups compliance in the current time step, in terms of 
	 * each system goal and for each perceived view transition. With this aim,
	 * this method performs the following steps for each goal/view transition:
	 * <ol>
	 * <li> it computes the joint contexts in the view, namely the contexts of
	 * 			those agents that perceive each other;
	 * <li> for each computed joint context, it retrieves the norms (in fact, 
	 * 			it just retrieves the leaf norms) that apply to that joint context,
	 * 			as well as which agent each norm applies to; and
	 * <li> for each norm that applies to the joint context, it generates norm
	 * 			groups taking into account the compliance of each norm and the
	 * 			conflicts that the norm compliance lead to
	 * </ol>
	 * 
	 * @param 	normCompliance the compliance and outcomes of norms in
	 * 					the current time step
	 * @param normGroupCompliance 
	 */
	protected void normGroupCompliance(Map<Goal,Map<ViewTransition,
			NormComplianceOutcomes>> normCompliance, 
			Map<Goal, NormGroupOutcomes> normGroupCompliance) {

		/* Likewise in the case of norms, norm groups are evaluated in terms 
		 * of a goal. With this aim, we generate norm groups (and hence, norm
		 * groups compliance) for each system goal */
		for(Goal goal : normCompliance.keySet()) {
			NormGroupOutcomes nGroupOutcomes = normGroupCompliance.get(goal);
			nGroupOutcomes.clear();

			for(ViewTransition vTrans : normCompliance.get(goal).keySet()) {

				/* Compute joint contexts */
				List<JointContext> jointContexts = this.computeJointContexts(vTrans);	

				/* Each joint context will generate >= 1 norm compliance pairs */
				for(JointContext jointContext : jointContexts) {
					Map<Norm, Long> normsApplicableToAgents = new HashMap<Norm, Long>();

					for(long agentId : jointContext.getAgentIds()) {
						EnvironmentAgentContext agContext = jointContext.getContext(agentId);

						/* Retrieve norms that apply to the agent context, and compute
						 * norm compliance outcomes */
						NormsApplicableToAgentContext nAppToPred = this.normReasoner.
								getNormsApplicable(agContext.getDescription());

						List<Norm> leaves = this.extractLeafNorms(
								nAppToPred.getApplicableNorms());

						for(Norm leaf : leaves) {
							normsApplicableToAgents.put(leaf, agentId);
						}
					}
					/* Generate norm groups by combining all the norms that are 
					 * applicable together in the joint context */
					List<Norm> norms = new ArrayList<Norm>(
							normsApplicableToAgents.keySet());

					this.generateNormGroups(norms, normsApplicableToAgents, 
							goal, vTrans, nGroupOutcomes);
				}
			}
		}
	}

	/**
	 * Generates norm groups from a combination of norms that are applicable
	 * together. Therefore, this method assumes that the {@code norms}
	 * received by parameter are applicable together in a joint context.
	 * To generate norm groups, this method computes the compliance of each
	 * norm in the {@code norms} received by parameter, as well as the number
	 * of conflicts that the norm compliance lead to
	 * 
	 * @param 	norms the list of norms that are applicable together
	 * @param 	normsApplicableToAgents 	a map that retrieves, for each norm,
	 * 					the agent to which the norm applies in the view transition
	 * @param 	goal the goal in terms of which the norm group will be evaluated
	 * @param 	vTrans the view transition in which the {@code norms} are 
	 * 					applicable together
	 * @param 	nGroupOutcomes the norm group outcomes where to save the
	 * 					information about norm groups' compliance
	 */
	protected void generateNormGroups(List<Norm> norms,
			Map<Norm, Long> normsApplicableToAgents, Goal goal, 
			ViewTransition vTrans, NormGroupOutcomes nGroupOutcomes) {

		View view = vTrans.getView(0);
		NormGroup normGroup;

		/* Only generate norm groups for leaves */		
		for(int i=0; i < norms.size(); i++) {
			for(int j=i+1; j < norms.size(); j++) {
				Norm n1 = norms.get(i);
				Norm n2 = norms.get(j);
				long agN1 = normsApplicableToAgents.get(n1);
				long agN2 = normsApplicableToAgents.get(n2);

				/* Compute norm compliance of the agent for the norm. With this aim,
				 * we first retrieve, for each norm, the agent to which the norm was
				 * applicable, and then we retrieve the agent's norm compliance
				 * (whether it fulfilled the norm or not) */
				NormCompliance complN1 = this.normReasoner.
						hasFulfilledNorm(agN1, n1, vTrans) ? NormCompliance.Fulfilment : 
							NormCompliance.Infringement;

				NormCompliance complN2 = this.normReasoner.
						hasFulfilledNorm(agN2, n2, vTrans) ? NormCompliance.Fulfilment : 
							NormCompliance.Infringement;

				/* Retrieve norm group that corresponds to the norms compliance */
				normGroup = this.retrieveNormGroup(n1, complN1, n2, complN2);

				/* Compute the number of conflicts of the group (for evaluation) */
				int numConflicts = 0, numNoConflicts = 0;
				List<Long> agents = new ArrayList<Long>();
				agents.add(agN1);
				agents.add(agN2);

				for(long agent : agents) {
					if(this.dmFunctions.hasConflict(view, agent, goal)) {
						numConflicts++;
					}
					else {
						numNoConflicts++;
					}
				}
				/* Add the number of conflicts that arose after
				 * fulfilling the norm group */
				nGroupOutcomes.addComplsWithConflict(normGroup, numConflicts);
				nGroupOutcomes.addComplsWithNoConflict(normGroup, numNoConflicts);
			}
		}
	}

	/**
	 * Returns the {@code NormGroup} that corresponds to the combination of
	 * two norms that have been fulfilled or infringed together. First, this
	 * method searches for the norm group in the norm group network. If it
	 * exists, then it retrieves it. Otherwise, it creates the new norm group
	 * adds it to the norm group network and activates it
	 * 
	 * @param n1 the first norm 
	 * @param complN1 the compliance of the first norm (Fulfilled or Infringed)
	 * @param n2 the second norm
	 * @param compl N2 the compliance of the second norm (Fulfilled or Infringed)
	 * @return Returns the {@code NormGroup} that corresponds to the combination
	 * 					of two norms that have been fulfilled or infringed together
	 * @see NormGroup
	 * @see NormCompliance
	 */
	protected NormGroup retrieveNormGroup(Norm n1, NormCompliance complN1,
			Norm n2, NormCompliance complN2) {

		String ngrDesc = NormGroup.getDescription(n1, complN1, n2, complN2);
		NormGroup normGroup;

		/* If the norm group exists in the norm group network, then
		 * retrieve it to evaluate it */
		if(this.normGroupNetwork.contains(ngrDesc)) {
			normGroup = this.normGroupNetwork.
					getNormGroupWithDescription(ngrDesc);
		}
		/* If the norm group does not exist, then create it and add 
		 * it to the norm group network */
		else {
			normGroup = new NormGroup(n1.getGoal());
			normGroup.addNorm(n1, complN1);
			normGroup.addNorm(n2, complN2);

			this.normGroupNetwork.add(normGroup);
			this.normGroupNetwork.setState(normGroup, NetworkNodeState.Active);
		}
		return normGroup;
	}

	/**
	 * Updates norm utilities and performances based on
	 * their norm compliance in the current time step
	 * 
	 * @param normCompliance the norm compliance in the current time step
	 * @param normGroupCompliance norm group compliance in the current time step
	 */
	protected void	updateUtilitiesAndPerformances(Map<Goal, 
			Map<ViewTransition,NormComplianceOutcomes>> normCompliance, 
			Map<Goal, NormGroupOutcomes> normGroupCompliance) {

		/* On the one hands, we evaluate norms in terms of several goals
		 * and dimensions. On the other hand, norm groups are only evaluated
		 * in terms of system goals and their "Effectiveness" */
		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			for(EvaluationCriteria dim : this.evalCriteria)	{
				for(ViewTransition vTrans : normCompliance.get(goal).keySet()) {

					/* Evaluate norms and retrieve a list of negatively rewarded norms */
					this.negRewardedNorms = this.normUtilityFunction.evaluate(dim, goal, 
							normCompliance.get(goal).get(vTrans), normativeNetwork);

					/* Add norms that have been rewarded with a negative value */
					this.addNegRewardedNorms(negRewardedNorms, goal);
				}
			}
			
			/* Evaluate norm groups if required */
			if(this.pursueLiberality) {
				this.nGroupUtilityFunction.evaluate(goal, normGroupCompliance.get(goal),
						normGroupNetwork);
			}
		}
	}

	/**
	 * Returns the joint contexts in a certain {@code viewTransition}
	 * 
	 * @param viewTransition the view transition
	 * @return the joint contexts in a certain {@code viewTransition}
	 * @see JointContext
	 */
	protected List<JointContext> computeJointContexts(ViewTransition viewTransition) {

		List<JointContext> jointContexts = new ArrayList<JointContext>();
		List<Long> agentIds = new ArrayList<Long>();
		View pView = viewTransition.getView(-1);
		View view = viewTransition.getView(0);
		EnvironmentAgentContext ag1Context, ag2Context;
		long agent1Id, agent2Id;

		/* Just check joint contexts for those agents that
		 * exist in all views of the stream */
		for(Long agentId : pView.getAgentIds())	{
			if(view.getAgentIds().contains(agentId))
				agentIds.add(agentId);
		}

		for(int i=0; i < agentIds.size(); i++) {
			for(int j=i+1; j < agentIds.size(); j++) {
				agent1Id = agentIds.get(i);
				agent2Id = agentIds.get(j);

				ag1Context = this.dmFunctions.agentContext(agent1Id, pView);
				ag2Context = this.dmFunctions.agentContext(agent2Id, pView);

				/* Check that none of the agents has a null (out of bounds) context */
				if(ag1Context != null && ag2Context != null) {

					/* If the agents perceive each other */
					if(ag1Context.getPerceivedAgentsIds().contains(agent2Id) &&
							ag2Context.getPerceivedAgentsIds().contains(agent1Id)) {

						JointContext jointContext = new JointContext();
						jointContext.addAgentContext(agent1Id, ag1Context);
						jointContext.addAgentContext(agent2Id, ag2Context);

						/* Add joint context to the return list */
						jointContexts.add(jointContext);
					}
				}
			}
		}
		return jointContexts;
	}

	/**
	 * Returns the norms in a list of {@code norms} that are leaves
	 * in the normative network. That is, norms that do not generalise
	 * any other norm, and hence have generalisation level 0
	 * 
	 * @param norms the list of norms
	 * @return the norms in a list of {@code norms} that are leaves
	 * 					in the normative network. That is, norms that do not generalise
	 * 					any other norm, and hence have generalisation level 0
	 */
	protected List<Norm> extractLeafNorms(List<Norm> norms) {
		List<Norm> leaves = new ArrayList<Norm>();

		/* Extract norms that are leaves in the normative network */
		for(Norm norm : norms) {
			if(this.normativeNetwork.isLeaf(norm)) {
				leaves.add(norm);
			}
		}
		return leaves;
	}


	/**
	 * Adds norms to a list of norms that have been negatively rewarded
	 * during the current time step (during the norm evaluation phase)
	 * 
	 * @param negRewNorms the negatively rewarded norms
	 */
	protected void addNegRewardedNorms(Map<Norm,
			List<SetOfPredicatesWithTerms>> negRewNorms, Goal goal) {

		for(Norm norm : negRewardedNorms.keySet()) {
			List<SetOfPredicatesWithTerms> agContexts = negRewNorms.get(norm);

			for(SetOfPredicatesWithTerms precond : agContexts) {
				NormModality mod = norm.getModality();
				EnvironmentAgentAction action = norm.getAction();

				boolean exists = this.normativeNetwork.
						containsNormWith(precond, mod, action);

				/* The norm does not exist -> add it to the normative network */
				if(!exists) {
					Norm specNorm = new Norm(precond, mod, action, goal);
					this.operators.add(specNorm);
					this.operators.activate(specNorm);
				}
			}
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}
}
