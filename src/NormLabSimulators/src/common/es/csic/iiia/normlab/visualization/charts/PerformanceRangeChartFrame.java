package es.csic.iiia.normlab.visualization.charts;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.config.EvaluationCriteria;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.PerformanceRange;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.network.NetworkNode;

/**
 * A frame that shows the utility of a norm or a norm group
 * with respect to a goal
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 * @see PerformanceRange
 */
public class PerformanceRangeChartFrame extends JFrame {

	//---------------------------------------------------------------------------
	// Static attributes
	//---------------------------------------------------------------------------
	
	private static final long serialVersionUID = 8286435099181955453L;

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private final int CHART_WIDTH = 550;
	private final int CHART_HEIGHT = 400;

	private List<PerformanceRangeChart> charts;

	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param nsm the norm synthesis machine
	 * @param goal the goal from which to show the utility
	 * @param node the node from which to show the utility
	 */
	public PerformanceRangeChartFrame(NormSynthesisMachine nsm, NetworkNode node) {
		
		super("Norm scores for goal GCols");
		this.charts = new ArrayList<PerformanceRangeChart>();

		if(node instanceof Norm) {
			for(EvaluationCriteria dim : nsm.getNormEvaluationDimensions()) {
				this.charts.add(new PerformanceRangeChart(nsm, dim, node));
			}	
		}
		else if (node instanceof NormGroup) {
			this.charts.add(new PerformanceRangeChart(
					nsm, EvaluationCriteria.Effectiveness, node));
		}
		
		this.initComponents();
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	/**
	 * Initialises the chart's components
	 */
	private void initComponents() {
		JPanel content = new JPanel(new GridLayout());
		ChartPanel chartPanel;

		for(PerformanceRangeChart chart : this.charts) {
			chartPanel = new ChartPanel(chart.getChart());
			content.add(chartPanel);
			chartPanel.setPreferredSize(new java.awt.Dimension(CHART_WIDTH, CHART_HEIGHT));
		}
		setContentPane(content);
	}

	/**
	 * Refreshes the chart's components
	 */
	public void refresh() {
		for(PerformanceRangeChart chart : this.charts) {
			chart.refresh();
		}
	}
}
