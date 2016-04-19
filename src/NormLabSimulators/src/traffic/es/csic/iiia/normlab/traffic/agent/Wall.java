package es.csic.iiia.normlab.traffic.agent;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class Wall implements TrafficElement {
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public Wall(int x, int y){}
	
	/**
	 * 
	 */
	@Override
	public int getX() {
		return 0;
	}

	/**
	 * 
	 */
	@Override
	public int getY() {
		return 0;
	}

	@Override
  public void move() {}

}
