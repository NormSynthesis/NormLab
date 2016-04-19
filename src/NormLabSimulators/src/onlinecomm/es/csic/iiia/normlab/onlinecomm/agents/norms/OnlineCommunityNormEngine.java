package es.csic.iiia.normlab.onlinecomm.agents.norms;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.normlab.onlinecomm.content.IContent;
import es.csic.iiia.normlab.onlinecomm.nsm.perception.CommunityView;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.reasoning.NormEngine;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class OnlineCommunityNormEngine extends NormEngine {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------
	
	private DomainFunctions dmFunctions;
	private OnlineCommunityFactFactory ocFactFactory;
	
	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------
	
	/**
	 * 
	 * @param dmFunctions
	 */
	public OnlineCommunityNormEngine(DomainFunctions dmFunctions, 
			PredicatesDomains predDomains, OnlineCommunityFactFactory factFactory) {
		
	  super(predDomains);
	  this.dmFunctions = dmFunctions;
	  this.ocFactFactory = factFactory;
  }
	
	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsApplicableToContent(IContent content) {
		long agentId = (long) content.getCreatorAgent();
		List<IContent> fakeUploads = new ArrayList<IContent>();
		fakeUploads.add(content);

		CommunityView fakeView = new CommunityView(fakeUploads, null, null);
		EnvironmentAgentContext context = this.dmFunctions.agentContext(agentId, fakeView);
		SetOfPredicatesWithTerms predicates = ocFactFactory.generatePredicates(context);
		
		this.reset();
		this.addFacts(predicates);
		this.reason();
		
		return this.applicableNorms;
	}
}
