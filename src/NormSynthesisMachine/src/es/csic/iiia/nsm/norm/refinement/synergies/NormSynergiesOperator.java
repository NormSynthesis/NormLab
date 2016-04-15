/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
package es.csic.iiia.nsm.norm.refinement.synergies;


/**
 * @author Javi
 *
 */
public class NormSynergiesOperator {

//
//	/**
//	 * 
//	 * @param nA
//	 * @param nB
//	 */
//	protected Norm chooseNormToSubstitute(Norm nA, Norm nB) {
//
//		/* 1. Preserve the norm that is complementary with a third norm */
//		boolean nAIsComplementary = this.normativeNetwork.isComplementary(nA);
//		boolean nBIsComplementary = this.normativeNetwork.isComplementary(nB);
//
//		if(nAIsComplementary && !nBIsComplementary) {
//			return nB;
//		}
//		if(nBIsComplementary &&!nAIsComplementary) {
//			return nA;
//		}
//
//		/* 2. Preserve the norm that is substituting a third norm */
//		if(this.normativeNetwork.isSubstituter(nA) &&
//				!this.normativeNetwork.isSubstituter(nB)) {
//			return nB;
//		}
//		else if(this.normativeNetwork.isSubstituter(nB) &&
//				!this.normativeNetwork.isSubstituter(nA)) {
//			return nA;
//		}
//
//		/* 4. In case of a draw, promote the norm with the lowest
//		 * substitutability index (that with the lowest number of brother
//		 * norms that have been substituted */
//		double nASubsIndex = this.computeSubstitutabilityIndex(nA);
//		double nBSubsIndex = this.computeSubstitutabilityIndex(nB);
//
//
//		if(nASubsIndex != nBSubsIndex) {
//			System.out.println("Subindex " + nA + ": " + nASubsIndex);
//			System.out.println("Subindex " + nB + ": " + nBSubsIndex);
//			return (nASubsIndex > nBSubsIndex ? nA : nB);
//		}
//
//		/* 3. Compute the highest generalisation level in the generalisation tree
//		 * of each norm. Preserve the norm in the most generalised sub-tree */
//		double nAGenIndex = this.computeGeneralisationIndex(nA);
//		double nBGenIndex = this.computeGeneralisationIndex(nB);
//
//		if(nAGenIndex != nBGenIndex) {
//			System.out.println("GenIndex " + nA + ": " + nAGenIndex);
//			System.out.println("Genindex " + nB + ": " + nBGenIndex);
//			return (nAGenIndex > nBGenIndex ? nB : nA);
//		}
//
//		/* 5. Randomly choose one of the norms */
//		if(this.random.nextBoolean()) {
//			return nA;
//		}
//		return nB;
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	private double computeGeneralisationIndex(Norm norm) {
//		List<Norm> visited = new ArrayList<Norm>();
//		return this.computeGeneralisationDegree(norm, visited);
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	private double computeGeneralisationDegree(Norm norm, List<Norm> visited) {
//
//		/* Check that the norm is represented and not visited before */
//		if(!this.normativeNetwork.isRepresented(norm) || visited.contains(norm)) {
//			return 0; // TODO: Cambiar logica con estados active / represented
//		}
//		visited.add(norm);
//
//		List<Norm> parents = this.normativeNetwork.getParents(norm);
//		List<Norm> children = this.normativeNetwork.getChildren(norm);
//		int genLevel = this.normativeNetwork.getGeneralisationLevel(norm);
//		int numChildren = children.size();
//
//		double genDegree = numChildren * Math.pow(10, genLevel);
//
//		/* Explore in height (parents) */
//		for(Norm parent : parents) {
//			double parentGenDegree = this.computeGeneralisationDegree(parent, visited);
//			genDegree += parentGenDegree;
//		}
//
//		/* Update complexities metrics */
//		this.nsMetrics.incNumNodesVisited();
//
//		return genDegree;
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	private double computeSubstitutabilityIndex(Norm norm) {
//		List<Norm> visited = new ArrayList<Norm>();
//		return this.computeSubstitutabilityDegree(norm, visited, 0);
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	private double computeSubstitutabilityDegree(Norm norm,
//			List<Norm> visited,	int distance) {
//
//		/* Check that the norm is represented and not visited before */
//		if(!this.normativeNetwork.isRepresented(norm) || visited.contains(norm)) {
//			return 0;
//		}
//		visited.add(norm);
//		List<Norm> parents = this.normativeNetwork.getParents(norm);
//		List<Norm> brothers = this.normativeNetwork.getBrothers(norm);
//
//		double subsDegree = 0.0;
//		for(Norm brother : brothers) {
//			if(normativeNetwork.getState(brother) == NetworkNodeState.Substituted) {
//				subsDegree += Math.pow(10, distance * -1);
//			}
//
//			/* Update complexities metrics */
//			this.nsMetrics.incNumNodesVisited();
//		}
//
//		/* Explore in height (parents) */
//		for(Norm parent : parents) {
//			double parentSubsDegree = this.computeSubstitutabilityDegree(parent,
//					visited, distance+1);
//			subsDegree += parentSubsDegree;
//		}
//
//		/* Update complexities metrics */
//		this.nsMetrics.incNumNodesVisited();
//		return subsDegree;
//	}

}
