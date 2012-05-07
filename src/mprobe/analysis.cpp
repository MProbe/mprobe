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


#include "analysis.h"

#include <limits>
#include <ctime>

#include <boost/random/uniform_real.hpp>
#include <boost/random/variate_generator.hpp>
#include <boost/math/special_functions/sign.hpp>
#include <boost/smart_ptr/scoped_array.hpp>

#include <mprobe.h>

using boost::variate_generator;
using boost::uniform_real;
using boost::math::copysign;
using boost::scoped_array;

Analysis::Analysis(SharedPtr< ProblemInstance >& p)
	: m_problem(p),
	m_rng(static_cast<unsigned int>(std::time(0))),
	m_equalityTol(0.0001),
	m_almostEqTol(0.1),
	m_inf(1e20),
	m_lineLenMin(1e-20),
	m_lineLenMax(m_inf),
	m_allowedEvalErrors(0.10),
	m_lineSegments(10000),
	m_interiorLinePts(7),
	m_minPointsNeeded(25),
	m_snap(true)
{
	m_bounds = new Real[p->variables()*2];
	for (int i = 0; i < p->variables(); ++i)
	{
		p->variableBounds(i, &lowerBound(i), &upperBound(i));
	}
	clampBounds(m_inf);

	m_shapes = new uint8_t[p->constraints()+p->objectives()];
	std::fill(m_shapes, &m_shapes[p->constraints()+p->objectives()], ESNotAvailable);

	m_effStatus = new uint8_t[p->constraints()];
	m_effectiveness = new Real[2*p->constraints()];
	std::fill(m_effStatus, &m_effStatus[p->constraints()], NotAvailable);
	m_regionEffects = new uint8_t[p->constraints()];
	std::fill(m_regionEffects, &m_regionEffects[p->constraints()], RENotAvailable);

	m_extrStatus = new uint8_t[p->objectives()];
	m_objExtrema = new Real[p->objectives()];
	m_objExtremumPoints = new Real[p->objectives()*p->variables()];
	m_optimumEffects = new uint8_t[p->objectives()];
	std::fill(m_extrStatus, &m_extrStatus[p->objectives()], NotAvailable);
	std::fill(m_optimumEffects, &m_optimumEffects[p->objectives()], OENotAvailable);

	// Defaults for extra histograms
	Real defFValHistUpperBounds[] = { -100, -10, -1, -0.1, 0.1, 1, 10, 100, 1e3 };
	const Real defFValHist1stBinWidth = 900.0;
	m_fvalHist.setBins(sizeof(defFValHistUpperBounds)/sizeof(Real), defFValHist1stBinWidth, defFValHistUpperBounds);

	Real defLineLenHistUpperBounds[] = { 5, 10, 25, 50, 100, 250, 500, 750, 1000 };
	const Real defLineLenHist1stBinWidth = 4;
	m_lineLenHist.setBins(sizeof(defLineLenHistUpperBounds)/sizeof(Real), defLineLenHist1stBinWidth, defLineLenHistUpperBounds);

	Real defSlopeHistUpperBounds[] = { 5, 10, 25, 50, 100, 250, 500, 750, 1000 };
	const Real defSlopeHist1stBinWidth = 4;
	m_slopeHist.setBins(sizeof(defSlopeHistUpperBounds)/sizeof(Real), defSlopeHist1stBinWidth, defSlopeHistUpperBounds);

	Real defShapeHistUpperBounds[] = { -100, -10, -1, -0.1, -0.0001, 0.0001, 0.1, 1, 10, 100, 1e3 };
	const Real defShapeHist1stBinWidth = 900.0;
	m_shapeHist.setBins(sizeof(defShapeHistUpperBounds)/sizeof(Real), defShapeHist1stBinWidth, defShapeHistUpperBounds);
}

Analysis::~Analysis()
{
	delete[] m_optimumEffects;
	delete[] m_extrStatus;
	delete[] m_objExtrema;
	delete[] m_objExtremumPoints;
	delete[] m_effectiveness;
	delete[] m_effStatus;
	delete[] m_shapes;
	delete[] m_bounds;
}

void Analysis::bounds(int var, Real& lower, Real& upper) const
{
	lower = lowerBound(var);
	upper = upperBound(var);
}

