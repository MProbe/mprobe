package controller;

import javax.swing.SwingUtilities;

import misc.SetCentral;
import model.AnalysisInstance;
import model.PersistentSettings;
import viewer.Settings;
import viewer.StatisticsDialog;

public class SettingsControl implements ICloseableControl {

	private Settings window;
	private AnalysisInstance aModel;
	private PersistentSettings settings;

	public SettingsControl(PersistentSettings settings, AnalysisInstance aInst) {
		aModel = aInst;
		this.settings = settings;

		final SettingsControl tmp = this;
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				window = new Settings(tmp, tmp.settings, aModel);
				SetCentral.setCentral(window);
				window.setVisible(true);
			}});
	}
	
	@Override
	public void close() {
		if (window != null) {
			window.dispose();
			window = null;
		}
	}

}
