package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Observable;
import java.util.Properties;

public class PersistentSettings extends Observable {
	private Properties settings;

	public PersistentSettings()
	{
		settings = new Properties();
	}
	
	public String getSetting(String setting) {
		return settings.getProperty(setting);
	}

	public String getSetting(String setting, String defaultValue) {
		return settings.getProperty(setting, defaultValue);
	}

	public void load(InputStream inStream) throws IOException {
		settings.load(inStream);
		notifyObservers();
	}

	public void load(Reader reader) throws IOException {
		settings.load(reader);
		setChanged();
		notifyObservers();
	}

	public void store(OutputStream out, String comments) throws IOException {
		settings.store(out, null);
	}

	public void store(Writer writer, String comments) throws IOException {
		settings.store(writer, null);
	}
	
	public String setSetting(String setting, String value) {
		Object oldVal = settings.setProperty(setting, value);
		if (oldVal == null || oldVal != value)
		{
			setChanged();
			notifyObservers();
		}
		if (oldVal != null)
		{
			return oldVal.toString();
		}
		else
		{
			return null;
		}
	}
}