void Analysis::resetBounds()
{
	for (int i = 0; i < m_problem->variables(); ++i)
	{
		m_problem->variableBounds(i, &lowerBound(i), &upperBound(i));
	}
}

void Analysis::clampBounds(Real mag)
{
	mag = std::abs(mag);
	for (int idx = 0; idx < 2*m_problem->variables(); ++idx)
		if (std::abs(m_bounds[idx]) > mag)
			m_bounds[idx] = copysign(mag, m_bounds[idx]);
}

void Analysis::setBounds(int var, Real lower, Real upper)
{
	lowerBound(var) = lower;
	upperBound(var) = upper;
}

void Analysis::setLowerBound(int var, Real lower)
{
	lowerBound(var) = lower;
}

void Analysis::setUpperBound(int var, Real upper)
{
	upperBound(var) = upper;
}

Real Analysis::lineLengthMin() const
{
	return m_lineLenMin;
}

Real Analysis::lineLengthMax() const
{
	return m_lineLenMax;
}

void Analysis::lineLengthBounds(Real& min, Real& max) const
{
	min = m_lineLenMin;
	max = m_lineLenMax;
}

void Analysis::setLineLengthBounds(Real min, Real max)
{
	if ( min > max)
		std::swap(min, max);
	m_lineLenMin = std::max(min, std::numeric_limits<Real>::epsilon());
	m_lineLenMax = max;
}

bool Analysis::snapDiscreteComponents() const
{
	return m_snap;
}

void Analysis::setSnapDiscreteComponents(bool snap)
{
	m_snap = snap;
}

unsigned int Analysis::numLineSegments() const
{
	return m_lineSegments;
}

void Analysis::setNumLineSegments(unsigned segs)
{
	m_lineSegments = segs;
}

unsigned int Analysis::interiorLinePoints() const
{
	return m_interiorLinePts;
}

void Analysis::setNumInteriorLinePoints(unsigned intPts)
{
	m_interiorLinePts = intPts;
}

unsigned int Analysis::minimumPointsNeeded() const
{
	return m_minPointsNeeded;
}

void Analysis::setMinimumPointsNeeded(unsigned ptsNeeded)
{
	m_minPointsNeeded = ptsNeeded;
}

Real Analysis::evalErrorTolerance() const
{
	return m_allowedEvalErrors;
}

bool Analysis::setEvalErrorTolerance(Real tol)
{
	if (tol > 1.0)
		return false;
	m_allowedEvalErrors = tol;
	return true;
}

Real Analysis::infinity() const
{
	return m_inf;
}

void Analysis::setInfinity(Real inf)
{
	if (inf > 0)
	{
		if (m_lineLenMax > inf)
			m_lineLenMax = inf;
		if (inf > m_inf)
			resetBounds();
		clampBounds(inf);
		m_inf = inf;
	}
}

Real Analysis::equalityTolerance() const
{
	return m_equalityTol;
}

bool Analysis::setEqualityTolerance(Real eqTol)
{
	if (eqTol >= m_almostEqTol)
		return false;
	m_equalityTol = eqTol;
	return true;
}

Real Analysis::almostEqualTolerance() const
{
	return m_almostEqTol;
}

bool Analysis::setAlmostEqualTolerance(Real alEqTol)
{
	if (alEqTol < m_equalityTol)
		return false;
	m_almostEqTol = alEqTol;
	return true;
}

uint8_t Analysis::effectiveness(int constraint, Real& efflb, Real& effub) const
{
	efflb = m_effectiveness[2*constraint];
	effub = m_effectiveness[2*constraint+1];
	return m_effStatus[constraint];
}


uint8_t Analysis::extremum(int objective, Real& ext) const
{
	ext = m_objExtrema[objective];
	return m_extrStatus[objective];
}

uint8_t Analysis::extremumPoint(int objective, Real* point) const
{
	const unsigned ptOffset = objective * m_problem->variables();
	std::copy(&m_objExtremumPoints[ptOffset], &m_objExtremumPoints[ptOffset + m_problem->variables()], point);
	return m_extrStatus[objective];
}

