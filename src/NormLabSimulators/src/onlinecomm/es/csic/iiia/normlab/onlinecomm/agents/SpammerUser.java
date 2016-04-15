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
 * Spammer agent
 * @author davidsanchezpinsach
 *
 */
public class SpammerUser extends OnlineCommunityUser{
	
	IContent content;
	
	//Default SpammerAgent
	public SpammerUser(OnlineCommunityUserReasoner reasoner, 
			PredicatesDomains predDomains, ContinuousSpace<Object> space, 
			Grid<Object> grid, int row,	OnlineCommunityNormEngine normEngine,
			OnlineCommunityFactFactory factFactory) {

		super(reasoner, predDomains, space, grid, row, null, null, 
				null, normEngine, factFactory);	
		
		upLoadProfile = new UploadProfile(1, 0, 1, 0, 0, 0);
		viewProfile = new ViewProfile(0.5, 0.4, 0.1, 1);
		complaintProfile = new ComplaintProfile(0.0, 0.2, 0.1, 0.3);	
	}
	
	public SpammerUser(OnlineCommunityUserReasoner reasoner, 
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
