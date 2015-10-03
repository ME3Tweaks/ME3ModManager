package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.TocBatchDescriptor;

@SuppressWarnings("serial")
public class AutoTocWindow extends JDialog {
	JLabel infoLabel;
	JProgressBar progressBar;
	//Mod mod;
	JCheckBox loggingMode;
	int maxBatchSize = 10;

	public static final int LOCALMOD_MODE = 0;
	public static final int UPGRADE_UNPACKED_MODE = 1;

	/**
	 * Makes a new AutoTOC window and starts the autotoc.
	 * 
	 * @param mod
	 *            Mod to toc.
	 * @param modmaker
	 *            flag to use if this is a modmaker or user initiated TOC
	 *            update.
	 */
	public AutoTocWindow(Mod mod, int mode) {
		//this.mod = mod;
		this.setTitle("AutoTOC");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//this.setPreferredSize(new Dimension(380, 138));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow(mode == LOCALMOD_MODE ? "Updating PCConsoleTOC files for " + mod.getModName() : "Upgrading " + mod.getModName()
				+ " for use with unpacked DLC files");
		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		switch (mode) {
		case LOCALMOD_MODE:
			new TOCWorker(mod).execute();
			break;
		case UPGRADE_UNPACKED_MODE:
			//new UnpackedUpgradeTOCWorker(mod).execute();
			break;
		}
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

	class TOCWorker extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numtoc = 0;
		String me3explorer;
		Mod mod;
		ArrayList<String> failedTOC;

		protected TOCWorker(Mod mod) {
			this.mod = mod;
			for (ModJob job : mod.jobs) {
				if (job.getJobType() == ModJob.CUSTOMDLC) {
					//don't autotoc custom DLC
					continue;
				}
				boolean hasTOC = false;
				//find out if it has a toc file
				for (String file : job.newFiles) {
					String filename = FilenameUtils.getName(file);
					if (filename.equals("PCConsoleTOC.bin")) {
						hasTOC = true;
						break;
					}
				}

				if (hasTOC) { //calc files
					for (String file : job.newFiles) {
						String filename = FilenameUtils.getName(file);
						if (filename.equals("PCConsoleTOC.bin")) {
							continue;
						} else {
							hasTOC = true;
							numtoc++;
						}
					}
				}
			}
			failedTOC = new ArrayList<String>();
			progressBar.setValue(0);
			me3explorer = ModManager.appendSlash(ModManager.getME3ExplorerEXEDirectory(true)) + "ME3Explorer.exe";
			ModManager.debugLogger.writeMessage("Starting the AutoTOC utility. Number of toc updates to do: " + numtoc);
			ModManager.debugLogger.writeMessage("Using ME3Explorer from: " + me3explorer);
		}

