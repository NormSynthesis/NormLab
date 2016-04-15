package es.csic.iiia.normlab.launcher.ui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import edu.stanford.ejalbert.BrowserLauncher;
import es.csic.iiia.normlab.launcher.NormLabExperimentsRunner;
import es.csic.iiia.normlab.launcher.utils.MessageConsole;

/**
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class DialogExperimentsLauncher extends javax.swing.JDialog {

	/* Serial ID */
	private static final long serialVersionUID = 4313141381353332370L;

	/* Directory containing available experiments */
	private String availableExpDir = "experiments" + File.separator + "available";

	/* Parent frame */
	private Frame parent;

	/* */
	private DefaultListModel<String> avExListModel;
	private DefaultListModel<String> exToRunListModel;
	private BrowserLauncher browserLauncher;

	private Thread threadExperiments;

	/**
	 * Creates new form NormLabExperimentsDialog
	 */
	public DialogExperimentsLauncher(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		this.parent = parent;

		initComponents();
		setResizable(false);

		this.loadAvailableExperiments();
		this.exToRunListModel.removeAllElements();
		
		try {
			this.browserLauncher =  new BrowserLauncher();
		} catch (Exception e) {
			this.errorMsg("Error while creating browser launcher", e.getMessage());
		}
	}

	/**
	 * 
	 */
	private void loadAvailableExperiments() {
		File avExpFolder = new File (availableExpDir);
		File[] availableExps = avExpFolder.listFiles();

		this.avExListModel.removeAllElements();

		for(File avExp : availableExps){
			String avExName = avExp.getName();

			if(avExName.contains(".xml")) {
				if(!exToRunListModel.contains(avExName)) {
					avExListModel.addElement(avExName);	
				}
			}
		}
	}

	/**
	 * 
	 */
	private void launchExperiments() {
		List<String> experiments = new ArrayList<String>();
		int numExps = this.exToRunListModel.getSize();

		for(int i=0; i<numExps; i++) {
			experiments.add(this.exToRunListModel.get(i));
		}

		/* Create experiments runner and show console */
		NormLabExperimentsRunner runner = 
				new NormLabExperimentsRunner(experiments);

		threadExperiments = new Thread(runner);
		this.showConsole();

		/* Execute experiments runner */
		threadExperiments.start();
	}

	/**
	 * 
	 */
	private void showConsole() {
		final DialogExperimentsConsole console = 
				new DialogExperimentsConsole(parent, true, threadExperiments);

		/* Redirect output */
		MessageConsole mc = new MessageConsole(console.getConsole());
		mc.redirectOut();
		mc.redirectErr(Color.RED, null);

		Runnable runnable = new Runnable() {
			public void run() {
				console.setLocationRelativeTo(parent);
				console.setVisible(true);
			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}

	/**
	 * 
	 */
	private void deleteExperiments() {
		List<String> toDelete = new ArrayList<String>();

		/* Retrieve experiments to delete */
		int numExps = this.avExListModel.getSize();
		for(int i=0; i<numExps; i++) {
			if(this.listAvailableEx.isSelectedIndex(i)) {
				toDelete.add(this.avExListModel.get(i));		
			}
		}
		numExps = this.exToRunListModel.getSize();
		for(int i=0; i<numExps; i++) {
			if(this.listExToRun.isSelectedIndex(i)) {
				toDelete.add(this.exToRunListModel.get(i));
			}
		}
		
		/* Ask for user confirmation */
		if(toDelete.size() > 0) {
			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog(this, 
					"Remove selected experiments? This cannot be undone", 
							"Remove experiments?", 
							dialogButton);

			/* Remove experiments if there is confirmation of the user */
			if(dialogResult == 0) {
				
				for(String exp : toDelete) {
					File expConfig = new File(availableExpDir + File.separator + exp);
					
					String expTrafficFilename = availableExpDir + File.separator +  
							exp.substring(0, exp.length() - 4) + ".parameters.traffic";
					String expOnlinecommFilename = availableExpDir + File.separator +  
							exp.substring(0, exp.length() - 4) + ".parameters.onlinecomm";
					
					File expTraffic = new File(expTrafficFilename);
					File expOnlinecomm = new File(expOnlinecommFilename);

					/* Remove experiment files */
					expConfig.delete();
					expTraffic.delete();
					expOnlinecomm.delete();
					
					/* Remove from the list */
					this.avExListModel.removeElement(exp);
					this.exToRunListModel.removeElement(exp);
				}
			} 
		}
	}
	
	/**
	 * 
	 * @param evt
	 */
	private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {                                         
		this.dispose();
	}                                        

	/**
	 * 
	 * @param evt
	 */
	private void btnLaunchActionPerformed(java.awt.event.ActionEvent evt) {
		if(exToRunListModel.getSize() > 0) {

			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog(this, 
					"Do you really want to launch selected experiments?\n"
							+ "Depending on the experiments configuration, this may take a long time", 
							"Launch experiments?", 
							dialogButton);

			if(dialogResult == 0) {
				this.launchExperiments();
			} 
		}
	}                                         

	/**
	 * 
	 * @param evt
	 */
	private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {                                       
		List<String> selectedExp = new ArrayList<String>(); 

		/* Get selected elements */
		for(int index : this.listAvailableEx.getSelectedIndices()) {
			selectedExp.add(avExListModel.get(index));
		}

		/* Copy selected experiments to experiments to run list 
		 * and remove them from the available experiments list */
		for(String exp : selectedExp) {
			exToRunListModel.addElement(exp);
			avExListModel.removeElement(exp);
		}
		this.listAvailableEx.clearSelection();
	}                                      

	/**
	 * 
	 * @param evt
	 */
	private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {                                          
		List<String> selectedExp = new ArrayList<String>(); 

		/* Get selected elements */
		for(int index : this.listExToRun.getSelectedIndices()) {
			selectedExp.add(exToRunListModel.get(index));
		}

		/* Copy selected experiments to available experiments list 
		 * and remove them from the experiments to run list */
		for(String exp : selectedExp) {
			avExListModel.addElement(exp);
			exToRunListModel.removeElement(exp);
		}
		this.listExToRun.clearSelection();
	}                                         

	/**
	 * 
	 * @param evt
	 */
	private void btnCreateExActionPerformed(java.awt.event.ActionEvent evt) {                                            
		DialogExperimentCreator expDialog = 
				new DialogExperimentCreator(this.parent, true);
		expDialog.setLocationRelativeTo(this);
		expDialog.setVisible(true);	
		this.loadAvailableExperiments();
	}                                           

	/**
	 * 
	 * @param evt
	 */
	private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {                                          
		this.deleteExperiments();
	}                                         

	/**
	 * 
	 * @param evt
	 */
  private void btnOpenResultsActionPerformed(java.awt.event.ActionEvent evt) {                                               
  	String usrDir = System.getProperty("user.dir");
  	this.browserLauncher.openURLinBrowser("file:///" + usrDir + "/experiments/performed/");
  }  
  
	/**
	 * 
	 * @param evt
	 */
	private void cbSelectAllActionPerformed(java.awt.event.ActionEvent evt) {
		int numExps = this.avExListModel.getSize();
		
		if(this.cbSelectAll.isSelected()) {
			this.listAvailableEx.addSelectionInterval(0, numExps-1);
		}
		else {
			this.loadAvailableExperiments();
		}
	}  

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void listExpDoubleClickEvent(MouseEvent evt) {

		/* Double click detected */
		if (evt.getClickCount() > 1) {
			JList<String> list = (JList<String>)evt.getSource();
			String selExp = list.getSelectedValue();

			DialogExperimentCreator expDialog = 
					new DialogExperimentCreator(this.parent, true, selExp);

			expDialog.setLocationRelativeTo(this);
			expDialog.setVisible(true);	
			this.loadAvailableExperiments();
		}
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
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		lblTitle = new javax.swing.JLabel();
		lblAvailableEx = new javax.swing.JLabel();
		lblInfo = new javax.swing.JLabel();
		btnAdd = new javax.swing.JButton();
		lblExToRun = new javax.swing.JLabel();
		btnLaunch = new javax.swing.JButton();
		btnClose = new javax.swing.JButton();
		btnRemove = new javax.swing.JButton();
		scrPanelAvailableEx = new javax.swing.JScrollPane();
		scrPanelExToRun = new javax.swing.JScrollPane();
		btnCreateEx = new javax.swing.JButton();
    btnDelete = new javax.swing.JButton();
    btnOpenResults = new javax.swing.JButton();
    cbSelectAll = new javax.swing.JCheckBox();
    
		exToRunListModel = new DefaultListModel<String>();
		listExToRun = new javax.swing.JList<String>();
		listExToRun.setModel(exToRunListModel);
		listExToRun.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				listExpDoubleClickEvent(evt);
			}
		});

		avExListModel = new DefaultListModel<String>();
		listAvailableEx = new javax.swing.JList<String>();
		listAvailableEx.setModel(avExListModel);
		listAvailableEx.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				listExpDoubleClickEvent(evt);
			}
		});

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);


		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/experiments.png")); // NOI18N
		lblTitle.setText("NormLab experiments launcher");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		lblAvailableEx.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
		lblAvailableEx.setText("Available experiments");

		btnAdd.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnAdd.setText(">>>");
		btnAdd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnAddActionPerformed(evt);
			}
		});

		lblExToRun.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
		lblExToRun.setText("Experiments to run");

		btnLaunch.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
		btnLaunch.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/start_small.png")); // NOI18N
		btnLaunch.setText("Run experiments");
		btnLaunch.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnLaunchActionPerformed(evt);
			}
		});

		btnClose.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/exit.png")); // NOI18N
		btnClose.setText("Exit");
		btnClose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnCloseActionPerformed(evt);
			}
		});

		btnRemove.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnRemove.setText("<<<");
		btnRemove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnRemoveActionPerformed(evt);
			}
		});

		scrPanelAvailableEx.setViewportView(listAvailableEx);

		scrPanelExToRun.setViewportView(listExToRun);

		btnCreateEx.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/create.png")); // NOI18N
		btnCreateEx.setText("Create new experiment");
		btnCreateEx.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnCreateExActionPerformed(evt);
			}
		});

		lblInfo.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/info.png")); // NOI18N
		lblInfo.setText("Double click an experiment to open its configuration");


    btnDelete.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/delete.png")); // NOI18N
    btnDelete.setText("Delete experiment");
    btnDelete.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnDeleteActionPerformed(evt);
        }
    });

    cbSelectAll.setText("Select all");
    cbSelectAll.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            cbSelectAllActionPerformed(evt);
        }
    });
    
    btnOpenResults.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/open_small.png")); // NOI18N
    btnOpenResults.setText("Open results");
    btnOpenResults.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnOpenResultsActionPerformed(evt);
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
                    .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnCreateEx)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDelete))
                .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnClose)
                        .addComponent(cbSelectAll))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnLaunch, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnOpenResults, javax.swing.GroupLayout.Alignment.TRAILING)))
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(scrPanelAvailableEx, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(lblAvailableEx, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnAdd)
                        .addComponent(btnRemove))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblExToRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scrPanelExToRun, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnCreateEx)
                .addComponent(btnDelete))
            .addGap(18, 18, 18)
            .addComponent(lblInfo)
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblAvailableEx)
                        .addComponent(lblExToRun))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(scrPanelAvailableEx, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addGap(98, 98, 98)
                    .addComponent(btnAdd)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnRemove))
                .addGroup(layout.createSequentialGroup()
                    .addGap(25, 25, 25)
                    .addComponent(scrPanelExToRun, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnLaunch)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnOpenResults))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(cbSelectAll)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClose)))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

		pack();
	}// </editor-fold>                        

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(DialogExperimentsLauncher.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentsLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentsLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogExperimentsLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogExperimentsLauncher dialog = new DialogExperimentsLauncher(new javax.swing.JFrame(), true);
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
	private javax.swing.JButton btnAdd;
	private javax.swing.JButton btnClose;
	private javax.swing.JButton btnCreateEx;
	private javax.swing.JButton btnLaunch;
	private javax.swing.JButton btnRemove;
  private javax.swing.JButton btnDelete;
  private javax.swing.JButton btnOpenResults;
  private javax.swing.JCheckBox cbSelectAll;
	private javax.swing.JLabel lblAvailableEx;
	private javax.swing.JLabel lblExToRun;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JLabel lblInfo;
	private javax.swing.JList<String> listAvailableEx;
	private javax.swing.JList<String> listExToRun;
	private javax.swing.JScrollPane scrPanelAvailableEx;
	private javax.swing.JScrollPane scrPanelExToRun;
	// End of variables declaration//GEN-END:variables
}
