package es.csic.iiia.nsm.norm.refinement.generalisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.NormRefiner;
import es.csic.iiia.nsm.strategy.GenericOperators;

/**
 * @author Javi
 *
 */
public class OptimisticNormGeneraliser implements NormGeneraliser {

	// -------------------------------------------------------------------------- 
	// Attributes
	// --------------------------------------------------------------------------
	
	/* Norm refiner (to be able to activate and to deactivate norms) */
	private NormRefiner normRefiner; 
	
	/* Normative network operators */
	private GenericOperators operators;
	
	/* Generalisation mode (Shallow/Deep) */
	private NormGeneralisationMode genMode;
	
	/* Generalisation step */
	private int genStep;
	
	/* Norm synthesis metrics */
	private NormSynthesisMetrics nsMetrics;
	
	/* Norms reasoner*/
	private NormReasoner normReasoner;
	
	/* Domain functions */
	private DomainFunctions dmFunctions;
	
	/* Predicates and their domains */
	private PredicatesDomains predDomains;
	
	/* Syntactic intersections between norms */
	private Map<String, NormIntersection> normIntersections;
	
	/* General norms that are syntactically feasible */
	private NormativeSystem feasibleGenNorms;
	
	/* General norms that are not syntactically feasible */
	private NormativeSystem notFeasibleGenNorms;
	
	// -------------------------------------------------------------------------- 
	// Methods 
	// --------------------------------------------------------------------------
	
