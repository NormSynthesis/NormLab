/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.refinement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormUtilityFunction;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.generalisation.ConservativeNormGeneraliser;
import es.csic.iiia.nsm.norm.refinement.generalisation.GeneralisationTrees;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormIntersection;
import es.csic.iiia.nsm.norm.refinement.generalisation.NullNormGeneraliser;
import es.csic.iiia.nsm.norm.refinement.generalisation.OptimisticNormGeneraliser;
import es.csic.iiia.nsm.sensing.ViewTransition;
import es.csic.iiia.nsm.strategy.GenericOperators;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormRefiner {

	protected Random random;
	protected List<EvaluationCriteria> normEvDimensions;
	protected NormSynthesisSettings nsmSettings;

	protected NormClassifier normClassifier;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGroupNetwork normGroupNetwork;
	protected NormSynthesisMetrics nsMetrics;

	protected NormUtilityFunction utilityFunction;	
	protected GenericOperators operators;
	protected NormGeneraliser normGeneraliser;
	protected NormGeneraliser.Approach genApproach; 
	protected NormGeneralisationMode genMode; 
	protected int genStep;

	protected Map<String, NormIntersection> normIntersections;
	protected Map<NormGroupCombination, Integer> potentialComplementaryPairs;
	protected Map<Norm, List<NormAttribute>> normClassifications;


	/**
	 * 
	 * @param normativeNetwork
	 */
	public NormRefiner(List<EvaluationCriteria> normEvDimensions,
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			PredicatesDomains predDomains, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, NormReasoner normReasoner,
			NormSynthesisMetrics nsMetrics, GenericOperators operators,
			NormGeneraliser.Approach genApproach, NormGeneralisationMode genMode,
			int genStep, Random random, GeneralisationTrees genTrees) {

		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.dmFunctions = dmFunctions;
		this.predDomains = predDomains;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = normGroupNetwork;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.genApproach = genApproach;
		this.genMode = genMode;
		this.genStep = genStep;
		this.random = random;
		this.nsMetrics = nsMetrics;

		this.normClassifications = new HashMap<Norm, List<NormAttribute>>();
		
		this.potentialComplementaryPairs = 
				new HashMap<NormGroupCombination,Integer>();
		
		this.normClassifier = new NormClassifier(normEvDimensions, nsmSettings,
				normativeNetwork, normGroupNetwork, operators, nsMetrics);

		/* Create norm generaliser */
		switch(this.genApproach) {
		case Conservative: 
			this.normGeneraliser = new ConservativeNormGeneraliser(this, operators, 
					dmFunctions, predDomains, normativeNetwork, nsMetrics, genTrees);
			break;

		case Optimistic: 
			this.normGeneraliser = new OptimisticNormGeneraliser(this, operators,
					genMode, genStep, dmFunctions, normReasoner, predDomains, nsMetrics);
			break;

		default:
			this.normGeneraliser = new NullNormGeneraliser();
			break;
		}
	}

	/**
	 * 
	 * @param normApplicability
	 * @param normsActivatedDuringGeneration
	 */
	public void step(Map<ViewTransition, NormsApplicableInView> normApplicability,
			List<Norm> normsActivatedDuringGeneration) {

		List<Norm> normsToActivate = new ArrayList<Norm>();
		List<Norm> normsToDeactivate = new ArrayList<Norm>();
		List<Norm> normsToGeneralise = new ArrayList<Norm>();
		boolean isEffective, isNecessary;
		boolean isIneffective, isUnnecessary;
		boolean classifiedInEffectiveness;
		boolean isGeneralisable;

		/* Compute norms that must be revised */
		List<Norm> normsToRevise = this.checkNormsToRevise(normApplicability);

		/* Classify norms */
		this.normClassifications = this.normClassifier.classify(normsToRevise);
		Set<Norm> normsClassified  = this.normClassifications.keySet();

		/* Refine norms based on norm classifications */
		for(Norm norm : normsClassified) {
			List<NormAttribute> attributes = normClassifications.get(norm);
			isEffective = attributes.contains(NormAttribute.Effective);
			isNecessary = attributes.contains(NormAttribute.Necessary);
			isIneffective = attributes.contains(NormAttribute.Ineffective);
			isUnnecessary = attributes.contains(NormAttribute.Unnecessary);
			isGeneralisable = attributes.contains(NormAttribute.Generalisable);
			classifiedInEffectiveness = isEffective || isIneffective;

			/* If the norm is whether ineffective or unnecessary, then deactivate
			 * it (specialise it into its children) */
			if(isIneffective || isUnnecessary) {
				normsToDeactivate.add(norm);
			}

			/* Only if norm generation is not reactive to conflicts:
			 * activate norms when they are proven to be necessary */
			if((isEffective || !classifiedInEffectiveness) && isNecessary) {
				if(!this.normativeNetwork.isRepresented(norm)) {				
					normsToActivate.add(norm);
				}
			}

			/* Check if the norm can be generalised */
			if(isGeneralisable) {
				normsToGeneralise.add(norm);
			}
		}

		/* Activate, deactivate and generalise norms */
		this.activateUp(normsToActivate);
		this.deactivateUp(normsToDeactivate);
		this.generaliseUp(normsToGeneralise);
	}

	/**
	 * 
	 * @param norms
	 */
	private void activateUp(List<Norm> norms) {
		for(Norm norm : norms) {
			this.activateUp(norm);
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	private void deactivateUp(List<Norm> norms) {
		List<Norm> visited = new ArrayList<Norm>();
		for(Norm norm : norms) {
			this.deactivateUp(norm, visited);
		}
	}

	/**
	 * 
	 * @param norms
	 * @param genMode
	 * @param genStep
	 */
	private void generaliseUp(List<Norm> norms) {
		for(Norm norm : norms) {
			this.generaliseUp(norm);
		}
	}

	/**
	 * Reactivates a norm that was previously created, activated and deactivated.
	 * This reactivation is performed recursively, trying to re-generalise the norm
	 * again, activating those parents that can be reactivated  
	 * 
	 * @param norm the norm to reactivate
	 */	
	public void activateUp(Norm norm) {
		if(!normativeNetwork.isRepresented(norm)) {

			/* Clear control lists if necessary */
			if(this.genApproach == NormGeneraliser.Approach.Optimistic) {
				((OptimisticNormGeneraliser)normGeneraliser).clearNotFeasibleGeneralisations();
			}

			/* Activate the norm */
			this.operators.activate(norm);

			List<Norm> children = this.normativeNetwork.getChildren(norm);
			List<Norm> parents = this.normativeNetwork.getParents(norm);
			List<Norm> notRepresented = this.normativeNetwork.getNotRepresentedNorms();

			/* Set its children as represented */
			for(Norm child : children) {
				if(this.normativeNetwork.isRepresented(child)) {
					this.operators.represent(child); 
				}
			}

			boolean isGeneralisable = 
					normativeNetwork.getAttributes(norm).contains(NormAttribute.Generalisable);

			/* If the norm has no parents, then it will remain active,
			 * representing its children. Then, we try to generalise it up */
			if(parents.isEmpty() && isGeneralisable) {
				this.generaliseUp(norm);
			}

			/* Otherwise, if the norm has parents, check for each parent if it 
			 * can be reactivated. A parent can be reactivated if it does
			 * not represent any inactive norm */
			else {

				/* Check if any parent can be reactivated */
				for(Norm parent : parents) {
					boolean activateParent = true;

					/* Check that the parent norm does not represent any inactive norm */
					for(Norm notRepr: notRepresented){
						if(this.normReasoner.satisfies(notRepr, parent) && 
								!notRepr.equals(parent))	{
							activateParent = false;
							break;
						}
					}
					if(activateParent) {
						this.activateUp(parent);
					}
				}
			}
		}
	}

	/**
	 * Recursively specialises a norm into its children, its children
	 * into their children, and so on
	 * 
	 * @param norm the norm to specialise
	 */
	public void deactivateUp(Norm norm, List<Norm> visited) {

		/* Clear control lists if necessary */
		if(this.genApproach == NormGeneraliser.Approach.Optimistic) {
			((OptimisticNormGeneraliser)normGeneraliser).clearFeasibleGeneralisations();
		}

		if(visited.contains(norm)) {
			return;
		}
		visited.add(norm);

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();

		/* Specialise down all parent norms */
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		for(Norm parent : parents) {
			this.deactivateUp(parent, visited);
		}

		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {
			List<Norm> childrenToActivate = new ArrayList<Norm>(children);
			operators.specialise(norm, childrenToActivate);
		}
	}

	/**
	 * 
	 * @param norm
	 */
	public void generaliseUp(Norm norm) {
		this.normGeneraliser.generaliseUp(norm, normativeNetwork);
	}

	/**
	 * 
	 */
	private List<Norm> checkNormsToRevise(
			Map<ViewTransition, NormsApplicableInView> normApplicability) {

		List<Norm> normsToRevise = new ArrayList<Norm>();

		for(NormsApplicableInView vna : normApplicability.values()) {
			for(long id : vna.getAgentIds()) {
				for(Norm norm : vna.get(id).getApplicableNorms()) {
					if(!normsToRevise.contains(norm)) {
						normsToRevise.add(norm);
					}
				}
			}
		}
		return normsToRevise;
	}


	///* 1. First, deactivate all those norms that have a substitutability 
	// * relationship with those norms that have been activated during 
	// * the norm generation phase */
	//for(Norm norm : normsActivatedDuringGeneration) {
	//	List<Norm> substitutable = normativeNetwork.getSubstitutableNorms(norm);
	//	if(!substitutable.isEmpty()) {
	//		this.normativeNetwork.addAttribute(norm, NormAttribute.Substituter);
	//		System.out.println("Reactivate substitutable (maybe it's complementary... " + norm);
	//	}
	//
	//	for(Norm toSubstitute : substitutable) {
	//		System.out.println("Deactivates substitutable " + toSubstitute);
	//		System.out.println("-----------------------------------------------------------------");
	//
	//		this.normativeNetwork.removeAttribute(toSubstitute, NormAttribute.Substituter);
	//		this.deactivateUp(toSubstitute, NetworkNodeState.Substituted, visited);
	//
	//		NormGroupCombination nGrComb = 
	//				this.normGroupNetwork.getNormGroupCombination(norm, toSubstitute);
	//
	//		if(!this.potentialComplementaryPairs.containsKey(nGrComb)) {
	//			this.potentialComplementaryPairs.put(nGrComb, 0);
	//		}
	//		int numReactivations = this.potentialComplementaryPairs.get(nGrComb);
	//		this.potentialComplementaryPairs.put(nGrComb, numReactivations+1);
	//
	//		/* Cycle detection. If the pair of norms have substituted one another
	//		 * 5 times or more, then remove the substitutability relationship
	//		 * (due to a possible false positive) */
	//		if(numReactivations > 5) {
	//			this.normativeNetwork.removeSubstitutability(norm, toSubstitute);
	//		}
	//
	//		/* Update complexities metrics */
	//		this.nsMetrics.incNumNodesVisited();
	//	}
	//}
	//visited.clear();
}
