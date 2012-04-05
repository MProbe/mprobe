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


#include "mprobe.h"

#include <vector>
#include <string>
#include <iterator>
#include <sstream>

#include <Poco/SharedPtr.h>

#include "pluginmanager.h"
#include "probleminstance.h"
#include "probleminstancefactory.h"
#include "readerplugin.h"
#include "analysis.h"

using Poco::SharedPtr;

static PluginManager* g_pluginManager;

void mprobe_init()
{
	if (!g_pluginManager)
		g_pluginManager = new PluginManager();

	PluginRegistration reg;
	reg.isCompatiblePlugin = ReaderPlugin::isCompatiblePlugin;
	reg.load = ReaderPlugin::load;
	reg.typeStr = ReaderPlugin::PluginTypeStr;

	g_pluginManager->registerPlugin(reg);
}

void mprobe_deinit(void)
{
	g_pluginManager->unloadAllPlugins();

	if (g_pluginManager)
		delete g_pluginManager;
	g_pluginManager = 0;
}

template <class T>
static inline bool convertSharedHandle(MPHandle h, SharedPtr<T>*& href)
{
#ifndef NOCHECKING
	if (h)
#endif
		return (href = (SharedPtr<T>*)h);
#ifndef NOCHECKING
	else
	{
		// TODO: flag error
		return (href = 0);
	}
#endif
}

static inline bool checkObjBounds(ProblemInstance* h, int o)
{
#ifndef NOCHECKING
	if (o >= 0 && o < h->objectives())
	{
		return true;
	}
	else
	{
		std::ostringstream ss;
		ss << "Objective index (" << o << ") is out of bounds [" << 0 << "," << h->objectives() <<").";
		h->setLatestError(IndexOutOfBounds, ss.str());
		return false;
	}
#else
	return true;
#endif
}

static inline bool checkConBounds(ProblemInstance* h, int c)
{
#ifndef NOCHECKING
	if (c >= 0 && c < h->constraints())
	{
		return true;
	}
	else
	{
		std::ostringstream ss;
		ss << "Constraint index (" << c << ") is out of bounds [" << 0 << "," << h->constraints() <<").";
		h->setLatestError(IndexOutOfBounds, ss.str());
		return false;
	}
#else
	return true;
#endif
}

bool checkPtr(ProblemInstance* h, void* ptr)
{
#ifndef NOCHECKING
	if (!ptr)
	{
		h->setLatestError(NullArgument, "Null pointer.");
		return false;
	}
#endif
	return true;
}

void mp_loadPlugins(const char* path)
{
	g_pluginManager->loadPlugins(std::string(path));
}

void mp_unloadPlugins(const char* path)
{
	g_pluginManager->unloadPlugins(std::string(path));
}

int mp_loadedPlugins()
{
	return g_pluginManager->numPlugins();
}

int mp_pluginCanRead(const char* plugin)
{
	SharedPtr<IPlugin> p = g_pluginManager->getPluginByName(plugin);
	if (p)
	{
		ReaderPlugin* rp = p.cast<ReaderPlugin>();
		if (rp)
			return 1;
	}
	return 0;
}

const char* mp_pluginName(int plugin)
{
	if (plugin < mp_loadedPlugins())
	{
		SharedPtr<IPlugin> p = g_pluginManager->getPlugin(plugin);
		const std::string& name = p->name();
		if (name.size())
		{
			char* nameCStr = new char[name.size()+1];
			std::copy(name.begin(), name.end(), nameCStr);
			nameCStr[name.size()] = '0';
			return nameCStr;
		}
	}
	return 0;
}

void mp_releasePluginName(const char* name)
{
	delete [] name;
}

MPHandle mp_load(int numFiles, const char** files)
{
	ProblemInstance* pi = ProblemInstanceFactory(g_pluginManager).fromFiles(numFiles, files);
	if (pi)
		return new SharedPtr<ProblemInstance>(pi);
	else
		return pi;
}

MPHandle mp_loadUsingPlugin(int numFiles, const char** files, const char* plugin)
{
	ProblemInstance* pi = ProblemInstanceFactory(g_pluginManager).fromFilesAndPlugin(numFiles, files, plugin);
	if (pi)
		return new SharedPtr<ProblemInstance>(pi);
	else
		return pi;
}

void mp_unload(MPHandle h)
{
	SharedPtr<ProblemInstance>* ppi;
	if (convertSharedHandle(h, ppi))
		delete  ppi;
}

int mp_compatibleFiles(int numFiles, const char** files)
{
	return ProblemInstanceFactory(g_pluginManager).compatibleFiles(numFiles, files);
}

