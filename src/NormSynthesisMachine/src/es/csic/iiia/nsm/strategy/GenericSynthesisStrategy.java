package es.csic.iiia.nsm.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.evaluation.NormComplianceOutcomes;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator;
import es.csic.iiia.nsm.norm.evaluation.NormUtilityFunction;
import es.csic.iiia.nsm.norm.evaluation.NormsApplicableInView;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupOutcomes;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerator;
import es.csic.iiia.nsm.norm.generation.cbr.CBRConflictSolvingMachine;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.NormClassifier;
import es.csic.iiia.nsm.norm.refinement.NormRefiner;
import es.csic.iiia.nsm.norm.refinement.generalisation.GeneralisationTrees;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.sensing.Monitor;
import es.csic.iiia.nsm.sensing.ViewTransition;

/**
 * A flexible, generic norm synthesis strategy that can be configured as 
 * desired by a system designer 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class GenericSynthesisStrategy implements NormSynthesisStrategy {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	/* Settings and MAS perception */
	protected List<EvaluationCriteria> normEvDimensions;
	protected NormSynthesisMachine nsm;
	protected NormSynthesisSettings nsmSettings;
	protected Monitor monitor;
	protected NormGeneraliser.Approach genApproach;
	protected NormGeneralisationMode genMode; 
	protected int genStep;
	
	/* NSM information model */
	protected DomainFunctions dmFunctions;
	protected PredicatesDomains predicatesDomains;
	protected NormReasoner normReasoner;
	protected NormativeNetwork normativeNetwork;
	protected NormGroupNetwork normGroupNetwork;
	protected NormSynthesisMetrics nsMetrics;
	protected GenericOperators operators;
	
	/* Norm generation stuff */
	protected CBRConflictSolvingMachine cbrMachine;
	protected NormGenerator normGenerator;
	
	/* Norm evaluation stuff */
	protected NormEvaluator normEvaluator;
	protected NormUtilityFunction utilityFunction;
	
	/* Norm refinement stuff */
	protected NormClassifier normClassifier;
	protected NormRefiner normRefiner;
	protected GeneralisationTrees genTrees;
	
	/* Auxiliar data structures */
	protected Map<Goal,List<Conflict>> conflicts;
	protected Map<ViewTransition, NormsApplicableInView> normApplicability;
	protected Map<Goal,Map<ViewTransition, NormComplianceOutcomes>> normCompliance;
	protected Map<Goal,NormGroupOutcomes> normGroupCompliance;
	protected List<ViewTransition> viewTransitions;
	protected List<Norm> normsInNormativeNetwork;
	protected List<Norm> normsInNormativeSystem;
	protected List<Norm> normsAddedToNNThisCycle;
	protected List<Norm> normsAddedToNSThisCycle;
	protected List<Norm> normsRemovedFromNSThisCycle;
	protected List<Norm> normsActivatedDuringGeneration;
	protected List<Norm> visitedNorms;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	nsm a norm synthesis machine
	 * @param 	genMode the SIMON generalisation mode
	 */
	public GenericSynthesisStrategy(NormSynthesisMachine nsm,
			NormGeneralisationMode genMode, int genStep) {

		/* Settings */ 
		this.nsm = nsm;
		this.nsmSettings = this.nsm.getNormSynthesisSettings();
		this.nsMetrics = nsm.getNormSynthesisMetrics();
		this.genApproach = nsmSettings.getNormGeneralisationApproach();
		this.genMode = nsmSettings.getOptimisticNormGeneralisationMode();
		this.genStep = nsmSettings.getOptimisticNormGeneralisationStep();
		this.normEvDimensions = nsm.getNormEvaluationDimensions();
		this.monitor = nsm.getMonitor();
		
		/* Information model */
		this.dmFunctions = nsm.getDomainFunctions();
		this.predicatesDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.normGroupNetwork = nsm.getNormGroupNetwork();
		this.normReasoner = nsm.getNormReasoner();
		
		/* Control lists */
		this.normsInNormativeNetwork 			= new ArrayList<Norm>();
		this.normsInNormativeSystem  			= new ArrayList<Norm>();
		this.normsAddedToNNThisCycle 			= new ArrayList<Norm>();
		this.normsAddedToNSThisCycle 			= new ArrayList<Norm>();
		this.normsRemovedFromNSThisCycle 	= new ArrayList<Norm>();
		this.visitedNorms 								= new ArrayList<Norm>();
		
		/* Data structure for conservative norm generalisation */
		this.genTrees = new GeneralisationTrees(this.predicatesDomains, 
				this.dmFunctions, this.normativeNetwork, this.nsMetrics);
		
		/* Normative network operators */
		this.operators = new GenericOperators(this, normReasoner,
				nsm, genTrees);
		
		/* MAS perception stuff */
		this.viewTransitions = new ArrayList<ViewTransition>();
		this.conflicts = new HashMap<Goal, List<Conflict>>();
	
		/* Norm generation stuff */
		this.cbrMachine = new CBRConflictSolvingMachine(this.normativeNetwork,
				normReasoner, this, nsm.getRandom(), this.nsMetrics);
		
		this.normGenerator = new NormGenerator(nsmSettings, monitor,
				normReasoner, dmFunctions, operators, normativeNetwork, 
				cbrMachine, nsm.getRandom(), nsMetrics, genApproach);

		/* Norm evaluation stuff */
		this.normApplicability = new HashMap<ViewTransition, 
				NormsApplicableInView>();

		this.normCompliance = new HashMap<Goal, 
				Map<ViewTransition,NormComplianceOutcomes>>();
		
		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normCompliance.put(goal, new HashMap<ViewTransition,
					NormComplianceOutcomes>());
		}

		this.normGroupCompliance = new HashMap<Goal,NormGroupOutcomes>();
		for(Goal goal : nsmSettings.getSystemGoals()) {
			this.normGroupCompliance.put(goal, new NormGroupOutcomes());
		}
		
		this.normEvaluator = new NormEvaluator(normEvDimensions,
				nsmSettings, dmFunctions, normativeNetwork, normGroupNetwork,
				normReasoner, operators, nsm.getNormSynthesisMetrics(),
				nsmSettings.getNormEvaluationMechanism());

		/* Norm refinement stuff */
		Random random = this.nsm.getRandom();
		this.normRefiner = new NormRefiner(normEvDimensions, nsmSettings, 
				dmFunctions, predicatesDomains, normativeNetwork, normGroupNetwork,
				normReasoner, nsm.getNormSynthesisMetrics(), operators,
				genApproach, genMode, genStep, random, genTrees);
	}

	/**
	 * Executes the norm synthesis strategy and outputs the resulting
	 * normative system. The norm synthesis cycle consists in three steps:
	 * <ol>
	 * <li> Norm generation. Generates norms for each detected conflict.
	 * 			Generated norms are aimed to avoid detected conflicts in the future;
	 * <li> Norm evaluation. Evaluates norms in terms of their effectiveness
	 * 			and necessity, based on the outcome of their compliances and 
	 * 			infringements, respectively; and
	 * <li> Norm refinement. Generalises norms which utilities are over
	 * 			generalisation thresholds, and specialises norms which utilities
	 * 			are under specialisation thresholds. Norm generalisations can be
	 * 			performed in Shallow or Deep mode.
	 * 
	 * @return the normative system resulting from the norm synthesis cycle
	 */
	public NormativeSystem execute() {	
		this.nsMetrics.resetNonRegulatedConflicts();
		this.visitedNorms.clear();

		/* Norm generation */
		normsActivatedDuringGeneration = 
				this.normGenerator.step(viewTransitions, conflicts);

		/* Norm evaluation */
		this.normEvaluator.step(viewTransitions, normApplicability,
				normCompliance, normGroupCompliance);

		/* Norm refinement */
		this.normRefiner.step(normApplicability, normsActivatedDuringGeneration);

		/* Manage lists that control new additions to the normative network,
		 * normative system, as well as norms that have been removed */
		this.manageNormControlLists();
		
		/* Return the current normative system */
		return normativeNetwork.getNormativeSystem();
	}

	/**
	 * 
	 */
	protected void manageNormControlLists() {
		this.normsAddedToNNThisCycle.clear();
		this.normsAddedToNSThisCycle.clear();
		this.normsRemovedFromNSThisCycle.clear();
		
		/* Check norm additions to the normative network */
		for(Norm norm : this.normativeNetwork.getNorms()) {
			if(!this.normsInNormativeNetwork.contains(norm)) {
				this.normsInNormativeNetwork.add(norm);
				this.normsAddedToNNThisCycle.add(norm);
				this.normReasoner.addNorm(norm); 
			}
		}
		
		/* Check norm additions to the normative system */
		for(Norm norm : this.normativeNetwork.getNormativeSystem()) {
			if(!this.normsInNormativeSystem.contains(norm)) {
				this.normsInNormativeSystem.add(norm);
				this.normsAddedToNSThisCycle.add(norm);
			}
		}
		
		/* Check norm removals from the normative system */
		List<Norm> toRemove = new ArrayList<Norm>();
		for(Norm norm : this.normsInNormativeSystem) {
			if(!this.normativeNetwork.getNormativeSystem().contains(norm)) {
				toRemove.add(norm);
				this.normsRemovedFromNSThisCycle.add(norm);
			}
		}
		for(Norm norm : toRemove) {
			this.normsInNormativeSystem.remove(norm);
		}
	}

	/** 
	 * Adds a default normative system (i.e., a pool of norms
	 * that will be active in the normative network)
	 * 
	 * @param poolOfNorms the pool of active norms in the normative network
	 */

  public void addDefaultNormativeSystem(List<Norm> defaultNorms) {
  	if(defaultNorms != null) {
		  for(Norm norm : defaultNorms) {
		  	this.operators.add(norm);
		  	this.operators.activate(norm);
		  }
  	}
  }
  
	/**
	 * 
	 * @return
	 */
	public GenericOperators getOperators() {
		return this.operators;
	}
	
	/**
	 * 
	 * @return
	 */
	public NormGenerator getNormGenerator() {
		return normGenerator;
	}

	/**
	 * 
	 * @return
	 */
	public NormEvaluator getNormEvaluator() {
		return normEvaluator;
	}

	/**
	 * 
	 * @return
	 */
	public NormClassifier getNormClassifier() {
		return normClassifier;
	}

	/**
	 * 
	 * @return
	 */
	public NormRefiner getNormRefiner() {
		return normRefiner;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsAddedToNSThisCycle() {
		return this.normsAddedToNSThisCycle;
	}

	/**
	 * 
	 * @return
	 */
	public List<Norm> getNormsRemovedFromNSThisCycle() {
		return this.normsRemovedFromNSThisCycle;
	}

	/**
	 * 
	 */
  @Override
  public void setup(NormSynthesisMachine nsm) {}
}