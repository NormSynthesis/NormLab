package es.csic.iiia.nsm.norm.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;

/**
 * An utility function evaluates norms in terms of some system dimensions
 * and goals. Norms are evaluated by using information about their outcomes
 * in the system, specifically in terms of their effectiveness and necessity
 * by means of RL formulas and computing rewards
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class RL_NormUtilityFunction implements NormUtilityFunction {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private Map<Norm, List<SetOfPredicatesWithTerms>> negRewardedNorms;
//	private NormSynthesisMetrics nsMetrics;
	private double alpha;
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public RL_NormUtilityFunction(NormSynthesisMetrics nsMetrics, double alpha) {
		this.negRewardedNorms = new HashMap<Norm, List<SetOfPredicatesWithTerms>>();
//		this.nsMetrics = nsMetrics;
		this.alpha = alpha;
	}

	/**
	 * Evaluates the norms that have been complied and infringed during the current
	 * time step, in terms of a dimension and a goal. It evaluates norms based on
	 * the outcomes of their compliances and infringements. For more information,
	 * see the documentation of {@code Dimension}
	 * 
	 * @param 	dim the dimension that the norm is evaluated in terms of
	 * @param 	goal the goal that the norm is evaluated in terms of
	 * @param 	nCompliance the object that contains information about the norms
	 * 					compliances and infringements during the current time step, and the
	 * 					outcomes of those compliances and infringements (conflict/no conflict)
	 * @param 	nNetwork the normative network
	 * @return a map that contains all the norms that have been negatively rewarded
	 * 					during the evaluation
	 * @see EvaluationCriteria
	 * @see Goal
	 */
	public Map<Norm, List<SetOfPredicatesWithTerms>> evaluate(EvaluationCriteria dim, Goal goal, 
			NormComplianceOutcomes nCompliance, NormativeNetwork nNetwork) {
		
		double oldScore, score, reward;
		this.negRewardedNorms.clear();

		switch(dim) {

		/* Evaluate norms' effectiveness based on its compliances */
		case Effectiveness:

			for(Norm appNorm : nCompliance.getFulfilledNorms()) {
				int nAC = nCompliance.getNumFulfilmentsWithConflict(appNorm);
				int nANoC = nCompliance.getNumFulfilmentsWithNoConflict(appNorm);

				if(nNetwork.getUtility(appNorm) == null) {
					break;
				}

				oldScore = nNetwork.getUtility(appNorm).getScore(dim, goal);
				reward = (float) nANoC / (nAC + nANoC); 
				score = (float) (oldScore + alpha * (reward - oldScore));
				nNetwork.setScore(appNorm, dim, goal, score);

				/* If the norm has been negatively rewarded, add it to the
				 * map of negatively rewarded norms */
				if(reward < 1.0f) {
					List<SetOfPredicatesWithTerms> agContexts = 
							nCompliance.getAgentContextsWhereNormApplies(appNorm);
	
					this.negRewardedNorms.put(appNorm, agContexts);
				}
			}
			break;

		/* Evaluate norms' necessity based on its infringements */
		case Necessity:

			for(Norm violNorm : nCompliance.getInfringedNorms()) {
				int nVC = nCompliance.getNumInfringementsWithConflict(violNorm);
				int nVNoC = nCompliance.getNumInfrsWithNoConflict(violNorm);	

				oldScore = nNetwork.getUtility(violNorm).getScore(dim, goal);
				reward = (float) nVC / (nVC + nVNoC); 
				score = (float) (oldScore + alpha * (reward - oldScore));
				nNetwork.setScore(violNorm, dim, goal, score);

				/* If the norm has been negatively rewarded, add it to the
				 * map of negatively rewarded norms */
				if(reward < 1.0f) {
					List<SetOfPredicatesWithTerms> agContexts = 
							nCompliance.getAgentContextsWhereNormApplies(violNorm);
	
					this.negRewardedNorms.put(violNorm, agContexts);
				}
			}
			break;
		}
		return this.negRewardedNorms;
	}
}
