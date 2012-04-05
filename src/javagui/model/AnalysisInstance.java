package model;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import model.MProbeLib.EmpiricalShape;
import model.MProbeLib.HistType;
import model.MProbeLib.OptimumEffect;
import model.MProbeLib.RegionEffect;
import model.MProbeLib.ResultStatus;
import model.ProblemInstance.ConstraintType;
import model.ProblemInstance.FunctionType;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;

public class AnalysisInstance extends Observable implements Observer {
	private ProblemInstance pInst;
	private Pointer handle;

	double[] shapeHistUBs;
	double	 shapeHistLBinWidth;
	double[] slopeHistUBs;
	double	 slopeHistLBinWidth;
	double[] fValHistUBs;
	double fValHistLBinWidth;
	double[] lineLenHistUBs;
	double lineLenHistLBinWidth;
	
	boolean analysing;

	public AnalysisInstance() {
		handle = null;
		analysing = false;
	}
	
	synchronized public void load(ProblemInstance inst) {
		pInst = inst;
		pInst.addObserver(this);
		handle = pInst.createAnalysis(this);
		
		setDefaultHistogramBins(HistType.Shape);
		setDefaultHistogramBins(HistType.Slope);
		setDefaultHistogramBins(HistType.FunctionValue);
		setDefaultHistogramBins(HistType.LineLength);
	}
	
	synchronized public void unload() {
		if (handle != Pointer.NULL)
			MProbeLib.INSTANCE.mp_releaseAnalysis(handle);
		handle = null;
		setChanged();
		notifyObservers();
	}
	
	public boolean loaded() {
		return handle != Pointer.NULL;
	}
	
	public boolean isAnalysing() {
		return analysing;
	}

	synchronized public void variableBoundLineSample(FunctionType funcType, int function, boolean extraHists) throws Exception
	{
		int type;
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		analysing = true;
		
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
		MProbeLib.INSTANCE.mp_aVariableBoundLineSample(handle, type, function, extraHists?1:0);
		analysing = false;
		setChanged();
		notifyObservers();
	}
	
	synchronized public EmpiricalShape getEmpiricalShape(FunctionType funcType, int function) throws Exception
	{
		int type;
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
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
		
		return EmpiricalShape.fromValue(MProbeLib.INSTANCE.mp_aGetEmpiricalShape(handle, function, type));
	}

	synchronized public Object getRegionEffect(int constraint) throws Exception {
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		return RegionEffect.fromValue(MProbeLib.INSTANCE.mp_aGetRegionEffect(handle, constraint));
	}

	public class Effectiveness implements Comparable {
		private double upperBoundEff;
		private double lowerBoundEff;
		private ConstraintType type;
		private ResultStatus status;
		
		Effectiveness(ResultStatus stat, ConstraintType ctype, double lowerEff, double upperEff) {
			status = stat;
			type = ctype;
			lowerBoundEff = lowerEff;
			upperBoundEff = upperEff;
		}
		
		public double getTotalEffectiveness() {
			if (status != ResultStatus.Computed)
				return Double.NaN;
			return lowerBoundEff + upperBoundEff;
		}
		
		public double getUpperBoundEffectiveness() {
			if (status != ResultStatus.Computed)
				return Double.NaN;
			return upperBoundEff;
		}

		public double getLowerBoundEffectiveness() {
			if (status != ResultStatus.Computed)
				return Double.NaN;
			return lowerBoundEff;
		}
		
		public double getIneffectiveness() {
			if (status != ResultStatus.Computed)
				return Double.NaN;
			return 1 - (lowerBoundEff + upperBoundEff);
		}
		
		public String toString() {
			if (status != ResultStatus.Computed)
				return status.toString();

			if (type == ConstraintType.Equality)
			{
				if (getTotalEffectiveness() <= 0.999)
				{
					return "Possible. LT: " + Double.toString(lowerBoundEff)
							+ " EQ: " + Double.toString(getIneffectiveness())
							+ " GT: " + Double.toString(upperBoundEff);
				}
				else
				{
					return Double.toString(getTotalEffectiveness());
				}
			}
			else
			{
				return Double.toString(getTotalEffectiveness());
			}
		}

