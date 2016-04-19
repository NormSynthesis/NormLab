/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.evaluation;

import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;

/**
 * @author Javi
 *
 */
public interface NormUtilityFunction {

	/**
	 * 
	 * @param dim
	 * @param goal
	 * @param nCompliance
	 * @param nNetwork
	 * @return
	 */
	public Map<Norm, List<SetOfPredicatesWithTerms>> evaluate(
			EvaluationCriteria dim, Goal goal, NormComplianceOutcomes nCompliance,
			NormativeNetwork nNetwork);
}