uint8_t Analysis::extremumPoint(int objective, const Real** point) const
{
	const unsigned ptOffset = objective * m_problem->variables();
	*point = &m_objExtremumPoints[ptOffset];
	return m_extrStatus[objective];
}

const Histogram& Analysis::getExtraHistogram(Analysis::ExtraHistType which) const
{
	switch (which) {
		case FunctionValue:
			return m_fvalHist;
		case LineLength:
			return m_lineLenHist;
		case Slope:
			return m_slopeHist;
		case Shape:
		default:
			return m_shapeHist;
	}
}

void Analysis::setExtraHistogram(Analysis::ExtraHistType which, const Histogram& hist)
{
	switch (which) {
		case FunctionValue:
			m_fvalHist = hist;
			m_fvalHist.reset();
			break;
		case LineLength:
			m_lineLenHist = hist;
			m_lineLenHist.reset();
			break;
		case Slope:
			m_slopeHist = hist;
			m_slopeHist.reset();
			break;
		case Shape:
			m_shapeHist = hist;
			m_shapeHist.reset();
			break;
		default:
			break;
	}
}

void Analysis::snapPoint(int vars, const int* presences, const Real* point, Real* snappedPoint)
{
	for (int i = 0; i < vars; ++i)
	{
		const int var = presences[i];
		const Real& p = point[var];
		Real& sp = snappedPoint[var];
		switch (m_problem->variableType(var)) {
			case VBinary:
				sp = (p < 0.5 ? 0.0 : 1.0);
				break;
			case VInteger:
				sp = std::floor(p + 0.5);
				if (sp < lowerBound(var))
					sp += 1.0;
				if (sp > upperBound(var))
					sp -= 1.0;
				break;
			case VReal:
			default:
				sp = p;
		}
	}
}

void Analysis::setExtremum(int objective, uint8_t status, Real val, const Real* point)
{
	const unsigned ptOffset = objective * m_problem->variables();
	if (status == NotAvailable || !point)
	{
		m_extrStatus[objective] = NotAvailable;
		return;
	}
	m_extrStatus[objective] = status;
	m_objExtrema[objective] = val;
	std::copy(point, &point[m_problem->variables()], &m_objExtremumPoints[ptOffset]);
}

void Analysis::updateExtremum(int objective, Real val, const Real* point)
{
	Real current;
	bool replace = false;

	if (extremum(objective, current) != Computed)
	{
		replace = true;
	}
	else if (m_problem->objectiveType(objective) == Maximization && val > current)
	{
		replace = true;
	}
	else if (m_problem->objectiveType(objective) == Minimization && val < current)
	{
		replace = true;
	}
	if (replace)
	{
		setExtremum(objective, Computed, val, point);
	}
}

void Analysis::initShapeHist(Histogram& shapeHist) const
{
	Real shapeHistBound[3] = { -m_equalityTol, m_equalityTol, m_almostEqTol};
	const Real negAlmostEqBinWidth = m_almostEqTol - m_equalityTol;
	shapeHist.setBins(3, negAlmostEqBinWidth, shapeHistBound);
}

void Analysis::accumulateEffectiveness(int constraint, Real value, uint64_t& effTotal, uint64_t& effLowerB, uint64_t& effUpperB)
{
	Real ub, lb;
	++effTotal;
	m_problem->constraintBounds(constraint, &lb, &ub);
	if (value < lb - m_equalityTol)
	{
		++effLowerB;
	}
	else if (value > ub + m_equalityTol)
	{
		++effUpperB;
	}
}