		@Override
		public int compareTo(Object arg) {
			if (!(arg instanceof Effectiveness))
				return -1;
			Effectiveness other = (Effectiveness) arg;
			
			if (status != other.status)
				return status.compareTo(other.status);
			else
				return Double.compare(getTotalEffectiveness(), other.getTotalEffectiveness());
		}
	}
	
	synchronized public Effectiveness getEffectiveness(int constraint) throws Exception
	{
		DoubleByReference lb = new DoubleByReference();
		DoubleByReference ub = new DoubleByReference();
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		ResultStatus status = ResultStatus.fromValue(MProbeLib.INSTANCE.mp_aEffectiveness(handle, constraint, lb, ub));
		return new Effectiveness(status, pInst.constraintType(constraint), lb.getValue(), ub.getValue());
	}

	synchronized public OptimumEffect getOptimumEffect(int objective) throws Exception {
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		return MProbeLib.OptimumEffect.fromValue(
				MProbeLib.INSTANCE.mp_aGetOptimumEffect(handle, objective));
	}
	
	synchronized public String getBestvalue(int objective) throws Exception
	{
		DoubleByReference extremum = new DoubleByReference();
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		ResultStatus status = ResultStatus.fromValue(MProbeLib.INSTANCE.mp_aExtremum(handle, objective, extremum));
		if (status == ResultStatus.Computed)
			return Double.toString(extremum.getValue());
		return status.toString();
	}
	
	synchronized public double[] getBestPoint(int objective) throws Exception
	{
		double[] point = new double[pInst.variables()];
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		ResultStatus status = ResultStatus.fromValue(MProbeLib.INSTANCE.mp_aExtremumPoint(handle, objective, point));
		MProbeLib.INSTANCE.mp_aExtremumPoint(handle, objective, point);
		if (status == ResultStatus.Computed)
			return point;
		return null;
	}

	synchronized public double getTempLowerBound(int variable) throws Exception {
		DoubleByReference lb = new DoubleByReference();
		DoubleByReference ub = new DoubleByReference();
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		MProbeLib.INSTANCE.mp_aBounds(handle, variable, lb, ub);
		return lb.getValue();
	}
	
	synchronized public void setTempLowerBound(int variable, double newBound) throws Exception {
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		MProbeLib.INSTANCE.mp_aSetLowerBound(handle, variable, newBound);
		setChanged();
		notifyObservers();
	}
	
	synchronized public double getTempUpperBound(int variable) throws Exception {
		DoubleByReference lb = new DoubleByReference();
		DoubleByReference ub = new DoubleByReference();
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		MProbeLib.INSTANCE.mp_aBounds(handle, variable, lb, ub);
		return ub.getValue();
	}

	synchronized public void setTempUpperBound(int variable, double newBound) throws Exception {
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		MProbeLib.INSTANCE.mp_aSetUpperBound(handle, variable, newBound);
		setChanged();
		notifyObservers();
	}

	synchronized public void resetBounds() throws Exception {
		
		if (handle == Pointer.NULL)
			throw new Exception("Null analysis instance pointer!");
		
		MProbeLib.INSTANCE.mp_aResetBounds(handle);
		setChanged();
		notifyObservers();
	}
	
	
	synchronized public double lineLengthMax()
	{
		return MProbeLib.INSTANCE.mp_aLineLengthMax(handle);
	}

	synchronized public double lineLengthMin()
	{
		return MProbeLib.INSTANCE.mp_aLineLengthMin(handle);
	}

	synchronized public void setLineLengthBounds(double min, double max)
	{
		MProbeLib.INSTANCE.mp_aSetLineLengthBounds(handle, min, max);
		setChanged();
		notifyObservers();
	}

