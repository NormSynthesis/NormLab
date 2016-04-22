package es.csic.iiia.normlab.onlinecomm.context;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.ui.RSApplication;
import es.csic.iiia.normlab.onlinecomm.agents.ModerateUser;
import es.csic.iiia.normlab.onlinecomm.agents.OnlineCommunityManager;
import es.csic.iiia.normlab.onlinecomm.agents.OnlineCommunityPopulation;
import es.csic.iiia.normlab.onlinecomm.agents.OnlineCommunityUser;
import es.csic.iiia.normlab.onlinecomm.agents.PornographicUser;
import es.csic.iiia.normlab.onlinecomm.agents.RudeUser;
import es.csic.iiia.normlab.onlinecomm.agents.SpammerUser;
import es.csic.iiia.normlab.onlinecomm.agents.ViolentUser;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityFactFactory;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityNormEngine;
import es.csic.iiia.normlab.onlinecomm.agents.norms.OnlineCommunityUserReasoner;
import es.csic.iiia.normlab.onlinecomm.agents.profile.ComplaintProfile;
import es.csic.iiia.normlab.onlinecomm.agents.profile.UploadProfile;
import es.csic.iiia.normlab.onlinecomm.agents.profile.ViewProfile;
import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySettings;
import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySynthesisSettings;
import es.csic.iiia.normlab.onlinecomm.graphics.CreateGraphics;
import es.csic.iiia.normlab.onlinecomm.nsm.OnlineCommunityNSAgent;
import es.csic.iiia.normlab.onlinecomm.nsm.perception.OnlineCommunityWatcher;
import es.csic.iiia.normlab.onlinecomm.section.SectionForum;
import es.csic.iiia.normlab.onlinecomm.section.SectionPhotoVideo;
import es.csic.iiia.normlab.onlinecomm.section.SectionTheReporter;
import es.csic.iiia.normlab.visualization.MessageConsole;
import es.csic.iiia.normlab.visualization.NormLabConsole;
import es.csic.iiia.normlab.visualization.NormLabInspector;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.config.DomainFunctions;
import es.csic.iiia.nsm.config.NormSynthesisSettings;

/**
 * Context builder of the simulation
 * 
 * @author David Sanchez-Pinsach
 * @author Iosu Mendizabal
 * @author Javier Morales (jmorales@iiia.csic.es)
 */
public class OnlineCommunityContextCreator implements ContextBuilder<Object> {

	private ContextData contextData; // Context data of the context
	private ContinuousSpace<Object> space; // Space of the context
	private Grid<Object> grid; // Grid of the context
	private Context<Object> context;
	
	private OnlineCommunityManager ocManager;
	private OnlineCommunitySettings ocSettings;
	private NormSynthesisSettings nsSettings;
	private Random random;
	
	// Schedule parameters
	private double start;
	private double interval; 
	private double priority;
	private long rndSeed;
	
	private OnlineCommunityNSAgent nsAgent;
	private NormLabInspector nInspector;

