package es.csic.iiia.normlab.launcher.ui;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import es.csic.iiia.normlab.launcher.model.RepastXMLManager;
import es.csic.iiia.normlab.launcher.onlinecomm.OnlineCommunityPopulation;
import es.csic.iiia.normlab.launcher.utils.JDecimalField;
import es.csic.iiia.normlab.launcher.utils.JIntegerField;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Javi
 */
public class DialogSimOnlineComConfiguration extends javax.swing.JDialog {

	private static RepastXMLManager configManager;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1926667708996650391L;

	private String normInfringementRate;
	private String maxTicks;
	private String randomSeed;
	private String contentQueueSize;
	private String population;
	
	private static Frame parent;

	/**
	 * Creates new form NormLabTrafficSimConfiguratorDialog
	 * @param configManager 
	 */
	public DialogSimOnlineComConfiguration(java.awt.Frame parentFrame,
			boolean modal, RepastXMLManager cManager) 
					throws Exception {

		super(parentFrame, modal);

		initComponents();
		setResizable(false);

		configManager = cManager;
		parent = parentFrame;
		
		this.loadConfig();
		this.showConfig();
	}

	/**
	 * 
	 * @param paramsFile
	 * @throws Exception 
	 */
	private void loadConfig() throws Exception {
		population = configManager.getAttribute("population");
		contentQueueSize = configManager.getAttribute("contentQueueSize");
		randomSeed = configManager.getAttribute("randomSeed");
		maxTicks= configManager.getAttribute("maxTicks");
		normInfringementRate= configManager.getAttribute("normInfringementRate");
	}

