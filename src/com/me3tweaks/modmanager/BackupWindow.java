package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.objects.ModType;

@SuppressWarnings("serial")
public class BackupWindow extends JDialog {
	JLabel infoLabel;
	// CheckBoxList dlcList;
	String consoleQueue[];
	boolean windowOpen = true;
	HashMap<String, JCheckBox> checkboxMap;
	ArrayList<String> failedBackups;
	String currentText;
	String BioGameDir;
	JPanel checkBoxPanel;
	JProgressBar progressBar;
	JButton backupButton;

	ModManagerWindow callingWindow;

	/**
	 * Manually invoked backup window
	 * 
	 * @param callingWindow
	 * @param BioGameDir
	 */
	public BackupWindow(ModManagerWindow callingWindow, String BioGameDir) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		checkboxMap = new HashMap<String, JCheckBox>();
		failedBackups = new ArrayList<String>();

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Backup DLCs");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(260, 452));

		setupWindow();

		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		//Set the backup flag to true
		Wini ini;
		try {
			File settings = new File(ModManager.settingsFilename);
			if (!settings.exists()) {
				ModManager.debugLogger.writeMessage("Created new settings file, didn't previously exist.");
				settings.createNewFile();
			}
			ini = new Wini(settings);
			ini.put("Settings", "dlc_backed_up", "1");
			ini.store();
		} catch (InvalidFileFormatException e) {
			ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
		}
		this.setVisible(true);
	}

	/**
	 * Automated backup window
	 * 
	 * @param modManagerWindow
	 * @param bioGameDir
	 * @param dlcName
	 *            DLC to backup
	 */
	public BackupWindow(ModManagerWindow callingWindow, String bioGameDir, String dlcName) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = bioGameDir;
		checkboxMap = new HashMap<String, JCheckBox>();
		failedBackups = new ArrayList<String>();

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("DLC Backup");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(260, 77));
		this.setResizable(false);

		setupWindowAutomated(dlcName);

		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		new backupDLCJob(BioGameDir, new String[] { dlcName }, true).execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("<html>Select DLCs to backup.\nThis will create backup .sfar files for you.\nThis backup tool only backs up original DLC files, not modified ones.</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		northPanel.add(progressBar, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);

		checkBoxPanel = new JPanel(new BorderLayout());
		TitledBorder DLCBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Installed DLCs");
		checkBoxPanel.setBorder(DLCBorder);
		JPanel checkBoxPanelLeft = new JPanel();
		checkBoxPanelLeft.setLayout(new BoxLayout(checkBoxPanelLeft, BoxLayout.Y_AXIS));
		JPanel checkBoxPanelRight = new JPanel();
		checkBoxPanelRight.setLayout(new BoxLayout(checkBoxPanelRight, BoxLayout.Y_AXIS));

		// dlcList = new CheckBoxList();
		String[] headerArray = ModType.getHeaderNameArray();

		int i = 0;
		// Add and enable/disable DLC checkboxes and add to hashmap
		for (String dlcName : headerArray) {
			JCheckBox checkbox = new JCheckBox(dlcName);
			// checkBoxPanel.add(checkbox);
			String filepath = ModManager.appendSlash(BioGameDir) + ModManager.appendSlash(ModType.getDLCPath(dlcName));
			File dlcPath = new File(filepath);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				checkbox.setEnabled(false);
				if (i < 8) {
					checkBoxPanelLeft.add(checkbox);
				} else {
					checkBoxPanelRight.add(checkbox);
				}
				i++;
				checkboxMap.put(dlcName, checkbox);
				continue;
			}

			// The folder exists.
			File mainSfar = new File(dlcPath + "\\Default.sfar");
			File testpathSfar = new File(dlcPath + "\\Patch_001.sfar");
			ModManager.debugLogger.writeMessage("Looking for Default.sfar, Patch_001.sfar in " + filepath);
			if (mainSfar.exists() || testpathSfar.exists()) {
				// File exists.

				checkbox.setEnabled(true);
				if (i < 8) {
					checkBoxPanelLeft.add(checkbox);
				} else {
					checkBoxPanelRight.add(checkbox);
				}
				i++;
				checkboxMap.put(dlcName, checkbox);
				continue;
			} else {
				ModManager.debugLogger.writeMessage(dlcName + " was not found.");
				checkbox.setEnabled(false);
				if (i < 8) {
					checkBoxPanelLeft.add(checkbox);
				} else {
					checkBoxPanelRight.add(checkbox);
				}
				i++;
				checkboxMap.put(dlcName, checkbox);
				continue;
			}
		}
		checkBoxPanel.add(checkBoxPanelLeft, BorderLayout.WEST);
		checkBoxPanel.add(checkBoxPanelRight, BorderLayout.EAST);
		rootPanel.add(checkBoxPanel, BorderLayout.CENTER);

		backupButton = new JButton("Backup selected DLCs");
		backupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//write to settings
				new backupDLCJob(BioGameDir, getJobs(), false).execute();
			}
		});

		JPanel backupPanel = new JPanel(new BorderLayout());
		backupPanel.add(backupButton, BorderLayout.CENTER);

		rootPanel.add(backupPanel, BorderLayout.SOUTH);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// methods will read this variable
			}
		});
	}

	private void setupWindowAutomated(String dlcName) {
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
	}

	private String[] getJobs() {
		String[] dlcNames = ModType.getHeaderNameArray();
		ArrayList<String> jobs = new ArrayList<String>();
		for (String dlcName : dlcNames) {
			JCheckBox checkbox = checkboxMap.get(dlcName);
			if (checkbox != null && checkbox.isSelected()) {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("Job added to backup: " + checkbox.getText() + " at "
							+ ModType.getDLCPath(checkbox.getText()));
				}
				jobs.add(checkbox.getText());
			}
		}

		return jobs.toArray(new String[jobs.size()]);
	}

	class backupDLCJob extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numjobs = 0;
		String bioGameDir;
		String[] jobs;
		boolean closeOnComplete;

		protected backupDLCJob(String bioGameDir, String[] jobs, boolean closeOnComplete) {
			if (backupButton != null) {
				backupButton.setEnabled(false);
				infoLabel.setText("Backing up DLC...");
			}
			this.closeOnComplete = closeOnComplete;
			this.jobs = jobs;
			this.bioGameDir = bioGameDir;
			numjobs = jobs.length;

			ModManager.debugLogger.writeMessage("Starting the backupDLCJob utility. Number of jobs to do: " + numjobs);
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the restore thread");
			HashMap<String, String> sfarHashes = ModType.getHashesMap();
			for (String dlcName : jobs) {
				if (windowOpen == true) {// if the window is closed this will quickly finish this thread after the current job finishes
					ModManager.debugLogger.writeMessage("Processing backup job");
					if (processBackupJob(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(ModType.getDLCPath(dlcName)), dlcName,
							sfarHashes)) {
						completed++;
					}
					publish(Integer.toString(completed));
				}
			}

			return true;

		}

		private boolean processBackupJob(String fullDLCDirectory, String dlcName, HashMap<String, String> sfarHashes) {
			// TODO Auto-generated method stub
			File dlcPath = new File(fullDLCDirectory);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage(fullDLCDirectory
							+ " does not exist. It might not be installed (this should have been caught!");
				}
				return false;
			}

			// The folder exists.
			File mainSfar = new File(fullDLCDirectory + "Default.sfar");
			File backupSfar = new File(fullDLCDirectory + "Default.sfar.bak");

			File testpatchSfar = new File(fullDLCDirectory + "Patch_001.sfar");
			File backupTestpatchSfar = new File(fullDLCDirectory + "Patch_001.sfar.bak");

			if (mainSfar.exists()) {
				try {
					//We should hash it and compare it against the known original
					if (!(MD5Checksum.getMD5Checksum(mainSfar.toString()).equals(sfarHashes.get(dlcName)))) {
						//It's not the original
						addFailure(dlcName, "DLC has been modified");
						return false;
					}
					Files.copy(mainSfar.toPath(), backupSfar.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					addFailure(dlcName, "I/O Exception occured");
					e.printStackTrace();
					return false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return true;
			}
			if (testpatchSfar.exists()) {
				try {
					//We should hash it and compare it against the known original
					if (!(MD5Checksum.getMD5Checksum(testpatchSfar.toString()).equals(sfarHashes.get(dlcName)))) {
						//It's not the original
						addFailure(dlcName, "DLC has been modified");
						return false;
					}
					Files.copy(testpatchSfar.toPath(), backupTestpatchSfar.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					addFailure(dlcName, "I/O Exception occured");
					e.printStackTrace();
					return false;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}

			return false; //neither sfar could be found.
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

				}
			}

		}

		@Override
		protected void done() {
			showFailedBackups();
			finishBackup(completed);
			if (closeOnComplete) {
				dispose();
			}
			return;
		}
	}

	protected void finishBackup(int completed) {
		ModManager.debugLogger.writeMessage("Finished backing up DLCs.");
		callingWindow.labelStatus.setText(completed + " DLCs backed up.");
		callingWindow.labelStatus.setVisible(true);
		if (backupButton != null) {
			backupButton.setEnabled(true);
			infoLabel.setText("Select DLCs to backup.");
		}
	}

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
			JOptionPane.showMessageDialog(null, header, "DLC backup errors", JOptionPane.ERROR_MESSAGE);
		}
	}
}
