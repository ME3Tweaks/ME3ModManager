package com.me3tweaks.modmanager;

import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.PCCDumpOptions;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;

public class VanillaBackupWindow extends JFrame {

	/**
	 * Window controller for the vanilla backup/restore tool
	 * 
	 * @author Mgamerz
	 *
	 */
	boolean windowOpen = true;
	JProgressBar progressBar;
	JButton dumpButton;
	final int threads = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
	private static String[] VanillaTestFiles = new String[] { "BIOGame", "Binaries", "BIOGame\\DLC", "BIOGame\\CookedPCConsole", "BIOGame\\Movies", "BIOGame\\Patches" };
	private JPanel dumpPanel;
	private static String VanillaUserRegistryKey = "SOFTWARE\\Mass Effect 3 Mod Manager";
	private static String VanillaUserRegistryValue = "VanillaCopyLocation";
	private boolean shouldShow = true;

	/**
	 * Manually invoked Vanilla Backup Window
	 * 
	 */
	public VanillaBackupWindow() {
		ModManager.debugLogger.writeMessage("Opening Vanilla Backup Window");
		setupWindow();
		if (shouldShow) {
			setVisible(true);
		}
	}

	private void setupWindow() {
		setTitle("Full Game Restoration");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImages(ModManager.ICONS);
		//setMinimumSize(new Dimension(200, 400));
		JPanel rootPanel = new JPanel(new BorderLayout());

		JPanel northPanel = new JPanel(new BorderLayout());
		JLabel vanillaWindowDescription = new JLabel(
				"<html><div style=\"width: 300px\">Mod Manager can create a full game snapshot that you can use to do a complete game restore from. This is known in Mod Manager as restoring to vanilla - where items are in an unmodded state.</div></html>");

		northPanel.add(vanillaWindowDescription, BorderLayout.NORTH);
		String path = null;
		try {
			path = Advapi32Util.registryGetStringValue(HKEY_CURRENT_USER, VanillaUserRegistryKey, VanillaUserRegistryValue);
			File backupDir = new File(path);

			if (backupDir.exists() && backupDir.isDirectory() && verifyVanillaBackup(backupDir)) {
				ModManager.debugLogger.writeMessage("Found valid vanilla copy location in registry: " + path);
				path = null;
			} else {
				ModManager.debugLogger.writeError("Found vanilla copy location in registry, but it doesn't seem to be valid, one of the validation cehcks failed");

			}
		} catch (Win32Exception e) {
			ModManager.debugLogger.writeErrorWithException("Win32Exception reading registry - assuming no backup exists yet", e);
		}
		String backupLocMessage = "";
		if (path != null && new File(path).exists() && new File(path).isDirectory()) {
			long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(path), false);
			String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
			backupLocMessage = "Backup location: " + path + ", " + sizeHR;
		} else {
			long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(ModManagerWindow.GetBioGameDir()), false);
			String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
			backupLocMessage = "No complete backup has been created. Creating one will require about " + sizeHR + ".";
		}
		JLabel backupLocation = new JLabel(backupLocMessage);
		northPanel.add(new JSeparator(), BorderLayout.CENTER);
		northPanel.add(backupLocation, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);

		JPanel backupPanel = new JPanel(new BorderLayout());
		JButton backupButton = new JButton("Create Backup");
		JButton restoreButton = new JButton("Restore Backup");

		backupButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//See if any Custom DLC is installed.
				ModManager.debugLogger.writeMessage("Vanilla Backup: Checking if any Custom DLC is installed.");
				File mainDlcDir = new File(ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + "DLC/");
				String[] directories = mainDlcDir.list(new FilenameFilter() {
					@Override
					public boolean accept(File current, String name) {
						return new File(current, name).isDirectory() && !name.equals("__metadata");
					}
				});

				for (String dir : directories) {
					if (!ModType.isKnownDLCFolder(dir)) {
						ModManager.debugLogger.writeError("Non standard DLC folder detected: " + dir + ", aborting vanilla backup");
						JOptionPane.showMessageDialog(VanillaBackupWindow.this,
								"Your game has one or more Custom DLCs installed.\nUninstall all Custom DLC before making a backup.", "Game is modded", JOptionPane.ERROR_MESSAGE);
						shouldShow = false;
						return;
					}
				}
				ModManager.debugLogger.writeMessage("Vanilla Backup: No Custom DLC appears to be installed.");

				//See if any DLC is modified.
				ModManager.debugLogger.writeMessage("Vanilla Backup: Checking if DLC is modified.");
				String[] headerArray = ModType.getDLCHeaderNameArray();
				HashMap<String, Long> sizesMap = ModType.getSizesMap();
				HashMap<String, String> nameMap = ModType.getHeaderFolderMap();

				int i = -1;
				// Add and enable/disable DLC checkboxes and add to hashmap
				for (String dlcName : headerArray) {
					String filepath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModType.getDLCPath(dlcName));
					File dlcPath = new File(filepath);
					// Check if directory exists
					if (!dlcPath.exists()) {
						ModManager.debugLogger.writeMessage(dlcName + " DLC folder not installed, skipping.");
						continue;
					}

					// The folder exists.
					File mainSfar = new File(dlcPath + "\\Default.sfar");
					File testpatchSfar = new File(dlcPath + "\\Patch_001.sfar");
					File mainSfarbackup = new File(dlcPath + "\\Default.sfar.bak");
					File testpatchSfarbackup = new File(dlcPath + "\\Patch_001.sfar.bak");
					ModManager.debugLogger.writeMessage("Looking for Default.sfar, Patch_001.sfar in " + filepath);
					if (mainSfar.exists() || testpatchSfar.exists()) {
						if (mainSfar.exists()) {
							if (mainSfar.length() != sizesMap.get(dlcName)) {
								ModManager.debugLogger
										.writeError("DLC size is not vanilla: " + dlcName + ", should be " + sizesMap.get(dlcName) + ", but it is " + mainSfar.length());
								JOptionPane.showMessageDialog(VanillaBackupWindow.this, dlcName + " has been modified.\nDLC must be unmodified in order to create a backup.",
										"Game is modded", JOptionPane.ERROR_MESSAGE);
								shouldShow = false;
								return;
							}
						} else {
							//TESTPATCH
							if (testpatchSfar.length() != sizesMap.get(dlcName) && testpatchSfar.length() != ModType.TESTPATCH_16_SIZE) {
								ModManager.debugLogger.writeError("TESTPATCH size is not vanilla, should be " + sizesMap.get(dlcName) + " or " + ModType.TESTPATCH_16_SIZE
										+ ", but it is " + testpatchSfar.length());
								JOptionPane.showMessageDialog(VanillaBackupWindow.this, dlcName + " has been modified.\nDLC must be unmodified in order to create a backup.",
										"Game is modded", JOptionPane.ERROR_MESSAGE);
								shouldShow = false;
								return;
							}
						}
					}

					ArrayList<String> unpackedFiles = SelectiveRestoreWindow.getUnpackedFilesList(new String[] { dlcName });
					if (unpackedFiles.size() > 0) {
						ModManager.debugLogger.writeError("DLC contains unpacked files:" + dlcName + ". Aborting");
						JOptionPane.showMessageDialog(VanillaBackupWindow.this, dlcName
								+ " has unpacked files in the DLC directory.\nVanilla backups cannot include these unpacked files - remove them in the Custom Restore window.",
								"Game is modded", JOptionPane.ERROR_MESSAGE);
						shouldShow = false;
						return;
					}
				}

				//DLC appears unmodified

				Object[] choices = { "Yes, I am sure it is unmodded", "No, it is modded", "Cancel" };
				Object defaultChoice = choices[1];
				int response = JOptionPane.showOptionDialog(VanillaBackupWindow.this,
						"Is your game currently unmodded, aka \"vanilla\"? If not, get it into an unmodded state first!\nDon't waste space making worthless backups.",
						"Vanilla Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, choices, defaultChoice);
				if (response == JOptionPane.YES_OPTION) {
					System.out.println("YES OPTION");
					long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(ModManagerWindow.GetBioGameDir()), false);
					String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
					JOptionPane.showMessageDialog(VanillaBackupWindow.this, "Please choose a directory to store the backup in.\nThe drive needs to have at least " + sizeHR
							+ " of free space\nand the selected directory must be empty.", "Select backup directory", JOptionPane.PLAIN_MESSAGE);

					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle("Select backup directory");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					//
					// disable the "All files" option.
					//
					chooser.setAcceptAllFileFilterUsed(false);
					//    
					if (chooser.showOpenDialog(VanillaBackupWindow.this) == JFileChooser.APPROVE_OPTION) {
						File chosenDir = chooser.getSelectedFile();
						long availableSpace = chosenDir.getUsableSpace();
						if (availableSpace > gamedirsize) {
							//Verify it is empty...
							if (chosenDir.list().length == 0) {
								//We should be good, finally!

							} else {
								ModManager.debugLogger.writeError("Selected directory not empty: " + chosenDir);
								JOptionPane.showMessageDialog(VanillaBackupWindow.this, chosenDir.getAbsolutePath() + "\nis not empty. The backup directory must be empty.",
										"Not enough space", JOptionPane.ERROR_MESSAGE);
								shouldShow = false;
								return;
							}
						} else {
							ModManager.debugLogger.writeError("Selected drive doesn't have enough space: " + chosenDir + " - Has " + availableSpace + ", we need " + gamedirsize);
							String partition = FilenameUtils.getPrefix(chosenDir.getAbsolutePath());
							JOptionPane.showMessageDialog(VanillaBackupWindow.this,
									partition + " does not have enough disk space to store the backup.\nNeeded: " + sizeHR + "\nAvailable: "
											+ ResourceUtils.humanReadableByteCount(availableSpace, true) + "\nPlease choose a different partition or free up space on " + partition
											+ ".",
									"Directory not empty", JOptionPane.ERROR_MESSAGE);
							shouldShow = false;
							return;
						}
					}

				}
				if (response == JOptionPane.NO_OPTION) {
					System.out.println("NO OPTION");
				}
				if (response == JOptionPane.CANCEL_OPTION) {
					System.out.println("CANCEL OPTION");
				}
			}
		});

		restoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(VanillaBackupWindow.this,
						"Your entire game installation located at\n" + ModManagerWindow.GetBioGameDir()
								+ "\nwill be deleted (and restored from the backup) if you continue.\nAre you sure you want to do a complete restore to this directory?",
						"GAME DIRECTORY WILL BE DELETED", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (response == JOptionPane.YES_OPTION) {

				}
			}
		});

		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.LINE_AXIS));

		actionsPanel.add(Box.createHorizontalGlue());
		actionsPanel.add(restoreButton);
		actionsPanel.add(Box.createRigidArea(new Dimension(5, 2)));
		actionsPanel.add(backupButton);

		backupPanel.add(actionsPanel, BorderLayout.CENTER);
		rootPanel.add(backupPanel, BorderLayout.CENTER);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(rootPanel);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);

		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	private boolean verifyVanillaBackup(File backupDir) {
		// TODO Auto-generated method stub
		return false;
	}

	class VanillaBackupThread extends SwingWorker<Boolean, ThreadCommand> {
		int completed = 0;
		int numjobs = 0;
		HashMap<String, String> sfarHashes;
		HashMap<String, Long> sfarSizes;

		protected VanillaBackupThread() {
			sfarHashes = ModType.getHashesMap();
			sfarSizes = ModType.getSizesMap();
		}

		@Override
		public Boolean doInBackground() {
			//Copy stuff
			
			return true;
		}
	}
}
