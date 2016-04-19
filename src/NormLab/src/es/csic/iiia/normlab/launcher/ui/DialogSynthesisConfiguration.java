/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.launcher.ui;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import es.csic.iiia.normlab.launcher.model.RepastXMLManager;
import es.csic.iiia.normlab.launcher.ui.NormLabFrame.NormLabSimulator;
import es.csic.iiia.normlab.launcher.utils.JDecimalField;
import es.csic.iiia.normlab.launcher.utils.JIntegerField;

/**
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class DialogSynthesisConfiguration extends JDialog {

	private static final long serialVersionUID = 5800466654224117256L;

	private static RepastXMLManager configManager;
	private static NormLabSimulator sim;
	private static int strategy;

	private String normGenerationApproach;
	private String pursueCompactness;
	private String pursueLiberality;
	private String numTicksToConverge;
	private String normEvaluationMechanism;
	private String normEvaluationLearningRate;
	private String normsDefaultUtility;
	private String normsPerfRangeSize;
	private String normGeneralisationApproach;
	private String optimisticNormGeneralisationMode;
	private String optimisticNormGeneralisationStep;
	private String normEffGenThreshold;
	private String normNecGenThreshold;
	private String normsMinEvaluationsToDecide;
	private String normEffDeactThreshold;
	private String normNecDeactThreshold;
	private String normEffActThreshold;
	private String normNecActThreshold;
	private String normGroupsMinEvaluationsToDecide;

	/**
	 * Creates new form NormLabConfiguratorFrame
	 * @throws Exception 
	 */
	public DialogSynthesisConfiguration(Frame parent, boolean modal,
			NormLabSimulator simulator, RepastXMLManager cManager, int synthStrategy)
					throws Exception {

		super(parent, modal);

		configManager = cManager;
		strategy = synthStrategy;
		sim = simulator;

		initComponents();
		setResizable(false);

		this.lblTitle.setText("NormLab: " + sim + " synthesis settings");

		/* Load synthesis configuration from the configuration 
		 * manager received by argument */
		this.loadConfig();

		/* Check the configuration that must be employed by default
		 * depending on the synthesis strategy that is to be employed */
		this.checkDefaultConfig(strategy);

		/* Display configuration in the dialog */
		this.displayConfig();

		/* Lock option (if necessary) depending on the chosen strategy */
		this.lockUnusedOptions(strategy);
	}

	/**
	 * 
	 * @param paramsFile
	 * @throws Exception 
	 */
	private void loadConfig() throws Exception {
		normGenerationApproach = configManager.getAttribute("normGenerationApproach");
		pursueCompactness = configManager.getAttribute("pursueCompactness");
		pursueLiberality= configManager.getAttribute("pursueLiberality");
		numTicksToConverge= configManager.getAttribute("numTicksToConverge");
		normEvaluationMechanism= configManager.getAttribute("normEvaluationMechanism");
		normEvaluationLearningRate= configManager.getAttribute("normEvaluationLearningRate");
		normsDefaultUtility= configManager.getAttribute("normsDefaultUtility");
		normsPerfRangeSize= configManager.getAttribute("normsPerfRangeSize");
		normGeneralisationApproach= configManager.getAttribute("normGeneralisationApproach");
		optimisticNormGeneralisationMode= configManager.getAttribute("optimisticNormGeneralisationMode");
		optimisticNormGeneralisationStep= configManager.getAttribute("optimisticNormGeneralisationStep");
		normEffGenThreshold= configManager.getAttribute("normEffGenThreshold");
		normNecGenThreshold= configManager.getAttribute("normNecGenThreshold");
		normsMinEvaluationsToDecide = configManager.getAttribute("normsMinEvaluationsToDecide");
		normEffDeactThreshold = configManager.getAttribute("normEffDeactThreshold");
		normNecDeactThreshold= configManager.getAttribute("normNecDeactThreshold");
		normEffActThreshold = configManager.getAttribute("normEffActThreshold");
		normNecActThreshold = configManager.getAttribute("normNecActThreshold");
		normGroupsMinEvaluationsToDecide= configManager.getAttribute("normGroupsMinEvaluationsToDecide");
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void saveConfig() throws Exception {
		
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		
		/* Format integer numbers */
		numTicksToConverge = format.parse(numTicksToConverge).toString();
		normsPerfRangeSize = format.parse(normsPerfRangeSize).toString();
		normsMinEvaluationsToDecide = format.parse(normsMinEvaluationsToDecide).toString();
		normGroupsMinEvaluationsToDecide = format.parse(normGroupsMinEvaluationsToDecide).toString();
		
		configManager.setAttribute("normGenerationApproach", normGenerationApproach);
		configManager.setAttribute("pursueCompactness", pursueCompactness);
		configManager.setAttribute("pursueLiberality", pursueLiberality);
		configManager.setAttribute("numTicksToConverge", numTicksToConverge);
		configManager.setAttribute("normEvaluationMechanism", normEvaluationMechanism);
		configManager.setAttribute("normEvaluationLearningRate", normEvaluationLearningRate);
		configManager.setAttribute("normsDefaultUtility", normsDefaultUtility);
		configManager.setAttribute("normsPerfRangeSize", normsPerfRangeSize);
		configManager.setAttribute("normGeneralisationApproach", normGeneralisationApproach);
		configManager.setAttribute("optimisticNormGeneralisationMode", optimisticNormGeneralisationMode);
		configManager.setAttribute("optimisticNormGeneralisationStep", optimisticNormGeneralisationStep);
		configManager.setAttribute("normEffGenThreshold", normEffGenThreshold);
		configManager.setAttribute("normNecGenThreshold", normNecGenThreshold);
		configManager.setAttribute("normsMinEvaluationsToDecide", normsMinEvaluationsToDecide);
		configManager.setAttribute("normEffDeactThreshold", normEffDeactThreshold);
		configManager.setAttribute("normNecDeactThreshold", normNecDeactThreshold);
		configManager.setAttribute("normEffActThreshold", normEffActThreshold);
		configManager.setAttribute("normNecActThreshold", normNecActThreshold);
		configManager.setAttribute("normGroupsMinEvaluationsToDecide", normGroupsMinEvaluationsToDecide);
	}

	/**
	 * 
	 */
	private void displayConfig() {
		this.rbReactive.setSelected(this.normGenerationApproach.equals("0"));
		this.rbDeliberative.setSelected(this.normGenerationApproach.equals("1"));
		this.rbCompactnessYes.setSelected(pursueCompactness.equals("true"));
		this.rbCompactnessNo.setSelected(pursueCompactness.equals("false"));
		this.rbLiberalityYes.setSelected(pursueLiberality.equals("true"));
		this.rbLiberalityNo.setSelected(pursueLiberality.equals("false"));
		this.txtStability.setText(numTicksToConverge);
		this.rbBB.setSelected(normEvaluationMechanism.equals("0"));
		this.rbMAvg.setSelected(normEvaluationMechanism.equals("1"));
		this.txtLearningFactor.setText(normEvaluationLearningRate);
		this.txtDefUtility.setText(normsDefaultUtility);
		this.txtPerfRangeSize.setText(normsPerfRangeSize);
		this.rbConservative.setSelected(normGeneralisationApproach.equals("0"));
		this.rbOptimistic.setSelected(normGeneralisationApproach.equals("1"));
		this.rbShallow.setSelected(optimisticNormGeneralisationMode.equals("0"));
		this.rbDeep.setSelected(optimisticNormGeneralisationMode.equals("1"));
		this.txtGenStep.setText(optimisticNormGeneralisationStep);
		this.txtActEff.setText(normEffActThreshold);
		this.txtActNec.setText(normNecActThreshold);
		this.txtDeactEff.setText(normEffDeactThreshold);
		this.txtDeactNec.setText(normNecDeactThreshold);
		this.txtGenEff.setText(normEffGenThreshold);
		this.txtGenNec.setText(normNecGenThreshold);
		this.txtDeliberativeness.setText(normsMinEvaluationsToDecide);
		this.txtDeliberativenessGroups.setText(normGroupsMinEvaluationsToDecide);

		this.enableCompactness(this.rbCompactnessYes.isSelected());
		this.enableLiberality(this.rbLiberalityYes.isSelected());
		this.enableDeliberativeness(this.rbDeliberative.isSelected());
		this.txtLearningFactor.setEnabled(this.rbBB.isSelected());
	}

	/**
	 * 
	 */
	private void checkDefaultConfig(int strategy) {
		switch(strategy) {

		case 3: this.configureAsBASE();		break;
		case 4:	this.configureAsIRON();		break;
		case 5:	this.configureAsSIMON();	break;
		case 6:	this.configureAsLION();		break;
		case 7:	this.configureAsDESMON();	break;
		}
	}



	/**
	 * Sets BASE's default parameters 
	 */
	private void configureAsBASE() {
		this.pursueCompactness = "false";
		this.pursueLiberality = "false";
		this.normEvaluationMechanism = "1";
		this.normsDefaultUtility = "0.5";
		this.normsMinEvaluationsToDecide = "1";
		this.normEvaluationLearningRate = "0.0";
		this.normGroupsMinEvaluationsToDecide = "0";
		this.normGenerationApproach = "0";
	}

	/**
	 * Sets IRON's default parameters
	 */
	private void configureAsIRON() {		
		this.pursueCompactness = "true";
		this.pursueLiberality = "false";
		this.normEvaluationMechanism = "0";
		this.normsDefaultUtility = "0.5";
		this.normsMinEvaluationsToDecide = "1";
		this.normGeneralisationApproach = "0";
		this.normGroupsMinEvaluationsToDecide = "0";
		this.normGenerationApproach = "0";
	}

	/**
	 * 
	 */
	private void configureAsSIMON() {
		this.pursueCompactness = "true";
		this.pursueLiberality = "false";
		this.normEvaluationMechanism = "0";
		this.normsDefaultUtility = "0.5";
		this.normsMinEvaluationsToDecide = "1";
		this.normGeneralisationApproach = "1";
		this.normGroupsMinEvaluationsToDecide = "0";
		this.normGenerationApproach = "0";
	}

	/**
	 * 
	 */
	private void configureAsLION() {
		this.pursueCompactness = "true";
		this.pursueLiberality = "true";
		this.normEvaluationMechanism = "1";
		this.normsDefaultUtility = "0.5";
		this.normsMinEvaluationsToDecide = "1";
		this.normGeneralisationApproach = "1";
		this.normEvaluationLearningRate = "0.0";
		this.normGenerationApproach = "0";
	}

	/**
	 * 
	 */
	private void configureAsDESMON() {
		this.pursueCompactness = "true";
		this.pursueLiberality = "false";
		this.normEvaluationMechanism = "1";
		this.normsDefaultUtility = "0.0";
		this.normGeneralisationApproach = "1";
		this.normEvaluationLearningRate = "0.0";
		this.normGroupsMinEvaluationsToDecide = "0";
		this.normGenerationApproach = "1";

		int num = Integer.valueOf(normsMinEvaluationsToDecide);
		if(num <= 1) {
			this.normsMinEvaluationsToDecide = "10";	
		}
	}

	/**
	 * 
	 */
	private void lockUnusedOptions(int strategy) {
		switch(strategy) {

		case 3: this.lockBASEUnusedOptions();		break;
		case 4:	this.lockIRONUnusedOptions();		break;
		case 5:	this.lockSIMONUnusedOptions();	break;
		case 6:	this.lockLIONUnusedOptions();		break;
		case 7:	this.lockDESMONUnusedOptions();	break;
		}
	}

	/**
	 * 
	 */
	private void lockBASEUnusedOptions() {
		this.rbCompactnessYes.setEnabled(false);
		this.rbCompactnessYes.setEnabled(false);
		this.rbLiberalityYes.setEnabled(false);
		this.rbBB.setEnabled(false);
		this.txtLearningFactor.setEnabled(false);
		this.rbDeliberative.setEnabled(false);	  
	}

	/**
	 * 
	 */
	private void lockIRONUnusedOptions() {
		this.rbOptimistic.setEnabled(false);
		this.rbCompactnessNo.setEnabled(false);
		this.rbLiberalityYes.setEnabled(false);
		this.rbMAvg.setEnabled(false);
		this.txtDeliberativeness.setEnabled(false);
		this.txtDeliberativenessGroups.setEnabled(false);
		this.rbDeliberative.setEnabled(false);
		this.rbOptimistic.setEnabled(false);
		this.rbShallow.setEnabled(false);
		this.rbDeep.setEnabled(false);
		this.txtGenStep.setEnabled(false);	  
	}

	/**
	 * 
	 */
	private void lockSIMONUnusedOptions() {
		this.rbCompactnessNo.setEnabled(false);
		this.rbLiberalityYes.setEnabled(false);
		this.rbMAvg.setEnabled(false);
		this.txtDeliberativeness.setEnabled(false);
		this.txtDeliberativenessGroups.setEnabled(false);
		this.rbConservative.setEnabled(false);
		this.txtActEff.setEnabled(false);
		this.txtActNec.setEnabled(false);
		this.rbDeliberative.setEnabled(false);
		this.rbConservative.setEnabled(false);	  
	}

	/**
	 * 
	 */
	private void lockLIONUnusedOptions() {
		this.rbCompactnessNo.setEnabled(false);
		this.rbLiberalityNo.setEnabled(false);
		this.txtLearningFactor.setEnabled(false);
		this.rbBB.setEnabled(false);
		this.txtDeliberativeness.setEnabled(false);
		this.rbConservative.setEnabled(false);
		this.txtActEff.setEnabled(false);
		this.txtActNec.setEnabled(false);
		this.rbDeliberative.setEnabled(false);	  
	}

	/**
	 * 
	 */
	private void lockDESMONUnusedOptions() {
		this.rbCompactnessNo.setEnabled(false);
		this.rbLiberalityYes.setEnabled(false);
		this.txtLearningFactor.setEnabled(false);
		this.txtDefUtility.setEnabled(false);
		this.rbBB.setEnabled(false);
		this.txtDeliberativenessGroups.setEnabled(false);
		this.rbConservative.setEnabled(false);
		this.rbReactive.setEnabled(false);	  
	}

	/**
	 * 
	 * @param enable
	 */
	private void enableCompactness(boolean enable) {
		this.rbConservative.setEnabled(enable);
		this.rbOptimistic.setEnabled(enable);
		this.rbDeep.setEnabled(enable);
		this.rbShallow.setEnabled(enable);
		this.txtGenStep.setEnabled(enable);
		this.txtGenEff.setEnabled(enable);
		this.txtGenNec.setEnabled(enable);
	}

	/**
	 * 
	 * @param enable
	 */
	private void enableLiberality(boolean enable) {
		this.txtDeliberativenessGroups.setEnabled(enable);
	}

	/**
	 * 
	 * @param b
	 */
	private void enableDeliberativeness(boolean enable) {
		this.txtDeliberativeness.setEnabled(enable);	  
		this.txtActEff.setEnabled(enable);
		this.txtActNec.setEnabled(enable);
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnSaveActionPerformed(ActionEvent evt) {
		try {
			this.saveConfig();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		this.dispose();		
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnExitActionPerformed(ActionEvent evt) {
		this.dispose();
	}

	private void rbCompactnessYesActionPerformed(ActionEvent evt) {
		this.pursueCompactness = "true"; 
		this.enableCompactness(true);
	}

	private void rbCompactnessNoActionPerformed(ActionEvent evt) {
		this.pursueCompactness = "false";
		this.enableCompactness(false);
	}


	private void rbLiberalityYesActionPerformed(ActionEvent evt) {
		this.pursueLiberality = "true";
		this.enableLiberality(true);
	}

	private void rbLiberalityNoActionPerformed(ActionEvent evt) {
		this.pursueLiberality = "false";
		this.enableLiberality(false);
	}

	private void rbBBActionPerformed(ActionEvent evt) {
		this.normEvaluationMechanism = "0";
		this.txtLearningFactor.setEnabled(true);
	}

	private void rbMAvgActionPerformed(ActionEvent evt) {
		this.normEvaluationMechanism = "1";
		this.txtLearningFactor.setEnabled(false);
	}

	private void rbReactiveActionPerformed(java.awt.event.ActionEvent evt) {                                           
		this.normGenerationApproach = "0";
		this.enableDeliberativeness(false);
	}                                          

	private void rbDeliberativeActionPerformed(java.awt.event.ActionEvent evt) {                                               
		this.normGenerationApproach = "1";
		this.enableDeliberativeness(true);
	}   

	private void txtGenStepActionPerformed(DocumentEvent evt) {
		this.optimisticNormGeneralisationStep = this.txtGenStep.getText();  
	}

	private void txtStabilityActionPerformed(DocumentEvent evt) {
		this.numTicksToConverge = this.txtStability.getText();
	}

	private void txtPerfRangeSizeActionPerformed(DocumentEvent evt) {
		this.normsPerfRangeSize = this.txtPerfRangeSize.getText();
	}

	private void txtLearningFactorActionPerformed(DocumentEvent evt) {
		this.normEvaluationLearningRate = this.txtLearningFactor.getText();
	}

	private void txtDefUtilityActionPerformed(DocumentEvent evt) {
		this.normsDefaultUtility = this.txtDefUtility.getText();
	}

	private void txtDeliberativenessActionPerformed(DocumentEvent evt) {
		this.normsMinEvaluationsToDecide = this.txtDeliberativeness.getText();
	}

	private void txtReactivityGroupsActionPerformed(DocumentEvent evt) {
		this.normGroupsMinEvaluationsToDecide = this.txtDeliberativenessGroups.getText();
	}

	private void txtActEffActionPerformed(DocumentEvent evt) {
		this.normEffActThreshold = this.txtActEff.getText();
	}

	private void txtActNecActionPerformed(DocumentEvent evt) {
		this.normNecActThreshold = this.txtActNec.getText();
	}

	private void txtDeactEffActionPerformed(DocumentEvent evt) {
		this.normEffDeactThreshold = this.txtDeactEff.getText();
	}

	private void txtDeactNecActionPerformed(DocumentEvent evt) {
		this.normNecDeactThreshold = this.txtDeactNec.getText();
	}

	private void txtGenEffActionPerformed(DocumentEvent evt) {
		this.normEffGenThreshold = this.txtGenEff.getText();
	}

	private void txtGenNecActionPerformed(DocumentEvent evt) {
		this.normNecGenThreshold = this.txtGenNec.getText();
	}

	private void rbConservativeActionPerformed(ActionEvent evt) {
		this.normGeneralisationApproach = "0";
	}

	private void rbOptimisticActionPerformed(ActionEvent evt) {
		this.normGeneralisationApproach = "1";
	}

	private void rbShallowActionPerformed(ActionEvent evt) {
		this.optimisticNormGeneralisationMode = "0";		
	}

	private void rbDeepActionPerformed(ActionEvent evt) {
		this.optimisticNormGeneralisationMode = "1";
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(DialogSynthesisConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogSynthesisConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogSynthesisConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogSynthesisConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogSynthesisConfiguration dialog;
				try {

					dialog = new DialogSynthesisConfiguration(new javax.swing.JFrame(), 
							true, sim, configManager, strategy);
					dialog.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							System.exit(0);
						}
					});
					dialog.setVisible(true);

				} 
				catch (HeadlessException e1) {
					e1.printStackTrace();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		rbgCompactness = new javax.swing.ButtonGroup();
		rbgLiberality = new javax.swing.ButtonGroup();
		rbgGenApproach = new javax.swing.ButtonGroup();
		rbgGenMode = new javax.swing.ButtonGroup();
		rbgEvApproach = new javax.swing.ButtonGroup();
		rbgActApproach = new javax.swing.ButtonGroup();
		panelConfig = new javax.swing.JPanel();
		panelGeneralisation = new javax.swing.JPanel();
		lblGenApproach = new javax.swing.JLabel();
		rbConservative = new javax.swing.JRadioButton();
		rbOptimistic = new javax.swing.JRadioButton();
		lblGenMode = new javax.swing.JLabel();
		lblGenStep = new javax.swing.JLabel();
		rbShallow = new javax.swing.JRadioButton();
		rbDeep = new javax.swing.JRadioButton();
		txtGenStep = new JIntegerField(1);
		lblGenThresholds = new javax.swing.JLabel();
		txtGenEff = new JDecimalField(2);
		lblGenEff = new javax.swing.JLabel();
		lblGenNec = new javax.swing.JLabel();
		txtGenNec = new JDecimalField(2);
		panelEvaluation = new javax.swing.JPanel();
		lblEvMech = new javax.swing.JLabel();
		rbBB = new javax.swing.JRadioButton();
		rbMAvg = new javax.swing.JRadioButton();
		lblLearningFactor = new javax.swing.JLabel();
		txtLearningFactor = new JDecimalField(2);
		lblPerfRangSize = new javax.swing.JLabel();
		txtPerfRangeSize = new JIntegerField(1);
		lblDefUtility = new javax.swing.JLabel();
		txtDefUtility = new JDecimalField(2);
		btnSave = new javax.swing.JButton();
		btnExit = new javax.swing.JButton();
		lblTitle = new javax.swing.JLabel();
		panelGeneral = new javax.swing.JPanel();
		lblCompactness = new javax.swing.JLabel();
		rbCompactnessYes = new javax.swing.JRadioButton();
		rbCompactnessNo = new javax.swing.JRadioButton();
		lblLiberailty = new javax.swing.JLabel();
		rbLiberalityYes = new javax.swing.JRadioButton();
		rbLiberalityNo = new javax.swing.JRadioButton();
		lblStability = new javax.swing.JLabel();
		txtStability = new JIntegerField(1);
		panelActDeact = new javax.swing.JPanel();
		lblReactivity = new javax.swing.JLabel();
		txtDeliberativeness = new JIntegerField(0);
		lblActThresholds = new javax.swing.JLabel();
		lblDeactThresholds = new javax.swing.JLabel();
		lblReactivityGroups = new javax.swing.JLabel();
		txtDeliberativenessGroups = new JIntegerField(0);
		lblActApproach = new javax.swing.JLabel();
		rbReactive = new javax.swing.JRadioButton();
		rbDeliberative = new javax.swing.JRadioButton();
		lblActEff = new javax.swing.JLabel();
		txtActEff = new JDecimalField(2);
		lblDeactEff = new javax.swing.JLabel();
		txtDeactEff = new JDecimalField(2);
		lblActNec = new javax.swing.JLabel();
		txtActNec = new JDecimalField(2);
		lblDeactNec = new javax.swing.JLabel();
		txtDeactNec = new JDecimalField(2);
		lblInfo = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

		panelGeneralisation.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm generalisation settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		lblGenApproach.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenApproach.setText("Generalisation approach");
		lblGenApproach.setToolTipText("<html> \nTwo approaches to norm generalisation:<br>\n1. Optimistic. It detects pairs of norms that are generalisable, <br>\nand generalises them to their most specific generalisation. <br>\nThis is the generalisation approach of SIMON. <br>\n2. Conservative. It generalises a group of norms to a <br>\n(more general) norm only when all the norms represented <br>\nby that norm have been previously created, and are active.<br>\nThis is the generalisation approach of IRON. \n</html>");

		rbgGenApproach.add(rbConservative);
		rbConservative.setText("Conservative");
		rbConservative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbConservativeActionPerformed(evt);
			}
		});

		rbgGenApproach.add(rbOptimistic);
		rbOptimistic.setSelected(true);
		rbOptimistic.setText("Optimistic");
		rbOptimistic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbOptimisticActionPerformed(evt);
			}
		});

		lblGenMode.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenMode.setText("Generalisation mode");
		lblGenMode.setToolTipText("<html>\nDeep generalisation allows to pick two norms from the<br>\nnormative network, and to find two norms represented by <br>\nthem that are generalisable. Shallow generalisation picks two <br>\nactive norms, and directly generalises them to a parent norm.\n</html>");

		lblGenStep.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenStep.setText("Generalisation step");
		lblGenStep.setToolTipText("Number of terms in norms' predicates that can be simultaneously generalised");
		lblGenStep.setVerticalAlignment(javax.swing.SwingConstants.TOP);

		rbgGenMode.add(rbShallow);
		rbShallow.setText("Shallow");
		rbShallow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbShallowActionPerformed(evt);
			}
		});

		rbgGenMode.add(rbDeep);
		rbDeep.setSelected(true);
		rbDeep.setText("Deep");
		rbDeep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbDeepActionPerformed(evt);
			}
		});

		txtGenStep.setText("1");
		txtGenStep.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtGenStepActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtGenStepActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtGenStepActionPerformed(e);
			}
		});

		lblGenThresholds.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblGenThresholds.setText("Generalisation thresholds");
		lblGenThresholds.setToolTipText("<html> Threshold above which a norm is considered as effective <br>\n and necessary enough to be generalisable </html>");

		txtGenEff.setText("0.0");
		txtGenEff.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtGenEffActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtGenEffActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtGenEffActionPerformed(e);
			}
		});

		lblGenEff.setText("Effectiveness [0,1]");

		lblGenNec.setText("Necessity       [0,1]");

		txtGenNec.setText("0.0");
		txtGenNec.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtGenNecActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtGenNecActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtGenNecActionPerformed(e);
			}
		});

		javax.swing.GroupLayout panelGeneralisationLayout = new javax.swing.GroupLayout(panelGeneralisation);
		panelGeneralisation.setLayout(panelGeneralisationLayout);
		panelGeneralisationLayout.setHorizontalGroup(
				panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelGeneralisationLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblGenStep)
								.addGroup(panelGeneralisationLayout.createSequentialGroup()
										.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(lblGenApproach)
												.addComponent(lblGenMode))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(rbOptimistic)
														.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																.addComponent(txtGenStep, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
																.addComponent(rbDeep)))))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addComponent(rbShallow, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(rbConservative))
																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																				.addComponent(lblGenThresholds)
																				.addGroup(panelGeneralisationLayout.createSequentialGroup()
																						.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																								.addComponent(lblGenEff)
																								.addComponent(lblGenNec))
																								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																										.addComponent(txtGenNec, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
																										.addComponent(txtGenEff))))
																										.addContainerGap())
				);
		panelGeneralisationLayout.setVerticalGroup(
				panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelGeneralisationLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelGeneralisationLayout.createSequentialGroup()
										.addComponent(lblGenThresholds)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblGenEff)
												.addComponent(txtGenEff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(txtGenNec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(lblGenNec)))
														.addGroup(panelGeneralisationLayout.createSequentialGroup()
																.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(lblGenApproach)
																		.addComponent(rbConservative)
																		.addComponent(rbOptimistic))
																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(rbDeep)
																						.addComponent(lblGenMode))
																						.addComponent(rbShallow))
																						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																						.addGroup(panelGeneralisationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																								.addComponent(txtGenStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(lblGenStep, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
																								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelEvaluation.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Norm evaluation settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		lblEvMech.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblEvMech.setText("Evaluation mechanism");
		lblEvMech.setToolTipText("<html>\nThere are two approaches to evaluate norms:<br>\n"
				+ "1. Moving average. A norms' performance is computed at time t<br>"
				+ "\nas the moving average of the last q punctual evaluations up to time t. <br>"
				+ "\nThis approach is the employed by BASE, LION and DESMON. <br>\n"
				+ "2. Bollinger Bands. It cumulates norms' evaluations by means <br>\n"
				+ "of a reinforcement learning formula. Then, it cumulates such "
				+ "evaluations <br>\nin a q-moving average, and computes its "
				+ "Bollinger Bands. This approach is <br>\nthe one employed by"
				+ " IRON and SIMON.\n</html>");

		rbgEvApproach.add(rbBB);
		rbBB.setText("Bollinger Bands");
		rbBB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbBBActionPerformed(evt);
			}
		});

		rbgEvApproach.add(rbMAvg);
		rbMAvg.setSelected(true);
		rbMAvg.setText("Moving average");
		rbMAvg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbMAvgActionPerformed(evt);
			}
		});

		lblLearningFactor.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblLearningFactor.setText("Learning factor [0,1]");
		lblLearningFactor.setToolTipText("<html>\nThe learning factor (alpha value) employed to compute <br>\nnorms' performances by employing reinforcement learning.\n</html>");

		txtLearningFactor.setText("0.1");
		txtLearningFactor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtLearningFactorActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtLearningFactorActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtLearningFactorActionPerformed(e);
			}
		});

		lblPerfRangSize.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblPerfRangSize.setText("Performance range size");
		lblPerfRangSize.setToolTipText("The q value considered to compute moving averages of norms' performances. ");

		txtPerfRangeSize.setText("50");
		txtPerfRangeSize.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtPerfRangeSizeActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtPerfRangeSizeActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtPerfRangeSizeActionPerformed(e);
			}
		});

		lblDefUtility.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblDefUtility.setText("Norms' default utility [0,1]");
		lblDefUtility.setToolTipText("The default utility of each norm once created");

		txtDefUtility.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtDefUtilityActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtDefUtilityActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtDefUtilityActionPerformed(e);
			}
		});

		javax.swing.GroupLayout panelEvaluationLayout = new javax.swing.GroupLayout(panelEvaluation);
		panelEvaluation.setLayout(panelEvaluationLayout);
		panelEvaluationLayout.setHorizontalGroup(
				panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelEvaluationLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblEvMech)
								.addComponent(lblPerfRangSize))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelEvaluationLayout.createSequentialGroup()
												.addComponent(rbMAvg)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(rbBB))
												.addGroup(panelEvaluationLayout.createSequentialGroup()
														.addGap(4, 4, 4)
														.addComponent(txtPerfRangeSize, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																.addComponent(lblLearningFactor)
																.addComponent(lblDefUtility))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																		.addComponent(txtDefUtility, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
																		.addComponent(txtLearningFactor))
																		.addContainerGap())
				);
		panelEvaluationLayout.setVerticalGroup(
				panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelEvaluationLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblEvMech)
								.addComponent(rbMAvg)
								.addComponent(txtLearningFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(rbBB)
								.addComponent(lblLearningFactor))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(txtDefUtility, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblDefUtility))
												.addGroup(panelEvaluationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblPerfRangSize)
														.addComponent(txtPerfRangeSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addContainerGap())
				);

		btnSave.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/save.png")); // NOI18N
		btnSave.setText("Save and exit");
		btnSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSaveActionPerformed(evt);
			}
		});

		btnExit.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/exit.png"));
		btnExit.setText("Exit");
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExitActionPerformed(evt);
			}
		});

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 19)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/settings.png")); // NOI18N
		lblTitle.setText("NormLab: norm synthesis settings");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		panelGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "General settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		lblCompactness.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblCompactness.setText("Pursue compactness?");
		lblCompactness.setToolTipText("Should the norm synthesis machine perform norm generalisations to synthesise compact normative systems?");

		rbgCompactness.add(rbCompactnessYes);
		rbCompactnessYes.setSelected(true);
		rbCompactnessYes.setText("Yes");
		rbCompactnessYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbCompactnessYesActionPerformed(evt);
			}
		});

		rbgCompactness.add(rbCompactnessNo);
		rbCompactnessNo.setText("No");
		rbCompactnessNo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbCompactnessNoActionPerformed(evt);
			}
		});

		lblLiberailty.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblLiberailty.setText("Pursue liberality?");
		lblLiberailty.setToolTipText("Should the norm synthesis machine exploit norm synergies to detect and remove substitutability relationships?");

		rbgLiberality.add(rbLiberalityYes);
		rbLiberalityYes.setSelected(true);
		rbLiberalityYes.setText("Yes");
		rbLiberalityYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbLiberalityYesActionPerformed(evt);
			}
		});

		rbgLiberality.add(rbLiberalityNo);
		rbLiberalityNo.setText("No");
		rbLiberalityNo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rbLiberalityNoActionPerformed(evt);
			}
		});

		lblStability.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblStability.setText("<html> Number of ticks of normative system <br> stability to converge </html>");
		lblStability.setToolTipText("Number of simulation ticks during which a normative system is required to remain unchanged in order to converge");
		lblStability.setVerticalAlignment(javax.swing.SwingConstants.TOP);

		txtStability.setText("1000");
		txtStability.setToolTipText("");
		txtStability.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				txtStabilityActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtStabilityActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtStabilityActionPerformed(e);
			}
		});

		javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
		panelGeneral.setLayout(panelGeneralLayout);
		panelGeneralLayout.setHorizontalGroup(
				panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelGeneralLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblCompactness)
								.addComponent(lblLiberailty))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelGeneralLayout.createSequentialGroup()
												.addComponent(rbCompactnessYes)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(rbCompactnessNo))
												.addGroup(panelGeneralLayout.createSequentialGroup()
														.addComponent(rbLiberalityYes)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(rbLiberalityNo)))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(lblStability, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(txtStability, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addContainerGap())
				);
		panelGeneralLayout.setVerticalGroup(
				panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelGeneralLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addComponent(lblStability, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelGeneralLayout.createSequentialGroup()
										.addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblCompactness)
												.addComponent(rbCompactnessYes)
												.addComponent(rbCompactnessNo)
												.addComponent(txtStability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblLiberailty)
														.addComponent(rbLiberalityYes)
														.addComponent(rbLiberalityNo))))
														.addContainerGap())
				);

		panelActDeact.setBorder(javax.swing.BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				"Norm generation/activation/deactivation settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		lblReactivity.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblReactivity.setText("Deliberativeness degree");
		lblReactivity.setToolTipText("Number of evidences of a norm's "
				+ "performance considered to assess whether it can be"
				+ " activated or deactivated");

		txtDeliberativeness.setText("10");
		txtDeliberativeness.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtDeliberativenessActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtDeliberativenessActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtDeliberativenessActionPerformed(e);
			}
		});

