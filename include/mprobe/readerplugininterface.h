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
/** @file readerplugininterface.h */

#ifndef READERPLUGININTERFACE_H_
#define READERPLUGININTERFACE_H_

#include "types.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
#define RP_EXTERN extern "C"
#else
#define RP_EXTERN extern
#endif

// No static reader plugin support for now
#if (defined(_WIN32) || defined(__CYGWIN__) && (!defined(__BORLANDC__) || (__BORLANDC__ >= 0x500)))
#  if defined BUILDING_RP_DLL
#    define RP_EXPORT RP_EXTERN __declspec(dllexport)
#  else
#    define RP_EXPORT RP_EXTERN __declspec(dllimport)
#  endif
#else
#  if __GNUC__>=4
#    define RP_EXPORT RP_EXTERN __attribute__ ((visibility("default")))
#  elif __SUNPRO_C > 0x550 || __SUNPRO_CC > 0x550
#    define RP_EXPORT RP_EXTERN __global
#  else
#    define RP_EXPORT RP_EXTERN
#  endif
#endif

// API versioning
#define READER_PLUGIN_API_VERSION_MAJOR 0
#define READER_PLUGIN_API_VERSION_MINOR 0
RP_EXPORT void getReaderPluginAPIVersion(int* major, int* minor);

typedef struct _ReaderPluginDetails {
	const char* name;					/**< Plugin name */
	const char* funcPrefix;				/**< Function name prefixes */
	unsigned flags;						/**< Plugin flags */
	struct _ReaderPluginDetails* next;	/**< Pointer to another plugin in this shared library */
} ReaderPluginDetails;

// Plugin Flags
#define RP_THREADSAFE			(1<<0) /**< Function evaluation and getters are thread-safe */
#define RP_NON_REENTRANT		(1<<1) /**< Functions are non-reentrant */

typedef void* RPHandle;
#define RPNULL 0

/** Get reader plugin details.
 * @returns Pointer to structure containing details.
 */
RP_EXPORT ReaderPluginDetails* getReaderPluginDetails(void);

/* API Documentation, we define a macro so that user code can properly declare prefixed functions*/
#if defined(DOXYGEN) || defined(RP_NOPREFIX)

/** Creates a problem instance.
 * @param numFiles	The number of filenames passed in the following parameters
 * @param files		Filenames: null terminated C strings
 * @return			Opaque handle to the instance, RPNULL on failure.
 * @attention file string encoding is assumed to be UTF-8 on Windows
 */
RP_EXPORT RPHandle createInstance(int numFiles, const char** files);

/** Creates a problem instance.
 * @param numFiles		The number of filenames passed in the following parameters
 * @param files			Filenames: null terminated C strings
 * @param[out] compat	Option parameter to retrieve information about specific files.
 * @return				A negative number indicates the number of additional files required.
 * 						Zero indicates that all the listed files are compatible and
 * 							can be used to create an instance.
 * 						A positive number indicates that some of the files can be used, the number represents the number
 *							 unrecognized files.
 */
RP_EXPORT int compatibleFiles(int numFiles, const char** files, int* compatible);

/** Release an instance.
 * @param handle	The handle to the instance
 */
RP_EXPORT void releaseInstance(RPHandle handle);

/** Releases a name.
 * @param name	Pointer to a name returned by one of the ____Name() functions
 */
RP_EXPORT void releaseName(RPHandle, const char* name);

/** Get the instance name.
 * @return Null terminated utf-8 encoded string containing instance name.
 * Free using releaseName to avoid a memory leak.
 */
RP_EXPORT const char* instanceName(RPHandle handle);

/** Get the instance name.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the instance (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
RP_EXPORT int instanceNameToBuf(RPHandle handle, int maxLen, char* buf);

/** Get the number of variables
 * @returns number of variables.
 */
RP_EXPORT int variables(RPHandle handle);

/** Get variable name.
 * @param variable		Variable index.
 * @return Null terminated utf-8 encoded string containing variable name.
 * Free using releaseName to avoid a memory leak.
 */
RP_EXPORT const char* variableName(RPHandle handle, int variable);

/** Get variable name.
 * @param variable		Variable index.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the variable (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
RP_EXPORT int variableNameToBuf(RPHandle handle, int variable, int maxLen, char* buf);

/** Get the type of variable
 * @param variable		Variable index.
 * @return Variable type:
 *  'r' for real,
 *  'b' for binary,
 *  'i' for integer.
 */
RP_EXPORT int variableType(RPHandle handle, int variable);

/** Get the bounds of the variable
 * @param variable		Variable index.
 * @param[out] lower	Lower bound.
 * @param[out] upper	Upper bound.
 */
