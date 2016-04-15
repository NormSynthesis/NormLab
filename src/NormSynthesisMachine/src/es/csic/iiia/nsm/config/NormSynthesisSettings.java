package es.csic.iiia.nsm.config;

import java.util.List;

import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator;
import es.csic.iiia.nsm.norm.generation.NormGenerator;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * Basic settings of the Norm Synthesis Machine. For instance,
 * the default utility of norms, or the number of ticks of stability
 * for the NSM to converge
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public interface NormSynthesisSettings {

	/**
	 * Returns the strategy to be used by the NSM to perform norm synthesis  
	 * 
	 * @return the strategy to be used by the NSM to perform norm synthesis
	 */
	public NormSynthesisStrategy.Option getNormSynthesisStrategy();
		
	/* Goals and dimensions to synthesise and evaluate norms */
	
	/**
	 * The goals of the NSM 
	 * 
	 * @return a {@code List} containing the system goals
	 * @see Goal
	 */
	public List<Goal> getSystemGoals();
	
	/**
	 * 
	 * @return
	 */
	public boolean pursueCompactness();
	
	/**
	 * 
	 * @return
	 */
	public boolean pursueLiberality();

	/**
	 * 
	 */
	public void setPursueCompactness(boolean pursueCompactness);
	
	/**
	 * 
	 */
	public void setPursueLiberality(boolean pursueLiberality);
	
	
	/* ------------------------
	 * NORM EVALUATION SETTINGS
	 * ------------------------ */
	
	
	/**
	 * Returns the mechanism employed to evaluate norms, either 
	 * RL and Bollinger Bands, or moving average
	 *  
	 * @return
	 */
	public NormEvaluator.Mechanism getNormEvaluationMechanism();
	
	/**
	 * The default utility when norms are created. Typically, it is set to 0.5  
	 * 
	 * @return the default norms' utility
	 */
	public double getNormsDefaultUtility();
	
	/**
	 * The learning rate (alpha) to compute rewards for norm evaluation. This
	 * parameter, which is only used for IRON and SIMON, is typically
	 * set to 0.1 or 0.2
	 * 
	 * @return the norm evaluation learning rate (alpha parameter)
	 */
	public double getNormEvaluationLearningRate();
	
	/**
	 * Returns the size of the window to compute norms' performance ranges
	 * 
	 * @return the size of the window to compute norms' performance ranges
	 */
	public int getNormsPerformanceRangesSize();
	
	/**
	 * 
	 * @param mechanism
	 */
	public void setNormEvaluationMechanism(NormEvaluator.Mechanism mechanism);
	
	/**
	 * 
	 * @param defaultUtility
	 */
	public void setNormsDefaultUtility(double defaultUtility);
	
	
	/* ----------------------------
	 * NORM GENERALISATION SETTINGS 
	 * ---------------------------- */

	
	/**
	 * 
	 * @return
	 */
	public NormGeneraliser.Approach getNormGeneralisationApproach();

	/**
	 * Returns the norm generalisation mode (Shallow/Deep) to be applied
	 * during the norm refinement phase (employed by SIMON and subsequent
	 * synthesis strategies)
	 * 
	 * @return 	the norm generalisation mode (Shallow/Deep) to be applied
	 * 					during the norm refinement phase (employed by SIMON and
	 * 					subsequent synthesis strategies)
	 * 
	 * @see NormGeneralisationMode
	 */
	public NormGeneralisationMode getOptimisticNormGeneralisationMode();
	
	/**
	 * Returns the norm generalisation step (namely, the number of norm 
	 * predicates that can be generalised simultaneously) to be used during
	 * the norm refinement phase 
	 * 
	 * @return 	the norm generalisation step (namely, the number of norm 
	 * 					predicates that can be generalised simultaneously) to be 
	 * 					used during the norm refinement phase
	 */
	public int getOptimisticNormGeneralisationStep();

	/**
	 * Returns the boundary over which a norm's utility is
	 * considered high enough to be generalised with other norms 
	 * 
	 * @param dim the dimension of the utility (effectiveness/necessity)
	 * @param goal the goal of the utility
	 * @return the generalisation boundary
	 * @see EvaluationCriteria
	 * @see Goal
	 * @see NormGroupUtility
	 */
	public double getNormGeneralisationBoundary(EvaluationCriteria dim, Goal goal);
	
	/**
	 * 
	 * @param genApproach
	 */
	public void setNormGeneralisationApproach(NormGeneraliser.Approach genApproach);
	
	
	/* -------------------------------------
	 * NORM ACTIVATION/DEACTIVATION SETTINGS 
	 * ------------------------------------- */
	
	/**
	 * Returns the approach employed by norm generation to decide 
	 * when to activate norms
	 * 
	 * @return 	the approach employed by norm generation to decide 
	 * when to activate norms
	 */
	public NormGenerator.Approach getNormGenerationApproach();
	
	
	/**
	 * 
	 * @param approach
	 */
	public void setNormGenerationApproach(NormGenerator.Approach approach);
	
	/**
	 * Returns the boundary below which a norm's utility is
	 * considered low enough to specialise the norm 
	 * 
	 * @param dim the dimension of the utility (effectiveness/necessity)
	 * @param goal the system goal of the utility
	 * @return the specialisation boundary
	 * @see EvaluationCriteria
	 * @see Goal
	 * @see NormGroupUtility
	 */
	public double getNormDeactivationBoundary(EvaluationCriteria dim, Goal goal);
	
	/**
	 * Returns the boundary above which a norm's utility is
	 * considered high enough to be generalised with other norms 
	 * 
	 * @param dim the dimension of the utility (effectiveness/necessity)
	 * @param goal the goal of the utility
	 * @return the generalisation boundary
	 * @see EvaluationCriteria
	 * @see Goal
	 * @see NormGroupUtility
	 */
	public double getNormDeactivationBoundaryEpsilon(EvaluationCriteria dim, Goal goal);

	/**
	 * 
	 * @return
	 */
	public int getMinEvaluationsToClassifyNorms();
		
	/**
	 * 
	 * @return
	 */
	public int getMinEvaluationsToClassifyNormGroups();

	/**
	 * Returns the number of ticks to consider that the norm synthesis machine 
	 * has converged to a normative system. It is considered to have converged
	 * whenever, during N ticks, the normative system remains without changes.
	 * 
	 * @return the number of ticks of stability
	 */
	public long getNumTicksOfStabilityForConvergence();
	
	/**
	 * 
	 * @param numEvals
	 */
	public void setMinEvaluationsToClassifyNorms(int numEvals);
		
	/**
	 * 
	 * @return
	 */
	public String getUserStrategy();
}
