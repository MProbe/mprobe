/*
    Copyright (C) 2011  Walter Waldron

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


#ifndef ANALYSIS_H_
#define ANALYSIS_H_

#include <stdint.h>

#include <boost/random/mersenne_twister.hpp>
#include <Poco/SharedPtr.h>

#include "histogram.h"
#include "probleminstance.h"
#include "types.h"

using Poco::SharedPtr;
using boost::mt19937;

class Analysis
{
public:
	Analysis(SharedPtr<ProblemInstance>&);
	virtual ~Analysis();

	// Analysis functions
	void variableBoundLineSample(int funcType, int func, bool extraHists);

	// Settings
	void bounds(int var, Real& lower, Real& upper) const;
	void resetBounds();
	void clampBounds(Real maxMagnitude);
	void setBounds(int var, Real lower, Real upper);
	inline Real upperBound(int var) const { return m_bounds[2*var+1]; }
	void setUpperBound(int var, Real upper);
	inline Real lowerBound(int var) const {return m_bounds[2*var]; }
	void setLowerBound(int var, Real lower);

	Real lineLengthMax() const;
	Real lineLengthMin() const;
	void lineLengthBounds(Real& min, Real& max) const;
	void setLineLengthBounds(Real, Real);

	bool snapDiscreteComponents() const;
	void setSnapDiscreteComponents(bool);

	unsigned numLineSegments() const;
	void setNumLineSegments(unsigned); // Number of line segments to cast

	unsigned interiorLinePoints() const;
	void setNumInteriorLinePoints(unsigned);

	unsigned minimumPointsNeeded() const;
	void setMinimumPointsNeeded(unsigned);

	Real evalErrorTolerance() const;
	bool setEvalErrorTolerance(Real);

	Real infinity() const;
	void setInfinity(Real);

	Real equalityTolerance() const;
	bool setEqualityTolerance(Real);

	Real almostEqualTolerance() const;
	bool setAlmostEqualTolerance(Real);

	// Analysis result functions

	enum ResultStatus {
		NotAvailable = 'n',
		InsufficientDataPoints = 'i',
		TooManyErrors = 'e',
		Computed = 'y'
	};

	uint8_t effectiveness(int constraint, Real& efflb, Real& effub) const;
	uint8_t extremum(int objective, Real&) const;
	uint8_t extremumPoint(int objective, Real* point) const;
	uint8_t extremumPoint(int objective, const Real** point) const;

	enum ExtraHistType {
		FunctionValue = 'f',
		LineLength = 'l',
		Slope = 'd',
		Shape = 's'
	};
	const Histogram& getExtraHistogram(ExtraHistType which) const;
	void setExtraHistogram(ExtraHistType which, const Histogram& hist);

	enum EmpiricalShape {
		ESShapeError = InsufficientDataPoints,
		ESTooManyMathErrors = TooManyErrors,
		ESNotAvailable = NotAvailable,
		Linear = 0,
		AlmostLinBoth,
		AlmostLinConvex,
		AlmostLinConcave,
		Convex,
		AlmostConvex,
		Concave,
		AlmostConcave,
		BothConc
	};

	uint8_t getEmpiricalShape(int func, int funcType);

	enum OptimumEffect {
		OEError = InsufficientDataPoints,
		OETooManyMathErrors = TooManyErrors,
		OENotAvailable = NotAvailable,
		ObjectiveLocal = 0,
		ObjectiveGlobal,
		ObjectiveAlmostGlobal
	};
	uint8_t getOptimumEffect(int objective);

	enum RegionEffect {
		REError = InsufficientDataPoints,
		RETooManyMathErrors = TooManyErrors,
		RENotAvailable = NotAvailable,
		REConvex = 0,
		RENonconvex,
		REAlmost

	};
	uint8_t getRegionEffect(int constraint);
	
protected:
	inline Real& lowerBound(int var) { return m_bounds[2*var]; }
	inline Real& upperBound(int var) { return m_bounds[2*var+1]; }

	void snapPoint(int vars, const int* presences, const Real* point, Real* snappedPoint);
	void setExtremum(int objective, uint8_t status, Real val, const Real* point);
	void updateExtremum(int objective, Real val, const Real* point);
	void initShapeHist(Histogram& shapeHist) const;
	void accumulateEffectiveness(int constraint, Real value, uint64_t& effTotal, uint64_t& effLowerB, uint64_t& effUpperB);
	void setEffectiveness(int constraint, uint64_t effTotal, uint64_t effLowerB, uint64_t effUpperB);
	uint8_t deduceFunctionShape(const Histogram& shapeHist);
	uint8_t deduceRegionEffect(int constrType, uint8_t shape);
	uint8_t deduceOptimumEffect(int objType, uint8_t shape);
private:

	SharedPtr<ProblemInstance> m_problem;

	mt19937 m_rng; // Random number generator

	Real* m_bounds; // Variable bounds for analysis

	// Settings
	Real m_equalityTol;
	Real m_almostEqTol;
	Real m_inf;
	Real m_lineLenMin;
	Real m_lineLenMax;
	Real m_allowedEvalErrors; // Fraction
	unsigned m_lineSegments;
	unsigned m_interiorLinePts;
	unsigned m_minPointsNeeded;

	bool m_snap;

	uint8_t* m_shapes;

	// Constraint analysis results
	uint8_t* m_effStatus;
	Real* m_effectiveness;
	uint8_t* m_regionEffects;

	// Objective analysis results
	uint8_t* m_extrStatus;
	Real* m_objExtrema;
	Real* m_objExtremumPoints;
	uint8_t* m_optimumEffects;

	// Extra histograms
	Histogram m_fvalHist;
	Histogram m_lineLenHist;
	Histogram m_slopeHist;
	Histogram m_shapeHist;
};

#endif // ANALYSIS_H_
