package com.me3tweaks.modmanager.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.AutoTocWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ResourceUtils;

public class Mod implements Comparable<Mod> {
	File modDescFile;
	boolean validMod = false, modCoal = false;
	String modName, modDisplayDescription, modDescription, modPath, modifyString;
	public ArrayList<ModJob> jobs;
	private String modAuthor;
	private int modmakerCode = 0;
	private String modSite;
	private String modmp;
	private double compiledAgainstModmakerVersion;
	private String modVersion;
	public double modCMMVer = 1.0;
	private int classicCode;
	private ArrayList<Patch> requiredPatches = new ArrayList<Patch>();

	/**
	 * Creates a new mod object.
	 * 
	 * @param filepath
	 *            Path to the moddesc.ini file.
	 */
	public Mod(String filepath) {
		if (filepath == null) {
			return;
		}
		modifyString = "";
		modDescFile = new File(filepath);
		modPath = modDescFile.getParent();
		jobs = new ArrayList<ModJob>();
		try {
			readDesc();
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error reading moddesc.ini:", e);
			setValidMod(false);
			return;
		}
	}

	/**
	 * Returns the legacy modcoal variable. This being true indicates a
	 * Coalesced.bin file in the mod root should be installed in legacy mode.
	 * 
	 * @return true if legacy coal install, false otherwise
	 */
	public boolean modsCoal() {
		return modCoal;
	}

