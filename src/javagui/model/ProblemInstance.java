package model;

import java.io.File;
import java.util.Observable;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;

public class ProblemInstance extends Observable {
	private Pointer handle;
	private String secondaryName;

	public ProblemInstance() {
		secondaryName = new String();
	}

	public void load(String[] files) throws Exception {
		handle = MProbeLib.INSTANCE.mp_load(files.length, files);
		if (handle == Pointer.NULL)
			throw new Exception("Couldn't load problem instance."); // TODO:
																	// Proper
																	// exception
																	// hierarchy
		if (instanceName().isEmpty())
		{
			if (files.length > 0)
			{
				File file = new File(files[0]);
				secondaryName = file.getName();
			}
			else
			{
				secondaryName = "";
			}
		}
			
		setChanged();
		notifyObservers(null);
	}

	public boolean loaded() {
		boolean notLoaded = (handle == null) || (handle == Pointer.NULL);
		return !notLoaded;
	}

	public void unload() {
		MProbeLib.INSTANCE.mp_unload(handle);
		handle = Pointer.NULL;
		setChanged();
		notifyObservers(null);
	}

	public Pointer createAnalysis(AnalysisInstance aInst) {
		Pointer ptr = MProbeLib.INSTANCE.mp_createAnalysis(handle);
		if (ptr != Pointer.NULL)
			return ptr;
		return null;
	}

	protected void releaseName(Pointer cstrptr) {
		MProbeLib.INSTANCE.mp_releaseName(handle, cstrptr);
	}

	public String instanceName() {
		String name = MProbeLib.INSTANCE.mp_instanceName(handle);
		if (name.isEmpty())
		{
			return secondaryName;
		}
		return name;
	}

	public int variables() {// how many variable we have
		return MProbeLib.INSTANCE.mp_variables(handle);
	}

	public String variableName(int i) {
		Pointer cstr = MProbeLib.INSTANCE.mp_variableName(handle, i);
		String jstr = cstr.getString(0);
		releaseName(cstr);
		return jstr;
	}

	public void variablePresence(int variable, int[] constraints,
			int[] objectives) throws Exception {
		if (constraints.length < constraints() + 1)
			throw new Exception(
					"constraints array must be greater or equal to "
							+ (constraints() + 1)); // TODO: proper exception
													// hierarchy
		if (objectives.length < objectives() + 1)
			throw new Exception(
					"constraints array must be greater or equal to "
							+ (objectives() + 1)); // TODO: proper exception
													// hierarchy
		MProbeLib.INSTANCE.mp_variablePresence(handle, variable, constraints,
				objectives);
	}
	
	public void variablePresenceInConstraints(int variable, int[] constraints) throws Exception
	{
		int[] objs = new int[objectives() + 1];
		variablePresence(variable, constraints, objs);
	}

	public void variablePresenceInObjectives(int variable, int[] objectives) throws Exception
	{
		int[] cons = new int[constraints() + 1];
		variablePresence(variable, cons, objectives);
	}
	
	public enum VariableType {
		Real, Integer, Binary
	}

	public VariableType variableType(int var) throws Exception {
		int varType = MProbeLib.INSTANCE.mp_variableType(handle, var);
		switch (varType) {
		case 'r':
			return VariableType.Real;
		case 'i':
			return VariableType.Integer;
		case 'b':
			return VariableType.Binary;
		default:
			throw new Exception("Unexpected variable type " + (char) varType
					+ "\n Are the instance and variable index valid?");
		}
	}

	public void variableBounds(int x, DoubleByReference lower,
			DoubleByReference upper) {
		MProbeLib.INSTANCE.mp_variableBounds(handle, x, lower, upper);
	}

	public double[] variableBounds(int x) {
		DoubleByReference lowerBound = new DoubleByReference();
		DoubleByReference upperBound = new DoubleByReference();
		double[] retVal = new double[2];
		variableBounds(x, lowerBound, upperBound);
		retVal[0] = lowerBound.getValue();
		retVal[1] = upperBound.getValue();
		return retVal;
	}

