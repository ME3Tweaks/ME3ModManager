package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;
import java.util.HashMap;

import com.me3tweaks.modmanager.ModManager;

/**
 * Class containing many constants for mods. This is the strings based mod names file.
 * @author Mgamerz
 *
 */
public class ModTypeConstants {
	//DLC Header names (ModDesc.ini)
	public static final String COAL = "COALESCED"; //Used with Legacy and 2.0
	public static final String BASEGAME = "BASEGAME"; //Used in 3.0+
	public static final String MP1 = "RESURGENCE";
	public static final String MP2 = "REBELLION";
	public static final String MP3 = "EARTH";
	public static final String MP4 = "RETALIATION";
	public static final String MP5 = "RECKONING";
	public static final String PATCH1 = "PATCH1";
	public static final String PATCH2 = "PATCH2";
	public static final String TESTPATCH = "TESTPATCH";
	public static final String HEN_PR = "FROM_ASHES";
	public static final String END = "EXTENDED_CUT";
	public static final String EXP1 = "LEVIATHAN";
	public static final String EXP2 = "OMEGA";
	public static final String EXP3 = "CITADEL";
	public static final String EXP3B = "CITADEL_BASE";
	public static final String APP01 = "APPEARANCE";
	public static final String GUN01 = "FIREFIGHT";
	public static final String GUN02 = "GROUNDSIDE";
	public static final String CUSTOMDLC = "CUSTOMDLC";
	public static final String BINI = "BALANCE_CHANGES";
	public static final String DH1 = "GENESIS2";
	public static final String COLLECTORSEDITION = "COLLECTORS_EDITION";
	public static final String TESTPATCH_16_HASH = "9f7811a54c7f3bc21f5de7600a1ce721";
	public static final long TESTPATCH_16_SIZE = 2455091;


	public static String[] getHeaderNameArray() {
		return new String[] { BASEGAME, MP1, MP2, MP3, MP4, MP5, PATCH1, PATCH2, TESTPATCH, HEN_PR, END, EXP1, EXP2, EXP3, EXP3B, APP01, GUN01,
				GUN02, DH1, COLLECTORSEDITION };
	}

	public static String[] getLoadingHeaderNameArray() {
		return new String[] { BASEGAME, MP1, MP2, MP3, MP4, MP5, PATCH1, PATCH2, TESTPATCH, HEN_PR, END, EXP1, EXP2, EXP3, EXP3B, APP01, GUN01,
				GUN02, DH1, COLLECTORSEDITION, BINI };
	}
	/**
	 * Gets the list of standard folders in the DLC folder. Includes the __metadata directory.
	 * @return Arraylist of strings of things like DLC_CON_MP1 etc. Does not include DLC_TESTPATCH.
	 */
	public static ArrayList<String> getStandardDLCFolders() {
		ArrayList<String> foldernames = new ArrayList<String>();
		foldernames.add("DLC_CON_MP1");
		foldernames.add("DLC_CON_MP2");
		foldernames.add("DLC_CON_MP3");
		foldernames.add("DLC_CON_MP4");
		foldernames.add("DLC_CON_MP5");
		foldernames.add("DLC_UPD_Patch01");
		foldernames.add("DLC_UPD_Patch02");
		foldernames.add("DLC_HEN_PR");
		foldernames.add("DLC_CON_END");
		foldernames.add("DLC_EXP_Pack001");
		foldernames.add("DLC_EXP_Pack002");
		foldernames.add("DLC_EXP_Pack003");
		foldernames.add("DLC_EXP_Pack003_Base");
		foldernames.add("DLC_CON_APP01");
		foldernames.add("DLC_CON_GUN01");
		foldernames.add("DLC_CON_GUN02");
		foldernames.add("DLC_CON_DH1");
		foldernames.add("DLC_OnlinePassHidCE");
		foldernames.add("__metadata"); //don't delete
		return foldernames;
	}
	
