package model;

import java.io.File;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

public class PluginSet extends Observable {
	private Set<String> pluginNames;
	private Set<File> pluginPaths;
	
	public PluginSet()
	{
		pluginNames = new TreeSet<String>();
		pluginPaths = new TreeSet<File>();
	}
	
	public boolean addPlugin(String plugin)
	{
		File file =  new File(plugin);
		return addPlugin(file);
	}
	
	public boolean addPlugin(File plugin)
	{
		if (plugin == null || !plugin.exists() || plugin.isDirectory())
		{
			return false;
		}
		
		if (!pluginNames.add(plugin.getName()))
		{
			return true;
		}
		
		try {
			System.load(plugin.getAbsolutePath());
			MProbeLib.INSTANCE.mp_loadPlugins(plugin.getAbsolutePath());		
			pluginPaths.add(plugin);
			/*
			 *  Can't unload the library from the system, otherwise we'd do it here
			 *  We only do this to get good error messages for failed loads.
			 */
		} catch (UnsatisfiedLinkError e)
		{
			JOptionPane.showMessageDialog(null, "Error loading plugin:\n" +
												e.getMessage(),
												"Error loading plugin",
												JOptionPane.ERROR_MESSAGE);
			return false;
		}

		setChanged();
		notifyObservers();
		return true;
	}
	
	public boolean removePlugin(String plugin)
	{
		File file =  new File(plugin);
		return removePlugin(file);
	}
	
	public boolean removePlugin(File plugin)
	{
		if (plugin == null || !plugin.exists() || plugin.isDirectory())
		{
			return false;
		}
		
		if (!pluginNames.remove(plugin.getName()))
		{
			pluginPaths.remove(plugin); // Just in case
			return true;
		}
		pluginPaths.remove(plugin); // Just in case
		MProbeLib.INSTANCE.mp_unloadPlugins(plugin.getAbsolutePath());
		setChanged();
		notifyObservers();
		return true;
	}
	
	public String[] getPlugins()
	{
		String[] paths = new String[pluginPaths.size()];
		Iterator<File> fileIt = pluginPaths.iterator();
		for (int i = 0; i < pluginPaths.size(); ++i)
		{
			paths[i] = fileIt.next().getAbsolutePath();
		}
		return paths;
	}
}
