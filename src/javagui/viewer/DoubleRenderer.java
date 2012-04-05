package viewer;

import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;

public class DoubleRenderer extends DefaultTableCellRenderer {
	DecimalFormat formatter;

    public DoubleRenderer() {
    	super();
    	formatter = new DecimalFormat("0.####E0");
    }

	public void setValue(Object value) {
		if (value instanceof Double)
		{
			Double d = (Double) value;
			setText(formatter.format(d));
		}
		else
			setText(value.toString());
	}
}
