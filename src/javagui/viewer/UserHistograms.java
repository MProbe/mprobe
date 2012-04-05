/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import model.AnalysisInstance;
import model.MProbeLib.HistType;

public class UserHistograms extends javax.swing.JFrame implements ActionListener {

	String functionName;

	double[] shapeHistBins;
	long[] shapeHistFrequencies;
	long shapeDataPoints;
	long shapePointsAbove;
	long shapePointsBelow;
	double shapeMean, shapeStdDev, shapeVariance, shapeMaximum, shapeMinimum;
	
	double[] slopeHistBins;
	long[] slopeHistFrequencies;
	long slopeDataPoints;
	long slopePointsAbove;
	long slopePointsBelow;
	double slopeMean, slopeStdDev, slopeVariance, slopeMaximum, slopeMinimum;
	
	double[] fValHistBins;
	long[] fValHistFrequencies;
	long fValDataPoints;
	long fValPointsAbove;
	long fValPointsBelow;
	double fValMean, fValStdDev, fValVariance, fValMaximum, fValMinimum;
	
	double[] lineLenHistBins;
	long[] lineLenHistFrequencies;
	long lineLenDataPoints;
	long lineLenPointsAbove;
	long lineLenPointsBelow;
	double lineLenMean, lineLenStdDev, lineLenVariance, lineLenMaximum, lineLenMinimum;

    public UserHistograms(AnalysisInstance aInst, String functionName) {
        initComponents();
        
        this.functionName = functionName;
        setTitle(functionName + " Histograms");
        
		shapeHistButton.addActionListener(this);
		slopeHistButton.addActionListener(this);
		fValHistButton.addActionListener(this);
		lineLenHistButton.addActionListener(this);
		
		cacheHistogramData(aInst);
    }
    
