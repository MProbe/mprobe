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


#ifndef READERPLUGIN_H_
#define READERPLUGIN_H_

#include "iplugin.h"

#include <string>
#include <vector>
#include <utility>

#include "Poco/AtomicCounter.h"
#include "Poco/SharedPtr.h"
#include "Poco/RefCountedObject.h"

#include "readerplugininterface.h"

namespace Poco {
class SharedLibrary;
}
using Poco::SharedLibrary;
using Poco::SharedPtr;

class ProblemInstance;

class ReaderPlugin : public IPlugin
{
	struct Details {
		ReaderPluginFunctionTable ftable;
		std::string name;
		unsigned flags;
	};

	ReaderPlugin(const Details&);

public:
	int referenceCount() const;
	std::string name() const;
	std::string type() const;
	ProblemInstance* read(int numFiles, const char** files);
	int compatibleFiles(int numFiles, const char** files, int* compat);

	static bool isCompatiblePlugin(SharedLibrary*);
	static std::vector<SharedPtr<IPlugin> > load(SharedLibrary*);
	static const std::string PluginTypeStr;
private:
	ReaderPluginFunctionTable functions;
	std::string pluginName;
	unsigned flags;

	struct NullReleasePolicy { template <class C> static void release(C*){}};
	SharedPtr<ReaderPluginFunctionTable, Poco::ReferenceCounter, NullReleasePolicy > ftablePtr;
};

#endif // READERPLUGIN_H_
