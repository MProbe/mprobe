package misc;

import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

public class DoubleVerifier extends InputVerifier {
	double min = Double.NEGATIVE_INFINITY;
	double max = Double.POSITIVE_INFINITY;
	static final Color INVALID_COLOR = Color.red;
	static final Color VALID_COLOR = Color.black;

	protected String lastInvalidityReason;
	
	public DoubleVerifier() {
		lastInvalidityReason = "";
	}

	public DoubleVerifier(double minimum, double maximum)
			throws IllegalArgumentException {
		if (min > max)
			throw new IllegalArgumentException(
					"minimum must be less than maximum");
		min = minimum;
		max = maximum;
	}
	
	public String getLastInvalidityReason()
	{
		return lastInvalidityReason;
	}
	
	@Override
	public boolean verify(JComponent comp) {
		if (!(comp instanceof JTextComponent)) {
			return false; // Can't throw
		}

		JTextComponent textComp = (JTextComponent) comp;
		try {
			Double val = Double.parseDouble(textComp.getText());
			if (val == null)
			{
				lastInvalidityReason = "Null value";
			}
			else if (val < min || val > max)
			{
				lastInvalidityReason = "Value out of bounds";
			}
			else if (and(val))
			{
				textComp.setForeground(VALID_COLOR);
				return true;
			}
		} catch (NumberFormatException e) {	}
		
		JOptionPane.showMessageDialog(comp,
				"Invalid input: "+lastInvalidityReason,
				"Invalid input",
				JOptionPane.ERROR_MESSAGE);

		textComp.setForeground(INVALID_COLOR);
		return false;
	}

	// Override and add extra conditions here
	protected boolean and(Double d) {
		return true;
	}

	public void setMinimum(double minimum) throws IllegalArgumentException {
		if (minimum > max)
			throw new IllegalArgumentException(
					"minimum must be less than maximum");
		min = minimum;
	}

	public void setMaximum(double maximum) throws IllegalArgumentException {
		if (maximum < min)
			throw new IllegalArgumentException(
					"minimum must be less than maximum");
		max = maximum;
	}
	
	public void setBounds(double minimum, double maximum)
			throws IllegalArgumentException {
		if (min > max)
			throw new IllegalArgumentException(
					"minimum must be less than maximum");
		min = minimum;
		max = maximum;
	}
}
