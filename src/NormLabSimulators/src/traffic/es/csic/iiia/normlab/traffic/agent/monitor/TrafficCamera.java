package es.csic.iiia.normlab.traffic.agent.monitor;

import es.csic.iiia.normlab.traffic.map.CarMap;
import es.csic.iiia.nsm.sensing.Sensor;
import es.csic.iiia.nsm.sensing.ViewTransition;

/**
 * The traffic institution
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class TrafficCamera implements Sensor {

	//-----------------------------------------------------------------
	// Attributes
	//-----------------------------------------------------------------

	private TrafficCameraPosition position;
	private int startRow, stopRow, startCol, stopCol;
	private TrafficView previousScope, currentScope;
	private ViewTransition perception;
	
	//-----------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------

	/**
	 * 
	 */
	public TrafficCamera(TrafficCameraPosition position, CarMap map) {
		
		this.position = position;
		this.startRow = position.getStartRow();
		this.stopRow = position.getStopRow();
		this.startCol = position.getStartCol();
		this.stopCol = position.getStopCol();
				
		// Initially perceive to avoid null scopes
		this.currentScope = new TrafficView(startRow, stopRow, startCol, stopCol);
		this.previousScope = new TrafficView(startRow, stopRow, startCol, stopCol);
		this.perception = new ViewTransition(this);
	}

	/**
	 * The monitor agent perceives
	 */
	public void perceive(CarMap map) {	
		this.previousScope.copy(currentScope);
		this.currentScope.update(map);

		this.perception.clear();
		this.perception.setView(-1, previousScope);
		this.perception.setView(0, currentScope);
	}

	/**
	 * 
	 * @return
	 */
	public TrafficCameraPosition getPosition() {
		return this.position;
	}

	/**
	 * 
	 * @return
	 */
	public TrafficView getPreviousView() {
		return this.previousScope;
	}
	
	/**
	 * 
	 * @return
	 */
	public TrafficView getView() {
		return currentScope;
	}

	/**
	 * 
	 */
	@Override
  public int getId() {
	  return 0;
  }

	/**
	 * 
	 */
	@Override
  public String getLocation() {
		return this.position.toString();
  }

	/**
	 * 
	 */
	@Override
  public void setPerceptionWindow(int ticks) {
  }

	/**
	 * 
	 */
	@Override
  public ViewTransition getPerception() {
	  return this.perception;
  }
}
