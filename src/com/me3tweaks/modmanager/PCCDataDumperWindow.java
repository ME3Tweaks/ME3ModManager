package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.me3tweaks.modmanager.objects.PCCDumpOptions;
import com.me3tweaks.modmanager.objects.ThreadCommand;

/**
 * Window controller for the pcc data dumping tool
 * 
 * @author Mgamerz
 *
 */
public class PCCDataDumperWindow extends JDialog {
	JLabel infoLabel;
	boolean windowOpen = true;
	String currentText;
	JProgressBar progressBar;
	JButton dumpButton;
	JCheckBox dumpScripts;
	JCheckBox dumpCoalesced;
	JCheckBox dumpProperties;
	JCheckBox dumpExports;
	JCheckBox dumpImports;
	JCheckBox dumpNames;
	ModManagerWindow callingWindow;

	/**
	 * Manually invoked pcc data dumping
	 * 
	 * @param callingWindow
	 * @param BioGameDir
	 */
	public PCCDataDumperWindow() {
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("PCC Data Dumper");
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel(
				"<html>Select the information you want to dump from game PCCs.<br>Dumping properties of PCCs can take a VERY LONG TIME.<br>To dump the entire game, it will take several hours.</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		northPanel.add(progressBar, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);

		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		TitledBorder DLCBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Dump options");
		checkBoxPanel.setBorder(DLCBorder);
		JPanel checkBoxPanelLeft = new JPanel();
		checkBoxPanelLeft.setLayout(new BoxLayout(checkBoxPanelLeft, BoxLayout.Y_AXIS));
		JPanel checkBoxPanelRight = new JPanel();
		checkBoxPanelRight.setLayout(new BoxLayout(checkBoxPanelRight, BoxLayout.Y_AXIS));

		dumpScripts = new JCheckBox("Unreal Scripts");
		dumpCoalesced = new JCheckBox("Coalesced Flags");
		dumpProperties = new JCheckBox("Export Properties");
		dumpExports = new JCheckBox("Exports");
		dumpImports = new JCheckBox("Imports");
		dumpNames = new JCheckBox("Name Table");

		dumpScripts.setToolTipText("Dumps unrealscript functions with tokenized bytecode");
		dumpCoalesced.setToolTipText("Dumps a [C] before exports that read their value from coalesced files");
		dumpProperties.setToolTipText("Dumps properties for each export. Will take a long time to cross reference data.");
		dumpExports.setToolTipText("Dumps the export list. This value is automatically chosen if using properties or data.");
		dumpImports.setToolTipText("Dumps the import list.");
		dumpNames.setToolTipText("Dumps the name table.");

		checkBoxPanelLeft.add(dumpNames);
		checkBoxPanelLeft.add(dumpImports);

		checkBoxPanelRight.add(dumpExports);
		checkBoxPanelRight.add(dumpCoalesced);
		checkBoxPanelRight.add(dumpScripts);
		checkBoxPanelRight.add(dumpProperties);

		JCheckBox[] checkboxes = new JCheckBox[] { dumpNames, dumpImports, dumpExports, dumpCoalesced, dumpScripts, dumpProperties };
		for (JCheckBox cb : checkboxes) {
			cb.setSelected(true);
		}
		checkBoxPanel.add(checkBoxPanelLeft, BorderLayout.WEST);
		checkBoxPanel.add(checkBoxPanelRight, BorderLayout.EAST);
		rootPanel.add(checkBoxPanel, BorderLayout.CENTER);

		dumpButton = new JButton("Dump PCC information");
		dumpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//write to settings
				//new backupDLCJob(BioGameDir, getJobs(), false).execute();
				String gameDirectory = new File(ModManagerWindow.GetBioGameDir()).getParent();
				PCCDumpOptions options = new PCCDumpOptions();
				options.gamePath = gameDirectory;
				options.coalesced = dumpCoalesced.isSelected();
				options.scripts = dumpScripts.isSelected();
				options.exports = dumpExports.isSelected();
				options.imports = dumpImports.isSelected();
				options.names = dumpNames.isSelected();
				options.properties = dumpProperties.isSelected();
				options.outputFolder = ModManager.getPCCDumpFolder();
				for (JCheckBox cb : checkboxes) {
					cb.setEnabled(false);
				}
				new DumpPCCJob(options).execute();
			}
		});

		JPanel backupPanel = new JPanel(new BorderLayout());
		backupPanel.add(dumpButton, BorderLayout.CENTER);
		backupPanel.add(new JLabel("<html><center>PCC dumping is slow and will take several hours.</center></html>"),BorderLayout.SOUTH);
		rootPanel.add(backupPanel, BorderLayout.SOUTH);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// methods will read this variable
			}
		});
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	private void setupWindowAutomated(String dlcName) {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("Backing up " + dlcName + "...");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setEnabled(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		northPanel.add(progressBar, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);
		rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		add(rootPanel);
		this.setTitle("PCC Data Dumper");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	class DumpPCCJob extends SwingWorker<Boolean, ThreadCommand> {

		private PCCDumpOptions options;

		public DumpPCCJob(PCCDumpOptions options) {
			this.options = options;
			dumpButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			// TODO Auto-generated method stub
			publish(new ThreadCommand("START_FILE_SCAN"));
			ArrayList<Path> files = new ArrayList<Path>();
			//Files.find(Paths.get(options.gamePath), 999, (p, bfa) -> bfa.isRegularFile()).forEach();
			Predicate<Path> predicate = p -> Files.isRegularFile(p) && p.toFile().getName().toLowerCase().endsWith(".pcc");
			files = (ArrayList<Path>) Files.walk(Paths.get(options.gamePath + "\\BIOGame")).filter(predicate).collect(Collectors.toList());
			int completed = 0;
			for (Path path : files) {
				publish(new ThreadCommand("UPDATE_STATUS", "<html>Dumping ["+(completed+1)+"/"+files.size()+"]:<br>"+path.toFile().getName()+"</html>"));
				ModManager.dumpPCC(path.toString(), options);
				completed++;
				int progressval = (int) ((completed / (files.size() * 1.0) * 100));
				publish(new ThreadCommand("SET_PROGRESS", null, progressval));
			}
			return null;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand tc : chunks) {
				String command = tc.getCommand();
				switch (command) {
				case "START_FILE_SCAN":
					infoLabel.setText("Building list of PCC files...");
					break;
				case "SET_PROGRESS":
					progressBar.setValue((int) tc.getData());
					break;
				case "UPDATE_STATUS":
					infoLabel.setText(tc.getMessage());
					break;
				}
			}
		}

	}
}
