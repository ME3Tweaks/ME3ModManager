package com.me3tweaks.modmanager.objects;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Patch class describes a patch file with metadata about the patch. It's
 * similar to the Mod class.
 * 
 * @author mgamerz
 *
 */
public class Patch implements Comparable<Patch> {
	public final Object lock = new Lock(); //threading wait() and notifyall();

	public static final int APPLY_SUCCESS = 0;
	public static final int APPLY_FAILED_OTHERERROR = -1;
	public static final int APPLY_FAILED_MODDESC_NOT_UPDATED = 1;
	public static final int APPLY_FAILED_SOURCE_FILE_WRONG_SIZE = 2;
	public static final int APPLY_FAILED_NO_SOURCE_FILE = 3;
	public static final int APPLY_FAILED_SIZE_CHANGED = 4;
	String targetPath, targetModule;
	private String patchPath;
	boolean isValid = false, finalizer = false;

	String patchName, patchDescription, patchFolderPath;
	long targetSize;
	double patchVersion, patchCMMVer;
	private String patchAuthor;
	private int me3tweaksid;
	private boolean isDynamic = false;

	public Patch(String descriptorPath, String patchPath) {
		ModManager.debugLogger.writeMessageConditionally("Loading patch: " + descriptorPath, ModManager.LOG_PATCH_INIT);
		readPatch(descriptorPath);
		setPatchPath(patchPath);
	}

	/**
	 * Copy constructor
	 * 
	 * @param patch
	 *            patch to copy
	 */
	public Patch(Patch patch) {
		targetPath = patch.targetPath;
		targetModule = patch.targetModule;
		setPatchPath(patch.getPatchPath());
		isValid = patch.isValid;
		finalizer = patch.finalizer;
		patchName = patch.patchName;
		patchDescription = patch.patchDescription;
		patchFolderPath = patch.patchFolderPath;
		targetSize = patch.targetSize;
		patchVersion = patch.patchVersion;
		patchCMMVer = patch.patchCMMVer;
		setPatchAuthor(patch.getPatchAuthor());
		setMe3tweaksid(patch.getMe3tweaksid());
	}

