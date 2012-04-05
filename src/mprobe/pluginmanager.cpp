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


#include "pluginmanager.h"

#include <iterator>
#include <algorithm>

#include "Poco/SharedLibrary.h"
#include "Poco/Path.h"
using Poco::Path;

PluginManager::~PluginManager()
{
}

void PluginManager::registerPlugin(PluginRegistration plugin)
{
	ScopedWriteRWLock w(rwlock);
	pluginTypes.push_back(plugin);
}

void PluginManager::loadPlugins(std::string path)
{
	ScopedWriteRWLock w(rwlock);
	SharedLibrary* lib = 0;

	if (libraryPluginMapping.find(Path(path).getFileName()) != libraryPluginMapping.end())
	{
		return; // Already loaded.
	}

	try {
		lib = new SharedLibrary(path);
	}
	catch (...)
	{
		return;
	}

	std::vector<PluginRegistration>::iterator it;
	for (it = pluginTypes.begin(); it != pluginTypes.end(); it++)
	{
		if (it->isCompatiblePlugin(lib))
		{
			typedef std::vector<SharedPtr<IPlugin> > PluginVector;
			PluginVector newlyLoadedPlugins = it->load(lib);
			PluginContainer::iterator pl_it = loadedPlugins.find(it->typeStr);
			PluginVector::iterator p_it;

			// if currently there is none with that type
			if (pl_it == loadedPlugins.end())
			{
				// Create a new entry for that type and fill it with the loaded plugins
				std::set<SharedPtr<IPlugin> > plugins(newlyLoadedPlugins.begin(), newlyLoadedPlugins.end());
				loadedPlugins.insert(std::make_pair(it->typeStr, plugins));
			}
			else
			{
				// else insert then into the existing entry
				pl_it->second.insert(newlyLoadedPlugins.begin(), newlyLoadedPlugins.end());
			}

			// Add an entry to the library <->plugin mapping for the loaded plugins
			libraryPluginMapping.insert(std::make_pair(Path(path).getFileName(),
										std::make_pair(lib, std::set<SharedPtr<IPlugin> >(newlyLoadedPlugins.begin(), newlyLoadedPlugins.end()))));
		}
	}
}

bool PluginManager::unloadPlugins(std::string path)
{
	ScopedWriteRWLock w(rwlock);
	LibraryPluginMapping::iterator it;
	path = Path(path).getFileName();

	// Find the loaded plugins for that library.
	it = libraryPluginMapping.find(path);
	if (it == libraryPluginMapping.end())
	{
		return true; // None loaded.
	}

	// pset = set of loaded plugins for the library
	const PluginSet& pset = it->second.second;
	PluginSet unloadable;
	std::set<std::string> typeSet;
	for (PluginSet::const_iterator ps_it = pset.begin(); ps_it != pset.end(); ps_it++)
	{
		typeSet.insert((*ps_it)->type());
		// if there are no indirect and no other direct references other than those from this class
		if ((*ps_it)->referenceCount() == 0 && ps_it->referenceCount() == 2)
		{
			// Then add the plugin to the set of unloadable plugins
			unloadable.insert(*ps_it);
		}
	}

	// Remove the unloadable plugins from the loadedPlugins mapping.
	for (std::set<std::string>::iterator ts_it = typeSet.begin(); ts_it != typeSet.end(); ts_it++)
	{
		PluginContainer::iterator ps_it = loadedPlugins.find(*ts_it);
		if (ps_it != loadedPlugins.end())
		{
			PluginSet& loadedOfType = ps_it->second;
			PluginSet temp;
			// temp = loaded - those to unload
			std::set_difference(loadedOfType.begin(),
								loadedOfType.end(),
								unloadable.begin(),
								unloadable.end(),
								std::inserter(temp,temp.begin()));
			loadedOfType = temp;
		}
	}

	// Now remove the unloadable plugins from the set of those loaded
	PluginSet temp; 
	std::set_difference(pset.begin(),
						pset.end(),
						unloadable.begin(),
						unloadable.end(),
						std::inserter(temp,temp.begin()));
	it->second.second = temp;

	// if all the plugins from this library were unloadable
	if (it->second.second.empty())
	{
		/* Delete the entry in the mapping.
		 * Since we're using smart pointers this should have the effect of unloading the
		 * library and destroying and freeing the plugins.
		 */
		libraryPluginMapping.erase(it);
		return true;
	}
	return false;
}

