package com.me3tweaks.modmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class Mod {
	File modDescFile;
	boolean validMod = false, modCoal = false;
	String modName, modDisplayDescription, modDescription, modPath, modifyString;
	ArrayList<ModJob> jobs;
	private String modAuthor;
	private String modmakerCode;
	private String modSite;
	private String modmp;
	private String modVersion;
	protected double modCMMVer = 1.0;

	public boolean isValidMod() {
		return validMod;
	}

	private void setValidMod(boolean validMod) {
		this.validMod = validMod;
	}

	/**
	 * Creates a new mod object.
	 * 
	 * @param filepath
	 *            Path to the moddesc.ini file.
	 */
	public Mod(String filepath) {
		modifyString = "";
		modDescFile = new File(filepath);
		modPath = modDescFile.getParent();
		jobs = new ArrayList<ModJob>();
		try {
			readDesc();
		} catch (Exception e) {
			e.printStackTrace();
			setValidMod(false);
			return;
		}
	}

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

		modDisplayDescription = modini.get("ModInfo", "moddesc");
		modName = modini.get("ModInfo", "modname");
		ModManager.debugLogger.writeMessage("------------------Reading " + modName + "------------------");
		// Check if this mod has been made for Mod Manager 2.0 or legacy mode
		modCMMVer = 1.0f;
		try {
			modCMMVer = Float.parseFloat(modini.get("ModManager", "cmmver"));
		} catch (NumberFormatException e) {
			ModManager.debugLogger
					.writeMessage("Didn't read a ModManager version of the mod. Setting modtype to legacy");
			modCMMVer = 1.0f;
		}

		// Backwards compatibility for mods that are built to target older
		// versions of mod manager (NO DLC)
		if (modCMMVer < 2.0f) {
			modCMMVer = 1.0f;
			ModManager.debugLogger
					.writeMessage("Modcmmver is less than 2, checking for coalesced.bin in folder (legacy)");
			File file = new File(ModManager.appendSlash(modPath) + "Coalesced.bin");
			if (!file.exists()) {
				ModManager.debugLogger.writeMessage(
						modName + " doesn't have Coalesced.bin and is marked as legacy, marking as invalid.");
				return;
			}
			addTask(ModType.COAL, null);
			setModDisplayDescription(true);

			validMod = true;
			ModManager.debugLogger.writeMessage(modName
					+ " valid, marked as legacy mod. Added coalesced swap job, marked valid, finish reading mod.");
			ModManager.debugLogger
					.writeMessage("--------------------------END OF " + modName + "--------------------------");
			return;
		}

		if (modCMMVer > 3.0f) {
			modCMMVer = 3.0f;
		}

		ModManager.debugLogger.writeMessage("Mod Manager version read was >= 2.0, marked as modern style mod.");
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

				String newFileIni = modini.get(modHeader, "newfiles");
				String oldFileIni = modini.get(modHeader, "replacefiles");
				// System.out.println("New files: "+newFileIni);
				// System.out.println("Old Files: "+oldFileIni);
				if (newFileIni == null || oldFileIni == null || newFileIni.equals("") || oldFileIni.equals("")) {
					ModManager.debugLogger
							.writeMessage("newfiles/replace files was null or empty, mod marked as invalid.");
					return;
				}

				StringTokenizer newStrok = new StringTokenizer(newFileIni, ";");
				StringTokenizer oldStrok = new StringTokenizer(oldFileIni, ";");
				if (newStrok.countTokens() != oldStrok.countTokens()) {
					// Same number of tokens aren't the same
					ModManager.debugLogger.writeMessage(
							"Number of files to update/replace do not match, mod being marked as invalid.");
					return;
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
					newJob = new ModJob(ModType.getDLCPath(modHeader), modHeader);
					if (modCMMVer >= 3 && modHeader.equals(ModType.TESTPATCH)) {
						newJob.TESTPATCH = true;
					}
				}
				while (newStrok.hasMoreTokens()) {
					String newFile = newStrok.nextToken();
					String oldFile = oldStrok.nextToken();
					// System.out.println("Validating tokens: "+newFile+" vs
					// "+oldFile);
					if (!newFile.equals(getSfarFilename(oldFile))) {
						ModManager.debugLogger.writeMessage("Filenames failed to match, mod marked as invalid: "
								+ newFile + " vs " + getSfarFilename(oldFile));
						return; // The names of the files don't match
					}

					// Add the file swap to task job - if this method returns
					// false it means a file doesn't exist somewhere
					if (!(newJob.addFileReplace(ModManager.appendSlash(modDescFile.getParent())
							+ ModManager.appendSlash(iniModDir) + newFile, oldFile))) {
						ModManager.debugLogger.writeMessage(
								"Failed to add file to replace (File likely does not exist), marking as invalid.");
						return;
					}
				}
				ModManager.debugLogger.writeMessage(modName + ": Successfully made a new Mod Job for: " + modHeader);
				addTask(modHeader, newJob);
			}
		}

		// Backwards compatibility for Mod Manager 2's modcoal flag (has now
		// moved to [BASEGAME] as of 3.0)
		if (modCMMVer < 3.0f && modCMMVer >= 2.0f) {
			modCMMVer = 2.0;
			ModManager.debugLogger
					.writeMessage(modName + ": Checking for modcoal in moddesc.ini because moddesc targets cmm2.0");

			int modCoalFlag = 0;
			try {
				modCoalFlag = Integer.parseInt(modini.get("ModInfo", "modcoal"));
				ModManager.debugLogger.writeMessage("Coalesced flag: " + modCoalFlag);

				if (modCoalFlag != 0) {
					File file = new File(ModManager.appendSlash(modPath) + "Coalesced.bin");
					ModManager.debugLogger.writeMessage("Coalesced flag was set, verifying its location");

					if (!file.exists()) {
						ModManager.debugLogger.writeMessage(
								modName + " doesn't have Coalesced.bin even though flag was set. Marking as invalid.");
						return;
					}
					addTask(ModType.COAL, null);
				}
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessage(
						"Was not able to read the coalesced mod value. Coal flag was not set/not entered, skipping setting coal");
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
			ModManager.debugLogger.writeMessage("Mod Marked as valid, finish reading mod.");
			setModDisplayDescription(false);
			validMod = true;
		}

		// read additional parameters
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
			modmakerCode = modini.get("ModInfo", "modid");
			ModManager.debugLogger.writeMessage("Detected modmaker code");
		}
		ModManager.debugLogger
				.writeMessage("--------------------------END OF " + modName + "--------------------------");
	}

	/**
	 * Gets the mod's modification path
	 * 
	 * @param sfarFilePath
	 * @return
	 */
	private String getSfarFilename(String sfarFilePath) {
		StringTokenizer strok = new StringTokenizer(sfarFilePath, "/|\\");
		String str = null;
		while (strok.hasMoreTokens()) {
			str = strok.nextToken();
		}
		// System.out.println("SFAR Shortened filename: "+str);
		return str;
	}

	/**
	 * Adds a task to this mod for when the mod is deployed.
	 * 
	 * @param name
	 * @param newJob
	 */
	protected void addTask(String name, ModJob newJob) {
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

	public String getModPath() {
		return modPath;
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
		if (modini.get("ModInfo", "modver") != null) {
			modVersion = modini.get("ModInfo", "modver");
			modDisplayDescription += "\nMod Version: " + modVersion;
		}
		// Add mod manager build version
		if (markedLegacy) {
			modDisplayDescription += "\nLegacy Mod";
		}

		// Add modifier
		modDisplayDescription += getModifyString();

		// Add MP Changer
		if (modini.get("ModInfo", "modmp") != null) {
			modmp = modini.get("ModInfo", "modmp");
			modDisplayDescription += "\nModifies Multiplayer: " + modmp;
		}
		// Add developer
		if (modini.get("ModInfo", "moddev") != null) {
			modAuthor = modini.get("ModInfo", "moddev");
			modDisplayDescription += "\nMod Developer: " + modAuthor;
		}
		// Add Devsite
		if (modini.get("ModInfo", "modsite") != null) {
			modSite = modini.get("ModInfo", "modsite");
			modDisplayDescription += "\nMod Site: " + modSite;
		}

		// Add Modmaker
		if (modini.get("ModInfo", "modid") != null) {
			modmakerCode = modini.get("ModInfo", "modid");
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
						System.out.println("Merge conflicts with file to update " + file);
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
						if (conflicts.containsKey(job.jobName)) {
							conflicts.get(job.jobName).add(file);
							System.out.println("ADDING TO CONFLICT LIST: " + file);
						} else {
							ArrayList<String> conflictFiles = new ArrayList<String>();
							conflictFiles.add(file);
							conflicts.put(job.jobName, conflictFiles);
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
			if (modVersion != null) {
				ini.put("ModInfo", "modver", modVersion);
			}
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
			if (modmakerCode != null) {
				ini.put("ModInfo", "modid", modmakerCode);
			}

			for (ModJob job : jobs) {
				ini.put(job.jobName, "moddir", getStandardFolderName(job.jobName));
				StringBuilder nfsb = new StringBuilder();
				boolean isFirst = true;
				for (String file : job.newFiles) {
					if (isFirst) {
						isFirst = false;
					} else {
						nfsb.append(";");
					}
					nfsb.append(FilenameUtils.getName(file));
				}
				ini.put(job.jobName, "newfiles", nfsb.toString());

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
				ini.put(job.jobName, "replacefiles", rfsb.toString());
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ini.store(os);
			return new String(os.toByteArray(), "ASCII");
		} catch (Exception e) {

		}
		return "failure";
	}

	private Object getStandardFolderName(String jobName) {
		switch (jobName) {
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
		default:
			return "UNKNOWN_DEFAULT_FOLDER";
		}
	}

	/**
	 * Merges this mod with another, ignoring conflict files from the parameter
	 * mod.
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
					myCorrespendingJob = job;
					break;
				}
			}
			if (myCorrespendingJob == null) {
				System.out.println("Merging entire job: " + otherjob.jobName);
				jobs.add(otherjob);
				continue;
			}
			String[] otherNewFiles = otherjob.getNewFiles();
			String[] otherReplacePaths = otherjob.getFilesToReplace();
			for (int i = 0; i < otherNewFiles.length; i++) {
				String otherfile = otherNewFiles[i];
				System.out.println("CURRENT JOB: " + myCorrespendingJob.jobName);
				if (ignoreFiles != null && ignoreFiles.get(otherjob.jobName) != null
						&& ignoreFiles.get(otherjob.jobName).contains(otherReplacePaths[i])) {
					System.out.println("SKIPPING CONFLICT MERGE: " + otherReplacePaths[i]);
					continue;
				} else {
					if (FilenameUtils.getName(otherfile).equals("PCConsoleTOC.bin")) {
						System.out.println("CHECKING IF SHOULD ADD TOC, EXIST IN THIS JOB ALREADY: "
								+ myCorrespendingJob.hasTOC());
						// check if its there already
						if (myCorrespendingJob.hasTOC()) {
							continue;// skip toc
						}
					}
					System.out.println("Merging file replace: " + otherfile);
					myCorrespendingJob.addFileReplace(otherfile, otherReplacePaths[i]);
				}
			}
		}
		return this;
	}

	// public boolean addConflictFile(String jobName, )

	/**
	 * Creates a new mod package in a folder with the same name as this mod.
	 * Creates a moddesc.ini file based on jobs in this mod.
	 */
	public void createNewMod() {
		File modFolder = new File(modName);
		modFolder.mkdirs();
		for (ModJob job : jobs) {
			File moduleDir = new File(modFolder + File.separator + getStandardFolderName(job.jobName));
			moduleDir.mkdirs();
			// scanned for matching job. Found it. Iterate over files...
			for (String mergefile : job.getNewFiles()) {
				File file = new File(mergefile);
				String baseName = FilenameUtils.getName(mergefile);
				try {
					File destinationFile = new File(moduleDir + File.separator + baseName);
					ModManager.debugLogger.writeMessage("Copying to new mod folder: " + file.getAbsolutePath() + " to "
							+ destinationFile.getAbsolutePath());
					FileUtils.copyFile(file, destinationFile);
				} catch (IOException e) {
					ModManager.debugLogger.writeMessage("IOException while merging mods.");
					ModManager.debugLogger.writeException(e);
				}
				System.out.println(job.jobName + ": " + file);
			}
		}
		try {
			ModManager.debugLogger.writeMessage("Creating moddesc.ini...");
			FileUtils.writeStringToFile(new File(modFolder + File.separator + "moddesc.ini"), createModDescIni(3.0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("IOException while merging mods.");
			ModManager.debugLogger.writeException(e);
			e.printStackTrace();
		}
		Mod newMod = new Mod(modFolder + File.separator + "moddesc.ini");
		new AutoTocWindow(null, newMod);
	}

	public static String convertNewlineToBr(String input) {
		return input.replaceAll("\n", "<br>");
	}

	public void setModName(String modName) {
		this.modName = modName;
	}
}
