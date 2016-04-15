package es.csic.iiia.normlab.launcher.model;

import java.io.File;
import java.io.IOException;

import repast.simphony.batch.BatchMain;
import repast.simphony.runtime.RepastMain;
import es.csic.iiia.normlab.launcher.utils.StreamGobbler;
import es.csic.iiia.normlab.launcher.utils.StreamLogger;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public final class RepastRunner {

	private static String javaHome;
	private static String javaBin;
	private static String classpath;
	private static String repastUIClass;
	private static String repastBatchClass;

	private static Process process;
	
	/**
	 * Prepare paths for Repast execution
	 */
	static {
		javaHome = System.getProperty("java.home");
		javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		classpath = System.getProperty("java.class.path");
		repastUIClass = RepastMain.class.getCanonicalName();
		repastBatchClass = BatchMain.class.getCanonicalName();
	}

	/**
	 * 
	 * @param scenarioDir
	 * @return the exit value of the executed Repast process
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int execGUI(String scenarioDir) 
			throws IOException, InterruptedException {

		/* Prepare Repast user path */
		String runtimePath = classpath;
		runtimePath = addUserClasspath(runtimePath);
		
		/* Create Repast process and execute it */
		ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", runtimePath,
				repastUIClass, scenarioDir); 

		process = builder.start();

		/* Redirect output stream and error stream to the current system output */
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
		outputGobbler.start();
		errorGobbler.start();

		/* Wait for the process to end */
		process.waitFor();
		
		return process.exitValue();
	}

	/**
	 * 
	 * @param paramsFile
	 * @param scenarioDir
	 * @param logFilePath
	 * @return the exit value of the executed Repast process
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int execBatch(String paramsFile, String scenarioDir, 
			String logFilePath) throws IOException, InterruptedException {
		
		
		/* Prepare Repast Batch class path and user path */
		String runtimePath = classpath;
		runtimePath = addBatchClasspath(runtimePath);
		runtimePath = addUserClasspath(runtimePath);
		
		/* Create Repast process and execute it */
		ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", runtimePath,
				repastBatchClass, "-params", paramsFile, scenarioDir); 

		process = builder.start();

		/* Redirect output stream and error stream to a log file */
		File logFile = new File(logFilePath);
		StreamLogger errorLogger = new StreamLogger(process.getErrorStream(), logFile);
		StreamLogger outputLogger = new StreamLogger(process.getInputStream(), logFile);
		outputLogger.start();
		errorLogger.start();

		/* Wait for the process to end and return its return value */
		process.waitFor();
		
		return process.exitValue();
	}
	
	/**
	 * Kills the Repast process that is currently running 
	 */
	public static void killCurrentProcess() {
		if(process != null) {
			process.destroy();
		}
	}
	
	/**
	 * Adds to the current class path the libraries employed 
	 * by the user to develop its custom strategies 
	 */
	private static String addUserClasspath(String classpath) {
		
		/* Add NormLabSimulator's user libraries */
		File userLibFolder = new File("../NormLabSimulators/lib/user/");
		for(String libName : userLibFolder.list()) {
			classpath += File.pathSeparator + userLibFolder.getAbsolutePath()
					+ File.separator + libName;
		}	
		return classpath;
	}
	
	/**
	 * Adds to the current class path the necessary 
	 * libraries to run Repast in batch mode
	 */
	private static String addBatchClasspath(String classpath) {
		
		/* Add NormLabSimulator's bin folder and project libraries */
		File binFolder = new File("../NormLabSimulators/bin");
		classpath += File.pathSeparator + binFolder.getAbsolutePath();

		File libFolder = new File("../NormLabSimulators/lib/");
		for(String libName : libFolder.list()) {
			classpath += File.pathSeparator + libFolder.getAbsolutePath()
					+ File.separator + libName;
		}	
				
		/* Add NormLabSimulator's repast build path libraries */
		File buildpathLibsFolder =
				new File("../NormLabSimulators/lib/repast.simphony/buildpath/");
		
		for(String libName : buildpathLibsFolder.list()) {
			classpath += File.pathSeparator + buildpathLibsFolder.getAbsolutePath()
					+ File.separator + libName;
		}		
		return classpath;
	}
}