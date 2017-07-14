package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class UnpackWindow extends JDialog {
	JLabel infoLabel;
	// CheckBoxList dlcList;
	String consoleQueue[];
	boolean windowOpen = true;
	HashMap<String, JCheckBox> checkboxMap;
	String currentText;
	JPanel checkBoxPanel;
	JProgressBar progressBar;
	JButton unpackButton;

	/**
	 * Manually invoked unpack window
	 * 
	 * @param callingWindow
	 * @param BioGameDir
	 */
	public UnpackWindow(ModManagerWindow callingWindow) {
		// callingWindow.setEnabled(false);
		checkboxMap = new HashMap<String, JCheckBox>();
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		File bg = new File(ModManagerWindow.GetBioGameDir());
		long freespace = bg.getFreeSpace();
		String freespaceStr = ResourceUtils.humanReadableByteCount(freespace, true);
		setTitle("Unpack DLCs");
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImages(ModManager.ICONS);

		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("<html>Select DLCs to unpack. Items in blue are not unpacked.<br>Unpacking all DLC requires about 30GB - you have "+freespaceStr+" free.<br></html>");
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
		String[] headerArray = ModType.getDLCHeaderNameArray();
		HashMap<String, Long> dlcSizeArray = ModType.getSizesMap();
		int i = 0;
		// Add and enable/disable DLC checkboxes and add to hashmap
		for (String dlcName : headerArray) {
			JCheckBox checkbox = new JCheckBox(dlcName);
			// checkBoxPanel.add(checkbox);
			String filepath = ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + ModManager.appendSlash(ModType.getDLCPath(dlcName));
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
				checkbox.setToolTipText("DLC not installed");
				checkboxMap.put(dlcName, checkbox);
				ModManager.debugLogger.writeMessage("DLC does not appear installed: " + dlcName);
				continue;
			}

			// The folder exists.
			File mainSfar = new File(dlcPath + "\\Default.sfar");
			ModManager.debugLogger.writeMessage("Looking for Default.sfar in " + filepath);
			if (mainSfar.exists()) {
				ModManager.debugLogger.writeMessage("Found a .sfar");
				long mainSfarSize = mainSfar.length();
				if (mainSfarSize < dlcSizeArray.get(dlcName)) {
					checkbox.setForeground(Color.BLACK);
					checkbox.setToolTipText("This DLC appears to already be unpacked (filesize is smaller than original).");
				} else {
					checkbox.setForeground(Color.BLUE);
					checkbox.setToolTipText("This DLC is not unpacked.");
				}

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
				if (dlcName.equals(ModType.TESTPATCH)) {
					//Check existence
					File patch001Sfar = new File(dlcPath + "\\Patch_001.sfar");
					if (patch001Sfar.exists()) {
						checkbox.setText(checkbox.getText() + "*");
						checkbox.setToolTipText(
								"<html>TESTPATCH cannot be unpacked.<br>Checking this box means it will do a read-only unpacking and place the files in the data/Patch_001_Extracted directory.</html>");
					} else {
						checkbox.setEnabled(false);
						checkbox.setToolTipText("<html>Patch_001 (TESTPATCH) does not appear to be installed</html>");
					}
				} else {
					checkbox.setEnabled(false);
					checkbox.setToolTipText("<html>This DLC does not appear to be installed</html>");
				}
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

		unpackButton = new JButton("Unpack selected DLCs");
		unpackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//write to settings
				if (ModManager.isMassEffect3Running()) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can unpack DLC.", "MassEffect3.exe is running",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				new UnpackDLCJob(ModManagerWindow.GetBioGameDir(), getJobs(), false).execute();
			}
		});

		JPanel unpackPanel = new JPanel(new BorderLayout());
		unpackPanel.add(unpackButton, BorderLayout.CENTER);

		rootPanel.add(unpackPanel, BorderLayout.SOUTH);
		rootPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(rootPanel);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windowOpen = false;
				// methods will read this variable
			}
		});

		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
	}

	private String[] getJobs() {
		String[] dlcNames = ModType.getDLCHeaderNameArray();
		ArrayList<String> jobs = new ArrayList<String>();
		for (String dlcName : dlcNames) {
			JCheckBox checkbox = checkboxMap.get(dlcName);
			if (checkbox != null && checkbox.isSelected()) {
				String jobname = checkbox.getText();
				if (jobname.endsWith("*")) {
					jobname = jobname.substring(0, jobname.length() - 1);
				}
				ModManager.debugLogger.writeMessage("Job added to unpack: " + jobname + " at " + ModType.getDLCPath(jobname));
				jobs.add(jobname);
			}
		}

		return jobs.toArray(new String[jobs.size()]);
	}

	class UnpackDLCJob extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numjobs = 0;
		String bioGameDir;
		String[] jobs;
		boolean closeOnComplete;
		ArrayList<String> failedUnpacks;
		boolean hasTestPatch = false;

		protected UnpackDLCJob(String bioGameDir, String[] jobs, boolean closeOnComplete) {
			if (unpackButton != null) {
				unpackButton.setEnabled(false);
				infoLabel.setText("Unpacking DLC...");
			}
			this.closeOnComplete = closeOnComplete;
			this.jobs = jobs;
			this.bioGameDir = bioGameDir;
			numjobs = jobs.length;
			failedUnpacks = new ArrayList<String>();

			ModManager.debugLogger.writeMessage("Starting the unpack DLC thread. Number of jobs to do: " + numjobs);
			progressBar.setIndeterminate(true);
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the unpack thread");
			for (String dlcName : jobs) {
				String dlcPath = ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(ModType.getDLCPath(dlcName));
				//check for backup
				File mainSfar = new File(dlcPath + "Default.sfar");

				File backupSfar = new File(dlcPath + "Default.sfar.bak");
				int _continue = JOptionPane.YES_OPTION;
				if (mainSfar.exists()) {
					//Primary DLC
					if (mainSfar.exists() && !backupSfar.exists()) {
						_continue = JOptionPane.showConfirmDialog(UnpackWindow.this, dlcName
								+ " does not have an SFAR backup.\nYou will have to use Origin's repair feature if you unpack this DLC and want to restore it.\n\nUnpack this DLC?",
								"DLC not backed up", JOptionPane.YES_NO_OPTION); //should probably move this to the UI thread...
					}

					if (_continue == JOptionPane.YES_OPTION) {
						publish("Unpacking " + dlcName);
						processUnpackJob(dlcPath, dlcName, false);
					}
					completed++;
					publish(Integer.toString(completed));
					continue;
				}
				File patch001Sfar = new File(dlcPath + "Patch_001.sfar");
				if (patch001Sfar.exists()) {
					hasTestPatch = true;
					//TESTPATCH - read only extraction
					publish("Unpacking " + dlcName);
					processUnpackJob(dlcPath, dlcName, true);
					completed++;
					publish(Integer.toString(completed));
					continue;
				}
			}
			return true;

		}

		private boolean processUnpackJob(String fullDLCDirectory, String dlcName, boolean testpatch) {
			File dlcPath = new File(fullDLCDirectory);
			// Check if directory exists
			if (!dlcPath.exists()) {
				// Maybe DLC is not installed?
				ModManager.debugLogger.writeError(fullDLCDirectory + " does not exist. It might not be installed (this should have been caught!");
				return false;
			}

			// The folder exists.
			File mainSfar = new File(fullDLCDirectory + "Default.sfar");
			if (testpatch) {
				mainSfar = new File(fullDLCDirectory + "Patch_001.sfar");
			}

			if (mainSfar.exists()) {
				ArrayList<String> command = new ArrayList<String>();
				command.add(ModManager.getCommandLineToolsDir() + "SFARTools-Extract.exe");
				command.add("--SFARPath");
				command.add(mainSfar.getAbsolutePath());
				command.add("--ExtractEntireArchive");
				if (testpatch) {
					command.add("--KeepArchiveIntact");
				}
				command.add("--OutputPath");
				if (testpatch) {
					command.add(ModManager.getTestpatchUnpackFolder());
				} else {
					command.add(new File(bioGameDir).getParent());
				}
				//Build command.
				ProcessBuilder pb = new ProcessBuilder(command);
				ProcessResult pr = ModManager.runProcess(pb);
				ModManager.debugLogger.writeMessage("Job completed successfully: " + (pr.getReturnCode() == 0));
				return pr.getReturnCode() == 0;
			}
			return false; //sfar could not be found.
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
					infoLabel.setText(update);
				}
			}

		}

		@Override
		protected void done() {
			progressBar.setIndeterminate(false);
			showFailedUnpacks();
			finishUnpack(completed);
			if (hasTestPatch) {
				//Open Explorer
				ResourceUtils.openFolderInExplorer(ModManager.getTestpatchUnpackFolder());
			}
			if (closeOnComplete) {
				dispose();
			}
		}

		public void addFailure(String dlcName, String reason) {
			failedUnpacks.add(dlcName + ": " + reason);
		}

		/**
		 * Shows a JDialog if a backup job failed. If there are no failures, it
		 * won't show anything.
		 */
		public void showFailedUnpacks() {
			if (failedUnpacks.size() > 0) {
				String header = "The following DLCs failed to unpack:\n\n";
				for (String failed : failedUnpacks) {
					header += failed + "\n";
				}
				JOptionPane.showMessageDialog(null, header, "DLC unpack errors", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected void finishUnpack(int completed) {
		ModManager.debugLogger.writeMessage("Finished unpacking DLCs.");
		ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(completed + " DLCs unpacked.");
		if (unpackButton != null) {
			unpackButton.setEnabled(true);
			infoLabel.setText("Unpacking completed.");
		}
	}
}
