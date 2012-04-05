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

#include "probleminstance.h"

#include <limits>

ProblemInstance::ProblemInstance (RPHandle hndl, SharedPtr<ReaderPluginFunctionTable> functions)
	: handle(hndl), ftable(functions)
{
	int i;
	m_variables = ftable->variables(handle);
	m_constraints = ftable->constraints(handle);
	m_objectives  = ftable->objectives(handle);

	m_variableTypes = new char[variables()+2*constraints()+2*objectives()];
	m_functionTypes = &m_variableTypes[variables()];
	m_constraintTypes = &m_functionTypes[constraints()+objectives()];
	m_objectiveTypes = &m_constraintTypes[constraints()];
	for (i = 0; i < variables(); ++i)
	{
		m_variableTypes[i] = ftable->variableType(handle, i);
	}
	for (i = 0; i < constraints(); ++i)
	{
		m_functionTypes[i] = ftable->functionType(handle, 'c', i);
	}
	for (i = 0; i < objectives(); ++i)
	{
        m_functionTypes[constraints()+i] = ftable->functionType(handle, 'o', i);
	}
	for (i = 0; i < constraints(); ++i)
	{
		m_constraintTypes[i] = ftable->constraintType(handle, i);
	}
	for (i = 0; i < objectives(); ++i)
	{
		m_objectiveTypes[i] = ftable->objectiveType(handle, i);
	}

	int* tmp = new int[std::max(objectives()+constraints()+2, variables())];
	m_presenceIndexes = new unsigned[2*variables()+constraints()+objectives()];
	for (i = 0; i < variables(); ++i)
	{
		ftable->variablePresence(handle, i, tmp, &tmp[constraints()]);
		m_presences.reserve(m_presences.size()+2+tmp[0]+tmp[constraints()]);
		varConstrPresenceIndex(i) = m_presences.size();
		m_presences.insert(m_presences.end(), tmp, &tmp[tmp[0]+1]);
		varObjPresenceIndex(i) = m_presences.size();
		m_presences.insert(m_presences.end(), &tmp[constraints()], &tmp[constraints()+tmp[constraints()]+1]);
	}

	m_variableCounts = new int[4*(constraints()+objectives())];
	std::fill(m_variableCounts, &m_variableCounts[4*(constraints()+objectives())], 0);
	for (i = 0; i < constraints(); ++i)
	{
		int vars = ftable->constraintVariables(handle, i, tmp);
		m_presences.reserve(m_presences.size()+1+vars);
		constraintPresenceIndex(i) = m_presences.size();
		m_presences.push_back(vars);
		m_presences.insert(m_presences.end(), tmp, &tmp[vars]);

		unsigned offset = 4*i;
		m_variableCounts[offset] = vars;
		for (int j = 0; j < vars; ++j)
		{
			switch (variableType(tmp[j])) {
				case 'r': ++m_variableCounts[offset+1]; break;
				case 'b': ++m_variableCounts[offset+2]; break;
				case 'i': ++m_variableCounts[offset+3]; break;
			}
		}
	}

	for (i = 0; i < objectives(); ++i)
	{
		int vars = ftable->objectiveVariables(handle, i, tmp);
		m_presences.reserve(m_presences.size()+1+vars);
		objectivePresenceIndex(i) = m_presences.size();
		m_presences.push_back(vars);
		m_presences.insert(m_presences.end(), tmp, &tmp[vars]);

		unsigned offset = 4*(constraints()+i);
		m_variableCounts[offset] = vars;
		for (int j = 0; j < vars; ++j)
		{
			switch (variableType(tmp[j])) {
				case 'r': ++m_variableCounts[offset+1]; break;
				case 'b': ++m_variableCounts[offset+2]; break;
				case 'i': ++m_variableCounts[offset+3]; break;
			}
		}
	}
	delete [] tmp;

	m_varBounds = new Real[2*(variables()+constraints())];
	m_constrBounds = &m_varBounds[2*variables()];

	for (i = 0; i < variables(); ++i)
	{
		ftable->variableBounds(handle, i, &m_varBounds[2*i], &m_varBounds[2*i+1]);
	}

	for (i = 0; i < constraints(); ++i)
	{
		ftable->constraintBounds(handle, i, &m_constrBounds[2*i], &m_constrBounds[2*i+1]);
		switch (constraintType(i)) {
			case 'l':
				m_constrBounds[2*i] = -std::numeric_limits<Real>::infinity();
				break;
			case 'g':
				m_constrBounds[2*i+1] = std::numeric_limits<Real>::infinity();
				break;
			case 'e':
				m_constrBounds[2*i] = m_constrBounds[2*i+1];
				break;
			case 'u':
				m_constrBounds[2*i] = -std::numeric_limits<Real>::infinity();
				m_constrBounds[2*i+1] = std::numeric_limits<Real>::infinity();
				break;
			default:
				break;
		}
	}

	latestErrorType = 0;
}

ProblemInstance::~ProblemInstance()
{
	delete [] m_varBounds;
	delete [] m_variableCounts;
	delete [] m_presenceIndexes;
	delete [] m_variableTypes;
	ftable->releaseInstance(handle);
}

void ProblemInstance::releaseName(const char* str)
{
	return ftable->releaseName(handle, str);
}

const char* ProblemInstance::instanceName()
{
	return ftable->instanceName(handle);
}

