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
 * Pornographic agent
 * @author davidsanchezpinsach
 *
 */
public class PornographicUser extends OnlineCommunityUser {

	IContent content;


	/**
	 * 
	 * @param predDomains
	 * @param space
	 * @param grid
	 * @param row
	 */
	public PornographicUser(OnlineCommunityUserReasoner reasoner, 
			PredicatesDomains predDomains, ContinuousSpace<Object> space, 
			Grid<Object> grid, int row,	OnlineCommunityNormEngine normEngine,
			OnlineCommunityFactFactory factFactory, int numViewsPerTick) {

		super(reasoner, predDomains, space, grid, row, null, null, 
				null, normEngine, factFactory, numViewsPerTick);

		upLoadProfile = new UploadProfile(0.6, 0.2, 0, 0.6, 0, 0);
		viewProfile = new ViewProfile(0.4, 0.2, 0.4, 1);
		complaintProfile = new ComplaintProfile(0, 0, 0, 0);
	}

	/**
	 * 
	 * @param predDomains
	 * @param space
	 * @param grid
	 * @param row
	 * @param uProfile
	 * @param vProfile
	 * @param cProfile
	 */
	public PornographicUser(OnlineCommunityUserReasoner reasoner, 
			PredicatesDomains predDomains, ContinuousSpace<Object> space,
			Grid<Object> grid, int row, UploadProfile uProfile, 
			ViewProfile vProfile, ComplaintProfile cProfile, 
			OnlineCommunityNormEngine normEngine,
			OnlineCommunityFactFactory factFactory, int numViewsPerTick) {
		
		super(reasoner, predDomains, space, grid, row, 
				uProfile, vProfile, cProfile, normEngine,
				factFactory, numViewsPerTick);

		upLoadProfile.generateRandomUploadList(grid); 
	}
}
