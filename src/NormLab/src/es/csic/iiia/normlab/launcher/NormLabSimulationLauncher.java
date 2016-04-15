package es.csic.iiia.normlab.launcher;

import es.csic.iiia.normlab.launcher.model.RepastRunner;
import es.csic.iiia.normlab.launcher.ui.NormLabFrame.NormLabSimulator;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormLabSimulationLauncher implements Runnable {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private NormLabSimulator simulator;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param simulator
	 */
	public NormLabSimulationLauncher(NormLabSimulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * Starts the Repast simulation in this thread. It passes Repast the 
	 * directory that contains the settings of the scenario 
	 */
	public void run() {
		
		/* Prepare Repast arguments */
		String repastSettingsDir = "../NormLabSimulators/repast-settings/";
		String args = repastSettingsDir + simulator.toString() + ".rs";
		
		/* Start new Repast instance */
		try {			
			RepastRunner.execGUI(args);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
