package es.csic.iiia.normlab.examples.traffic;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.generation.cbr.CBRConflictSolvingMachine;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;

/**
 * The operators that the SIMON strategy uses to perform norm synthesis
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class TrafficNSExample5_NSOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	protected NormReasoner normReasoner;					// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	protected TrafficNSExample5_NSStrategy strategy;	// the norm synthesis strategy
	protected NormativeNetwork normativeNetwork;	// the normative network
	protected CBRConflictSolvingMachine genMachine;	// the norm generation machine
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param 	strategy the SIMON norm synthesis strategy
	 * @param 	normReasoner the norm reasoner, to reason about norm
	 * 					applicability	and compliance
	 * @param 	nsm the norm synthesis machine
	 */
	public TrafficNSExample5_NSOperators(TrafficNSExample5_NSStrategy strategy, 
			NormReasoner normReasoner, NormSynthesisMachine nsm, 
			NormSynthesisMetrics nsMetrics) {
		
		this.strategy = strategy;
		this.normReasoner = normReasoner;
		this.dmFunctions = nsm.getDomainFunctions();
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		
		this.genMachine = new CBRConflictSolvingMachine(this.normativeNetwork,
				normReasoner, strategy, nsm.getRandom(), nsMetrics);
	}

	/**
	 * Adds a norm to the normative network (if the normative network
	 * does not contain it yet) and activates it by setting its state
	 * to <tt>active</tt> in the normative network
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {
		if(!normativeNetwork.contains(norm)) {			
			normativeNetwork.add(norm);
			this.activate(norm);
			this.strategy.normCreated(norm);
		}
	}

	/**
	 * Activates a given {@code norm} in the normative network, resets
	 * its utility and adds the norm to the norm reasoner. Thus, the
	 * strategy will take the norm into account to compute norm
	 * applicability and compliance
	 * 
	 * @param norm the norm to activate
	 */	
	public void activate(Norm norm) {
		this.normativeNetwork.setState(norm, NetworkNodeState.Active);

		/* Add norm to the norm engine */
		this.normReasoner.addNorm(norm);
		this.strategy.normActivated(norm);
	}
}