	/**
	 * Returns a hashmap of Mod Manager Headers => DLC folder names (from DLC folder, like DLC_CON_MP1)
	 * @return hashmap of headers to folder names
	 */
	public static HashMap<String,String> getHeaderFolderMap() {
		HashMap<String,String> foldernames = new HashMap<String,String>();
		foldernames.put(MP1,"DLC_CON_MP1");
		foldernames.put(MP2,"DLC_CON_MP2");
		foldernames.put(MP3,"DLC_CON_MP3");
		foldernames.put(MP4,"DLC_CON_MP4");
		foldernames.put(MP5,"DLC_CON_MP5");
		foldernames.put(PATCH1,"DLC_UPD_Patch01");
		foldernames.put(PATCH2,"DLC_UPD_Patch02");
		foldernames.put(HEN_PR,"DLC_HEN_PR");
		foldernames.put(TESTPATCH,"DLC_TESTPATCH");
		foldernames.put(END,"DLC_CON_END");
		foldernames.put(EXP1,"DLC_EXP_Pack001");
		foldernames.put(EXP2,"DLC_EXP_Pack002");
		foldernames.put(EXP3,"DLC_EXP_Pack003");
		foldernames.put(EXP3B,"DLC_EXP_Pack003_Base");
		foldernames.put(APP01,"DLC_CON_APP01");
		foldernames.put(GUN01,"DLC_CON_GUN01");
		foldernames.put(GUN02,"DLC_CON_GUN02");
		foldernames.put(DH1,"DLC_CON_DH1");
		foldernames.put(COLLECTORSEDITION,"DLC_OnlinePassHidCE");
		return foldernames;
	}

	/**
	 * Returns the subdirectory from biogame (no leading or trailing slashes)
	 * that corresponds to a DLC folder where the .sfar file should exist. This is typically ...CookedPCConsole, or PCConsole if TESTPATCH.
	 * 
	 * @param modType
	 *            Name of the DLC, as a ModType constant. Also known as a header
	 * @return String containing the subpath, or null if it doen't exist.
	 */
	public static String getDLCPath(String modType) {
		String subPath = null;
		//System.out.println("Getting DLC path for: "+modType);
		switch (modType) {
		case BASEGAME:
			return "CookedPCConsole";
		case BINI: 
			return "..\\Binaries\\win32\\asi";
		case MP1:
			return "DLC\\DLC_CON_MP1\\CookedPCConsole"; //Resurgence
		case MP2:
			return "DLC\\DLC_CON_MP2\\CookedPCConsole"; //Rebellion
		case MP3:
			return "DLC\\DLC_CON_MP3\\CookedPCConsole"; //Earth
		case MP4:
			return "DLC\\DLC_CON_MP4\\CookedPCConsole"; //Retaliation
		case MP5:
			return "DLC\\DLC_CON_MP5\\CookedPCConsole"; //Reckoning
		case PATCH1:
			return "DLC\\DLC_UPD_Patch01\\CookedPCConsole";
		case PATCH2:
			return "DLC\\DLC_UPD_Patch02\\CookedPCConsole";
		case TESTPATCH: //This is a special case
			return "Patches\\PCConsole";
		case HEN_PR:
			return "DLC\\DLC_HEN_PR\\CookedPCConsole"; //From Ashes
		case END:
			return "DLC\\DLC_CON_END\\CookedPCConsole"; //Extended Cut
		case EXP1:
			return "DLC\\DLC_EXP_Pack001\\CookedPCConsole"; //Leviathan
		case EXP2:
			return "DLC\\DLC_EXP_Pack002\\CookedPCConsole"; //Omega
		case EXP3:
			return "DLC\\DLC_EXP_Pack003\\CookedPCConsole"; //Citadel
		case EXP3B:
			return "DLC\\DLC_EXP_Pack003_Base\\CookedPCConsole"; //Citadel 2
		case APP01:
			return "DLC\\DLC_CON_APP01\\CookedPCConsole"; //Appearance Pack
		case GUN01:
			return "DLC\\DLC_CON_GUN01\\CookedPCConsole";
		case GUN02:
			return "DLC\\DLC_CON_GUN02\\CookedPCConsole";
		case DH1:
			return "DLC\\DLC_CON_DH1\\CookedPCConsole";
		case COLLECTORSEDITION:
			return "DLC\\DLC_OnlinePassHidCE\\CookedPCConsole";
		}
		return subPath;
	}
	
