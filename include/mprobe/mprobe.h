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
/** @file mprobe.h */


#ifndef MPROBE_H_
#define MPROBE_H_

#include "types.h"

#ifdef MPROBE_STATIC
#  define MPROBE_API extern
#else // MPROBE_DLL
#  if defined(WIN32) && (!defined(__BORLANDC__) || (__BORLANDC__ >= 0x500))
#    ifdef BUILDING_MPROBE
#      define MPROBE_API extern __declspec(dllexport)
#    else
#      define MPROBE_API extern __declspec(dllimport)
#    endif
#  else
#    if __GNUC__>=4
#      define MPROBE_API extern __attribute__ ((visibility("default")))
#    elif __SUNPRO_C > 0x550 || __SUNPRO_CC > 0x550
#      define MPROBE_API extern __global
#    else
#      define MPROBE_API extern
#    endif
#  endif
#endif

#if defined(_WIN32) && defined(USE_STDCALL)
#  define MPROBE_API_CC __stdcall
#else
#  define MPROBE_API_CC // cdecl
#endif

#define  MPROBE_API_FUNC(retType) MPROBE_API retType MPROBE_API_CC

typedef void* MPHandle;
#define MPNULL 0

#ifdef __cplusplus
extern "C" {
#endif

/** @defgroup init Initialisation and Deinitialisation functions
 * @todo: look into replacing these manual calls with dll init and fini functions
 * @{
 */
MPROBE_API_FUNC(void) mprobe_init(void);
MPROBE_API_FUNC(void) mprobe_deinit(void);
/** @} */

/** @defgroup plugin Plugin functions
 * @{
 */
MPROBE_API_FUNC(void) mp_loadPlugins(const char* path);
MPROBE_API_FUNC(void) mp_unloadPlugins(const char* path);
MPROBE_API_FUNC(int) mp_loadedPlugins();
MPROBE_API_FUNC(int) mp_pluginCanRead(const char*);
MPROBE_API_FUNC(const char*) mp_pluginName(int plugin);
MPROBE_API_FUNC(void) mp_releasePluginName(const char*);
/** @} */

/** @defgroup load Problem instance loading/unloading functions
 * @{
 */
MPROBE_API_FUNC(int) mp_compatibleFiles(int numFiles, const char** files);
MPROBE_API_FUNC(int) mp_compatibleFilesFor(int plugin, int numFiles, const char** files);
MPROBE_API_FUNC(MPHandle) mp_load(int numFiles, const char** files);
MPROBE_API_FUNC(MPHandle) mp_loadUsingPlugin(int numFiles, const char** files, const char* plugin);
MPROBE_API_FUNC(void) mp_unload(MPHandle);
/** @} */

/** @defgroup instance Problem instance functions
 * @{
 */


/** Releases a name.
 * @param name	Pointer to a name returned by one of the ____Name() functions
 */
MPROBE_API_FUNC(void) mp_releaseName(MPHandle, const char* name);


/** Get the instance name.
 * @return Null terminated utf-8 encoded string containing instance name.
 * Free using releaseName to avoid a memory leak.
 */
MPROBE_API_FUNC(const char*) mp_instanceName(MPHandle);

/** Get the instance name.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the instance (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
MPROBE_API_FUNC(int) mp_instanceNameToBuf(MPHandle, int, char* buf);

/** Get the number of variables
 * @returns number of variables.
 */
MPROBE_API_FUNC(int) mp_variables(MPHandle);

/** Get variable name.
 * @param variable		Variable index.
 * @return Null terminated utf-8 encoded string containing variable name.
 * Free using releaseName to avoid a memory leak.
 */
MPROBE_API_FUNC(const char*) mp_variableName(MPHandle, int variable);

/** Get variable name.
 * @param variable		Variable index.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] bufs		Name of the variable (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
MPROBE_API_FUNC(int) mp_variableNameToBuf(MPHandle, int variable, int maxLen, char* buf);

/** Get the type of variable
 * @param variable		Variable index.
 * @return Variable type:
 *  'r' for real,
 *  'b' for binary,
 *  'i' for integer.
 */
enum VariableType {
	VReal = 'r',
	VBinary = 'b',
	VInteger = 'i'
};
MPROBE_API_FUNC(int) mp_variableType(MPHandle, int variable);

/** Returns lists of indices of constraints and objectives in which the variable is present.
 * @param variable			Variable index.
 * @param[out] constraints	pointer to array with at least an element for each constraint plus one element.
 * @param[out] objectives	pointer to array with at least an element for each objective plus one element.
 * The first element indicates the number of elements used.
 */
MPROBE_API_FUNC(void) mp_variablePresence(MPHandle, int variable, int* constraints, int* objectives);

/** Get the bounds of the variable
 * @param variable		Variable index.
 * @param[out] lower	Lower bound.
 * @param[out] upper	Upper bound.
 */
MPROBE_API_FUNC(void) mp_variableBounds(MPHandle, int variable, Real* lower, Real* upper);

/** Get the number of objectives
 * @returns number of objectives.
 */
MPROBE_API_FUNC(int) mp_objectives(MPHandle);

/** Get objective name.
 * @param objective		Objective index.
 * @return Null terminated utf-8 encoded string containing objective name.
 * Free using releaseName to avoid a memory leak.
 */
MPROBE_API_FUNC(const char*) mp_objectiveName(MPHandle, int objective);

/** Get objective name.
 * @param objective		Objective index.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the objective (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
MPROBE_API_FUNC(int) mp_objectiveNameToBuf(MPHandle, int objective, int maxLen, char* buf);


/** Get whether objective is to minimized or maximized
 * @param objective 	Objective index.
 * @return 'm' for minimization, 'M' for maximization.
 */
enum ObjectiveType {
	Maximization = 'M',
	Minimization = 'm'
};
MPROBE_API_FUNC(int) mp_objectiveType(MPHandle, int objective);

/** Get variables present in objective.
 * @param objective			Objective index.
 * @param[out] variables	pointer to array with at least an element for each variable.
 * @return					Number of variables present in objective.
 * @note					Negative return values may occur in case of error.
 */
MPROBE_API_FUNC(int) mp_objectiveVariables(MPHandle, int objective, int* variables);

/** Calculates objective value.
 * @param		objective	Objective index.
 * @param		variables	Variable values.
 * @param[out]	result		Value of objective evalutated at point given by variables parameter.
 * @return Zero on error, non-zero otherwise.
 */
MPROBE_API_FUNC(int) mp_evaluateObjectiveVal(MPHandle handle, int objective, Real* variables, Real* result);

/** Calculate objective gradient
 * @param objective	Objective index.
 * @param variables		Variable values.
 * @param[out] result	User provided location to store results.
 * @return Zero on error, non-zero otherwise.
 */
MPROBE_API_FUNC(int) mp_evaluateObjectiveGrad(MPHandle handle, int objective, Real* variables, Real* result);

/** Get number of constraints
 * @return Number of constraints.
 */
MPROBE_API_FUNC(int) mp_constraints(MPHandle);

/** Get constraint name.
 * @param constraint		Constraint index.
 * @return Null terminated utf-8 encoded string containing constraint name.
 * Free using releaseName to avoid a memory leak.
 */
MPROBE_API_FUNC(const char*) mp_constraintName(MPHandle, int constraint);

/** Get constraint name.
 * @param constraint	Constraint index.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the constraint (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
MPROBE_API_FUNC(int) mp_constraintNameToBuf(MPHandle, int constraint, int maxLen, char* buf);

/** Get the type of constraint.
 * @param constraint Constraint index.
 * @return Contraint type:
 * 'r' for range.
 * 'l' for less than or equal.
 * 'g' for greater than or equal.
 * 'e' for equality.
 * 'u' for unconstraining.
 */
enum ConstraintType {
	Range = 'r',
	LEqual = 'l',
	GEqual = 'g',
	Equality = 'e',
	Unconstrained = 'u'
};
MPROBE_API_FUNC(int) mp_constraintType(MPHandle, int constraint);

/** Get constraint bounds.
 * @param constraint	Constraint index.
 * @param[out] lower	Lower bound.
 * @param[out] upper	Upper bound.
 */
MPROBE_API_FUNC(void) mp_constraintBounds(MPHandle, int constraint, Real* lower, Real* upper);

/** Get variables present in constraint.
 * @param constraint		Constraint index.
 * @param[out] variables	pointer to array with at least an element for each variable.
 * @return					Number of variables present in constraint.
 * @note					Negative return values may occur in case of error.
 */
MPROBE_API_FUNC(int) mp_constraintVariables(MPHandle, int constraint, int* variables);

/** Calculates contraint value.
 * @param constraint	Contraint index.
 * @param variables		Variable values.
 * @param result		Value of constraint evalutated at point given by variables parameter.
 * @return Zero on error, non-zero otherwise.
 */
MPROBE_API_FUNC(int) mp_evaluateConstraintVal(MPHandle handle, int constraint, Real* variables, Real* result);


/** Calculate constraint gradient
 * @param constraint	Constraint index.
 * @param variables		Variable values.
 * @param[out] result	User provided location to store results.
 * @return Zero on error, non-zero otherwise.
 */
MPROBE_API_FUNC(int) mp_evaluateConstraintGrad(MPHandle handle, int constraint, Real* variables, Real* result);

/** Get the type of equation.
 * @param type		'c' for constraint, 'o' for objective
 * @param function	either the index for a constraint or objective, depending on the first parameter.
 * @return Returns 'l' for linear, 'q' for quadratic, 'n' for other nonlinear.
 */
enum FunctionType {
	Linear = 'l',
	Quadratic = 'q',
	Nonlinear = 'n',
	Constraint = 'c',
	Objective = 'o'
};
MPROBE_API_FUNC(int) mp_functionType(MPHandle, int type, int function);
/** @} */

enum ErrorType
{
	NoError = 0,
	NullArgument, /**< Null pointer or instance */
	InvalidArgument,
	IndexOutOfBounds,
	InternalError
};


/** @defgroup pierror Problem instance specific error detection and reporting functions.
 * @{
 */
MPROBE_API_FUNC(int) mp_GetLastError(MPHandle, const char**); /**< Outputs error type and sets pointer to string (or null)*/
MPROBE_API_FUNC(void) mp_ReleaseErrorStr(MPHandle, const char*);
/** @} */


/** @defgroup instance Analysis instance functions
 * @{
 */
MPROBE_API_FUNC(MPHandle) mp_createAnalysis(MPHandle);
MPROBE_API_FUNC(void) mp_releaseAnalysis(MPHandle);

MPROBE_API_FUNC(void) mp_aVariableBoundLineSample(MPHandle, int funcType, int func, int extraHists);

MPROBE_API_FUNC(void) mp_aBounds(MPHandle, int var, Real* lower, Real* upper);
MPROBE_API_FUNC(void) mp_aResetBounds(MPHandle);
MPROBE_API_FUNC(void) mp_aClampBounds(MPHandle, Real maxMagnitude);
MPROBE_API_FUNC(void) mp_aSetBounds(MPHandle, int var, Real lower, Real upper);
MPROBE_API_FUNC(Real) mp_aUpperBound(MPHandle, int var);
MPROBE_API_FUNC(void) mp_aSetUpperBound(MPHandle, int var, Real upper);
MPROBE_API_FUNC(Real) mp_aLowerBound(MPHandle, int var);
MPROBE_API_FUNC(void) mp_aSetLowerBound(MPHandle, int var, Real upper);
MPROBE_API_FUNC(Real) mp_aLineLengthMax(MPHandle);
MPROBE_API_FUNC(Real) mp_aLineLengthMin(MPHandle);
MPROBE_API_FUNC(void) mp_aLineLengthBounds(MPHandle, Real* min, Real* max);
MPROBE_API_FUNC(void) mp_aSetLineLengthBounds(MPHandle, Real, Real);
MPROBE_API_FUNC(int) mp_aSnapDiscreteComponents(MPHandle);
MPROBE_API_FUNC(void) mp_aSetSnapDiscreteComponents(MPHandle, int);
MPROBE_API_FUNC(unsigned) mp_aNumLineSegments(MPHandle);
MPROBE_API_FUNC(void) mp_aSetNumLineSegments(MPHandle, unsigned);
MPROBE_API_FUNC(unsigned) mp_aInteriorLinePoints(MPHandle);
MPROBE_API_FUNC(void) mp_aSetNumInteriorLinePoints(MPHandle, unsigned);
MPROBE_API_FUNC(unsigned) mp_aMinimumPointsNeeded(MPHandle);
MPROBE_API_FUNC(void) mp_aSetMinimumPointsNeeded(MPHandle, unsigned);
MPROBE_API_FUNC(Real) mp_aEvalErrorTolerance(MPHandle);
MPROBE_API_FUNC(int) mp_aSetEvalErrorTolerance(MPHandle, Real);
MPROBE_API_FUNC(Real) mp_aInfinity(MPHandle);
MPROBE_API_FUNC(void) mp_aSetInfinity(MPHandle, Real);
MPROBE_API_FUNC(Real) mp_aEqualityTolerance(MPHandle);
MPROBE_API_FUNC(int) mp_aSetEqualityTolerance(MPHandle, Real);
MPROBE_API_FUNC(Real) mp_aAlmostEqualTolerance(MPHandle);
MPROBE_API_FUNC(int) mp_aSetAlmostEqualTolerance(MPHandle, Real);
MPROBE_API_FUNC(uint8_t) mp_aEffectiveness(MPHandle, int constraint, Real* efflb, Real* effub);
MPROBE_API_FUNC(uint8_t) mp_aExtremum(MPHandle, int objective, Real* extremeVal);
MPROBE_API_FUNC(uint8_t) mp_aExtremumPoint(MPHandle, int objective, Real* point);

enum ResultStatus {
	NotAvailable = 'n',
	InsufficientDataPoints = 'i',
	TooManyErrors = 'e',
	Computed = 'y'
};

enum HistType {
	FunctionValue = 'f',
	LineLength = 'l',
	Slope = 'd',
	Shape = 's'
};

MPROBE_API_FUNC(uint64_t) mp_aGetHistogramBin(MPHandle, int histType, int bin);
MPROBE_API_FUNC(unsigned) mp_aGetHistogramNumBins(MPHandle, int histType);
MPROBE_API_FUNC(uint64_t) mp_aGetHistogramNumOutside(MPHandle, int histType);
MPROBE_API_FUNC(uint64_t) mp_aGetHistogramNumAboveRange(MPHandle, int histType);
MPROBE_API_FUNC(uint64_t) mp_aGetHistogramNumBelowRange(MPHandle, int histType);
MPROBE_API_FUNC(uint64_t) mp_aGetHistogramDataPoints(MPHandle, int histType);
MPROBE_API_FUNC(Real)     mp_aGetHistogramMean(MPHandle, int histType);
MPROBE_API_FUNC(Real)     mp_aGetHistogramStdDev(MPHandle, int histType);
MPROBE_API_FUNC(Real)     mp_aGetHistogramVariance(MPHandle, int histType);
MPROBE_API_FUNC(Real)     mp_aGetHistogramPopVariance(MPHandle, int histType);
MPROBE_API_FUNC(Real)     mp_aGetHistogramMaximum(MPHandle, int histType);
MPROBE_API_FUNC(Real)     mp_aGetHistogramMinimum(MPHandle, int histType);

MPROBE_API_FUNC(void) mp_aSetHistogramBins(MPHandle, int histType, int bins, Real firstBinWidth, Real* upperBounds);

enum EmpiricalShape {
	ESShapeError = InsufficientDataPoints,
	ESTooManyMathErrors = TooManyErrors,
	ESNotAvailable = NotAvailable,
	ESLinear = 0,
	ESAlmostLinBoth,
	ESAlmostLinConvex,
	ESAlmostLinConcave,
	ESConvex,
	ESAlmostConvex,
	ESConcave,
	ESAlmostConcave,
	ESBothConc
};

MPROBE_API_FUNC(uint8_t) mp_aGetEmpiricalShape(MPHandle, int func, int funcType);

enum OptimumEffect {
	OEError = InsufficientDataPoints,
	OETooManyMathErrors = TooManyErrors,
	OENotAvailable = NotAvailable,
	ObjectiveLocal = 0,
	ObjectiveGlobal,
	ObjectiveAlmostGlobal
};
MPROBE_API_FUNC(uint8_t) mp_aGetOptimumEffect(MPHandle, int objective);

enum RegionEffect {
	REError = InsufficientDataPoints,
	RETooManyMathErrors = TooManyErrors,
	RENotAvailable = NotAvailable,
	REConvex = 0,
	RENonconvex,
	REAlmost

};
MPROBE_API_FUNC(uint8_t) mp_aGetRegionEffect(MPHandle, int constraint);

/** @} */

#ifdef __cplusplus
}
#endif

#endif // MPROBE_H_