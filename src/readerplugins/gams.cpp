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


#define RP_NOPREFIX
#include "readerplugininterface.h"

#include <limits>
#include <algorithm>
#include <cstring>
#include <string>
#include <iostream>
#include <set>
#include <vector>

#include <cassert>

#include <Poco/Path.h>
#include <Poco/File.h>
#include <Poco/FileStreamFactory.h>
#include <Poco/Environment.h>

#include <boost/filesystem.hpp>

#include <gmomco.hpp>
#include <gevmco.hpp>
#include <dctmco.hpp>
#include <gamsxco.hpp>
#include <optco.hpp>

using namespace GAMS;
using namespace boost::filesystem3;

class GInstance
{
public:
	GInstance() : removeTmpDir(false) {}
	~GInstance() { if (removeTmpDir) remove_all(tmpDir); }

	GMO gmo;

	boost::filesystem3::path tmpDir;
	bool removeTmpDir;

	void computePresences(); // Must be called once the GMO is set
	const std::vector<int>& getVarCP(int var) { return varCP[var]; }
	const std::vector<int>& getVarOP(int var) { return varOP[var]; }
	const std::vector<int>& getConstrP(int constr) { return constrP[constr]; }
	const std::vector<int>& getObjP(int obj) { return objP[obj]; }

private:
	std::vector<std::vector<int> > varCP, varOP, constrP, objP;
};

ReaderPluginDetails details = { "GAMS", "", 0, 0};

void getReaderPluginAPIVersion(int* major, int* minor)
{
	*major = READER_PLUGIN_API_VERSION_MAJOR;
	*minor = READER_PLUGIN_API_VERSION_MINOR;
	std::string msg, sysdir("blah");
	GMO gmo;
	gmo.Init(sysdir, msg);
}

ReaderPluginDetails* getReaderPluginDetails(void)
{
	return &details;
}

bool extCompare(const std::string& ext1, const char* ext2)
{
	std::string::const_iterator ext1it = ext1.begin();
	const char* ext2it = ext2;
	while (*ext2it && ext1it != ext1.end())
	{
		if (std::tolower(*ext1it) != std::tolower(*ext2it))
			return false;
		++ext2it;
		++ext1it;
	}
	if ((*ext2it && ext1it == ext1.end()) || (ext1it != ext1.end() && !*ext2it))
		return false;
	return true;
}

GInstance* createScratchFiles(const std::string& sysdir, const std::string& inputFile, std::string* controlFile)
{
	GInstance* inst = 0;
	std::string msg;
	GAMSX gsx;
	OPT options;

	try {

	if (!gsx.Init(sysdir, msg))
	{
		std::cerr << "Could not initialize gamsx object! " << msg << std::endl;
		return 0;
	}

	if (!options.Init(sysdir, msg))
	{
		std::cerr << "Could not initialize gams options object! " << msg << std::endl;
		return 0;
	}

	if (exists(boost::filesystem3::path(sysdir)/"optgams.def"))
	{
		if (options.ReadDefinition((boost::filesystem3::path(sysdir)/"optgams.def").string()))
		{
			/* Failed reading options file, not sure what this really means, treat as fatal for now.
			 * this is what the examples do... I think that it is probably not a fatal error
			 * but there is no documentation on whether the options object will be in an invalid state
			 * (though it shouldn't)
			 */
			return 0;
		}
	}

	/* For some reason the examples temporarily
	 * set this option to zero then restore it later.
	 * So we follow suite.
	 */
	int saveEOLOnly = options.EOLOnlySet(0);

	// Set our parameters
	std::string params;
	params = "I=" + inputFile;
	params += " lo=1"; // Log level
	// set the default solver for all problem types to be the converter (to convert to scratch files)
	params += " lp=convertd mip=convertd nlp=convertd"; // TODO: there are many more problem types to add here

	// Create a temporary directory to put the scratch files into
	// For security purposes, the exact name should have a random portion
	boost::filesystem3::path tmpDir;
	do {
		tmpDir = temp_directory_path()/unique_path("mprobe_gams_%%%%%%%%%%%%");
	} while (exists(tmpDir));

	create_directory(tmpDir);

	params += " scrdir=" + tmpDir.string();
	params += " scrext=scr"; // ensure uniform scr file naming across platforms/versions

	options.ReadFromStr(params); // Send our settings to the object
	options.EOLOnlySet(saveEOLOnly); // Restore this setting...
	options.SetStrStr("sysdir", sysdir);

	if (gsx.RunExecDLL(options.GetHandle(), sysdir, 0, msg))
	{
		std::cerr << "GAMS execution failed! " << msg << std::endl;
		remove_all(tmpDir);
		return 0;
	}

	inst = new GInstance;
	inst->tmpDir = tmpDir.generic_string();
	inst->removeTmpDir = true;

	if (exists(tmpDir/"gamscntr.scr"))
	{
		*controlFile = (tmpDir/"gamscntr.scr").string();
	}
	else // Should not happen, but just in case...
	{
		boost::filesystem3::directory_iterator dir_it(tmpDir);
		while (dir_it != directory_iterator())
		{
			if (dir_it->path().filename().string().find("cntr"))
			{
				*controlFile = dir_it->path().string();
				break;
			}
		}
		std::cerr << "Could not determine control file location!\n";
		delete inst;
		return 0;
	}

	return inst;

	} catch (...) {}
	if (inst)
		delete inst;
	return 0;
}

