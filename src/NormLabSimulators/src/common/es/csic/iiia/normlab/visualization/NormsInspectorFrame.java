/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.visualization;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.config.Goal;
import es.csic.iiia.nsm.config.NormSynthesisSettings;
import es.csic.iiia.nsm.metrics.NormSynthesisMetrics;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.NormEvaluator;
import es.csic.iiia.nsm.norm.evaluation.Utility;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.generation.NormGenerator;
import es.csic.iiia.nsm.norm.network.NetworkEdgeType;
import es.csic.iiia.nsm.norm.network.NetworkNodeState;
import es.csic.iiia.nsm.norm.network.NormativeNetwork;
import es.csic.iiia.nsm.norm.network.group.NormGroupNetwork;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser;
import es.csic.iiia.nsm.norm.refinement.generalisation.NormGeneraliser.Approach;
import es.csic.iiia.nsm.strategy.NormSynthesisStrategy;

/**
 *
 * @author Javi
 */
public class NormsInspectorFrame extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3551842164900242573L;

	private static NormLabInspector nInspector;
	private static NormLabConsole console;

	private NormSynthesisMachine nsm;
	private NormativeNetwork normativeNetwork;
	private NormGroupNetwork normGroupNetwork;
	private NormSynthesisSettings nsmSettings;
	private NormSynthesisMetrics nsMetrics;

	private Norm selectedNorm;
	private NormGroup selectedNormGroup;

	private String strategyName;

	/**
	 * Creates new form NormsInspectorFrame
	 */
	public NormsInspectorFrame(NormLabInspector nSInspector, 
			NormLabConsole nConsole) {

		initComponents();

		nInspector = nSInspector;
		console = nConsole;

		this.nsm = nInspector.getNormSynthesisMachine();
		this.normativeNetwork = this.nsm.getNormativeNetwork();
		this.normGroupNetwork = this.nsm.getNormGroupNetwork();
		this.nsmSettings = this.nsm.getNormSynthesisSettings();
		this.nsMetrics = this.nsm.getNormSynthesisMetrics();
		this.selectedNorm = null;
		this.selectedNormGroup = null;
		this.strategyName = nsm.getStrategy().getClass().getSimpleName();

		this.displaySettings();
		this.displayMetrics();

		this.updateTreeNS();
		this.updateTreeUnusedNorms();
		this.updateTreeActiveNormGroups();
		this.updateTreeInactiveNormGroups();

		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	/**
	 * 
	 */
	private void displaySettings() {
		NormSynthesisStrategy.Option strategy = 
				this.nsmSettings.getNormSynthesisStrategy();
		NormGenerator.Approach generationApproach = 
				this.nsmSettings.getNormGenerationApproach();
		NormEvaluator.Mechanism evaluationMech = 
				this.nsmSettings.getNormEvaluationMechanism();
		NormGeneraliser.Approach generalisationApproach = 
				this.nsmSettings.getNormGeneralisationApproach();

		/* Set strategy name */
		if(strategy == NormSynthesisStrategy.Option.User) {
			this.lblStrategyInfo.setText(strategyName);
		}
		else {
			this.lblStrategyInfo.setText(strategy.toString());
		}
		
		/* Set norm generation and deliberativeness settings */
		String generationApproachText = generationApproach.toString();
		String deliberativenessText = "---";
		if(generationApproach == NormGenerator.Approach.Deliberative) {
			int deliberativeness = this.nsmSettings.getMinEvaluationsToClassifyNorms();
			deliberativenessText = String.valueOf(deliberativeness) + 
					(deliberativeness == 1? " evidence" : " evidences");	
		}
		
		/* Set norm evaluation settings */
		String evaluationMechanismText = evaluationMech.toString();
		String perfRangeSizeText = 
				String.valueOf(this.nsmSettings.getNormsPerformanceRangesSize());
		
		/* Set norm generalisation settings */
		String generalisationApproachText = "---";
		String generalisationModeText = "---";
		String generalisationStepText = "---";
		
		if(nsmSettings.pursueCompactness()) {
			generalisationApproachText = generalisationApproach.toString();
			
			if(generalisationApproach == Approach.Optimistic) {
				generalisationModeText = 
						nsmSettings.getOptimisticNormGeneralisationMode().toString();
				generalisationStepText = 
						String.valueOf(nsmSettings.getOptimisticNormGeneralisationStep());
			}
		}
		
		/* Set norm synergies settings */
		String deliberativenessGroupsText = "---";
		if(this.nsmSettings.pursueLiberality()) {
			deliberativenessGroupsText = String.valueOf(
					this.nsmSettings.getMinEvaluationsToClassifyNormGroups());
		}

		this.lblGenerationInfo.setText(generationApproachText);
		this.lblDeliberativenessInfo.setText(deliberativenessText);
		this.lblEvaluationApproachInfo.setText(evaluationMechanismText);
		this.lblPerfRangeSizeInfo.setText(perfRangeSizeText);
		
		this.lblGeneralisationInfo.setText(generalisationApproachText);
		this.lblGenModeInfo.setText(generalisationModeText);
		this.lblGenStepInfo.setText(generalisationStepText);
		this.lblDelibPairsInfo.setText(deliberativenessGroupsText);
	}

	/**
	 * 
	 */
	public void displayMetrics() {

		/* Normative network metrics */
		int nnCardinality = this.normativeNetwork.getNorms().size();
		int numGenRels = this.normativeNetwork.
				getRelationships(NetworkEdgeType.Generalisation).size();
		int numSubsRels = this.normativeNetwork.
				getRelationships(NetworkEdgeType.Substitutability).size();
		int numComplRels = this.normativeNetwork.
				getRelationships(NetworkEdgeType.Complementarity).size();

		this.lblStoredNormsInfo.setText(String.valueOf(nnCardinality));
		this.lblGenRelsInfo.setText(String.valueOf(numGenRels));
		this.lblSubsRelsInfo.setText(String.valueOf(numSubsRels));
		this.lblCompRelsInfo.setText(String.valueOf(numComplRels));

		/* Normative system metrics */
		Goal goal = this.nsmSettings.getSystemGoals().get(0);
		Utility u = this.nsMetrics.getNormativeSystemUtility();

		int nsMinimality = this.nsMetrics.getNormativeSystemMinimality();
		int nsFMinimality = this.nsMetrics.getNormativeSystemFitoussiMinimality();
		double nsEff = u.getScore(EvaluationCriteria.Effectiveness, goal);
		double nsNec = u.getScore(EvaluationCriteria.Necessity, goal);

		nsEff = (Math.floor( nsEff * 100.0 ) / 100.0);
		nsNec = (Math.floor( nsNec * 100.0 ) / 100.0);

		this.lblActiveNormsInfo.setText(String.valueOf(nsMinimality));
		this.lblNSReprNormsInfo.setText(String.valueOf(nsFMinimality));
		this.lblNSMedEffInfo.setText(String.valueOf(nsEff != 0 ? nsEff : "---"));
		this.lblNSMedNecInfo.setText(String.valueOf(nsNec != 0 ? nsNec : "---"));

		/* Update norm synthesis metrics */
		long numStoredNorms = this.nsMetrics.getNumNodesInMemory(); 
		long numAccToNorms = this.nsMetrics.getNumNodesVisited();
		double medCompTime = this.nsMetrics.getMedianComputationTime() / 1000;
		double totalCompTime = this.nsMetrics.getTotalComputationTime() / 1000;
		long numTicksStability = this.nsMetrics.getNumTicksOfStability();
		boolean converged = this.nsMetrics.hasConverged();

		medCompTime = Math.round( medCompTime * 10000.0 ) / 10000.0;
		totalCompTime = Math.round( totalCompTime * 100.0 ) / 100.0;

		this.lblStoredNormsInfo.setText(String.valueOf(numStoredNorms));
		this.lblNormAccessesInfo.setText(String.valueOf(numAccToNorms));
		this.lblMedCompTimeInfo.setText(String.valueOf(medCompTime) + " s");
		this.lblTotalCompTimeInfo.setText(String.valueOf(totalCompTime) + " s");
		this.lblNSStabilityInfo.setText(String.valueOf(numTicksStability) + " ticks");
		if(converged) {
			this.lblConvergenceInfo.setText("YES!");
			this.lblConvergenceInfo.setForeground(new Color(0,150,0));
		}
		else {
			this.lblConvergenceInfo.setText("Not yet");
			this.lblConvergenceInfo.setForeground(Color.RED);
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeNSChanged(TreeSelectionEvent evt) {
		if(treeNS.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeNS.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof Norm) {
				this.selectedNorm = (Norm)src;
				this.updateSelectedNorm();
			}
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeUnusedNormsChanged(TreeSelectionEvent evt) {
		if(treeUnusedNorms.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeUnusedNorms.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof Norm) {
				this.selectedNorm = (Norm)src;
				this.updateSelectedNorm();
			}
		}
	}

	/**
	 * 
	 * 
	 * @param cSolution
	 */
	private void updateTreeNS() {
		List<Norm> nsNorms = normativeNetwork.getNormativeSystem();
		TreePath focus = null;

		this.treeNS.removeAll();

		int f_minimality = this.nsm.getNormSynthesisMetrics().
				getNormativeSystemFitoussiMinimality();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = 
				new DefaultMutableTreeNode("Norms in use (" + 
						nsNorms.size() + "), w " + f_minimality + " leaves");

		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norms that are being evaluated
		for(Norm norm : nsNorms) {
			this.fillTree(rootNode, norm);
		}

		this.treeNS.setModel(tmodel);
		this.treeNS.setSelectionPath(focus);
		this.treeNS.validate();
	}

	/**
	 * 
	 */
	private void updateTreeUnusedNorms() {
		List<Norm> notRepresented = normativeNetwork.getNotRepresentedNorms();
		List<Norm> unusedGeneralNorms = new ArrayList<Norm>();
		List<Norm> unusedLeaves = new ArrayList<Norm>();
		List<Norm> candidateNorms = new ArrayList<Norm>();
		List<Norm> substitutedNorms = new ArrayList<Norm>();
		TreePath focus = null;

		this.treeUnusedNorms.removeAll();

		for(Norm norm : notRepresented) {
			if(this.normativeNetwork.getState(norm) == NetworkNodeState.Candidate) {
				candidateNorms.add(norm);
			}
			else if(this.normativeNetwork.getState(norm) == NetworkNodeState.Inactive) {
				if(this.normativeNetwork.getGeneralisationLevel(norm) == 1) {
					unusedLeaves.add(norm);
				}
				else {
					unusedGeneralNorms.add(norm);
				}
			}
			else if(this.normativeNetwork.getState(norm) == NetworkNodeState.Substituted) {
				substitutedNorms.add(norm);
			}
		}

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode =
				new DefaultMutableTreeNode("Norms not in use");

		DefaultMutableTreeNode candidateNormsRoot =
				new DefaultMutableTreeNode("Candidate norms (" +
						candidateNorms.size() + ")");

		DefaultMutableTreeNode unusedGeneralNormsRoot =
				new DefaultMutableTreeNode("Discarded general norms (" +
						unusedGeneralNorms.size() + ")");

		DefaultMutableTreeNode unusedLeavesRoot =
				new DefaultMutableTreeNode("Discarded leaves (" + 
						unusedLeaves.size() + ")");

		DefaultMutableTreeNode substitutedNormsRoot =
				new DefaultMutableTreeNode("Substituted norms (" +
						substitutedNorms.size() + ")");

		rootNode.add(candidateNormsRoot);
		rootNode.add(unusedGeneralNormsRoot);
		rootNode.add(unusedLeavesRoot);
		rootNode.add(substitutedNormsRoot);

		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norms that are being evaluated
		for(Norm norm : candidateNorms) {
			this.fillTree(candidateNormsRoot, norm);
		}		

		for(Norm norm : unusedGeneralNorms) {
			this.fillTree(unusedGeneralNormsRoot, norm);
		}		

		// Add norms that are being evaluated
		for(Norm norm : unusedLeaves) {
			this.fillTree(unusedLeavesRoot, norm);
		}

		for(Norm norm : substitutedNorms) {
			this.fillTree(substitutedNormsRoot, norm);
		}		

		this.treeUnusedNorms.setModel(tmodel);
		this.treeUnusedNorms.setSelectionPath(focus);
		this.treeUnusedNorms.validate();
	}

	/**
	 * 
	 */
	private void updateSelectedNorm() {
		// TODO
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeActiveNormGroupsChanged(TreeSelectionEvent evt) {
		if(treeActiveNormGroups.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeActiveNormGroups.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof NormGroup) {
				this.selectedNormGroup = (NormGroup)src;
				this.updateSelectedNormGroup();
			}
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void treeInactiveNormGroupsChanged(TreeSelectionEvent evt) {
		if(treeInactiveNormGroups.getLastSelectedPathComponent() 
				instanceof DefaultMutableTreeNode) {

			DefaultMutableTreeNode srcNode = 
					(DefaultMutableTreeNode)treeInactiveNormGroups.getLastSelectedPathComponent();
			Object src = srcNode.getUserObject();

			if(src instanceof NormGroup) {
				this.selectedNormGroup = (NormGroup)src;
				this.updateSelectedNormGroup();
			}
		}
	}

	/**
	 * 
	 * 
	 * @param cSolution
	 */
	private void updateTreeActiveNormGroups() {
		List<NormGroup> activeNormGroups = normGroupNetwork.getActiveNodes();
		TreePath focus = null;

		Collections.sort(activeNormGroups);

		this.treeActiveNormGroups.removeAll();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Norm groups in use (" + activeNormGroups.size() + ")");
		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norm groups that are being evaluated
		for(NormGroup normGroup : activeNormGroups) {
			this.fillTree(rootNode, normGroup);
		}

		this.treeActiveNormGroups.setModel(tmodel);
		this.treeActiveNormGroups.setSelectionPath(focus);
		this.treeActiveNormGroups.validate();
	}

	/**
	 * 
	 */
	private void updateTreeInactiveNormGroups() {
		List<NormGroup> nonRepresented = normGroupNetwork.getNotRepresentedNormGroups();
		TreePath focus = null;

		Collections.sort(nonRepresented);

		this.treeInactiveNormGroups.removeAll();

		/* Update tree of norms in use */
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Norm groups in use (" + nonRepresented.size() + ")");
		TreeModel tmodel = new DefaultTreeModel(rootNode);

		// Add norm groups that are being evaluated
		for(NormGroup normGroup : nonRepresented) {
			this.fillTree(rootNode, normGroup);
		}

		this.treeInactiveNormGroups.setModel(tmodel);
		this.treeInactiveNormGroups.setSelectionPath(focus);
		this.treeInactiveNormGroups.validate();
	}

	/**
	 * 
	 */
	private void updateSelectedNormGroup() {
		// TODO
	}


	/**
	 * 
	 * @param node
	 * @param children
	 */
	private void fillTree(DefaultMutableTreeNode parentNode, Norm norm) {
		List<Norm> children = normativeNetwork.getChildren(norm);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(norm);
		parentNode.add(node);

		for(Norm child : children) {
			this.fillTree(node, child);
		}
	}

	/**
	 * 
	 * @param node
	 * @param children
	 */
	private void fillTree(DefaultMutableTreeNode parentNode, NormGroup normGroup) {
		List<NormGroup> children = normGroupNetwork.getChildren(normGroup);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(normGroup);
		parentNode.add(node);

		for(NormGroup child : children) {
			this.fillTree(node, child);
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
		this.displayMetrics();
		this.updateTreeNS();
		this.updateTreeUnusedNorms();
		this.updateTreeActiveNormGroups();
		this.updateTreeInactiveNormGroups();
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnShowNormPerfRangeActionPerformed(java.awt.event.ActionEvent evt) {
		if(this.selectedNorm != null) {
			nInspector.addNormScoreChart(this.selectedNorm);
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnShowNormGroupPerfRangeActionPerformed(java.awt.event.ActionEvent evt) {
		if(this.selectedNormGroup != null) {
			nInspector.addNormGroupScoreChart(this.selectedNormGroup);
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnConsoleActionPerformed(java.awt.event.ActionEvent evt) {                                           
		if(!console.isVisible()) {
			console.setVisible(true);
		}
	}   

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		lblTitle = new javax.swing.JLabel();
		panelNSSettings = new javax.swing.JPanel();
		panelNSSettingsLeft = new javax.swing.JPanel();
		lblStrategy = new javax.swing.JLabel();
		lblGeneration = new javax.swing.JLabel();
		lblDeliberativeness = new javax.swing.JLabel();
		lblPerfRangeSize = new javax.swing.JLabel();
		lblPerfRangeSizeInfo = new javax.swing.JLabel();
		lblEvaluationApproachInfo = new javax.swing.JLabel();
		lblDeliberativenessInfo = new javax.swing.JLabel();
		lblStrategyInfo = new javax.swing.JLabel();
		lblGenerationInfo = new javax.swing.JLabel();
		lblEvaluation = new javax.swing.JLabel();
		panelNSSettingsRight = new javax.swing.JPanel();
		lblGeneralisationInfo = new javax.swing.JLabel();
		lblGenMode = new javax.swing.JLabel();
		lblDelibPairs = new javax.swing.JLabel();
		lblDelibPairsInfo = new javax.swing.JLabel();
		lblGenModeInfo = new javax.swing.JLabel();
		lblGenStep = new javax.swing.JLabel();
		lblGenStepInfo = new javax.swing.JLabel();
		lblGeneralisation = new javax.swing.JLabel();
		panelNNMetrics = new javax.swing.JPanel();
		lblStoredNorms = new javax.swing.JLabel();
		lblStoredNormsInfo = new javax.swing.JLabel();
		lblActiveNorms = new javax.swing.JLabel();
		lblActiveNormsInfo = new javax.swing.JLabel();
		lblGenRels = new javax.swing.JLabel();
		lblGenRelsInfo = new javax.swing.JLabel();
		lblSubsRels = new javax.swing.JLabel();
		lblSubsRelsInfo = new javax.swing.JLabel();
		lblCompRels = new javax.swing.JLabel();
		lblCompRelsInfo = new javax.swing.JLabel();
		panelNSMetrics = new javax.swing.JPanel();
		lblNSReprNorms = new javax.swing.JLabel();
		lblNSReprNormsInfo = new javax.swing.JLabel();
		lblNSMedEff = new javax.swing.JLabel();
		lblNSMedEffInfo = new javax.swing.JLabel();
		lblNSMedNec = new javax.swing.JLabel();
		lblNSMedNecInfo = new javax.swing.JLabel();
		panelNSynthesisMetrics = new javax.swing.JPanel();
		lblNormAccesses = new javax.swing.JLabel();
		lblNormAccessesInfo = new javax.swing.JLabel();
		lblMedCompTime = new javax.swing.JLabel();
		lblMedCompTimeInfo = new javax.swing.JLabel();
		lblTotalCompTime = new javax.swing.JLabel();
		lblTotalCompTimeInfo = new javax.swing.JLabel();
		lblNSStability = new javax.swing.JLabel();
		lblNSStabilityInfo = new javax.swing.JLabel();
		lblConvergence = new javax.swing.JLabel();
		lblConvergenceInfo = new javax.swing.JLabel();
		panelNormsAndGroups = new javax.swing.JTabbedPane();
		panelNorms = new javax.swing.JPanel();
		scrPanelNS = new javax.swing.JScrollPane();
		treeNS = new javax.swing.JTree();
		scrPanelUnusedNorms = new javax.swing.JScrollPane();
		treeUnusedNorms = new javax.swing.JTree();
		panelSelectedNorm = new javax.swing.JPanel();
		panelNormDescription = new javax.swing.JPanel();
		btnShowNormPerfRange = new javax.swing.JButton();
		lblNormPerfRange = new javax.swing.JLabel();
		lblNormativeSystem = new javax.swing.JLabel();
		lblNormsNotInUse = new javax.swing.JLabel();
		panelNormGroups = new javax.swing.JPanel();
		lblActiveNormGroups = new javax.swing.JLabel();
		scrPanelActiveNGroups = new javax.swing.JScrollPane();
		treeActiveNormGroups = new javax.swing.JTree();
		lblInactiveNormGroups = new javax.swing.JLabel();
		scrPanelInactiveNormGroups = new javax.swing.JScrollPane();
		treeInactiveNormGroups = new javax.swing.JTree();
		panelSelectedNormGroup = new javax.swing.JPanel();
		panelNormGroupDescription = new javax.swing.JPanel();
		btnShowNormGroupPerfRange = new javax.swing.JButton();
		lblNormGroupPerfRange = new javax.swing.JLabel();
		btnUpdate = new javax.swing.JButton();
		btnConsole = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/normlab/icons/magnifGlass.png")); // NOI18N
		lblTitle.setText("Norms Inspector");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		panelNSSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblStrategy.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblStrategy.setText("Synthesis strategy:");

		lblGeneration.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGeneration.setText("Norm generation:");

		lblDeliberativeness.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblDeliberativeness.setText("Deliberativeness: ");

		lblPerfRangeSize.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblPerfRangeSize.setText("Perf. range size:");

		lblPerfRangeSizeInfo.setText("50 evidences");

		lblEvaluationApproachInfo.setText("Reinf. Learning");

		lblDeliberativenessInfo.setText("1 evidence");

		lblStrategyInfo.setText("DESMON");

		lblGenerationInfo.setText("Reactive");

		lblEvaluation.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblEvaluation.setText("Norm evaluation:");

		javax.swing.GroupLayout panelNSSettingsLeftLayout = new javax.swing.GroupLayout(panelNSSettingsLeft);
		panelNSSettingsLeft.setLayout(panelNSSettingsLeftLayout);
		panelNSSettingsLeftLayout.setHorizontalGroup(
				panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSSettingsLeftLayout.createSequentialGroup()
						.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(lblStrategy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblGeneration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblDeliberativeness, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblEvaluation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblPerfRangeSize, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(lblEvaluationApproachInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblDeliberativenessInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblGenerationInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblStrategyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblPerfRangeSizeInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
				);
		panelNSSettingsLeftLayout.setVerticalGroup(
				panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSSettingsLeftLayout.createSequentialGroup()
						.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblStrategy)
								.addComponent(lblStrategyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblGeneration)
										.addComponent(lblGenerationInfo))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblDeliberativeness)
												.addComponent(lblDeliberativenessInfo))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblEvaluation)
														.addComponent(lblEvaluationApproachInfo))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panelNSSettingsLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(lblPerfRangeSize)
																.addComponent(lblPerfRangeSizeInfo)))
				);

		lblGeneralisationInfo.setText("Optimistic");

		lblGenMode.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenMode.setText("Generalisation mode:");

		lblDelibPairs.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblDelibPairs.setText("<html> Deliberativeness: <br> (norms pairs) </html>");
		lblDelibPairs.setVerticalAlignment(javax.swing.SwingConstants.TOP);

		lblDelibPairsInfo.setText("1 evidence");
		lblDelibPairsInfo.setVerticalAlignment(javax.swing.SwingConstants.TOP);

		lblGenModeInfo.setText("Shallow");

		lblGenStep.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenStep.setText("Generalisation step:");

		lblGenStepInfo.setText("1");

		lblGeneralisation.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGeneralisation.setText("Norm generalisation:");

		javax.swing.GroupLayout panelNSSettingsRightLayout = new javax.swing.GroupLayout(panelNSSettingsRight);
		panelNSSettingsRight.setLayout(panelNSSettingsRightLayout);
		panelNSSettingsRightLayout.setHorizontalGroup(
				panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSSettingsRightLayout.createSequentialGroup()
						.addGroup(panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(lblDelibPairs, javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblGenStep, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblGenMode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
								.addComponent(lblGeneralisation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelNSSettingsRightLayout.createSequentialGroup()
												.addComponent(lblGenModeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addGap(16, 16, 16))
												.addGroup(panelNSSettingsRightLayout.createSequentialGroup()
														.addComponent(lblGeneralisationInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addGap(6, 6, 6))
														.addGroup(panelNSSettingsRightLayout.createSequentialGroup()
																.addComponent(lblGenStepInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addGap(6, 6, 6))
																.addComponent(lblDelibPairsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
				);
		panelNSSettingsRightLayout.setVerticalGroup(
				panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSSettingsRightLayout.createSequentialGroup()
						.addGroup(panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblGeneralisation)
								.addComponent(lblGeneralisationInfo))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblGenMode)
										.addComponent(lblGenModeInfo))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblGenStep)
												.addComponent(lblGenStepInfo))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNSSettingsRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(lblDelibPairs)
														.addComponent(lblDelibPairsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addContainerGap())
				);

		javax.swing.GroupLayout panelNSSettingsLayout = new javax.swing.GroupLayout(panelNSSettings);
		panelNSSettings.setLayout(panelNSSettingsLayout);
		panelNSSettingsLayout.setHorizontalGroup(
				panelNSSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSSettingsLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panelNSSettingsLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelNSSettingsRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);
		panelNSSettingsLayout.setVerticalGroup(
				panelNSSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSSettingsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNSSettingsRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(panelNSSettingsLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelNNMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Normative network metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblStoredNorms.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblStoredNorms.setText("Stored norms:");

		lblStoredNormsInfo.setText("20");

		lblActiveNorms.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblActiveNorms.setText("Active norms:");

		lblActiveNormsInfo.setText("20");

		lblGenRels.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenRels.setText("Generalisation relationships:");

		lblGenRelsInfo.setText("20");

		lblSubsRels.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblSubsRels.setText("Substitutability relationships:");

		lblSubsRelsInfo.setText("20");

		lblCompRels.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblCompRels.setText("Complementarity relationships:");

		lblCompRelsInfo.setText("20");

		javax.swing.GroupLayout panelNNMetricsLayout = new javax.swing.GroupLayout(panelNNMetrics);
		panelNNMetrics.setLayout(panelNNMetricsLayout);
		panelNNMetricsLayout.setHorizontalGroup(
				panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNNMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(lblCompRels, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
								.addComponent(lblSubsRels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblGenRels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblActiveNorms, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblStoredNorms, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblCompRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblActiveNormsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblGenRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblSubsRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblStoredNormsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addContainerGap())
				);
		panelNNMetricsLayout.setVerticalGroup(
				panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNNMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblStoredNorms)
								.addComponent(lblStoredNormsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblActiveNorms)
										.addComponent(lblActiveNormsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblGenRels)
												.addComponent(lblGenRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblSubsRels)
														.addComponent(lblSubsRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panelNNMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(lblCompRels)
																.addComponent(lblCompRelsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addGap(16, 16, 16))
				);

		panelNSMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Normative system metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblNSReprNorms.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNSReprNorms.setText("Represented norms:");

		lblNSReprNormsInfo.setText("20");

		lblNSMedEff.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNSMedEff.setText("Median effectiveness:");

		lblNSMedEffInfo.setText("20");

		lblNSMedNec.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNSMedNec.setText("Median necessity:");

		lblNSMedNecInfo.setText("20");

		javax.swing.GroupLayout panelNSMetricsLayout = new javax.swing.GroupLayout(panelNSMetrics);
		panelNSMetrics.setLayout(panelNSMetricsLayout);
		panelNSMetricsLayout.setHorizontalGroup(
				panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(lblNSMedEff, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
								.addComponent(lblNSReprNorms, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNSMedNec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblNSMedEffInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
										.addComponent(lblNSMedNecInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblNSReprNormsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addContainerGap())
				);
		panelNSMetricsLayout.setVerticalGroup(
				panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblNSReprNorms)
								.addComponent(lblNSReprNormsInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNSMedEff)
										.addComponent(lblNSMedEffInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelNSMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblNSMedNec)
												.addComponent(lblNSMedNecInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addContainerGap())
				);

		panelNSynthesisMetrics.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm synthesis metrics", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblNormAccesses.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNormAccesses.setText("Norm accesses:");

		lblNormAccessesInfo.setText("20");

		lblMedCompTime.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblMedCompTime.setText("Median computation time:");

		lblMedCompTimeInfo.setText("12.34 secs");

		lblTotalCompTime.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblTotalCompTime.setText("Total computation time:");

		lblTotalCompTimeInfo.setText("12.34 secs");

		lblNSStability.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNSStability.setText("Stability of current NS:");

		lblNSStabilityInfo.setText("12345 ticks");

		lblConvergence.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblConvergence.setText("Convergence:");

		lblConvergenceInfo.setText("Not yet");

		javax.swing.GroupLayout panelNSynthesisMetricsLayout = new javax.swing.GroupLayout(panelNSynthesisMetrics);
		panelNSynthesisMetrics.setLayout(panelNSynthesisMetricsLayout);
		panelNSynthesisMetricsLayout.setHorizontalGroup(
				panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSynthesisMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(lblMedCompTime, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
								.addComponent(lblTotalCompTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNormAccesses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(lblMedCompTimeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, /*javax.swing.GroupLayout.DEFAULT_SIZE*/ 60, Short.MAX_VALUE)
										.addComponent(lblNormAccessesInfo, javax.swing.GroupLayout.DEFAULT_SIZE, /* javax.swing.GroupLayout.DEFAULT_SIZE */ 60, Short.MAX_VALUE)
										.addComponent(lblTotalCompTimeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, /* javax.swing.GroupLayout.DEFAULT_SIZE */ 60, Short.MAX_VALUE))
										.addGap(18, 18, 18)
										.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(panelNSynthesisMetricsLayout.createSequentialGroup()
														.addComponent(lblConvergence, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(lblConvergenceInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addGroup(panelNSynthesisMetricsLayout.createSequentialGroup()
																.addComponent(lblNSStability, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(lblNSStabilityInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)))
																.addContainerGap())
				);
		panelNSynthesisMetricsLayout.setVerticalGroup(
				panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNSynthesisMetricsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelNSynthesisMetricsLayout.createSequentialGroup()
										.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblNormAccesses)
												.addComponent(lblNormAccessesInfo))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblMedCompTime)
														.addComponent(lblMedCompTimeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(lblTotalCompTime)
																.addComponent(lblTotalCompTimeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addContainerGap())
																.addGroup(panelNSynthesisMetricsLayout.createSequentialGroup()
																		.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(lblNSStabilityInfo)
																				.addComponent(lblNSStability))
																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																				.addGroup(panelNSynthesisMetricsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(lblConvergenceInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																						.addComponent(lblConvergence))
																						.addContainerGap())))
				);

		panelNormsAndGroups.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

		scrPanelNS.setViewportView(treeNS);

		scrPanelUnusedNorms.setViewportView(treeUnusedNorms);

		panelSelectedNorm.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Selected norm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 204))); // NOI18N

		panelNormDescription.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		/* Add tree listeners */
		treeNS.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeNSChanged(evt);
			}
		});	

		treeUnusedNorms.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeUnusedNormsChanged(evt);
			}
		});	

		treeActiveNormGroups.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeActiveNormGroupsChanged(evt);
			}
		});	

		treeInactiveNormGroups.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeInactiveNormGroupsChanged(evt);
			}
		});	


		javax.swing.GroupLayout panelNormDescriptionLayout = new javax.swing.GroupLayout(panelNormDescription);
		panelNormDescription.setLayout(panelNormDescriptionLayout);
		panelNormDescriptionLayout.setHorizontalGroup(
				panelNormDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE)
				);
		panelNormDescriptionLayout.setVerticalGroup(
				panelNormDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 161, Short.MAX_VALUE)
				);

		btnShowNormPerfRange.setIcon(new javax.swing.ImageIcon("misc/normlab/icons/utility.png")); // NOI18N
		btnShowNormPerfRange.setText("Show ");
		btnShowNormPerfRange.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnShowNormPerfRangeActionPerformed(evt);
			}
		});

		lblNormPerfRange.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNormPerfRange.setText("Performance ranges");

		javax.swing.GroupLayout panelSelectedNormLayout = new javax.swing.GroupLayout(panelSelectedNorm);
		panelSelectedNorm.setLayout(panelSelectedNormLayout);
		panelSelectedNormLayout.setHorizontalGroup(
				panelSelectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSelectedNormLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelSelectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNormDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(panelSelectedNormLayout.createSequentialGroup()
										.addComponent(lblNormPerfRange, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(btnShowNormPerfRange)))
										.addContainerGap())
				);
		panelSelectedNormLayout.setVerticalGroup(
				panelSelectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSelectedNormLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panelNormDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(panelSelectedNormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(btnShowNormPerfRange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNormPerfRange, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap())
				);

		lblNormativeSystem.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNormativeSystem.setText("Normative system");

		lblNormsNotInUse.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNormsNotInUse.setText("Norms not in use");

		javax.swing.GroupLayout panelNormsLayout = new javax.swing.GroupLayout(panelNorms);
		panelNorms.setLayout(panelNormsLayout);
		panelNormsLayout.setHorizontalGroup(
				panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblNormativeSystem)
								.addComponent(scrPanelNS, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblNormsNotInUse)
										.addComponent(scrPanelUnusedNorms, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(panelSelectedNorm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addContainerGap())
				);
		panelNormsLayout.setVerticalGroup(
				panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelSelectedNorm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelNormsLayout.createSequentialGroup()
										.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblNormativeSystem)
												.addComponent(lblNormsNotInUse))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNormsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(scrPanelNS, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
														.addComponent(scrPanelUnusedNorms, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
														.addContainerGap())
				);

		panelNormsAndGroups.addTab("Synthesised norms", panelNorms);

		lblActiveNormGroups.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblActiveNormGroups.setText("Active groups");

		scrPanelActiveNGroups.setViewportView(treeActiveNormGroups);

		lblInactiveNormGroups.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblInactiveNormGroups.setText("Inactive groups");

		scrPanelInactiveNormGroups.setViewportView(treeInactiveNormGroups);

		panelSelectedNormGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Selected norm group", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 204))); // NOI18N

		panelNormGroupDescription.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout panelNormGroupDescriptionLayout = new javax.swing.GroupLayout(panelNormGroupDescription);
		panelNormGroupDescription.setLayout(panelNormGroupDescriptionLayout);
		panelNormGroupDescriptionLayout.setHorizontalGroup(
				panelNormGroupDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE)
				);
		panelNormGroupDescriptionLayout.setVerticalGroup(
				panelNormGroupDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 161, Short.MAX_VALUE)
				);

		btnShowNormGroupPerfRange.setIcon(new javax.swing.ImageIcon("misc/normlab/icons/utility.png")); // NOI18N
		btnShowNormGroupPerfRange.setText("Show ");
		btnShowNormGroupPerfRange.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnShowNormGroupPerfRangeActionPerformed(evt);
			}
		});

		lblNormGroupPerfRange.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNormGroupPerfRange.setText("Performance ranges");

		javax.swing.GroupLayout panelSelectedNormGroupLayout = new javax.swing.GroupLayout(panelSelectedNormGroup);
		panelSelectedNormGroup.setLayout(panelSelectedNormGroupLayout);
		panelSelectedNormGroupLayout.setHorizontalGroup(
				panelSelectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSelectedNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelSelectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNormGroupDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(panelSelectedNormGroupLayout.createSequentialGroup()
										.addComponent(lblNormGroupPerfRange, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(btnShowNormGroupPerfRange)))
										.addContainerGap())
				);
		panelSelectedNormGroupLayout.setVerticalGroup(
				panelSelectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelSelectedNormGroupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panelNormGroupDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(panelSelectedNormGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(btnShowNormGroupPerfRange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblNormGroupPerfRange, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap())
				);

		javax.swing.GroupLayout panelNormGroupsLayout = new javax.swing.GroupLayout(panelNormGroups);
		panelNormGroups.setLayout(panelNormGroupsLayout);
		panelNormGroupsLayout.setHorizontalGroup(
				panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblActiveNormGroups)
								.addComponent(scrPanelActiveNGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(lblInactiveNormGroups)
										.addComponent(scrPanelInactiveNormGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(panelSelectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addContainerGap())
				);
		panelNormGroupsLayout.setVerticalGroup(
				panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelNormGroupsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addGroup(panelNormGroupsLayout.createSequentialGroup()
										.addGroup(panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblActiveNormGroups)
												.addComponent(lblInactiveNormGroups))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelNormGroupsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(scrPanelActiveNGroups, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
														.addComponent(scrPanelInactiveNormGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
														.addComponent(panelSelectedNormGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addContainerGap())
				);

		panelNormsAndGroups.addTab("Synthesised norm groups", panelNormGroups);

		btnUpdate.setIcon(new javax.swing.ImageIcon("misc/normlab/icons/update.png")); // NOI18N
		btnUpdate.setText("Update");
		btnUpdate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnUpdateActionPerformed(evt);
			}
		});

		btnConsole.setIcon(new javax.swing.ImageIcon("misc/normlab/icons/console_small.png")); // NOI18N
		btnConsole.setText("Open console");
		btnConsole.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnConsoleActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelNormsAndGroups)
								.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
										.addComponent(panelNSMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(panelNSynthesisMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addGroup(layout.createSequentialGroup()
												.addComponent(panelNSSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(panelNNMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addGroup(layout.createSequentialGroup()
														.addComponent(btnUpdate)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(btnConsole)))
														.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(panelNSSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelNNMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
										.addComponent(panelNSMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panelNSynthesisMetrics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(panelNormsAndGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addContainerGap())
				);


		pack();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {

		try {
			for (javax.swing.UIManager.LookAndFeelInfo info :
				javax.swing.UIManager.getInstalledLookAndFeels()) {

				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(NormsInspectorFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(NormsInspectorFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(NormsInspectorFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(NormsInspectorFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new NormsInspectorFrame(nInspector, console).setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnShowNormGroupPerfRange;
	private javax.swing.JButton btnShowNormPerfRange;
	private javax.swing.JButton btnUpdate;
	private javax.swing.JButton btnConsole;
	private javax.swing.JLabel lblActiveNormGroups;
	private javax.swing.JLabel lblActiveNorms;
	private javax.swing.JLabel lblActiveNormsInfo;
	private javax.swing.JLabel lblCompRels;
	private javax.swing.JLabel lblCompRelsInfo;
	private javax.swing.JLabel lblConvergence;
	private javax.swing.JLabel lblConvergenceInfo;
	private javax.swing.JLabel lblDelibPairs;
	private javax.swing.JLabel lblDelibPairsInfo;
	private javax.swing.JLabel lblDeliberativeness;
	private javax.swing.JLabel lblDeliberativenessInfo;
	private javax.swing.JLabel lblEvaluation;
	private javax.swing.JLabel lblEvaluationApproachInfo;
	private javax.swing.JLabel lblGenMode;
	private javax.swing.JLabel lblGenModeInfo;
	private javax.swing.JLabel lblGenRels;
	private javax.swing.JLabel lblGenRelsInfo;
	private javax.swing.JLabel lblGenStep;
	private javax.swing.JLabel lblGenStepInfo;
	private javax.swing.JLabel lblGeneralisation;
	private javax.swing.JLabel lblGeneralisationInfo;
	private javax.swing.JLabel lblGeneration;
	private javax.swing.JLabel lblGenerationInfo;
	private javax.swing.JLabel lblInactiveNormGroups;
	private javax.swing.JLabel lblMedCompTime;
	private javax.swing.JLabel lblMedCompTimeInfo;
	private javax.swing.JLabel lblNSMedNecInfo;
	private javax.swing.JLabel lblNSMedEff;
	private javax.swing.JLabel lblNSMedEffInfo;
	private javax.swing.JLabel lblNSMedNec;
	private javax.swing.JLabel lblNSReprNorms;
	private javax.swing.JLabel lblNSReprNormsInfo;
	private javax.swing.JLabel lblNSStability;
	private javax.swing.JLabel lblNSStabilityInfo;
	private javax.swing.JLabel lblNormAccesses;
	private javax.swing.JLabel lblNormAccessesInfo;
	private javax.swing.JLabel lblNormGroupPerfRange;
	private javax.swing.JLabel lblNormPerfRange;
	private javax.swing.JLabel lblNormativeSystem;
	private javax.swing.JLabel lblNormsNotInUse;
	private javax.swing.JLabel lblPerfRangeSize;
	private javax.swing.JLabel lblPerfRangeSizeInfo;
	private javax.swing.JLabel lblStoredNorms;
	private javax.swing.JLabel lblStoredNormsInfo;
	private javax.swing.JLabel lblStrategy;
	private javax.swing.JLabel lblStrategyInfo;
	private javax.swing.JLabel lblSubsRels;
	private javax.swing.JLabel lblSubsRelsInfo;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblTotalCompTime;
	private javax.swing.JLabel lblTotalCompTimeInfo;
	private javax.swing.JPanel panelNNMetrics;
	private javax.swing.JPanel panelNSMetrics;
	private javax.swing.JPanel panelNSSettings;
	private javax.swing.JPanel panelNSSettingsLeft;
	private javax.swing.JPanel panelNSSettingsRight;
	private javax.swing.JPanel panelNSynthesisMetrics;
	private javax.swing.JPanel panelNormDescription;
	private javax.swing.JPanel panelNormGroupDescription;
	private javax.swing.JPanel panelNormGroups;
	private javax.swing.JPanel panelNorms;
	private javax.swing.JTabbedPane panelNormsAndGroups;
	private javax.swing.JPanel panelSelectedNorm;
	private javax.swing.JPanel panelSelectedNormGroup;
	private javax.swing.JScrollPane scrPanelActiveNGroups;
	private javax.swing.JScrollPane scrPanelInactiveNormGroups;
	private javax.swing.JScrollPane scrPanelNS;
	private javax.swing.JScrollPane scrPanelUnusedNorms;
	private javax.swing.JTree treeActiveNormGroups;
	private javax.swing.JTree treeInactiveNormGroups;
	private javax.swing.JTree treeNS;
	private javax.swing.JTree treeUnusedNorms;
	// End of variables declaration//GEN-END:variables
}
