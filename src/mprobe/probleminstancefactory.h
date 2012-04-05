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


#ifndef PROBLEMINSTANCEFACTORY_H_
#define PROBLEMINSTANCEFACTORY_H_

#include <string>

class PluginManager;
class ProblemInstance;

class ProblemInstanceFactory
{
public:
	ProblemInstanceFactory(PluginManager*);

	bool compatibleFiles(int numFiles, const char** files);
	bool compatibleFiles(int plugin, int numFiles, const char** files);

	// Destroy instances with delete
	ProblemInstance* fromFiles(int numFiles, const char** files);
	ProblemInstance* fromFilesAndPlugin(int numFiles, const char** files, std::string pluginName);
	ProblemInstance* fromFilesAndPlugin(int numFiles, const char** files, int pluginName);
private:
	PluginManager* pman;
};

#endif // PROBLEMINSTANCEFACTORY_H_
