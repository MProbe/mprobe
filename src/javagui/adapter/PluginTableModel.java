package adapter;

import java.util.Observable;
import java.util.Observer;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import model.PluginSet;

public class PluginTableModel extends AbstractTableModel implements Observer {

	private PluginSet pluginSet;
	
	public PluginTableModel(PluginSet ps)
	{
		pluginSet = ps;
		pluginSet.addObserver(this);
	}
	
	@Override
	public String getColumnName(int column) {
		return "Plugins";
	}
	
	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return pluginSet.getPlugins().length;
	}

	public Class getColumnClass(int column) {
		return String.class;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		String[] plugins = pluginSet.getPlugins();
		if (col != 0 || row >= plugins.length)
			return new String();
		return plugins[row];
	}


	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	@Override
	public void update(Observable o, Object arg1) {
		if (o == pluginSet)
		{
			fireTableChanged(new TableModelEvent(this));
		}
	}

}
