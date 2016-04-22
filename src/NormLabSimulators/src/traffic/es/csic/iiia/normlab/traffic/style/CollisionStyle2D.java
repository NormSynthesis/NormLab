package es.csic.iiia.normlab.traffic.style;


import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.NamedShapeCreator;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import sl.shapes.StarPolygon;
import es.csic.iiia.normlab.traffic.agent.Collision;

/**
 * CarStyle2D - Colors agents based on source positions.
 *
 * @author Javier Morales
 *
 */
public class CollisionStyle2D extends DefaultStyleOGL2D {

	protected Shape shape = new StarPolygon(0,0,30,14,12); 

	//----------------------------------------------------------
	// Attributes 
	//----------------------------------------------------------

	private ShapeFactory2D factory;

	//----------------------------------------------------------
	// Methods 
	//----------------------------------------------------------

	/**
	 * 
	 */
	public void init(ShapeFactory2D factory) {
		this.factory = factory;

		Rectangle2D bounds = shape.getBounds2D();
		float size = 30;
		float scaleX = size / (float) bounds.getWidth();
		float scaleY = size / (float) bounds.getWidth();
		shape = AffineTransform.getScaleInstance(scaleX, scaleY).createTransformedShape(shape);
		GeneralPath path = new GeneralPath(shape);
		path.closePath();
		
		NamedShapeCreator creator = factory.createNamedShape("collision");
		creator.addShape(shape, Color.red, true);
		creator.registerShape();
	}

	/**
	 * @return Color.BLUE.
	 */
	public Color getColor(Object o) {
		Collision col = (Collision)o;
		if(col.isViolation()) {
			return Color.magenta;
		}
		return Color.red;
	}

	/**
	 * @return a circle of radius 4.
	 */
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (spatial == null) {
			return factory.getNamedSpatial("collision");
		} else {
			return spatial;
		}
	}

	/**
	 * 
	 */
	public int getBorderSize(Object object) {
		return 10;
	}
}