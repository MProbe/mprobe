package adapter;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import model.AnalysisInstance;
import model.ProblemInstance;
import model.ProblemInstance.VariableType;

public class VariableTableModel extends AbstractTableModel implements Observer {

	private ProblemInstance pModel; // parts of adapted model
	private AnalysisInstance aModel;
	private int objectiveForBestPoint;
	
	// Defines the column ordering
	static final int nameIdx = 0;
	static final int idIdx = nameIdx+1;
	static final int typeIdx = idIdx+1;
	static final int funcsIdx = typeIdx+1;
	static final int origLbIdx = funcsIdx+1;
	static final int tmpLbIdx = origLbIdx+1;
	static final int tmpUbIdx = tmpLbIdx+1;
	static final int origUbIdx = tmpUbIdx+1;
	static final int bestPtValIdx = origUbIdx+1;
	enum VarTableColumn {

		Name (nameIdx, "Name"),
		Identifier(idIdx, "Id."),
		Type(typeIdx, "Type"),
		NumFuncs(funcsIdx, "# funcs."),
		OrigLowerBound(origLbIdx, "Orig. lwr bound"),
		TempLowerBound(tmpLbIdx, "Tmp. lwr bound"),
		TempUpperBound(tmpUbIdx, "Tmp. uppr bound"),
		OrigUpperBound(origUbIdx, "Orig. uppr bound"),
		BestPointValue(bestPtValIdx, "Obj. best. pt.")
		;		
		int columnIdx;
		String headerStr;
		VarTableColumn(int col, String header)
		{
			columnIdx = col;
			headerStr = header;
		}
		static VarTableColumn fromColumn(int col) {
			for (VarTableColumn vCol : VarTableColumn.values())
				if (vCol.columnIdx == col)
					return vCol;
			return null;
		}
	}

	public VariableTableModel(ProblemInstance pInst, AnalysisInstance aInst) {
		objectiveForBestPoint = 0;
		pModel = pInst;
		pModel.addObserver(this);
		aModel = aInst;
		aModel.addObserver(this);
	}

	@Override
	public Class getColumnClass(int column) {
		
		VarTableColumn col = VarTableColumn.fromColumn(column);
		switch (col) {
		case TempLowerBound:
		case TempUpperBound:
		case BestPointValue:
			if (aModel.isAnalysing())
				return String.class;
		default: break;
		}
		switch (col) {
		case Name:
			return String.class;
		case Identifier:
			return  Integer.class;
		case Type:
			return VariableType.class;
		case NumFuncs:
			return Integer.class;
		case OrigLowerBound:
		case TempLowerBound:
		case TempUpperBound:
		case OrigUpperBound:
		case BestPointValue:
			return Double.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return VarTableColumn.values().length;
	}

	@Override
	public String getColumnName(int column) {
		return VarTableColumn.fromColumn(column).headerStr;
	}

	@Override
	public int getRowCount() {
		if (!pModel.loaded())
			return 0;
		return pModel.variables();
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (!pModel.loaded() || !aModel.loaded())
			return "";
		
		try {
			switch (VarTableColumn.fromColumn(column)) {
			case Name:
				return pModel.variableName(row);
			case Identifier:
				return new Integer(row);
			case Type:
				return pModel.variableType(row);
			case NumFuncs:
			{
				int[] objs = new int[pModel.objectives() + 1];
				int[] cons = new int[pModel.constraints() + 1];

				pModel.variablePresence(row, cons, objs);

				return new Integer(objs[0] + cons[0]);
			}		
			case OrigLowerBound:
				return new Double(pModel.variableBounds(row)[0]);
			case TempLowerBound:
				return (aModel.isAnalysing() ? "Analysis in progress" :
						new Double(aModel.getTempLowerBound(row)));
			case TempUpperBound:
				return (aModel.isAnalysing() ? "Analysis in progress" :
						new Double(aModel.getTempUpperBound(row)));
			case OrigUpperBound:
				return new Double(pModel.variableBounds(row)[1]);
			case BestPointValue:
			{
				if (aModel.isAnalysing())
					return "Analysis in progress";
				
				if (!(objectiveForBestPoint < pModel.objectives()))
					return Double.NaN;

				double[] point = aModel.getBestPoint(objectiveForBestPoint);
				
				if (point != null)
					return point[row];
				
				return Double.NaN;
			}
			default:
				return "";
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			return "Error";
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		switch (column) {
		case tmpLbIdx:
			return true;
		case tmpUbIdx:
			return true;
		default:
			return false;
		}
	}
	
	public void setValueAt(Object value, int row, int column) {

		if (!aModel.loaded())
			return;

		if (column == tmpLbIdx || column == tmpUbIdx) {
			double bound = (Double) value;
			boolean error = false;
			try {
				if (column == tmpLbIdx)
				{
					if (bound >= (Double)getValueAt(row, origLbIdx)
						&& bound <= (Double)getValueAt(row, tmpUbIdx))
					{
						aModel.setTempLowerBound(row, bound);
					}
					else
					{
						error = true;
					}
				}
				else
				{
					if (bound >= (Double)getValueAt(row, tmpLbIdx)
							&& bound <= (Double)getValueAt(row, origUbIdx))
					{
						aModel.setTempUpperBound(row, bound);
					}
					else
					{
						error = true;
					}
				}
			} catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						null,
						e.getMessage());
			}
			if (error) {
				JOptionPane
						.showMessageDialog(
								null,
								"ERROR! New bounds may be outside original bounds or temporary bounds are reversed!");
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if ((Object) o == (Object) pModel)
			fireTableChanged(new TableModelEvent(this));
		else if ((Object) o == (Object) aModel)
			fireTableChanged(new TableModelEvent(this));
	}
	
	public class VariableTypeRowFilter extends RowFilter<VariableTableModel, Integer> {
		private VariableType typeToInclude;
		
		public VariableTypeRowFilter(VariableType type)
		{
			typeToInclude = type;
		}
		
		 @Override
		public boolean include(Entry<? extends VariableTableModel, ? extends Integer> entry) {
			 VariableType type = (VariableType)
					 entry.getModel().getValueAt(entry.getIdentifier(), typeIdx);
			 return type == typeToInclude;
		}
	}
	
	public class VariableRowFilter extends RowFilter<VariableTableModel, Integer> {
		
		private int[] variables;
		
		public VariableRowFilter(int[] vars)
		{
			variables = vars.clone();
			Arrays.sort(variables);
		}
		
		 @Override
		public boolean include(Entry<? extends VariableTableModel, ? extends Integer> entry) {
			 int constraint = (Integer) entry.getModel().getValueAt(entry.getIdentifier(), idIdx);
			 return Arrays.binarySearch(variables, constraint) >= 0;
		}
	}

	public void setObjectiveForBestPoint(int objective) {
		if (objective >= 0)
		{
			objectiveForBestPoint = objective;
			fireTableRowsUpdated(0, Math.max(0, getRowCount()-1));
		}
	}
}
