/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.refinement;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.norm.Norm;

/**
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public interface NormPerformancesDetector {

	/**
	 * 
	 * @param norm
	 * @param dim
	 * @return
	 */
	public boolean shouldBeDeactivated(Norm norm, EvaluationCriteria dim);
	
	/**
	 * 
	 * @param norm
	 * @param dim
	 * @return
	 */
	public boolean shouldBeActivated(Norm norm, EvaluationCriteria dim);
}
