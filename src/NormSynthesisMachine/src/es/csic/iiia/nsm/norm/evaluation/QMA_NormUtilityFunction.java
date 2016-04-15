package es.csic.iiia.nsm.norm.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupOutcomes;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;

/**
 * An utility function evaluates norms in terms of some system dimensions
 * and goals. Norms are evaluated by using information about their outcomes
 * in the system, specifically in terms of their effectiveness and necessity
 * by means of RL formulas and computing rewards
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class QMA_NormUtilityFunction implements NormUtilityFunction {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private Map<Norm, List<SetOfPredicatesWithTerms>> negRewardedNorms;
	private NormSynthesisMetrics nsMetrics;
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public QMA_NormUtilityFunction(NormSynthesisMetrics nsMetrics) {
		this.negRewardedNorms = new HashMap<Norm, List<SetOfPredicatesWithTerms>>();
		this.nsMetrics = nsMetrics;
	}

	/**
	 * Evaluates the norms that have been complied and infringed during the current
	 * time step, in terms of a dimension and a goal. It evaluates norms based on
	 * the outcomes of their fulfilments and infringements. For more information,
	 * see the documentation of {@code Dimension}
	 * 
	 * @param 	dim the dimension that the norm is evaluated in terms of
	 * @param 	goal the goal that the norm is evaluated in terms of
	 * @param 	nCompliance the object that contains information about the norms
	 * 					fulfilments and infringements during the current time step, and the
	 * 					outcomes of those fulfilments and infringements (conflict/no conflict)
	 * @param 	nNetwork the normative network
	 * @return a map that contains all the norms that have been negatively rewarded
	 * 					during the evaluation
	 * @see EvaluationCriteria
	 * @see Goal
	 */
	public Map<Norm, List<SetOfPredicatesWithTerms>> evaluate(EvaluationCriteria dim, Goal goal, 
			NormComplianceOutcomes nCompliance, NormativeNetwork nNetwork) {

		float reward;
		this.negRewardedNorms.clear();

		switch(dim) {

		/* Evaluate norms' effectiveness based on its fulfilments */
		case Effectiveness:

			for(Norm appNorm : nCompliance.getFulfilledNorms()) {
				int nAC = nCompliance.getNumFulfilmentsWithConflict(appNorm);
				int nANoC = nCompliance.getNumFulfilmentsWithNoConflict(appNorm);

				if(nNetwork.getUtility(appNorm) == null) {
					break;
				}

				reward = (float) nANoC / (nAC + nANoC); 
				nNetwork.setScore(appNorm, dim, goal, reward);

				/* If the norm has been negatively rewarded, add it to the
				 * map of negatively rewarded norms */
				List<SetOfPredicatesWithTerms> agContexts = 
						nCompliance.getAgentContextsWhereNormApplies(appNorm);

				this.negRewardedNorms.put(appNorm, agContexts);

//				/* Update complexities metrics */
//				this.nsMetrics.incNumNodesVisited();
			}
			break;

			/* Evaluate norms' necessity based on its infringements */
		case Necessity:

			for(Norm violNorm : nCompliance.getInfringedNorms()) {
				int nVC = nCompliance.getNumInfringementsWithConflict(violNorm);
				int nVNoC = nCompliance.getNumInfrsWithNoConflict(violNorm);	

				reward = (float) nVC / (nVC + nVNoC); 
				nNetwork.setScore(violNorm, dim, goal, reward);

				/* If the norm has been negatively rewarded, add it to the
				 * map of negatively rewarded norms */
				List<SetOfPredicatesWithTerms> agContexts = 
						nCompliance.getAgentContextsWhereNormApplies(violNorm);

				this.negRewardedNorms.put(violNorm, agContexts);

//				/* Update complexities metrics */
//				this.nsMetrics.incNumNodesVisited();
			}
			break;
		}
		return this.negRewardedNorms;
	}

	/**
	 * Evaluates the norm groups that have been fulfilled during the current
	 * time step, in terms of a goal. It evaluates norm groups based on
	 * the outcomes of their fulfilments
	 * 
	 * @param 	goal the goal that the norm is evaluated in terms of
	 * @param 	nGroupCompliance the object that contains information about the
	 * 					norm groups fulfilments during the current time step, and the
	 * 					outcomes of those fulfilments (conflict/no conflict)
	 * @param 	nGroupNetwork the norm group network
	 */
	public void evaluate(Goal goal, NormGroupOutcomes nGroupCompliance,
			NormGroupNetwork nGroupNetwork) {

		float reward;

		for(NormGroup normGroup : nGroupCompliance.getNormGroups()) {
			int nAC = nGroupCompliance.getNumComplsWithConflict(normGroup);
			int nANoC = nGroupCompliance.getNumComplsWithNoConflict(normGroup);

			if(nGroupNetwork.getUtility(normGroup) == null) {
				break;
			}

			reward = (float) nANoC / (nAC + nANoC); 
			nGroupNetwork.setScore(normGroup, EvaluationCriteria.Effectiveness, goal, reward);
		}
	}
}
