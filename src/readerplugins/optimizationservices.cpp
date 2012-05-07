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


#define RP_NOPREFIX
#include "readerplugininterface.h"

#include <limits>
#include <algorithm>
#include <cstring>
#include <string>
#include <iostream>

#include <cassert>

#include <Poco/Path.h>
#include <Poco/FileStreamFactory.h>

#include <coin/OSInstance.h>
#include <coin/OSErrorClass.h>
#include <coin/OSiLReader.h>
#include <coin/OSnl2osil.h>
#include <coin/OSmps2osil.h>

ReaderPluginDetails details = { "Optimisation Services", "", RP_NON_REENTRANT, 0};

class RPOSInstance
{
public:
	RPOSInstance() : i(0),r(0),mps(0),nl(0) {}
	~RPOSInstance() {
		if (r)
			delete r;
		else if (nl)
			delete nl;
		else if (mps)
			delete mps;
		i = 0;
		r = 0;
		mps = 0;
		nl = 0;
	}

	OSInstance* i;

	OSiLReader* r;
	OSmps2osil* mps;
	OSnl2osil* nl;

	void computePresences(); // Must be called once the OSInstance has been set!
	const std::vector<int>& getVarCP(int var) { return varCP[var]; }
	const std::vector<int>& getVarOP(int var) { return varOP[var]; }
	const std::vector<int>& getConstrP(int constr) { return constrP[constr]; }
	const std::vector<int>& getObjP(int obj) { return objP[obj]; }
private:
	void loopOverLCCols(int col, int curStart, int nextStart, std::vector<std::set<int> >&, std::vector<std::set<int> >&);
	void loopOverLCRows(int row, int curStart, int nextStart, std::vector<std::set<int> >&, std::vector<std::set<int> >&);
	std::vector<std::vector<int> > varCP, varOP, constrP, objP;
};

enum FileType {
	OSIL=1,
	MPS,
	NL
};


void getReaderPluginAPIVersion(int* major, int* minor)
{
	*major = READER_PLUGIN_API_VERSION_MAJOR;
	*minor = READER_PLUGIN_API_VERSION_MINOR;
}

ReaderPluginDetails* getReaderPluginDetails(void)
{
	return &details;
}

bool extCompare(const std::string& ext1, const char* ext2)
{
	std::string::const_iterator ext1it = ext1.begin();
	const char* ext2it = ext2;
	while (*ext2it && ext1it != ext1.end())
	{
		if (std::tolower(*ext1it) != std::tolower(*ext2it))
			return false;
		++ext2it;
		++ext1it;
	}
	if ((*ext2it && ext1it == ext1.end()) || (ext1it != ext1.end() && !*ext2it))
		return false;
	return true;
}

RPHandle createInstance(int numFiles, const char** files)
{
	using namespace Poco;
	RPOSInstance* instance = RPNULL;
	std::string ext;
	unsigned type = 0;
	int i;

	for (i = 0; i < numFiles; ++i)
	{
		ext = Path(files[i]).getExtension();
		if (extCompare(ext,"osil"))
		{
			type = OSIL;
			break;
		}
		else if (extCompare(ext,"nl"))
		{
			type = NL;
			break;
		}
		else if (extCompare(ext,"mps"))
		{
			type = MPS;
			break;
		}
	}

	if (type != 0)
		instance = new RPOSInstance;

	if (type == OSIL)
	{
		std::string osil;
		std::istream* input = 0;
		try {
			input = FileStreamFactory().open(Path(files[i]));
		} catch (...) {
			delete instance;
			return RPNULL;
		}

		input->seekg(0, std::ios::end);
		osil.reserve(input->tellg());
		input->seekg(0, std::ios::beg);
		osil.assign((std::istreambuf_iterator<char>(*input)),
					std::istreambuf_iterator<char>());
		delete input;
		instance->r = new OSiLReader;
		instance->i = instance->r->readOSiL(osil);
	}
	else if (type == NL)
	{
		instance->nl = new OSnl2osil(Path(files[i]).toString(Path::PATH_NATIVE));
		if (!instance->nl->createOSInstance())
		{
			delete instance->nl;
			delete instance;
			return RPNULL;
		}
		instance->i = instance->nl->osinstance;
	}
	else if (type == MPS)
	{
		instance->mps = new OSmps2osil(Path(files[i]).toString(Path::PATH_NATIVE));
		if (!instance->mps->createOSInstance())
		{
			delete instance->mps;
			delete instance;
			return RPNULL;
		}
		instance->i = instance->mps->osinstance;
	}
	instance->computePresences();
	return instance;
}

