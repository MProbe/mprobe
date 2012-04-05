package model;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;

public interface MProbeLib extends Library {
	MProbeLib INSTANCE = (MProbeLib) Native.loadLibrary("mprobe", MProbeLib.class);

	// *******************************************************************
	// declare all function names here i.e getVariableNames()
	// ////////////////////////////////////////////////////////////////
	char[] getVariableName(); // example

	void mprobe_init();

	void mprobe_deinit();

	void mp_loadPlugins(String path);

	void mp_unloadPlugins(String path);

	Pointer mp_load(int numFile, String[] files);

	void mp_unload(Pointer pHandle);

	int mp_compatibleFiles(int numFiles, String[] filenames, int[] compatibility);

	// Function forward from plugin
	void mp_releaseName(Pointer pHandle, Pointer name);

	String mp_instanceName(Pointer pHandle);

	int mp_instanceNameToBuf(Pointer pHandle, int maxLen, char[] buf);

	int mp_variables(Pointer pHandle);

	Pointer mp_variableName(Pointer pHandle, int variable);

	int mp_variableNameToBuf(Pointer pHandle, int variable, int maxLen,
			char[] buf);

	void mp_variablePresence(Pointer pHandle, int variable, int[] constraints,
			int[] objectives);

	int mp_variableType(Pointer pHandle, int variable);

	void mp_variableBounds(Pointer pHandle, int variable,
			DoubleByReference lower, DoubleByReference upper);

	int mp_objectives(Pointer pHandle);

	Pointer mp_objectiveName(Pointer pHandle, int objective);

	int mp_objectiveNameToBuf(Pointer pHandle, int objective, int maxLen,
			char[] buf);

	int mp_objectiveType(Pointer pHandle, int objective);

	int mp_objectiveVariables(Pointer pHandle, int objective, int[] variables);

	int mp_constraints(Pointer pHandle);

	Pointer mp_constraintName(Pointer pHandle, int constradouble);

	int mp_constraintNameToBuf(Pointer pHandle, int constraint, int maxLen,
			char[] buf);

	int mp_constraintType(Pointer pHandle, int constradouble);

	void mp_constraintBounds(Pointer pHandle, int constraint,
			DoubleByReference lowerBound, DoubleByReference upperBound);

	int mp_constraintVariables(Pointer pHandle, int constraint, int[] variables);

	int mp_functionType(Pointer pHandle, int type, int function);

	// Analysis functions
	Pointer mp_createAnalysis(Pointer pHandle);
	void mp_releaseAnalysis(Pointer pHandle);

	void mp_aVariableBoundLineSample(Pointer pHandle, int funcType, int func,
			int extraHistsBool);

	void mp_aBounds(Pointer aHandle, int var, DoubleByReference lower,
			DoubleByReference upper);

	void mp_aResetBounds(Pointer aHandle);

	void mp_aClampBounds(Pointer aHandle, double maxMagnitude);

	void mp_aSetBounds(Pointer aHandle, int var, double lower, double upper);

	double mp_aUpperBound(Pointer aHandle, int var);

	void mp_aSetUpperBound(Pointer aHandle, int var, double upper);

	double mp_aLowerBound(Pointer aHandle, int var);

	void mp_aSetLowerBound(Pointer aHandle, int var, double upper);

	double mp_aLineLengthMax(Pointer aHandle);

	double mp_aLineLengthMin(Pointer aHandle);

	void mp_aLineLengthBounds(Pointer aHandle, DoubleByReference min,
			DoubleByReference max);

	void mp_aSetLineLengthBounds(Pointer aHandle, double min, double max);

	int mp_aSnapDiscreteComponents(Pointer aHandle);

	void mp_aSetSnapDiscreteComponents(Pointer aHandle, int snapBool);

	int mp_aNumLineSegments(Pointer aHandle);

	void mp_aSetNumLineSegments(Pointer aHandle, int numSegments);

	int mp_aInteriorLinePoints(Pointer aHandle);

	void mp_aSetNumInteriorLinePoints(Pointer aHandle, int numIntPoints);

	int mp_aMinimumPointsNeeded(Pointer aHandle);

	void mp_aSetMinimumPointsNeeded(Pointer aHandle, int minPts);

	double mp_aEvalErrorTolerance(Pointer aHandle);

	double mp_aSetEvalErrorTolerance(Pointer aHandle, double tolerance);

	double mp_aInfinity(Pointer aHandle);

	void mp_aSetInfinity(Pointer aHandle, double infinity);

	double mp_aEqualityTolerance(Pointer aHandle);

	double mp_aSetEqualityTolerance(Pointer aHandle, double equalityTol);

	double mp_aAlmostEqualTolerance(Pointer aHandle);

	int mp_aSetAlmostEqualTolerance(Pointer aHandle, double almostEqualTol);

	enum ResultStatus {
		NotAvailable('n', "Not available"),
		InsufficientDataPoints('i', "Insufficient data points"),
		TooManyErrors('e', "Too many errors"),
		Computed('y', "Computed");

		private int value;
		private String description;

		ResultStatus(int value, String desc) {
			this.value = value;
			description = desc;
		}
		
		public String toString() {
				return description;
		}
		
