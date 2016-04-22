package user.strategies;

import java.util.List;

import es.csic.iiia.normlab.traffic.car.CarAction;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.SetOfPredicatesWithTerms;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.NormModality;
import es.csic.iiia.nsm.norm.NormativeSystem;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 * My second strategy
 */
public class UserStrategy2 implements NormSynthesisStrategy {

	/* The normative network, a data structure to keep track of norms */
	private NormativeNetwork normativeNetwork;
	
	/**
	 * 
	 */
	@Override
	public NormativeSystem execute() {
		return normativeNetwork.getNormativeSystem();
	}

	/**
	* 
	*/
	@Override
	public void setup(NormSynthesisMachine nsm) {
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.createNormativeSystem(); 	  
	}
  
	/**
	 * Creates a normative system to give way to the cars on the left
	 */
	private void createNormativeSystem() {
		
		/* Create norm preconditions */
		SetOfPredicatesWithTerms n1Precondition = new SetOfPredicatesWithTerms();
		n1Precondition.add("l", ">");
		n1Precondition.add("f", "*");
		n1Precondition.add("r", "*");
		
		/* Create norms */
		Norm n1 = new Norm(n1Precondition, NormModality.Prohibition, CarAction.Go);

		/* Add the norms to the normative network and activate them */
		this.normativeNetwork.add(n1);
		normativeNetwork.setState(n1, NetworkNodeState.Active);
	}

	/**
	 * 
	 */
	@Override
  public void addDefaultNormativeSystem(List<Norm> poolOfNorms) {}


}

