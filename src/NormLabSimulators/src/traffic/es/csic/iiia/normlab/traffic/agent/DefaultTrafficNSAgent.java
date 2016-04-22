package es.csic.iiia.normlab.traffic.agent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.engine.environment.RunEnvironment;
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
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class DefaultTrafficNSAgent
implements TrafficNSAgent {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private NormSynthesisMachine nsm;
	private NormSynthesisSettings nsSettings;
	private NormativeSystem normativeSystem;
	private List<Norm> addedNorms;
	private List<Norm> removedNorms;
	private TrafficMetrics nsMetrics;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * @throws Exception 
	 * 
	 */
	public DefaultTrafficNSAgent(List<TrafficCamera> cameras,
			PredicatesDomains predDomains, DomainFunctions dmFunctions, 
			NormSynthesisSettings nsSettings, TrafficSimulatorSettings simSettings,
			Random random) 
					throws Exception {

		this.nsSettings = nsSettings;
		this.normativeSystem = new NormativeSystem();
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();

		/* 1. Create norm synthesis machine */
		this.nsm = new NormSynthesisMachine(nsSettings, predDomains,
				dmFunctions, random, !RunEnvironment.getInstance().isBatch());

		/* 2. Add sensors to the monitor of the norm synthesis machine */
		for(TrafficCamera camera : cameras) {
			this.nsm.addSensor(camera);	
		}

		/* 3. Create traffic metrics */
		this.nsMetrics = new TrafficMetrics(simSettings, nsm);

		/* 4. Set the norm synthesis strategy */
		this.setNormSynthesisStrategy();
	}

	/**
	 * @throws IncorrectSetupException 
	 * 
	 */
	public void step(long timeStep) throws IncorrectSetupException {
		double startTime, finishTime, compTime;

		this.addedNorms.clear();
		this.removedNorms.clear();

		/* Save time just before executing the strategy */
		startTime = System.nanoTime();

		/* Execute strategy and obtain new normative system */
		NormativeSystem newNormativeSystem = nsm.executeStrategy(timeStep);

		/* Compute elapsed time during the execution of the strategy */
		finishTime = System.nanoTime();
		compTime = (finishTime - startTime) / 1000000;

		this.nsm.getNormSynthesisMetrics().addNewComputationTime(compTime);

		/* Check norm additions to the normative system */ 
		for(Norm norm : newNormativeSystem) {
			if(!normativeSystem.contains(norm)) {
				this.normativeSystem.add(norm);
				this.addedNorms.add(norm);
			}
		}

		// Check norm removals
		for(Norm norm : normativeSystem) {
			if(!newNormativeSystem.contains(norm)) {
				this.removedNorms.add(norm);
			}
		}	

		// Remove norms from normative systems
		for(Norm norm : removedNorms) {
			this.normativeSystem.remove(norm);
		}
	}

	/**
	 * Sets the norm synthesis strategy
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	protected void setNormSynthesisStrategy() throws Exception {
		NormSynthesisStrategy.Option option = nsSettings.getNormSynthesisStrategy();

		switch(option) {

		/* Create and setup custom norm synthesis strategy */
		case User:
			NormSynthesisStrategy strategy;
						
			/* Get custom synthesis strategy */
			String strategyName = this.nsSettings.getUserStrategy();
			Class<?> strategyClass = Class.forName(strategyName);
			
			/* Get constructor of the strategy with a parameter of class 
			 * NormSynthesisMachine and create a new instance of the strategy */
			Constructor<?> c = strategyClass.getDeclaredConstructor();
			Object obj = c.newInstance();

			/* Load strategy class */
			if (obj instanceof NormSynthesisStrategy) {
				strategy = (NormSynthesisStrategy)obj;
			}
			else {
				throw new Exception("The provided strategy does not implement "
						+ "the NormSynthesisStrategy interface...");
			}
			this.nsm.setup(strategy, nsSettings, nsMetrics, null, null);
			break;

		/* Setup predefined norm synthesis strategy */
		default:
			this.nsm.setup(nsSettings, nsMetrics, null, null);
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

//	/**
//	 * Sets a custom norm synthesis strategy
//	 */
//	protected NormSynthesisStrategy createCustomNormSynthesisStrategy() {
//		MyFirstStrategy strategy = new MyFirstStrategy();
//		strategy.setup(nsm);
//		return strategy;
//	}
}