	/**
	 * Gets a PCConsoleTOC path, relative to the Mass Effect 3 directory.
	 * This path will work both in SFARs (as in a path) and unpacked DLC.
	 * @param header Header to use to lookup
	 * @return PCConsoleTOC location
	 */
	public static String getTOCPathFromHeader(String header) {
		switch (header) {
		case "RESURGENCE":
			return "/BIOGame/DLC/DLC_CON_MP1/PCConsoleTOC.bin";
		case "REBELLION":
			return "/BIOGame/DLC/DLC_CON_MP2/PCConsoleTOC.bin";
		case "EARTH":
			return "/BIOGame/DLC/DLC_CON_MP3/PCConsoleTOC.bin";
		case "RETALIATION":
			return "/BIOGame/DLC/DLC_CON_MP4/PCConsoleTOC.bin";
		case "RECKONING":
			return "/BIOGame/DLC/DLC_CON_MP5/PCConsoleTOC.bin";
		case "PATCH1":
			return "/BIOGame/DLC/DLC_UPD_Patch01/PCConsoleTOC.bin";
		case "PATCH2":
			return "/BIOGame/DLC/DLC_UPD_Patch02/PCConsoleTOC.bin";
		case "BASEGAME":
			return "\\BIOGame\\PCConsoleTOC.bin";
		case "TESTPATCH":
			return "/BIOGame/DLC/DLC_TestPatch/PCConsoleTOC.bin";
		case "FROM_ASHES":
			return "/BIOGame/DLC/DLC_HEN_PR/PCConsoleTOC.bin";
		case "APPEARANCE":
			return "/BIOGame/DLC/DLC_CON_APP01/PCConsoleTOC.bin";
		case "FIREFIGHT":
			return "/BIOGame/DLC/DLC_CON_GUN01/PCConsoleTOC.bin";
		case "GROUNDSIDE":
			return "/BIOGame/DLC/DLC_CON_GUN02/PCConsoleTOC.bin";
		case "EXTENDED_CUT":
			return "/BIOGame/DLC/DLC_CON_END/PCConsoleTOC.bin";
		case "LEVIATHAN":
			return "/BIOGame/DLC/DLC_EXP_Pack001/PCConsoleTOC.bin";
		case "OMEGA":
			return "/BIOGame/DLC/DLC_EXP_Pack002/PCConsoleTOC.bin";
		case "CITADEL":
			return "/BIOGame/DLC/DLC_EXP_Pack003/PCConsoleTOC.bin";
		case "CITADEL_BASE":
			return "/BIOGame/DLC/DLC_EXP_Pack003_Base/PCConsoleTOC.bin";
		case "GENESIS2":
			return "/BIOGame/DLC/DLC_CON_DH1/PCConsoleTOC.bin";
		case "COLLECTORS_EDITION":
			return "/BIOGame/DLC/DLC_OnlinePassHidCE/PCConsoleTOC.bin";
		default:
			ModManager.debugLogger.writeMessage("Getting TOC path failed, unknown header: " + header);
			return null;
		}
	}

	/**
	 * Returns a hashmap of names -> hashes of a DLC.
	 * 
	 * @return HashMap with ModType.NAME mapped to that DLCs respective original
	 *         hash value.
	 */
	public static HashMap<String, String> getHashesMap() {
		HashMap<String, String> dlcMap = new HashMap<String, String>();
		dlcMap.put(MP1, "a80cc9089d01ba62fa465e70253a8ab4");
		dlcMap.put(MP2, "949a4197ac8fb97221f63da41f61c6b7");
		dlcMap.put(MP3, "69fd670cac701dc16d034fb5ebb17524");
		dlcMap.put(MP4, "10987f6f49a786637b045ba38e1cb78f");
		dlcMap.put(MP5, "4645cc530f4f309dc7be4eb1dffccab6");
		dlcMap.put(PATCH1, "f025e9b197bfa9e0ce24ca7aefc7b00f");
		dlcMap.put(PATCH2, "77c5584cff4726ad754cbecefa38adad");
		dlcMap.put(TESTPATCH, "c53a2ac7c3b6f62e76b3e529b7cc61e5");
		dlcMap.put(HEN_PR, "64ab5bae7ae4ad75108009d76c73389b");
		dlcMap.put(END, "a0f9f2acdba80acba100218f205e385e");
		dlcMap.put(EXP1, "3b9b37d842378e96038c17389dd63032");
		dlcMap.put(EXP2, "ba6f1055dff2cc63c72c34b59a2df9cb");
		dlcMap.put(EXP3, "b361c4bca1ac106dbb0c4b629e7c3022");
		dlcMap.put(EXP3B, "f4c66724f2cf26e4bbe3b62d9024b709");
		dlcMap.put(APP01, "d27098a14da986f4562bda557ed778cc");
		dlcMap.put(GUN01, "d05977324e5ef172e8d0f10ec664ab9f");
		dlcMap.put(GUN02, "6d7fa053fac1696c6b64ea20669db5c0");
		dlcMap.put(DH1, "ea34559050385d928e45db218caa4007");
		dlcMap.put(COLLECTORSEDITION, "60d2058c6f4f6f1691e347ebda78b3bb");
		return dlcMap;
	}