RP_EXPORT void variableBounds(RPHandle handle, int variable, Real* lower, Real* upper);

/** Returns lists of indices of constraints and objectives in which the variable is present.
 * @param variable			Variable index.
 * @param[out] constraints	pointer to array with at least an element for each constraint plus one element.
 * @param[out] objectives	pointer to array with at least an element for each objective plus one element.
 * The first element indicates the number of elements used.
 */
RP_EXPORT void variablePresence(RPHandle handle, int variable, int* constraints, int* objectives);

/** Get the number of objectives
 * @returns number of objectives.
 */
RP_EXPORT int objectives(RPHandle handle);

/** Get objective name.
 * @param objective		Objective index.
 * @return Null terminated utf-8 encoded string containing objective name.
 * Free using releaseName to avoid a memory leak.
 */
RP_EXPORT const char* objectiveName(RPHandle handle, int objective);

/** Get objective name.
 * @param objective		Objective index.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the objective (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
RP_EXPORT int objectiveNameToBuf(RPHandle handle, int objective, int maxLen, char* buf);

/** Get whether objective is to minimized or maximized
 * @param objective 	Objective index.
 * @return 'm' for minimization, 'M' for maximization.
 */
RP_EXPORT int objectiveType(RPHandle handle, int objective);

/** Get variables present in objective.
 * @param objective			Objective index.
 * @param[out] variables	pointer to array with at least an element for each variable.
 * @return					Number of variables present in objective.
 * @note					Negative return values may occur in case of error.
 */
RP_EXPORT int objectiveVariables(RPHandle handle, int objective, int* variables);

/** Calculates objective value.
 * @param		objective	Objective index.
 * @param		variables	Variable values.
 * @param[out]	result		Value of objective evalutated at point given by variables parameter.
 * @return Zero on error, non-zero otherwise.
 */
RP_EXPORT int evaluateObjectiveVal(RPHandle handle, int objective, Real* variables, Real* result);

/** Calculate objective gradient
 * @param objective	Objective index.
 * @param variables		Variable values.
 * @param[out] result	User provided location to store results.
 * @return Zero on error, non-zero otherwise.
 */
RP_EXPORT int evaluateObjectiveGrad(RPHandle handle, int objective, Real* variables, Real* result);

/** Get number of constraints
 * @return Number of constraints.
 */
RP_EXPORT int constraints(RPHandle handle);

/** Get constraint name.
 * @param constraint		Constraint index.
 * @return Null terminated utf-8 encoded string containing constraint name.
 * Free using releaseName to avoid a memory leak.
 */
RP_EXPORT const char* constraintName(RPHandle handle, int constraint);

/** Get constraint name.
 * @param constraint	Constraint index.
 * @param maxLen		Max number of bytes which the array pointed by str can hold.
 * @param[out] buf		Name of the constraint (utf-8 encoding.)
 * @return Number of bytes written (incl. null termination.) If maxLen is zero or buf is null,
 * the number of bytes required to contain the entire string is returned.
 */
RP_EXPORT int constraintNameToBuf(RPHandle handle, int constraint, int maxLen, char* buf);


/** Get the type of constraint.
 * @param constraint Constraint index.
 * @return Contraint type:
 * 'r' for range.
 * 'l' for less than or equal.
 * 'g' for greater than or equal.
 * 'e' for equality.
 * 'u' for unconstraining.
 */
RP_EXPORT int constraintType(RPHandle  handle, int constraint);

/** Get constraint bounds.
 * @param constraint	Constraint index.
 * @param[out] lower	Lower bound.
 * @param[out] upper	Upper bound.
 */
RP_EXPORT void constraintBounds(RPHandle handle, int constraint, Real* lower, Real* upper);

/** Get variables present in constraint.
 * @param constraint		Constraint index.
 * @param[out] variables	pointer to array with at least an element for each variable.
 * @return					Number of variables present in constraint.
 * @note					Negative return values may occur in case of error.
 */
RP_EXPORT int constraintVariables(RPHandle handle, int constraint, int* variables);

/** Calculates contraint value.
 * @param constraint	Contraint index.
 * @param variables		Variable values.
 * @param result		Value of constraint evalutated at point given by variables parameter.
 * @return Zero on error, non-zero otherwise.
 */
RP_EXPORT int evaluateConstraintVal(RPHandle handle, int constraint, Real* variables, Real* result);

/** Calculate constraint gradient
 * @param constraint	Constraint index.
 * @param variables		Variable values.
 * @param[out] result	User provided location to store results.
 * @return Zero on error, non-zero otherwise.
 */
RP_EXPORT int evaluateConstraintGrad(RPHandle handle, int constraint, Real* variables, Real* result);

