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


#include "probleminstancefactory.h"

#include <limits>

#include "pluginmanager.h"
#include "probleminstance.h"
#include "readerplugin.h"

ProblemInstanceFactory::ProblemInstanceFactory(PluginManager* pm)
	: pman(pm)
{
}

bool ProblemInstanceFactory::compatibleFiles(int numFiles, const char** files)
{
	PluginManager::PluginSet plugins = pman->getPluginsByType(ReaderPlugin::PluginTypeStr);
	PluginManager::PluginSet::iterator ps_it;

	for (ps_it = plugins.begin(); ps_it != plugins.end(); ps_it++)
	{
		SharedPtr<ReaderPlugin> plugin = ps_it->cast<ReaderPlugin>();
		if (!plugin)
			continue;
		if (plugin->compatibleFiles(numFiles, files, 0) > 0)
			return true;
	}

	return false;
}

bool ProblemInstanceFactory::compatibleFiles(int plugin, int numFiles, const char** files)
{
	SharedPtr<ReaderPlugin> reader = pman->getPlugin(plugin).cast<ReaderPlugin>();
	int* compat = new int[numFiles];
	if (reader)
	{
		bool retVal = reader->compatibleFiles(numFiles, files, compat);
		delete [] compat;
		return retVal;
	}
	return false;
}


ProblemInstance* ProblemInstanceFactory::fromFiles(int numFiles, const char** files)
{
	PluginManager::PluginSet plugins = pman->getPluginsByType(ReaderPlugin::PluginTypeStr);
	PluginManager::PluginSet::iterator ps_it;
	ReaderPlugin* bestMatch = 0;
	int best = std::numeric_limits<int>::min();

	for (ps_it = plugins.begin(); ps_it != plugins.end(); ps_it++)
	{
		SharedPtr<ReaderPlugin> plugin = ps_it->cast<ReaderPlugin>();
		if (!plugin)
			continue;
		int current = plugin->compatibleFiles(numFiles, files, 0);
		if ((best < 0 && current > best) || (best > 0 && current < best))
		{
			bestMatch = plugin;
			best = current;
		}
		if (best == 0) // Perfect match
			break;
	}

	if (best >= 0)
		return bestMatch->read(numFiles, files);
	return 0;
}

ProblemInstance* ProblemInstanceFactory::fromFilesAndPlugin(int numFiles, const char** files, std::string pluginName)
{
	SharedPtr<IPlugin> plugin = pman->getPluginByName(pluginName);
	SharedPtr<ReaderPlugin> rplugin;

	if (plugin)
	{
		rplugin = plugin.cast<ReaderPlugin>();
		if (rplugin)
			return rplugin->read(numFiles, files);
	}
	return 0;
}