int ProblemInstance::instanceNameToBuf(int maxLen, char* str)
{
	return ftable->instanceNameToBuf(handle, maxLen, str);
}

int ProblemInstance::variables() const
{
	return m_variables;
}

const char* ProblemInstance::variableName(int var)
{
	return ftable->variableName(handle, var);
}

int ProblemInstance::variableNameToBuf(int var,int maxLen, char* str)
{
	return ftable->variableNameToBuf(handle, var, maxLen, str);
}

int ProblemInstance::variableType(int var) const
{
	return m_variableTypes[var];
}

void ProblemInstance::variableBounds(int var, Real* lwr, Real* upr) const
{
	*lwr = m_varBounds[2*var];
	*upr = m_varBounds[2*var+1];
}

void ProblemInstance::variablePresence(int var, int* constr, int* obj)  const
{
	std::vector<int>::const_iterator start, end;
	start = m_presences.begin();
    std::advance(start, varConstrPresenceIndex(var));
	end = start;
    std::advance(end, (*start)+1);
	std::copy(start, end, constr);
	start = end;
    std::advance(end, (*start)+1);
	std::copy(start, end, obj);
}

int ProblemInstance::objectives() const
{
	return m_objectives;
}

const char* ProblemInstance::objectiveName(int obj)
{
	return ftable->objectiveName(handle, obj);
}

int ProblemInstance::objectiveNameToBuf(int obj, int maxLen, char* str)
{
	return ftable->objectiveNameToBuf(handle, obj, maxLen, str);
}

int ProblemInstance::objectiveType(int obj)  const
{
	return m_objectiveTypes[obj];
}

int ProblemInstance::objectiveVariables(int obj, int* vars)  const
{
	std::vector<int>::const_iterator start, end;
	int totalVars;
	start = m_presences.begin();
	std::advance(start, objectivePresenceIndex(obj));
	totalVars = *start;
	end = ++start;
	std::advance(end, totalVars);
	std::copy(start, end, vars);
	return totalVars;
}

int ProblemInstance::objectiveVariables(int objective, const int** variables) const
{
	*variables = &m_presences[objectivePresenceIndex(objective)];
	return *((*variables)++);
}

int ProblemInstance::objectiveVariableTypes(int objective, int* real, int* binary, int* integer)  const
{
	unsigned offset = 4*(constraints()+objective);
	*real = m_variableCounts[offset+1];
	*binary = m_variableCounts[offset+2];
	*integer = m_variableCounts[offset+3];
	return m_variableCounts[offset];
}

int ProblemInstance::evaluateObjectiveVal(int obj, Real* vars, Real* res)
{
	return ftable->evaluateObjectiveVal(handle, obj, vars, res);
}

int ProblemInstance::evaluateObjectiveGrad(int obj, Real* vars, Real* res)
{
	return ftable->evaluateObjectiveGrad(handle, obj, vars, res);
}

int ProblemInstance::constraints() const
{
	return m_constraints;
}

const char* ProblemInstance::constraintName(int constr)
{
	return ftable->constraintName(handle, constr);
}

int ProblemInstance::constraintNameToBuf(int constr, int maxLen, char* str)
{
	return ftable->constraintNameToBuf(handle, constr, maxLen, str);
}

int ProblemInstance::constraintType(int constr)  const
{
	return m_constraintTypes[constr];
}

void ProblemInstance::constraintBounds(int constr, Real* lwr, Real* upr)  const
{
	*lwr = m_constrBounds[2*constr];
	*upr = m_constrBounds[2*constr+1];
}

int ProblemInstance::constraintVariables(int constr, int* vars)  const
{
	std::vector<int>::const_iterator start, end;
	int totalVars;
	start = m_presences.begin();
	std::advance(start, constraintPresenceIndex(constr));
	totalVars = *start;
	end = ++start;
	std::advance(end, totalVars);
	std::copy(start, end, vars);
	return totalVars;
}

int ProblemInstance::constraintVariables(int constraint, const int** variables) const
{
	*variables = &m_presences[constraintPresenceIndex(constraint)];
	return *((*variables)++);
}

int ProblemInstance::constraintVariableTypes(int constraint, int* real, int* binary, int* integer) const
{
	unsigned offset = 4*constraint;
	*real = m_variableCounts[offset+1];
	*binary = m_variableCounts[offset+2];
	*integer = m_variableCounts[offset+3];
	return m_variableCounts[offset];
}

int ProblemInstance::evaluateConstraintVal(int constr, Real* vars, Real* res)
{
	return ftable->evaluateConstraintVal(handle, constr, vars, res);
}

int ProblemInstance::evaluateConstraintGrad(int constr, Real* vars, Real* res)
{
	return ftable->evaluateConstraintGrad(handle, constr, vars, res);
}

int ProblemInstance::functionType(int type, int func) const
{
	int index = func + (type == 'c' ? 0 : constraints());
	return m_functionTypes[index];
}

void ProblemInstance::setLatestError(int type, const std::string& errorStr)
{
	latestErrorType = type;
	latestErrorStr = errorStr;
}

int ProblemInstance::getLatestError(std::string& errorStr)
{
	int tmp = latestErrorType;
	latestErrorType = 0;
	errorStr = latestErrorStr;
	latestErrorStr.clear();
	return tmp;
}
