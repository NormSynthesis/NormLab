package es.csic.iiia.normlab.examples.traffic;

import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class TrafficNSExample3_NSStrategy implements NormSynthesisStrategy {
	
	/* The normative network, a structure to keep track of synthesised norms */
	private NormativeNetwork normativeNetwork;
	
	/**
	 * 
	 * @param nsm
	 */
	public TrafficNSExample3_NSStrategy(NormSynthesisMachine nsm) {
		this.normativeNetwork = nsm.getNormativeNetwork();
	}
	
	/**
	 * A basic norm synthesis strategy that always outputs an
	 * empty normative system
	 * 
	 * @return an empty normative system
	 */
	@Override
  public NormativeSystem execute() {
		return this.normativeNetwork.getNormativeSystem();
  }

	/**
	 * 
	 */
	@Override
  public void addDefaultNormativeSystem(List<Norm> poolOfNorms) {}

	/**
	 * 
	 */
	@Override
  public void setup(NormSynthesisMachine nsm) {}
}
