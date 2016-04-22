package es.csic.iiia.normlab.visualization;

import es.csic.iiia.nsm.norm.network.NormativeNetwork;

/**
 * The thread that creates and updates the NSM norms tracer
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public class NormsInspectorManager extends Thread {

	//---------------------------------------------------------------------------
	// Attributes
	//---------------------------------------------------------------------------
	
//	private NormsTracerFrame traceFrame;
	private NormsInspectorFrame inspectorFrame;
	private boolean refresh;
	
	//---------------------------------------------------------------------------
	// Methods
	//---------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param trafficInstitutions
	 */
	public NormsInspectorManager(NormLabInspector manager,
			NormativeNetwork normativeNetwork, NormLabConsole console) {
		
//		this.traceFrame = new NormsTracerFrame(manager);
		this.inspectorFrame = new NormsInspectorFrame(manager, console);
		this.refresh = false;
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
	 * Updates the trace frame to the infinite
	 */
	@Override
	public void run() {
//		this.traceFrame.setVisible(true);
		this.inspectorFrame.setVisible(true);
		
		while(true) {
			if(this.refresh) {
//				this.inspectorFrame.refresh();
			}
			
			try {
	      Thread.currentThread();
				Thread.sleep(Integer.MAX_VALUE);

      } catch (InterruptedException e) {
      }
		}
	}
}
