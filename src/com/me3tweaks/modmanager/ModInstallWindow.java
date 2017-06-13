package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.ProcessResult;
import com.me3tweaks.modmanager.repairdb.BasegameHashDB;
import com.me3tweaks.modmanager.repairdb.RepairFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
/**
 * Window that injects the files into the game/dlc.
 * 
 * @author Mgamerz
 *
 */
public class ModInstallWindow extends JDialog {
	JLabel infoLabel;
	String bioGameDir;
	final int levelCount = 7;
	JTextArea consoleArea;
	String consoleQueue[];
	String currentText;
	JProgressBar progressBar;
	ModManagerWindow callingWindow;
	public final static String CUSTOMDLC_METADATA_FILE = "_metacmm.txt";

	public ModInstallWindow(ModManagerWindow callingWindow, String bioGameDir, Mod mod) {
		// callingWindow.setEnabled(false);
		this.callingWindow = callingWindow;
		this.bioGameDir = bioGameDir;
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle("Applying Mod");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(320, 220));
		consoleQueue = new String[levelCount];

		setupWindow(mod);

		this.setIconImages(ModManager.ICONS);
		this.pack();
		this.setLocationRelativeTo(callingWindow);

		checkModCMMVersion(mod);
		boolean installMod = validateRequiredModulesAreAvailable(callingWindow, mod);
		if (installMod) {
			new InjectionCommander(mod).execute();
			this.setVisible(true);
		} else {
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Mod install cancelled");
			dispose();
		}
	}

	/**
	 * Checks to make sure that the MODDESC can be fully parsed.
	 * 
	 * @param mod
	 *            mod to check against
	 */
	private void checkModCMMVersion(Mod mod) {
		if (mod.getCMMVer() > ModManager.MODDESC_VERSION_SUPPORT) {
			JOptionPane.showMessageDialog(callingWindow,
					"This mod specifies it requires a newer version of Mod Manager: " + mod.getCMMVer() + ".\nMod Manager will attempt to install the mod but it may not work.",
					"Outdated Mod Manager", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Checks that the modjobs required modules are available and prompts if
	 * they aren't
	 * 
	 * @return true if all are available or user ignored missing
	 */
	private boolean validateRequiredModulesAreAvailable(ModManagerWindow callingWindow, Mod mod) {
		ArrayList<ModJob> missingModules = new ArrayList<ModJob>();
		for (ModJob job : mod.jobs) {
			if (job.getJobType() == ModJob.DLC) {
				String commandlinetoolsdir = ModManager.getCommandLineToolsDir();
				File fullautotoc = new File(commandlinetoolsdir + "FullAutoTOC.exe");
				File sfarinjector = new File(commandlinetoolsdir + "SFARTools-Inject.exe");
				if (!fullautotoc.exists() || !sfarinjector.exists()) {
					dispose();
					ModManager.debugLogger.writeError("Mod Manager Command Line tools are not available. Aborting installation");
					JOptionPane.showMessageDialog(null,
							"Installation of mods requires the Mod Manager Command Line tools library.\nThis will automatically download on startup when connected to the internet.\nMod installation cannot continue.",
							"Required Component Missing", JOptionPane.ERROR_MESSAGE);
					return false;
				}

				//check that sfar is available
				String sfarName = "Default.sfar";
				if (job.TESTPATCH) {
					sfarName = "Patch_001.sfar";
				}
				String sfarPath = ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName;
				File sfar = new File(sfarPath);
				if (!sfar.exists()) {
					missingModules.add(job);
				}
			}
		}
		if (missingModules.size() <= 0) {
			ModManager.debugLogger.writeMessage("Mod has all required DLCs available");
			return true;
		}

		//module is missing
		StringBuilder sb = new StringBuilder();
		sb.append("This mod has tasks for the following missing DLC.\nIf the mod descriptor details the job description, they will be listed below.\n");
		for (ModJob job : missingModules) {
			ModManager.debugLogger.writeMessage("Mod requires missing DLC Module: " + job.getJobName());
			sb.append(" - ");
			sb.append(job.getJobName());
			sb.append("\n");
			if (job.getRequirementText() != null && !job.getRequirementText().equals("")) {
				sb.append("   - ");
				sb.append(job.getRequirementText());
				sb.append("\n");
			}
		}
		sb.append("\nThese jobs will be skipped. Continue with the mod install?");
		int result = JOptionPane.showConfirmDialog(callingWindow, sb.toString(), "Missing DLC", JOptionPane.WARNING_MESSAGE);
		ModManager.debugLogger.writeMessage(result == JOptionPane.YES_OPTION ? "User continuing install even with missing DLC modules" : "User canceled Mod Install");
		return result == JOptionPane.YES_OPTION;
	}

	private void setupWindow(Mod mod) {
		JPanel rootPanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("<html><center>Now Installing<br>" + mod.getModName() + "</center></html>", SwingConstants.CENTER);
		northPanel.add(infoLabel, BorderLayout.NORTH);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);

		northPanel.add(progressBar, BorderLayout.SOUTH);
		northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rootPanel.add(northPanel, BorderLayout.NORTH);

		consoleArea = new JTextArea();
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);

		consoleArea.setEditable(false);

		rootPanel.add(consoleArea, BorderLayout.CENTER);
		getContentPane().add(rootPanel);
	}

	/**
	 * Handles mod installation
	 * 
	 * @author mgamerz
	 *
	 */
	class InjectionCommander extends SwingWorker<Boolean, String> {
		AtomicInteger completed = new AtomicInteger(0);
		double numjobs = 0;
		double taskSteps = 0;
		double completedTaskSteps = 0;
		Mod mod;
		ModJob[] jobs;
		ArrayList<String> failedJobs;
		private BasegameHashDB bghDB;
		private boolean installCancelled = false;
		private boolean failedLoadingDB = false;
		private boolean alternatesApplied;
		private ArrayList<String> outdatedinstalledfolders;
		private boolean skipTOC = false;

		protected InjectionCommander(Mod mod) {
			this.mod = new Mod(mod); //clone before applying alternates and optional addins
			ModManager.debugLogger.writeMessage("========Installing " + this.mod.getModName() + "========");
			ModManager.debugLogger.writeMessage("Applying alternate files before parsing jobs");
			alternatesApplied = this.mod.applyAutomaticAlternates(bioGameDir);
			alternatesApplied |= this.mod.applyManualAlternates(bioGameDir); //Must be separate or it might short circuit in compilation!
			if (alternatesApplied) {
				ModManager.debugLogger.writeMessage("At least one alternate file was applied, install now requires pre-toc.");
			}
			ModManager.debugLogger.writeMessage("Finshing applying alternate files. Now checking for manually selected addins...");
			this.mod.applyManualCustomDLCs();
			ModManager.debugLogger.writeMessage("Finished applying addins. Preparing to start the injection thread.");
			this.jobs = this.mod.getJobs();
			numjobs = jobs.length;
			failedJobs = new ArrayList<String>();
			ModManager.debugLogger.writeMessage("Starting the InjectionCommander thread. Number of jobs to do: " + numjobs);
		}

		@Override
		public Boolean doInBackground() {
			ModManager.debugLogger.writeMessage("===========INJECTION COMMANDER BACKGROUND THREAD==============");
			ModManager.debugLogger.writeMessage("Checking for DLC Bypass.");
			if (!ModManager.hasKnownDLCBypass(bioGameDir)) {
				ModManager.debugLogger.writeMessage("No DLC bypass detected, installing binkw32 bypass...");
				if (!ModManager.installBinkw32Bypass(bioGameDir, false)) {
					ModManager.debugLogger.writeError("Binkw32 bypass failed to install");
				}
			} else {
				ModManager.debugLogger.writeMessage("A DLC bypass is installed");
			}

			boolean checkedDB = false;
			for (ModJob job : jobs) {
				if (job.getJobName().equals(ModType.CUSTOMDLC) || job.getJobName().equals(ModType.TESTPATCH)) {
					continue;
				}
				if ((job.getJobName().equals(ModType.BASEGAME) && job.getFilesToReplaceTargets().size() == 0 && job.getFilesToRemoveTargets().size() == 0)) {
					continue;
				}
				checkedDB = true;
				if (precheckGameDB(jobs)) {
					ModManager.debugLogger.writeMessage("Precheck DB method has returned true, indicating user wants to open repair DB and cancel mod install.");
					skipTOC = true;
					return false;
				} else {
					ModManager.debugLogger.writeMessage("Precheck DB method has returned false, everything is OK and mod install will continue");
				}
				break;
			}
			if (!checkedDB) {
				ModManager.debugLogger.writeMessage("Mod only adds files to basegame/adds custom DLC. The Game DB check has been skipped");
			}

			ModManager.debugLogger.writeMessage("Processing mod jobs in job queue.");
			ExecutorService modinstallExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
			for (ModJob job : jobs) {
				//submit jobs
				JobTask jtask = new JobTask(job);
				futures.add(modinstallExecutor.submit(jtask));
			}
			modinstallExecutor.shutdown();
			try {
				modinstallExecutor.awaitTermination(5, TimeUnit.MINUTES);
				for (Future<Boolean> f : futures) {
					boolean b = f.get();
					if (!b) {
						ModManager.debugLogger.writeError("A task failed!");
						//throw some sort of error here...
					}
				}
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeErrorWithException("EXECUTION EXCEPTION WHILE INSTALLING MOD: ", e);
				return null;
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("UNKNOWN EXCEPTION OCCURED: ", e);
				return null;
			}

			checkForOutdatedDLC();

			return true;
		}

		private void checkForOutdatedDLC() {
			if (mod.getOutdatedDLCModules().size() > 0) {
				outdatedinstalledfolders = new ArrayList<>();
				ArrayList<String> installedDLC = ModManager.getInstalledDLC(bioGameDir);
				for (String installeddlcitem : installedDLC) {
					for (String outdateditem : mod.getOutdatedDLCModules()) {
						if (installeddlcitem.toUpperCase().equals(outdateditem.toUpperCase())) {
							//outdated item is still installed.
							outdatedinstalledfolders.add(outdateditem);
						}
					}
				}
			}

		}

		class JobTask implements Callable<Boolean> {
			private ModJob job;

			public JobTask(ModJob job) {
				this.job = job;
			}

			@Override
			public Boolean call() throws Exception {
				if (installCancelled) {
					return false;
				}
				boolean result = false;
				switch (job.getJobType()) {
				case ModJob.DLC:
					result = processDLCJob(job);
					break;
				case ModJob.BASEGAME:
					result = processBasegameJob(job);
					break;
				case ModJob.CUSTOMDLC:
					result = processCustomDLCJob(job);
					break;
				case ModJob.BALANCE_CHANGES:
					result = processBalanceChangesJob(job);
					break;
				}
				//end of each callable...
				if (result) {
					completed.incrementAndGet();
					ModManager.debugLogger.writeMessage("Successfully finished mod job.");
				} else {
					ModManager.debugLogger.writeMessage("Mod job failed: " + job.getDLCFilePath());
					failedJobs.add(job.getDLCFilePath());
				}
				publish(Integer.toString(completed.get()));
				return result;
			}
		}

		/**
		 * Checks the game DB for files in all jobs to see if any need to be
		 * added.
		 * 
		 * @param jobs
		 *            Jobs to check
		 * @return true if user clicks YES to open DB window, false if they
		 *         don't (or all is Ok)
		 */
		private boolean precheckGameDB(ModJob[] jobs) {
			ModManager.debugLogger.writeMessage("---PRECHECKING GAME DATABASE---");
			File bgdir = new File(ModManager.appendSlash(bioGameDir));
			String me3dir = ModManager.appendSlash(bgdir.getParent());

			publish("Checking game database for files that need to be backed up");
			if (bghDB == null) {
				publish("Loading game repair database");
				try {
					bghDB = new BasegameHashDB(null, me3dir, false);
				} catch (SQLException e) {
					while (e.getNextException() != null) {
						ModManager.debugLogger.writeErrorWithException("DB FAILED TO LOAD.", e);
						e = e.getNextException();
					}
				}
				if (bghDB == null) {
					//cannot continue
					failedLoadingDB = true;
					JOptionPane.showMessageDialog(null,
							"<html>The game repair database failed to load.<br>" + "Only one connection to the local repair database is allowed at a time.<br>"
									+ "Please make sure you only have one instance of Mod Manager running.<br>Mod Manager appears as Java (TM) Platform Binary (or javaw.exe on Windows Vista/7) in Task Manager.<br><br>If the issue persists and you are sure only one instance is running, close Mod Manager and<br>delete the the data\\databases folder.<br>You will need to re-create the game repair database afterwards.<br><br>If this *STILL* does not fix your issue, please send a log to FemShep through the help menu.</html>",
							"Database Failure", JOptionPane.ERROR_MESSAGE);
					return true;
				}

			}
			//check if DB exists
			if (!bghDB.isBasegameTableCreated()) {
				JOptionPane.showMessageDialog(ModInstallWindow.this,
						"The game repair database has not been created.\nMods that affect the basegame or official DLC require the game repair database to install.",
						"No Game Repair Database", JOptionPane.ERROR_MESSAGE);
				return true; //open DB window
			}

			for (ModJob job : jobs) {
				if (job.getJobType() == ModJob.BALANCE_CHANGES) {
					ModManager.debugLogger.writeMessage("Skipping GRDB check for balance changes job");
					continue;
				}
				publish("Checking database on " + job.getJobName());
				if (job.getJobType() == ModJob.BASEGAME) {
					//BGDB files are required
					ArrayList<String> filesToReplace = job.getFilesToReplaceTargets();
					int numFilesToReplace = filesToReplace.size();
					for (int i = 0; i < numFilesToReplace; i++) {
						String fileToReplace = filesToReplace.get(i);
						if (FilenameUtils.getName(fileToReplace).equalsIgnoreCase("PCConsoleTOC.bin")) {
							continue; //skip PCConsoleTOC as they'll be updated outside of mod installs. Especially the basegame one.
						}
						File basegamefile = new File(me3dir + fileToReplace);

						String relative = ResourceUtils.getRelativePath(basegamefile.getAbsolutePath(), me3dir, File.separator);
						RepairFileInfo rfi = bghDB.getFileInfo(relative);
						if (rfi == null) {
							ModManager.debugLogger.writeMessage("File not in GameDB, showing prompt: " + relative);
							// file is missing. Basegame DB likely hasn't been made
							int reply = JOptionPane.showConfirmDialog(null,
									"<html>" + relative
											+ " is not in the game repair database.<br>In order to restore basegame files and unpacked DLC files this database needs to be created or updated.<br>Open the database window?</html>",
									"Mod Installation Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if (reply == JOptionPane.NO_OPTION) {
								return false;
							} else {
								return true;
							}
						}
					}
				} else {
					//DLC files are not required... unless all are present
					ArrayList<String> filesToReplace = job.getFilesToReplaceTargets();
					ArrayList<String> filesToRemove = job.getFilesToRemoveTargets();
					int numFilesToReplace = filesToReplace.size();
					boolean fileIsMissing = false;
					//Check for files to replace not being present for backup
					for (int i = 0; i < numFilesToReplace; i++) {
						String fileToReplace = filesToReplace.get(i);
						File unpackeddlcfile = new File(me3dir + fileToReplace);
						if (!unpackeddlcfile.exists()) {
							fileIsMissing = true;
							ModManager.debugLogger.writeMessage("Game DB: unpacked DLC file not present. DLC is assumed to still be in SFAR: " + job.getJobName());
							break;
						}
					}

					//Check for files to remove not being present for backup
					for (String removeFile : filesToRemove) {
						File unpackeddlcfile = new File(me3dir + removeFile);
						if (!unpackeddlcfile.exists()) {
							fileIsMissing = true;
							ModManager.debugLogger.writeMessage("Game DB: unpacked DLC file not present. DLC is assumed to still be in SFAR: " + job.getJobName());
							break;
						}
					}

					if (fileIsMissing) {
						continue;
					}

					//DLC appears unpacked					
					for (int i = 0; i < numFilesToReplace; i++) {
						String fileToReplace = filesToReplace.get(i);
						if (FilenameUtils.getName(fileToReplace).equalsIgnoreCase("PCConsoleTOC.bin")) {
							continue; //skip PCConsoleTOC as they'll be updated outside of mod installs. Especially the basegame one.
						}
						File unpackeddlcfile = new File(me3dir + fileToReplace);

						//check if in GDB
						String relative = ResourceUtils.getRelativePath(unpackeddlcfile.getAbsolutePath(), me3dir, File.separator);
						RepairFileInfo rfi = bghDB.getFileInfo(relative);
						if (rfi == null) {
							// file is missing. Basegame DB likely hasn't been made
							int reply = JOptionPane.showConfirmDialog(null,
									"<html>One or more of the files this mod is installing is not in the game repair database.<br>In order to restore game files this database needs to be created or updated.<br>Open the database window?</html>",
									"Mod Installation Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if (reply == JOptionPane.NO_OPTION) {
								return false;
							} else {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		private boolean processBasegameJob(ModJob job) {
			ModManager.debugLogger.writeMessage("===Processing a basegame job===");
			completedTaskSteps = 0;
			taskSteps = job.getFilesToReplace().size() + job.getFilesToAdd().size() + job.getFilesToRemoveTargets().size();
			publish("Processing basegame files...");
			File bgdir = new File(ModManager.appendSlash(bioGameDir));
			String me3dir = ModManager.appendSlash(bgdir.getParent());
			// Make backup folder if it doesn't exist
			String backupfolderpath = me3dir.toString() + "cmmbackup\\";
			File cmmbackupfolder = new File(backupfolderpath);
			if (cmmbackupfolder.mkdirs()) {
				ModManager.debugLogger.writeMessage("Created unpacked files backup directory.");
			}
			// Prep replacement job
			{
				ArrayList<String> filesToReplace = job.getFilesToReplaceTargets();
				ArrayList<String> newFiles = job.getFilesToReplace();
				int numFilesToReplace = filesToReplace.size();
				ModManager.debugLogger.writeMessage("Number of files to replace in the basegame: " + numFilesToReplace);
				for (int i = 0; i < numFilesToReplace; i++) {
					String fileToReplace = filesToReplace.get(i);
					String newFile = newFiles.get(i);

					boolean shouldContinue = checkBackupAndHash(me3dir, fileToReplace, job);
					if (!shouldContinue) {
						installCancelled = true;
						return false;
					}

					// install file.
					File unpacked = new File(me3dir + fileToReplace);
					Path originalpath = Paths.get(unpacked.toString());
					if (!unpacked.getAbsolutePath().toLowerCase().endsWith("pcconsoletoc.bin")) {
						try {
							publish(ModType.BASEGAME + ": Installing " + FilenameUtils.getName(newFile));
							Path newfilepath = Paths.get(newFile);
							Files.copy(newfilepath, originalpath, StandardCopyOption.REPLACE_EXISTING);
							completedTaskSteps++;
							ModManager.debugLogger.writeMessage("Installed mod file: " + newFile + " => " + unpacked);
						} catch (IOException e) {
							ModManager.debugLogger.writeException(e);
							return false;
						}
					} else {
						ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Post-Install TOC indicates we should skip installing PCConsoleTOC for this job");
					}
				}
			}

			//ADD TASKS
			ArrayList<String> filesToAddTargets = job.getFilesToAddTargets();
			ArrayList<String> filesToAdd = job.getFilesToAdd();
			int numFilesToAdd = filesToAdd.size();
			ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Number of files to add to the basegame: " + numFilesToAdd);
			for (int i = 0; i < numFilesToAdd; i++) {
				String fileToAddTarget = filesToAddTargets.get(i);
				String fileToAdd = filesToAdd.get(i);
				// install file.
				File installFile = new File(me3dir + fileToAddTarget);
				Path installPath = Paths.get(installFile.toString());
				try {
					publish(ModType.BASEGAME + ": Installing " + FilenameUtils.getName(fileToAdd));
					Path newfilepath = Paths.get(fileToAdd);
					if (installFile.exists()) {
						installFile.delete();
					}
					Files.copy(newfilepath, installPath, StandardCopyOption.REPLACE_EXISTING);
					if (job.getAddFilesReadOnlyTargets().contains(fileToAddTarget)) {
						installFile.setReadOnly();
						ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Set read only: " + installFile);
					}
					completedTaskSteps++;
					ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Installed mod file: " + fileToAdd + " => " + installFile);
				} catch (IOException e) {
					ModManager.debugLogger.writeException(e);
					return false;
				}
			}

			//REMOVAL TASKS
			ArrayList<String> filesToRemove = job.getFilesToRemoveTargets();
			ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Number of files to remove: " + filesToRemove.size());
			for (String fileToRemove : filesToRemove) {
				boolean userCanceled = checkBackupAndHash(me3dir, fileToRemove, job);
				if (userCanceled) {
					installCancelled = true;
					return false;
				}

				// remove file.
				File unpacked = new File(me3dir + fileToRemove);
				ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Removing file: " + unpacked);
				publish(job.getJobName() + ": Removing " + FilenameUtils.getName(unpacked.getAbsolutePath()));
				FileUtils.deleteQuietly(unpacked);
				completedTaskSteps++;
				ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Deleted game file: " + unpacked);
			}
			return true;
		}

		private boolean processBalanceChangesJob(ModJob job) {
			ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]===Processing a balance changes job===");
			completedTaskSteps = 0;
			taskSteps = job.getFilesToReplace().size() + job.getFilesToAdd().size() + job.getFilesToRemoveTargets().size();
			publish("Processing balance changes files...");
			File bgdir = new File(ModManager.appendSlash(bioGameDir));
			String me3dir = ModManager.appendSlash(bgdir.getParent());

			// Prep replacement job
			{
				ArrayList<String> filesToReplace = job.getFilesToReplaceTargets();
				ArrayList<String> newFiles = job.getFilesToReplace();
				int numFilesToReplace = filesToReplace.size();
				ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Number of files to replace in the basegame (balance changes): " + numFilesToReplace);
				for (int i = 0; i < numFilesToReplace; i++) {
					String fileToReplace = filesToReplace.get(i);
					String newFile = newFiles.get(i);

					// install file.
					File unpacked = new File(me3dir + fileToReplace);
					Path originalpath = Paths.get(unpacked.toString());
					try {
						publish(ModType.BASEGAME + ": Installing " + FilenameUtils.getName(newFile));
						Path newfilepath = Paths.get(newFile);
						if (!unpacked.getParentFile().exists()) {
							unpacked.getParentFile().mkdirs();
						}
						Files.copy(newfilepath, originalpath, StandardCopyOption.REPLACE_EXISTING);
						completedTaskSteps++;
						ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Installed mod file: " + newFile + " => " + unpacked);
					} catch (IOException e) {
						ModManager.debugLogger.writeException(e);
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * Processes a DLC job. Installs via SFAR (injection) if files in their
		 * unpacked location do not exist.
		 * 
		 * @param job
		 *            Job to install
		 * @return true if successful, false otherwise
		 */
		private boolean processDLCJob(ModJob job) {
			ModManager.debugLogger.writeMessage("===Processing a dlc job: " + job.getJobName() + "===");
			if (job.getJobName().equals("LEVIATHAN")) {
				System.out.println("BREAK");
			}
			File bgdir = new File(ModManager.appendSlash(bioGameDir));
			String me3dir = ModManager.appendSlash(bgdir.getParent());

			//Check for files to replace not being present
			for (int i = 0; i < job.filesToReplaceTargets.size(); i++) {
				String fileToReplace = job.filesToReplaceTargets.get(i);
				File unpackeddlcfile = new File(me3dir + fileToReplace);
				if (!unpackeddlcfile.exists()) {
					ModManager.debugLogger.writeMessage("[" + job.getJobName() + "]Game DB: unpacked DLC file not present. DLC job will use SFAR method: " + job.getJobName());
					return processSFARDLCJob(job);
				}
			}

			//Check that the default.sfar file is not smaller than the normal size (typically means unpacked)
			String sfarName = "Default.sfar";
			if (job.TESTPATCH) {
				sfarName = "Patch_001.sfar";
			}
			long knownsfarsize = ModType.getSizesMap().get(job.getJobName());
			String sfarPath = ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName;
			File sfarFile = new File(sfarPath);
			if (sfarFile.exists()) {
				if (sfarName.equals("Patch_001.sfar") || sfarFile.length() >= knownsfarsize) {
					ModManager.debugLogger.writeMessage("[" + job.getJobName()
							+ "]SFAR is same or larger in bytes than the known original. Likely is the vanilla one, or has been modified, but not unpacked. Using the SFAR method: "
							+ job.getJobName());
					return processSFARDLCJob(job);
				}
			} else {
				ModManager.debugLogger.writeError("[" + job.getJobName() + "]SFAR doesn't exist for unpacked DLC... interesting... " + sfarPath);
			}

			//We don't need to check for files to remove, as if it this is an unpacked DLC we can just skip the file. If it is missing in the DLC then there would be nothing we can do.

			//UNPACKED DLC METHOD
			return updateUnpackedDLC(job);
		}

		/**
		 * Processes a DLC job using the unpacked DLC method
		 * 
		 * @param job
		 *            job to conduct
		 * @return true is successful, false otherwise
		 */
		private boolean updateUnpackedDLC(ModJob job) {
			completedTaskSteps = 0;
			taskSteps = job.getFilesToReplace().size() + job.getFilesToAdd().size() + job.getFilesToRemoveTargets().size();
			publish(job.getJobName() + ": Installing using unpacked DLC method");
			File bgdir = new File(ModManager.appendSlash(bioGameDir));
			String me3dir = ModManager.appendSlash(bgdir.getParent());

			// Make backup folder if it doesn't exist

			//REPLACEMENT TASKS
			ArrayList<String> filesToReplace = job.getFilesToReplaceTargets();
			ArrayList<String> newFiles = job.getFilesToReplace();
			int numFilesToReplace = filesToReplace.size();
			ModManager.debugLogger.writeMessage("Number of files to replace in the DLC: " + numFilesToReplace);
			for (int i = 0; i < numFilesToReplace; i++) {
				String fileToReplace = filesToReplace.get(i);
				String newFile = newFiles.get(i);

				boolean shouldContinue = checkBackupAndHash(me3dir, fileToReplace, job);
				if (!shouldContinue) {
					installCancelled = true;
					return false;
				}

				// install file.
				File unpacked = new File(me3dir + fileToReplace);
				Path originalpath = Paths.get(unpacked.toString());
				if (!unpacked.getAbsolutePath().toLowerCase().endsWith("pcconsoletoc.bin")) {

					try {
						Path newfilepath = Paths.get(newFile);
						Files.copy(newfilepath, originalpath, StandardCopyOption.REPLACE_EXISTING);
						completedTaskSteps++;
						ModManager.debugLogger.writeMessage("Installed mod file: " + newFile + " => " + originalpath);
					} catch (IOException e) {
						ModManager.debugLogger.writeException(e);
						return false;
					}
				} else {
					ModManager.debugLogger.writeMessage("Post-Install TOC indicates we should skip installing PCConsoleTOC for this job");

				}
			}

			//ADD TASKS
			ArrayList<String> filesToAdd = job.getFilesToAdd();
			ArrayList<String> filesToAddTargets = job.getFilesToAddTargets();
			int numFilesToAdd = filesToAddTargets.size();
			ModManager.debugLogger.writeMessage("Number of files to add in the DLC: " + numFilesToAdd);
			for (int i = 0; i < numFilesToAdd; i++) {
				String addFile = filesToAdd.get(i);
				String addFileTarget = filesToAddTargets.get(i);

				// install file.
				File unpacked = new File(me3dir + addFileTarget);
				Path originalpath = Paths.get(unpacked.toString());
				try {
					ModManager.debugLogger.writeMessage("Adding new mod file: " + addFile);
					publish(job.getJobName() + ": Adding new file " + FilenameUtils.getName(addFile));
					Path newfilepath = Paths.get(addFile);
					if (unpacked.exists()) {
						unpacked.delete();
					}
					Files.copy(newfilepath, originalpath, StandardCopyOption.REPLACE_EXISTING);
					if (job.getAddFilesReadOnlyTargets().contains(addFileTarget)) {
						unpacked.setReadOnly();
						ModManager.debugLogger.writeMessage("Set read-only: " + unpacked);
					}
					completedTaskSteps++;
					ModManager.debugLogger.writeMessage("Added mod file: " + addFile);
				} catch (IOException e) {
					ModManager.debugLogger.writeException(e);
					return false;
				}
			}

			//REMOVAL TASKS
			ArrayList<String> filesToRemove = job.getFilesToRemoveTargets();
			ModManager.debugLogger.writeMessage("Number of files to remove: " + filesToRemove.size());
			for (String fileToRemove : filesToRemove) {
				boolean shouldContinue = checkBackupAndHash(me3dir, fileToRemove, job);
				if (!shouldContinue) {
					installCancelled = true;
					return false;
				}

				// install file.
				File unpacked = new File(me3dir + fileToRemove);
				Path originalpath = Paths.get(unpacked.toString());
				if (unpacked.exists()) {
					try {
						ModManager.debugLogger.writeMessage("Removing file: " + unpacked);
						publish(job.getJobName() + ": Removing " + FilenameUtils.getName(unpacked.getAbsolutePath()));
						Files.delete(originalpath);
						completedTaskSteps++;
						ModManager.debugLogger.writeMessage("Deleted mod file: " + unpacked);
					} catch (IOException e) {
						ModManager.debugLogger.writeException(e);
						return false;
					}
				} else {
					ModManager.debugLogger.writeMessage(unpacked + " was to be removed but does not exist, skipping");
					completedTaskSteps++;
					publish(job.getJobName() + ": " + FilenameUtils.getName(unpacked.getAbsolutePath()) + " not present for removal, skipping");
				}
			}
			return true;
		}

		/**
		 * Checks for a backup file and the hash of the original one in the DB
		 * to make sure they match if no backup is found.
		 * 
		 * @param me3dir
		 *            ME3 DIR to use as a base
		 * @param fileToReplace
		 *            file that will be replaced, as a relative path
		 * @param job
		 *            job (for outputting name)
		 * @return true if file is backed up (and hashed OK), false if it is not
		 *         backed up/error/hashfail
		 */
		private boolean checkBackupAndHash(String me3dir, String fileToReplace, ModJob job) {
			String backupfolderpath = me3dir.toString() + "cmmbackup\\";
			File cmmbackupfolder = new File(backupfolderpath);
			boolean madeDir = cmmbackupfolder.mkdirs();
			if (madeDir) {
				ModManager.debugLogger.writeMessage("Created unpacked files backup directory.");
			}

			// Check for backup
			File unpacked = new File(me3dir + fileToReplace);
			File backupfile = new File(backupfolderpath + fileToReplace);
			Path originalpath = Paths.get(unpacked.toString());

			ModManager.debugLogger.writeMessage("Checking for backup file at " + backupfile);
			if (!backupfile.exists()) {
				// backup the file
				if (bghDB == null) {
					publish(job.getJobName() + ": Loading repair database");
					try {
						bghDB = new BasegameHashDB(null, me3dir, false);
					} catch (SQLException e) {
						installCancelled = true;
						failedLoadingDB = true;
						ModManager.debugLogger.writeErrorWithException("DB FAILED TO LOAD!", e);
						return false;
					}
				}
				Path backuppath = Paths.get(backupfile.toString());
				backupfile.getParentFile().mkdirs();

				String relative = ResourceUtils.getRelativePath(unpacked.getAbsolutePath(), me3dir, File.separator);
				boolean installAndUpdate = false;

				if (!FilenameUtils.getName(fileToReplace).equalsIgnoreCase("PCConsoleTOC.bin")) {
					RepairFileInfo rfi = bghDB.getFileInfo(relative);
					// validate file to backup.
					boolean justInstall = false;
					if (rfi == null) {
						int reply = JOptionPane.showOptionDialog(null,
								"<html><div style=\"width: 400px\">The file:<br>" + relative + "<br>is not in the repair database. "
										+ "Installing/Removing this file may overwrite your default setup if you restore and have custom mods like texture swaps installed.</div></html>",
								"Backing Up Unverified File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
								new String[] { "Add to DB and install", "Install file", "Cancel mod installation" }, "default");
						switch (reply) {
						case JOptionPane.CANCEL_OPTION:
							installCancelled = true;
							return false;
						case JOptionPane.NO_OPTION:
							justInstall = true;
							break;
						case JOptionPane.YES_OPTION:
							installAndUpdate = true;
							break;
						}
					}

					// Check filesize
					if (!justInstall && !installAndUpdate) {
						if (unpacked.length() != rfi.filesize) {
							// MISMATCH!
							int reply = JOptionPane.showOptionDialog(null,
									"<html>The filesize of the file:<br>" + relative + "<br>does not match the one stored in the repair game database.<br>" + unpacked.length()
											+ " bytes (installed) vs " + rfi.filesize + " bytes (database)<br><br>"
											+ "This file could be corrupted or modified since the database was created.<br>"
											+ "Backing up this file may overwrite your default setup if you use custom mods like texture swaps when you restore.<br></html>",
									"Backing Up Unverified File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
									new String[] { "Backup and update DB", "Backup this file", "Cancel mod installation" }, "default");
							switch (reply) {
							case JOptionPane.CANCEL_OPTION:
								return false;
							case JOptionPane.NO_OPTION:
								justInstall = true;
								break;
							case JOptionPane.YES_OPTION:
								installAndUpdate = true;
								break;
							}
						}
					}

					// Check hash
					if (!justInstall && !installAndUpdate) {
						// this is outside of the previous if statement as the
						// previous one could set the restoreAnyways variable
						// again.
						try {
							if (!MD5Checksum.getMD5Checksum(unpacked.getAbsolutePath()).equals(rfi.md5)) {
								int reply = JOptionPane.showOptionDialog(null,
										"<html>The hash of the file:<br>" + relative + "<br>does not match the one stored in the repair game database.<br>"
												+ "This file has changed since the database was created.<br>"
												+ "Backing up this file may overwrite your default setup if you use custom mods like texture swaps when restoring.<br></html>",
										"Backing Up Unverified File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
										new String[] { "Backup and update DB", "Backup this file", "Cancel mod installation" }, "default");
								switch (reply) {
								case JOptionPane.CANCEL_OPTION:
									return false;
								case JOptionPane.NO_OPTION:
									justInstall = true;
									break;
								case JOptionPane.YES_OPTION:
									installAndUpdate = true;
									break;
								}
							}
						} catch (Exception e) {
							ModManager.debugLogger.writeException(e);
						}
					}
				}
				try {
					// backup and then copy file
					Files.copy(originalpath, backuppath);
					ModManager.debugLogger.writeMessage("Backed up " + fileToReplace);
					if (installAndUpdate) {
						ArrayList<File> updateFile = new ArrayList<File>();
						updateFile.add(unpacked);
						bghDB.updateDB(updateFile);
					}
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("ERROR BACKING UP FILE:", e);
					return false;
				}
			}
			return true;
		}

		/**
		 * Processes a DLC job using the SFAR method
		 * 
		 * @param job
		 *            job to conduct
		 * @return true if success, false otherwise
		 */
		public boolean processSFARDLCJob(ModJob job) {
			boolean result = true;
			completedTaskSteps = 0;
			taskSteps = 0;

			//REPLACE JOB
			if (job.getFilesToReplaceTargets().size() > 0) {

				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.getCommandLineToolsDir() + "SFARTools-Inject.exe");
				commandBuilder.add("--sfarpath");
				String sfarName = "Default.sfar";
				if (job.TESTPATCH) {
					sfarName = "Patch_001.sfar";
				}
				File sfarFile = new File(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName);
				if (!sfarFile.exists()) {
					//missing module
					ModManager.debugLogger.writeMessage("Missing DLC Module, skipping: " + sfarFile.getAbsolutePath());
					publish("DLC is missing: " + job.getDLCFilePath());
					return true;
				}
				commandBuilder.add(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName);
				commandBuilder.add("--replacefiles");
				commandBuilder.add("--files");

				ArrayList<String> filesToReplace = job.getFilesToReplaceTargets();
				ArrayList<String> newFiles = job.getFilesToReplace();
				ModManager.debugLogger.writeMessage("Number of files to replace: " + filesToReplace.size());

				publish("Updating " + filesToReplace.size() + " files in " + job.getJobName());
				for (int i = 0; i < filesToReplace.size(); i++) {
					commandBuilder.add(filesToReplace.get(i));

					String newFile = newFiles.get(i);
					commandBuilder.add(newFile);
				}
				int returncode = 1;
				ProcessBuilder pb = new ProcessBuilder(commandBuilder);
				ModManager.debugLogger.writeMessage("Executing process for DLC Injection Job.");
				// p = Runtime.getRuntime().exec(command);
				ProcessResult pr = ModManager.runProcess(pb);
				returncode = pr.getReturnCode();
				ModManager.debugLogger.writeMessage("Return code for process was 0: " + (returncode == 0));
				result = (returncode == 0) && result;
			}

			//ADD FILE TASK
			if (job.getFilesToAdd().size() > 0) {
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.getCommandLineToolsDir() + "SFARTools-Inject.exe");
				commandBuilder.add("--sfarpath");
				String sfarName = "Default.sfar";
				if (job.TESTPATCH) {
					sfarName = "Patch_001.sfar";
				}
				File sfarFile = new File(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName);
				if (!sfarFile.exists()) {
					//missing module
					ModManager.debugLogger.writeMessage("Missing DLC Module, skipping: " + sfarFile.getAbsolutePath());
					publish("DLC is missing: " + job.getDLCFilePath());
					return true;
				}
				commandBuilder.add(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName);
				commandBuilder.add("--addfiles");
				commandBuilder.add("--files");
				ArrayList<String> filesToAdd = job.getFilesToAdd();
				ArrayList<String> filesToAddTargets = job.getFilesToAddTargets();
				ModManager.debugLogger.writeMessage("Number of files to add: " + filesToAdd.size());

				publish("Adding " + filesToAdd.size() + " files to " + job.getJobName());
				for (int i = 0; i < filesToAdd.size(); i++) {
					commandBuilder.add(filesToAddTargets.get(i));
					commandBuilder.add(filesToAdd.get(i));
				}

				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("[" + job.getJobName() + "] Executing injection");
				int returncode = 1;
				ProcessBuilder pb = new ProcessBuilder(command);
				ModManager.debugLogger.writeMessage("Executing process for SFAR File Injection (Adding files).");
				ProcessResult pr = ModManager.runProcess(pb, job.getJobName());
				returncode = pr.getReturnCode();
				ModManager.debugLogger.writeMessage("ProcessSFARJob ADD FILES returned 0: " + (returncode == 0));
				result = (returncode == 0) && result;
			}

			//REMOVE FILE TASK
			if (job.getFilesToRemoveTargets().size() > 0) {
				ArrayList<String> commandBuilder = new ArrayList<String>();
				commandBuilder.add(ModManager.getCommandLineToolsDir() + "SFARTools-Inject.exe");
				commandBuilder.add("--sfarpath");
				String sfarName = "Default.sfar";
				if (job.TESTPATCH) {
					sfarName = "Patch_001.sfar";
				}
				File sfarFile = new File(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName);
				if (!sfarFile.exists()) {
					//missing module
					ModManager.debugLogger.writeMessage("Missing DLC Module, skipping: " + sfarFile.getAbsolutePath());
					publish("DLC is missing: " + job.getDLCFilePath());
					return true;
				}
				commandBuilder.add(ModManager.appendSlash(bioGameDir) + ModManager.appendSlash(job.getDLCFilePath()) + sfarName);
				commandBuilder.add("--deletefiles");
				commandBuilder.add("--files");
				ArrayList<String> filesToRemove = job.getFilesToRemoveTargets();
				ModManager.debugLogger.writeMessage("Number of files to remove: " + filesToRemove.size());

				publish("Removing " + filesToRemove.size() + " files from " + job.getJobName() + ", this may take some time...");
				for (int i = 0; i < filesToRemove.size(); i++) {
					commandBuilder.add(filesToRemove.get(i));
				}

				String[] command = commandBuilder.toArray(new String[commandBuilder.size()]);
				StringBuilder sb = new StringBuilder();
				for (String arg : command) {
					sb.append(arg + " ");
				}
				ModManager.debugLogger.writeMessage("Executing removal command: " + sb.toString());
				int returncode = 1;
				ProcessBuilder pb = new ProcessBuilder(command);
				returncode = ModManager.runProcess(pb).getReturnCode();
				result = returncode == 0 && result;
			}
			return result;
		}

		/**
		 * Copies the CUSTOMDLC folder to the specified directory
		 * 
		 * @param job
		 *            job describing the customDLC job
		 * @return true if successful, false otherwise.
		 */
		private boolean processCustomDLCJob(ModJob job) {
			ModManager.debugLogger.writeMessage("===Processing a customdlc job===");
			ArrayList<String> destfolders = job.getDestFolders();
			File dlcdir = new File(ModManager.appendSlash(bioGameDir) + "DLC" + File.separator);
			for (String folder : destfolders) {
				File dlcFolder = new File(dlcdir + File.separator + folder);
				if (dlcFolder.exists() && dlcFolder.isDirectory()) {
					ModManager.debugLogger.writeMessage("[CUSTOMDLC JOB]Deleting existing CustomDLC folder: " + dlcFolder);
					FileUtils.deleteQuietly(dlcFolder);
				}
			}
			taskSteps = job.getFilesToReplaceTargets().size();
			completedTaskSteps = 0;
			ModManager.debugLogger.writeMessage("[CUSTOMDLC JOB]Number of files to install: " + taskSteps);
			for (int i = 0; i < job.getFilesToReplaceTargets().size(); i++) {
				String target = job.getFilesToReplaceTargets().get(i);
				String fileDestination = dlcdir + target;
				String fileSource = job.getFilesToReplace().get(i);
				// install file.
				try {
					publish(ModType.CUSTOMDLC + ": Installing " + FilenameUtils.getName(fileSource));
					Path sourcePath = Paths.get(fileSource);
					Path destPath = Paths.get(fileDestination);
					File dest = new File(fileDestination);
					dest.mkdirs();
					Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
					completedTaskSteps++;
					ModManager.debugLogger.writeMessage("[CUSTOMDLC JOB]Installed mod file: " + fileSource + " => " + fileDestination);

				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("[CUSTOMDLC JOB]Installing custom dlc file failed:", e);
					return false;
				}
			}
			//autotoc if necessary, create metadata file
			for (String str : job.getDestFolders()) {
				try {
					String metadatapath = dlcdir + File.separator + str + File.separator + CUSTOMDLC_METADATA_FILE;
					ModManager.debugLogger.writeMessage("[CUSTOMDLC JOB]Writing custom DLC metadata file: " + metadatapath);
					FileUtils.writeStringToFile(new File(metadatapath), mod.getModName() + " " + mod.getVersion());
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("[CUSTOMDLC JOB]Couldn't write custom dlc metadata file:", e);
				}
			}
			return true;
		}

		@Override
		protected void process(List<String> updates) {
			for (String update : updates) {
				if (numjobs != 0) {
					int fullJobCompletion = (int) (((double) completed.get() / numjobs) * 100);
					if (taskSteps != 0) {
						fullJobCompletion += (int) (((completedTaskSteps / taskSteps) * 100) / numjobs);
					}
					progressBar.setValue(fullJobCompletion);
				}
				try {
					Double.parseDouble(update); // see if we got a number. if we
					// did that means we should
					// update the bar
				} catch (NumberFormatException e) {
					// this is not a progress update, it's a string update
					addToQueue(update);
				}
			}

		}

		@Override
		protected void done() {
			boolean success = false;
			boolean hasException = false;
			try {
				success = get();
			} catch (InterruptedException e) {
				ModManager.debugLogger.writeException(e);
				hasException = true;
			} catch (ExecutionException e) {
				ModManager.debugLogger.writeException(e);
				hasException = true;
			}

			if (outdatedinstalledfolders != null && outdatedinstalledfolders.size() > 0) {
				for (String outdated : outdatedinstalledfolders) {
					int result = JOptionPane.showConfirmDialog(ModInstallWindow.this,
							"<html><div style='width: 400px'>" + mod.getModName() + "'s mod descriptor indicates that the currently installed CustomDLC " + outdated + "("
									+ ME3TweaksUtils.getThirdPartyModName(outdated)
									+ ") is not compatible/no longer necessary for this mod. The mod indicates they should be deleted as they may conflict with this mod.<br><br>Delete the Custom DLC folder "
									+ outdated + "?</div></html>",
							"Outdated CustomDLC detected", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						String deleteFolder = bioGameDir + "DLC/" + outdated;
						ModManager.debugLogger.writeMessage("Deleting outdated custom DLC: " + deleteFolder);
						boolean deleted = false;//FileUtils.deleteQuietly(new File(deleteFolder));
						if (deleted) {
							ModManager.debugLogger.writeMessage("Deleted outdated custom DLC: " + deleteFolder);
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Deleted " + outdated);
						} else {
							ModManager.debugLogger.writeError("deleteQuietly() returned false when attempting to delete outdated custom DLC: " + deleteFolder);
							ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Failed to " + outdated);
							JOptionPane.showMessageDialog(ModInstallWindow.this, "Failed to delete custom DLC folder:\n" + deleteFolder, "Outdated Custom DLC deletion failed",
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						ModManager.debugLogger.writeMessage("User declined deleting outdated DLC: " + outdated);
					}
				}
			} else {
				ModManager.debugLogger.writeMessage("No outdated custom dlc to remove, continuing install...");
			}

			if (!skipTOC) {
				ModManager.debugLogger.writeMessage("Running Game-Wide AutoTOC after mod install");
				new AutoTocWindow(bioGameDir);
			}

			if (success) {
				if (numjobs != completed.get()) {
					// failed something
					StringBuilder sb = new StringBuilder();
					sb.append(
							"Failed to process mod installation.\nSome parts of the install may have succeeded.\nCheck the log file by copying it to the clipboard in the help menu.");
					callingWindow.labelStatus.setText("Failed to install at least 1 part of mod");
					ModManager.debugLogger.writeMessage(
							mod.getModName() + " failed to fully install. Jobs required to copmlete: " + numjobs + ", while injectioncommander only reported " + completed);
					JOptionPane.showMessageDialog(null, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					// we're good
					callingWindow.labelStatus.setText(" " + mod.getModName() + " installed");
					for (ModJob job : jobs) {
						if (job.getJobType() == ModJob.BALANCE_CHANGES) {
							if (ModManager.checkIfASIBinkBypassIsInstalled(bioGameDir)) {
								if (!ASIModWindow.IsASIModGroupInstalled(5)) { //update group 5 = Balance Changes on ME3Tweaks
									ModManager.debugLogger.writeMessage("Balance changes ASI is not installed. Advertising install");
									int result = JOptionPane.showConfirmDialog(ModInstallWindow.this,
											"This mod contains edits to the balance changes file.\nFor these edits to take effect you need to have the Balance Changes Replacer ASI mod installed.\nOpen the ASI management window to install this?",
											"ASI mod required", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
									if (result == JOptionPane.YES_OPTION) {
										new ASIModWindow(new File(bioGameDir).getParent());
									}
								}
							} else {
								//loader not in
								ModManager.debugLogger.writeMessage("ASI loader not installed. Advertising install");
								int result = JOptionPane.showConfirmDialog(ModInstallWindow.this,
										"This mod contains edits to the balance changes file.\nFor these edits to take effect you need to have the binkw32 ASI loader installed as well as the Balance Changes Replacer ASI.\nOpen the ASI management window to install these?",
										"ASI Loader + ASI mod required", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
								if (result == JOptionPane.YES_OPTION) {
									new ASIModWindow(new File(bioGameDir).getParent());
								}
							}
						}
					}
				}
			} else {
				if (!hasException) {
					ModManager.debugLogger.writeMessage("Installation canceled by user because game repair database update is required (or connection failed and auto canceled.");
					if (bghDB != null) {
						bghDB.shutdownDB();
						bghDB = null;
					}
					System.gc();//force shutdown the old DB

					callingWindow.labelStatus.setText("Mod install canceled");
					dispose();
					if (!failedLoadingDB) {
						File bgdir = new File(ModManager.appendSlash(bioGameDir));
						String me3dir = ModManager.appendSlash(bgdir.getParent());
						try {
							bghDB = new BasegameHashDB(null, me3dir, true);
						} catch (SQLException e) {
							while (e.getNextException() != null) {
								ModManager.debugLogger.writeErrorWithException("DB FAILED TO LOAD.", e);
								e = e.getNextException();
							}
						}
						if (bghDB == null) {
							//cannot continue
							JOptionPane.showMessageDialog(null,
									"<html>The game repair database failed to load.<br>" + "Only one connection to the local repair database is allowed at a time.<br>"
											+ "Please make sure you only have one instance of Mod Manager running.<br>Mod Manager appears as Java (TM) Platform Binary (or javaw.exe on Windows Vista/7) in Task Manager.<br><br>If the issue persists and you are sure only one instance is running, close Mod Manager and<br>delete the the data\\databases folder.<br>You will need to re-create the game repair database afterwards.<br><br>If this *STILL* does not fix your issue, please send a log to FemShep through the help menu.</html>",
									"Database Failure", JOptionPane.ERROR_MESSAGE);

						}
					}
				} else {
					ModManager.debugLogger.writeError("Mod Injection thread encountered an error. See the exception above.");
				}
			}
			finishInstall();
			return;
		}

		protected void finishInstall() {
			ModManager.debugLogger.writeMessage("=========Finished installing " + mod.getModName() + "==========");
			dispose();
		}
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
			if (i < consoleQueue.length - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private void updateInfo() {
		consoleArea.setText(getConsoleString());
	}
}
