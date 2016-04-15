package es.csic.iiia.normlab.traffic.config;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.normlab.synthesis.config.DefaultNormSynthesisSettings;
import es.csic.iiia.nsm.config.Goal;

/**
 * 
 * @author Javi
 *
 */
public class TrafficNormSynthesisSettings extends DefaultNormSynthesisSettings {

	private List<Goal> systemGoals;
	
	/**
	 * 
	 */
	public TrafficNormSynthesisSettings() {
		Goal gCols = new Gcols();
		this.systemGoals = new ArrayList<Goal>();
		this.systemGoals.add(gCols);
	}
	
	/**
	 * 
	 */
	@Override
	public List<Goal> getSystemGoals() {
		return systemGoals;
	}
}
