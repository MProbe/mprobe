package adapter;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import model.AnalysisInstance;
import model.MProbeLib.EmpiricalShape;
import model.MProbeLib.OptimumEffect;
import model.ProblemInstance;
import model.ProblemInstance.FunctionShape;
import model.ProblemInstance.FunctionType;
import model.ProblemInstance.ObjectiveType;

public class ObjectiveTableModel extends AbstractTableModel implements Observer {

	// parts of adapted model
	private ProblemInstance pModel;
	private AnalysisInstance aModel;
	
	// Defines the column ordering
	static final int nameIdx = 0;
	static final int idIdx = nameIdx+1;
	static final int typeIdx = idIdx+1;
	static final int algShapeIdx = typeIdx+1;
	static final int empShapeIdx = algShapeIdx+1;
	static final int optEffIdx = empShapeIdx+1;
	static final int bestValIdx = optEffIdx+1;
	static final int totVarsIdx = bestValIdx+1;
	static final int realVarsIdx = totVarsIdx+1;
	static final int intPBinVarsIdx = realVarsIdx+1;
	static final int binVarsIdx = intPBinVarsIdx+1;

	enum ObjTableColumn {

		Name (nameIdx, "Name"),
		Identifier(idIdx, "Id."),
		Type(typeIdx, "Type"),
		AlgebraicShape(algShapeIdx, "Alg. shape"),
		EmpricalShape(empShapeIdx, "Emp. shape"),
		OptimumEffect(optEffIdx, "Optimum eff."),
		BestValue(bestValIdx, "Best value"),
		TotVars(totVarsIdx, "Total vars."),
		RealVars(realVarsIdx, "Real vars."),
		IntPBinVars(intPBinVarsIdx, "Int. + Bin. vars."),
		BinVars(binVarsIdx, "Binary vars."),
		;		
		int columnIdx;
		String headerStr;
		ObjTableColumn(int col, String header)
		{
			columnIdx = col;
			headerStr = header;
		}
		static ObjTableColumn fromColumn(int col) {
			for (ObjTableColumn oCol : ObjTableColumn.values())
				if (oCol.columnIdx == col)
					return oCol;
			return null;
		}
	}

	public ObjectiveTableModel(ProblemInstance pInst, AnalysisInstance aInst) {
		pModel = pInst;
		pModel.addObserver(this);
		
		aModel = aInst;
		aModel.addObserver(this);
	}

	@Override
	public Class getColumnClass(int column) {
		
		ObjTableColumn col = ObjTableColumn.fromColumn(column);
		switch (col) {
		case EmpricalShape:
		case OptimumEffect:
		case BestValue:
			if (aModel.isAnalysing())
				return String.class;
		default: break;
		}
		
		switch (col) {
		case Name:
			return String.class;
		case Identifier:
		case TotVars:
		case RealVars:
		case IntPBinVars:
		case BinVars:
			return Integer.class;
		case Type:
			return ObjectiveType.class;
		case AlgebraicShape:
			return FunctionType.class;
		case EmpricalShape:
			return EmpiricalShape.class;
		case OptimumEffect:
			return OptimumEffect.class;
		case BestValue:
			return String.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return ObjTableColumn.values().length;
	}

	@Override
	public String getColumnName(int column) {
		return ObjTableColumn.fromColumn(column).headerStr;
	}

	@Override
	public int getRowCount() {
		if (!pModel.loaded())
			return 0;
		return pModel.objectives();
	}

	private enum VarCount {
		Total, Real, Integer, Binary, IntPBin
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (!pModel.loaded() || !aModel.loaded())
			return "";
		
		try {
			switch (ObjTableColumn.fromColumn(column)) {
			case Name:
				return pModel.objectiveName(row);
			case Identifier:
				return new Integer(row);
			case Type:
				return pModel.objectiveType(row);
			case AlgebraicShape:
				return pModel.functionType(
							ProblemInstance.FunctionType.Objective, row);
			case EmpricalShape:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getEmpiricalShape(ProblemInstance.FunctionType.Objective, row));
			case OptimumEffect:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getOptimumEffect(row));
			case BestValue:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getBestvalue(row));
			case TotVars:
				return new Integer(countVariables(row, VarCount.Total));
			case RealVars:
				return new Integer(countVariables(row, VarCount.Real));
			case IntPBinVars:
				return new Integer(countVariables(row, VarCount.IntPBin));
			case BinVars:
				return new Integer(countVariables(row, VarCount.Binary));
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

		default:
			return false;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if ((Object) o == (Object) pModel)
			fireTableChanged(new TableModelEvent(this));
		else if ((Object) o == (Object) aModel)
			fireTableChanged(new TableModelEvent(this));
	}

	private int countVariables(int constraint, VarCount count) {
		int[] variables = new int[pModel.variables() + 1];
		int total = 0;
		int real = 0;
		int integer = 0;
		int binary = 0;
		try {
			total = pModel.objectiveVariables(constraint, variables);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Early out
		if (count == VarCount.Total)
			return total;

		for (int i = 1; i <= total; ++i) {
			try {
				switch (pModel.variableType(variables[i])) {
				case Real:
					++real;
					break;
				case Integer:
					++integer;
					break;
				case Binary:
					++binary;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		switch (count) {
		case Real:
			return real;
		case Integer:
			return integer;
		case Binary:
			return binary;
		case IntPBin:
			return integer + binary;
		default:
			return -1;
		}
	}
	
	public class AlgebraicShapeRowFilter extends RowFilter<ObjectiveTableModel, Integer> {
		
		private FunctionShape shapeToInclude;
		
		public AlgebraicShapeRowFilter(FunctionShape shape)
		{
			shapeToInclude = shape;
		}
		
		 @Override
		public boolean include(Entry<? extends ObjectiveTableModel, ? extends Integer> entry) {
			 FunctionShape shape = (FunctionShape)
					 entry.getModel().getValueAt(entry.getIdentifier(), algShapeIdx);
			 return shape == shapeToInclude;
		}
	}
	
	public class EmpiricalShapeRowFilter extends RowFilter<ObjectiveTableModel, Integer> {
		
		private EmpiricalShape shapeToInclude;
		
		public EmpiricalShapeRowFilter(EmpiricalShape shape)
		{
			shapeToInclude = shape;
		}

		@Override
		public boolean include(Entry<? extends ObjectiveTableModel, ? extends Integer> entry) {
			Object value = entry.getModel().getValueAt(entry.getIdentifier(), empShapeIdx);
			if (value instanceof EmpiricalShape)
			{
				EmpiricalShape shape = (EmpiricalShape) value;
				return shape.toInt() == shapeToInclude.toInt();
			}
			else
			{
				return true;
			}
		}
	}
	
	public class ObjectiveRowFilter extends RowFilter<ObjectiveTableModel, Integer> {
		
		private int[] objectives;
		
		public ObjectiveRowFilter(int[] objs)
		{
			objectives = objs.clone();
			Arrays.sort(objectives);
		}
		
		 @Override
		public boolean include(Entry<? extends ObjectiveTableModel, ? extends Integer> entry) {
			 int objective = (Integer) entry.getModel().getValueAt(entry.getIdentifier(), idIdx);
			 return Arrays.binarySearch(objectives, objective) >= 0;
		}
	}
}
