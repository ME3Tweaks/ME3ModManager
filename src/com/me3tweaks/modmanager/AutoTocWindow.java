package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.objects.TocBatchDescriptor;

@SuppressWarnings("serial")
public class AutoTocWindow extends JDialog {
	JLabel infoLabel;
	JProgressBar progressBar;
	//Mod mod;
	JCheckBox loggingMode;
	int maxBatchSize = 10;
	public int mode = LOCALMOD_MODE;
	private HashMap<String, String> updatedGameTOCs;
	public static final int LOCALMOD_MODE = 0;
	public static final int UPGRADE_UNPACKED_MODE = 1;
	public static final int INSTALLED_MODE = 1;

	/**
	 * Makes a new AutoTOC window and starts the autotoc.
	 * 
	 * @param mod
	 *            Mod to toc.
	 * @param modmaker
	 *            flag to use if this is a modmaker or user initiated TOC
	 *            update.
	 */
	/*
	 * public AutoTocWindow(Mod mod, int mode) { //mod is unused for now.
	 * //this.mod = mod; this.setTitle("AutoTOC");
	 * this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	 * //this.setPreferredSize(new Dimension(380, 138));
	 * this.setResizable(false);
	 * this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	 * setupWindow(mode == LOCALMOD_MODE ? "Updating PCConsoleTOC files for " +
	 * mod.getModName() : "Upgrading " + mod.getModName() +
	 * " for use with unpacked DLC files");
	 * this.setIconImages(ModManager.ICONS); this.pack();
	 * this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW); this.mode =
	 * LOCALMOD_MODE; new TOCWorker(mod).execute(); this.setVisible(true); }
	 */

	/**
	 * Automatically updates PCConsoleTOC files
	 * 
	 * @param mod
	 *            Mod to use for sizes and possible TOc files
	 * @param mode
	 *            mode to use. Local mode uses mod's Toc files. Installed mode
	 *            uses game toc files.
	 * @param biogameDir
	 *            Directory of biogame. Can be null.
	 */
	public AutoTocWindow(Mod mod, int mode, String biogameDir) {
		this.mode = mode;
		updatedGameTOCs = new HashMap<String, String>();
		ModManager.debugLogger.writeMessage("===Starting AutoTOC. Mode: " + mode + "===");
		this.setTitle("Mod Manager AutoTOC");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		switch (mode) {
		case LOCALMOD_MODE:
			setupWindow("Updating " + mod.getModName() + "'s PCConsoleTOC files");
			break;
		case INSTALLED_MODE:
			setupWindow("Updating installed PCConsoleTOCs for " + mod.getModName());
			break;
		default:
			ModManager.debugLogger.writeError("Unknown AutoTOC mode: " + mode);
			JOptionPane.showMessageDialog(null, "Unknown AutoTOC mode: " + mode, "AutoTOC Error", JOptionPane.ERROR_MESSAGE);
			dispose();
			return;
		}
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		new TOCWorker(mod, biogameDir).execute();
		this.setVisible(true);
	}

	private void setupWindow(String labelText) {
		JPanel aboutPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("<html>" + labelText + "</html>");
		aboutPanel.add(infoLabel, BorderLayout.NORTH);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		aboutPanel.add(progressBar, BorderLayout.CENTER);
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(aboutPanel);
	}

	/**
	 * Returns the hashmap of updated TOC files (JOB HEADER NAME => PATH OF TOC)
	 * 
	 * @return null if autotoc did not run in gametoc mode, otherwise list of
	 *         updated TOC files copied from the game and updated
	 */
	public HashMap<String, String> getUpdatedGameTOCs() {
		return updatedGameTOCs;
	}

	class TOCWorker extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numtoc = 0;
		String me3explorer;
		Mod mod;
		ArrayList<String> failedTOC;

		/**
		 * Localmod mode constructor
		 * 
		 * @param mod
		 */
		protected TOCWorker(Mod mod, String biogameDir) {
			this.mod = mod;
			calculateNumberOfUpdates(mod.jobs);
			failedTOC = new ArrayList<String>();
			progressBar.setValue(0);
			me3explorer = ModManager.appendSlash(ModManager.getME3ExplorerEXEDirectory(true)) + "ME3Explorer.exe";
			ModManager.debugLogger.writeMessage("Starting the AutoTOC worker. Number of toc updates to do: " + numtoc);
			ModManager.debugLogger.writeMessage("Using ME3Explorer from: " + me3explorer);
		}

