package es.csic.iiia.normlab.launcher.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import es.csic.iiia.normlab.launcher.ui.NormLabFrame.NormLabSimulator;

public class NormLabExperimentsManager {

	private String dirTrafficParams = "repast-settings/TrafficJunction.rs/";
	private String dirOnlinecommParams = "repast-settings/OnlineCommunity.rs/";
	private String dirExperiments = "experiments/";
	private String dirAvailableExps = "experiments/available/";
	private String dirTempExps = dirExperiments + ".tmp/";
	
	private String paramsFilename = "parameters";
	private String trafficParamsExpFilename = paramsFilename + ".traffic";
	private String onlinecommParamsExpFileName = paramsFilename + ".onlinecomm";

	private RepastXMLManager expConfigManager;
	private RepastXMLManager trafficParamsManager;
	private RepastXMLManager onlineCommParamsManager;

	/**
	 * @throws IOException 
	 * 
	 */
	public void createNewExperimentFiles() throws IOException {

		/* Create a copy of parameters.xml of both the traffic simulator 
		 * and the online community simulator */
		this.copyFile(dirTrafficParams, paramsFilename + ".xml", 
				dirTempExps, trafficParamsExpFilename);

		this.copyFile(dirOnlinecommParams, paramsFilename + ".xml", 
				dirTempExps, onlinecommParamsExpFileName);

		/* Create configuration managers such copies */
		this.trafficParamsManager = 
				new RepastXMLManager(dirTempExps, trafficParamsExpFilename);

		this.onlineCommParamsManager = 
				new RepastXMLManager(dirTempExps, onlinecommParamsExpFileName);
	}


	/**
	 * 
	 * @param experimentName
	 */
	public void createNewExperimentConfiguration(String experimentName) {
		this.expConfigManager = new RepastXMLManager(dirTempExps, 
				experimentName + ".xml");
  }
	
	/**
	 * 
	 * @param experimentFilename
	 * @throws Exception 
	 */
	public void loadExperimentConfiguration(String experimentFilename)
			throws Exception {

		/* Import traffic and online communities parameter files as 
		 * temporal parameter files */
		this.importExperimentFiles(experimentFilename);

		/* Create new experiment configuration managers */
		this.expConfigManager = new RepastXMLManager(dirTempExps, 
				experimentFilename);
		
		/* Create new managers for the temporal parameter files */
		this.trafficParamsManager = 
				new RepastXMLManager(dirTempExps, trafficParamsExpFilename);

		this.onlineCommParamsManager = 
				new RepastXMLManager(dirTempExps, onlinecommParamsExpFileName);
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void saveExperimentConfiguration(String experimentName) 
			throws Exception {

		/* Copy temporal experiment files to the the available experiments folder */
		this.exportExperimentFiles(experimentName);
		
		/* Delete temporal experiment files */
		this.deleteTempExperimentFiles(experimentName);
	}
	
	/**
	 * Create copes of parameters.xml of both the traffic simulator 
	 * and the online community simulator
	 * 
	 * @param experimentFilename the name of the experiment to load
	 * @throws IOException 
	 */
	private void importExperimentFiles(String experimentFilename) 
			throws IOException {
		
		String expName = experimentFilename.substring(0,
				experimentFilename.length()-4);
		
		this.copyFile(dirAvailableExps, experimentFilename, 
				dirTempExps, experimentFilename);
		
		this.copyFile(dirAvailableExps, expName + ".parameters.traffic", 
				dirTempExps, trafficParamsExpFilename);

		this.copyFile(dirAvailableExps, expName + ".parameters.onlinecomm", 
				dirTempExps, onlinecommParamsExpFileName);
	}


	/**
	 * Create copies of parameters.xml of both the traffic simulator 
	 * and the online community simulator
	 * 
	 * @param experimentFilename the name of the experiment to load
	 * @throws IOException 
	 */
	public void exportExperimentFiles(String experimentName) throws IOException {
		
		/* Copy temporal experiment parameter files to the 
		 * available experiments folder */
		this.copyFile(dirTempExps, experimentName + ".xml", 
				dirAvailableExps, experimentName + ".xml");
		
		this.copyFile(dirTempExps, trafficParamsExpFilename,  
				dirAvailableExps, experimentName + ".parameters.traffic");

		this.copyFile(dirTempExps, onlinecommParamsExpFileName,  
				dirAvailableExps, experimentName + ".parameters.onlinecomm");
		
		/* Reload experiment parameter managers */
		this.trafficParamsManager = new RepastXMLManager(dirAvailableExps, 
				experimentName + ".parameters.traffic");
		
		this.onlineCommParamsManager = new RepastXMLManager(dirAvailableExps, 
				experimentName + ".parameters.onlinecomm");
	}
	
	/**
	 * 
	 * @param experimentName
	 */
	private void deleteTempExperimentFiles(String experimentName) {
		this.deleteFile(dirTempExps, experimentName + ".xml");
		this.deleteFile(dirTempExps, trafficParamsExpFilename);
		this.deleteFile(dirTempExps, onlinecommParamsExpFileName);	  
  }

	/**
	 * @throws IOException 
	 * 
	 */
	private void copyFile(String srcParamsDir, String srcParamsFileName, 
			String destParamsDir, String destParamsFilename) throws IOException {

		File paramsFile = new File(srcParamsDir + srcParamsFileName);
		File destParamsFile = new File(destParamsDir + destParamsFilename);
		FileUtils.copyFile(paramsFile, destParamsFile, false);
	}
	
	/**
	 * 
	 * @param paramsDir
	 * @param paramsFilename
	 */
	private void deleteFile(String paramsDir, String paramsFilename) {
		File file = new File(paramsDir + paramsFilename);
		file.delete();
	}
	
	/**
	 * 
	 * @return
	 */
	public RepastXMLManager getExperimentConfigManager() {
		return this.expConfigManager;
	}
	
	/**
	 * 
	 * @param sim
	 * @return
	 */
	public RepastXMLManager getParamsManager(NormLabSimulator sim) {
		switch(sim) {
		case TrafficJunction: return this.trafficParamsManager;
		case OnlineCommunity: return this.onlineCommParamsManager;
		}
		return null;
	}
	

}