	/**
	 * Parses the moddesc.ini file and validates it.
	 * 
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	private void readDesc() throws InvalidFileFormatException, IOException {
		Wini modini = new Wini(modDescFile);
		String modFolderPath = ModManager.appendSlash(modDescFile.getParent());
		modDisplayDescription = modini.get("ModInfo", "moddesc");
		modName = modini.get("ModInfo", "modname");
		ModManager.debugLogger.writeMessage("-------MOD----------------Reading " + modName + "--------------------");
		// Check if this mod has been made for Mod Manager 2.0 or legacy mode
		modCMMVer = 1.0f;
		try {
			modCMMVer = Float.parseFloat(modini.get("ModManager", "cmmver"));
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeMessage("Didn't read a ModManager version of the mod. Setting modtype to legacy");
			modCMMVer = 1.0f;
		}

		// Backwards compatibility for mods that are built to target older
		// versions of mod manager (NO DLC)
		if (modCMMVer < 2.0f) {
			modCMMVer = 1.0f;
			ModManager.debugLogger.writeMessage("Modcmmver is less than 2, checking for coalesced.bin in folder (legacy)");
			File file = new File(ModManager.appendSlash(modPath) + "Coalesced.bin");
			if (!file.exists()) {
				ModManager.debugLogger.writeMessage(modName + " doesn't have Coalesced.bin and is marked as legacy, marking as invalid.");
				return;
			}
			addTask(ModType.COAL, null);

			validMod = true;
			ModManager.debugLogger.writeMessage(modName + " valid, marked as legacy mod. Added coalesced swap job, marked valid, finish reading mod.");
			ModManager.debugLogger.writeMessage("-----MOD------------END OF " + modName + "--------------------");
			return;
		}

		if (modCMMVer > 3.0f && modCMMVer < 3.1f) {
			modCMMVer = 3.0f;
		}

		modCMMVer = (double) Math.round(modCMMVer * 10) / 10;

		ModManager.debugLogger.writeMessage("Mod Manager version read: " + modCMMVer);
		ModManager.debugLogger.writeMessage("Checking for DLC headers in the ini file.");

		// It's a 2.0 or above mod. Check for mod tags in the desc file
		String[] modIniHeaders = ModType.getHeaderNameArray();
		for (String modHeader : modIniHeaders) {
			ModManager.debugLogger.writeMessage("Scanning for header: " + modHeader + " in ini of " + modName);

			// Check for each mod. If it exists, add the task
			String iniModDir = modini.get(modHeader, "moddir");
			if (iniModDir != null && !iniModDir.equals("")) {
				// It's a DLC header, we should check for the files to mod, and
				// make sure they all match properly
				ModManager.debugLogger.writeMessage("Found INI header " + modHeader);

				//REPLACE FILES (Mod Manager 2.0+)
				String newFileIni = modini.get(modHeader, "newfiles");
				String oldFileIni = modini.get(modHeader, "replacefiles");
				//NEW FILES (Mod Manager 4.1+)
				String addFileIni = modini.get(modHeader, "addfiles");
				String addFileTargetIni = modini.get(modHeader, "addfilestargets");
				//REMOVE FILES (Mod Manager 4.1+)
				String removeFileTargetIni = modini.get(modHeader, "removefiletargets");
				String requirementText = null;

				if (newFileIni == null || oldFileIni == null || newFileIni.equals("") || oldFileIni.equals("")) {
					ModManager.debugLogger.writeMessage("newfiles/replace files was null or empty, mod marked as invalid.");
					return;
				}

				//Parse replace files
				StringTokenizer newStrok = new StringTokenizer(newFileIni, ";");
				StringTokenizer oldStrok = new StringTokenizer(oldFileIni, ";");
				if (newStrok.countTokens() != oldStrok.countTokens()) {
					// Same number of tokens aren't the same
					ModManager.debugLogger.writeMessage("Number of files to update/replace do not match, mod being marked as invalid.");
					return;
				}

				//Mod Manager 4.1+ ADD/REMOVE/JOBDESCRIPTIONS
				StringTokenizer addStrok = null;
				StringTokenizer addTargetStrok = null;
				StringTokenizer removeStrok = null;
				if (modCMMVer >= 4.1) {
					//Parse add files
					if (addFileIni != null && addFileTargetIni != null && !addFileIni.equals("") && !addFileTargetIni.equals("")) {
						addStrok = new StringTokenizer(addFileIni, ";");
						addTargetStrok = new StringTokenizer(addFileTargetIni, ";");
						if (addStrok.countTokens() != addTargetStrok.countTokens()) {
							// Same number of tokens aren't the same
							ModManager.debugLogger.writeMessage("Number of files to add and number of target files do not match, mod being marked as invalid.");
							return;
						}
					}

					//remove files doesn't need a token match, but create the tokenizer for later
					if (removeFileTargetIni != null && !removeFileTargetIni.equals("")) {
						removeStrok = new StringTokenizer(removeFileTargetIni, ";");
					}
					requirementText = modini.get(modHeader, "jobdescription");
					ModManager.debugLogger.writeMessage(modHeader+" job description: "+requirementText);
				}

				// Check to make sure the filenames are the same, and if they
				// are, then the mod is going to be valid.
				// Start building the mod job.
				// modCMMVer = 3.0;
				ModJob newJob;
				if (modCMMVer >= 3 && modHeader.equals(ModType.BASEGAME)) {
					newJob = new ModJob();
				} else {
					// DLC Job
					newJob = new ModJob(ModType.getDLCPath(modHeader), modHeader, requirementText);
					if (modCMMVer >= 3 && modHeader.equals(ModType.TESTPATCH)) {
						newJob.TESTPATCH = true;
					}
				}
				while (newStrok.hasMoreTokens()) {
					String newFile = newStrok.nextToken();
					String oldFile = oldStrok.nextToken();
					// ModManager.debugLogger.writeMessage("Validating tokens: "+newFile+" vs
					// "+oldFile);
					if (!newFile.equals(getSfarFilename(oldFile))) {
						ModManager.debugLogger.writeError("[REPLACEFILE]Filenames failed to match, mod marked as invalid: " + newFile + " vs " + getSfarFilename(oldFile));
						return; // The names of the files don't match
					}

					// Add the file swap to task job - if this method returns
					// false it means a file doesn't exist somewhere
					if (!(newJob.addFileReplace(modFolderPath + ModManager.appendSlash(iniModDir) + newFile, oldFile))) {
						ModManager.debugLogger.writeError("Failed to add file to replace (File likely does not exist), marking as invalid.");
						return;
					}
				}
				if (addStrok != null) {
					while (addStrok.hasMoreTokens()) {
						String addFile = addStrok.nextToken();
						String targetFile = addTargetStrok.nextToken();
						if (!addFile.equals(getSfarFilename(targetFile))) {
							ModManager.debugLogger.writeError("[ADDFILE]Filenames failed to match, mod marked as invalid: " + addFile + " vs " + getSfarFilename(targetFile));
							return; // The names of the files don't match
						}

						// Add the file swap to task job - if this method returns
						// false it means a file doesn't exist somewhere
						if (!(newJob.addNewFileTask(modFolderPath + ModManager.appendSlash(iniModDir) + addFile, targetFile))) {
							ModManager.debugLogger.writeError("[ADDFILE]Failed to add task for file addition (File likely does not exist), marking as invalid.");
							return;
						}
					}
				}
				if (removeStrok != null) {
					while (removeStrok.hasMoreTokens()) {
						String removeFilePath = removeStrok.nextToken();
						//add remove file to job
						if (!newJob.addRemoveFileTask(removeFilePath)) {
							ModManager.debugLogger.writeError("[REMOVE]Failed to add task for file removal.");
							return;
						}
					}
				}
				ModManager.debugLogger.writeMessage(modName + ": Successfully made a new Mod Job for: " + modHeader);
				addTask(modHeader, newJob);
			}
		}

		// CHECK FOR CUSTOMDLC HEADER (3.1+)
		if (modCMMVer >= 3.1) {
			ModManager.debugLogger.writeMessage("Mod built for CMM 3.1+, checking for CUSTOMDLC header");
			String iniModDir = modini.get(ModType.CUSTOMDLC, "sourcedirs");
			if (iniModDir != null && !iniModDir.equals("")) {
				ModManager.debugLogger.writeMessage("Found CUSTOMDLC header");

				//customDLC flag is set
				String sourceFolderIni = modini.get(ModType.CUSTOMDLC, "sourcedirs");
				String destFolderIni = modini.get(ModType.CUSTOMDLC, "destdirs");
				// ModManager.debugLogger.writeMessage("New files: "+newFileIni);
				// ModManager.debugLogger.writeMessage("Old Files: "+oldFileIni);
				if (sourceFolderIni == null || destFolderIni == null || sourceFolderIni.equals("") || destFolderIni.equals("")) {
					ModManager.debugLogger.writeMessage("sourcedirs/destdirs files was null or empty, mod marked as invalid.");
					return;
				}

				StringTokenizer srcStrok = new StringTokenizer(sourceFolderIni, ";");
				StringTokenizer destStrok = new StringTokenizer(sourceFolderIni, ";");
				if (srcStrok.countTokens() != destStrok.countTokens()) {
					// Same number of tokens aren't the same
					ModManager.debugLogger.writeMessage("Number of source and destination directories for custom DLC job do not match, mod being marked as invalid.");
					return;
				}

				ModJob newJob = new ModJob();
				newJob.setJobName(ModType.CUSTOMDLC); //backwards, it appears...
				newJob.setJobType(ModJob.CUSTOMDLC);
				newJob.sourceFolders = new ArrayList<String>();
				newJob.setDestFolders(new ArrayList<String>());

				while (srcStrok.hasMoreTokens()) {
					String sourceFolder = srcStrok.nextToken();
					String destFolder = destStrok.nextToken();

					File sf = new File(modFolderPath + sourceFolder);
					if (!sf.exists()) {
						ModManager.debugLogger.writeError("Custom DLC Source folder does not exist: " + sf.getAbsolutePath() + ", mod marked as invalid");
						return;
					}
					List<File> sourceFiles = (List<File>) FileUtils.listFiles(sf, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
					for (File file : sourceFiles) {
						String relativePath = ResourceUtils.getRelativePath(file.getAbsolutePath(), sf.getAbsolutePath(), File.separator);
						String destFilePath = ModManager.appendSlash(destFolder) + relativePath;
						if (!(newJob.addFileReplace(file.getAbsolutePath(), destFilePath))) {
							ModManager.debugLogger.writeError("Failed to add file to replace (File likely does not exist), marking as invalid.");
							return;
						}
					}
					newJob.sourceFolders.add(sourceFolder);
					newJob.getDestFolders().add(destFolder);
				}
				ModManager.debugLogger.writeMessage(modName + ": Successfully made a new Mod Job for: " + ModType.CUSTOMDLC);
				addTask(ModType.CUSTOMDLC, newJob);
			}
		}

		// Backwards compatibility for Mod Manager 2's modcoal flag (has now
		// moved to [BASEGAME] as of 3.0)
		if (modCMMVer < 3.0f && modCMMVer >= 2.0f) {
			modCMMVer = 2.0;
			ModManager.debugLogger.writeMessage(modName + ": Checking for modcoal in moddesc.ini because moddesc targets cmm2.0");

			int modCoalFlag = 0;
			try {
				modCoalFlag = Integer.parseInt(modini.get("ModInfo", "modcoal"));
				ModManager.debugLogger.writeMessage("Coalesced flag: " + modCoalFlag);

				if (modCoalFlag != 0) {
					File file = new File(ModManager.appendSlash(modPath) + "Coalesced.bin");
					ModManager.debugLogger.writeMessage("Coalesced flag was set, verifying its location");

					if (!file.exists()) {
						ModManager.debugLogger.writeMessage(modName + " doesn't have Coalesced.bin even though flag was set. Marking as invalid.");
						return;
					} else {
						ModManager.debugLogger.writeMessage("Coalesced.bin is OK");
					}
					addTask(ModType.COAL, null);
				}
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessage("Was not able to read the coalesced mod value. Coal flag was not set/not entered, skipping setting coal");
			}
		}
		// Check for coalesced in the new mod manager version [2.0 only]
		// (modcoal)

		ModManager.debugLogger.writeMessage("Finished reading mod.");
		if (modCMMVer < 3.0f) {
			// only print if it's not able to use BASEGAME
			ModManager.debugLogger.writeMessage("Coalesced Swap: " + modCoal);
		}
		ModManager.debugLogger.writeMessage("Number of Mod Jobs:" + jobs.size());
		if (jobs.size() > 0 || modCoal == true) {
			ModManager.debugLogger.writeMessage("Verified source files, mod should be OK to install");
			setModDisplayDescription(false);
			validMod = true;
		}

		// read additional parameters
		// Add version
		if (modini.get("ModInfo", "modver") != null) {
			modVersion = modini.get("ModInfo", "modver");
			ModManager.debugLogger.writeMessage("Detected mod version: " + modVersion);
		}

		if (modini.get("ModInfo", "updatecode") != null) {
			try {
				classicCode = Integer.parseInt(modini.get("ModInfo", "updatecode"));
				ModManager.debugLogger.writeMessage("Detected me3tweaks update code: " + classicCode);
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeError("Classic update code is not an integer");
			}
		}

		// Add MP Changer
		if (modini.get("ModInfo", "modmp") != null) {
			modmp = modini.get("ModInfo", "modmp");
			ModManager.debugLogger.writeMessage("Detected multiplayer modification");
		}
		// Add developer
		if (modini.get("ModInfo", "moddev") != null) {
			modAuthor = modini.get("ModInfo", "moddev");
			ModManager.debugLogger.writeMessage("Detected developer name");
		}
		// Add Devsite
		if (modini.get("ModInfo", "modsite") != null) {
			modSite = modini.get("ModInfo", "modsite");
			ModManager.debugLogger.writeMessage("Detected developer site");
		}

		// Add Modmaker
		if (modini.get("ModInfo", "modid") != null) {
			try {
				modmakerCode = Integer.parseInt(modini.get("ModInfo", "modid"));
				ModManager.debugLogger.writeMessage("Detected modmaker code");
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeError("ModMaker code failed to resolve to an integer.");
			}
		}

		//modmaker compiledagainst flag (1.5+)
		if (modini.get("ModInfo", "compiledagainst") != null) {
			try {
				compiledAgainstModmakerVersion = Double.parseDouble(modini.get("ModInfo", "compiledagainst"));
				ModManager.debugLogger.writeMessage("Server version compiled against: " + compiledAgainstModmakerVersion);
				if (compiledAgainstModmakerVersion < 1.5) {
					try {
						int ver = Integer.parseInt(modVersion);
						ver++;
						modVersion = Integer.toString(ver);
						ModManager.debugLogger.writeMessage("ModMaker mod (<1.5), +1 to revision.");
					} catch (NumberFormatException e) {
						ModManager.debugLogger.writeError("ModMaker version failed to resolve to an integer.");
						modVersion = Integer.toString(1);
					}
				}
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeError("ModMaker code failed to resolve to an integer.");
			}
		} else {
			if (modmakerCode > 0) {
				//modmaker mod
				compiledAgainstModmakerVersion = 1.4;
				ModManager.debugLogger.writeMessage("Unknown server version, assuming 1.4 compilation target: " + compiledAgainstModmakerVersion);
				try {
					int ver = Integer.parseInt(modVersion);
					ver++;
					modVersion = Integer.toString(ver);
					ModManager.debugLogger.writeMessage("ModMaker mod (<1.5), +1 to revision.");
				} catch (NumberFormatException e) {
					ModManager.debugLogger.writeError("ModMaker version failed to resolve to an integer.");
					modVersion = Integer.toString(1);
				}
			}
		}

		if (modCMMVer > 3.2) {
			ModManager.debugLogger.writeError("Mod is for newer version of Mod Manager, may have issues with this version");
		}
		setModDisplayDescription(modCMMVer < 3.0);

		//check for patches directory
		if (modCMMVer >= 2) {
			File patchesdir = new File(modFolderPath + "patches/");
			if (patchesdir.isDirectory()) {
				ModManager.debugLogger.writeMessage("Mod has unprocessed patches! Importing patches now.");
				File[] directories = patchesdir.listFiles(File::isDirectory);
				for (File directory : directories) {
					String path = directory.getAbsolutePath();
					File patchDesc = new File(ModManager.appendSlash(path) + "patchdesc.ini");
					System.out.println("Checking for file at " + patchDesc);
					if (!patchDesc.exists()) {
						continue;
					}
					Patch subPatch = new Patch(patchDesc.getAbsolutePath());
					if (subPatch.isValid()) {
						ModManager.debugLogger.writeMessage("Valid patch: " + subPatch.getPatchName() + ", importing to library and processing");
						subPatch = subPatch.importPatch();
						requiredPatches.add(subPatch);
					}
				}
				if (patchesdir.list().length <= 0) {
					ModManager.debugLogger.writeMessage("All patches imported. Deleting patches directory.");
					FileUtils.deleteDirectory(patchesdir);
				}
			}

		}

		ModManager.debugLogger.writeMessage("Finished loading moddesc.ini for this mod");
		ModManager.debugLogger.writeMessage("-------MOD----------------END OF " + modName + "--------------------------");
	}

	/**
	 * Gets the filepath's internal filename
	 * 
	 * @param sfarFilePath
	 *            path in sfar (or unpacked DLC)
	 * @return filename of passed in string
	 */
	private String getSfarFilename(String sfarFilePath) {
		StringTokenizer strok = new StringTokenizer(sfarFilePath, "/|\\");
		String str = null;
		while (strok.hasMoreTokens()) {
			str = strok.nextToken();
		}
		// ModManager.debugLogger.writeMessage("SFAR Shortened filename: "+str);
		return str;
	}

