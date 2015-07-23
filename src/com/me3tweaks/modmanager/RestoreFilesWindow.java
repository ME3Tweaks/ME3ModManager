package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.me3tweaks.modmanager.basegamedb.BasegameHashDB;
import com.me3tweaks.modmanager.basegamedb.RepairFileInfo;
import com.me3tweaks.modmanager.objects.ModType;

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
	int backupMode;
	ModManagerWindow callingWindow;

	public RestoreFilesWindow(ModManagerWindow callingWindow, String BioGameDir, int backupMode) {
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		this.backupMode = backupMode;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Restoring game files");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(330, 240));
		this.setResizable(false);
		consoleQueue = new String[levelCount];
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		addToQueue("Restoring original game files if necessary.");
		new RestoreDataJob(backupMode).execute();
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
		rootPanel.setBorder(new EmptyBorder(5,5,5,5));
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
		String[] modTypes;
		int numjobs = 0;
		HashMap<String, String> sfarHashes;
		HashMap<String, Long> sfarSizes;


		protected RestoreDataJob(int backupMode) {
			sfarHashes = ModType.getHashesMap();
			sfarSizes = ModType.getSizesMap();
			switch (backupMode) {
			case RestoreMode.BASEGAME:
				modTypes = new String[] {ModType.BASEGAME};
				break;
			case RestoreMode.ALL:
				modTypes = ModType.getHeaderNameArray();
				break;
			case RestoreMode.SP:
				modTypes = ModType.getSPHeaderNameArray();
				break;
			case RestoreMode.MP:
				modTypes = ModType.getMPHeaderNameArray();
				break;
			case RestoreMode.MPBASE:
				modTypes = ModType.getMPBaseHeaderNameArray();
				break;
			case RestoreMode.SPBASE:
				modTypes = ModType.getSPBaseHeaderNameArray();
				break;
			}

			numjobs = modTypes.length;
			ModManager.debugLogger.writeMessage("Starting the Restore Data utility. Number of jobs to do: " + numjobs);
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the restore thread");
			for (String restoreType : modTypes) {
				// System.out.println("Window open:" + windowOpen);
				if (windowOpen == true) {// if the window is closed this will quickly finish this thread after the current job finishes
					//publish("------------Now Processing: " + dlcName + "-----------");
					if (restoreType.equals(ModType.BASEGAME)) {
						publish(ModType.BASEGAME + ": Loading repair database");
						processRestoreBasegame();
					} else {
						processRestoreJob(ModManager.appendSlash(RestoreFilesWindow.this.BioGameDir) + ModManager.appendSlash(ModType.getDLCPath(restoreType)), restoreType);
					}
					completed++;
					publish(Integer.toString(completed));
				}
			}

			return true;

		}
		
		/**
		 * Processes a data restoration of the basegame by copying all files in the backup directory back into the base game folder.
		 * @return
		 */
		private boolean processRestoreBasegame() {
			//load Basegame DB
			BasegameHashDB bghDB = new BasegameHashDB(null,new File(BioGameDir).getParent(), false);
			String me3dir = (new File(RestoreFilesWindow.this.BioGameDir)).getParent();
			String backupfolder = ModManager.appendSlash(me3dir)+"cmmbackup\\";
			File backupdir = new File(backupfolder);
			if (backupdir.exists()){
				Collection<File> backupfiles = FileUtils.listFiles(new File(backupfolder), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
				for (File backup : backupfiles) {
					//verify it.
					String relative = ResourceUtils.getRelativePath(backup.getAbsolutePath(), backupfolder, File.separator);
					RepairFileInfo rfi = bghDB.getFileInfo(relative);
					boolean restoreAnyways = false;
					if (rfi == null) {
						int reply = JOptionPane.showOptionDialog(null, "<html>The file:<br>"+relative+"<br>is not in the repair database. "
								+ "Restoring this file may overwrite your default setup if you use custom mods like texture swaps.<br></html>", "Restoring Unverified File", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE, null, new String[] {"Restore this file", "Skip restoring this file", "Cancel basegame restore"}, "default");
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
							int reply = JOptionPane.showOptionDialog(null, "<html>The filesize of the file:<br>"+relative+"<br>does not match the one stored in the repair game database.<br>"
									+ backup.length() +" bytes (backup) vs "+rfi.filesize+" bytes (database)<br><br>"
									+ "This file could be corrupted or modified since the database was created.<br>"
									+ "Restoring this file may overwrite your default setup if you use custom mods like texture swaps.<br></html>", "Restoring Unverified File", JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE, null, new String[] {"Restore this file", "Skip restoring this file", "Cancel basegame restore"}, "default");
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
							if (!MD5Checksum.getMD5Checksum(backup.getAbsolutePath()).equals(rfi.md5)){
								int reply = JOptionPane.showOptionDialog(null, "<html>The hash of the file:<br>"+relative+"<br>does not match the one stored in the repair game database.<br>"
										+ "This file has changed since the database was created.<br>"
										+ "Restoring this file may overwrite your default setup if you use custom mods like texture swaps.<br></html>", "Restoring Unverified File", JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.WARNING_MESSAGE, null, new String[] {"Restore this file", "Skip restoring this file", "Cancel basegame restore"}, "default");
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
					ModManager.debugLogger.writeMessage("Restoring "+relative);
					try {
						publish(ModType.BASEGAME + ": Restoring "+backup.getName());
						Files.copy(Paths.get(backup.toString()), Paths.get(ModManager.appendSlash(me3dir)+relative), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						return false;
					}
				}
			} else {
				return false;
			}
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
				
				return false;
			}

			// The folder exists.
			File defaultSfar = new File(fullDLCDirectory + "Default.sfar");
			File testpatchSfar = new File(fullDLCDirectory + "Patch_001.sfar");
			File mainSfar = null;
			if (defaultSfar.exists()) {
				mainSfar = new File(fullDLCDirectory + "Default.sfar");
			} else {
				if (testpatchSfar.exists()){
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
				if (!filesize.equals(sfarSizes.get(jobName))){
					System.out.println("size mismatch: "+filesize+" vs "+sfarSizes.get(jobName));
					sizeMatch = false;
					ModManager.debugLogger.writeMessage(jobName + ": size mismatch between known original and existing - marking for restore");
					
					publish(jobName + ": Mismatch on known original filesize");
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
			
			if (backupSfar == null){
				//no backup!
				publish(jobName + ": No backup exists, cannot restore.");
				JOptionPane.showMessageDialog(null, "<html>No backup for "+jobName+" exists, you'll have to restore through Origin's Repair Game.<br>Select Tools>Backup DLC to avoid this issue after the game is restored.</html>","Error", JOptionPane.ERROR_MESSAGE);
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
						return true;
					} else {
						// DLC is modified but we don't have a backup
						ModManager.debugLogger.writeMessage(jobName + ": Backup hash doesn't match known original, unable to automatically restore");
						return false;
					}
				} catch (Exception e) {
					// it died somehow
					e.printStackTrace();
					return false;
				}
			} else {
				System.out.println("BACKUP DOES NOT EXIST.");
			}

			return false;
		}

		@Override
		protected void process(List<String> updates) {
			// System.out.println("Restoring next DLC");
			for (String update : updates) {
				try {
					Integer.parseInt(update); // see if we got a number. if we did that means we should update the bar
					if (numjobs != 0) {
						progressBar.setValue((int) (((float) completed / numjobs) * 100));
					}
				} catch (NumberFormatException e) {
					// this is not a progress update, it's a string update
					addToQueue(update);
				}
			}

		}

		@Override
		protected void done() {
			finishRestore();
			return;
		}
	}

	protected void finishRestore() {
		ModManager.debugLogger.writeMessage("Finished restoring data.");
		callingWindow.labelStatus.setText("Game files restored");
		callingWindow.labelStatus.setVisible(true);
		dispose();
	}

	/**
	 * Adds a message to the queue.
	 * @param newLine message to add
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
