/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.launcher.ui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 *
 * @author Javi
 */
public class DialogAboutNormLab extends javax.swing.JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9105598517037231552L;

	private static BrowserLauncher browserLauncher;

	/**
	 * Creates new form NormLabAboutDialog
	 */
	public DialogAboutNormLab(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		initComponents();
		setResizable(false);
		
		try {
			browserLauncher =  new BrowserLauncher();
			this.linkToWebsite(this.lblSiteJavi, "http://iiia.csic.es/~jmorales");
			this.linkToWebsite(this.lblSiteMaite, "http://www.maia.ub.es/~maite");
			this.linkToWebsite(this.lblSiteJar, "http://iiia.csic.es/~jar");
			this.linkToWebsite(this.lblSiteMichael, "http://www.cs.ox.ac.uk/people/michael.wooldridge/");
			this.linkToWebsite(this.lblSiteWamberto, "http://www.abdn.ac.uk/ncs/profiles/w.w.vasconcelos/");
		}
		catch (Exception e) {
			this.errorMsg("Error while creating browser launcher", e.getMessage());
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
	 * 
	 * @param label
	 * @param url
	 * @param text
	 */
	private void linkToWebsite(JLabel label, final String url) 
			throws Exception {
		label.setText("<html><a href=\"\">Personal website</a> </html>");
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				browserLauncher.openURLinBrowser(url);
			}
		});
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnIIIAActionPerformed(ActionEvent evt) {
		browserLauncher.openURLinBrowser("http://www.iiia.csic.es");
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnUBActionPerformed(ActionEvent evt) {
		browserLauncher.openURLinBrowser("http://www.mat.ub.edu");
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnAberdeenActionPerformed(ActionEvent evt) {
		browserLauncher.openURLinBrowser("http://www.abdn.ac.uk");
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnOxfordActionPerformed(ActionEvent evt) {
		browserLauncher.openURLinBrowser("http://www.ox.ac.uk");
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnContactActionPerformed(java.awt.event.ActionEvent evt) {                                           
		browserLauncher.openURLinBrowser("mailto:jmorales@csic.es");
	}                                          

	/**
	 * 
	 * @param evt
	 */
	private void btnForumsActionPerformed(java.awt.event.ActionEvent evt) {                                          
		// TODO
	}                                         

	/**
	 * 
	 * @param evt
	 */
	private void btnReportBugActionPerformed(java.awt.event.ActionEvent evt) {                                             
		browserLauncher.openURLinBrowser("https://github.com/NormSynthesis/NormLabSimulators/issues");
	}                                            


	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		btnIIIA = new javax.swing.JButton();
		lblTitle = new javax.swing.JLabel();
		btnUB = new javax.swing.JButton();
		panelAuthors = new javax.swing.JPanel();
		lblPhotoMichael = new javax.swing.JLabel();
		lblPhotoJavi = new javax.swing.JLabel();
		lblPhotoMaite = new javax.swing.JLabel();
		lblPhotoWamberto = new javax.swing.JLabel();
		lblPhotoJar = new javax.swing.JLabel();
		lblNameJavi = new javax.swing.JLabel();
		lblNameMaite = new javax.swing.JLabel();
		lblNameJar = new javax.swing.JLabel();
		lblNameMichael = new javax.swing.JLabel();
		lblNameWamberto = new javax.swing.JLabel();
		lblInfoJar = new javax.swing.JLabel();
		lblInfoMaite = new javax.swing.JLabel();
		lblInfoMichael = new javax.swing.JLabel();
		lblInfoJavi = new javax.swing.JLabel();
		lblInfoWamberto = new javax.swing.JLabel();
		lblSiteJavi = new javax.swing.JLabel();
		lblSiteMaite = new javax.swing.JLabel();
		lblSiteJar = new javax.swing.JLabel();
		lblSiteMichael = new javax.swing.JLabel();
		lblSiteWamberto = new javax.swing.JLabel();
		btnOxford = new javax.swing.JButton();
		panelHelp = new javax.swing.JPanel();
		btnContact = new javax.swing.JButton();
		btnForums = new javax.swing.JButton();
		btnReportBug = new javax.swing.JButton();
    lblHelpInfo = new javax.swing.JLabel();
    lblBugInfo = new javax.swing.JLabel();
    lblHelpInfo2 = new javax.swing.JLabel();
    lblBugInfo2 = new javax.swing.JLabel();
		btnAberdeen = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		btnIIIA.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/iiia-csic.jpg")); // NOI18N
		btnIIIA.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnIIIAActionPerformed(evt);
			}
		});

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
		lblTitle.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/microscope.png")); // NOI18N
		lblTitle.setText("NormLab. Version 1.0 (2016)");
		lblTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		btnUB.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/ub.png")); // NOI18N
		btnUB.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnUBActionPerformed(evt);
			}
		});


		panelAuthors.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Authors", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		lblPhotoMichael.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/personal_michael.jpg")); // NOI18N

		lblPhotoJavi.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/personal_javi.jpg")); // NOI18N

		lblPhotoMaite.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/personal_maite.jpg")); // NOI18N

		lblPhotoWamberto.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/personal_wamberto.png")); // NOI18N

		lblPhotoJar.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/personal_jar.jpg")); // NOI18N

		lblNameJavi.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNameJavi.setText("Dr. Javier Morales");

		lblNameMaite.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNameMaite.setText("Dr. Maite L\u00f3pez-S\u00e1nchez");

		lblNameJar.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNameJar.setText("Dr. Juan A. Rodr\u00ed�guez-Aguilar");

		lblNameMichael.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNameMichael.setText("Prof. Michael Wooldridge");

		lblNameWamberto.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNameWamberto.setText("Prof. Wamberto Vasconcelos");

		lblInfoJar.setText("<html> Artificial Intelligence Research Institute (IIIA-CSIC)<br> Bellaterra, Spain  </html>");
		lblInfoMaite.setText("<html> University of Barcelona (UB)<br> Barcelona, Spain\n </html>");
		lblInfoMichael.setText("<html> University of Oxford<br> Oxford, UK </html>");
		lblInfoJavi.setText("<html> Artificial Intelligence Research Institute (IIIA-CSIC)<br> Bellaterra, Spain  </html>");
		lblInfoWamberto.setText("<html> University of Aberdeen<br> Aberdeen, UK </html>");
		lblSiteJavi.setText("<html> <a href=\"\">Personal website</a> </html>");
		lblSiteMaite.setText("<html> <a href=\"\">Personal website</a> </html>");
		lblSiteJar.setText("<html> <a href=\"\">Personal website</a> </html>");
		lblSiteMichael.setText("<html> <a href=\"\">Personal website</a> </html>");
		lblSiteWamberto.setText("<html> <a href=\"\">Personal website</a> </html>");

    javax.swing.GroupLayout panelAuthorsLayout = new javax.swing.GroupLayout(panelAuthors);
    panelAuthors.setLayout(panelAuthorsLayout);
    panelAuthorsLayout.setHorizontalGroup(
        panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelAuthorsLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelAuthorsLayout.createSequentialGroup()
                            .addComponent(lblPhotoMaite)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblNameMaite)
                                .addComponent(lblInfoMaite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblSiteMaite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(panelAuthorsLayout.createSequentialGroup()
                            .addComponent(lblPhotoJavi)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblNameJavi)
                                .addComponent(lblInfoJavi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblSiteJavi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGap(18, 18, 18)
                    .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblPhotoWamberto)
                        .addComponent(lblPhotoMichael))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblNameMichael)
                        .addComponent(lblNameWamberto)
                        .addComponent(lblInfoMichael, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblInfoWamberto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSiteMichael, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSiteWamberto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addComponent(lblPhotoJar)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblNameJar)
                        .addComponent(lblInfoJar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblSiteJar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    panelAuthorsLayout.setVerticalGroup(
        panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelAuthorsLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblPhotoJavi)
                .addComponent(lblPhotoMichael)
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addComponent(lblNameMichael)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblInfoMichael, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblSiteMichael, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addComponent(lblNameJavi)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblInfoJavi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblSiteJavi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(8, 8, 8)
            .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblPhotoMaite)
                .addComponent(lblPhotoWamberto)
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addComponent(lblNameMaite)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblInfoMaite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblSiteMaite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addComponent(lblNameWamberto)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblInfoWamberto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblSiteWamberto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(panelAuthorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblPhotoJar)
                .addGroup(panelAuthorsLayout.createSequentialGroup()
                    .addComponent(lblNameJar)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblInfoJar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblSiteJar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    btnOxford.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/oxford.jpg")); // NOI18N
    btnOxford.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnOxfordActionPerformed(evt);
        }
    });

    panelHelp.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Help", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

    btnContact.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/contact.png")); // NOI18N
    btnContact.setText("Contact for help");
    btnContact.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnContactActionPerformed(evt);
        }
    });

    btnForums.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/forums.png")); // NOI18N
    btnForums.setText("Ask in the forums");
    btnForums.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnForumsActionPerformed(evt);
        }
    });

    btnReportBug.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/bug.png")); // NOI18N
    btnReportBug.setText("Report bug");
    btnReportBug.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnReportBugActionPerformed(evt);
        }
    });

    lblHelpInfo.setText("Do you need help with NormLab? Contact the authors");
    lblHelpInfo.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    lblBugInfo.setText("Did you find a bug? Please report it and contribute");
    lblBugInfo.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    lblHelpInfo2.setText("or ask for your doubts in the forums");

    lblBugInfo2.setText("to the project! We really appreciate your help");

    javax.swing.GroupLayout panelHelpLayout = new javax.swing.GroupLayout(panelHelp);
    panelHelp.setLayout(panelHelpLayout);
    panelHelpLayout.setHorizontalGroup(
        panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelHelpLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblHelpInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblHelpInfo2)
                .addGroup(panelHelpLayout.createSequentialGroup()
                    .addComponent(btnContact)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnForums)))
            .addGap(18, 18, 18)
            .addGroup(panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnReportBug)
                .addComponent(lblBugInfo2)
                .addComponent(lblBugInfo))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    panelHelpLayout.setVerticalGroup(
        panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelHelpLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblHelpInfo)
                .addComponent(lblBugInfo))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblHelpInfo2)
                .addComponent(lblBugInfo2))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(panelHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnContact)
                .addComponent(btnForums)
                .addComponent(btnReportBug))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    btnAberdeen.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/aberdeen.png")); // NOI18N
    btnAberdeen.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnAberdeenActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelAuthors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnIIIA, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnUB, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnOxford, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnAberdeen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(panelAuthors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(panelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnIIIA)
                .addComponent(btnUB)
                .addComponent(btnOxford)
                .addComponent(btnAberdeen))
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
			java.util.logging.Logger.getLogger(DialogAboutNormLab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogAboutNormLab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogAboutNormLab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogAboutNormLab.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogAboutNormLab dialog = new DialogAboutNormLab(new javax.swing.JFrame(), true);
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
	private javax.swing.JButton btnAberdeen;
	private javax.swing.JButton btnContact;
	private javax.swing.JButton btnForums;
	private javax.swing.JButton btnIIIA;
	private javax.swing.JButton btnOxford;
	private javax.swing.JButton btnReportBug;
	private javax.swing.JButton btnUB;
  private javax.swing.JLabel lblBugInfo;
  private javax.swing.JLabel lblBugInfo2;
  private javax.swing.JLabel lblHelpInfo;
  private javax.swing.JLabel lblHelpInfo2;
	private javax.swing.JLabel lblInfoJar;
	private javax.swing.JLabel lblInfoJavi;
	private javax.swing.JLabel lblInfoMaite;
	private javax.swing.JLabel lblInfoMichael;
	private javax.swing.JLabel lblInfoWamberto;
	private javax.swing.JLabel lblNameJar;
	private javax.swing.JLabel lblNameJavi;
	private javax.swing.JLabel lblNameMaite;
	private javax.swing.JLabel lblNameMichael;
	private javax.swing.JLabel lblNameWamberto;
	private javax.swing.JLabel lblPhotoJar;
	private javax.swing.JLabel lblPhotoJavi;
	private javax.swing.JLabel lblPhotoMaite;
	private javax.swing.JLabel lblPhotoMichael;
	private javax.swing.JLabel lblPhotoWamberto;
	private javax.swing.JLabel lblSiteJar;
	private javax.swing.JLabel lblSiteJavi;
	private javax.swing.JLabel lblSiteMaite;
	private javax.swing.JLabel lblSiteMichael;
	private javax.swing.JLabel lblSiteWamberto;
	private javax.swing.JLabel lblTitle;
	private javax.swing.JPanel panelAuthors;
	private javax.swing.JPanel panelHelp;
	// End of variables declaration//GEN-END:variables
}