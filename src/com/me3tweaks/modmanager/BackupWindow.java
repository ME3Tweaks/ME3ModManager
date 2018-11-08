package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

import com.me3tweaks.modmanager.objects.ModTypeConstants;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.CheckBoxLabel;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class BackupWindow extends JDialog {
	private static final String allDlcBackedUpStr = "All unmodified DLC has been backed up.";
	JLabel infoLabel;
	// CheckBoxList dlcList;
	boolean windowOpen = true;
	HashMap<String, JCheckBox> checkboxMap;
	JProgressBar progressBar;
	JButton backupButton;
	ArrayList<BackupPanelPairs> panelPairs;
	private JLabel statusLabel, noBackedUpDLCLabel, noNotBackedUpDLCLabel;
	private HashMap<String, Long> sizesMap = ModTypeConstants.getSizesMap();
	private String currentBackupInProgress;

	/**
	 * Manually invoked backup window
	 * 
	 * @param callingWindow
	 */
	public BackupWindow(ModManagerWindow callingWindow) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);
		checkboxMap = new HashMap<String, JCheckBox>();
		panelPairs = new ArrayList<>();
		setupWindow2(callingWindow);
		setVisible(true);
	}

	/**
	 * Automated backup window
	 *
	 * @param callingWindow
	 * @param dlcName
	 *            DLC to backup
	 */
	public BackupWindow(ModManagerWindow callingWindow, String dlcName) {
		super(null, Dialog.ModalityType.APPLICATION_MODAL);

		checkboxMap = new HashMap<String, JCheckBox>();

		this.setTitle("DLC Backup");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(260, 77));
		this.setResizable(false);

		setupWindowAutomated(dlcName, callingWindow);

		new backupDLCJob(ModManagerWindow.GetBioGameDir(), new String[] { dlcName }, true).execute();
		setVisible(true);
	}

	private void setupWindow2(ModManagerWindow callingWindow) {
		setTitle("Backup DLCs");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel rootPanel = new JPanel(new VerticalLayout());
		infoLabel = new JLabel("<html><center>Select DLCs to backup.<br>These backups are used when using Mod Manager SFAR restore options.</center></html>",
				SwingConstants.CENTER);
		rootPanel.add(infoLabel);

		statusLabel = new JLabel("", SwingConstants.CENTER);
		rootPanel.add(statusLabel);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		rootPanel.add(progressBar, BorderLayout.SOUTH);

		JPanel backedUpListPanel = new JPanel(new HorizontalLayout());
		JPanel notBackedUpListPanel = new JPanel(new HorizontalLayout());

		JPanel MPDLCbackedUpListPanel = new JPanel(new VerticalLayout());
		JPanel SPDLCbackedUpListPanel = new JPanel(new VerticalLayout());

		JPanel MPDLCbackUpListPanel = new JPanel(new VerticalLayout());
		JPanel SPDLCbackUpListPanel = new JPanel(new VerticalLayout());

		// dlcList = new CheckBoxList();
		String[] headerArray = ModTypeConstants.getDLCHeaderNameArray();
		//check sfar size.

		int i = 0;
		// Add and enable/disable DLC checkboxes and add to hashmap
		boolean hasDLCNotBackedUp = false;
		for (String dlcName : headerArray) {
			JCheckBox checkbox = new JCheckBox(dlcName);
			checkbox.setVerticalAlignment(SwingConstants.CENTER);
			JXCollapsiblePane backupPane = new JXCollapsiblePane();
			backupPane.add(checkbox);

			JXCollapsiblePane backedUpPane = new JXCollapsiblePane();
			backedUpPane.add(new CheckBoxLabel(dlcName));

			if (i < 8) {
				MPDLCbackedUpListPanel.add(backedUpPane);
				MPDLCbackUpListPanel.add(backupPane);
			} else {
				SPDLCbackedUpListPanel.add(backedUpPane);
				SPDLCbackUpListPanel.add(backupPane);
			}
			i++;

			BackupPanelPairs panelPair = new BackupPanelPairs(dlcName, backupPane, checkbox, backedUpPane);
			panelPairs.add(panelPair);

			// checkBoxPanel.add(checkbox);
			String filepath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName));
			File dlcPath = new File(filepath);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				checkbox.setEnabled(false);
				checkbox.setToolTipText("This DLC is not installed.");
				checkboxMap.put(dlcName, checkbox);

				ModManager.debugLogger.writeMessage("DLC does not appear to be installed: " + dlcName);
				continue;
			}

			// The folder exists.
			File mainSfar = new File(dlcPath + "\\Default.sfar");
			File testpatchSfar = new File(dlcPath + "\\Patch_001.sfar");
			ModManager.debugLogger.writeMessage("Looking for Default.sfar, Patch_001.sfar in " + filepath);

			if (mainSfar.exists() || testpatchSfar.exists()) {
				ModManager.debugLogger.writeMessage("Found a .sfar");
				// File exists.
				checkbox.setEnabled(true);
				//check for backups
				File mainSfarbackup = new File(dlcPath + "\\Default.sfar.bak");
				File testpathSfarbackup = new File(dlcPath + "\\Patch_001.sfar.bak");
				if (!mainSfarbackup.exists() && !testpathSfarbackup.exists()) {
					//Checking size...
					File f = mainSfar;
					boolean isTestPatch = false;
					if (!f.exists()) {
						f = testpatchSfar;
						isTestPatch = true;
					}

					if (f.length() != sizesMap.get(dlcName) && (isTestPatch && f.length() != ModTypeConstants.TESTPATCH_16_SIZE)) {
						//MODIFIED!
						ModManager.debugLogger.writeMessage("Unbacked-up DLC has been modified: " + dlcName);
						checkbox.setSelected(false);
						checkbox.setEnabled(false);
						checkbox.setToolTipText("This DLC archive has been modified and cannot be backed up");
						checkbox.setText("<html><body><span style='text-decoration: line-through;'>" + checkbox.getText() + "</span></body></html>");
					} else {
						checkbox.setSelected(true);
						checkbox.setToolTipText("Check the box to backup (" + ResourceUtils.humanReadableByteCount(f.length(), true) + ")");
						hasDLCNotBackedUp = true;
					}
				}
				checkboxMap.put(dlcName, checkbox);
				continue;
			} else {
				//this won't be hit unless the DLC folder exists but the SFAR is missing
				ModManager.debugLogger.writeMessage(dlcName + " path was found but the SFAR is missing - its improperly installed.");
				checkbox.setEnabled(false);
				checkbox.setToolTipText("This DLC is not properly installed.");
				backedUpPane.setCollapsed(false);
				backupPane.setCollapsed(true);
				checkboxMap.put(dlcName, checkbox);
				continue;
			}
		}

		TitledBorder backedUpBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DLC already backed up");
		TitledBorder notBackedUpBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DLC not yet backed up");

		JPanel outerTopPanel = new JPanel(new VerticalLayout());
		outerTopPanel.setBorder(backedUpBorder);
		backedUpListPanel.add(MPDLCbackedUpListPanel);
		backedUpListPanel.add(SPDLCbackedUpListPanel);

		noBackedUpDLCLabel = new JLabel("No DLC has been backed up yet.", SwingConstants.CENTER);
		outerTopPanel.add(backedUpListPanel);
		outerTopPanel.add(noBackedUpDLCLabel);

		JPanel outerBottomPanel = new JPanel(new VerticalLayout());
		outerBottomPanel.setBorder(notBackedUpBorder);
		notBackedUpListPanel.add(MPDLCbackUpListPanel);
		notBackedUpListPanel.add(SPDLCbackUpListPanel);

		noNotBackedUpDLCLabel = new JLabel(allDlcBackedUpStr, SwingConstants.CENTER);
		outerBottomPanel.add(notBackedUpListPanel);
		outerBottomPanel.add(noNotBackedUpDLCLabel);

		backupButton = new JButton("Backup selected DLCs");
		backupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//write to settings
				new backupDLCJob(ModManagerWindow.GetBioGameDir(), getJobs(), false).execute();
			}
		});
		if (!hasDLCNotBackedUp) {
			backupButton.setText("All DLC already backed up");
			backupButton.setEnabled(false);
			backupButton.setVisible(false);
		}
		outerBottomPanel.add(backupButton);

		JPanel mainContentPanel = new JPanel(new HorizontalLayout());
		mainContentPanel.add(outerTopPanel);
		mainContentPanel.add(outerBottomPanel);
		rootPanel.add(mainContentPanel);

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
		refreshViewState();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	private boolean isAllAvailableDLCIsBackedUp(){
		String[] headerArray = ModTypeConstants.getDLCHeaderNameArray();
		for (String dlcName : headerArray) {
			String filepath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName));
			File dlcPath = new File(filepath);
			if (!dlcPath.exists()) {
				continue;
			}

			// The folder exists.
			File mainSfar = new File(dlcPath + "\\Default.sfar");
			File testpatchSfar = new File(dlcPath + "\\Patch_001.sfar");
			ModManager.debugLogger.writeMessage("Looking for Default.sfar, Patch_001.sfar in " + filepath);

			if (mainSfar.exists() || testpatchSfar.exists()) {
				// File exists.
				File mainSfarbackup = new File(dlcPath + "\\Default.sfar.bak");
				File testpathSfarbackup = new File(dlcPath + "\\Patch_001.sfar.bak");
				if (!mainSfarbackup.exists() && !testpathSfarbackup.exists()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Refreshes the interface's collapse and uncollapsed panels, as well as
	 * hiding/showing the backup button and indicators of no content in both
	 * columns.
	 */
	private void refreshViewState() {
		ModManager.debugLogger.writeMessage("refreshViewState()");
		boolean hasDLCBackedUp = false;
		boolean hasDLCNotBackedUp = false;

		String[] headerArray = ModTypeConstants.getDLCHeaderNameArray();

		// Add and enable/disable DLC checkboxes and add to hashmap
		for (String dlcName : headerArray) {
			BackupPanelPairs pair = null;
			for (BackupPanelPairs p : panelPairs) {
				if (p.header.equals(dlcName)) {
					pair = p;
					break;
				}
			}

			if (pair == null) {
				return; //this shouldn't happen.
			}

			JCheckBox checkbox = pair.checkbox;
			JXCollapsiblePane backupPane = pair.backupPanel;
			JXCollapsiblePane backedUpPane = pair.backedUpPanel;

			String filepath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName));
			File dlcPath = new File(filepath);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				checkbox.setEnabled(false);
				backedUpPane.setCollapsed(true);
				backupPane.setCollapsed(false);
				continue;
			}

			// The folder exists.
			File mainSfar = new File(dlcPath + "\\Default.sfar");
			File testpatchSfar = new File(dlcPath + "\\Patch_001.sfar");

			if (mainSfar.exists() || testpatchSfar.exists()) {
				File mainSfarbackup = new File(dlcPath + "\\Default.sfar.bak");
				File testpathSfarbackup = new File(dlcPath + "\\Patch_001.sfar.bak");
				if (!mainSfarbackup.exists() && !testpathSfarbackup.exists()) {
					//backup does not exist
					//Checking size...
					File f = mainSfar;
					boolean isTestPatch = false;
					if (!f.exists()) {
						f = testpatchSfar;
						isTestPatch = true;
					}

					if (f.length() != sizesMap.get(dlcName) && (isTestPatch && f.length() != ModTypeConstants.TESTPATCH_16_SIZE)) {
						//dlc modified - do not show button for this.
					} else {
						if (dlcName.equalsIgnoreCase(currentBackupInProgress)) {
							hasDLCNotBackedUp = true;
						}
						hasDLCNotBackedUp = true;
					}
					//backup missing but sfar eixsts
					backedUpPane.setCollapsed(true);
					backupPane.setCollapsed(false);
				} else {
					//backup file exists
					if (dlcName.equals(currentBackupInProgress)) {
						//backup in progress
						backedUpPane.setCollapsed(true);
						backupPane.setCollapsed(false);
					} else {
						//backup done
						backedUpPane.setCollapsed(false);
						backupPane.setCollapsed(true);
						hasDLCBackedUp = true;
					}
				}
				continue;
			} else {
				//dlc not installed
				checkbox.setEnabled(false);
				backedUpPane.setCollapsed(false);
				backupPane.setCollapsed(true);
				continue;
			}
		}

		noBackedUpDLCLabel.setVisible(!hasDLCBackedUp);
		noNotBackedUpDLCLabel.setVisible(!hasDLCNotBackedUp);
		if (!hasDLCNotBackedUp) {
			backupButton.setText("All installed DLC already backed up");
			backupButton.setEnabled(false);
			backupButton.setVisible(false);
		}
		pack();
	}

	/**
	 * Sets up the window for an automated session.
	 * @param dlcName THe name of the DLC to backup.
	 * @param callingWindow The calling window to center against. Can be null.
	 */
	private void setupWindowAutomated(String dlcName, JFrame callingWindow) {
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

		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
	}

	private String[] getJobs() {
		String[] dlcNames = ModTypeConstants.getDLCHeaderNameArray();
		ArrayList<String> jobs = new ArrayList<String>();
		for (String dlcName : dlcNames) {
			JCheckBox checkbox = checkboxMap.get(dlcName);
			if (checkbox != null && checkbox.isSelected()) {
				ModManager.debugLogger.writeMessage("Job added to backup: " + checkbox.getText() + " at " + ModTypeConstants.getDLCPath(checkbox.getText()));
				jobs.add(checkbox.getText());
			}
		}

		return jobs.toArray(new String[jobs.size()]);
	}

	class backupDLCJob extends SwingWorker<Boolean, ThreadCommand> {
		int completed = 0;
		int numjobs = 0;
		String bioGameDir;
		String[] jobs;
		boolean closeOnComplete;
		ArrayList<String> failedBackups;
		private int jobCode;

		protected backupDLCJob(String bioGameDir, String[] jobs, boolean closeOnComplete) {
			if (backupButton != null) {
				backupButton.setEnabled(false);
				infoLabel.setText("Running backup job...");
				statusLabel.setText("<html><center>Preparing to backup DLC<br>Please wait</center></html>");
			}

			jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("DLCBackup", "Backing up DLC...");
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Backing up DLC...");
			this.closeOnComplete = closeOnComplete;
			this.jobs = jobs;
			this.bioGameDir = bioGameDir;
			numjobs = jobs.length;
			failedBackups = new ArrayList<String>();

			ModManager.debugLogger.writeMessage("Starting the backup DLC thread. Number of jobs to do: " + numjobs);
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the restore thread");
			HashMap<String, String> sfarHashes = ModTypeConstants.getHashesMap();
			for (String dlcName : jobs) {
				if (windowOpen == true) {// if the window is closed this will quickly finish this thread after the current job finishes
					if (processBackupJob(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(ModTypeConstants.getDLCPath(dlcName)), dlcName, sfarHashes)) {
						completed++;
					}
					publish(new ThreadCommand("PROGRESS_UPDATE"));
					publish(new ThreadCommand("DLC_JOB_FINISHED", dlcName));
				}
			}
			return true;
		}

		private boolean processBackupJob(String fullDLCDirectory, String dlcName, HashMap<String, String> sfarHashes) {
			ModManager.debugLogger.writeMessage("Processing backup job for " + dlcName);
			// TODO Auto-generated method stub
			File dlcPath = new File(fullDLCDirectory);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				ModManager.debugLogger.writeMessage(fullDLCDirectory + " does not exist. It might not be installed (this should have been caught!)");
				return false;
			}

			// The folder exists.
			File mainSfar = new File(fullDLCDirectory + "Default.sfar");
			File backupSfar = new File(fullDLCDirectory + "Default.sfar.bak");

			File testpatchSfar = new File(fullDLCDirectory + "Patch_001.sfar");
			File backupTestpatchSfar = new File(fullDLCDirectory + "Patch_001.sfar.bak");

			if (mainSfar.exists()) {
				try {
					//we can just check sfar size in pre-select step
					//Thread.yield(); //a UI updater method that is dependent on the original background thread. So if the background thread is busy it doesn't work. well done sun.
					publish(new ThreadCommand("BACKING_UP", dlcName, mainSfar.length()));

					Files.copy(mainSfar.toPath(), backupSfar.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					addFailure(dlcName, "I/O Exception occured: " + e.getMessage());
					ModManager.debugLogger.writeErrorWithException("IO Exception in backup procedures!", e);
					return false;
				} catch (Exception e) {
					addFailure(dlcName, "Unknown error occured: " + e.getMessage());
					ModManager.debugLogger.writeErrorWithException("Unknown error in backup procedures!", e);
					return false;
				}
				return true;
			}
			if (testpatchSfar.exists()) {
				try {
					publish(new ThreadCommand("BACKING_UP", dlcName, testpatchSfar.length()));
					Files.copy(testpatchSfar.toPath(), backupTestpatchSfar.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					addFailure(dlcName, "I/O Exception occured: " + e.getMessage());
					ModManager.debugLogger.writeErrorWithException("IO Exception in backup procedures!", e);
					return false;
				} catch (Exception e) {
					addFailure(dlcName, "Unknown error occured: " + e.getMessage());
					ModManager.debugLogger.writeErrorWithException("Unknown error in backup procedures!", e);
					return false;
				}
				return true;
			}
			return false; //neither sfar could be found.
		}

		@Override
		protected void process(List<ThreadCommand> updates) {
			// System.out.println("Restoring next DLC");
			for (ThreadCommand update : updates) {
				switch (update.getCommand()) {
				case "PROGRESS_UPDATE":
					if (numjobs != 0) {
						progressBar.setValue((int) (((float) completed / numjobs) * 100));
					}
					break;
				case "DLC_JOB_FINISHED":
					JCheckBox checkbox = checkboxMap.get(update.getMessage());
					if (checkbox != null) {
						checkbox.setSelected(false);
						break;
					}
					break;
				case "BACKING_UP":
					currentBackupInProgress = update.getMessage();
					if (statusLabel != null) {
						statusLabel.setText("<html><center>Backing Up<br>" + update.getMessage() + " (" + ResourceUtils.humanReadableByteCount((long) update.getData(), true)
								+ ")</center></html>");
						refreshViewState();
						new Timer().schedule(new RepackUITask(BackupWindow.this), 250); //animation change
					}
					break;
				}
			}
		}

		@Override
		protected void done() {
			ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
			currentBackupInProgress = null;
			try {
				get();
				finishBackup(completed);
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Exception in the backup dlc thread: ", e);
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Error backing up DLC");
				if (backupButton != null) {
					refreshViewState();
					infoLabel.setText("<html><center>Backup job completed but with errors.<br>See the Mod Manager log in the help menu.</center></html>");
					statusLabel.setText("");
				}
			}
			if (closeOnComplete) {
				dispose();
			} else {
				pack();
				new Timer().schedule(new RepackUITask(BackupWindow.this), 250);
			}
			showFailedBackups();
		}

		/**
		 * Adds a failed indicator for the backup job.
		 * 
		 * @param dlcName
		 *            Failed backup name.
		 * @param reason
		 *            Reason the job failed.
		 */
		public void addFailure(String dlcName, String reason) {
			failedBackups.add(dlcName + ": " + reason);
		}

		/**
		 * Shows a JDialog if a backup job failed. If there are no failures, it
		 * won't show anything.
		 */
		public void showFailedBackups() {
			if (failedBackups.size() > 0) {
				String header = "The following DLCs failed to backup:\n\n";
				for (String failed : failedBackups) {
					header += failed + "\n";
				}
				JOptionPane.showMessageDialog(BackupWindow.this, header, "DLC backup errors", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		protected void finishBackup(int completed) {
			ModManager.debugLogger.writeMessage("Finished backing up DLCs.");
			if (jobs.length == 1 && completed == 1) {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(jobs[0] + " backed up");
			} else {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(completed + " DLCs backed up");
			}
			if (backupButton != null) {
				refreshViewState();
				infoLabel.setText("<html><center>Backup job completed.<br>You can restore these DLC files via the Restore Menu by using the SFAR options.</center></html>");
				statusLabel.setText("");
				backupButton.setEnabled(isAllAvailableDLCIsBackedUp());
			}
		}
	}

	class RepackUITask extends TimerTask {

		private JDialog mDw;

		public RepackUITask(JDialog dw) {
			mDw = dw;
		}

		public void run() {
			ModManager.debugLogger.writeMessage("UI Timer: repacking DLC window");
			mDw.pack();
		}
	}

	private class BackupPanelPairs {
		private String header;
		private JXCollapsiblePane backupPanel;
		private JXCollapsiblePane backedUpPanel;
		private JCheckBox checkbox;

		public BackupPanelPairs(String header, JXCollapsiblePane backupPanel, JCheckBox checkbox, JXCollapsiblePane backedUpPanel) {
			this.header = header;
			this.backupPanel = backupPanel;
			this.checkbox = checkbox;
			this.backedUpPanel = backedUpPanel;
		}
	}
}
