package es.csic.iiia.normlab.onlinecomm.agents.norms;

import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySettings;
import es.csic.iiia.normlab.onlinecomm.nsm.agent.OnlineCommunityUserContext;
import es.csic.iiia.nsm.agent.EnvironmentAgentContext;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.reasoning.JessFactsGenerator;

/**
 * Facts generator tool. Generates facts for the car reasoner and to build the
 * condition (left part) of a norm. It adapts the facts to the format of the car
 * reasoner or the norm condition, in base of the FactType passed by parameter  
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class OnlineCommunityFactFactory extends JessFactsGenerator {

	
	//-------------------------------------------------------------------------	
	// Attributes 
	//-------------------------------------------------------------------------
	
	private OnlineCommunitySettings ocSettings;
	
	//-------------------------------------------------------------------------	
	// Methods 
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param predicatesDomains
	 */
	public OnlineCommunityFactFactory(PredicatesDomains predicatesDomains, 
			OnlineCommunitySettings ocSettings) {
		
		super(predicatesDomains);
		this.ocSettings = ocSettings;
	}

	
	/**
	 * Defines the type of a fact. A fact for a Reasoner is used to know what is the current
	 * state of the world. A fact for a norm is used to define a situation that fires a norm
	 *   
	 * @author Javier Morales (jmorales@iiia.csic.es)
	 *
	 */
	public enum FactType {
		Reasoner, Norm
	}

	/**
	 * Generates a string containing a set of facts
	 * 
	 * @param factType
	 * @param scope
	 * @return
	 */
	public SetOfPredicatesWithTerms generatePredicates(EnvironmentAgentContext aContext) {
		SetOfPredicatesWithTerms predicatesWithTerms = new SetOfPredicatesWithTerms();
		OnlineCommunityUserContext context = (OnlineCommunityUserContext) aContext;

		if(this.ocSettings.normsHaveUserId()) {
			if(context.getId() != -1) {
				predicatesWithTerms.add("usr", String.valueOf(context.getId()));
			}
		}
		if(context.getSection() != -1) {
			predicatesWithTerms.add("sec", String.valueOf(context.getSection()));
		}
		if(context.getContentType() != -1) {
			predicatesWithTerms.add("cnt", context.getContentTypeDesc());
		}
		return predicatesWithTerms;
	}	
}
