package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class PatchWindow extends JDialog {
	JLabel infoLabel;
	String BioGameDir;
	final int levelCount = 7;
	JTextArea consoleArea;
	String consoleQueue[];
	String currentText;
	JProgressBar progressBar;
	
	ModManagerWindow callingWindow;

	public PatchWindow(ModManagerWindow callingWindow, DLCInjectJob[] jobs, String BioGameDir, Mod mod) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Injecting mods into DLC");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(320, 220));
		consoleQueue = new String[levelCount];

		setupWindow();

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		new InjectionCommander(jobs, mod).execute();
		this.setVisible(true);
	}

	private void setupWindow() {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		// TODO Auto-generated method stub
		infoLabel = new JLabel("<html>Injecting files into DLC...<br>This may take a few minutes.</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		
		northPanel.add(progressBar, BorderLayout.SOUTH);
		rootPanel.add(northPanel, BorderLayout.NORTH);

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		
		consoleArea.setEditable(false);
		
		rootPanel.add(consoleArea,BorderLayout.CENTER);
		getContentPane().add(rootPanel);
	}

	class InjectionCommander extends SwingWorker<Boolean, String> {
		int completed = 0;
		int numjobs = 0;
		Mod mod;
		DLCInjectJob[] jobs;
		ArrayList<String> failedJobs;

		protected InjectionCommander(DLCInjectJob[] jobs, Mod mod) {
			this.mod = mod;
			numjobs = jobs.length;
			failedJobs = new ArrayList<String>();
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage("Starting the InjectionCommander utility. Number of jobs to do: "+numjobs);
			}
			this.jobs = jobs;
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage(getBetaDirectory());
			}
		}

		@Override
		public Boolean doInBackground() {
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage("Starting the background thread");
			}
			for (DLCInjectJob job : jobs) {
				if (ModManager.logging){
					ModManager.debugLogger.writeMessage("Starting new injection job");
				}
				if (processDLCJob(job)){
					completed++;
					if (ModManager.logging){
						ModManager.debugLogger.writeMessage("Successfully finished injection job");
					}
				} else {
					if (ModManager.logging){
						ModManager.debugLogger.writeMessage("Injection job failed, marking as failure");
					}
					failedJobs.add(job.getDLCFilePath());
				}
				publish(Integer.toString(completed));
			}
			
			return true;

		}

		private boolean processDLCJob(DLCInjectJob job) {
			// TODO Auto-generated method stub
			//System.out.println("Processing DLCJOB");
			ArrayList<String> commandBuilder = new ArrayList<String>();
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage(getBetaDirectory());
			}
			commandBuilder.add(ModManagerWindow.appendSlash(getBetaDirectory())+"ME3Explorer.exe");
			commandBuilder.add("-dlcinject");
			commandBuilder.add(ModManagerWindow.appendSlash(BioGameDir)+ModManagerWindow.appendSlash(job.getDLCFilePath())+"Default.sfar");
			String[] filesToReplace = job.getFilesToReplace();
			String[] newFiles = job.getNewFiles();
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage("Number of files to replace: "+filesToReplace.length);
			}
			publish("Injecting "+filesToReplace.length+" files into "+job.DLCFilePath+"\\Default.sfar");

			for (int i = 0; i<filesToReplace.length;i++){
				commandBuilder.add(filesToReplace[i]);
				commandBuilder.add(newFiles[i]);
				//System.out.println("adding file to command");
			}
			
			//System.out.println("Building command");
			String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
			//Debug stuff
			StringBuilder sb = new StringBuilder();
			for (String arg : command){
				sb.append(arg+" ");
			}
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage(sb.toString());
			}
			Process p = null;
				int returncode = 1;
				try {
					if (ModManager.logging){
						ModManager.debugLogger.writeMessage("Executing process for DLC Injection Job.");
					}
					p = Runtime.getRuntime().exec(command);
					returncode = p.waitFor();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
						
						
			return (p!=null && returncode == 0);
		}

		@Override
		protected void process(List<String> updates) {
			//System.out.println("Restoring next DLC");
			for (String update : updates) {
				try {
					if (ModManager.logging){
						ModManager.debugLogger.writeMessage(update);
					}
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
			if (numjobs != completed){
				//failed something
				StringBuilder sb = new StringBuilder();
				sb.append("Failed to inject DLC into the following folders:\n");
				for (String jobName : failedJobs){
					sb.append(" - "+jobName+"\n");
				}
				callingWindow.labelStatus.setText(" Failed to inject files into at least 1 DLC");
				JOptionPane.showMessageDialog(null, sb.toString(), "Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				//we're good
				callingWindow.labelStatus.setText(" "+mod.getModName()+" installed");
			}
			finishPatch();
			return;
		}
	}
	
	protected void finishPatch(){
		if (ModManager.logging){
			ModManager.debugLogger.writeMessage("Finished injecting DLCs.");
		}
		dispose();
	}

	public String getBetaDirectory() {
		File executable = new File(ModManagerWindow.appendSlash(System.getProperty("user.dir"))+"ME3Explorer.exe");
		if (ModManager.logging){
			ModManager.debugLogger.writeMessage("Searching for ME3Explorer in "+executable.getAbsolutePath());
		}
		if (!executable.exists()){
			//try another file
			executable = new File("ME3Explorer\\ME3Explorer.exe");
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage("Searching for ME3Explorer in "+executable.getAbsolutePath());
			}
			if (!executable.exists()){
				executable = new File("ME3Explorer_0102w_beta\\ME3Explorer.exe");
				if (ModManager.logging){
					ModManager.debugLogger.writeMessage("Searching for ME3Explorer in "+executable.getAbsolutePath());
				}
				if (!executable.exists()){
					StringBuilder sb = new StringBuilder();
					sb.append("Failed to find ME3Explorer.exe in the following directories:\n");
					sb.append(" - "+System.getProperty("user.dir")+"\n");
					sb.append(" - "+System.getProperty("user.dir")+"\\ME3Explorer\\"+"\n");
					sb.append(" - "+System.getProperty("user.dir")+"\\ME3Explorer_0102w_beta\\");
					JOptionPane.showMessageDialog(null, sb.toString(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				
			}
		}
		if (ModManager.logging){
			ModManager.debugLogger.writeMessage("Founed exe in folder: "+ModManagerWindow.appendSlash(executable.getAbsolutePath()));
		}
		return ModManagerWindow.appendSlash(executable.getParent());//ModManagerWindow.appendSlash("ME3Explorer_0102w_beta");
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
				{
					sb.append("\n");
				}
		}
		return sb.toString();
	}

	private void updateInfo() {
		// Log.i(Launch.APPTAG, "DebugView\n"+this.toString());
		consoleArea.setText(getConsoleString());
	}
}