//		txtActEff.setText("0.0");
		txtActEff.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtActEffActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtActEffActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtActEffActionPerformed(e);
			}
		});

		lblActThresholds.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblActThresholds.setText("Activation thresholds");
		lblActThresholds.setToolTipText("<html>\nThreshold above which a norm "
				+ "is considered as effective <br>\nand necessary enough "
				+ "to be part of the normative system\n</html>");

		txtActNec.setText("0.0");
		txtActNec.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtActNecActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtActNecActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtActNecActionPerformed(e);
			}
		});

		lblActEff.setText("Effectiveness [0,1]");

		lblActNec.setText("Necessity       [0,1]");

//		txtDeactEff.setText("0.0");
		txtDeactEff.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtDeactEffActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtDeactEffActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtDeactEffActionPerformed(e);
			}
		});

		lblDeactThresholds.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblDeactThresholds.setText("Deactivation thresholds");
		lblDeactThresholds.setToolTipText("<html> Threshold below which a"
				+ " norm is considered as not effective <br>\nor necessary "
				+ "enough to be part of the normative system </html>");

//		txtDeactNec.setText("0.0");
		txtDeactNec.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtDeactNecActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtDeactNecActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtDeactNecActionPerformed(e);
			}
		});

		lblDeactEff.setText("Effectiveness [0,1]");
		lblDeactNec.setText("Necessity       [0,1]");

		lblReactivityGroups.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblReactivityGroups.setText("<html>Deliberativeness degree <br>\n(norm pairs)\n</html>");
		lblReactivityGroups.setToolTipText("Number of evidences of a norm group's"
				+ " performance required to detect synergies between a pair of norms");

		txtDeliberativenessGroups.setText("1");
		txtDeliberativenessGroups.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtReactivityGroupsActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtReactivityGroupsActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtReactivityGroupsActionPerformed(e);
			}
		});

		lblActApproach.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblActApproach.setText("Generation approach");
		lblActApproach.setToolTipText("<html>\nApproach considered when deciding whether to activate created norms.<br>\nIn reactivate generation, norms are activated right after being created.<br>\nIn deliberative generation, norms are activated only when they have been <br>\nevaluated a minimum amount of times, and there is enough evidence to<br>\nassess whether they perform well. \n</html>");

		rbgActApproach.add(rbReactive);
		rbReactive.setSelected(true);
		rbReactive.setText("Reactive");
		rbReactive.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbReactiveActionPerformed(evt);
			}
		});

		rbgActApproach.add(rbDeliberative);
		rbDeliberative.setText("Deliberative");
		rbDeliberative.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbDeliberativeActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelActDeactLayout = new javax.swing.GroupLayout(panelActDeact);
		panelActDeact.setLayout(panelActDeactLayout);
		panelActDeactLayout.setHorizontalGroup(
				panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelActDeactLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelActDeactLayout.createSequentialGroup()
										.addComponent(lblActApproach)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(rbReactive)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(rbDeliberative))
										.addGroup(panelActDeactLayout.createSequentialGroup()
												.addComponent(lblReactivity)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(txtDeliberativeness, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(panelActDeactLayout.createSequentialGroup()
														.addComponent(lblReactivityGroups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(txtDeliberativenessGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
														.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																.addComponent(lblActThresholds)
																.addGroup(panelActDeactLayout.createSequentialGroup()
																		.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
																				.addComponent(lblActEff)
																				.addComponent(lblActNec))
																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																				.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																						.addComponent(txtActEff)
																						.addComponent(txtActNec, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))))
																						.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																								.addGroup(panelActDeactLayout.createSequentialGroup()
																										.addGap(19, 19, 19)
																										.addComponent(lblDeactThresholds))
																										.addGroup(panelActDeactLayout.createSequentialGroup()
																												.addGap(18, 18, 18)
																												.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(lblDeactEff)
																														.addComponent(lblDeactNec))
																														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																														.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																																.addComponent(txtDeactEff, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
																																.addComponent(txtDeactNec))))
																																.addContainerGap())
				);
		panelActDeactLayout.setVerticalGroup(
				panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelActDeactLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblActApproach)
								.addComponent(rbReactive)
								.addComponent(rbDeliberative)
								.addComponent(lblActThresholds)
								.addComponent(lblDeactThresholds))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblReactivity)
										.addComponent(txtDeliberativeness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(lblActEff)
										.addComponent(txtActEff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(lblDeactEff)
										.addComponent(txtDeactEff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(3, 3, 3)
										.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(txtDeliberativenessGroups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblReactivityGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGroup(panelActDeactLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblActNec)
														.addComponent(txtActNec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(txtDeactNec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(lblDeactNec)))
														.addContainerGap())
				);


		lblInfo.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/info.png")); // NOI18N
		lblInfo.setText("Hold mouse pointer on each parameter to show more details about it");

		javax.swing.GroupLayout panelConfigLayout = new javax.swing.GroupLayout(panelConfig);
		panelConfig.setLayout(panelConfigLayout);
		panelConfigLayout.setHorizontalGroup(
				panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelConfigLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelGeneralisation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(panelConfigLayout.createSequentialGroup()
										.addComponent(btnExit)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnSave))
										.addGroup(panelConfigLayout.createSequentialGroup()
												.addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
														.addComponent(lblInfo, javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(panelActDeact, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(panelEvaluation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(panelGeneral, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addGap(0, 0, Short.MAX_VALUE)))
														.addContainerGap())
				);
		panelConfigLayout.setVerticalGroup(
				panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelConfigLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle)
						.addGap(18, 18, 18)
						.addComponent(lblInfo)
						.addGap(18, 18, 18)
						.addComponent(panelGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelEvaluation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelActDeact, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelGeneralisation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnExit))
								.addContainerGap())
				);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(panelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(panelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				);

		pack();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnSave;
	private javax.swing.JButton btnExit;
	private javax.swing.JLabel lblActApproach;
	private javax.swing.JLabel lblActEff;
	private javax.swing.JLabel lblActNec;
	private javax.swing.JLabel lblActThresholds;
	private javax.swing.JLabel lblCompactness;
	private javax.swing.JLabel lblDeactEff;
	private javax.swing.JLabel lblDeactNec;
	private javax.swing.JLabel lblDeactThresholds;
	private javax.swing.JLabel lblDefUtility;
	private javax.swing.JLabel lblEvMech;
	private javax.swing.JLabel lblGenApproach;
	private javax.swing.JLabel lblGenEff;
	private javax.swing.JLabel lblGenMode;
	private javax.swing.JLabel lblGenNec;
	private javax.swing.JLabel lblGenStep;
	private javax.swing.JLabel lblGenThresholds;
	private javax.swing.JLabel lblInfo;
	private javax.swing.JLabel lblLearningFactor;
	private javax.swing.JLabel lblLiberailty;
	private javax.swing.JLabel lblPerfRangSize;
	private javax.swing.JLabel lblReactivity;
	private javax.swing.JLabel lblReactivityGroups;
	private javax.swing.JLabel lblStability;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JPanel panelActDeact;
	private javax.swing.JPanel panelConfig;
	private javax.swing.JPanel panelEvaluation;
	private javax.swing.JPanel panelGeneral;
	private javax.swing.JPanel panelGeneralisation;
	private javax.swing.JRadioButton rbCompactnessNo;
	private javax.swing.JRadioButton rbCompactnessYes;
	private javax.swing.JRadioButton rbConservative;
	private javax.swing.JRadioButton rbDeep;
	private javax.swing.JRadioButton rbDeliberative;
	private javax.swing.JRadioButton rbLiberalityNo;
	private javax.swing.JRadioButton rbLiberalityYes;
	private javax.swing.JRadioButton rbMAvg;
	private javax.swing.JRadioButton rbOptimistic;
	private javax.swing.JRadioButton rbBB;
	private javax.swing.JRadioButton rbReactive;
	private javax.swing.JRadioButton rbShallow;
	private javax.swing.ButtonGroup rbgActApproach;
	private javax.swing.ButtonGroup rbgCompactness;
	private javax.swing.ButtonGroup rbgEvApproach;
	private javax.swing.ButtonGroup rbgGenApproach;
	private javax.swing.ButtonGroup rbgGenMode;
	private javax.swing.ButtonGroup rbgLiberality;
	private JDecimalField txtActEff;
	private JDecimalField txtActNec;
	private JDecimalField txtDeactEff;
	private JDecimalField txtDeactNec;
	private JDecimalField txtDefUtility;
	private JDecimalField txtGenEff;
	private JDecimalField txtGenNec;
	private JIntegerField txtGenStep;
	private JDecimalField txtLearningFactor;
	private JIntegerField txtPerfRangeSize;
	private JIntegerField txtDeliberativeness;
	private JIntegerField txtDeliberativenessGroups;
	private JIntegerField txtStability;
	// End of variables declaration//GEN-END:variables
}
