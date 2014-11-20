package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.exception.ExceptionUtils;

@SuppressWarnings("serial")
/**
 * Window that injects the files into the game/dlc.
 * @author Mgamerz
 *
 */
public class PatchWindow extends JDialog {
	JLabel infoLabel;
	String BioGameDir;
	final int levelCount = 7;
	JTextArea consoleArea;
	String consoleQueue[];
	String currentText;
	JProgressBar progressBar;
	
	ModManagerWindow callingWindow;

	public PatchWindow(ModManagerWindow callingWindow, ModJob[] jobs, String BioGameDir, Mod mod) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.BioGameDir = BioGameDir;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Applying Mod");
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
		infoLabel = new JLabel("<html>Applying mod to game...<br>This may take a few minutes.</html>");
		northPanel.add(infoLabel, BorderLayout.NORTH);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		
		northPanel.add(progressBar, BorderLayout.SOUTH);
		northPanel.setBorder(new EmptyBorder(5,5,5,5));
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
		ModJob[] jobs;
		ArrayList<String> failedJobs;

		protected InjectionCommander(ModJob[] jobs, Mod mod) {
			this.mod = mod;
			numjobs = jobs.length;
			failedJobs = new ArrayList<String>();
			ModManager.debugLogger.writeMessage("Starting the InjectionCommander utility. Number of jobs to do: "+numjobs);
			this.jobs = jobs;
			ModManager.debugLogger.writeMessage("Using ME3Explorer from: "+ModManager.getME3ExplorerEXEDirectory(false));
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("Starting the background thread for PatchWindow");
			
			for (ModJob job : jobs) {
				ModManager.debugLogger.writeMessage("Starting mod job");
				
				if ((job.modType == ModJob.DLC)? processDLCJob(job) : processBasegameJob(job)){ //pick the right method to execute. Return values are the same.
					completed++;
					ModManager.debugLogger.writeMessage("Successfully finished mod job");
					
				} else {
					ModManager.debugLogger.writeMessage("Mod job failed: "+job.getDLCFilePath());
					failedJobs.add(job.getDLCFilePath());
				}
				publish(Integer.toString(completed));
			}
			return true;
		}
		
		private boolean processBasegameJob(ModJob job) {
			File bgdir = new File(ModManagerWindow.appendSlash(BioGameDir));
			String me3dir = ModManagerWindow.appendSlash(bgdir.getParent());
			//Make backup folder if it doesn't exist
			String backupfolderpath = me3dir.toString()+"cmmbackup\\";
			File cmmbackupfolder = new File(backupfolderpath);
			cmmbackupfolder.mkdirs();
			ModManager.debugLogger.writeMessage("Basegame backup directory should have been created if it does not exist already.");
			
			
			//Prep replacement job
			String[] filesToReplace = job.getFilesToReplace();
			String[] newFiles = job.getNewFiles(); 
			int numFilesToReplace = filesToReplace.length;
			ModManager.debugLogger.writeMessage("Number of files to replace in the basegame: "+numFilesToReplace);
			for (int i = 0; i<numFilesToReplace; i++){
				String fileToReplace = filesToReplace[i];
				String newFile = newFiles[i];
				
				//Check for backup
				File basegamefile = new File(me3dir+fileToReplace);
				File backupfile = new File(backupfolderpath+fileToReplace);
				Path originalpath = Paths.get(basegamefile.toString());
				
				ModManager.debugLogger.writeMessage("Checking for backup file at "+backupfile);
				if (!backupfile.exists()) {
					//backup the file
					
					Path backuppath = Paths.get(backupfile.toString());
					backupfile.getParentFile().mkdirs();
					try {
						//backup and then copy file
						Files.copy(originalpath, backuppath);
						ModManager.debugLogger.writeMessage("Backed up "+fileToReplace);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					Path newfilepath = Paths.get(newFile);
					Files.copy(newfilepath, originalpath, StandardCopyOption.REPLACE_EXISTING);
					ModManager.debugLogger.writeMessage("Installed mod file: "+newFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
			return true;
		}

		private boolean processDLCJob(ModJob job) {
			// TODO Auto-generated method stub
			//System.out.println("Processing DLCJOB");
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManagerWindow.appendSlash(ModManager.getME3ExplorerEXEDirectory(true))+"ME3Explorer.exe");
			commandBuilder.add("-dlcinject");
			String sfarName = "Default.sfar";
			if (job.TESTPATCH) {
				sfarName = "Patch_001.sfar";
			}
			commandBuilder.add("\""+ModManagerWindow.appendSlash(BioGameDir)+ModManagerWindow.appendSlash(job.getDLCFilePath())+sfarName+"\"");
			String[] filesToReplace = job.getFilesToReplace();
			String[] newFiles = job.getNewFiles();
			ModManager.debugLogger.writeMessage("Number of files to replace: "+filesToReplace.length);
			
			publish("Injecting "+filesToReplace.length+" files into "+job.DLCFilePath+"\\"+sfarName);
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
			ModManager.debugLogger.writeMessage(sb.toString());
			Process p = null;
				int returncode = 1;
				try {
					ProcessBuilder pb = new ProcessBuilder(command);
					ModManager.debugLogger.writeMessage("Executing process for DLC Injection Job.");
					//p = Runtime.getRuntime().exec(command);
					p = pb.start();
					ModManager.debugLogger.writeMessage("Executed command, waiting...");
					returncode = p.waitFor();
				} catch (IOException | InterruptedException e) {
					ModManager.debugLogger.writeMessage(ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
					return false;
				}
						
			ModManager.debugLogger.writeMessage("processDLCJob RETURN VAL: "+(p!=null && returncode == 0));
			return (p!=null && returncode == 0);
		}

		@Override
		protected void process(List<String> updates) {
			//System.out.println("Restoring next DLC");
			for (String update : updates) {
				try {
					ModManager.debugLogger.writeMessage(update);
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
				sb.append("Failed to process mod installation.\nSome parts of the install may have succeeded.\nTurn on debugging via Help>About and check the log file.");
				callingWindow.labelStatus.setText(" Failed to install at least 1 part of mod");
				JOptionPane.showMessageDialog(null, sb.toString(), "Error",
						JOptionPane.ERROR_MESSAGE);
				ModManager.debugLogger.writeMessage(mod.getModName()+" failed to fully install.");
			} else {
				//we're good
				callingWindow.labelStatus.setText(" "+mod.getModName()+" installed");
			}
			finishPatch();
			return;
		}
	}
	
	protected void finishPatch(){
		ModManager.debugLogger.writeMessage("Finished installing mod.");
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
				{
					sb.append("\n");
				}
		}
		return sb.toString();
	}

	private void updateInfo() {
		consoleArea.setText(getConsoleString());
	}
}
