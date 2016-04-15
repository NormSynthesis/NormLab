package es.csic.iiia.normlab.launcher.ui;

import java.awt.HeadlessException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import es.csic.iiia.normlab.launcher.model.RepastXMLManager;

/**
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class DialogSimTrafficConfiguration extends javax.swing.JDialog {

	private static final long serialVersionUID = -118664013293035294L;
	
	private static RepastXMLManager configManager;
	
	private String map;
	private String randomSeed;
	private String newCarsFreq;
	private String numCarsToAdd;
	private String maxTicks;
	private String normInfringementRate;
	
	/**
	 * Creates new form NormLabTrafficSimConfiguratorDialog
	 * @param configManager 
	 * @throws Exception 
	 */
	public DialogSimTrafficConfiguration(java.awt.Frame parent,
			boolean modal, RepastXMLManager cManager) 
					throws Exception {

		super(parent, modal);

		initComponents();
		setResizable(false);

		configManager = cManager;

		this.loadConfig();
		this.showConfig();
	}

	/**
	 * 
	 * @param paramsFile
	 * @throws Exception 
	 */
	private void loadConfig() throws Exception {
		map = configManager.getAttribute("map");
		randomSeed = configManager.getAttribute("randomSeed");
		newCarsFreq = configManager.getAttribute("newCarsFreq");
		numCarsToAdd = configManager.getAttribute("numCarsToAdd");
		maxTicks= configManager.getAttribute("maxTicks");
		normInfringementRate= configManager.getAttribute("normInfringementRate");
	}

	/**
	 * 
	 */
	private void showConfig() {
		this.txtMap.setText(map);
		this.txtRandomSeed.setText(randomSeed);
		this.txtNewCarsFreq.setText(newCarsFreq);
		this.txtNewCarsToAdd.setText(numCarsToAdd);
		this.txtMaxSimTicks.setText(maxTicks);
		this.txtViolProb.setText(normInfringementRate);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void saveConfig() throws Exception {
		configManager.setAttribute("map", map);
		configManager.setAttribute("randomSeed", randomSeed);
		configManager.setAttribute("newCarsFreq", newCarsFreq);
		configManager.setAttribute("numCarsToAdd", numCarsToAdd);
		configManager.setAttribute("maxTicks", maxTicks);
		configManager.setAttribute("normInfringementRate", normInfringementRate);
		configManager.setAttribute("useGui", "true");
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

	private void txtMapActionPerformed(DocumentEvent evt) {                                       
		this.map = this.txtMap.getText(); 
	}                        

	private void txtRandomSeedActionPerformed(DocumentEvent evt) {                                              
		this.randomSeed = this.txtRandomSeed.getText();
	}                                             

	private void txtNewCarsFreqActionPerformed(DocumentEvent evt) {                                               
		this.newCarsFreq = this.txtNewCarsFreq.getText();
	}                                              

	private void txtNewCarsToAddActionPerformed(DocumentEvent evt) {                                                
		this.numCarsToAdd = this.txtNewCarsToAdd.getText();
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
		txtNewCarsFreq = new javax.swing.JTextField();
		lblNewCarsFreq = new javax.swing.JLabel();
		txtNewCarsToAdd = new javax.swing.JTextField();
		lblCarsToAdd = new javax.swing.JLabel();
		lblMapNumber = new javax.swing.JLabel();
		txtMap = new javax.swing.JTextField();
		lblViolProb = new javax.swing.JLabel();
		txtViolProb = new javax.swing.JTextField();
		btnSave = new javax.swing.JButton();
		btnExit = new javax.swing.JButton();
		lblRandomSeed = new javax.swing.JLabel();
		txtRandomSeed = new javax.swing.JTextField();
		lblMaxSimTicks = new javax.swing.JLabel();
		lblInfo = new javax.swing.JLabel();
		txtMaxSimTicks = new javax.swing.JTextField();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 19)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/collision.png")); // NOI18N
		lblTitle.setText("Traffic Simulator settings");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);


		lblNewCarsFreq.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNewCarsFreq.setText("Car addition frequency");

		txtNewCarsToAdd.setText("3");

		lblCarsToAdd.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblCarsToAdd.setText("Number of cars to simultaneously add");

		lblMapNumber.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblMapNumber.setText("Use map");

		txtMap.setText("1");

		lblViolProb.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblViolProb.setText("Norm infringement probability");

		lblNewCarsFreq.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNewCarsFreq.setText("Car addition frequency");
    lblNewCarsFreq.setToolTipText("<html>Every how many ticks do you want new"
    		+ " cars <br>to be added to the scenario?</html>");

		txtNewCarsFreq.setText("1");
		txtNewCarsFreq.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNewCarsFreqActionPerformed(e);	
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNewCarsFreqActionPerformed(e);	
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNewCarsFreqActionPerformed(e);			
			}
		});

		txtNewCarsToAdd.setText("3");
		txtNewCarsToAdd.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNewCarsToAddActionPerformed(e);		
				}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNewCarsToAddActionPerformed(e);		
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNewCarsToAddActionPerformed(e);			}
		});

		lblCarsToAdd.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblCarsToAdd.setText("Number of cars to simultaneously add");
    lblCarsToAdd.setToolTipText("<html>\nWhen adding new cars to the scenario,"
    		+ " how many of them<br>\ndo you want that are "
    		+ "simultaneously added?<br>\n</html>");

		lblMapNumber.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblMapNumber.setText("Use map");
    lblMapNumber.setToolTipText("Map along which cars will travel");

		txtMap.setText("1");
		txtMap.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtMapActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtMapActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtMapActionPerformed(e);
			}
		});

		lblViolProb.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblViolProb.setText("Norm infringement probability");
    lblViolProb.setToolTipText("<html>\nCars' probability to infringe"
    		+ " those norms that<br>\napply to them at a given"
    		+ " time (i.e., tick)\n</html>");


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
    lblRandomSeed.setToolTipText("<html>\nSeed to be employed for the random "
    		+ "components of the simulator<br>\n(e.g., the positions whereto"
    		+ "add new cars in the scenario)\n</html>");

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

		lblInfo.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/info.png")); // NOI18N
		lblInfo.setText("Hold mouse pointer on each parameter to show more details about it");

		txtMaxSimTicks.setText("30000");
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

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(layout.createSequentialGroup()
														.addComponent(lblCarsToAdd)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(txtNewCarsToAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(lblInfo)
														.addGroup(layout.createSequentialGroup()
																.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addComponent(lblViolProb)
																		.addComponent(lblMaxSimTicks))
																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																				.addComponent(txtMaxSimTicks, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
																				.addComponent(txtViolProb))))
																				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																				.addGroup(layout.createSequentialGroup()
																						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																								.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																								.addGroup(layout.createSequentialGroup()
																										.addComponent(btnExit)
																										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																										.addComponent(btnSave))
																										.addGroup(layout.createSequentialGroup()
																												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(layout.createSequentialGroup()
																																.addComponent(lblNewCarsFreq)
																																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																.addComponent(txtNewCarsFreq, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
																																.addGroup(layout.createSequentialGroup()
																																		.addComponent(lblMapNumber)
																																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(txtMap, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
																																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																																		.addComponent(lblRandomSeed)
																																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																		.addComponent(txtRandomSeed, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
																																		.addContainerGap())))
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
								.addComponent(lblMapNumber)
								.addComponent(txtMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(lblRandomSeed)
								.addComponent(txtRandomSeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblNewCarsFreq)
										.addComponent(txtNewCarsFreq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblCarsToAdd)
												.addComponent(txtNewCarsToAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblMaxSimTicks)
														.addComponent(txtMaxSimTicks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(lblViolProb)
																.addComponent(txtViolProb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
																.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(btnExit)
																		.addComponent(btnSave))
																		.addContainerGap())
				);
		pack();
	}

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
			java.util.logging.Logger.getLogger(DialogSimTrafficConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogSimTrafficConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogSimTrafficConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogSimTrafficConfiguration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogSimTrafficConfiguration dialog;
        try {
	        dialog = new DialogSimTrafficConfiguration(new javax.swing.JFrame(), true,
	        		configManager);
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
	private javax.swing.JButton btnSave;
	private javax.swing.JButton btnExit;
	private javax.swing.JLabel lblCarsToAdd;
	private javax.swing.JLabel lblInfo;
	private javax.swing.JLabel lblMapNumber;
	private javax.swing.JLabel lblMaxSimTicks;
	private javax.swing.JLabel lblNewCarsFreq;
	private javax.swing.JLabel lblRandomSeed;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblViolProb;
	private javax.swing.JTextField txtMap;
	private javax.swing.JTextField txtMaxSimTicks;
	private javax.swing.JTextField txtNewCarsFreq;
	private javax.swing.JTextField txtNewCarsToAdd;
	private javax.swing.JTextField txtRandomSeed;
	private javax.swing.JTextField txtViolProb;
	// End of variables declaration//GEN-END:variables
}
