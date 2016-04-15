package es.csic.iiia.normlab.examples.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.csic.iiia.normlab.traffic.agent.TrafficNSAgent;
import es.csic.iiia.normlab.traffic.agent.monitor.TrafficCamera;
import es.csic.iiia.normlab.traffic.config.TrafficSimulatorSettings;
import es.csic.iiia.normlab.traffic.metrics.TrafficMetrics;
import es.csic.iiia.nsm.IncorrectSetupException;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class TrafficNSExample5_NSAgent implements TrafficNSAgent {

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
	public TrafficNSExample5_NSAgent(TrafficSimulatorSettings simSettings, 
			NormSynthesisSettings nsSettings, PredicatesDomains predDomains,
			DomainFunctions dmFunctions, List<TrafficCamera> cameras) {
		
		/* Create the normative system, which will contain the norms 
		 * available to the agents */
		this.normativeSystem = new NormativeSystem();
		
		/* Create lists to control additions and
		 * removals of the normative system */
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();
		
		/* Create norm synthesis machine */
		this.nsm = new NormSynthesisMachine(nsSettings, predDomains,
				dmFunctions, new Random(), true);

		/* Add sensors to the monitor of the norm synthesis machine */
		for(TrafficCamera camera : cameras) {
			this.nsm.addSensor(camera);	
		}
		
		/* Create traffic metrics */
		TrafficMetrics nsMetrics = new TrafficMetrics(simSettings, nsm);
		
		/* Create the norm synthesis strategy */
		TrafficNSExample5_NSStrategy strategy =	
				new TrafficNSExample5_NSStrategy();
		
		/* Setup the norm synthesis machine with the created strategy */
		this.nsm.setup(strategy, nsSettings, nsMetrics, null, null);
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
}
