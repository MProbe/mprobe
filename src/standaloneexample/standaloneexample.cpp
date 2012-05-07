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

#include <mprobe.h>
#include <iostream>

std::string stripCRLF(std::string str)
{
	size_t firstCRLF = str.find_first_of('\r');
	while (firstCRLF != std::string::npos)
	{
		str = str.erase(firstCRLF, 1);
		firstCRLF = str.find_first_of('\r');
	}
	firstCRLF = str.find_first_of('\n');
	while (firstCRLF != std::string::npos)
	{
		str = str.erase(firstCRLF, 1);
		firstCRLF = str.find_first_of('\n');
	}
	return str;
}

std::string empShapeToStr(uint8_t empShape)
{
	switch (empShape) {
		case ESShapeError:
			return "Error";
		case ESTooManyMathErrors:
			return "MathErrs";
		case ESNotAvailable:
			return "NotAvail";
		case ESLinear:
			return "Linear";
		case ESAlmostLinBoth:
			return "AlmostLinear(Both)";
		case ESAlmostConcave:
			return "AlmostConcave";
		case ESAlmostConvex:
			return "AlmostConvex";
		case ESConvex:
			return "Convex";
		case ESConcave:
			return "Concave";
		case ESBothConc:
			return "Conc.&Conv.";
	}
	return "";
}

