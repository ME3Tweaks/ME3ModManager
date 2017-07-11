package com.me3tweaks.modmanager.modmaker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;

/**
 * Utilities class for interfacing with ME3Tweaks and ModMaker
 * 
 * @author Mgamerz
 *
 */
public class ME3TweaksUtils {
	public static final int FILENAME = 0;
	public static final int HEADER = 1;
	public static final int INTERNAL = 2;

	private static HashMap<String, String> coalHashMap, tocHashMap;

	/**
	 * Downloads a pristine coalesced to the correct pristine directory. This
	 * method should be executed in the background.
	 * 
	 * @param name
	 *            What to download (uses mode to figure out what this is)
	 * @param mode
	 *            indicates what what the name parameter is, which will convert
	 *            to the correct filename on the server
	 * @return newly download pristine file, null otherwise (if failed)
	 */
	public static String downloadPristineCoalesced(String name, int mode) {
		ModManager.debugLogger.writeMessage("Getting pristine Coalesced for: " + name + " with mode " + mode);
		String filename = "error";
		String standardFolder = "UNKNOWN_DEFAULT_FOLDER";
		switch (mode) {
		//convert to headers so standard folder works
		case FILENAME:
			filename = name;
			standardFolder = Mod.getStandardFolderName(coalFilenameToHeaderName(name));
			break;
		case HEADER:
			filename = headerNameToCoalFilename(name);
			standardFolder = Mod.getStandardFolderName(name);
			break;
		case INTERNAL:
			filename = internalNameToCoalFilename(name);
			standardFolder = Mod.getStandardFolderName(internalNameToHeaderName(name));
			break;
		}

		String link = "https://me3tweaks.com/coal/" + filename;
		File target = new File(ModManager.getPristineDir() + standardFolder + File.separator + filename);
		target.delete();
		try {
			FileUtils.copyURLToFile(new URL(link), target);
			ModManager.debugLogger.writeMessage("Downloaded Pristine Coalesced to: " + target.getAbsolutePath());
		} catch (MalformedURLException e) {
			ModManager.debugLogger.writeException(e);
			return null;
		} catch (IOException e) {
			ModManager.debugLogger.writeException(e);
			return null;
		}
		return target.getAbsolutePath();
	}

	/**
	 * Downloads a pristine TOC to the correct pristine directory. This method
	 * should be executed in the background.
	 * 
	 * @param name
	 *            What to download (uses mode to figure out what this is)
	 * @param mode
	 *            indicates what what the name parameter is, which will convert
	 *            to the correct filename on the server
	 * @return newly download pristine file, null otherwise (if failed)
	 */
	public static String downloadPristineTOC(String name, int mode) {
		ModManager.debugLogger.writeMessage("Getting pristine TOC for: " + name + " with mode " + mode);
		String filename = "PCConsoleTOC.bin";
		String standardFolder = "UNKNOWN_DEFAULT_FOLDER";
		switch (mode) {
		//convert to headers so standard folder works
		case FILENAME:
			standardFolder = Mod.getStandardFolderName(coalFilenameToHeaderName(name));
			break;
		case HEADER:
			standardFolder = Mod.getStandardFolderName(name);
			break;
		case INTERNAL:
			standardFolder = Mod.getStandardFolderName(internalNameToHeaderName(name));
			break;
		}

		String link = "https://me3tweaks.com/toc/" + standardFolder + "/" + filename;
		File target = new File(ModManager.getPristineDir() + standardFolder + File.separator + filename);
		target.delete();
		try {
			FileUtils.copyURLToFile(new URL(link), target);
			ModManager.debugLogger.writeMessage("Downloaded Pristine TOC to: " + target.getAbsolutePath());
		} catch (MalformedURLException e) {
			ModManager.debugLogger.writeException(e);
			return null;
		} catch (IOException e) {
			ModManager.debugLogger.writeException(e);
			return null;
		}
		return target.getAbsolutePath();
	}

