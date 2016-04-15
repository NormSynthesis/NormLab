/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.z_deprecated.synthesis.strategy.lion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.NormAttribute;
import es.csic.iiia.nsm.norm.refinement.generalisation.GeneralisableNorms;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormIntersection;
import es.csic.iiia.nsm.sensing.ViewTransition;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class LIONNormRefiner {

	protected Random random;
	protected List<EvaluationCriteria> normEvDimensions;
	protected NormSynthesisSettings nsmSettings;

	protected LIONNormClassifier normClassifier;
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGroupNetwork normGroupNetwork;
	protected NormSynthesisMetrics nsMetrics;
	
	protected Map<Norm, List<NormAttribute>> normClassifications;

	protected LIONUtilityFunction utilityFunction;	
	protected LIONOperators operators;
	protected NormGeneralisationMode genMode; 
	protected int genStep;

	protected Map<String, NormIntersection> normIntersections;
	protected Map<NormGroupCombination, Integer> potentialComplementaryPairs;
	protected boolean isNormGenReactiveToConflicts;

	/**
	 * 
	 * @param normativeNetwork
	 */
	public LIONNormRefiner(List<EvaluationCriteria> normEvDimensions,
			NormSynthesisSettings nsmSettings, DomainFunctions dmFunctions,
			PredicatesDomains predDomains, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, NormReasoner normReasoner,
			NormSynthesisMetrics nsMetrics, LIONOperators operators,
			NormGeneralisationMode genMode, int genStep, Random random) {

		this.normEvDimensions = normEvDimensions;
		this.nsmSettings = nsmSettings;
		this.dmFunctions = dmFunctions;
		this.predDomains = predDomains;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = normGroupNetwork;
		this.normReasoner = normReasoner;
		this.operators = operators;
		this.genMode = genMode;
		this.genStep = genStep;
		this.random = random;
		this.nsMetrics = nsMetrics;

		this.potentialComplementaryPairs = new HashMap<NormGroupCombination,Integer>();
		this.normIntersections = new HashMap<String, NormIntersection>();
		this.normClassifications = new HashMap<Norm, List<NormAttribute>>();
		this.normClassifier = new LIONNormClassifier(normEvDimensions,
				nsmSettings,normativeNetwork, normGroupNetwork, operators, nsMetrics);

		this.isNormGenReactiveToConflicts = this.nsmSettings.
				isNormGenerationReactiveToConflicts();
	}

	/**
	 * 
	 * @param normClassifications
	 */
	public void step(Map<ViewTransition, NormsApplicableInView> normApplicability,
			List<Norm> normsActivatedDuringGeneration) {

		List<Norm> processed = new ArrayList<Norm>();
		List<Norm> visited = new ArrayList<Norm>();

		/* 1. First, deactivate all those norms that have a substitutability 
		 * relationship with those norms that have been activated during 
		 * the norm generation phase */
		for(Norm norm : normsActivatedDuringGeneration) {
			List<Norm> substitutable = normativeNetwork.getSubstitutableNorms(norm);
			if(!substitutable.isEmpty()) {
				this.normativeNetwork.addAttribute(norm, NormAttribute.Substituter);
				System.out.println("Reactivate substitutable (maybe it's complementary... " + norm);
			}

			for(Norm toSubstitute : substitutable) {
				System.out.println("Deactivates substitutable " + toSubstitute);
				System.out.println("-----------------------------------------------------------------");

				this.normativeNetwork.removeAttribute(toSubstitute, NormAttribute.Substituter);
				this.deactivateUp(toSubstitute, NetworkNodeState.Substituted, visited);

				NormGroupCombination nGrComb = 
						this.normGroupNetwork.getNormGroupCombination(norm, toSubstitute);

				if(!this.potentialComplementaryPairs.containsKey(nGrComb)) {
					this.potentialComplementaryPairs.put(nGrComb, 0);
				}
				int numReactivations = this.potentialComplementaryPairs.get(nGrComb);
				this.potentialComplementaryPairs.put(nGrComb, numReactivations+1);

				/* Cycle detection. If the pair of norms have substituted one another
				 * 5 times or more, then remove the substitutability relationship
				 * (due to a possible false positive) */
				if(numReactivations > 5) {
					this.normativeNetwork.removeSubstitutability(norm, toSubstitute);
				}

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
			}
		}
		visited.clear();

		/* Compute norms that must be revised */
		List<Norm> normsToRevise = this.checkNormsToRevise(normApplicability);

		/* Classify norms */
		this.normClassifications = this.normClassifier.step(normsToRevise);

		/* Refine norms based on norm classifications */
		for(Norm norm : normClassifications.keySet()) {

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();

			if(processed.contains(norm)) {
				continue;
			}
			List<NormAttribute> attributes = normClassifications.get(norm);

			boolean isEffective = attributes.contains(NormAttribute.Effective);
			boolean isNecessary = attributes.contains(NormAttribute.Necessary);
			boolean isIneffective = attributes.contains(NormAttribute.Ineffective);
			boolean isUnnecessary = attributes.contains(NormAttribute.Unnecessary);
			boolean classifiedInEffectiveness = isEffective || isIneffective;

			/* If the norm is whether ineffective or unnecessary, then deactivate
			 * it (specialise it into its children) */
			if(isIneffective || isUnnecessary) {
				visited.clear();
				deactivateUp(norm, NetworkNodeState.Discarded, visited);
			}

			/* ONLY IF norm generation is not reactive to conflicts:
			 * activate norms when they are proven to be necessary */
			else if(!isNormGenReactiveToConflicts) 
			{
				if((isEffective || !classifiedInEffectiveness) && isNecessary) {
					if(	this.normativeNetwork.isLeaf(norm) &&
							!this.normativeNetwork.isRepresented(norm)) 
					{
						this.activateUp(norm);
					}
				}
			}

			/* If the norm has enough utility to be generalised, 
			 * then try to generalise it */
			boolean isGeneralisable = attributes.contains(NormAttribute.Generalisable);
			if(isGeneralisable) {
				generaliseUp(norm, genMode, genStep);
			}

			/* If the norm is substitutable, retrieve the norm it is 
			 * substitutable with and choose one of them to be specialised */
			boolean isSubstitutable = attributes.contains(NormAttribute.Substitutable);
			if(isSubstitutable) {
				Norm norm2 = this.normClassifier.getSubstitutableNorm(norm);
				Norm normToSubstitute = this.chooseNormToSubstitute(norm, norm2);
				Norm substituter;
				if(norm.equals(normToSubstitute)) {
					substituter = norm2;
				}
				else {
					substituter = norm;
				}

				/* Specialise norm to substitute */
				visited.clear();
				deactivateUp(normToSubstitute, NetworkNodeState.Substituted, visited);

				processed.add(norm);
				processed.add(norm2);

				/* Mark substituter */
				this.normativeNetwork.addAttribute(substituter, NormAttribute.Substituter);
				
				System.out.println("Substituting norm " + normToSubstitute);
				System.out.println("Substituter " + substituter);
				System.out.println("-----------------------------------------------------------");
			}
		}
	}

	/**
	 * Reactivates a norm that was previously created, activated and deactivated.
	 * This reactivation is performed recursively, trying to re-generalise the norm
	 * again, activating those parents that can be reactivated  
	 * 
	 * @param norm the norm to reactivate
	 */	
	private void activateUp(Norm norm) {
		if(!normativeNetwork.isRepresented(norm)) {

			/* Activate the norm */
			this.operators.activate(norm);

			List<Norm> children = this.normativeNetwork.getChildren(norm);
			List<Norm> parents = this.normativeNetwork.getParents(norm);
			List<Norm> notRepresented = this.normativeNetwork.getNotRepresentedNorms();

			/* Deactivate all its child and set them as "represented" */
			for(Norm child : children) {
				if(this.normativeNetwork.isRepresented(child)) {
					this.operators.deactivate(child, NetworkNodeState.Represented); 
				}
			}

			/* If the norm has no parents, then it will remain active,
			 * representing its children. Then, we try to generalise it up */
			if(parents.isEmpty()) {
				this.generaliseUp(norm, genMode, genStep);
			}

			/* Otherwise, if the norm has parents, check for each parent
			 * if it can be reactivated. A parent can be reactivated if it does
			 * not include any norm that is not represented, namely if it does
			 * not represent any discarded norms */
			else {

				/* Check if any parent can be reactivated */
				for(Norm parent : parents) {
					boolean activateParent = true;

					/* Check that the parent norm does not contain an inactive norm */
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
	public void deactivateUp(Norm norm, NetworkNodeState specState, List<Norm> visited) {
		if(visited.contains(norm)) {
			return;
		}
		visited.add(norm);

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();

		/* Specialise down all parent norms */
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		for(Norm parent : parents) {
			this.deactivateUp(parent, specState, visited);
		}

		List<Norm> children = this.normativeNetwork.getChildren(norm);

		/* Only specialise norms that are represented by
		 * an active norm, whether itself or a parent norm */
		if(this.normativeNetwork.isRepresented(norm)) {
			List<Norm> childrenToActivate = new ArrayList<Norm>(children);
			operators.specialise(norm, specState, childrenToActivate);
		}
	}

	/**
	 * Tries to generalise up a norm {@code normA} together with other norms in
	 * the normative system. Notice that, unlike in the case of IRON, SIMON does
	 * not compare a norm with all the other norms in the normative network, but
	 * only to those of the normative system. The SIMON norm generalisation has
	 * two working modes:
	 * <ol>
	 * <li> <b>shallow</b> generalisation, which performs norm generalisations
	 * 			by comparing the preconditions of norms in a similar way to that of
	 * 			IRON; and 
	 * <li> <b>deep</b> generalisation, which performs a deeper search by 
	 * 			computing the <b>intersection</b> of terms in the predicates of
	 * 			norms' preconditions.
	 * </ol>
	 * 
	 * Furthermore, the SIMON generalisation requires a generalisation step
	 * {@code genStep}, which describes how many predicates/terms can be
	 * generalised at the same time. As an example, consider two norms with 3
	 * predicates each one of them, and each predicate with 1 term. Consider now
	 * that 2 of the 3 terms in both norms intersect, but the third predicate in
	 * both norms are different and have a common subsumer term (a potential
	 * generalisation). If the generalisation step is genStep=1, then we may
	 * generalise these two norms, since it allows to generalise 1 predicate/term
	 * at the same time. However, if 1 of the 3 terms intersect, and hence 2
	 * terms are different, then we cannot generalise them, since the
	 * generalisation step {@code genStep} does not allow to generalise two
	 * predicates/terms at the same time.
	 * 
	 * @param normA the norm to generalise
	 * @param genMode the SIMON generalisation mode (Shallow/Deep)
	 * @param genStep the generalisation step
	 */
	public void generaliseUp(Norm normA,
			NormGeneralisationMode genMode, int genStep) {


		/* Get all the active norms (the normative system) */
		NormativeSystem NS = (NormativeSystem)
				normativeNetwork.getNormativeSystem().clone();

		/* Compute matches with each norm */
		for(Norm normB : NS) {
			boolean generalise = true;
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();

			/* Never generalise the norm with itself */
			if(normA.equals(normB)) {
				continue;
			}

			/* 1. Get generalisable norms */
			GeneralisableNorms genNorms = this.
					getGeneralisableNorms(normA, normB, genStep);

			/* If there is no possible generalisation, do nothing */
			if(genNorms == null) {
				continue;
			}

			/* 2. If there is a possible generalisation, then get generalisable
			 * norms and the parent norm */
			Norm n1 = genNorms.getNormA();
			Norm n2 = genNorms.getNormB();
			Norm parent = genNorms.getParent();

			if(n1 == null || n2 == null || parent == null) {
				continue;
			}

			/* 3. Check that the parent norm does not contain a discarded norm */
			List<Norm> notRepresented = this.normativeNetwork.getNotRepresentedNorms();
			
			for(Norm norm: notRepresented) {
				NetworkNodeState nState = this.normativeNetwork.getState(norm);
				if(nState == NetworkNodeState.Hibernated) {
					continue;
				}
				if(this.normReasoner.satisfies(norm, parent) && 
						!norm.equals(parent))	{
					generalise = false;
					break;
				}
			}

			/* 4. Do not generalise if any of the generalisable norms or the parent
			 * norm are not consistent with the given domain they have been 
			 * generated for */
			for(Norm norm : genNorms.getAllNorms()) {
				if(!this.dmFunctions.isConsistent(norm.getPrecondition())) {
					generalise = false;
					break;
				}
			}

			/* Do not generalise either if the parent norm is already created
			 * and active (that is, the same parent norm has been created in a 
			 * previous generalisation with other two norms) */
			if(this.normativeNetwork.contains(parent) && 
					//					(this.normativeNetwork.getState(parent) == NetworkNodeState.ACTIVE ||
					//					this.normativeNetwork.getState(parent) == NetworkNodeState.REPRESENTED)) { 
					this.normativeNetwork.isRepresented(parent)) {

				// TODO: Cambiar logica con estados active / represented
				generalise = false;
			}

			/* 5. Perform the norm generalisation if all the previous tests
			 * have been passed */
			if(generalise) {
				List<Norm> allNorms = new ArrayList<Norm>();
				allNorms.add(n1);
				allNorms.add(n2);
				allNorms.add(parent);

				for(Norm norm : allNorms) {

					/* If the normative network contains the norm, retrieve it
					 * and save it in the provisional norms list */
					if(this.normativeNetwork.contains(norm)) {
						int idx = allNorms.indexOf(norm);
						norm = this.normativeNetwork.getNorm(norm.getPrecondition(),
								norm.getModality(), norm.getAction());
						allNorms.set(idx, norm);
					}

					/* Else, add the norm to the normative network */
					else {
						this.operators.add(norm);
					}
				}

				n1 = allNorms.get(0);
				n2 = allNorms.get(1);
				parent = allNorms.get(2);

				/* Activate parent norm */
				if(!this.normativeNetwork.isRepresented(parent)) {
					//				if(this.normativeNetwork.getState(parent) != NetworkNodeState.ACTIVE &&
					//						this.normativeNetwork.getState(parent) != NetworkNodeState.REPRESENTED) {

					this.operators.activate(parent);
					this.normativeNetwork.getUtility(parent).reset();
				}

				/* Perform the norm generalisation */
				if(!this.normativeNetwork.isAncestor(parent, n1)) {
					this.operators.generalise(n1, parent);	
				}
				if(!this.normativeNetwork.isAncestor(parent, n2)) {
					this.operators.generalise(n2, parent); 
				}

				/* Deactivate children */
				for(Norm child : this.normativeNetwork.getChildren(parent)) {
					this.operators.deactivate(child, NetworkNodeState.Represented); // TODO: Cambiado
					
					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
				}
			}
		}
	}


	/**
	 * Given two norms Returns the generalisable norms {@code normA} and
	 * {@code normB}, it performs the following operations:
	 * <ol>
	 * <li> it computes the intersection between norms A and B;
	 * <li> if the predicates of both norms are equal but K={@code genStep}
	 * 			predicates, then it computes their generalisable norms. Otherwise,
	 * 			it return a <tt>null</tt> {@code GeneralisableNorms}
	 * </ol> 
	 * 
	 * @param normA the first norm 
	 * @param normB the second norm
	 * @param genStep the generalisation step
	 * @return the generalisable norms A and B, and their potential parent
	 */
	protected GeneralisableNorms getGeneralisableNorms(
			Norm normA, Norm normB, int genStep) {

		NormModality normModality = normA.getModality();
		NormModality otherNormModality = normB.getModality();
		EnvironmentAgentAction action = normA.getAction();
		EnvironmentAgentAction otherAction = normB.getAction();
		GeneralisableNorms genNorms = null;
		NormIntersection intersection;

		/* Check that post-conditions are the same*/
		if(normModality != otherNormModality || action != otherAction) {
			return genNorms;
		}

		/* Get the intersection between both norm preconditions */
		String desc = NormIntersection.getDescription(normA, normB);

		if(!this.normIntersections.containsKey(desc)) {
			intersection = new NormIntersection(normA, normB, 
					this.predDomains, genMode);
			this.normIntersections.put(desc, intersection);
		}
		intersection = this.normIntersections.get(desc);

		/* If both norms have all their predicates in common but K predicates,
		 * then generalise both norms are generalisable */
		if(intersection.getDifferenceCardinality() > 0 && 
				intersection.getDifferenceCardinality() <= genStep) {

			genNorms = new GeneralisableNorms(intersection, genStep);
		}		
		return genNorms;
	}

	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	protected Norm chooseNormToSubstitute(Norm nA, Norm nB) {

		/* 1. Preserve the norm that is complementary with a third norm */
		boolean nAIsComplementary = this.normativeNetwork.isComplementary(nA);
		boolean nBIsComplementary = this.normativeNetwork.isComplementary(nB);

		if(nAIsComplementary && !nBIsComplementary) {
			return nB;
		}
		if(nBIsComplementary &&!nAIsComplementary) {
			return nA;
		}

		/* 2. Preserve the norm that is substituting a third norm */
		if(this.normativeNetwork.isSubstituter(nA) &&
				!this.normativeNetwork.isSubstituter(nB)) {
			return nB;
		}
		else if(this.normativeNetwork.isSubstituter(nB) &&
				!this.normativeNetwork.isSubstituter(nA)) {
			return nA;
		}

		/* 4. In case of a draw, promote the norm with the lowest
		 * substitutability index (that with the lowest number of brother
		 * norms that have been substituted */
		double nASubsIndex = this.computeSubstitutabilityIndex(nA);
		double nBSubsIndex = this.computeSubstitutabilityIndex(nB);


		if(nASubsIndex != nBSubsIndex) {
			System.out.println("Subindex " + nA + ": " + nASubsIndex);
			System.out.println("Subindex " + nB + ": " + nBSubsIndex);
			return (nASubsIndex > nBSubsIndex ? nA : nB);
		}

		/* 3. Compute the highest generalisation level in the generalisation tree
		 * of each norm. Preserve the norm in the most generalised sub-tree */
		double nAGenIndex = this.computeGeneralisationIndex(nA);
		double nBGenIndex = this.computeGeneralisationIndex(nB);

		if(nAGenIndex != nBGenIndex) {
			System.out.println("GenIndex " + nA + ": " + nAGenIndex);
			System.out.println("Genindex " + nB + ": " + nBGenIndex);
			return (nAGenIndex > nBGenIndex ? nB : nA);
		}

		/* 5. Randomly choose one of the norms */
		if(this.random.nextBoolean()) {
			return nA;
		}
		return nB;
	}

	/**
	 * 
	 * @return
	 */
	private double computeGeneralisationIndex(Norm norm) {
		List<Norm> visited = new ArrayList<Norm>();
		return this.computeGeneralisationDegree(norm, visited);
	}

	/**
	 * 
	 * @return
	 */
	private double computeGeneralisationDegree(Norm norm, List<Norm> visited) {

		/* Check that the norm is represented and not visited before */
		if(!this.normativeNetwork.isRepresented(norm) || visited.contains(norm)) {
			//		if((this.normativeNetwork.getState(norm) != NetworkNodeState.ACTIVE && 
			//				this.normativeNetwork.getState(norm) != NetworkNodeState.REPRESENTED) ||
			//				visited.contains(norm)) {

			return 0; // TODO: Cambiar logica con estados active / represented
		}
		visited.add(norm);

		List<Norm> parents = this.normativeNetwork.getParents(norm);
		List<Norm> children = this.normativeNetwork.getChildren(norm);
		int genLevel = this.normativeNetwork.getGeneralisationLevel(norm);
		int numChildren = children.size();

		double genDegree = numChildren * Math.pow(10, genLevel);

		/* Explore in height (parents) */
		for(Norm parent : parents) {
			double parentGenDegree = this.computeGeneralisationDegree(parent, visited);
			genDegree += parentGenDegree;
		}

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();

		return genDegree;
	}

	/**
	 * 
	 * @return
	 */
	private double computeSubstitutabilityIndex(Norm norm) {
		List<Norm> visited = new ArrayList<Norm>();
		return this.computeSubstitutabilityDegree(norm, visited, 0);
	}

	/**
	 * 
	 * @return
	 */
	private double computeSubstitutabilityDegree(Norm norm,
			List<Norm> visited,	int distance) {

		/* Check that the norm is represented and not visited before */
		if(!this.normativeNetwork.isRepresented(norm) || visited.contains(norm)) {
			return 0;
		}
		visited.add(norm);
		List<Norm> parents = this.normativeNetwork.getParents(norm);
		List<Norm> brothers = this.normativeNetwork.getBrothers(norm);

		double subsDegree = 0.0;
		for(Norm brother : brothers) {
			if(normativeNetwork.getState(brother) == NetworkNodeState.Substituted) {
				subsDegree += Math.pow(10, distance * -1);
			}

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}

		/* Explore in height (parents) */
		for(Norm parent : parents) {
			double parentSubsDegree = this.computeSubstitutabilityDegree(parent,
					visited, distance+1);
			subsDegree += parentSubsDegree;
		}

		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();
		return subsDegree;
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
}