package controller;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import misc.SetCentral;
import model.AnalysisInstance;
import model.MProbeLib;
import model.PersistentSettings;
import model.PluginSet;
import model.ProblemInstance;
import viewer.FilePath;
import viewer.MainWin;
import adapter.PluginTableModel;

public class MProbeController implements Observer {
	private ProblemInstance pModel;
	private AnalysisInstance aModel;
	private MainWin mainWin;
	private PersistentSettings persistentSettings;
	private PluginSet pluginSet;
	private FilePath fp;
	private LinkedList<ICloseableControl> controls;
	private LinkedList<VariableControl> varControls;
	private LinkedList<ObjectiveControl> objControls;
	private LinkedList<ConstraintControl> constrControls;

	public MProbeController() {
		controls = new LinkedList<ICloseableControl>();
		varControls = new LinkedList<VariableControl>();
		objControls = new LinkedList<ObjectiveControl>();
		constrControls = new LinkedList<ConstraintControl>();
		
		pModel = new ProblemInstance();
		pModel.addObserver(this);
		
		aModel = new AnalysisInstance();
		aModel.addObserver(this);

		pluginSet = new PluginSet();
		
		persistentSettings = new PersistentSettings();
		loadPersistentSettings();

		String pluginsSetting = persistentSettings.getSetting("plugins");
		String[] plugins = null;
		if (pluginsSetting != null)
		{
			plugins = pluginsSetting.split(File.pathSeparator);
		}
		else
		{
			plugins = new String[]{""};
		}

		for (String plugin: plugins)
		{
			if (!plugin.isEmpty())
				pluginSet.addPlugin(plugin);
		}

		try {
			SwingUtilities.invokeAndWait(new guiInit(this));
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
		}
	}

	public synchronized void shutdown() {
		varControls.clear();
		objControls.clear();
		constrControls.clear();

		for (ICloseableControl control : controls)
			control.close();

		if (mainWin != null)
			mainWin.dispose();
		if (fp != null)
			fp.dispose();

		if (aModel.loaded())
			aModel.unload();
		aModel = null;

		if (pModel.loaded())
			pModel.unload();
		pModel = null;
		
		String pluginSetting = new String();
		String[] plugins = pluginSet.getPlugins();
		
		for (String plugin: plugins)
		{
			if (!pluginSetting.isEmpty())
			{
				pluginSetting += File.pathSeparator;
			}
			pluginSetting += plugin;
		}
		persistentSettings.setSetting("plugins", pluginSetting);
		
		savePersistentmSettings();
	}
	
	class guiInit implements Runnable {
		MProbeController control;
		guiInit(MProbeController ctrl)
		{
			control = ctrl;
		}
		public void run() {
			PersistentSettings settings = control.getPersistentSettings();
			PluginTableModel pluginModel = new PluginTableModel(pluginSet);
			control.mainWin = new MainWin(control, pModel);
			control.fp = new FilePath(control, pluginModel, settings.getSetting("lastpath"));
			SetCentral.setCentral(mainWin);
		}
	}
	
	public void openFilepath() {
		fp.setVisible(true);
		fp.setLocationRelativeTo(mainWin);
	}

