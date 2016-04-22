/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.refinement.synergies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormCompliance;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.refinement.NormAttribute;

/**
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class NormSynergiesDetector {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	/* The settings of the norm synthesis machine */
	private NormSynthesisSettings nsmSettings; 

	/* A normative network */
	private NormativeNetwork normativeNetwork;

	/* A network of norm groups */
	private NormGroupNetwork normGroupNetwork;

	/* Norm synthesis metrics */
	private NormSynthesisMetrics nsMetrics;

	/* To keep track of substitutable and complementary norms */
	private Map<Norm, List<Norm>> substitutableNorms;
	private Map<Norm, List<Norm>> complementaryNorms;

	/* To compare numerical series */
	private NumericalSeriesComparator nSeriesComparator;


	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * 
	 * @param normativeNetwork
	 */
	public NormSynergiesDetector(NormSynthesisSettings nsmSettings, 
			NormativeNetwork normativeNetwork, NormGroupNetwork normGroupNetwork,
			NormSynthesisMetrics nsMetrics) {

		this.nsmSettings = nsmSettings;
		this.normativeNetwork = normativeNetwork;
		this.normGroupNetwork = normGroupNetwork;
		this.nsMetrics = nsMetrics;

		this.substitutableNorms = new HashMap<Norm, List<Norm>>();
		this.complementaryNorms = new HashMap<Norm, List<Norm>>();
		
		this.nSeriesComparator = 
				new NumericalSeriesComparator(normGroupNetwork, nsmSettings);
	}

	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	public List<NormAttribute> checkSubstitutability(Norm nA, Norm nB, 
			NormGroupCombination nABComb) {

		List<NormAttribute> classifications = new ArrayList<NormAttribute>();
		boolean allcombinationsExist = nABComb.containsAllCombinations();
		boolean isLeaf = this.normativeNetwork.isLeaf(nA);
		Goal goal = nA.getGoal();

		/* Check that all group combinations exist to check substitutability */
		if(allcombinationsExist && isLeaf) {

			/* Get performance series for different fulfilment 
			 * and infringement combinations */
			NormGroup groupFF = nABComb.get(NormCompliance.Fulfilment,
					NormCompliance.Fulfilment);
			NormGroup groupFI = nABComb.get(NormCompliance.Fulfilment,
					NormCompliance.Infringement);
			NormGroup groupIF = nABComb.get(NormCompliance.Infringement,
					NormCompliance.Fulfilment);

			try {
				
				/* Check if groups FI and IF are similar to FF, and that
				 * group FF does not under-perform */
				boolean groupFISimilarToFF = 
						this.nSeriesComparator.areSimilar(groupFI, groupFF, goal);
				
				boolean groupIFSimilarToFF = 
						this.nSeriesComparator.areSimilar(groupIF, groupFF, goal);
				
				boolean groupFFDoesNotUnderPerform =
						!this.shouldBeDeactivated(groupFF, EvaluationCriteria.Effectiveness);
	
				/* Conditions to be substitutable*/
				if(groupFFDoesNotUnderPerform && groupFISimilarToFF && groupIFSimilarToFF) {
	
					/* Add substitutability attributes */
					classifications.add(NormAttribute.Substitutable);
					this.addSubstitutableNorms(nA, nB);
					
					System.out.println("Substitutability detected " + nA);
					System.out.println("Substitutability detected " + nB);
					System.out.println("------------------------------------------------");
	
					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
					this.nsMetrics.incNumNodesVisited();
				}
			} 
			catch(Exception e) {
				// Do nothing
			}
		}
		return classifications;
	}
	
	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	public List<NormAttribute> checkComplementarity(Norm nA, Norm nB, 
			NormGroupCombination nABComb) {

		List<NormAttribute> classifications = new ArrayList<NormAttribute>();
		boolean enoughInformation = nABComb.containsAllCombinations();
		boolean isLeaf = this.normativeNetwork.isLeaf(nA);
		Goal goal = nA.getGoal();

		/* Check that there is information enough to check complementarity */
		if(enoughInformation && isLeaf) {

			/* Get performance series for different fulfilment 
			 * and infringement combinations */
			NormGroup groupFF = nABComb.get(NormCompliance.Fulfilment,
					NormCompliance.Fulfilment);
			NormGroup groupFI = nABComb.get(NormCompliance.Fulfilment,
					NormCompliance.Infringement);
			NormGroup groupIF = nABComb.get(NormCompliance.Infringement,
					NormCompliance.Fulfilment);

			try {
				
				/* Check if group FF is above groups IF and FI */
				boolean groupFFGreaterThanFI =
						this.nSeriesComparator.isGreater(groupFF, groupFI, goal);
				
				boolean groupFFGreaterThanIF = 
						this.nSeriesComparator.isGreater(groupFF, groupIF, goal);
	
				/* Conditions to be substitutable*/
				if(groupFFGreaterThanFI && groupFFGreaterThanIF) {
					classifications.add(NormAttribute.Complementary);
					this.addComplementaryNorms(nA, nB);
	
					System.out.println("Complementarity detected " + nA);
					System.out.println("Complementarity detected " + nB);
					System.out.println("------------------------------------------------");
				}
			} 
			catch(Exception e) {
				// Do nothing
			}
		}
		return classifications;
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
	private boolean shouldBeDeactivated(NormGroup normGroup, EvaluationCriteria dim) {
		Utility utility = this.normGroupNetwork.getUtility(normGroup);
		Goal goal = normGroup.getGoal();
		
		double threshold = this.nsmSettings.getNormDeactivationThreshold(dim, goal);
		double avg = utility.getPerformanceRange(dim, goal).getCurrentAverage();

		int minNumEvalsToClassify = this.nsmSettings.getMinEvaluationsToClassifyNorms();
		int numValues = utility.getPerformanceRange(dim, goal).getNumPunctualValues();

		/* The norm under performs */
		if(numValues > minNumEvalsToClassify) {
			if(avg <= Math.max(0, threshold)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	private void addSubstitutableNorms(Norm nA, Norm nB) {
		if(!this.substitutableNorms.containsKey(nA)) {
			this.substitutableNorms.put(nA, new ArrayList<Norm>());
		}
		this.substitutableNorms.get(nA).add(nB);
	}
	
	/**
	 * 
	 * @param nA
	 * @param nB
	 */
	private void addComplementaryNorms(Norm nA, Norm nB) {
		if(!this.complementaryNorms.containsKey(nA)) {
			this.complementaryNorms.put(nA, new ArrayList<Norm>());
		}
		this.complementaryNorms.get(nA).add(nB);
	}
	
	/**
	 * @param norm
	 * @return
	 */
	public List<Norm> getSubstitutableNorms(Norm norm) {
		return this.substitutableNorms.get(norm);
	}
	
	/**
	 * @param norm
	 * @return
	 */
	public List<Norm> getComplementaryNorms(Norm norm) {
		return this.complementaryNorms.get(norm);
	}
}
