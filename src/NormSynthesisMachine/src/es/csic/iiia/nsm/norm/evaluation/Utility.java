package es.csic.iiia.nsm.norm.evaluation;

import java.util.HashMap;
import java.util.List;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;

/**
 * The utility of a norm, that may have utility with respect to several
 * dimensions and goals, and hence may be evaluated in terms of several
 * dimensions and goals
 *  
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see EvaluationCriteria
 * @see Goal
 */
public class Utility {

	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------

	private double defaultUtility;
	private int perfRangeSize;

	private List<EvaluationCriteria> dimensions;
	private List<Goal> goals;

	private HashMap<EvaluationCriteria, HashMap<Goal, Double>> scores;
	private HashMap<EvaluationCriteria,	HashMap<Goal, PerformanceRange>> perfRanges;

	//---------------------------------------------------------------------------
	// Methods 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param dimensions the norm evaluation dimensions
	 * @param goals the system goals
	 */
	public Utility(double defaultUtility, int perfRangeSize) {
		this.defaultUtility = defaultUtility;
		this.perfRangeSize = perfRangeSize;

		this.scores = new HashMap<EvaluationCriteria, HashMap<Goal, Double>>();	
		this.perfRanges = new HashMap<EvaluationCriteria,
				HashMap<Goal, PerformanceRange>>();
	}

	/**
	 * Constructor with dimensions and goals
	 * 
	 * @param dimensions the norm evaluation dimensions
	 * @param goals the system goals
	 */
	public Utility(double defaultUtility, int perfRangeSize,
			List<EvaluationCriteria> dimensions, List<Goal> goals) {

		this(defaultUtility, perfRangeSize);
		this.dimensions = dimensions;
		this.goals = goals;

		this.init();
	}

	/**
	 * Initialises the utility, setting all the scores
	 * to the norms' utility default value 
	 * 
	 * @see EvaluationCriteria
	 * @see Goal
	 */
	public void init() {
		for(EvaluationCriteria dim : dimensions) {
			this.initDimension(dim);
		}
	}
	
	/**
	 * Initialises the utility, setting all the scores
	 * to the norms' utility default value 
	 * 
	 * @see EvaluationCriteria
	 * @see Goal
	 */
	public void reset() {
		for(EvaluationCriteria dim : dimensions) {
			this.resetDimension(dim);
		}
	}

	/**
	 * 
	 * @param dim
	 */
	public void initDimension(EvaluationCriteria dim) {
		HashMap<Goal, Double> dimScores = new HashMap<Goal, Double>();
		HashMap<Goal, PerformanceRange> dimPerformances =
				new HashMap<Goal, PerformanceRange>();

		this.scores.put(dim, dimScores);
		this.perfRanges.put(dim, dimPerformances);

		for(Goal goal : goals) {
			PerformanceRange perfRange = new PerformanceRange(perfRangeSize);
			this.perfRanges.get(dim).put(goal, perfRange);
			this.setScore(dim, goal, defaultUtility);
		}
	}
	
	/**
	 * 
	 * @param dim
	 */
	public void resetDimension(EvaluationCriteria dim) {
		for(Goal goal : goals) {
			this.perfRanges.get(dim).get(goal).reset();
			this.setScore(dim, goal, defaultUtility);
		}
	}

	/**
	 * 
	 */
	public void setDefaultScore() {
		for(EvaluationCriteria dim : dimensions) {
			for(Goal goal : goals) {
				this.setScore(dim, goal, defaultUtility);	
			}
		}
	}
	
	/**
	 * Returns the score of the utility for a given dimension/goal
	 * 
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * @return the score for the given dimension/goal
	 * @see EvaluationCriteria
	 * @see Goal
	 */
	public double getScore(EvaluationCriteria dim, Goal goal) {
		return this.scores.get(dim).get(goal);
	}

	/**
	 * Sets the score of the utility for a given dimension/goal
	 * 
	 * @param dim the norm evaluation dimension 
	 * @param goal the system goal
	 * @param score the new score
	 * @see EvaluationCriteria
	 * @see Goal
	 */
	public void setScore(EvaluationCriteria dim, Goal goal, double score) {
		this.scores.get(dim).put(goal, score);
		this.perfRanges.get(dim).get(goal).addValue(score);
	}

	/**
	 * Returns the average score of the utility for a
	 * given dimension/goal for a period of time
	 * 
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * @return the average score for the given dimension/goal for
	 * 					the period of time given by the method
	 * 					{@code NormSynthesisSettings.getNormsPerformanceRangesSize()}
	 */
	public double getScoreAverage(EvaluationCriteria dim, Goal goal) {
		return this.perfRanges.get(dim).get(goal).getCurrentAverage();
	}

	/**
	 * Returns the score window used to compute the performance
	 * range of the norm during a period of time, in terms of a 
	 * dimension/goal
	 * 
	 * @param dim the norm evaluation dimension
	 * @param goal the system goal
	 * 
	 * @return the score window 
	 */
	public PerformanceRange getPerformanceRange(EvaluationCriteria dim, Goal goal) {
		return this.perfRanges.get(dim).get(goal);
	}
}
