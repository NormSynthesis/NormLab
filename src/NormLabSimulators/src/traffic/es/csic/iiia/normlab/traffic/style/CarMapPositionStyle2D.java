package es.csic.iiia.normlab.traffic.style;

import java.awt.Color;
import java.awt.Paint;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;
import es.csic.iiia.normlab.traffic.config.TrafficSimulatorSettings;

/**
 * NormStyle2D - Coloring of value layer (higher performance than agent color) 
 * used to paint background of blocking norms red and background of 
 * non-lane fields green.
 *
 * @author Jan Koeppen (jankoeppen@gmx.net)
 *
 */
public class CarMapPositionStyle2D implements ValueLayerStyleOGL {

	//-----------------------------------------------------------------
	// Attributes
	//-----------------------------------------------------------------

	private final Color cWall 						= Color.getHSBColor(0.6f, 0.8f, 0.4f);
	private final Color cRoad 						= Color.lightGray;
	private final Color cNormFulfilment 	= Color.green;
	private final Color cNormInfringement	= Color.red;

	private ValueLayer layer;

	//-----------------------------------------------------------------
	// Methods
	//-----------------------------------------------------------------


	@Override
	public void init(ValueLayer layer) {
		this.layer = layer;
	}
	
	/**
	 * Returns the color based on the value at given coordinates.
	 */
	public Paint getPaint(double... coordinates) {
		double v = layer.get(coordinates);

		if (v == TrafficSimulatorSettings.WALL_POSITION) return cWall;
		return cRoad;
	}

	/**
	 * 
	 */
	public float getCellSize() {
		return 34f;
	}

	/**
	 * 
	 */
	@Override
	public Color getColor(double... coordinates) {
		double v = layer.get(coordinates);
		Color color = cRoad;
		if (v == TrafficSimulatorSettings.WALL_POSITION) {
			color = cWall;
		}
		else if (v == TrafficSimulatorSettings.NORM_FULFILMENT) {
			color = cNormFulfilment;
		}
		else if (v == TrafficSimulatorSettings.NORM_INFRINGEMENT) {
			color = cNormInfringement;
		}
		return color;
	}
}
