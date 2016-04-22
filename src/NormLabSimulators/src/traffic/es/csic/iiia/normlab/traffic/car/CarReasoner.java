package es.csic.iiia.normlab.traffic.car;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import es.csic.iiia.normlab.traffic.TrafficSimulator;
import es.csic.iiia.normlab.traffic.agent.Car;
import es.csic.iiia.normlab.traffic.car.context.CarContext;
import es.csic.iiia.normlab.traffic.factory.TrafficFactFactory;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.reasoning.NormEngine;

/**
 * Reasoner for the agent. Extends NormEngine to be able to reason 
 * about facts of the system and norms' applicability
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class CarReasoner extends NormEngine {

	//------------------------------------------------------------
	// Attributes																															
	//------------------------------------------------------------

	private TrafficFactFactory factFactory;
	private CarReasonerState state;			// the state of the agent's reasoner
	private Norm normToComplyWith;			// the last norm the agent complied with
	private Norm normToInfringe;				// the last infringed norm
	private List<Norm> applicableNorms;	// norms that are applicable to the facts
	private boolean casualStop;					// is a casual stop?
	private double normInfringementRate;

	//------------------------------------------------------------
	// Constructors
	//------------------------------------------------------------

	/**
	 * Constructor
	 */
	public CarReasoner(PredicatesDomains predDomains,
			TrafficFactFactory factFactory, double normInfringementRate) {
		
		super(predDomains);

		this.factFactory = factFactory;
		this.state = CarReasonerState.NoNormActivated;
		this.normInfringementRate = normInfringementRate;
		this.casualStop = false;
	}

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Does reasoning to activate rules in base of the facts in the knowledge base 
	 */
	public CarAction decideAction(Car car, CarContext context) {
		Random rand = TrafficSimulator.getRandom();
		boolean violate;
		int violateProb;
		
		this.state = CarReasonerState.NoNormActivated;
		this.normToInfringe = null;
		this.normToComplyWith = null;
		
		this.reset();

		/* Add world facts */
		
		/* If the context is null, then the agent is
		 * about getting out of the scenario -> Perform action 'Go' */
		if(context == null) {
			return CarAction.Go;
		}

		SetOfPredicatesWithTerms predicates =  
				factFactory.generatePredicates(context);
		
		this.addFacts(predicates);

		/* Collided cars remain stopped */
		if(car.isCollided()) {
			return CarAction.Stop;
		}

		/* Reason about the norms that apply to the given system facts */
		this.applicableNorms = this.reason();

		/* Remove random component of JESS */
		Collections.sort(applicableNorms);

		// Obtain next supposed action to do according to the norm specification
		// TODO: Maybe here in the future several norms are fired for a certain situation
		// and we will have to choose what norm to apply (in base of some criteria).
		for(Norm n : applicableNorms) {
			violate = false;
			
//			if(n.getId()==19)
//				System.out.println();

			/* Decide if applying the norm or not */ 
			violateProb = (int)(this.normInfringementRate * 100.0f);
			int num = rand.nextInt(100)+1;
			violate = (num <= violateProb) ? true : false;

			/* Randomly choose if applying the norm or not. Case apply the norm */
			if(violate)	{
				this.normToInfringe = n;
				state = CarReasonerState.NormWillBeInfringed;
				return CarAction.Go;
			}
			else {
				this.normToComplyWith = n;
				state = CarReasonerState.NormWillBeFullfilled;

				CarAction action = (CarAction)n.getAction();
				return action.getOpposite();
			}
		}
		// Let the facts base empty and return the action chosen by the car
		applicableNorms.clear();

		return CarAction.Go;
	}

	/**
	 * Returns true if the last applicable norm was finally applied. False else
	 * 
	 * @return
	 */
	public CarReasonerState getState() {
		return state;
	}

	/**
	 * Returns the last norm that has been applied by the car
	 * 
	 * @return
	 */
	public Norm getNormToApply() {
		return this.normToComplyWith;
	}

	/**
	 * Returns the last norm that has been violated by the car
	 * @return
	 */
	public Norm getNormToViolate() {
		return this.normToInfringe;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isCasualStop() {
		return this.casualStop;
	}

	/**
	 * Returns the norms that apply to the facts that
	 * have been added to the norm engine
	 * 
	 * @return the list of applicable norms
	 */
	public List<Norm> getApplicableNorms() {
		return this.applicableNorms;
	}
}