	/**
	 * Adds a task to this mod for when the mod is deployed.
	 * 
	 * @param name
	 * @param newJob
	 */
	public void addTask(String name, ModJob newJob) {
		if (name.equals(ModType.COAL)) {
			modCoal = true;
			updateModifyString(ModType.COAL);
			return;
		}
		updateModifyString(name);
		jobs.add(newJob);

	}

	/**
	 * Updates the string showing what this mod edits in terms of files
	 * 
	 * @param taskName
	 *            name of task to modify (EARTH, Coalesced, etc)
	 */
	private void updateModifyString(String taskName) {
		if (modifyString.equals("")) {
			modifyString = "\nModifies: " + taskName;
		} else {
			modifyString += ", " + taskName;
		}
	}

	/**
	 * Returns mod's folder, with a / on the end
	 * 
	 * @return
	 */
	public String getModPath() {
		return ModManager.appendSlash(modPath);
	}

	public String getModName() {
		return modName;
	}

	/**
	 * Sets this mod description. It should be run through breakfixer() first.
	 * 
	 * @param desc
	 */
	public void setModDescription(String desc) {
		this.modDescription = desc;
	}

	public void setModDisplayDescription(boolean markedLegacy) {
		modDisplayDescription = "This mod has no description in it's moddesc.ini file or there was an error reading the description of this mod.";
		if (modDescFile == null) {
			ModManager.debugLogger.writeMessage("Mod Desc file is null, unable to read description");
			return;
		}
		Wini modini = null;
		try {
			modini = new Wini(modDescFile);
			modDisplayDescription = modini.get("ModInfo", "moddesc");
			modDescription = modini.get("ModInfo", "moddesc");
			modDisplayDescription = breakFixer(modDisplayDescription);
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
			ModManager.debugLogger.writeMessage("Invalid File Format exception on moddesc.");
			return;
		} catch (IOException e) {
			ModManager.debugLogger.writeMessage("I/O Error reading mod file. It may not exist or it might be corrupt.");
			return;
		}
		modDisplayDescription = breakFixer(modDisplayDescription);

		// Add 1st newline
		modDisplayDescription += "\n";

		// Add modversion
		if (modVersion != null) {
			modDisplayDescription += "\nMod Version: " + modVersion;
		}
		// Add mod manager build version
		if (markedLegacy) {
			modDisplayDescription += "\nLegacy Mod";
		}

		// Add modifier
		modDisplayDescription += getModifyString();

		// Add MP Changer
		if (modmp != null) {
			modDisplayDescription += "\nModifies Multiplayer: " + modmp;
		}
		// Add developer
		if (modAuthor != null) {
			modDisplayDescription += "\nMod Developer: " + modAuthor;
		}
		// Add Devsite
		if (modSite != null) {
			modDisplayDescription += "\nMod Site: " + modSite;
		}

		// Add Modmaker
		if (modmakerCode > 0) {
			//modmakerCode = modini.get("ModInfo", "modid");
			modDisplayDescription += "\nModMaker code: " + modmakerCode;
		}
	}

