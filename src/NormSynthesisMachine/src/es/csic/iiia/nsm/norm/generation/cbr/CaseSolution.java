package es.csic.iiia.nsm.norm.generation.cbr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.sensing.View;

/**
 * A case solution is a solution aimed to solve a conflicting situation
 * described in the {@code CaseDescription} of a {@code Case}.
 * In particular, a case solution describes what actions are 
 * prohibited and obligated for some agents in certain views.
 * For instance, a very simple case solution would contain a single 
 * {@code View}, and an agent with id {@code agentId} which is prohibited
 * to perform a single action in that {@code View}
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see Case
 * @see CaseDescription
 * @see View
 */
public class CaseSolution {
	
	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private Map<View, List<Long>> blamedAgentsIds;
	private List<Long> allBlamedAgentsIds;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public CaseSolution() {
		this.blamedAgentsIds = new HashMap<View, List<Long>>();
		this.allBlamedAgentsIds = new ArrayList<Long>();
	}

	/**
	 * Adds an {@code action} that the agent with id {@code agentId} is prohibited
	 * to perform in the situation described by the {@code view}
	 * 
	 * @param 	view the view in which the agent is prohibited
	 * 					to perform the {@code action}
	 * @param 	agentId the id of the agent that is prohibited to perform
	 * 					the action in the situation described by the given {@code view}
	 * @param 	action the action that the agent is prohibited to perform
	 * 					in the given {@code view}
	 */
	public void addBlamedAgent(View view, Long agentId) {
		if(!this.blamedAgentsIds.containsKey(view)) {
			this.blamedAgentsIds.put(view, new ArrayList<Long>());
		}
		this.blamedAgentsIds.get(view).add(agentId);
		this.allBlamedAgentsIds.add(agentId);
	}
	
	/**
	 * Returns a {@code Map} that contains all the agents that have an obligated
	 * action in the given {@code view}, together with the {@code action} they
	 * are prohibited to perform
	 * 
	 * @return a {@code Map} that contains all the agents that have a prohibited
	 * 					action in the given {@code view}, together with the
	 * 					{@code action} they are prohibited to perform
	 */
	public List<Long> getBlamedAgentsIds(View view) {
		return this.blamedAgentsIds.get(view);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Long> getBlamedAgentsIds() {
		return this.allBlamedAgentsIds;
	}
	
	/**
	 * Returns the score of this case solution
	 * 
	 * @return the score of this case solution
	 */
	public float getScore() {
		return 0f;
	}
}
