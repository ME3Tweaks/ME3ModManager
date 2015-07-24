package com.me3tweaks.modmanager.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.AutoTocWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.ResourceUtils;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;

/**
 * Patch class describes a patch file with metadata about the patch. It's
 * similar to the Mod class.
 * 
 * @author mgamerz
 *
 */
public class Patch {
	String targetPath, targetModule;
	boolean isValid = false;

	String patchName, patchDescription, patchFolderPath;
	long targetSize;
	double patchVersion, patchCMMVer;

	public Patch(String descriptorPath) {
		ModManager.debugLogger.writeMessage("Loading patch: " + descriptorPath);
		readPatch(descriptorPath);
	}

	private void readPatch(String path) {
		File patchDescIni = new File(path);
		if (!patchDescIni.exists()) {
			isValid = false;
			ModManager.debugLogger.writeError("Patch descriptor does not exist: " + patchDescIni.getAbsolutePath());
			return;
		}
		Wini patchini;
		try {
			patchini = new Wini(patchDescIni);

			patchFolderPath = ModManager.appendSlash(patchDescIni.getParent());
			patchDescription = patchini.get("PatchInfo", "patchdesc");
			patchName = patchini.get("PatchInfo", "patchname");
			ModManager.debugLogger.writeMessage("------------------Reading Patch" + patchName + "------------------");
			ModManager.debugLogger.writeMessage("Patch Folder: " + patchFolderPath);
			ModManager.debugLogger.writeMessage("Patch Name: " + patchName);
			ModManager.debugLogger.writeMessage("Patch Description: " + patchDescription);
			// Check if this mod has been made for Mod Manager 2.0 or legacy mode
			patchCMMVer = 3.2f;
			patchVersion = 1;
			try {
				patchCMMVer = Float.parseFloat(patchini.get("ModManager", "cmmver"));
				ModManager.debugLogger.writeMessage("Patch Targets Mod Manager: " + patchCMMVer);
				patchVersion = Float.parseFloat(patchini.get("PatchInfo", "patchver"));
				patchVersion = (double) Math.round(patchVersion * 10) / 10; //tenth rounding
				ModManager.debugLogger.writeMessage("Patch Version: " + patchVersion);
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessage("Didn't read a target version (cmmver) in the descriptor file. Targetting 3.2.");
				patchCMMVer = 3.2f;
				ModManager.debugLogger.writeException(e);
			}

			targetModule = patchini.get("PatchInfo", "targetmodule");
			targetPath = patchini.get("PatchInfo", "targetfile");
			targetSize = Long.parseLong(patchini.get("PatchInfo", "targetsize"));
			ModManager.debugLogger.writeMessage("Patch Targets Module: " + targetModule);
			ModManager.debugLogger.writeMessage("Patch Targets File in module: " + targetPath);
			ModManager.debugLogger.writeMessage("Patch only works with files of size: " + targetSize);

			if (targetPath == null || targetModule == null || targetPath.equals("") || targetModule.equals("")) {
				ModManager.debugLogger.writeMessage("Invalid patch, targetfile or targetmodule was empty or missing");
				isValid = false;
			} else if (targetSize <= 0) {
				ModManager.debugLogger.writeMessage("Invalid patch, target size of file to patch has to be bigger than 0");
				isValid = false;
			} else if (targetPath.endsWith("Coalesced.bin")) {
				ModManager.debugLogger.writeMessage("Invalid patch, patches do not work with Coalesced.bin");
				isValid = false;
			} else {
				isValid = true;
			}
			ModManager.debugLogger.writeMessage("Finished loading patchdesc.ini for this patch.");
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeException(e);
			isValid = false;
		} catch (IOException e) {
			ModManager.debugLogger.writeException(e);
			isValid = false;
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeException(e);
			isValid = false;
		}
		ModManager.debugLogger.writeMessage("--------------------------END OF " + patchName + "--------------------------");
	}

