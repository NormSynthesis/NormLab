package user.strategies;

import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * A simple custom norm synthesis strategy that 
 * always returns an empty normative system 
 */
public class UserStrategy1 implements NormSynthesisStrategy {

	/* A normative network (a data structure to keep track of norms) */
	private NormativeNetwork normativeNetwork;

	/**
	 * Sets up the strategy by retrieving the normative network
	 * employed by the Norm Synthesis Machine
	 */
	@Override
	public void setup(NormSynthesisMachine nsm) {
		this.normativeNetwork = nsm.getNormativeNetwork();
	}

	/**
	 * Executes the strategy and returns an empty normative system
	 */
	@Override
	public NormativeSystem execute() {
		return normativeNetwork.getNormativeSystem();
	}

	/**
	 * Adds a default empty normative system
	 */
	@Override
	public void addDefaultNormativeSystem(List<Norm> poolOfNorms) {}
}