		private void calculateNumberOfUpdates(ArrayList<ModJob> jobs) {
			for (ModJob job : jobs) {
				if (job.getJobType() == ModJob.CUSTOMDLC) {
					numtoc += job.getSourceFolders().size();
					continue;
				}
				boolean hasTOC = false;
				if (mode == LOCALMOD_MODE) {
					//find out if it has a toc file
					for (String file : job.newFiles) {
						String filename = FilenameUtils.getName(file);
						if (filename.equals("PCConsoleTOC.bin")) {
							hasTOC = true;
							break;
						}
					}
				} else {
					hasTOC = true; //force game toc
				}

				if (hasTOC) { //calc files
					for (String file : job.newFiles) {
						String filename = FilenameUtils.getName(file);
						if (filename.equalsIgnoreCase("PCConsoleTOC.bin") || filename.equalsIgnoreCase("Mount.dlc")) {
							continue;
						} else {
							//increment number of files to update
							numtoc++;
						}
					}
				}
			}
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("AutoTOC background thread has started.");

			//get list of all files to update for the progress bar
			for (ModJob job : mod.jobs) {
				if (job.getJobType() == ModJob.CUSTOMDLC) {
					for (String srcFolder : job.getSourceFolders()) {
						ArrayList<String> commandBuilder = new ArrayList<String>();
						// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
						commandBuilder.add(me3explorer);
						commandBuilder.add("-autotoc");
						commandBuilder.add(mod.getModPath()+srcFolder);

						String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);

						//for logging
						StringBuilder sb = new StringBuilder();
						for (String arg : command) {
							sb.append(arg + " ");
						}
						ModManager.debugLogger.writeMessage("Performing a CUSTOM DLC TOC update with command: " + sb.toString());
						int returncode = 1;
						ProcessBuilder pb = new ProcessBuilder(command);
						ProcessResult pr = ModManager.runProcess(pb);
						returncode = pr.getReturnCode();
						if (returncode != 0 || pr.hadError()) {
							ModManager.debugLogger.writeError("ME3Explorer returned a non 0 code (or threw error): " + returncode);
						} else {
							completed++;
							ModManager.debugLogger.writeMessage("Number of completed tasks: " + completed + ", num left to do: " + (numtoc - completed));
							publish(Integer.toString(completed));
						}
					}
					continue;
				}
				ModManager.debugLogger.writeMessage("======AutoTOC job on module " + job.getJobName() + "=======");
				boolean hasTOC = false;
				if (mode == LOCALMOD_MODE) {
					//see if has toc file
					for (String file : job.newFiles) {
						String filename = FilenameUtils.getName(file);
						if (filename.equals("PCConsoleTOC.bin")) {
							hasTOC = true;
							break;
						}
					}
				} else {
					//get TOC file.
					String tocPath = ModManager.getGameFile(ModType.getTOCPathFromHeader(job.getJobName()), job.getJobName(),
							ModManager.getTempDir() + job.getJobName() + "_PCConsoleTOC.bin");
					if (tocPath != null) {
						updatedGameTOCs.put(job.getJobName(), tocPath);
						hasTOC = true;
					} else {
						ModManager.debugLogger.writeError("Unable to get module's PCConsoleTOC file: " + job.getJobName() + ". This update will be skipped.");
					}
				}

				if (hasTOC) { //toc this job
					//batches
					ArrayList<TocBatchDescriptor> batchJobs = new ArrayList<TocBatchDescriptor>();
					int numJobsInCurrentBatch = 0;
					String modulePath = null;
					boolean moreThan1batch = false;
					//add first job
					TocBatchDescriptor tbd = new TocBatchDescriptor();
					batchJobs.add(tbd);

					//break into batches
					for (String newFile : job.newFiles) {
						String filename = FilenameUtils.getName(newFile);
						if (filename.equals("PCConsoleTOC.bin")) {
							continue; //this doesn't need updated.
						}
						modulePath = FilenameUtils.getFullPath(newFile);
						tbd.addNameSizePair(filename, (new File(newFile)).length());
						numJobsInCurrentBatch++;
						if (numJobsInCurrentBatch > maxBatchSize) {
							batchJobs.add(tbd);
							tbd = new TocBatchDescriptor();
							numJobsInCurrentBatch = 0;
							moreThan1batch = true;
						}
					}

					//break into batches
					for (String addFile : job.addFiles) {
						String filename = FilenameUtils.getName(addFile);
						if (filename.equals("PCConsoleTOC.bin")) {
							continue; //this doens't need updated.
						}
						modulePath = FilenameUtils.getFullPath(addFile);
						tbd.addNameSizePair(filename, (new File(addFile)).length());
						numJobsInCurrentBatch++;
						if (numJobsInCurrentBatch > maxBatchSize) {
							batchJobs.add(tbd);
							tbd = new TocBatchDescriptor();
							numJobsInCurrentBatch = 0;
							moreThan1batch = true;
						}
					}

					if (moreThan1batch && numJobsInCurrentBatch > 0) {
						batchJobs.add(tbd); //enter last batch task
					}

					//TOC to update
					String tocPath = modulePath + "PCConsoleTOC.bin";
					if (updatedGameTOCs.containsKey(job.getJobName())) {
						tocPath = updatedGameTOCs.get(job.getJobName());
						ModManager.debugLogger.writeMessage("TOCing Alternative File: " + tocPath);
					}
					//feed jobs into me3explorer for processing
					for (TocBatchDescriptor batchJob : batchJobs) {
						ArrayList<String> commandBuilder = new ArrayList<String>();
						// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
						commandBuilder.add(me3explorer);
						commandBuilder.add("-toceditorupdate");
						commandBuilder.add(tocPath);
						for (AbstractMap.SimpleEntry<String, Long> tocEntryMap : batchJob.getNameSizePairs()) {
							commandBuilder.add(tocEntryMap.getKey()); //internal filename (if in DLC)
							commandBuilder.add(Long.toString(tocEntryMap.getValue()));
						}
						String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);

						//for logging
						StringBuilder sb = new StringBuilder();
						for (String arg : command) {
							sb.append(arg + " ");
						}
						ModManager.debugLogger.writeMessage("Performing a batch TOC update with command: " + sb.toString());
						int returncode = 1;
						ProcessBuilder pb = new ProcessBuilder(command);
						ProcessResult pr = ModManager.runProcess(pb);
						returncode = pr.getReturnCode();
						if (returncode != 0 || pr.hadError()) {
							ModManager.debugLogger.writeError("ME3Explorer returned a non 0 code (or threw error): " + returncode);
						} else {
							completed += batchJob.getNameSizePairs().size();
							ModManager.debugLogger.writeMessage("Number of completed tasks: " + completed + ", num left to do: " + (numtoc - completed));
							publish(Integer.toString(completed));
						}
					}
				}
			}
			return true;
		}

		@Override
		protected void process(List<String> updates) {
			//System.out.println("Restoring next DLC");
			for (String update : updates) {
				try {
					Integer.parseInt(update); // see if we got a number. if we did that means we should update the bar
					if (numtoc != 0) {
						progressBar.setValue((int) (((float) completed / numtoc) * 100));
					}
				} catch (NumberFormatException e) {
					// this is not a progress update, it's a string update
					//addToQueue(update);
				}
			}

		}

		@Override
		protected void done() {
			try {
				get();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("AutoTOC prematurely ended:", e);
			}

			if (numtoc != completed) {
				ModManager.debugLogger.writeError("AutoToc DONE: Number of tasks DOES NOT EQUAL number of completed: " + numtoc + " total tasks, " + completed + " completed");
				if (ModManagerWindow.ACTIVE_WINDOW != null) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("AutoTOC had an error (check logs)");
				}
				dispose();

			} else {
				//we're good
				if (ModManagerWindow.ACTIVE_WINDOW != null && mode == LOCALMOD_MODE) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(mod.getModName() + " TOC files updated");
				}
				dispose();
			}
			return;
		}
	}
}
