package adapter;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import model.AnalysisInstance;
import model.MProbeLib.EmpiricalShape;
import model.MProbeLib.RegionEffect;
import model.ProblemInstance;
import model.ProblemInstance.ConstraintType;
import model.ProblemInstance.FunctionShape;
import model.ProblemInstance.FunctionType;

public class ConstraintTableModel extends AbstractTableModel implements Observer {
	
	ProblemInstance pModel; // parts of adapted model
	AnalysisInstance aModel;

	// Defines the column ordering
	static final int nameIdx = 0;
	static final int idIdx = nameIdx+1;
	static final int typeIdx = idIdx+1;
	static final int lbIdx = typeIdx+1;
	static final int ubIdx = lbIdx+1;
	static final int algShapeIdx = ubIdx+1;
	static final int empShapeIdx = algShapeIdx+1;
	static final int regEffIdx = empShapeIdx+1;
	static final int totEffIdx = regEffIdx+1;
	static final int lbEffIdx = totEffIdx+1;
	static final int ubEffIdx = lbEffIdx+1;
	static final int satIdx = ubEffIdx+1;
	static final int totVarsIdx = satIdx+1;
	static final int realVarsIdx = totVarsIdx+1;
	static final int binVarsIdx = realVarsIdx+1;

	enum ConstrTableColumn {

		Name (nameIdx, "Name"),
		Identifier(idIdx, "Id."),
		Type(typeIdx, "Type"),
		LowerBound(lbIdx, "Lower bound"),
		UpperBound(ubIdx, "Upper bound"),
		AlgebraicShape(algShapeIdx, "Alg. shape"),
		EmpricalShape(empShapeIdx, "Emp. shape"),
		RegionEffect(regEffIdx, "Reg. effect"),
		TotEffectiveness(totEffIdx, "Total eff."),
		LBEffectiveness(lbEffIdx, "LB eff."),
		UBEffectiveness(ubEffIdx, "UB eff."),
		Satisfied(satIdx, "Satisfied"),
		TotVars(totVarsIdx, "Total vars."),
		RealVars(realVarsIdx, "Real vars."),
		BinVars(binVarsIdx, "Binary vars."),
		;		
		int columnIdx;
		String headerStr;
		ConstrTableColumn(int col, String header)
		{
			columnIdx = col;
			headerStr = header;
		}
		static ConstrTableColumn fromColumn(int col) {
			for (ConstrTableColumn cCol : ConstrTableColumn.values())
				if (cCol.columnIdx == col)
					return cCol;
			return null;
		}
	}

	public ConstraintTableModel(ProblemInstance pInst, AnalysisInstance aInst) {
		pModel = pInst;
		pModel.addObserver(this);

		aModel = aInst;
		aModel.addObserver(this);
	}

	@Override
	public Class getColumnClass(int column) {
		
		ConstrTableColumn col = ConstrTableColumn.fromColumn(column);
		switch (col) {
		case EmpricalShape:
		case RegionEffect:
		case TotEffectiveness:
		case LBEffectiveness:
		case UBEffectiveness:
		case Satisfied:
			if (aModel.isAnalysing())
				return String.class;
		default: break;
		}
		
		switch (ConstrTableColumn.fromColumn(column)) {
		case Name:
			return String.class;
		case Identifier:
		case TotVars:
		case RealVars:
		case BinVars:
			return Integer.class;
		case Type:
			return ConstraintType.class;
		case TotEffectiveness:
		case LBEffectiveness:
		case UBEffectiveness:
		case Satisfied:
		case LowerBound:
		case UpperBound:
			return Double.class;
		case AlgebraicShape:
			return FunctionType.class;
		case EmpricalShape:
			return EmpiricalShape.class;
		case RegionEffect:
			return RegionEffect.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return ConstrTableColumn.values().length;
	}

	@Override
	public String getColumnName(int column) {
		return ConstrTableColumn.fromColumn(column).headerStr;
	}

	@Override
	public int getRowCount() {
		if (!pModel.loaded())
			return 0;
		return pModel.constraints();
	}

	private enum VarCount {
		Total, Real, Integer, Binary, IntPBin
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (!pModel.loaded() || !aModel.loaded())
			return "";
		
		try {
			switch (ConstrTableColumn.fromColumn(column)) {
			case Name:
				return pModel.constraintName(row);
			case Identifier:
				return new Integer(row);
			case Type:
				return pModel.constraintType(row);
			case LowerBound:
				return new Double(pModel.constraintBounds(row)[0]);
			case UpperBound:
				return new Double(pModel.constraintBounds(row)[1]);
			case AlgebraicShape:
				return pModel.functionType(FunctionType.Constraint, row);
			case EmpricalShape:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getEmpiricalShape(FunctionType.Constraint, row));
			case RegionEffect:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getRegionEffect(row));
			case TotEffectiveness:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getEffectiveness(row).getTotalEffectiveness());
			case LBEffectiveness:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getEffectiveness(row).getLowerBoundEffectiveness());
			case UBEffectiveness:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getEffectiveness(row).getUpperBoundEffectiveness());
			case Satisfied:
				return (aModel.isAnalysing() ? "Analysis in progress" :
					aModel.getEffectiveness(row).getIneffectiveness());
			case TotVars:
				return new Integer(countVariables(row, VarCount.Total));
			case RealVars:
				return new Integer(countVariables(row, VarCount.Real));
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
			total = pModel.constraintVariables(constraint, variables);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Early out
		if (count == VarCount.Total)
			return total;

		for (int i = 0; i < total; ++i) {
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
	
	public class AlgebraicShapeRowFilter extends RowFilter<ConstraintTableModel, Integer> {
		
		private FunctionShape shapeToInclude;
		
		public AlgebraicShapeRowFilter(FunctionShape shape)
		{
			shapeToInclude = shape;
		}
		
		 @Override
		public boolean include(Entry<? extends ConstraintTableModel, ? extends Integer> entry) {
			 FunctionShape shape = (FunctionShape)
					 entry.getModel().getValueAt(entry.getIdentifier(), algShapeIdx);
			 return shape == shapeToInclude;
		}
	}
	
	public class EmpiricalShapeRowFilter extends RowFilter<ConstraintTableModel, Integer> {
		
		private EmpiricalShape shapeToInclude;
		
		public EmpiricalShapeRowFilter(EmpiricalShape shape)
		{
			shapeToInclude = shape;
		}

		@Override
		public boolean include(Entry<? extends ConstraintTableModel, ? extends Integer> entry) {
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

	public class ConstraintRowFilter extends RowFilter<ConstraintTableModel, Integer> {
		
		private int[] constraints;
		
		public ConstraintRowFilter(int[] constr)
		{
			constraints = constr.clone();
			Arrays.sort(constraints);
		}
		
		 @Override
		public boolean include(Entry<? extends ConstraintTableModel, ? extends Integer> entry) {
			 int constraint = (Integer) entry.getModel().getValueAt(entry.getIdentifier(), idIdx);
			 return Arrays.binarySearch(constraints, constraint) >= 0;
		}
	}
}
