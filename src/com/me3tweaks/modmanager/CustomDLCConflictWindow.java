package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.StarterKitWindow.StarterKitGenerator;
import com.me3tweaks.modmanager.objects.CustomDLC;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.objects.MountFlag;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.MultiLineTableCell;
import com.me3tweaks.modmanager.utilities.EXEFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Shows conflicts between custom dlc mods.
 * 
 * @author Mgamerz
 *
 */
public class CustomDLCConflictWindow extends JDialog {

	private static final int COL_FILENAME = 0;
	private static final int COL_SUPERCEDING = 1;
	private static final int COL_SUPERCEDED = 2;
	private JProgressBar guiProgressBar;
	private JButton guiPatchButton;
	private String conflictingGUIMod;
	private JPanel progressPanel;
	private JLabel statusText;
	private JPanel windowpanel;
	public String transplanterpath;
	public HashMap<String, CustomDLC> secondPriorityUIConflictFiles;
	public ArrayList<String> blacklistedGUIconflictfiles = new ArrayList<String>(Arrays.asList(new String[] { "BioD_CitCas.pcc", "SFXWeapon_SniperRifle_Collector_LOC_INT.pcc",
			"SFXWeapon_AssaultRifle_Spitfire.pcc", "SFXWeapon_SMG_Collector_LOC_INT.pcc", "Startup_DLC_CON_MAPMOD_INT.pcc" }));

