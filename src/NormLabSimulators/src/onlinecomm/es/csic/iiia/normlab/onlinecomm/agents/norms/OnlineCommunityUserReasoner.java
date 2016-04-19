package es.csic.iiia.normlab.onlinecomm.agents.norms;

import java.util.Collections;
import java.util.concurrent.Semaphore;

import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySettings;
import es.csic.iiia.normlab.onlinecomm.context.ContextData;
import es.csic.iiia.normlab.onlinecomm.nsm.agent.OnlineCommunityUserAction;
import es.csic.iiia.normlab.onlinecomm.nsm.agent.OnlineCommunityUserContext;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.reasoning.NormEngine;

/**
 * Reasoner for the agent. Contains the rule engine to do reasoning
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class OnlineCommunityUserReasoner extends NormEngine {

	//------------------------------------------------------------
	// Attributes																															
	//------------------------------------------------------------

	private OnlineCommunitySettings ocSettings;
	
	/* The state of the reasoner */
	private OnlineCommunityUserReasonerState state;

	/* Since we use an unique reasoner for all the agents, we must
	 * synchronize the access to the method decideAction() */
	private Semaphore sync;
	
	/* Norms to violate or to apply */
	private Norm normToApply;
	private Norm normToViolate;
	private ContextData contextData;
	
	//---------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------

	/**
	 * 
	 * @param dmFunctions
	 */
	public OnlineCommunityUserReasoner(PredicatesDomains predDomains,
			ContextData contextData, OnlineCommunitySettings ocSettings) {
		super(predDomains);
		
		this.state = OnlineCommunityUserReasonerState.NoNormActivated;
		this.contextData = contextData;
		this.ocSettings = ocSettings;
		
		this.sync = new Semaphore(1);
	}

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Does reasoning to activate rules in base of the facts in the knowledge base 
	 */
	public OnlineCommunityUserAction decideAction(OnlineCommunityUserContext context) {
		OnlineCommunityUserAction returnAction = OnlineCommunityUserAction.Upload;
		
		try {
	    this.sync.acquire();
    } catch (InterruptedException e) {
	    e.printStackTrace();
    }
		
		this.state = OnlineCommunityUserReasonerState.NoNormActivated;
		boolean violate;
		int violateProb;
		
		this.reset();

		// Add world facts
		SetOfPredicatesWithTerms predicates = context.getDescription();
		
		/* Add agents' context fact */
		this.addFacts(predicates);

		// Reason about applicable norms
		this.reason();

		// Remove random component of JESS
		Collections.sort(applicableNorms);

		// Obtain next supposed action to do according to the norm specification
		int numApplNorms = applicableNorms.size();
		boolean decided = false;
		int i=0;
		
		while(i<numApplNorms && !decided) {
			Norm n = applicableNorms.get(i);
			
			violate = false;
			violateProb = (int)(ocSettings.getNormInfringementRate() * 100);
			
			int num = this.contextData.nextIntRandom(100) + 1;
			violate = (num <= violateProb) ? true : false;

			// Randomly choose if applying the norm or not. Case apply the norm
			if(violate) {
				decided = true;
				this.normToViolate = n;
				state = OnlineCommunityUserReasonerState.NormWillBeViolated;
				returnAction = OnlineCommunityUserAction.Upload;
			}
			else{
				decided = true;
				this.normToApply = n;
				state = OnlineCommunityUserReasonerState.NormWillBeApplied;
				returnAction = OnlineCommunityUserAction.Nothing;
			}
		}
		
//		for(Norm n : applicableNorms) {
//			violate = false;
//			violateProb = (int)(CommunityNormSynthesisSettings.SIM_NORM_VIOLATION_RATE * 100);
//			
//			int num = this.contextData.nextIntRandom(100) + 1;
//			violate = (num <= violateProb) ? true : false;
//
//			// Randomly choose if applying the norm or not. Case apply the norm
//			if(violate) {
//				this.normToViolate = n;
//				state = CommunityAgentReasonerState.NormWillBeViolated;
//				returnAction = CommunityAgentAction.Upload;
//			}
//			else{
//				this.normToApply = n;
//				state = CommunityAgentReasonerState.NormWillBeApplied;
//				returnAction = CommunityAgentAction.Nothing;
//			}
//		}
		// Let the facts base empty and return the action chosen by the agent
		applicableNorms.clear();
    
		this.sync.release();
		
		return returnAction;
	}
	
	/**
	 * Returns true if the last applicable norm was finally applied. False else
	 * 
	 * @return
	 */
	public OnlineCommunityUserReasonerState getState() {
		return state;
	}

	/**
	 * Returns the last norm that has been applied by the car
	 * 
	 * @return
	 */
	public Norm getNormToApply() {
		return this.normToApply;
	}

	/**
	 * Returns the last norm that has been violated by the car
	 * @return
	 */
	public Norm getNormToViolate() {
		return this.normToViolate;
	}
}
