package es.csic.iiia.normlab.traffic.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.render.Texture2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VImage2D;
import saf.v3d.scene.VSpatial;
import es.csic.iiia.normlab.traffic.agent.Car;
import es.csic.iiia.normlab.traffic.car.CarReasonerState;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class CarStyle2D extends DefaultStyleOGL2D {

	//----------------------------------------------------------
	// Attributes 
	//----------------------------------------------------------

	private Random random; 

	private String path;
	private String blueArrow, goldenArrow, greenArrow;
	private String magentaArrow, redArrow, orangeArrow;

	private BufferedImage blueCar, goldenCar, greenCar;
	private BufferedImage magentaCar, redCar, orangeCar;

	private Texture2D txBlueCar, txGoldenCar, txGreenCar;
	private Texture2D txMagentaCar, txRedCar, txOrangeCar;

	//----------------------------------------------------------
	// Methods 
	//----------------------------------------------------------

	public CarStyle2D() {
		path = "misc" + File.separator + "traffic" + File.separator + 
				"icon" + File.separator;

		this.blueArrow 		= path + "blueArrow.png";
		this.goldenArrow 	= path + "goldenArrow.png";
		this.greenArrow 	= path + "greenArrow.png";
		this.magentaArrow	= path + "magentaArrow.png";
		this.redArrow 		= path + "redArrow.png";
		this.orangeArrow 	= path + "orangeArrow.png";

		try {
			this.blueCar 		= ImageIO.read(new File(blueArrow));
			this.goldenCar 	= ImageIO.read(new File(goldenArrow));
			this.greenCar 	= ImageIO.read(new File(greenArrow));
			this.magentaCar = ImageIO.read(new File(magentaArrow));
			this.redCar 		= ImageIO.read(new File(redArrow));
			this.orangeCar 	= ImageIO.read(new File(orangeArrow));

			this.txBlueCar 		= new Texture2D(blueCar);
			this.txGoldenCar 	= new Texture2D(goldenCar);
			this.txGreenCar 	= new Texture2D(greenCar);
			this.txMagentaCar = new Texture2D(magentaCar);
			this.txRedCar 		= new Texture2D(redCar);
			this.txOrangeCar 	= new Texture2D(orangeCar);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}

		/* Random instance */
		this.random = new Random();
	}

	/**
	 * 
	 */
	public int getBorderSize(Object object) {
		return 10;
	}

	/**
	 * @return a circle of radius 4.
	 */
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if(spatial == null) {
			switch(random.nextInt(6)) {
			case 0:	spatial = new VImage2D(this.txBlueCar);			break;
			case 1:	spatial = new VImage2D(this.txGoldenCar);		break;
			case 2:	spatial = new VImage2D(this.txGreenCar);		break;
			case 3:	spatial = new VImage2D(this.txMagentaCar);	break;
			case 4:	spatial = new VImage2D(this.txRedCar);			break;
			case 5:	spatial = new VImage2D(this.txOrangeCar);		break;
			}
		}
		return spatial;
	}

	/**
	 * 
	 */
	public float getRotation(Object agent) {
		switch(((Car)agent).getPosition().getDirection()) {
		case North:		return 90f;					
		case East:		return 180; 						
		case South:		return 270; 	
		case West:		return 0; 
		}
		return 0;
	}

	/**
	 * 
	 */
	@Override
	public Color getColor(Object object) {
		Car car = (Car)object;

		if(car.isCasualStop()) {
			return Color.WHITE;
		}
		if(car.isCollided()) {
			return Color.RED;
		} 
		else if(car.getReasonerState() == CarReasonerState.NormWillBeFullfilled) {
			return Color.GREEN;
		}
		else if(car.getReasonerState() == CarReasonerState.NormWillBeInfringed) {
			return Color.RED;
		}
		return Color.WHITE;
	}

	/**
	 * 
	 */
	public Font getLabelFont(Object object) {
		Font font = new Font("Arial", Font.PLAIN, 15);
		return font;
	}

	/**
	 * 
	 */
	@Override
	public String getLabel(Object object) {
		Car c = (Car)object;
		CarReasonerState state = c.getReasonerState();
		String ret;
		
		if(state == CarReasonerState.NormWillBeFullfilled)	{
			int id = c.getNormToApply().getId();
			ret = String.valueOf(id);
		}
		else if(state == CarReasonerState.NormWillBeInfringed)	{
			int id = c.getNormToViolate().getId();
			ret = String.valueOf(id);
		}
		else {
			ret = " ";
		}
//		return String.valueOf(c.getId());
		return ret;
	}

	/**
	 * 
	 */
	public float getLabelXOffset(Object object) {
		return -6f;
	}

	/**
	 * 
	 */
	public float getLabelYOffset(Object object) {
		return -1f;
	}
	
	/**
	 * 
	 */
  public Position getLabelPosition(Object object) {
    return Position.NORTH_WEST;
  }
}