	/**
	 * Method that is called from repast when you start the simulation (Our MAIN).
	 * 
	 * @param context
	 * 			The context of the repast simulation.
	 * 
	 * @return Context<Object>
	 * 			It returns the built context.
	 */
	public Context<Object> build(Context<Object> cntxt) {
		context = cntxt;
		start = 1;
		interval = 1;
		priority = 0;
		
		context.setId("onlineCommunityContext");
		
		/* Load configuration of the online community simulator and
		 * the Norm Synthesis Machine */
		ocSettings = new OnlineCommunitySettings();
		nsSettings = new OnlineCommunitySynthesisSettings();

		/* Set a randomly defined random seed if the
		 * random seed is equal to 0 */
		rndSeed = ocSettings.getRandomSeed();
		if(rndSeed == 0l) {
			rndSeed = System.currentTimeMillis();
		}
		random = new Random(rndSeed);
		
		/* Load population to use during the simulation */
		String populationName = ocSettings.getPopulation();
		
		OnlineCommunityPopulation population = 
				new OnlineCommunityPopulation("files/onlinecomm/populations/",
						populationName + ".xml");

		/* Create context data */
		contextData = new ContextData(population.getSize(), 
				ocSettings.getContentQueueSize(), random);
		
		/* Create online community manager */
		this.createOnlineCommunityManager(population);
		
		/* Create the online community scenario */
		this.createScenario();
		
		/* Add user population to the scenario */
		this.addUserPopulation(population, 
				ocManager.getPredicatesDomains());

		
		/* Create norm synthesis agent */
		try {
	    this.createNSMAgent();
    }
		catch (Exception e) {
	    e.printStackTrace();
    }

		/* Create user panel if simulation is not batch */
		if(!RunEnvironment.getInstance().isBatch()) {
			this.makeUserPanel();	
		}
		
		/* Compute ideal NS cardinality and add it to context data */
		int idealNSCardinality = 0;
		for(OnlineCommunityUser user : population.getUsers()) {
			if(user.getType() != 1) {
				idealNSCardinality += user.getQuantity();
			}
		}
		contextData.setIdealNormativeSystemCardinality(idealNSCardinality);

		/* Set simulation stop tick */
		RunEnvironment.getInstance().endAt(ocSettings.getMaxTicks());

		/* Create the GUI if required) */
		NormSynthesisMachine nsm = this.nsAgent.getNormSynthesisMachine();
		boolean useGui = !RunEnvironment.getInstance().isBatch();
		if(useGui) {
			
			/* Redirect output and show norms inspector */
			NormLabConsole console = this.redirectOutput();
			this.nInspector = new NormLabInspector(nsm, console);
			this.ocManager.setNormsInspector(nInspector);
			this.nInspector.show();
		}
		
		return context;
	}

	/**
	 * 
	 */
	private NormLabConsole redirectOutput() {
		final NormLabConsole consoleFrame = new NormLabConsole();
		consoleFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		/* Redirect output */
		if(!RunEnvironment.getInstance().isBatch()) {
			Runnable runnable = new Runnable() {
				public void run() {
					new MessageConsole(consoleFrame.getConsole());
				}
			};
			EventQueue.invokeLater(runnable);	
		}
		return consoleFrame;
  }
	/**
	 * 
	 */
	private void createScenario() {
		ContinuousSpaceFactory spaceFactory = 
				ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);