	/**
	 * Moves this patch into the data/patches directory
	 * 
	 * @return True if successful, false otherwise
	 */
	public boolean importPatch() {
		ModManager.debugLogger.writeMessage("Importing patch to library");
		String patchDirPath = ModManager.getPatchesDir() + "patches/";
		File patchDir = new File(patchDirPath);
		patchDir.mkdirs();

		String destinationDir = patchDirPath + getPatchName();
		File destDir = new File(destinationDir);
		if (destDir.exists()) {
			System.err.println("Destination directory already exists.");
			return false;
		}
		try {
			ModManager.debugLogger.writeMessage("Moving patch to library");
			FileUtils.moveDirectory(new File(patchFolderPath), destDir);
			ModManager.debugLogger.writeMessage("Patch migrated to library");
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Failed to import patch:", e);
			return false;
		}
		return true;
	}

	/**
	 * Applies this patch. Inserts itself as a task in the specified mod.
	 * 
	 * @param mod
	 *            Mod to apply with
	 * @return True if successful, false otherwise
	 */
	public boolean applyPatch(Mod mod) {
		//We must check if the mod we are applying to already has this file. If it does we will apply to that mod.
		//If it does not we will add new task for it.
		//If the files are not the right size we will not apply.
		boolean requiresreload = false;
		ModManager.debugLogger.writeMessage("=============APPLY PATCH " + getPatchName() + "=============");
		try {

			File jpatch = new File(ModManager.getToolsDir() + "jptch.exe");
			if (!jpatch.exists()) {
				ME3TweaksUtils.downloadJDiffTools();
			}

			if (!ModManager.hasPristineTOC(targetModule, ME3TweaksUtils.HEADER)) {
				ME3TweaksUtils.downloadPristineTOC(targetModule, ME3TweaksUtils.HEADER);
			}

			//Prepare mod
			String modSourceFile = mod.getModTaskPath(targetPath, targetModule);
			if (modSourceFile == null) {
				requiresreload = true;
				ModManager.debugLogger.writeMessage(mod.getModName() + " does not appear to modify " + targetPath + " in module " + targetModule + ", performing file fetch");
				//we need to check if its in the patch library's source folder
				modSourceFile = ModManager.getPatchSource(targetPath, targetModule);

				if (modSourceFile == null) {
					//couldn't copy or extract file, have nothing we can patch
					ModManager.debugLogger.writeMessage(mod.getModName() + "'s patch " + getPatchName() + " was not able to acquire a source file to patch.");
					return false;
				}

				//copy sourcefile to mod dir
				File libraryFile = new File(modSourceFile);
				if (libraryFile.length() != targetSize) {
					ModManager.debugLogger.writeError("File that is going to be patched does not match patch descriptor size! Unable to apply patch");
					return false;
				}
				//File modFile = new File(ModManager.appendSlash(mod.getModPath())+Mod.getStandardFolderName(targetModule)+File.separator+FilenameUtils.getName(targetPath));
				//FileUtils.copyFile(libraryFile, modFile);

				//we need to add a task for this
				ModJob targetJob = null;
				String standardFolder = ModManager.appendSlash(Mod.getStandardFolderName(targetModule));
				String filename = FilenameUtils.getName(targetPath);
				for (ModJob job : mod.jobs) {
					if (job.getJobName().equals(targetModule)) {
						ModManager.debugLogger.writeError("Adding file to existing job: " + targetModule);
						targetJob = job;
						String jobFolder = ModManager.appendSlash(new File(job.getNewFiles()[0]).getParentFile().getAbsolutePath());
						String relativepath = ModManager.appendSlash(ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator));
						System.out.println(relativepath);
						File modFilePath = new File(ModManager.appendSlash(mod.getModPath()) + relativepath + filename);
						FileUtils.copyFile(new File(ModManager.getPristineTOC(targetModule, ME3TweaksUtils.HEADER)), modFilePath);

						job.addFileReplace(modFilePath.getAbsolutePath(), targetPath);
						break;
					}
				}

				if (targetJob == null) {
					ModManager.debugLogger.writeError("Creating new job: " + targetModule);
					//no job for the module this task needs
					//we need to add it as a new task and then add add a PCConsoleTOC for it
					double newCmmVer = Math.max(mod.modCMMVer, 3.2);
					ModJob job;
					if (targetModule.equals(ModType.BASEGAME)) {
						job = new ModJob();
					} else {
						job = new ModJob(ModType.getDLCPath(targetModule), targetModule);
					}
					File modulefolder = new File(ModManager.appendSlash(mod.getModPath() + standardFolder));
					modulefolder.mkdirs();
					ModManager.debugLogger.writeMessage("Adding PCConsoleTOC.bin to new job");
					File tocSource = new File(ModManager.getPristineTOC(targetModule, ME3TweaksUtils.HEADER));
					File tocDest = new File(modulefolder + File.separator + "PCConsoleTOC.bin");
					FileUtils.copyFile(tocSource, tocDest);
					job.addFileReplace(tocSource.getAbsolutePath(), ME3TweaksUtils.coalFileNameToDLCTOCDir(ME3TweaksUtils.headerNameToCoalFilename(targetModule)));

					ModManager.debugLogger.writeMessage("Adding " + filename + " to new job");
					File modFile = new File(modulefolder + File.separator + filename);
					FileUtils.copyFile(libraryFile, modFile);
					job.addFileReplace(modFile.getAbsolutePath(), targetPath);
					mod.addTask(targetModule, job);
					mod.modCMMVer = newCmmVer;
				}

				//write new moddesc.ini file
				String descini = mod.createModDescIni(mod.modCMMVer);
				ModManager.debugLogger.writeMessage("Updating moddesc.ini with updated job");
				FileUtils.writeStringToFile(mod.modDescFile, descini);

				//reload mod in staging with new job added
				ModManager.debugLogger.writeMessage("Reloading updated mod with new moddesc.ini file");
				mod = new Mod(mod.modDescFile.getAbsolutePath());
				new AutoTocWindow(mod);
				modSourceFile = mod.getModTaskPath(targetPath, targetModule);
			}
			//rename file (so patch doesn't continuously recalculate itself)
			File stagingFile = new File(ModManager.getTempDir()+"patch_staging"); //this file is used as base, and then patch puts file back in original place
			stagingFile.delete();
			FileUtils.moveFile(new File(modSourceFile), stagingFile);
			
			//apply patch
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.getToolsDir() + "jptch.exe");
			commandBuilder.add(stagingFile.getAbsolutePath());
			commandBuilder.add(getPatchFolderPath() + "patch.jsf");
			commandBuilder.add(modSourceFile);
			StringBuilder sb = new StringBuilder();
			for (String arg : commandBuilder) {
				sb.append("\""+arg + "\" ");
			}