	public int objectives() {
		return MProbeLib.INSTANCE.mp_objectives(handle);
	}

	public String objectiveName(int x) {
		Pointer cstr = MProbeLib.INSTANCE.mp_objectiveName(handle, x);
		String jstr = cstr.getString(0);
		releaseName(cstr);
		return jstr;
	}

	public enum ObjectiveType {
		Maximize, Minimize
	}

	public ObjectiveType objectiveType(int obj) throws Exception {
		switch (MProbeLib.INSTANCE.mp_objectiveType(handle, obj)) {
		case 'M':
			return ObjectiveType.Maximize;
		case 'm':
			return ObjectiveType.Minimize;
		default:
			throw new Exception("Unexpected objective type.");
		}
	}

	public int objectiveVariables(int objective, int[] variables)
			throws Exception {
		if (variables.length < variables())
			throw new Exception("Array must be greater or equal to "
					+ (variables())); // TODO: proper exception hierarchy
		return MProbeLib.INSTANCE
				.mp_objectiveVariables(handle, objective, variables);
	}

	public int constraints() {
		return MProbeLib.INSTANCE.mp_constraints(handle);
	}

	public String constraintName(int x) {
		Pointer cstr = MProbeLib.INSTANCE.mp_constraintName(handle, x);
		String jstr = cstr.getString(0);
		releaseName(cstr);
		return jstr;
	}

	public enum ConstraintType {
		Range, Equality, LEq, GEq, Unconstraining;
		
		public String toString() {
			switch (this) {
				case Range:
					return "Range";
				case Equality:
					return "Equality";
				case LEq:
					return "Less or equal";
				case GEq:
					return "Greater or equal";
				case Unconstraining:
					return "Unconstraining";
				default:
					return "";
			}
		}
	}

	public ConstraintType constraintType(int constraint) throws Exception {
		switch (MProbeLib.INSTANCE.mp_constraintType(handle, constraint)) {
		case 'r':
			return ConstraintType.Range;
		case 'e':
			return ConstraintType.Equality;
		case 'u':
			return ConstraintType.Unconstraining;
		case 'l':
			return ConstraintType.LEq;
		case 'g':
			return ConstraintType.GEq;
		default:
			throw new Exception("Unexpected constraint type.");
		}
	}

	public void constraintBounds(int x, DoubleByReference a, DoubleByReference b) {
		MProbeLib.INSTANCE.mp_constraintBounds(handle, x, a, b);
	}

	public double[] constraintBounds(int x) {
		DoubleByReference lowerBound = new DoubleByReference();
		DoubleByReference upperBound = new DoubleByReference();
		double[] retVal = new double[2];
		constraintBounds(x, lowerBound, upperBound);
		retVal[0] = lowerBound.getValue();
		retVal[1] = upperBound.getValue();
		return retVal;
	}

	public int constraintVariables(int constraint, int[] variables)
			throws Exception {
		if (variables.length < variables())
			throw new Exception("Array must be greater or equal to "
					+ (variables())); // TODO: proper exception hierarchy
		return MProbeLib.INSTANCE.mp_constraintVariables(handle, constraint,
				variables);
	}

	public enum FunctionType {
		Objective, Constraint
	}

	public enum FunctionShape {
		Nonlinear, Quadratic, Linear
	}

	public FunctionShape functionType(FunctionType funcType, int function)
			throws Exception {
		int type;
		switch (funcType) {
		case Objective:
			type = 'o';
			break;
		case Constraint:
			type = 'c';
			break;
		default:
			throw new Exception("Invalid function type.");
		}
		switch (MProbeLib.INSTANCE.mp_functionType(handle, type, function)) {
		case 'n':
			return FunctionShape.Nonlinear;
		case 'q':
			return FunctionShape.Quadratic;
		case 'l':
			return FunctionShape.Linear;
		default:
			throw new Exception("Unexpected function shape.");
		}
	}

}
