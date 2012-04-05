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

/** @file iplugin.h */

#ifndef IPLUGIN_H_
#define IPLUGIN_H_

#include <string>

class IPlugin
{
public:
	virtual ~IPlugin() {}
	virtual std::string name() const = 0; /**< UTF-8 encoded plugin name */
	virtual std::string type() const = 0;
	virtual int referenceCount() const = 0; /**< If zero then safe to unload */
};

#endif // IPLUGIN_H_