bool PluginManager::unloadAllPlugins()
{
	ScopedWriteRWLock w(rwlock);
	LibraryPluginMapping::iterator it;
	for (it = libraryPluginMapping.begin(); it != libraryPluginMapping.end(); it++)
	{
		// NOTE: Copy and pasted from above.
		// pset = set of loaded plugins for the library
		const PluginSet& pset = it->second.second;
		PluginSet unloadable;
		std::set<std::string> typeSet;
		for (PluginSet::const_iterator ps_it = pset.begin(); ps_it != pset.end(); ps_it++)
		{
			typeSet.insert((*ps_it)->type());
			// if there are no indirect and no other direct references other than those from this class
			if ((*ps_it)->referenceCount() == 0 && ps_it->referenceCount() == 2)
			{
				// Then add the plugin to the set of unloadable plugins
				unloadable.insert(*ps_it);
			}
		}

		// Remove the unloadable plugins from the loadedPlugins mapping.
		for (std::set<std::string>::iterator ts_it = typeSet.begin(); ts_it != typeSet.end(); ts_it++)
		{
			PluginContainer::iterator ps_it = loadedPlugins.find(*ts_it);
			if (ps_it != loadedPlugins.end())
			{
				PluginSet& loadedOfType = ps_it->second;
				PluginSet temp;
				// temp = loaded - those to unload
				std::set_difference(loadedOfType.begin(),
									loadedOfType.end(),
									unloadable.begin(),
									unloadable.end(),
									std::inserter(temp,temp.begin()));
				loadedOfType = temp;
			}
		}

		// Now remove the unloadable plugins from the set of those loaded
		PluginSet temp;
		std::set_difference(pset.begin(),
							pset.end(),
							unloadable.begin(),
							unloadable.end(),
							std::inserter(temp,temp.begin()));
		it->second.second = temp;
	}

	it = libraryPluginMapping.begin();
	bool allUnloaded = true;
	while (it != libraryPluginMapping.end())
	{
		LibraryPluginMapping::iterator current = it;
		++it;
		if (current->second.second.empty())
		{
			libraryPluginMapping.erase(current);
		}
		else
		{
			allUnloaded = false;
		}
	}
	return allUnloaded;
}

void PluginManager::forceUnloadAllPlugins()
{
	ScopedWriteRWLock w(rwlock);
	loadedPlugins.clear();
	libraryPluginMapping.clear();
}

int PluginManager::numPlugins() const
{
	ScopedReadRWLock w(rwlock);
	PluginContainer::const_iterator pcIt;
	int total = 0;
	for (pcIt = loadedPlugins.begin(); pcIt != loadedPlugins.end(); ++pcIt)
	{
		total += pcIt->second.size();
	}
	return total;
}

SharedPtr<IPlugin> PluginManager::getPlugin(int p)
{
	ScopedReadRWLock w(rwlock);
	PluginContainer::iterator pcIt;
	PluginSet::iterator psIt;
	for (pcIt = loadedPlugins.begin(); pcIt != loadedPlugins.end(); ++pcIt)
	{
		for (psIt = pcIt->second.begin(); psIt != pcIt->second.end(); ++psIt)
		{
			if (!--p)
				return *psIt;
		}
	}
	return SharedPtr<IPlugin>();
}

PluginManager::PluginSet PluginManager::getPluginsByType(std::string type)
{
	ScopedReadRWLock w(rwlock);
	PluginContainer::iterator it = loadedPlugins.find(type);
	if (it != loadedPlugins.end())
		return it->second;
	return PluginManager::PluginSet();
}

SharedPtr<IPlugin> PluginManager::getPluginByName(std::string name)
{
	ScopedReadRWLock w(rwlock);
	LibraryPluginMapping::iterator it;
	PluginSet::iterator pIt;
	for (it = libraryPluginMapping.begin(); it != libraryPluginMapping.end(); it++)
	{
		for (pIt = it->second.second.begin(); pIt != it->second.second.end(); pIt++)
		{
			if ((*pIt)->name() == name)
				return *pIt;
		}
	}
	return SharedPtr<IPlugin>();
}
