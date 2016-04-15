package es.csic.iiia.normlab.onlinecomm.nsm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.engine.environment.RunEnvironment;
import es.csic.iiia.normlab.onlinecomm.agents.OnlineCommunityManager;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityUserReasoner;
import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySettings;
import es.csic.iiia.normlab.onlinecomm.context.ContextData;
import es.csic.iiia.normlab.onlinecomm.metrics.OnlineCommunityMetrics;
import es.csic.iiia.normlab.onlinecomm.nsm.perception.OnlineCommunityWatcher;
import es.csic.iiia.nsm.IncorrectSetupException;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.reasoning.NormEngine;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * 
 */
public class OnlineCommunityNSAgent {

	// ---------------------------------------------------------------------------
	// Attributes
	// ---------------------------------------------------------------------------

	private NormSynthesisMachine nsm;
	private NormSynthesisSettings nsSettings;
	private OnlineCommunityUserReasoner usersReasoner;
	private NormEngine normEngine;
	
	private NormativeSystem normativeSystem;
	private List<Norm> addedNorms;
	private List<Norm> removedNorms;
	
//	private List<OnlineCommunityUser> agents;
	private OnlineCommunityMetrics nsMetrics;
	
	// ---------------------------------------------------------------------------
	// Methods
	// ---------------------------------------------------------------------------

	/**
	 * 
	 */
	public OnlineCommunityNSAgent(OnlineCommunityWatcher watcher, 
			ContextData contextData, PredicatesDomains predDomains, 
			DomainFunctions dmFunctions, NormSynthesisSettings nsSettings, 
			OnlineCommunitySettings ocSettings,	OnlineCommunityManager ocManager, 
			Random random)
					throws Exception {

		this.normativeSystem = new NormativeSystem();
		this.addedNorms = new ArrayList<Norm>();
		this.removedNorms = new ArrayList<Norm>();
		
		this.normEngine = ocManager.getNormEngine();
		this.usersReasoner = ocManager.getUsersReasoner();
		this.nsSettings = nsSettings;
		
		/* 1. Create norm synthesis machine */
		this.nsm = new NormSynthesisMachine(nsSettings, predDomains,
				dmFunctions, random, !RunEnvironment.getInstance().isBatch());

		/* 2. Add watcher to perceive the online community */
		this.nsm.addSensor(watcher);

		/* 3. Create metrics */
		this.nsMetrics = new OnlineCommunityMetrics(contextData, nsm, ocSettings);

		/* 4. Set the norm synthesis strategy */
		this.setNormSynthesisStrategy();
	}

	/**
	 * @throws IncorrectSetupException
	 * 
	 */
	public void step() throws IncorrectSetupException {
		double startTime, finishTime, compTime;
		NormativeSystem newNormativeSystem;

		this.addedNorms.clear();
		this.removedNorms.clear();

		double tick = RunEnvironment.getInstance().
				getCurrentSchedule().getTickCount();

//		if (this.agents == null) {
//			this.agents = this.getAgents();
//		}

		/* Save time just before executing the strategy */
		startTime = System.nanoTime();

		/* Execute strategy and obtain new normative system */
		newNormativeSystem = nsm.executeStrategy(tick);

		/* Compute elapsed time during the strategy execution */
		finishTime = System.nanoTime();
		compTime = (finishTime - startTime) / 1000000;

		this.nsMetrics.addNewComputationTime(compTime);

		/* Check norm additions to the normative system */ 
		for(Norm norm : newNormativeSystem) {
			if(!normativeSystem.contains(norm)) {
				this.normativeSystem.add(norm);
				this.addedNorms.add(norm);

				this.normEngine.addNorm(norm);
				this.usersReasoner.addNorm(norm);
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
			this.usersReasoner.removeNorm(norm);
		}

		if(tick == 5000) {
			System.out.println(this.normativeSystem);
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
		case Custom:
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
	public OnlineCommunityMetrics getMetrics() {
		return this.nsMetrics;
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