int compatibleFiles(int numFiles, const char** files, int* compat)
{
	int i;
	bool foundOne = false;

	for (i = 0; i < numFiles; i++)
	{
		std::string ext = Poco::Path(files[i]).getExtension();
		if (extCompare(ext,"osil") || extCompare(ext,"nl") || extCompare(ext,"mps"))
		{
			foundOne = true;
			if (compat)
				compat[i] = 0;
			break;
		}
		else if (compat) // else unrecognized
		{
			compat[i] = 1;
		}
	}

	if (compat) // Mark remainder as unrecognized
		for (++i; i < numFiles; i++)
			compat[i] = 1;
	return foundOne? numFiles-1:-1;
}

void releaseInstance(RPHandle h)
{
	delete (RPOSInstance*)h;
}

void releaseName(RPHandle, const char* n)
{
	delete[] n;
}

const char* instanceName(RPHandle h)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string name = osil.getInstanceName();
	char* n = new char[name.size()+1];
	name.copy(n, name.size());
	n[name.size()] = '\0';
	return n;
}

int instanceNameToBuf(RPHandle h, int maxLen, char* buf)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string name = osil.getInstanceName();
	if (!buf || maxLen <= 0)
		return osil.getInstanceName().size()+1;

	int len = name.copy(buf, maxLen-1);
	buf[len] = '\0';
	return len+1;
}

int variables(RPHandle h)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	return osil.getVariableNumber();
}

const char* variableName(RPHandle h, int v)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* names = osil.getVariableNames();
	if (names)
	{
		char* n = new char[names[v].size()+1];
		names[v].copy(n, names[v].size());
		n[names[v].size()] = '\0';
		return n;
	}
	else
	{
		return 0;
	}
}

int variableNameToBuf(RPHandle h, int v, int maxLen, char* buf)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* names = osil.getVariableNames();
	if (!buf || maxLen <= 0)
	{
		if (names)
			return names[v].size()+1;
		else
			return 1;
	}

	if (names)
	{
		int len = names[v].copy(buf, maxLen-1);
		buf[len] = '\0';
		return len+1;
	}
	else
	{
		buf[0] = '\0';
		return 1;
	}
}

void variablePresence(RPHandle h, int var, int* constr, int* objs)
{
	RPOSInstance* inst = ((RPOSInstance*)h);

	if (!constr || !objs)
		return;

	const std::vector<int>& varcp = inst->getVarCP(var);
    const std::vector<int>& varop = inst->getVarOP(var);

	*constr++ = varcp.size();
	std::copy(varcp.begin(), varcp.end(), constr);

	*objs++ = varop.size();
	std::copy(varop.begin(), varop.end(), objs);
}

int variableType(RPHandle h, int v)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	char* types = osil.getVariableTypes();
	if (types)
	{
		switch (std::tolower(types[v]))
		{
			case 'c':
				return 'r';
				break;
			case 'b':
				return 'b';
				break;
			case 'i':
				return 'i';
				break;
			default:
				return 0;
		}
	}
	return 0;
}

void variableBounds(RPHandle h, int v, Real* lb, Real* ub)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	double* lbs = osil.getVariableLowerBounds();
	double* ubs = osil.getVariableUpperBounds();
	if (lbs && ubs)
	{
		*lb = lbs[v];
		*ub = ubs[v];
	}
}

int objectives(RPHandle h)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	return osil.getObjectiveNumber();
}

const char* objectiveName(RPHandle h, int o)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* names = osil.getObjectiveNames();
	if (names)
	{
		char* n = new char[names[o].size()+1];
		names[o].copy(n, names[o].size());
		n[names[o].size()] = '\0';
		return n;
	}
	else
	{
		return 0;
	}
}

int objectiveNameToBuf(RPHandle h, int o, int maxLen, char* buf)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* names = osil.getObjectiveNames();
	if (!buf || maxLen <= 0)
	{
		if (names)
			return names[o].size()+1;
		else
			return 1;
	}

	if (names)
	{
		int len = names[o].copy(buf, maxLen-1);
		buf[len] = '\0';
		return len+1;
	}
	else
	{
		buf[0] = '\0';
		return 1;
	}
}

int objectiveType(RPHandle h, int o)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* minMax = osil.getObjectiveMaxOrMins();
	if (minMax)
	{
		if (minMax[o] == "min")
			return 'm';
		else
			return 'M';
	}
	return 0;
}

