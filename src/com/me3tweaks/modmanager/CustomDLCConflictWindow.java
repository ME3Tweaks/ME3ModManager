package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Cursor;
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
import java.util.Random;
import java.util.TreeSet;

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

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.StarterKitWindow.StarterKitGenerator;
import com.me3tweaks.modmanager.objects.CustomDLC;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.objects.MountFlag;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.ThreadCommand;

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

	public CustomDLCConflictWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setPreferredSize(new Dimension(500, 500));
		setTitle("Custom DLC Conflicts");
		setIconImages(ModManager.ICONS);
		setModalityType(ModalityType.APPLICATION_MODAL);

		// TODO Auto-generated method stub
		//JTextPane tp = new JTextPane();
		String biogameDirectory = ModManager.appendSlash(ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
		ArrayList<String> installedDLCs = ModManager.getInstalledDLC(biogameDirectory);
		ArrayList<CustomDLC> customDLCs = new ArrayList<CustomDLC>();
		for (String dlc : installedDLCs) {
			File mountFile = new File(biogameDirectory + "DLC/" + dlc + File.separator + "CookedPCConsole/Mount.dlc");
			if (!ModType.isKnownDLCFolder(dlc) && dlc.startsWith("DLC_") && mountFile.exists()) {
				customDLCs.add(new CustomDLC(new MountFile(mountFile.getAbsolutePath()), dlc));
			}
		}

		//get conflicts, create table

		HashMap<String, ArrayList<CustomDLC>> conflicts = ModManager.getCustomDLCConflicts(customDLCs, biogameDirectory + "DLC/");
		int datasize = conflicts.entrySet().size();
		Object[][] data = new Object[datasize][3];
		int i = 0;
		for (Map.Entry<String, ArrayList<CustomDLC>> entry : conflicts.entrySet()) {
			String key = entry.getKey();
			ArrayList<CustomDLC> value = entry.getValue();

			//write values to table data
			data[i][COL_FILENAME] = key;
			data[i][COL_SUPERCEDING] = value.get(value.size() - 1).getDlcName();
			String superceeded = "";
			for (int x = 0; x <= value.size() - 2; x++) {
				superceeded += value.get(x).getDlcName() + " ";
			}

			data[i][COL_SUPERCEDED] = superceeded;
			i++;
		}

		String[] columnNames = { "Filename", "Superceding DLC", "Superceeded DLC" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable(model) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(COL_FILENAME).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(COL_SUPERCEDED).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(COL_SUPERCEDING).setCellRenderer(centerRenderer);

		JScrollPane scrollpane = new JScrollPane(table);

		JPanel panel = new JPanel(new BorderLayout());

		String buttonText = "<html><center>Files listed below are Custom DLC files that have conflicts.<br>The Custom DLC with the highest mount priority will supercede others, and may cause the the superceded DLC to not work or cause game instability.<br><u><font color='#000099'>Click for info on how to toggle DLC in Mod Manager.</u></font></center></html>";
		String message = "<html>To toggle Custom DLCs in Mod Manager, make sure all Custom DLC has been imported into Mod Manager.<br>This can be done through the Mod Management > Import Mods > Import installed Custom DLC mod.<br>Once imported, you can install it by simply applying the mod.<br>You can remove (disable) a mod by deleting it through the Custom DLC Manager, and then apply it again to enable it.</html>";
		JButton infoLinkButton = new JButton();
		infoLinkButton.setText(buttonText);
		infoLinkButton.setHorizontalAlignment(SwingConstants.CENTER);
		infoLinkButton.setBorderPainted(false);
		infoLinkButton.setBackground(UIManager.getColor("Panel.background"));
		infoLinkButton.setFocusPainted(false);
		infoLinkButton.setMargin(new Insets(0, 0, 0, 0));
		infoLinkButton.setContentAreaFilled(false);
		infoLinkButton.setBorderPainted(false);
		infoLinkButton.setOpaque(false);
		infoLinkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		infoLinkButton.setToolTipText("Click for info on how to toggle Custom DLC mods in Mod Manager");
		infoLinkButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, message, "Toggling Custom DLC in Mod Manager", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		panel.add(infoLinkButton, BorderLayout.NORTH);
		panel.add(scrollpane, BorderLayout.CENTER);

		HashMap<String, CustomDLC> secondPriorityUIConflictFiles = detectUIModConflicts(conflicts);
		if (secondPriorityUIConflictFiles != null) {
			guiPatchButton = new JButton("UI mod is conflicting with other mods", UIManager.getIcon("OptionPane.warningIcon"));
			guiPatchButton.setToolTipText(
					"<html>Mod Manager has detected that an installed GUI mod (SP Controller Support or Interface Scaling) is superceding other installed Custom DLCs.<br>Click for more info.</html>");

			guiProgressBar = new JProgressBar();
			guiProgressBar.setVisible(true);
			guiProgressBar.setIndeterminate(true);
			guiPatchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(CustomDLCConflictWindow.this,
							"An interface mod is superceding other installed Custom DLC mods.\n" + "This will disable features of those mods, which may cause game instability.\n"
									+ "Mod Manager can generate a new mod that will use those superceding files with your interface\n"
									+ "mod's UI files, which will fix this specific issue.\n\n"
									+ "This generated mod will only apply to the current versions of the conflicting mods.\n"
									+ "If you update any of them, you must uninstall the generated mod and regenerate it again.\n\n" + "Generate a GUI conflict compatibility mod?",
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
								int renameresult = JOptionPane.showOptionDialog(CustomDLCConflictWindow.this,
										"A mod named " + whatTheUserEntered + " already exists in Mod Manager.", "Name conflict", JOptionPane.OK_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
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
			progressPanel.add(guiProgressBar, BorderLayout.NORTH);
			progressPanel.add(statusText, BorderLayout.SOUTH);
			progressPanel.setVisible(false);
			guiPatchPanel.add(progressPanel, BorderLayout.SOUTH);
			panel.add(guiPatchPanel, BorderLayout.SOUTH);
		}
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
	}

	class GUICompatGeneratorThread extends SwingWorker<Boolean, ThreadCommand> {

		private HashMap<String, CustomDLC> secondPriorityUIConflictFiles;
		private String modName;
		private String biogameDirectory;

		public GUICompatGeneratorThread(String modName, String biogameDirectory, HashMap<String, CustomDLC> secondPriorityUIConflictFiles2) {
			this.modName = modName;
			this.biogameDirectory = biogameDirectory;
			this.secondPriorityUIConflictFiles = secondPriorityUIConflictFiles2;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			String guilibrarypath = ModManager.getGUILibraryFor(conflictingGUIMod);
			String transplanterpath = ModManager.getGUITransplanterCLI();
			if (guilibrarypath == null) {
				publish(new ThreadCommand("MISSING_GUI_LIBRARY"));
				return false;
			}
			if (transplanterpath == null) {
				publish(new ThreadCommand("MISSING_TRANSPLANTER"));
				return false;
			}

			String internalName = modName.toUpperCase().replaceAll(" ", "_");
			ModManager.debugLogger.writeMessage("Compatibility pack will be named DLC_CON_" + internalName);
			StarterKitWindow.StarterKitGenerator skg = new StarterKitGenerator(guiPatchButton, progressPanel,CustomDLCConflictWindow.this);
			skg.setInternaldisplayname("GUI Compatibility Pack from MM " + ModManager.BUILD_NUMBER);
			skg.setMountpriority(6000);
			skg.setModdev("Mod Manager Build " + ModManager.BUILD_NUMBER);
			skg.setMountflag(new MountFlag(null, 0x8));
			skg.setInternaldlcname(internalName);
			String desc = "User generated compatibility pack made to inject new interface files into the conflicting files from the following mods:\n";

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

			skg.setModdesc(desc);
			skg.setModname(modName);

			//random tlk
			Random rand = new Random();
			int n = (rand.nextInt(100) * 100) + 13370000;
			skg.setTlkid(n);
			ModManager.debugLogger.writeMessage("Running StarterKitGenerator. Thread will suspend until that thread has completed");

			skg.execute();
			publish(new ThreadCommand("SET_STATUS_TEXT", "Generating Starter Kit for " + modName));

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
			for (Map.Entry<String, CustomDLC> resolutionFile : secondPriorityUIConflictFiles.entrySet()) {
				String sourcePath = biogameDirectory + "DLC/" + resolutionFile.getValue().getDlcName() + "/CookedPCConsole/" + resolutionFile.getKey();
				String copyTargetPath = skg.getGeneratedMod().getModPath() + "DLC_CON_" + internalName + "/CookedPCConsole/" + resolutionFile.getKey();
				try {
					ModManager.debugLogger.writeMessage("Copying 2nd tier conflict file: " + sourcePath + " => " + copyTargetPath);
					FileUtils.copyFile(new File(sourcePath), new File(copyTargetPath));
					transplantFiles.add(copyTargetPath);
				} catch (IOException e1) {
					ModManager.debugLogger.writeErrorWithException("ERROR COPYING FILE INTO COMPAT PACKAGE: ", e1);
					FileUtils.deleteQuietly(new File(skg.getGeneratedMod().getModPath()));
					publish(new ThreadCommand("ERROR_FILE_COPY_INTO_COMPAT"));
					return false;
				}
			}

			publish(new ThreadCommand("SET_STATUS_TEXT", "Locating GUI library"));

			ModManager.debugLogger.writeMessage("Copy of 2nd tier fields completed. Locating GUI library");

			//Run ME3-GUI-Transplanter over CookedPCConsole files
			for (String transplantFile : transplantFiles) {
				publish(new ThreadCommand("SET_STATUS_TEXT", "Transplanting SWFs into " + new File(transplantFile).getName()));
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(transplanterpath);
				commandBuilder.add("--injectswf");
				commandBuilder.add("--inputfolder");
				commandBuilder.add(guilibrarypath);
				commandBuilder.add("--targetfile");
				commandBuilder.add(transplantFile);
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				ModManager.debugLogger.writeMessage("Injecting SWFs into " + transplantFile);
				int returncode = 1;
				ProcessBuilder pb = new ProcessBuilder(command);
				returncode = ModManager.runProcess(pb).getReturnCode();
			}

			//Remove .bak files
			publish(new ThreadCommand("SET_STATUS_TEXT", "Deleting .bak files"));
			List<File> files = (List<File>) FileUtils.listFiles(new File(skg.getGeneratedMod().getModPath() + "DLC_CON_" + internalName), new String[] { "bak" }, true);
			for (File file : files) {
				FileUtils.deleteQuietly(file);
			}

			//Run autotoc
			publish(new ThreadCommand("SET_STATUS_TEXT", "Runing autotoc on new mod"));
			ArrayList<String> commandBuilder = new ArrayList<String>();
			// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
			commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(false) + "ME3Explorer.exe");
			commandBuilder.add("-autotoc");
			commandBuilder.add(skg.getGeneratedMod().getModPath() + "DLC_CON_" + internalName);
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			ModManager.debugLogger.writeMessage("Running AutoTOC on newly created mod.");
			int returncode = 1;
			ProcessBuilder pb = new ProcessBuilder(command);
			ProcessResult pr = ModManager.runProcess(pb);
			returncode = pr.getReturnCode();
			if (returncode != 0 || pr.hadError()) {
				ModManager.debugLogger.writeError("ME3Explorer returned a non 0 code (or threw error) running AutoTOC: " + returncode);
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
				case "ERROR_FILE_COPY_INTO_COMPAT":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, "Error copying conflicting files into the new mod.", "Missing GUI Library",
							JOptionPane.ERROR_MESSAGE);
					break;
				case "ERROR_NO_STARTER_MOD":
					JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, "Starter Kit failed to generate a mod.", "Starter Kit Generator failed", JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
						"Compatibility mod has been created.\nApply " + modName
								+ " to fix the UI overriding conflicts.\n\nIf you update any of your conflicting mods, uninstall this mod then generate a new compatibilty mod.\nGenerating a compatibiilty pack while "
								+ modName + " is installed will likely crash the game when the new mod is applied.",
						"Mod created", JOptionPane.INFORMATION_MESSAGE);
				dispose();
				new ModManagerWindow(false);
			} else {
				statusText.setText(modName + " was not created");
				JOptionPane.showMessageDialog(CustomDLCConflictWindow.this,
						"An error occured while generating the compatibility mod.\nOpen the log viewer to find more detailed information.\n\nIf you continue to have issues, please contact FemShep (see the help menu) and attach the log to your message.",
						"Mod not created", JOptionPane.ERROR_MESSAGE);
				dispose();
			}
		}
	}

	/**
	 * Detects if the conflicts are caused by one of the following mods: -
	 * DLC_CON_XBX (SP Controller Support) - DLC_CON_UIScaling (Interface
	 * Scaling Mod) - DLC_CON_UIScaling_Shared (Interface Scaling Add-On)
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
			CustomDLC str = conflictingDLCs.get(conflictingDLCs.size() - 1);
			if (knownGUImods.contains(str.getDlcName())) {
				ModManager.debugLogger.writeMessage("GUI mod " + str + " superceeding: " + entry.getKey());
				conflictingGUIMod = conflictingDLCs.get(conflictingDLCs.size() - 1).getDlcName();
				secondPriorityUIConflictFiles.put(entry.getKey(), conflictingDLCs.get(conflictingDLCs.size() - 2));
			}
		}
		if (secondPriorityUIConflictFiles.size() > 0) {
			return secondPriorityUIConflictFiles;
		}
		return null;
	}
}
