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


#ifndef HISTOGRAM_H
#define HISTOGRAM_H

#include <vector>
#include <stdint.h>

#include "types.h"

/* NOTE: This class assumes ieee floating point rules are following w.r.t. INFs.
 */

class Histogram
{
public:
	Histogram();

	void setBins(unsigned num,  Real firstBinWidth, Real* upperBounds);
	void reset();

	void accumulatePoint(Real pt);

	uint64_t getBin(int bin) const;
	unsigned numBins() const;
	uint64_t numOutsideHistogram() const;
	uint64_t numBelowHistRange() const;
	uint64_t numAboveHistRange() const;

	// Additional statistics
	uint64_t dataPoints() const;
	Real mean() const;
	Real stddev() const;
	Real variance() const;
	Real populationVariance() const; // Maximum likelyhood estimator
	Real maximumPoint() const;
	Real minimumPoint() const;
private:
	std::vector<Real> upperBounds;
	std::vector<uint64_t> bins;

	// Data for mean, variance, stddev calculations using Welford algorithm
	uint64_t points;
	Real m;
	Real sq;
	Real sq_comp; // Extra variable for Kahan compensation on sq

	Real min, max;
};

#endif // HISTOGRAM_H
