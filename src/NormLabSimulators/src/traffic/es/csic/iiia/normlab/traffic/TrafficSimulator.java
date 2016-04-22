package es.csic.iiia.normlab.traffic;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.WindowConstants;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import es.csic.iiia.normlab.examples.traffic.TrafficNSExample1_NSAgent;
import es.csic.iiia.normlab.examples.traffic.TrafficNSExample2_NSAgent;
import es.csic.iiia.normlab.examples.traffic.TrafficNSExample3_NSAgent;
import es.csic.iiia.normlab.examples.traffic.TrafficNSExample4_NSAgent;
import es.csic.iiia.normlab.examples.traffic.TrafficNSExample5_NSAgent;
import es.csic.iiia.normlab.traffic.agent.DefaultTrafficNSAgent;
import es.csic.iiia.normlab.traffic.agent.TrafficElement;
import es.csic.iiia.normlab.traffic.agent.TrafficNSAgent;
import es.csic.iiia.normlab.traffic.agent.monitor.TrafficCamera;
import es.csic.iiia.normlab.traffic.agent.monitor.TrafficCameraPosition;
import es.csic.iiia.normlab.traffic.config.TrafficNormSynthesisSettings;
import es.csic.iiia.normlab.traffic.config.TrafficSimulatorSettings;
import es.csic.iiia.normlab.traffic.factory.CarContextFactory;
import es.csic.iiia.normlab.traffic.factory.TrafficFactFactory;
import es.csic.iiia.normlab.traffic.map.CarMap;
import es.csic.iiia.normlab.traffic.metrics.TrafficMetrics;
import es.csic.iiia.normlab.traffic.normsynthesis.TrafficDomainFunctions;
import es.csic.iiia.normlab.visualization.MessageConsole;
import es.csic.iiia.normlab.visualization.NormLabConsole;
import es.csic.iiia.normlab.visualization.NormLabInspector;
import es.csic.iiia.nsm.IncorrectSetupException;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.TaxonomyOfTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * Scene Manager - Main class of implementation. Controls cooperation
 * of all the components, agent generation etc.
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
@AgentAnnot(displayName = "Main Agent")
public class TrafficSimulator implements TrafficElement {

	//-----------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------

	private static Random random;
	private static CarMap carMap = null;
	private static TrafficFactFactory factFactory;
	private static CarContextFactory carContextFactory;
	private static TrafficSimulatorSettings simSettings;
	private static TrafficNormSynthesisSettings nsSettings;

	private static long tick = 0;

	//-----------------------------------------------------------------
	// Attributes
	//-----------------------------------------------------------------

	private PredicatesDomains predDomains;
	private TrafficNSAgent normSynthesisAgent;
	private TrafficMetrics trafficMetrics;
	private List<TrafficCamera> trafficCameras;
	
	private NormLabInspector nInspector;
	
	private boolean useGui;
	
	//-----------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param grid
	 * @param context
	 * @param valueLayer
	 * @param map
	 * @throws Exception 
	 */
	public TrafficSimulator(Context<TrafficElement> context, 
			Grid<TrafficElement> map) throws Exception {

		/* Load simulator settings and norm synthesis settings */
		simSettings = new TrafficSimulatorSettings();
		nsSettings = new TrafficNormSynthesisSettings();

		/* Set a randomly defined random seed if the random seed is equal to 0 */
		long seed = simSettings.getRandomSeed();
		if(seed == 0l) {
			seed = System.currentTimeMillis();
		}
		random = new Random(seed);

		/* Create traffic predicates and their domains */
		this.createPredicatesDomains();

		/* Create fact factory (for agents), car context factory, and car map */
		factFactory = new TrafficFactFactory(this.predDomains);
		carContextFactory = new CarContextFactory(factFactory);
		carMap = new CarMap(context, map, predDomains,
				carContextFactory, factFactory, simSettings);

		/* Create norm synthesis elements */
		this.createMonitorAgents();
		this.createNormSynthesisStuff();

		/* Create traffic metrics */
		this.trafficMetrics = 
				(TrafficMetrics) this.normSynthesisAgent.getMetrics();

		NormSynthesisMachine nsm = 
				this.normSynthesisAgent.getNormSynthesisMachine();
		
		/* Create the GUI if required) */
		this.useGui = !RunEnvironment.getInstance().isBatch();
		if(this.useGui) {
			
			/* Redirect output and show norms inspector */
			NormLabConsole console = this.redirectOutput();
			this.nInspector = new NormLabInspector(nsm, console);
			this.nInspector.show();
		}
		
		System.out.println("\nStarting simulation with random seed = " + seed);
	}

	/**
	 * 
	 */
	private NormLabConsole redirectOutput() {
		final NormLabConsole consoleFrame = new NormLabConsole();
		consoleFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		/* Redirect output */
		if(!RunEnvironment.getInstance().isBatch()) {
			Runnable runnable = new Runnable() {
				public void run() {
					new MessageConsole(consoleFrame.getConsole());
				}
			};
			EventQueue.invokeLater(runnable);	
		}
		return consoleFrame;
  }

