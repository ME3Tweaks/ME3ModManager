package com.me3tweaks.modmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class Mod {
	File modDescFile;
	boolean validMod = false, modCoal = false;
	String modName, modDesc, modPath, modifyString;
	ArrayList<ModJob> jobs;

	public boolean isValidMod() {
		return validMod;
	}

	private void setValidMod(boolean validMod) {
		this.validMod = validMod;
	}

	/**
	 * Creates a new mod object.
	 * @param filepath Path to the moddesc.ini file.
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
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	private void readDesc() throws InvalidFileFormatException, IOException {
		Wini wini = new Wini(modDescFile);

		modDesc = wini.get("ModInfo", "moddesc");
		modName = wini.get("ModInfo", "modname");
		ModManager.debugLogger.writeMessage("------------------Reading "+modName+"------------------");
		// Check if this mod has been made for Mod Manager 2.0 or legacy mode
		float modcmmver = 1.0f;
		try {
			modcmmver = Float.parseFloat(wini.get("ModManager", "cmmver"));
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeMessage("Didn't read a ModManager version of the mod. Setting modtype to legacy");
			modcmmver = 1.0f;
		}

		// Backwards compatibility for mods that are built to target older versions of mod manager (NO DLC)
		if (modcmmver < 2.0f) {
			ModManager.debugLogger.writeMessage("Modcmmver is less than 2, checking for coalesced.bin in folder (legacy)");
			File file = new File(ModManagerWindow.appendSlash(modPath) + "Coalesced.bin");
			if (!file.exists()) {
				ModManager.debugLogger.writeMessage(modName + " doesn't have Coalesced.bin and is marked as legacy, marking as invalid.");
				return;
			}

			addTask(ModType.COAL, null);
			setModDescription(true);

			validMod = true;
			ModManager.debugLogger.writeMessage(modName + " valid, marked as legacy mod. Added coalesced swap job, marked valid, finish reading mod.");
			ModManager.debugLogger.writeMessage("--------------------------END OF "+modName+"--------------------------");
			return;
		}
		
		ModManager.debugLogger.writeMessage("Mod Manager version read was >= 2.0, marked as modern style mod.");
		ModManager.debugLogger.writeMessage("Checking for DLC headers in the ini file.");
		
		// It's a 2.0 or above mod. Check for mod tags in the desc file
		String[] modIniHeaders = ModType.getHeaderNameArray();
		for (String modHeader : modIniHeaders) {
			ModManager.debugLogger.writeMessage("Scanning for header: " + modHeader + " in ini of " + modName);
			
			// Check for each mod. If it exists, add the task
			String iniModDir = wini.get(modHeader, "moddir");
			if (iniModDir != null && !iniModDir.equals("")) {
				// It's a DLC header, we should check for the files to mod, and make sure they all match properly
				ModManager.debugLogger.writeMessage("Found INI header " + modHeader);
				
				String newFileIni = wini.get(modHeader, "newfiles");
				String oldFileIni = wini.get(modHeader, "replacefiles");
				// System.out.println("New files: "+newFileIni);
				// System.out.println("Old Files: "+oldFileIni);
				if (newFileIni == null || oldFileIni == null || newFileIni.equals("") || oldFileIni.equals("")) {
					ModManager.debugLogger.writeMessage("newfiles/replace files was null or empty, mod marked as invalid.");
					return;
				}

				StringTokenizer newStrok = new StringTokenizer(newFileIni, ";");
				StringTokenizer oldStrok = new StringTokenizer(oldFileIni, ";");
				if (newStrok.countTokens() != oldStrok.countTokens()) {
					// Same number of tokens aren't the same
					ModManager.debugLogger.writeMessage("Number of files to update/replace do not match, mod being marked as invalid.");
					return;
				}

				// Check to make sure the filenames are the same, and if they are, then the mod is going to be valid.
				// Start building the mod job.
				ModJob newJob;
				if (modHeader.equals(ModType.BASEGAME)){
					newJob = new ModJob();
				} else {
					//DLC Job
					newJob = new ModJob(ModType.getDLCPath(modHeader));
					if (modHeader.equals(ModType.TESTPATCH)) {
						newJob.TESTPATCH = true;
					}
				}
				while (newStrok.hasMoreTokens()) {
					String newFile = newStrok.nextToken();
					String oldFile = oldStrok.nextToken();
					// System.out.println("Validating tokens: "+newFile+" vs "+oldFile);
					if (!newFile.equals(getSfarFilename(oldFile))) {
						ModManager.debugLogger.writeMessage("Filenames failed to match, mod marked as invalid: " + newFile + " vs " + getSfarFilename(oldFile));
						return; // The names of the files don't match
					}
					
					//Add the file swap to task job - if this method returns false it means a file doesn't exist somewhere
					if (!(newJob.addFileReplace(ModManagerWindow.appendSlash(modDescFile.getParent()) + ModManagerWindow.appendSlash(iniModDir) + newFile, oldFile))){
						ModManager.debugLogger.writeMessage("Failed to add file to replace (File likely does not exist), marking as invalid.");
						return;
					}
				}
				ModManager.debugLogger.writeMessage(modName + ": Successfully made a new Mod Job for: " + modHeader);
				addTask(modHeader, newJob);
			}
		}

		// Backwards compatibility for Mod Manager 2's modcoal flag (has now moved to [BASEGAME] as of 3.0)
		if (modcmmver < 3.0f && modcmmver>=2.0f) {
			ModManager.debugLogger.writeMessage(modName + ": Checking for modcoal in moddesc.ini because moddesc targets cmm2.0");
			
			int modCoalFlag = 0;
			try {
				modCoalFlag = Integer.parseInt(wini.get("ModInfo", "modcoal"));
				ModManager.debugLogger.writeMessage("Coalesced flag: "+modCoalFlag);

				if (modCoalFlag != 0) {
					File file = new File(ModManagerWindow.appendSlash(modPath) + "Coalesced.bin");
					ModManager.debugLogger.writeMessage("Coalesced flag was set, verifying its location");
					
					if (!file.exists()) {
						ModManager.debugLogger.writeMessage(modName + " doesn't have Coalesced.bin even though flag was set. Marking as invalid.");
						return;
					}
					addTask(ModType.COAL, null);
				}
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessage("Was not able to read the coalesced mod value. Coal flag was not set/not entered, skipping setting coal");
			}
		}
		// Check for coalesced in the new mod manager version [2.0 only] (modcoal)

		
		
		
			ModManager.debugLogger.writeMessage("Finished reading mod.");
			if (modcmmver < 3.0f) {
				//only print if it's not able to use BASEGAME
				ModManager.debugLogger.writeMessage("Coalesced Swap: "+modCoal);
			}
			ModManager.debugLogger.writeMessage("Number of Mod Jobs:" +jobs.size());
		if (jobs.size() > 0 || modCoal == true) {
			ModManager.debugLogger.writeMessage("Mod Marked as valid, finish reading mod.");
			setModDescription(false);
			validMod = true;
		}
		ModManager.debugLogger.writeMessage("--------------------------END OF "+modName+"--------------------------");
		
	}

	/**
	 * Gets the mod's modification path
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
	 * @param taskName name of task to modify (EARTH, Coalesced, etc)
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

	public void setModDescription(boolean markedLegacy) {
		modDesc = "This mod has no description in it's moddesc.ini file or there was an error reading the description of this mod.";
		if (modDescFile == null) {
				ModManager.debugLogger.writeMessage("Mod Desc file is null, unable to read description");
			return;
		}
		Wini modini = null;
		try {
			modini = new Wini(modDescFile);
			modDesc = modini.get("ModInfo", "moddesc");
			modDesc = breakFixer(modDesc);
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
			ModManager.debugLogger.writeMessage("Invalid File Format exception on moddesc.");
			return;
		} catch (IOException e) {
			ModManager.debugLogger.writeMessage("I/O Error reading mod file. It may not exist or it might be corrupt.");
			return;
		}
		modDesc = breakFixer(modDesc);

		// Add 1st newline
		modDesc += "\n";

		// Add modversion
		if (modini.get("ModInfo", "modver") != null) {
			modDesc += "\nMod Version: " + modini.get("ModInfo", "modver");
		}
		// Add mod manager build version
		if (markedLegacy) {
			modDesc += "\nLegacy Mod";
		}

		// Add modifier
		modDesc += getModifyString();

		// Add MP Changer
		if (modini.get("ModInfo", "modmp") != null) {
			modDesc += "\nModifies Multiplayer: " + modini.get("ModInfo", "modmp");
		}
		// Add developer
		if (modini.get("ModInfo", "moddev") != null) {
			modDesc += "\nMod Developer: " + modini.get("ModInfo", "moddev");
		}
		// Add Devsite
		if (modini.get("ModInfo", "modsite") != null) {
			modDesc += "\nMod Site: " + modini.get("ModInfo", "modsite");
		}
	}

	public String getModDescription() {
		return modDesc;
	}

	/**
	 * Replaces all break (br between <>) lines with a newline character. Used to add newlines to ini4j.
	 * 
	 * @param string
	 *            String to parse
	 * @return String that has been fixed
	 */
	private String breakFixer(String string) {
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
}