RPHandle createInstance(int numFiles, const char** files)
{
	using namespace Poco;
	std::string ext;
	std::string controlFile;
	std::string sysdir;
	int firstDir = -1;
	int firstModelFile = -1;
	bool scratchSupplied = true;
	bool haveEnvVar = false;

	try {
		boost::filesystem3::path gamsPath(Poco::Environment().get("GAMS"));
		if (is_directory(gamsPath))
		{
			boost::filesystem3::directory_iterator dir_it(gamsPath);
			bool haveExecutable = false;
			bool haveAPILib = false;
			while (dir_it != directory_iterator())
			{
				boost::filesystem3::path file = dir_it->path();
				std::string fileStr = file.filename().generic_string();
				std::transform(fileStr.begin(), fileStr.end(), fileStr.begin(), (int(*)(int)) std::tolower);
				if (fileStr.find("joatdc") != std::string::npos)
				{
					haveAPILib = true;
				}
				else if (fileStr.find("gams") && Poco::File(file.generic_string()).canExecute())
				{
					haveExecutable = true;
				}
				if (haveAPILib && haveExecutable)
				{
					break;
				}
				dir_it++;
			}
			sysdir = gamsPath.string();
			haveEnvVar = true;
		}
	} catch (...) {}

	for (int i = 0; i < numFiles; ++i)
	{
		ext = Path(files[i]).getExtension();
		if (extCompare(ext,"gdx") || extCompare(ext,"gms"))
		{
			// if ever given both scratch files
			// and gms/gdx files, use the latter
			if (firstModelFile == -1 || scratchSupplied)
			{
				firstModelFile = i;
			}
			scratchSupplied = false;
		}
		else if (extCompare(ext,"dat") || extCompare(ext,"scr"))
		{
			if (firstModelFile == -1)
			{
				firstModelFile = i;
			}

			if (controlFile.empty() && std::string(files[i]).find("cntr") != std::string::npos)
			{
				controlFile = files[i];
			}
		}
		else if (!haveEnvVar && firstDir == -1)
		{
			// We look for an included dir to be the GAMS path
			boost::filesystem3::path path(files[i]);
			if (boost::filesystem3::is_directory(path))
			{
				firstDir = i;
			}
		}
	}

	if (firstDir != -1)
	{
		sysdir = files[firstDir];
	}

	if (sysdir.empty() // GAMS location not found
		|| (scratchSupplied && firstModelFile == -1)) // No scratch files actually supplied
	{
		return RPNULL;
	}

	GInstance* inst;

	if (!scratchSupplied)
	{
		inst = createScratchFiles(sysdir, files[firstModelFile], &controlFile);
		if (!inst)
		{
			return RPNULL; // Could not create scratch files
		}
	}
	else
	{
		if (controlFile.empty())
		{
			controlFile = firstModelFile;
		}
		inst = new GInstance;
	}

	GMO* psGmo = &inst->gmo;
	std::string msg;

	if (!psGmo->Init(sysdir, msg))
	{
		std::cerr << "gmoInit failed! " << msg << std::endl;
		delete inst;
		return RPNULL;
	}

	GEV* psGev = new GEV;
	if (!psGev->Init(sysdir, msg))
	{
		std::cerr << "gevInit failed!" << msg << std::endl;
		delete inst;
		delete psGev;
		return RPNULL;
	}

	if (psGev->InitEnvironmentLegacy(controlFile))
	{
		std::cerr << "gevInitEnvironmentLegacy failed!\n";
		std::cerr << "\tThis operation could have failed due to control file issues or library path/linker problems.\n";
		delete inst;
		delete psGev;
		return RPNULL;
	}

	psGmo->RegisterEnvironment(psGev->GetHandle(), msg);

	if (psGmo->LoadDataLegacy(msg))
	{
		std::cerr << "gmoLoadDataLegacy failed! " << msg << std::endl;
		delete inst;
		delete psGev;
		return RPNULL;
	}

	if (psGmo->IndexBase() != 0)
		psGmo->IndexBaseSet(0);

	psGmo->CompleteData(msg);
	if (!msg.empty())
		std::cerr << msg << std::endl;

	psGmo->ObjStyleSet(2);
	inst->computePresences();
	return inst;
}

