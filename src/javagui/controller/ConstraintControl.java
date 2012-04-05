package controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.NumberFormatter;

import controller.MProbeController.guiInit;

import misc.ProgressDialog;
import misc.SetCentral;
import model.AnalysisInstance;
import model.ProblemInstance;
import viewer.ConstraintWin;
import viewer.UserHistograms;
import adapter.ConstraintTableModel;

public class ConstraintControl implements ICloseableControl {
	private ProblemInstance pModel;
	private AnalysisInstance aModel;
	private ConstraintWin window;
	private ConstraintTableModel tableModel;
	private LinkedList<UserHistograms> histWindows;
	private MProbeController mainControl;

	public ConstraintControl(MProbeController maincontrol, ProblemInstance pInst, AnalysisInstance aInst) {
		mainControl = maincontrol;
		pModel = pInst;
		aModel = aInst;
		tableModel = new ConstraintTableModel(pInst, aInst);

		histWindows = new LinkedList<UserHistograms>();
		
		final ConstraintControl tmp = this;
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				window = new ConstraintWin(tmp, tableModel);
				window.setVisible(true);
				SetCentral.setCentral(window);
			}});
	}

	public void close() {
		if (window != null) {
			window.dispose();
			window = null;
		}
		for (UserHistograms histWindow: histWindows)
		{
			if (histWindow != null)
			{
				histWindow.dispose();
				histWindow = null;
			}
		}
	}

	public void analyzeConstraint(final int selectedFunc, final boolean extraHists) {
		final String actionStr = "Analysing constraint " +  pModel.constraintName(selectedFunc); 
		try {
			final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				setProgress(50);
				firePropertyChange("currentAction", "", actionStr);
				aModel.variableBoundLineSample(ProblemInstance.FunctionType.Constraint, selectedFunc, extraHists);
				if (extraHists)
				{
					String histTitle = "Constraint " +  pModel.constraintName(selectedFunc);
					UserHistograms extraHistWin = new UserHistograms(aModel, histTitle);
					histWindows.add(extraHistWin);
					extraHistWin.setLocationRelativeTo(window);
					extraHistWin.setVisible(true);
				}
				setProgress(100);
				return null;
			}};
			
			ProgressDialog progress = new ProgressDialog(worker, actionStr);
			String closeDelay = mainControl.getPersistentSettings().getSetting("progressclosedelay");
			Integer delay = 1000;
			if (closeDelay != null)
			{
				try {
					delay = Integer.parseInt(closeDelay);
				} catch (NumberFormatException e) {}
			}
			progress.setDisposeDelayMS(delay);
			progress.setLocationRelativeTo(window);
			worker.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void analyzeConstraints(final int[] selected) {
		final String actionStr = "Analysing constraints"; 
		try {
			final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < selected.length; i++) {
					if (isCancelled())
						return null;
					setProgress((i+1)*100/(selected.length+1));
					firePropertyChange("currentAction", "", "Analysing constraint " +  pModel.constraintName(selected[i]));
					aModel.variableBoundLineSample(ProblemInstance.FunctionType.Constraint, selected[i], false);
				}
				setProgress(100);
				return null;
			}};
			
			ProgressDialog progress = new ProgressDialog(worker, actionStr);
			String closeDelay = mainControl.getPersistentSettings().getSetting("progressclosedelay");
			Integer delay = 1000;
			if (closeDelay != null)
			{
				try {
					delay = Integer.parseInt(closeDelay);
				} catch (NumberFormatException e) {}
			}
			progress.setDisposeDelayMS(delay);
			progress.setLocationRelativeTo(window);
			worker.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void analyzeAll() {
		try {
			final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				for (int i = 0; i < pModel.constraints(); i++) {
					if (isCancelled())
						return null;
					setProgress((i+1)*100/(pModel.constraints()+1));
					firePropertyChange("currentAction", "", "Analysing constraint " +  pModel.constraintName(i));
					aModel.variableBoundLineSample(ProblemInstance.FunctionType.Constraint, i, false);
				}
				setProgress(100);
				return null;
			}};
			
			ProgressDialog progress = new ProgressDialog(worker, "Analysing all constraints");
			String closeDelay = mainControl.getPersistentSettings().getSetting("progressclosedelay");
			Integer delay = 1000;
			if (closeDelay != null)
			{
				try {
					delay = Integer.parseInt(closeDelay);
				} catch (NumberFormatException e) {}
			}
			progress.setDisposeDelayMS(delay);
			progress.setLocationRelativeTo(window);
			worker.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showConstraintsfromVariable(int variable) {
		if (window == null)
			return;
		int[] tmp = new int[pModel.constraints() + 1];
		try {
			pModel.variablePresenceInConstraints(variable, tmp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] constraints = new int[tmp[0]];
		for (int i = 0; i < tmp[0]; i++)
			constraints[i] = tmp[i+1];
		window.showOnlySelectedConstraints(constraints);
	}
	
	public void showVariablesFromConstraint(int constraint) {
		mainControl.showVariablesFromConstraint(constraint);
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
			
			out.append("Constraint workshop trace:");
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

	public void constraintSelected(int constraint) {
		mainControl.constraintSelected(constraint);
	}

	public void variableSelected(int variable) {
		if (window != null)
			window.variableSelected(variable);
	}
}