void Analysis::variableBoundLineSample(int funcType, int func, bool extraHists)
{
	variate_generator<mt19937&, uniform_real<Real> > variateGen(m_rng, uniform_real<Real>());
	int (ProblemInstance::*evalFuncVal)(int,Real*,Real*); // A little C++ magic to remove some duplication
	uint64_t effTotal = 0, effLowerB = 0, effUpperB = 0; // Effectiveness accumulators
	int totalVars, realVars, binaryVars, integerVars;
	scoped_array<Real> endPt1, endPt2;
	scoped_array<Real> tempPoint;
	const int* variablePresences;
	Real endPt1Val, endPt2Val;
	unsigned lineErrors = 0;
	unsigned mathErrors = 0;
	Histogram shape;
	bool snapPoints;

	initShapeHist(shape);

	endPt1.reset(new Real[m_problem->variables()]);
	std::fill(endPt1.get(), &endPt1[m_problem->variables()], 0.0);

	endPt2.reset(new Real[m_problem->variables()]);
	std::fill(endPt2.get(), &endPt2[m_problem->variables()], 0.0);

	tempPoint.reset(new Real[m_problem->variables()]);
	std::fill(tempPoint.get(), &tempPoint[m_problem->variables()], 0.0);

	if (funcType == Objective)
	{
		evalFuncVal = &ProblemInstance::evaluateObjectiveVal;
		totalVars = m_problem->objectiveVariableTypes(func, &realVars, &binaryVars, &integerVars);
		m_problem->objectiveVariables(func, &variablePresences);
	}
	else
	{
		evalFuncVal = &ProblemInstance::evaluateConstraintVal;
		totalVars = m_problem->constraintVariableTypes(func, &realVars, &binaryVars, &integerVars);
		m_problem->constraintVariables(func, &variablePresences);
	}

	snapPoints = (binaryVars + integerVars > 0) && m_snap;

	if (extraHists)
	{
		m_fvalHist.reset();
		m_lineLenHist.reset();
		m_slopeHist.reset();
		m_shapeHist.reset();
	}

	for (unsigned i = 0; i < m_lineSegments; ++i)
	{
		Real lineLength = 0.0;
		for (int j = 0; j < totalVars; ++j)
		{
			const int var = variablePresences[j]; // shorthand

			// Generate values within the bounds for this variable
			if (std::abs(lowerBound(var) - upperBound(var)) > std::numeric_limits<Real>::epsilon()*100)
			{
				variateGen.distribution() = uniform_real<Real>(lowerBound(var), upperBound(var));
				// Generate the values
				endPt1[var] = variateGen();
				endPt2[var] = variateGen();

				// Add contribution to line length
				const Real delta = endPt1[var] - endPt2[var];
				lineLength += delta * delta;
			}
			else
			{
				// This is to avoid violating some preconditions which leads to freezing.
				endPt1[var] = lowerBound(var);
				endPt2[var] = upperBound(var);
			}
		}

		lineLength = std::sqrt(lineLength);

		if (lineLength >= m_lineLenMax)
		{
			++lineErrors;
			continue;
		}
		else if (lineLength <= m_lineLenMin)
		{
			++lineErrors;
			continue;
		}

		if (!(m_problem->*evalFuncVal)(func, endPt1.get(), &endPt1Val))
		{
			++lineErrors;
			++mathErrors;
			continue;
		}
		if (!(m_problem->*evalFuncVal)(func, endPt2.get(), &endPt2Val))
		{
			++lineErrors;
			++mathErrors;
			continue;
		}

		if (std::abs(endPt1Val) >= m_inf || std::abs(endPt2Val) >= m_inf)
		{
			++lineErrors;
			++mathErrors;
			continue;
		}

		if (funcType == Objective)
		{
			updateExtremum(func, endPt1Val, endPt1.get());
			updateExtremum(func, endPt2Val, endPt2.get());
		}
		else
		{
			if (snapPoints)
			{
				Real snappedVal;

				snapPoint(totalVars, variablePresences, endPt1.get(), tempPoint.get());

				if ((m_problem->*evalFuncVal)(func, tempPoint.get(), &snappedVal)
					&& std::abs(snappedVal) < m_inf)
				{
					accumulateEffectiveness(func, snappedVal, effTotal, effLowerB, effUpperB);
				}

				snapPoint(totalVars, variablePresences, endPt2.get(), tempPoint.get());

				if ((m_problem->*evalFuncVal)(func, tempPoint.get(), &snappedVal)
					&& std::abs(snappedVal) < m_inf)
				{
					accumulateEffectiveness(func, snappedVal, effTotal, effLowerB, effUpperB);
				}
			}
			else
			{
				accumulateEffectiveness(func, endPt1Val, effTotal, effLowerB, effUpperB);
				accumulateEffectiveness(func, endPt2Val, effTotal, effLowerB, effUpperB);
			}
		}

		if (extraHists)
		{
			m_fvalHist.accumulatePoint(endPt1Val);
			m_fvalHist.accumulatePoint(endPt2Val);
			m_lineLenHist.accumulatePoint(lineLength);
			m_slopeHist.accumulatePoint(std::abs(endPt2Val - endPt1Val)/lineLength);
		}

		/* Reusing some arrays:
		 * endPt1 will become the interior point
		 * endPt2 will become the increment
		 */
		scoped_array<Real>& interiorPt = endPt1;
		scoped_array<Real>& increment = endPt2;

		for (int j = 0; j < totalVars; ++j)
		{
			const int var = variablePresences[j]; // shorthand

			// note: increment and endPt2 are the same array
			increment[var] = (endPt2[var] - endPt1[var]) / (m_interiorLinePts + 2);
		}

		const Real valueFraction = (endPt2Val - endPt1Val) / (m_interiorLinePts + 2);

		// Interior point processing
		for (unsigned j = 1; j <= m_interiorLinePts; ++j)
		{
			for (int k = 0; k < totalVars; ++k)
			{
				const int var = variablePresences[k]; // shorthand

				interiorPt[var] += increment[var];
			}

			// Evaluate
			Real interiorPtVal;
			if (!(m_problem->*evalFuncVal)(func, interiorPt.get(), &interiorPtVal) || std::abs(interiorPtVal) >= m_inf)
			{
				++mathErrors;
				continue;
			}

			const Real interpVal = endPt1Val + j * valueFraction;
			const Real delta = interpVal - interiorPtVal; // FIXME: Catastrophic cancellation may occur from this calculation
			shape.accumulatePoint(delta);
			if (extraHists)
				m_shapeHist.accumulatePoint(delta);

			if (funcType == Objective)
			{
				updateExtremum(func, interiorPtVal, interiorPt.get());
			}
		}
	}

	// Conclude analysis
	if (funcType == Constraint)
	{
		if (mathErrors < (m_lineSegments * 2 + m_interiorLinePts * (m_lineSegments - lineErrors)) * m_allowedEvalErrors)
		{
			if (shape.dataPoints() >= m_minPointsNeeded)
			{
				m_shapes[func] = deduceFunctionShape(shape);
				m_regionEffects[func] = deduceRegionEffect(m_problem->constraintType(func), m_shapes[func]);
			}
			else
			{
				m_shapes[func] = ESShapeError;
			}

			if (effTotal >= m_minPointsNeeded)
			{
				setEffectiveness(func, effTotal, effLowerB, effUpperB);
			}
			else
			{
				m_effStatus[func] = InsufficientDataPoints;
			}
		}
		else
		{
			m_shapes[func] = ESTooManyMathErrors;
			m_regionEffects[func] = RETooManyMathErrors;
			m_effStatus[func] = TooManyErrors;
		}
	}
	else // Objective
	{
		if (mathErrors < (m_lineSegments * 2 + m_interiorLinePts * (m_lineSegments - lineErrors)) * m_allowedEvalErrors)
		{
			if (shape.dataPoints() >= m_minPointsNeeded)
			{
				const int offset = func + m_problem->constraints();
				m_shapes[offset] = deduceFunctionShape(shape);
				m_optimumEffects[func] = deduceOptimumEffect(m_problem->objectiveType(func), m_shapes[offset]);
			}
			else
			{
				m_shapes[func + m_problem->constraints()] = ESShapeError;
				m_optimumEffects[func] = OEError;
			}
		}
		else
		{
			m_shapes[func + m_problem->constraints()] = ESTooManyMathErrors;
			m_optimumEffects[func] = OETooManyMathErrors;
		}
	}
}