int objectiveVariables(RPHandle h, int obj, int* vars)
{
	RPOSInstance* inst = ((RPOSInstance*)h);

	if (!vars)
		return -1;

	const std::vector<int>& objp = inst->getObjP(obj);

	std::copy(objp.begin(), objp.end(), vars);
	return objp.size();
}

int evaluateObjectiveVal(RPHandle h, int obj, Real* pt, Real* result)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	try {
		*result = osil.calculateFunctionValue(-1-obj, pt, true);
	} catch (...) {
		return 0;
	}
	return 1;
}

int evaluateObjectiveGrad(RPHandle h, int obj, Real* pt, Real* result)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	double* tmp;
	try {
		tmp = osil.calculateObjectiveFunctionGradient(pt, -1-obj, true);
	} catch (...) {
		return 0;
	}
	std::copy(tmp, &tmp[osil.getVariableNumber()], result);
	return 1;
}

int constraints(RPHandle h)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	return osil.getConstraintNumber();
}

const char* constraintName(RPHandle h, int c)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* names = osil.getConstraintNames();
	if (names)
	{
		char* n = new char[names[c].size()+1];
		names[c].copy(n, names[c].size());
		n[names[c].size()] = '\0';
		return n;
	}
	else
	{
		return 0;
	}
}

int constraintNameToBuf(RPHandle h, int c, int maxLen, char* buf)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	std::string* names = osil.getObjectiveNames();
	if (!buf || maxLen <= 0)
	{
		if (names)
			return names[c].size()+1;
		else
			return 1;
	}

	if (names)
	{
		int len = names[c].copy(buf, maxLen-1);
		buf[len] = '\0';
		return len+1;
	}
	else
	{
		buf[0] = '\0';
		return 1;
	}
}

int constraintType(RPHandle h, int c)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	char* type = osil.getConstraintTypes();
	if (type)
		return std::tolower(type[c]);
	return 0;
}

void constraintBounds(RPHandle h, int c, Real* lb, Real* ub)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	double* lbs = osil.getConstraintLowerBounds();
	double* ubs = osil.getConstraintUpperBounds();
	if (lbs && ubs)
	{
		*lb = lbs[c];
		*ub = ubs[c];
	}
}

int constraintVariables(RPHandle h, int constr, int* vars)
{
	RPOSInstance* inst = ((RPOSInstance*)h);

	if (!vars)
		return -1;

	const std::vector<int>& constrp = inst->getConstrP(constr);

	std::copy(constrp.begin(), constrp.end(), vars);
	return constrp.size();
}

int evaluateConstraintVal(RPHandle h, int constr, Real* pt, Real* result)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	try {
		*result = osil.calculateFunctionValue(constr, pt, true);
	} catch (...) {
		return 0;
	}
	return 1;
}

int evaluateConstraintGrad(RPHandle h, int constr, Real* pt, Real* result)
{
	OSInstance& osil = *((RPOSInstance*)h)->i;
	SparseVector* tmp;
	try {
		tmp = osil.calculateConstraintFunctionGradient(pt, constr, true);
	} catch (...) {
		return 0;
	}
	std::fill(result, &result[osil.getVariableNumber()], 0.0);
	if (tmp)
	{
		for (int i = 0; i < tmp->number; ++i)
		{
			result[tmp->indexes[i]] = tmp->values[i];
		}
	}
	return 1;
}

int functionType(RPHandle h, int type, int which)
{
	InstanceData* idata = ((RPOSInstance*)h)->i->instanceData;
	NonlinearExpressions *nexprs = idata->nonlinearExpressions;
	QuadraticCoefficients *qcoeffs = idata->quadraticCoefficients;
	int osilFunctionindex = (type == 'o')? -(which+1) : which;

    if (nexprs)
    {
        for (int i = 0; i < nexprs->numberOfNonlinearExpressions; ++i)
        {
            if (osilFunctionindex == nexprs->nl[i]->idx)
                return 'n';
        }
    }

    if (qcoeffs)
    {
        for (int i = 0; i < qcoeffs->numberOfQuadraticTerms; ++i)
        {
            if (osilFunctionindex == qcoeffs->qTerm[i]->idx)
                return 'q';
        }
    }

	return 'l'; // Assuming !nonlinear && !quadratic => linear
}

