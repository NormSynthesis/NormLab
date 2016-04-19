/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.refinement.generalisation;

import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;

/**
 * @author Javi
 *
 */
public interface NormGeneraliser {
	
	/**
	 * 
	 * @author Javier Morales (jmorales@iiia.csic.es)
	 *
	 */
	public enum Approach {
		
		Conservative,
		
		Optimistic;
	}
	
	/**
	 * 
	 * @param norm
	 */
	public void generaliseUp(Norm norm, NormativeNetwork normativeNetwork);
}