	/**
	 * Returns a hashmap of names -> filesizes of a DLC.
	 * 
	 * @return HashMap with ModType.NAME mapped to that DLCs respective original
	 *         size in bytes.
	 */
	public static HashMap<String, Long> getSizesMap() {
		HashMap<String, Long> dlcMap = new HashMap<String, Long>();
		dlcMap.put(MP1, 220174473L);
		dlcMap.put(MP2, 139851674L);
		dlcMap.put(MP3, 198668075L);
		dlcMap.put(MP4, 441856666L);
		dlcMap.put(MP5, 208777784L);
		dlcMap.put(PATCH1, 208998L);
		dlcMap.put(PATCH2, 302772L);
		dlcMap.put(TESTPATCH, 2455154L);
		dlcMap.put(HEN_PR, 594778936L);
		dlcMap.put(END, 1919137514L);
		dlcMap.put(EXP1, 1561239503L);
		dlcMap.put(EXP2, 1849136836L);
		dlcMap.put(EXP3, 1886013531L);
		dlcMap.put(EXP3B, 1896814656L);
		dlcMap.put(APP01, 53878606L);
		dlcMap.put(GUN01, 18708500L);
		dlcMap.put(GUN02, 17134896L);
		dlcMap.put(DH1, 284862077L);
		dlcMap.put(COLLECTORSEDITION, 56321927L);
		return dlcMap;
	}

	/**
	 * Gets list of headers for known official SP DLC a
	 * @return
	 */
	public static String[] getSPHeaderNameArray() {
		return new String[] { COLLECTORSEDITION, HEN_PR, END, EXP1, EXP2, EXP3, EXP3B, APP01, GUN01, GUN02, DH1, TESTPATCH };
	}

	/**
	 * Gets list of headers for known official SP DLC and the basegame header
	 * @return
	 */
	public static String[] getSPBaseHeaderNameArray() {
		return new String[] { BASEGAME, COLLECTORSEDITION, HEN_PR, END, EXP1, EXP2, EXP3, EXP3B, APP01, GUN01, GUN02, DH1, TESTPATCH};
	}
	
	/**
	 * Gets the list of headers for known offical DLC
	 * @return
	 */
	public static String[] getDLCHeaderNameArray() {
		return new String[] {MP1, MP2, MP3, MP4, MP5, PATCH1, PATCH2, TESTPATCH, COLLECTORSEDITION, HEN_PR, END, EXP1, EXP2, EXP3, EXP3B, APP01, GUN01, GUN02, DH1 };
	}

	/**
	 * Gets list of headers for known official MP DLC
	 * @return
	 */
	public static String[] getMPHeaderNameArray() {
		return new String[] { MP1, MP2, MP3, MP4, MP5, PATCH1, PATCH2, TESTPATCH };
	}

	public static String[] getMPBaseHeaderNameArray() {
		return new String[] { BASEGAME, MP1, MP2, MP3, MP4, MP5, PATCH1, PATCH2, TESTPATCH };
	}

	/**
	 * Checks if the the parameter is in the list of known DLC foldernames.
	 * @param destFolder foldername to check
	 * @return true if in the list, false otherwise. Comparison is done case insensitively.
	 */
	public static boolean isKnownDLCFolder(String destFolder) {
		for (String knownFolder : getStandardDLCFolders()) {
			if (destFolder.equalsIgnoreCase(knownFolder)) {
				return true;
			}
		}
		if (destFolder.equalsIgnoreCase("DLC_TestPatch")) {
			return true;
		}
		return false;
	}
}