int compatibleFiles(int numFiles, const char** files, int* compat)
{
	int compatibleCount = 0;
	int i;

	for (i = 0; i < numFiles; i++)
	{
		std::string ext = Poco::Path(files[i]).getExtension();
		if (extCompare(ext,"dat") || extCompare(ext,"scr") // Scratch files supplied
			|| extCompare(ext,"gms") || extCompare(ext,"gdx")) // gams input files supplied
		{
			compatibleCount++;
			if (compat)
				compat[i] = 0;
		}
		else if (is_directory(boost::filesystem3::path(files[i]))) // GAMS dir (hopefully)
		{
			if (compat)
				compat[i] = 0;
		}
		else if (compat) // else unrecognized
		{
			compat[i] = 1;
		}
	}

	return compatibleCount? 0:-1;
}

void releaseInstance(RPHandle h)
{
}

void releaseName(RPHandle, const char* n)
{
	if (n)
		delete[] n;
}

const char* instanceName(RPHandle h)
{
	char* name = new char[GMS_SSSIZE];
	gmoNameModel(((GInstance*)h)->gmo.GetHandle(), name);
	return name;
}

int instanceNameToBuf(RPHandle h, int maxLen, char* buf)
{
	//TODO
	return 0;
}

int variables(RPHandle h)
{
	return ((GInstance*)h)->gmo.N();
}

const char* variableName(RPHandle h, int v)
{
	gmoHandle_t gmo = ((GInstance*)h)->gmo.GetHandle();
	char* name = new char[GMS_SSSIZE];
	name[0] = '\0';

	if (gmoDict(gmo))
	{
		gmoGetVarNameOne(gmo, v, name);
	}
	return name;
}

int variableNameToBuf(RPHandle h, int v, int maxLen, char* buf)
{
	// TODO
	return 0;
}

void variablePresence(RPHandle h, int var, int* constr, int* objs)
{
	GInstance* inst = ((GInstance*)h);

	if (!constr || !objs)
		return;

	const std::vector<int>& varcp = inst->getVarCP(var);
    const std::vector<int>& varop = inst->getVarOP(var);

	*constr++ = varcp.size();
	std::copy(varcp.begin(), varcp.end(), constr);

	*objs++ = varop.size();
	std::copy(varop.begin(), varop.end(), objs);
}

int variableType(RPHandle h, int v)
{
	int type = ((GInstance*)h)->gmo.GetVarTypeOne(v);
	switch (type) {
		case gmovar_X:
			return 'r';
		case gmovar_B:
			return 'b';
		case gmovar_I:
			return 'i';
		case gmovar_SC: // Semi continuous
			return 'r';
		case gmovar_SI: // Semi integer
			return 'i';
		case gmovar_S1: // special ordered set 1
		case gmovar_S2: // special ordered set 1
			return 0; // Don't know how to deal with these
	}
	return 0; // error
}

