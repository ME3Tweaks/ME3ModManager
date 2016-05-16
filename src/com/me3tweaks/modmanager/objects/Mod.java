package com.me3tweaks.modmanager.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ini4j.BasicProfile;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.AutoTocWindow;
import com.me3tweaks.modmanager.DeltaWindow;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.utilities.ByteArrayInOutStream;
import com.me3tweaks.modmanager.utilities.ResourceUtils;
import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class Mod implements Comparable<Mod> {
	public static final String DELTAS_FOLDER = "DELTAS";
	public static final String ORIGINAL_FOLDER = "ORIGINAL";
	public static final String VARIANT_FOLDER = "VARIANTS";
	public static final String DEFAULT_SERVER_FOLDER = "PUT_SERVER_PATH_HERE";
	File modDescFile;
	boolean validMod = false;
	String modName, modDisplayDescription, modDescription;
	private String modPath;
	String modifyString;
	public ArrayList<ModJob> jobs;
	private String modAuthor;
	private int modmakerCode = 0;
	private String modSite;
	private String modmp;
	private double compiledAgainstModmakerVersion;
	private String modVersion;
	public double modCMMVer = 1.0;
	private int classicCode;
	private boolean ignoreLoadErrors = false;
	private ArrayList<Patch> requiredPatches = new ArrayList<Patch>();
	private ArrayList<ModDelta> modDeltas = new ArrayList<ModDelta>();
	private String failedReason;
	private String serverModFolder = DEFAULT_SERVER_FOLDER; //only for mod devs
	private ArrayList<AlternateFile> alternateFiles = new ArrayList<AlternateFile>();
	private ArrayList<String> sideloadOnlyTargets = new ArrayList<>();
	private String sideloadURL;
	private ArrayList<String> blacklistedFiles = new ArrayList<>();

	public String getServerModFolder() {
		return serverModFolder;
	}

	public void setServerModFolder(String serverModFolder) {
		this.serverModFolder = serverModFolder;
	}

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
		setModPath(ModManager.appendSlash(modDescFile.getParent()));
		jobs = new ArrayList<ModJob>();
		try {
			readDesc(new Wini(modDescFile));
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error reading moddesc.ini:", e);
			setValidMod(false);
			return;
		}
	}

	/**
	 * Loads a moddesc from a stream of bytes. Typically this is from a
	 * compressed archive.
	 * 
	 * @param bytes
	 */
	public Mod(ByteArrayInOutStream bytes) {
		modDescFile = new File(System.getProperty("user.dir"));
		ignoreLoadErrors = true;
		modifyString = "";
		jobs = new ArrayList<ModJob>();
		try {
			Wini wini = new Wini();
			wini.load(bytes.getInputStream());
			readDesc(wini);
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Error reading moddesc.ini from stream:", e);
			setValidMod(false);
			return;
		}
	}

	/**
	 * Empty constructor. This should not be used unless you really know what
	 * you're doing. (manually adding jobs etc)
	 * 
	 * Instantiates the jobs variable and modifystring to blank and nothing
	 * else.
	 */
	public Mod() {
		jobs = new ArrayList<ModJob>();
		modifyString = "";
	}

	/**
	 * Copy Constructor
	 * 
	 * @param mod
	 *            Mod to clone
	 */
	public Mod(Mod mod) {
		modDescFile = mod.modDescFile;
		modPath = mod.modPath;
		modName = mod.modName;
		modVersion = mod.modVersion;
		modSite = mod.modSite;
		modDisplayDescription = mod.modDisplayDescription;
		modDescription = mod.modDescription;
		validMod = mod.validMod;
		jobs = new ArrayList<ModJob>();
		modifyString = mod.modifyString;
		modAuthor = mod.modAuthor;
		modmakerCode = mod.modmakerCode;
		modmp = mod.modmp;
		compiledAgainstModmakerVersion = mod.compiledAgainstModmakerVersion;
		modCMMVer = mod.modCMMVer;
		classicCode = mod.classicCode;
		ignoreLoadErrors = mod.ignoreLoadErrors;
		failedReason = mod.failedReason;
		serverModFolder = mod.serverModFolder;
		sideloadOnlyTargets = new ArrayList<String>();
		sideloadURL = mod.sideloadURL;
		alternateFiles = new ArrayList<AlternateFile>();
		requiredPatches = new ArrayList<Patch>();
		modDeltas = new ArrayList<ModDelta>();
		for (ModJob job : mod.jobs) {
			jobs.add(new ModJob(job));
		}
		for (String str : mod.sideloadOnlyTargets) {
			sideloadOnlyTargets.add(str);
		}
		for (AlternateFile str : mod.alternateFiles) {
			alternateFiles.add(new AlternateFile(str));
		}
		for (Patch str : mod.requiredPatches) {
			requiredPatches.add(new Patch(str));
		}
		for (ModDelta str : mod.modDeltas) {
			modDeltas.add(new ModDelta(str));
		}
	}

	/**
	 * Parses the moddesc.ini file and validates it.
	 * 
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	private void readDesc(Wini modini) throws InvalidFileFormatException, IOException {
		String modFolderPath = ModManager.appendSlash(modDescFile.getParent());
		modDescription = modini.get("ModInfo", "moddesc");
		//modDisplayDescription = modini.get("ModInfo", "moddesc");
		modName = modini.get("ModInfo", "modname");
		ModManager.debugLogger.writeMessageConditionally("-------MOD----------------Reading " + modName + "--------------------", ModManager.LOG_MOD_INIT);
		// Check if this mod has been made for Mod Manager 2.0 or legacy mode
		modCMMVer = 1.0f;
		try {
			modCMMVer = Float.parseFloat(modini.get("ModManager", "cmmver"));
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeMessageConditionally("Didn't read a ModManager version of the mod. Setting modtype to legacy", ModManager.LOG_MOD_INIT);
			modCMMVer = 1.0f;
		}

		//READ NON-ESSENTIAL DATA, USEFUL FOR FAILED MODS, AS THEY WILL ABORT ON ERROR
		if (modini.get("ModInfo", "modver") != null) {
			modVersion = modini.get("ModInfo", "modver");
			ModManager.debugLogger.writeMessageConditionally("Detected mod version: " + modVersion, ModManager.LOG_MOD_INIT);
		}

		if (modini.get("ModInfo", "updatecode") != null) {
			try {
				classicCode = Integer.parseInt(modini.get("ModInfo", "updatecode"));
				ModManager.debugLogger.writeMessageConditionally("Detected me3tweaks update code: " + classicCode, ModManager.LOG_MOD_INIT);
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeError("Classic update code is not an integer. Defaulting to 0");
			}
		}

		// Add MP Change
		if (modini.get("ModInfo", "modmp") != null) {
			modmp = modini.get("ModInfo", "modmp");
			ModManager.debugLogger.writeMessageConditionally("Detected multiplayer modification", ModManager.LOG_MOD_INIT);
		}
		// Add developer
		if (modini.get("ModInfo", "moddev") != null) {
			modAuthor = modini.get("ModInfo", "moddev");
			ModManager.debugLogger.writeMessageConditionally("Detected developer name", ModManager.LOG_MOD_INIT);
		}
		// Add Devsite
		if (modini.get("ModInfo", "modsite") != null) {
			modSite = modini.get("ModInfo", "modsite");
			ModManager.debugLogger.writeMessageConditionally("Detected developer site", ModManager.LOG_MOD_INIT);
		}

		// Add Modmaker
		if (modini.get("ModInfo", "modid") != null) {
			try {
				modmakerCode = Integer.parseInt(modini.get("ModInfo", "modid"));
				ModManager.debugLogger.writeMessageConditionally("Detected modmaker code", ModManager.LOG_MOD_INIT);
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeError("ModMaker code failed to resolve to an integer: " + modini.get("ModInfo", "modid"));
			}
		}

		// Backwards compatibility for mods that are built to target older
		// versions of mod manager (NO DLC)
		if (modCMMVer < 2.0f) {
			modCMMVer = 1.0f;
			ModManager.debugLogger.writeMessageConditionally("Modcmmver is less than 2, checking for coalesced.bin in folder (legacy)", ModManager.LOG_MOD_INIT);
			if (!ignoreLoadErrors) {
				File file = new File(ModManager.appendSlash(getModPath()) + "Coalesced.bin");
				if (!file.exists() && !ignoreLoadErrors) {
					ModManager.debugLogger.writeError(modName + " doesn't have Coalesced.bin and is marked as legacy, marking as invalid.");
					setFailedReason("Mod is legacy (1.0), which requires a Coalesced.bin in the same folder as the moddesc.ini file. One is not present.");
					return;
				}
			}
			File file = new File(ModManager.appendSlash(getModPath()) + "Coalesced.bin");
			ModManager.debugLogger.writeMessageConditionally("Mod Manager 1.0 mod, verifying Coaleseced.bin location", ModManager.LOG_MOD_INIT);

			if (!file.exists() && !ignoreLoadErrors) {
				ModManager.debugLogger.writeMessageConditionally(modName + " doesn't have Coalesced.bin even though flag was set. Marking as invalid.", ModManager.LOG_MOD_INIT);
				setFailedReason(
						"Mod targets Mod Manager 1.x but the Coalesced.bin file in the mod folder doesn't exist. Place a Coalesced.bin file in the same folder as moddesc.ini or remove the modcoal descriptor.");

				return;
			} else {
				ModManager.debugLogger.writeMessageConditionally("Coalesced.bin is OK", ModManager.LOG_MOD_INIT);
			}
			ModJob job = new ModJob();
			job.addFileReplace(file.getAbsolutePath(), "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
			addTask(ModType.BASEGAME, job);

			validMod = true;
			generateModDisplayDescription();
			ModManager.debugLogger.writeMessage("Finished reading moddesc.ini file for cmm 1.0 mod " + modName);
			ModManager.debugLogger.writeMessageConditionally(modName + " targets CMM 1.0. Added coalesced swap job.", ModManager.LOG_MOD_INIT);
			ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
			return;
		}

		if (modCMMVer > 3.0f && modCMMVer < 3.1f) {
			modCMMVer = 3.0f;
		}

		//some mods shipped as 3.2
		if (modCMMVer > 3.1f && modCMMVer < 4.0f) {
			modCMMVer = 3.1;
		}

		modCMMVer = (double) Math.round(modCMMVer * 10) / 10;

		ModManager.debugLogger.writeMessageConditionally("Mod Manager version read: " + modCMMVer, ModManager.LOG_MOD_INIT);
		ModManager.debugLogger.writeMessageConditionally("Checking for DLC headers in the ini file.", ModManager.LOG_MOD_INIT);

		// It's a 2.0 or above mod. Check for mod tags in the desc file
		String[] modIniHeaders = ModType.getHeaderNameArray();
		for (String modHeader : modIniHeaders) {
			// Check for each mod. If it exists, add the task
			String iniModDir = modini.get(modHeader, "moddir");
			if (iniModDir != null && !iniModDir.equals("")) {
				// It's a DLC header, we should check for the files to mod, and
				// make sure they all match properly
				ModManager.debugLogger.writeMessageConditionally("Found INI header " + modHeader, ModManager.LOG_MOD_INIT);

				//REPLACE FILES (Mod Manager 2.0+)
				String newFileIni = modini.get(modHeader, "newfiles");
				String oldFileIni = modini.get(modHeader, "replacefiles");
				//NEW FILES (Mod Manager 4.1+)
				String addFileIni = modini.get(modHeader, "addfiles");
				String addFileTargetIni = modini.get(modHeader, "addfilestargets");
				//REMOVE FILES (Mod Manager 4.1+)
				String removeFileTargetIni = modini.get(modHeader, "removefilestargets");
				String requirementText = null;

				boolean taskDoesSomething = false;
				if (newFileIni != null && oldFileIni != null && !newFileIni.equals("") && !oldFileIni.equals("")) {
					taskDoesSomething = true;
				}
				if (addFileIni != null && addFileTargetIni != null && !addFileIni.equals("") && !addFileTargetIni.equals("")) {
					taskDoesSomething = true;
				}
				if (removeFileTargetIni != null && !removeFileTargetIni.equals("")) {
					taskDoesSomething = true;
				}

				if (!taskDoesSomething) {
					setFailedReason("Mod has a header (" + modHeader + ") with tasks that effectively do nothing. Add tasks or remove the header to fix this issue.");
					ModManager.debugLogger.writeError("This task appears to do nothing. It should be removed if this is the case. Marking mod as invalid.");
					return;
				}

				StringTokenizer newStrok = null, oldStrok = null;
				if (newFileIni != null && oldFileIni != null) {
					//Parse replace files
					newStrok = new StringTokenizer(newFileIni, ";");
					oldStrok = new StringTokenizer(oldFileIni, ";");
					if (newStrok.countTokens() != oldStrok.countTokens()) {
						// Same number of tokens aren't the same
						ModManager.debugLogger.writeError("Number of files to update/replace do not match, mod being marked as invalid.");
						setFailedReason("Mod has a header (" + modHeader
								+ ") that has different number of files to update/replace. The lists must be the same length (source files and destination paths).");
						ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
						return;
					}
				}

				//Mod Manager 4.1+ ADD/REMOVE/JOBDESCRIPTIONS
				StringTokenizer addStrok = null;
				StringTokenizer addTargetStrok = null;
				StringTokenizer removeStrok = null;
				if (modCMMVer >= 4.1) {
					//check for add/remove pairs
					if ((addFileIni != null && addFileTargetIni == null) || (addFileIni == null && addFileTargetIni != null)) {
						ModManager.debugLogger.writeError("addfiles/addtargetfiles is missing, but one is present, but both are required. Mod marked as invalid");
						setFailedReason("Mod has a header (" + modHeader + ") that has an addfiles or addtargetfiles task, but the corresponding task is not present.");
						ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
						return;
					}

					//Parse add files
					if (addFileIni != null && addFileTargetIni != null && !addFileIni.equals("") && !addFileTargetIni.equals("")) {
						addStrok = new StringTokenizer(addFileIni, ";");
						addTargetStrok = new StringTokenizer(addFileTargetIni, ";");
						if (addStrok.countTokens() != addTargetStrok.countTokens()) {
							// Same number of tokens aren't the same
							ModManager.debugLogger.writeError("Number of files to add and number of target files do not match, mod being marked as invalid.");
							setFailedReason(
									"Mod has a header (" + modHeader + ") that has an addfiles task, but the number of source files and the number of targets do not match.");
							ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
							return;
						}
					}

					//remove files doesn't need a token match, but create the tokenizer for later
					if (removeFileTargetIni != null && !removeFileTargetIni.equals("")) {
						removeStrok = new StringTokenizer(removeFileTargetIni, ";");
					}
					requirementText = modini.get(modHeader, "jobdescription");
					ModManager.debugLogger.writeMessageConditionally(modHeader + " job description: " + requirementText, ModManager.LOG_MOD_INIT);
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
				newJob.setSourceDir(iniModDir);
				if (newStrok != null && oldStrok != null)
					while (newStrok.hasMoreTokens()) {
						String newFile = newStrok.nextToken();
						String oldFile = oldStrok.nextToken();
						// ModManager.debugLogger.writeMessageConditionally("Validating tokens: "+newFile+" vs
						// "+oldFile);
						if (!newFile.equals(getSfarFilename(oldFile))) {
							setFailedReason("Mod has a newfiles/replacefiles task in a header (" + modHeader + ") that has different filenames in the source vs target paths: "
									+ newFile + " vs " + getSfarFilename(oldFile) + ". These filenames currently must be the same. This restriction may be lifted in the future.");
							ModManager.debugLogger.writeError("[REPLACEFILE]Filenames failed to match, mod marked as invalid: " + newFile + " vs " + getSfarFilename(oldFile));
							return; // The names of the files don't match
						}

						// Add the file swap to task job - if this method returns
						// false it means a file doesn't exist somewhere
						if (!(newJob.addFileReplace(modFolderPath + ModManager.appendSlash(iniModDir) + newFile, oldFile)) && !ignoreLoadErrors) {
							ModManager.debugLogger.writeError("Failed to add file to replace (File likely does not exist), marking as invalid.");
							setFailedReason("Mod has a newfiles/replacefiles task in a header (" + modHeader
									+ ") that encountered an error while building the list of source/targets. This likely means the source file does not exist: " + modFolderPath
									+ ModManager.appendSlash(iniModDir) + newFile);
							ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
							return;
						}
					}
				if (addStrok != null) {
					while (addStrok.hasMoreTokens()) {
						String addFile = addStrok.nextToken();
						String targetFile = addTargetStrok.nextToken();
						if (!addFile.equals(getSfarFilename(targetFile))) {
							ModManager.debugLogger.writeError("[ADDFILE]Filenames failed to match, mod marked as invalid: " + addFile + " vs " + getSfarFilename(targetFile));
							setFailedReason("Mod specifies an addfile/addfiles target task in (" + modHeader + "), but the filenames for source vs targets doesn't match: "
									+ addFile + " vs " + getSfarFilename(targetFile));
							ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
							return; // The names of the files don't match
						}

						// Add the file swap to task job - if this method returns
						// false it means a file doesn't exist somewhere
						if (!(newJob.addNewFileTask(modFolderPath + ModManager.appendSlash(iniModDir) + addFile, targetFile) && !ignoreLoadErrors)) {
							ModManager.debugLogger.writeError("[ADDFILE]Failed to add task for file addition (File likely does not exist), marking as invalid.");
							setFailedReason("Mod has an addfiles/addfilestargets task in a header (" + modHeader
									+ ") that encountered an error while building the list of source/targets. This likely means the source file does not exist: " + modFolderPath
									+ ModManager.appendSlash(iniModDir) + addFile);
							ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
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
							setFailedReason("Mod failed to add file for removal as part of header (" + modHeader
									+ "), his shouldn't be able to happen! The file target that failed: " + removeFilePath);
							ModManager.debugLogger.writeMessageConditionally("-----MOD------------END OF " + modName + "--------------------", ModManager.LOG_MOD_INIT);
							return;
						}
					}
				}
				ModManager.debugLogger.writeMessageConditionally(modName + ": Successfully made a new Mod Job for: " + modHeader, ModManager.LOG_MOD_INIT);
				addTask(modHeader, newJob);
			}
		}

		// CHECK FOR CUSTOMDLC HEADER (3.1+)
		if (modCMMVer >= 3.1) {
			ModManager.debugLogger.writeMessageConditionally("Mod built for CMM 3.1+, checking for CUSTOMDLC header", ModManager.LOG_MOD_INIT);
			String iniModDir = modini.get(ModType.CUSTOMDLC, "sourcedirs");
			if (iniModDir != null && !iniModDir.equals("")) {
				ModManager.debugLogger.writeMessageConditionally("Found CUSTOMDLC header", ModManager.LOG_MOD_INIT);

				//customDLC flag is set
				String sourceFolderIni = modini.get(ModType.CUSTOMDLC, "sourcedirs");
				String destFolderIni = modini.get(ModType.CUSTOMDLC, "destdirs");
				// ModManager.debugLogger.writeMessageConditionally("New files: "+newFileIni);
				// ModManager.debugLogger.writeMessageConditionally("Old Files: "+oldFileIni);
				if (sourceFolderIni == null || destFolderIni == null || sourceFolderIni.equals("") || destFolderIni.equals("")) {
					setFailedReason("Mod specifies a CUSTOMDLC header, but one or both of the lists was empty.");
					ModManager.debugLogger.writeError("sourcedirs/destdirs files was null or empty, mod marked as invalid.");
					return;
				}

				StringTokenizer srcStrok = new StringTokenizer(sourceFolderIni, ";");
				StringTokenizer destStrok = new StringTokenizer(sourceFolderIni, ";");
				if (srcStrok.countTokens() != destStrok.countTokens()) {
					// Same number of tokens aren't the same
					setFailedReason("Mod specifies a CUSTOMDLC header, but the list of source directories and target directories didn't match.");
					ModManager.debugLogger.writeError("Number of source and destination directories for custom DLC job do not match, mod being marked as invalid.");
					return;
				}

				ModJob newJob = new ModJob();
				newJob.setJobName(ModType.CUSTOMDLC); //backwards, it appears...
				newJob.setJobType(ModJob.CUSTOMDLC);
				newJob.setSourceFolders(new ArrayList<String>());
				newJob.setDestFolders(new ArrayList<String>());

				while (srcStrok.hasMoreTokens()) {
					String sourceFolder = srcStrok.nextToken();
					String destFolder = destStrok.nextToken();

					File sf = new File(modFolderPath + sourceFolder);
					if (!sf.exists() && !ignoreLoadErrors) {
						setFailedReason("Mod specifies a CUSTOMDLC header, but one of the source directories doesn't list: " + sf.getAbsolutePath());
						ModManager.debugLogger.writeError("Custom DLC Source folder does not exist: " + sf.getAbsolutePath() + ", mod marked as invalid");
						return;
					}
					if (ModType.isKnownDLCFolder(destFolder)) {
						// Same number of tokens aren't the same
						setFailedReason(
								"Mod specifies a CUSTOMDLC header, but one of the target directories isn't allowed as it is a known official DLC foldername: " + destFolder);
						ModManager.debugLogger.writeError("Custom DLC folder is not allowed as it is a default game one: " + destFolder);
						return;
					}

					if (!destFolder.startsWith("DLC_")) {
						setFailedReason(
								"Mod specifies a CUSTOMDLC destination folder that doesn't start with DLC_. When installing this mod, this will do nothing because Mass Effect 3 will not load any DLC that does not start with DLC_." + destFolder);
						ModManager.debugLogger.writeError("Custom DLC target folder doesn't start with DLC_: " + destFolder);
						return;
					}

					if (sf.exists()) { //ignore errors is not present here.
						List<File> sourceFiles = (List<File>) FileUtils.listFiles(sf, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
						for (File file : sourceFiles) {
							String relativePath = ResourceUtils.getRelativePath(file.getAbsolutePath(), sf.getAbsolutePath(), File.separator);
							String destFilePath = ModManager.appendSlash(destFolder) + relativePath;
							if (!(newJob.addFileReplace(ResourceUtils.normalizeFilePath(file.getAbsolutePath()), ResourceUtils.normalizeFilePath(destFilePath))
									&& !ignoreLoadErrors)) {
								setFailedReason("Mod specifies a CUSTOMDLC header, but a file in one of the source directories (" + sf
										+ ") had a file that was unable to be added. This error should be encountered.");
								ModManager.debugLogger.writeError("Failed to add file to replace (File likely does not exist), marking as invalid.");
								return;
							}
						}
					}
					newJob.getSourceFolders().add(sourceFolder);
					newJob.getDestFolders().add(destFolder);
				}
				ModManager.debugLogger.writeMessageConditionally(modName + ": Successfully made a new Mod Job for: " + ModType.CUSTOMDLC, ModManager.LOG_MOD_INIT);
				addTask(ModType.CUSTOMDLC, newJob);
			}

			//In 3.1+, we can check for 4.2+ since this won't trigger on lower
			//MOD MANAGER 4.2.3: Support [CUSTOMDLC]>altfiles, Support [UPDATES]
			if (modCMMVer >= 4.2) {
				String altText = modini.get("CUSTOMDLC", "altfiles");
				if (altText != null && !altText.equals("")) {
					ArrayList<String> alts = ValueParserLib.getSplitValues(altText);
					if (alts == null) {
						//error
						ModManager.debugLogger.writeError("ALTERNATES altfiles was specified, but the value couldn't be parsed. It's likely invalid: " + altText);
						setFailedReason(
								"The moddesc indicates there should be files installed differently depending on certain conditions, but it failed to parse. The [ALTERNATES] altfiles text that failed to parse was :"
										+ altText + "\n\nThis may not affect your specific ME3 enviroment, but the mod has been marked as invalid to avoid possible issues.");
						return;
					}
					for (String alt : alts) {
						AlternateFile af = new AlternateFile(alt);
						ModManager.debugLogger.writeMessageConditionally("Alternate file specified: " + af.toString(), ModManager.LOG_MOD_INIT);
						alternateFiles.add(af);
					}
				}
				if (modini.get("UPDATES", "serverfolder") != null) {
					serverModFolder = modini.get("UPDATES", "serverfolder");
					ModManager.debugLogger.writeMessageConditionally("Detected me3tweaks server folder: " + classicCode, ModManager.LOG_MOD_INIT);
				}
				sideloadURL = modini.get("UPDATES", "sideloadurl");
				String sideloadonly = modini.get("UPDATES", "sideloadonly");
				if (sideloadURL == null && sideloadonly != null) {
					ModManager.debugLogger.writeError("Mod specifies sideload only files but does not specify sideloading link");
					setFailedReason(
							"This mod specifies a list of files that can only be updated via sideloading but does not specify a sideload URL. This can lead to a state where users will be unable to update the mod. Provide a (valid) sideload URL to fix this by adding 'sideloadurl=<URL>' under [UPDATES] in moddesc.ini.");
					return;
				}
				if (sideloadonly != null) {
					StringTokenizer sideloadTok = new StringTokenizer(sideloadonly, ";");
					while (sideloadTok.hasMoreTokens()) {
						String sideloadonlyfile = sideloadTok.nextToken();
						sideloadonlyfile = sideloadonlyfile.replaceAll("\\\\", "/");
						ModManager.debugLogger.writeMessageConditionally("Sideload only file for manifest: " + sideloadonlyfile, ModManager.LOG_MOD_INIT);
						sideloadOnlyTargets.add(sideloadonlyfile);
					}
				}
				String blacklisted = modini.get("UPDATES", "blacklistedfiles");
				if (blacklisted != null) {
					StringTokenizer blacklistedTok = new StringTokenizer(blacklisted, ";");
					while (blacklistedTok.hasMoreTokens()) {
						String blacklistedfile = blacklistedTok.nextToken();
						blacklistedfile = blacklistedfile.replaceAll("\\\\", "/");
						ModManager.debugLogger.writeMessageConditionally("Blacklisted file for manifests: " + blacklistedfile, ModManager.LOG_MOD_INIT);
						getBlacklistedFiles().add(blacklistedfile);
					}
				}
			}
		}

		// Backwards compatibility for Mod Manager 2's modcoal flag (has now
		// moved to [BASEGAME] as of 3.0)
		if (modCMMVer < 3.0f && modCMMVer >= 2.0f) {
			modCMMVer = 2.0;
			ModManager.debugLogger.writeMessageConditionally(modName + ": Targets CMM2.0. Checking for modcoal flag", ModManager.LOG_MOD_INIT);

			int modCoalFlag = 0;
			try {
				modCoalFlag = Integer.parseInt(modini.get("ModInfo", "modcoal"));
				ModManager.debugLogger.writeMessageConditionally("Coalesced flag: " + modCoalFlag, ModManager.LOG_MOD_INIT);

				if (modCoalFlag != 0) {
					File file = new File(ModManager.appendSlash(getModPath()) + "Coalesced.bin");
					ModManager.debugLogger.writeMessageConditionally("Coalesced flag was set, verifying its location", ModManager.LOG_MOD_INIT);

					if (!file.exists() && !ignoreLoadErrors) {
						ModManager.debugLogger.writeMessageConditionally(modName + " doesn't have Coalesced.bin even though flag was set. Marking as invalid.",
								ModManager.LOG_MOD_INIT);
						setFailedReason(
								"Mod targets Mod Manager 2.0 and specifies a Coalesced file should be present, but one doesn't exist. Place a Coalesced.bin file in the same folder as moddesc.ini or remove the modcoal descriptor.");

						return;
					} else {
						ModManager.debugLogger.writeMessageConditionally("Coalesced.bin is OK", ModManager.LOG_MOD_INIT);
					}
					ModJob job = new ModJob();
					job.addFileReplace(file.getAbsolutePath(), "\\BIOGame\\CookedPCConsole\\Coalesced.bin");
					addTask(ModType.BASEGAME, job);
				}
			} catch (NumberFormatException e) {
				ModManager.debugLogger.writeMessageConditionally("Was not able to read the coalesced mod value. Coal flag was not set/not entered, skipping setting coal",
						ModManager.LOG_MOD_INIT);
			}
		}

		ModManager.debugLogger.writeMessageConditionally("Number of Mod Jobs:" + jobs.size(), ModManager.LOG_MOD_INIT);
		if (jobs.size() > 0) {
			ModManager.debugLogger.writeMessageConditionally("Verified source files, mod should be OK to install", ModManager.LOG_MOD_INIT);
			validMod = true;
		}

		//modmaker compiledagainst flag (1.5+)
		if (modini.get("ModInfo", "compiledagainst") != null) {
			try {
				compiledAgainstModmakerVersion = Double.parseDouble(modini.get("ModInfo", "compiledagainst"));
				ModManager.debugLogger.writeMessageConditionally("Server version compiled against: " + compiledAgainstModmakerVersion, ModManager.LOG_MOD_INIT);
				if (compiledAgainstModmakerVersion < 1.5) {
					try {
						int ver = Integer.parseInt(modVersion);
						ver++;
						modVersion = Integer.toString(ver);
						ModManager.debugLogger.writeMessageConditionally("ModMaker mod (<1.5), +1 to revision.", ModManager.LOG_MOD_INIT);
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
				ModManager.debugLogger.writeMessageConditionally("Unknown server version, assuming 1.4 compilation target: " + compiledAgainstModmakerVersion,
						ModManager.LOG_MOD_INIT);
				try {
					int ver = Integer.parseInt(modVersion);
					ver++;
					modVersion = Integer.toString(ver);
					ModManager.debugLogger.writeMessageConditionally("ModMaker mod (<1.5), +1 to revision.", ModManager.LOG_MOD_INIT);
				} catch (NumberFormatException e) {
					ModManager.debugLogger.writeError("ModMaker version failed to resolve to an integer.");
					modVersion = Integer.toString(1);
				}
			}
		}

		if (modCMMVer > ModManager.MODDESC_VERSION_SUPPORT) {
			ModManager.debugLogger.writeError("Mod is for newer version of Mod Manager, may have issues with this version.");
		}
		//check for patches directory
		if (modCMMVer >= 2 && !ignoreLoadErrors /*
												 * don't bother with patches on
												 * compressed
												 */) {
			File patchesdir = new File(modFolderPath + "patches/");
			if (patchesdir.isDirectory()) {
				ModManager.debugLogger.writeMessageConditionally("Mod has unprocessed patches! Importing patches now.", ModManager.LOG_MOD_INIT);
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
						ModManager.debugLogger.writeMessageConditionally("Valid patch: " + subPatch.getPatchName() + ", importing to library and processing",
								ModManager.LOG_MOD_INIT);
						subPatch = subPatch.importPatch();
						requiredPatches.add(subPatch);
					}
				}
				if (patchesdir.list().length <= 0) {
					ModManager.debugLogger.writeMessageConditionally("All patches imported. Deleting patches directory.", ModManager.LOG_MOD_INIT);
					FileUtils.deleteDirectory(patchesdir);
				}
			}
		}

		//Read deltas
		File dir = new File(modFolderPath + DELTAS_FOLDER);
		File[] deltas = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
		if (deltas != null) {
			for (File delta : deltas) {
				ModDelta md = new ModDelta(delta.getAbsolutePath());
				if (md.isValidDelta()) {
					modDeltas.add(md);
				}
			}
			if (modDeltas.size() > 0) {
				ModManager.debugLogger.writeMessageConditionally("This mod has " + modDeltas.size() + " deltas.", ModManager.LOG_MOD_INIT);
			}
		}

		//Verify alternates and apply automatic ones
		if (alternateFiles != null) {
			ModManager.debugLogger.writeMessage("Verifying automatic alternate files are valid for this mod");
			HashMap<String, ArrayList<String>> autoOriginalFiles = new HashMap<String, ArrayList<String>>(); //header to altfiles map
			for (AlternateFile af : alternateFiles) {
				ModManager.debugLogger.writeMessage("Verifying " + af.getAltFile() + " on " + af.getCondition() + " of " + af.getConditionalDLC());
				if (af.isValidLocally(modPath)) {
					//Verify pass
					ModManager.debugLogger.writeMessageConditionally("This mod has " + modDeltas.size() + " deltas.", ModManager.LOG_MOD_INIT);
					String condition = af.getCondition();
					String modfile = af.getModFile();
					String task = af.getConditionalDLC();

					if (!condition.equals(AlternateFile.CONDITION_MANUAL)) {
						ArrayList<String> headerAlternates = autoOriginalFiles.get(task);
						if (headerAlternates != null) {
							if (headerAlternates.contains(modfile.toLowerCase())) {
								//auto alts cannot apply to the same thing
								ModManager.debugLogger.writeError("Automatic alternate files contains duplicate for same file: " + modfile);
								setFailedReason("This mod specifies automatic conditional changes for the following file in more than 1 instance: " + modfile
										+ "\n\nEach file in Mod Manager mods can only have 1 automatically applied conditional operation attached to it.");
								return;
							} else {
								headerAlternates.add(modfile.toLowerCase());
							}
						} else {
							ArrayList<String> alts = new ArrayList<>();
							alts.add(modfile.toLowerCase());
							autoOriginalFiles.put(task, alts);
						}
					}
				} else {
					ModManager.debugLogger.writeError("Invalid alternate file specified: " + af + ". Some pieces of required information may be missing.");
					setFailedReason("This mod contains an invalid alternate file specification:\n" + af);
					return;
				}
			}

			for (AlternateFile af : alternateFiles) {
				//Application pass

			}
		}

		generateModDisplayDescription();
		ModManager.debugLogger.writeMessage("Finished loading moddesc.ini for " + getModName());
		ModManager.debugLogger.writeMessageConditionally("-------MOD----------------END OF " + modName + "--------------------------", ModManager.LOG_MOD_INIT);
	}

	public ArrayList<ModDelta> getModDeltas() {
		return modDeltas;
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
		/*
		 * if (name.equals(ModType.COAL)) { modCoal = true;
		 * updateModifyString(ModType.COAL); return; }
		 */
		if (name.equals(ModType.CUSTOMDLC)) {
			String appendStr = name + " (";
			boolean first = true;
			for (String destFolder : newJob.getDestFolders()) {
				if (!first) {
					appendStr += ",";
					first = false;
				}
				appendStr += destFolder;
			}
			appendStr += ")";
			updateModifyString(appendStr);
		} else {
			updateModifyString(name);
		}
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

	public void generateModDisplayDescription() {
		modDisplayDescription = "This mod has no description in it's moddesc.ini file or there was an error reading the description of this mod.";
		if (modDescFile == null) {
			ModManager.debugLogger.writeMessage("Mod Desc file is null, unable to read description");
			return;
		}
		modDisplayDescription = breakFixer(modDescription);

		modDisplayDescription += "\n=============================\n";

		//Available deltas
		if (modDeltas.size() > 0) {
			modDisplayDescription += "Included Variants:\n";
			for (ModDelta delta : modDeltas) {
				modDisplayDescription += " - " + delta.getDeltaName();
				modDisplayDescription += "\n";
			}
		}

		// Add modversion
		if (modVersion != null) {
			modDisplayDescription += "\nMod Version: " + modVersion;
		}

		// Add developer
		if (modAuthor != null) {
			modDisplayDescription += "\nMod Developer: " + modAuthor;
		}

		// Add mod manager build version
		modDisplayDescription += "\nTargets Mod Manager " + modCMMVer;
		if (classicCode > 0 /* && ModManager.IS_DEBUG */) {
			modDisplayDescription += "\nUpdate code: " + classicCode;
		}
		if (getSideloadOnlyTargets().size() > 0) {
			modDisplayDescription += "\nFuture updates may require sideloading an update package";
		}

		// Add MP Changer
		if (modmp != null) {
			modDisplayDescription += "\nModifies Multiplayer: " + modmp;
		}

		// Add Modmaker
		if (modmakerCode > 0) {
			//modmakerCode = modini.get("ModInfo", "modid");
			modDisplayDescription += "\nModMaker code: " + modmakerCode;
		}

		// Add modifier
		modDisplayDescription += getModifyString();
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
		ModManager.debugLogger.writeMessage("Checking if mods can cleanly merge. Will report only first failure.");
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

			//Compare my replace files to others replace/remove 
			for (String file : job.getFilesToReplaceTargets()) {
				if (FilenameUtils.getName(file).equals("PCConsoleTOC.bin")) {
					continue;
				}
				ModManager.debugLogger.writeMessage("==Checking file for conflicts " + file + "==");

				for (String otherfile : otherCorrespondingJob.getFilesToReplaceTargets()) {
					ModManager.debugLogger.writeMessage("Comparing replace files: " + file + " vs " + otherfile);
					if (file.equalsIgnoreCase(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge conflicts with file to update " + file);
						return false;
					}
				}
				for (String otherfile : otherCorrespondingJob.getFilesToRemoveTargets()) {
					if (file.equalsIgnoreCase(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge conflicts with file to update " + file);
						return false;
					}
				}
			}

			//Compare my replace files to others replace/remove 
			for (String file : job.getFilesToRemoveTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToReplaceTargets()) {
					if (file.equalsIgnoreCase(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge would add a task to remove a file requiring replace: " + file);
						return false;
					}
				}
			}

			//compare files to add
			for (String file : job.getFilesToAdd()) {
				for (String otherfile : otherCorrespondingJob.getFilesToAdd()) {
					if (file.equalsIgnoreCase(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge conflicts with file to add " + file);
						return false;
					}
				}
			}

			//Compare my add files to others remove
			for (String file : job.getFilesToAddTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToRemoveTargets()) {
					if (file.equalsIgnoreCase(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge conflicts with file to add/remove " + file);
						return false;
					}
				}
			}
			for (String file : job.getFilesToRemoveTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToAddTargets()) {
					if (file.equalsIgnoreCase(otherfile)) {
						ModManager.debugLogger.writeMessage("Merge conflicts with file to add/remove " + file);
						return false;
					}
				}
			}

			//don't care about files to remove. I think...

		}
		ModManager.debugLogger.writeMessage("No conflicted detected.");
		return true;
	}

	/**
	 * Gets the list of files that conflict with the specified mod, in terms of
	 * files to replace.
	 * 
	 * @param other
	 *            Other mod to compare against
	 * @return hashmap of job names mapped to a list of strings of conflicting
	 *         file names
	 */
	public HashMap<String, ArrayList<String>> getReplaceConflictsWithMod(Mod other) {
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
			for (String file : job.getFilesToReplaceTargets()) {
				if (FilenameUtils.getName(file).equals("PCConsoleTOC.bin")) {
					continue;
				}
				for (String otherfile : otherCorrespondingJob.getFilesToReplaceTargets()) {
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
	 * Gets the list of files that conflict with the specified mod, in terms of
	 * files to replace.
	 * 
	 * @param other
	 *            Other mod to compare against
	 * @return hashmap of job names mapped to a list of strings of conflicting
	 *         file names
	 */
	public HashMap<String, ArrayList<String>> getAddConflictsWithMod(Mod other) {
		HashMap<String, ArrayList<String>> conflicts = new HashMap<String, ArrayList<String>>();
		for (ModJob job : jobs) {
			System.out.println("ADDCONF: JOB PASS:" + job.getJobName());
			ModJob otherCorrespondingJob = null;
			for (ModJob otherjob : other.jobs) {
				if (otherjob.equals(job)) {
					System.out.println("ADDCONF: FOUND SAME JOB: " + job.getJobName());
					otherCorrespondingJob = otherjob;
					break;
				}
			}
			if (otherCorrespondingJob == null) {
				System.out.println("ADDCONF: NO CORR JOB");
				continue;
			}
			// scanned for matching job. Found it. Iterate over files...
			for (String file : job.getFilesToAddTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToAddTargets()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.getJobName())) {
							conflicts.get(job.getJobName()).add(file);
							ModManager.debugLogger.writeMessage("ADDING TO ADD CONFLICT LIST: " + file);
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
			System.out.println("ADDCONF: CONF SIZE 0");
			return null;
		}
		System.out.println("ADDCONF: RET NOT NULL");
		return conflicts;
	}

	/**
	 * Gets the list of files that conflict with the specified mod, in terms of
	 * files to remove.
	 * 
	 * @param other
	 *            Other mod to compare against
	 * @return hashmap of job names mapped to a list of strings of conflicting
	 *         file names
	 */
	public HashMap<String, ArrayList<String>> getRemoveConflictsWithMod(Mod other) {
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
			for (String file : job.getFilesToRemoveTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToRemoveTargets()) {
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
	 * @param keepUpdaterCode
	 * 
	 * @param modName
	 *            Name of this mod
	 * @param modDescription
	 *            Description of this mod
	 * @param folderName
	 *            mod's foldername
	 * @return moddesc.ini file as a string
	 */
	public String createModDescIni(boolean keepUpdaterCode, double cmmVersion) {
		// Write mod descriptor file
		try {
			Wini ini = new Wini();

			// put modmanager, modinfo
			ini.put("ModManager", "cmmver", cmmVersion);
			ini.put("ModInfo", "modname", modName);
			ini.put("ModInfo", "moddev", modAuthor);
			if (modDescription != null) {
				ini.put("ModInfo", "moddesc", ResourceUtils.convertNewlineToBr(modDescription));
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
			if (keepUpdaterCode && isME3TweaksUpdatable()) {
				if (getClassicUpdateCode() > 0) {
					ini.put("ModInfo", "updatecode", getClassicUpdateCode());
				} else {
					ini.put("ModInfo", "me3tweaksid", getModMakerCode());
				}
			}

			for (ModJob job : jobs) {
				boolean isFirst = true;
				if (job.getRequirementText() != null && !job.getRequirementText().equals("")) {
					ini.put(job.getJobName(), "jobdescription", job.getRequirementText());
				}

				if (job.getJobType() == ModJob.CUSTOMDLC) {
					StringBuilder sfsb = new StringBuilder();
					StringBuilder dfsb = new StringBuilder();

					//source dirs
					for (String file : job.getSourceFolders()) {
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
				isFirst = true;
				for (String file : job.getFilesToReplace()) {
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
				for (String file : job.getFilesToReplaceTargets()) {
					System.out.println("appending: " + file);
					if (isFirst) {
						isFirst = false;
					} else {
						rfsb.append(";");
					}
					rfsb.append(file);
				}

				//add files list
				isFirst = true;
				StringBuilder afsb = new StringBuilder();
				for (String file : job.getFilesToAdd()) {
					if (isFirst) {
						isFirst = false;
					} else {
						afsb.append(";");
					}
					afsb.append(FilenameUtils.getName(file));
				}

				//addfilestargets files list
				isFirst = true;
				StringBuilder aftsb = new StringBuilder();
				for (String file : job.getFilesToAddTargets()) {
					if (isFirst) {
						isFirst = false;
					} else {
						aftsb.append(";");
					}
					aftsb.append(file);
				}

				//removefiles list
				isFirst = true;
				StringBuilder rftsb = new StringBuilder();
				for (String file : job.getFilesToRemoveTargets()) {
					if (isFirst) {
						isFirst = false;
					} else {
						rftsb.append(";");
					}
					rftsb.append(file);
				}

				//DLC, basegame
				ini.put(job.getJobName(), "newfiles", nfsb.toString());
				ini.put(job.getJobName(), "replacefiles", rfsb.toString());

				if (job.getFilesToAdd().size() > 0) {
					ini.put(job.getJobName(), "addfiles", afsb.toString());
				}

				if (job.getFilesToAddTargets().size() > 0) {
					ini.put(job.getJobName(), "addfilestargets", aftsb.toString());
				}

				if (job.getFilesToRemoveTargets().size() > 0) {
					ini.put(job.getJobName(), "removefilestargets", rftsb.toString());
				}
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
		case "FROM_ASHES":
			return "FROM_ASHES";
		case "EXTENDED_CUT":
			return "EXTENDED_CUT";
		case "LEVIATHAN":
			return "LEVIATHAN";
		case "OMEGA":
			return "OMEGA";
		case "CITADEL":
			return "CITADEL";
		case "CITADEL_BASE":
			return "CITADEL_BASE";
		case "APPEARANCE":
			return "APPEARANCE";
		case "FIREFIGHT":
			return "FIREFIGHT";
		case "GROUNDSIDE":
			return "GROUNDSIDE";
		case "GENESIS2":
			return "GENESIS2";
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
		ModManager.debugLogger.writeMessage("Merging mods: " + getModName() + " <= " + other.getModName());
		this.modName = newName;
		HashMap<String, ArrayList<String>> ignoreReplaceFiles = getReplaceConflictsWithMod(other);
		HashMap<String, ArrayList<String>> ignoreAddFiles = getAddConflictsWithMod(other);
		HashMap<String, ArrayList<String>> ignoreReplaceRemoveFiles = getReplaceRemoveConflictsWithMod(other);
		HashMap<String, ArrayList<String>> ignoreAddRemoveFiles = getAddRemoveConflictsWithMod(other);

		//HashMap<String, ArrayList<String>> ignoreRemoveFiles = getRemoveConflictsWithMod(other);

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

			//MERGE REPLACE FILES
			ArrayList<String> otherNewFiles = otherjob.getFilesToReplace();
			ArrayList<String> otherReplacePaths = otherjob.getFilesToReplaceTargets();
			for (int i = 0; i < otherNewFiles.size(); i++) {
				String otherfile = otherNewFiles.get(i);
				ModManager.debugLogger.writeMessage("Merging job: " + myCorrespendingJob.getJobName());

				//Check for bigger other TOC (means it likely has extra files)
				if (FilenameUtils.getName(otherfile).equals("PCConsoleTOC.bin")) {
					for (int z = 0; z < myCorrespendingJob.getFilesToReplace().size(); z++) {
						String myJobFile = myCorrespendingJob.getFilesToReplace().get(z);
						System.out.println("My job files: " + myJobFile);
						if (FilenameUtils.getName(myJobFile).equals("PCConsoleTOC.bin")) {
							//comparing 2 of the same TOC files
							File myToc = new File(myJobFile);
							File otherToc = new File(otherfile);
							if (otherToc.length() > myToc.length()) {
								//other has more files. we sould use it instead
								ModManager.debugLogger.writeMessage("Other job has bigger TOC, using that one instead. Likely adds a file");
								myCorrespendingJob.getFilesToReplace().set(z, otherfile);
								break;
							}
						}
					}
				}

				if (ignoreReplaceFiles != null && ignoreReplaceFiles.get(otherjob.getJobName()) != null
						&& ignoreReplaceFiles.get(otherjob.getJobName()).contains(otherReplacePaths.get(i))) {
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

			//MERGE ADD FILES
			ArrayList<String> otherAddFiles = otherjob.getFilesToAdd();
			ArrayList<String> otherAddFilesTargets = otherjob.getFilesToAddTargets();
			for (int i = 0; i < otherAddFiles.size(); i++) {
				String otherfile = otherAddFiles.get(i);
				ModManager.debugLogger.writeMessage("CURRENT JOB: " + myCorrespendingJob.getJobName());
				//skip add/add conflict
				if (ignoreAddFiles != null && ignoreAddFiles.get(otherjob.getJobName()) != null
						&& ignoreAddFiles.get(otherjob.getJobName()).contains(otherAddFilesTargets.get(i))) {
					ModManager.debugLogger.writeMessage("SKIPPING CONFLICT MERGE: " + otherAddFilesTargets.get(i));
					continue;
				}
				//skip add/remove conflict
				if (ignoreAddRemoveFiles != null && ignoreAddRemoveFiles.get(otherjob.getJobName()) != null
						&& ignoreAddRemoveFiles.get(otherjob.getJobName()).contains(otherAddFilesTargets.get(i))) {
					ModManager.debugLogger.writeMessage("SKIPPING CONFLICT MERGE: " + otherAddFilesTargets.get(i));
					continue;
				} else {
					if (FilenameUtils.getName(otherfile).equals("PCConsoleTOC.bin")) {
						ModManager.debugLogger.writeMessage("CHECKING IF SHOULD ADD TOC, EXIST IN THIS JOB ALREADY: " + myCorrespendingJob.hasTOC());
						// check if its there already
						if (myCorrespendingJob.hasTOC()) {
							continue;// skip toc
						}
					}
					ModManager.debugLogger.writeMessage("Merging file add: " + otherfile);
					myCorrespendingJob.addNewFileTask(otherfile, otherAddFilesTargets.get(i));
				}
			}

			//MERGE REMOVE FILES
			ArrayList<String> otherRemoveFiles = otherjob.getFilesToRemoveTargets();
			for (int i = 0; i < otherRemoveFiles.size(); i++) {
				ModManager.debugLogger.writeMessage("CURRENT JOB: " + myCorrespendingJob.getJobName());

				//ignore replace/remove conflict
				if (ignoreReplaceRemoveFiles != null && ignoreReplaceRemoveFiles.get(otherjob.getJobName()) != null
						&& ignoreReplaceRemoveFiles.get(otherjob.getJobName()).contains(otherRemoveFiles.get(i))) {
					ModManager.debugLogger.writeMessage("SKIPPING CONFLICT MERGE: " + otherRemoveFiles.get(i));
					continue;
				}

				//ignore add/remove conflict
				if (ignoreAddRemoveFiles != null && ignoreAddRemoveFiles.get(otherjob.getJobName()) != null
						&& ignoreAddRemoveFiles.get(otherjob.getJobName()).contains(otherRemoveFiles.get(i))) {
					ModManager.debugLogger.writeMessage("SKIPPING CONFLICT MERGE: " + otherRemoveFiles.get(i));
					continue;
				} else {
					ModManager.debugLogger.writeMessage("Merging file remove: " + otherRemoveFiles.get(i));
					myCorrespendingJob.addRemoveFileTask(otherRemoveFiles.get(i));
				}
			}
		}

		//MERGE DELTAS
		for (ModDelta delta : other.getModDeltas()) {
			modDeltas.add(delta);
		}

		if (other.modCMMVer > modCMMVer) {
			//upgrade to highest cmm ver
			modCMMVer = other.modCMMVer;
		}

		modmakerCode = 0; //disable modmaker updates
		return this;
	}

	/**
	 * Creates a new mod package in a folder with the same name as this mod.
	 * Copies files to the new directory based on the name of this mod. Creates
	 * a moddesc.ini file based on jobs in this mod.
	 * 
	 * @param otherMergingMod
	 *            Other mod this one is merging with. This can be null. This is
	 *            used to find the Custom DLC directory to copy to the new mod
	 *            if one isn't present for this mod.
	 */
	public Mod createNewMod(Mod otherMergingMod) {
		printModContents();
		File modFolder = new File(ModManager.getModsDir() + modName);
		modFolder.mkdirs();
		for (ModJob job : jobs) {
			if (job.getJobType() == ModJob.CUSTOMDLC) {
				for (String sourceFolder : job.getSourceFolders()) {
					try {
						File srcFolder = new File(ModManager.appendSlash(modDescFile.getParentFile().getAbsolutePath()) + sourceFolder);
						if (!srcFolder.exists() && otherMergingMod != null) {
							//may be in other mod as we haven't copied it yet and we don't use file pointers here (folder names)
							srcFolder = new File(ModManager.appendSlash(otherMergingMod.modDescFile.getParentFile().getAbsolutePath()) + sourceFolder);
						}
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
			//Copy files to replace
			for (String mergefile : job.getFilesToReplace()) {
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
			//copy files to add
			for (String mergefile : job.getFilesToAdd()) {
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

		//COPY DELTAS
		if (modDeltas.size() > 0) {
			File dir = new File(modFolder + File.separator + DELTAS_FOLDER);
			dir.mkdirs();
			for (ModDelta delta : modDeltas) {
				try {
					FileUtils.copyFile(new File(delta.getDeltaFilepath()), new File(dir + File.separator + FilenameUtils.getName(delta.getDeltaFilepath())));
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Unable to copy delta:", e);
				}
			}
		}

		try {
			ModManager.debugLogger.writeMessage("Creating moddesc.ini...");
			FileUtils.writeStringToFile(new File(modFolder + File.separator + "moddesc.ini"), createModDescIni(false, modCMMVer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeMessage("IOException while merging mods.");
			ModManager.debugLogger.writeException(e);
			e.printStackTrace();
		}
		Mod newMod = new Mod(modFolder + File.separator + "moddesc.ini");
		for (ModDelta delta : newMod.getModDeltas()) {
			new DeltaWindow(newMod, delta, true, true);
		}
		if (newMod.isValidMod()) {
			new AutoTocWindow(newMod, AutoTocWindow.LOCALMOD_MODE, ModManagerWindow.ACTIVE_WINDOW.fieldBiogameDir.getText());
		} else {
			return null;
		}
		return newMod;
	}

	private void printModContents() {
		ModManager.debugLogger.writeMessage("========" + modName + "========");
		for (ModJob job : jobs) {
			if (job.getJobType() == ModJob.CUSTOMDLC) {
				for (String sourceFolder : job.getSourceFolders()) {
					ModManager.debugLogger.writeMessage(job.getJobName() + ": " + sourceFolder);
				}
				continue;
			}
			for (String sourceFile : job.getFilesToAdd()) {
				ModManager.debugLogger.writeMessage(job.getJobName() + ": " + sourceFile);
			}
		}
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
			System.out.println(job);
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
		return getModPath() + File.separator + "moddesc.ini";
	}

	public double getCMMVer() {
		return modCMMVer;
	}

	/**
	 * Returns if this mod is able to check for updates on ME3Tweaks
	 * 
	 * @return true if classic and has update code, or has modmaker code and
	 *         version > 0
	 */
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
		if (header.equals(ModType.COAL)) {
			header = ModType.BASEGAME;
			modulePath = "\\BIOGame\\CookedPCConsole\\Coalesced.bin";
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

	/**
	 * Checks for conflicts with the other mod pertaining to add/replace.
	 * 
	 * @param other
	 * @return Hashmap of job names mapping to a list of targets that conflict
	 */
	public HashMap<String, ArrayList<String>> getAddRemoveConflictsWithMod(Mod other) {
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
			for (String file : job.getFilesToAddTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToRemoveTargets()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.getJobName())) {
							conflicts.get(job.getJobName()).add(file);
							ModManager.debugLogger.writeMessage("ADDING TO ADDREMOVE CONFLICT LIST: " + file);
						} else {
							ArrayList<String> conflictFiles = new ArrayList<String>();
							conflictFiles.add(file);
							conflicts.put(job.getJobName(), conflictFiles);
							ModManager.debugLogger.writeMessage("ADDING TO ADDREMOVE CONFLICT LIST: " + file);
						}
					}
				}
			}

			//versa
			for (String file : job.getFilesToRemoveTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToAddTargets()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.getJobName())) {
							conflicts.get(job.getJobName()).add(file);
							ModManager.debugLogger.writeMessage("ADDING TO ADDREMOVE CONFLICT LIST: " + file);
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
	 * Checks for conflicts with the other mod pertaining to remove/replace.
	 * 
	 * @param other
	 * @return Hashmap of job names mapping to a list of targets that conflict
	 */
	public HashMap<String, ArrayList<String>> getReplaceRemoveConflictsWithMod(Mod other) {
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
			for (String file : job.getFilesToReplaceTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToRemoveTargets()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.getJobName())) {
							conflicts.get(job.getJobName()).add(file);
							ModManager.debugLogger.writeMessage("ADDING TO REPLACEREMOVE CONFLICT LIST: " + file);
						} else {
							ArrayList<String> conflictFiles = new ArrayList<String>();
							conflictFiles.add(file);
							conflicts.put(job.getJobName(), conflictFiles);
						}
					}
				}
			}

			//versa
			for (String file : job.getFilesToRemoveTargets()) {
				for (String otherfile : otherCorrespondingJob.getFilesToReplaceTargets()) {
					if (file.equals(otherfile)) {
						if (conflicts.containsKey(job.getJobName())) {
							conflicts.get(job.getJobName()).add(file);
							ModManager.debugLogger.writeMessage("ADDING TO REPLACEREMOVE CONFLICT LIST: " + file);
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
	 * Finds a source file using the specified target file
	 * 
	 * @param targetFile
	 *            target to find match
	 * @param useReplace
	 *            search replacements. Otherwise search adds
	 * @return null if not found (should not occur!) or the string source path
	 */
	public String findTargetSourceFileFromJob(boolean useReplace, ModJob job, String targetFile) {
		ArrayList<String> sourceList = useReplace ? job.getFilesToReplace() : job.getFilesToAdd();
		ArrayList<String> targetList = useReplace ? job.getFilesToReplaceTargets() : job.getFilesToAddTargets();

		for (int i = 0; i < sourceList.size(); i++) {
			if (targetList.get(i).equals(targetFile)) {
				return sourceList.get(i);
			}
		}
		return null;
	}

	/**
	 * Gets a modjob from this mod by the name
	 * 
	 * @param moduleName
	 *            module job to return
	 * @return null if not found, job otherwise
	 */
	public ModJob getJobByModuleName(String moduleName) {
		for (ModJob job : jobs) {
			if (job.getJobName().equals(moduleName)) {
				return job;
			}
		}
		return null;
	}

	public void setAuthor(String modAuthor) {
		this.modAuthor = modAuthor;
	}

	public void setSite(String modSite) {
		this.modSite = modSite;
	}

	public String getModSite() {
		return modSite;
	}

	public String getFailedReason() {
		return failedReason;
	}

	public void setFailedReason(String failedReason) {
		setValidMod(false);
		this.failedReason = failedReason;
	}

	public void setModPath(String modPath) {
		this.modPath = modPath;
	}

	public ArrayList<String> getSideloadOnlyTargets() {
		return sideloadOnlyTargets;
	}

	public void setSideloadOnlyTargets(ArrayList<String> sideloadOnlyTargets) {
		this.sideloadOnlyTargets = sideloadOnlyTargets;
	}

	public String getSideloadURL() {
		return sideloadURL;
	}

	public void setSideloadURL(String sideloadURL) {
		this.sideloadURL = sideloadURL;
	}

	/**
	 * Applies automatic alternate files to this mod
	 * 
	 * @param biogamedir
	 *            biogame directory
	 */
	public boolean applyAutomaticAlternates(String biogamedir) {
		if (alternateFiles.size() > 0) {
			boolean altApplied = false;
			ModManager.debugLogger.writeMessage(getModName() + ": Checking automatic alternate files list to see if applicable and will apply if conditions are right");
			//get list of installed DLC
			ArrayList<String> installedDLC = ModManager.getInstalledDLC(biogamedir);
			ArrayList<String> officialDLCHeaders = new ArrayList<String>(Arrays.asList(ModType.getDLCHeaderNameArray()));

			for (AlternateFile af : alternateFiles) {
				ModManager.debugLogger.writeMessage("Checking if Alt file applies: " + af);
				String condition = af.getCondition();
				String conditionaldlc = af.getConditionalDLC();
				if (!installedDLC.contains(conditionaldlc)) {
					//check if its a header...
					if (officialDLCHeaders.contains(conditionaldlc)) {
						//convert to dlc name
						HashMap<String, String> headerFolderMap = ModType.getHeaderFolderMap();
						String fname = headerFolderMap.get(conditionaldlc);
						if (fname != null) {
							conditionaldlc = fname;
						} else {
							ModManager.debugLogger.writeError("[alt file application] Alt file specifies non-existent Custom DLC/Mod Manager Header: " + conditionaldlc);
							continue;
						}
					}
				}

				switch (condition) {
				case AlternateFile.CONDITION_DLC_NOT_PRESENT:
					if (!installedDLC.contains(conditionaldlc.toUpperCase())) {
						ModJob job = null;
						if (officialDLCHeaders.contains(conditionaldlc)) {
							//official DLC task
							job = getJobByModuleName(conditionaldlc);
						} else {
							//custom dlc task
							job = getJobByModuleName(ModType.CUSTOMDLC);
						}
						applyAlternateOperation(job, af);
						altApplied = true;
					}
					break;
				case AlternateFile.CONDITION_DLC_PRESENT:
					if (installedDLC.contains(conditionaldlc.toUpperCase())) {
						ModJob job = null;
						if (officialDLCHeaders.contains(conditionaldlc)) {
							//official DLC task
							job = getJobByModuleName(conditionaldlc);
						} else {
							//custom dlc task
							job = getJobByModuleName(ModType.CUSTOMDLC);
						}
						applyAlternateOperation(job, af);
						altApplied = true;
					}
					break;
				}
			}
			return altApplied;
		} else {
			return false; //no operations performed
		}
	}

	private void applyAlternateOperation(ModJob job, AlternateFile af) {
		System.out.println("break");
		switch (af.getOperation()) {
		case AlternateFile.OPERATION_INSTALL:
			//add file to task
			if (job.getFilesToReplace().contains(getModPath() + af.getAltFile())) {
				return; //file to replace already exists, user may have already applied mod in session.
			}
			ModManager.debugLogger.writeMessage("Condition match, " + af.getModFile() + " will now install new file " + af.getAltFile());
			job.getFilesToReplace().add(getModPath() + af.getAltFile());
			job.getFilesToReplaceTargets().add(af.getModFile());
			break;
		case AlternateFile.OPERATION_NOINSTALL:
			ModManager.debugLogger.writeMessage("Condition match, will no longer modify " + af.getModFile());
			boolean ftr = job.getFilesToReplace().remove(getModPath() + af.getAltFile());
			boolean ftrt = job.getFilesToReplaceTargets().remove(af.getModFile());
			if (ftr ^ ftrt) {
				ModManager.debugLogger.writeError(
						"Application of NO_INSTALL has caused mod to become invalid as one of the replace lists didn't contain the correct values, but the other one did.");
			}
			break;
		case AlternateFile.OPERATION_SUBSTITUTE:
			ModManager.debugLogger.writeMessage("Condition match, repointing " + af.getModFile() + " to use " + af.getAltFile());
			int index = job.getFilesToReplaceTargets().indexOf(af.getModFile());
			job.getFilesToReplace().set(index, getModPath() + af.getAltFile());
			break;
		}
	}

	public ArrayList<String> getBlacklistedFiles() {
		return blacklistedFiles;
	}

	public void setBlacklistedFiles(ArrayList<String> blacklistedFiles) {
		this.blacklistedFiles = blacklistedFiles;
	}
}