		static public ResultStatus fromValue(int value) {
			for (ResultStatus rStat : ResultStatus.values())
				if (rStat.value == value)
					return rStat;
				return null;
		}
	};

	byte mp_aEffectiveness(Pointer aHandle, int constraint,
			DoubleByReference efflb, DoubleByReference effub);

	byte mp_aExtremum(Pointer aHandle, int objective,
			DoubleByReference extremeVal);

	byte mp_aExtremumPoint(Pointer aHandle, int objective, double[] point);

	enum HistType {
		Shape('s', "Shape"),
		Slope('d', "Slope"),
		FunctionValue('f', "Function Value"),
		LineLength('l', "Line length"); 
		private int value;
		private String desc;

		HistType(int value, String desc) {
			this.value = value;
			this.desc = desc;
		}
		
		public String toString() {
			return this.desc;
		}
		public int toInt()
		{
			return value;
		}
	};

	long mp_aGetHistogramBin(Pointer aHandle, int histType, int bin);
	int mp_aGetHistogramNumBins(Pointer aHandle, int histType);
	long mp_aGetHistogramNumOutside(Pointer aHandle, int histType);
	long mp_aGetHistogramNumAboveRange(Pointer aHandle, int histType);
	long mp_aGetHistogramNumBelowRange(Pointer aHandle, int histType);
	long mp_aGetHistogramDataPoints(Pointer aHandle, int histType);
	double mp_aGetHistogramMean(Pointer aHandle, int histType);
	double mp_aGetHistogramStdDev(Pointer aHandle, int histType);
	double mp_aGetHistogramVariance(Pointer aHandle, int histType);
	double mp_aGetHistogramPopVariance(Pointer aHandle, int histType);
	double mp_aGetHistogramMaximum(Pointer aHandle, int histType);
	double mp_aGetHistogramMinimum(Pointer aHandle, int histType);
	
	void mp_aSetHistogramBins(Pointer aHandle, int hist, int bins,
			double firstBinWidth, double[] upperBounds);

	enum EmpiricalShape {
		ESShapeError(ResultStatus.InsufficientDataPoints.value, "Shape Error"),
		ESTooManyMathErrors(ResultStatus.TooManyErrors.value, "Too many math errors"),
		ESNotAvailable(ResultStatus.NotAvailable.value, "Not available"),
		ESLinear(0, "Linear"),
		ESAlmostLinBoth(1, "Almost linear: Convex and concave"),
		ESAlmostLinConvex(2, "Almost linear: Convex"),
		ESAlmostLinConcave(3, "Almost linear: Concave"),
		ESConvex(4, "Convex"),
		ESAlmostConvex(5, "Almost convex"),
		ESConcave(6, "Concave"),
		ESAlmostConcave(7, "Almost concave"),
		ESBothConc(8, "Convex and concave");

		private int value;
		private String description;

		EmpiricalShape(int value, String desc) {
			this.value = value;
			description = desc;
		}
		
		public String toString() {
				return description;
		}
		
		static public EmpiricalShape fromValue(int value) {
			for (EmpiricalShape eShape : EmpiricalShape.values())
				if (eShape.value == value)
					return eShape;
			return null;
		}
		
		public int toInt()
		{
			return value;
		}
	};

	byte mp_aGetEmpiricalShape(Pointer aHandle, int func, int funcType);

	enum OptimumEffect {
		OEError(ResultStatus.InsufficientDataPoints.value, "Error"),
		OETooManyMathErrors(ResultStatus.TooManyErrors.value, "Too many math errors"),
		OENotAvailable(ResultStatus.NotAvailable.value, "Not available"),
		ObjectiveLocal(0, "Local"),
		ObjectiveGlobal(1, "Global"),
		ObjectiveAlmostGlobal(2, "Almost Global");

		private int value;
		private String description;

		OptimumEffect(int value, String desc) {
			this.value = value;
			description = desc;
		}
		
		public String toString() {
				return description;
		}
		
		static public OptimumEffect fromValue(int value) {
			for (OptimumEffect oEff : OptimumEffect.values())
				if (oEff.value == value)
					return oEff;
			return null;
		}
		
		public int toInt()
		{
			return value;
		}
	};

	byte mp_aGetOptimumEffect(Pointer aHandle, int objective);

	enum RegionEffect {
		REError(ResultStatus.InsufficientDataPoints.value, "Error"),
		RETooManyMathErrors(ResultStatus.TooManyErrors.value, "Too many errors"),
		RENotAvailable(ResultStatus.NotAvailable.value, "Not available"),
		REConvex(0, "Convex"),
		RENonconvex(1, "Nonconvex"),
		REAlmost(2, "Almost convex");

		private int value;
		private String description;

		RegionEffect(int value, String desc) {
			this.value = value;
			description = desc;
		}
		
		public String toString() {
				return description;
		}
		
		static public RegionEffect fromValue(int value) {
			for (RegionEffect rEff : RegionEffect.values())
				if (rEff.value == value)
					return rEff;
			return null;
		}
		public int toInt()
		{
			return value;
		}
	};

	byte mp_aGetRegionEffect(Pointer aHandle, int constraint);

}
