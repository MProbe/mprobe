package viewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import misc.CollapsingNotFilter;
import misc.EveryRowFilter;
import misc.FreezeHandle;
import model.ProblemInstance.VariableType;
import adapter.VariableTableModel;
import controller.VariableControl;

public class VariableWin extends JFrame implements WindowListener, ActionListener, ListSelectionListener {
	public static final String title = "Variable workshop";
	private static final String[] ACTIONS = {"reset original bounds",
											"show constraints containing selected variable",
											"show objectives containing selected variable" };

	private JLabel showOnly;
	private JComboBox showOnlyContent;
	private JCheckBox RSelection; // reverse selection
	private JLabel variableNum;

	private JTable variableTable;

	private JButton showValue;
	private JComboBox valueType;
	private JCheckBox freeze;
	private JButton trace;
	private JMenuItem[] rightClickActions;

	private VariableTableModel model;
	private VariableControl control;
	private FreezeHandle freezeHandle;

	TableRowSorter<VariableTableModel> sorter;

	
	static final String[] showOnlys = { "all", "type: real",
			"type: general integer", "type: binary",
			"other" }; //other must be last
	
	public VariableWin(VariableControl ctrl, VariableTableModel vtm) {
		super(title);

		model = vtm;
		control = ctrl;
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);

		Container content = getContentPane();
		content.setLayout(new BorderLayout());

		// set the top area of the window
		JPanel Upanel = new JPanel();
		Upanel.setLayout(new BoxLayout(Upanel, BoxLayout.X_AXIS));
		showOnly = new JLabel("Show only:");
		showOnlyContent = new JComboBox(showOnlys);
		showOnlyContent.addActionListener(this);
		RSelection = new JCheckBox("reverse selection");
		RSelection.addActionListener(this);
		variableNum = new JLabel(
				"this field indicates how many variable are visible");
		Upanel.add(showOnly);
		Upanel.add(Box.createRigidArea(new Dimension(20, 0)));
		Upanel.add(showOnlyContent);
		Upanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Upanel.add(RSelection);
		Upanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Upanel.add(variableNum);
		content.add(Upanel, BorderLayout.NORTH);

		// set the mid area of the window(i.e the table for variables)
		variableTable = new JTable(model);
		sorter = new VariableRowSorter<VariableTableModel>(model);
		sorter.setRowFilter(new EveryRowFilter());
		variableTable.setRowSorter(sorter);
		variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		variableTable.setDefaultRenderer(Double.class, new DoubleRenderer());
		JScrollPane scroll = new JScrollPane(variableTable);
		scroll.setPreferredSize(new Dimension(300, 300));
		content.add(scroll, BorderLayout.CENTER);
		freezeHandle = new FreezeHandle(variableTable, scroll);

		variableTable.getSelectionModel().addListSelectionListener(this);
		
		// set the popMenu for the Table
		final JPopupMenu popup = new JPopupMenu();

		rightClickActions = new JMenuItem[ACTIONS.length];
		for (int i=0; i < ACTIONS.length; i++) {
			rightClickActions[i] = new JMenuItem(ACTIONS[i]);
			popup.add(rightClickActions[i]);
			rightClickActions[i].addActionListener(this);
		}

		variableTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		// set the down area of the window
		JPanel Dpanel = new JPanel();
		Dpanel.setLayout(new BoxLayout(Dpanel, BoxLayout.X_AXIS));


		freeze = new JCheckBox("Freeze variable names/i.d.");
		trace = new JButton("Trace");
		freeze.addActionListener(this);
		trace.addActionListener(this);
		Dpanel.add(freeze);
		Dpanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Dpanel.add(trace);
		content.add(Dpanel, BorderLayout.SOUTH);

		pack();
		updateVariableNumberLabel();
	}

	protected void setFilter(int selectedFilter) {
		RowFilter filter = null;
		switch (selectedFilter) {
		case 0:
			filter = new EveryRowFilter();
			break;
		case 1:
			filter = model.new VariableTypeRowFilter(VariableType.Real);
			break;
		case 2:
			filter = model.new VariableTypeRowFilter(VariableType.Integer);
			break;
		case 3:
			filter = model.new VariableTypeRowFilter(VariableType.Binary);
			break;		
		}
		if (RSelection.isSelected() && filter != null)
			filter = new CollapsingNotFilter(filter);
		sorter.setRowFilter(filter);
        updateVariableNumberLabel();
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		control.close();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
	
	class VariableRowSorter<M extends TableModel> extends TableRowSorter<M> {
		
		VariableRowSorter() {
			super();
		}
		
		VariableRowSorter(M model)
		{
			super(model);
		}

		protected boolean 	useToString(int column) {
			if (column == 8)
				return true;
			return false;
		}
	}

	public void showOnlySelectedVariables(int[] variables) {
		showOnlyContent.setSelectedIndex(showOnlyContent.getItemCount()-1);
		sorter.setRowFilter(model.new VariableRowFilter(variables));
        updateVariableNumberLabel();
	};

	private void updateVariableNumberLabel() {
		int total = variableTable.getModel().getRowCount();
    	int shown = variableTable.getRowCount();
    	variableNum.setText(shown + " of " + total + " variables visible");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();
		if (src == rightClickActions[0])
		{
			if (variableTable.getSelectedRow() >= 0)
				control.resetTempbounds();
		}
		else if (src == rightClickActions[1])
		{
			control.showConstraintsfromVariable((Integer) model.getValueAt(variableTable.getSelectedRow(), 1));
		}
		else if (src == rightClickActions[2])
		{
			control.showObjectivesfromVariable((Integer) model.getValueAt(variableTable.getSelectedRow(), 1));
		}
		else if (src == trace)
		{
			if (variableTable.getRowCount() <= 0)
			{
				return;
			}

			int[] modelRowIndexes = new int[variableTable.getRowCount()];
			for (int i = 0; i < modelRowIndexes.length; ++i)
			{
				modelRowIndexes[i] = variableTable.convertRowIndexToModel(i);
			}
			control.trace(modelRowIndexes);
		}
		else if (src == freeze)
		{
			freezeHandle.freeze();
		}
		else if (src == RSelection)
		{
			if (sorter.getRowFilter() != null)
			{
				sorter.setRowFilter(new CollapsingNotFilter(sorter.getRowFilter()));
				updateVariableNumberLabel();
			}
		}
		else if (src == showOnlyContent)
		{
			setFilter(showOnlyContent.getSelectedIndex());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
    	if (e.getValueIsAdjusting())
    		return;

        if (e.getSource() == variableTable.getSelectionModel()
              && variableTable.getRowSelectionAllowed())
        {
        	int variable = variableTable.getSelectedRow();
        	if (variable < 0)
        		return;
        	variable = variableTable.convertRowIndexToModel(variable);
        	control.variableSelected(variable);
        }
	}

	public void objectiveSelected(int objective) {
		// TODO Auto-generated method stub
		
	}
	
	public void constraintSelected(int constraint) {
		// TODO Auto-generated method stub
		
	}
}
