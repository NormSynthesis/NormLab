package es.csic.iiia.normlab.onlinecomm.metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.csic.iiia.normlab.onlinecomm.config.OnlineCommunitySettings;
import es.csic.iiia.normlab.onlinecomm.content.IContent;
import es.csic.iiia.normlab.onlinecomm.content.comment.SpamComment;
import es.csic.iiia.normlab.onlinecomm.context.ContextData;
import es.csic.iiia.normlab.onlinecomm.graphics.SlidingWindowMetric;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.metrics.DefaultNormSynthesisMetrics;

/**
 * 
 * @author Iosu Mendizabal
 *
 */
public class OnlineCommunityMetrics extends DefaultNormSynthesisMetrics {

	private ContextData contextData;
	private NormSynthesisMachine nsm;
	private SlidingWindowMetric nonRegulatedComplaintsWindow; 
	private NormsFileManager normsFile;
	private NormSetsFileManager normSetsFile;
	private OnlineCommunitySettings ocSettings;

	private Map<IContent,Integer> contentNumEvals;
	private Map<Integer,Float> maxCVR;
	private Map<Integer,Float> accCVR;
	private Map<Integer,Integer> numCVR;

	private double medianViewComplaintRatio, minViewComplaintRatio;
	private double maxViewComplaintRatio;
	private double convergenceQuality;

	private StringBuffer contentMetricsBuffer;

	/**
	 * 
	 * @param contextData
	 * @param nsm
	 */
	public OnlineCommunityMetrics(ContextData contextData,
			NormSynthesisMachine nsm, OnlineCommunitySettings ocSettings)	{

		super(nsm);

		this.contextData = contextData;
		this.ocSettings = ocSettings;
		this.nsm = nsm;

		this.nonRegulatedComplaintsWindow = new SlidingWindowMetric(10);
		this.normsFile = new NormsFileManager(contextData, nsm, ocSettings);

		this.contentNumEvals = new HashMap<IContent,Integer>();
		this.maxCVR = new HashMap<Integer,Float>();
		this.accCVR = new HashMap<Integer,Float>();
		this.numCVR = new HashMap<Integer,Integer>();

		/* To keep track of content metrics */
		this.contentMetricsBuffer = new StringBuffer();
	}

