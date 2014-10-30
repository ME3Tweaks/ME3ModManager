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

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

@SuppressWarnings("serial")
public class BackupWindow extends JDialog {
	JLabel infoLabel;
	// CheckBoxList dlcList;
	String consoleQueue[];
	boolean windowOpen = true, forceAuthentic = true;
	HashMap<String, JCheckBox> checkboxMap;
	ArrayList<String> failedBackups;
	String currentText;
	String BioGameDir;
	JPanel checkBoxPanel;
	JProgressBar progressBar;
	JButton backupButton;
	JCheckBox checkboxForceOriginal;

	ModManagerWindow callingWindow;

	public BackupWindow(ModManagerWindow callingWindow, String BioGameDir) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		checkboxMap = new HashMap<String, JCheckBox>();
		failedBackups = new ArrayList<String>();

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Backup DLCs");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(260, 363));
		this.setResizable(false);

		setupWindow();

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		//Set the backup flag to true
		Wini ini;
		try {
			File settings = new File(ModManager.settingsFilename);
			if (!settings.exists())
				settings.createNewFile();
			ini = new Wini(settings);
			ini.put("Settings", "dlc_backup_flag", "1");
			ini.store();
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
		}
		
		
		
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("Select DLCs to backup.");
		northPanel.add(infoLabel, BorderLayout.NORTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setEnabled(false);
		// progressBar.setPreferredSize(new Dimension(0, 28));
		northPanel.add(progressBar, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);

		checkBoxPanel = new JPanel(new BorderLayout());
		TitledBorder DLCBorder= BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Installed DLCs");
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
			String filepath = ModManagerWindow.appendSlash(BioGameDir) + ModManagerWindow.appendSlash(ModType.getDLCPath(dlcName));
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
				ModManager.debugLogger.writeMessage(dlcName+" was not found.");
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
				new backupDLCJob(BioGameDir, getJobs()).execute();
			}

		});

		JPanel backupPanel = new JPanel(new BorderLayout());
		checkboxForceOriginal = new JCheckBox("Only backup if original");
		checkboxForceOriginal.setSelected(true);
		backupPanel.add(checkboxForceOriginal, BorderLayout.NORTH);
		backupPanel.add(backupButton, BorderLayout.CENTER);

		rootPanel.add(backupPanel, BorderLayout.SOUTH);
		rootPanel.setBorder(new EmptyBorder(5,5,5,5));
		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// methods will read this variable
			}
		});
	}

	private String[] getJobs() {
		String[] dlcNames = ModType.getHeaderNameArray();
		ArrayList<String> jobs = new ArrayList<String>();
		for (String dlcName : dlcNames){
			JCheckBox checkbox = checkboxMap.get(dlcName);
			if (checkbox!=null && checkbox.isSelected()){
				if (ModManager.logging){
					ModManager.debugLogger.writeMessage("Job added to backup: "+checkbox.getText()+" at "+ModType.getDLCPath(checkbox.getText()));
				}
				jobs.add(checkbox.getText());
			}
		}
		
		//Setup authenticity flag (since this method is only claled here)
		forceAuthentic = checkboxForceOriginal.isSelected();
		
		return jobs.toArray(new String[jobs.size()]);
	}

	class backupDLCJob extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numjobs = 0;
		String bioGameDir;
		String[] jobs;

		protected backupDLCJob(String bioGameDir, String[] jobs) {
			backupButton.setEnabled(false);

			this.jobs = jobs;
			this.bioGameDir = bioGameDir;
			numjobs = jobs.length;
			infoLabel.setText("Backing up DLC...");
			ModManager.debugLogger.writeMessage("Starting the backupDLCJob utility. Number of jobs to do: " + numjobs);
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the restore thread");
			HashMap<String,String> sfarHashes = ModType.getHashesMap();
			for (String dlcName : jobs) {
				if (windowOpen == true) {// if the window is closed this will quickly finish this thread after the current job finishes
					ModManager.debugLogger.writeMessage("Processing backup job");
					if (processBackupJob(ModManagerWindow.appendSlash(bioGameDir) + ModManagerWindow.appendSlash(ModType.getDLCPath(dlcName)),dlcName,sfarHashes)){
						completed++;
					}
					publish(Integer.toString(completed));
				}
			}

			return true;

		}

		private boolean processBackupJob(String fullDLCDirectory, String dlcName, HashMap<String,String> sfarHashes) {
			// TODO Auto-generated method stub
			File dlcPath = new File(fullDLCDirectory);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				if (ModManager.logging){
					ModManager.debugLogger.writeMessage(fullDLCDirectory + " does not exist. It might not be installed (this should have been caught!");
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
					if (forceAuthentic){
						//We should hash it and compare it against the known original
						if (!(MD5Checksum.getMD5Checksum(mainSfar.toString()).equals(sfarHashes.get(dlcName)))){
							//It's not the original
							addFailure(dlcName, "DLC hash does not match known original");
							return false;
						}
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
			if (testpatchSfar.exists()){
				try {
					if (forceAuthentic){
						//We should hash it and compare it against the known original
						if (!(MD5Checksum.getMD5Checksum(testpatchSfar.toString()).equals(sfarHashes.get(dlcName)))){
							//It's not the original
							addFailure(dlcName, "DLC hash does not match known original");
							return false;
						}
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
			return;
		}
	}

	protected void finishBackup(int completed) {
		if (ModManager.logging){
			ModManager.debugLogger.writeMessage("Finished backing up DLCs.");
		}
		callingWindow.labelStatus.setText(" " + completed + " DLCs backed up.");
		callingWindow.labelStatus.setVisible(true);
		backupButton.setEnabled(true);
		infoLabel.setText("Select DLCs to backup.");
	}

	public void addFailure(String dlcName, String reason) {
		failedBackups.add(dlcName+": "+reason);
	}
	
	/**
	 * Shows a JDialog if a backup job failed. If there are no failures, it won't show anything.
	 */
	public void showFailedBackups(){
		if (failedBackups.size()>0){
			String header = "The following DLCs failed to backup:\n\n";
			for(String failed : failedBackups){
				header+=failed+"\n";
			}
			JOptionPane
			.showMessageDialog(null,header,"DLC backup errors", JOptionPane.ERROR_MESSAGE);
		}
	}
}