	synchronized public boolean snapDiscreteComponents()
	{
		return MProbeLib.INSTANCE.mp_aSnapDiscreteComponents(handle) != 0;
	}

	synchronized public void setSnapDiscreteComponents(boolean snapBool)
	{
		MProbeLib.INSTANCE.mp_aSetSnapDiscreteComponents(handle, snapBool?1:0);
		setChanged();
		notifyObservers();
	}

	synchronized public int numLineSegments()
	{
		return MProbeLib.INSTANCE.mp_aNumLineSegments(handle);
	}

	synchronized public void setNumLineSegments(int numSegments)
	{
		MProbeLib.INSTANCE.mp_aSetNumLineSegments(handle, numSegments);
		setChanged();
		notifyObservers();
	}

	synchronized public int interiorLinePoints()
	{
		return MProbeLib.INSTANCE.mp_aInteriorLinePoints(handle);
	}

	synchronized public void setNumInteriorLinePoints(int numIntPoints)
	{
		MProbeLib.INSTANCE.mp_aSetNumInteriorLinePoints(handle, numIntPoints);
		setChanged();
		notifyObservers();
	}

	synchronized public int minimumPointsNeeded()
	{
		return MProbeLib.INSTANCE.mp_aMinimumPointsNeeded(handle);
	}

	synchronized public void setMinimumPointsNeeded(int minPts)
	{
		MProbeLib.INSTANCE.mp_aSetMinimumPointsNeeded(handle, minPts);
		setChanged();
		notifyObservers();
	}

	synchronized public double evalErrorTolerance()
	{
		return MProbeLib.INSTANCE.mp_aEvalErrorTolerance(handle);
	}

	synchronized public double setEvalErrorTolerance(double tolerance)
	{
		double result = MProbeLib.INSTANCE.mp_aSetEvalErrorTolerance(handle, tolerance);
		setChanged();
		notifyObservers();
		return result;
	}

	synchronized public double infinity()
	{
		return MProbeLib.INSTANCE.mp_aInfinity(handle);
	}

	synchronized public void setInfinity(double infinity)
	{
		MProbeLib.INSTANCE.mp_aSetInfinity(handle, infinity);
		setChanged();
		notifyObservers();
	}

	synchronized public double equalityTolerance()
	{
		return MProbeLib.INSTANCE.mp_aEqualityTolerance(handle);
	}

	synchronized public double setEqualityTolerance(double equalityTol)
	{
		double result = MProbeLib.INSTANCE.mp_aSetEqualityTolerance(handle, equalityTol);
		setChanged();
		notifyObservers();
		return result;
	}

	synchronized public double almostEqualTolerance()
	{
		return MProbeLib.INSTANCE.mp_aAlmostEqualTolerance(handle);
	}

	synchronized public boolean setAlmostEqualTolerance(double almostEqualTol)
	{
		boolean result = MProbeLib.INSTANCE.mp_aSetAlmostEqualTolerance(handle, almostEqualTol) != 0;
		setChanged();
		notifyObservers();
		return result;
	}
	