int mp_compatibleFilesFor(int plugin, int numFiles, const char** files)
{
	return ProblemInstanceFactory(g_pluginManager).compatibleFiles(plugin, numFiles, files);
}

static inline bool checkVarBounds(ProblemInstance* h, int v)
{
#ifndef NOCHECKING
	if (v >= 0 && v < h->variables())
	{
		return true;
	}
	else
	{
		std::ostringstream ss;
		ss << "Variable index (" << v << ") is out of bounds [" << 0 << "," << h->variables() <<").";
		h->setLatestError(IndexOutOfBounds, ss.str());
		return false;
	}
#else
	return true;
#endif
}

void mp_releaseName(MPHandle h, const char* n)
{
	SharedPtr<ProblemInstance>* ppi;
	if (convertSharedHandle(h, ppi))
		return (*ppi)->releaseName(n);
}

const char* mp_instanceName(MPHandle h)
{
	SharedPtr<ProblemInstance>* ppi;
	if (convertSharedHandle(h, ppi))
		return (*ppi)->instanceName();
	return 0;
}

int mp_instanceNameToBuf(MPHandle h, int m, char* b)
{
	SharedPtr<ProblemInstance>* ppi;
	if (convertSharedHandle(h, ppi))
		return (*ppi)->instanceNameToBuf(m, b);
	return 0;
}

int mp_variables(MPHandle h)
{
	SharedPtr<ProblemInstance>* ppi;
	if (convertSharedHandle(h, ppi))
		return (*ppi)->variables();
	return 0;
}

const char* mp_variableName(MPHandle h, int v)
{
	SharedPtr<ProblemInstance>* ppi;
	if (!convertSharedHandle(h, ppi))
		return 0;
	ProblemInstance* pi = *ppi;
	if (checkVarBounds(pi, v))
		return pi->variableName(v);
	else
		return 0;
}

int mp_variableNameToBuf(MPHandle h, int v, int m, char* b)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;

	if (checkVarBounds(pi, v))
		return pi->variableNameToBuf(v, m ,b);
	else
		return 0;
}

void mp_variablePresence(MPHandle h, int v, int* cs, int* os)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return;

	ProblemInstance* pi = *ppi;

	if (checkVarBounds(pi, v) && checkPtr(pi, cs) && checkPtr(pi, os))
		return pi->variablePresence(v, cs, os);
}

int mp_variableType(MPHandle h, int v)
{
	SharedPtr<ProblemInstance>* ppi;
	if (!convertSharedHandle(h, ppi))
		return 0;
	ProblemInstance* pi = *ppi;
	if (checkVarBounds(pi, v))
		return pi->variableType(v);
	return 0;
}

void mp_variableBounds(MPHandle h, int v, Real* lb, Real* ub)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return;

	ProblemInstance* pi = *ppi;

	if (checkVarBounds(pi, v))
		return pi->variableBounds(v, lb, ub);
}

int mp_objectives(MPHandle h)
{
	SharedPtr<ProblemInstance>* ppi;
	if (!convertSharedHandle(h, ppi))
		return 0;
	ProblemInstance* pi = *ppi;
	return pi->objectives();
}

const char* mp_objectiveName(MPHandle h, int o)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, o))
		return pi->objectiveName(o);
	return 0;
}

int mp_objectiveNameToBuf(MPHandle h, int o, int m, char* b)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, o))
		return pi->objectiveNameToBuf(o, m, b);
	return 0;
}

int mp_objectiveType(MPHandle h, int o)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, o))
		return pi->objectiveType(o);
	return 0;
}

int mp_objectiveVariables(MPHandle h, int o, int* vs)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return -1;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, o) && checkPtr(pi, vs))
		return pi->objectiveVariables(o, vs);
	return -1;
}

int mp_evaluateObjectiveVal(MPHandle h, int o, Real* point, Real* result)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 1;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, o) && checkPtr(pi, point) && checkPtr(pi, result))
		return pi->evaluateObjectiveVal(o, point, result);
	return 1;
}

int mp_evaluateObjectiveGrad(MPHandle h, int o, Real* point, Real* result)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 1;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, o) && checkPtr(pi, point) && checkPtr(pi, result))
		return pi->evaluateObjectiveGrad(o, point, result);
	return 1;
}

int mp_constraints(MPHandle h)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
		return pi->constraints();
}

const char* mp_constraintName(MPHandle h, int c)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
	if (checkConBounds(pi, c))
		return pi->constraintName(c);
	return 0;
}

int mp_constraintNameToBuf(MPHandle h, int c, int m, char* b)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
	if (checkConBounds(pi, c))
		return pi->constraintNameToBuf(c, m, b);
	return 0;
}

