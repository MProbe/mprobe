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

/** @file pluginmanager.h */

#ifndef PLUGINMANAGER_H_
#define PLUGINMANAGER_H_

#include <vector>
#include <string>
#include <map>
#include <set>

#include <Poco/SharedPtr.h>
#include <Poco/RWLock.h>

#include "iplugin.h"

namespace Poco
{
class SharedLibrary;
}
using Poco::SharedLibrary;
using Poco::SharedPtr;
using Poco::RWLock;
using Poco::ScopedReadRWLock;
using Poco::ScopedWriteRWLock;

struct PluginRegistration
{
	std::string typeStr;
	bool (*isCompatiblePlugin)(SharedLibrary*);
	std::vector<SharedPtr<IPlugin> > (*load)(SharedLibrary*); /**< Note: supports multiple plugins per SharedLibrary */
	PluginRegistration() : isCompatiblePlugin(0), load(0) {}
};


class PluginManager
{
public:
	~PluginManager();
	void registerPlugin(PluginRegistration);
	void loadPlugins(std::string path);
	bool unloadPlugins(std::string path);
	bool unloadAllPlugins();
	void forceUnloadAllPlugins();

	int numPlugins() const;
	SharedPtr<IPlugin> getPlugin(int);

	typedef std::set<SharedPtr<IPlugin> > PluginSet;

	PluginSet getPluginsByType(std::string);
	SharedPtr<IPlugin> getPluginByName(std::string);
private:

	typedef std::vector<PluginRegistration> PluginTypeContainer;
	typedef std::map<std::string, PluginSet>  PluginContainer;
	typedef std::map<std::string, std::pair<SharedPtr<SharedLibrary>, PluginSet > > LibraryPluginMapping;

	/*
	 * Store references to the loaded plugins in two places for easy access:
	 *  - A mapping to retrieve loaded plugins by type.
	 *  - A mapping to retrieve loaded plugins from the library they were loaded from.
	 */

	PluginContainer loadedPlugins;
	PluginTypeContainer pluginTypes;
	LibraryPluginMapping libraryPluginMapping;

	mutable RWLock rwlock;
};

#endif // PLUGINMANAGER_H_
