package es.csic.iiia.nsm.norm.refinement.generalisation;

import java.util.List;

import es.csic.iiia.nsm.norm.Norm;

/**
 * An potential generalisation of IRON is a generalisation that the NSM 
 * may potentially perform for a particular group of norms. Each potential
 * generalisation contains:
 * <ol>
 * <li> a potential <tt>parent</tt> norm that may be created if a set of child
 * 			norms fulfil certain conditions to be generalised to the parent; and
 * <li>	a {@code List} of <tt>children</tt>, namely a list of child norms that
 * 			must exist and perform well to be generalised to a parent norm.
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see GeneralisationReasoner
 * @see GeneralisationTrees
 */
public class NormGeneralisation {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private List<Norm> children;
	private Norm parent;
	

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param parent the potential parent
	 * @param children the children that are necessary to generalise
	 */
	public NormGeneralisation(List<Norm> children, Norm parent) {
		this.parent = parent;
		this.children = children;
	}

	/**
	 * Returns the parent of this potential generalisation
	 * 
	 * @return the parent of this potential generalisation
	 */
	public Norm getParent() {
		return this.parent;
	}
	
	/**
	 * Returns a {@code List} of the children in this potential generalisation
	 * 
	 * @return a {@code List} of the children in this potential generalisation
	 */
	public List<Norm> getChildren() {
		return this.children;
	}
		
	/**
	 * Returns a string describing the potential generalisation
	 */
	@Override
	public String toString() {
		String s = parent + ":\t[[ ";
		int c = 0;
		
		for(Norm child : children) {
			c++;
			s += child;
			if(c < children.size())
				s += ", ";
		}
		s += " ]]";
		
		return s;
	}
}
