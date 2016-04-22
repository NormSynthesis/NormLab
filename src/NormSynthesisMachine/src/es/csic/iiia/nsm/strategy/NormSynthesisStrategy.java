package es.csic.iiia.nsm.strategy;

import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;

/**
 * A strategy in charge of performing norm synthesis
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NormSynthesisStrategy {
	
	/**
	 * 
	 * @author "Javier Morales (jmorales@iiia.csic.es)"
	 *
	 */
	public enum Option {
		Example, User, Custom, BASE, IRON, SIMON, LION, DESMON; 
	}

	/**
	 * Sets up the strategy by taking the necessary information
	 * from the norm synthesis machine received as a parameter
	 * 
	 * @param nsm a norm synthesis machine
	 */
	public void setup(NormSynthesisMachine nsm);
	
	/**
	 * Executes the strategy, performing the norm synthesis cycle
	 * 
	 * @return the {@code NormativeSystem} system resulting from the
	 * 					norm synthesis cycle
	 */
	public NormativeSystem execute();
	
	
	/**
	 * Adds a default normative system to start with
	 * 
	 * @param poolOfNorms a default list of norms to add to the 
	 * 										normative system
	 */
	public void addDefaultNormativeSystem(List<Norm> poolOfNorms);
	
}
