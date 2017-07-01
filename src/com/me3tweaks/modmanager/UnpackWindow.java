package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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

@SuppressWarnings("serial")
public class UnpackWindow extends JDialog {
	JLabel infoLabel;
	// CheckBoxList dlcList;
	String consoleQueue[];
	boolean windowOpen = true;
	HashMap<String, JCheckBox> checkboxMap;
	String currentText;
	String BioGameDir;
	JPanel checkBoxPanel;
	JProgressBar progressBar;
	JButton unpackButton;

	ModManagerWindow callingWindow;

	/**
	 * Manually invoked unpack window
	 * 
	 * @param callingWindow
	 * @param BioGameDir
	 */
	public UnpackWindow(ModManagerWindow callingWindow, String BioGameDir) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		checkboxMap = new HashMap<String, JCheckBox>();

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Unpack DLCs");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//this.setPreferredSize(new Dimension(260, 452));

		setupWindow();

		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("<html>Select DLCs to unpack.<br>Unpacking DLCs can take a really long time.<br>Do not use this if you are going to install ALOT.</html>");
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
				if (mainSfarSize < dlcSizeArray.get(dlcName)){
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
					checkbox.setToolTipText("<html>TESTPATCH cannot be unpacked.<br>To unpack for files only you must do it manually through ME3Explorer.</html>");
				}
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

		unpackButton = new JButton("Unpack selected DLCs");
		unpackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//write to settings
				if (ModManager.isMassEffect3Running()) {
					JOptionPane.showMessageDialog(ModManagerWindow.ACTIVE_WINDOW, "Mass Effect 3 must be closed before you can unpack DLC.","MassEffect3.exe is running", JOptionPane.ERROR_MESSAGE);
					return;
				}
				new UnpackDLCJob(BioGameDir, getJobs(), false).execute();
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
	}

	private String[] getJobs() {
		String[] dlcNames = ModType.getDLCHeaderNameArray();
		ArrayList<String> jobs = new ArrayList<String>();
		for (String dlcName : dlcNames) {
			JCheckBox checkbox = checkboxMap.get(dlcName);
			if (checkbox != null && checkbox.isSelected()) {
				if (ModManager.logging) {
					ModManager.debugLogger.writeMessage("Job added to unpack: " + checkbox.getText() + " at "
							+ ModType.getDLCPath(checkbox.getText()));
				}
				jobs.add(checkbox.getText());
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
				if (mainSfar.exists() && !backupSfar.exists()) {
					_continue = JOptionPane
							.showConfirmDialog(
									UnpackWindow.this,
									dlcName
											+ " does not have an SFAR backup.\nYou will have to use Origin's repair feature if you unpack this DLC and want to restore it.\n\nUnpack this DLC?",
									"DLC not backed up", JOptionPane.YES_NO_OPTION);
				}

				if (_continue == JOptionPane.YES_OPTION) {
					publish("Unpacking " + dlcName);
					processUnpackJob(dlcPath, dlcName);
				}
				completed++;
				publish(Integer.toString(completed));
			}
			return true;

		}

		private boolean processUnpackJob(String fullDLCDirectory, String dlcName) {
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

			if (mainSfar.exists()) {
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.getME3ExplorerEXEDirectory(true) + "ME3Explorer.exe");
				commandBuilder.add("-dlcunpack");
				commandBuilder.add(mainSfar.getAbsolutePath());
				commandBuilder.add(new File(bioGameDir).getParent());

				//Build command.
				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				// Debug stuff
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("Executing unpack command: " + sb.toString());
				Process p = null;
				int returncode = 1;
				try {
					ProcessBuilder pb = new ProcessBuilder(command);
					ModManager.debugLogger.writeMessage("Executing process for DLC Unpack Job.");
					// p = Runtime.getRuntime().exec(command);
					long timeStart = System.currentTimeMillis();
					p = pb.start();
					ModManager.debugLogger.writeMessage("Executed command, waiting...");
					returncode = p.waitFor();
					long timeEnd = System.currentTimeMillis();
					ModManager.debugLogger.writeMessage("Process has finished. Took " + (timeEnd - timeStart) + " ms.");
				} catch (IOException | InterruptedException e) {
					ModManager.debugLogger.writeException(e);
					return false;
				}

				ModManager.debugLogger.writeMessage("Job completed successfully: " + (p != null && returncode == 0));
				return p != null && returncode == 0;
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
		callingWindow.labelStatus.setText(completed + " DLCs unpacked.");
		callingWindow.labelStatus.setVisible(true);
		if (unpackButton != null) {
			unpackButton.setEnabled(true);
			infoLabel.setText("Unpacking completed.");
		}
	}
}
