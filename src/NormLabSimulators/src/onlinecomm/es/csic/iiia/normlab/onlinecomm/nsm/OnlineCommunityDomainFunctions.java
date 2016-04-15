package es.csic.iiia.normlab.onlinecomm.nsm;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityFactFactory;
import es.csic.iiia.normlab.onlinecomm.content.IContent;
import es.csic.iiia.normlab.onlinecomm.nsm.agent.OnlineCommunityUserAction;
import es.csic.iiia.normlab.onlinecomm.nsm.agent.OnlineCommunityUserContext;
import es.csic.iiia.normlab.onlinecomm.nsm.perception.CommunityView;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.sensing.View;
import es.csic.iiia.nsm.sensing.ViewTransition;

public class OnlineCommunityDomainFunctions implements DomainFunctions {

	//-------------------------------------------------------------------------
	// Attributes
	//-------------------------------------------------------------------------

	private OnlineCommunityFactFactory factFactory;

	//-------------------------------------------------------------------------
	// Methods
	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param factFactory
	 */
	public OnlineCommunityDomainFunctions(OnlineCommunityFactFactory factFactory) {
		this.factFactory = factFactory;
	}

	/**
	 * 
	 */
	@Override
	public boolean isConsistent(SetOfPredicatesWithTerms agentContext) {
		return true;
	}

	/**
	 * 
	 */
	@Override
	public EnvironmentAgentContext agentContext(long agentId, View view) {
		CommunityView sv = (CommunityView) view;
		int uploadListSize = sv.getActualUploadList().size();
		OnlineCommunityUserContext agentContext = null;

		for(int i = 0 ; i < uploadListSize ; i++){
			if(sv.getActualUploadList().get(i).getCreatorAgent() == agentId) {
				IContent content = sv.getActualUploadList().get(i);

				agentContext = new OnlineCommunityUserContext(agentId, content.getSection(),
						content.getType(), content.getTypeDescription());

				SetOfPredicatesWithTerms desc = 
						factFactory.generatePredicates(agentContext);

				agentContext.setDescription(desc);
			}
		}
		return agentContext;
	}

	/**
	 * 
	 */
	@Override
	public List<EnvironmentAgentAction> agentAction(long agentId,
			ViewTransition viewTransition) {

		List<EnvironmentAgentAction> actions = new ArrayList<EnvironmentAgentAction>();
		
		CommunityView view = (CommunityView)viewTransition.getView(0);
		for(IContent content : view.getActualUploadList()) {
			if(content.getCreatorAgent() == agentId) {
				actions.add(OnlineCommunityUserAction.Upload);
			}
		}
		return actions;
	}

	/**
	 * 
	 */
	@Override
	public List<Conflict> getConflicts(Goal goal, ViewTransition viewTransition) {
		CommunityView sv = (CommunityView) viewTransition.getView(0);
		List<Conflict> conflicts = new ArrayList<Conflict>();

		if(!(goal instanceof GComplaints)) { 
			return conflicts;
		}	

		List<IContent> contents = new ArrayList<IContent>();
		for(IContent content : sv.getActualComplaintList()) {
			if(!contents.contains(content)) {
				contents.add(content);
			}
		}	

		for(IContent content : contents){
			List<IContent> fakeUploads = new ArrayList<IContent>();
			fakeUploads.add(content);

			View fakeView = new CommunityView(fakeUploads, null, null);
			ViewTransition fakeViewTrans = new ViewTransition(
					viewTransition.getSensor());
			fakeViewTrans.setView(-1, fakeView);
			fakeViewTrans.setView(0, fakeView);

			List<Long> conflictingAgents = new ArrayList<Long>();
			conflictingAgents.add((long)content.getCreatorAgent());

			Conflict conflict = new Conflict(viewTransition.getSensor(), 
					fakeView, fakeViewTrans, conflictingAgents);
			conflicts.add(conflict);
		}
		return conflicts;
	}

	/**
	 * 
	 */
	public List<Conflict> getConflicts(Goal goal, ViewTransition viewTransition,
			long agentId) {

		List<Conflict> allConflicts = this.getConflicts(goal, viewTransition);
		List<Conflict> agentConflicts = new ArrayList<Conflict>();

		for(Conflict conflict : allConflicts) {
			if(conflict.getConflictingAgents().contains(agentId)) {
				agentConflicts.add(conflict);
			}
		}
		return agentConflicts;
	}

	/**
	 * 
	 */
	@Override
	public boolean hasConflict(View view, long agentId, Goal goal) {
		CommunityView sv = (CommunityView) view;

		if(!(goal instanceof GComplaints)) { 
			return false;
		}

		for(IContent content : sv.getActualComplaintList()){
			long contentCreatorId = content.getCreatorAgent();

			if(contentCreatorId == agentId) {
				return true;
			}
		}
		return false;
	}


	//---------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 */
	@Override
	public JPanel getNormDescriptionPanel(Norm norm) {
		JPanel normDescPanel = new JPanel();

		return normDescPanel;
	}

	/**
	 * 
	 */
	@Override
  public List<Long> getConflictSource(Conflict conflict) {
	  // TODO Auto-generated method stub
	  return null;
  }
}
