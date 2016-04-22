package es.csic.iiia.normlab.launcher.utils;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.text.InternationalFormatter;

/**
 * 
 * @author Javi
 *
 */
public class JDecimalField extends JFormattedTextField {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param iMaxLen
	 * @param iFormat
	 */
	public JDecimalField(final int iMaxLen, final double minimum, 
			final double maximum) {

		this.setFormatterFactory(new AbstractFormatterFactory() {

			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				NumberFormat format = DecimalFormat.getNumberInstance(Locale.US);
				format.setMinimumFractionDigits(2);
				format.setMaximumFractionDigits(iMaxLen);
				
				format.setRoundingMode(RoundingMode.HALF_UP);
				InternationalFormatter formatter = new InternationalFormatter(format);
				formatter.setAllowsInvalid(false);
				formatter.setMinimum(minimum);
				formatter.setMaximum(maximum);
				
				return formatter;
			}
		});
		
		this.addFocusListener(new FocusListener() {
			@Override public void focusLost(final FocusEvent pE) {}
			@Override public void focusGained(final FocusEvent pE) {
				((JDecimalField)pE.getSource()).selectAll();
			}
		});
	}
}