	/**
	 * 
	 */
	public void update(double timeStep) {
		super.update(timeStep);

		int tick = (int) timeStep;
		if(tick==1) {
			init();
		}

		this.nonRegulatedComplaintsWindow.addValue(
				contextData.getNumCurrentNonRegulatedComplaints());

		contextData.setSerie(tick, contextData.getNumNonRegulatedComplaints());
		contextData.setSerie2(tick, contextData.getNumComplaint());
		//contextData.setSerie3(tick, contextData.getNumCurrentNonRegulatedComplaints());
		contextData.setSerie3(tick, this.nonRegulatedComplaintsWindow.getAvg());

		// Compute median views
		List<Double> viewComplaintRatios = new ArrayList<Double>();

		for(IContent content : contextData.getAllContents()) {
			if(content.getType() != 1) {
				if(content.getNumViews() > 0) {
					double viewComplaintRatio = 100 * ((double)content.getNumComplaints() / (double)content.getNumViews());
					viewComplaintRatios.add(viewComplaintRatio);
				}
			}
		}

		Collections.sort(viewComplaintRatios);

		int n = viewComplaintRatios.size();


		//odd
		if(n > 0)	{
			this.setMinViewComplaintRatio(viewComplaintRatios.get(0));
			this.setMaxViewComplaintRatio(viewComplaintRatios.get(n-1));

			if(n%2!=0) {
				medianViewComplaintRatio = viewComplaintRatios.get((n-1)/2);
				//even
			} else {
				medianViewComplaintRatio = ( viewComplaintRatios.get(n/2) + viewComplaintRatios.get((n/2)-1) ) / 2;
			}	
		}

		checkConvergence(tick);

		List<Double> viewComplaintRatioWithoutNormViolation = new ArrayList<Double>();
		List<Double> viewComplaintRatioWithNormViolation = new ArrayList<Double>();


		for(IContent content : contextData.getAllContents())
		{
			if(content.getType() != 1) 
			{
				if(content.getNumViews() > 0) {
					if(content.getViolatedNorm() == null){
						double viewComplaintRatio = 100 * ((double)content.getNumComplaints() / (double)content.getNumViews());
						viewComplaintRatioWithoutNormViolation.add(viewComplaintRatio);
					}else{
						double viewComplaintRatio = 100 * ((double)content.getNumComplaints() / (double)content.getNumViews());
						viewComplaintRatioWithNormViolation.add(viewComplaintRatio);
					}
				}
			}
		}

		
		long stopTick = ocSettings.getMaxTicks();
		if(tick == stopTick){
			crearHistograma(viewComplaintRatioWithNormViolation, viewComplaintRatioWithoutNormViolation);
		}
		//		outputValues(tick, stopTick);

		this.updateDataset(tick, stopTick);

		if(tick == stopTick) {
			this.saveFiles();
		}

//		try {
//			//if(this.hasConverged()){	
//			if(this.hasNormativeSystemChangedThisTick()){
//				//    	if(this.hasNormativeNetworkChangedThisTick()) {
//
//				PrintStream ps = new PrintStream("output/onlinecomm/norms/NormGraph-"+tick+".txt");
//				PrintStream orig = System.out;
//				System.setOut(ps);
//
//				NormativeNetwork nNetwork = nsm.getNormativeNetwork();
//				Collection<Norm> actNorms = nNetwork.getActiveNorms();
//				for (Norm norm : actNorms){
//					Goal gComplaints = this.nsm.getNormSynthesisSettings().getSystemGoals().get(0);
//					Utility normUtility = nNetwork.getUtility(norm);
//					int nec = (int) (normUtility.getScore(EvaluationCriteria.Necessity, gComplaints) * 100); // Necessity
//					int level = nNetwork.getGeneralisationLevel(norm);
//					System.out.println("n:"+norm.getName()+"\t"+nec+"\t"+level+"\tActive");
//					List<Norm> parents = nNetwork.getParents(norm);
//					if(!parents.isEmpty()){
//						System.out.print("e:");
//						for (Norm parent : parents){
//							System.out.print(parent.getName()+" ");
//						}
//						System.out.println();
//					}	
//				}
//				Collection<Norm> inactNorms = nNetwork.getInactiveNorms();
//				boolean represented = false;
//
//				for (Norm norm : inactNorms){
//					norm = nNetwork.retrieveNorm(norm);
//					represented = nNetwork.isRepresented(norm);
//
//					Goal gComplaints = this.nsm.getNormSynthesisSettings().getSystemGoals().get(0);
//					Utility normUtility = nNetwork.getUtility(norm);
//					if(normUtility == null)
//						System.out.println();
//					int nec = (int) (normUtility.getScore(EvaluationCriteria.Necessity, gComplaints) * 100); // Necessity
//					int level = nNetwork.getGeneralisationLevel(norm);
//					if(represented){
//						System.out.println("n:"+norm.getName()+"\t"+nec+"\t"+level+"\tGeneralised");
//					}else{
//						System.out.println("n:"+norm.getName()+"\t"+nec+"\t"+level+"\tDiscarded");
//					}
//					List<Norm> parents = nNetwork.getParents(norm);
//					if(!parents.isEmpty()){
//						System.out.print("e:");
//						for (Norm parent : parents){
//							System.out.print(parent.getName()+" ");
//						}
//						System.out.println();
//					}	
//				}
//
//				System.setOut(orig);
//				ps.close();
//
//				// Para saber el estado de una norma (activo/inactivo)
//				// nNetwork.isActive(norm);
//
//				// Para saber las relaciones de una norma:
//				// 1. Padres
//				// nNetwork.getParents(norm);
//				// 2. Hijos
//				// nNetwork.getChildren(norm);
//
//				// Para saber la utilidad de una norma (para los pesos)
//				/*
//					Goal gComplaints = this.nsm.getNormSynthesisSettings().getSystemGoals().get(0);
//					Utility normUtility = nNetwork.getUtility(norm);
//					double eff = normUtility.getScore(Dimension.Effectiveness, gComplaints); // Effectiveness
//					double nec = normUtility.getScore(Dimension.Effectiveness, gComplaints); // Necessity
//				 */
//			}
//		} catch (IOException ex) {
//		}

		this.updateContentMetrics(timeStep);
	}

