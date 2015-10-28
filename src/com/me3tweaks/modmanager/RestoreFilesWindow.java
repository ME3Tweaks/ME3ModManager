package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.RestoreMode;
import com.me3tweaks.modmanager.repairdb.BasegameHashDB;
import com.me3tweaks.modmanager.repairdb.RepairFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class RestoreFilesWindow extends JDialog {
	JLabel infoLabel;
	JTextArea consoleArea;
	final int levelCount = 10; //number of lines in the console
	String consoleQueue[];
	boolean windowOpen = true;
	String currentText;
	String BioGameDir;
	JProgressBar progressBar;
	int restoreMode;
	ModManagerWindow callingWindow;

	public RestoreFilesWindow(ModManagerWindow callingWindow, String BioGameDir, int restoreMode) {
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		this.restoreMode = restoreMode;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Restoring game files");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(330, 240));
		this.setResizable(false);
		consoleQueue = new String[levelCount];
		setupWindow();
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		addToQueue("Restoring game files");
		new RestoreDataJob().execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		// TODO Auto-generated method stub
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("Restoring game files to original state...");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		northPanel.add(progressBar, BorderLayout.SOUTH);

		rootPanel.add(northPanel, BorderLayout.NORTH);

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);

		consoleArea.setEditable(false);
		rootPanel.add(consoleArea, BorderLayout.CENTER);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// do something...
			}
		});
	}

	class RestoreDataJob extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numjobs = 0;
		HashMap<String, String> sfarHashes;
		HashMap<String, Long> sfarSizes;

		protected RestoreDataJob() {
			sfarHashes = ModType.getHashesMap();
			sfarSizes = ModType.getSizesMap();
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the restore thread with restore mode " + restoreMode);
			if (windowOpen) {
				switch (restoreMode) {
				case RestoreMode.ALL:
					numjobs = ModType.getHeaderNameArray().length + 1;
					publish("Deleting custom DLC, restoring basegame/unpacked files,SFARs, unpacked content");
					return removeCustomDLC() && processRestoreBasegame(false, false) && restoreSFARsUsingHeaders(ModType.getDLCHeaderNameArray()) && processDeleteUnpackedFiles();
				case RestoreMode.REMOVECUSTOMDLC:
					publish("Deleting custom DLCs");
					numjobs = 1;
					return removeCustomDLC();
				case RestoreMode.BASEGAME:
					numjobs = 1;
					publish("Restoring basegame files");
					return processRestoreBasegame(false, true);
				case RestoreMode.UNPACKED:
					numjobs = 1;
					publish("Restoring unpacked DLC files");
					return processRestoreBasegame(true, false);
				case RestoreMode.UNPACKEDBASEGAME:
					numjobs = 1;
					publish("Restoring basegame and unpacked DLC files");
					return processRestoreBasegame(false, false);
				case RestoreMode.VANILLIFYDLC:
					numjobs = 2 + ModType.getDLCHeaderNameArray().length;
					publish("Attempting to return DLC to vanilla state");
					return removeCustomDLC() && restoreSFARsUsingHeaders(ModType.getDLCHeaderNameArray()) && processDeleteUnpackedFiles();
				case RestoreMode.ALLDLC:
					numjobs = ModType.getHeaderNameArray().length;
					publish("Restoring all DLC SFARs");
					return restoreSFARsUsingHeaders(ModType.getDLCHeaderNameArray());
				case RestoreMode.SP:
					numjobs = ModType.getSPHeaderNameArray().length;
					publish("Restoring SP SFARs");
					return restoreSFARsUsingHeaders(ModType.getSPHeaderNameArray());
				case RestoreMode.MP:
					numjobs = ModType.getMPHeaderNameArray().length;
					publish("Restoring MP SFARs");
					return restoreSFARsUsingHeaders(ModType.getMPHeaderNameArray());
				case RestoreMode.MPBASE:
					numjobs = ModType.getMPHeaderNameArray().length + 1;
					publish("Restoring MP SFARs and basegame files");
					return processRestoreBasegame(false, true) && restoreSFARsUsingHeaders(ModType.getMPHeaderNameArray());
				case RestoreMode.SPBASE:
					numjobs = ModType.getSPHeaderNameArray().length + 1;
					publish("Restoring SP SFARs and basegame files");
					return processRestoreBasegame(false, true) && restoreSFARsUsingHeaders(ModType.getSPHeaderNameArray());
				case RestoreMode.REMOVEUNPACKEDITEMS:
					//numjobs calculated in the procedure
					publish("Deleting unpacked files");
					return processDeleteUnpackedFiles();
				default:
					return false;
				}
			}
			return false;
		}

		/**
		 * Gets list of files in the CookedPCConsole directories of each known
		 * standard DLC folder (assuming it exists) that does not end with a
		 * .sfar or .bak (all unpacked files skipping sfars and backups)
		 * 
		 * @return arraylist of full file paths
		 */
		private ArrayList<String> getUnpackedFilesList() {
			ArrayList<String> filepaths = new ArrayList<String>();
			String[] dlcHeaders = ModType.getDLCHeaderNameArray();

			for (String header : dlcHeaders) {
				String dlcFolderPath = ModManager.appendSlash(RestoreFilesWindow.this.BioGameDir) + ModManager.appendSlash(ModType.getDLCPath(header));
				File dlcDirectory = new File(dlcFolderPath);
				if (dlcDirectory.exists()) {
					File files[] = dlcDirectory.listFiles();
					for (File file : files) {
						if (file.isFile()) {
							String filepath = file.getAbsolutePath();
							if (!filepath.endsWith(".sfar") && !filepath.endsWith(".bak")) {
								ModManager.debugLogger.writeMessage("Unpacked file: " + filepath);
								filepaths.add(filepath);
							}
						}
					}
					//find PCConsoleTOC.bin for it
					File dlcConsoleTOC = new File(ModManager.appendSlash(dlcDirectory.getParent())+"PCConsoleTOC.bin");
					if (dlcConsoleTOC.exists()) {
						ModManager.debugLogger.writeMessage("Unpacked file: " + dlcConsoleTOC.getAbsolutePath());
						filepaths.add(dlcConsoleTOC.getAbsolutePath());
					}
				}
			}
			return filepaths;
		}

		private boolean processDeleteUnpackedFiles() {
			completed = 0;
			ArrayList<String> filestodelete = getUnpackedFilesList();
			numjobs = filestodelete.size();
			ModManager.debugLogger.writeMessage("===Deleting " + numjobs + " unpacked files===");
			for (String filepath : filestodelete) {
				ModManager.debugLogger.writeMessage("Deleting " + filepath);
				publish("Deleting " + FilenameUtils.getName(filepath));
				FileUtils.deleteQuietly(new File(filepath));
				completed++;
				publish(Integer.toString(completed));
			}
			ModManager.debugLogger.writeMessage("===Deleting the unpacked DLC backup folder===");
			String me3dir = (new File(RestoreFilesWindow.this.BioGameDir)).getParent();
			String dlcbackupfolder = ModManager.appendSlash(me3dir) + "cmmbackup\\BIOGame\\DLC";
			File dlcBackupDirectory = new File(dlcbackupfolder);
			try {
				FileUtils.deleteDirectory(dlcBackupDirectory);
				ModManager.debugLogger.writeMessage("Deleted cmmbackup/biogame/dlc.");
			} catch (IOException e) {
				if (dlcBackupDirectory.exists()) {
					ModManager.debugLogger.writeErrorWithException("Unable to delete unpacked backup directory that exists:", e);
				}
			}

			ModManager.debugLogger.writeMessage("===End of unpacked deletion===");
			return true;
		}

		private boolean restoreSFARsUsingHeaders(String[] dlcHeaders) {
			int restoresCompleted = 0;
			for (String header : dlcHeaders) {
				if (processRestoreJob(ModManager.appendSlash(RestoreFilesWindow.this.BioGameDir) + ModManager.appendSlash(ModType.getDLCPath(header)), header)) {
					ModManager.debugLogger.writeMessage("Processed Restore SFAR Job (SUCCESS): "+header);
					completed++; //for progress bar
					restoresCompleted++; //for local checking
					publish(Integer.toString(completed));
				} else {
					ModManager.debugLogger.writeError("Processed Restore SFAR Job (UNSUCCESSFUL): "+header);
				}
			}
			return restoresCompleted == dlcHeaders.length;
		}

		/**
		 * Deletes all non-standard DLC folders from the BioGAme/DLC directory
		 * using the known list of DLCs
		 */
		private boolean removeCustomDLC() {
			// TODO Auto-generated method stub
			File rootFolder = new File(ModManager.appendSlash(RestoreFilesWindow.this.BioGameDir) + "DLC");
			if (rootFolder.exists()) {
				File[] folders = rootFolder.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
				for (File dlcfolder : folders) {
					String foldername = dlcfolder.getName();
					if (!ModType.getStandardDLCFolders().contains(foldername)) {
						try {
							FileUtils.deleteDirectory(dlcfolder);
							ModManager.debugLogger.writeMessage("Deleted DLC folder: " + dlcfolder.getAbsolutePath());
						} catch (IOException e) {
							ModManager.debugLogger.writeErrorWithException("Unabled to delete DLC folder: " + dlcfolder, e);
							return false;
						}
					}
				}
				ModManager.debugLogger.writeMessage("Deleted all DLC folders (if there were any)");
				completed++;
				publish(Integer.toString(completed));
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Processes a data restoration of the basegame by copying all files in
		 * the backup directory back into the base game folder.
		 * 
		 * @param skipDLC
		 *            Skips restoring items in the BioGame/DLC folders (unpacked
		 *            DLC)
		 * @return true if successful, false otherwise
		 */
		private boolean processRestoreBasegame(boolean skipBasegame, boolean skipDLC) {
			//load Basegame DB
			ModManager.debugLogger.writeMessage("Processing a basegame/unpacked DLC job. Skip basegame: " + skipBasegame + ", Skip DLC: " + skipDLC);

			BasegameHashDB bghDB = new BasegameHashDB(null, new File(BioGameDir).getParent(), false);
			String me3dir = (new File(RestoreFilesWindow.this.BioGameDir)).getParent();
			String backupfolder = ModManager.appendSlash(me3dir) + "cmmbackup\\";
			String dlcbackupfolder = ModManager.appendSlash(me3dir) + "cmmbackup\\BIOGame\\DLC";
			File backupdir = new File(backupfolder);
			if (backupdir.exists()) {
				Collection<File> backupfiles = FileUtils.listFiles(new File(backupfolder), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				for (File backup : backupfiles) {
					if (backup.getAbsolutePath().startsWith(dlcbackupfolder) && skipDLC) {
						//basegame only
						continue;
					}
					if (!backup.getAbsolutePath().startsWith(dlcbackupfolder) && skipBasegame) {
						//DLC only
						continue;
					}
					ModManager.debugLogger.writeMessage("Restoring file: " + backup.getAbsolutePath() + ", starts with DLC path? "
							+ backup.getAbsolutePath().startsWith(dlcbackupfolder) + " path is " + dlcbackupfolder);
					String taskTitle = backup.getAbsolutePath().startsWith(dlcbackupfolder) ? "UNPACKED DLC" : "BASEGAME";
					//verify it.
					String relative = ResourceUtils.getRelativePath(backup.getAbsolutePath(), backupfolder, File.separator);
					RepairFileInfo rfi = bghDB.getFileInfo(relative);
					boolean restoreAnyways = false;
					if (rfi == null) {
						int reply = JOptionPane.showOptionDialog(null,
								"<html>The file:<br>" + relative + "<br>is not in the repair database. "
										+ "Restoring this file may overwrite your default setup if you use custom mods like texture swaps.<br></html>",
								"Restoring Unverified File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
								new String[] { "Restore this file", "Skip restoring this file", "Cancel basegame restore" }, "default");
						switch (reply) {
						case JOptionPane.CANCEL_OPTION:
							return false;
						case JOptionPane.NO_OPTION:
							continue;
						case JOptionPane.YES_OPTION:
							restoreAnyways = true;
							break;
						}
					}
					if (!restoreAnyways) {
						//verify the file
						if (backup.length() != rfi.filesize) {
							//MISMATCH!
							int reply = JOptionPane.showOptionDialog(null,
									"<html>The filesize of the file:<br>" + relative + "<br>does not match the one stored in the repair game database.<br>" + backup.length()
											+ " bytes (backup) vs " + rfi.filesize + " bytes (database)<br><br>"
											+ "This file could be corrupted or modified since the database was created.<br>"
											+ "Restoring this file may overwrite your default setup if you use custom mods like texture swaps.<br></html>",
									"Restoring Unverified File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
									new String[] { "Restore this file", "Skip restoring this file", "Cancel basegame restore" }, "default");
							switch (reply) {
							case JOptionPane.CANCEL_OPTION:
								return false;
							case JOptionPane.NO_OPTION:
								continue;
							case JOptionPane.YES_OPTION:
								restoreAnyways = true;
								break;
							}
						}
					}

					if (!restoreAnyways) {
						//this is outside of the previous if statement as the previous one could set the restoreAnyways variable again.
						try {
							if (!MD5Checksum.getMD5Checksum(backup.getAbsolutePath()).equals(rfi.md5)) {
								int reply = JOptionPane.showOptionDialog(null,
										"<html>The hash of the file:<br>" + relative + "<br>does not match the one stored in the repair game database.<br>"
												+ "This file has changed since the database was created.<br>"
												+ "Restoring this file may overwrite your default setup if you use custom mods like texture swaps.<br></html>",
										"Restoring Unverified File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
										new String[] { "Restore this file", "Skip restoring this file", "Cancel basegame restore" }, "default");
								switch (reply) {
								case JOptionPane.CANCEL_OPTION:
									return false;
								case JOptionPane.NO_OPTION:
									continue;
								case JOptionPane.YES_OPTION:
									restoreAnyways = true;
									break;
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							ModManager.debugLogger.writeException(e);
						}
					}

					//restore it					
					//String relative = new File(backupfolder).toURI().relativize(backup.toURI()).getPath();
					ModManager.debugLogger.writeMessage("Restoring " + relative);
					try {
						publish(taskTitle + ": Restoring " + backup.getName());
						Files.copy(Paths.get(backup.toString()), Paths.get(ModManager.appendSlash(me3dir) + relative), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						return false;
					}
				}
			} else {
				return false;
			}
			completed++;
			publish(Integer.toString(completed));
			return true;
		}

		private boolean processRestoreJob(String fullDLCDirectory, String jobName) {
			// TODO Auto-generated method stub
			ModManager.debugLogger.writeMessage("------------" + jobName + "------------");

			File dlcPath = new File(fullDLCDirectory);

			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				ModManager.debugLogger.writeMessage(jobName + ": DLC Path does not exist: " + dlcPath);
				ModManager.debugLogger.writeMessage(jobName + " might not be installed. Skipping.");
				return true; //not an error
			}

			// The folder exists.
			File defaultSfar = new File(fullDLCDirectory + "Default.sfar");
			File testpatchSfar = new File(fullDLCDirectory + "Patch_001.sfar");
			File mainSfar = null;
			if (defaultSfar.exists()) {
				mainSfar = new File(fullDLCDirectory + "Default.sfar");
			} else {
				if (testpatchSfar.exists()) {
					mainSfar = new File(fullDLCDirectory + "Patch_001.sfar");
				}
			}

			if (mainSfar == null) {
				//The original DLC element is missing.
				mainSfar = new File("DLC SFAR MISSING");
			}

			String mainSfarHash = "Error: Unable to hash";
			if (mainSfar.exists()) {
				// the sfar exists. 
				//Check filesize first, its faster.
				boolean sizeMatch = true; //default to true - falls back to hashing in the event something goes wrong.
				Long filesize = mainSfar.length();
				if (!filesize.equals(sfarSizes.get(jobName))) {
					sizeMatch = false;
					ModManager.debugLogger.writeMessage(jobName + ": size mismatch between known original and existing - marking for restore");
					publish(jobName + ": DLC is modified [size]");
				}

				if (sizeMatch) {
					//We should hash it to check if it's original
					try {
						//publish(jobName + ": Known original hash: " + sfarHashes.get(jobName));
						publish(jobName + ": Verifying DLC...");
						mainSfarHash = MD5Checksum.getMD5Checksum(mainSfar.toString());
						ModManager.debugLogger.writeMessage(jobName + ": Hash of Default.sfar is " + mainSfarHash);

						if (mainSfarHash.equals(sfarHashes.get(jobName))) {
							// This DLC sfar matches the known original, we're good
							ModManager.debugLogger.writeMessage(jobName + ": OK");

							publish(jobName + ": DLC is OK");
							return true;
						} else {
							ModManager.debugLogger.writeMessage(jobName + ": hash mismatch between known original and existing - marking for restore");
							publish(jobName + ": DLC is modified [hash]");
						}
					} catch (Exception e) {
						// it died somehow
						ModManager.debugLogger.writeMessage(e.getMessage());
						return false;
					}
				}
			} else {
				ModManager.debugLogger.writeMessage(jobName + ": DLC file does not exist: " + mainSfar.toString());
				ModManager.debugLogger.writeMessage(jobName + " might not be installed. Skipping.");
			}

			// Check for backup
			File backupSfar = null;
			File defaultSfarBackup = new File(fullDLCDirectory + "Default.sfar.bak");
			File testpatchSfarBackup = new File(fullDLCDirectory + "Patch_001.sfar.bak");

			if (defaultSfarBackup.exists()) {
				backupSfar = defaultSfarBackup;
			}
			if (testpatchSfar.exists()) {
				backupSfar = testpatchSfarBackup;
			}

			if (backupSfar == null) {
				//no backup!
				publish(jobName + ": No backup exists, cannot restore.");
				JOptionPane.showMessageDialog(null,
						"<html>No backup for " + jobName
								+ " exists, you'll have to restore through Origin's Repair Game.<br>Select Tools>Backup DLC to avoid this issue after the game is restored.</html>",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			String backupSfarHash = "Error";
			if (backupSfar.exists()) {
				// the sfar exists. We should hash it to check if it's original
				try {

					publish(jobName + ": Verifying backup...");
					backupSfarHash = MD5Checksum.getMD5Checksum(backupSfar.toString());
					ModManager.debugLogger.writeMessage(jobName + ": backupSfar hash: " + backupSfarHash);
					//publish(jobName + ": Hash of backup sfar is " + backupSfarHash);
					if (backupSfarHash.equals(sfarHashes.get(jobName))) {
						// This DLC sfar matches the known original - let's copy it to Default.sfar
						publish(jobName + ": Restoring backup...");
						Files.copy(backupSfar.toPath(), mainSfar.toPath(), StandardCopyOption.REPLACE_EXISTING);
						completed++;
						publish(Integer.toString(completed));
						return true;
					} else {
						// DLC is modified but we don't have a backup
						ModManager.debugLogger.writeMessage(jobName + ": Backup hash doesn't match known original, unable to automatically restore");
						return false;
					}
				} catch (Exception e) {
					// it died somehow
					ModManager.debugLogger.writeErrorWithException("Failure restoring backup SFAR:", e);
					return false;
				}
			}

			return false;
		}

		@Override
		protected void process(List<String> updates) {
			// System.out.println("Restoring next DLC");
			for (String update : updates) {
				try {
					int updateVal = Integer.parseInt(update); // see if we got a number. if we did that means we should update the bar
					ModManager.debugLogger.writeMessage(updateVal +" of "+ numjobs+ " tasks completed");
					if (numjobs != 0) {
						progressBar.setValue((int) (((float) updateVal / numjobs) * 100));
					}
				} catch (NumberFormatException e) {
					// this is not a progress update, it's a string update
					addToQueue(update);
				}
			}

		}

		@Override
		protected void done() {
			try {
				boolean result = get();
				ModManager.debugLogger.writeMessage("RESULT OF RESTORATION THREAD:" +result);
				finishRestore();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Restore thread had an error.", e);
				callingWindow.labelStatus.setText("Restoration of game files had an error (check logs)");
				callingWindow.labelStatus.setVisible(true);
				dispose();
			}
			return;
		}

		protected void finishRestore() {
			ModManager.debugLogger.writeMessage("Finished restoration thread.");
			String status = "Unknown restore operation: " + restoreMode + ", report this to FemShep";
			switch (restoreMode) {
			case RestoreMode.ALL:
			case RestoreMode.BASEGAME:
			case RestoreMode.ALLDLC:
			case RestoreMode.UNPACKED:
			case RestoreMode.UNPACKEDBASEGAME:
			case RestoreMode.SP:
			case RestoreMode.MP:
			case RestoreMode.MPBASE:
			case RestoreMode.SPBASE:
				status = "Game files restored";
				break;
			case RestoreMode.REMOVECUSTOMDLC:
				status = "Custom DLCs removed";
				break;
			case RestoreMode.REMOVEUNPACKEDITEMS:
				status = "Deleted " + completed + " unpacked files";
				break;
			case RestoreMode.VANILLIFYDLC:
				status = "Vanillified DLC";
				break;
			}
			callingWindow.labelStatus.setText(status);
			callingWindow.labelStatus.setVisible(true);
			dispose();
		}
	}

	/**
	 * Adds a message to the queue.
	 * 
	 * @param newLine
	 *            message to add
	 */
	public void addToQueue(String newLine) {
		for (int i = consoleQueue.length - 1; i >= 1; i--) {
			consoleQueue[i] = consoleQueue[i - 1];
		}
		consoleQueue[0] = newLine;
		updateInfo();
	}

	public String getConsoleString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < consoleQueue.length; i++) {
			sb.append((consoleQueue[i] != null) ? consoleQueue[i] : "");
			if (i < consoleQueue.length - 1)
				sb.append("\n");
		}
		return sb.toString();
	}

	private void updateInfo() {
		// Log.i(Launch.APPTAG, "DebugView\n"+this.toString());
		consoleArea.setText(getConsoleString());
	}

}
