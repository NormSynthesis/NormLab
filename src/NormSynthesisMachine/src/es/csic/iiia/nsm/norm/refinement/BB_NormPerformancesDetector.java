package es.csic.iiia.nsm.norm.refinement;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class BB_NormPerformancesDetector implements NormPerformancesDetector {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	/* The settings of the norm synthesis machine */
	private NormSynthesisSettings nsmSettings; 

	/* A normative network */
	private NormativeNetwork normativeNetwork;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 * @param normativeNetwork
	 */
	public BB_NormPerformancesDetector(NormSynthesisSettings nsmSettings, 
			NormativeNetwork normativeNetwork) {

		this.nsmSettings = nsmSettings;
		this.normativeNetwork = normativeNetwork;
	}

	/**
	 * Returns <tt>true</tt> if the node (whether it is a norm or a norm group)
	 * under performs with respect to a certain dimension (effectiveness or 
	 * necessity), and in terms of all system goals
	 * 
	 * @param node the node (norm or norm group)
	 * @param dim the evaluation dimension
	 * @return <tt>true</tt> if the norm under performs with respect
	 * 					to a certain criteria (effectiveness or necessity), 
	 * 					and in terms of the norm's goal
	 */
	public boolean shouldBeDeactivated(Norm norm, EvaluationCriteria dim) {
		Utility utility = this.normativeNetwork.getUtility(norm);
		
		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			double topBoundary = (float)utility.getPerformanceRange(dim, goal).
					getCurrentTopBoundary();
			double satDegree = this.nsmSettings.getNormDeactivationThreshold(dim, goal);

			int minNumEvalsToClassify = this.nsmSettings.getMinEvaluationsToClassifyNorms();
			int numValues = utility.getPerformanceRange(dim, goal).getNumPunctualValues();
	
			/* The norm under performs */
			if(numValues > minNumEvalsToClassify) {
				if(topBoundary < satDegree) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> if the norm performs well with respect to a certain
	 * criteria (effectiveness or necessity), and in terms of the norm's goal
	 * 
	 * @param norm the norm
	 * @param dim the evaluation dimension
	 * @return <tt>true</tt> if the norm performs well with respect
	 * 					to a certain criteria (effectiveness or necessity), 
	 * 					and in terms of the norm's goal
	 */
	public boolean shouldBeActivated(Norm norm, EvaluationCriteria dim) {
		Utility utility = this.normativeNetwork.getUtility(norm);
		
		for(Goal goal : this.nsmSettings.getSystemGoals()) {
			double bottomBoundary = utility.getPerformanceRange(dim, goal).
					getCurrentBottomBoundary();
			double satDegree = this.nsmSettings.getNormDeactivationThreshold(dim, goal);

			int minNumEvalsToClassify = this.nsmSettings.getMinEvaluationsToClassifyNorms();
			int numValues = utility.getPerformanceRange(dim, goal).getNumPunctualValues();
	
			/* The norm under performs */
			if(numValues > minNumEvalsToClassify) {
				if(bottomBoundary > satDegree) {
					return true;
				}
			}
		}
		return false;
	}
}
