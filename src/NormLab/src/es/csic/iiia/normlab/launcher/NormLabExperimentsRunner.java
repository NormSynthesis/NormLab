package es.csic.iiia.normlab.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import es.csic.iiia.normlab.launcher.model.RepastRunner;
import es.csic.iiia.normlab.launcher.model.RepastXMLManager;
import es.csic.iiia.normlab.launcher.utils.ExperimentDataProcessor;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormLabExperimentsRunner implements Runnable {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private ExperimentDataProcessor expDataProcessor;
	private RepastXMLManager configManager;
	private RepastXMLManager paramsManager;
	
	private List<String> experiments;

	private String repastDir = "../NormLabSimulators/repast-settings/";
	private String availableExpDir = "experiments/available/";
	private String performedExpDir = "experiments/performed/";
	private String tempExpDir = "experiments/.tmp/";
	private String outputDataDir = "output/";
	
	private String expOutputDir;
	private String expOutputLogsDir;
	private String expOutputDataDir;
	private String experimentName;
	private String simulator;
	private String scenarioDir;
	private String logFilePath;

	private String[] repastArgs;
	private File batchParamsFile;

	private int numSimulations;
	private boolean computeAvgs;
	private boolean computeMeans;
	private boolean createCharts;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 * @param experiments
	 */
	public NormLabExperimentsRunner(List<String> experiments) {
		this.experiments = experiments;
		
		this.expDataProcessor = new ExperimentDataProcessor();
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void run() {
		boolean isInterrupted = false;
		
		try {
			
			/* Launch each experiment received by parameter */
			if(!isInterrupted) {
				for(String experiment : experiments) {
					experimentName = experiment.substring(0, experiment.length() - 4);
	
					/* Retrieve experiment configuration */
					configManager =	new RepastXMLManager(availableExpDir, 
							experiment);
	
					/* Load experiment configuration and prepare repast arguments */
					this.loadConfiguration(experiment);
					this.prepareRepastArgs(experiment);
	
					/* Generate folders to put the experiments' data files */
					this.generateOutputExpFolders();
	
					System.out.println("Running experiment '" + experimentName + 
							"' (" + numSimulations + " simulations in total)");
					
					/* Run N Repast simulations */
					for(int i = 1; i <= this.numSimulations; i++) {
						isInterrupted = Thread.currentThread().isInterrupted();
						
						if(isInterrupted) {
							throw new InterruptedException("");
						}
						else {
							System.out.println("  > Running simulation number " + i);
							
							this.generateLogFilename(i);
		
							/* Run simulation */
							RepastRunner.execBatch(repastArgs[1], repastArgs[2], logFilePath);
							
							/* Move the data files output by Repast to 
							 * their corresponding folder */
							this.moveDataFiles(i);
						}
					}
				
					System.out.println();
					System.out.println("Experiment '" + experimentName + "' has finished. "
							+ "Post processing experiment data...");
					
					/* Post process experiment data */
					this.postProcessExperiments();
					
					System.out.println("Experiment '" + experimentName + "' postprocessed.");
					System.out.println();
				}
				System.out.println("Experiments finished");
			}
		}
		
		/* If the user interrupts the experiments, kill currently 
		 * running process and notify the user */
		catch(InterruptedException e) {
			RepastRunner.killCurrentProcess();
			
			System.out.println();
			System.out.println("Experiments stopped. Please close this window.");
		}
		catch (Exception e) {
	    e.printStackTrace();
    }
	}

	/**
	 * 
	 * @param experiment
	 * @throws Exception
	 */
	private void loadConfiguration(String experiment)	throws Exception {
		simulator = configManager.getAttribute("simulator");
		numSimulations = Integer.valueOf(configManager.getAttribute("simulations"));
		computeAvgs = Boolean.valueOf(configManager.getAttribute("computeAvgs"));
		computeMeans = Boolean.valueOf(configManager.getAttribute("computeMeans"));
		createCharts = Boolean.valueOf(configManager.getAttribute("createCharts"));
	}

	/**
	 * 
	 * @throws IOException
	 */
	private void generateOutputExpFolders() throws IOException {
		expOutputDir = performedExpDir + experimentName + File.separator;
		expOutputLogsDir = expOutputDir + "logs" + File.separator;
		expOutputDataDir = expOutputDir + "data" + File.separator;

		File expOutputFolder = new File(expOutputDir);
		File expOutputLogsFolder = new File(expOutputLogsDir);
		File expOutputDataFolder = new File(expOutputDataDir);

		this.backupPreviousExpFiles(expOutputFolder);

		/* Create experiment output folders */
		expOutputFolder.mkdir();
		expOutputLogsFolder.mkdir();
		expOutputDataFolder.mkdir();
	}

	/**
	 * 
	 * @param expOutputLogsFolder
	 * @param expOutputDataFolder
	 * @throws IOException 
	 */
	private void backupPreviousExpFiles(File expOutputFolder) 
			throws IOException {
		
		if(expOutputFolder.exists()) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			Date date = new Date();
			
			File backupExpFolder = new File(expOutputFolder.getPath() + 
					"-backup-" + dateFormat.format(date));
			FileUtils.moveDirectory(expOutputFolder, backupExpFolder);
		}  
  }

	/**
	 * 
	 * @param exec
	 * @return
	 */
	private void generateLogFilename(int numSimulation) {
		logFilePath = expOutputLogsDir + File.separator + 
				experimentName + "-exec-" + numSimulation + ".log";
	}


	/**
	 * @throws IOException 
	 * 
	 */
	private void moveDataFiles(int numSim) throws IOException {
		
		/* Retrieve simulator output path */
		String outputPath = outputDataDir;
		if(simulator.equals("TrafficJunction")) {
			outputPath += "traffic"; 
		}
		else if(simulator.equals("OnlineCommunity")) {
			outputPath += "onlinecomm";
		}
		
		/* Retrieve all .dat files in the directory */
		File outputDir = new File(outputPath);
		File[] files = outputDir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().contains(".dat");
		    }
		});
		
		/* Move all files to experiment data directory */
		for(File file : files) {
			File destFile = new File(expOutputDataDir + 
					file.getName() + "." + numSim);
			
			Path from = file.toPath(); 
			Path to = destFile.toPath();
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
//			FileUtils..moveFile(file, destFile);
		}
  }
	

	/**
	 * 
	 */
	private void postProcessExperiments() {
		if(this.computeAvgs) {
			
		}
	}
	
	/**
	 * 
	 * @param expFilename
	 * @return
	 * @throws Exception 
	 */
	private void prepareRepastArgs(String expFilename) throws Exception {
		String paramsFilename = "";

		/* Retrieve the experiment's parameters file and its scenario dir */
		if(simulator.equals("TrafficJunction")) {
			paramsFilename = experimentName + ".parameters.traffic";
		}
		else if(simulator.equals("OnlineCommunity")) {
			paramsFilename = experimentName + ".parameters.onlinecomm";
		}

		scenarioDir = repastDir + simulator + ".rs";

		/* Load experiment parameters and format them in such a way that
		 * Batch Repast can understand them */
		paramsManager = new RepastXMLManager(availableExpDir, paramsFilename);
		batchParamsFile = paramsManager.generateBatchParams(tempExpDir +
				"batch_params.xml");

		repastArgs = new String[3];
		repastArgs[0] = "-params";
		repastArgs[1] = batchParamsFile.getPath();
		repastArgs[2] = scenarioDir;
	}
}
