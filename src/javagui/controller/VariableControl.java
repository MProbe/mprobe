package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import misc.SetCentral;
import model.AnalysisInstance;
import model.ProblemInstance;
import viewer.ObjectiveWin;
import viewer.VariableWin;
import adapter.VariableTableModel;

public class VariableControl implements ICloseableControl {
	private ProblemInstance pModel;
	private AnalysisInstance aModel;
	private VariableTableModel tableModel;
	private VariableWin window;
	private MProbeController mainControl;

	public VariableControl(MProbeController maincontrol, ProblemInstance pInst, AnalysisInstance aInst) {
		mainControl = maincontrol;
		pModel = pInst;
		aModel = aInst;
		tableModel = new VariableTableModel(pModel, aModel);
		
		final VariableControl tmp = this;
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				window = new VariableWin(tmp, tableModel);
				window.setVisible(true);
				SetCentral.setCentral(window);
			}});
	}
	public void close() {
		if (window != null) {
			window.dispose();
			window = null;
		}
	}
	public void showVariablesFromConstraint(int constraint) {
		if (window == null)
			return;
		int[] tmp = new int[pModel.variables()];
		int actualLength;
		try {
			actualLength = pModel.constraintVariables(constraint, tmp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		int[] variables = new int[actualLength];
		for (int i = 0; i < actualLength; i++)
			variables[i] = tmp[i];
		window.showOnlySelectedVariables(variables);
	}
	public void showVariablesFromObjective(int objective) {
		if (window == null)
			return;
		int[] tmp = new int[pModel.variables()];
		int actualLength;
		try {
			actualLength = pModel.objectiveVariables(objective, tmp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		int[] variables = new int[actualLength];
		for (int i = 0; i < actualLength; i++)
			variables[i] = tmp[i];
		window.showOnlySelectedVariables(variables);
	}
	
	public void showObjectivesfromVariable (int variable) {
		mainControl.showObjectivesfromVariable(variable);
	}

	public void showConstraintsfromVariable (int variable) {
		mainControl.showConstraintsfromVariable(variable);
	}
	
	public void resetTempbounds() {
		try {
			aModel.resetBounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void trace(int[] modelRowIndexes)
	{	
		File file = new File(mainControl.getTraceFilePath());
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),"UTF8"));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error opening to trace file", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			DecimalFormat doubleFormat = new DecimalFormat("0.###E0");
			NumberFormatter dFormatter = new NumberFormatter(doubleFormat);
			String separator = "\t";
			
			out.append("Variable table trace:");
			out.newLine();
			
			for (int i = 0; i < tableModel.getColumnCount(); ++i)
			{
				out.append(tableModel.getColumnName(i));
				out.append(separator);
			}
			out.newLine();
			
			
			for (int i = 0; i < modelRowIndexes.length; ++i)
			{
				for (int j = 0; j < tableModel.getColumnCount(); ++j)
				{
					Object value = tableModel.getValueAt(i, j);
					if (value.getClass() == Double.class)
					{
						try {
							out.append(dFormatter.valueToString(value));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					else
					{
						out.append(value.toString());
					}
					out.append(separator);
				}
				out.newLine();
			}
			
			out.newLine();
			out.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "Error writing to trace file", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	public void variableSelected(int variable) {
		mainControl.variableSelected(variable);
	}
	public void constraintSelected(int constraint) {
		if (window != null)
			window.constraintSelected(constraint);
	}
	public void objectiveSelected(int objective) {
		tableModel.setObjectiveForBestPoint(objective);
		if (window != null)
			window.objectiveSelected(objective);
	}
}
