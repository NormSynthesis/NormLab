package es.csic.iiia.nsm.norm.evaluation.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.agent.EnvironmentAgentContext;

/**
 * A joint context is the combination of local contexts of several agents that
 * perceive each other
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class JointContext {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private List<Long> agentIds;
	private Map<Long, EnvironmentAgentContext> agentContexts;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public JointContext() {
		this.agentIds = new ArrayList<Long>();
		this.agentContexts = new HashMap<Long, EnvironmentAgentContext>();
	}
	
	/**
	 * Adds the context {@code agentContext} of an agent with id {@code agentId}
	 * 
	 * @param agentId the id of the agent
	 * @param agentContext the context of the agent with id {@code agentId}
	 */
	public void addAgentContext(long agentId, EnvironmentAgentContext agentContext) {
		if(!this.agentIds.contains(agentId)) {
			this.agentIds.add(agentId);
		}
		this.agentContexts.put(agentId, agentContext);
	}
	
	/**
	 * Returns the identifiers of all the agents in the joint context
	 * 
	 * @return the identifiers of all the agents in the joint context
	 */
	public List<Long> getAgentIds() {
		return this.agentIds;
	}
	
	/**
	 * Returns the context of an agent with id {@code agentId}
	 * 
	 * @return the context of an agent with id {@code agentId}
	 */
	public EnvironmentAgentContext getContext(long agentId) {
		return this.agentContexts.get(agentId);
	}
	
	/**
	 * Returns all the agent contexts in the joint context
	 * 
	 * @return all the agent contexts in the joint context
	 */
	public Collection<EnvironmentAgentContext> getAllContexts() {
		return this.agentContexts.values();
	}
}