uint8_t Analysis::deduceFunctionShape(const Histogram& shapeHist)
{
	/*
	 * numBelowHistRange is the negative nonlinear count
	 * Bin 0 will be the negative almost equal bin
	 * Bin 1 the "linear count" bin
	 * Bin 2 the positive almost equal bin
	 * numAboveHistRange is the positive
	 */

	// If all but linear bin are empty and linear bin not empty, then it's linear
	if (!shapeHist.numBelowHistRange()
		&& !shapeHist.getBin(0)
		&& !shapeHist.getBin(2)
		&& !shapeHist.numAboveHistRange()
		&& shapeHist.getBin(1))
		return Linear;

	// If no nonlinear, then almost linear
	if (!shapeHist.numBelowHistRange() && !shapeHist.numAboveHistRange())
	{
		if (!shapeHist.getBin(0) && shapeHist.getBin(2)) // if only in the lin and positive almost lin bins
			return AlmostLinConvex; // then it is almost linear convex

		if (shapeHist.getBin(0) && !shapeHist.getBin(2)) // if only in the lin and negative almost lin bins
			return AlmostLinConcave; // then it is almost linear convex

		return AlmostLinBoth; // Otherwise it is both
	}

	// Convex
	if (!shapeHist.numBelowHistRange()
		&& !shapeHist.getBin(0)
		&& shapeHist.numAboveHistRange())
		return Convex;

	// Almost convex
	if (!shapeHist.numBelowHistRange()
		&& shapeHist.getBin(0)
		&& shapeHist.numAboveHistRange())
		return AlmostConvex;

	// Concave
	if (!shapeHist.getBin(2)
		&& !shapeHist.numAboveHistRange()
		&& shapeHist.numBelowHistRange())
		return Concave;

	// Almost Concave
	if (shapeHist.numBelowHistRange()
		&& shapeHist.getBin(2)
		&& !shapeHist.numAboveHistRange())
		return AlmostConcave;

	if (shapeHist.numAboveHistRange() && shapeHist.numBelowHistRange())
		return BothConc;

	return ESShapeError; // All the conditions cover all the cases.
}


