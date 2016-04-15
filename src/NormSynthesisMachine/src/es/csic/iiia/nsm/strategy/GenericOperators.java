package es.csic.iiia.nsm.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.generalisation.GeneralisationTrees;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;

/**
 * A set of generic normative network operators employed by a generic 
 * norm synthesis strategy to perform norm synthesis 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class GenericOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormReasoner normReasoner;					// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	protected GenericSynthesisStrategy strategy;	// the norm synthesis strategy
	protected NormativeNetwork normativeNetwork;	// the normative network
	protected NormGroupNetwork normGroupNetwork;	// the norm groups network
	protected NormGenerationMachine genMachine;		// the norm generation machine
	protected NormSynthesisSettings nsmSettings;	// norm synthesis settings
	protected NormSynthesisMetrics nsMetrics;			// norm synthesis metrics
	protected boolean pursueCompactness;					// perform norm generalisations?
	protected boolean pursueLiberality;						// exploit norm synergies?

	/* Type of norm generalisations to employ (either conservative or optimistic) */ 
	protected NormGeneraliser.Approach nGenApproach;

	/* Data structure to keep track of potential generalisations 
	 * (only used for conservative norm generalisation) */
	protected GeneralisationTrees genTrees;

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
	public GenericOperators(GenericSynthesisStrategy strategy, 
			NormReasoner normReasoner, NormSynthesisMachine nsm,
			GeneralisationTrees genTrees) {

		this.strategy = strategy;
		this.normReasoner = normReasoner;
		this.genTrees = genTrees;

		this.dmFunctions = nsm.getDomainFunctions();
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.normGroupNetwork = nsm.getNormGroupNetwork();
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.nsMetrics = nsm.getNormSynthesisMetrics();

		this.nGenApproach = nsmSettings.getNormGeneralisationApproach();
		this.pursueCompactness = nsmSettings.pursueCompactness();
		this.pursueLiberality = nsmSettings.pursueLiberality();
	}

	/**
	 * Adds a norm to the normative network if the normative network
	 * does not contain it yet. Also, if the NSM is performing
	 * conservative norm generalisations, it generates and keeps
	 * track of the potential generalisations of the norm 
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {
		if(!normativeNetwork.contains(norm)) {

			/* Add the norm to the network in case it does not exist on it */
			this.normativeNetwork.add(norm);
			this.normativeNetwork.setState(norm, NetworkNodeState.Created);

			/* Find potential generalisation relationships of the norm with
			 * other norms in the normative network */
			this.link(norm);

			/* If the strategy performs conservative norm generalisations,
			 * then create potential generalisations for the added norm */
			if(pursueCompactness) {
				if(nGenApproach == NormGeneraliser.Approach.Conservative) {
					this.genTrees.add(norm);
				}	
			}

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesSynthesised();
			this.nsMetrics.incNumNodesInMemory();
		}
	}

	/**
	 * Hibernates a given {@code norm} in the normative network
	 * by setting its state to "hibernated"  
	 * 
	 * @param norm the norm to hibernate
	 */
	public void hibernate(Norm norm) {
		if(this.normativeNetwork.getState(norm) == NetworkNodeState.Created) {
			this.normativeNetwork.setState(norm, NetworkNodeState.Hibernated);
		}
	}

	/**
	 * Activates a given {@code norm} in the normative network by setting 
	 * its state to "active". Also, if the NSM is exploiting norm synergies
	 * to pursue liberality, it activates the norm groups containing the norm
	 * 
	 * @param norm the norm to activate
	 * @see {@code NormGroup}
	 */	
	public void activate(Norm norm) {
		if(!normativeNetwork.isRepresented(norm)) {
			normativeNetwork.setState(norm, NetworkNodeState.Active);

			this.activateNormGroups(norm);
		}
	}

	/**
	 * Sets the state of a norm to "represented" in the normative network.
	 * This operation is performed when the norm is generalised (represented) 
	 * by a parent norm in the normative network  
	 * 
	 * @param norm the norm to represent
	 */
	public void represent(Norm norm) {
		this.normativeNetwork.setState(norm, NetworkNodeState.Represented);
	}

	/**
	 * Deactivates a given {@code norm} in the normative network by setting 
	 * its state to "inactive". Also, if the NSM is exploiting norm synergies
	 * to pursue liberality, it deactivates the norm groups containing the norm
	 * 
	 * @param norm the norm to deactivate
	 */
	public void deactivate(Norm norm) {
		this.normativeNetwork.setState(norm, NetworkNodeState.Inactive);

		/* Deactivate norm groups containing the norm */
		if(this.pursueLiberality) {
			if(this.normGroupNetwork.hasNormGroupCombinations(norm)) {
				Map<Norm,NormGroupCombination> nGrCombs = 
						this.normGroupNetwork.getNormGroupCombinations(norm);

				for(Norm n : nGrCombs.keySet()) {
					this.normGroupNetwork.setState(nGrCombs.get(n), 
							NetworkNodeState.Inactive);	

					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
				}	
			}
		}
	}

	/**
	 * Generalises a child norm into a parent norm by establishing
	 * a generalisation relationship from the child to the parent 
	 * in the normative network
	 * 
	 * @param child the child norm
	 * @param parent the parent norm
	 */
	public void generalise(Norm child, Norm parent) {		
		this.normativeNetwork.addGeneralisation(child, parent);

		/* Deactivate the child norm if it is represented by
		 * an ancestor (the parent norm, likely) */
		for(Norm p : this.normativeNetwork.getParents(child)) {
			if(this.normativeNetwork.isRepresented(p)) {
				this.represent(child); 
				break;
			}

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}

	/**
	 * Specialises a norm in the normative network by deactivating 
	 * the norm and activating its children. 
	 * 
	 * @param norm the norm to specialise
	 * @param children the children which to specialise the norm into
	 */
	public void specialise(Norm norm, List<Norm> children) {
		this.deactivate(norm);	

		/* Activate child norms that are not represented by an ancestor */
		for(Norm child : children) {
			if(!normativeNetwork.isRepresented(child)) {
				this.activate(child);
			}

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}

	/**
	 * @param norm
	 */
	public void reset(Norm norm) {
		norm = this.normativeNetwork.retrieveNorm(norm);
		this.normativeNetwork.getUtility(norm).reset();
		this.normativeNetwork.resetAttributes(norm);
	}

	/**
	 * Searches for potential generalisation relationships that a norm
	 * may have with other norms in the normative network
	 * 
	 * @param norm the norm to search relationships for
	 */
	public void link(Norm norm) {
		List<Norm> topBoundary =
				(List<Norm>)this.normativeNetwork.getTopBoundary();
		List<Norm> visitedNorms = new ArrayList<Norm>();

		for(Norm normB : topBoundary) {
			if(!norm.equals(normB)) {
				this.searchRelationships(norm, normB, visitedNorms);
			}
		}
	}

	/**
	 * Searches for potential generalisation relationships that norm
	 * {@code normA} may have with norm {@normB} in the normative network
	 * 
	 * @param normA a norm
	 * @param normB another norm
	 * @param visitedNorms a list of visited norms (for recursiveness)
	 */
	private void searchRelationships(Norm normA, Norm normB, 
			List<Norm> visitedNorms) {

		List<Norm> normAChildren = this.normativeNetwork.getChildren(normA);
		List<Norm> normBChildren = this.normativeNetwork.getChildren(normB);
		boolean linked = false;

		List<Norm> normBSatisfiedChildren = 
				this.normReasoner.getSatisfiedNorms(normA, normBChildren);
		List<Norm> normBChildrenSatisfyingA = 
				this.normReasoner.getNormsSatisfying(normBChildren, normA);
		List<Norm> normBChildrenNotSatisfyingA = 
				this.normReasoner.getNormsNotSatisfying(normBChildren, normA);

		/* To avoid that a norm can generalise itself */
		if(normA.equals(normB)) {
			return;
		}

		/* Check if normA can be a parent of normB. To this aim, we check
		 * is normB satisfies normA. In that case, generalise from normB
		 * to normA, only iff normA is not already an ancestor of normB */
		if(normReasoner.satisfies(normB, normA))	{
			if(!this.normativeNetwork.isAncestor(normA, normB)) {
				this.generalise(normB, normA);
				linked = true;
			}

			/* Check that normB has no child norm that is already a child of normA */
			for(Norm normBChild : normBChildren) {
				if(normAChildren.contains(normBChild)) {
					this.normativeNetwork.removeGeneralisation(normBChild, normA);
				}

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
			}
		} 

		/* Check if normA can be a child of normB. With this aim, first check 
		 * if normA satisfies normB */
		else if(normReasoner.satisfies(normA, normB)) {

			/* If normA satisfies normB, and does not satisfy neither of its children,
			 * then normB directly generalises normA */
			if(normBSatisfiedChildren.isEmpty()) {

				/* Norm normA is a child of normB. Generalise from normA to normB
				 * only iff normB is not already an ancestor of normA */ 
				if(!this.normativeNetwork.isAncestor(normB, normA)) {
					this.generalise(normA, normB);
					linked = true;
				}

				/* Check that normA is not between normB and any of its children. 
				 * With this aim, check if there is any child of normB that satisfies
				 * normA. For each child that satisfies normA, remove the generalisation
				 * relationship between normB and its children, and establish new 
				 * generalisation relationships from the child to normA, and from normA
				 * to normB */
				if(this.normativeNetwork.isAncestor(normB, normA)) {
					for(Norm normBChild : normBChildrenSatisfyingA) {
						this.normativeNetwork.removeGeneralisation(normBChild, normB);
						this.generalise(normBChild, normA);
					}
				}
			}
		}

		/* If the norm was not linked to any norm, keep searching */
		if(!linked) {
			for(Norm normBChild : normBChildren) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		} 

		/* If the norm has been linked, we keep on searching generalisation
		 * relationships only with those children of normB that do not
		 * satisfy normA. This is to check that, in case normA is now a parent
		 * of normB, it is not also a parent of one of its children */
		else if(!normBChildrenNotSatisfyingA.isEmpty()) {		
			for(Norm normBChild : normBChildrenNotSatisfyingA) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		}

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();
	}

	/**
	 * Activate norm groups that contain a given norm
	 * 
	 * @param norm the norm 
	 */
	private void activateNormGroups(Norm norm) {
		if(this.normGroupNetwork.hasNormGroupCombinations(norm)) {
			Map<Norm,NormGroupCombination> nGrCombs = 
					this.normGroupNetwork.getNormGroupCombinations(norm);

			for(Norm n : nGrCombs.keySet()) {

				/* Activate all norm groups in which the norm is */
				this.normGroupNetwork.setState(nGrCombs.get(n), NetworkNodeState.Active);

				/* Reset all norm groups in which the norm is */
				for(NormGroup nGroup : nGrCombs.get(n).getAllNormGroups()) {
					Utility u = this.normGroupNetwork.getUtility(nGroup);
					u.reset();

					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
				}

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
			}	
		}
	}
}