	/**
	 * Downloads the JDiffTools if they don't exist in the tools dir. If they do
	 * then this method does nothing.
	 */
	public static void downloadJDiffTools() {
		ModManager.debugLogger.writeMessage("Downloading JoJo Diff Tools");

		String difflink = "https://me3tweaks.com/modmanager/tools/jdiff.exe";
		String patchlink = "https://me3tweaks.com/modmanager/tools/jptch.exe";

		File diffTarget = new File(ModManager.getToolsDir() + "jdiff.exe");
		File patchTarget = new File(ModManager.getToolsDir() + "jptch.exe");
		try {
			if (!diffTarget.exists()) {
				FileUtils.copyURLToFile(new URL(difflink), diffTarget);
				ModManager.debugLogger.writeMessage("Downloaded jdiff.exe to: " + diffTarget.getAbsolutePath());
			}
			if (!patchTarget.exists()) {
				FileUtils.copyURLToFile(new URL(patchlink), patchTarget);
				ModManager.debugLogger.writeMessage("Downloaded jptch.exe to: " + patchTarget.getAbsolutePath());
			}
		} catch (MalformedURLException e) {
			ModManager.debugLogger.writeException(e);
			return;
		} catch (IOException e) {
			ModManager.debugLogger.writeException(e);
			return;
		}
		return;
	}