uint8_t Analysis::deduceOptimumEffect(int objType, uint8_t shape)
{
	if (objType == Maximization)
	{
		switch (shape) {
			case Linear:
			case AlmostLinConcave:
			case Concave:
				return ObjectiveGlobal;
			case AlmostConcave:
			case AlmostLinBoth:
			case AlmostLinConvex:
				return ObjectiveAlmostGlobal;
			case AlmostConvex:
			case Convex:
			case BothConc:
				return ObjectiveLocal;
			default:
				return OEError;
		}
	}
	else
	{
		switch (shape) {
			case Linear:
			case AlmostLinConvex:
			case Convex:
				return ObjectiveGlobal;
			case AlmostConvex:
			case AlmostLinBoth:
			case AlmostLinConcave:
				return ObjectiveAlmostGlobal;
			case AlmostConcave:
			case Concave:
			case BothConc:
				return ObjectiveLocal;
			default:
				return OEError;
		}
	}
}

uint8_t Analysis::deduceRegionEffect(int constrType, uint8_t shape)
{
	switch (shape) {
		case Linear:
			return REConvex;
		case AlmostLinConvex:
			if (constrType == LEqual)
				return REConvex;
			else
				return REAlmost;
		case AlmostLinConcave:
			if (constrType == GEqual)
				return REConvex;
			else
				return REAlmost;
		case AlmostLinBoth:
			return REAlmost;
		case Convex:
			if (constrType == LEqual)
				return REConvex;
			else
				return RENonconvex;
		case AlmostConvex:
			if (constrType == LEqual)
				return REAlmost;
			else
				return RENonconvex;
		case Concave:
			if (constrType == GEqual)
				return REConvex;
			else
				return RENonconvex;
		case AlmostConcave:
			if (constrType == GEqual)
				return REAlmost;
			else
				return RENonconvex;
		case BothConc:
			return RENonconvex;
		default:
			return REError;
	}
}

uint8_t Analysis::getEmpiricalShape(int func, int funcType)
{
	if (funcType == Objective)
		return m_shapes[func+m_problem->constraints()];
	else
		return m_shapes[func];
}

uint8_t Analysis::getOptimumEffect(int objective)
{
	return m_optimumEffects[objective];
}

void Analysis::setEffectiveness(int constraint, uint64_t effTotal, uint64_t effLowerB, uint64_t effUpperB)
{
	m_effectiveness[2*constraint] = (Real)effLowerB/effTotal;
	m_effectiveness[2*constraint+1] = (Real)effUpperB/effTotal;
	m_effStatus[constraint] = Computed;
}

uint8_t Analysis::getRegionEffect(int constraint)
{
	return m_regionEffects[constraint];
}

