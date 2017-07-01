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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.PCCDumpOptions;
import com.me3tweaks.modmanager.objects.ProcessResult;
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
	JProgressBar progressBar;
	JButton dumpButton;
	JCheckBox dumpScripts;
	JCheckBox dumpCoalesced;
	JCheckBox dumpProperties;
	JCheckBox dumpExports;
	JCheckBox dumpImports;
	JCheckBox dumpNames;
	final int threads = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);

	/**
	 * Manually invoked pcc data dumping window
	 * 
	 */
	public PCCDataDumperWindow() {
		ModManager.debugLogger.writeMessage("Opening PCCDataDumperWindow");
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("PCC Data Dumper");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		JLabel[] threadOperationLabels = new JLabel[threads]; //VMs won't be supported obviously
		infoLabel = new JLabel(
				"<html>Select the information you want to dump from game PCCs.<br>Dumping properties of PCCs can take a VERY LONG TIME.<br>To dump the entire game, it will take several hours.</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		//northPanel.add(progressBar, BorderLayout.SOUTH);

		JPanel progressPanel = new JPanel(new BorderLayout());
		TitledBorder progressBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Current operations");
		progressPanel.setBorder(progressBorder);
		progressPanel.add(progressBar, BorderLayout.NORTH);

		JPanel threadPanel = new JPanel();
		threadPanel.setLayout(new BoxLayout(threadPanel, BoxLayout.PAGE_AXIS));
		for (int i = 0; i < threads; i++) {
			JLabel label = new JLabel("Thread " + (i + 1) + " - idle");
			threadOperationLabels[i] = label;
			threadPanel.add(label);
		}
		progressPanel.add(threadPanel);

		northPanel.add(progressPanel, BorderLayout.SOUTH);

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
				ModManager.debugLogger.writeMessage("Dumping pcc files...");

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
				new DumpPCCJob(options, threadOperationLabels).execute();
			}
		});

		JPanel backupPanel = new JPanel(new BorderLayout());
		backupPanel.add(dumpButton, BorderLayout.CENTER);
		backupPanel.add(new JLabel("<html><center>PCC dumping is slow and will take several hours.<br>Dumps are placed in the data/PCCDumps folder.</center></html>", SwingConstants.CENTER), BorderLayout.SOUTH);
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
		public AtomicInteger completed = new AtomicInteger(0);
		public int threads = 1;
		public int totalfiles;
		ArrayList<Path> files = new ArrayList<Path>();
		private JLabel[] threadOperationLabels;

		public DumpPCCJob(PCCDumpOptions options, JLabel[] threadOperationLabels) {
			this.options = options;
			this.threadOperationLabels = threadOperationLabels;
			threads = threadOperationLabels.length;
			dumpButton.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			// TODO Auto-generated method stub
			publish(new ThreadCommand("UPDATE_STATUS", "Building list of PCC files..."));
			//Files.find(Paths.get(options.gamePath), 999, (p, bfa) -> bfa.isRegularFile()).forEach();
			if (files.size() == 0) {
				Predicate<Path> predicate = p -> Files.isRegularFile(p) && p.toFile().getName().toLowerCase().endsWith(".pcc");
				files = (ArrayList<Path>) Files.walk(Paths.get(options.gamePath + "\\BIOGame")).filter(predicate).collect(Collectors.toList());
			}
			publish(new ThreadCommand("UPDATE_STATUS", "Dumping PCC files..."));

			ExecutorService autotocExecutor = Executors.newFixedThreadPool(threads);
			ArrayList<Future<ProcessResult>> futures = new ArrayList<Future<ProcessResult>>();
			for (Path path : files) {
				//submit jobs
				DumpTask jtask = new DumpTask(path.toString(), options);
				futures.add(autotocExecutor.submit(jtask));
			}
			autotocExecutor.shutdown();
			try {
				autotocExecutor.awaitTermination(5, TimeUnit.MINUTES);
				for (Future<ProcessResult> f : futures) {
					ProcessResult pr = f.get();
					if (pr.getReturnCode() != 0) {
						ModManager.debugLogger.writeError("File failed to dump!");
						//throw some sort of error here...
					}
				}
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeErrorWithException("EXECUTION EXCEPTION WHILE DUMPING FILES: ", e);
				return null;
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("UNKNOWN EXCEPTION OCCURED: ", e);
				return null;
			}
			return null;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand tc : chunks) {
				String command = tc.getCommand();
				switch (command) {
				case "SET_PROGRESS":
					progressBar.setValue((int) tc.getData());
					break;
				case "UPDATE_STATUS":
					infoLabel.setText(tc.getMessage());
					break;
				case "ASSIGN_TASK":
					for (int i = 0; i < threadOperationLabels.length; i++) {
						JLabel label = threadOperationLabels[i];
						String text = label.getText();
						String idleText = "Thread " + (i + 1) + " - idle";
						if (text.equals(idleText)) {
							//mark in use
							label.setText("Thread " + (i + 1) + " - " + tc.getMessage());
							break;
						}
					}
					break;
				case "RELEASE_TASK":
					for (int i = 0; i < threadOperationLabels.length; i++) {
						JLabel label = threadOperationLabels[i];
						String text = label.getText();
						String inUseText = "Thread " + (i + 1) + " - " + tc.getMessage();
						if (text.equals(inUseText)) {
							//mark in use
							label.setText("Thread " + (i + 1) + " - idle");
							break;
						}
					}
					break;
				}
			}
		}

		class DumpTask implements Callable<ProcessResult> {
			private String filepath;
			private PCCDumpOptions options;

			public DumpTask(String filepath, PCCDumpOptions options) {
				this.filepath = filepath;
				this.options = options;
			}

			@Override
			public ProcessResult call() throws Exception {
				publish(new ThreadCommand("ASSIGN_TASK", FilenameUtils.getName(filepath)));
				ProcessResult pr = ModManager.dumpPCC(filepath, options);
				completed.incrementAndGet();
				publish(new ThreadCommand("RELEASE_TASK", FilenameUtils.getName(filepath)));
				int progressval = (int) ((completed.get() / (files.size() * 1.0) * 100));
				publish(new ThreadCommand("SET_PROGRESS", null, progressval));
				publish(new ThreadCommand("UPDATE_STATUS", "Dumping PCC files... " + completed.get() + " of " + files.size()));

				return pr;
			}
		}

	}
}