			ModManager.debugLogger.writeMessage("Executing JPATCH patch command: " + sb.toString());

			ProcessBuilder patchProcessBuilder = new ProcessBuilder(commandBuilder);
			//patchProcessBuilder.redirectErrorStream(true);
			//patchProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process patchProcess = patchProcessBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(patchProcess.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			    System.out.println("tasklist: " + line);
			patchProcess.waitFor();
			stagingFile.delete();
			ModManager.debugLogger.writeMessage("File has been patched.");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeErrorWithException("IOException applying mod:", e);
			return false;
		} catch (InterruptedException e) {
			ModManager.debugLogger.writeErrorWithException("Patching process was interrupted:", e);
			return false;
		}
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getTargetModule() {
		return targetModule;
	}

	public void setTargetModule(String targetModule) {
		this.targetModule = targetModule;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getPatchName() {
		return patchName;
	}

	public void setPatchName(String patchName) {
		this.patchName = patchName;
	}

	public String getPatchDescription() {
		return patchDescription;
	}

	public void setPatchDescription(String patchDescription) {
		this.patchDescription = patchDescription;
	}

	public String getPatchFolderPath() {
		return patchFolderPath;
	}

	public void setPatchFolderPath(String patchFolderPath) {
		this.patchFolderPath = patchFolderPath;
	}

	public long getTargetSize() {
		return targetSize;
	}

	public void setTargetSize(long targetSize) {
		this.targetSize = targetSize;
	}

	public double getPatchVersion() {
		return patchVersion;
	}

	public void setPatchVersion(double patchVersion) {
		this.patchVersion = patchVersion;
	}

	public double getPatchCMMVer() {
		return patchCMMVer;
	}

	public void setPatchCMMVer(double patchCMMVer) {
		this.patchCMMVer = patchCMMVer;
	}
}