/** Get the type of equation.
 * @param type		'c' for constraint, 'o' for objective
 * @param function	either the index for a constraint or objective, depending on the first parameter.
 * @return Returns 'l' for linear, 'q' for quadratic, 'n' for other nonlinear.
 */
RP_EXPORT int functionType(RPHandle handle, int type, int function);

#endif // defined(DOXYGEN) || defined(RP_NOPREFIX)

/** Declare all the reader plugin functions with a prefix */
#define DECLARE_READER_PLUGIN(prefix) \
RP_EXPORT RPHandle prefix##createInstance(int, const char**);					\
RP_EXPORT int prefix##compatibleFiles(int, const char**, int*);					\
RP_EXPORT void prefix##releaseInstance(RPHandle);								\
RP_EXPORT void prefix##releaseName(const char*);								\
RP_EXPORT const char* prefix##instanceName(RPHandle);							\
RP_EXPORT int prefix##instanceNameToBuf(RPHandle, int, char*);					\
RP_EXPORT int prefix##variables(RPHandle);										\
RP_EXPORT const char* prefix##variableName(RPHandle, int);						\
RP_EXPORT int prefix##variableNameToBuf(RPHandle, int, int, char*);				\
RP_EXPORT void prefix##variablePresence(RPHandle, int, int*, int*);				\
RP_EXPORT int prefix##variableType(RPHandle, int);								\
RP_EXPORT void prefix##variableBounds(RPHandle, int, Real*, Real*);				\
RP_EXPORT int prefix##objectives(RPHandle);										\
RP_EXPORT const char* prefix##objectiveName(RPHandle, int);						\
RP_EXPORT int prefix##objectiveNameToBuf(RPHandle, int, int, char*);			\
RP_EXPORT int prefix##objectiveType(RPHandle, int);								\
RP_EXPORT int prefix##objectiveVariables(RPHandle, int, int*);					\
RP_EXPORT int prefix##evaluateObjectiveVal(RPHandle, int, Real*, Real*);		\
RP_EXPORT int prefix##evaluateObjectiveGrad(RPHandle, int, Real*, Real*);		\
RP_EXPORT int prefix##constraints(RPHandle);									\
RP_EXPORT const char* prefix##constraintName(RPHandle, int);					\
RP_EXPORT int prefix##constraintNameToBuf(RPHandle, int, int, char*);			\
RP_EXPORT int prefix##constraintType(RPHandle, int);							\
RP_EXPORT void prefix##constraintBounds(RPHandle, int, Real*, Real*);			\
RP_EXPORT void prefix##constraintVariables(RPHandle, int, int*);				\
RP_EXPORT int prefix##evaluateConstraintVal(RPHandle, int, Real*, Real*);		\
RP_EXPORT int prefix##evaluateConstraintGrad(RPHandle, int, Real*, Real*);	\
RP_EXPORT int prefix##functionType(RPHandle, int, int)

/** Convenience struct */
typedef struct _ReaderPluginFunctionTable {
RPHandle	(*createInstance)(int, const char**);
int			(*compatibleFiles)(int, const char**, int*);
void		(*releaseInstance)(RPHandle);
void		(*releaseName)(RPHandle, const char*);
const char*	(*instanceName)(RPHandle);
int			(*instanceNameToBuf)(RPHandle, int, char*);
int			(*variables)(RPHandle);
const char*	(*variableName)(RPHandle, int);
int			(*variableNameToBuf)(RPHandle, int, int, char*);
void		(*variablePresence)(RPHandle, int, int*, int*);
int			(*variableType)(RPHandle, int);
void		(*variableBounds)(RPHandle, int, Real*, Real*);
int			(*objectives)(RPHandle);
const char*	(*objectiveName)(RPHandle, int);
int			(*objectiveNameToBuf)(RPHandle, int, int, char*);
int			(*objectiveType)(RPHandle, int);
int			(*objectiveVariables)(RPHandle, int, int*);
int			(*evaluateObjectiveVal)(RPHandle, int, Real*, Real*);
int			(*evaluateObjectiveGrad)(RPHandle, int, Real*, Real*);
int			(*constraints)(RPHandle);
const char*	(*constraintName)(RPHandle, int);
int			(*constraintNameToBuf)(RPHandle, int, int, char*);
int			(*constraintType)(RPHandle, int);
void		(*constraintBounds)(RPHandle, int, Real*, Real*);
int			(*constraintVariables)(RPHandle, int, int*);
int			(*evaluateConstraintVal)(RPHandle, int, Real*, Real*);
int			(*evaluateConstraintGrad)(RPHandle, int, Real*, Real*);
int			(*functionType)(RPHandle, int, int);
} ReaderPluginFunctionTable;

#ifdef __cplusplus
}
#endif

#endif // READERPLUGININTERFACE_H_