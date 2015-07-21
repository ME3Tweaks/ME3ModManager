package com.me3tweaks.modmanager.modmaker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.Mod;
import com.me3tweaks.modmanager.ModManager;

public class ME3TweaksUtils {
	public static final int FILENAME = 0;
	public static final int HEADER = 1;
	public static final int INTERNAL = 2;

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

		String link = "http://me3tweaks.com/coal/" + filename;
		File target = new File(ModManager.getPristineDir() + standardFolder + File.separator + filename);
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
		case "Default_DLC_CON_Pack001.bin":
			return "LEVIATHAN";
		case "Default_DLC_CON_Pack002.bin":
			return "OMEGA";
		case "Default_DLC_CON_Pack003.bin":
			return "CITADEL";
		case "Default_DLC_CON_Pack003_Base.bin":
			return "CITADEL_BASE";
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
		case "Default_DLC_CON_Pack001.bin":
			return "LEVIATHAN";
		case "Default_DLC_CON_Pack002.bin":
			return "OMEGA";
		case "Default_DLC_CON_Pack003.bin":
			return "CITADEL";
		case "Default_DLC_CON_Pack003_Base.bin":
			return "CITADEL_BASE";
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
			return "Coalesced.bin";
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
			return "Default_DLC_CON_Pack001.bin";
		case "OMEGA":
			return "Default_DLC_CON_Pack002.bin";
		case "CITADEL":
			return "Default_DLC_CON_Pack003.bin";
		case "CITADEL_BASE":
			return "Default_DLC_CON_Pack003_Base.bin";
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
			return "Default_DLC_CON_Pack001.bin";
		case "OMEGA":
			return "Default_DLC_CON_Pack002.bin";
		case "CITADEL":
			return "Default_DLC_CON_Pack003.bin";
		case "CITADEL_BASE":
			return "Default_DLC_CON_Pack003_Base.bin";
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
		default:
			ModManager.debugLogger.writeMessage("ME3TWEAKSUTILS ERROR: INTERNAL => HEADER " + internal);
			return null;
		}
	}

	/*
	 * public static HashMap<String, String> getCoalHashesMap() {
	 * HashMap<String,String> hashesMap = new HashMap<String,String>();
	 * hashesMap.put(MP1,"a80cc9089d01ba62fa465e70253a8ab4");
	 * hashesMap.put(MP2,"949a4197ac8fb97221f63da41f61c6b7");
	 * hashesMap.put(MP3,"69fd670cac701dc16d034fb5ebb17524");
	 * hashesMap.put(MP4,"10987f6f49a786637b045ba38e1cb78f");
	 * hashesMap.put(MP5,"4645cc530f4f309dc7be4eb1dffccab6");
	 * hashesMap.put(PATCH1,"f025e9b197bfa9e0ce24ca7aefc7b00f");
	 * hashesMap.put(PATCH2,"77c5584cff4726ad754cbecefa38adad");
	 * hashesMap.put(TESTPATCH,"c53a2ac7c3b6f62e76b3e529b7cc61e5");
	 * hashesMap.put(HEN_PR,"64ab5bae7ae4ad75108009d76c73389b");
	 * hashesMap.put(END,"a0f9f2acdba80acba100218f205e385e");
	 * hashesMap.put(EXP1,"3b9b37d842378e96038c17389dd63032");
	 * hashesMap.put(EXP2,"ba6f1055dff2cc63c72c34b59a2df9cb");
	 * hashesMap.put(EXP3,"b361c4bca1ac106dbb0c4b629e7c3022");
	 * hashesMap.put(EXP3B,"f4c66724f2cf26e4bbe3b62d9024b709");
	 * hashesMap.put(APP01,"d27098a14da986f4562bda557ed778cc");
	 * hashesMap.put(GUN01,"d05977324e5ef172e8d0f10ec664ab9f");
	 * hashesMap.put(GUN02,"6d7fa053fac1696c6b64ea20669db5c0"); return
	 * hashesMap; }
	 */

}