int mp_constraintType(MPHandle h, int c)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;
	if (checkConBounds(pi, c))
		return pi->constraintType(c);
	return 0;
}

void mp_constraintBounds(MPHandle h, int c, Real* lb, Real* ub)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return;

	ProblemInstance* pi = *ppi;
	if (checkConBounds(pi, c))
		return pi->constraintBounds(c, lb, ub);
}

int mp_constraintVariables(MPHandle h, int c, int* vs)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return -1;

	ProblemInstance* pi = *ppi;
	if (checkConBounds(pi, c) && checkPtr(pi, vs))
		return pi->constraintVariables(c, vs);
	return -1;
}

int mp_evaluateConstraintVal(MPHandle h, int c, Real* point, Real* result)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 1;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, c) && checkPtr(pi, point) && checkPtr(pi, result))
		return pi->evaluateConstraintVal(c, point, result);
	return 1;
}

int mp_evaluateConstraintGrad(MPHandle h, int c, Real* point, Real* result)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 1;

	ProblemInstance* pi = *ppi;
	if (checkObjBounds(pi, c) && checkPtr(pi, point) && checkPtr(pi, result))
		return pi->evaluateConstraintGrad(c, point, result);
	return 1;
}

int mp_functionType(MPHandle h, int t, int w)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;

	if ((t == 'o' && checkObjBounds(pi, w))
		||  (t == 'c' && checkConBounds(pi, w)))
		return pi->functionType(t, w);

	return 0;
}

int mp_GetLastError(MPHandle h, const char** err)
{
	SharedPtr<ProblemInstance>* ppi;
	std::string errStr;
	int type;

	if (!convertSharedHandle(h, ppi))
		return 0;

	ProblemInstance* pi = *ppi;

	type = pi->getLatestError(errStr);
	if (err)
	{
		if (type && errStr.size())
		{
			char* str = new char[errStr.size()+1];
			int copied = errStr.copy(str, errStr.size());
			str[copied] = '0';
			*err = str;
		}
		else
		{
			*err = 0;
		}
	}
	return type;
}

void mp_ReleaseErrorStr(MPHandle, const char* str)
{
	delete [] str;
}


MPHandle mp_createAnalysis(MPHandle h)
{
	SharedPtr<ProblemInstance>* ppi;

	if (!convertSharedHandle(h, ppi))
		return 0;

	return new Analysis(*ppi);
}

void mp_releaseAnalysis(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	delete a;
}

void mp_aVariableBoundLineSample(MPHandle h, int funcType, int func, int extraHists)
{
	Analysis* a = (Analysis*)h;
	a->variableBoundLineSample(funcType, func, extraHists);
}

void mp_aBounds(MPHandle h, int var, Real* lower, Real* upper)
{
	Analysis* a = (Analysis*)h;
	return a->bounds(var, *lower, *upper);
}

void mp_aResetBounds(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->resetBounds();
}

void mp_aClampBounds(MPHandle h, Real maxMagnitude)
{
	Analysis* a = (Analysis*)h;
	a->clampBounds(maxMagnitude);
}

void mp_aSetBounds(MPHandle h, int var, Real lower, Real upper)
{
	Analysis* a = (Analysis*)h;
	a->setBounds(var, lower, upper);
}

Real mp_aUpperBound(MPHandle h, int var)
{
	const Analysis* a = (Analysis*)h;
	return a->upperBound(var);
}

void mp_aSetUpperBound(MPHandle h, int var, Real upper)
{
	Analysis* a = (Analysis*)h;
	a->setUpperBound(var, upper);
}

Real mp_aLowerBound(MPHandle h, int var)
{
	const Analysis* a = (Analysis*)h;
	return a->lowerBound(var);
}

void mp_aSetLowerBound(MPHandle h, int var, Real upper)
{
	Analysis* a = (Analysis*)h;
	a->setLowerBound(var, upper);
}

Real mp_aLineLengthMax(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->lineLengthMax();
}

Real mp_aLineLengthMin(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->lineLengthMin();
}

void mp_aLineLengthBounds(MPHandle h, Real* min, Real* max)
{
	Analysis* a = (Analysis*)h;
	a->lineLengthBounds(*min, *max);
}

void mp_aSetLineLengthBounds(MPHandle h, Real min, Real max)
{
	Analysis* a = (Analysis*)h;
	a->setLineLengthBounds(min, max);
}

int mp_aSnapDiscreteComponents(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->snapDiscreteComponents();
}

void mp_aSetSnapDiscreteComponents(MPHandle h, int snap)
{
	Analysis* a = (Analysis*)h;
	a->setSnapDiscreteComponents(snap);
}