	/**
	 * Empty constructor, only use if you are planning to manually add all
	 * required fields.
	 */
	public Patch() {

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
			try {
				String idstr = patchini.get("PatchInfo", "me3tweaksid");
				setMe3tweaksid(Integer.parseInt(idstr));
				ModManager.debugLogger.writeMessageConditionally("Patch ID on ME3Tweaks: " + getMe3tweaksid(), ModManager.LOG_PATCH_INIT);
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeError("me3tweaksid is not an integer, setting to 0");
			}

			ModManager.debugLogger.writeMessageConditionally("------PATCH--------------Reading Patch " + patchName + "-----------------", ModManager.LOG_PATCH_INIT);
			File patchFile = new File(patchFolderPath + "patch.jsf");
			if (!patchFile.exists()) {
				ModManager.debugLogger.writeError("Patch.jsf is missing, patch is invalid");
				ModManager.debugLogger.writeMessageConditionally("------PATCH--------------End of " + patchName + "-----------------", ModManager.LOG_PATCH_INIT);
				isValid = false;
				return;
			}
			patchPath = patchFile.getAbsolutePath();

			ModManager.debugLogger.writeMessageConditionally("Patch Folder: " + patchFolderPath, ModManager.LOG_PATCH_INIT);
			ModManager.debugLogger.writeMessageConditionally("Patch Name: " + patchName, ModManager.LOG_PATCH_INIT);
			ModManager.debugLogger.writeMessageConditionally("Patch Description: " + patchDescription, ModManager.LOG_PATCH_INIT);
			// Check if this mod has been made for Mod Manager 2.0 or legacy mode
			patchCMMVer = 3.2f;
			patchVersion = 1;
			try {
				patchCMMVer = Float.parseFloat(patchini.get("ModManager", "cmmver"));
				patchCMMVer = (double) Math.round(patchCMMVer * 10) / 10; //tenth rounding;
				ModManager.debugLogger.writeMessageConditionally("Patch Targets Mod Manager: " + patchCMMVer, ModManager.LOG_PATCH_INIT);
				setPatchAuthor(patchini.get("PatchInfo", "patchdev"));
				ModManager.debugLogger.writeMessageConditionally("Patch Developer (if any) " + getPatchAuthor(), ModManager.LOG_PATCH_INIT);
				String strPatchVersion = patchini.get("PatchInfo", "patchver");
				if (strPatchVersion != null) {
					patchVersion = Float.parseFloat(strPatchVersion);
					patchVersion = (double) Math.round(patchVersion * 10) / 10; //tenth rounding
					ModManager.debugLogger.writeMessageConditionally("Patch Version: " + patchVersion, ModManager.LOG_PATCH_INIT);
				} else {
					patchVersion = 1.0;
					ModManager.debugLogger.writeMessageConditionally("Patch Version: Not specified, defaulting to 1.0", ModManager.LOG_PATCH_INIT);
				}
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessageConditionally("Didn't read a target version (cmmver) in the descriptor file. Targetting 4.0.", ModManager.LOG_PATCH_INIT);
				patchCMMVer = 3.2f;
				ModManager.debugLogger.writeException(e);
			}

			String finalizerStr = patchini.get("PatchInfo", "finalizer");
			if (finalizerStr != null && finalizerStr.toLowerCase().equals("true")) {
				finalizer = true;
				ModManager.debugLogger.writeMessageConditionally("Patch is marked as finalizer", ModManager.LOG_PATCH_INIT);
			}

			targetModule = patchini.get("PatchInfo", "targetmodule");
			targetPath = patchini.get("PatchInfo", "targetfile");
			targetSize = Long.parseLong(patchini.get("PatchInfo", "targetsize"));
			ModManager.debugLogger.writeMessageConditionally("Patch Targets Module: " + targetModule, ModManager.LOG_PATCH_INIT);
			ModManager.debugLogger.writeMessageConditionally("Patch Targets File in module: " + targetPath, ModManager.LOG_PATCH_INIT);
			ModManager.debugLogger.writeMessageConditionally("Patch only works with files of size: " + targetSize, ModManager.LOG_PATCH_INIT);

			if (targetPath == null || targetModule == null || targetPath.equals("") || targetModule.equals("")) {
				ModManager.debugLogger.writeMessageConditionally("Invalid patch, targetfile or targetmodule was empty or missing", ModManager.LOG_PATCH_INIT);
				isValid = false;
			} else if (targetSize <= 0) {
				ModManager.debugLogger.writeMessageConditionally("Invalid patch, target size of file to patch has to be bigger than 0", ModManager.LOG_PATCH_INIT);
				isValid = false;
			} else if (targetPath.endsWith("Coalesced.bin")) {
				ModManager.debugLogger.writeError("Invalid patch, patches do not work with Coalesced.bin");
				isValid = false;
			} else {
				isValid = true;
			}
			ModManager.debugLogger.writeMessageConditionally("Finished loading patchdesc.ini for " + getPatchName(), ModManager.LOG_PATCH_INIT);
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
		ModManager.debugLogger.writeMessageConditionally("------PATCH--------------END OF " + patchName + "-------------------------", ModManager.LOG_PATCH_INIT);
	}

	public boolean isFinalizer() {
		return finalizer;
	}

	public void setFinalizer(boolean finalizer) {
		this.finalizer = finalizer;
	}

	/**
	 * Moves this patch into the data/patches directory
	 * 
	 * @return new patch object if successful, null otherwise
	 */
	public Patch importPatch() {
		ModManager.debugLogger.writeMessage("Importing patch to library");
		String patchDirPath = ModManager.getPatchesDir() + "patches/";
		File patchDir = new File(patchDirPath);
		patchDir.mkdirs();

		String destinationDir = patchDirPath + getPatchName();
		File destDir = new File(destinationDir);
		if (destDir.exists()) {
			ModManager.debugLogger.writeError("Cannot import patch: Destination directory already exists (patch with same name already exists in the patches folder)");
			return null;
		}
		try {
			ModManager.debugLogger.writeMessage("Moving patch to library");
			FileUtils.moveDirectory(new File(patchFolderPath), destDir);
			ModManager.debugLogger.writeMessage("Patch migrated to library");
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("Failed to import patch:", e);
			return null;
		}
		ModManager.debugLogger.writeMessage("Reloading imported patch");
		return new Patch(destinationDir + File.separator + "patchdesc.ini", destinationDir + File.separator + "patch.jsf");
	}

	/**
	 * Gets the source file that would be used if this patch was applied to the
	 * specified mod
	 * 
	 * @param mod
	 * @return null if no source (error), path otherwise
	 */
	public String getSourceFilePath(Mod mod) {
		String modSourceFile = mod.getModTaskPath(targetPath, targetModule);
		if (modSourceFile == null) {
			ModManager.debugLogger.writeMessage(mod.getModName() + " does not appear to modify " + targetPath + " in module " + targetModule + ", performing file fetch");
			//we need to check if its in the patch library's source folder
			modSourceFile = ModManager.getPatchSource(targetPath, targetModule);
			File mf = new File(modSourceFile);
			if (mf.length() != targetSize) {
				ModManager.debugLogger.writeError("Fetched file is the wrong size! Need: " + targetSize + " but we got " + mf.length());
				return null;
			}
			return modSourceFile;
		} else {
			File mf = new File(modSourceFile);
			if (mf.length() != targetSize) {
				ModManager.debugLogger.writeError("Fetched file is the wrong size! Need: " + targetSize + " but we got " + mf.length());
				return null;
			}
			return modSourceFile;
		}
	}