	public CustomDLCConflictWindow() {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setPreferredSize(new Dimension(600, 500));
		setTitle("Custom DLC Conflicts");
		setIconImages(ModManager.ICONS);
		setModalityType(ModalityType.APPLICATION_MODAL);

		// TODO Auto-generated method stub
		//JTextPane tp = new JTextPane();
		String biogameDirectory = ModManager.appendSlash(ModManagerWindow.GetBioGameDir());
		ArrayList<String> installedDLCs = ModManager.getInstalledDLC(biogameDirectory);
		ArrayList<CustomDLC> customDLCs = new ArrayList<CustomDLC>();
		for (String dlc : installedDLCs) {
			File mountFile = new File(biogameDirectory + "DLC/" + dlc + File.separator + "CookedPCConsole/Mount.dlc");
			if (!ModTypeConstants.isKnownDLCFolder(dlc) && dlc.toUpperCase().startsWith("DLC_") && mountFile.exists()) {
				customDLCs.add(new CustomDLC(new MountFile(mountFile.getAbsolutePath()), dlc));
			}
		}

		//get conflicts, create table

		HashMap<String, ArrayList<CustomDLC>> conflicts = ModManager.getCustomDLCConflicts(customDLCs, biogameDirectory + "DLC/");
		HashMap<String, ArrayList<CustomDLC>> mountpriorityconflicts = new HashMap<String, ArrayList<CustomDLC>>();
		for (Map.Entry<String, ArrayList<CustomDLC>> entry : conflicts.entrySet()) {
			if (entry.getValue().size() <= 1) {
				continue; //not priority conflict
			}
			mountpriorityconflicts.put(entry.getKey(), entry.getValue());
		}

		int datasize = mountpriorityconflicts.entrySet().size();
		Object[][] data = new Object[datasize][3];
		int i = 0;
		for (Map.Entry<String, ArrayList<CustomDLC>> entry : mountpriorityconflicts.entrySet()) {
			String key = entry.getKey();
			ArrayList<CustomDLC> value = entry.getValue();

			//write values to table data
			data[i][COL_FILENAME] = key;
			data[i][COL_SUPERCEDING] = value.get(value.size() - 1).getDlcName() + " (" + value.get(value.size() - 1).getMountFile().getMountPriority() + ")";
			String superceeded = "";
			for (int x = value.size() - 2; x >= 0; x--) {
				superceeded += value.get(x).getDlcName() + " (" + value.get(x).getMountFile().getMountPriority() + ")\n";
			}

			data[i][COL_SUPERCEDED] = superceeded;
			i++;
		}

		String[] columnNames = { "Filename", "Superceding DLC", "Superceeded DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		final MultiLineTableCell mltc = new MultiLineTableCell();
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			public TableCellRenderer getCellRenderer(int row, int column) {
				if (column == COL_SUPERCEDED) {
					return mltc;
				} else {
					return super.getCellRenderer(row, column);
				}
			}
		};
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(COL_FILENAME).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(COL_SUPERCEDED).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(COL_SUPERCEDING).setCellRenderer(centerRenderer);

		JScrollPane scrollpane = new JScrollPane(table);
		windowpanel = new JPanel(new BorderLayout());

		String buttonText = "<html><center>Files listed below are Custom DLC files that have conflicts.<br>The Custom DLC with the highest mount priority will supercede others, and may cause the the superceded DLC to not work or cause game instability.<br>You can toggle Custom DLC in the Custom DLC Manager in the Restore Menu.</center></html>";
		JLabel infoLinkButton = new JLabel(buttonText);
		infoLinkButton.setText(buttonText);
		infoLinkButton.setHorizontalAlignment(SwingConstants.CENTER);
		/*
		 * infoLinkButton.setBorderPainted(false);
		 * infoLinkButton.setBackground(UIManager.getColor("Panel.background"));
		 * infoLinkButton.setFocusPainted(false); infoLinkButton.setMargin(new
		 * Insets(0, 0, 0, 0)); infoLinkButton.setContentAreaFilled(false);
		 * infoLinkButton.setBorderPainted(false);
		 * infoLinkButton.setOpaque(false); infoLinkButton.setCursor(new
		 * Cursor(Cursor.HAND_CURSOR)); infoLinkButton.
		 * setToolTipText("Click for info on how to toggle Custom DLC mods in Mod Manager"
		 * ); infoLinkButton.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) {
		 * JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, message,
		 * "Toggling Custom DLC in Mod Manager",
		 * JOptionPane.INFORMATION_MESSAGE); } });
		 */
		windowpanel.add(infoLinkButton, BorderLayout.NORTH);
		windowpanel.add(scrollpane, BorderLayout.CENTER);

		guiPatchButton = new JButton("UI mod is conflicting with other mods", UIManager.getIcon("OptionPane.warningIcon"));
		guiPatchButton.setToolTipText(
				"<html>Mod Manager has detected that an installed GUI mod (SP Controller Support or Interface Scaling) is superceding other installed Custom DLCs.<br>Click for more info.</html>");
		guiPatchButton.setVisible(false);
		guiPatchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(CustomDLCConflictWindow.this,
						"An interface mod is superceding other installed Custom DLC mods, or another mod is using interfaces\nthat won't work with controllers.\n"
								+ "These conflicts may cause game instability or you to become locked in an interface.\n"
								+ "Mod Manager can generate a new mod that will inject controller interfaces into these files.\n"
								+ "This generated mod will only apply to the current versions of the conflicting mods, so\n"
								+ "if you update any of them, you must uninstall the generated mod and regenerate a new one.\n\n" + "Generate a GUI conflict compatibility mod?",
						"Generate a compatibilty mod", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					//generate
					String modName = "MM GUI Compatibility";
					boolean nameIsBad = true;
					while (nameIsBad) {
						String whatTheUserEntered = JOptionPane.showInputDialog(CustomDLCConflictWindow.this,
								"Enter a name for the compatibilty mod.\nAlphanumeric (spaces/underscore allowed) only, no more than 20 characters.", "Enter mod name",
								JOptionPane.QUESTION_MESSAGE);
						if (whatTheUserEntered == null) {
							return;
						}
						whatTheUserEntered = whatTheUserEntered.trim();
						//space, and alphanumerics
						boolean asciionly = whatTheUserEntered.chars()
								.allMatch(c -> c == 0x20 || c == 0x5F || (c > 0x30 && c < 0x3A) || (c > 0x40 && c < 0x5B) || (c > 0x60 && c < 0x7B)); //what the f is this?
						if (!asciionly) {
							ModManager.debugLogger.writeError("Name is not ascii alphanumeric only: " + whatTheUserEntered);
							continue;
						}
						if (whatTheUserEntered.length() > 20 || whatTheUserEntered.length() < 1) {
							ModManager.debugLogger.writeError("Name is empty or too long: " + whatTheUserEntered);
							continue;
						}

						//check if already exists
						File patchFolder = new File(ModManager.getModsDir() + whatTheUserEntered);
						if (patchFolder.exists() && patchFolder.isDirectory()) {
							ModManager.debugLogger.writeError("Mod already exists, prompting user to overwrite: " + patchFolder);
							String[] options = new String[] { "Delete existing mod", "Enter a new name" };
							int renameresult = JOptionPane.showOptionDialog(CustomDLCConflictWindow.this, "A mod named " + whatTheUserEntered + " already exists in Mod Manager.",
									"Name conflict", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							if (renameresult == JOptionPane.NO_OPTION) {
								continue;
							} else {
								FileUtils.deleteQuietly(patchFolder);
							}
						}

						modName = whatTheUserEntered.trim();
						nameIsBad = false;
					}
					progressPanel.setVisible(true);
					guiPatchButton.setVisible(false);
					GUICompatGeneratorThread gcgt = new GUICompatGeneratorThread(modName, biogameDirectory, secondPriorityUIConflictFiles);
					gcgt.execute();
				}
			}
		});

		JPanel guiPatchPanel = new JPanel(new BorderLayout());
		guiPatchPanel.add(guiPatchButton, BorderLayout.NORTH);

		progressPanel = new JPanel(new BorderLayout());
		statusText = new JLabel("Preparing to create compatibilty mod", SwingConstants.CENTER);
		guiProgressBar = new JProgressBar();
		guiProgressBar.setVisible(true);
		guiProgressBar.setIndeterminate(true);
		progressPanel.add(guiProgressBar, BorderLayout.NORTH);
		progressPanel.add(statusText, BorderLayout.SOUTH);
		progressPanel.setVisible(false);
		guiPatchPanel.add(progressPanel, BorderLayout.SOUTH);
		windowpanel.add(guiPatchPanel, BorderLayout.SOUTH);

		if (ModManager.isGUIModInstalled(biogameDirectory)) {
			new CustomDLCGUIScanner(biogameDirectory, conflicts).execute();
		}

		windowpanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(windowpanel);
		pack();
	}

	class CustomDLCGUIScanner extends SwingWorker<Void, ThreadCommand> {

		private HashMap<String, ArrayList<CustomDLC>> dlcfilemap;
		private String biogameDirectory;
		private double numFilesToScan;
		private final AtomicInteger numFilesScanned = new AtomicInteger();

		public CustomDLCGUIScanner(String biogameDirectory, HashMap<String, ArrayList<CustomDLC>> conflicts) {
			this.dlcfilemap = conflicts;
			this.biogameDirectory = biogameDirectory;
			progressPanel.setVisible(true);
			statusText.setText("Scanning Custom DLC files for GUI conflicts");
		}

		@Override
		protected Void doInBackground() throws Exception {
			secondPriorityUIConflictFiles = detectUIModConflicts(dlcfilemap);
			HashMap<String, ArrayList<CustomDLC>> guiFilesWithNoSuperceding = detectNewFilesSupercedingUI(dlcfilemap);
			ModManager.debugLogger.writeMessage("The following items will be transplanted into if the user accepts UI transplants:");
			for (Map.Entry<String, CustomDLC> secondtier : secondPriorityUIConflictFiles.entrySet()) {
				ModManager.debugLogger.writeMessage(secondtier.getValue().getDlcName() + " " + secondtier.getKey());
			}
			if (guiFilesWithNoSuperceding != null) {

				ArrayList<String> knownGUImods = new ArrayList<String>(Arrays.asList(ModManager.KNOWN_GUI_CUSTOMDLC_MODS));
				for (Entry<String, ArrayList<CustomDLC>> entry : guiFilesWithNoSuperceding.entrySet()) {
					//add conflicts to gui conflict list since they have guis and aren't overridden by gui mod
					//find highest priority non gui mod in list...
					//ModManager.debugLogger.writeMessage("Enumerating file "+entry.getKey());
					ArrayList<CustomDLC> dlcs = entry.getValue();
					for (int i = dlcs.size() - 1; i >= 0; i--) {
						CustomDLC tier = dlcs.get(i);
						if (knownGUImods.contains(tier.getDlcName())) {
							ModManager.debugLogger.writeMessage("- Skip tier " + tier.getDlcName());
							continue;
						} else {
							ModManager.debugLogger.writeMessage(entry.getValue().get(i) + " " + entry.getKey());
							secondPriorityUIConflictFiles.put(entry.getKey(), entry.getValue().get(i)); //used to be 0 in versions < BUILD 62 PREVIEW 6. I think this used to cause it to choose the lowest priority conflict rather than the highest
							break;
						}
					}
				}
			}
			return null;
		}

		protected void done() {
			guiProgressBar.setVisible(false);
			if (secondPriorityUIConflictFiles != null) {
				guiPatchButton.setVisible(true);
				statusText.setText("A GUI mod needs to update " + secondPriorityUIConflictFiles.entrySet().size() + " files to work properly");
			} else {
				guiPatchButton.setVisible(false);
				statusText.setText("No GUI mod file conflicts");
			}

		}

		/**
		 * Detects files that contain interfaces but are not superceded by a UI
		 * DLC
		 * 
		 * @param dlcfilemap
		 * @return
		 */
		private HashMap<String, ArrayList<CustomDLC>> detectNewFilesSupercedingUI(HashMap<String, ArrayList<CustomDLC>> dlcfilemap) {
			// TODO Auto-generated method stub
			transplanterpath = ModManager.getGUITransplanterCLI(true);
			if (transplanterpath == null) {
				return null;
			}
			HashMap<String, ArrayList<CustomDLC>> nonguimodfiles = new HashMap<>();
			//filter out files where the gui mod is superceding, we don't care.
			for (Map.Entry<String, ArrayList<CustomDLC>> entry : dlcfilemap.entrySet()) {
				if (entry.getValue().get(entry.getValue().size() - 1).isGUIMod() || blacklistedGUIconflictfiles.contains(entry.getKey())) {
					//ignores heavy MP4/MP5 files that dont have any actual GUIs.
					continue; //not conflict... may be bad logic if two mods have same new file for some reason though. //used to have if size is not zero pre 62
				}
				nonguimodfiles.put(entry.getKey(), entry.getValue());
			}
			numFilesToScan = nonguimodfiles.size();
			int cores = Runtime.getRuntime().availableProcessors();
			cores = Math.max(1, cores - 1);
			ModManager.debugLogger.writeMessage("GUI Conflict scanner will use " + cores + " threads.");
			ExecutorService guiscanExecutor = Executors.newFixedThreadPool(cores);
			ArrayList<Future<GUIScanResult>> futures = new ArrayList<Future<GUIScanResult>>();
			//submit jobs
			for (Map.Entry<String, ArrayList<CustomDLC>> entry : nonguimodfiles.entrySet()) {
				GUIScanTask gst = new GUIScanTask(entry.getKey(), entry.getValue());
				futures.add(guiscanExecutor.submit(gst));
			}
			guiscanExecutor.shutdown();
			HashMap<String, ArrayList<CustomDLC>> returnconflictfiles = new HashMap<>();
			try {
				guiscanExecutor.awaitTermination(5, TimeUnit.MINUTES);
				for (Future<GUIScanResult> f : futures) {
					GUIScanResult result = f.get();
					if (result == null) {
						ModManager.debugLogger.writeError("A file failed to scan.");
						throw new Exception("One or more files failed to scan.");
					}
					if (result.hasGUI) {
						returnconflictfiles.put(result.filename, result.dlcs);
					}
				}
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeErrorWithException("EXECUTION EXCEPTION WHILE SCANNING FILE (AFTER EXECUTOR FINISHED): ", e);
				return null;
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("UNKNOWN EXCEPTION OCCURED: ", e);
				return null;
			}
			return returnconflictfiles;
		}

		class GUIScanTask implements Callable<GUIScanResult> {
			private String filepath;
			private ArrayList<CustomDLC> dlcs;

			public GUIScanTask(String filepath, ArrayList<CustomDLC> dlcs) {
				this.filepath = filepath;
				this.dlcs = dlcs;
			}

			@Override
			public GUIScanResult call() throws Exception {
				//scan file
				String scanFile = biogameDirectory + "DLC/" + dlcs.get(0).getDlcName() + "/CookedPCConsole/" + filepath;
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(transplanterpath);
				commandBuilder.add("--guiscan");
				commandBuilder.add("--inputfile");
				commandBuilder.add(ResourceUtils.normalizeFilePath(scanFile, true));
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				ModManager.debugLogger.writeMessage("[" + FilenameUtils.getBaseName(scanFile) + "]Scanning for GFxMovieInfo export: " + scanFile);
				int returncode = 2;
				ProcessBuilder pb = new ProcessBuilder(command);
				returncode = ModManager.runProcess(pb, FilenameUtils.getBaseName(scanFile)).getReturnCode();

				publish(new ThreadCommand("UPDATE_PROGRESS", null, numFilesScanned.incrementAndGet()));
				if (returncode == 1) {
					//FILE CONTAINS GUI!
					ModManager.debugLogger.writeMessage("[" + FilenameUtils.getBaseName(scanFile) + "]GUI Export found in non-GUI mod while GUI mod is installed: " + scanFile);
					return new GUIScanResult(true, filepath, dlcs);
				}
				return new GUIScanResult(false, filepath, dlcs);
			}
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand tc : chunks) {
				String command = tc.getCommand();
				switch (command) {
				case "SHOW_SCANNER_PROGRESSBAR":
					guiProgressBar.setVisible(true);
					break;
				case "UPDATE_SCANNER_TEXT":

					break;
				case "UPDATE_PROGRESS":
					guiProgressBar.setIndeterminate(false);
					guiProgressBar.setVisible(true);
					guiProgressBar.setValue((int) ((int) tc.getData() * 100 / numFilesToScan));
					break;
				}
			}
		}

		class GUIScanResult {
			private boolean hasGUI;
			private String filename;
			private ArrayList<CustomDLC> dlcs;

			public GUIScanResult(boolean hasGUI, String filename, ArrayList<CustomDLC> dlcs) {
				super();
				this.hasGUI = hasGUI;
				this.filename = filename;
				this.dlcs = dlcs;
			}

			public boolean isHasGUI() {
				return hasGUI;
			}

			public void setHasGUI(boolean hasGUI) {
				this.hasGUI = hasGUI;
			}

			public String getFilename() {
				return filename;
			}

			public void setFilename(String filename) {
				this.filename = filename;
			}

			public GUIScanResult() {

			}
		}
	}

	class GUICompatGeneratorThread extends SwingWorker<Boolean, ThreadCommand> {

		private HashMap<String, CustomDLC> secondPriorityUIConflictFiles;
		private String modName;
		private Mod outputMod;
		private String biogameDirectory;
		private Lock lock = new ModManager.Lock();
		private boolean userAcceptedFirstFailMessage = false;

		public GUICompatGeneratorThread(String modName, String biogameDirectory, HashMap<String, CustomDLC> secondPriorityUIConflictFiles) {
			this.modName = modName;
			this.biogameDirectory = biogameDirectory;
			this.secondPriorityUIConflictFiles = secondPriorityUIConflictFiles;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			String guilibrarypath = ModManager.getGUILibraryFor(conflictingGUIMod, true);
			transplanterpath = ModManager.getGUITransplanterCLI(true);
			if (guilibrarypath == null) {
				ModManager.debugLogger.writeError("GUI LIBRARY IS MISSING: " + conflictingGUIMod);
				publish(new ThreadCommand("MISSING_GUI_LIBRARY"));
				return false;
			}
			if (transplanterpath == null) {
				ModManager.debugLogger.writeError("Unable to acquire GUI transplanter");
				publish(new ThreadCommand("MISSING_TRANSPLANTER"));
				return false;
			} else {
				//check version
				int version = EXEFileInfo.getRevisionOfProgram(transplanterpath);
				if (version < ModManager.MIN_REQUIRED_CMDLINE_REV) {
					ModManager.debugLogger
							.writeError("Outdated transplanter detected - aborting install. Local: " + version + ", required: 1.0.0." + ModManager.MIN_REQUIRED_CMDLINE_REV);
					publish(new ThreadCommand("OUTDATED_TRANSPLANTER", Integer.toString(version)));
					return false;
				}

			}

			String internalName = modName.toUpperCase().replaceAll(" ", "_");
			ModManager.debugLogger.writeMessage("Compatibility pack will be named DLC_MOD_" + internalName);
			StarterKitWindow.StarterKitGenerator skg = new StarterKitGenerator(guiPatchButton, progressPanel, CustomDLCConflictWindow.this);
			skg.setInternaldisplayname("GUI Compatibility Pack from Mod Manager " + ModManager.BUILD_NUMBER);
			skg.setMountpriority(31050);
			skg.setModdev("Mod Manager Build " + ModManager.BUILD_NUMBER);
			skg.setMountflag(new MountFlag(null, 0x8));
			skg.setInternaldlcname(internalName);
			String desc = "User generated compatibility pack made to inject proper interface files into the conflicting files from the following mods:\n";

			TreeSet<String> conflictingDLC = new TreeSet<String>();
			for (CustomDLC dlc : secondPriorityUIConflictFiles.values()) {
				conflictingDLC.add(dlc.getDlcName());
			}
			for (String str : conflictingDLC) {
				desc += " - " + str + "\n";
			}
			desc += "\nThis pack was generated at ";
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			desc += dateFormat.format(date);
			desc += " with Mod Manager " + ModManager.VERSION + " - Build " + ModManager.BUILD_NUMBER + ".";

			skg.setModdesc(desc);
			skg.setModname(modName);

			//random tlk
			Random rand = new Random();
			int n = (rand.nextInt(100) * 100) + 13370000;
			skg.setTlkid(n);
			ModManager.debugLogger.writeMessage("Running StarterKitGenerator. Thread will suspend until that thread has completed");

			skg.execute();
			publish(new ThreadCommand("SET_STATUS_TEXT", "Generating Starter Kit for " + modName));
			publish(new ThreadCommand("SET_PROGRESSBAR_VISIBLE"));

			synchronized (skg.lock) {
				while (!skg.completed) {
					try {
						skg.lock.wait();
					} catch (InterruptedException ex) {
						ModManager.debugLogger.writeErrorWithException("Unable to wait for for starter kit to finish:", ex);
					}
				}
			}
			publish(new ThreadCommand("SET_PROGRESSBAR_VISIBLE"));
			ModManager.debugLogger.writeMessage("StarterKit should have finished, resuming compat generator");
			if (skg.getGeneratedMod().getModPath() == null) {
				//something went wrong
				ModManager.debugLogger.writeError("Generated mod path is null, something went wrong!");
				publish(new ThreadCommand("ERROR_NO_STARTER_MOD"));
				return false;
			}

			publish(new ThreadCommand("SET_STATUS_TEXT", "Copying tier 2 files to new mod"));
			//starter kit has finished. Copy files to it.

			ArrayList<String> transplantFiles = new ArrayList<>();
			double i = 0;
			int filesToCopy = secondPriorityUIConflictFiles.entrySet().size();
			for (Map.Entry<String, CustomDLC> resolutionFile : secondPriorityUIConflictFiles.entrySet()) {
				publish(new ThreadCommand("SET_PROGRESS", null, i / filesToCopy));
				String sourcePath = biogameDirectory + "DLC/" + resolutionFile.getValue().getDlcName() + "/CookedPCConsole/" + resolutionFile.getKey();
				String copyTargetPath = skg.getGeneratedMod().getModPath() + "DLC_MOD_" + internalName + "/CookedPCConsole/" + resolutionFile.getKey();
				//	try {
				ModManager.debugLogger.writeMessage("Decompressing 2nd tier conflict file: " + sourcePath + " => " + copyTargetPath);
				String hash = MD5Checksum.getMD5Checksum(sourcePath);
				ModManager.debugLogger.writeMessage("Hash of " + sourcePath + ": " + hash);
				//FileUtils.copyFile(new File(sourcePath), new File(copyTargetPath));
				ModManager.decompressPCC(new File(sourcePath), new File(copyTargetPath));
				transplantFiles.add(copyTargetPath);
				i++;
			}

			//publish(new ThreadCommand("SET_STATUS_TEXT", "Locating GUI library"));

			ModManager.debugLogger.writeMessage("Copy of 2nd tier fields completed.");

			//Run ME3-GUI-Transplanter over CookedPCConsole files
			i = 0;
			if (ModManager.COMPRESS_COMPAT_OUTPUT) {
				ModManager.debugLogger.writeMessage("Output files will be compressed (--Compress)");
			}
			for (String transplantFile : transplantFiles) {
				publish(new ThreadCommand("SET_PROGRESS", null, i / transplantFiles.size()));

				boolean isBlacklisted = false;
				for (String doNotTransplantInto : blacklistedGUIconflictfiles) {
					if (FilenameUtils.getName(transplantFile).equalsIgnoreCase(doNotTransplantInto)) {
						ModManager.debugLogger.writeMessage("Ignoring blacklisted transplant target: " + doNotTransplantInto + " - Promoting it directly above GUI mods.");
						isBlacklisted = true;
						break; //This file is blacklisted - just leave it be.
					}
				}
				if (isBlacklisted) {
					i++;
					continue;
				}
				/*
				 * if (new File(transplantFile).getName().equals(
				 * "BioD_Nor_203aGalaxyMap.pcc")) { i++; ModManager.debugLogger.
				 * writeMessage("Skipping tranpslant of BioD_Nor_203aGalaxyMap.pcc for crash prevention."
				 * ); continue; }
				 */
				publish(new ThreadCommand("SET_STATUS_TEXT", "Transplanting SWFs into " + new File(transplantFile).getName()));
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(transplanterpath);
				commandBuilder.add("--injectswf");
				commandBuilder.add("--inputfolder");
				commandBuilder.add(ResourceUtils.normalizeFilePath(guilibrarypath, true));
				commandBuilder.add("--targetfile");
				commandBuilder.add(ResourceUtils.normalizeFilePath(transplantFile, true));
				if (ModManager.COMPRESS_COMPAT_OUTPUT) {
					commandBuilder.add("--compress");
				}
				//commandBuilder.add("-v");
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				ModManager.debugLogger.writeMessage("Injecting SWFs into " + transplantFile);
				int returncode = 1;
				ProcessBuilder pb = new ProcessBuilder(command);
				returncode = ModManager.runProcess(pb).getReturnCode();
				if (returncode != 0) {
					//ERROR!
					ModManager.debugLogger.writeError("GUI Transplanter returned non zero code, PCC failed to verify. Aborting");
					publish(new ThreadCommand("SET_STATUS_TEXT", "Transplanted PCC failed to verify"));
					publish(new ThreadCommand("ERROR_PCC_VERIFY_FAILED", FilenameUtils.getName(transplantFile)));
					ModManager.debugLogger.writeError("UI Mod Compat Builder has encountered a critical error. Performing cleanup procedures.");
					//FileUtils.deleteQuietly(new File(skg.getGeneratedMod().getModPath()));
					synchronized (lock) {
						while (!userAcceptedFirstFailMessage) {
							lock.wait();
						}
					}
					return false;
				}
				i++;
			}

			//Remove .bak files
			publish(new ThreadCommand("SET_STATUS_TEXT", "Deleting .bak files"));
			List<File> files = (List<File>) FileUtils.listFiles(new File(skg.getGeneratedMod().getModPath() + "DLC_MOD_" + internalName), new String[] { "bak" }, true);
			for (File file : files) {
				FileUtils.deleteQuietly(file);
			}

			//Run autotoc
			publish(new ThreadCommand("SET_STATUS_TEXT", "Running AutoTOC on new mod"));
			publish(new ThreadCommand("SET_PROGRESS", null, 1.0));
			ArrayList<String> commandBuilder = new ArrayList<String>();
			// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
			commandBuilder.add(ModManager.getCommandLineToolsDir() + "FullAutoTOC.exe");
			commandBuilder.add("--tocfolders");
			commandBuilder.add(skg.getGeneratedMod().getModPath() + "DLC_MOD_" + internalName);
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			ModManager.debugLogger.writeMessage("Running AutoTOC on newly created mod.");
			ProcessBuilder pb = new ProcessBuilder(command);
			ProcessResult pr = ModManager.runProcess(pb);
			int returncode = pr.getReturnCode();
			if (returncode != 0 || pr.hadError()) {
				ModManager.debugLogger.writeError("FullAutoTOC returned a non 0 code (or threw error) running AutoTOC: " + returncode);
			}
			outputMod = skg.getGeneratedMod();
			String workspaceFolder = outputMod.getModPath() + "WORKSPACE/";
			boolean deletedWS = FileUtils.deleteQuietly(new File(workspaceFolder));
			if (deletedWS) {
				ModManager.debugLogger.writeMessage("Deleted workspace folder from starter kit generated mod");
			}
			return true;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand tc : chunks) {
				String command = tc.getCommand();
				switch (command) {
				case "SET_PROGRESSBAR_VISIBLE":
					progressPanel.setVisible(true);
					guiPatchButton.setVisible(false);
					break;
				case "SET_STATUS_TEXT":
					statusText.setText(tc.getMessage());
					break;
				case "MISSING_GUI_LIBRARY":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
							"Unable to aquire the required GUI library.\nMake sure you are online so Mod Manager can download it if necessary.", "Missing GUI Library",
							JOptionPane.ERROR_MESSAGE);
					break;
				case "MISSING_TRANSPLANTER":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
							"Unable to aquire the required GUI transplanting tool.\nMake sure you are online so Mod Manager can download it if necessary.", "Missing GUI Library",
							JOptionPane.ERROR_MESSAGE);
					break;
				case "OUTDATED_TRANSPLANTER":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
							"To build GUI compatibility packages, you need to have Transplanter-CLI 1.0.0." + ModManager.MIN_REQUIRED_CMDLINE_REV
									+ " or higher.\nYour local version is 1.0.0." + tc.getMessage()
									+ ".\nThis tool is included as part of Mod Manager Command Line Tools. Check the version of the file in data/tools/ModManagerCommandLine.",
							"Outdated GUI Transplanter", JOptionPane.ERROR_MESSAGE);
					break;
				case "ERROR_PCC_VERIFY_FAILED":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
							"PCC failed to verify while transplanting: " + tc.getMessage()
									+ "\nInstalling this mod with this file would crash your game when it loaded.\nBuilding this compatibility pack has been aborted.",
							"PCC failed to verify", JOptionPane.ERROR_MESSAGE);
					userAcceptedFirstFailMessage = true;
					synchronized (lock) {
						lock.notifyAll();
					}
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Unable to build GUI compatibility mod");
					break;
				case "ERROR_FILE_COPY_INTO_COMPAT":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, "Error copying conflicting files into the new mod.", "Missing GUI Library",
							JOptionPane.ERROR_MESSAGE);
					break;
				case "ERROR_NO_STARTER_MOD":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, "Starter Kit failed to generate a mod.", "Starter Kit Generator failed", JOptionPane.ERROR_MESSAGE);
					break;
				case "SET_PROGRESS":
					guiProgressBar.setIndeterminate(false);
					Double val = (double) tc.getData();
					val *= 100;
					guiProgressBar.setVisible(true);
					guiProgressBar.setValue(val.intValue());
					break;
				}
			}
		}

		@Override
		protected void done() {
			boolean successful = false;
			try {
				successful = get();
			} catch (Exception e) {
				ModManager.debugLogger.writeException(e);
			}
			if (successful) {
				ModManager.debugLogger.writeMessage(modName + " created. Showing user directions on how to generate a new one.");
				statusText.setText(modName + " has been created");
				dispose();
				ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
				ModManagerWindow.ACTIVE_WINDOW.highlightMod(outputMod);
				JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, "Compatibility mod has been created.\nApply " + modName
						+ " to fix the UI overriding conflicts.\n\nIf you update any of your conflicting mods, uninstall this mod then generate a new compatibilty mod.\nGenerating a compatibiilty pack while "
						+ modName + " is installed will likely crash the game when the new mod is applied.", "Mod created", JOptionPane.INFORMATION_MESSAGE);
			} else {
				ModManager.debugLogger.writeError("Failed to create compatibility mod - see above logs.");
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Failed to generate compatibility mod");
				statusText.setText(modName + " was not created");
				JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
						"An error occured while generating the compatibility mod.\nOpen the log viewer to find more detailed information.\n\nIf you continue to have issues, please contact FemShep (see the help menu)\nand attach the log to your message.",
						"Mod not created", JOptionPane.ERROR_MESSAGE);
				dispose();
			}
		}
	}

	/**
	 * Detects if the conflicts are caused by one of the following mods: -
	 * DLC_CON_XBX (SP Controller Support) - DLC_CON_UIScaling (Interface
	 * Scaling Mod) - DLC_CON_UIScaling_Shared (Interface Scaling Add-On).
	 * 
	 * 
	 * @param conflicts
	 *            hashmap of conflict files and the list of dlc they appear in
	 * @return null if no UI mod conflicts, otherwise a map of the next
	 *         superceeding Custom DLC mapped to its file.
	 */
	private HashMap<String, CustomDLC> detectUIModConflicts(HashMap<String, ArrayList<CustomDLC>> conflicts) {
		ArrayList<String> knownGUImods = new ArrayList<String>(Arrays.asList(ModManager.KNOWN_GUI_CUSTOMDLC_MODS));
		HashMap<String, CustomDLC> secondPriorityUIConflictFiles = new HashMap<>();
		for (Map.Entry<String, ArrayList<CustomDLC>> entry : conflicts.entrySet()) {
			ArrayList<CustomDLC> conflictingDLCs = entry.getValue();
			if (conflictingDLCs.size() > 1) {
				CustomDLC str = conflictingDLCs.get(conflictingDLCs.size() - 1);
				if (knownGUImods.contains(str.getDlcName())) {
					ModManager.debugLogger.writeMessage("GUI mod " + str + " superceeding: " + entry.getKey());
					conflictingGUIMod = conflictingDLCs.get(conflictingDLCs.size() - 1).getDlcName();
					secondPriorityUIConflictFiles.put(entry.getKey(), conflictingDLCs.get(conflictingDLCs.size() - 2));
				}
			}
		}
		if (secondPriorityUIConflictFiles.size() > 0) {
			return secondPriorityUIConflictFiles;
		}
		return null;
	}
}
