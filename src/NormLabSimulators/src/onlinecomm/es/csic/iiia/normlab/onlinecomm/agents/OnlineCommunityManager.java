package es.csic.iiia.normlab.onlinecomm.agents;

import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityFactFactory;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityNormEngine;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityUserReasoner;
import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySettings;
import es.csic.iiia.normlab.onlinecomm.context.ContextData;
import es.csic.iiia.normlab.onlinecomm.nsm.OnlineCommunityDomainFunctions;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.TaxonomyOfTerms;
import es.csic.iiia.nsm.config.DomainFunctions;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class OnlineCommunityManager {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private ContextData contextData;
	private OnlineCommunitySettings ocSettings;

	private OnlineCommunityFactFactory factFactory;
	private OnlineCommunityNormEngine normEngine;
	private OnlineCommunityUserReasoner usersReasoner;

	private PredicatesDomains predDomains;
	private DomainFunctions dmFunctions;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 */
	public OnlineCommunityManager(ContextData contextData, 
			OnlineCommunitySettings ocSettings, 
			OnlineCommunityPopulation population) {

		this.contextData = contextData;
		this.ocSettings = ocSettings;

		/* Create predicates domains */
		this.createPredicatesDomains(population);

		/* Create class to generate facts to describe the 
		 * on-line community scenario */
		this.factFactory = new OnlineCommunityFactFactory(predDomains, ocSettings);

		/* Create online community domain functions */
		this.dmFunctions = new OnlineCommunityDomainFunctions(factFactory);

		/* Create norm engine to reason about norms */
		this.normEngine= new OnlineCommunityNormEngine(dmFunctions, 
				predDomains, factFactory);

		/* Create agents reasoner */
		this.usersReasoner = new OnlineCommunityUserReasoner(predDomains,
				contextData, ocSettings);
	}

	/**
	 * 
	 */
	public void step() {
		this.contextData.resetNumCurrentNonRegulatedComplaints();
		this.contextData.getActualUploadList().clear();
		this.contextData.getActualComplaintList().clear();
		this.contextData.getActualViewList().clear();
	}

	/**
	 * 
	 * @return
	 */
	public OnlineCommunityFactFactory getFactFactory() {
		return this.factFactory;
	}

	/**
	 * 
	 * @return
	 */
	public OnlineCommunityNormEngine getNormEngine() {
		return this.normEngine;
	}

	/**
	 * 
	 * @return
	 */
	public OnlineCommunityUserReasoner getUsersReasoner() {
		return this.usersReasoner;
	}

	/**
	 * 
	 * @return
	 */
	public PredicatesDomains getPredicatesDomains() {
		return this.predDomains;
	}
	
	/**
	 * 
	 * @return
	 */
	public DomainFunctions getDomainFunctions() {
		return this.dmFunctions;
	}

	/**
	 * Creates the predicate and their domains for the traffic scenario
	 */
	private PredicatesDomains createPredicatesDomains(
			OnlineCommunityPopulation population) {
		
		predDomains = new PredicatesDomains();

		/* Predicate "section" domain*/
		TaxonomyOfTerms secPredTaxonomy = new TaxonomyOfTerms("sec");
		secPredTaxonomy.addTerm("*");
		secPredTaxonomy.addTerm("1");
		secPredTaxonomy.addTerm("2");
		secPredTaxonomy.addTerm("3");

		secPredTaxonomy.addRelationship("1", "*");
		secPredTaxonomy.addRelationship("2", "*");
		secPredTaxonomy.addRelationship("3", "*");

		/* Predicate "content" domain*/
		TaxonomyOfTerms cntTypePredTaxonomy = new TaxonomyOfTerms("cnt");
		cntTypePredTaxonomy.addTerm("correct");
		cntTypePredTaxonomy.addTerm("spam");
		cntTypePredTaxonomy.addTerm("porn");
		cntTypePredTaxonomy.addTerm("violent");
		cntTypePredTaxonomy.addTerm("insult");

		cntTypePredTaxonomy.addRelationship("correct", "*");
		cntTypePredTaxonomy.addRelationship("spam", "*");
		cntTypePredTaxonomy.addRelationship("porn", "*");
		cntTypePredTaxonomy.addRelationship("violent", "*");
		cntTypePredTaxonomy.addRelationship("insult", "*");

		/* Predicate "user" domain*/		
		if(ocSettings.normsHaveUserId()) {
			TaxonomyOfTerms usrPredTaxonomy = new TaxonomyOfTerms("usr");
			usrPredTaxonomy.addTerm("*");

			/* Add users */
			int numUsers = population.getSize(); // TODO 
			for (int usrIdx = 0; usrIdx <= numUsers; usrIdx++){
				usrPredTaxonomy.addTerm("" + usrIdx);
				usrPredTaxonomy.addRelationship("" + usrIdx, "*");
			}
			predDomains.addPredicateDomain("usr", usrPredTaxonomy);
		}

		predDomains.addPredicateDomain("sec", secPredTaxonomy);
		predDomains.addPredicateDomain("cnt", cntTypePredTaxonomy);

		return predDomains;
	}
}
