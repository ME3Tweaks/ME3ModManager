package com.me3tweaks.modmanager.objects;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ResourceUtils;
import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;

/**
 * Patch class describes a patch file with metadata about the patch. It's similar
 * to the Mod class.
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
			patchDescription = patchini.get("PatchInfo", "patchdescription");
			patchName = patchini.get("PatchInfo", "patchname");
			ModManager.debugLogger.writeMessage("------------------Reading Patch" + patchName + "------------------");
			// Check if this mod has been made for Mod Manager 2.0 or legacy mode
			patchCMMVer = 3.2f;
			patchVersion = 1;
			try {
				patchCMMVer = Float.parseFloat(patchini.get("ModManager", "cmmver"));
				ModManager.debugLogger.writeMessage("Patch Targets Mod Manager: "+patchCMMVer);
				patchVersion = Float.parseFloat(patchini.get("PatchInfo", "patchver"));
				ModManager.debugLogger.writeMessage("Patch Version: "+patchVersion);
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessage("Didn't read a target version (cmmver) in the descriptor file. Targetting 3.2.");
				patchCMMVer = 3.2f;
			}
			
			targetModule = patchini.get("PatchInfo", "targetmodule");
			targetPath = patchini.get("PatchInfo", "targetfile");
			targetSize = Long.parseLong(patchini.get("PatchInfo", "targetsize"));
			ModManager.debugLogger.writeMessage("Patch Targets Module: "+targetModule);
			ModManager.debugLogger.writeMessage("Patch Targets File in module: "+targetPath);
			ModManager.debugLogger.writeMessage("Patch only works with files of size: "+targetSize);

						
			if (targetPath == null || targetModule == null || targetPath.equals("") || targetModule.equals("")){
				ModManager.debugLogger.writeMessage("Invalid patch, targetfile or targetmodule was empty or missing");
				isValid = false;
			} else if (targetSize <= 0){
				ModManager.debugLogger.writeMessage("Invalid patch, target size of file to patch has to be bigger than 0");
				isValid = false;
			} else if (targetPath.endsWith("Coalesced.bin")){
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
	 * @return True if successful, false otherwise
	 */
	public boolean importPatch(){
		ModManager.debugLogger.writeMessage("Importing patch to library");
		String patchDirPath = ModManager.getPatchesDir()+"patches/";
		File patchDir = new File(patchDirPath);
		patchDir.mkdirs();
		
		String destinationDir = patchDirPath+getPatchName();
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
	 * @param mod Mod to apply with
	 * @return True if successful, false otherwise
	 */
	public boolean applyPatch(Mod mod) {
		//We must check if the mod we are applying to already has this file. If it does we will apply to that mod.
		//If it does not we will add new task for it.
		//If the files are not the right size we will not apply.
		File jpatch = new File(ModManager.getToolsDir()+"jptch.exe");
		if (!jpatch.exists()) {
			ME3TweaksUtils.downloadJDiffTools();
		}
		
		//Prepare mod
		String modSourceFile = mod.getModTaskPath(targetPath, targetModule);
		if (modSourceFile == null){
			ModManager.debugLogger.writeMessage(mod.getModName()+" does not appear to modify "+targetPath+" in module "+targetModule+", performing file fetch");
			//we need to check if its in the patch library's source folder
			if (!ModManager.hasPristinePatchSource(targetPath,targetModule)){
				ModManager.debugLogger.writeMessage("Fetching source file from game");
				modSourceFile = ModManager.getPatchSource(targetPath, targetModule);
			}
			if (modSourceFile == null) {
				//couldn't copy or extract file, have nothing we can patch
				ModManager.debugLogger.writeMessage(mod.getModName()+"'s patch "+getPatchName()+" was not able to acquire a source file to patch.");
				return false;
			}
			
			//copy sourcefile to mod dir
			File libraryFile = new File(modSourceFile);
			File modFile = new File(ModManager.appendSlash(mod.getModPath())+Mod.getStandardFolderName(targetModule)+File.separator+FilenameUtils.getName(targetPath));
			FileUtils.copyFile(libraryFile, modFile);

			//we need to add a task for this
			ModJob targetJob = null;
			for (ModJob job : mod.jobs) {
				if (job.getJobName().equals(targetModule)){
					targetJob = job;
					String jobFolder = ModManager.appendSlash(new File(job.getNewFiles()[0]).getParentFile().getAbsolutePath());
					String relativepath = ModManager.appendSlash(ResourceUtils.getRelativePath(jobFolder, mod.getModPath(), File.separator));
					System.out.println(relativepath);
					String filename = FilenameUtils.getName(targetPath);
					FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModType.BASEGAME,ME3TweaksUtils.HEADER)), new File(ModManager.appendSlash(mod.getModPath())+relativepath+"Coalesced.bin"));
					job.addFileReplace(mod.getModPath()+relativepath+"Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
					break;
				}
			}
						
			if (targetJob == null) {
				//no basegame header, but has tasks, and does not mod coal
				//means it doesn't modify basegame files at all so we can just add the header and set modver to 3 (or max of both in case of 2 as modcoal was not set)
				double newCmmVer = Math.max(mod.modCMMVer, 3.0);
				ModJob job = new ModJob();
				File basegamefolder = new File(ModManager.appendSlash(mod.getModPath())+"BASEGAME");
				basegamefolder.mkdirs();
				FileUtils.copyFile(new File(ModManager.getPristineCoalesced(ModType.BASEGAME,ME3TweaksUtils.HEADER)), new File(ModManager.appendSlash(mod.getModPath())+"BASEGAME/Coalesced.bin"));
				job.addFileReplace(ModManager.appendSlash(mod.getModPath())+"BASEGAME/Coalesced.bin", "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
				mod.addTask(ModType.BASEGAME, job);
				mod.modCMMVer = newCmmVer;
			}
			
			//write new moddesc.ini file
			String descini = mod.createModDescIni(mod.modCMMVer);
			ModManager.debugLogger.writeMessage("Writing new moddesc.ini with new coal modding job");
			FileUtils.writeStringToFile(new File(stagingIniPath) , descini);
			
			//reload mod in staging with new job added
			ModManager.debugLogger.writeMessage("Reloading Staging mod with new moddesc.ini file");
			mod = new Mod(stagingIniPath);
			basegamecoal = mod.getBasegameCoalesced();
		
		}
		
		return false;
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
