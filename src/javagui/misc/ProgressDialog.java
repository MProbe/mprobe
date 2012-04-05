package misc;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box.Filler;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;


public class ProgressDialog extends JDialog implements ActionListener, PropertyChangeListener, WindowListener {

	private JProgressBar progressBar;
	private JLabel statusLabel;
	
	private boolean statusIsState;
	private boolean disposeOnFinish;
	private boolean timerStarted;
	private int disposeDelayMS;
	private SwingWorker future;
	
	private Filler filler1;
    private Filler filler2;
    private Filler filler3;
    private Filler filler4;

	public ProgressDialog(SwingWorker worker, String title)
	{
		setLayout(new GridLayout(2,1));

		setTitle(title);
		addWindowListener(this);

		timerStarted = false;
		disposeOnFinish = true;
		disposeDelayMS = 1000;
		
		future = worker;
		future.addPropertyChangeListener(this);	

		statusLabel = new JLabel();
		setStatusToState();

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(worker.getProgress());
		progressBar.setStringPainted(true);

		initComponents();
		setVisible(true);
	}
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 0), new java.awt.Dimension(40, 32767));
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 40));
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(32767, 40));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        getContentPane().add(statusLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        getContentPane().add(progressBar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        getContentPane().add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        getContentPane().add(filler2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        getContentPane().add(filler3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        getContentPane().add(filler4, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
	public boolean disposeOnFinish() {
		return disposeOnFinish;
	}

	public void setDisposeOnFinish(boolean disposeOnFinish) {
		this.disposeOnFinish = disposeOnFinish;
	}

	public int getDisposeDelayMS() {
		return disposeDelayMS;
	}

	public void setDisposeDelayMS(int disposeDelayMS) {
		this.disposeDelayMS = disposeDelayMS;
	}

	private void setStatusToState()
	{
		statusIsState = true;
		switch (future.getState()) {
		case PENDING:
			statusLabel.setText("Pending...");
			break;
		case STARTED:
			statusLabel.setText("Started...");
			break;
		case DONE:
			statusLabel.setText("Done.");
			break;
		}
		if (future.isCancelled())
		{
			if (!future.isDone())
			{
				statusLabel.setText("Cancelling...");
			}
			else
			{
				statusLabel.setText("Cancelled.");
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress"))
		{
			progressBar.setValue(future.getProgress());
		}
		else if (evt.getPropertyName().equals("state"))
		{
			if (statusIsState || future.getState() == SwingWorker.StateValue.DONE)
				setStatusToState();			
		}
		else if (evt.getPropertyName().equals("currentAction"))
		{
			statusIsState = false;
			statusLabel.setText(evt.getNewValue().toString());
		}

		if (disposeOnFinish && future.isDone())
		{
			if (disposeDelayMS > 0)
			{
				if (!timerStarted)
				{
					timerStarted = true;
					Timer timer = new Timer(disposeDelayMS, this);
					timer.setRepeats(false);
					timer.start();
				}
			}
			else
			{
				dispose();
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof Timer)
		{
			Timer timer = (Timer)e.getSource();
			if (timerStarted && disposeOnFinish)
			{
				dispose();
				timer.stop();
				timer.removeActionListener(this);
			}
		}
	}
	@Override
	public void windowActivated(WindowEvent arg0) {
	}
	@Override
	public void windowClosed(WindowEvent arg0) {
	}
	
	@Override
	public void windowClosing(WindowEvent arg0) {
		future.cancel(false);
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}
	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}
	@Override
	public void windowIconified(WindowEvent arg0) {
	}
	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}
