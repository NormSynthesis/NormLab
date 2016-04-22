package es.csic.iiia.normlab.launcher.utils;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.text.InternationalFormatter;

/**
 * 
 * @author Javi
 *
 */
public class JIntegerField extends JFormattedTextField {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param iMaxLen
	 * @param iFormat
	 */
	public JIntegerField(final int minimum) {
		this(minimum, Integer.MAX_VALUE);
	}
	
	/**
	 * 
	 * @param iMaxLen
	 * @param iFormat
	 */
	public JIntegerField(final int minimum, final int maximum) {

		this.setFormatterFactory(new AbstractFormatterFactory() {

			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
				format.setParseIntegerOnly(true);

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
				((JIntegerField)pE.getSource()).selectAll();
			}
		});
	}
}