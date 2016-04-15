package es.csic.iiia.normlab.synthesis.config;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import es.csic.iiia.nsm.NormSynthesisMachine.NormGeneralisationMode;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator.Mechanism;
import es.csic.iiia.nsm.norm.generation.NormGenerator;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * Basic configuration for the traffic simulator and the norm synthesis process
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public abstract class DefaultNormSynthesisSettings implements NormSynthesisSettings {

	/* Norm synthesis strategy to use */
	private NormSynthesisStrategy.Option synthesisStrategy;
	
	/* Traffic norm synthesis agent */
	private int nsExample;

	/* Perform norm generalisations to pursue compactness? */
	private boolean pursueCompactness;
	
	/* Exploit norm synergies to pursue liberality? */
	private boolean pursueLiberality;
	
	/* Norm evaluation mechanism */
	private NormEvaluator.Mechanism normEvaluationMechanism;
	
	/* Approach to generalise norms (conservative/optimistic) */
	private NormGeneraliser.Approach normGeneralisationApproach;
	
	/* Norm generalisation mode */
	private NormGeneralisationMode optimisticNormGenMode;

	/* Norm generalisation step */
	private int optimisticNormGenStep;

	/* The effectiveness generalisation threshold */
	private double normEffGenThreshold;

	/* The necessity generalisation threshold */
	private double normNecGenThreshold;

	/* The effectiveness deactivation threshold*/
	private double normEffDeactThreshold;

	/* The necessity deactivation threshold */
	private double normNecDeactThreshold;

	/* Length of historic sliding window to compute norms' 
	 * cumulative effectiveness and necessities */
	private int normsPerfRangeSize;

	/* Norms' default utility when created */
	private double normsDefaultUtility;

	/* Learning rate of the method */
	private double normEvaluationLearningRate;
	
	/* Number of ticks of stability to converge */
	private long numTicksToConverge;

	/* */
	private double normEffDeactThresholdEpsilon;
	
	/* */
	private double normNecDeactThresholdEpsilon;

	/* */
	private int normsMinEvalsToDecide;

	/* */
	private int normGroupsMinEvalsToDecide;

	/* */
	private String userStrategyName;

	/* */
	private NormGenerator.Approach normGenerationApproach;

	//-----------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------

	/**
	 * 
	 */
	public DefaultNormSynthesisSettings() {
		init();	
	}
	
	/**
	 * Loads the NSM settings
	 */
	public void init() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		/* General settings  */
		
		this.nsExample = (Integer)p.getValue("normSynthesisExample");
		int strategy = (Integer)p.getValue("normSynthesisStrategy");
		
		switch(strategy) {
		case 	0: this.synthesisStrategy = NormSynthesisStrategy.Option.Example;		break;
		case 	1:	this.synthesisStrategy = NormSynthesisStrategy.Option.Custom;		break;
		case 	2:	this.synthesisStrategy = NormSynthesisStrategy.Option.Generic;	break;
		case 	3:	this.synthesisStrategy = NormSynthesisStrategy.Option.BASE;			break;
		case 	4:	this.synthesisStrategy = NormSynthesisStrategy.Option.IRON;			break;
		case 	5:	this.synthesisStrategy = NormSynthesisStrategy.Option.SIMON;		break;
		case 	6:	this.synthesisStrategy = NormSynthesisStrategy.Option.LION;			break;
		case 	7:	this.synthesisStrategy = NormSynthesisStrategy.Option.DESMON;		break;
		}
		
		this.pursueCompactness = (Boolean)p.getValue("pursueCompactness");
		this.pursueLiberality = (Boolean)p.getValue("pursueLiberality");
		this.numTicksToConverge = (Long)p.getValue("numTicksToConverge");
		
		/* Norm evaluation settings */
		
		int nem = (Integer)p.getValue("normEvaluationMechanism");
		if(nem == 0) {
			this.normEvaluationMechanism = NormEvaluator.Mechanism.BollingerBands;	
		}
		else if(nem == 1) {
			this.normEvaluationMechanism = NormEvaluator.Mechanism.MovingAverage;
		}
		
		this.normEvaluationLearningRate = (Double)p.getValue("normEvaluationLearningRate");
		this.normsPerfRangeSize = (Integer)p.getValue("normsPerfRangeSize");
		this.normsDefaultUtility = (Double)p.getValue("normsDefaultUtility");
		
		/* Norm generalisation settings */
		
		int nga = (Integer)p.getValue("normGeneralisationApproach");
		if(nga == 0) {
			this.normGeneralisationApproach = NormGeneraliser.Approach.Conservative;
		}
		else if(nga == 1) {
			this.normGeneralisationApproach = NormGeneraliser.Approach.Optimistic;
		}
		
		int ngm = (Integer)p.getValue("optimisticNormGeneralisationMode");
		if(ngm == 0) {
			this.optimisticNormGenMode = NormGeneralisationMode.Shallow;
		}
		else if(ngm == 1) {
			this.optimisticNormGenMode = NormGeneralisationMode.Deep;
		}
		else {
			this.optimisticNormGenMode = NormGeneralisationMode.None;
		}
		
		this.optimisticNormGenStep = (Integer)p.getValue("optimisticNormGeneralisationStep");
		this.normEffGenThreshold = (Double)p.getValue("normEffGenThreshold");
		this.normNecGenThreshold = (Double)p.getValue("normNecGenThreshold");
				
		/* Norm activation/deactivation settings */
		nga = (Integer)p.getValue("normGenerationApproach");
		if(nga == 0) {
			this.normGenerationApproach = NormGenerator.Approach.Reactive;
		}
		else if(nga == 1) {
			this.normGenerationApproach = NormGenerator.Approach.Deliberative;
		}

		this.normEffDeactThreshold = (Double)p.getValue("normEffDeactThreshold");
		this.normNecDeactThreshold = (Double)p.getValue("normNecDeactThreshold");
		this.normEffDeactThresholdEpsilon = (Double)p.getValue("normEffDeactThresholdEpsilon");
		this.normNecDeactThresholdEpsilon = (Double)p.getValue("normNecDeactThresholdEpsilon");
		this.normsMinEvalsToDecide = (Integer)p.getValue("normsMinEvaluationsToDecide");
		this.normGroupsMinEvalsToDecide = (Integer)p.getValue("normGroupsMinEvaluationsToDecide");
		
		/* For LION on, set default utility in a different manner... */
		switch(this.synthesisStrategy) {
		case LION:
		case DESMON:
			this.normsDefaultUtility = 0f;
			break;
		default:
		}
		
		/* Get user strategy name */
		this.userStrategyName = (String)p.getValue("userStrategyName");
	}

	/**
	 * 
	 */
	@Override
	public double getNormsDefaultUtility() {
		return this.normsDefaultUtility;
	}

	/**
	 * 
	 */
	@Override
	public double getNormEvaluationLearningRate() {
		return normEvaluationLearningRate;
	}

	/**
	 * 
	 */
	@Override
	public int getNormsPerformanceRangesSize() {
		return normsPerfRangeSize;
	}

	/**
	 * 
	 */
	@Override
	public long getNumTicksOfStabilityForConvergence() {
		return numTicksToConverge;
	}

	/**
	 * 
	 */
	@Override
	public double getNormGeneralisationBoundary(EvaluationCriteria dim, Goal goal) {
		if(dim == EvaluationCriteria.Effectiveness) {
			return this.normEffGenThreshold;
		}
		else if(dim == EvaluationCriteria.Necessity) {
			return this.normNecGenThreshold;
		}
		return 0f;
	}

	/**
	 * 
	 */
	@Override
	public double getNormDeactivationBoundary(EvaluationCriteria dim, Goal goal) {
		if(dim == EvaluationCriteria.Effectiveness) {
			return this.normEffDeactThreshold;
		}
		else if(dim == EvaluationCriteria.Necessity) {
			return this.normNecDeactThreshold;
		}
		return 0;
	}

	/**
	 * 
	 */
	public double getNormDeactivationBoundaryEpsilon(EvaluationCriteria dim, Goal goal) {
		if(dim == EvaluationCriteria.Effectiveness) {
			return this.normEffDeactThresholdEpsilon;
		}
		else if(dim == EvaluationCriteria.Necessity) {
			return this.normNecDeactThresholdEpsilon;
		}
		return 0;
	}

	/**
	 * 
	 */
	@Override
	public NormGeneralisationMode getOptimisticNormGeneralisationMode() {
		return this.optimisticNormGenMode;
	}

	/**
	 * 
	 */
	@Override
	public int getOptimisticNormGeneralisationStep() {
		return this.optimisticNormGenStep;
	}

	/**
	 * 
	 */
	@Override
	public int getMinEvaluationsToClassifyNorms() {
		return this.normsMinEvalsToDecide;
	}

	/**
	 * 
	 */
	@Override
	public int getMinEvaluationsToClassifyNormGroups() {
		return this.normGroupsMinEvalsToDecide;
	}

	/**
	 * 
	 */
	@Override
	public NormSynthesisStrategy.Option getNormSynthesisStrategy() {
		return this.synthesisStrategy;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNormSynthesisExample() {
		return this.nsExample;
	}

	/**
	 * @return the synthesisStrategy
	 */
	public NormSynthesisStrategy.Option getSynthesisStrategy() {
		return synthesisStrategy;
	}

	/**
	 * @return the nsExample
	 */
	public int getNsExample() {
		return nsExample;
	}

	/**
	 * @return the pursueCompactness
	 */
	public boolean mustPursueCompactness() {
		return pursueCompactness;
	}

	/**
	 * @return the pursueLiberality
	 */
	public boolean mustPursueLiberality() {
		return pursueLiberality;
	}

	/**
	 * @return the normEvaluationMechanism
	 */
	public NormEvaluator.Mechanism getNormEvaluationMechanism() {
		return normEvaluationMechanism;
	}

	/**
	 * @return the normGenApproach
	 */
	public NormGeneraliser.Approach getNormGeneralisationApproach() {
		return normGeneralisationApproach;
	}

	/**
	 * @return the optimisticNormGenMode
	 */
	public NormGeneralisationMode getOptimisticNormGenMode() {
		return optimisticNormGenMode;
	}

	/**
	 * 
	 */
	@Override
  public boolean pursueCompactness() {
	  return this.pursueCompactness;
  }

	/**
	 * 
	 */
	@Override
  public boolean pursueLiberality() {
	  return this.pursueLiberality;
  }

	@Override
  public void setPursueCompactness(boolean pursueCompactness) {
	  this.pursueCompactness = pursueCompactness;
  }

	@Override
  public void setPursueLiberality(boolean pursueLiberality) {
		this.pursueLiberality = pursueLiberality;
  }

	@Override
  public void setNormEvaluationMechanism(Mechanism mechanism) {
	  this.normEvaluationMechanism = mechanism;
  }

	@Override
  public void setNormsDefaultUtility(double defaultUtility) {
	  this.normsDefaultUtility = defaultUtility;
  }

	@Override
  public void setNormGeneralisationApproach(
      NormGeneraliser.Approach genApproach) {
		
	  this.normGeneralisationApproach = genApproach;	  
  }

	@Override
  public void setMinEvaluationsToClassifyNorms(int numEvals) {
	  this.normsMinEvalsToDecide = numEvals;
  }

	@Override
  public String getUserStrategy() {
		return this.userStrategyName;
  }

	@Override
  public NormGenerator.Approach getNormGenerationApproach() {
	  return this.normGenerationApproach;
  }
	
	@Override
	public void setNormGenerationApproach(NormGenerator.Approach approach) {
		this.normGenerationApproach = approach;
	}
}
