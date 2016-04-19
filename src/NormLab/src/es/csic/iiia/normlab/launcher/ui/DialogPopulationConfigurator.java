/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.normlab.launcher.ui;

import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import es.csic.iiia.normlab.launcher.onlinecomm.ComplaintProfile;
import es.csic.iiia.normlab.launcher.onlinecomm.OnlineCommunityPopulation;
import es.csic.iiia.normlab.launcher.onlinecomm.OnlineCommunityPopulation.UserType;
import es.csic.iiia.normlab.launcher.onlinecomm.UploadProfile;
import es.csic.iiia.normlab.launcher.onlinecomm.ViewProfile;
import es.csic.iiia.normlab.launcher.utils.JDecimalField;
import es.csic.iiia.normlab.launcher.utils.JIntegerField;

/**
 *
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class DialogPopulationConfigurator extends JDialog {

	/* */
	private static final long serialVersionUID = 8476544015656480598L;
	private static String populationName;
	
	/* */
	private String availablePopsDir = "files/onlinecomm/populations/";
	private String patternPopsDir = "files/onlinecomm/populations/.pattern/";
	private OnlineCommunityPopulation population;

	private String uploadFreqKey = "uploadFreq";
	private String uploadCorrectKey = "uploadCorrect";
	private String uploadSpamKey = "uploadSpam";
	private String uploadPornKey = "uploadPorn";
	private String uploadViolentKey = "uploadViolent";
	private String uploadInsultKey = "uploadInsult";
	private String viewForumKey = "viewForum";
	private String viewTheReporterKey = "viewTheReporter";
	private String viewMultimediaKey = "viewMultimedia";
	private String viewModeKey = "viewMode";
	private String complainSpamKey = "complainSpam";
	private String complainPornKey = "complainPorn";
	private String complainViolentKey = "complainViolent";
	private String complainInsultKey = "complainInsult";
	
	private Map<UserType, Map<String, String>> uploadProfile;
	private Map<UserType, Map<String, String>> viewProfile;
	private Map<UserType, Map<String, String>> complainProfile;
	
	private String numModerates;
	private String numSpammers;
	private String numPornographics;
	private String numViolents;
	private String numRudes;
	
	private List<UserType> userTypes;
	private UserType userType;
	
	private boolean userSaved;
	
	/**
	 * Creates new form DialogChoosePopulation
	 * @param population2 
	 */
	public DialogPopulationConfigurator(Frame parent, boolean modal,
			String population) {
		
		super(parent, modal);
		this.prepareDataStructures();
		
		initComponents();
		setResizable(false);

		populationName = population;
		this.loadAvailablePopulations();
		if(!population.equals("")) {
			this.cbAvailablePop.setSelectedItem(population);
		}
		
		userSaved = false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean userSaved() {
		return this.userSaved;
	}
	
	/**
	 * 
	 * @return
	 */
	public OnlineCommunityPopulation getPopulation() {
		return this.population;
	}

	/**
	 * 
	 */
	private void prepareDataStructures() {
		this.userType = UserType.Moderate;
		
		this.userTypes = new ArrayList<UserType>();
		this.userTypes.add(UserType.Moderate);
		this.userTypes.add(UserType.Spammer);
		this.userTypes.add(UserType.Pornographic);
		this.userTypes.add(UserType.Violent);
		this.userTypes.add(UserType.Rude);
		
		this.uploadProfile = new HashMap<UserType, Map<String, String>>();
		this.viewProfile = new HashMap<UserType, Map<String, String>>();
		this.complainProfile = new HashMap<UserType, Map<String, String>>();	  

		for(UserType type : this.userTypes) {
			this.uploadProfile.put(type, new HashMap<String, String>());
			this.viewProfile.put(type, new HashMap<String, String>());
			this.complainProfile.put(type, new HashMap<String, String>());
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
	 */
	private void loadAvailablePopulations() {
		File avPopsFolder = new File (availablePopsDir);
		List<File> availablePops = Arrays.asList(avPopsFolder.listFiles(
				new FilenameFilter() {
					
			public boolean accept(File directory, String fileName) {
				return fileName.endsWith(".xml");
			}    
		}));

		/* If there are no available populations, create a new one */
		if(availablePops.isEmpty()) {
			this.createNewPopulation();
		}

		/* Otherwise, retrieve available populations */
		else {
			for(File avPop : availablePops){
				String avPopName = avPop.getName();
				if(avPopName.contains(".xml")) {
					this.cbAvailablePop.addItem(
							avPopName.substring(0,avPopName.length()-4));	
				}
			}
		}
	}

	/**
	 * 
	 */
	private void createNewPopulation() {
		try {
			this.loadPopulation(patternPopsDir,	"emptyPopulation");
			this.displayPopulationConfig();
		}
		catch (Exception e) {
			this.errorMsg("Error while creating new population", e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param population
	 * @throws Exception 
	 */
	private void loadPopulation(String populationDir, String populationName)
			throws Exception {

		this.population = new OnlineCommunityPopulation(populationDir, 
				populationName + ".xml");
		
		populationName = population.getName();
		this.numModerates = String.valueOf(population.getNumUsers(UserType.Moderate));
		this.numSpammers = String.valueOf(population.getNumUsers(UserType.Spammer));
		this.numPornographics = String.valueOf(population.getNumUsers(UserType.Pornographic));
		this.numViolents = String.valueOf(population.getNumUsers(UserType.Violent));
		this.numRudes = String.valueOf(population.getNumUsers(UserType.Rude));
		
		for(UserType usrType : this.userTypes) {

			UploadProfile upProfile = this.population.getUploadProfile(usrType);
			
			this.uploadProfile.get(usrType).put(uploadFreqKey, 
					String.valueOf(upProfile.getUploadProbability()));
			this.uploadProfile.get(usrType).put(uploadCorrectKey, 
					String.valueOf(upProfile.getCorrect()));
			this.uploadProfile.get(usrType).put(uploadSpamKey, 
					String.valueOf(upProfile.getSpam()));
			this.uploadProfile.get(usrType).put(uploadPornKey, 
					String.valueOf(upProfile.getPorn()));
			this.uploadProfile.get(usrType).put(uploadViolentKey, 
					String.valueOf(upProfile.getViolent()));
			this.uploadProfile.get(usrType).put(uploadInsultKey, 
					String.valueOf(upProfile.getInsult()));
			
			ViewProfile vProfile = this.population.getViewProfile(usrType);
			
			this.viewProfile.get(usrType).put(viewForumKey, 
					String.valueOf(vProfile.getForum()));
			this.viewProfile.get(usrType).put(viewTheReporterKey, 
					String.valueOf(vProfile.getTheReporter()));
			this.viewProfile.get(usrType).put(viewMultimediaKey, 
					String.valueOf(vProfile.getPhotoVideo()));
			this.viewProfile.get(usrType).put(viewModeKey, 
					String.valueOf(vProfile.getViewMode()));
			
			ComplaintProfile cpProfile = this.population.getComplainProfile(usrType);
			
			this.complainProfile.get(usrType).put(complainSpamKey,
					String.valueOf(cpProfile.getSpam()));
			this.complainProfile.get(usrType).put(complainPornKey,
					String.valueOf(cpProfile.getPorn()));
			this.complainProfile.get(usrType).put(complainViolentKey,
					String.valueOf(cpProfile.getViolent()));
			this.complainProfile.get(usrType).put(complainInsultKey,
					String.valueOf(cpProfile.getInsult()));
		}
	}

	/**
	 * @throws Exception 
	 * @throws NumberFormatException 
	 * 
	 */
	private void savePopulation() 
			throws NumberFormatException, Exception {

		/* Save population attributes */
		this.population.setName(populationName);
		this.population.setNumUsers(UserType.Moderate, Integer.valueOf(this.numModerates));
		this.population.setNumUsers(UserType.Spammer, Integer.valueOf(this.numSpammers));
		this.population.setNumUsers(UserType.Pornographic, Integer.valueOf(this.numPornographics));
		this.population.setNumUsers(UserType.Violent, Integer.valueOf(this.numViolents));
		this.population.setNumUsers(UserType.Rude, Integer.valueOf(this.numRudes));
		
		for(UserType usrType : this.userTypes) {
			UploadProfile upProfile = this.population.getUploadProfile(usrType);
			
			upProfile.setUploadProbability(Double.valueOf(
					this.uploadProfile.get(usrType).get(uploadFreqKey)));
			upProfile.setCorrect(Double.valueOf(
					this.uploadProfile.get(usrType).get(uploadCorrectKey)));
			upProfile.setSpam(Double.valueOf(
					this.uploadProfile.get(usrType).get(uploadSpamKey)));
			upProfile.setPorn(Double.valueOf(
					this.uploadProfile.get(usrType).get(uploadPornKey)));
			upProfile.setViolent(Double.valueOf(
					this.uploadProfile.get(usrType).get(uploadViolentKey)));
			upProfile.setInsult(Double.valueOf(
					this.uploadProfile.get(usrType).get(uploadInsultKey)));

			ViewProfile vProfile = this.population.getViewProfile(usrType);
			vProfile.setForum(Double.valueOf(
					this.viewProfile.get(usrType).get(viewForumKey)));
			vProfile.setTheReporter(Double.valueOf(
					this.viewProfile.get(usrType).get(viewTheReporterKey)));
			vProfile.setPhotoVideo(Double.valueOf(
					this.viewProfile.get(usrType).get(viewMultimediaKey)));
			vProfile.setViewMode(Integer.valueOf(
					this.viewProfile.get(usrType).get(viewModeKey)));
			
			ComplaintProfile cpProfile = this.population.getComplainProfile(usrType);
			cpProfile.setSpam(Double.valueOf(
					this.complainProfile.get(usrType).get(complainSpamKey)));
			cpProfile.setPorn(Double.valueOf(
					this.complainProfile.get(usrType).get(complainPornKey)));
			cpProfile.setViolent(Double.valueOf(
					this.complainProfile.get(usrType).get(complainViolentKey)));
			cpProfile.setInsult(Double.valueOf(
					this.complainProfile.get(usrType).get(complainInsultKey)));
		}

		/* Finally save population to XML */
		this.population.saveToXML(availablePopsDir, population.getName() + ".xml");
		
		this.userSaved = true;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void displayPopulationConfig() throws Exception {
		this.txtNamePop.setText(population.getName());
		this.txtNumModerates.setText(String.valueOf(population.getNumUsers(UserType.Moderate)));
		this.txtNumSpammers.setText(String.valueOf(population.getNumUsers(UserType.Spammer)));
		this.txtNumPornographics.setText(String.valueOf(population.getNumUsers(UserType.Pornographic)));
		this.txtNumViolent.setText(String.valueOf(population.getNumUsers(UserType.Violent)));
		this.txtNumRudes.setText(String.valueOf(population.getNumUsers(UserType.Rude)));

		String selUsrProfile = (String)this.cbUserProfiles.getSelectedItem();
		UserType type = UserType.valueOf(selUsrProfile);

		/* Load user type profile */
		this.displayUserProfile(type);
	}

	/**
	 * 
	 * @param type
	 */
	private void displayUserProfile(UserType type) {

		this.txtUploadFreq.setText(String.valueOf(uploadProfile.get(type).get(uploadFreqKey)));
		this.txtUploadCorrect.setText(String.valueOf(uploadProfile.get(type).get(uploadCorrectKey)));
		this.txtUploadSpam.setText(String.valueOf(uploadProfile.get(type).get(uploadSpamKey)));
		this.txtUploadPorn.setText(String.valueOf(uploadProfile.get(type).get(uploadPornKey)));
		this.txtUploadViolent.setText(String.valueOf(uploadProfile.get(type).get(uploadViolentKey)));
		this.txtUploadInsult.setText(String.valueOf(uploadProfile.get(type).get(uploadInsultKey)));
		
		this.txtViewForum.setText(String.valueOf(viewProfile.get(type).get(viewForumKey)));
		this.txtViewTheReporter.setText(String.valueOf(viewProfile.get(type).get(viewTheReporterKey)));
		this.txtViewMultimedia.setText(String.valueOf(viewProfile.get(type).get(viewMultimediaKey)));

		this.btnGroupViewCriterion.clearSelection();

		switch(Integer.valueOf(viewProfile.get(type).get(viewModeKey))) {
		case 0:	this.rbNewest.setSelected(true);			break;
		case 1:	this.rbMostViewed.setSelected(true);	break;
		case 2:	this.rbRandom.setSelected(true);			break;
		}

		this.txtComplainSpam.setText(String.valueOf(complainProfile.get(type).get(complainSpamKey)));
		this.txtComplainPorn.setText(String.valueOf(complainProfile.get(type).get(complainPornKey)));
		this.txtComplainViolent.setText(String.valueOf(complainProfile.get(type).get(complainViolentKey)));
		this.txtComplainInsult.setText(String.valueOf(complainProfile.get(type).get(complainInsultKey)));
	}

	/**
	 * 
	 */
	private void resetUploadProfile() {
		this.txtUploadFreq.setText("0.0");
		this.txtUploadCorrect.setText("0.0");
		this.txtUploadSpam.setText("0.0");
		this.txtUploadPorn.setText("0.0");
		this.txtUploadViolent.setText("0.0");
		this.txtUploadInsult.setText("0.0");	  
	}

	/**
	 * 
	 */
	private void resetViewProfile() {
		this.btnGroupViewCriterion.clearSelection();
		this.txtViewForum.setText("0.0");
		this.txtViewTheReporter.setText("0.0");
		this.txtViewMultimedia.setText("0.0");
	}

	/**
	 * 
	 */
	private void resetComplainProfile() {
		this.txtComplainSpam.setText("0.0");
		this.txtComplainPorn.setText("0.0");
		this.txtComplainViolent.setText("0.0");
		this.txtComplainInsult.setText("0.0");	  
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnCreatePopActionPerformed(java.awt.event.ActionEvent evt) {
		this.createNewPopulation();
	}

	/**
	 * 
	 * @param evt
	 */
	private void cbAvailablePopActionPerformed(java.awt.event.ActionEvent evt) {
		String population = (String)this.cbAvailablePop.getSelectedItem();

		try {
			this.loadPopulation(availablePopsDir, population);
			this.displayPopulationConfig();
		}
		catch (Exception e) {
			this.errorMsg("Error while loading population configuration", e.getMessage());
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void cbUserProfilesActionPerformed(java.awt.event.ActionEvent evt) {
		String selType = (String)this.cbUserProfiles.getSelectedItem();
		UserType type = UserType.valueOf(selType);
		this.userType = type;

		try {
//			UploadProfile upProfile = population.getUploadProfile(type);
//			ViewProfile vProfile = population.getViewProfile(type);
//			ComplaintProfile cpProfile = population.getComplainProfile(type);

			this.displayUserProfile(type);
		}
		catch (Exception e) {
			this.resetUploadProfile();
			this.resetViewProfile();
			this.resetComplainProfile();
		}
	}

	/**
	 * 
	 * @param evt
	 */
	private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			this.savePopulation();
			this.dispose();
		}
		catch (Exception e) {
			this.errorMsg("Error while saving population configuration", e.getMessage());		
		}
	}

  /**
	 * 
	 * @param evt
	 */
	private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {                                        
		this.dispose();
	}

	/**
	 * 
	 * @param evt
	 */
  private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {                                         
  	
  }
  
	//------------------------------------------------------------------------------
	//
	//------------------------------------------------------------------------------

	private void txtNamePopActionPerformed(DocumentEvent e) {
		populationName = this.txtNamePop.getText();
	}                                          

	private void txtNumModeratesActionPerformed(DocumentEvent e) {
		this.numModerates = this.txtNumModerates.getText();
	}                                               

	private void txtNumSpammersActionPerformed(DocumentEvent e) {                                               
		this.numSpammers = this.txtNumSpammers.getText();
	}                                              

	private void txtNumPornographicsActionPerformed(DocumentEvent e) {                                                    
		this.numPornographics = this.txtNumPornographics.getText();
	}                                                   

	private void txtNumViolentActionPerformed(DocumentEvent e) {                                              
		this.numViolents = this.txtNumViolent.getText();
	}                                             

	private void txtNumRudesActionPerformed(DocumentEvent e) {                                            
		this.numRudes = this.txtNumRudes.getText();
	}              
	
	private void rbNewestActionPerformed(java.awt.event.ActionEvent evt) {                                         
		this.viewProfile.get(this.userType).put(viewModeKey, "0");
	}                                        

	private void rbMostViewedActionPerformed(java.awt.event.ActionEvent evt) {                                             
		this.viewProfile.get(this.userType).put(viewModeKey, "1");
	}                                            

	private void rbRandomActionPerformed(java.awt.event.ActionEvent evt) {                                         
		this.viewProfile.get(this.userType).put(viewModeKey, "2");
	}   

	private void txtUploadFreqActionPerformed(DocumentEvent e) {
		this.uploadProfile.get(this.userType).put(uploadFreqKey, this.txtUploadFreq.getText());
	}                                             

	private void txtUploadCorrectActionPerformed(DocumentEvent e) {
		this.uploadProfile.get(this.userType).put(uploadCorrectKey, this.txtUploadCorrect.getText());
	}

	private void txtUploadSpamActionPerformed(DocumentEvent e) {                                              
		this.uploadProfile.get(this.userType).put(uploadSpamKey, this.txtUploadSpam.getText());
	}                                             

	private void txtUploadPornActionPerformed(DocumentEvent e) {                                              
		this.uploadProfile.get(this.userType).put(uploadPornKey, this.txtUploadPorn.getText());
	}                                             

	private void txtUploadViolentActionPerformed(DocumentEvent e) {                                                 
		this.uploadProfile.get(this.userType).put(uploadViolentKey, this.txtUploadViolent.getText());
	}                                                

	private void txtUploadInsultActionPerformed(DocumentEvent e) {                                                
		this.uploadProfile.get(this.userType).put(uploadInsultKey, this.txtUploadInsult.getText());
	}                                               

	private void txtViewTheReporterActionPerformed(DocumentEvent e) {                                                   
		this.viewProfile.get(this.userType).put(viewTheReporterKey, this.txtViewTheReporter.getText());
	}                                                  

	private void txtViewMultimediaActionPerformed(DocumentEvent e) {                                                  
		this.viewProfile.get(this.userType).put(viewMultimediaKey, this.txtViewMultimedia.getText());
	}                                                 

	private void txtViewForumActionPerformed(DocumentEvent e) {                                             
		this.viewProfile.get(this.userType).put(viewForumKey, this.txtViewForum.getText());
	}                                            

	private void txtComplainSpamActionPerformed(DocumentEvent e) {                                                
		this.complainProfile.get(this.userType).put(complainSpamKey, this.txtComplainSpam.getText());
	}                                               

	private void txtComplainPornActionPerformed(DocumentEvent e) {                                                
		this.complainProfile.get(this.userType).put(complainPornKey, this.txtComplainPorn.getText());
	}                                               

	private void txtComplainViolentActionPerformed(DocumentEvent e) {                                                   
		this.complainProfile.get(this.userType).put(complainViolentKey, this.txtComplainViolent.getText());
	}                                                  

	private void txtComplainInsultActionPerformed(DocumentEvent e) {                                                  
		this.complainProfile.get(this.userType).put(complainInsultKey, this.txtComplainInsult.getText());
	}                                                 
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

    btnDelete = new javax.swing.JButton();
		btnGroupViewCriterion = new ButtonGroup();
		lblTitle = new JLabel();
		lblAvailablePop = new JLabel();
		btnCreatePop = new JButton();
		btnSave = new JButton();
		panelPop = new JPanel();
		txtNamePop = new JTextField();
		lblUserProfiles = new JLabel();
		panelUserProfile = new JPanel();
		panelUploadProfile = new JPanel();
		txtUploadSpam = new JDecimalField(2);
		txtUploadPorn = new JDecimalField(2);
		txtUploadViolent = new JDecimalField(2);
		lblUploadFreq = new JLabel();
		txtUploadInsult = new JDecimalField(2);
		lblUploadCorrect = new JLabel();
		lblUploadSpam = new JLabel();
		lblUploadPorn = new JLabel();
		lblUploadViolent = new JLabel();
		lblUploadInsult = new JLabel();
		txtUploadFreq = new JDecimalField(2);
		txtUploadCorrect = new JDecimalField(2);
		sepUploadProfile = new JSeparator();
		panelViewProfile = new JPanel();
		lblTheReporter = new JLabel();
		lblMultimedia = new JLabel();
		lblForum = new JLabel();
		txtViewTheReporter = new JDecimalField(2);
		txtViewMultimedia = new JDecimalField(2);
		txtViewForum = new JDecimalField(2);
		lblViewCriterion = new JLabel();
		rbNewest = new JRadioButton();
		rbMostViewed = new JRadioButton();
		rbRandom = new JRadioButton();
		panelComplainProfile = new JPanel();
		lblProbComplain = new JLabel();
		lblComplainSpam = new JLabel();
		txtComplainSpam = new JDecimalField(2);
		txtComplainPorn = new JDecimalField(2);
		txtComplainViolent = new JDecimalField(2);
		txtComplainInsult = new JDecimalField(2);
		lblComplainPorn = new JLabel();
		lblComplainViolent = new JLabel();
		lblComplainInsult = new JLabel();
		cbUserProfiles = new JComboBox<String>();
		panelNumAgents = new JPanel();
		txtNumModerates = new JIntegerField(0);
		txtNumSpammers = new JIntegerField(0);
		txtNumPornographics = new JIntegerField(0);
		txtNumViolent = new JIntegerField(0);
		lblNumSpammers = new JLabel();
		txtNumRudes = new JIntegerField(0);
		lblNumModerates = new JLabel();
		lblNumPornographics = new JLabel();
		lblNumViolent = new JLabel();
		lblNumRudes = new JLabel();
		sepUserProfile = new JSeparator();
		lblNamePop = new JLabel();
		cbAvailablePop = new JComboBox<String>();
		btnExit = new JButton();

		this.cbUserProfiles.addItem(UserType.Moderate.name());
		this.cbUserProfiles.addItem(UserType.Spammer.name());
		this.cbUserProfiles.addItem(UserType.Pornographic.name());
		this.cbUserProfiles.addItem(UserType.Violent.name());
		this.cbUserProfiles.addItem(UserType.Rude.name());

		this.addTxtListeners();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		lblTitle.setBackground(new java.awt.Color(255, 255, 255));
		lblTitle.setFont(new java.awt.Font("Tahoma", 0, 19)); // NOI18N
		lblTitle.setIcon(new ImageIcon("misc/launcher/icons/onlinecomm.png")); // NOI18N
		lblTitle.setText("User population configurator");
		lblTitle.setBorder(BorderFactory.createEtchedBorder());
		lblTitle.setOpaque(true);

		lblAvailablePop.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
		lblAvailablePop.setText("Available populations");

		btnCreatePop.setIcon(new ImageIcon("misc/launcher/icons/create.png")); // NOI18N
		btnCreatePop.setText("Create new");
		btnCreatePop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnCreatePopActionPerformed(evt);
			}
		});

		btnSave.setIcon(new ImageIcon("misc/launcher/icons/save.png")); // NOI18N
		btnSave.setText("Save and exit");
		btnSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSaveActionPerformed(evt);
			}
		});

		panelPop.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Selected population", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		lblUserProfiles.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblUserProfiles.setText("User profiles");

		panelUserProfile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Selected user profile", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		panelUploadProfile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), "Upload profile", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblUploadFreq.setText("Upload frequency");

		lblUploadCorrect.setText("% correct contents");

		lblUploadSpam.setText("% spam contents");

		lblUploadPorn.setText("% porn contents");

		lblUploadViolent.setText("% violent contents");

		lblUploadInsult.setText("% insult contents");

		txtUploadCorrect.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadCorrectActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadCorrectActionPerformed(e);
			}
		});

		javax.swing.GroupLayout panelUploadProfileLayout = new javax.swing.GroupLayout(panelUploadProfile);
		panelUploadProfile.setLayout(panelUploadProfileLayout);
		panelUploadProfileLayout.setHorizontalGroup(
				panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelUploadProfileLayout.createSequentialGroup()
						.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelUploadProfileLayout.createSequentialGroup()
										.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(lblUploadCorrect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
												.addGroup(panelUploadProfileLayout.createSequentialGroup()
														.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
																.addComponent(lblUploadPorn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(lblUploadViolent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(lblUploadInsult, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
																.addGap(0, 0, Short.MAX_VALUE))
																.addComponent(lblUploadSpam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(lblUploadFreq, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addGroup(panelUploadProfileLayout.createSequentialGroup()
																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																				.addComponent(txtUploadFreq, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
																				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelUploadProfileLayout.createSequentialGroup()
																						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																						.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																								.addComponent(txtUploadCorrect, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(txtUploadSpam, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(txtUploadPorn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(txtUploadViolent, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addComponent(txtUploadInsult, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))))
																								.addComponent(sepUploadProfile))
																								.addContainerGap())
				);
		panelUploadProfileLayout.setVerticalGroup(
				panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelUploadProfileLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblUploadFreq)
								.addComponent(txtUploadFreq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(18, 18, 18)
								.addComponent(sepUploadProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(lblUploadCorrect)
										.addComponent(txtUploadCorrect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(lblUploadSpam)
												.addComponent(txtUploadSpam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(lblUploadPorn)
														.addComponent(txtUploadPorn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(lblUploadViolent)
																.addComponent(txtUploadViolent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(panelUploadProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(lblUploadInsult)
																		.addComponent(txtUploadInsult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);


		panelViewProfile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), "View profile", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblTheReporter.setText("% in 'The Reporter'");

		lblMultimedia.setText("% in 'Multimedia'");

		lblForum.setText("% in 'Forum'");

		lblViewCriterion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblViewCriterion.setText("View criterion");

		btnGroupViewCriterion.add(rbNewest);
		rbNewest.setText("Newest");
		rbNewest.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbNewestActionPerformed(evt);
			}
		});

		btnGroupViewCriterion.add(rbMostViewed);
		rbMostViewed.setText("Most viewed");
		rbMostViewed.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbMostViewedActionPerformed(evt);
			}
		});

		btnGroupViewCriterion.add(rbRandom);
		rbRandom.setText("Random");
		rbRandom.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rbRandomActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout panelViewProfileLayout = new javax.swing.GroupLayout(panelViewProfile);
		panelViewProfile.setLayout(panelViewProfileLayout);
		panelViewProfileLayout.setHorizontalGroup(
				panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelViewProfileLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(panelViewProfileLayout.createSequentialGroup()
										.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(lblForum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblMultimedia, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblTheReporter, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
														.addComponent(txtViewForum)
														.addComponent(txtViewMultimedia)
														.addComponent(txtViewTheReporter, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addGroup(panelViewProfileLayout.createSequentialGroup()
																.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addComponent(rbRandom)
																		.addComponent(rbMostViewed)
																		.addComponent(rbNewest)
																		.addComponent(lblViewCriterion))
																		.addGap(0, 0, Short.MAX_VALUE)))
																		.addContainerGap())
				);
		panelViewProfileLayout.setVerticalGroup(
				panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelViewProfileLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblTheReporter)
								.addComponent(txtViewTheReporter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(txtViewMultimedia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(lblMultimedia))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelViewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(txtViewForum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblForum))
												.addGap(18, 18, 18)
												.addComponent(lblViewCriterion)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(rbNewest)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(rbMostViewed)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(rbRandom)
												.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		panelComplainProfile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), "Complain profile", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 204))); // NOI18N

		lblProbComplain.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblProbComplain.setHorizontalAlignment(SwingConstants.CENTER);
		lblProbComplain.setText("<html>\nProbability to complain about viewed...\n</html>");

		lblComplainSpam.setText("Spam contents");

		lblComplainPorn.setText("Porn contents");

		lblComplainViolent.setText("Violent contents");

		lblComplainInsult.setText("Insult contents");

		javax.swing.GroupLayout panelComplainProfileLayout = new javax.swing.GroupLayout(panelComplainProfile);
		panelComplainProfile.setLayout(panelComplainProfileLayout);
		panelComplainProfileLayout.setHorizontalGroup(
				panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelComplainProfileLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(lblProbComplain, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
								.addGroup(panelComplainProfileLayout.createSequentialGroup()
										.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(lblComplainSpam)
												.addComponent(lblComplainPorn)
												.addComponent(lblComplainViolent)
												.addComponent(lblComplainInsult))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(txtComplainPorn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(txtComplainSpam, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(txtComplainViolent, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(txtComplainInsult, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))))
														.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelComplainProfileLayout.setVerticalGroup(
				panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelComplainProfileLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblProbComplain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
						.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(lblComplainSpam)
								.addComponent(txtComplainSpam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(txtComplainPorn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(lblComplainPorn))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(txtComplainViolent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(lblComplainViolent))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panelComplainProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(txtComplainInsult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(lblComplainInsult))
														.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		javax.swing.GroupLayout panelUserProfileLayout = new javax.swing.GroupLayout(panelUserProfile);
		panelUserProfile.setLayout(panelUserProfileLayout);
		panelUserProfileLayout.setHorizontalGroup(
				panelUserProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelUserProfileLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panelUploadProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelViewProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(panelComplainProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelUserProfileLayout.setVerticalGroup(
				panelUserProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelUserProfileLayout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(panelUserProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelUploadProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGroup(panelUserProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
										.addComponent(panelComplainProfile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panelViewProfile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
				);


		cbUserProfiles.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbUserProfilesActionPerformed(evt);
			}
		});

		panelNumAgents.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Number of agents", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

		lblNumSpammers.setText("spammers");

		lblNumModerates.setText("moderates");

		lblNumPornographics.setText("pornographic");

		lblNumViolent.setText("violent");

		lblNumRudes.setText("rude");

		GroupLayout panelNumAgentsLayout = new GroupLayout(panelNumAgents);
		panelNumAgents.setLayout(panelNumAgentsLayout);
		panelNumAgentsLayout.setHorizontalGroup(
				panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(panelNumAgentsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(lblNumSpammers)
								.addComponent(lblNumModerates))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(txtNumModerates, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
										.addComponent(txtNumSpammers, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
										.addGap(18, 18, 18)
										.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(lblNumPornographics)
												.addComponent(lblNumViolent))
												.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addComponent(txtNumViolent, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
														.addGroup(panelNumAgentsLayout.createSequentialGroup()
																.addComponent(txtNumPornographics, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18, 18)
																.addComponent(lblNumRudes)
																.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(txtNumRudes, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)))
																.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		panelNumAgentsLayout.setVerticalGroup(
				panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(panelNumAgentsLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblNumModerates)
								.addComponent(lblNumPornographics)
								.addComponent(lblNumRudes)
								.addComponent(txtNumModerates, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtNumPornographics, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtNumRudes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addGroup(panelNumAgentsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(lblNumSpammers)
												.addComponent(txtNumSpammers, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblNumViolent))
												.addComponent(txtNumViolent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);

		lblNamePop.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		lblNamePop.setText("Name");

    btnDelete.setIcon(new javax.swing.ImageIcon("misc/launcher/icons/delete_small.png")); // NOI18N
    btnDelete.setText("Delete population");
    btnDelete.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnDeleteActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout panelPopLayout = new javax.swing.GroupLayout(panelPop);
    panelPop.setLayout(panelPopLayout);
    panelPopLayout.setHorizontalGroup(
        panelPopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelPopLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelPopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelUserProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sepUserProfile)
                .addGroup(panelPopLayout.createSequentialGroup()
                    .addComponent(lblUserProfiles)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cbUserProfiles, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(panelPopLayout.createSequentialGroup()
                    .addGroup(panelPopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtNamePop, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblNamePop)
                        .addComponent(btnDelete))
                    .addGap(18, 18, 18)
                    .addComponent(panelNumAgents, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addContainerGap())
    );
    panelPopLayout.setVerticalGroup(
        panelPopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(panelPopLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(panelPopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelPopLayout.createSequentialGroup()
                    .addComponent(lblNamePop)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(txtNamePop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(btnDelete))
                .addComponent(panelNumAgents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(sepUserProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(panelPopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblUserProfiles)
                .addComponent(cbUserProfiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(panelUserProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

		cbAvailablePop.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		cbAvailablePop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cbAvailablePopActionPerformed(evt);
			}
		});

		btnExit.setIcon(new ImageIcon("misc/launcher/icons/exit.png")); // NOI18N
		btnExit.setText("Exit");
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExitActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(panelPop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
										.addComponent(btnExit)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnSave))
										.addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGroup(layout.createSequentialGroup()
												.addComponent(lblAvailablePop)
												.addGap(18, 18, 18)
												.addComponent(cbAvailablePop, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnCreatePop)))
												.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTitle)
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(btnCreatePop)
								.addComponent(lblAvailablePop)
								.addComponent(cbAvailablePop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(panelPop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(btnSave)
										.addComponent(btnExit))
										.addContainerGap())
				);

		pack();
	}// </editor-fold>//GEN-END:initComponents

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
			java.util.logging.Logger.getLogger(DialogPopulationConfigurator.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(DialogPopulationConfigurator.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(DialogPopulationConfigurator.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(DialogPopulationConfigurator.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				DialogPopulationConfigurator dialog = new DialogPopulationConfigurator(new JFrame(), 
						true, populationName);
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

	/**
	 * 
	 */
	private void addTxtListeners() {

		txtNamePop.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNamePopActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNamePopActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNamePopActionPerformed(e);
			}
		});

		this.txtNumModerates.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNumModeratesActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNumModeratesActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNumModeratesActionPerformed(e);
			}
		});

		this.txtNumSpammers.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNumSpammersActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNumSpammersActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNumSpammersActionPerformed(e);
			}
		});

		this.txtNumPornographics.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNumPornographicsActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNumPornographicsActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNumPornographicsActionPerformed(e);
			}
		});

		this.txtNumViolent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNumViolentActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNumViolentActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNumViolentActionPerformed(e);
			}
		});

		this.txtNumRudes.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtNumRudesActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtNumRudesActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtNumRudesActionPerformed(e);
			}
		});

		this.txtUploadFreq.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadFreqActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtUploadFreqActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadFreqActionPerformed(e);
			}
		});

		this.txtUploadCorrect.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadCorrectActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtUploadCorrectActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadCorrectActionPerformed(e);
			}
		});

		this.txtUploadSpam.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadSpamActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtUploadSpamActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadSpamActionPerformed(e);
			}
		});

		this.txtUploadPorn.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadPornActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtUploadPornActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadPornActionPerformed(e);
			}
		});

		this.txtUploadViolent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadViolentActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtUploadViolentActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadViolentActionPerformed(e);
			}
		});

		this.txtUploadInsult.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtUploadInsultActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtUploadInsultActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtUploadInsultActionPerformed(e);
			}
		});

		this.txtViewTheReporter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtViewTheReporterActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtViewTheReporterActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtViewTheReporterActionPerformed(e);
			}
		});

		this.txtViewForum.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtViewForumActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtViewForumActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtViewForumActionPerformed(e);
			}
		});

		this.txtViewMultimedia.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtViewMultimediaActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtViewMultimediaActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtViewMultimediaActionPerformed(e);
			}
		});

		this.txtComplainSpam.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtComplainSpamActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtComplainSpamActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtComplainSpamActionPerformed(e);
			}
		});

		this.txtComplainPorn.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtComplainPornActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtComplainPornActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtComplainPornActionPerformed(e);
			}
		});

		this.txtComplainViolent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtComplainViolentActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtComplainViolentActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtComplainViolentActionPerformed(e);
			}
		});

		this.txtComplainInsult.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				txtComplainInsultActionPerformed(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				txtComplainInsultActionPerformed(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				txtComplainInsultActionPerformed(e);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton btnCreatePop;
	private ButtonGroup btnGroupViewCriterion;
	private JButton btnSave;
	private JComboBox<String> cbAvailablePop;
	private JComboBox<String> cbUserProfiles;
	private JButton btnExit;
	private JButton btnDelete;
	private JLabel lblAvailablePop;
	private JLabel lblComplainInsult;
	private JLabel lblComplainPorn;
	private JLabel lblComplainSpam;
	private JLabel lblComplainViolent;
	private JLabel lblForum;
	private JLabel lblMultimedia;
	private JLabel lblNamePop;
	private JLabel lblNumModerates;
	private JLabel lblNumPornographics;
	private JLabel lblNumRudes;
	private JLabel lblNumSpammers;
	private JLabel lblNumViolent;
	private JLabel lblProbComplain;
	private JLabel lblTheReporter;
	private JLabel lblTitle;
	private JLabel lblUploadCorrect;
	private JLabel lblUploadFreq;
	private JLabel lblUploadInsult;
	private JLabel lblUploadPorn;
	private JLabel lblUploadSpam;
	private JLabel lblUploadViolent;
	private JLabel lblUserProfiles;
	private JLabel lblViewCriterion;
	private JPanel panelComplainProfile;
	private JPanel panelNumAgents;
	private JPanel panelPop;
	private JPanel panelUploadProfile;
	private JPanel panelUserProfile;
	private JPanel panelViewProfile;
	private JRadioButton rbMostViewed;
	private JRadioButton rbNewest;
	private JRadioButton rbRandom;
	private JSeparator sepUploadProfile;
	private JSeparator sepUserProfile;
	private JDecimalField txtComplainInsult;
	private JDecimalField txtComplainPorn;
	private JDecimalField txtComplainSpam;
	private JDecimalField txtComplainViolent;
	private JTextField txtNamePop;
	private JIntegerField txtNumModerates;
	private JIntegerField txtNumPornographics;
	private JIntegerField txtNumRudes;
	private JIntegerField txtNumSpammers;
	private JIntegerField txtNumViolent;
	private JDecimalField txtUploadCorrect;
	private JDecimalField txtUploadFreq;
	private JDecimalField txtUploadInsult;
	private JDecimalField txtUploadPorn;
	private JDecimalField txtUploadSpam;
	private JDecimalField txtUploadViolent;
	private JDecimalField txtViewForum;
	private JDecimalField txtViewMultimedia;
	private JDecimalField txtViewTheReporter;
	// End of variables declaration//GEN-END:variables
}
