package es.csic.iiia.nsm.z_deprecated.synthesis.strategy.lion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroupCombination;
import es.csic.iiia.nsm.norm.generation.Conflict;
import es.csic.iiia.nsm.norm.generation.NormGenerationMachine;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.reasoning.NormReasoner;
import es.csic.iiia.nsm.norm.refinement.NormAttribute;
import es.csic.iiia.nsm.strategy.generation.cbr.CBRNormGenerationMachine;

/**
 * The operators that the SIMON strategy uses to perform norm synthesis
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class LIONOperators {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	protected NormReasoner normReasoner;					// norm reasoner
	protected DomainFunctions dmFunctions;				// domain functions
	protected PredicatesDomains predDomains;			// predicates and their domains
	protected LIONStrategy strategy;							// the norm synthesis strategy
	protected NormativeNetwork normativeNetwork;	// the normative network
	protected NormGroupNetwork normGroupNetwork;	// the norm groups network
	protected NormGenerationMachine genMachine;		// the norm generation machine
	protected NormSynthesisSettings nsmSettings;	// norm synthesis settings
	protected NormSynthesisMetrics nsMetrics;			// norm synthesis metrics
	protected boolean isNormGenReactiveToConflicts;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param 	strategy the SIMON norm synthesis strategy
	 * @param 	normReasoner the norm reasoner, to reason about norm
	 * 					applicability	and compliance
	 * @param 	nsm the norm synthesis machine
	 */
	public LIONOperators(LIONStrategy strategy, NormReasoner normReasoner, 
			NormSynthesisMachine nsm) {

		this.strategy = strategy;
		this.normReasoner = normReasoner;
		this.dmFunctions = nsm.getDomainFunctions();
		this.predDomains = nsm.getPredicatesDomains();
		this.normativeNetwork = nsm.getNormativeNetwork();
		this.normGroupNetwork = nsm.getNormGroupNetwork();
		this.nsmSettings = nsm.getNormSynthesisSettings();
		this.nsMetrics = nsm.getNormSynthesisMetrics();
		this.genMachine = new CBRNormGenerationMachine(this.normativeNetwork,
				normReasoner, strategy, nsm.getRandom(), this.nsMetrics);
		
		this.isNormGenReactiveToConflicts = 
				nsmSettings.isNormGenerationReactiveToConflicts();
	}

	/**
	 * Creates norms to regulate a given {@code conflict} that the norm
	 * synthesis machine has perceived in the scenario. The conflict
	 * is detected in terms of a system {@code goal}
	 * 
	 * @param conflict the perceived conflict
	 * @param goal the goal with respect to which the conflict has arisen
	 * @see Conflict
	 * @see Goal
	 */
	public List<Norm> create(Conflict conflict, Goal goal) {
		List<Norm> normsToAdd = new ArrayList<Norm>();
		List<Norm> normsToActivate = new ArrayList<Norm>();
		List<Norm> norms;

		/* Perform norm generation */
		norms = genMachine.createNormsFromConflict(conflict, dmFunctions, goal);

		for(Norm norm : norms) {

			/* If the normative network does not contain the norm, (i.e., the norm
			 * does not exist), then add it to the normative network */
			if(!normativeNetwork.contains(norm)) {
				normsToAdd.add(norm);
			}

			/* ONLY IF norm generation is reactive to conflicts:
			 * If the normative network contains the norm, but it is not represented
			 * (that is, the norm and all its ancestors are inactive),
			 * then activate the norm */
			else if(isNormGenReactiveToConflicts && 
					!normativeNetwork.isRepresented(norm)) {

				boolean isIneffective = this.normativeNetwork.getAttributes(norm).
						contains(NormAttribute.Ineffective);
				boolean isUnnecessary = this.normativeNetwork.getAttributes(norm).
						contains(NormAttribute.Unnecessary);

				if(!isIneffective && !isUnnecessary) {
					normsToActivate.add(norm);
				}
			}
		}

		/* Add norms to add */
		for(Norm norm : normsToAdd)	{
			this.add(norm);
			this.normativeNetwork.setState(norm, NetworkNodeState.Hibernated);
		}

		/* Activate norms */
		for(Norm norm : normsToActivate)	{
			this.activate(norm);
		}
		return normsToActivate;

	}

	/**
	 * Adds a norm to the normative network (if the normative network
	 * does not contain it yet) and activates it by setting its state
	 * to <tt>active</tt> in the normative network
	 * 
	 * @param norm the norm to add
	 */
	public void add(Norm norm) {
		if(!normativeNetwork.contains(norm)) {

			/* Add the norm to the network in case it does not exist on it */
			this.normativeNetwork.add(norm);

			/* The new norm has been added because it is
			 * regarded as effective and necessary */
			//			this.normativeNetwork.addAttribute(norm, NormAttribute.EFFECTIVE);
			//			this.normativeNetwork.addAttribute(norm, NormAttribute.NECESSARY);

			/* Activate the norm and link it to other norms in the network */
			if(this.isNormGenReactiveToConflicts) {
				this.activate(norm);
			}
			this.link(norm);
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesSynthesised();
			this.nsMetrics.incNumNodesInMemory();
		}
	}

	/**
	 * Activates a given {@code norm} in the normative network, resets
	 * its utility and adds the norm to the norm reasoner. Thus, the
	 * strategy will take the norm into account to compute norm
	 * applicability and compliance
	 * 
	 * @param norm the norm to activate
	 */	
	public void activate(Norm norm) {
		if(!normativeNetwork.isRepresented(norm)) {
			normativeNetwork.setState(norm, NetworkNodeState.Active);

			if(this.normGroupNetwork.hasNormGroupCombinations(norm)) {
				Map<Norm,NormGroupCombination> nGrCombs = 
						this.normGroupNetwork.getNormGroupCombinations(norm);

				for(Norm n : nGrCombs.keySet()) {

					/* Activate all norm groups in which the norm is */
					this.normGroupNetwork.setState(nGrCombs.get(n), NetworkNodeState.Active);

					/* Reset all norm groups in which the norm is */
					for(NormGroup nGroup : nGrCombs.get(n).getAllNormGroups()) {
						Utility u = this.normGroupNetwork.getUtility(nGroup);
						u.reset();
						
						/* Update complexities metrics */
						this.nsMetrics.incNumNodesVisited();
					}
					
					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
				}	
			}
		}
	}

	/**
	 * Deactivates a given {@code norm} in the normative network and removes
	 * it from the norm reasoner. Thus, the strategy will not take
	 * the norm into account to compute norm applicability and compliance
	 * 
	 * @param norm the norm to deactivate
	 */
	public void deactivate(Norm norm, NetworkNodeState newState) {
		this.normativeNetwork.setState(norm, newState);

		if(newState != NetworkNodeState.Represented) {
//			/* Reset the norm's effectiveness and necessity (and also its classifications 
//			 * in terms of effectiveness and necessity) */
//			this.normativeNetwork.getUtility(norm).reset();
//			this.normativeNetwork.removeAttribute(norm, NormAttribute.EFFECTIVE);
//			this.normativeNetwork.removeAttribute(norm, NormAttribute.INEFFECTIVE);
//			this.normativeNetwork.removeAttribute(norm, NormAttribute.NECESSARY);
//			this.normativeNetwork.removeAttribute(norm, NormAttribute.UNNECESSARY);

			if(this.normGroupNetwork.hasNormGroupCombinations(norm)) {
				Map<Norm,NormGroupCombination> nGrCombs = 
						this.normGroupNetwork.getNormGroupCombinations(norm);

				for(Norm n : nGrCombs.keySet()) {
					this.normGroupNetwork.setState(nGrCombs.get(n), NetworkNodeState.Inactive);	
					
					/* Update complexities metrics */
					this.nsMetrics.incNumNodesVisited();
				}	
			}
		}
	}

	/**
	 * Generalises a {@code child} norm to a {@code parent} norm
	 * 
	 * @param child the child norm
	 * @param parent the parent norm
	 */
	public void generalise(Norm child, Norm parent) {		
		this.normativeNetwork.addGeneralisation(child, parent);

		/* Deactivate the child norm if it is represented by
		 * an ancestor (the parent norm, likely) */
		for(Norm p : this.normativeNetwork.getParents(child)) {
			if(this.normativeNetwork.isRepresented(p)) {
				this.deactivate(child, NetworkNodeState.Represented); 
				break;
			}
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}

	/**
	 * Specialises a norm in the normative network
	 * 
	 * @param norm the norm to specialise
	 * @param children the children into which to specialise the norm
	 */
	public void specialise(Norm norm, NetworkNodeState specState, List<Norm> children) {

		/* Deactivation of a general norm */
		if(children.size() > 0) {
			this.deactivate(norm, NetworkNodeState.Specialised);	
		}
		else {
			this.deactivate(norm, specState); // TODO: Set the reason to specialise in the leaf
		}

		/* Activate child norms that are not represented by an ancestor */
		for(Norm child : children) {
			if(!normativeNetwork.isRepresented(child)) {
				this.activate(child);
			}
			
			/* Update complexities metrics */
			this.nsMetrics.incNumNodesVisited();
		}
	}

	/**
	 * 
	 * @param norm
	 */
	public void link(Norm norm) {
		List<Norm> topBoundary =
				(List<Norm>)this.normativeNetwork.getTopBoundary();
		List<Norm> visitedNorms = new ArrayList<Norm>();

		for(Norm normB : topBoundary) {
			if(!norm.equals(normB)) {
				this.searchRelationships(norm, normB, visitedNorms);
			}
		}
	}

	/**
	 * 
	 * @param normA
	 * @param normB
	 * @param visitedNorms
	 */
	private void searchRelationships(Norm normA, Norm normB, List<Norm> visitedNorms) {
		List<Norm> normAChildren = this.normativeNetwork.getChildren(normA);
		List<Norm> normBChildren = this.normativeNetwork.getChildren(normB);
		boolean linked = false;

		List<Norm> normBSatisfiedChildren = 
				this.normReasoner.getSatisfiedNorms(normA, normBChildren);
		List<Norm> normBChildrenSatisfyingA = 
				this.normReasoner.getNormsSatisfying(normBChildren, normA);
		List<Norm> normBChildrenNotSatisfyingA = 
				this.normReasoner.getNormsNotSatisfying(normBChildren, normA);

		/* Para evitar generalizaciones a una misma norma */
		if(normA.equals(normB) /* || visitedNorms.contains(normB)*/) {
			return;
		}

		/* Comprobacion de paternidad. Compruebo si A puede ser padre de B.
		 * Compruebo si B satisface a A. Si lo hace, generalizo de 
		 * B a A siempre que A no sea ya un ancestor */
		if(normReasoner.satisfies(normB, normA))	{
			if(!this.normativeNetwork.isAncestor(normA, normB)) {
				this.generalise(normB, normA);
				linked = true;
			}

			/* Nos aseguramos de que B no tenga ningun hijo que sea tambien hijo de A */
			for(Norm normBChild : normBChildren) {
				if(normAChildren.contains(normBChild)) {
					this.normativeNetwork.removeGeneralisation(normBChild, normA);
				}
				
				/* Update complexities metrics */
				this.nsMetrics.incNumNodesVisited();
			}
		} 

		/* Comprobacion de descendencia. Compruebo si A es hija de B.
		 * Primero compruebo si A satisface a B */
		else if(normReasoner.satisfies(normA, normB)) {

			/* Si A satisface a B y no satisface a ninguno de sus hijos, 
			 * entonces A es hija directa de B */
			if(normBSatisfiedChildren.isEmpty()) {

				/* A es hija de B. Generalizamos, siempre que no sea ancestor ya */ 
				if(!this.normativeNetwork.isAncestor(normB, normA)) {
					this.generalise(normA, normB);
					linked = true;
				}

				/* Ahora comprobamos que A no esté en medio de B y alguna de sus hijas.
				 * Para ello, comprobamos si hay alguna hija de B que satisfaga a A.
				 * Para cada una de ellas que satisfaga A, quitamos la generalizacion
				 * de la hija hacia B y generalizamos a A */
				if(this.normativeNetwork.isAncestor(normB, normA)) {
					for(Norm normBChild : normBChildrenSatisfyingA) {
						this.normativeNetwork.removeGeneralisation(normBChild, normB);
						this.generalise(normBChild, normA);
					}
				}
			}
		}

		/* Si no se ha conseguido enlazar con nadie, seguimos buscando */
		if(!linked) {
			for(Norm normBChild : normBChildren) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		} 

		/* Si se ha enlazado continuamos igual, por si se puede seguir enlazando
		 * por abajo. La unica cuestion es que solo seguimos bajando por
		 * aquellos hijos que no satisfacen A (para asegurarnos de que si A
		 * ya se ha puesto como padre de B,  no se ponga tambien como padre 
		 * de sus hijas */
		else if(!normBChildrenNotSatisfyingA.isEmpty()) {		
			for(Norm normBChild : normBChildrenNotSatisfyingA) {
				this.searchRelationships(normA, normBChild, visitedNorms);
			}
		}
		
		/* Update complexities metrics */
		this.nsMetrics.incNumNodesVisited();
	}
}