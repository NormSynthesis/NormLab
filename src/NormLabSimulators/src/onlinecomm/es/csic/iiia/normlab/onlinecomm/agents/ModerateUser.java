package es.csic.iiia.normlab.onlinecomm.agents;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityFactFactory;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityNormEngine;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityUserReasoner;
import es.csic.iiia.normlab.onlinecomm.agents.profile.ComplaintProfile;
import es.csic.iiia.normlab.onlinecomm.agents.profile.UploadProfile;
import es.csic.iiia.normlab.onlinecomm.agents.profile.ViewProfile;
import es.csic.iiia.normlab.onlinecomm.content.IContent;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;

/**
 * Moderate agent
 * @author davidsanchezpinsach
 *
 */
public class ModerateUser extends OnlineCommunityUser {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	IContent content;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 * @param predDomains
	 * @param space
	 * @param grid
	 * @param row
	 */
	public ModerateUser(OnlineCommunityUserReasoner reasoner, 
			PredicatesDomains predDomains, ContinuousSpace<Object> space, 
			Grid<Object> grid, int row,	OnlineCommunityNormEngine normEngine,
			OnlineCommunityFactFactory factFactory) {

		super(reasoner, predDomains, space, grid, row, null, null, 
				null, normEngine, factFactory);	
		upLoadProfile = new UploadProfile(0.4, 0.8, 0, 0, 0, 0);
		viewProfile = new ViewProfile(0.5, 0.1, 0.4, 1);
		complaintProfile = new ComplaintProfile(1, 1, 1, 1);	
	}

	/**
	 * 
	 * @param space
	 * @param grid
	 * @param row
	 * @param uProfile
	 * @param vProfile
	 * @param cProfile
	 */
	public ModerateUser(OnlineCommunityUserReasoner reasoner, 
			PredicatesDomains predDomains, ContinuousSpace<Object> space,
			Grid<Object> grid, int row, UploadProfile uProfile, 
			ViewProfile vProfile, ComplaintProfile cProfile, 
			OnlineCommunityNormEngine normEngine,
			OnlineCommunityFactFactory factFactory) {
		
		super(reasoner, predDomains, space, grid, row, 
				uProfile, vProfile, cProfile, normEngine,
				factFactory);

		upLoadProfile.generateRandomUploadList(grid); 
	}
}