	/**
	 * 
	 */
	private void showConfig() {
		this.lblPopulationName.setText(population);
		this.txtContentQueueSize.setText(contentQueueSize);
		this.txtRandomSeed.setText(randomSeed);
		this.txtMaxSimTicks.setText(maxTicks);
		this.txtViolProb.setText(normInfringementRate);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void saveConfig() throws Exception {
		
		NumberFormat format = NumberFormat.getInstance(Locale.US);

		randomSeed = format.parse(randomSeed).toString();
		contentQueueSize = format.parse(contentQueueSize).toString();
		maxTicks = format.parse(maxTicks).toString();
		
		configManager.setAttribute("randomSeed", randomSeed);
		configManager.setAttribute("contentQueueSize", contentQueueSize);
		configManager.setAttribute("maxTicks", maxTicks);
		configManager.setAttribute("normInfringementRate", normInfringementRate);
		configManager.setAttribute("useGui", "true");
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
	 * @param evt
	 */
	private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
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
	private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {
//		try {
//			this.loadConfig();
//			this.showConfig();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		this.dispose();
	}     

	/**
	 * 
	 * @param evt
	 */
	private void btnChangePopulationActionPerformed(ActionEvent evt) {
		DialogPopulationConfigurator dialog = 
				new DialogPopulationConfigurator(parent, true, population);
		
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		
		try {
			if(dialog.userSaved()) {
				OnlineCommunityPopulation pop = dialog.getPopulation();
		    configManager.setAttribute("population", pop.getName());
		    population = pop.getName();
		    this.lblPopulationName.setText(population);	
			}
    }
		catch (Exception e) {
	    this.errorMsg("", e.getMessage());
    }
	}

	private void txtContentQueueSizeActionPerformed(DocumentEvent evt) {                                              
		this.contentQueueSize = this.txtContentQueueSize.getText();
	}                                             

	private void txtRandomSeedActionPerformed(DocumentEvent evt) {                                              
		this.randomSeed = this.txtRandomSeed.getText();
	}                                             

	private void txtMaxSimTicksActionPerformed(DocumentEvent evt) {                                               
		this.maxTicks = this.txtMaxSimTicks.getText();
	}

	private void txtViolProbActionPerformed(DocumentEvent evt) {                                            
		this.normInfringementRate = this.txtViolProb.getText();
	}    

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		lblTitle = new javax.swing.JLabel();
		txtRandomSeed = new JIntegerField(0);
		lblMaxSimTicks = new javax.swing.JLabel();
		txtContentQueueSize = new JIntegerField(1);
		lblInfo = new javax.swing.JLabel();
		lblContentQueueSize = new javax.swing.JLabel();
		txtMaxSimTicks = new JIntegerField(1);
		lblViolProb = new javax.swing.JLabel();
		txtViolProb = new JDecimalField(2, 0.0, 1.00);
		btnSave = new javax.swing.JButton();
		btnExit = new javax.swing.JButton();
		lblRandomSeed = new javax.swing.JLabel();
		lblPopulation = new javax.swing.JLabel();
		lblPopulationName = new javax.swing.JLabel();
		btnChangePopulation = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 19)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/onlinecomm.png")); // NOI18N
		lblTitle.setText("Online Community simulator settings");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		txtRandomSeed.setText("0");
		txtRandomSeed.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtRandomSeedActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtRandomSeedActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtRandomSeedActionPerformed(e);
			}
		});

		lblMaxSimTicks.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblMaxSimTicks.setText("Maximum simulation ticks");
		lblMaxSimTicks.setToolTipText("Maximum amount of ticks that the simulation will last");

		txtContentQueueSize.setText("3000");
		txtContentQueueSize.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtContentQueueSizeActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtContentQueueSizeActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtContentQueueSizeActionPerformed(e);
			}
		});

		lblInfo.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/info.png")); // NOI18N
		lblInfo.setText("Hold mouse pointer on each parameter to show more details about it");

		lblContentQueueSize.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblContentQueueSize.setText("Contents queue size");
		lblContentQueueSize.setToolTipText("<html>\nSize of the FIFO queue "
				+ "that keeps track of contents uploaded by users\n</html>");

		txtMaxSimTicks.setText("5000");
		txtMaxSimTicks.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtMaxSimTicksActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtMaxSimTicksActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtMaxSimTicksActionPerformed(e);
			}
		});

		lblViolProb.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblViolProb.setText("Norm infringement probability");
		lblViolProb.setToolTipText("<html>Users' probability to infringe those"
				+ " norms that<br> apply to them at a given"
				+ " time (i.e., tick) </html>");

		txtViolProb.setText("0.3");
		txtViolProb.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtViolProbActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtViolProbActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtViolProbActionPerformed(e);
			}
		});

		btnSave.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/save.png")); // NOI18N
		btnSave.setText("Save and exit");
		btnSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSaveActionPerformed(evt);
			}
		});

		btnExit.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/exit.png")); // NOI18N
		btnExit.setText("Exit");
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExitActionPerformed(evt);
			}
		});

		lblRandomSeed.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblRandomSeed.setText("Use random seed");
		lblRandomSeed.setToolTipText("<html> Seed to be employed for the "
				+ "random components of the simulator<br> \n(e.g., the "
				+ "sections whereto upload new contents) </html>");

		lblPopulation.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblPopulation.setText("Population");

		lblPopulationName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblPopulationName.setForeground(new java.awt.Color(0, 51, 204));
		lblPopulationName.setText("70M-30S-NIR-0.3");

		btnChangePopulation.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/onlinecomm_small.png")); // NOI18N
		btnChangePopulation.setText("Change");
		btnChangePopulation.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnChangePopulationActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(btnExit)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnSave))
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(lblInfo)
														.addGroup(layout.createSequentialGroup()
																.addComponent(lblPopulation)
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(lblPopulationName)
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(btnChangePopulation))
																.addGroup(layout.createSequentialGroup()
																		.addComponent(lblContentQueueSize)
																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(txtContentQueueSize, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(layout.createSequentialGroup()
																				.addComponent(lblViolProb)
																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																				.addComponent(txtViolProb, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
																				.addGroup(layout.createSequentialGroup()
																						.addComponent(lblRandomSeed)
																						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																						.addComponent(txtRandomSeed, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(layout.createSequentialGroup()
																								.addComponent(lblMaxSimTicks)
																								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addComponent(txtMaxSimTicks, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
																								.addGap(0, 0, Short.MAX_VALUE)))
																								.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(lblInfo)
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblPopulation)
								.addComponent(lblPopulationName)
								.addComponent(btnChangePopulation))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblContentQueueSize)
										.addComponent(txtContentQueueSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblMaxSimTicks)
												.addComponent(txtMaxSimTicks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblViolProb)
														.addComponent(txtViolProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(lblRandomSeed)
																.addComponent(txtRandomSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																.addGap(18, 18, 18)
																.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(btnSave)
																		.addComponent(btnExit))
																		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pack();
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
			java.util.logging.Logger.getLogger(DialogSimOnlineComConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogSimOnlineComConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogSimOnlineComConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogSimOnlineComConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogSimOnlineComConfiguration dialog;
				try {
					dialog = new DialogSimOnlineComConfiguration(new javax.swing.JFrame(),
							true, configManager);
					dialog.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(java.awt.event.WindowEvent e) {
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

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnChangePopulation;
	private javax.swing.JButton btnSave;
	private javax.swing.JButton btnExit;
	private javax.swing.JLabel lblContentQueueSize;
	private javax.swing.JLabel lblInfo;
	private javax.swing.JLabel lblMaxSimTicks;
	private javax.swing.JLabel lblPopulation;
	private javax.swing.JLabel lblPopulationName;
	private javax.swing.JLabel lblRandomSeed;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblViolProb;
	private JIntegerField txtContentQueueSize;
	private JIntegerField txtMaxSimTicks;
	private JIntegerField txtRandomSeed;
	private JDecimalField txtViolProb;
	// End of variables declaration//GEN-END:variables
}