int main(int argc, const char** argv)
{
	if (argc < 3)
	{
		std::cerr << "Usage: " << argv[0] << "pluginfile modelfiles...\n";
		return 0;
	}

	mprobe_init();

	mp_loadPlugins(argv[1]);

	if (mp_loadedPlugins() <= 0)
	{
		std::cerr << "No plugins loaded from file: " << argv[1];
		std::cerr << "\n Exiting...\n";
		mprobe_deinit();
		return 1;
	}
	else
	{
		std::cout << "Loaded " << mp_loadedPlugins() << " plugins.\n\n";
	}

	MPHandle model = mp_load( argc - 2, argv + 2);

	if (model == MPNULL)
	{
		std::cerr << "Failed to load model from files:\n";
		for (int i = 0; i < argc - 2; ++i)
		{
			std::cerr << "\t" << argv[2 + i];
		}
		std::cerr << "\n Exiting...\n";
		mprobe_deinit();
		return 2;
	}

	const char* name = mp_instanceName(model);
	std::cout << "Loaded model: " << name << std::endl;
	mp_releaseName(model, name);

	int totvars;
	int rvars = 0, ivars = 0, bvars = 0;

	totvars = mp_variables(model);
	for (int i = 0; i < totvars; i++)
	{
	switch (mp_variableType(model, i))
	{
	case 'r':
		rvars++;
		break;
	case 'b':
		bvars++;
		break;
	case 'i':
		ivars++;
		break;
	}
	}

	std::cout << "Variables: " << totvars << std::endl;
	std::cout << "\tReal: " << rvars << std::endl;
	std::cout << "\tInteger: " << ivars << std::endl;
	std::cout << "\tBinary: " << bvars << std::endl;

	int totobjs = 0, linobjs = 0, quadobjs = 0, nonlinobjs = 0;
	int nzinobjs = 0;
	int* presences = new int[totvars];

	totobjs = mp_objectives(model);
	for (int i = 0; i < totobjs; i++)
	{
		switch (mp_functionType(model, 'o', i))
		{
			case 'l':
				linobjs++;
				break;
			case 'q':
				quadobjs++;
				break;
			case 'n':
				nonlinobjs++;
				break;
		}
		nzinobjs += mp_objectiveVariables(model, i, presences);
	}

	std::cout << "Objectives: " << totobjs << std::endl;
	std::cout << "\tLinear: " << linobjs << std::endl;
	std::cout << "\tQuadratic: " << quadobjs << std::endl;
	std::cout << "\tNonlinear: " << nonlinobjs << std::endl;

	int totconst = 0, linconst = 0, quadconst = 0, nonlinconst = 0;
	int linrng = 0, lineq = 0, linineq = 0;
	int quadrng = 0, quadeq = 0, quadineq = 0;
	int nonlinrng = 0, nonlineq = 0, nonlinineq = 0;
	int nzsinconstrs = 0;

	totconst = mp_constraints(model);
	for (int i = 0; i < totconst; i++)
	{
		int ftype = mp_functionType(model, 'c', i);
		int constrType = mp_constraintType(model, i);

		if (ftype == 'l' && constrType == 'r')
			linconst++, linrng++;
		else if (ftype == 'l' && constrType == 'e')
			linconst++, lineq++;
		else if (ftype == 'l' && constrType != 'u')
			linconst++, linineq++;
		else if (ftype == 'q' && constrType == 'r')
			quadconst++, quadrng++;
		else if (ftype == 'q' && constrType == 'e')
			quadconst++, quadeq++;
		else if (ftype == 'q' && constrType != 'u')
			quadconst++, quadineq++;
		else if (ftype == 'n' && constrType == 'r')
			nonlinconst++, nonlinrng++;
		else if (ftype == 'n' && constrType == 'e')
			nonlinconst++, nonlineq++;
		else if (ftype == 'n' && constrType != 'u')
			nonlinconst++, nonlinineq++;

		nzsinconstrs += mp_constraintVariables(model, i, presences);
	}

	std::cout << "Constraints: " << totconst << std::endl;
	std::cout << "\tLinear: " << linconst << std::endl;
	std::cout << "\t\tinequalities: " << linineq << std::endl;
	std::cout << "\t\tranges: " << linrng << std::endl;
	std::cout << "\t\tequalites: " << lineq << std::endl;
	std::cout << "\tQuadratic: " << quadconst << std::endl;
	std::cout << "\t\tinequalities: " << quadineq << std::endl;
	std::cout << "\t\tranges: " << quadrng << std::endl;
	std::cout << "\t\tequalites: " << quadeq << std::endl;
	std::cout << "\tNonlinear: " << nonlinconst << std::endl;
	std::cout << "\t\tinequalities: " << nonlinineq << std::endl;
	std::cout << "\t\tranges: " << nonlinrng << std::endl;
	std::cout << "\t\tequalites: " << nonlineq << std::endl;

	std::cout << "Non-zeros:\n";
	std::cout << "\tIn objectives: " << nzinobjs << std::endl;
	std::cout << "\tIn constraints: " << nzsinconstrs << std::endl;

	std::cout << "\nPresences:\n";

	for (int i = 0; i < totconst; ++i)
	{
		const int numVarPerLine = 4;
		int j;
		int varsInConstr = mp_constraintVariables(model, i, presences);
		
		name = mp_constraintName(model, i);

		std::string tmp1 = stripCRLF(name);
		mp_releaseName(model, name);

		std::cout << "Constraint " << tmp1 << " uses variables: \n";

		for (j = 0; j < varsInConstr; ++j)
		{
			if (j % numVarPerLine == 0)
				 std::cout << '\t';

			name = mp_variableName(model, presences[j]);
			std::string tmp2 = stripCRLF(name);
			mp_releaseName(model, name);

			std::cout << tmp2;

			if (j + 1 < varsInConstr)
			{
				if (j % numVarPerLine < numVarPerLine - 1)
				{
					std::cout << ", ";
				}
				else
				{
					std::cout << '\n';
				}
			}
		}
		std::cout << '\n';
	}
	for (int i = 0; i < totobjs; ++i)
	{
		const int numVarPerLine = 4;
		int j;
		int varsInObjs = mp_objectiveVariables(model, i, presences);

		name = mp_objectiveName(model, i);

		std::string tmp1 = stripCRLF(name);
		mp_releaseName(model, name);

		std::cout << "Objective " << tmp1 << " uses variables: \n";

		for (j = 0; j < varsInObjs; ++j)
		{
			if (j % numVarPerLine == 0)
				 std::cout << '\t';

			name = mp_variableName(model, presences[j]);
			std::string tmp2 = stripCRLF(name);
			mp_releaseName(model, name);

			std::cout << tmp2;

			if (j + 1 < varsInObjs)
			{
				if (j % numVarPerLine < numVarPerLine - 1)
				{
					std::cout << ", ";
				}
				else
				{
					std::cout << '\n';
				}
			}
		}
		std::cout << '\n';
	}

	delete [] presences;
	MPHandle analysis = mp_createAnalysis(model);

	std::cout << "\nBeginning automated analysis:\n";
	std::cout << "\nUsing default settings:\n";
	std::cout << "Line length maximum: " << mp_aLineLengthMax(analysis) << std::endl;
	std::cout << "Line length minimum: " << mp_aLineLengthMin(analysis) << std::endl;
	std::cout << "Snapping discrete components: " << (mp_aSnapDiscreteComponents(analysis) ? "true": "false") << std::endl;
	std::cout << "Number of line segments: " << mp_aNumLineSegments(analysis) << std::endl;
	std::cout << "Interior line points: " << mp_aInteriorLinePoints(analysis) << std::endl;
	std::cout << "Minimum points needed for conclusions: " << mp_aMinimumPointsNeeded(analysis) << std::endl;
	std::cout << "Evaluation error tolerance: " << mp_aEvalErrorTolerance(analysis) << std::endl;
	std::cout << "Infinity: " << mp_aInfinity(analysis) << std::endl;
	std::cout << "Equality tolerance: " << mp_aEqualityTolerance(analysis) << std::endl;
	std::cout << "Almost equal tolerance: " << mp_aAlmostEqualTolerance(analysis) << std::endl;

	for (int i = 0; i < totconst; ++i)
	{
		mp_aVariableBoundLineSample(analysis, 'c', i, false);
	}
	for (int i = 0; i < totobjs; ++i)
	{
		mp_aVariableBoundLineSample(analysis, 'o', i, false);
	}

	std::cout << "\nAnalysis results:\n";
	std::cout << "Constraints:\n";
	std::cout << "Name\t" << "Emp.Shape\t" << "Reg.Effect\t" << "Tot.Eff\t" << "LB.Eff\t" << "UB.Eff\n";

	for (int i = 0; i < totconst; ++i)
	{
		Real lbEff, ubEff;

		name = mp_constraintName(model, i);
		std::cout << stripCRLF(name) << '\t';
		mp_releaseName(model, name);
		
		std::cout << empShapeToStr(mp_aGetEmpiricalShape(analysis, i, 'c')) << "\t";

		switch(mp_aGetRegionEffect(analysis, i))
		{
			case REConvex:
				std::cout << "Convex\t"; break;
			case RENonconvex:
				std::cout << "Nonconvex\t"; break;
			case REAlmost:
				std::cout << "Almost\t"; break;
		}

		mp_aEffectiveness(analysis, i, &lbEff, &ubEff);
		std::cout << lbEff + ubEff << "\t" << lbEff << "\t" << ubEff << "\n";
	}

	std::cout << "Objectives:\n";
	std::cout << "Name\t" << "Emp.Shape\t" << "BestPt.\t" << "Opt.Eff\n";
	for (int i = 0; i < totobjs; ++i)
	{
		Real extremum;

		name = mp_objectiveName(model, i);
		std::cout << stripCRLF(name) << '\t';
		mp_releaseName(model, name);

		std::cout << empShapeToStr(mp_aGetEmpiricalShape(analysis, i, 'o')) << "\t";

		mp_aExtremum(analysis, i, &extremum);
		std::cout << extremum << "\t";

		switch (mp_aGetOptimumEffect(analysis, i)) {
			case ObjectiveLocal:
				std::cout << "Local"; break;
			case ObjectiveGlobal:
				std::cout << "Global"; break;
			case ObjectiveAlmostGlobal:
				std::cout << "AlmostGlobal"; break;
		}
		std::cout << std::endl;
	}

	mp_releaseAnalysis(analysis);
	mp_unload(model);
	mprobe_deinit();
	return 0;
}