package es.csic.iiia.normlab.examples.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.csic.iiia.normlab.traffic.agent.TrafficNSAgent;
import es.csic.iiia.normlab.traffic.car.CarAction;
import es.csic.iiia.normlab.traffic.config.TrafficSimulatorSettings;
import es.csic.iiia.normlab.traffic.metrics.TrafficMetrics;
import es.csic.iiia.nsm.IncorrectSetupException;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class TrafficNSExample2_NSAgent implements TrafficNSAgent {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private NormSynthesisMachine nsm;
	private NormativeSystem normativeSystem;
	private List<Norm> addedNorms;
	private List<Norm> removedNorms;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor of the example
	 */
	public TrafficNSExample2_NSAgent(TrafficSimulatorSettings simSettings, 
			NormSynthesisSettings nsSettings, PredicatesDomains predDomains,
			DomainFunctions dmFunctions) {
		
		/* Create the normative system, which will contain the norms 
		 * available to the agents */
		this.normativeSystem = new NormativeSystem();
		
		/* Create lists to control norm additions and norm removals
		 * from the normative system */
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();
				
		/* Create the norm synthesis machine */
		this.nsm = new NormSynthesisMachine(nsSettings, predDomains,
				dmFunctions, new Random(), true);
		
		/* Create traffic metrics */
		TrafficMetrics nsMetrics = new TrafficMetrics(simSettings, nsm);
		
		/* Create the norm synthesis strategy */
		TrafficNSExample2_NSStrategy strategy =	
				new TrafficNSExample2_NSStrategy(this.nsm);
		
		/* Setup the norm synthesis machine */
		this.nsm.setup(strategy, nsSettings, nsMetrics, null, null);
		
		/* Create default normative system */
		this.createDefaultNormativeSystem();
		
	}

	/**
	 * Called at every tick of the simulation
	 * 
	 * @throws IncorrectSetupException 
	 */
	public void step(long timeStep) throws IncorrectSetupException {
		this.addedNorms.clear();
		this.removedNorms.clear();

		/* Execute strategy and obtain new normative system */
		NormativeSystem newNormativeSystem = nsm.executeStrategy(timeStep);

		/* Check norm additions to the normative system */ 
		for(Norm norm : newNormativeSystem) {
			if(!normativeSystem.contains(norm)) {
				this.normativeSystem.add(norm);
				this.addedNorms.add(norm);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public NormSynthesisMetrics getMetrics() {
		return this.nsm.getNormSynthesisMetrics();
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getAddedNorms() {
		return this.addedNorms;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getRemovedNorms() {
		return this.removedNorms;
	}

	/**
	 * 
	 * @return
	 */
	public NormativeSystem getNormativeSystem() {
		return normativeSystem;
	}

	/**
	 * 
	 * @return
	 */
	public NormSynthesisMachine getNormSynthesisMachine() {
		return this.nsm;
	}
	
	/**
	 * Creates a normative system with a few core norms that are 
	 * highly effective and necessary
	 */
	private void createDefaultNormativeSystem() {
		NormativeNetwork normativeNetwork = this.nsm.getNormativeNetwork();
		
		/* Create norms */
		SetOfPredicatesWithTerms n1Precondition = new SetOfPredicatesWithTerms();
		n1Precondition.add("l", ">");
		n1Precondition.add("f", "*");
		n1Precondition.add("r", "*");
		
		/* Create norm */
		Norm n1 = new Norm(n1Precondition, 
				NormModality.Prohibition, CarAction.Go);
		
		/* Add the norm to the normative network and activate it */
		normativeNetwork.add(n1);
		normativeNetwork.setState(n1, NetworkNodeState.Active);
	}
}
