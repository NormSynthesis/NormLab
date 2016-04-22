/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.refinement.synergies;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.evaluation.PerformanceRange;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.utilities.Minkowski;

/**
 * @author Javi
 *
 */
public class NumericalSeriesComparator {
	
	/* */
	private NormGroupNetwork normGroupNetwork;
	
	/* */
	private NormSynthesisSettings nsmSettings;
	
	/* To compute the minkowski distance between two series*/
	private Minkowski minkowski;
	

	/**
	 * 
	 */
	public NumericalSeriesComparator(NormGroupNetwork normGroupNetwork, 
			NormSynthesisSettings nsmSettings) {
		
		this.normGroupNetwork = normGroupNetwork;
		this.nsmSettings = nsmSettings;
		
		this.minkowski = new Minkowski(2);
	}


	/**
	 * 
	 * @param group1
	 * @param group2
	 * @param goal
	 * @return
	 */
	public boolean isGreater(NormGroup group1, NormGroup group2, Goal goal) 
	throws NotEnoughEvidenceException, IllegalArgumentException {
		
		double distance = 1;

		PerformanceRange perfRangeGroup1 = this.normGroupNetwork.
				getUtility(group1).getPerformanceRange(EvaluationCriteria.Effectiveness, goal);
		PerformanceRange perfRangeGroup2 = this.normGroupNetwork.
				getUtility(group2).getPerformanceRange(EvaluationCriteria.Effectiveness, goal);
		int minNumEvalsToClassify =
				this.nsmSettings.getMinEvaluationsToClassifyNormGroups();

		List<Double> avg1 = perfRangeGroup1.getAverage();
		List<Double> avg2 = perfRangeGroup2.getAverage();
		List<Double> topBndr1 = perfRangeGroup1.getTopBoundary();
		List<Double> topBndr2 = perfRangeGroup2.getTopBoundary();
		List<Double> bottomBndr1 = perfRangeGroup1.getBottomBoundary();
		List<Double> bottomBndr2 = perfRangeGroup2.getBottomBoundary();

		List<List<Double>> seriesGroup1 = new ArrayList<List<Double>>();
		List<List<Double>> seriesGroup2 = new ArrayList<List<Double>>();
		seriesGroup1.add(avg1);
		seriesGroup1.add(topBndr1);
		seriesGroup1.add(bottomBndr1);
		seriesGroup2.add(avg2);
		seriesGroup2.add(topBndr2);
		seriesGroup2.add(bottomBndr2);

		for(int i=0; i<seriesGroup1.size(); i++) {
			List<Double> s1 = seriesGroup1.get(i);
			List<Double> s2 = seriesGroup2.get(i);

			int numValues = s1.size();
			if(numValues > s2.size()) {
				numValues = s2.size();
			}

			/* Check that there is a minimum number of values 
			 * to evaluate similarity */ 
			if(numValues < minNumEvalsToClassify) {
				throw new NotEnoughEvidenceException();
			}

			distance = this.minkowski.distance(s1, s2, numValues);
			
			float s1Avg = this.average(s1);
			float s2Avg = this.average(s2);

			if(distance < 1.2 || s1Avg <= s2Avg) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param group1
	 * @param group2
	 * @param goal
	 * @return
	 */
	public boolean areSimilar(NormGroup group1, NormGroup group2, Goal goal)
	throws NotEnoughEvidenceException, IllegalArgumentException {
		double distance = 1;

		PerformanceRange perfRangeGroup1 = this.normGroupNetwork.
				getUtility(group1).getPerformanceRange(EvaluationCriteria.Effectiveness, goal);
		PerformanceRange perfRangeGroup2 = this.normGroupNetwork.
				getUtility(group2).getPerformanceRange(EvaluationCriteria.Effectiveness, goal);
		int minNumEvalsToClassify =
				this.nsmSettings.getMinEvaluationsToClassifyNormGroups();

		List<Double> avg1 = perfRangeGroup1.getAverage();
		List<Double> avg2 = perfRangeGroup2.getAverage();
		List<Double> topBndr1 = perfRangeGroup1.getTopBoundary();
		List<Double> topBndr2 = perfRangeGroup2.getTopBoundary();
		List<Double> bottomBndr1 = perfRangeGroup1.getBottomBoundary();
		List<Double> bottomBndr2 = perfRangeGroup2.getBottomBoundary();

		List<List<Double>> seriesGroup1 = new ArrayList<List<Double>>();
		List<List<Double>> seriesGroup2 = new ArrayList<List<Double>>();
		seriesGroup1.add(avg1);
		seriesGroup1.add(topBndr1);
		seriesGroup1.add(bottomBndr1);
		seriesGroup2.add(avg2);
		seriesGroup2.add(topBndr2);
		seriesGroup2.add(bottomBndr2);

		for(int i=0; i<seriesGroup1.size(); i++) {
			List<Double> s1 = seriesGroup1.get(i);
			List<Double> s2 = seriesGroup2.get(i);

			int numValues = s1.size();
			if(numValues > s2.size()) {
				numValues = s2.size();
			}

			/* Check that there is a minimum number of values 
			 * to evaluate similarity */ 
			if(numValues < minNumEvalsToClassify) {
				throw new NotEnoughEvidenceException();
			}

			distance = this.minkowski.distance(s1, s2, numValues);

			if(distance > 1.2) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param floats
	 * @return
	 */
	private float average(List<Double> doubles) {
		float avg = 0f;
		for(Double num : doubles) {
			avg += num;
		}		
		return avg/doubles.size();
	}

}
