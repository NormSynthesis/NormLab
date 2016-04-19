package es.csic.iiia.normlab.onlinecomm.config;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.normlab.onlinecomm.nsm.GComplaints;
import es.csic.iiia.normlab.synthesis.config.DefaultNormSynthesisSettings;
import es.csic.iiia.nsm.config.Goal;

/**
 * 
 * @author Javi
 *
 */
public class OnlineCommunitySynthesisSettings extends DefaultNormSynthesisSettings {

	private List<Goal> systemGoals;
	
	/**
	 * 
	 */
	public OnlineCommunitySynthesisSettings() {
		Goal gComplaints = new GComplaints();
		this.systemGoals = new ArrayList<Goal>();
		this.systemGoals.add(gComplaints);
	}
	
	/**
	 * 
	 */
	@Override
	public List<Goal> getSystemGoals() {
		return systemGoals;
	}
}
