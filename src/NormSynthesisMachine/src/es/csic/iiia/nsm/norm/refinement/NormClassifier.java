package es.csic.iiia.nsm.norm.refinement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator.Mechanism;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.generation.NormGenerator.Approach;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.refinement.synergies.NormSynergiesDetector;
import es.csic.iiia.nsm.strategy.GenericOperators;

/**
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormClassifier {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private List<EvaluationCriteria> evCriteria;
	private NormativeNetwork normativeNetwork;
	private NormGroupNetwork normGroupNetwork;
	private NormSynthesisMetrics nsMetrics;
	private NormSynthesisSettings nsmSettings;
	private NormSynergiesDetector normSynergiesDetector;
	private NormPerformancesDetector nPerfsDetector;
	private NormEvaluator.Mechanism nEvalMechanism;
	private Map<Norm, List<NormAttribute>> normClassifications;
	 
	private boolean pursueCompactness;
	private boolean pursueLiberality;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	public NormClassifier(List<EvaluationCriteria> normEvDimensions,
			NormSynthesisSettings nsmSettings, NormativeNetwork normativeNetwork,
			NormGroupNetwork normGroupNetwork, GenericOperators operators,
			NormSynthesisMetrics nsMetrics) {

		this.nsMetrics = nsMetrics;
		this.evCriteria = normEvDimensions;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = normGroupNetwork;
		this.nsmSettings = nsmSettings;
		
		this.pursueCompactness = nsmSettings.pursueCompactness();
		this.pursueLiberality = nsmSettings.pursueLiberality();
		this.nEvalMechanism = nsmSettings.getNormEvaluationMechanism();
		
		this.normClassifications = new HashMap<Norm, List<NormAttribute>>();
		
		/* Create an object to detect norms' performances */
		if(this.nEvalMechanism == Mechanism.BollingerBands) {
			this.nPerfsDetector =	new BB_NormPerformancesDetector(nsmSettings,
					normativeNetwork);	
		}
		else if(this.nEvalMechanism == Mechanism.MovingAverage) {
			this.nPerfsDetector =	new QMA_NormPerformancesDetector(nsmSettings,
					normativeNetwork);	
		}

		/* Create an object to detect norms' synergies */
		this.normSynergiesDetector = new NormSynergiesDetector(nsmSettings,
				normativeNetwork, normGroupNetwork, nsMetrics);
	}

	/**
	 * Classifies the norms in the list received by parameter. Whenever it
	 * classifies a norm, it assigns a label to the norm in the normative network
	 * 
	 * @param norms the list of norms
	 */
	public Map<Norm, List<NormAttribute>> classify(List<Norm> norms) {
		this.normClassifications.clear();

		/* Perform detection of ineffective, unnecessary and substitutable norms */
		for(Norm norm : norms) {
			
			/* Perform the same operation for each system goal */
			List<NormAttribute> classifications = new ArrayList<NormAttribute>();

			/* 1. Classify norms in terms of their effectiveness / necessity */
			classifications.addAll(
					this.checkEffectiveness(norm));
			
			classifications.addAll(
					this.checkNecessity(norm));
			
			/* 2. Detect if norms are generalisable */
			if(this.pursueCompactness) {
				classifications.addAll(
						this.checkIfGeneralisable(norm));
			}
			
			/* 3. Exploit norm synergies to detect substitutability and 
			 * complementarity relationships between norms */
			if(this.pursueLiberality) {
				
				Norm normA = norm;
				if(normGroupNetwork.hasNormGroupCombinations(normA)) {
					Map<Norm, NormGroupCombination> nGrCombs = 
							normGroupNetwork.getNormGroupCombinations(normA);
	
					for(Norm normB : nGrCombs.keySet()) {
						NormGroupCombination nABComb = nGrCombs.get(normB);
						boolean areSubstitutable = 
								normativeNetwork.areSubstitutable(normA, normB);
						
						boolean areComplementary = 
								normativeNetwork.areComplementary(normA, normB);
						
						boolean areActive = normGroupNetwork.isActive(nABComb);
						
						/* Check substitutability */
						if(!areSubstitutable && areActive) {
								classifications.addAll(normSynergiesDetector.
										checkSubstitutability(normA,	normB, nABComb));
						}
						
						/* Check complementarity */
						if(!areComplementary && areActive) {
								classifications.addAll(normSynergiesDetector.
										checkComplementarity(normA, normB, nABComb));
						}
					}
				}
			}
			
			/* Add norm attributes */
			for(NormAttribute attr : classifications) {
				this.assignAttribute(norm, attr);	
			}
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
		return normClassifications;
	}

	/**
	 * 
	 * @param norm
	 */
	private List<NormAttribute> checkEffectiveness(Norm norm) {
		List<NormAttribute> classifications = new ArrayList<NormAttribute>();
		List<NormAttribute> attributes = this.normativeNetwork.getAttributes(norm);
		
		/* General norms are not classified as ineffective */
		if(this.normativeNetwork.isLeaf(norm)) {

			boolean isEffective = attributes.contains(NormAttribute.Effective);
			boolean isIneffective = attributes.contains(NormAttribute.Ineffective);
			boolean notClassified = !isEffective && !isIneffective;

			/* The norm is classified as effective (or even it has not been
			 * classified as ineffective yet) and now it under performs
			 * -> classify the norm as ineffective */
			if(isEffective || notClassified) {
				if(this.nPerfsDetector.shouldBeDeactivated(norm, 
						EvaluationCriteria.Effectiveness)) {
					
					classifications.add(NormAttribute.Ineffective);
				}
			}

			/* The norm is classified as ineffective (or even it has not been
			 * classified as effective yet) and now it performs well
			 * -> classify the norm as effective */
			if(isIneffective || notClassified)	{
				if(this.nPerfsDetector.shouldBeActivated(norm,
						EvaluationCriteria.Effectiveness)) {
					
					classifications.add(NormAttribute.Effective);
				}
			}
		}
		return classifications;
	}

	/**
	 * 
	 * @param norm
	 */
	private List<NormAttribute> checkNecessity(Norm norm) {
		List<NormAttribute> classifications = new ArrayList<NormAttribute>(); 
		List<NormAttribute> attrs = this.normativeNetwork.getAttributes(norm);
		boolean normGenerationIsReactive = 
				nsmSettings.getNormGenerationApproach() == Approach.Deliberative;

		/* Try to classify only leaves in the normative network (general
		 * norms are never classified as unnecessary) */
		if(this.normativeNetwork.isLeaf(norm)) {
			
			boolean isClassifiedAsNecessary = attrs.contains(NormAttribute.Necessary);
			boolean isClassifiedAsUnnecessary = attrs.contains(NormAttribute.Unnecessary);
			boolean hasNotBeenClassified = !isClassifiedAsNecessary && !isClassifiedAsUnnecessary;

			/* Check whether the norm can be considered as unnecessary. If the norm  
			 * has been classified as necessary (or even it has not been classified 
			 * as unnecessary yet) and now it under performs -> classify the norm 
			 * as unnecessary */
			if(isClassifiedAsNecessary || hasNotBeenClassified) {
				if(this.nPerfsDetector.shouldBeDeactivated(norm,
						EvaluationCriteria.Necessity)) {
					
					classifications.add(NormAttribute.Unnecessary);
				}
			}

			/* ONLY if norm generation is deliberative, check if the norm can be 
			 * considered as necessary. If the norm is classified as unnecessary 
			 * (or even it has not been classified as necessary yet) and now 
			 * it performs well -> classify the norm as necessary */
			if(normGenerationIsReactive) {
				if(isClassifiedAsUnnecessary || hasNotBeenClassified)	{
					if(this.nPerfsDetector.shouldBeActivated(norm, 
							EvaluationCriteria.Necessity)) {
						
						classifications.add(NormAttribute.Necessary);
					}
				}	
			}
		}
		return classifications;
	}

	/**
	 * 
	 * @param norm
	 */
	private List<NormAttribute> checkIfGeneralisable(Norm norm) {
		List<NormAttribute> classifications = new ArrayList<NormAttribute>(); 
		List<NormAttribute> attrs = normativeNetwork.getAttributes(norm);
		
		boolean isActive = normativeNetwork.getState(norm) == NetworkNodeState.Active;
		boolean isGeneralisable = attrs.contains(NormAttribute.Generalisable);
		boolean canGeneralise = true;
		
		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			
			/* Check if the norm is generalisable only if it is active and 
			 * it has not been previously classified as generalisable */
			if(isActive && !isGeneralisable) {
				for(EvaluationCriteria dim : this.evCriteria)	 {
					Utility utility = this.normativeNetwork.getUtility(norm);
					
					double topBoundary = utility.
							getPerformanceRange(dim, goal).getCurrentTopBoundary();
					double satDegree = this.nsmSettings.
							getNormGeneralisationBoundary(dim, goal);
	
					if(topBoundary < satDegree) {
						canGeneralise = false;
					}
				}
			}
		}
		
		/* Add attribute to be generalised */ 
		if(canGeneralise) {
			classifications.add(NormAttribute.Generalisable);
		}
		return classifications;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getSubstitutableNorms(Norm norm) {
		return this.normSynergiesDetector.getSubstitutableNorms(norm);
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getComplementaryNorms(Norm norm) {
		return this.normSynergiesDetector.getComplementaryNorms(norm);
	}

	
	//---------------------------------------------------------------------------
	// Private methods 
	//---------------------------------------------------------------------------
	
	/**
	 * 
	 * @param norm
	 * @param attribute
	 */
	private void assignAttribute(Norm norm, NormAttribute attribute) {

		if(!normClassifications.containsKey(norm)) {
			normClassifications.put(norm, new ArrayList<NormAttribute>());
		}
		if(!normClassifications.get(norm).contains(attribute)) {
			normClassifications.get(norm).add(attribute);
		}

		/* Add attribute to the normative network */
		this.normativeNetwork.addAttribute(norm, attribute);

		/* Remove the dual attribute from the normative network */
		switch(attribute) {
		case Effective: 
			this.normativeNetwork.removeAttribute(norm, NormAttribute.Ineffective);
			break;

		case Ineffective:
			this.normativeNetwork.removeAttribute(norm, NormAttribute.Effective);
			break;

		case Necessary:
			this.normativeNetwork.removeAttribute(norm, NormAttribute.Unnecessary);
			break;

		case Unnecessary:
			this.normativeNetwork.removeAttribute(norm, NormAttribute.Necessary);
			break;
			
		case Substitutable:
			List<Norm> subs = this.normSynergiesDetector.getSubstitutableNorms(norm);
			for(Norm normB : subs) {
				
				/* Add complementarity relationship */
				this.normativeNetwork.addSubstitutability(norm, normB);
				this.normativeNetwork.removeComplementarity(norm, normB);

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
				this.nsMetrics.incNumNodesVisited();
			}
			break;

		case Complementary:
			List<Norm> comp = this.normSynergiesDetector.getComplementaryNorms(norm);
			for(Norm normB : comp) {
				
				/* Add complementarity relationship */
				this.normativeNetwork.addComplementarity(norm, normB);
				this.normativeNetwork.removeSubstitutability(norm, normB);

				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
				this.nsMetrics.incNumNodesVisited();
			}
			break;
			
		default:
			break;
		}
	}
}