void variableBounds(RPHandle h, int v, Real* lb, Real* ub)
{
	*lb = ((GInstance*)h)->gmo.GetVarLowerOne(v);
	*ub = ((GInstance*)h)->gmo.GetVarUpperOne(v);
}

int objectives(RPHandle h)
{
	return 1;
}

const char* objectiveName(RPHandle h, int o)
{
	gmoHandle_t gmo = ((GInstance*)h)->gmo.GetHandle();
	char* name = new char[GMS_SSSIZE];
	name[0] = '\0';

	if (gmoDict(gmo))
	{
		gmoGetObjName(gmo, name);
	}
	return name;
}

int objectiveNameToBuf(RPHandle h, int o, int maxLen, char* buf)
{
	// TODO
	return 0;
}

int objectiveType(RPHandle h, int o)
{
	if (((GInstance*)h)->gmo.Sense() == gmoObj_Min)
	{
		return 'm';
	}
	else
	{
		return 'M';
	}
}

int objectiveVariables(RPHandle h, int obj, int* vars)
{
	GInstance* inst = ((GInstance*)h);

	if (!vars)
		return -1;

	const std::vector<int>& objp = inst->getObjP(obj);

	std::copy(objp.begin(), objp.end(), vars);
	return objp.size();
}

int evaluateObjectiveVal(RPHandle h, int obj, Real* pt, Real* result)
{
	int errors, retval;
	retval = ((GInstance*)h)->gmo.EvalFuncObj(pt, *result, errors);
	return !errors || retval;
}

int evaluateObjectiveGrad(RPHandle h, int obj, Real* pt, Real* result)
{
	int errors, retval;
	double fx, gx;
	retval = ((GInstance*)h)->gmo.EvalGradObj(pt, fx, result, gx, errors);
	return !errors || retval;
}

int constraints(RPHandle h)
{
	return ((GInstance*)h)->gmo.M();
}

const char* constraintName(RPHandle h, int c)
{
	gmoHandle_t gmo = ((GInstance*)h)->gmo.GetHandle();
	char* name = new char[GMS_SSSIZE];
	name[0] = '\0';

	if (gmoDict(gmo))
	{
		gmoGetEquNameOne(gmo, c, name);
	}
	return name;
}

int constraintNameToBuf(RPHandle h, int c, int maxLen, char* buf)
{
	//TODO
	return 0;
}

int constraintType(RPHandle h, int c)
{
	int type = ((GInstance*)h)->gmo.GetEquTypeOne(c);
	switch (type) {
		case gmoequ_E:
			return 'e';
		case gmoequ_G:
			return 'g';
		case gmoequ_L:
			return 'l';
		case gmoequ_N:
			return 'u';
		case gmoequ_B: // ??
		{
			std::string type;
			if (!((GInstance*)h)->gmo.GetEquTypeTxt(c, type))
				std::cout << "Found equation of type " << type << std::endl;
		}
		case gmoequ_X: // external
		case gmoequ_C: // conic
			return 0;
	}
	return 0; // error
}

void constraintBounds(RPHandle h, int c, Real* lb, Real* ub)
{
	int type = ((GInstance*)h)->gmo.GetEquTypeOne(c);
	double rhs = ((GInstance*)h)->gmo.GetRhsOne(c);
	switch (type) {
		case gmoequ_E:
			*lb = rhs;
			*ub = rhs;
			break;
		case gmoequ_G:
			*lb = rhs;
			*ub = ((GInstance*)h)->gmo.Pinf();
			break;
		case gmoequ_L:
			*lb = ((GInstance*)h)->gmo.Minf();
			*ub = rhs;
			break;
		default:
			*lb = ((GInstance*)h)->gmo.Minf();
			*ub = ((GInstance*)h)->gmo.Pinf();
	}
}