	/**
	 * Constructor of the optimistic norm generaliser
	 */
	public OptimisticNormGeneraliser(NormRefiner normRefiner, 
			GenericOperators operators, NormGeneralisationMode genMode, int genStep,
			DomainFunctions dmFunctions, NormReasoner normReasoner,
			PredicatesDomains predDomains, NormSynthesisMetrics nsMetrics) {
		
		this.normRefiner = normRefiner;
		this.operators = operators;
		this.genMode = genMode;
		this.genStep = genStep;
		this.nsMetrics = nsMetrics;
		this.predDomains = predDomains;
		this.dmFunctions = dmFunctions;
		this.normReasoner = normReasoner;
		
		this.feasibleGenNorms = new NormativeSystem();
		this.notFeasibleGenNorms = new NormativeSystem();
		this.normIntersections = new HashMap<String, NormIntersection>();
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
	@Override
	public void generaliseUp(Norm normA, NormativeNetwork normativeNetwork) {

		/* Variables to control generalisation flow */
		boolean genNormsExist, parentIsFeasible, genNormsAreConsistent;
		boolean parentIsRepresented, gensPerformed;

		NormativeSystem NS = (NormativeSystem)normativeNetwork.
				getNormativeSystem().clone();

		/* Norms to perform generalisations */
		Norm n1 = null;
		Norm n2 = null;
		Norm parent = null;

		do {
			if(genMode == NormGeneralisationMode.Deep) {
				NS = (NormativeSystem)normativeNetwork.getNormativeSystem().clone();
			}

			genNormsExist = false;
			parentIsFeasible = false;
			genNormsAreConsistent = false;
			parentIsRepresented = false;
			gensPerformed = false;
			int numNormsInNS = NS.size();
			int numNorm = 0;

			/* Retrieve NORM B: Perform pairwise comparisons of each
			 * norm with the other norms in the normative system */
			while(!gensPerformed && numNorm < numNormsInNS) {
				Norm normB = NS.get(numNorm);
				numNorm++;

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();

				/* Never generalise a norm with itself */
				if(!normA.equals(normB)) {

					/* 1. Compare norms and get generalisable norms */
					GeneralisableNorms genNorms =
							this.getGeneralisableNorms(normA, normB, genStep);

					/* 2. Check that there exist norms to perform a generalisation */
					genNormsExist = this.existsGeneralisation(genNorms);

					/* 3. Check that the parent norm is feasible */
					if(genNormsExist) {
						n1 = genNorms.getNormA();
						n2 = genNorms.getNormB();
						parent = genNorms.getParent();
						parentIsFeasible = this.isGenNormFeasible(parent, normativeNetwork);
					}

					/* 4. Check if the parent norm is already created and active
					 * (that is, the same parent norm has been created in a 
					 * previous generalisation with other two norms) */
					if(genNormsExist && parentIsFeasible) {
						parentIsRepresented = normativeNetwork.isRepresented(parent);
					}

					/* 5. Check that the parent norm is feasible (that is, it was not
					 * stated as not feasible in a previous iteration of this method 
					 * in the current time step */
					if(genNormsExist && parentIsFeasible && !parentIsRepresented) {
						genNormsAreConsistent = this.areConsistent(genNorms);
					}

					/* 6. Do not generalise if any of the generalisable norms or the parent
					 * norm are not consistent with the given domain they have been
					 * generated for */
					if(genNormsExist && parentIsFeasible && 
							!parentIsRepresented && genNormsAreConsistent) {

						this.performGeneralisation(n1, n2, parent, normativeNetwork);
						gensPerformed = true;
					}
				}
			}
		} while(gensPerformed && genMode == NormGeneralisationMode.Deep); 
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
	 * @param n1
	 * @param n2
	 * @param parent
	 */
	private void performGeneralisation(Norm n1, Norm n2, Norm parent, 
			NormativeNetwork normativeNetwork) {

		List<Norm> allNorms = new ArrayList<Norm>();
		List<Norm> children = new ArrayList<Norm>();
		children.add(n1);
		children.add(n2);
		allNorms.add(n1);
		allNorms.add(n2);
		allNorms.add(parent);

		for(Norm norm : allNorms) {

			/* If the normative network contains the norm, retrieve it
			 * and save it in the provisional norms list */
			if(normativeNetwork.contains(norm)) {
				int idx = allNorms.indexOf(norm);
				norm = normativeNetwork.getNorm(norm.getPrecondition(),
						norm.getModality(), norm.getAction());
				allNorms.set(idx, norm);
			}

			/* Else, add the norm to the normative network */
			else {
				this.operators.add(norm);
			}
		}

		/* Retrieve children norms  and parent norm */
		n1 = allNorms.get(0);
		n2 = allNorms.get(1);
		parent = allNorms.get(2);

		/* 1. Activate parent norm */
		if(!normativeNetwork.isRepresented(parent)) {
			this.normRefiner.activateUp(parent);
			normativeNetwork.getUtility(parent).reset();
		}

		/* 2. Establish generalisation relationships between each 
		 * child and the parent */
		for(Norm child : children) {
			if(!normativeNetwork.isAncestor(parent, child)) {
				this.operators.generalise(child, parent);	
			}

			/* 3. Set child as represented */
			this.operators.represent(child);
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}	  
		
		System.out.println("GENERALISATION ");
		System.out.println("N1: " + n1);
		System.out.println("N2: " + n2);
		System.out.println("parent: " + parent);
		System.out.println("--------------------------------------------");
	}

	
	/**
	 * @param feasibleGenNorms
	 * @param notFeasibleGenNorms
	 */
	private boolean isGenNormFeasible(Norm genNorm, NormativeNetwork normativeNetwork) {
		boolean feasible = true;
		List<Norm> notRepresented;

		/* If the parent is not feasible, then do not keep on computing */
		if(notFeasibleGenNorms.contains(genNorm)) {
			feasible = false;
		}
		/* If we do not know about the parent's feasibility, then compute
		 * its feasibility and save it in the corresponding list */
		else if(!feasibleGenNorms.contains(genNorm)) {

			/* Check that the parent norm does not contain a discarded norm */
			notRepresented =	normativeNetwork.getNotRepresentedNorms();

			for(Norm norm: notRepresented) {
				NetworkNodeState nState = normativeNetwork.getState(norm);

				/* If the general norm represents a norm that must not be represented, 
				 * then the general norm is not feasible */
				if(nState != NetworkNodeState.Hibernated) {
					if(this.normReasoner.satisfies(norm, genNorm) && 
							!norm.equals(genNorm))	
					{
						this.notFeasibleGenNorms.add(genNorm);
						feasible = false;
						break;
					}	
				}
			}
			/* If the general norm is feasible, add it to the  
			 * list that keeps track of feasible general norms */
			if(feasible) {
				this.feasibleGenNorms.add(genNorm);
			}
		}
		return feasible;
	}
	
	/**
	 * @param genNorms
	 */
	private boolean existsGeneralisation(GeneralisableNorms genNorms) {

		/* There is no possible generalisation */
		if(genNorms == null) {
			return false;
		}
		/* If there is a possible generalisation, then get generalisable
		 * norms and check that all norms exist */
		else {
			for(Norm norm : genNorms.getAllNorms()) {
				if(norm == null) {
					return false;
				}
			}
		}
		/* There is a possible generalisation */
		return true;
	}
	

	/**
	 * @param genNorms
	 * @return
	 */
	private boolean areConsistent(GeneralisableNorms genNorms) {
		for(Norm norm : genNorms.getAllNorms()) {
			if(!this.dmFunctions.isConsistent(norm.getPrecondition())) {
				return false;
			}
		}
		return true;
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
	protected GeneralisableNorms getGeneralisableNorms(Norm normA, Norm normB) {
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
	 */
	public void clearFeasibleGeneralisations() {
		this.feasibleGenNorms.clear();
	}
	
	/**
	 * 
	 */
	public void clearNotFeasibleGeneralisations() {
		this.notFeasibleGenNorms.clear();
	}
}
