package es.csic.iiia.normlab.traffic.config;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * Basic configuration of the traffic simulator 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class TrafficSimulatorSettings {

	//-----------------------------------------------------------------
	// Graphical User Interface settings
	//-----------------------------------------------------------------

	public static final float ROAD_POSITION 		= 0f;
	public static final float WALL_POSITION 		= 1f;
	public static final float NORM_FULFILMENT 	= 2f;
	public static final float NORM_INFRINGEMENT = 3f;

	//-----------------------------------------------------------------
	// Simulator settings
	//-----------------------------------------------------------------

	/* Simulation number */
	private int numExec;
	
	/* Frequency for cars to enter in the scenario */
	private int newCarsFrequency;

	/* Number of cars to add to the scenario at the same time */
	private int numCarsToAdd;

	/* Random seed */
	private int randomSeed;

	/* Defines if the simulator uses a GUI */
	private boolean useGui;

	/* Max ticks to simulate */
	private long maxTicks;

	/* Use traffic lights or not? */
	private boolean useTrafficLights;

	/* Use just traffic lights? */
	private boolean useOnlyTrafficLights;

	/* Path for data */
	private String outputDataPath;

	/* Name for the norms file. This file saves norms resulting  
	 * from different executions */
	private String totalNormsFilename;

	/* Name for the norms file. This file saves norms resulting 
	 * from different executions */
	private String finalNormsFilename;

	/* Name for the norm sets file. This file saves norm sets resulting 
	 * from different executions */
	private String finalNormSetsFilename;

	/* Probability for an agent to infringe an applicable norm */
	private double normInfringementRate;

	//-----------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------

	/**
	 * Constructor
	 */
	public TrafficSimulatorSettings() {
		init();	
	}
	
	/**
	 * Loads the traffic simulator settings 
	 */
	private void init() {
		Parameters p = RunEnvironment.getInstance().getParameters();

		this.randomSeed = (Integer)p.getValue("randomSeed");
		this.newCarsFrequency = (Integer)p.getValue("newCarsFreq");
		this.numCarsToAdd = (Integer)p.getValue("numCarsToAdd");
		this.maxTicks = (Long)p.getValue("maxTicks");
		this.useGui = (Boolean)p.getValue("useGui");
		this.normInfringementRate = (Double)p.getValue("normInfringementRate");
		this.outputDataPath = (String)p.getValue("outputDataPath");
		this.totalNormsFilename = (String)p.getValue("totalNormsFilename");
		this.finalNormsFilename = (String)p.getValue("finalNormsFilename");
		this.finalNormSetsFilename = (String)p.getValue("finalNormSetsFilename");
		this.numExec = (Integer)p.getValue("numExec");
		this.useTrafficLights = (Boolean)p.getValue("useTrafficLights");
		this.useOnlyTrafficLights = (Boolean)p.getValue("useOnlyTrafficLights");
	}

	/**
	 * @return the newCarsFrequency
	 */
	public int getNewCarsFrequency() {
		return newCarsFrequency;
	}

	/**
	 * @return the numCarsToAdd
	 */
	public int getNumCarsToAdd() {
		return numCarsToAdd;
	}

	/**
	 * @return the randomSeed
	 */
	public int getRandomSeed() {
		return randomSeed;
	}

	/**
	 * @return the useGui
	 */
	public boolean useGui() {
		return useGui;
	}

	/**
	 * @return the maxTicks
	 */
	public long getMaxTicks() {
		return maxTicks;
	}

	/**
	 * @return the useTrafficLights
	 */
	public boolean useTrafficLights() {
		return useTrafficLights;
	}

	/**
	 * @return the useOnlyTrafficLights
	 */
	public boolean useOnlyTrafficLights() {
		return useOnlyTrafficLights;
	}

	/**
	 * @return the outputDataPath
	 */
	public String getOutputDataPath() {
		return outputDataPath;
	}

	/**
	 * @return the totalNormsFilename
	 */
	public String getTotalNormsFilename() {
		return totalNormsFilename;
	}

	/**
	 * @return the finalNormsFilename
	 */
	public String getFinalNormsFilename() {
		return finalNormsFilename;
	}

	/**
	 * @return the finalNormSetsFilename
	 */
	public String getFinalNormSetsFilename() {
		return finalNormSetsFilename;
	}

	/**
	 * @return the normInfringementRate
	 */
	public double getNormInfringementRate() {
		return normInfringementRate;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumExec() {
		return this.numExec;
	}
	
}
