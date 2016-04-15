package es.csic.iiia.nsm.norm.evaluation.group;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;

/**
 * An utility function evaluates norms in terms of some system dimensions
 * and goals. Norms are evaluated by using information about their outcomes
 * in the system, specifically in terms of their effectiveness and necessity
 * by means of RL formulas and computing rewards
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class QMA_NormGroupUtilityFunction implements NormGroupUtilityFunction {
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

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
