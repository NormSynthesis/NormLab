package es.csic.iiia.normlab.launcher.utils;
import java.awt.Color;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.PlainDocument;

/**
 * 
 * @author Javi
 *
 */
public class JNumericField extends JFormattedTextField {
	private static final long serialVersionUID = 1L;

	private static final char DOT = '.';
	private static final char NEGATIVE = '-';
	private static final String BLANK = "";
	private static final int DEF_PRECISION = 2;

	public static final int NUMERIC = 2;
	public static final int DECIMAL = 3;

	public static final String FM_NUMERIC = "0123456789";
	public static final String FM_DECIMAL = FM_NUMERIC + DOT; 

	private int maxLength = 0;
	private int format = NUMERIC;
	private String negativeChars = BLANK;
	private String allowedChars = null;
	private boolean allowNegative = false;
	private int precision = 0;

	protected PlainDocument numberFieldFilter;

	/**
	 * 
	 */
	public JNumericField() {
		this(2, DECIMAL);
	}

	/**
	 * 
	 * @param iMaxLen
	 */
	public JNumericField(int iMaxLen) {
		this(iMaxLen, NUMERIC);
	}

	/**
	 * 
	 * @param iMaxLen
	 * @param iFormat
	 */
	public JNumericField(int iMaxLen, int iFormat) {
		setAllowNegative(true);
		setMaxLength(iMaxLen);
		setFormat(iFormat);

		numberFieldFilter = new JNumberFieldFilter();

		super.setDocument(numberFieldFilter);

		this.setFormatterFactory(new AbstractFormatterFactory() {

			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				NumberFormat format = DecimalFormat.getInstance();
				format.setMinimumFractionDigits(2);
				format.setMaximumFractionDigits(2);
				format.setRoundingMode(RoundingMode.HALF_UP);
				InternationalFormatter formatter = new InternationalFormatter(format);
				formatter.setAllowsInvalid(false);
				formatter.setMinimum(0.0);
				formatter.setMaximum(1.0);
				return formatter;
			}
		});
	}

	/**
	 * 
	 * @param maxLen
	 */
	public void setMaxLength(int maxLen) {
		if (maxLen > 0)
			maxLength = maxLen;
		else
			maxLength = 0;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * 
	 */
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);

		if (enable) {
			setBackground(Color.white);
			setForeground(Color.black);
		} else {
			setBackground(Color.lightGray);
			setForeground(Color.darkGray);
		}
	}

	/**
	 * 
	 */
	public void setEditable(boolean enable) {
		super.setEditable(enable);

		if (enable) {
			setBackground(Color.white);
			setForeground(Color.black);
		} else {
			setBackground(Color.lightGray);
			setForeground(Color.darkGray);
		}
	}

	/**
	 * 
	 * @param iPrecision
	 */
	public void setPrecision(int iPrecision) {
		if (format == NUMERIC)
			return;

		if (iPrecision >= 0)
			precision = iPrecision;
		else
			precision = DEF_PRECISION;
	}

	/**
	 * 
	 * @return
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * 
	 * @return
	 */
	public Number getNumber() {
		Number number = null;
		if (format == NUMERIC)
			number = new Integer(getText());
		else
			number = new Double(getText());

		return number;
	}

	/**
	 * 
	 * @param value
	 */
	public void setNumber(Number value) {
		setText(String.valueOf(value));
	}

	/**
	 * 
	 * @return
	 */
	public int getInt() {
		return Integer.parseInt(getText());
	}

	/**
	 * 
	 * @param value
	 */
	public void setInt(int value) {
		setText(String.valueOf(value));
	}

	/**
	 * 
	 * @return
	 */
	public float getFloat() {
		return (new Float(getText())).floatValue();
	}

	/**
	 * 
	 * @param value
	 */
	public void setFloat(float value) {
		setText(String.valueOf(value));
	}

	/**
	 * 
	 * @return
	 */
	public double getDouble() {
		return (new Double(getText())).doubleValue();
	}

	/**
	 * 
	 * @param value
	 */
	public void setDouble(double value) {
		setText(String.valueOf(value));
	}

	/**
	 * 
	 * @return
	 */
	public int getFormat() {
		return format;
	}

	/**
	 * 
	 * @param iFormat
	 */
	public void setFormat(int iFormat) {
		switch (iFormat) {
		case NUMERIC:
		default:
			format = NUMERIC;
			precision = 0;
			allowedChars = FM_NUMERIC;
			break;

		case DECIMAL:
			format = DECIMAL;
			precision = DEF_PRECISION;
			allowedChars = FM_DECIMAL;
			break;
		}
	}

	/**
	 * 
	 * @param b
	 */
	public void setAllowNegative(boolean b) {
		allowNegative = b;

		if (b)
			negativeChars = "" + NEGATIVE;
		else
			negativeChars = BLANK;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isAllowNegative() {
		return allowNegative;
	}

	/**
	 * 
	 */
	public void setDocument(Document document) {
	}

	/**
	 * 
	 * @author Javi
	 *
	 */
	class JNumberFieldFilter extends PlainDocument {
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		public JNumberFieldFilter() {
			super();
		}

		/**
		 * 
		 */
		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {

//			String text = getText(0, offset) + str
//					+ getText(offset, (getLength() - offset));
//
//			if (str == null || text == null)
//				return;
//
//			for (int i = 0; i < str.length(); i++) {
//				if ((allowedChars + negativeChars).indexOf(str.charAt(i)) == -1)
//					return;
//			}
//
//			int precisionLength = 0, dotLength = 0, minusLength = 0;
//			int textLength = text.length();
//
//			try {
//				if (format == NUMERIC) {
//					if (!((text.equals(negativeChars)) && (text.length() == 1)))
//						new Long(text);
//				} else if (format == DECIMAL) {
//					if (!((text.equals(negativeChars)) && (text.length() == 1))) 
//						new Double(text);
//
//					int dotIndex = text.indexOf(DOT);
//					if (dotIndex != -1) {
//						dotLength = 1;
//						precisionLength = textLength - dotIndex - dotLength;
//
//						if (precisionLength > precision)
//							return;
//					}
//				}
//			} catch (Exception ex) {
//				return;
//			}
//
//			if (text.startsWith("" + NEGATIVE)) {
//				if (!allowNegative)
//					return;
//				else
//					minusLength = 1;
//			}
//
//			if (maxLength < (textLength - dotLength - precisionLength - minusLength))
//				return;

			super.insertString(offset, str, attr);
		}


		//		private void check(String str, String text) throws Exception {
		//			if (str == null || text == null) {
		//				throw new Exception();
		//			}
		//
		//			for (int i = 0; i < str.length(); i++) {
		//				if ((allowedChars + negativeChars).indexOf(str.charAt(i)) == -1) {
		//					throw new Exception();
		//				}
		//			}
		//
		//			int precisionLength = 0, dotLength = 0, minusLength = 0;
		//			int textLength = text.length();
		//
		//				if (format == NUMERIC) {
		//					if (!((text.equals(negativeChars)) && (text.length() == 1))) {
		//						new Long(text);
		//					}
		//				} else if (format == DECIMAL) {
		//					if (!((text.equals(negativeChars)) && (text.length() == 1))) { 
		//						new Double(text);
		//					}
		//
		//					int dotIndex = text.indexOf(DOT);
		//					if (dotIndex != -1) {
		//						dotLength = 1;
		//						precisionLength = textLength - dotIndex - dotLength;
		//
		//						if (precisionLength > precision) {
		//							throw new Exception();
		//						}
		//					}
		//				}
		//
		//			if (text.startsWith("" + NEGATIVE)) {
		//				if (!allowNegative) {
		//					throw new Exception();
		//				}
		//				else {
		//					minusLength = 1;
		//				}
		//			}
		//
		//			if (maxLength < (textLength - dotLength - precisionLength - minusLength)) {
		//				throw new Exception();
		//			}
		//		}
	}
}