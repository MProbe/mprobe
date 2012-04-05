package misc;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.concurrent.Callable;

public class SetCentral implements Callable<Void> {
	
	private Window window;
	
	SetCentral (Window window)
	{
		this.window = window;
	}
	
	static public void setCentral(Window window) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation((dim.width - window.getSize().width) / 2,
				(dim.height - window.getSize().height) / 2);
	}

	@Override
	public Void call() throws Exception {
		setCentral(window);
		return null;
	}
}
