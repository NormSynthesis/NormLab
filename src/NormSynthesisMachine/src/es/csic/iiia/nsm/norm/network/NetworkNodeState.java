package es.csic.iiia.nsm.norm.network;

/**
 * Enumeration that defines the different states of a norm in the normative 
 * network. A norm in the normative network may be either active or inactive.
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see NormGroupNetwork
 */
public enum NetworkNodeState {
	Created, 
	
	Hibernated, 
	
	Active, 
	
	Inactive, 
	
	Discarded, 
	
	Represented, 
	
	Specialised, 
	
	Substituted, 
	
	Excluded; 
}

