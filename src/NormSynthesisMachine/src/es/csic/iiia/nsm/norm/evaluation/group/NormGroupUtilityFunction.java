/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.evaluation.group;

import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;

/**
 * @author Javi
 *
 */
public interface NormGroupUtilityFunction {

	/**
	 * 
	 * @param dim
	 * @param goal
	 * @param nCompliance
	 * @param nNetwork
	 * @return
	 */
	public void evaluate(Goal goal, NormGroupOutcomes nGroupCompliance,
			NormGroupNetwork nGroupNetwork);
}
