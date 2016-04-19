/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.launcher.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import es.csic.iiia.normlab.launcher.model.NormLabExperimentsManager;
import es.csic.iiia.normlab.launcher.model.RepastXMLManager;
import es.csic.iiia.normlab.launcher.ui.NormLabFrame.NormLabSimulator;
import es.csic.iiia.normlab.launcher.utils.JIntegerField;
import es.csic.iiia.normlab.launcher.utils.SingleRootFileSystemView;

/**
 *
 * @author Javi
 */
public class DialogExperimentCreator extends javax.swing.JDialog {

	private static final long serialVersionUID = -413376329275890120L;

	private String userStrategyDir = "src" + File.separator + "user";
	
	private NormLabExperimentsManager expManager;
	private NormLabSimulator simulator;
	private Frame parent;
	private int strategy;

	/**
	 * Creates new form NormLabExperimentCreatorDialog
	 * @throws IOException 
	 */
	public DialogExperimentCreator(Frame parent, boolean modal) {

		super(parent, modal);

		initComponents();
		setResizable(false);
		
		this.simulator = NormLabSimulator.TrafficJunction;
		this.parent = parent;
		this.strategy = 2;

		/* Create new experiments manager and invoke its method 
		 * to create new experiment files */
		try {
			this.expManager = new NormLabExperimentsManager();
	    this.expManager.createNewExperimentFiles();
    }
		
		/* Catch exception and show error message */
		catch (IOException e) {
    	this.errorMsg("Error while creating new experiment files", 
    			e.getMessage());
    }
	}

	/**
	 * Creates new form NormLabExperimentCreatorDialog
	 * @throws IOException 
	 */
	public DialogExperimentCreator(Frame parent, boolean modal,
			String experimentName) {

		/* Invoke first constructor to create the Dialog */
		this(parent, modal);

		/* Load configuration of the experiment received by parameter */
		try{
			this.loadConfiguration(experimentName);
		}
		
		/* Catch exception and show error message */
		catch (Exception e) {
    	this.errorMsg("Error while loading experiment files", 
    			e.getMessage());
    }
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	private void loadExperiment() throws Exception {
		File expToLoad = this.getExperimentToLoad();

		if(expToLoad != null) {
			this.loadConfiguration(expToLoad.getName());
		}
	}

	/**
	 * 
	 * @param expLoadedFile2
	 * @throws Exception 
	 */
	private void loadConfiguration(String experimentFilename) throws Exception {

		/* Load experiment configuration */
		this.expManager.loadExperimentConfiguration(experimentFilename);

		RepastXMLManager confManager = 
				this.expManager.getExperimentConfigManager();

		/* Load configuration attributes */
		String name = confManager.getAttribute("name");
		String sim = confManager.getAttribute("simulator");
		String strategy = confManager.getAttribute("strategy");
		String numExecs = confManager.getAttribute("simulations");
		String computeAvgs = confManager.getAttribute("computeAvgs");
		String computeMeans = confManager.getAttribute("computeMeans");
		String createCharts = confManager.getAttribute("createCharts");

		this.txtExperimentName.setText(name);
		this.txtNumSimulations.setText(numExecs);
		this.cbCompAvg.setSelected(computeAvgs.equals("true"));
		this.cbCompMeans.setSelected(computeMeans.equals("true"));
		this.cbGenCharts.setSelected(createCharts.equals("true"));

		/* Load simulator */
		this.btnGroupChooseSim.clearSelection();
		if(sim.equals("TrafficJunction")) {
			this.simulator = NormLabSimulator.TrafficJunction;
			this.rbTrafficSim.setSelected(true);
		}
		else if(sim.equals("OnlineCommunity")) {
			this.simulator = NormLabSimulator.OnlineCommunity;
			this.rbOnlineCommSim.setSelected(true);
		}
		else {
			throw new Exception("The configuration file does not "
					+ "contain a valid simulator");
		}

		/* Load strategy */
		this.strategy = Integer.valueOf(strategy);
		this.selectStrategyButtonById(this.strategy);
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	private void saveExperiment() throws Exception {

		/* Retrieve experiment information */
		String name = this.txtExperimentName.getText();
		String sim = this.simulator.toString();
		String strategy = String.valueOf(this.strategy);
		String numSims = this.txtNumSimulations.getText();
		String computeAvg = String.valueOf(this.cbCompAvg.isSelected());
		String computeMeans = String.valueOf(this.cbCompMeans.isSelected());
		String createCharts = String.valueOf(this.cbGenCharts.isSelected());

		/* Create and retrieve a new experiment configuration */
		this.expManager.createNewExperimentConfiguration(name);
		
		RepastXMLManager expConfigManager = 
				this.expManager.getExperimentConfigManager();
		
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		numSims = format.parse(numSims).toString();
		
		/* Add simulator's parameters configuration */
		expConfigManager.addAttribute("name", name);
		expConfigManager.addAttribute("simulator", sim);
		expConfigManager.addAttribute("strategy", strategy);
		expConfigManager.addAttribute("simulations", numSims);
		expConfigManager.addAttribute("computeAvgs", computeAvg);
		expConfigManager.addAttribute("computeMeans", computeMeans);
		expConfigManager.addAttribute("createCharts", createCharts);
		
		/* Set synthesis strategy in the parameters file */
		RepastXMLManager paramsManager = 
				this.expManager.getParamsManager(simulator);
		
		paramsManager.setAttribute("normSynthesisStrategy", strategy);
		
		/* Save experiment configuration */
		this.expManager.saveExperimentConfiguration(name);
	}
	
	/**
	 * 
	 * @return
	 */
	private File getExperimentToLoad() {
		File expToLoad = null;

		String avExpPath = System.getProperty("user.dir") + 
				File.separator + "experiments" + File.separator + "available";

		File avExpFolder = new File(avExpPath);
		FileSystemView fsv = new SingleRootFileSystemView(avExpFolder);

		JFileChooser chooser = new JFileChooser(fsv);
		chooser.setDialogTitle("Select the xml file of the experiment to load");

		chooser.setToolTipText("Select the xml file containing the configuration "
				+ "of the experiment you want to load");

		FileNameExtensionFilter filter = 
				new FileNameExtensionFilter("xml files (*.xml)", "xml");

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			expToLoad = chooser.getSelectedFile();
		}
		return expToLoad;
	}
	
	/**
	 * 
	 * @return
	 */
	private File getUserStrategy() {
		File strategyFile = null;

		String userStrDir = System.getProperty("user.dir") + 
				File.separator + "src" + File.separator + "user";

		File userStrFolder = new File(userStrDir);
		FileSystemView fsv = new SingleRootFileSystemView(userStrFolder);

		JFileChooser chooser = new JFileChooser(fsv);
		chooser.setDialogTitle("Select the java file that implements "
				+ "your norm synthesis strategy");

		chooser.setToolTipText("Select the java file of your norm synthesis "
				+ "strategy. Recall that your strategy must implement the interface"
				+ "es.csic.iiia.nsm.NormSynthesisMachine.");

		FileNameExtensionFilter filter = 
				new FileNameExtensionFilter("java files (*.java)", "java");

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			strategyFile = chooser.getSelectedFile();
		}
		return strategyFile;	  
	}

