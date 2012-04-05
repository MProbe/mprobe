package viewer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import model.ProblemInstance;
import controller.MProbeController;

public class MainWin extends JFrame implements ActionListener, WindowListener, Observer {

	public static final String title = "Welcome to MProbe";
	public static final String showStatisticSummary = "Show Statistic Summary";
	public static final String showVariableWorkshop = "Open variable workshop";
	public static final String showConstrainWorkshop = "Open constraint workshop";
	public static final String showObjectWorkshop = "Open objective workshop";
	public static final String showAnalysisSettings = "Open analysis settings";

	private JMenuItem load;
	private JMenuItem helpinfo;
	private JMenuItem about;
	private JMenuItem close;
	private JMenuItem filepath;
	private JButton statisticSummary;
	private JButton variableWorkshop;
	private JButton constraintWorkshop;
	private JButton objectiveWorkshop;
	private JButton analysisSettings;

	private MProbeController control;
	private ProblemInstance model;

	/**
	 * the main window for MProbe
	 * @param pModel 
	 */
	public MainWin(MProbeController ctrl, ProblemInstance inst) {
		super(title);

		control = ctrl;
		model = inst;
		
		model.addObserver(this);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);

		Container content = getContentPane();
		content.setLayout(new FlowLayout());

		// set MenuBar
		JMenuBar MainMenu = new JMenuBar();
		// set the file menu
		JMenu file = new JMenu("file");
		filepath = new JMenuItem("set file path");
		load = new JMenuItem("load file");
		close = new JMenuItem("exit");
		file.add(filepath);
		file.add(load);
		file.add(close);
		// set the help menu
		JMenu help = new JMenu("help");
		helpinfo = new JMenuItem("help contents");
		about = new JMenuItem("about MProbe");
		help.add(helpinfo);
		help.add(about);
		// add the menus to menuBar
		MainMenu.add(file);
		MainMenu.add(help);
		this.setJMenuBar(MainMenu);

		// set the buttons in the main window
		statisticSummary = new JButton(showStatisticSummary);
		variableWorkshop = new JButton(showVariableWorkshop);
		constraintWorkshop = new JButton(showConstrainWorkshop);
		objectiveWorkshop = new JButton(showObjectWorkshop);
		analysisSettings = new JButton(showAnalysisSettings);

		enableButtons(false);
		content.add(statisticSummary);
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add(variableWorkshop);
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add(constraintWorkshop);
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add(objectiveWorkshop);
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add(analysisSettings);

		this.setSize(400, 400);
		setVisible(true);

		load.addActionListener(this);
		helpinfo.addActionListener(this);
		about.addActionListener(this);
		close.addActionListener(this);
		filepath.addActionListener(this);
		statisticSummary.addActionListener(this);
		variableWorkshop.addActionListener(this);
		constraintWorkshop.addActionListener(this);
		objectiveWorkshop.addActionListener(this);
		analysisSettings.addActionListener(this);
	}

	/**
	 * enable or disable all buttons in the main window
	 */
	public void enableButtons(boolean status) {
		statisticSummary.setEnabled(status);
		variableWorkshop.setEnabled(status);
		constraintWorkshop.setEnabled(status);
		objectiveWorkshop.setEnabled(status);
		analysisSettings.setEnabled(status);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == close)
			control.shutdown();
		else if (source == filepath)
			control.openFilepath();
		else if (source == load)
			control.loadFiles();
		else if (source == statisticSummary)
			control.showStatisticSummary();
		else if (source == constraintWorkshop)
			control.showConstraintWorkshop();
		else if (source == objectiveWorkshop)
			control.showObjectiveWorkshop();
		else if (source == variableWorkshop)
			control.showVariableWorkshop();
		else if (source == analysisSettings)
			control.showAnalysisSettings();
		/*
		 * TODO helpinfo; about;
		 */
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		control.shutdown();
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

	@Override
	public void update(Observable o, Object arg) {
		if ((Object)o == (Object)model)
			enableButtons(model.loaded());
	}

}
