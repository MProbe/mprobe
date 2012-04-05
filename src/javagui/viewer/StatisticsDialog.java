package viewer;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Observable;
import java.util.Observer;

import model.ProblemInstance;
import model.ProblemInstance.ConstraintType;
import model.ProblemInstance.FunctionShape;
import model.ProblemInstance.FunctionType;
import controller.StatisticsControl;


public class StatisticsDialog extends javax.swing.JDialog implements Observer, WindowListener  {

	StatisticsControl control;
	ProblemInstance model;

	/**
	 * Creates new form StatisticsDialog
	 */
	public StatisticsDialog(StatisticsControl ctrl, ProblemInstance inst,
			boolean modal) {
		super((javax.swing.JDialog) null, modal);
		initComponents();

		control = ctrl;
		model = inst;
		model.addObserver(this);
		addWindowListener(this);
		
		refreshView();
	}

	private void refreshView() {
		
		if (!model.loaded()) {
			setTitle("Statistics");
			varTotField.setText("");
			varRealField.setText("");
			varBinField.setText("");
			varIntField.setText("");
			objTotField.setText("");
			objLinField.setText("");
			objQuadField.setText("");
			objNonlinField.setText("");
			nzObjField.setText("");
			nzConstrField.setText("");
			constrTField.setText("");
			constrLField.setText("");
			constrLIField.setText("");
			constrLRField.setText("");
			constrLEField.setText("");
			constrQField.setText("");
			constrQIField.setText("");
			constrQRField.setText("");
			constrQEField.setText("");
			constrNField.setText("");
			constrNIField.setText("");
			constrNRField.setText("");
			constrNEField.setText("");
			return;
		}
		
		setTitle("Statistics"+ (model.instanceName().isEmpty()?"":": " + model.instanceName()));
		int v = model.variables();
		int o = model.objectives();
		int c = model.constraints();

		// summary for variable
		int r = 0;
		int b = 0;
		int other_int = 0;
		for (int i = 0; i < v; i++) {
			try {
				switch (model.variableType(i)) {
				case Real:
					++r;
					break;
				case Binary:
					++b;
					break;
				case Integer:
					++other_int;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		varTotField.setText(Integer.toString(v));
		varRealField.setText(Integer.toString(r));
		varBinField.setText(Integer.toString(b));
		varIntField.setText(Integer.toString(other_int));

		// summary for objective function
		int l = 0;
		int q = 0;
		int n = 0;
		int obj_nonzero = 0;
		int[] presences = new int[v + 1];
		for (int i = 0; i < o; i++) {
			try {
				switch (model.functionType(FunctionType.Objective, i)) {
				case Linear:
					++l;
					break;
				case Quadratic:
					++q;
					break;
				case Nonlinear:
					++n;
					break;
				}
				obj_nonzero += model.objectiveVariables(i, presences);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

		objTotField.setText(Integer.toString(o));
		objLinField.setText(Integer.toString(l));
		objQuadField.setText(Integer.toString(q));
		objNonlinField.setText(Integer.toString(n));

		nzObjField.setText(Integer.toString(obj_nonzero));

		// summary for constraint function
		int constrL = 0, constrLI = 0, constrLR = 0, constrLE = 0, constrQ = 0, constrQI = 0, constrQR = 0, constrQE = 0, constrN = 0, constrNI = 0, constrNR = 0, constrNE = 0;
		int constrnt_nonzero = 0;
		for (int i = 0; i < c; i++) {
			FunctionShape ftype;
			ConstraintType constrtype;
			try {
				ftype = model.functionType(FunctionType.Constraint, i);
				constrtype = model.constraintType(i);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (ftype == FunctionShape.Linear) {
				constrL++;
				if (constrtype == ConstraintType.Range)
					constrLR++;
				else if (constrtype == ConstraintType.Equality)
					constrLE++;
				else if (constrtype != ConstraintType.Unconstraining)
					constrLI++;
			} else if (ftype == FunctionShape.Quadratic) {
				constrQ++;
				if (constrtype == ConstraintType.Range)
					constrQR++;
				else if (constrtype == ConstraintType.Equality)
					constrQE++;
				else if (constrtype != ConstraintType.Unconstraining)
					constrQI++;
			} else if (ftype == FunctionShape.Nonlinear) {
				constrN++;
				if (constrtype == ConstraintType.Range)
					constrNR++;
				else if (constrtype == ConstraintType.Equality)
					constrNE++;
				else if (constrtype != ConstraintType.Unconstraining)
					constrNI++;
			}
			try {
				constrnt_nonzero += model.constraintVariables(i, presences);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		constrTField.setText(Integer.toString(c));
		constrLField.setText(Integer.toString(constrL));
		constrLIField.setText(Integer.toString(constrLI));
		constrLRField.setText(Integer.toString(constrLR));
		constrLEField.setText(Integer.toString(constrLE));
		constrQField.setText(Integer.toString(constrQ));
		constrQIField.setText(Integer.toString(constrQI));
		constrQRField.setText(Integer.toString(constrQR));
		constrQEField.setText(Integer.toString(constrQE));
		constrNField.setText(Integer.toString(constrN));
		constrNIField.setText(Integer.toString(constrNI));
		constrNRField.setText(Integer.toString(constrNR));
		constrNEField.setText(Integer.toString(constrNE));
		nzConstrField.setText(Integer.toString(constrnt_nonzero));
	}

	@Override
	public void update(Observable ob, Object arg) {
		if ((Object) ob == (Object) model) {
				refreshView();
		}
			
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

        varPanel = new javax.swing.JPanel();
        varTotLabel = new javax.swing.JLabel();
        varTotField = new javax.swing.JTextField();
        varRealLabel = new javax.swing.JLabel();
        varRealField = new javax.swing.JTextField();
        varBinLabel = new javax.swing.JLabel();
        varBinField = new javax.swing.JTextField();
        varIntLabel = new javax.swing.JLabel();
        varIntField = new javax.swing.JTextField();
        objPanel = new javax.swing.JPanel();
        objTotLabel = new javax.swing.JLabel();
        objTotField = new javax.swing.JTextField();
        objLinLabel = new javax.swing.JLabel();
        objLinField = new javax.swing.JTextField();
        objQuadLabel = new javax.swing.JLabel();
        objQuadField = new javax.swing.JTextField();
        objNonlinLabel = new javax.swing.JLabel();
        objNonlinField = new javax.swing.JTextField();
        constrPanel = new javax.swing.JPanel();
        constrTotLabel = new javax.swing.JLabel();
        constrTField = new javax.swing.JTextField();
        constrLinLabel = new javax.swing.JLabel();
        constrLField = new javax.swing.JTextField();
        constrLinIneqLabel = new javax.swing.JLabel();
        constrLIField = new javax.swing.JTextField();
        constrLinRngLabel = new javax.swing.JLabel();
        constrLRField = new javax.swing.JTextField();
        constrLinEqLabel = new javax.swing.JLabel();
        constrLEField = new javax.swing.JTextField();
        constrQuadLabel = new javax.swing.JLabel();
        constrQField = new javax.swing.JTextField();
        constrQuadIneqLabel = new javax.swing.JLabel();
        constrQIField = new javax.swing.JTextField();
        constrQuadRngLabel = new javax.swing.JLabel();
        constrQRField = new javax.swing.JTextField();
        constrQuadEqLabel = new javax.swing.JLabel();
        constrQEField = new javax.swing.JTextField();
        constrNonLabel = new javax.swing.JLabel();
        constrNField = new javax.swing.JTextField();
        constrNonIneqLabel = new javax.swing.JLabel();
        constrNIField = new javax.swing.JTextField();
        constrNonRngLabel = new javax.swing.JLabel();
        constrNRField = new javax.swing.JTextField();
        constrNonEqLabel = new javax.swing.JLabel();
        constrNEField = new javax.swing.JTextField();
        nzPanel = new javax.swing.JPanel();
        nzObjLabel = new javax.swing.JLabel();
        nzObjField = new javax.swing.JTextField();
        nzConstrLabel = new javax.swing.JLabel();
        nzConstrField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(500, 300));
        setPreferredSize(new java.awt.Dimension(500, 300));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        varPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Variables"));
        varPanel.setLayout(new java.awt.GridLayout(4, 2));

        varTotLabel.setText("Total");
        varPanel.add(varTotLabel);

        varTotField.setEditable(false);
        varTotField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        varPanel.add(varTotField);

        varRealLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        varRealLabel.setText("Real");
        varPanel.add(varRealLabel);

        varRealField.setEditable(false);
        varRealField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        varPanel.add(varRealField);

        varBinLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        varBinLabel.setText("Binary");
        varPanel.add(varBinLabel);

        varBinField.setEditable(false);
        varBinField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        varPanel.add(varBinField);

        varIntLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        varIntLabel.setText("Integer");
        varPanel.add(varIntLabel);

        varIntField.setEditable(false);
        varIntField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        varPanel.add(varIntField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(varPanel, gridBagConstraints);

        objPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Objectives"));
        objPanel.setLayout(new java.awt.GridLayout(4, 2));

        objTotLabel.setText("Total");
        objPanel.add(objTotLabel);

        objTotField.setEditable(false);
        objTotField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        objPanel.add(objTotField);

        objLinLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        objLinLabel.setText("Linear");
        objPanel.add(objLinLabel);

        objLinField.setEditable(false);
        objLinField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        objPanel.add(objLinField);

        objQuadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        objQuadLabel.setText("Quadratic");
        objPanel.add(objQuadLabel);

        objQuadField.setEditable(false);
        objQuadField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        objPanel.add(objQuadField);

        objNonlinLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        objNonlinLabel.setText("Nonlinear");
        objPanel.add(objNonlinLabel);

        objNonlinField.setEditable(false);
        objNonlinField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        objPanel.add(objNonlinField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(objPanel, gridBagConstraints);

        constrPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Constraints"));
        constrPanel.setLayout(new java.awt.GridLayout(13, 2));

        constrTotLabel.setText("Total");
        constrPanel.add(constrTotLabel);

        constrTField.setEditable(false);
        constrTField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrTField);

        constrLinLabel.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        constrLinLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        constrLinLabel.setText("Linear");
        constrPanel.add(constrLinLabel);

        constrLField.setEditable(false);
        constrLField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrLField);

        constrLinIneqLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrLinIneqLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrLinIneqLabel.setText("Inequalities");
        constrPanel.add(constrLinIneqLabel);

        constrLIField.setEditable(false);
        constrLIField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrLIField);

        constrLinRngLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrLinRngLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrLinRngLabel.setText("Ranges");
        constrPanel.add(constrLinRngLabel);

        constrLRField.setEditable(false);
        constrLRField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrLRField);

        constrLinEqLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrLinEqLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrLinEqLabel.setText("Equalities");
        constrPanel.add(constrLinEqLabel);

        constrLEField.setEditable(false);
        constrLEField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrLEField);

        constrQuadLabel.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        constrQuadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        constrQuadLabel.setText("Quadratic");
        constrPanel.add(constrQuadLabel);

        constrQField.setEditable(false);
        constrQField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrQField);

        constrQuadIneqLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrQuadIneqLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrQuadIneqLabel.setText("Inequalities");
        constrPanel.add(constrQuadIneqLabel);

        constrQIField.setEditable(false);
        constrQIField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrQIField);

        constrQuadRngLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrQuadRngLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrQuadRngLabel.setText("Ranges");
        constrPanel.add(constrQuadRngLabel);

        constrQRField.setEditable(false);
        constrQRField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrQRField);

        constrQuadEqLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrQuadEqLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrQuadEqLabel.setText("Equalities");
        constrPanel.add(constrQuadEqLabel);

        constrQEField.setEditable(false);
        constrQEField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrQEField);

        constrNonLabel.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        constrNonLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        constrNonLabel.setText("Nonlinear");
        constrPanel.add(constrNonLabel);

        constrNField.setEditable(false);
        constrNField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrNField);

        constrNonIneqLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrNonIneqLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrNonIneqLabel.setText("Inequalities");
        constrPanel.add(constrNonIneqLabel);

        constrNIField.setEditable(false);
        constrNIField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrNIField);

        constrNonRngLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrNonRngLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrNonRngLabel.setText("Ranges");
        constrPanel.add(constrNonRngLabel);

        constrNRField.setEditable(false);
        constrNRField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrNRField);

        constrNonEqLabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        constrNonEqLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        constrNonEqLabel.setText("Equalities");
        constrPanel.add(constrNonEqLabel);

        constrNEField.setEditable(false);
        constrNEField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        constrPanel.add(constrNEField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(constrPanel, gridBagConstraints);

        nzPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Nonzeros"));
        nzPanel.setLayout(new java.awt.GridLayout(2, 2));

        nzObjLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nzObjLabel.setText("In Objectives");
        nzPanel.add(nzObjLabel);

        nzObjField.setEditable(false);
        nzObjField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        nzPanel.add(nzObjField);

        nzConstrLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        nzConstrLabel.setText("In Constraints");
        nzPanel.add(nzConstrLabel);

        nzConstrField.setEditable(false);
        nzConstrField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        nzPanel.add(nzConstrField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(nzPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField constrLEField;
    private javax.swing.JTextField constrLField;
    private javax.swing.JTextField constrLIField;
    private javax.swing.JTextField constrLRField;
    private javax.swing.JLabel constrLinEqLabel;
    private javax.swing.JLabel constrLinIneqLabel;
    private javax.swing.JLabel constrLinLabel;
    private javax.swing.JLabel constrLinRngLabel;
    private javax.swing.JTextField constrNEField;
    private javax.swing.JTextField constrNField;
    private javax.swing.JTextField constrNIField;
    private javax.swing.JTextField constrNRField;
    private javax.swing.JLabel constrNonEqLabel;
    private javax.swing.JLabel constrNonIneqLabel;
    private javax.swing.JLabel constrNonLabel;
    private javax.swing.JLabel constrNonRngLabel;
    private javax.swing.JPanel constrPanel;
    private javax.swing.JTextField constrQEField;
    private javax.swing.JTextField constrQField;
    private javax.swing.JTextField constrQIField;
    private javax.swing.JTextField constrQRField;
    private javax.swing.JLabel constrQuadEqLabel;
    private javax.swing.JLabel constrQuadIneqLabel;
    private javax.swing.JLabel constrQuadLabel;
    private javax.swing.JLabel constrQuadRngLabel;
    private javax.swing.JTextField constrTField;
    private javax.swing.JLabel constrTotLabel;
    private javax.swing.JTextField nzConstrField;
    private javax.swing.JLabel nzConstrLabel;
    private javax.swing.JTextField nzObjField;
    private javax.swing.JLabel nzObjLabel;
    private javax.swing.JPanel nzPanel;
    private javax.swing.JTextField objLinField;
    private javax.swing.JLabel objLinLabel;
    private javax.swing.JTextField objNonlinField;
    private javax.swing.JLabel objNonlinLabel;
    private javax.swing.JPanel objPanel;
    private javax.swing.JTextField objQuadField;
    private javax.swing.JLabel objQuadLabel;
    private javax.swing.JTextField objTotField;
    private javax.swing.JLabel objTotLabel;
    private javax.swing.JTextField varBinField;
    private javax.swing.JLabel varBinLabel;
    private javax.swing.JTextField varIntField;
    private javax.swing.JLabel varIntLabel;
    private javax.swing.JPanel varPanel;
    private javax.swing.JTextField varRealField;
    private javax.swing.JLabel varRealLabel;
    private javax.swing.JTextField varTotField;
    private javax.swing.JLabel varTotLabel;
    // End of variables declaration//GEN-END:variables

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		control.close();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
