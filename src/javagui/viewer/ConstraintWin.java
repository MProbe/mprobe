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
import adapter.ConstraintTableModel;
import controller.ConstraintControl;

public class ConstraintWin extends JFrame implements WindowListener, ActionListener, ListSelectionListener {
	private static final String title = "Constraint Workshop";
	private static final String[] ACTIONS = {"analyze selected functions",
										"analyze selected function (with extra histograms)",
										"analyze all functions",
										"show variables in the selected constraint"};

	private JLabel showOnly;
	private JComboBox showOnlyContent;
	private JCheckBox RSelection; // reverse selection
	private JLabel constraintNum;

	private JTable constraintTable;

	private JCheckBox freeze;
	private JButton trace;

	private JMenuItem[] rightClickActions;

	private ConstraintTableModel model;
	private ConstraintControl control;

	TableRowSorter<ConstraintTableModel> sorter;
	private FreezeHandle freezeHandle;
	
	static final String[] showOnlys = { "all",
										"algebraic shape: linear",
										"algebraic shape: quadratic",
										"algebraic shape: general nonlinear",
										"empirical shape: convex and almost convex",
										"empirical shape: concave and almost concave",
										"empirical shape: convex and concave",
										"other" }; //other must be last

	public ConstraintWin(ConstraintControl ctrl, ConstraintTableModel ctm) {
		super(title);

		model = ctm;
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
		constraintNum = new JLabel(
				"this field indicates how many constraints are visible");
		Upanel.add(showOnly);
		Upanel.add(Box.createRigidArea(new Dimension(20, 0)));
		Upanel.add(showOnlyContent);
		Upanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Upanel.add(RSelection);
		Upanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Upanel.add(constraintNum);
		content.add(Upanel, BorderLayout.NORTH);

		// set the mid area of the window(i.e the table for variables)
		constraintTable = new JTable(model);
		sorter = new TableRowSorter<ConstraintTableModel>(model);
		sorter.setRowFilter(new EveryRowFilter());
		constraintTable.setRowSorter(sorter);
		constraintTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		constraintTable.setDefaultRenderer(Double.class, new DoubleRenderer());
		
		JScrollPane scroll = new JScrollPane(constraintTable);
		scroll.setPreferredSize(new Dimension(300, 300));
		content.add(scroll, BorderLayout.CENTER);
		freezeHandle = new FreezeHandle(constraintTable, scroll);

		// set the down area of the window
		JPanel Dpanel = new JPanel();
		Dpanel.setLayout(new BoxLayout(Dpanel, BoxLayout.X_AXIS));
		freeze = new JCheckBox("Freeze constraint names & ids.");
		trace = new JButton("Trace");
		freeze.addActionListener(this);
		trace.addActionListener(this);
		Dpanel.add(freeze);
		Dpanel.add(Box.createRigidArea(new Dimension(40, 0)));
		Dpanel.add(trace);
		content.add(Dpanel, BorderLayout.SOUTH);

		constraintTable.getSelectionModel().addListSelectionListener(this);
		
		// set the popMenu for the Table
		final JPopupMenu popup = new JPopupMenu();

		rightClickActions = new JMenuItem[ACTIONS.length];
		for (int i=0; i < ACTIONS.length; i++) {
			rightClickActions[i] = new JMenuItem(ACTIONS[i]);
			popup.add(rightClickActions[i]);
			rightClickActions[i].addActionListener(this);
		}

		constraintTable.addMouseListener(new MouseAdapter() {
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
		updateConstraintNumberLabel();
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
        updateConstraintNumberLabel();
	}


	public void showOnlySelectedConstraints(int[] constraints) {
        showOnlyContent.setSelectedIndex(showOnlyContent.getItemCount()-1);
        sorter.setRowFilter(model.new ConstraintRowFilter(constraints));
        updateConstraintNumberLabel();
	};

	private void updateConstraintNumberLabel() {
		int total = constraintTable.getModel().getRowCount();
    	int shown = constraintTable.getRowCount();
    	constraintNum.setText(shown +" of "+total+" constraints visible");
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
			int [] selected = constraintTable.getSelectedRows();
			
			for (int i = 0; i < selected.length; i++)
			{
				selected[i] = (Integer)constraintTable.getValueAt(selected[i], 1);
			}
			control.analyzeConstraints(selected);
		}
		else if (src == (Object)rightClickActions[1])
		{
			int selected = constraintTable.getSelectedRow();
			if (selected >= 0)
			{
				control.analyzeConstraint((Integer)constraintTable.getValueAt(selected, 1), true);
			}
		}
		else if (src == (Object)rightClickActions[2])
		{
			control.analyzeAll();
		}
		else if (src == (Object)rightClickActions[3])
		{
			if (constraintTable.getSelectedRow() >= 0)
				control.showVariablesFromConstraint((Integer)constraintTable.getValueAt(constraintTable.getSelectedRow(), 1));
		}
		else if (src == freeze)
		{
			freezeHandle.freeze();
		}
		else if (src == trace)
		{
			if (constraintTable.getRowCount() <= 0)
			{
				return;
			}

			int[] modelRowIndexes = new int[constraintTable.getRowCount()];
			for (int i = 0; i < modelRowIndexes.length; ++i)
			{
				modelRowIndexes[i] = constraintTable.convertRowIndexToModel(i);
			}
			control.trace(modelRowIndexes);
		}
		else if (src == RSelection)
		{
			if (sorter.getRowFilter() != null)
			{
				sorter.setRowFilter(new CollapsingNotFilter(sorter.getRowFilter()));
				updateConstraintNumberLabel();
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

        if (e.getSource() == constraintTable.getSelectionModel()
              && constraintTable.getRowSelectionAllowed())
        {
        	int constraint = constraintTable.getSelectedRow();
        	if (constraint < 0)
        		return;
        	constraint = constraintTable.convertRowIndexToModel(constraint);
        	control.constraintSelected(constraint);
        }
	}

	public void variableSelected(int variable) {
		// TODO Auto-generated method stub
		
	}
}