	/**
	 * Step method. Controls everything in the simulation
	 */
	@ScheduledMethod(start=1, interval=1, priority=2)
	public void step() {

		if(tick++ > 0) {

			// 1. Move cars, emit cars and handle new collisions
			carMap.makeCarsMove();

			for(TrafficCamera camera : this.trafficCameras) {
				camera.perceive(carMap);
			}

			try {
				this.normSynthesisAgent.step(tick);
			}
			catch (IncorrectSetupException e) {
				System.err.println("FATAL error, end of simulation...");
				e.printStackTrace();
				RunEnvironment.getInstance().endRun();
			}
			
			/* Informa cars that norms have been added to the normative network
			 * and removed from the normative network */
			for(Norm norm : this.normSynthesisAgent.getAddedNorms()) {
				carMap.broadcastAddNorm(norm);
			}
			for(Norm norm : this.normSynthesisAgent.getRemovedNorms()) {
				carMap.broadcastRemoveNorm(norm);
			}
			
			/* Make cars perceive and reason */
			carMap.makeCarsPerceive();
		}

		/* Display metrics */
		if(this.useGui) {
			nInspector.refresh();
		}
		
		// Stop simulation if required update
		if(tick >= simSettings.getMaxTicks() || this.mustStop()) {
			System.out.println("End of simulation");
			trafficMetrics.save();
			RunEnvironment.getInstance().endRun();
		}
	}


	/**
	 * Creates the traffic institutions
	 */
	private void createMonitorAgents() {
		this.trafficCameras = new ArrayList<TrafficCamera>();

		for(TrafficCameraPosition pos : TrafficCameraPosition.getPossiblePositions()) {
			this.trafficCameras.add(new TrafficCamera(pos, carMap));
		}
	}

	/**
	 * Creates the norm synthesis agent
	 * @throws Exception 
	 */
	private void createNormSynthesisStuff() throws Exception {
		DomainFunctions dmFunctions = 
				new TrafficDomainFunctions(predDomains, carContextFactory);

		if(nsSettings.getNormSynthesisStrategy() == NormSynthesisStrategy.Option.Example) {
			int example = nsSettings.getNormSynthesisExample();
			switch(example) {
			case 1:
				this.normSynthesisAgent = new	TrafficNSExample1_NSAgent(simSettings, 
						nsSettings, this.predDomains,	dmFunctions);
				break;

			case 2:
				this.normSynthesisAgent = new	TrafficNSExample2_NSAgent(simSettings, 
						nsSettings, this.predDomains,	dmFunctions);
				break;

			case 3:
				this.normSynthesisAgent = new	TrafficNSExample3_NSAgent(simSettings, 
						nsSettings, this.predDomains, dmFunctions);
				break;

			case 4:
				this.normSynthesisAgent = new	TrafficNSExample4_NSAgent(simSettings, 
						nsSettings, this.predDomains, dmFunctions, this.trafficCameras);
				break;

			case 5:
				this.normSynthesisAgent = new	TrafficNSExample5_NSAgent(simSettings, 
						nsSettings, this.predDomains, dmFunctions, this.trafficCameras);
				break;			
			}
		}

		/* Use a norm synthesis strategy */
		else {
			this.normSynthesisAgent = new	DefaultTrafficNSAgent( 
					this.trafficCameras, predDomains, dmFunctions,
					nsSettings, simSettings, random);	
		}
	}

	/**
	 * Creates the agent language. That is, the predicates, and their domains 
	 */
	private void createPredicatesDomains() {

		/* Predicate "left" domain */
		TaxonomyOfTerms leftPredTaxonomy = new TaxonomyOfTerms("l");
		leftPredTaxonomy.addTerm("*");
		leftPredTaxonomy.addTerm("<");
		leftPredTaxonomy.addTerm(">");
		leftPredTaxonomy.addTerm("-");
		leftPredTaxonomy.addRelationship("<", "*");
		leftPredTaxonomy.addRelationship(">", "*");
		leftPredTaxonomy.addRelationship("-", "*");

		/* Predicate "front" domain*/
		TaxonomyOfTerms frontPredTaxonomy = new TaxonomyOfTerms("f", leftPredTaxonomy);
		frontPredTaxonomy.addTerm("^");
		frontPredTaxonomy.addRelationship("^", "*");

		/* Predicate "right" domain*/
		TaxonomyOfTerms rightPredTaxonomy = new TaxonomyOfTerms("r", leftPredTaxonomy);
		rightPredTaxonomy.addTerm("w");
		rightPredTaxonomy.addRelationship("w", "*");

		this.predDomains = new PredicatesDomains();
		this.predDomains.addPredicateDomain("l", leftPredTaxonomy);
		this.predDomains.addPredicateDomain("f", frontPredTaxonomy);
		this.predDomains.addPredicateDomain("r", rightPredTaxonomy);
	}

	//-----------------------------------------------------------------
	// Other
	//-----------------------------------------------------------------

	/**
	 * 
	 */
	private boolean mustStop() {
		//		if(tick < 14000) { // TODO: Min num ticks to check convergence
		//			return false;
		//		}
		return this.trafficMetrics.hasConverged();
	}

	//-----------------------------------------------------------------
	// Static methods
	//-----------------------------------------------------------------

	/**
	 * 
	 * @return
	 */
	public static long getTick() {
		return tick;
	}

	/**
	 * 
	 * @return
	 */
	public static Random getRandom() {
		return random;
	}

	/**
	 * 
	 * @return
	 */
	public static CarMap getMap() {
		return carMap;
	}

	/**
	 * 
	 */
	public static TrafficSimulatorSettings getConfig() {
		return simSettings;
	}

	@Override
	public int getX() {
		return -1;
	}

	@Override
	public int getY() {
		return -1;
	}

	@Override
	public void move() {

	}

	public static CarContextFactory getCarContextFactory() {
		return carContextFactory;
	}

	public static TrafficFactFactory getFactFactory() {
		return factFactory;
	}
}

