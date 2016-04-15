package es.csic.iiia.nsm.norm.refinement.generalisation;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.agent.EnvironmentAgentAction;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.refinement.NormRefiner;
import es.csic.iiia.nsm.strategy.GenericOperators;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class ConservativeNormGeneraliser implements NormGeneraliser {

	/* Norms refiner */
	private NormRefiner normRefiner;
	
	/* Norm synthesis metrics */
	private NormSynthesisMetrics nsMetrics;

	/* Norm synthesis operators */
	private GenericOperators operators; 
	
	/* Potential generalisations */
	private GeneralisationTrees genTrees;

	/**
	 * 
	 * @param genMode
	 * @param genStep
	 * @param dmFunctions
	 * @param normReasoner
	 * @param nsMetrics
	 */
	public ConservativeNormGeneraliser(NormRefiner normRefiner, 
			GenericOperators operators, DomainFunctions dmFunctions, 
			PredicatesDomains predDomains, NormativeNetwork normativeNetwork,
			NormSynthesisMetrics nsMetrics,	GeneralisationTrees genTrees) {

		this.normRefiner = normRefiner;
		this.nsMetrics = nsMetrics;
		this.genTrees = genTrees;
		this.operators = operators;
	}

	/**
	 * 
	 */
	@Override
	public void generaliseUp(Norm norm, NormativeNetwork normativeNetwork) {
		List<PotentialGeneralisation> validGeneralisations;
		
		/* Get valid generalisations of the norm */
		validGeneralisations = validGeneralisations(normativeNetwork, norm);

		/* Perform each valid generalisation */
		for(PotentialGeneralisation generalisation : validGeneralisations) {
			List<Norm> children = generalisation.getChildren();
			Norm parent = generalisation.getParent();
		
			/* A list with all the norms to process */
			List<Norm> allNorms = new ArrayList<Norm>();
			allNorms.addAll(children);
			allNorms.add(parent);
			
			/* Add each norm to the normative network */
			for(Norm n : allNorms) {

				/* If the normative network already contains the norm, retrieve it
				 * and save it in the list of children */
				if(normativeNetwork.contains(n)) {
					int idx = allNorms.indexOf(n);
					n = normativeNetwork.getNorm(n.getPrecondition(),
							n.getModality(), n.getAction());
					children.set(idx, n);
				}

				/* Otherwise, add the norm to the normative network */
				else {
					this.operators.add(n);
				}
			}
			
			/* Retrieve parent norm (just in case it already existed 
			 * in the normative network */
			parent = allNorms.get(allNorms.size()-1);
			
			/* 1. Activate parent norm */
			this.normRefiner.activateUp(parent);
			
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
			
			/* Set the generalisation as performed */
			generalisation.setPerformed(true);
			
			/* Update computation metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}	

	/**
	 * 
	 * @param nNetwork
	 * @param norm
	 * @return
	 */
	private List<PotentialGeneralisation> validGeneralisations(NormativeNetwork nNetwork, Norm norm) {
		List<PotentialGeneralisation> potGens = this.genTrees.get(norm);
		List<PotentialGeneralisation> validGens = new ArrayList<PotentialGeneralisation>();
		
		if(potGens == null) {
			System.out.println();
		}
		for(PotentialGeneralisation potGen : potGens) {
			
			/* Only perform the generalisation if it has not been performed */
			if(!potGen.isPerformed()) {
				if(this.isValid(potGen, nNetwork)) {
					validGens.add(potGen);
				}	
			} 
			
			/* Update computation metrics */
			this.nsMetrics.incNumNodesVisited();
		}
		return validGens;
	}

	/**
	 * 
	 * @param candGen
	 * @param nNetwork
	 * 
	 * @return
	 */
	private boolean isValid(PotentialGeneralisation candGen,
			NormativeNetwork nNetwork) {

		for(Norm norm : candGen.getChildren()) {
			SetOfPredicatesWithTerms precond = norm.getPrecondition();
			NormModality modality = norm.getModality();
			EnvironmentAgentAction action = norm.getAction();

			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();

			// If child does not exist -> Not valid
			if(!nNetwork.containsNormWith(precond, modality, action))
				return false;

			Norm childNorm = nNetwork.getNorm(precond, modality, action);

			// If child exists but it is not represented -> Not valid
			if(!nNetwork.isRepresented(childNorm)){
				return false;
			}
		}
		return true;
	}
}
