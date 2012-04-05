package viewer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import adapter.PluginTableModel;
import controller.MProbeController;

public class FilePath extends JFrame implements ActionListener {
	private static String title = "file path";
	private MProbeController control;

	private JButton addPlugin;
	private JButton removePlugin;
	private JButton addSrc;
	private JButton removeSrc;

	private JTable pluginFiles;
	private PluginTableModel pluginsModel;
	private JTable srcFiles;
	private DefaultTableModel srcdata;
	private String path;

	public FilePath(MProbeController ctrl, PluginTableModel pluginModel, String lastPath) {
		super(title);

		control = ctrl;
		
		if (lastPath == null || lastPath.isEmpty())
			path = null;
		else
			path = lastPath;
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		JPanel pluginPanel = new JPanel();
		pluginPanel.setPreferredSize(new Dimension(200, 200));
		JPanel srcPanel = new JPanel();
		srcPanel.setPreferredSize(new Dimension(200, 200));

		// set the plug-in panel
		pluginPanel.setLayout(new BoxLayout(pluginPanel, BoxLayout.X_AXIS));
		this.pluginsModel = pluginModel;
		pluginFiles = new JTable(pluginModel);
		pluginFiles
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scroll1 = new JScrollPane(pluginFiles);
		scroll1.setPreferredSize(new Dimension(300, 300));
		pluginPanel.add(scroll1);
		pluginPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		JPanel commandarea1 = new JPanel();
		commandarea1.setLayout(new BoxLayout(commandarea1, BoxLayout.Y_AXIS));
		addPlugin = new JButton("add plug-in files");
		removePlugin = new JButton("remove plug-in files");
		commandarea1.add(addPlugin);
		commandarea1.add(Box.createVerticalStrut(20));
		commandarea1.add(removePlugin);
		pluginPanel.add(commandarea1);

		// set the src panel
		srcPanel.setLayout(new BoxLayout(srcPanel, BoxLayout.X_AXIS));
		String[] tableheader2 = { "source files" };
		String[][] s2 = {};
		srcdata = new DefaultTableModel(s2, tableheader2);
		srcFiles = new JTable(srcdata);
		srcFiles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scroll2 = new JScrollPane(srcFiles);
		scroll2.setPreferredSize(new Dimension(300, 300));
		srcPanel.add(scroll2);
		srcPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		JPanel commandarea2 = new JPanel();
		addSrc = new JButton("add source files");
		removeSrc = new JButton("remove source files");
		commandarea2.setLayout(new BoxLayout(commandarea2, BoxLayout.Y_AXIS));
		commandarea2.add(addSrc);
		commandarea2.add(Box.createVerticalStrut(20));
		commandarea2.add(removeSrc);
		srcPanel.add(commandarea2);

		add(pluginPanel);
		add(srcPanel);

		addSrc.addActionListener(this);
		addPlugin.addActionListener(this);
		removePlugin.addActionListener(this);
		removeSrc.addActionListener(this);

		setSize(500, 500);
	}

	public String getPluginfile() {
		if (pluginsModel.getRowCount() == 0)
			return null;
		return (String) pluginsModel.getValueAt(0, 0);
	}

	public String[] getSrcfiles() {
		if (srcdata.getRowCount() == 0)
			return null;
		String[] srcfiles = new String[srcdata.getRowCount()];

		for (int i = 0; i < srcdata.getRowCount(); i++) {
			srcfiles[i] = (String) srcdata.getValueAt(i, 0);
		}
		return srcfiles;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == addSrc) {
			final JFileChooser fc = new JFileChooser(path);
			int returnVal = fc.showOpenDialog(getComponent(0));
			String srcFile = "";
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;
			if (fc.getSelectedFile() == null || !fc.getSelectedFile().exists())
				return;
			
			srcFile = fc.getSelectedFile().getPath();
			
			String[] srcfiles = { srcFile };
			srcdata.addRow(srcfiles);

			if (fc.getCurrentDirectory() != null)
			{
				path = fc.getCurrentDirectory().getAbsolutePath();
				control.setLastPathUsed(path);
			}
		} else if (source == addPlugin) {
			final JFileChooser fc = new JFileChooser(path);
			int returnVal = fc.showOpenDialog(getComponent(0));
			String pluginFile = "";
			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;
			if (fc.getSelectedFile() == null || !fc.getSelectedFile().exists())
				return;
				
			pluginFile = fc.getSelectedFile().getAbsolutePath();

			control.addPlugin(pluginFile);
			
			if (fc.getCurrentDirectory() != null)
			{
				path = fc.getCurrentDirectory().getAbsolutePath();
				control.setLastPathUsed(path);
			}
		} else if (source == removePlugin) {
			if (pluginFiles.getSelectedRow() != -1) {
				control.removePlugin((String) pluginFiles.getValueAt(
						pluginFiles.getSelectedRow(), 0));
			}
		} else if (source == removeSrc) {
			if (srcFiles.getSelectedRow() != -1) {
				srcdata.removeRow(srcFiles.getSelectedRow());
			}
		}
	}

}