    private void cacheHistogramData(AnalysisInstance aInst)
    {
    	shapeHistBins = aInst.getHistogramUpperBounds(HistType.Shape).clone();
    	Arrays.sort(shapeHistBins);
    	shapeHistFrequencies = new long[aInst.getHistogramNumBins(HistType.Shape)];
    	for (int i = 0; i < shapeHistBins.length; ++i)
    	{
    		shapeHistFrequencies[i] = (int) aInst.getHistogramBin(HistType.Shape, i);
    	}
    	shapeDataPoints = aInst.getHistogramDataPoints(HistType.Shape);
    	shapePointsAbove = aInst.getHistogramNumAboveRange(HistType.Shape);
    	shapePointsBelow = aInst.getHistogramNumBelowRange(HistType.Shape);
    	shapeMean = aInst.getHistogramMean(HistType.Shape);
    	shapeStdDev = aInst.getHistogramStdDev(HistType.Shape);
    	shapeVariance  = aInst.getHistogramVariance(HistType.Shape);
    	shapeMaximum = aInst.getHistogramMaximum(HistType.Shape);
    	shapeMinimum = aInst.getHistogramMinimum(HistType.Shape);
    	
    	slopeHistBins = aInst.getHistogramUpperBounds(HistType.Slope).clone();
    	Arrays.sort(slopeHistBins);
    	slopeHistFrequencies = new long[aInst.getHistogramNumBins(HistType.Slope)];
    	for (int i = 0; i < slopeHistBins.length; ++i)
    	{
    		slopeHistFrequencies[i] = (int) aInst.getHistogramBin(HistType.Slope, i);
    	}
    	slopeDataPoints = aInst.getHistogramDataPoints(HistType.Slope);
    	slopePointsAbove = aInst.getHistogramNumAboveRange(HistType.Slope);
    	slopePointsBelow = aInst.getHistogramNumBelowRange(HistType.Slope);
    	slopeMean = aInst.getHistogramMean(HistType.Slope);
    	slopeStdDev = aInst.getHistogramStdDev(HistType.Slope);
    	slopeVariance  = aInst.getHistogramVariance(HistType.Slope);
    	slopeMaximum = aInst.getHistogramMaximum(HistType.Slope);
    	slopeMinimum = aInst.getHistogramMinimum(HistType.Slope);
  
    	fValHistBins = aInst.getHistogramUpperBounds(HistType.FunctionValue).clone();
    	Arrays.sort(fValHistBins);
    	fValHistFrequencies = new long[aInst.getHistogramNumBins(HistType.FunctionValue)];
    	for (int i = 0; i < fValHistBins.length; ++i)
    	{
    		fValHistFrequencies[i] = (int) aInst.getHistogramBin(HistType.FunctionValue, i);
    	}
    	fValDataPoints = aInst.getHistogramDataPoints(HistType.FunctionValue);
    	fValPointsAbove = aInst.getHistogramNumAboveRange(HistType.FunctionValue);
    	fValPointsBelow = aInst.getHistogramNumBelowRange(HistType.FunctionValue);
    	fValMean = aInst.getHistogramMean(HistType.FunctionValue);
    	fValStdDev = aInst.getHistogramStdDev(HistType.FunctionValue);
    	fValVariance  = aInst.getHistogramVariance(HistType.FunctionValue);
    	fValMaximum = aInst.getHistogramMaximum(HistType.FunctionValue);
    	fValMinimum = aInst.getHistogramMinimum(HistType.FunctionValue);
    	
    	lineLenHistBins = aInst.getHistogramUpperBounds(HistType.LineLength).clone();
    	Arrays.sort(lineLenHistBins);
    	lineLenHistFrequencies = new long[aInst.getHistogramNumBins(HistType.LineLength)];
    	for (int i = 0; i < lineLenHistBins.length; ++i)
    	{
    		lineLenHistFrequencies[i] = (int) aInst.getHistogramBin(HistType.LineLength, i);
    	}
    	lineLenDataPoints = aInst.getHistogramDataPoints(HistType.LineLength);
    	lineLenPointsAbove = aInst.getHistogramNumAboveRange(HistType.LineLength);
    	lineLenPointsBelow = aInst.getHistogramNumBelowRange(HistType.LineLength);
    	lineLenMean = aInst.getHistogramMean(HistType.LineLength);
    	lineLenStdDev = aInst.getHistogramStdDev(HistType.LineLength);
    	lineLenVariance  = aInst.getHistogramVariance(HistType.LineLength);
    	lineLenMaximum = aInst.getHistogramMaximum(HistType.LineLength);
    	lineLenMinimum = aInst.getHistogramMinimum(HistType.LineLength);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        shapeHistButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        slopeHistButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        fValHistButton = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        lineLenHistButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Extra Histograms");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        shapeHistButton.setText("Shape");
        getContentPane().add(shapeHistButton, new java.awt.GridBagConstraints());
        getContentPane().add(filler1, new java.awt.GridBagConstraints());

        slopeHistButton.setText("Slope");
        getContentPane().add(slopeHistButton, new java.awt.GridBagConstraints());
        getContentPane().add(filler3, new java.awt.GridBagConstraints());

        fValHistButton.setText("Function value");
        getContentPane().add(fValHistButton, new java.awt.GridBagConstraints());
        getContentPane().add(filler4, new java.awt.GridBagConstraints());

        lineLenHistButton.setText("Line length");
        getContentPane().add(lineLenHistButton, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton fValHistButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JButton lineLenHistButton;
    private javax.swing.JButton shapeHistButton;
    private javax.swing.JButton slopeHistButton;
    // End of variables declaration//GEN-END:variables

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		HistogramWin histWin = null;
		if (src == shapeHistButton)
		{
			histWin = new HistogramWin(functionName + " Shape", shapeHistBins, shapeHistFrequencies);
			histWin.setDataPoints(shapeDataPoints);
			histWin.setPointsAbove(shapePointsAbove);
			histWin.setPointsBelow(shapePointsBelow);
			histWin.setExtraStats(shapeMean, shapeStdDev, shapeVariance, shapeMaximum, shapeMinimum);
	
		}
		else if (src == slopeHistButton)
		{
			histWin = new HistogramWin(functionName + " Slope", slopeHistBins, slopeHistFrequencies);
			histWin.setDataPoints(slopeDataPoints);
			histWin.setPointsAbove(slopePointsAbove);
			histWin.setPointsBelow(slopePointsBelow);
			histWin.setExtraStats(slopeMean, slopeStdDev, slopeVariance, slopeMaximum, slopeMinimum);
		}
		else if (src == fValHistButton)
		{
			histWin = new HistogramWin(functionName + " Function Value", fValHistBins, fValHistFrequencies);
			histWin.setDataPoints(fValDataPoints);
			histWin.setPointsAbove(fValPointsAbove);
			histWin.setPointsBelow(fValPointsBelow);
			histWin.setExtraStats(fValMean, fValStdDev, fValVariance, fValMaximum, fValMinimum);
		}
		else if (src == lineLenHistButton)
		{
			histWin = new HistogramWin(functionName + " Line length", lineLenHistBins, lineLenHistFrequencies);
			histWin.setDataPoints(lineLenDataPoints);
			histWin.setPointsAbove(lineLenPointsAbove);
			histWin.setPointsBelow(lineLenPointsBelow);
			histWin.setExtraStats(lineLenMean, lineLenStdDev, lineLenVariance, lineLenMaximum, lineLenMinimum);
		}
		if (histWin != null)
		{
			histWin.setVisible(true);
			histWin.setLocationRelativeTo(this);
		}
	}
}
