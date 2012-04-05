/*
    <one line to give the library's name and an idea of what it does.>
    Copyright (C) 2011  <copyright holder> <email> <email>

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


#include "histogram.h"

#include <algorithm>
#include <limits>
#include <functional>
#include <cmath>

Histogram::Histogram()
	: points(0), m(0.0), sq(0.0), sq_comp(0.0),
	min(std::numeric_limits<Real>::infinity()),
	max(-std::numeric_limits<Real>::infinity())
{
}

void Histogram::setBins(unsigned num, Real firstBinWidth, Real* uBs)
{
	std::sort(uBs, &uBs[num]);
	upperBounds.clear();
	upperBounds.push_back(uBs[0]-firstBinWidth);
	upperBounds.insert(upperBounds.end(), uBs, &uBs[num]);
	upperBounds.push_back(std::numeric_limits<Real>::infinity());
	reset();
}

void Histogram::reset()
{
	bins.assign(upperBounds.size(), 0);
	points = 0;
	m = 0.0;
	sq = 0.0;
	sq_comp = 0.0;
	min = std::numeric_limits<Real>::infinity();
	max = -std::numeric_limits<Real>::infinity();
}

void Histogram::accumulatePoint(Real pt)
{
	std::vector<Real>::iterator it = std::upper_bound(upperBounds.begin(), upperBounds.end(), pt);

	++(bins[std::distance(upperBounds.begin(), it)]);
	++points;

	// Welford algorithm using Kahan compensated summation for sq.
	double newMean = m + (pt - m) / points;
	Real compAdd = (pt - m) * (pt - newMean) - sq_comp;
	Real newSq = sq + compAdd;
	sq_comp = (newSq - sq) - compAdd;
	sq = newSq;
	m = newMean;

	if (pt > max)
		max = pt;
	if (pt < min)
		min = pt;
}

uint64_t Histogram::getBin(int bin) const
{
	return bins[bin+1];
}


unsigned Histogram::numBins() const
{
	return upperBounds.size() - 2;
}


uint64_t Histogram::numOutsideHistogram() const
{
	return numBelowHistRange()+numAboveHistRange();
}

uint64_t Histogram::numBelowHistRange() const
{
	return bins.front();
}

uint64_t Histogram::numAboveHistRange() const
{
	return bins.back();
}

uint64_t Histogram::dataPoints() const
{
	return points;
}

Real Histogram::mean() const
{
	return m;
}

Real Histogram::stddev() const
{
	return std::sqrt(variance());
}

Real Histogram::variance() const
{
	if (points > 0)
		return sq / (points - 1.0);
	return std::numeric_limits<Real>::quiet_NaN();
}

Real Histogram::populationVariance() const
{
	if (points > 0)
		return sq / points;
	return std::numeric_limits<Real>::quiet_NaN();
}

Real Histogram::maximumPoint() const
{
	return max;
}

Real Histogram::minimumPoint() const
{
	return min;
}
