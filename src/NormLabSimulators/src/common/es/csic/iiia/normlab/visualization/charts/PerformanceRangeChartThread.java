package es.csic.iiia.normlab.visualization.charts;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.ui.RefineryUtilities;

import es.csic.iiia.nsm.NormSynthesisMachine;
import es.csic.iiia.nsm.norm.Norm;
import es.csic.iiia.nsm.norm.evaluation.group.NormGroup;
import es.csic.iiia.nsm.norm.network.NetworkNode;

/**
 * Thread that controls the norm utility chart frame 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class PerformanceRangeChartThread 
extends Thread implements WindowListener {
	
	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
	private PerformanceRangeChartFrame chart;
	private boolean refresh;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param trafficInstitutions
	 */
	public PerformanceRangeChartThread(NormSynthesisMachine nsm, NetworkNode node) {
		this.refresh = false;
		
		if(node instanceof Norm) {
			chart = new PerformanceRangeChartFrame(nsm, node);	
		}
		else if(node instanceof NormGroup) {
			chart = new PerformanceRangeChartFrame(nsm, node);
		}
		chart.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		chart.pack();
    
		RefineryUtilities.centerFrameOnScreen(chart);
	}
	
	/**
	 * Allow the GUI to be updated
	 */
	public void allowRefreshing() {
		synchronized(this) {
			this.refresh = true;
		}
	}
	
	/**
	 * Run method. Refreshes the frame until the infinite
	 */
	@Override
	public void run() {
		this.chart.setVisible(true);
		
		while(true) {
			if(this.refresh) {
				this.chart.refresh();
			}
			try {
	      Thread.currentThread();
				Thread.sleep(Integer.MAX_VALUE);	
	    }
			catch (InterruptedException e) {}
		}
	}

	@Override
  public void windowOpened(WindowEvent e) {}

	@Override
  public void windowClosed(WindowEvent e) {}

	@Override
  public void windowIconified(WindowEvent e) {}

	@Override
  public void windowDeiconified(WindowEvent e) {}

	@Override
  public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
  public void windowClosing(WindowEvent e) {
	  JFrame frame = (JFrame)e.getSource();
	  frame.setVisible(false);
  }
}

