package controller;

import javax.swing.SwingUtilities;

import misc.SetCentral;
import model.ProblemInstance;
import viewer.StatisticsDialog;
import viewer.VariableWin;

public class StatisticsControl implements ICloseableControl {
	ProblemInstance model;
	StatisticsDialog statsDialog;

	public StatisticsControl(ProblemInstance inst) {
		model = inst;

		final StatisticsControl tmp = this;
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				statsDialog = new StatisticsDialog(tmp, model, false);
				SetCentral.setCentral(statsDialog);
				statsDialog.setVisible(true);
			}});
	}
	
	public void close() {
		if (statsDialog != null)
		{
			statsDialog.dispose();
			statsDialog = null;
		}
	}
}
