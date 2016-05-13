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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.StarterKitWindow.StarterKitGenerator;
import com.me3tweaks.modmanager.objects.CustomDLC;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.objects.MountFlag;
import com.me3tweaks.modmanager.objects.ProcessResult;

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

	public CustomDLCConflictWindow() {
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupWindow() {
		setPreferredSize(new Dimension(500, 500));
		setTitle("Custom DLC Conflicts");

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
			data[i][COL_SUPERCEDING] = value.get(value.size()-1).getDlcName();
			String superceeded = "";
			for (int x = 0; x <= value.size() - 2; x++) {
				superceeded += value.get(x).getDlcName() + " ";
			}

			data[i][COL_SUPERCEDED] = superceeded;
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
		String message = "<html>To toggle Custom DLCs in Mod Manager, make sure all Custom DLC has been imported into Mod Manager.<br>This can be done through the Mod Management > Import Mods > Import Custom DLC Mod.<br>Once imported, you can install it by simply applying the mod.<br>You can remove (disable) a mod by deleting it through the Custom DLC Manager, and then apply it again to enable it.</html>";
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
				JOptionPane.showMessageDialog(CustomDLCConflictWindow.this, message, "Toggling Custom DLC in Mod Manager",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		panel.add(infoLinkButton, BorderLayout.NORTH);
		panel.add(scrollpane, BorderLayout.CENTER);

		HashMap<CustomDLC, String> secondPriorityUIConflictFiles = detectUIModConflicts(conflicts);
		if (secondPriorityUIConflictFiles != null) {
			guiPatchButton = new JButton("UI mod is conflicting with other mods", UIManager.getIcon("OptionPane.warningIcon"));
			guiPatchButton
					.setToolTipText("<html>Mod Manager has detected that an installed GUI mod (SP Controller Support or Interface Scaling) is superceding other installed Custom DLCs.<br>Click for more info.</html>");

			guiProgressBar = new JProgressBar();
			guiProgressBar.setVisible(false);

			guiPatchButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(CustomDLCConflictWindow.this,
							"An interface mod is superceding other installed Custom DLC mods.\n"
									+ "This will disable features of those mods, which may cause game instability.\n"
									+ "Mod Manager can generate a new mod that will use those superceding files with your interface\n"
									+ "mod's UI files, which will fix this specific issue.\n\n"
									+ "This generated mod will only apply to the current versions of the conflicting mods.\n"
									+ "If you update any of them, you must uninstall the generated mod and regenerate it again.\n\n"
									+ "Generate a GUI conflict compatibility mod?", "Generate a compatibilty mod", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						//generate
						String modName = "MM GUI Compatibility";
						boolean nameIsBad = true;
						while (nameIsBad) {
							String whatTheUserEntered = JOptionPane.showInputDialog(CustomDLCConflictWindow.this,
									"Enter a name for the compatibilty mod.\nAlphanumeric (spaces/underscore allowed) only, no more than 20 characters.");
							if (whatTheUserEntered == null) {
								return;
							}
							whatTheUserEntered = whatTheUserEntered.trim();
							//space, and alphanumerics
							boolean asciionly = whatTheUserEntered.chars().allMatch(
									c -> c == 0x20 || c == 0x5F || (c > 0x30 && c < 0x3A) || (c > 0x40 && c < 0x5B) || (c > 0x60 && c < 0x7B)); //what the f is this?
							if (!asciionly) {
								ModManager.debugLogger.writeError("Name is not ascii alphanumeric only: "+whatTheUserEntered);
								continue;
							}
							if (whatTheUserEntered.length() > 20 || whatTheUserEntered.length() < 1) {
								ModManager.debugLogger.writeError("Name is empty or too long: "+whatTheUserEntered);
								continue;
							}
							
							//check if already exists
							File patchFolder = new File(ModManager.getModsDir() + whatTheUserEntered);
							if (patchFolder.exists() && patchFolder.isDirectory()) {
								ModManager.debugLogger.writeError("Mod already exists, prompting user to overwrite: "+patchFolder);
								int renameresult = JOptionPane.showOptionDialog(CustomDLCConflictWindow.this,
										"A mod named "+whatTheUserEntered+" already exists in Mod Manager.", 
										"Name conflict",
										JOptionPane.OK_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE,
										null,
										new String[]{"Delete existing mod", "Enter a new name"},
										"default");
								if (renameresult == JOptionPane.NO_OPTION) {
									continue;
								} else {
									FileUtils.deleteQuietly(patchFolder);
								}
							}
							
							modName = whatTheUserEntered.trim();
							nameIsBad = false;
						}
						guiProgressBar.setVisible(true);
						guiProgressBar.setIndeterminate(true);
						guiPatchButton.setVisible(false);
						GUICompatGeneratorThread gcgt = new GUICompatGeneratorThread(modName, biogameDirectory, secondPriorityUIConflictFiles);
						gcgt.execute();
					}
				}
			});

			JPanel guiPatchPanel = new JPanel(new BorderLayout());
			guiPatchPanel.add(guiPatchButton, BorderLayout.NORTH);
			guiPatchPanel.add(guiProgressBar, BorderLayout.SOUTH);
			panel.add(guiPatchPanel, BorderLayout.SOUTH);
		}
		add(panel);
		pack();
	}

	class GUICompatGeneratorThread extends SwingWorker<Void, Void> {

		private HashMap<CustomDLC, String> secondPriorityUIConflictFiles;
		private String modName;
		private String biogameDirectory;

		public GUICompatGeneratorThread(String modName, String biogameDirectory, HashMap<CustomDLC, String> secondPriorityUIConflictFiles) {
			this.modName = modName;
			this.biogameDirectory = biogameDirectory;
			this.secondPriorityUIConflictFiles = secondPriorityUIConflictFiles;
		}

		@Override
		protected Void doInBackground() throws Exception {
			String internalName = modName.toUpperCase().replaceAll(" ", "_");
			ModManager.debugLogger.writeMessage("Compatibility pack will be named DLC_CON_" + internalName);
			StarterKitWindow.StarterKitGenerator skg = new StarterKitGenerator(guiPatchButton, guiProgressBar);
			skg.setInternaldisplayname("GUI Compatibility Pack from MM " + ModManager.BUILD_NUMBER);
			skg.setMountpriority(6000);
			skg.setModdev("Mod Manager Build " + ModManager.BUILD_NUMBER);
			skg.setMountflag(new MountFlag(null, 0x8));
			skg.setInternaldlcname(internalName);
			String desc = "User generated compatibility pack made to inject new interface files into the conflicting files from the following mods:\n";

			TreeSet<String> conflictingDLC = new TreeSet<String>();
			for (CustomDLC dlc : secondPriorityUIConflictFiles.keySet()) {
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
			synchronized (skg.lock) {
				while (guiProgressBar.isVisible()) {
					try {
						skg.lock.wait();
					} catch (InterruptedException ex) {
						// TODO Auto-generated catch block
						ModManager.debugLogger.writeErrorWithException("Unable to wait for for starter kit to finish:", ex);
					}
				}
			}
			ModManager.debugLogger.writeMessage("StarterKit should have finished, resuming compat generator");
			if (skg.getGeneratedMod().getModPath() == null) {
				//something went wrong

				return null;
			}

			//starter kit has finished. Copy files to it.
			ArrayList<String> transplantFiles = new ArrayList<>();
			for (Map.Entry<CustomDLC, String> resolutionFile : secondPriorityUIConflictFiles.entrySet()) {
				String sourcePath = biogameDirectory + "DLC/" + resolutionFile.getKey().getDlcName() + "/CookedPCConsole/"
						+ resolutionFile.getValue();
				String copyTargetPath = skg.getGeneratedMod().getModPath() + "DLC_CON_" + internalName + "/CookedPCConsole/"
						+ resolutionFile.getValue();
				try {
					ModManager.debugLogger.writeMessage("Copying 2nd tier conflict file: " + sourcePath + " => " + copyTargetPath);
					FileUtils.copyFile(new File(sourcePath), new File(copyTargetPath));
					transplantFiles.add(copyTargetPath);
				} catch (IOException e1) {
					ModManager.debugLogger.writeErrorWithException("ERROR COPYING FILE INTO COMPAT PACKAGE: ", e1);
				}
			}

			ModManager.debugLogger.writeMessage("Copy of 2nd tier fields completed. Locating GUI library");
			String guilibrarypath = ModManager.getGUILibraryFor(conflictingGUIMod);
			String transplanterpath = ModManager.getGUITransplanterCLI();

			//Run ME3-GUI-Transplanter over CookedPCConsole files
			for (String transplantFile : transplantFiles) {
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
			List<File> files = (List<File>) FileUtils.listFiles(new File(skg.getGeneratedMod().getModPath() + "DLC_CON_" + internalName),
					new String[] { "bak" }, true);
			for (File file : files) {
				FileUtils.deleteQuietly(file);
			}

			//Run autotoc
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
			return null;
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (Exception e) {
				ModManager.debugLogger.writeException(e);
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
	private HashMap<CustomDLC, String> detectUIModConflicts(HashMap<String, ArrayList<CustomDLC>> conflicts) {
		ArrayList<String> knownGUImods = new ArrayList<String>(Arrays.asList(ModManager.KNOWN_GUI_CUSTOMDLC_MODS));
		HashMap<CustomDLC, String> secondPriorityUIConflictFiles = new HashMap<>();
		for (Map.Entry<String, ArrayList<CustomDLC>> entry : conflicts.entrySet()) {
			ArrayList<CustomDLC> conflictingDLCs = entry.getValue();
			CustomDLC str = conflictingDLCs.get(conflictingDLCs.size() - 1);
			if (knownGUImods.contains(str.getDlcName())) {
				ModManager.debugLogger.writeMessage("GUI mod " + str + " superceeding: " + entry.getKey());
				conflictingGUIMod = conflictingDLCs.get(conflictingDLCs.size() - 1).getDlcName();
				secondPriorityUIConflictFiles.put(conflictingDLCs.get(conflictingDLCs.size() - 2), entry.getKey());
			}
		}
		if (secondPriorityUIConflictFiles.size() > 0) {
			return secondPriorityUIConflictFiles;
		}
		return null;
	}
}
