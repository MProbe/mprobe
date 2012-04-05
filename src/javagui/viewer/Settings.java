package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import misc.DoubleVerifier;
import misc.SortedArrayListListModel;
import model.AnalysisInstance;
import model.MProbeLib.HistType;
import model.PersistentSettings;
import controller.SettingsControl;

public class Settings extends javax.swing.JFrame implements
		ActionListener, PropertyChangeListener, ChangeListener, WindowListener, Observer {

	private AnalysisInstance aModel;
	private SortedArrayListListModel<Double> uBoundModel;
	private SettingsControl control;
	private PersistentSettings settings;

	public Settings(SettingsControl ctrl, PersistentSettings settings, AnalysisInstance aInst) {
		super("Settings");

		control = ctrl;
    	aModel = aInst;
    	this.settings = settings;
    	
    	settings.addObserver(this);
    	aModel.addObserver(this);

    	initComponents();
 
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
 
		DefaultComboBoxModel histCBModel = new DefaultComboBoxModel();
		for (HistType ht: HistType.values())
		{
			histCBModel.addElement(ht);
		}
		histSelectionComboBox.setModel(histCBModel);		
		
		uBoundModel = new SortedArrayListListModel<Double>();
		uBoundsListWidget.setModel(uBoundModel);
		uBoundsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		DecimalFormat doubleFormat = new DecimalFormat("0.###E0");
		NumberFormatter formatter = new NumberFormatter(doubleFormat);
		formatter.setValueClass(Double.class);
		DefaultFormatterFactory formatterFact = new DefaultFormatterFactory(formatter);

		eqTolFField.setFormatterFactory(formatterFact);
		almostEqTolFField.setFormatterFactory(formatterFact);
		allowedEvalErrFField.setFormatterFactory(formatterFact);
		infinityFField.setFormatterFactory(formatterFact);
		lineLenMinFField.setFormatterFactory(formatterFact);
		lineLenMaxFField.setFormatterFactory(formatterFact);
		newBinUBoundFField.setFormatterFactory(formatterFact);
		
        newBinUBoundFField.setValue(0.1);
        update(aModel, null);
        
		snapPointsCheckBox.addActionListener(this);
		browseTraceDirButton.addActionListener(this);
		
        intPtsSpinner.addChangeListener(this);
        lineSegsSpinner.addChangeListener(this);
        minPtsReqSpinner.addChangeListener(this);
        progDelaySpinner.addChangeListener(this);

		eqTolFField.addPropertyChangeListener("value", this);
		infinityFField.addPropertyChangeListener("value", this);
		lineLenMaxFField.addPropertyChangeListener("value", this);
		lineLenMinFField.addPropertyChangeListener("value", this);
		almostEqTolFField.addPropertyChangeListener("value", this);
        allowedEvalErrFField.addPropertyChangeListener("value", this);

		newBinUBoundFField.addPropertyChangeListener("value", this);

        eqTolFField.setInputVerifier(new DoubleVerifier(){
        	@Override
        	public boolean and(Double d) {
        		if (d >= (Double)infinityFField.getValue())
        		{
        			lastInvalidityReason = "value must be less than infinity";
        			return false;
        		}
        		else if (d <= 0.0)
        		{
        			lastInvalidityReason = "value must be greater than zero";
        			return false;
        		}
        		return super.and(d);
        	}
        });

        almostEqTolFField.setInputVerifier(new DoubleVerifier(){
        	@Override
        	public boolean and(Double d) {
        		if (d <= (Double)eqTolFField.getValue())
        		{
        			lastInvalidityReason = "value must be greater than equality tolerance";
        			return false;
        		}
        		return super.and(d);
        	}
        });
        
        infinityFField.setInputVerifier(new DoubleVerifier(){
        	@Override
        	public boolean and(Double d) {
        		if (d <= (Double)almostEqTolFField.getValue())
        		{
        			lastInvalidityReason = "value must be greater than almost equality tolerance";
        			return false;
        		}
        		else if (d < (Double)lineLenMaxFField.getValue())
        		{
        			lastInvalidityReason = "value must be greater than or equal to line length max";
        			return false;
        		}
        		return super.and(d);
        	}
        });
        
        lineLenMinFField.setInputVerifier(new DoubleVerifier(){
        	@Override
        	public boolean and(Double d) {
        		if (d >= (Double)lineLenMaxFField.getValue())
        		{
        			lastInvalidityReason = "value must be less than line length max";
        			return false;
        		}
        		else if (d <= 0.0)
        		{
        			lastInvalidityReason = "value must be greater than zero";
        			return false;
        		}
        		return super.and(d);
        	}
        });
        
        lineLenMaxFField.setInputVerifier(new DoubleVerifier(){
        	@Override
        	public boolean and(Double d) {
        		if (d > (Double)infinityFField.getValue())
        		{
        			lastInvalidityReason = "value must be less than or equal to infinity";
        			return false;
        		}
        		else if (d <= (Double)lineLenMinFField.getValue())
        		{
        			lastInvalidityReason = "value must be greater than line length min";
        			return false;
        		}
        		return super.and(d);
        	}
        });
        
        allowedEvalErrFField.setInputVerifier(new DoubleVerifier(0.0, 1.0));
        
        newBinUBoundFField.setInputVerifier(new DoubleVerifier(-Double.MAX_VALUE, Double.MAX_VALUE));
    }

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedSettingsPanel = new javax.swing.JTabbedPane();
        analysisParametersPanel = new javax.swing.JPanel();
        eqTolLabel = new javax.swing.JLabel();
        almostEqTolLabel = new javax.swing.JLabel();
        lineSegsLabel = new javax.swing.JLabel();
        interiorPtsLabel = new javax.swing.JLabel();
        minPtsReqLabel = new javax.swing.JLabel();
        minPtsReqSpinner = new javax.swing.JSpinner();
        lineSegsSpinner = new javax.swing.JSpinner();
        intPtsSpinner = new javax.swing.JSpinner();
        lineLenMinLabel = new javax.swing.JLabel();
        lineLenMaxLabel = new javax.swing.JLabel();
        evalErrorFracLabel = new javax.swing.JLabel();
        snapPointsCheckBox = new javax.swing.JCheckBox();
        infinityLabel = new javax.swing.JLabel();
        eqTolFField = new javax.swing.JFormattedTextField();
        almostEqTolFField = new javax.swing.JFormattedTextField();
        infinityFField = new javax.swing.JFormattedTextField();
        allowedEvalErrFField = new javax.swing.JFormattedTextField();
        lineLenMinFField = new javax.swing.JFormattedTextField();
        lineLenMaxFField = new javax.swing.JFormattedTextField();
        histogramSettingsPanel = new javax.swing.JPanel();
        histSelectionComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        uBoundsListWidget = new javax.swing.JList();
        uBoundsLabel = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        newBinUBoundLabel = new javax.swing.JLabel();
        binAddButton = new javax.swing.JButton();
        removeBinButton = new javax.swing.JButton();
        newBinUBoundFField = new javax.swing.JFormattedTextField();
        miscSettingsPanel = new javax.swing.JPanel();
        traceDirLabel = new javax.swing.JLabel();
        traceDirField = new javax.swing.JTextField();
        browseTraceDirButton = new javax.swing.JButton();
        progDelayLabel = new javax.swing.JLabel();
        progDelaySpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        analysisParametersPanel.setLayout(new java.awt.GridBagLayout());

        eqTolLabel.setText("Equality Tolerance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(eqTolLabel, gridBagConstraints);

        almostEqTolLabel.setText("Almost Equality Tolerance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(almostEqTolLabel, gridBagConstraints);

        lineSegsLabel.setText("Line segments");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(lineSegsLabel, gridBagConstraints);

        interiorPtsLabel.setText("Interior points");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(interiorPtsLabel, gridBagConstraints);

        minPtsReqLabel.setText("Minimum points needed for conclusions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(minPtsReqLabel, gridBagConstraints);

        lineSegsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10000), Integer.valueOf(1), null, Integer.valueOf(1)));
        lineSegsSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(lineSegsSpinner, ""));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        analysisParametersPanel.add(lineSegsSpinner, gridBagConstraints);

        intPtsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(7), Integer.valueOf(1), null, Integer.valueOf(1)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        analysisParametersPanel.add(intPtsSpinner, gridBagConstraints);

        lineLenMinLabel.setText("Line length minimum");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(lineLenMinLabel, gridBagConstraints);

        lineLenMaxLabel.setText("Line length maximum");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(lineLenMaxLabel, gridBagConstraints);

        evalErrorFracLabel.setText("Allowed evaluation error fraction");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(evalErrorFracLabel, gridBagConstraints);

        minPtsReqSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(25), Integer.valueOf(1), null, Integer.valueOf(1)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        analysisParametersPanel.add(minPtsReqSpinner, gridBagConstraints);

		snapPointsCheckBox.setText("Snap points");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 9;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		analysisParametersPanel.add(snapPointsCheckBox, gridBagConstraints);

        infinityLabel.setText("Infinity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        analysisParametersPanel.add(infinityLabel, gridBagConstraints);

		
		eqTolFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		analysisParametersPanel.add(eqTolFField, gridBagConstraints);


		almostEqTolFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		analysisParametersPanel.add(almostEqTolFField, gridBagConstraints);


		infinityFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		analysisParametersPanel.add(infinityFField, gridBagConstraints);


		allowedEvalErrFField
				.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		analysisParametersPanel.add(allowedEvalErrFField, gridBagConstraints);


		lineLenMinFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		analysisParametersPanel.add(lineLenMinFField, gridBagConstraints);


		lineLenMaxFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		analysisParametersPanel.add(lineLenMaxFField, gridBagConstraints);

		tabbedSettingsPanel.addTab("Analysis Parameters", analysisParametersPanel);

		histogramSettingsPanel.setLayout(new java.awt.GridBagLayout());

		histSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Shape", "Slope", "Function Value",
						"Line Length" }));
		histSelectionComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						histSelectionComboBoxActionPerformed(evt);
					}
				});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		histogramSettingsPanel.add(histSelectionComboBox, gridBagConstraints);

		jScrollPane1.setViewportView(uBoundsListWidget);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		histogramSettingsPanel.add(jScrollPane1, gridBagConstraints);

		uBoundsLabel.setText("Upper Bounds");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		histogramSettingsPanel.add(uBoundsLabel, gridBagConstraints);

		resetButton.setText("Reset defaults");
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		histogramSettingsPanel.add(resetButton, gridBagConstraints);

		newBinUBoundLabel.setText("New bin upper bound");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		histogramSettingsPanel.add(newBinUBoundLabel, gridBagConstraints);

		binAddButton.setText("Add");
		binAddButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				binAddButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		histogramSettingsPanel.add(binAddButton, gridBagConstraints);

		removeBinButton.setText("Remove");
		removeBinButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				removeBinButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		histogramSettingsPanel.add(removeBinButton, gridBagConstraints);


		newBinUBoundFField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.8;
		histogramSettingsPanel.add(newBinUBoundFField, gridBagConstraints);

		tabbedSettingsPanel.addTab("Histograms", histogramSettingsPanel);

        miscSettingsPanel.setLayout(new java.awt.GridBagLayout());

        traceDirLabel.setText("Trace file directory:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 0.1;
        miscSettingsPanel.add(traceDirLabel, gridBagConstraints);

        traceDirField.setEditable(false);
        if (settings.getSetting("tracefile") != null)
        	traceDirField.setText(settings.getSetting("tracefile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        miscSettingsPanel.add(traceDirField, gridBagConstraints);

        browseTraceDirButton.setText("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 0.1;
        miscSettingsPanel.add(browseTraceDirButton, gridBagConstraints);

        progDelayLabel.setText("Progress bar close delay:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        miscSettingsPanel.add(progDelayLabel, gridBagConstraints);

        progDelaySpinner.setModel(new javax.swing.SpinnerNumberModel(1000, 0, 120000, 1000));
        if (settings.getSetting("progressclosedelay") != null)
        	progDelaySpinner.setValue(Integer.parseInt(settings.getSetting("progressclosedelay")));
        progDelaySpinner.setEditor(new javax.swing.JSpinner.NumberEditor(progDelaySpinner, "0 ms"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        miscSettingsPanel.add(progDelaySpinner, gridBagConstraints);
        
        tabbedSettingsPanel.addTab("Misc. Settings", miscSettingsPanel);

		getContentPane().add(tabbedSettingsPanel);
		tabbedSettingsPanel.getAccessibleContext().setAccessibleName("tab");

		pack();
	}// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField allowedEvalErrFField;
    private javax.swing.JFormattedTextField almostEqTolFField;
    private javax.swing.JLabel almostEqTolLabel;
    private javax.swing.JPanel analysisParametersPanel;
    private javax.swing.JButton binAddButton;
    private javax.swing.JButton browseTraceDirButton;
    private javax.swing.JFormattedTextField eqTolFField;
    private javax.swing.JLabel eqTolLabel;
    private javax.swing.JLabel evalErrorFracLabel;
    private javax.swing.JComboBox histSelectionComboBox;
    private javax.swing.JPanel histogramSettingsPanel;
    private javax.swing.JFormattedTextField infinityFField;
    private javax.swing.JLabel infinityLabel;
    private javax.swing.JSpinner intPtsSpinner;
    private javax.swing.JLabel interiorPtsLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JFormattedTextField lineLenMaxFField;
    private javax.swing.JLabel lineLenMaxLabel;
    private javax.swing.JFormattedTextField lineLenMinFField;
    private javax.swing.JLabel lineLenMinLabel;
    private javax.swing.JLabel lineSegsLabel;
    private javax.swing.JSpinner lineSegsSpinner;
    private javax.swing.JLabel minPtsReqLabel;
    private javax.swing.JSpinner minPtsReqSpinner;
    private javax.swing.JPanel miscSettingsPanel;
    private javax.swing.JLabel progDelayLabel;
    private javax.swing.JSpinner progDelaySpinner; 
    private javax.swing.JFormattedTextField newBinUBoundFField;
    private javax.swing.JLabel newBinUBoundLabel;
    private javax.swing.JButton removeBinButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JCheckBox snapPointsCheckBox;
    private javax.swing.JTabbedPane tabbedSettingsPanel;
    private javax.swing.JTextField traceDirField;
    private javax.swing.JLabel traceDirLabel;
    private javax.swing.JLabel uBoundsLabel;
    private javax.swing.JList uBoundsListWidget;
    // End of variables declaration//GEN-END:variables

	@Override
	public void stateChanged(ChangeEvent arg0) {
		Object src = arg0.getSource();
		if (src == intPtsSpinner)
		{
			if (intPtsSpinner.getValue() instanceof Integer)
				aModel.setNumInteriorLinePoints((Integer)intPtsSpinner.getValue());
			else
				JOptionPane.showMessageDialog(this,
					    "Number of interior points must be an integer value",
					    "Value type error",
					    JOptionPane.ERROR_MESSAGE);
		}
		else if (src == lineSegsSpinner)
		{
			if (lineSegsSpinner.getValue() instanceof Integer)
				aModel.setNumLineSegments((Integer)lineSegsSpinner.getValue());
			else
				JOptionPane.showMessageDialog(this,
					    "Number of line segments must be an integer value",
					    "Value type error",
					    JOptionPane.ERROR_MESSAGE);
		}
		else if (src == minPtsReqSpinner)
		{
			if (minPtsReqSpinner.getValue() instanceof Integer)
				aModel.setMinimumPointsNeeded((Integer)minPtsReqSpinner.getValue());
			else
				JOptionPane.showMessageDialog(this,
					    "Number of points required must be an integer value",
					    "Value type error",
					    JOptionPane.ERROR_MESSAGE);
		}
		else if (src == progDelaySpinner)
		{
			settings.setSetting("progressclosedelay", progDelaySpinner.getValue().toString());
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();
		if (src == snapPointsCheckBox)
		{
			aModel.setSnapDiscreteComponents(snapPointsCheckBox.isSelected());
		}
		else if (src == browseTraceDirButton)
		{
			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fc.showDialog(this, "Select Directory");
			if (result == JFileChooser.APPROVE_OPTION)
			{
				File dir = fc.getSelectedFile();
				if (dir.exists() && dir.isDirectory())
				{
					traceDirField.setText(dir.getPath());
					settings.setSetting("tracefile", dir.getPath());
				}
				else if (!dir.exists())
				{
					result = JOptionPane.showConfirmDialog(
						    this,
						    "Directory:\n" +
						    dir.getPath() +
						    "\n doesn't exist, create it now?",
						    "Directory doesn't exist",
						    JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.OK_OPTION)
					{
						if (!dir.mkdir())
						{
							JOptionPane.showMessageDialog(this, "Could not create directory!",
									"Couldn't not create directory", JOptionPane.ERROR_MESSAGE);
						}
						else
						{
							traceDirField.setText(dir.getPath());
							settings.setSetting("tracefile", dir.getPath());
						}
					}
				}
			}
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		Object src = arg0.getSource();
		if (src == eqTolFField)
		{
			aModel.setEqualityTolerance((Double)eqTolFField.getValue());
		}
		else if (src == infinityFField)
		{
			aModel.setInfinity((Double)infinityFField.getValue());	
		}
		else if (src == lineLenMaxFField || src == lineLenMinFField)
		{

			aModel.setLineLengthBounds((Double)lineLenMinFField.getValue(),
											(Double)lineLenMaxFField.getValue());
		}
		else if (src == almostEqTolFField)
		{
			aModel.setAlmostEqualTolerance((Double)almostEqTolFField.getValue());	
		}
		else if (src == allowedEvalErrFField)
		{
			aModel.setEvalErrorTolerance((Double)allowedEvalErrFField.getValue());	
		}
	}

	private void histSelectionComboBoxActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_histSelectionComboBoxActionPerformed
		refreshHistogramData();
	}// GEN-LAST:event_histSelectionComboBoxActionPerformed

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_resetButtonActionPerformed
		aModel.setDefaultHistogramBins((HistType)histSelectionComboBox.getSelectedItem());
		refreshHistogramData();
	}// GEN-LAST:event_resetButtonActionPerformed

	private void binAddButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_binAddButtonActionPerformed
		Double d = (Double)newBinUBoundFField.getValue();
		HistType type = (HistType)histSelectionComboBox.getSelectedItem();
		
		uBoundModel.add(d);
		
		if (type == HistType.LineLength && d < 0.0)
		{
			JOptionPane.showMessageDialog(null,
				    "Line length is non negative",
				    "Logic error.",
				    JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			updateModelHistogram();
		}
	}// GEN-LAST:event_binAddButtonActionPerformed

	private void removeBinButtonActionPerformed(ActionEvent evt) {
		uBoundModel.removeIndices(uBoundsListWidget.getSelectedIndices());
		updateModelHistogram();
	}// GEN-LAST:event_removeBinButtonActionPerformed

	private void refreshHistogramData()
	{
		uBoundModel.clear();
		for (double d: aModel.getHistogramUpperBounds(
				(HistType)histSelectionComboBox.getSelectedItem()))
			uBoundModel.add(d);
		
		uBoundModel.add((uBoundModel.getElementAt(uBoundModel.getSize()-1))
				-aModel.getHistogramLowerBinWidth((HistType)histSelectionComboBox.getSelectedItem()));
	}
	
	private void updateModelHistogram()
	{
		if (uBoundModel.size() >= 2)
		{
			double[] tmp = new double[uBoundModel.size()-1];
			double lowBinWidth = uBoundModel.getElementAt(uBoundModel.getSize()-2)
								-uBoundModel.getElementAt(uBoundModel.getSize()-1);

			for (int i = 0; i < tmp.length; ++i)
			{
				tmp[i] = uBoundModel.getElementAt(tmp.length-1-i);
			}
			aModel.setHistogramBins((HistType)histSelectionComboBox.getSelectedItem(),
					lowBinWidth, tmp);
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
		control.close();
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

	@Override
	public void update(Observable o, Object arg1) {
		if (o == aModel)
		{
			if (aModel.loaded())
			{
				snapPointsCheckBox.setSelected(aModel.snapDiscreteComponents());
				
		        intPtsSpinner.setValue(aModel.interiorLinePoints());
		        lineSegsSpinner.setValue(aModel.numLineSegments());
		        minPtsReqSpinner.setValue(aModel.minimumPointsNeeded());

				eqTolFField.setValue(aModel.equalityTolerance());
		        almostEqTolFField.setValue(aModel.almostEqualTolerance());
		        infinityFField.setValue(aModel.infinity());
		        lineLenMinFField.setValue(aModel.lineLengthMin());
		        lineLenMaxFField.setValue(aModel.lineLengthMax());
		        allowedEvalErrFField.setValue(aModel.evalErrorTolerance());
		        
		        refreshHistogramData();
			}
		}
		else if (o == settings)
		{
			String tracePath = settings.getSetting("tracefile");
			if (tracePath != null && tracePath != traceDirField.getText())
				traceDirField.setText(tracePath);
			
			String progressCloseDelay = settings.getSetting("progressclosedelay");
			if (progressCloseDelay != null && progressCloseDelay != progDelaySpinner.getValue())
				progDelaySpinner.setValue(Integer.parseInt(progressCloseDelay));
		}
	}
}
