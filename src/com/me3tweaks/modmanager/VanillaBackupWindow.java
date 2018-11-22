package com.me3tweaks.modmanager;

import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

public class VanillaBackupWindow extends JDialog {

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
	private JButton backupButton;
	private JButton restoreButton;
	private JLabel backupLocation;
	private String backupPath;

	/**
	 * Manually invoked Vanilla Backup Window
	 * 
	 */
	public VanillaBackupWindow(boolean isBackup) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		ModManager.debugLogger.writeMessage("Opening Vanilla Backup Window");
		setupWindow(isBackup);
		if (shouldShow) {
			setVisible(true);
		}
	}

	/**
	 * Fetches the full backup path from the registry.
	 * 
	 * @return Filepath from registry to full backup path. If none exists, this
	 *         returns null.
	 */
	public static String GetFullBackupPath(boolean returnEvenIfInvalid) {
		String backupPath = null;
		try {
			backupPath = Advapi32Util.registryGetStringValue(HKEY_CURRENT_USER, VanillaUserRegistryKey, VanillaUserRegistryValue);
			File backupDir = new File(backupPath);
			if (verifyVanillaBackup(backupDir)) {
				ModManager.debugLogger.writeMessage("Found valid vanilla copy location in registry: " + backupPath);
			} else {
				ModManager.debugLogger.writeError("Found vanilla copy location in registry, but it doesn't seem to be valid, at least one of the validation checks failed");
			}
		} catch (Win32Exception e) {
			ModManager.debugLogger.writeErrorWithException("Win32Exception reading registry - assuming no backup exists yet (this is not really an error... mostly).", e);
		}

		if (backupPath != null) {
			if (new File(backupPath).exists() && new File(backupPath).isDirectory()) {
				return backupPath;
			} else if (returnEvenIfInvalid) {
				ModManager.debugLogger.writeError("returnEvenIfInvalid - returning path anyways.");
				return backupPath;
			}
		}
		return null;
	}

	private void setupWindow(boolean isBackup) {
		backupPath = GetFullBackupPath(true);

		setTitle("Full Game Restoration");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImages(ModManager.ICONS);
		//setMinimumSize(new Dimension(200, 400));
		JPanel rootPanel = new JPanel(new BorderLayout());

		JPanel northPanel = new JPanel(new BorderLayout());

		backupButton = new JButton("Create Backup");
		restoreButton = new JButton("Restore Backup");

		String message = "";
		String backupLocMessage = "";
		if (backupPath != null) {
			if (new File(backupPath).exists() && new File(backupPath).isDirectory()) {
				long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(backupPath), false);
				String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
				backupLocMessage = "Backup location: " + backupPath + ", " + sizeHR;
				message = "<html><div style=\"width: 300px\">A full backup is available on disk.<br>This backup is shared with ALOT Installer.</div></html>";
			} else {
				long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(ModManagerWindow.GetBioGameDir()), false);
				String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
				backupLocMessage = "No complete backup is available. Creating a new one will require about " + sizeHR + ".";
				restoreButton.setEnabled(false);
				message = "<html><div style=\"width: 300px\">A full backup indicator exists, but the listed directory doesn't exist:<br>" + backupPath + "</div></html>";

			}
		} else {
			long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(ModManagerWindow.GetBioGameDir()), false);
			String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
			backupLocMessage = "No complete backup has been created. Creating one will require about " + sizeHR + ".";
			restoreButton.setEnabled(false);
			message = "<html><div style=\"width: 300px\">Mod Manager can create a full game snapshot that you can use to do a complete game restore from. This is known in Mod Manager as restoring to vanilla - where items are in an unmodded state.</div></html>";
		}

		JLabel vanillaWindowDescription = new JLabel(message);
		northPanel.add(vanillaWindowDescription, BorderLayout.NORTH);

		backupLocation = new JLabel(backupLocMessage);
		northPanel.add(new JSeparator(), BorderLayout.CENTER);
		northPanel.add(backupLocation, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);

		JPanel backupPanel = new JPanel(new BorderLayout());

		backupButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ModManager.debugLogger.writeMessage("Backup button clicked - checking if backup already exists");
				if (backupPath != null && new File(backupPath).exists() && new File(backupPath).isDirectory()) {
					ModManager.debugLogger.writeMessage("Backup already exists - prompting user for action");
					int option = JOptionPane.showConfirmDialog(VanillaBackupWindow.this,
							"A backup of your game already exists at the following directory:\n" + backupPath
									+ "\nAre you sure you want to create a new copy? The old one won't be deleted.",
							"Backup already exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

					if (option == JOptionPane.NO_OPTION) {
						ModManager.debugLogger.writeMessage("Backup already exists - user declined to make new one.");
						return;
					}
					ModManager.debugLogger.writeMessage("Backup already exists - user creating new one anyways.");

				}

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
					if (!ModTypeConstants.isKnownDLCFolder(dir)) {
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
				String[] headerArray = ModTypeConstants.getDLCHeaderNameArray();
				HashMap<String, Long> sizesMap = ModTypeConstants.getSizesMap();
				HashMap<String, String> nameMap = ModTypeConstants.getHeaderFolderMap();

				int i = -1;
				// Add and enable/disable DLC checkboxes and add to hashmap
				for (String dlcName : headerArray) {
					String filepath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName));
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
							if (testpatchSfar.length() != sizesMap.get(dlcName) && testpatchSfar.length() != ModTypeConstants.TESTPATCH_16_SIZE) {
								ModManager.debugLogger.writeError("TESTPATCH size is not vanilla, should be " + sizesMap.get(dlcName) + " or " + ModTypeConstants.TESTPATCH_16_SIZE
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
					long gamedirsize = ResourceUtils.GetDirectorySize(Paths.get(ModManagerWindow.GetBioGameDir()), false);
					String sizeHR = ResourceUtils.humanReadableByteCount(gamedirsize, true);
					JOptionPane.showMessageDialog(VanillaBackupWindow.this, "Please choose a directory to store the backup in.\nThe drive needs to have at least " + sizeHR
							+ " of free space\nand the selected directory must be empty.", "Select backup directory", JOptionPane.PLAIN_MESSAGE);

					Platform.runLater(() -> {
						DirectoryChooser chooser = new DirectoryChooser();
						chooser.setInitialDirectory(new java.io.File("."));
						chooser.setTitle("Select backup directory");
						File chosenDir = chooser.showDialog(null);

						if (chosenDir != null) {
							long availableSpace = chosenDir.getUsableSpace();
							if (availableSpace > gamedirsize) {
								//Verify it is empty...
								if (chosenDir.list().length == 0) {
									//We should be good, finally!
									ModManager.debugLogger.writeMessage("Creating complete game backup.");
									ModManager.debugLogger.writeMessage("Source: " + new File(ModManagerWindow.GetBioGameDir()).getParent());
									ModManager.debugLogger.writeMessage("Destination: " + chosenDir.getAbsolutePath());
									new VanillaBackupThread(new File(ModManagerWindow.GetBioGameDir()).getParent(), chosenDir.getAbsolutePath(), true).execute();
								} else {
									ModManager.debugLogger.writeError("Selected directory not empty: " + chosenDir);
									JOptionPane.showMessageDialog(VanillaBackupWindow.this, chosenDir.getAbsolutePath() + "\nis not empty. The backup directory must be empty.",
											"Not enough space", JOptionPane.ERROR_MESSAGE);
									shouldShow = false;
									return;
								}
							} else {
								ModManager.debugLogger
										.writeError("Selected drive doesn't have enough space: " + chosenDir + " - Has " + availableSpace + ", we need " + gamedirsize);
								String partition = FilenameUtils.getPrefix(chosenDir.getAbsolutePath());
								JOptionPane.showMessageDialog(VanillaBackupWindow.this,
										partition + " does not have enough disk space to store the backup.\nNeeded: " + sizeHR + "\nAvailable: "
												+ ResourceUtils.humanReadableByteCount(availableSpace, true) + "\nPlease choose a different partition or free up space on "
												+ partition + ".",
										"Directory not empty", JOptionPane.ERROR_MESSAGE);
								shouldShow = false;
								return;
							}
						}
					});
				}
			}
		});

		restoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (backupPath != null) {
					int response = JOptionPane.showConfirmDialog(VanillaBackupWindow.this,
							"Your entire game installation located at\n" + new File(ModManagerWindow.GetBioGameDir()).getParent()
									+ "\nwill be deleted and restored from the backup.\nAre you sure you want to do a complete restore to this directory?",
							"GAME DIRECTORY WILL BE DELETED", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						//Delete directory and copy over it.
						new VanillaBackupThread(backupPath, new File(ModManagerWindow.GetBioGameDir()).getParent(), false).execute();
					}
				}
			}
		});

		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.LINE_AXIS));

		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(150, 20));
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);

		actionsPanel.add(progressBar);
		actionsPanel.add(Box.createRigidArea(new Dimension(5, 2)));
		//actionsPanel.add(Box.createHorizontalGlue());
		actionsPanel.add(restoreButton);
		actionsPanel.add(Box.createRigidArea(new Dimension(5, 2)));
		actionsPanel.add(backupButton);

		backupPanel.add(actionsPanel, BorderLayout.CENTER);

		//backupPanel.add(progressBar, BorderLayout.SOUTH);
		rootPanel.add(backupPanel, BorderLayout.CENTER);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(rootPanel);

		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);

		if (isBackup) {
			backupButton.requestFocus();
		} else {
			restoreButton.requestFocus();
		}
	}

	private static boolean verifyVanillaBackup(File backupDir) {
		ModManager.debugLogger.writeMessage("Performing quick verify on backup directory: " + backupDir.getAbsolutePath());
		for (String testSubpath : VanillaTestFiles) {
			File testFile = new File(backupDir.getAbsolutePath() + File.separator + testSubpath);
			if (!testFile.exists()) {
				ModManager.debugLogger.writeMessage("Backup directory failed quick verify, expected file missing: " + testFile.getAbsolutePath());
				return false;
			}
		}
		ModManager.debugLogger.writeMessage("Backup directory passed quick verification");
		return true;
	}

	/**
	 * Clones a folder recursively from one folder to another, skipping backups.
	 * 
	 * @author Mgamerz
	 *
	 */
	class VanillaBackupThread extends SwingWorker<Boolean, ThreadCommand> {

		private String sourceDir;
		private String destDir;
		private int numFilesToCopy;
		private AtomicInteger filesCopied = new AtomicInteger();
		private boolean isBackup;

		protected VanillaBackupThread(String sourceDir, String destDir, boolean isBackup) {
			this.sourceDir = sourceDir;
			this.destDir = destDir;
			this.isBackup = isBackup;
			backupButton.setEnabled(false);
			restoreButton.setEnabled(false);
		}

		@Override
		public Boolean doInBackground() {
			//Get files list 

			if (!isBackup) {
				ModManager.SetDefaultTextureGamerSettings();
			}

			ArrayList<String> filesToCopyRelative = new ArrayList<String>();
			File sourceDirFile = new File(sourceDir);
			File destDirFile = new File(destDir);
			if (!isBackup) {
				FileUtils.deleteQuietly(destDirFile);
				destDirFile.mkdirs();
			}
			int startLength = sourceDir.length();

			try {
				Files.walkFileTree(sourceDirFile.toPath(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						String filepath = file.toString();
						String extension = FilenameUtils.getExtension(filepath);
						switch (extension) {
						case "bak":
						case "pdf":
						case "wav":
							return FileVisitResult.CONTINUE; //don't include this crap
						}

						if (filepath.contains("cmmbackup")) {
							System.out.println("Excluding file: " + file.toAbsolutePath());
							return FileVisitResult.CONTINUE; //don't include cmmbackup
						}

						String relpath = file.toAbsolutePath().toString().substring(startLength);
						filesToCopyRelative.add(relpath);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) {

						// Skip folders that can't be traversed
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
						// Ignore errors traversing a folder
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
			}

			numFilesToCopy = filesToCopyRelative.size();

			for (String relPath : filesToCopyRelative) {
				File sourceFile = new File(sourceDir + relPath);
				File destFile = new File(destDir + relPath);
				try {
					String message = "Copying " + sourceFile.getName() + ", " + ResourceUtils.humanReadableByteCount(sourceFile.length(), true);
					publish(new ThreadCommand("SET_CURRENT_OPERATION", message));
					FileUtils.copyFile(sourceFile, destFile);
					filesCopied.incrementAndGet();
					publish(new ThreadCommand("UPDATE_PROGRESS"));
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error copying file for backup or restore!", e);
				}
			}
			File cmm_vanilla = new File(destDir + "cmm_vanilla");
			if (isBackup) {
				try {
					FileUtils.touch(cmm_vanilla);
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Error setting directory cmm_vanilla file:", e);
				}
				Advapi32Util.registrySetStringValue(HKEY_CURRENT_USER, VanillaUserRegistryKey, VanillaUserRegistryValue, destDir);
				ModManager.debugLogger.writeMessage("Updated registry key to point to new backup directory.");
				publish(new ThreadCommand("BACKUP_COMPLETE"));
			} else {
				FileUtils.deleteQuietly(cmm_vanilla);
				publish(new ThreadCommand("RESTORE_COMPLETE"));
			}

			return true;
		}

		@Override
		protected void process(List<ThreadCommand> chunks) {
			for (ThreadCommand latest : chunks) {
				switch (latest.getCommand()) {
				case "UPDATE_PROGRESS":
					progressBar.setVisible(true);
					int progress = (int) (filesCopied.get() * 100.0 / numFilesToCopy);
					progressBar.setValue(progress);
					break;
				case "SET_CURRENT_OPERATION":
					backupLocation.setText(latest.getMessage());
					break;
				case "BACKUP_COMPLETE":
					dispose();
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Complete game backup completed.");
					break;
				case "RESTORE_COMPLETE":
					dispose();
					ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Game restored from backup");
					break;
				}
			}
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (InterruptedException e) {
				ModManager.debugLogger.writeErrorWithException("VanillaBackupThread interrupted exception:", e);
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeErrorWithException("VanillaBackupThread encountered exception:", e);
			}

			dispose();

		}
	}
}