unsigned mp_aNumLineSegments(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->numLineSegments();
}

void mp_aSetNumLineSegments(MPHandle h, unsigned num)
{
	Analysis* a = (Analysis*)h;
	a->setNumLineSegments(num);
}

unsigned mp_aInteriorLinePoints(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->interiorLinePoints();
}

void mp_aSetNumInteriorLinePoints(MPHandle h, unsigned points)
{
	Analysis* a = (Analysis*)h;
	a->setNumInteriorLinePoints(points);
}

unsigned mp_aMinimumPointsNeeded(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->minimumPointsNeeded();
}

void mp_aSetMinimumPointsNeeded(MPHandle h, unsigned points)
{
	Analysis* a = (Analysis*)h;
	a->setMinimumPointsNeeded(points);
}

Real mp_aEvalErrorTolerance(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->evalErrorTolerance();
}

int mp_aSetEvalErrorTolerance(MPHandle h, Real tol)
{
	Analysis* a = (Analysis*)h;
	return a->setEvalErrorTolerance(tol);
}

Real mp_aInfinity(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->infinity();
}

void mp_aSetInfinity(MPHandle h, Real inf)
{
	Analysis* a = (Analysis*)h;
	a->setInfinity(inf);
}

Real mp_aEqualityTolerance(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->equalityTolerance();
}

int mp_aSetEqualityTolerance(MPHandle h, Real eqtol)
{
	Analysis* a = (Analysis*)h;
	return a->setEqualityTolerance(eqtol);
}

Real mp_aAlmostEqualTolerance(MPHandle h)
{
	Analysis* a = (Analysis*)h;
	return a->almostEqualTolerance();
}

int mp_aSetAlmostEqualTolerance(MPHandle h, Real alEqTol)
{
	Analysis* a = (Analysis*)h;
	return a->setAlmostEqualTolerance(alEqTol);
}

uint8_t mp_aEffectiveness(MPHandle h, int constraint, Real* efflb, Real* effub)
{
	Analysis* a = (Analysis*)h;
	return a->effectiveness(constraint, *efflb, *effub);
}

uint8_t mp_aExtremum(MPHandle h, int objective, Real* extremeVal)
{
	Analysis* a = (Analysis*)h;
	return a->extremum(objective, *extremeVal);
}

uint8_t mp_aExtremumPoint(MPHandle h, int objective, Real* point)
{
	Analysis* a = (Analysis*)h;
	return a->extremumPoint(objective, point);
}


uint64_t mp_aGetHistogramBin(MPHandle h, int histType, int bin)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.getBin(bin);
}

unsigned mp_aGetHistogramNumBins(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.numBins();
}

uint64_t mp_aGetHistogramNumOutside(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.numOutsideHistogram();
}

uint64_t mp_aGetHistogramNumAboveRange(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.numAboveHistRange();
}

uint64_t mp_aGetHistogramNumBelowRange(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.numBelowHistRange();
}

uint64_t mp_aGetHistogramDataPoints(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.dataPoints();
}

Real mp_aGetHistogramMean(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.mean();
}

Real mp_aGetHistogramStdDev(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.stddev();
}

Real mp_aGetHistogramVariance(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.variance();
}

Real mp_aGetHistogramPopVariance(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.populationVariance();
}

Real mp_aGetHistogramMaximum(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.maximumPoint();
}

Real mp_aGetHistogramMinimum(MPHandle h, int histType)
{
	Analysis* a = (Analysis*)h;
	const Histogram& hist = a->getExtraHistogram((Analysis::ExtraHistType)histType);
	return hist.minimumPoint();
}

void mp_aSetHistogramBins(MPHandle h, int which, int bins, Real firstBinWidth, Real* upperBounds)
{
	Analysis* a = (Analysis*)h;
	Real* tmp = new Real[bins];
	std::copy(upperBounds, &upperBounds[bins], tmp);
	Histogram tmpHist;
	tmpHist.setBins(bins, firstBinWidth, tmp);
	a->setExtraHistogram((Analysis::ExtraHistType)which, tmpHist);
	delete[] tmp;
}

uint8_t mp_aGetEmpiricalShape(MPHandle h, int func, int funcType)
{
	Analysis* a = (Analysis*)h;
	return a->getEmpiricalShape(func, funcType);
}

uint8_t mp_aGetOptimumEffect(MPHandle h, int objective)
{
	Analysis* a = (Analysis*)h;
	return a->getOptimumEffect(objective);
}

uint8_t mp_aGetRegionEffect(MPHandle h, int constraint)
{
	Analysis* a = (Analysis*)h;
	return a->getRegionEffect(constraint);
}
