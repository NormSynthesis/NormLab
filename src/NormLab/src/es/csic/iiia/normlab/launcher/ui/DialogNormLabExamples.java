/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.launcher.ui;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class DialogNormLabExamples extends JDialog {

	private JButton btnExit;
	private JButton btnEx1;
	private JButton btnEx2;
	private JButton btnEx3;
	private JButton btnEx4;
	private JButton btnEx5;
	private JLabel lblEx1;
	private JLabel lblEx2;
	private JLabel lblEx3;
	private JLabel lblEx4;
	private JLabel lblEx5;
	private JLabel lblTitle;
	private JPanel panelEx1;
	private JPanel panelEx2;
	private JPanel panelEx3;
	private JPanel panelEx4;
	private JPanel panelEx5;
	
	/* */
	private int option;

	/**
	 * 
	 */
	private static final long serialVersionUID = -2252503998363974362L;
	/**
	 * Creates new form NormLabExamplesFrame
	 */
	public DialogNormLabExamples(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		initComponents();
		setResizable(false);

		this.option = 0;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	/**
	 * 
	 * @return
	 */
	public int getOption() {
		return this.option;
	}

	private void btnEx1ActionPerformed(java.awt.event.ActionEvent evt) {
		this.option = 1;
		this.dispose();
	}

	private void btnEx2ActionPerformed(java.awt.event.ActionEvent evt) {
		this.option = 2;
		this.dispose();
	}

	private void btnEx3ActionPerformed(java.awt.event.ActionEvent evt) {
		this.option = 3;
		this.dispose();
	}

	private void btnEx4ActionPerformed(java.awt.event.ActionEvent evt) {
		this.option = 4;
		this.dispose();
	}

	private void btnEx5ActionPerformed(java.awt.event.ActionEvent evt) {
		this.option = 5;
		this.dispose();
	}

	private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
		//    	this.option = 0;
		this.dispose();
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
			java.util.logging.Logger.getLogger(DialogNormLabExamples.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogNormLabExamples.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogNormLabExamples.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogNormLabExamples.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogNormLabExamples dialog = 
						new DialogNormLabExamples(new JFrame(), true);
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


	private void initComponents() {

		panelEx1 = new JPanel();
		btnEx1 = new JButton();
		lblEx1 = new JLabel();
		lblTitle = new JLabel();
		panelEx2 = new JPanel();
		btnEx2 = new JButton();
		lblEx2 = new JLabel();
		panelEx3 = new JPanel();
		btnEx3 = new JButton();
		lblEx3 = new JLabel();
		panelEx4 = new JPanel();
		btnEx4 = new JButton();
		lblEx4 = new JLabel();
		panelEx5 = new JPanel();
		btnEx5 = new JButton();
		lblEx5 = new JLabel();
		btnExit = new JButton();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

		panelEx1.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), 
				"Example 1. A simulation with no norms", 
				TitledBorder.DEFAULT_JUSTIFICATION, 
				TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnEx1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnEx1.setText("Launch example 1");
		btnEx1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEx1ActionPerformed(evt);
			}
		});

		lblEx1.setText("<html>\nLaunches a road traffic simulation in which "
				+ "cars enter the scenario and cross <br>\na junction to reach "
				+ "their destinations. No norms rule the system. Since cars <br> "
				+ "\ndo not have skills to coordinate, collisions continuously arise. \n</html>");

		GroupLayout panelEx1Layout = new GroupLayout(panelEx1);
		panelEx1.setLayout(panelEx1Layout);
		panelEx1Layout.setHorizontalGroup(
				panelEx1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx1Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblEx1, GroupLayout.PREFERRED_SIZE, 
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnEx1)
										.addContainerGap())
				);
		panelEx1Layout.setVerticalGroup(
				panelEx1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx1Layout.createSequentialGroup()
						.addGroup(panelEx1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addGroup(panelEx1Layout.createSequentialGroup()
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblEx1, GroupLayout.PREFERRED_SIZE, 
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGroup(panelEx1Layout.createSequentialGroup()
														.addGap(4, 4, 4)
														.addComponent(btnEx1, GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																.addContainerGap())
				);

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		lblTitle.setIcon(new ImageIcon("misc/launcher/icons/collision.png")); // NOI18N
		lblTitle.setText("NormLab: road traffic examples");
		lblTitle.setBorder(BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		panelEx2.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), 
				"Example 2. A simulation with a predefined normative system of one norm",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
				new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnEx2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnEx2.setText("Launch example 2");
		btnEx2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEx2ActionPerformed(evt);
			}
		});

		lblEx2.setText("<html>\nLaunches a road traffic simulation in which "
				+ "cars behaviours are regulated <br>\nby a predefined normative "
				+ "system with one left-hand-side priority norm. <br>\nThis allows"
				+ " to avoid some car collisions, but not to completely remove them.\n</html>");

		GroupLayout panelEx2Layout = new GroupLayout(panelEx2);
		panelEx2.setLayout(panelEx2Layout);
		panelEx2Layout.setHorizontalGroup(
				panelEx2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx2Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblEx2, GroupLayout.PREFERRED_SIZE, 
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnEx2)
										.addContainerGap())
				);
		panelEx2Layout.setVerticalGroup(
				panelEx2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx2Layout.createSequentialGroup()
						.addGroup(panelEx2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addGroup(panelEx2Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(lblEx2, GroupLayout.PREFERRED_SIZE, 
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGroup(panelEx2Layout.createSequentialGroup()
														.addGap(4, 4, 4)
														.addComponent(btnEx2, GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																.addContainerGap())
				);

		panelEx3.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), 
				"Example 3. A simulation without car collisions", 
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnEx3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnEx3.setText("Launch example 3");
		btnEx3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEx3ActionPerformed(evt);
			}
		});

		lblEx3.setText("<html>\nLaunches a road traffic simulation in which cars "
				+ "behaviours are regulated <br>\nby a predefined normative system"
				+ " with three norms that allow to <br>\ncompletely remove car "
				+ "collisions (as far as cars comply with its norms).\n</html>");

		GroupLayout panelEx3Layout = new GroupLayout(panelEx3);
		panelEx3.setLayout(panelEx3Layout);
		panelEx3Layout.setHorizontalGroup(
				panelEx3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx3Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblEx3)
						.addGap(8, 8, 8)
						.addComponent(btnEx3)
						.addContainerGap())
				);
		panelEx3Layout.setVerticalGroup(
				panelEx3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx3Layout.createSequentialGroup()
						.addGroup(panelEx3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addGroup(panelEx3Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(lblEx3, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGroup(panelEx3Layout.createSequentialGroup()
														.addGap(4, 4, 4)
														.addComponent(btnEx3, GroupLayout.DEFAULT_SIZE, 
																GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																.addContainerGap())
				);

		panelEx4.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Example 4. Automatic norm generation", 
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnEx4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnEx4.setText("Launch example 4");
		btnEx4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEx4ActionPerformed(evt);
			}
		});

		lblEx4.setText("<html>\nLaunches a road traffic simulation in which a Norm "
				+ "Synthesis Machine <br>\nmonitors the traffic junction, detects car"
				+ " collisions, and generates norms <br>\nto avoid such collisions in"
				+ " the future. Norms are not evaluated nor refined.\n</html>");

		GroupLayout panelEx4Layout = new GroupLayout(panelEx4);
		panelEx4.setLayout(panelEx4Layout);
		panelEx4Layout.setHorizontalGroup(
				panelEx4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx4Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblEx4, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
						.addGap(8, 8, 8)
						.addComponent(btnEx4)
						.addContainerGap())
				);
		panelEx4Layout.setVerticalGroup(
				panelEx4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx4Layout.createSequentialGroup()
						.addGroup(panelEx4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addGroup(panelEx4Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(lblEx4, GroupLayout.PREFERRED_SIZE, 
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGroup(panelEx4Layout.createSequentialGroup()
														.addGap(4, 4, 4)
														.addComponent(btnEx4, GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																.addContainerGap())
				);

		panelEx5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Example 5. Automatic norm generation and norm evaluation", 
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		btnEx5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		btnEx5.setText("Launch example 5");
		btnEx5.setToolTipText("");
		btnEx5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEx5ActionPerformed(evt);
			}
		});

		lblEx5.setText("<html>\nLaunches a road traffic simulation in which a Norm Synthesis "
				+ "Machine <br>\nmonitors the traffic junction, detects car collisions, generates"
				+ "norms <br>\nto avoid such collisions in the future, and iteratively "
				+ "evaluates norms.\n</html>");

		GroupLayout panelEx5Layout = new GroupLayout(panelEx5);
		panelEx5.setLayout(panelEx5Layout);
		panelEx5Layout.setHorizontalGroup(
				panelEx5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx5Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblEx5, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
						.addGap(8, 8, 8)
						.addComponent(btnEx5)
						.addContainerGap())
				);
		panelEx5Layout.setVerticalGroup(
				panelEx5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panelEx5Layout.createSequentialGroup()
						.addGroup(panelEx5Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addGroup(panelEx5Layout.createSequentialGroup()
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblEx5, GroupLayout.PREFERRED_SIZE, 
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addGroup(panelEx5Layout.createSequentialGroup()
														.addGap(4, 4, 4)
														.addComponent(btnEx5, GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																.addContainerGap())
				);

		btnExit.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/exit.png")); // NOI18N
		btnExit.setText("Exit");
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnCloseActionPerformed(evt);
			}
		});

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(panelEx3, GroupLayout.Alignment.TRAILING, 
										GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, 
										Short.MAX_VALUE)
										.addComponent(lblTitle, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(panelEx1, GroupLayout.DEFAULT_SIZE, 
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(panelEx2, GroupLayout.Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(panelEx4, GroupLayout.DEFAULT_SIZE, 
																		GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(panelEx5, GroupLayout.DEFAULT_SIZE,
																				GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
																						.addGap(0, 0, Short.MAX_VALUE)
																						.addComponent(btnExit, GroupLayout.PREFERRED_SIZE, 110,
																								GroupLayout.PREFERRED_SIZE)))
																								.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
						.addComponent(panelEx1, GroupLayout.PREFERRED_SIZE, 
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelEx2, GroupLayout.PREFERRED_SIZE, 
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelEx3, GroupLayout.PREFERRED_SIZE, 
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelEx4, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelEx5, GroupLayout.PREFERRED_SIZE, 
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(btnExit)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	// End of variables declaration//GEN-END:variables
}