	/**
	 * Converts a coal filename (Default_DLC_CON_MP3.bin) into the internal name
	 * (MP3)
	 * 
	 * @param coalName
	 *            Filename to convert to a short name
	 * @return Internal name or null if unknown.
	 */
	public static String coalFilenameToInternalName(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "MP1";
		case "Default_DLC_CON_MP2.bin":
			return "MP2";
		case "Default_DLC_CON_MP3.bin":
			return "MP3";
		case "Default_DLC_CON_MP4.bin":
			return "MP4";
		case "Default_DLC_CON_MP5.bin":
			return "MP5";
		case "Default_DLC_UPD_Patch01.bin":
			return "PATCH1";
		case "Default_DLC_UPD_Patch02.bin":
			return "PATCH2";
		case "Coalesced.bin":
			return "BASEGAME";
		case "Default_DLC_TestPatch.bin":
			return "TESTPATCH";
		case "Default_DLC_HEN_PR.bin":
			return "FROM_ASHES";
		case "Default_DLC_CON_APP01.bin":
			return "APPEARANCE";
		case "Default_DLC_CON_GUN01.bin":
			return "FIREFIGHT";
		case "Default_DLC_CON_GUN02.bin":
			return "GROUNDSIDE";
		case "Default_DLC_CON_END.bin":
			return "EXTENDED_CUT";
		case "Default_DLC_EXP_Pack001.bin":
			return "LEVIATHAN";
		case "Default_DLC_EXP_Pack002.bin":
			return "OMEGA";
		case "Default_DLC_EXP_Pack003.bin":
			return "CITADEL";
		case "Default_DLC_EXP_Pack003_Base.bin":
			return "CITADEL_BASE";
		case "ServerCoalesced.bin":
			return "BALANCE_CHANGES";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: HEADER => INTERNAL " + coalName);
			return null;
		}
	}

	public static String coalFilenameToHeaderName(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "RESURGENCE";
		case "Default_DLC_CON_MP2.bin":
			return "REBELLION";
		case "Default_DLC_CON_MP3.bin":
			return "EARTH";
		case "Default_DLC_CON_MP4.bin":
			return "RETALIATION";
		case "Default_DLC_CON_MP5.bin":
			return "RECKONING";
		case "Default_DLC_UPD_Patch01.bin":
			return "PATCH1";
		case "Default_DLC_UPD_Patch02.bin":
			return "PATCH2";
		case "Coalesced.bin":
			return "BASEGAME";
		case "Default_DLC_TestPatch.bin":
			return "TESTPATCH";
		case "Default_DLC_HEN_PR.bin":
			return "FROM_ASHES";
		case "Default_DLC_CON_APP01.bin":
			return "APPEARANCE";
		case "Default_DLC_CON_GUN01.bin":
			return "FIREFIGHT";
		case "Default_DLC_CON_GUN02.bin":
			return "GROUNDSIDE";
		case "Default_DLC_CON_END.bin":
			return "EXTENDED_CUT";
		case "Default_DLC_EXP_Pack001.bin":
			return "LEVIATHAN";
		case "Default_DLC_EXP_Pack002.bin":
			return "OMEGA";
		case "Default_DLC_EXP_Pack003.bin":
			return "CITADEL";
		case "Default_DLC_EXP_Pack003_Base.bin":
			return "CITADEL_BASE";
		case "ServerCoalesced.bin":
			return "BALANCE_CHANGES";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: FILENAME => HEADER " + coalName);
			return null;
		}
	}

	/**
	 * Converts ModDesc.ini headers and job names into ModMaker-style internal
	 * names
	 * 
	 * @param header
	 * @return
	 */
	public static String headerNameToInternalName(String header) {
		switch (header) {
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
		case "BASEGAME":
			return "BASEGAME";
		case "TESTPATCH":
			return "TESTPATCH";
		case "FROM_ASHES":
			return "FROM_ASHES";
		case "APPEARANCE":
			return "APPEARANCE";
		case "FIREFIGHT":
			return "FIREFIGHT";
		case "GROUNDSIDE":
			return "GROUNDSIDE";
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
		case "GENESIS2":
			return "GENESIS2";
		case "COLLECTORS_EDITION":
			return "OnlinePassHidCE";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: HEADER => INTERNAL " + header);
			return null;
		}
	}

	/**
	 * Converts ModDesc.ini headers and job names into DLC folder names. These
	 * should be used for making mod folders. This is not the folders in the
	 * game that a DLC is placed in.
	 * 
	 * @param header
	 *            Header to lookup
	 * @return local DLC name, such as MP1 or HEN_PR
	 */
	public static String headerNameToShortDLCFolderName(String header) {
		switch (header) {
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
		case "BASEGAME":
			return "BASEGAME";
		case "TESTPATCH":
			return "TESTPATCH";
		case "FROM_ASHES":
			return "HEN_PR";
		case "APPEARANCE":
			return "APP01";
		case "FIREFIGHT":
			return "GUN01";
		case "GROUNDSIDE":
			return "GUN02";
		case "EXTENDED_CUT":
			return "END";
		case "LEVIATHAN":
			return "EXP001";
		case "OMEGA":
			return "EXP002";
		case "CITADEL":
			return "EXP003";
		case "CITADEL_BASE":
			return "EXP003_BASE";
		case "GENESIS2":
			return "DH1";
		case "COLLECTORS_EDITION":
			return "OnlinePassHidCE";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: HEADER => INTERNAL " + header);
			return null;
		}
	}

	public static String headerNameToCoalFilename(String header) {
		switch (header) {
		case "RESURGENCE":
			return "Default_DLC_CON_MP1.bin";
		case "REBELLION":
			return "Default_DLC_CON_MP2.bin";
		case "EARTH":
			return "Default_DLC_CON_MP3.bin";
		case "RETALIATION":
			return "Default_DLC_CON_MP4.bin";
		case "RECKONING":
			return "Default_DLC_CON_MP5.bin";
		case "PATCH1":
			return "Default_DLC_UPD_Patch01.bin";
		case "PATCH2":
			return "Default_DLC_UPD_Patch02.bin";
		case "BASEGAME":
			return "Coalesced.bin";
		case "TESTPATCH":
			return "Default_DLC_TestPatch.bin";
		case "FROM_ASHES":
			return "Default_DLC_HEN_PR.bin";
		case "APPEARANCE":
			return "Default_DLC_CON_APP01.bin";
		case "FIREFIGHT":
			return "Default_DLC_CON_GUN01.bin";
		case "GROUNDSIDE":
			return "Default_DLC_CON_GUN02.bin";
		case "EXTENDED_CUT":
			return "Default_DLC_CON_END.bin";
		case "LEVIATHAN":
			return "Default_DLC_EXP_Pack001.bin";
		case "OMEGA":
			return "Default_DLC_EXP_Pack002.bin";
		case "CITADEL":
			return "Default_DLC_EXP_Pack003.bin";
		case "CITADEL_BASE":
			return "Default_DLC_EXP_Pack003_Base.bin";
		case "BALANCE_CHANGES":
			return "ServerCoalesced.bin";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: HEADER => FILENAME " + header);
			return null;
		}
	}

	/**
	 * 
	 * Converts an internal/modmaker-style coal name into the Coalesced filename
	 * it represents e.g. MP3=> (Default_DLC_CON_MP3.bin).
	 * 
	 * @param internal
	 *            Short name to convert
	 * @return Coaleced filename or null if unknown.
	 */
	public static String internalNameToCoalFilename(String internal) {
		switch (internal) {
		case "MP1":
			return "Default_DLC_CON_MP1.bin";
		case "MP2":
			return "Default_DLC_CON_MP2.bin";
		case "MP3":
			return "Default_DLC_CON_MP3.bin";
		case "MP4":
			return "Default_DLC_CON_MP4.bin";
		case "MP5":
			return "Default_DLC_CON_MP5.bin";
		case "PATCH1":
			return "Default_DLC_UPD_Patch01.bin";
		case "PATCH2":
			return "Default_DLC_UPD_Patch02.bin";
		case "BASEGAME":
			return "Coalesced.bin";
		case "TESTPATCH":
			return "Default_DLC_TestPatch.bin";
		case "FROM_ASHES":
			return "Default_DLC_HEN_PR.bin";
		case "APPEARANCE":
			return "Default_DLC_CON_APP01.bin";
		case "FIREFIGHT":
			return "Default_DLC_CON_GUN01.bin";
		case "GROUNDSIDE":
			return "Default_DLC_CON_GUN02.bin";
		case "EXTENDED_CUT":
			return "Default_DLC_CON_END.bin";
		case "LEVIATHAN":
			return "Default_DLC_EXP_Pack001.bin";
		case "OMEGA":
			return "Default_DLC_EXP_Pack002.bin";
		case "CITADEL":
			return "Default_DLC_EXP_Pack003.bin";
		case "CITADEL_BASE":
			return "Default_DLC_EXP_Pack003_Base.bin";
		case "BALANCE_CHANGES":
			return "ServerCoalesced.bin";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: NO MATCH INTERNAL => FILENAME: " + internal);
			return null;
		}
	}

	/**
	 * Converts Internal (MP3) names into header names (EARTH)
	 * 
	 * @param internal
	 * @return
	 */
	public static String internalNameToHeaderName(String internal) {
		switch (internal) {
		case "MP1":
			return "RESURGENCE";
		case "MP2":
			return "REBELLION";
		case "MP3":
			return "EARTH";
		case "MP4":
			return "RETALIATION";
		case "MP5":
			return "RECKONING";
		case "PATCH1":
			return "PATCH1";
		case "PATCH2":
			return "PATCH2";
		case "BASEGAME":
			return "BASEGAME";
		case "TESTPATCH":
			return "TESTPATCH";
		case "FROM_ASHES":
			return "FROM_ASHES";
		case "APPEARANCE":
			return "APPEARANCE";
		case "FIREFIGHT":
			return "FIREFIGHT";
		case "GROUNDSIDE":
			return "GROUNDSIDE";
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
		case "BALANCE_CHANGES":
			return "BALANCE_CHANGES";
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: INTERNAL => HEADER " + internal);
			return null;
		}
	}

	public static HashMap<String, String> getCoalHashesMap() {
		if (coalHashMap != null) {
			return coalHashMap;
		}
		coalHashMap = new HashMap<String, String>();
		coalHashMap.put(ModType.BASEGAME, "540053c7f6eed78d92099cf37f239e8e");
		coalHashMap.put(ModType.MP1, "7206b8f7a3cadb5f1e425263638816b4");
		coalHashMap.put(ModType.MP2, "48d9ceaa751e850cfc4fe39bd72339f3");
		coalHashMap.put(ModType.MP3, "0493ab300be5513cad3bbcc1670b22ff");
		coalHashMap.put(ModType.MP4, "35eb556e8757b265d3ee934a489796b4");
		coalHashMap.put(ModType.MP5, "48eb9900ccbcd4927be7988e3939a765");
		coalHashMap.put(ModType.PATCH1, "a3d61c61f4e6dfff4167d46c188f2dba");
		coalHashMap.put(ModType.PATCH2, "3bf6ce760fcdd69e656ef10a1eb08692");
		coalHashMap.put(ModType.TESTPATCH, "82f2ddaf4ecc9c60c2d083740069653a");
		coalHashMap.put(ModType.HEN_PR, "d8007a2a44af5682c86dbdf327d44864");
		coalHashMap.put(ModType.END, "e94e2fb635c01c6edcea018cc7b9701c");
		coalHashMap.put(ModType.EXP1, "363a36372ef369b5ea8f00f662e467e4");
		coalHashMap.put(ModType.EXP2, "c13410248a3bc6de4d653622d150eb80");
		coalHashMap.put(ModType.EXP3, "8646b866e7660e7056681f14dcd2db76");
		coalHashMap.put(ModType.EXP3B, "fbfd633f640eccf5f21ac1603a137d5a");
		coalHashMap.put(ModType.APP01, "efe108aa8f2142734c07d888b79f4c0b");
		coalHashMap.put(ModType.GUN01, "fe9084127be47b8c084dad67c64cd211");
		coalHashMap.put(ModType.GUN02, "f3231b3855fdf288482a541b147dfca9");
		coalHashMap.put(ModType.BINI, "1d3e646cdf9da8bcb8207d8fd961f7f5");
		coalHashMap.put(ModType.COLLECTORSEDITION, "");
		coalHashMap.put(ModType.DH1, "");
		return coalHashMap;
	}

	public static HashMap<String, String> getTOCHashesMap() {
		if (tocHashMap != null) {
			return tocHashMap;
		}
		tocHashMap = new HashMap<String, String>();
		tocHashMap.put(ModType.BASEGAME, "07e157a9bc1bb7ee13f0310d8b165f08");
		tocHashMap.put(ModType.MP1, "4ffa2aab35ba7e16243b8cf573629f0a");
		tocHashMap.put(ModType.MP2, "7136eb641f5c1dca0e9c54583ad7560f");
		tocHashMap.put(ModType.MP3, "25e8ae6f428a6d09058175b81d59451d");
		tocHashMap.put(ModType.MP4, "ec1035b4153959a5a7f051616a5f257b");
		tocHashMap.put(ModType.MP5, "74e86f1189403b01975a67d27dd0bc99");
		tocHashMap.put(ModType.PATCH1, "ddc9f8aca8b4a1eabab5f28966b09718");
		tocHashMap.put(ModType.PATCH2, "32e5f7f628e16b53546a345a63a76d82");
		tocHashMap.put(ModType.TESTPATCH, "e5e7d1199145ebc08d6c1508318c2b55");
		tocHashMap.put(ModType.HEN_PR, "0fcf41d28e5b8fdb4440068e45f9781d");
		tocHashMap.put(ModType.END, "977880ad14ab246171f5c2c9e9975174");
		tocHashMap.put(ModType.EXP1, "5ae9c8cfac5867982d3ec15adc2ba037");
		tocHashMap.put(ModType.EXP2, "5497ab73be4c12ca9e4e0303d090ea72");
		tocHashMap.put(ModType.EXP3, "5d8eb55ef0150b8e644c8dccb814f617");
		tocHashMap.put(ModType.EXP3B, "0771adb534768b3a0229ca6f57e9ec5b");
		tocHashMap.put(ModType.APP01, "21c396e4ae50b4d8b3cf55fe2b9c0722");
		tocHashMap.put(ModType.GUN01, "53f06f917f27af46af25cae77f595d75");
		tocHashMap.put(ModType.GUN02, "6c26b453dfaf663ebaeeafeac78440c2");
		tocHashMap.put(ModType.COLLECTORSEDITION, "");
		tocHashMap.put(ModType.DH1, "");
		return tocHashMap;
	}

	/**
	 * Converts the Coalesced.bin filenames to their respective PCConsoleTOC
	 * directory in the .sfar files.
	 * 
	 * @param coalName
	 *            name of coal being packed into the mod
	 * @return path to the file to repalce
	 */
	public static String coalFileNameToDLCTOCDir(String coalName) {
		switch (coalName) {
		case "Default_DLC_CON_MP1.bin":
			return "/BIOGame/DLC/DLC_CON_MP1/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP2.bin":
			return "/BIOGame/DLC/DLC_CON_MP2/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP3.bin":
			return "/BIOGame/DLC/DLC_CON_MP3/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP4.bin":
			return "/BIOGame/DLC/DLC_CON_MP4/PCConsoleTOC.bin";
		case "Default_DLC_CON_MP5.bin":
			return "/BIOGame/DLC/DLC_CON_MP5/PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch01.bin":
			return "/BIOGame/DLC/DLC_UPD_Patch01/PCConsoleTOC.bin";
		case "Default_DLC_UPD_Patch02.bin":
			return "/BIOGame/DLC/DLC_UPD_Patch02/PCConsoleTOC.bin";
		case "Coalesced.bin":
			return "\\BIOGame\\PCConsoleTOC.bin";
		case "Default_DLC_TestPatch.bin":
			return "/BIOGame/DLC/DLC_TestPatch/PCConsoleTOC.bin";
		case "Default_DLC_HEN_PR.bin":
			return "/BIOGame/DLC/DLC_HEN_PR/PCConsoleTOC.bin";
		case "Default_DLC_CON_APP01.bin":
			return "/BIOGame/DLC/DLC_CON_APP01/PCConsoleTOC.bin";
		case "Default_DLC_CON_GUN01.bin":
			return "/BIOGame/DLC/DLC_CON_GUN01/PCConsoleTOC.bin";
		case "Default_DLC_CON_GUN02.bin":
			return "/BIOGame/DLC/DLC_CON_GUN02/PCConsoleTOC.bin";
		case "Default_DLC_CON_END.bin":
			return "/BIOGame/DLC/DLC_CON_END/PCConsoleTOC.bin";
		case "Default_DLC_EXP_Pack001.bin":
			return "/BIOGame/DLC/DLC_EXP_Pack001/PCConsoleTOC.bin";
		case "Default_DLC_EXP_Pack002.bin":
			return "/BIOGame/DLC/DLC_EXP_Pack002/PCConsoleTOC.bin";
		case "Default_DLC_EXP_Pack003.bin":
			return "/BIOGame/DLC/DLC_EXP_Pack003/PCConsoleTOC.bin";
		case "Default_DLC_EXP_Pack003_Base.bin":
			return "/BIOGame/DLC/DLC_EXP_Pack003_Base/PCConsoleTOC.bin";
		default:
			ModManager.debugLogger.writeMessage("[coalFileNameToDLCTOCDIR] UNRECOGNIZED COAL FILE: " + coalName);
			return null;
		}
	}

	/**
	 * Retrieves a Custom DLC mod's name by using the ME3Tweaks Third Party Mod
	 * Name Service
	 * 
	 * @param customdlcfoldername
	 *            Custom DLC Folder name
	 * @return Unknown Mod if not found, otherwise the listed name.
	 */
	public static String getThirdPartyModName(String customdlcfoldername) {
		if (ModManager.THIRD_PARTY_MOD_JSON == null) {
			return "Unknown Mod";
		}
		ModManager.debugLogger.writeMessage("Looking up name of mod using the 3rd party mod id service: " + customdlcfoldername);
		try {
			JSONParser parser = new JSONParser();
			JSONObject dbObj = (JSONObject) parser.parse(ModManager.THIRD_PARTY_MOD_JSON);
			JSONObject modinfo = (JSONObject) dbObj.get(customdlcfoldername.toUpperCase());
			if (modinfo == null) {
				return "Unknown Mod";
			} else {
				return (String) modinfo.get("modname");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "Unknown Mod";
	}

	/**
	 * Retreives information about a 3rd party mod based on its folder name.
	 * Returns null if not in the database.
	 * 
	 * @param customdlcfoldername
	 *            Folder to search against
	 * @return
	 */
	public static ThirdPartyModInfo getThirdPartyModInfo(String customdlcfoldername) {
		if (ModManager.THIRD_PARTY_MOD_JSON == null) {
			return null;
		}
		ModManager.debugLogger.writeMessage("Looking up name of mod using the 3rd party mod id service: " + customdlcfoldername);
		try {
			JSONParser parser = new JSONParser();
			JSONObject dbObj = (JSONObject) parser.parse(ModManager.THIRD_PARTY_MOD_JSON);
			JSONObject modinfo = (JSONObject) dbObj.get(customdlcfoldername.toUpperCase());
			if (modinfo == null) {
				return null;
			} else {
				return new ThirdPartyModInfo(customdlcfoldername, modinfo);
			}
		} catch (ParseException e) {
			ModManager.debugLogger.writeErrorWithException("Failed to parse 3rd party mod information: ", e);
		}

		return null;
	}

	public static ThirdPartyModInfo getThirdPartyModInfoByMountID(String priorityString) {
		if (ModManager.THIRD_PARTY_MOD_JSON == null) {
			return null;
		}
		ModManager.debugLogger.writeMessage("Looking up mod information by mount priority using the 3rd party mod id service: " + priorityString);
		try {
			JSONParser parser = new JSONParser();
			JSONObject dbObj = (JSONObject) parser.parse(ModManager.THIRD_PARTY_MOD_JSON);

			for (Object key : dbObj.keySet()) {
				//based on you key types
				String keyStr = (String) key;
				JSONObject modinfo = (JSONObject) dbObj.get(keyStr);
				String mountpriority = (String) modinfo.get("mountpriority");
				//System.out.println(mountpriority);
				if (mountpriority.equalsIgnoreCase(priorityString)) {
					return new ThirdPartyModInfo(keyStr, modinfo);
				}
			}
		} catch (ParseException e) {
			ModManager.debugLogger.writeErrorWithException("Failed to parse 3rd party mod information: ", e);
		}
		return null;
	}

	/**
	 * Gets a random ME3Tweaks Tip from the tips service
	 * @return random tip string, blank string if service not available or error occured.
	 */
	public static String getME3TweaksTip() {
		if (ModManager.TIPS_SERVICE_JSON == null) {
			return "";
		}
		
		try {
			JSONParser parser = new JSONParser();
			JSONObject dbObj = (JSONObject) parser.parse(ModManager.TIPS_SERVICE_JSON);
			JSONArray tipsArray = (JSONArray) dbObj.get("tips");
			ArrayList<String> tips = new ArrayList<String>();
			for (Object value : tipsArray.toArray()) {
				//based on you key types
				tips.add((String) value);
			}
			Random rand = new Random();
			int  n = rand.nextInt(tips.size());
			return tips.get(n);
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Failed to parse tips: ", e);
		}
		return "";
	}
}