	/**
	 * Shows an error message 
	 * 
	 * @param title
	 * @param msg
	 */
	private void errorMsg(String title, String msg) {
		JOptionPane optionPane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE);
		JDialog dialog = optionPane.createDialog(title);
		dialog.setLocationRelativeTo(this);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);	  
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	private String getClassName(File file) {
		String path = file.getAbsolutePath();
		int idx = path.indexOf(userStrategyDir);
		int length = userStrategyDir.length();
		
		String className = path.substring(idx + length + 1);
		className = className.replaceAll("/", ".");
		className = className.replaceAll("\\\\", ".");
		className = className.replaceAll(".java", "");
		return className;
	}

	/**
	 * 
	 * @return
	 */
	private JRadioButton getStrategyButtonById(int id) {
		switch(id) {
		case 1: 	return this.rbUserStrategy;
		case 2: 	return this.rbGenericStrategy;
		case 3: 	return this.rbBASEStrategy;
		case 4: 	return this.rbIRONStrategy;
		case 5: 	return this.rbSIMONStrategy;
		case 6: 	return this.rbLIONStrategy;
		case 7: 	return this.rbDESMONStrategy;
		default: 	return null;
		}
	}

	/**
	 * 
	 * @param valueOf
	 */
	private void selectStrategyButtonById(Integer id) {
		this.btnGroupChooseStrategy.clearSelection();

		switch(id) {
		case 1: 	this.rbUserStrategy.setSelected(true);	break;
		case 2: 	this.rbGenericStrategy.setSelected(true);	break;
		case 3: 	this.rbBASEStrategy.setSelected(true);		break;
		case 4: 	this.rbIRONStrategy.setSelected(true);		break;
		case 5: 	this.rbSIMONStrategy.setSelected(true);		break;
		case 6: 	this.rbLIONStrategy.setSelected(true);		break;
		case 7: 	this.rbDESMONStrategy.setSelected(true);	break;
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbUserStrategyActionPerformed(ActionEvent evt) {
		JRadioButton pSelectedButton = this.getStrategyButtonById(this.strategy);

		/* Ask for the strategy file */
		File strategyFile = this.getUserStrategy();

		/* A strategy has been provided */
		if(strategyFile != null) {
			String className = this.getClassName(strategyFile);

			try {
				RepastXMLManager paramsManager = 
						this.expManager.getParamsManager(simulator);

				/* Configure user strategy name */
				this.strategy = 1;
				
				paramsManager.setAttribute("normSynthesisStrategy", String.valueOf(strategy));
				paramsManager.setAttribute("userStrategyCanonicalName", className);
			}

			/* Exception. Show error message */
			catch (Exception e) {
				this.errorMsg("Error while retrieving self-made strategy", e.getMessage());
			}
		}
		
		/* No strategy has been provided */
		else {
			this.btnGroupChooseStrategy.clearSelection();
			pSelectedButton.setSelected(true);
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbGenericStrategyActionPerformed(ActionEvent evt) {
		this.strategy = 2;
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbBASEStrategyActionPerformed(ActionEvent evt) {
		this.strategy = 3;
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbIRONStrategyActionPerformed(ActionEvent evt) {
		this.strategy = 4;
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbSIMONStrategyActionPerformed(ActionEvent evt) {
		this.strategy = 5;
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbLIONStrategyActionPerformed(ActionEvent evt) {
		this.strategy = 6;
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbDESMONStrategyActionPerformed(ActionEvent evt) {
		this.strategy = 7;
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbTrafficSimActionPerformed(java.awt.event.ActionEvent evt) {   
		this.simulator = NormLabSimulator.TrafficJunction;

		if(this.rbUserStrategy.isSelected()) {
			this.btnGroupChooseStrategy.clearSelection();
			this.rbGenericStrategy.setSelected(true);
			this.strategy = 2;
		}
	}                                            

	/**
	 * 
	 * @param evt
	 */
	private void rbOnlineCommSimActionPerformed(java.awt.event.ActionEvent evt) {                                                
		this.simulator = NormLabSimulator.OnlineCommunity;

		if(this.rbUserStrategy.isSelected()) {
			this.btnGroupChooseStrategy.clearSelection();
			this.rbGenericStrategy.setSelected(true);
			this.strategy = 2;
		}
	}   

	/**
	 * 
	 * @param evt
	 */
	private void btnConfigureSimActionPerformed(java.awt.event.ActionEvent evt) {       
		JDialog dialog = null;

		/* Get params manager of the given simulator */
		RepastXMLManager paramsManager = 
				this.expManager.getParamsManager(simulator);

		/* Create corresponding configuration dialog */
		try {
			if(simulator == NormLabSimulator.TrafficJunction) {
				dialog = new DialogSimTrafficConfiguration(this.parent, 
						true, paramsManager);
			}
			else if(simulator == NormLabSimulator.OnlineCommunity) {
				dialog = new DialogSimOnlineComConfiguration(this.parent, 
						true, paramsManager);
			}

			/* Show dialog */
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}

		/* Exception. Show error message */
		catch (Exception e) {
			this.errorMsg("Error saving settings", e.getMessage());
			e.printStackTrace();
		}
	}                                               

	/**
	 * 
	 * @param evt
	 */
	private void btnConfigureSynthesisActionPerformed(java.awt.event.ActionEvent evt) {                                                      

		/* Get params manager of the given simulator */
		RepastXMLManager paramsManager = 
				this.expManager.getParamsManager(simulator);

		/* Create configuration dialog */
		try {
			DialogSynthesisConfiguration dialog = 
					new DialogSynthesisConfiguration(this.parent, true, 
							simulator, paramsManager, strategy);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}

		/* Exception. Show error message */
		catch (Exception e) {
			this.errorMsg("Error saving settings", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
		try {
			this.loadExperiment();
		}
		catch (Exception e) {
			this.errorMsg("Error while loading experiment configuration", e.getMessage());
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		try {
			this.saveExperiment();
			this.dispose();
		}
		catch (Exception e) {
			this.errorMsg("Error while creating experiment", e.getMessage());
		}
	}
	
	private void cbAppendDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAppendDateActionPerformed
		// TODO add your handling code here:
	}

	private void cbAppendTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAppendTimeActionPerformed
		// TODO add your handling code here:
	}

	private void txtExperimentNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtExperimentNameActionPerformed
		// TODO add your handling code here:
	}

	private void txtNumSimulationsActionPerformed(java.awt.event.ActionEvent evt) {                                                  
		// TODO add your handling code here:
	}                                                 

	private void cbCompAvgActionPerformed(java.awt.event.ActionEvent evt) {                                          
		// TODO add your handling code here:
	}                                         

	private void cbGenChartsActionPerformed(java.awt.event.ActionEvent evt) {                                            
		// TODO add your handling code here:
	}                                           

	private void cbCompMeansActionPerformed(java.awt.event.ActionEvent evt) {                                            
		// TODO add your handling code here:
	}  

	private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
		this.dispose();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		btnGroupChooseSim = new javax.swing.ButtonGroup();
		btnGroupChooseStrategy = new javax.swing.ButtonGroup();
		lblTitle = new javax.swing.JLabel();
		btnExit = new javax.swing.JButton();
		lblExperimentName = new javax.swing.JLabel();
		txtExperimentName = new javax.swing.JTextField();
		cbAppendDate = new javax.swing.JCheckBox();
		cbAppendTime = new javax.swing.JCheckBox();
		panelChoose = new javax.swing.JPanel();
		rbGenericStrategy = new javax.swing.JRadioButton();
		rbBASEStrategy = new javax.swing.JRadioButton();
		rbIRONStrategy = new javax.swing.JRadioButton();
		rbSIMONStrategy = new javax.swing.JRadioButton();
		rbLIONStrategy = new javax.swing.JRadioButton();
		rbDESMONStrategy = new javax.swing.JRadioButton();
		rbUserStrategy = new javax.swing.JRadioButton();
		panelConfigureStrategy = new javax.swing.JPanel();
		btnConfigureSim = new javax.swing.JButton();
		btnConfigureSynthesis = new javax.swing.JButton();
		panelChooseSim = new javax.swing.JPanel();
		rbOnlineCommSim = new javax.swing.JRadioButton();
		lblOnlineCommSim = new javax.swing.JLabel();
		lblTrafficSim = new javax.swing.JLabel();
		rbTrafficSim = new javax.swing.JRadioButton();
		btnSave = new javax.swing.JButton();
		panelConfigure = new javax.swing.JPanel();
		txtNumSimulations = new JIntegerField(1);
		lblNumSimulations = new javax.swing.JLabel();
		cbCompAvg = new javax.swing.JCheckBox();
		cbGenCharts = new javax.swing.JCheckBox();
		cbCompMeans = new javax.swing.JCheckBox();
		btnLoad = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/experiments.png")); // NOI18N
		lblTitle.setText("NormLab experiment creator");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		btnExit.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/exit.png")); // NOI18N
		btnExit.setText("Exit");
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExitActionPerformed(evt);
			}
		});

		lblExperimentName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
		lblExperimentName.setText("Experiment name");

		txtExperimentName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		txtExperimentName.setText("MyExperiment");
		txtExperimentName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				txtExperimentNameActionPerformed(evt);
			}
		});

		cbAppendDate.setText("Append date");
		cbAppendDate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbAppendDateActionPerformed(evt);
			}
		});

		cbAppendTime.setText("Append time");
		cbAppendTime.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbAppendTimeActionPerformed(evt);
			}
		});

		panelChoose.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "2. Choose a norm synthesis strategy", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnGroupChooseStrategy.add(rbGenericStrategy);
		rbGenericStrategy.setSelected(true);
		rbGenericStrategy.setText("Custom (a norm synthesis strategy fully configured by you)");
		rbGenericStrategy.setActionCommand("rbGenericStrategy");
		rbGenericStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbGenericStrategyActionPerformed(evt);
			}
		});

		btnGroupChooseStrategy.add(rbBASEStrategy);
		rbBASEStrategy.setText("BASE (Basic norm synthesis strategy)");
		rbBASEStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbBASEStrategyActionPerformed(evt);
			}
		});

		btnGroupChooseStrategy.add(rbIRONStrategy);
		rbIRONStrategy.setText("IRON (Intelligent Robust On-line Norm synthesis)");
		rbIRONStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbIRONStrategyActionPerformed(evt);
			}
		});

		btnGroupChooseStrategy.add(rbSIMONStrategy);
		rbSIMONStrategy.setText("SIMON (SImple Minimal On-line Norm synthesis");
		rbSIMONStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbSIMONStrategyActionPerformed(evt);
			}
		});

		btnGroupChooseStrategy.add(rbLIONStrategy);
		rbLIONStrategy.setText("LION (LIberal On-line Norm synthesis)");
		rbLIONStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbLIONStrategyActionPerformed(evt);
			}
		});

		btnGroupChooseStrategy.add(rbDESMONStrategy);
		rbDESMONStrategy.setText("DESMON (DEliberative Simple Minimal On-line Norm synthesis)");
		rbDESMONStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbDESMONStrategyActionPerformed(evt);
			}
		});

		btnGroupChooseStrategy.add(rbUserStrategy);
		rbUserStrategy.setText("Self-made (a norm synthesis strategy developed by yourself)");
		rbUserStrategy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbUserStrategyActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelChooseLayout = new javax.swing.GroupLayout(panelChoose);
		panelChoose.setLayout(panelChooseLayout);
		panelChooseLayout.setHorizontalGroup(
				panelChooseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelChooseLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelChooseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(rbGenericStrategy)
								.addComponent(rbBASEStrategy)
								.addComponent(rbIRONStrategy)
								.addComponent(rbLIONStrategy)
								.addComponent(rbSIMONStrategy)
								.addComponent(rbDESMONStrategy)
								.addComponent(rbUserStrategy))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelChooseLayout.setVerticalGroup(
				panelChooseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelChooseLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(rbUserStrategy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(rbGenericStrategy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(rbBASEStrategy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(rbIRONStrategy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(rbSIMONStrategy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(rbLIONStrategy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(rbDESMONStrategy)
						.addContainerGap())
				);

		panelConfigureStrategy.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "3. Configure your simulation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnConfigureSim.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/sim.png")); // NOI18N
		btnConfigureSim.setText("Simulator settings");
		btnConfigureSim.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnConfigureSimActionPerformed(evt);
			}
		});

		btnConfigureSynthesis.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/configuration.png")); // NOI18N
		btnConfigureSynthesis.setText("Synthesis settings");
		btnConfigureSynthesis.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnConfigureSynthesisActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelConfigureStrategyLayout = new javax.swing.GroupLayout(panelConfigureStrategy);
		panelConfigureStrategy.setLayout(panelConfigureStrategyLayout);
		panelConfigureStrategyLayout.setHorizontalGroup(
				panelConfigureStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelConfigureStrategyLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelConfigureStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(btnConfigureSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnConfigureSynthesis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
				);
		panelConfigureStrategyLayout.setVerticalGroup(
				panelConfigureStrategyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelConfigureStrategyLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnConfigureSim, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(btnConfigureSynthesis, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelChooseSim.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "1. Choose a simulator", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
		panelChooseSim.setForeground(new java.awt.Color(0, 102, 153));

		btnGroupChooseSim.add(rbOnlineCommSim);
		rbOnlineCommSim.setText("Online Community simulator");
		rbOnlineCommSim.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbOnlineCommSimActionPerformed(evt);
			}
		});

		lblOnlineCommSim.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		lblOnlineCommSim.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/onlinecomm.png")); // NOI18N

		lblTrafficSim.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		lblTrafficSim.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/collision.png")); // NOI18N

		btnGroupChooseSim.add(rbTrafficSim);
		rbTrafficSim.setSelected(true);
		rbTrafficSim.setText("Traffic junction simulator");
		rbTrafficSim.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbTrafficSimActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelChooseSimLayout = new javax.swing.GroupLayout(panelChooseSim);
		panelChooseSim.setLayout(panelChooseSimLayout);
		panelChooseSimLayout.setHorizontalGroup(
				panelChooseSimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelChooseSimLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelChooseSimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addGroup(panelChooseSimLayout.createSequentialGroup()
										.addGap(21, 21, 21)
										.addComponent(lblTrafficSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addComponent(rbTrafficSim))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(panelChooseSimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addGroup(panelChooseSimLayout.createSequentialGroup()
														.addGap(21, 21, 21)
														.addComponent(lblOnlineCommSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addComponent(rbOnlineCommSim))
														.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelChooseSimLayout.setVerticalGroup(
				panelChooseSimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelChooseSimLayout.createSequentialGroup()
						.addGap(9, 9, 9)
						.addGroup(panelChooseSimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(rbTrafficSim)
								.addComponent(rbOnlineCommSim))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelChooseSimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelChooseSimLayout.createSequentialGroup()
												.addComponent(lblOnlineCommSim)
												.addGap(0, 0, Short.MAX_VALUE))
												.addComponent(lblTrafficSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addContainerGap())
				);

		btnSave.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/save.png")); // NOI18N
		btnSave.setText("Save and exit");
		btnSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSaveActionPerformed(evt);
			}
		});

		panelConfigure.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "4. Configure your experiment", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		txtNumSimulations.setHorizontalAlignment(javax.swing.JTextField.LEFT);
		txtNumSimulations.setText("10");
		txtNumSimulations.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				txtNumSimulationsActionPerformed(evt);
			}
		});

		lblNumSimulations.setText("Number of simulations");

		cbCompAvg.setText("Compute averages");
		cbCompAvg.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbCompAvgActionPerformed(evt);
			}
		});

		cbGenCharts.setText("Generate charts");
		cbGenCharts.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbGenChartsActionPerformed(evt);
			}
		});
		cbGenCharts.setEnabled(false);
		
		cbCompMeans.setText("Compute means");
		cbCompMeans.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbCompMeansActionPerformed(evt);
			}
		});
		cbCompMeans.setEnabled(false);

		javax.swing.GroupLayout panelConfigureLayout = new javax.swing.GroupLayout(panelConfigure);
		panelConfigure.setLayout(panelConfigureLayout);
		panelConfigureLayout.setHorizontalGroup(
				panelConfigureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelConfigureLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelConfigureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(cbCompMeans)
								.addComponent(cbGenCharts)
								.addComponent(lblNumSimulations)
								.addComponent(cbCompAvg)
								.addComponent(txtNumSimulations, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelConfigureLayout.setVerticalGroup(
				panelConfigureLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelConfigureLayout.createSequentialGroup()
						.addGap(15, 15, 15)
						.addComponent(lblNumSimulations)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(txtNumSimulations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(cbCompAvg)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(cbCompMeans)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(cbGenCharts)
						.addContainerGap(10, Short.MAX_VALUE))
				);

		btnLoad.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/load_small.png")); // NOI18N
		btnLoad.setText("Load experiment");
		btnLoad.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnLoadActionPerformed(evt);
			}
		});

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelChoose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelChooseSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelConfigureStrategy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelConfigure, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnExit)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSave))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblExperimentName)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(txtExperimentName, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(btnLoad)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txtExperimentName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblExperimentName)
                .addComponent(btnLoad))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(panelChooseSim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(panelChoose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(panelConfigureStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(panelConfigure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnSave)
                .addComponent(btnExit))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

		pack();
	}// </editor-fold>//GEN-END:initComponents


	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentCreator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogExperimentCreator dialog = new DialogExperimentCreator(new javax.swing.JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnConfigureSim;
	private javax.swing.JButton btnConfigureSynthesis;
	private javax.swing.JButton btnExit;
	private javax.swing.ButtonGroup btnGroupChooseSim;
	private javax.swing.ButtonGroup btnGroupChooseStrategy;
	private javax.swing.JButton btnLoad;
	private javax.swing.JButton btnSave;
	private javax.swing.JCheckBox cbAppendDate;
	private javax.swing.JCheckBox cbAppendTime;
	private javax.swing.JCheckBox cbCompAvg;
	private javax.swing.JCheckBox cbCompMeans;
	private javax.swing.JCheckBox cbGenCharts;
	private javax.swing.JLabel lblExperimentName;
	private javax.swing.JLabel lblNumSimulations;
	private javax.swing.JLabel lblOnlineCommSim;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblTrafficSim;
	private javax.swing.JPanel panelChoose;
	private javax.swing.JPanel panelChooseSim;
	private javax.swing.JPanel panelConfigure;
	private javax.swing.JPanel panelConfigureStrategy;
	private javax.swing.JRadioButton rbBASEStrategy;
	private javax.swing.JRadioButton rbUserStrategy;
	private javax.swing.JRadioButton rbDESMONStrategy;
	private javax.swing.JRadioButton rbGenericStrategy;
	private javax.swing.JRadioButton rbIRONStrategy;
	private javax.swing.JRadioButton rbLIONStrategy;
	private javax.swing.JRadioButton rbOnlineCommSim;
	private javax.swing.JRadioButton rbSIMONStrategy;
	private javax.swing.JRadioButton rbTrafficSim;
	private javax.swing.JTextField txtExperimentName;
	private JIntegerField txtNumSimulations;
	// End of variables declaration//GEN-END:variables
}