	public String getModDisplayDescription() {
		return modDisplayDescription;
	}

	public String getModDescription() {
		return modDescription;
	}

	/**
	 * Replaces all break (br between <>) lines with a newline character. Used
	 * to add newlines to ini4j.
	 * 
	 * @param string
	 *            String to parse
	 * @return String that has been fixed
	 */
	public static String breakFixer(String string) {
		String br = "<br>";
		if (string == null) {
			return string;
		}
		return string.replaceAll(br, "\n");
	}

	@Override
	public String toString() {
		return getModName();
	}

	public ModJob[] getJobs() {
		return jobs.toArray(new ModJob[jobs.size()]);
	}

	public String getModifyString() {
		if (!modifyString.equals("")) {
			modifyString += "\n";
		}
		return modifyString;
	}

	public boolean canMergeWith(Mod other) {
		for (ModJob job : jobs) {
			ModJob otherCorrespondingJob = null;
			for (ModJob otherjob : other.jobs) {
				if (otherjob.equals(job)) {
					otherCorrespondingJob = otherjob;
					break;
				}
			}
			if (otherCorrespondingJob == null) {
				continue;
			}
			// scanned for matching job. Found it. Iterate over files...
			for (String file : job.getFilesToReplace()) {
				if (FilenameUtils.getName(file).equals("PCConsoleTOC.bin")) {
					continue;
				}
				for (String otherfile : otherCorrespondingJob.getFilesToReplace()) {
					if (file.equals(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge conflicts with file to update " + file);
						return false;
					}
				}
			}
		}
		return true;
	}

	public HashMap<String, ArrayList<String>> getConflictsWithMod(Mod other) {
		HashMap<String, ArrayList<String>> conflicts = new HashMap<String, ArrayList<String>>();
		for (ModJob job : jobs) {
			ModJob otherCorrespondingJob = null;
			for (ModJob otherjob : other.jobs) {
				if (otherjob.equals(job)) {
					otherCorrespondingJob = otherjob;
					break;
				}
			}
			if (otherCorrespondingJob == null) {
				continue;
			}
			// scanned for matching job. Found it. Iterate over files...
			for (String file : job.getFilesToReplace()) {
				if (FilenameUtils.getName(file).equals("PCConsoleTOC.bin")) {
					continue;
				}
				for (String otherfile : otherCorrespondingJob.getFilesToReplace()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.getJobName())) {
							conflicts.get(job.getJobName()).add(file);
							ModManager.debugLogger.writeMessage("ADDING TO CONFLICT LIST: " + file);
						} else {
							ArrayList<String> conflictFiles = new ArrayList<String>();
							conflictFiles.add(file);
							conflicts.put(job.getJobName(), conflictFiles);
						}
					}
				}
			}
		}
		if (conflicts.size() <= 0) {
			return null;
		}
		return conflicts;
	}

	/**
	 * Creates a moddesc.ini string that should be written to a file that
	 * describes this mod object.
	 * 
	 * @param modName
	 *            Name of this mod
	 * @param modDescription
	 *            Description of this mod
	 * @param folderName
	 *            mod's foldername
	 * @return moddesc.ini file as a string
	 */
	public String createModDescIni(double cmmVersion) {
		// Write mod descriptor file
		try {
			Wini ini = new Wini();

			// put modmanager, modinfo
			ini.put("ModManager", "cmmver", cmmVersion);
			ini.put("ModInfo", "modname", modName);
			ini.put("ModInfo", "moddev", modAuthor);
			if (modDescription != null) {
				ini.put("ModInfo", "moddesc", modDescription);
			}
			if (modVersion != null) {
				ini.put("ModInfo", "modver", modVersion);
			}
			if (modmp != null) {
				ini.put("ModInfo", "modmp", modmp);
			}
			if (modSite != null) {
				ini.put("ModInfo", "modsite", modSite);
			}
			if (modmakerCode > 0) {
				ini.put("ModInfo", "modid", Integer.toString(modmakerCode));
			}
			if (compiledAgainstModmakerVersion > 0) {
				ini.put("ModInfo", "compiledagainst", Double.toString(compiledAgainstModmakerVersion));
			}

			for (ModJob job : jobs) {
				boolean isFirst = true;

				if (job.getJobType() == ModJob.CUSTOMDLC) {
					StringBuilder sfsb = new StringBuilder();
					StringBuilder dfsb = new StringBuilder();

					//source dirs
					for (String file : job.sourceFolders) {
						if (isFirst) {
							isFirst = false;
						} else {
							sfsb.append(";");
						}
						sfsb.append(FilenameUtils.getName(file));
					}
					isFirst = true;
					//dest dirs
					for (String file : job.getDestFolders()) {
						if (isFirst) {
							isFirst = false;
						} else {
							dfsb.append(";");
						}
						dfsb.append(FilenameUtils.getName(file));
					}

					ini.put(job.getJobName(), "sourcedirs", sfsb.toString());
					ini.put(job.getJobName(), "destdirs", dfsb.toString());
					continue; //skip dlc,basegame on this pass
				}

				ini.put(job.getJobName(), "moddir", getStandardFolderName(job.getJobName()));
				StringBuilder nfsb = new StringBuilder();
				//new files list
				for (String file : job.newFiles) {
					if (isFirst) {
						isFirst = false;
					} else {
						nfsb.append(";");
					}
					nfsb.append(FilenameUtils.getName(file));
				}

				//replace files list
				isFirst = true;
				StringBuilder rfsb = new StringBuilder();
				for (String file : job.filesToReplace) {
					if (isFirst) {
						isFirst = false;
					} else {
						rfsb.append(";");
					}
					rfsb.append(file);
				}

				//DLC, basegame
				ini.put(job.getJobName(), "newfiles", nfsb.toString());
				ini.put(job.getJobName(), "replacefiles", rfsb.toString());
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ini.store(os);
			return new String(os.toByteArray(), "ASCII");
		} catch (Exception e) {
			ModManager.debugLogger.writeError("Error saving message!");
			ModManager.debugLogger.writeException(e);
		}
		return "Failed to write new moddesc.ini file";
	}

	/**
	 * Gets the standard folder name a mod uses for a module. Essentially is a
	 * Header => Internal converter
	 * 
	 * @param header
	 * @return
	 */
	public static String getStandardFolderName(String header) {
		switch (header) {
		case "BASEGAME":
			return "BASEGAME";
		case "RESURGENCE":
			return "MP1";
		case "REBELLION":
			return "MP2";
		case "EARTH":
			return "MP3";
		case "RETALIATION":
			return "MP4";
		case "RECKONING":
			return "MP5";
		case "PATCH1":
			return "PATCH1";
		case "PATCH2":
			return "PATCH2";
		case "TESTPATCH":
			return "TESTPATCH";
		case "CUSTOMDLC":
			return "CUSTOMDLC";
		default:
			return "UNKNOWN_DEFAULT_FOLDER";
		}
	}

	/**
	 * Merges this mod with another, ignoring conflict files from the parameter
	 * mod. This mod is the one merged into (this mod object is returned)
	 * 
	 * @param other
	 *            Other mod to merge with
	 * @return
	 */
	public Mod mergeWith(Mod other, String newName) {
		this.modName = newName;
		HashMap<String, ArrayList<String>> ignoreFiles = getConflictsWithMod(other);
		for (ModJob otherjob : other.jobs) {
			ModJob myCorrespendingJob = null;
			for (ModJob job : jobs) {
				if (otherjob.equals(job)) {
					//find jobs that match each other in both mods
					myCorrespendingJob = job;
					break;
				}
			}
			if (myCorrespendingJob == null) {
				//no match, just merge entire job
				ModManager.debugLogger.writeMessage("Merging entire job: " + otherjob.getJobName());
				jobs.add(otherjob);
				continue;
			}
			String[] otherNewFiles = otherjob.getNewFiles();
			ArrayList<String> otherReplacePaths = otherjob.getFilesToReplace();
			for (int i = 0; i < otherNewFiles.length; i++) {
				String otherfile = otherNewFiles[i];
				ModManager.debugLogger.writeMessage("CURRENT JOB: " + myCorrespendingJob.getJobName());
				if (ignoreFiles != null && ignoreFiles.get(otherjob.getJobName()) != null && ignoreFiles.get(otherjob.getJobName()).contains(otherReplacePaths.get(i))) {
					ModManager.debugLogger.writeMessage("SKIPPING CONFLICT MERGE: " + otherReplacePaths.get(i));
					continue;
				} else {
					if (FilenameUtils.getName(otherfile).equals("PCConsoleTOC.bin")) {
						ModManager.debugLogger.writeMessage("CHECKING IF SHOULD ADD TOC, EXIST IN THIS JOB ALREADY: " + myCorrespendingJob.hasTOC());
						// check if its there already
						if (myCorrespendingJob.hasTOC()) {
							continue;// skip toc
						}
					}
					ModManager.debugLogger.writeMessage("Merging file replace: " + otherfile);
					myCorrespendingJob.addFileReplace(otherfile, otherReplacePaths.get(i));
				}
			}
		}
		if (other.modCMMVer > modCMMVer) {
			//upgrade to highest cmm ver
			modCMMVer = other.modCMMVer;
		}

		modmakerCode = 0;
		return this;
	}

	// public boolean addConflictFile(String jobName, )

	/**
	 * Creates a new mod package in a folder with the same name as this mod.
	 * Copies files to the new directory based on the name of this mod. Creates
	 * a moddesc.ini file based on jobs in this mod.
	 */
	public Mod createNewMod() {
		File modFolder = new File(ModManager.getModsDir() + modName);
		modFolder.mkdirs();
		for (ModJob job : jobs) {
			if (job.getJobType() == ModJob.CUSTOMDLC) {
				for (String sourceFolder : job.sourceFolders) {
					try {
						File srcFolder = new File(ModManager.appendSlash(modDescFile.getParentFile().getAbsolutePath()) + sourceFolder);
						File destFolder = new File(modFolder + File.separator + sourceFolder); //source folder is a string
						ModManager.debugLogger.writeMessage("Copying custom DLC folder: " + srcFolder.getAbsolutePath() + " to " + destFolder.getAbsolutePath());
						FileUtils.copyDirectory(srcFolder, destFolder);
					} catch (IOException e) {
						ModManager.debugLogger.writeMessage("IOException while merging mods (custom DLC).");
						ModManager.debugLogger.writeException(e);
					}
				}
				continue;
			}

			File moduleDir = new File(modFolder + File.separator + getStandardFolderName(job.getJobName()));
			moduleDir.mkdirs();
			// scanned for matching job. Found it. Iterate over files...
			for (String mergefile : job.getNewFiles()) {
				File file = new File(mergefile);
				String baseName = FilenameUtils.getName(mergefile);
				try {
					File destinationFile = new File(moduleDir + File.separator + baseName);
					ModManager.debugLogger.writeMessage("Copying to new mod folder: " + file.getAbsolutePath() + " to " + destinationFile.getAbsolutePath());
					FileUtils.copyFile(file, destinationFile);
				} catch (IOException e) {
					ModManager.debugLogger.writeMessage("IOException while merging mods.");
					ModManager.debugLogger.writeException(e);
				}
				ModManager.debugLogger.writeMessage(job.getJobName() + ": " + file);
			}
		}
		try {
			ModManager.debugLogger.writeMessage("Creating moddesc.ini...");
			FileUtils.writeStringToFile(new File(modFolder + File.separator + "moddesc.ini"), createModDescIni(modCMMVer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("IOException while merging mods.");
			ModManager.debugLogger.writeException(e);
			e.printStackTrace();
		}
		Mod newMod = new Mod(modFolder + File.separator + "moddesc.ini");
		new AutoTocWindow(newMod);
		return newMod;
	}

	public static String convertNewlineToBr(String input) {
		return input.replaceAll("\n", "<br>");
	}

	public void setModName(String modName) {
		this.modName = modName;
	}

	@Override
	public int compareTo(Mod other) {
		return getModName().toLowerCase().compareTo(other.getModName().toLowerCase());
	}

	/**
	 * Checks if this mod has a job for custom DLC
	 * 
	 * @return true if contains custom DLC job, false otherwise
	 */
	public boolean containsCustomDLCJob() {
		if (modCMMVer < 3.1) {
			return false;
		}
		for (ModJob job : jobs) {
			if (job.getJobType() == ModJob.CUSTOMDLC) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to parse the mod version from the CMM string If it fails it
	 * returns a 0
	 * 
	 * @return
	 */
	public double getVersion() {
		if (modVersion == null) {
			return 0;
		}
		try {
			return Double.parseDouble(modVersion);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public int getClassicUpdateCode() {
		if (classicCode <= 0) {
			return 0;
		}

		return classicCode;
	}

	public int getModMakerCode() {
		return modmakerCode;
	}

	public String getDescFile() {
		return modPath + File.separator + "moddesc.ini";
	}

	public double getCMMVer() {
		return modCMMVer;
	}

	public boolean isME3TweaksUpdatable() {
		if ((getClassicUpdateCode() > 0 || getModMakerCode() > 0) && getVersion() > 0) {
			return true;
		}
		return false;
	}

	public String getAuthor() {
		return modAuthor;
	}

	public void setModUpdateCode(int i) {
		classicCode = i;
	}

	public void setVersion(double i) {
		modVersion = Double.toString(i);
	}

	/**
	 * Returns true if this mod has a job that modifies the basegame coalesced
	 * 
	 * @return true if basegame coal is swapped, false otherwise
	 */
	public boolean modifiesBasegameCoalesced() {
		for (ModJob job : jobs) {
			if (job.getJobName() == ModType.COAL) {
				return true;
			}
			for (String file : job.filesToReplace) {
				file = file.replaceAll("\\\\", "/"); //make sure all are the same (since the yall work)
				if (file.toLowerCase().equals("/BIOGame/CookedPCConsole/Coalesced.bin".toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets this mods basegame coalesced file it will install.
	 * 
	 * @return new basegame coalesced file, or null if this mod does not specify
	 *         one
	 */
	public String getBasegameCoalesced() {
		/*
		 * if (modsCoal()){ return ModManager.appendSlash(modPath) +
		 * "Coalesced.bin"; } for (ModJob job : jobs) { System.out.println(
		 * "GETBASECOAL SCANNING: "+job.getJobName()); for (int i = 0; i <
		 * job.filesToReplace.size(); i++){ String file =
		 * job.filesToReplace.get(i); file = file.replaceAll("\\\\", "/");
		 * //make sure all are the same (since the yall work) if
		 * (file.toLowerCase().equals("".toLowerCase())){ return
		 * job.newFiles.get(i); } } } return null;
		 */
		return getModTaskPath("\\BIOGame\\CookedPCConsole\\Coalesced.bin", ModType.BASEGAME);
	}

	/**
	 * Searches through all jobs for the specified path. Uses a module ID
	 * (header) to find a job.
	 * 
	 * @return path to new file if found, null if it does't exist.
	 */
	public String getModTaskPath(String modulePath, String header) {
		if (header.equals(ModType.COAL) && modsCoal()) {
			return ModManager.appendSlash(modPath) + "Coalesced.bin";
		}
		modulePath = modulePath.replaceAll("\\\\", "/");
		if (!modulePath.startsWith("/")) {
			modulePath = "/" + modulePath;
		}
		for (ModJob job : jobs) {
			if (!job.getJobName().equals(header)) {
				continue;
			}
			for (int i = 0; i < job.filesToReplace.size(); i++) {
				String file = job.filesToReplace.get(i);
				file = file.replaceAll("\\\\", "/"); //make sure all are the same (since the yall work)
				if (!file.startsWith("/")) {
					file = "/" + file;
				}
				if (file.toLowerCase().equals(modulePath.toLowerCase())) {
					return job.newFiles.get(i);
				}
			}
		}
		return null;
	}

	public ArrayList<Patch> getRequiredPatches() {
		return requiredPatches;
	}

	public void setRequiredPatches(ArrayList<Patch> requiredPatches) {
		this.requiredPatches = requiredPatches;
	}

	public boolean isValidMod() {
		return validMod;
	}

	private void setValidMod(boolean validMod) {
		this.validMod = validMod;
	}
}
