/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.launcher.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import edu.stanford.ejalbert.BrowserLauncher;
import es.csic.iiia.normlab.launcher.NormLabSimulationLauncher;
import es.csic.iiia.normlab.launcher.model.RepastXMLManager;
import es.csic.iiia.normlab.launcher.utils.SingleRootFileSystemView;

/**
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class NormLabFrame extends javax.swing.JFrame {

	//---------------------------------------------------------------------------
	// Enumerations
	//---------------------------------------------------------------------------

	public enum NormLabSimulator {
		TrafficJunction, OnlineCommunity;
	}

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	/* Generated serial ID */
	private static final long serialVersionUID = 4908309231752858644L;

	private String userStrategyDir = "src" + File.separator + "user";
	private String trafficParamsDir = "repast-settings/TrafficJunction.rs/";
	private String onlinecommParamsDir = "repast-settings/OnlineCommunity.rs/";
	private String paramsFileName = "parameters.xml";

	private RepastXMLManager trafficConfigManager;
	private RepastXMLManager onlineCommConfigManager;
	private BrowserLauncher browserLauncher;
	private int strategy;
	private File strategyFile;
	private NormLabSimulator simulator;


	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Creates new form NormLabFrame2
	 */
	public NormLabFrame() {
		this.trafficConfigManager =
				new RepastXMLManager(trafficParamsDir, paramsFileName);
		this.onlineCommConfigManager = 
				new RepastXMLManager(onlinecommParamsDir, paramsFileName);

		this.simulator = NormLabSimulator.TrafficJunction;
		this.strategy = 2;

		try {
			this.browserLauncher =  new BrowserLauncher();
		} catch (Exception e) {
			this.errorMsg("Error while creating browser launcher", e.getMessage());
		}

		initComponents();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2,
				dim.height/2-this.getSize().height/2);
		
		ImageIcon img = new ImageIcon("misc/launcher/icons/normlab.jpg");
		this.setIconImage(img.getImage());
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
	 * @throws Exception 
	 * 
	 */
	private void saveConfig(NormLabSimulator sim) throws Exception {
		switch(sim) {
		case TrafficJunction:
			trafficConfigManager.setAttribute("normSynthesisStrategy",
					String.valueOf(strategy));
			break;
		case OnlineCommunity:
			onlineCommConfigManager.setAttribute("normSynthesisStrategy", 
					String.valueOf(strategy));
			break;
		}		
	}                                                     

	/**
	 * 
	 * @param evt
	 */
	private void btnExamplesActionPerformed(ActionEvent evt) {

		/* Show NormLab examples dialog */
		DialogNormLabExamples examplesDialog = 
				new DialogNormLabExamples(this, true);
		
		examplesDialog.setLocationRelativeTo(this);
		examplesDialog.setVisible(true);

		/* Launch chosen example*/
		int example = examplesDialog.getOption();
		if(example > 0) {
			this.trafficConfigManager.backupParameters();

			try {
				/* Configure parameters to use an example strategy */
				this.trafficConfigManager.setAttribute("normSynthesisStrategy", "0");
				this.trafficConfigManager.setAttribute("normSynthesisExample",
						String.valueOf(example));

				/* Launch traffic simulator */
				this.launch(NormLabSimulator.TrafficJunction);
				
				/* Restore original parameters */
				this.trafficConfigManager.restoreParameters(
						NormLabSimulator.TrafficJunction);
			} 
			catch (Exception e) {
				this.errorMsg("Error while executing simulation", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnDocActionPerformed(ActionEvent evt) {
		String usrDir = System.getProperty("user.dir");
		this.browserLauncher.openURLinBrowser("file:///" + usrDir + "/docs/");
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnExperimentsActionPerformed(java.awt.event.ActionEvent evt) {                                               
		DialogExperimentsLauncher expDialog = 
				new DialogExperimentsLauncher(this, true);
		expDialog.setLocationRelativeTo(this);
		expDialog.setVisible(true);
	} 

	/**
	 * 
	 * @param evt
	 */
	private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {                                         
		DialogAboutNormLab dialog = new DialogAboutNormLab(this, true);
		dialog.setLocationRelativeTo(this);
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
	 * @return
	 */
	private boolean getUserStrategy() {
		boolean fileProvided = false;

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
			this.strategyFile = chooser.getSelectedFile();
			fileProvided = true;
		}
		return fileProvided;	  
	}

	/**
	 * 
	 * @param evt
	 */
	private void rbUserStrategyActionPerformed(ActionEvent evt) {
		JRadioButton pSelectedButton = this.getStrategyButtonById(this.strategy);

		/* Ask for the strategy file */
		boolean fileProvided = this.getUserStrategy();

		/* A strategy has been provided */
		if(fileProvided) {
			String className = this.getClassName(strategyFile);

			try {
				switch(simulator) {
				case TrafficJunction:
					this.trafficConfigManager.setAttribute("userStrategyName", className);
					break;

				case OnlineCommunity:
					this.onlineCommConfigManager.setAttribute("userStrategyName", className);
					break;
				}
				this.strategy = 1;
			}
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
		try {
			if(simulator == NormLabSimulator.TrafficJunction) {
				DialogSimTrafficConfiguration configDialog;
				configDialog = new DialogSimTrafficConfiguration(this, 
						true, this.trafficConfigManager);

				configDialog.setLocationRelativeTo(this);
				configDialog.setVisible(true);
			}
			else {
				DialogSimOnlineComConfiguration configDialog;
				configDialog = new DialogSimOnlineComConfiguration(this, 
						true, this.onlineCommConfigManager);

				configDialog.setLocationRelativeTo(this);
				configDialog.setVisible(true);
			}
		}
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
		DialogSynthesisConfiguration configDialog = null;
		try {
			if(simulator == NormLabSimulator.TrafficJunction) {
				configDialog = new DialogSynthesisConfiguration(this, true, 
						simulator, this.trafficConfigManager, strategy);
			}
			else if(simulator == NormLabSimulator.OnlineCommunity) {
				configDialog = new DialogSynthesisConfiguration(this, true, 
						simulator, this.onlineCommConfigManager, strategy);
			}
			configDialog.setLocationRelativeTo(this);
			configDialog.setVisible(true);
		}
		catch (Exception e) {
			this.errorMsg("Error saving settings", e.getMessage());
			e.printStackTrace();
		}
	}  

	/**
	 * 
	 * @param evt
	 */
	private void btnLaunchActionPerformed(java.awt.event.ActionEvent evt) {                                          
		try {
			this.saveConfig(simulator);
			this.launch(simulator);
		} 
		catch (Exception e) {
			this.errorMsg("Error executing simulation", e.getMessage());
			e.printStackTrace();
		}
	}     

	/**
	 * 
	 * @param sim
	 * @throws Exception 
	 */
	private void launch(final NormLabSimulator sim) throws Exception {
		final DialogLoadingSimulation runningDialog = new DialogLoadingSimulation(this, true);
		runningDialog.setLocationRelativeTo(this);
		runningDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		/* Open a new dialog that loads a Repast simulation */
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>() {
			@Override
			protected Void doInBackground() {
				NormLabSimulationLauncher launcher = new NormLabSimulationLauncher(sim);
				launcher.run();
				return null;
			}

			@Override
			protected void done() {
				runningDialog.dispose();
			}
		};
		worker.execute();

		runningDialog.setVisible(true);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(NormLabFrame.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(NormLabFrame.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(NormLabFrame.class.getName()).log(Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			Logger.getLogger(NormLabFrame.class.getName()).log(Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new NormLabFrame().setVisible(true);
			}
		});
	}


	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		btnGroupChooseStrategy = new javax.swing.ButtonGroup();
		btnGroupChooseSim = new javax.swing.ButtonGroup();
		panelNormLab = new javax.swing.JPanel();
		lblTitle = new javax.swing.JLabel();
		panelGuide = new javax.swing.JPanel();
		btnExamples = new javax.swing.JButton();
		btnDoc = new javax.swing.JButton();
		panelExperiments = new javax.swing.JPanel();
		btnExperiments = new javax.swing.JButton();
		panelLaunchSimulation = new javax.swing.JPanel();
		panelLaunch = new javax.swing.JPanel();
		btnLaunch = new javax.swing.JButton();
		panelChoose = new javax.swing.JPanel();
		rbGenericStrategy = new javax.swing.JRadioButton();
		rbBASEStrategy = new javax.swing.JRadioButton();
		rbIRONStrategy = new javax.swing.JRadioButton();
		rbSIMONStrategy = new javax.swing.JRadioButton();
		rbLIONStrategy = new javax.swing.JRadioButton();
		rbDESMONStrategy = new javax.swing.JRadioButton();
		rbUserStrategy = new javax.swing.JRadioButton();
		panelChooseSim = new javax.swing.JPanel();
		rbOnlineCommSim = new javax.swing.JRadioButton();
		lblOnlineCommSim = new javax.swing.JLabel();
		lblTrafficSim = new javax.swing.JLabel();
		rbTrafficSim = new javax.swing.JRadioButton();
		panelConfigureStrategy = new javax.swing.JPanel();
		btnConfigureSim = new javax.swing.JButton();
		btnConfigureSynthesis = new javax.swing.JButton();
    btnAbout = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);

		panelNormLab.setPreferredSize(new java.awt.Dimension(650, 375));

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 19)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/microscope.png")); // NOI18N
		lblTitle.setText("NormLab: a framework to support research on norm synthesis");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		panelGuide.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Get familiar with NormLab", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

		btnExamples.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/examples.png")); // NOI18N
		btnExamples.setText("Examples");
		btnExamples.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExamplesActionPerformed(evt);
			}
		});

		btnDoc.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/doc.png")); // NOI18N
		btnDoc.setText("Documentation");
		btnDoc.setToolTipText("");
		btnDoc.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnDocActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelGuideLayout = new javax.swing.GroupLayout(panelGuide);
		panelGuide.setLayout(panelGuideLayout);
		panelGuideLayout.setHorizontalGroup(
				panelGuideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelGuideLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnExamples, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(btnDoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);
		panelGuideLayout.setVerticalGroup(
				panelGuideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelGuideLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelGuideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(btnExamples)
								.addComponent(btnDoc))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelExperiments.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Run experiments", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

		btnExperiments.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/experiments.png")); // NOI18N
		btnExperiments.setText("NormLab Experiments");
		btnExperiments.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExperimentsActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelExperimentsLayout = new javax.swing.GroupLayout(panelExperiments);
		panelExperiments.setLayout(panelExperimentsLayout);
		panelExperimentsLayout.setHorizontalGroup(
				panelExperimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelExperimentsLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnExperiments, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
						.addContainerGap())
				);
		panelExperimentsLayout.setVerticalGroup(
				panelExperimentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelExperimentsLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnExperiments)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelLaunchSimulation.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Execute a simulation with norm synthesis", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		panelLaunch.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "4. Execute the simulation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnLaunch.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
		btnLaunch.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/start.png")); // NOI18N
		btnLaunch.setText("Go!");
		btnLaunch.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnLaunchActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelLaunchLayout = new javax.swing.GroupLayout(panelLaunch);
		panelLaunch.setLayout(panelLaunchLayout);
		panelLaunchLayout.setHorizontalGroup(
				panelLaunchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelLaunchLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnLaunch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);
		panelLaunchLayout.setVerticalGroup(
				panelLaunchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelLaunchLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(btnLaunch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				);

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

		javax.swing.GroupLayout panelLaunchSimulationLayout = new javax.swing.GroupLayout(panelLaunchSimulation);
		panelLaunchSimulation.setLayout(panelLaunchSimulationLayout);
		panelLaunchSimulationLayout.setHorizontalGroup(
				panelLaunchSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelLaunchSimulationLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelLaunchSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(panelChoose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelChooseSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelLaunchSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(panelLaunch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panelConfigureStrategy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addContainerGap())
				);
		panelLaunchSimulationLayout.setVerticalGroup(
				panelLaunchSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLaunchSimulationLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelLaunchSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								.addGroup(panelLaunchSimulationLayout.createSequentialGroup()
										.addComponent(panelConfigureStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(panelLaunch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
										.addGroup(panelLaunchSimulationLayout.createSequentialGroup()
												.addComponent(panelChooseSim, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(panelChoose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
												.addContainerGap())
				);


    btnAbout.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/info.png")); // NOI18N
    btnAbout.setText("About NormLab");
    btnAbout.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnAboutActionPerformed(evt);
        }
    });
    
    javax.swing.GroupLayout panelNormLabLayout = new javax.swing.GroupLayout(panelNormLab);
    panelNormLab.setLayout(panelNormLabLayout);
    panelNormLabLayout.setHorizontalGroup(
        panelNormLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelNormLabLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelNormLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelNormLabLayout.createSequentialGroup()
                    .addComponent(panelGuide, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(panelExperiments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(panelLaunchSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelNormLabLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(btnAbout)))
            .addContainerGap())
    );
    panelNormLabLayout.setVerticalGroup(
        panelNormLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelNormLabLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(panelNormLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelGuide, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(panelExperiments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(panelLaunchSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnAbout)
            .addGap(12, 12, 12))
    );

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(panelNormLab, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(panelNormLab, javax.swing.GroupLayout.PREFERRED_SIZE, 612, javax.swing.GroupLayout.PREFERRED_SIZE)
				);

		pack();
	}

	//---------------------------------------------------------------------------
	// GUI attributes
	//---------------------------------------------------------------------------

  private javax.swing.JButton btnAbout;
	private javax.swing.JButton btnConfigureSim;
	private javax.swing.JButton btnConfigureSynthesis;
	private javax.swing.JButton btnDoc;
	private javax.swing.JButton btnExamples;
	private javax.swing.JButton btnExperiments;
	private javax.swing.ButtonGroup btnGroupChooseSim;
	private javax.swing.ButtonGroup btnGroupChooseStrategy;
	private javax.swing.JButton btnLaunch;
	private javax.swing.JLabel lblOnlineCommSim;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblTrafficSim;
	private javax.swing.JPanel panelChoose;
	private javax.swing.JPanel panelChooseSim;
	private javax.swing.JPanel panelConfigureStrategy;
	private javax.swing.JPanel panelExperiments;
	private javax.swing.JPanel panelGuide;
	private javax.swing.JPanel panelLaunch;
	private javax.swing.JPanel panelLaunchSimulation;
	private javax.swing.JPanel panelNormLab;
	private javax.swing.JRadioButton rbBASEStrategy;
	private javax.swing.JRadioButton rbUserStrategy;
	private javax.swing.JRadioButton rbDESMONStrategy;
	private javax.swing.JRadioButton rbGenericStrategy;
	private javax.swing.JRadioButton rbIRONStrategy;
	private javax.swing.JRadioButton rbLIONStrategy;
	private javax.swing.JRadioButton rbOnlineCommSim;
	private javax.swing.JRadioButton rbSIMONStrategy;
	private javax.swing.JRadioButton rbTrafficSim;

}
