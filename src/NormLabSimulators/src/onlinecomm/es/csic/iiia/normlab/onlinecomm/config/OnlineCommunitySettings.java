package es.csic.iiia.normlab.onlinecomm.config;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * 
 * Class to make the necessary iron configurations.
 * 
 * @author Iosu Mendizabal
 *
 */
public class OnlineCommunitySettings {

	/* Agent population */
	private String population; 
	
	/* Random seed */
	private int randomSeed;

	/* Defines if the simulator uses a GUI */
	private boolean useGui;

	/* Max ticks to simulate */
	private long maxTicks;

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

	/* Simulation number */
	private int numExec;

	/* Size of the contents queue */
	private long contentQueueSize;
	
	/* Norms include predicate usr(id) or not? */
	private boolean normsHaveUserId;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 */
	public OnlineCommunitySettings() {
		init();
	}
	
	/**
	 * Constructor of the social configuration.
	 */
	public void init(){		
		Parameters p = RunEnvironment.getInstance().getParameters();

		this.population = (String)p.getValue("population");
		this.randomSeed = (Integer)p.getValue("randomSeed");
		this.maxTicks = (Long)p.getValue("maxTicks");
		this.useGui = (Boolean)p.getValue("useGui");
		this.normInfringementRate = (Double)p.getValue("normInfringementRate");
		this.outputDataPath = (String)p.getValue("outputDataPath");
		this.totalNormsFilename = (String)p.getValue("totalNormsFilename");
		this.finalNormsFilename = (String)p.getValue("finalNormsFilename");
		this.finalNormSetsFilename = (String)p.getValue("finalNormSetsFilename");
		this.numExec = (Integer)p.getValue("numExec");
		
		this.contentQueueSize = (Long)p.getValue("contentQueueSize");
		this.normsHaveUserId = (Boolean)p.getValue("normsHaveUserId");
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
	
	/**
	 * 
	 * @return
	 */
	public boolean normsHaveUserId() {
		return this.normsHaveUserId;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getContentQueueSize() {
		return this.contentQueueSize;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPopulation() {
		return this.population;
	}
}
