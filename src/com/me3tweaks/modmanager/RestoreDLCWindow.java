package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class RestoreDLCWindow extends JDialog {
	JLabel infoLabel;
	JTextArea consoleArea;
	final int levelCount = 7;
	String consoleQueue[];
	boolean windowOpen = true;
	String currentText;
	String BioGameDir;
	JProgressBar progressBar;
	int backupMode;

	ModManagerWindow callingWindow;

	public RestoreDLCWindow(ModManagerWindow callingWindow, String BioGameDir, int backupMode) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		this.backupMode = backupMode;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Restoring original DLCs");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(320, 230));
		this.setResizable(false);
		consoleQueue = new String[levelCount];
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		addToQueue("Restoring original DLC files if necessary.");
		new RestoreDLCJob(BioGameDir, backupMode).execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		// TODO Auto-generated method stub
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("Restoring DLCs to original versions if possible.");
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

		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// do something...
			}
		});
	}

	class RestoreDLCJob extends SwingWorker<Boolean, String> {
		int completed = 0;
		String[] modTypes;
		int numjobs = 0;
		String bioGameDir;
		HashMap<String, String> sfarHashes;

		protected RestoreDLCJob(String bioGameDir, int backupMode) {
			this.bioGameDir = bioGameDir;
			sfarHashes = ModType.getHashesMap();
			switch (backupMode) {
			case RestoreMode.ALL:
				modTypes = ModType.getHeaderNameArray();
				break;
			case RestoreMode.SP:
				modTypes = ModType.getSPHeaderNameArray();
				break;
			case RestoreMode.MP:
				modTypes = ModType.getMPHeaderNameArray();
				break;
			}

			numjobs = modTypes.length;
			if (ModManager.logging) {
				ModManager.debugLogger.writeMessage("Starting the RestoreDLCJob utility. Number of jobs to do: " + numjobs);
			}
		}

		@Override
		public Boolean doInBackground() {
			if (ModManager.logging) {
				ModManager.debugLogger.writeMessage("Starting the restore thread");
			}
			for (String dlcName : modTypes) {
				// System.out.println("Window open:" + windowOpen);
				if (windowOpen == true) {// if the window is closed this will quickly finish this thread after the current job finishes
					publish("------------Now Processing: " + dlcName + "-----------");
					processRestoreJob(ModManagerWindow.appendSlash(bioGameDir) + ModManagerWindow.appendSlash(ModType.getDLCPath(dlcName)), dlcName);
					completed++;
					publish(Integer.toString(completed));
				}
			}

			return true;

		}

		private boolean processRestoreJob(String fullDLCDirectory, String jobName) {
			// TODO Auto-generated method stub
			if (ModManager.logging) {
				ModManager.debugLogger.writeMessage("------------" + jobName + "------------");
			}
			File dlcPath = new File(fullDLCDirectory);

			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage(jobName + ": DLC Path does not exist: " + dlcPath);
					ModManager.debugLogger.writeMessage(jobName + " might not be installed. Skipping.");
				}
				return false;
			}

			// The folder exists.
			File mainSfar = new File(fullDLCDirectory + "Default.sfar");
			String mainSfarHash = "Error: Unable to hash";
			if (mainSfar.exists()) {
				// the sfar exists. We should hash it to check if it's original
				try {
					publish(jobName + ": Known original hash: " + sfarHashes.get(jobName));
					publish(jobName + ": Hashing existing Default.sfar");
					mainSfarHash = MD5Checksum.getMD5Checksum(mainSfar.toString());
					if (ModManager.logging){
						ModManager.debugLogger.writeMessage(jobName + ": Hash of Default.sfar is " + mainSfarHash);
					}
					publish(jobName + ": mainSfar hash: " + mainSfarHash);
					if (mainSfarHash.equals(sfarHashes.get(jobName))) {
						// This DLC sfar matches the known original, we're good
						if (ModManager.logging) {
							ModManager.debugLogger.writeMessage(jobName + ": OK");
						}
						publish(jobName + ": Verified as original");
						return true;
					}
				} catch (Exception e) {
					// it died somehow
					if (ModManager.logging) {
						ModManager.debugLogger.writeMessage(e.getMessage());
					}
					return false;
				}
			} else {

				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage(jobName + ": DLC file does not exist: " + mainSfar.toString());
					ModManager.debugLogger.writeMessage(jobName + " might not be installed. Skipping.");
					ModManager.debugLogger.writeMessage("Note: TESTPATCH is not implemented in this build. It will always fail.");
				}
			}

			// Check for backup
			File backupSfar = new File(fullDLCDirectory + "Default.sfar.bak");
			String backupSfarHash = "Error";
			if (backupSfar.exists()) {
				// the sfar exists. We should hash it to check if it's original
				try {
					publish(jobName + ": Hashing existing Default.sfar.bak");
					backupSfarHash = MD5Checksum.getMD5Checksum(backupSfar.toString());
					if (ModManager.logging){
						ModManager.debugLogger.writeMessage(jobName + ": backupSfar hash: " + backupSfarHash);
					}
					publish(jobName + ": Hash of backup sfar is " + backupSfarHash);
					if (backupSfarHash.equals(sfarHashes.get(jobName))) {
						// This DLC sfar matches the known original - let's copy it to Default.sfar
						publish(jobName + ": Restoring backup sfar to Default.sfar");
						Files.copy(backupSfar.toPath(), mainSfar.toPath(), StandardCopyOption.REPLACE_EXISTING);
						return true;
					} else {
						// DLC is modified but we don't have a backup
						if (ModManager.logging){
							ModManager.debugLogger.writeMessage(jobName + ": Backup hash doesn't match known original, unable to automatically restore");
						}
						return false;
					}
				} catch (Exception e) {
					// it died somehow
					e.printStackTrace();
					return false;
				}
			}

			return true;
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
		if (ModManager.logging){
			ModManager.debugLogger.writeMessage("Finished restoring DLCs.");
		}
		callingWindow.labelStatus.setText(" DLCs restored");
		callingWindow.labelStatus.setVisible(true);
		dispose();
	}

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
