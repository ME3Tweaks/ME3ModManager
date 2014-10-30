package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class AutoTocWindow extends JDialog {
	JLabel infoLabel;
	JProgressBar progressBar;
	ModManagerWindow callingWindow;
	Mod mod;
	JCheckBox loggingMode;

	/**
	 * Makes a new AutoTOC window and starts the autotoc.
	 * @param callingWindow The ModManagerWindow (used for updating the status text.)
	 * @param mod Mod to toc.
	 * @param modmaker flag to use if this is a modmaker or user initiated TOC update.
	 */
	public AutoTocWindow(ModManagerWindow callingWindow, Mod mod) {
		this.mod = mod;
		this.callingWindow = callingWindow;
		this.setTitle("AutoTOC");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(380, 138));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setupWindow();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		performTOC();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel aboutPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel(
				"<html>Updating PCConsoleTOC on "+mod.modName+"</html>");
		aboutPanel.add(infoLabel, BorderLayout.NORTH);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		aboutPanel.add(progressBar, BorderLayout.CENTER);
		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		
		
		this.getContentPane().add(aboutPanel);
	}
	
	private void performTOC(){
		new TOCWorker(mod).execute();
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
				if (job.modType == ModJob.BASEGAME) {
					continue; //skip basegame toc
				}
				for (String file : job.newFiles) {
					String filename = FilenameUtils.getName(file);
					if (filename.equals("PCConsoleTOC.bin")) {
						continue;
					} else {
						numtoc++;
					}
				}
			}
			failedTOC = new ArrayList<String>();
			progressBar.setValue(0);
			me3explorer = ModManagerWindow.appendSlash(ModManager.getME3ExplorerEXEDirectory())+"ME3Explorer.exe";
			ModManager.debugLogger.writeMessage("Starting the AutoTOC utility. Number of toc updates to do: "+numtoc);
			ModManager.debugLogger.writeMessage("Using ME3Explorer from: "+me3explorer);
		}

		@Override
		public Boolean doInBackground() {
			//get list of all files to update for the progress bar
			for (ModJob job : mod.jobs){
				if (job.modType == ModJob.BASEGAME) {
					continue; //we don't TOC the basegame.
				}
				//get path to PCConsoleTOC
				for (String newFile : job.newFiles) {
					String filename = FilenameUtils.getName(newFile);
					if (filename.equals("PCConsoleTOC.bin")) {
						continue;
					}
					String modulePath = FilenameUtils.getFullPath(newFile); //inside mod, folders like PATCH2 or MP4. Already has a / on the end.
					ArrayList<String> commandBuilder = new ArrayList<String>();
					// <exe> -toceditorupdate <TOCFILE> <FILENAME> <SIZE>
					commandBuilder.add(me3explorer);
					commandBuilder.add("-toceditorupdate");
					commandBuilder.add(modulePath+"PCConsoleTOC.bin");
					commandBuilder.add(filename); //internal filename (of DLC)
					commandBuilder.add(Long.toString((new File(newFile)).length()));
					
					String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
					//Debug stuff
					StringBuilder sb = new StringBuilder();
					for (String arg : command){
						sb.append(arg+" ");
					}
					
					Process p = null;
					int returncode = 1;
					ModManager.debugLogger.writeMessage("Executing process for TOC Update.");
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
			if (numtoc != completed){
				//failed something
				/*StringBuilder sb = new StringBuilder();
				sb.append("Failed to TOC the following files in the following folders:\n");
				for (String jobName : failedJobs){
					sb.append(" - "+jobName+"\n");
				}
				callingWindow.labelStatus.setText(" Failed to install at least 1 part of mod");
				JOptionPane.showMessageDialog(null, sb.toString(), "Error",
						JOptionPane.ERROR_MESSAGE); */
			} else {
				//we're good
				callingWindow.labelStatus.setText(mod.getModName()+" TOC files updated");
				dispose();
			}
			return;
		}
	}
}
