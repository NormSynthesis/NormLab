package es.csic.iiia.nsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.DefaultNormSynthesisMetrics;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator;
import es.csic.iiia.nsm.norm.generation.NormGenerator.Approach;
import es.csic.iiia.nsm.norm.network.DefaultOmegaFunction;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.OmegaFunction;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.sensing.Monitor;
import es.csic.iiia.nsm.sensing.Sensor;
import es.csic.iiia.nsm.strategy.GenericSynthesisStrategy;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;
import es.csic.iiia.nsm.visualization.NormSynthesisInspector;

/**
 * The Norm Synthesis Machine (NSM), containing:
 * <ol>
 * <li>	The norm evaluation dimensions (effectiveness and necessity).
 * 			During the norm evaluation phase, norms are evaluated in
 * 			terms of their: (i) effectiveness, based on the outcome of
 * 			their compliances, and (ii) necessity, based on the outcome
 * 			of their infringements;
 * <li>	The configuration <tt>settings</tt> of the norm synthesis machine; 
 * <li>	The monitor, containing sensors to perceive the scenario; 
 * <li>	The normative network, whose nodes stand for norms and whose edges
 * 			stand for relationships between norms; 
 * <li>	The omega function, that computes the normative system from the 
 * 			normative network; 
 * <li>	The norm synthesis <tt>strategy</tt>, which contains a method 
 * 			{@code execute()} that performs the norm synthesis cycle; 
 * <li>	The domain functions <tt>dmFunctions</tt> that allow to perform
 * 			norm synthesis for a specific domain. 
 * <li>	The NSM metrics, that contains information about the metrics of
 * 			different elements in the norm	synthesis process.
 * </ol>
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormSynthesisMachine {

	/**
	 * Generalisation mode. Mode <tt>Shallow<tt> performs shallow
	 * generalisations, while mode <tt>Deep<tt> performs deep generalisations
	 * 
	 * @author "Javier Morales (jmorales@iiia.csic.es)"
	 */
	public enum NormGeneralisationMode {
		None, Shallow, Deep;
	}

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	/* Settings and MAS perception */
	private Random random;												// Randomiser
	private List<EvaluationCriteria> evCriteria;	// Norm evaluation dimensions
	private NormSynthesisSettings settings;				// Norm synthesis settings
	private Monitor monitor;											// Monitor to perceive the scenario

	/* Information model */
	private NormativeNetwork nNetwork;						// The normative network
	private NormGroupNetwork nGroupNetwork; 			// The network of norm groups
	private OmegaFunction omegaFunction;					// Function to compute the NS
	private PredicatesDomains predDomains;				// Predicates and their domains

	/* Norm synthesis machinery */
	private NormSynthesisStrategy strategy;				// The norm synthesis strategy
	private DomainFunctions dmFunctions;					// Domain functions 
	private NormReasoner normReasoner; 						// The norm reasoner
	private NormReasoner normEvaluationReasoner;	// The norm evaluation reasoner

	/* Metrics and visualisation */
	private NormSynthesisMetrics metrics;					// Norm synthesis metrics
	private NormSynthesisInspector tracer; 				// GUI

	private boolean useGui;													// Use GUI?
	private boolean firstExecution;								// First execution of the strategy?

	//---------------------------------------------------------------------------
	// Constructors 
	//---------------------------------------------------------------------------

	/**
	 * The Norm Synthesis Machine constructor.
	 * 
	 * @param 	settings basic settings of the norm synthesis machine
	 * @param 	predDomains the predicates and terms to specify norms
	 * 					for the given domain
	 * @param 	dmFunctions the domain functions, that allow to perform 
	 * 					norm synthesis for a specific domain
	 * @param 	useGui indicates if the user requires a GUI or not
	 * @see 		PredicatesDomains
	 * @see			DomainFunctions
	 */
	public NormSynthesisMachine(NormSynthesisSettings settings, 
			PredicatesDomains predDomains, DomainFunctions dmFunctions, 
			Random random, boolean useGui) {

		this.settings = settings;
		this.predDomains = predDomains;
		this.dmFunctions = dmFunctions;
		this.useGui = useGui;
		this.random = random;

		this.firstExecution = true;

		/* Add norm evaluation dimensions */
		this.evCriteria = new ArrayList<EvaluationCriteria>();
		this.evCriteria.add(EvaluationCriteria.Effectiveness);
		this.evCriteria.add(EvaluationCriteria.Necessity);

		/* Create the monitor to perform system sensing */
		this.monitor = new Monitor();

		/* Create the normative network (norms and relationships between norms) */
		this.nNetwork = new NormativeNetwork(this);
		this.nGroupNetwork = new NormGroupNetwork(this);
	}

	//---------------------------------------------------------------------------
	// Public methods 
	//---------------------------------------------------------------------------

	/**
	 * Sets the NSM up with a predefined synthesis strategy. It invokes another
	 * method 'setup' that creates all necessary objects to perform norm synthesis 
	 * 
	 * @param strategy a strategy to perform norm synthesis
	 * @param nsMetrics metrics to track the NSM synthesis 
	 * @param poolOfNorms a default pool of norms to start with
	 */
	public void setup(NormSynthesisStrategy strategy, NormSynthesisSettings settings, 
			NormSynthesisMetrics nsMetrics, OmegaFunction oFunction, 
			List<Norm> poolOfNorms) {

		this.strategy = strategy;
		this.setup(settings, nsMetrics, oFunction, poolOfNorms);
	}

	/**
	 * Sets the NSM up. Creates all the necessary objects to perform norm 
	 * synthesis and assigns a synthesis strategy 
	 * 
	 * @param settings NSM settings
	 * @param nsMetrics metrics to track the NSM synthesis 
	 * @param oFunction a function to retrieve a NS from a NN
	 * @param poolOfNorms a default pool of norms to start with
	 */
	public void setup(NormSynthesisSettings settings, NormSynthesisMetrics nsMetrics,
			OmegaFunction oFunction, List<Norm> poolOfNorms) {

		NormSynthesisStrategy.Option option = settings.getNormSynthesisStrategy();

		this.settings = settings;
		this.metrics = nsMetrics;
		this.omegaFunction = oFunction;

		/* Create default metrics */
		if(metrics == null) {
			this.metrics = new DefaultNormSynthesisMetrics(this);
		}

		/* Create omega function to retrieve the normative system
		 * from the normative network */
		if(omegaFunction == null) {
			this.omegaFunction = new DefaultOmegaFunction();
		}
		this.nNetwork.setOmegaFunction(this.omegaFunction);

		/* Create norms reasoners */
		this.normReasoner = new NormReasoner(this.settings.getSystemGoals(), 
				this.predDomains, this.dmFunctions, this.metrics);

		this.normEvaluationReasoner = new NormReasoner(this.settings.getSystemGoals(), 
				this.predDomains, this.dmFunctions, this.metrics);

		this.setSynthesisStrategy(option);

		/* Add default normative system */
		this.strategy.addDefaultNormativeSystem(poolOfNorms);
		
		/* Finally setup the norm synthesis strategy */
		this.strategy.setup(this);
	}

	/**
	 * Adds a sensor to the monitor of the norm synthesis machine
	 * 
	 * @param sensor the sensor to add to monitor
	 * @see Monitor
	 * @see Sensor
	 */
	public void addSensor(Sensor sensor) {
		this.monitor.addSensor(sensor);
	}

	/**
	 * Performs the norm synthesis cycle by executing
	 * the norm synthesis strategy
	 * 
	 * @return the {@code NormativeSystem} resulting from 
	 * 					the norm synthesis cycle. The normative system
	 * 					is computed by the omega function, from the
	 * 					normative network
	 * @see NormSynthesisStrategy
	 * @see NormativeSystem
	 */
	public NormativeSystem executeStrategy(double timeStep) 
			throws IncorrectSetupException {

		/* First, check that the NSM has been correctly setup */
		if(this.firstExecution) {
			this.firstExecution= false;
			this.checkSetup();

			/* Create the GUI if required) */
			if(this.useGui) {
				tracer = new NormSynthesisInspector(this);
				tracer.show();	
			}
		}

		/* Executes the strategy and get the resulting normative system */
		NormativeSystem ns = this.strategy.execute();
		this.metrics.update(timeStep);

		if(this.useGui) {
			tracer.refresh();
		}
		return ns;
	}

	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 */
	private void setSynthesisStrategy(NormSynthesisStrategy.Option option) {
		int genStep = settings.getOptimisticNormGeneralisationStep();
		NormGeneralisationMode genMode = 
				settings.getOptimisticNormGeneralisationMode();

		/* Create and set norm synthesis strategy */
		switch(option) {

		case BASE:		this.configureAsBASE(settings);			break;
		case IRON:		this.configureAsIRON(settings);			break;
		case SIMON:		this.configureAsSIMON(settings);		break;
		case LION:		this.configureAsLION(settings);			break;
		case DESMON:	this.configureAsDESMON(settings);		break;

		default:
			break;
		}

		/* Create generic synthesis strategy */
		if(option != NormSynthesisStrategy.Option.Example &&
				option != NormSynthesisStrategy.Option.Custom) {

			this.strategy = new GenericSynthesisStrategy(this, genMode, genStep);
		}
	}

	/**
	 * BASE does not generalise norms nor exploit norm synergies  
	 */
	private void configureAsBASE(NormSynthesisSettings settings) {
		settings.setNormEvaluationMechanism(NormEvaluator.Mechanism.MovingAverage);
		settings.setPursueCompactness(false);
		settings.setPursueLiberality(false);
		settings.setNormGenerationApproach(Approach.Reactive);
//		settings.setNormsDefaultUtility(0.5);
		settings.setMinEvaluationsToClassifyNorms(1);
	}

	/**
	 * IRON performs conservative norm generalisations  
	 */
	private void configureAsIRON(NormSynthesisSettings settings) {
		settings.setNormEvaluationMechanism(NormEvaluator.Mechanism.BollingerBands);
		settings.setNormGeneralisationApproach(NormGeneraliser.Approach.Conservative);
		settings.setPursueCompactness(true);
		settings.setPursueLiberality(false);
		settings.setNormGenerationApproach(Approach.Reactive);
//		settings.setNormsDefaultUtility(0.5);
		settings.setMinEvaluationsToClassifyNorms(1);
	}

	/**
	 * SIMON performs optimistic norm generalisations   
	 */
	private void configureAsSIMON(NormSynthesisSettings settings) {
		settings.setNormEvaluationMechanism(NormEvaluator.Mechanism.BollingerBands);
		settings.setNormGeneralisationApproach(NormGeneraliser.Approach.Optimistic);
		settings.setPursueCompactness(true);
		settings.setPursueLiberality(false);
		settings.setNormGenerationApproach(Approach.Reactive);
//		settings.setNormsDefaultUtility(0.5);
		settings.setMinEvaluationsToClassifyNorms(1);
	}

	/**
	 * LION generalises norms and exploits norm synergies  
	 */
	private void configureAsLION(NormSynthesisSettings settings) {
		settings.setNormEvaluationMechanism(NormEvaluator.Mechanism.MovingAverage);
		settings.setNormGeneralisationApproach(NormGeneraliser.Approach.Optimistic);
		settings.setPursueCompactness(true);
		settings.setPursueLiberality(true);
		settings.setNormGenerationApproach(Approach.Reactive);
		settings.setMinEvaluationsToClassifyNorms(1);
//	settings.setNormsDefaultUtility(0.5);
	}

	/**
	 * DESMON generalises norms but does not exploit norm synergies  
	 */
	private void configureAsDESMON(NormSynthesisSettings settings) {
		settings.setNormEvaluationMechanism(NormEvaluator.Mechanism.MovingAverage);
		settings.setNormGeneralisationApproach(NormGeneraliser.Approach.Optimistic);
		settings.setNormGenerationApproach(Approach.Deliberative);
		settings.setPursueCompactness(true);
		settings.setPursueLiberality(false);
//		settings.setNormsDefaultUtility(0);
	}

	/**
	 * Checks the initial setup of the norm synthesis machine, ensuring
	 * that the user has added {@code sensors} to perceive the scenario,
	 * an {@code omega function} to compute the normative system from the
	 * normative network, and a norm synthesis {@code strategy}
	 * 
	 * @throws IncorrectSetupException if one of the following conditions hold:
	 * 					(1) no sensors have been added to the monitor; or
	 * 					(2) no omega function has been set; or
	 * 					(3) no strategy has been set
	 * @see IncorrectSetupException
	 */
	private void checkSetup() throws IncorrectSetupException {
		NormSynthesisStrategy.Option strategy = 
				this.settings.getNormSynthesisStrategy();

		/* Check the sensors and the omega function if the strategy is not custom */
		if(strategy != NormSynthesisStrategy.Option.Example &&
				strategy != NormSynthesisStrategy.Option.Custom) {

			if(this.monitor.getNumSensors() == 0) {
				throw new IncorrectSetupException("No sensors have been added yet");
			}
			if(this.omegaFunction == null) {
				throw new IncorrectSetupException("Omega function not defined yet");
			}	
		}
		/* Check that the strategy has been set */
		if(this.strategy == null) {
			throw new IncorrectSetupException("Strategy not defined yet. Use method"
					+ " \"setup()\" to set a strategy");
		}
	}

	//---------------------------------------------------------------------------
	// Getters
	//---------------------------------------------------------------------------

	/**
	 * Returns the norm synthesis settings
	 * 
	 * @return the norm synthesis settings
	 * @see NormSynthesisSettings
	 */
	public NormSynthesisSettings getNormSynthesisSettings() {
		return this.settings;
	}

	/**
	 * Returns an object {@code PredicatesDomains} that contains:
	 * 
	 * (1) 	all the possible predicates in the agents' contexts; and
	 * (2) 	the domain of each possible predicate, represented as
	 * 			a {@code Taxonomy}.
	 * 
	 * @return an object {@code PredicatesDomains} that contains all the
	 * 					possible predicates in the agents' contexts, and the domain
	 * 					of each possible predicate, represented as a {@code Taxonomy}.
	 * @see PredicatesDomains
	 */
	public PredicatesDomains getPredicatesDomains() {
		return this.predDomains;
	}

	/**
	 * Returns the domain functions that allow to perform
	 * norm synthesis for a specific domain
	 * 
	 * @return the domain functions
	 * @see DomainFunctions
	 */
	public DomainFunctions getDomainFunctions() {
		return this.dmFunctions;
	}

	/**
	 * Returns the norm synthesis metrics
	 * 
	 * @return the norm synthesis metrics
	 * @see NormSynthesisMetrics
	 */
	public NormSynthesisMetrics getNormSynthesisMetrics() {
		return this.metrics;
	}

	/**
	 * Returns the monitor that perceives the environment
	 * 
	 * @return the monitor that perceives the environment
	 * @see Monitor
	 */
	public Monitor getMonitor() {
		return this.monitor;
	}

	/**
	 * Returns the normative network
	 * 
	 * @return the normative network
	 * @see NormativeNetwork
	 */
	public NormativeNetwork getNormativeNetwork() {
		return nNetwork;
	}

	/**
	 * Returns the norm groups network
	 * 
	 * @return the norm groups network
	 * @see NormGroupNetwork
	 */
	public NormGroupNetwork getNormGroupNetwork() {
		return nGroupNetwork;
	}

	/**
	 * Returns the omega function, that computes a normative 
	 * system from a normative network
	 * 
	 * @return the omega function
	 * @see OmegaFunction
	 */
	public OmegaFunction getOmegaFunction() {
		return this.omegaFunction;
	}

	/**
	 * Returns the norm synthesis strategy
	 * 
	 * @return the norm synthesis strategy
	 * @see NormSynthesisStrategy
	 */
	public NormSynthesisStrategy getStrategy() {
		return strategy;
	}

	/**
	 * Returns the norm evaluation dimensions
	 * 
	 * @return the norm evaluation dimensions
	 * @see EvaluationCriteria
	 */
	public List<EvaluationCriteria> getNormEvaluationDimensions() {
		return this.evCriteria;
	}

	/**
	 * Returns the norm reasoner
	 * 
	 * @return the norm reasoner
	 */
	public NormReasoner getNormReasoner() {
		return this.normReasoner;
	}

	/**
	 * 
	 * @return
	 */
	public NormReasoner getNormEvaluationReasoner() {
		return this.normEvaluationReasoner;
	}

	/**
	 * Use Graphical User Interface (norms tracer)?
	 * 
	 * @return <tt>true</tt> if the NSM must use GUI 
	 */
	public boolean isGUI() {
		return this.useGui;
	}

	/**
	 * Returns the norms tracer
	 * 
	 * @return the norms tracer
	 */
	public NormSynthesisInspector getTracer() {
		return this.tracer;
	}

	/**
	 * Returns the random values generator
	 * 
	 * @return the random values generator
	 */
	public Random getRandom() {
		return this.random;
	}
}