		@Override
		public Boolean doInBackground() {
			//get list of all files to update for the progress bar
			for (ModJob job : mod.jobs) {
				if (job.getJobType() == ModJob.CUSTOMDLC) {
					continue;
				}
				boolean hasTOC = false;
				for (String file : job.newFiles) {
					String filename = FilenameUtils.getName(file);
					if (filename.equals("PCConsoleTOC.bin")) {
						hasTOC = true;
						break;
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
							continue; //this doens't need updated.
						}
						modulePath = FilenameUtils.getFullPath(newFile);
						tbd.addNameSizePair(filename, (new File(newFile)).length());
						numJobsInCurrentBatch++;
						if (numJobsInCurrentBatch > maxBatchSize){
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

						Process p = null;
						int returncode = 1;
						try {
							ProcessBuilder pb = new ProcessBuilder(command);
							p = pb.start();
							returncode = p.waitFor();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (returncode != 0) {
							ModManager.debugLogger.writeError("ME3Explorer returned a non 0 code: " + returncode);
							//failedTOC.add(filepath);
						} else {
							completed+=batchJob.getNameSizePairs().size();
							publish(Integer.toString(completed));
						}
						
					}
					/*
					
					//old way
					for (String newFile : job.newFiles) {
						int num = 0;
						String filename = FilenameUtils.getName(newFile);
						if (filename.equals("PCConsoleTOC.bin")) {
							continue; //this doens't need updated.
						}
						String modulePath = FilenameUtils.getFullPath(newFile); //inside mod, folders like PATCH2 or MP4. Already has a / on the end.
						ArrayList<String> commandBuilder = new ArrayList<String>();
						// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
						commandBuilder.add(me3explorer);
						commandBuilder.add("-toceditorupdate");
						commandBuilder.add(modulePath + "PCConsoleTOC.bin");
						commandBuilder.add(filename); //internal filename (if in DLC)
						commandBuilder.add(Long.toString((new File(newFile)).length()));

						String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
						//Debug stuff
*/						
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
			if (numtoc != completed) {
				//failed something
				StringBuilder sb = new StringBuilder();
				sb.append("Failed to TOC at least one of the files in this mod.");
				for (ModJob job : mod.jobs) {
					if (job.getJobType() == ModJob.CUSTOMDLC) {
						JOptionPane.showMessageDialog(null, "This mod includes custom DLC content. Custom DLC content must be manually TOCed.",
								"AutoTOC Info", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				if (ModManagerWindow.ACTIVE_WINDOW != null) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Failed to TOC at least 1 file in mod");
				}

			} else {
				for (ModJob job : mod.jobs) {
					if (job.getJobType() == ModJob.CUSTOMDLC) {
						JOptionPane.showMessageDialog(null, "This mod includes custom DLC content. Custom DLC content must be manually TOCed.",
								"AutoTOC Info", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				//we're good
				if (ModManagerWindow.ACTIVE_WINDOW != null) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(mod.getModName() + " TOC files updated");
				}
				dispose();
			}
			return;
		}
	}

	/*class UnpackedUpgradeTOCWorker extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numtoc = 0;
		String me3explorer;
		ArrayList<String> failedTOC;
		Mod mod;

		protected UnpackedUpgradeTOCWorker(Mod mod) {
			this.mod = mod;
			String biogameDir = ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText();
			String me3dir = new File(biogameDir).getParent();

			//get number of TOCs to do
			for (ModJob job : mod.jobs) {
				String relativeUnpackedDirectory = ModType.getDLCPath(job.getJobName());
				String unpackedDirectory = ModManager.appendSlash(me3dir) + relativeUnpackedDirectory;
				File folder = new File(me3dir + "/BIOGame" + unpackedDirectory);
				File[] listOfFiles = folder.listFiles();
				if (listOfFiles != null) {
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile() && !listOfFiles[i].getName().endsWith(".sfar") && !listOfFiles[i].getName().endsWith(".bak")) {
							numtoc++;
						}
					}
				}
			}

			failedTOC = new ArrayList<String>();
			progressBar.setValue(0);
			me3explorer = ModManager.appendSlash(ModManager.getME3ExplorerEXEDirectory(true)) + "ME3Explorer.exe";
			ModManager.debugLogger.writeMessage("Starting the AutoTOC utility in upgrade to unpacked DLC mode. Number of toc updates to do: "
					+ numtoc);
			ModManager.debugLogger.writeMessage("Using ME3Explorer from: " + me3explorer);
		}

		@Override
		public Boolean doInBackground() {
			if (true)
				return true;
			//get list of all files to update for the progress bar
			for (ModJob job : mod.jobs) {
				if (job.getJobType() == ModJob.CUSTOMDLC) {
					continue;
				}
				boolean hasTOC = false;
				for (String file : job.newFiles) {
					String filename = FilenameUtils.getName(file);
					if (filename.equals("PCConsoleTOC.bin")) {
						hasTOC = true;
						break;
					}
				}

				if (hasTOC) { //toc this job
					//get path to PCConsoleTOC
					for (String newFile : job.newFiles) {

						String filename = FilenameUtils.getName(newFile);
						if (filename.equals("PCConsoleTOC.bin")) {
							continue; //this doens't need updated.
						}
						String modulePath = FilenameUtils.getFullPath(newFile); //inside mod, folders like PATCH2 or MP4. Already has a / on the end.
						ArrayList<String> commandBuilder = new ArrayList<String>();
						// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
						commandBuilder.add(me3explorer);
						commandBuilder.add("-toceditorupdate");
						commandBuilder.add(modulePath + "PCConsoleTOC.bin");
						commandBuilder.add(filename); //internal filename (if in DLC)
						commandBuilder.add(Long.toString((new File(newFile)).length()));

						String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
						//Debug stuff
						StringBuilder sb = new StringBuilder();
						for (String arg : command) {
							sb.append(arg + " ");
						}

						Process p = null;
						int returncode = 1;
						ModManager.debugLogger.writeMessage("Executing process for TOC Update: " + sb.toString());
						try {
							ProcessBuilder pb = new ProcessBuilder(command);
							p = pb.start();
							returncode = p.waitFor();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (returncode != 0) {
							System.out.println("SOMETHINGS WRONG.");
							//failedTOC.add(filepath);
						} else {
							completed++;
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
			if (true)
				return;

			if (numtoc != completed) {
				//failed something
				StringBuilder sb = new StringBuilder();
				sb.append("Failed to TOC at least one of the files in this mod.");
				for (ModJob job : mod.jobs) {
					if (job.getJobType() == ModJob.CUSTOMDLC) {
						JOptionPane.showMessageDialog(null, "This mod includes custom DLC content. Custom DLC content must be manually TOCed.",
								"AutoTOC Info", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				if (ModManagerWindow.ACTIVE_WINDOW != null) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Failed to TOC at least 1 file in mod");
				}

			} else {
				for (ModJob job : mod.jobs) {
					if (job.getJobType() == ModJob.CUSTOMDLC) {
						JOptionPane.showMessageDialog(null, "This mod includes custom DLC content. Custom DLC content must be manually TOCed.",
								"AutoTOC Info", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				//we're good
				if (ModManagerWindow.ACTIVE_WINDOW != null) {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(mod.getModName() + " TOC files updated");
				}
				dispose();
			}
			return;
		}
	}*/
}
