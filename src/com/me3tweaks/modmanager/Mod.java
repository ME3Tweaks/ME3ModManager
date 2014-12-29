package com.me3tweaks.modmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

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
	protected int cmmVer = 1;

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
		Wini modini = new Wini(modDescFile);

		modDisplayDescription = modini.get("ModInfo", "moddesc");
		modName = modini.get("ModInfo", "modname");
		ModManager.debugLogger.writeMessage("------------------Reading "+modName+"------------------");
		// Check if this mod has been made for Mod Manager 2.0 or legacy mode
		float modcmmver = 1.0f;
		try {
			modcmmver = Float.parseFloat(modini.get("ModManager", "cmmver"));
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeMessage("Didn't read a ModManager version of the mod. Setting modtype to legacy");
			modcmmver = 1.0f;
		}

		// Backwards compatibility for mods that are built to target older versions of mod manager (NO DLC)
		if (modcmmver < 2.0f) {
			cmmVer = 1;
			ModManager.debugLogger.writeMessage("Modcmmver is less than 2, checking for coalesced.bin in folder (legacy)");
			File file = new File(ModManager.appendSlash(modPath) + "Coalesced.bin");
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
			String iniModDir = modini.get(modHeader, "moddir");
			if (iniModDir != null && !iniModDir.equals("")) {
				// It's a DLC header, we should check for the files to mod, and make sure they all match properly
				ModManager.debugLogger.writeMessage("Found INI header " + modHeader);
				
				String newFileIni = modini.get(modHeader, "newfiles");
				String oldFileIni = modini.get(modHeader, "replacefiles");
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
				cmmVer = 3;
				ModJob newJob;
				if (modHeader.equals(ModType.BASEGAME)){
					newJob = new ModJob();
				} else {
					//DLC Job
					newJob = new ModJob(ModType.getDLCPath(modHeader), modHeader);
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
					if (!(newJob.addFileReplace(ModManager.appendSlash(modDescFile.getParent()) + ModManager.appendSlash(iniModDir) + newFile, oldFile))){
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
			cmmVer = 2;
			ModManager.debugLogger.writeMessage(modName + ": Checking for modcoal in moddesc.ini because moddesc targets cmm2.0");
			
			int modCoalFlag = 0;
			try {
				modCoalFlag = Integer.parseInt(modini.get("ModInfo", "modcoal"));
				ModManager.debugLogger.writeMessage("Coalesced flag: "+modCoalFlag);

				if (modCoalFlag != 0) {
					File file = new File(ModManager.appendSlash(modPath) + "Coalesced.bin");
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
		
		//read additional parameters
		// Add MP Changer
		if (modini.get("ModInfo", "modmp") != null) {
			modmp = modini.get("ModInfo", "modmp");
		}
		// Add developer
		if (modini.get("ModInfo", "moddev") != null) {
			modAuthor = modini.get("ModInfo", "moddev");
			ModManager.debugLogger.writeMessage("Mod Marked as valid, finish reading mod.");
		}
		// Add Devsite
		if (modini.get("ModInfo", "modsite") != null) {
			modSite = modini.get("ModInfo", "modsite");
		}
		
		// Add Modmaker
		if (modini.get("ModInfo", "modid") != null) {
			modmakerCode = modini.get("ModInfo", "modid");
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

	public String getModDescription() {
		return modDisplayDescription;
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
	
	public boolean canMergeWith(Mod other) {
		for (ModJob job : jobs){
			ModJob otherCorrespondingJob = null;
			for (ModJob otherjob : other.jobs){
				if (otherjob.equals(job)) {
					otherCorrespondingJob = otherjob;
					break;
				}
			}
			if (otherCorrespondingJob == null) {
				continue;
			}
			//scanned for matching job. Found it. Iterate over files...
			for (String file : job.getFilesToReplace()) {
				if (file.equals("PCConsoleTOC.bin")) {
					continue;
				}
				for (String otherfile : otherCorrespondingJob.getFilesToReplace()) {
					if (file.equals(otherfile)) {
						return false;
					}
				}			
			}
		}
		return true;
	}
	
	public HashMap<String, ArrayList<String>> getConflictsWithMod(Mod other) {
		HashMap<String, ArrayList<String>> conflicts = new HashMap<String, ArrayList<String>>();
		for (ModJob job : jobs){
			ModJob otherCorrespondingJob = null;
			for (ModJob otherjob : other.jobs){
				if (otherjob.equals(job)) {
					otherCorrespondingJob = otherjob;
					break;
				}
			}
			if (otherCorrespondingJob == null) {
				continue;
			}
			//scanned for matching job. Found it. Iterate over files...
			for (String file : job.getFilesToReplace()) {
				if (FilenameUtils.getName(file).equals("PCConsoleTOC.bin")) {
					continue;
				}
				for (String otherfile : otherCorrespondingJob.getFilesToReplace()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.jobName)){
							conflicts.get(job.jobName).add(file);
						} else {
							ArrayList<String> conflictFiles = new ArrayList<String>();
							conflictFiles.add(file);
							conflicts.put(job.jobName, conflictFiles);
						}
					}
				}			
			}
		}
		if (conflicts.size() <= 0){
			return null;
		}
		return conflicts;
	}
	
	/**
	 * Creates a moddesc.ini string that should be written to a file that describes this mod object.
	 * @param modName Name of this mod
	 * @param modDescription Description of this mod
	 * @param folderName mod's foldername
 	 * @return moddesc.ini file as a string
	 */
	public String createModDescIni() {
		// Write mod descriptor file
		try {
			Wini ini = new Wini();
			
			//put modmanager, modinfo
			ini.put("ModManager", "cmmver", 3.0);
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
			return new String(os.toByteArray(),"ASCII");
		} catch (Exception e) {
			
		}
		return "failure";
		

			
			
			
			/*
			// Create directories, move files to them
			for (String reqcoal : requiredCoals) {
				File compCoalDir = new File(moddir.toString() + "\\"+coalFilenameToShortName(reqcoal)); //MP4, PATCH2 folders in mod package
				compCoalDir.mkdirs();
				String fileNameWithOutExt = FilenameUtils.removeExtension(reqcoal);
				//copy coal
				File coalFile = new File("coalesceds\\"+fileNameWithOutExt+"\\"+reqcoal);
				File destCoal = new File(compCoalDir+"\\"+reqcoal);
				destCoal.delete();
				if (coalFile.renameTo(destCoal)){
					ModManager.debugLogger.writeMessage("Moved "+reqcoal+" to proper mod element directory");
				} else {
					ModManager.debugLogger.writeMessage("ERROR! Didn't move "+reqcoal+" to the proper mod element directory. Could already exist.");
				}
				//copy pcconsoletoc
				File tocFile = new File("toc\\"+coalFilenameToShortName(reqcoal)+"\\PCConsoleTOC.bin");
				File destToc = new File(compCoalDir+"\\PCConsoleTOC.bin");
				destToc.delete();
				if (tocFile.renameTo(destToc)){
					ModManager.debugLogger.writeMessage("Moved "+reqcoal+" TOC to proper mod element directory");
				} else {
					ModManager.debugLogger.writeMessage("ERROR! Didn't move "+reqcoal+" TOC to the proper mod element directory. Could already exist.");
				}
				if (reqcoal.equals("Coalesced.bin")) {
					//it is basegame. copy the tlk files!
					String[] tlkFiles = {"INT", "ESN", "DEU", "ITA", "FRA", "RUS", "POL"};
					for (String tlkFilename : tlkFiles) {
						File compiledTLKFile = new File("tlk\\"+"BIOGame_"+tlkFilename+".tlk");
						if (!compiledTLKFile.exists()) {
							ModManager.debugLogger.writeMessage("TLK file "+compiledTLKFile+" is missing, might not have been compiled by this modmaker version. skipping.");
							continue;
						}
						File destTLKFile= new File(compCoalDir+"\\BIOGame_"+tlkFilename+".tlk");
						if (compiledTLKFile.renameTo(destTLKFile)){
							ModManager.debugLogger.writeMessage("Moved "+compiledTLKFile+" TLK to BASEGAME directory");
						} else {
							ModManager.debugLogger.writeMessage("ERROR! Didn't move "+compiledTLKFile+" TLK to the BASEGAME directory. Could already exist.");
						}
					}
				}

				File compCoalSourceDir = new File("coalesceds\\"+fileNameWithOutExt);
				
				//TODO: Add PCConsoleTOC.bin to the desc file.
				boolean basegame = 	reqcoal.equals("Coalesced.bin");
				ini.put(coalNameToModDescName(reqcoal), "moddir", coalFilenameToShortName(reqcoal));
				
				if (basegame) {
					StringBuilder newsb = new StringBuilder();
					StringBuilder replacesb = new StringBuilder();
					//coalesced
					newsb.append(reqcoal);
					replacesb.append(coalFileNameToDLCDir(reqcoal));
					
					//tlk, if they exist.
					String[] tlkFiles = {"INT", "ESN", "DEU", "ITA", "FRA", "RUS", "POL"};
					for (String tlkFilename : tlkFiles) {
						File basegameTLKFile = new File(compCoalDir+"\\BIOGame_"+tlkFilename+".tlk");
						if (basegameTLKFile.exists()) {
							newsb.append(";BIOGame_"+tlkFilename+".tlk");
							replacesb.append(";\\BIOGame\\CookedPCConsole\\BIOGame_"+tlkFilename+".tlk");
							continue;
						}
					}
					newsb.append(";PCConsoleTOC.bin");
					replacesb.append(";"+coalFileNameToDLCTOCDir(reqcoal));
					ini.put(coalNameToModDescName(reqcoal), "newfiles", newsb.toString());
					ini.put(coalNameToModDescName(reqcoal), "replacefiles", replacesb.toString());					
				} else {
					ini.put(coalNameToModDescName(reqcoal), "newfiles", reqcoal+";PCConsoleTOC.bin");
					ini.put(coalNameToModDescName(reqcoal), "replacefiles", coalFileNameToDLCDir(reqcoal)+";"+coalFileNameToDLCTOCDir(reqcoal));					
				}


				
				try {
					if (!ModManager.IS_DEBUG) {
						FileUtils.deleteDirectory(compCoalSourceDir);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ini.store();
			ModManager.debugLogger.writeMessage("Cleaning up...");
			try {
				FileUtils.deleteDirectory(new File("tlk"));
				FileUtils.deleteDirectory(new File("toc"));
				FileUtils.deleteDirectory(new File("coalesceds"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			ModManager.debugLogger.writeMessage("IOException in CreateCMMMod()!");
			ModManager.debugLogger.writeException(e);
		}

		//TOC the mod
		ModManager.debugLogger.writeMessage("Running autotoc on modmaker mod.");
		Mod newMod = new Mod(moddesc.toString());
		if (!newMod.validMod) {
			//SOMETHING WENT WRONG!
		}
		new AutoTocWindow(callingWindow, newMod);

		//Mod Created!
		return null;*/
	}
	
	private Object getStandardFolderName(String jobName) {
		switch (jobName){
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
	 * Creates a CMM Mod package from the completed previous steps.
	 */
	private void createCMMMod() {
		
	}

	public Mod mergeWith(Mod mod2) {
		// TODO Auto-generated method stub
		return null;
	}
}
