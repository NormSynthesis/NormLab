package es.csic.iiia.normlab.visualization;

import java.util.ArrayList;
import java.util.List;

import es.csic.iiia.normlab.visualization.charts.PerformanceRangeChartThread;
import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;

/**
 * This tool manages builds and starts a new thread for each frame 
 * that is showing relevant information of the system 
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class NormLabInspector {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------

	private NormSynthesisMachine nsm;		// the norm synthesis machine
	private Thread tracerThread;		// thread of norms tracer
	private boolean converged;		// the norm synthesis process has converged?
	private List<PerformanceRangeChartThread> normScoreCharts;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param nsm the norm synthesis machine
	 */
	public NormLabInspector(NormSynthesisMachine nsm, NormLabConsole console)  {
		this.nsm = nsm;
		this.converged= false;

		/* Construct threads to show norms charts and frames */
		if(nsm.isGUI()) {
			this.normScoreCharts = new ArrayList<PerformanceRangeChartThread>();
			this.tracerThread = new NormsInspectorManager(this,
					nsm.getNormativeNetwork(), console);

		}
		this.allowRefreshing();
	}

	/**
	 * This method is called when the thread runs
	 */
	public void show() {
		if(nsm.isGUI())
			this.tracerThread.start();
	}

	/**
	 * Allows the GUI to be refreshed
	 */
	public void allowRefreshing() {
//		this.tracerThread.allowRefreshing();
	}

	/**
	 * Refresh the GUI
	 */
	public void refresh() {
		if(nsm.isGUI()) {
			tracerThread.interrupt();

			// Update norm score charts
			for(PerformanceRangeChartThread nScChart : normScoreCharts) {
				nScChart.interrupt();
			}
		}
	}

	/**
	 * Adds and shows a new utility chart for a norm
	 * 
	 * @param norm the norm
	 */
	public synchronized void addNormScoreChart(Norm norm) {
		PerformanceRangeChartThread tChart =
				new PerformanceRangeChartThread(nsm, norm);
		this.normScoreCharts.add(tChart);

		tChart.allowRefreshing();
		tChart.start();
	}

	/**
	 * Adds and shows a new utility chart for a norm group
	 * 
	 * @param nGroup the norm group
	 */
	public synchronized void addNormGroupScoreChart(NormGroup nGroup) {
		PerformanceRangeChartThread tChart = 
				new PerformanceRangeChartThread(nsm, nGroup);
		this.normScoreCharts.add(tChart);

		tChart.allowRefreshing();
		tChart.start();
	}

	/**
	 * 
	 * @param converged
	 */
	public void setConverged(boolean converged) {
		this.converged = converged;
	}

	/**
	 * Returns <tt>true</tt> if the norm synthesis machine has converged
	 * to a normative system
	 * 
	 * @return <tt>true</tt> if the norm synthesis machine has converged
	 * to a normative system
	 */
	public boolean hasConverged() {
		return this.converged;
	}

	/**
	 * Returns the norm synthesis machine
	 * 
	 * @return the norm synthesis machine
	 */
	public NormSynthesisMachine getNormSynthesisMachine() {
		return this.nsm;
	}
}