	synchronized public long getHistogramBin(HistType type, int bin)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramBin(handle, type.toInt(), bin);
	}

	synchronized public int getHistogramNumBins(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramNumBins(handle, type.toInt());
	}
	
	synchronized public long getHistogramNumOutside(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramNumOutside(handle, type.toInt());
	}
	
	synchronized public long getHistogramNumAboveRange(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramNumAboveRange(handle, type.toInt());
	}
	
	synchronized public long getHistogramNumBelowRange(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramNumBelowRange(handle, type.toInt());
	}
	
	synchronized public long getHistogramDataPoints(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramDataPoints(handle, type.toInt());
	}

	synchronized public double getHistogramMean(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramMean(handle, type.toInt());
	}
	
	synchronized public double getHistogramStdDev(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramStdDev(handle, type.toInt());
	}
	
	synchronized public double getHistogramVariance(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramVariance(handle, type.toInt());
	}
	
	synchronized public double getHistogramPopVariance(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramPopVariance(handle, type.toInt());
	}
	
	synchronized public double getHistogramMaximum(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramMaximum(handle, type.toInt());
	}
	
	synchronized public double getHistogramMinimum(HistType type)
	{
		return MProbeLib.INSTANCE.mp_aGetHistogramMinimum(handle, type.toInt());
	}
	
	synchronized public double getHistogramLowerBinWidth(HistType type)
	{
		switch (type) {
		case Shape:
			return shapeHistLBinWidth;
		case Slope:
			return slopeHistLBinWidth;
		case FunctionValue:
			return fValHistLBinWidth;
		case LineLength:
			return lineLenHistLBinWidth;
		default:
			return Double.NaN;
		}
	}
	
	synchronized public double[] getHistogramUpperBounds(HistType type)
	{
		switch (type) {
		case Shape:
			return shapeHistUBs;
		case Slope:
			return slopeHistUBs;
		case FunctionValue:
			return fValHistUBs;
		case LineLength:
			return lineLenHistUBs;
		default:
			return new double[0];
		}
	}
	
	synchronized public void setDefaultHistogramBins(HistType type) {
		switch (type)
		{
		case Shape:
			shapeHistUBs = new double[]{-100, -10, -1, -0.1, -0.0001, 0.0001, 0.1, 1, 10, 100, 1000};
			shapeHistLBinWidth = 900.0;
			setHistogramBins(HistType.Shape, shapeHistLBinWidth, shapeHistUBs);
			break;
		case Slope:
			slopeHistUBs = new double[]{5,10,25,50,100,250,500,750,1000};
			slopeHistLBinWidth = 5;
			setHistogramBins(HistType.Slope, slopeHistLBinWidth, slopeHistUBs);
			break;
		case FunctionValue:
			fValHistUBs = new double[]{-100, -10, -1, -0.1, -0.0001, 0.0001, 0.1, 1, 10, 100, 1000};
			fValHistLBinWidth = 900.0;
			setHistogramBins(HistType.FunctionValue, fValHistLBinWidth, fValHistUBs);
			break;
		case LineLength:
			lineLenHistUBs = new double[]{5,10,25,50,100,250,500,750,1000};
			lineLenHistLBinWidth = 5;
			setHistogramBins(HistType.LineLength, lineLenHistLBinWidth, lineLenHistUBs);
		}
	}
		
	
	synchronized public void setHistogramBins(HistType type, double firstBinWidth, double[] upperBounds) {
		Arrays.sort(upperBounds);
		switch (type)
		{
		case Shape:
			shapeHistLBinWidth = firstBinWidth;
			shapeHistUBs = upperBounds.clone();
			break;
		case Slope:
			slopeHistLBinWidth = firstBinWidth;
			slopeHistUBs = upperBounds.clone();
			break;
		case FunctionValue:
			fValHistLBinWidth = firstBinWidth;
			fValHistUBs = upperBounds.clone();
			break;
		case LineLength:
			lineLenHistLBinWidth = firstBinWidth;
			lineLenHistUBs = upperBounds.clone();
		default:
			return;
		}
		MProbeLib.INSTANCE.mp_aSetHistogramBins(handle, type.toInt(), upperBounds.length, firstBinWidth, upperBounds);
		setChanged();
		notifyObservers();
	}
	
	@Override
	synchronized public void update(Observable obsrvble, Object arg1) {
		if (obsrvble == pInst)
		{
			if (handle != Pointer.NULL)
				MProbeLib.INSTANCE.mp_releaseAnalysis(handle);
			{
				if (pInst.loaded())
					handle = pInst.createAnalysis(this);
				else
					handle = null;
			}
		}
		setChanged();
		notifyObservers();
	}
}
