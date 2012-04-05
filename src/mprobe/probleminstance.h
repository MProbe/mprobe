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


#ifndef PROBLEMINSTANCE_H_
#define PROBLEMINSTANCE_H_

#include <vector>

#include <Poco/SharedPtr.h>

#include "readerplugininterface.h"
#include "readerplugin.h"

using Poco::SharedPtr;

/*
 * For now, assume all problem instances are provided from reader plugins,
 * this avoids unnecessary vtables + indirection.
 */

class ProblemInstance
{
public:
	ProblemInstance (RPHandle, SharedPtr<ReaderPluginFunctionTable>);
	~ProblemInstance();

	void releaseName(const char* name);
	const char* instanceName();
	int instanceNameToBuf(int maxLen, char* str);
	int variables() const;
	const char* variableName(int variable);
	int variableNameToBuf(int variable,int maxLen, char* str);
	int variableType(int variable) const;
	void variableBounds(int variable, Real* lower, Real* upper) const;
	void variablePresence(int variable, int* constraints, int* objectives) const;

	int objectives() const;
	const char* objectiveName(int objective);
	int objectiveNameToBuf(int objective, int maxLen, char* str);
	int objectiveType(int objective) const;
	int objectiveVariables(int objective, int* variables) const;
	int objectiveVariables(int objective, const int** variables) const;
	int objectiveVariableTypes(int objective, int* real, int* binary, int* integer) const;
	int evaluateObjectiveVal(int objective, Real* variables, Real* result);
	int evaluateObjectiveGrad(int objective, Real* variables, Real* result);

	int constraints() const;
	const char* constraintName(int constraint);
	int constraintNameToBuf(int constraint, int maxLen, char* str);
	int constraintType(int constraint) const;
	void constraintBounds(int constraint, Real* lower, Real* upper) const;
	int constraintVariables(int constraint, int* variables) const;
	int constraintVariables(int constraint, const int** variables) const;
	int constraintVariableTypes(int constraint, int* real, int* binary, int* integer) const;
	int evaluateConstraintVal(int constraint, Real* variables, Real* result);
	int evaluateConstraintGrad(int constraint, Real* variables, Real* result);

	int functionType(int type, int function) const;

	void setLatestError(int type, const std::string& errorStr);
	int getLatestError(std::string& errorStr);
private:

	RPHandle handle;
	SharedPtr<ReaderPluginFunctionTable> ftable;

	char* m_variableTypes;
	char* m_functionTypes;
	char* m_constraintTypes;
	char* m_objectiveTypes;
	unsigned* m_presenceIndexes;
	int* m_variableCounts;
	Real* m_varBounds;
	Real* m_constrBounds;
	std::vector<int> m_presences;
	std::string latestErrorStr;
	int m_variables;
	int m_constraints;
	int m_objectives;
	int bVars;
	int iVars;
	int nlVars;
	int latestErrorType;

	inline unsigned varConstrPresenceIndex(int var) const {
		return m_presenceIndexes[2*var];
	}
	inline unsigned& varConstrPresenceIndex(int var) {
		return m_presenceIndexes[2*var];
	}
	inline unsigned varObjPresenceIndex(int var) const {
		return m_presenceIndexes[2*var+1];
	}
	inline unsigned& varObjPresenceIndex(int var) {
		return m_presenceIndexes[2*var+1];
	}
	inline unsigned constraintPresenceIndex(int constr) const {
		return m_presenceIndexes[2*variables()+constr];
	}
	inline unsigned& constraintPresenceIndex(int constr) {
		return m_presenceIndexes[2*variables()+constr];
	}
	inline unsigned objectivePresenceIndex(int obj) const {
		return m_presenceIndexes[2*variables()+constraints()+obj];
	}
	inline unsigned& objectivePresenceIndex(int obj) {
		return m_presenceIndexes[2*variables()+constraints()+obj];
	}
};
#endif // PROBLEMINSTANCE_H_
