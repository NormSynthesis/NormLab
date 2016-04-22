package es.csic.iiia.normlab.traffic.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.InfiniteBorders;
import repast.simphony.valueLayer.GridValueLayer;
import es.csic.iiia.normlab.traffic.TrafficSimulator;
import es.csic.iiia.normlab.traffic.agent.Car;
import es.csic.iiia.normlab.traffic.agent.Collision;
import es.csic.iiia.normlab.traffic.agent.TrafficElement;
import es.csic.iiia.normlab.traffic.car.CarPosition;
import es.csic.iiia.normlab.traffic.car.CarReasonerState;
import es.csic.iiia.normlab.traffic.car.context.TrafficStateCodifier;
import es.csic.iiia.normlab.traffic.config.TrafficSimulatorSettings;
import es.csic.iiia.normlab.traffic.factory.CarContextFactory;
import es.csic.iiia.normlab.traffic.factory.TrafficFactFactory;
import es.csic.iiia.normlab.traffic.utils.Direction;
import es.csic.iiia.normlab.traffic.utils.Turn;
import es.csic.iiia.normlab.traffic.utils.Utilities;
import es.csic.iiia.nsm.agent.language.PredicatesDomains;
import es.csic.iiia.nsm.norm.Norm;

/**
 * CarMap - Meta information management 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class CarMap extends TrafficMatrix {
	
	//---------------------------------------------------------------------------
	// Attributes 
	//---------------------------------------------------------------------------
	
	private TrafficSimulatorSettings simSettings;
	private PredicatesDomains predDomains;
	private CarContextFactory carContextFactory;
	private TrafficFactFactory factFactory;
	
	private List<Car> allCars, travelingCars, carsToRemove;
	private LinkedList<Car> availableCars;
	private List<Collision> collisions, collisionsToRemove;
	private Map<String, GridPoint> positionsToCheck;
	
	private Context<TrafficElement> context = null;
	private Grid<TrafficElement> map = null;
	private GridValueLayer originalVLayer;
	private GridValueLayer vLayer;

	private int xDim = 0;
	private int yDim = 0;
	private long lastCarEmitTick = 1l;
	private int lowerLane = 0;
	private int upperLane = 0;
	private int leftLane = 0;
	private int rightLane = 0;

	/**
	 * Traffic view dimensions
	 */
	private int startRow, stopRow, startCol, stopCol;
		
	//---------------------------------------------------------------------------
	// Constructors 
	//---------------------------------------------------------------------------

	/**
	 * Constructor
	 *  
	 * @param context
	 * @param grid
	 * @param normLayer
	 */
	public CarMap(Context<TrafficElement> context, Grid<TrafficElement> map,
			PredicatesDomains predDomains, CarContextFactory carContextFactory,
			TrafficFactFactory factFactory, TrafficSimulatorSettings simSettings) {
		
		super(map.getDimensions().getHeight(), map.getDimensions().getWidth());
		
		this.simSettings = simSettings;
		this.predDomains = predDomains;
		this.carContextFactory = carContextFactory;
		this.factFactory = factFactory;
		
		this.xDim = map.getDimensions().getWidth();
		this.yDim = map.getDimensions().getHeight();
		this.context = context;
		this.map = map;

		this.availableCars = new LinkedList<Car>();
		this.travelingCars = new ArrayList<Car>();
		this.allCars = new ArrayList<Car>();
		this.carsToRemove = new ArrayList<Car>();
		this.collisions = new ArrayList<Collision>();
		this.collisionsToRemove = new ArrayList<Collision>();
		this.positionsToCheck = new HashMap<String,GridPoint>();
		
		this.leftLane =  (int)(Math.floor(0.5*xDim))-1;
		this.rightLane = leftLane+2;
		this.lowerLane = (int)(Math.floor(0.5*yDim))-1;
		this.upperLane = lowerLane+2;
		
		this.startRow = 0;
		this.stopRow = yDim-1;
		this.startCol = 0;
		this.stopCol = xDim-1;
		
		this.generateCars();
		
		this.vLayer = (GridValueLayer) this.context.getValueLayer("trafficMapPositions");
		this.originalVLayer = new GridValueLayer("", true, 
				new InfiniteBorders<TrafficElement>(), 
				(int)vLayer.getDimensions().getWidth(), 
				(int)vLayer.getDimensions().getHeight());
		
		/* Copy values to original value layer */
		for(int x=0; x < this.xDim; x++) {
			for(int y=0; y < this.yDim; y++) {
				this.originalVLayer.set(vLayer.get(x,y), x,y);
			}
		}
	}

	//----------------------------------------------------------
	// Methods 
	//----------------------------------------------------------

	/**
	 * Generates the collection of cars to use
	 */
	public void generateCars() {
		Car car;

		for(short i=1; i<100; i++) {
			car = new Car(i, true, predDomains, carContextFactory, 
					factFactory, simSettings.getNormInfringementRate());
			allCars.add(car);
			availableCars.add(car);
		}
	}

	//------------------------------------------------------------------
	// 
	//------------------------------------------------------------------

	/**
	 * Makes all the works that must be done by the map in a step
	 * 
	 * @return
	 */
	public void makeCarsMove() {
		this.removeCollisions();
		this.moveCars();
		this.addCarsToMap();
		this.codify();
	}

	/**
	 * 
	 */
	public void makeCarsPerceive() {
		for(Car car : this.travelingCars) {
			car.perceiveAndReason();
		}
		this.updateValueLayer();
	}
	
	/**
	 * 
	 */
	private void updateValueLayer() {
		
		/* Init value layer with default values */
		for(int x=0; x < this.xDim; x++) {
			for(int y=0; y < this.yDim; y++) {
				vLayer.set(this.originalVLayer.get(x,y), x, y);
			}
		}
		
		for(Car car : this.travelingCars) {
			int x = car.getX();
			int y = car.getY();
			if(car.getReasonerState() == CarReasonerState.NormWillBeFullfilled) {
				this.vLayer.set(TrafficSimulatorSettings.NORM_FULFILMENT, x,y);
			}
			else if(car.getReasonerState() == CarReasonerState.NormWillBeInfringed) {
				this.vLayer.set(TrafficSimulatorSettings.NORM_INFRINGEMENT, x,y);
			}
		}
	}
	
	/**
	 * 
	 */
	private void codify()
	{
		this.clear();
		String codState;
		
		// Clear previous information
		this.clear();
		
		for(int row=startRow; row<=stopRow; row++) {
			for(int col=startCol; col<=stopCol; col++) {
  			TrafficElement elem = this.getElement(row, col);
  			
  				// Create binary description and add it to the position
  				codState = TrafficStateCodifier.codify(elem);  				
  				this.set(row, col, codState);
			}
		}
	}
	
	/**
	 * Executes step method for each car and adds them to their new position
	 */
	private void moveCars()
	{
		this.carsToRemove.clear();
		this.positionsToCheck.clear();
		
		// Cars compute their new position
		for(Car car : travelingCars)
			car.move();
		
		// Move cars in the map
		for(Car car : travelingCars) {
			int x = car.getX();
			int y = car.getY();

			// Move car
			if(!this.isPositionOutOfBounds(x, y))
			{
				map.moveTo(car, x, y);
				this.addPositionToCheck(car.getPosition().getGridPoint());
			}
			// Car moved out of the map. Add for removal
			else {
				this.addCarToRemove(car);
			}
		}

		// Remove cars out of bounds
		for(Car car : this.carsToRemove)	{
			remove(car);
		}

		// Manage collisions
		for(String posId : this.positionsToCheck.keySet())
		{
			GridPoint pos = this.positionsToCheck.get(posId);
			int x = pos.getX();
			int y = pos.getY();

			if(this.getNumElements(x, y) > 1)
			{
				Collision col = new Collision(x, y, map);
				Iterable<TrafficElement> elements = map.getObjectsAt(x,y);
				remove(elements);
				
				context.add(col);
				map.moveTo(col, x, y);

				this.collisions.add(col);
			}
		}
	} 

	/**
	 * Emits new cars
	 */
	public void addCarsToMap() {
		int numAddedCars = 0;
		int numAvailableCars = availableCars.size();

		// Emit cars every N steps
		if(TrafficSimulator.getTick() == (long)(this.lastCarEmitTick +
				simSettings.getNewCarsFrequency())) {

			int carsToEmit = Math.min(simSettings.getNumCarsToAdd(), 4);
			
			CarPosition cp = null;

			this.lastCarEmitTick = TrafficSimulator.getTick();

			while(numAvailableCars > 0 && numAddedCars < carsToEmit){
				cp = getFreeRandomStartPoint();
				numAddedCars++;

				// Cancel, since no starting points are free
				if(cp == null)
					break;  
				else {
					add(cp);
				}
			}		
		}
	}

	/**
	 * Adds a list of norms to all the cars in the simulation
	 * 
	 * @param norms
	 */
	public void broadcastAddNorm(Norm norm) {
		for(Car c : allCars) {
			c.getReasoner().addNorm(norm);
		}
	}

	/**
	 * Adds a list of norms to all the cars in the simulation
	 * 
	 * @param norms
	 */
	public void broadcastRemoveNorm(Norm norm) {
		for(Car c : allCars) {
			c.getReasoner().removeNorm(norm);
		}
	}

	//----------------------------------------------------------
	// Add and remove
	//----------------------------------------------------------

	/**
	 * 
	 * @param pos
	 */
	private void add(CarPosition pos)
	{
		Car car = availableCars.pop();
		car.init(pos);
		add(car);
	}

	/**
	 * Adds a car to the simulation
	 * 
	 * @param car
	 */
	private void add(Car car)
	{
		travelingCars.add(car);
		context.add(car);
		map.moveTo(car, car.getX(), car.getY());
	}
	
	/**
	 * 
	 * @param pos
	 */
	private void addPositionToCheck(GridPoint pos)
	{
		String s = pos.getX() + "-" + pos.getY();
		this.positionsToCheck.put(s, pos);
	}
	
	/**
	 * 
	 * @param elements
	 */
	private void remove(Iterable<TrafficElement> elements)
	{
		List<TrafficElement> toRemove = new ArrayList<TrafficElement>();
		
		for(TrafficElement element : elements)
			toRemove.add(element);
		
		for(TrafficElement element : toRemove)
			remove(element);			
	}
	
	/**
	 * 
	 * @param element
	 */
	private void remove(TrafficElement element)
	{
		context.remove(element);

		if(element instanceof Car) {
			removeCar((Car)element);
		}
	}
	
	/**
	 * Removes a car from the simulation and its position
	 * 
	 * @param car
	 */
	private void removeCar(Car car)
	{
		travelingCars.remove(car);
		availableCars.addLast(car);
	}

	/**
	 * Removes collided cars from the car map
	 */
	public void removeCollisions()
	{
		this.collisionsToRemove.clear();
		
		for(Collision col : this.collisions) {
			this.collisionsToRemove.add(col);
		}

		for(Collision col : this.collisionsToRemove) {
			this.removeCollision(col);
		}
	}
	
	/**
	 * 
	 * @param col
	 */
	private void removeCollision(Collision col)
	{
		context.remove(col);
		this.collisions.remove(col);
	}
	
	/**
	 * 
	 * @param car
	 */
	private void addCarToRemove(Car car)
	{
		this.carsToRemove.add(car);
	}

	//----------------------------------------------------------
	// Getters and setters
	//----------------------------------------------------------

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getNumElements(int x, int y)
	{
		Iterable<TrafficElement> elements = map.getObjectsAt(x,y);
		int elemsCount = 0;

		Iterator<TrafficElement> iterator = elements.iterator();
		while(iterator.hasNext()) {
			iterator.next();
			elemsCount++;
		}
		return elemsCount;
	}
	
	/**
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public TrafficElement getElement(int row, int col) {
		return this.map.getObjectAt(col, yDim-1-row);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Car getCar(long id) {
		for(Car car : travelingCars) {
			if(car.getId() == id)
				return car;
		}
		return null;
	}

	/**
	 * Returns the number of cars currently driving into the scenario
	 * 
	 * @return
	 */
	public int getNumCars() {
		return travelingCars.size();
	}
		
	/**
	 * Returns the start point in the map for a given direction
	 */
	private CarPosition getStartPoint(Direction dir){
		int tx = 0,ty = 0;
		switch(dir)
		{
		case North:
			tx = leftLane;
			ty = yDim-1;
			break;
		case East:
			tx = xDim-1;
			ty = upperLane;
			break;
		case South:
			tx = rightLane;
			ty = 0;
			break;
		case West: 
			tx = 0;
			ty = lowerLane;
			break;
		}
		CarPosition cp = new CarPosition(tx,ty,dir.getOppositeDirection());
		return cp;
	}

	/**
	 * Returns a free random start point in the map
	 * 
	 * @return
	 */
	private CarPosition getFreeRandomStartPoint()
	{
		Direction dir = Utilities.getRandomDirection();
		CarPosition cp = getStartPoint(dir);
		int cnt = 0;

		while(!isFree(cp.getGridPoint()) && cnt++ < 3) {
			cp = getStartPoint(dir = Utilities.getTurnDirection(dir, Turn.Right));
		}
		if(!isFree(cp.getGridPoint()))
			cp = null;

		return cp;
	}

	/**
	 * Returns true if the car is out of the map
	 * 
	 * @param p
	 * @return
	 */
	public boolean isPositionOutOfBounds(int x, int y)
	{
		if(x<0 || y < 0 || x >= xDim || y >= yDim)
			return true;
		return false;
	}
	
	/**
	 * Returns true if a grid point of the map is free (with no car) 
	 * 
	 * @param p
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isFree(GridPoint p)
	{
		int count = 0;
		Iterable<TrafficElement> elements = map.getObjectsAt(p.getX(),p.getY());
		for(TrafficElement elem : elements) {
			count++;
		}
		return count==0;
	}

	/**
	 * Returns the x dimension of the map
	 * 
	 * @return  the xDim
	 */
	public int getXDim() 
	{
		return xDim;
	}

	/**
	 * Returns the y dimension of the map
	 * 
	 * @return  the yDim
	 */
	public int getYDim() 
	{
		return yDim;
	}

	/**
	 * Returns the lower lane of the map
	 * 
	 * @return  the lowerLane
	 */
	public int getLowerLane() 
	{
		return lowerLane;
	}

	/**
	 * Returns the upper lane of the map
	 * 
	 * @return  the upperLane
	 */
	public int getUpperLane() 
	{
		return upperLane;
	}

	/**
	 * Returns the left lane of the map
	 * 
	 * @return  the leftLane
	 */
	public int getLeftLane() 
	{
		return leftLane;
	}

	/**
	 * Returns the right lane of the map
	 * 
	 * @return  the rightLane
	 */
	public int getRightLane()
	{
		return rightLane;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumViolCols() {
		int numCols = 0;
		for(Collision col : this.collisions) {
			if(col.isViolation()) {
				numCols++;
			}
		}
		return numCols;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumNoViolCols() {
		int numCols = 0;
		for(Collision col : this.collisions) {
			if(!col.isViolation()) {
				numCols++;
			}
		}
		return numCols;
	}
}
