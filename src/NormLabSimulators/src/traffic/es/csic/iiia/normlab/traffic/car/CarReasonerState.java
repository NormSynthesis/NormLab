package es.csic.iiia.normlab.traffic.car;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public enum CarReasonerState {

	/**
	 * No norms have been neither fired nor applied in the current tick
	 */
	NoNormActivated,

	/**
	 * The fired norm will be applied
	 */
	NormWillBeFullfilled,

	/**
	 * The fired norm will be violated
	 */
	NormWillBeInfringed;
}