	public synchronized void loadFiles() {

		if (pModel.loaded()) {
			pModel.unload();
		}

		try {
			String[] files = fp.getSrcfiles();
			if (files == null )
			{
				files = new String[0];
			}
			pModel.load(files);
			aModel.load(pModel);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
				    "Could not load instance.",
				    "Load error.",
				    JOptionPane.WARNING_MESSAGE);
		}
	}

	public void addPlugin(String plugin) {
		if (plugin == null || plugin.isEmpty())
			return;
		
		pluginSet.addPlugin(plugin);
	}

	public void removePlugin(String plugin) {
		if (plugin == null || plugin.isEmpty())
			return;

		pluginSet.removePlugin(plugin);
	}
	
	public void setLastPathUsed(String path)
	{
		PersistentSettings settings = getPersistentSettings();
		settings.setSetting("lastpath", path);
	}

	public void showStatisticSummary() {
		StatisticsControl c = new StatisticsControl(pModel);
		controls.add(c);
	}

	public void showConstraintWorkshop() {
		ConstraintControl c = new ConstraintControl(this , pModel, aModel);
		controls.add(c);
		constrControls.add(c);
	}

	public void showVariableWorkshop() {
		VariableControl c = new VariableControl(this, pModel, aModel);
		controls.add(c);
		varControls.add(c);
	}

	public void showObjectiveWorkshop() {
		ObjectiveControl c = new ObjectiveControl(this, pModel, aModel);
		controls.add(c);
		objControls.add(c);
	}

	public void showAnalysisSettings() {
		SettingsControl c = new SettingsControl(persistentSettings, aModel);
		controls.add(c);
	}
	
	public static void main(String[] arg) {

		try {
			System.loadLibrary("mprobe");
		}
		catch (UnsatisfiedLinkError e)
		{
			JOptionPane.showMessageDialog(null, e, "Error loading mprobe library.", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		MProbeLib.INSTANCE.mprobe_init();
		
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    @Override
		    public void run()
		    {
		    	MProbeLib.INSTANCE.mprobe_deinit();
		    }
		});
		
		@SuppressWarnings("unused")
		MProbeController mainCtrl = new MProbeController();
	}

	@Override
	public void update(Observable o, Object arg) {
	}

	//show the variables that are in the given constraint
	public void showVariablesFromConstraint(int constraint) {
		for (ICloseableControl control: controls)
		{
			if (control instanceof VariableControl)
			{
				VariableControl varCon = (VariableControl) control;
				varCon.showVariablesFromConstraint(constraint);
			}
		}
	}

	//show the variables that are in the given objective
	public void showVariablesFromObjective(int objective) {
		for (VariableControl control: varControls)
		{
			control.showVariablesFromObjective(objective);
		}
	}

	//show the objective that contains the given variable
	public void showObjectivesfromVariable (int variable){
		for (ObjectiveControl control: objControls)
		{
			control.showObjectivesfromVariable(variable);
		}
	}

	//show the constraints that contain the given variable
	public void showConstraintsfromVariable (int variable){
		for (ConstraintControl control: constrControls)
		{
			control.showConstraintsfromVariable(variable);
		}
	}
	
	private void loadPersistentSettings()
	{
		File settingsFile = getPersistentSettingsFile();
		if (settingsFile == null)
		{
			JOptionPane.showMessageDialog(
					null,
					"Could not load settings.", "Error loading settings.", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			persistentSettings.load(new BufferedInputStream(new FileInputStream(settingsFile)));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					e, "Error saving settings.", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void savePersistentmSettings()
	{
		File settingsFile = getPersistentSettingsFile();
		
		
		
		if (settingsFile == null)
		{
			JOptionPane.showMessageDialog(
					null,
					"Could not save settings.", "Error saving settings.", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			persistentSettings.store(new BufferedOutputStream(new FileOutputStream(settingsFile)), null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					e, "Error saving settings.", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private File getPersistentSettingsFile()
	{
	    String userHome = System.getProperty("user.home");
	    if(userHome == null) {
	        JOptionPane.showMessageDialog(null, "Could not find user home directory for settings storage.");
	        return null;
	    }
	    File home = new File(userHome);
	    File settingsDirectory = new File(home, ".mprobe");
		if (!settingsDirectory.exists()) {
			if (!settingsDirectory.mkdir()) {
				JOptionPane.showMessageDialog(
						null,
						"Could not create settings directory: "
								+ settingsDirectory.getPath(),
						"Error creating directory.", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		File settingsFile = new File(settingsDirectory, "settings");
		if (!settingsFile.exists()) {
			try {
				if (!settingsFile.createNewFile()) {
					JOptionPane.showMessageDialog(
							null,
							"Could not create settings file: "
									+ settingsFile.getPath(),
							"Error creating file.", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(
						null, e, "Error creating file.", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return null;
			}
		}
	    return settingsFile;
	}
	
	public PersistentSettings getPersistentSettings()
	{
		return persistentSettings;
	}
	
	public String getTraceFilePath() {
		PersistentSettings settings = getPersistentSettings();
		String path = settings.getSetting("tracefile");
		if (path == null || path.isEmpty())
		{
			String userHome = System.getProperty("user.home");
			File home = new File(userHome);
		    File settingsDirectory = new File(home, ".mprobe");
		    path = settingsDirectory.getAbsolutePath();
		}
	
		File file = new File(path);
		if (file.isDirectory())
		{
			String filename = pModel.instanceName();
			filename = filename.replace('.', '_');
			if (filename.isEmpty())
			{
				filename = "trace";
			}
			path += File.separator + filename + ".txt";
		}
		return path;
	}

	public void constraintSelected(int constraint) {
		for (VariableControl control: varControls)
		{
			control.constraintSelected(constraint);
		}
	}
	
	public void objectiveSelected(int objective) {
		for (VariableControl control: varControls)
		{
			control.objectiveSelected(objective);
		}
	}
	
	public void variableSelected(int variable) {
		for (ObjectiveControl control: objControls)
		{
			control.variableSelected(variable);
		}
		for (ConstraintControl control: constrControls)
		{
			control.variableSelected(variable);
		}
	}
}