	/**
	 * Applies this patch. Inserts itself as a task in the specified mod.
	 * 
	 * @param mod
	 *            Mod to apply with
	 * @return APPLY_SUCCESS if successful, otherwise other constants if failed.
	 */
	public int applyPatch(Mod mod) {
		//We must check if the mod we are applying to already has this file. If it does we will apply to that mod.
		//If it does not we will add new task for it.
		//If the files are not the right size we will not apply.
		ModManager.debugLogger.writeMessage("=============APPLY PATCH " + getPatchName() + "=============");
		try {
			if (!ModManager.hasPristineTOC(targetModule, ME3TweaksUtils.HEADER)) {
				ME3TweaksUtils.downloadPristineTOC(targetModule, ME3TweaksUtils.HEADER);
			}

			//Prepare mod
			String modSourceFile = mod.getModTaskPath(targetPath, targetModule);
			if (modSourceFile == null) {
				ModManager.debugLogger.writeMessage(mod.getModName() + " does not appear to modify " + targetPath + " in module " + targetModule + ", performing file fetch");
				//we need to check if its in the patch library's source folder
				modSourceFile = ModManager.getPatchSource(targetPath, targetModule);

				if (modSourceFile == null) {
					//couldn't copy or extract file, have nothing we can patch
					ModManager.debugLogger.writeMessage("Unable to acquire file using original path. Attempting to pull from backup.");
					modSourceFile = ModManager.getBackupPatchSource(targetPath, targetModule);
					if (modSourceFile == null) {
						ModManager.debugLogger.writeMessage(mod.getModName() + "'s patch " + getPatchName() + " was not able to acquire a source file to patch.");
						return APPLY_FAILED_NO_SOURCE_FILE;
					}
				}

				//copy sourcefile to mod dir
				File libraryFile = new File(modSourceFile);
				if (libraryFile.length() != targetSize) {
					libraryFile.delete();
					ModManager.debugLogger.writeMessage("Initial file fetch file is not the correct size - library file deleted. Attempting lookup via cmmbackup");
					//Check if this is backed up - we might be able to pull a backup file instead
					modSourceFile = ModManager.getBackupPatchSource(targetPath, targetModule);
					if (modSourceFile != null) {
						libraryFile = new File(modSourceFile);
						if (libraryFile.length() != targetSize) {
							ModManager.debugLogger.writeError("Backup file that was fetched does not match patch descriptor size (" + libraryFile.length()
									+ " vs one can be applied to: " + targetSize + ")! Unable to apply patch");
							return APPLY_FAILED_SOURCE_FILE_WRONG_SIZE;
						}
					} else {
						ModManager.debugLogger.writeError("No file that was the correct size could be used for patching.");
						return APPLY_FAILED_SOURCE_FILE_WRONG_SIZE;
					}
				}

				File modFile = new File(ModManager.appendSlash(mod.getModPath()) + Mod.getStandardFolderName(targetModule) + File.separator + FilenameUtils.getName(targetPath));
				ModManager.debugLogger.writeMessage("Copying libary file to mod package: " + libraryFile.getAbsolutePath() + " => " + modFile.getAbsolutePath());
				FileUtils.copyFile(libraryFile, modFile);

				//we need to add a task for this, lookup if job exists already
				ModJob targetJob = null;
				String standardFolder = ModManager.appendSlash(Mod.getStandardFolderName(targetModule));
				String filename = FilenameUtils.getName(targetPath);
				for (ModJob job : mod.jobs) {
					if (job.getJobName().equals(targetModule)) {
						ModManager.debugLogger.writeMessage("Checking existing job: " + targetModule);
						targetJob = job;
						String jobFolder = null;
						if (job.getFilesToReplace().size() > 0) {
							jobFolder = ModManager.appendSlash(new File(job.getFilesToReplace().get(0)).getParentFile().getAbsolutePath());
						} else if (job.getFilesToAdd().size() > 0) {
							jobFolder = ModManager.appendSlash(new File(job.getFilesToAdd().get(0)).getParentFile().getAbsolutePath());
						} else {
							jobFolder = ModManager.appendSlash(mod.getModPath() + targetModule);
						}
						String relativepath = ModManager.appendSlash(ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator));

						//ADD PATCH FILE TO JOB
						File modFilePath = new File(ModManager.appendSlash(mod.getModPath()) + relativepath + filename);
						ModManager.debugLogger.writeMessage("Adding new mod task => " + targetModule + ": add " + modFilePath.getAbsolutePath());
						job.addFileReplace(modFilePath.getAbsolutePath(), targetPath, false);

						//CHECK IF JOB HAS TOC - SOME MIGHT NOT, FOR SOME WEIRD REASON
						//copy toc
						File tocFile = new File(mod.getModPath() + relativepath + "PCConsoleTOC.bin");
						if (!tocFile.exists()) {
							FileUtils.copyFile(new File(ModManager.getPristineTOC(targetModule, ME3TweaksUtils.HEADER)), tocFile);
						} else {
							ModManager.debugLogger.writeMessage("Toc file already exists in module: " + targetModule);
						}
						//add toc to jobs
						String tocTask = mod.getModTaskPath(ME3TweaksUtils.coalFileNameToDLCTOCDir(ME3TweaksUtils.headerNameToCoalFilename(targetModule)), targetModule);
						if (tocTask == null) {
							//add toc replacejob
							job.addFileReplace(tocFile.getAbsolutePath(), targetPath, false);
						}
						break;
					}
				}

				if (targetJob == null) {
					ModManager.debugLogger.writeMessage("Creating new job: " + targetModule);
					//no job for the module this task needs
					//we need to add it as a new task and then add add a PCConsoleTOC for it
					double newCmmVer = Math.max(mod.modCMMVer, 3.2);
					ModJob job;
					if (targetModule.equals(ModType.BASEGAME)) {
						job = new ModJob();
					} else {
						job = new ModJob(ModType.getDLCPath(targetModule), targetModule, null);
					}
					job.setOwningMod(mod);

					File modulefolder = new File(ModManager.appendSlash(mod.getModPath() + standardFolder));
					modulefolder.mkdirs();
					ModManager.debugLogger.writeMessage("Adding PCConsoleTOC.bin to new job");
					File tocSource = new File(ModManager.getPristineTOC(targetModule, ME3TweaksUtils.HEADER));
					File tocDest = new File(modulefolder + File.separator + "PCConsoleTOC.bin");
					FileUtils.copyFile(tocSource, tocDest);
					job.addFileReplace(tocDest.getAbsolutePath(), ME3TweaksUtils.coalFileNameToDLCTOCDir(ME3TweaksUtils.headerNameToCoalFilename(targetModule)), false);

					ModManager.debugLogger.writeMessage("Adding " + filename + " to new job");
					/*
					 * File modFile = new File(modulefolder + File.separator +
					 * filename); FileUtils.copyFile(libraryFile, modFile);
					 */
					job.addFileReplace(modFile.getAbsolutePath(), targetPath, false);
					mod.addTask(targetModule, job);
					mod.modCMMVer = newCmmVer;
				}

				//write new moddesc.ini file
				String descini = mod.createModDescIni(true, mod.modCMMVer);
				ModManager.debugLogger.writeMessage("Updating moddesc.ini with updated job");
				FileUtils.writeStringToFile(mod.modDescFile, descini);

				//reload mod in staging with new job added
				ModManager.debugLogger.writeMessage("Reloading updated mod with new moddesc.ini file");
				mod = new Mod(mod.modDescFile.getAbsolutePath());
				modSourceFile = mod.getModTaskPath(targetPath, targetModule);
			}
			if (modSourceFile == null) {
				ModManager.debugLogger
						.writeError("Source file should have been copied to mod directory already. ModDesc.ini however is missing a newfiles/replacefiles task in the job.");
				return APPLY_FAILED_MODDESC_NOT_UPDATED;
			}
			File sourceFile = new File(modSourceFile);
			if (sourceFile.length() != targetSize) {
				ModManager.debugLogger
						.writeError("Source file is the wrong size! This patch only applies to files of size " + targetSize + " but the file is " + sourceFile.length());
				return APPLY_FAILED_SOURCE_FILE_WRONG_SIZE;
			}
			//rename file (so patch doesn't continuously recalculate itself)
			File stagingFile = new File(ModManager.getTempDir() + "patch_staging"); //this file is used as base, and then patch puts file back in original place
			ModManager.debugLogger.writeMessage("Staging source file: " + modSourceFile + " => " + stagingFile.getAbsolutePath());

			stagingFile.delete();
			FileUtils.moveFile(sourceFile, stagingFile);

			//apply patch
			ArrayList<String> commandBuilder = new ArrayList<String>();
			commandBuilder.add(ModManager.getToolsDir() + "jptch.exe");
			commandBuilder.add(stagingFile.getAbsolutePath());
			commandBuilder.add(patchPath);
			commandBuilder.add(modSourceFile);
			StringBuilder sb = new StringBuilder();
			for (String arg : commandBuilder) {
				sb.append("\"" + arg + "\" ");
			}

			ProcessBuilder patchProcessBuilder = new ProcessBuilder(commandBuilder);
			ProcessResult result = ModManager.runProcess(patchProcessBuilder);
			stagingFile.delete();
			if (result.hadError()) {
				ModManager.debugLogger.writeError("Error occured while attempting to apply patch.");
				ModManager.debugLogger.writeException(result.getError());
				return APPLY_FAILED_OTHERERROR;
			}
			if (result.getReturnCode() != 0) {
				return APPLY_FAILED_OTHERERROR;
			}

			File outfile = new File(modSourceFile);
			if (!finalizer && outfile.length() != targetSize) {
				//filesize has changed but this is not a finalizer
				return APPLY_FAILED_SIZE_CHANGED;
			}

			ModManager.debugLogger.writeMessage("File has been patched. Output size is " + sourceFile.length());
			return APPLY_SUCCESS;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeErrorWithException("IOException applying mod:", e);
			return APPLY_FAILED_OTHERERROR;
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

	public String getPatchAuthor() {
		return patchAuthor;
	}

	@Override
	public int compareTo(Patch otherPatch) {
		return getPatchName().compareTo(otherPatch.getPatchName());
	}

	public static String generatePatchDesc(ME3TweaksPatchPackage pack) {
		Wini ini = new Wini();

		// put modmanager, PATCHINFO
		ini.put("ModManager", "cmmver", pack.getTargetversion());
		ini.put("PatchInfo", "patchname", pack.getPatchname());
		ini.put("PatchInfo", "patchdesc", pack.getPatchdesc());
		ini.put("PatchInfo", "patchdev", pack.getPatchdev());
		ini.put("PatchInfo", "patchver", pack.getPatchver());
		ini.put("PatchInfo", "targetmodule", pack.getTargetmodule());
		ini.put("PatchInfo", "targetfile", pack.getTargetfile());
		ini.put("PatchInfo", "targetsize", pack.getTargetsize());
		ini.put("PatchInfo", "finalizer", pack.isFinalizer());
		ini.put("PatchInfo", "me3tweaksid", pack.getMe3tweaksid());

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ini.store(os);
			return new String(os.toByteArray(), "ASCII");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String convertToME3TweaksSQLInsert() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO mixinlibrary VALUES (\n\tnull,\n");
		sb.append("\t\"" + patchName + "\",\n");
		sb.append("\t\"" + patchDescription + "\",\n");
		sb.append("\t\"" + ((getPatchAuthor() == null) ? "FemShep" : getPatchAuthor()) + "\",\n");
		sb.append("\t" + patchVersion + ",\n");
		if (patchCMMVer < 4.0) {
			patchCMMVer = 4.0;
		}
		sb.append("\t" + patchCMMVer + ",\n");
		sb.append("\t\"" + targetModule + "\",\n");
		String sqlPath = targetPath.replaceAll("\\\\", "\\\\\\\\");
		sb.append("\t\"" + sqlPath + "\",\n");

		sb.append("\t" + targetSize + ",\n");
		sb.append("\tfalse, /*FINALIZER*/\n");

		String serverfolder = patchName.toLowerCase().replaceAll(" - ", "-").replaceAll(" ", "-");
		sb.append("\t\"https://me3tweaks.com/mixins/library/" + serverfolder + "/patch.jsf\",\n");
		sb.append("\t\"" + patchName + "\",\n");
		sb.append("\tnull\n");
		sb.append(");");
		File copyTo = new File("server/" + serverfolder + "/patch.jsf");
		File dirHeader = copyTo.getParentFile();
		dirHeader.mkdirs();
		return sb.toString();
	}

	public int getMe3tweaksid() {
		return me3tweaksid;
	}

	public void setPatchAuthor(String patchAuthor) {
		this.patchAuthor = patchAuthor;
	}

	public void setMe3tweaksid(int me3tweaksid) {
		this.me3tweaksid = me3tweaksid;
	}

	public String getPatchPath() {
		return patchPath;
	}

	public void setPatchPath(String patchPath) {
		this.patchPath = patchPath;
	}

	public void setIsDynamic(boolean b) {
		this.isDynamic = b;
	}

	public boolean isDynamic() {
		return this.isDynamic;
	}
}
