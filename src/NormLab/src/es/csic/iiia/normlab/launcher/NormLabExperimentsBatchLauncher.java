package es.csic.iiia.normlab.launcher;

import java.util.Arrays;

public class NormLabExperimentsBatchLauncher {


	/**
	 * Starts the Repast simulation in this thread. It passes Repast the 
	 * directory that contains the settings of the scenario 
	 */
	public static void main(String[] args) {
		NormLabExperimentsRunner expLauncher =
				new NormLabExperimentsRunner(Arrays.asList(args));

		try {
			Thread t = new Thread(expLauncher);
			t.start();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
