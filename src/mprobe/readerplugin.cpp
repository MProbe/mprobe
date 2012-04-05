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


#include "readerplugin.h"

#include <cstring>
#include <cassert>
#include <sstream>
#include <iostream>

#include "Poco/SharedLibrary.h"
#include "Poco/Path.h"
#include "probleminstance.h"

using Poco::SharedLibrary;
using Poco::NotFoundException;

const std::string ReaderPlugin::PluginTypeStr = "readerplugin";

ReaderPlugin::ReaderPlugin(const Details& details)
	: functions(details.ftable), pluginName(details.name), ftablePtr(&functions)
{
}

int ReaderPlugin::referenceCount() const
{
	return ftablePtr.referenceCount() - 1;
}

std::string ReaderPlugin::name() const
{
	return pluginName;
}

std::string ReaderPlugin::type() const
{
	return PluginTypeStr;
}

/*
 * This templated function was created to save explicitely typing all the
 * casts of the results of dlsym. This template can be very dangerous.
 */
template <typename A>
void fptr_cast(A& a, void* fptr)
{
	 /* No standard compliant way of casting void* to function pointer
	  * but _many_ compilers allow this as an extension
	  */
	a = (A) fptr;
}

bool ReaderPlugin::isCompatiblePlugin(SharedLibrary* lib)
{
	void (*getVersion)(int*,int*);
	int major, minor;
	if (!lib->isLoaded())
	{
		return false;
	}

	try {
		fptr_cast(getVersion, lib->getSymbol("getReaderPluginAPIVersion"));
	} catch (Poco::NotFoundException&)
	{
		return false;
	}

	getVersion(&major, &minor);
	if (major == READER_PLUGIN_API_VERSION_MAJOR && minor <= READER_PLUGIN_API_VERSION_MINOR)
	{
		return true;
	}
	return false;
}

std::vector<SharedPtr<IPlugin> > ReaderPlugin::load(SharedLibrary* lib)
{
	ReaderPluginDetails* (*getReaderPluginDetails)();
	ReaderPluginDetails* details;
	std::vector<SharedPtr<IPlugin> > plugins;

	if (!isCompatiblePlugin(lib))
	{
		return plugins;
	}

	try {
		fptr_cast(getReaderPluginDetails, lib->getSymbol("getReaderPluginDetails"));
	} catch (NotFoundException&)
	{
		return plugins;
	}

	details = getReaderPluginDetails();

	while (details != 0)
	{
		Details readerDetails;
		ReaderPluginFunctionTable& functions = readerDetails.ftable;
		std::string prefix = details->funcPrefix;

		std::memset(&readerDetails.ftable, 0, sizeof(readerDetails.ftable));
		if (details->name != 0)
		{
			readerDetails.name = details->name;
		}

		readerDetails.flags = details->flags;

		try {
#define AUTO_FPTR_CAST(funcName) fptr_cast(functions.funcName, lib->getSymbol(prefix+#funcName))
			/* For example: AUTO_FPTR_CAST(createInstance); is equivalent to
			 * fptr_cast(functions.createInstance, lib->getSymbol(prefix+"createInstance"));
			 */
			AUTO_FPTR_CAST(createInstance);
			AUTO_FPTR_CAST(compatibleFiles);
			AUTO_FPTR_CAST(releaseInstance);
			AUTO_FPTR_CAST(releaseName);
			AUTO_FPTR_CAST(instanceName);
			AUTO_FPTR_CAST(instanceNameToBuf);
			AUTO_FPTR_CAST(variables);
			AUTO_FPTR_CAST(variableBounds);
			AUTO_FPTR_CAST(variableName);
			AUTO_FPTR_CAST(variableNameToBuf);
			AUTO_FPTR_CAST(variablePresence);
			AUTO_FPTR_CAST(variableType);
			AUTO_FPTR_CAST(objectives);
			AUTO_FPTR_CAST(objectiveName);
			AUTO_FPTR_CAST(objectiveNameToBuf);
			AUTO_FPTR_CAST(objectiveType);
			AUTO_FPTR_CAST(objectiveVariables);
			AUTO_FPTR_CAST(evaluateObjectiveVal);
			AUTO_FPTR_CAST(evaluateObjectiveGrad);
			AUTO_FPTR_CAST(constraints);
			AUTO_FPTR_CAST(constraintName);
			AUTO_FPTR_CAST(constraintNameToBuf);
			AUTO_FPTR_CAST(constraintType);
			AUTO_FPTR_CAST(constraintBounds);
			AUTO_FPTR_CAST(constraintVariables);
			AUTO_FPTR_CAST(evaluateConstraintVal);
			AUTO_FPTR_CAST(evaluateConstraintGrad);
			AUTO_FPTR_CAST(functionType);
#undef AUTO_FPTR_CAST
        } catch (NotFoundException&)
		{
			details = details->next;
			continue;
		}

		for (unsigned i = 0; i < sizeof(functions)/sizeof(void(*)()); i++)
			assert(((void(**)())&functions)[i] != (void(*)())0);

		plugins.push_back(new ReaderPlugin(readerDetails));

		details = details->next;
	}

	return plugins;
}

ProblemInstance* ReaderPlugin::read(int numFiles, const char** files)
{
	RPHandle handle = functions.createInstance(numFiles, files);
	if (handle == RPNULL)
	{
		return 0;
	}
	else
	{
		return new ProblemInstance(handle, ftablePtr);
	}
}

int ReaderPlugin::compatibleFiles(int numFiles, const char** files, int* compat)
{
	return functions.compatibleFiles(numFiles, files, compat);
}