int constraintVariables(RPHandle h, int constr, int* vars)
{
	GInstance* inst = ((GInstance*)h);

	if (!vars)
		return -1;

	const std::vector<int>& constrp = inst->getConstrP(constr);

	std::copy(constrp.begin(), constrp.end(), vars);
	return constrp.size();
}

int evaluateConstraintVal(RPHandle h, int constr, Real* pt, Real* result)
{
	int errors, retval;
	retval = ((GInstance*)h)->gmo.EvalFunc(constr, pt, *result, errors);
	return !errors || retval;
}

int evaluateConstraintGrad(RPHandle h, int constr, Real* pt, Real* result)
{
	int errors, retval;
	double fx, gx;
	retval = ((GInstance*)h)->gmo.EvalGrad(constr, pt, fx, result, gx, errors);
	return !errors || retval;
}

int functionType(RPHandle h, int type, int which)
{
	int order;
	if (type == 'o')
	{
		order = ((GInstance*)h)->gmo.GetObjOrder();
	}
	else
	{
		order = ((GInstance*)h)->gmo.GetEquOrderOne(which);
	}
	switch (order)
	{
		case gmoorder_L:
			return 'l';
		case gmoorder_Q:
			return 'q';
		case gmoorder_NL:
			return 'n';
	}
	return 0; // error
}

void GInstance::computePresences()
{
	// The only presences which cannot be directly found are the constraint ones, while constructing it we want to
	// remove duplicates.
	std::vector<std::set<int> > constrP_tmp;

	constrP_tmp.assign(gmo.M(), std::set<int>());
	varCP.assign(gmo.N(), std::vector<int>());
	varOP.assign(gmo.N(), std::vector<int>());
	objP.assign(1, std::vector<int>());

	const int maxNZ = std::max(gmo.NZ(), gmo.ObjNZ());
	int* colStart = new int[gmo.N()+1];
	int* rowIdx = new int[maxNZ];
	int* nlflag = new int[maxNZ];
	double* jacval = new double[maxNZ];

	if (gmo.GetMatrixCol(colStart, rowIdx, jacval, nlflag))
	{
		std::cerr << "gmo.GetMatrixCol signaled an error!\n\tPresence data will be conservative!\n";

		for (int i = 0; i < gmo.N(); ++i)
		{
			varCP[i].clear();
			varCP[i].reserve(gmo.M());
			for (int j = 0; i < gmo.M(); ++j)
			{
				varCP[i].push_back(j);
			}
		}
		for (int i = 0; i < gmo.M(); ++i)
		{
			constrP[i].clear();
			constrP[i].reserve(gmo.N());
			for (int j = 0; i < gmo.N(); ++j)
			{
				constrP[i].push_back(j);
			}
		}
	}
	else
	{
		for (int var = 0; var < gmo.N(); ++var)
		{
			varCP[var].clear();
			varCP[var].reserve(colStart[var+1]-colStart[var]);
			int nextStart = colStart[var+1];
			int i = colStart[var];
			while (i < nextStart)
			{
				varCP[var].push_back(rowIdx[i]);
				constrP_tmp[rowIdx[i]].insert(var);
				++i;
			}
		}
		constrP.assign(constrP_tmp.size(), std::vector<int>());
		for (int i = 0; i < gmo.M(); ++i)
		{
			constrP[i].assign(constrP_tmp[i].begin(), constrP_tmp[i].end());
		}
	}

	int nz, nlz;
	std::fill(colStart, &colStart[gmo.N()+1], 0);
	if (gmo.GetObjSparse(colStart, jacval, nlflag, nz, nlz))
	{
		std::cerr << "gmo.GetObjSparse signaled an error!\n\tPresence data will be conservative!\n";

		objP[0].reserve(gmo.N());
		for (int i = 0; i < gmo.N(); ++i)
		{
			objP[0].push_back(i);
		}
	}
	else
	{
		objP[0].assign(&colStart[0], &colStart[gmo.ObjNZ()]);
		for (int i = 0; i < gmo.ObjNZ(); ++i)
		{
			varOP[colStart[i]].push_back(0);
		}
	}

	delete [] colStart;
	delete [] rowIdx;
	delete [] nlflag;
	delete [] jacval;
}