		space = spaceFactory.
				createContinuousSpace("space", context, 
						new RandomCartesianAdder <Object >(),
						new repast.simphony.space.continuous.WrapAroundBorders(),
						contextData.getNumColumns(), contextData.getNumRows());

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null); 
		grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(
						new WrapAroundBorders(), new SimpleGridAdder<Object>(), 
						false, contextData.getNumColumns(), 
						contextData.getNumRows())); 

		/* Save the context data in position 0,0 */
		context.add(contextData);
		space.moveTo(contextData, 0, 0);
		grid.moveTo(contextData, 0, 0);

		/* Create online community sections */
		this.createSections();
  }

	/**
	 * Method to create the iron agent and also create its scheduled methods to run every tick.
	 * @throws Exception 
	 */
	private OnlineCommunityNSAgent createNSMAgent() throws Exception {
		ScheduleParameters scheduleParams;
		ISchedule schedule;

		schedule = RunEnvironment.getInstance().getCurrentSchedule();
		OnlineCommunityWatcher watcher = new OnlineCommunityWatcher(contextData);

		PredicatesDomains predDomains = ocManager.getPredicatesDomains();
		DomainFunctions dmFunctions = ocManager.getDomainFunctions();
		
		this.nsAgent = new OnlineCommunityNSAgent(watcher, contextData,
				predDomains, dmFunctions, nsSettings, ocSettings, ocManager, 
				random);

		/* Get random and set it in the contextData */
		random = this.nsAgent.getNormSynthesisMachine().getRandom();
		contextData.setRandom(random);

		/* Schedule watcher */
		scheduleParams = ScheduleParameters.createRepeating(1, 1, -2);
		schedule.schedule(scheduleParams, watcher, "perceive");

		/* Schedule norm synthesis agent */
		scheduleParams = ScheduleParameters.createRepeating(1, 1,
				ScheduleParameters.LAST_PRIORITY);
		
		schedule.schedule(scheduleParams, nsAgent, "step");
		context.add(nsAgent);
		context.add(nsAgent.getMetrics());

		System.out.println("Starting simulation with random seed " + rndSeed);

		return nsAgent;
	}

	/**
	 * Method to create the iron agent and also create its scheduled methods to run every tick.
	 */
	private void createOnlineCommunityManager(
			OnlineCommunityPopulation population) {
		
		ScheduleParameters scheduleParams;
		ISchedule schedule = 
				RunEnvironment.getInstance().getCurrentSchedule();

		ocManager =	new OnlineCommunityManager(contextData, 
				ocSettings, population);

		// Create scheduler for watcher
		scheduleParams = ScheduleParameters.createRepeating(1, 1, 
				ScheduleParameters.FIRST_PRIORITY);
		
		schedule.schedule(scheduleParams, ocManager, "step");
		context.add(ocManager);
	}


	/**
	 * Method to create sections of the simulation
	 */
	private void createSections() {
		int row = contextData.getNumRows() - 1;

		// Section The reporter
		SectionTheReporter sectionTheReporter = new SectionTheReporter();

		context.add(sectionTheReporter);
		space.moveTo(sectionTheReporter, contextData.getSectionReporter(), row);
		grid.moveTo(sectionTheReporter, contextData.getSectionReporter(), row);

		// Section Forum
		SectionForum sectionForum = new SectionForum();

		context.add(sectionForum);
		space.moveTo(sectionForum, contextData.getSectionForum(), row);
		grid.moveTo(sectionForum, contextData.getSectionForum(), row);

		// Section Photo & Video
		SectionPhotoVideo sectionPhotoVideo = new SectionPhotoVideo();

		context.add(sectionPhotoVideo);
		space.moveTo(sectionPhotoVideo, contextData.getSectionMultimedia(), row);
		grid.moveTo(sectionPhotoVideo, contextData.getSectionMultimedia(), row);
	}

	/**
	 * Method to create agents population for the simulation.
	 * 
	 * @param users
	 *            ArrayList of agents that are going to take part in the simulation.
	 */
	public void addUserPopulation(OnlineCommunityPopulation population,
			PredicatesDomains predDomains) {
		int row = 0;

		List<OnlineCommunityUser> users = population.getUsers();
		int popSize = population.getSize();
		
		for(int usrIdx = 0; usrIdx < users.size(); usrIdx++){
			for(int numUsers = 0; numUsers < users.get(usrIdx).getQuantity(); 
					numUsers++) {
				
				row = contextData.getNewRow();
				createAgent(users.get(usrIdx), predDomains, row, popSize);
				contextData.setNumAgents(contextData.getNumAgents()+1);
			}
		}
	}

	/**
	 * Method to create a new Agent
	 * 
	 * @param agent
	 * 			Object with all the information to create a new one.
	 */
	private void createAgent(OnlineCommunityUser agent, 
			PredicatesDomains predDomains, int row, int popSize) {

		UploadProfile up = new UploadProfile(
				agent.getUploadProfile().getUploadProbability(),
				agent.getUploadProfile().getCorrect(),
				agent.getUploadProfile().getSpam(),
				agent.getUploadProfile().getPorn(),
				agent.getUploadProfile().getViolent(),
				agent.getUploadProfile().getInsult());

		// Take the total comments number from the parameters of Repast.
		Parameters params = RunEnvironment.getInstance().getParameters();
		int totalComments = (Integer) params.getValue("Total Comments");					

		contextData.setTotalComments(totalComments);

		up.setTotalComments(contextData.getTotalComments());
		up.setMaxCommentsTheReporter(contextData.getMaxCommentsTheReporter());
		up.setMaxCommentsForum(contextData.getMaxCommentsForum());
		up.setMaxCommentsPhotoVideo(contextData.getMaxCommentsPhotoVideo());

		ViewProfile vp = new ViewProfile(agent.getViewProfile().getForum(),
				agent.getViewProfile().getTheReporter(),
				agent.getViewProfile().getPhotoVideo(),
				agent.getViewProfile().getViewMode());

		ComplaintProfile cp = new ComplaintProfile(
				agent.getComplaintProfile().getSpam(),
				agent.getComplaintProfile().getPorn(),
				agent.getComplaintProfile().getViolent(),
				agent.getComplaintProfile().getInsult());


		/* Create agent depending on the type */
		OnlineCommunityUser commAgent = null;

		OnlineCommunityUserReasoner reasoner = ocManager.getUsersReasoner();
		OnlineCommunityNormEngine normEngine = ocManager.getNormEngine();
		OnlineCommunityFactFactory factFactory = ocManager.getFactFactory();
		
		int numViews = Math.max(30, popSize/2);
		switch (agent.getType()) {
		case 1:
			commAgent = new ModerateUser(reasoner, predDomains,space, grid,
					row, up, vp, cp, normEngine, factFactory, numViews);
			break;
		case 2:
			commAgent = new SpammerUser(reasoner, predDomains,space, grid, 
					row, up, vp, cp, normEngine, factFactory, numViews);
			break;
		case 3:
			commAgent = new PornographicUser(reasoner, predDomains,space, grid,
					row, up, vp, cp, normEngine, factFactory, numViews);
			break;
		case 4:
			commAgent = new ViolentUser(reasoner, predDomains,space, grid,
					row, up, vp, cp, normEngine, factFactory, numViews);
			break;
		case 5:
			commAgent = new RudeUser(reasoner, predDomains,space, grid, 
					row, up, vp, cp, normEngine, factFactory, numViews);
			break;

		default:
			break;
		}

		/* Add agent to the repast context */
		context.add(commAgent);
		space.moveTo(commAgent, 0, row);
		grid.moveTo(commAgent, 0, row);
		configureScheduleAgent(commAgent, start, interval, priority);		
	}

	/**
	 * Method to create schedule methods for each agent
	 * 
	 * @param agent
	 *            Agent that is going to use this schedule
	 * @param start
	 *            Start of tick action of the agent
	 * @param interval
	 *            Interval of tick action of the agent
	 * @param priority
	 *            Priority of the schedule of this agent
	 */
	private void configureScheduleAgent(OnlineCommunityUser agent, double start, double interval, double priority) {
		ScheduleParameters scheduleParams;
		ISchedule schedule;

		schedule = RunEnvironment.getInstance().getCurrentSchedule();

		// Create scheduler for upload content
		scheduleParams = ScheduleParameters.createRepeating(start, interval, priority);
		schedule.schedule(scheduleParams, agent, "upLoadContent");

		// Create scheduler for view and complaint content
		scheduleParams = ScheduleParameters.createRepeating(500, interval, priority + contextData.getMaxAgents());
		schedule.schedule(scheduleParams, agent, "viewAndComplaintContent");
	}

	/**
	 * Method to feed the user panel
	 */
	private void makeUserPanel() {
		JPanel panel = new JPanel();

		panel.setBorder(new TitledBorder("Graphics:"));
		panel.setLayout(new GridLayout(6, 2, 15, 0));// 6 rows, 2 cols

		final JButton button = new JButton("Complaint Graphic");
		JLabel label = new JLabel("View Graphics");
		button.setEnabled(true);

		panel.add(label);
		panel.add(button);

		// Add action listener to button
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				CreateGraphics graphic = new CreateGraphics("Grafica Complaints");
				contextData.addSeriesToCollection(contextData.getSeries());
				//contextData.addSeriesToCollection(contextData.getSerie2());
				contextData.addSeriesToCollection(contextData.getSerie3());
				graphic.setDataset(contextData.getCollection());
				graphic.createChart();
				//button.setEnabled(false);
			}
		});
		//		RSApplication.getRSApplicationInstance().removeCustomUserPanel();
		RSApplication.getRSApplicationInstance().addCustomUserPanel(panel);
	}
}
