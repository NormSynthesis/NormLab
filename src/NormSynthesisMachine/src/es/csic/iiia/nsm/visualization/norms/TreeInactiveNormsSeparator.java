package es.csic.iiia.nsm.visualization.norms;

/**
 * Returns a line to separate a tree
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class TreeInactiveNormsSeparator implements TreeSeparator{

	/**
	 * Returns a line to separate a tree
	 */
	public String toString() {
		return "== Inactive ==";
	}
}