void RPOSInstance::loopOverLCCols(int col, int curStart, int nextStart, std::vector<std::set<int> >& constrp, std::vector<std::set<int> >& varcp)
{
	LinearConstraintCoefficients *lccoeffs = i->instanceData->linearConstraintCoefficients;

	assert(curStart <= nextStart); // "Model or specification understanding is in error."
	for (int i = curStart; i < nextStart; ++i)
	{
		constrp[lccoeffs->rowIdx->getEl(i)].insert(col);
		varcp[col].insert(lccoeffs->rowIdx->getEl(i));
	}
}

void RPOSInstance::loopOverLCRows(int row, int curStart, int nextStart, std::vector<std::set<int> >& constrp, std::vector<std::set<int> >& varcp)
{
	LinearConstraintCoefficients *lccoeffs = i->instanceData->linearConstraintCoefficients;

	assert(curStart <= nextStart); // "Model or specification understanding is in error."
	for (int i = curStart; i < nextStart; ++i)
	{
		constrp[row].insert(lccoeffs->colIdx->getEl(i));
		varcp[lccoeffs->colIdx->getEl(i)].insert(row);
	}
}

void RPOSInstance::computePresences()
{
	if (!i->instanceData)
		return;

	InstanceData* idata = i->instanceData;

	// We use these to remove duplicates while constructing the data, then transfer the it
	std::vector<std::set<int> > objP_tmp, constrP_tmp, varCP_tmp, varOP_tmp;

	objP_tmp.assign(i->getObjectiveNumber(), std::set<int>());
	constrP_tmp.assign(i->getConstraintNumber(), std::set<int>());
	varCP_tmp.assign(i->getVariableNumber(), std::set<int>());
	varOP_tmp.assign(i->getVariableNumber(), std::set<int>());

	try {
		LinearConstraintCoefficients *lccoeffs = idata->linearConstraintCoefficients;
		if (lccoeffs)
		{
			if (i->getLinearConstraintCoefficientMajor())
			{
				/*
				* I found plenty of information about common cases, but very little about corner cases.
				* So here is my logic to work through them:
				*  OSiL objects typically have numberOfVariables + 1 start elements, but only numberOfVariables are really needed.
				*   so in a corner case where only numberOfVariables start elements are present, we could use numberOfValues - start[n] to
				*   determine number of elements for that variable.
				*  When a variable is not present as a linear term in a constraint, assume start[n] - start[n+1] is the number of
				*   contraints it is present as a linear term. See above case for when there are only numberOfVariables start elements.
				*  The previous assumption implies that  0 <= start[n] <= start[n+1]. (i.e. they're sorted)
				* This part can be greatly simplified if documentation is found which states how the start elements are guaranteed to relate
				*  to the number of columns/rows
				*/
				assert(lccoeffs->iNumberOfStartElements >= idata->variables->numberOfVariables);
				//"Less linear constraint coefficient start elements than expected,"
				int terminalStartVal;
				if (lccoeffs->iNumberOfStartElements > idata->variables->numberOfVariables)
				{
					terminalStartVal = lccoeffs->start->getEl(idata->variables->numberOfVariables);
				}
				else
				{
					terminalStartVal = lccoeffs->numberOfValues;
				}

				// Deal with last column as special case, TODO: is this necessary? see assumption above
				for (int i = 0; i < idata->variables->numberOfVariables - 1; ++i)
				{
					loopOverLCCols(i, lccoeffs->start->getEl(i), lccoeffs->start->getEl(i+1), constrP_tmp, varCP_tmp);
				}
				loopOverLCCols(idata->variables->numberOfVariables - 1,
								lccoeffs->start->getEl(idata->variables->numberOfVariables-1), terminalStartVal, constrP_tmp, varCP_tmp);
			}
			else
			{
				assert(lccoeffs->iNumberOfStartElements >= idata->constraints->numberOfConstraints);
				//"Less linear constraint coefficient start elements than expected,"

				int terminalStartVal;
				if (lccoeffs->iNumberOfStartElements > idata->constraints->numberOfConstraints)
				{
					terminalStartVal = lccoeffs->start->getEl(idata->constraints->numberOfConstraints);
				}
				else
				{
					terminalStartVal = lccoeffs->numberOfValues;
				};
				// Deal with last column as special case, TODO: is this necessary? see assumption above
				for (int i = 0; i < idata->constraints->numberOfConstraints - 1; ++i)
				{
					loopOverLCRows(i, lccoeffs->start->getEl(i), lccoeffs->start->getEl(i+1), constrP_tmp, varCP_tmp);
				}
				loopOverLCRows(idata->constraints->numberOfConstraints - 1,
								lccoeffs->start->getEl(idata->constraints->numberOfConstraints-1), terminalStartVal, constrP_tmp, varCP_tmp);
			}
		}

		// Linear part of objectives
		Objectives* objs = idata->objectives;
		if (objs)
		{
			for (int i = 0; i < objs->numberOfObjectives; ++i)
			{
				for (int j = 0; j < objs->obj[i]->numberOfObjCoef; ++j)
				{
					objP_tmp[i].insert(objs->obj[i]->coef[j]->idx);
					varOP_tmp[objs->obj[i]->coef[j]->idx].insert(i);
				}
			}
		}

		// Quadratics
		QuadraticCoefficients *qcoeffs = idata->quadraticCoefficients;
		if (qcoeffs)
		{
			for (int i = 0; i < qcoeffs->numberOfQuadraticTerms; ++i)
			{
				if (qcoeffs->qTerm[i]->idx < 0)
				{
					const int objIdx = -1-qcoeffs->qTerm[i]->idx;
					objP_tmp[objIdx].insert(qcoeffs->qTerm[i]->idxOne);
					objP_tmp[objIdx].insert(qcoeffs->qTerm[i]->idxTwo);
					varOP_tmp[qcoeffs->qTerm[i]->idxOne].insert(objIdx);
					varOP_tmp[qcoeffs->qTerm[i]->idxTwo].insert(objIdx);
				}
				else
				{
					const int constrIdx = qcoeffs->qTerm[i]->idx;
					constrP_tmp[constrIdx].insert(qcoeffs->qTerm[i]->idxOne);
					constrP_tmp[constrIdx].insert(qcoeffs->qTerm[i]->idxTwo);
					varCP_tmp[qcoeffs->qTerm[i]->idxOne].insert(constrIdx);
					varCP_tmp[qcoeffs->qTerm[i]->idxTwo].insert(constrIdx);
				}
			}
		}

		// Nonlinear exprs
		NonlinearExpressions *nexprs = idata->nonlinearExpressions;
		if (nexprs)
		{
			for (int i = 0; i < nexprs->numberOfNonlinearExpressions; ++i)
			{
				std::map<int, int> varIdxMap;
				std::map<int, int>::iterator varIdxMapIt;

				nexprs->nl[i]->osExpressionTree->m_treeRoot->getVariableIndexMap(&varIdxMap);
				if (nexprs->nl[i]->idx < 0)
				{
					const int objective = -1-nexprs->nl[i]->idx;
					for (varIdxMapIt = varIdxMap.begin(); varIdxMapIt != varIdxMap.end(); ++varIdxMapIt)
					{
						objP_tmp[objective].insert(varIdxMapIt->first);
						varOP_tmp[varIdxMapIt->first].insert(objective);
					}
				}
				else
				{
					const int constraint = nexprs->nl[i]->idx;
					for (varIdxMapIt = varIdxMap.begin(); varIdxMapIt != varIdxMap.end(); ++varIdxMapIt)
					{
						constrP_tmp[constraint].insert(varIdxMapIt->first);
						varCP_tmp[varIdxMapIt->first].insert(constraint);
					}
				}
			}

			varCP.assign(varCP_tmp.size(), std::vector<int>());
			varOP.assign(varOP_tmp.size(), std::vector<int>());
			constrP.assign(constrP_tmp.size(), std::vector<int>());
			objP.assign(objP_tmp.size(), std::vector<int>());
			for (unsigned i = 0; i < varCP_tmp.size(); ++i)
			{
				varCP[i].insert(varCP[i].end(), varCP_tmp[i].begin(), varCP_tmp[i].end());
			}
			for (unsigned i = 0; i < varOP_tmp.size(); ++i)
			{
				varOP[i].insert(varOP[i].end(), varOP_tmp[i].begin(), varOP_tmp[i].end());
			}
			for (unsigned i = 0; i < constrP_tmp.size(); ++i)
			{
				constrP[i].insert(constrP[i].end(), constrP_tmp[i].begin(), constrP_tmp[i].end());
			}
			for (unsigned i = 0; i < objP_tmp.size(); ++i)
			{
				objP[i].insert(objP[i].end(), objP_tmp[i].begin(), objP_tmp[i].end());
			}
		}
	} catch (const ErrorClass& e) {
		std::cerr << e.errormsg << std::endl; // This is about all this error class is useful for.
	}
}