	/**
	 * 
	 * @param timeStep
	 */
	private void updateContentMetrics(double tick) {
		float contentCVR, maxCVR, accCVR;
		float numComplaints, numViews;
		int numEval, numCVRsPerTick;

		for(IContent content : this.contextData.getActualViewList()) {
			if(content instanceof SpamComment) {

				numComplaints = content.getNumComplaints();
				numViews = content.getNumViews();
				contentCVR = (numComplaints / numViews);

				if(!this.contentNumEvals.containsKey(content)) {
					numEval = 1;
				}
				else {
					numEval = this.contentNumEvals.get(content) + 1;
				}

				if(!this.maxCVR.containsKey(numEval)) {
					maxCVR = 0f;
					accCVR = 0f;
				}
				else {
					maxCVR = this.maxCVR.get(numEval);
					accCVR = this.accCVR.get(numEval);
				}

				if(!this.numCVR.containsKey(numEval)) {
					numCVRsPerTick = 1;
				}
				else {
					numCVRsPerTick = this.numCVR.get(numEval) + 1;
				}

				/* Compute max complain/view ratio */
				maxCVR = Math.max(maxCVR, contentCVR);
				accCVR += contentCVR;

				this.contentNumEvals.put(content, numEval);
				this.maxCVR.put(numEval, maxCVR);
				this.accCVR.put(numEval, accCVR);
				this.numCVR.put(numEval, numCVRsPerTick);
			}
		}
	}

	//	/**
	//	 * 
	//	 */
	//	private void outputContentMetrics(double tick) {
	//		if(this.ticksToTrackContents.contains(tick)) {
	//			IContent content = this.getContentCreatedInTick(tick);
	//			this.trackedContents.add(content);	
	//		}
	//
	//		this.contentMetricsBuffer.append((int)tick);
	//		for(IContent content : trackedContents) {
	//
	//			if(content != null) { 
	//				this.contentMetricsBuffer.append("\t " + content.getNumViews() + "\t " + 
	//						content.getNumComplaints());
	//			}
	//		}
	//		this.contentMetricsBuffer.append("\n");
	//	}

	//	/**
	//	 * 
	//	 * @param tick
	//	 * @return
	//	 */
	//	private IContent getContentCreatedInTick(double tick) {
	//		for(IContent content : contextData.getAllContents()) {
	//			if(content instanceof SpamComment && 
	//					content.getTick() == tick) {
	//				return content;
	//			}
	//		}
	//		return null;
	//	}

	/**
	 * 
	 * @param tick
	 * @param stopTick
	 */
	private void updateDataset(long tick, long stopTick) {}

	private void crearHistograma(List<Double> viewComplaintRatios, List<Double> viewComplaintRatioWithoutNormViolation) {
//		HistogramDataset dataset = new HistogramDataset();
//
//		double[] v1 = new double[viewComplaintRatios.size()];
//		for(int i = 0 ; i < viewComplaintRatios.size() ; i++){
//			v1[i] = viewComplaintRatios.get(i)/100;
//		}
//		double[] v2 = new double[viewComplaintRatioWithoutNormViolation.size()];
//		for(int i = 0 ; i < viewComplaintRatioWithoutNormViolation.size() ; i++){
//			v2[i] = viewComplaintRatioWithoutNormViolation.get(i)/100;
//		}
//
//		int bin = 10;
//		dataset.addSeries("viewComplaintRatioViolation", v1, bin, 0, 1);
//		dataset.addSeries("ViewComplaintRatioNoViolation", v2, bin, 0 ,1);
//
//		String plotTitle = "Histogram";
//		String xaxis = "Ratio";
//		String yaxis = "Contents";
//		PlotOrientation orientation = PlotOrientation.VERTICAL;
//		boolean show = true;
//		boolean toolTips = false;
//		boolean urls = false;
//		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis,
//				yaxis, dataset, orientation, show, toolTips, urls);
//		int width = 800;
//		int height = 800;
//		try {
//			ChartUtilities.saveChartAsPNG(new File("histogram.png"), chart,
//					width, height);
//		} catch (IOException e) {
//		}
	}

