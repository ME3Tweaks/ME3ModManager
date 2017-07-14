package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.PCCDataDumperWindow.DumpPCCJob.DumpTaskResult;
import com.me3tweaks.modmanager.objects.PCCDumpOptions;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Window controller for the pcc data dumping tool
 * 
 * @author Mgamerz
 *
 */
public class PCCDataDumperWindow extends JFrame {
	ArrayList<Path> files;
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
	JCheckBox dumpSWF;
	final int threads = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
	private JPanel dumpPanel;

	/**
	 * Manually invoked pcc data dumping window
	 * 
	 */
	public PCCDataDumperWindow() {
		ModManager.debugLogger.writeMessage("Opening PCCDataDumperWindow");
		setupWindow();
		setVisible(true);
	}

	public PCCDataDumperWindow(ArrayList<Path> files) {
		this.files = files;
		ModManager.debugLogger.writeMessage("Opening PCCDataDumperWindow (with files)");
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setTitle("PCC Data Dumper");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImages(ModManager.ICONS);
		setMinimumSize(new Dimension(200, 400));
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		JLabel[] threadOperationLabels = new JLabel[threads]; //VMs won't be supported obviously
		if (files == null) {
			infoLabel = new JLabel(
					"<html>Select the information you want to dump from game PCCs.<br>To dump specific files, drag and drop a folder containing PCC<br>files or specific PCC files onto the main Mod Manager window.</html>");
		} else {
			infoLabel = new JLabel("<html>Select the information you want to dump from the selected PCCs.<br>Dumping properties of PCCs can take a VERY LONG TIME.</html>");
		}
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
		dumpSWF = new JCheckBox("SWFs (UI files)");

		dumpScripts.setToolTipText("Dumps unrealscript functions with tokenized bytecode.");
		dumpCoalesced.setToolTipText("Dumps a [C] before exports that read their value from coalesced files.");
		dumpProperties.setToolTipText("Dumps properties for each export. Will take a long time to cross reference data.");
		dumpExports.setToolTipText("Dumps the export list. This value is automatically chosen if using properties or data.");
		dumpImports.setToolTipText("Dumps the import list.");
		dumpNames.setToolTipText("Dumps the name table.");
		dumpSWF.setToolTipText(
				"<html>Dumps SWF/GFxMovieInfo exports to SWF files that can be externally modified.<br>SWF files from PCCs will be placed into the dump folder inside a folder with the same name as the PCC, with the full object name as the filename.<br>Read the guide on ME3Tweaks for information on SWF editing.</html>");

		checkBoxPanelLeft.add(dumpNames);
		checkBoxPanelLeft.add(dumpImports);
		checkBoxPanelLeft.add(dumpSWF);

		checkBoxPanelRight.add(dumpExports);
		checkBoxPanelRight.add(dumpCoalesced);
		checkBoxPanelRight.add(dumpScripts);
		checkBoxPanelRight.add(dumpProperties);

		JCheckBox[] checkboxes = new JCheckBox[] { dumpNames, dumpImports, dumpExports, dumpCoalesced, dumpScripts, dumpProperties, dumpSWF };
		for (JCheckBox cb : checkboxes) {
			if (cb != dumpSWF) {
				cb.setSelected(true);
			}
		}
		checkBoxPanel.add(checkBoxPanelLeft, BorderLayout.WEST);
		checkBoxPanel.add(checkBoxPanelRight, BorderLayout.EAST);
		rootPanel.add(checkBoxPanel, BorderLayout.CENTER);

		String text = (files == null ? "Dump PCC information (entire game)" : "Dump PCC information");
		dumpButton = new JButton(text);
		String tooltip = (files == null
				? "<html>Dump information from all PCC files located in the BIOGame directory.<br>Patch_001.sfar will be extracted (but not modified) and dumped as well.</html>"
				: "Dumps information from the PCCs that were used to open this window.");
		dumpButton.setToolTipText(tooltip);
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
				options.swfs = dumpSWF.isSelected();
				options.properties = dumpProperties.isSelected();
				options.outputFolder = ModManager.getPCCDumpFolder();
				for (JCheckBox cb : checkboxes) {
					cb.setEnabled(false);
				}
				//files will be null if this window was not opened from drag and drop
				new DumpPCCJob(options, threadOperationLabels, files).execute();
			}
		});

		dumpPanel = new JPanel(new BorderLayout());
		dumpPanel.add(dumpButton, BorderLayout.CENTER);
		dumpPanel.add(new JLabel("<html><center>Property dumping may take several minutes per PCC.<br>Dumps are placed in the data/PCCDumps folder.</center></html>",
				SwingConstants.CENTER), BorderLayout.SOUTH);
		rootPanel.add(dumpPanel, BorderLayout.SOUTH);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// methods will read this variable
			}
		});
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	class DumpPCCJob extends SwingWorker<ArrayList<DumpTaskResult>, ThreadCommand> {

		private PCCDumpOptions options;
		public AtomicInteger completed = new AtomicInteger(0);
		public int threads = 1;
		public int totalfiles;
		ArrayList<Path> files = new ArrayList<Path>();
		private JLabel[] threadOperationLabels;

		public DumpPCCJob(PCCDumpOptions options, JLabel[] threadOperationLabels, ArrayList<Path> files) {
			this.options = options;
			this.files = files;
			this.threadOperationLabels = threadOperationLabels;
			threads = threadOperationLabels.length;
			dumpButton.setEnabled(false);
		}

		@Override
		protected ArrayList<DumpTaskResult> doInBackground() throws Exception {
			// TODO Auto-generated method stub
			publish(new ThreadCommand("UPDATE_STATUS", "Building list of PCC files..."));
			if (files == null || files.size() == 0) {
				Predicate<Path> predicate = p -> Files.isRegularFile(p) && p.toFile().getName().toLowerCase().endsWith(".pcc");
				files = (ArrayList<Path>) Files.walk(Paths.get(options.gamePath + "\\BIOGame")).filter(predicate).collect(Collectors.toList());

				//Find testpatch, add it
				File testpatch = new File(options.gamePath + "\\BIOGame\\Patches\\PCConsole\\Patch_001.sfar");
				if (testpatch.exists()) {
					//extract readonly
					File testpatchfolder = new File(ModManager.getTempDir() + "\\TESTPATCH_PCCDUMP");
					testpatchfolder.mkdirs();

					ArrayList<String> command = new ArrayList<String>();
					command.add(ModManager.getCommandLineToolsDir() + "SFARTools-Extract.exe");
					command.add("--SFARPath");
					command.add(testpatch.getAbsolutePath());
					command.add("--ExtractEntireArchive");
					command.add("--KeepArchiveIntact");
					command.add("--FlatFolderExtraction");
					command.add("--OutputPath");
					command.add(testpatchfolder.getAbsolutePath());
					ProcessBuilder extractionCommand = new ProcessBuilder(command);
					publish(new ThreadCommand("UPDATE_STATUS", "Extracting TESTPATCH..."));
					ModManager.runProcess(extractionCommand);

					ArrayList<Path> testpatchfiles = (ArrayList<Path>) Files.walk(testpatchfolder.toPath()).filter(predicate).collect(Collectors.toList());
					files.addAll(testpatchfiles);
				}
			}
			ModManager.debugLogger.writeMessage("Number of files to dump: " + files.size());
			publish(new ThreadCommand("UPDATE_STATUS", "Dumping PCC files..."));

			ExecutorService autotocExecutor = Executors.newFixedThreadPool(threads);
			ArrayList<Future<DumpTaskResult>> futures = new ArrayList<Future<DumpTaskResult>>();
			for (Path path : files) {
				//submit jobs
				DumpTask jtask = new DumpTask(path.toString(), options);
				futures.add(autotocExecutor.submit(jtask));
			}
			autotocExecutor.shutdown();
			ArrayList<DumpTaskResult> results = new ArrayList<>();
			ArrayList<DumpTaskResult> retrySingleThreaded = new ArrayList<>();
			try {
				autotocExecutor.awaitTermination(5, TimeUnit.MINUTES);
				for (Future<DumpTaskResult> f : futures) {
					DumpTaskResult pr = f.get();
					if (pr.getResult().getReturnCode() == 0) {
						results.add(pr);
					} else {
						retrySingleThreaded.add(pr);
					}
				}
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeErrorWithException("EXECUTION EXCEPTION WHILE DUMPING FILES: ", e);
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("UNKNOWN EXCEPTION OCCURED: ", e);
			}

			for (DumpTaskResult tr : retrySingleThreaded) {
				String filepath = tr.getFilepath();
				ModManager.debugLogger.writeMessage("Retrying dump in single threaded mode on file: "+filepath);
				String taskname = FilenameUtils.getName(filepath) + " (" + ResourceUtils.humanReadableByteCount(new File(filepath).length(), true) + ")";
				publish(new ThreadCommand("ASSIGN_TASK", taskname));
				ProcessResult pr = ModManager.dumpPCC(filepath, options);
				completed.incrementAndGet();
				publish(new ThreadCommand("RELEASE_TASK", taskname));
				int progressval = (int) ((completed.get() / (files.size() * 1.0) * 100));
				publish(new ThreadCommand("SET_PROGRESS", null, progressval));
				publish(new ThreadCommand("UPDATE_STATUS", "Dumping PCC files... " + completed.get() + " of " + files.size()));
				results.add(new DumpTaskResult(filepath, pr));
			}

			return results;
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
						if (text.startsWith(inUseText)) {
							//mark in use
							label.setText("Thread " + (i + 1) + " - idle");
							break;
						}
					}
					break;
				}
			}
		}

		@Override
		protected void done() {
			try {
				ArrayList<DumpTaskResult> results = get();
				ArrayList<DumpTaskResult> failures = new ArrayList<>();
				for (DumpTaskResult dtr : results) {
					if (dtr.getResult().getReturnCode() != 0) {
						//Something failed..
						failures.add(dtr);
					}
				}
				if (failures.size() > 0) {
					//Something failed.
					ModManager.debugLogger.writeError(failures.size() + " files failed to dump.");
					infoLabel.setText("PCC dumping completed with errors.");
					JOptionPane.showMessageDialog(PCCDataDumperWindow.this, failures.size() + " files failed to dump.\nSave a diagnostics log to disk to view failures.",
							"Completed with errors", JOptionPane.ERROR_MESSAGE);
				} else {
					infoLabel.setText("PCC dumping complete.");
				}
			} catch (ExecutionException | InterruptedException e) {
				ModManager.debugLogger.writeErrorWithException("ERROR DUMPING FILES:", e);
			}
			ModManager.debugLogger.writeMessage("Dumping complete. Dumped " + completed.get() + " files");
			JButton openPCCDumpFolder = new JButton("Open dump folder");
			openPCCDumpFolder.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ResourceUtils.openFolderInExplorer(ModManager.getPCCDumpFolder());
					dispose();
				}
			});
			dumpPanel.remove(dumpButton);
			dumpPanel.add(openPCCDumpFolder, BorderLayout.CENTER);
			revalidate();
		}

		/**
		 * Task for dumping a single PCC file
		 * 
		 * @author Mgamerz
		 *
		 */
		class DumpTask implements Callable<DumpTaskResult> {
			private String filepath;
			private PCCDumpOptions options;

			public DumpTask(String filepath, PCCDumpOptions options) {
				this.filepath = filepath;
				this.options = options;
			}

			@Override
			public DumpTaskResult call() throws Exception {
				String taskname = FilenameUtils.getName(filepath) + " (" + ResourceUtils.humanReadableByteCount(new File(filepath).length(), true) + ")";
				publish(new ThreadCommand("ASSIGN_TASK", taskname));
				ProcessResult pr = ModManager.dumpPCC(filepath, options);
				if (pr.getReturnCode() == 0) {
					completed.incrementAndGet();
				}
				publish(new ThreadCommand("RELEASE_TASK", taskname));
				int progressval = (int) ((completed.get() / (files.size() * 1.0) * 100));
				publish(new ThreadCommand("SET_PROGRESS", null, progressval));
				publish(new ThreadCommand("UPDATE_STATUS", "Dumping PCC files... " + completed.get() + " of " + files.size()));
				return new DumpTaskResult(filepath, pr);
			}
		}

		/**
		 * Result object for the DumpTask Executor Service
		 * 
		 * @author Mgamerz
		 *
		 */
		public class DumpTaskResult {
			private String filepath;
			private ProcessResult result;

			public DumpTaskResult(String filepath, ProcessResult result) {
				this.filepath = filepath;
				this.result = result;
			}

			public String getFilepath() {
				return filepath;
			}

			public ProcessResult getResult() {
				return result;
			}
		}
	}
}
