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
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import misc.CollapsingNotFilter;
import misc.EveryRowFilter;
import misc.FreezeHandle;
import model.MProbeLib.EmpiricalShape;
import model.ProblemInstance.FunctionShape;
import adapter.ObjectiveTableModel;
import controller.ObjectiveControl;

public class ObjectiveWin extends JFrame implements WindowListener, ActionListener, ListSelectionListener {
	public static final String title = "Objective function Workshop";
	private static final String[] ACTIONS = {"analyze selected functions",
		"analyze selected function (with extra histograms)",
		"analyze all functions",
		"show variables in the selected objective"};

	private JLabel showOnly;
	private JComboBox showOnlyContent;
	private JCheckBox RSelection; // reverse selection
	private JLabel objectiveNum;

	private JTable objectiveTable;

	private JButton trace;
	private JCheckBox freeze;
	private FreezeHandle freezeHandle;

	private JMenuItem[] rightClickActions;

	private ObjectiveTableModel model;
	private ObjectiveControl control;

	TableRowSorter<ObjectiveTableModel> sorter;

	static final String[] showOnlys = { "all",
		"algebraic shape: linear",
		"algebraic shape: quadratic",
		"algebraic shape: general nonlinear",
		"empirical shape: convex and almost convex",
		"empirical shape: concave and almost concave",
		"empirical shape: convex and concave",
		"other" }; //other must be last

	public ObjectiveWin(ObjectiveControl ctrl, ObjectiveTableModel otm) {
		super(title);

		model = otm;
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
		objectiveNum = new JLabel(
				"this field indicates how many objectives are visible");
		Upanel.add(showOnly);
		Upanel.add(Box.createRigidArea(new Dimension(20, 0)));
		Upanel.add(showOnlyContent);
		Upanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Upanel.add(RSelection);
		Upanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Upanel.add(objectiveNum);
		content.add(Upanel, BorderLayout.NORTH);

		// set the mid area of the window (i.e the table for objective
		// functions)
		objectiveTable = new JTable(model);
		sorter = new TableRowSorter<ObjectiveTableModel>(model);
		sorter.setRowFilter(new EveryRowFilter());
		objectiveTable.setRowSorter(sorter);
		objectiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		objectiveTable.setDefaultRenderer(Double.class, new DoubleRenderer());

		JScrollPane scroll = new JScrollPane(objectiveTable);
		scroll.setPreferredSize(new Dimension(300, 300));
		content.add(scroll, BorderLayout.CENTER);
		freezeHandle = new FreezeHandle(objectiveTable, scroll);

		// set the down area of the window
		JPanel Dpanel = new JPanel();
		Dpanel.setLayout(new BoxLayout(Dpanel, BoxLayout.X_AXIS));
		freeze = new JCheckBox("Freeze objective names & ids.");
		trace = new JButton("Trace");
		freeze.addActionListener(this);
		trace.addActionListener(this);
		Dpanel.add(freeze);
		Dpanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Dpanel.add(trace);
		content.add(Dpanel, BorderLayout.SOUTH);
		
		objectiveTable.getSelectionModel().addListSelectionListener(this);

		// set the popMenu for the Table
		final JPopupMenu popup = new JPopupMenu();
		rightClickActions = new JMenuItem[ACTIONS.length];
		for (int i=0; i < ACTIONS.length; i++) {
			rightClickActions[i] = new JMenuItem(ACTIONS[i]);
			popup.add(rightClickActions[i]);
			rightClickActions[i].addActionListener(this);
		}

		objectiveTable.addMouseListener(new MouseAdapter() {
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

		pack();
		updateObjectiveNumberLabel();
	}
	
	private void setFilter(int selectedFilter) {
		RowFilter filter = null;
		switch (selectedFilter) {
		case 0:
			filter = new EveryRowFilter();
			break;
		case 1:
			filter = model.new AlgebraicShapeRowFilter(FunctionShape.Linear);
			break;
		case 2:
			filter = model.new AlgebraicShapeRowFilter(FunctionShape.Quadratic);
			break;
		case 3:
			filter = model.new AlgebraicShapeRowFilter(FunctionShape.Nonlinear);
			break;
		case 4:
			filter = model.new EmpiricalShapeRowFilter(EmpiricalShape.ESAlmostConvex);
			break;
		case 5:
			filter = model.new EmpiricalShapeRowFilter(EmpiricalShape.ESAlmostConcave);
			break;
		case 6:
			filter = model.new EmpiricalShapeRowFilter(EmpiricalShape.ESBothConc);
			break;			
		}
		if (RSelection.isSelected() && filter != null)
			filter = new CollapsingNotFilter(filter);
		sorter.setRowFilter(filter);
        updateObjectiveNumberLabel();
	}

	public void showOnlySelectedObjectives(int[] objectives) {
		showOnlyContent.setSelectedIndex(showOnlyContent.getItemCount()-1);
		sorter.setRowFilter(model.new ObjectiveRowFilter(objectives));
		updateObjectiveNumberLabel();
	};

	private void updateObjectiveNumberLabel() {

		int total = objectiveTable.getModel().getRowCount();
		int shown = objectiveTable.getRowCount();
		objectiveNum.setText(shown + " of " + total + " objectives visible");
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == (Object)rightClickActions[0])
		{
			int [] selected = objectiveTable.getSelectedRows();
			
			for (int i = 0; i < selected.length; i++)
			{
				selected[i] = (Integer)objectiveTable.getValueAt(selected[i], 1);
			}
			control.analyzeObjectives(selected);
		}
		else if (src == (Object)rightClickActions[1])
		{
			int selected = objectiveTable.getSelectedRow();
			if (selected >= 0)
			{
				control.analyzeObjective((Integer)objectiveTable.getValueAt(selected, 1), true);
			}
		}
		else if (src == (Object)rightClickActions[2])
		{
			control.analyzeAll();
		}
		else if (src == (Object)rightClickActions[3])
		{
			if (objectiveTable.getSelectedRow() >= 0)
				control.showVariablesFromObjective((Integer)objectiveTable.getValueAt(objectiveTable.getSelectedRow(), 1));
		}
		else if (src == freeze)
		{
			freezeHandle.freeze();
		}
		else if (src == trace)
		{
			if (objectiveTable.getRowCount() <= 0)
			{
				return;
			}

			int[] modelRowIndexes = new int[objectiveTable.getRowCount()];
			for (int i = 0; i < modelRowIndexes.length; ++i)
			{
				modelRowIndexes[i] = objectiveTable.convertRowIndexToModel(i);
			}
			control.trace(modelRowIndexes);
		}
		else if (src == RSelection)
		{
			if (sorter.getRowFilter() != null)
			{
				sorter.setRowFilter(new CollapsingNotFilter(sorter.getRowFilter()));
				updateObjectiveNumberLabel();
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

        if (e.getSource() == objectiveTable.getSelectionModel()
              && objectiveTable.getRowSelectionAllowed())
        {
        	int objective = objectiveTable.getSelectedRow();
        	if (objective < 0)
        		return;
        	objective = objectiveTable.convertRowIndexToModel(objective);
        	control.objectiveSelected(objective);
        }
	}

	public void variableSelected(int variable) {
		// TODO Auto-generated method stub
		
	}
}