	/**
	 * 
	 * @param tick
	 */
	private void checkConvergence(int tick) {
		
		long stopTick = this.ocSettings.getMaxTicks();

		if (tick == stopTick && this.hasConverged()) {
			int actualNorms = nsm.getNormativeNetwork().getNormativeSystem().size();
			int systemIdealCardinality = contextData.getIdealNormativeSystemCardinality();
			convergenceQuality = actualNorms / systemIdealCardinality;
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getComplaintsForNonRegulatedNorms(){
		return this.contextData.getNumCurrentNonRegulatedComplaints();
	}

	/**
	 * 
	 * @return
	 */
	public float getAvgComplaintsForNonRegulatedNorms() {
		return this.nonRegulatedComplaintsWindow.getAvg();
	}

	/**
	 * 
	 * @return
	 */
	public int getComplaintsForRegulatedNorms() {
		return this.contextData.getNumComplaint();
	}
	/**
	 * 
	 * @return
	 */
	public int getActualComplaints(){
		int contador = 0;
		for(int i = 0 ; i < contextData.getActualComplaintList().size() ; i++){
			if(contextData.getActualComplaintList().get(i).getNumComplaints() == 1){
				contador++;
			}
		}
		return contador;
	}

	/**
	 * Save files 
	 * @param convergenceQuality 
	 */
	public void saveFiles() {
		this.normsFile.load();
		this.normsFile.saveConvergence(this.hasConverged(), convergenceQuality);
		this.normsFile.save();

		this.normSetsFile = new NormSetsFileManager(this, nsm, 
				this.hasConverged(), this.ocSettings);
		this.normSetsFile.load();
		this.normSetsFile.save();

		this.saveComputationMetrics();
		this.saveContentMetricsFile();
	}

	/**
	 * 
	 */
	private void saveContentMetricsFile() {
		List<Integer> contentEvalTicks = 
				new ArrayList<Integer>(this.accCVR.keySet());
		Collections.sort(contentEvalTicks);

		int maxEvalTick = contentEvalTicks.get(contentEvalTicks.size()-1);

		for(int i=1; i<=maxEvalTick; i++) {
			float maxRatio = this.maxCVR.get(i);
			float accRatio = this.accCVR.get(i);
			float numRatios = this.numCVR.get(i);
			float avgRatio = accRatio / numRatios;

			this.contentMetricsBuffer.
			append(i + "\t" + maxRatio + "\t" + avgRatio + "\t" + numRatios + "\n");
		}

		String filename = ocSettings.getOutputDataPath() + "/ContentMetrics.dat";
		File outputFile = new File(filename);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, false));
			String s = this.contentMetricsBuffer.toString();
			bw.write(s);
			bw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void saveComputationMetrics() {

		String filename = ocSettings.getOutputDataPath() + "/ComputationMetrics.dat";
		File outputFile = new File(filename);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));

			double minCompTime = this.getMinComputationTime();
			double maxCompTime = this.getMaxComputationTime();
			double medianCompTime = this.getMedianComputationTime();
			double totalCompTime = this.getTotalComputationTime();
			double nodesSynth = this.getNumNodesSynthesised();
			double nodesInMem = this.getNumNodesInMemory();
			double nodesVisited = this.getNumNodesVisited();

			bw.write(String.valueOf(minCompTime) + "\t" + String.valueOf(maxCompTime) + "\t" +
					String.valueOf(medianCompTime) + "\t" + String.valueOf(totalCompTime) + "\t" + 
					String.valueOf(nodesSynth) + "\t" +	String.valueOf(nodesInMem) + "\t" + 
					String.valueOf(nodesVisited) + "\t" +"\n");
			bw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Normative system upon convergence");
		System.out.println(this.nsm.getNormativeNetwork().getNormativeSystem());
		System.out.println("------------------------------------------");
	}


	/**
	 * Returns the number of ACTIVE norms
	 * 
	 * @return
	 */
	public int getNormativeSystemCardinality() {
		return this.nsm.getNormativeNetwork().getNormativeSystem().size();
	}

	/**
	 * Returns the total number of synthesised norms
	 * 
	 * @return
	 */
	public int getNormativeNetworkCardinality() {
		return this.nsm.getNormativeNetwork().getNorms().size();
	}


	public double getMedianViewComplaintRatio() {
		return medianViewComplaintRatio;
	}

	public double getMinViewComplaintRatio() {
		return minViewComplaintRatio;
	}

	public void setMinViewComplaintRatio(double minViewComplaintRatio) {
		this.minViewComplaintRatio = minViewComplaintRatio;
	}

	public double getMaxViewComplaintRatio() {
		return maxViewComplaintRatio;
	}

	public void setMaxViewComplaintRatio(double maxViewComplaintRatio) {
		this.maxViewComplaintRatio = maxViewComplaintRatio;
	}

	private void init(){}

}
