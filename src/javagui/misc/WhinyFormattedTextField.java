package misc;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

public class WhinyFormattedTextField extends JFormattedTextField {
	
	@Override
	protected void invalidEdit()
	{
		if (getInputVerifier() != null && getInputVerifier() instanceof DoubleVerifier)
		{
			JOptionPane.showMessageDialog(this,
					((DoubleVerifier)getInputVerifier()).getLastInvalidityReason(),
					"Invalid input.",
					JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(this,
					(Object)"Invalid input.",
					"Invalid input.",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
}
