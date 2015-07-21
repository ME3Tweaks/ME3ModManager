package com.me3tweaks.modmanager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

public class ME3TweaksUtils {
	/**
	 * Downloads a pristine coalesced to the correct pristine directory. This
	 * method should be executed in the background.
	 * 
	 * @param jobName
	 *            Coalesced STRING to download (ModType)
	 * @return newly download pristine file, null otherwise (if failed)
	 */
	public static String downloadPristineCoalesced(String jobName) {
		ModManager.debugLogger.writeMessage("Getting pristine Coalesced for: "+jobName);
		String link = "http://me3tweaks.com/coal/" + internalNameToCoalFilename(jobName);
		File target = new File(ModManager.getPristineDir() + Mod.getStandardFolderName(jobName) + File.separator + internalNameToCoalFilename(jobName));
		try {
			FileUtils.copyURLToFile(new URL(link), target);
			ModManager.debugLogger.writeMessage("Downloaded Pristine Coalesced to: "+target.getAbsolutePath());
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
	 * 
	 * Converts an internal/modmaker-style coal name into the Coalesced filename it represents
	 * e.g. MP3=> (Default_DLC_CON_MP3.bin).
	 * 
	 * @param shortName
	 *            Short name to convert
	 * @return Coaleced filename or null if unknown.
	 */
	public static String internalNameToCoalFilename(String shortName) {
		switch (shortName) {
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
			ModManager.debugLogger.writeMessage("ERROR: UNKNOWN COAL FROM ME3TWEAKS NAMES: " + shortName);
			return null;
		}
	}
	
	/**
	 * Converts ModDesc.ini headers and job names into ModMaker-style internal names
	 * @param header
	 * @return
	 */
	public static String headerToInternalName(String header) {
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
			ModManager.debugLogger.writeMessage("ERROR: UNKNOWN HEADER: " + header);
			return null;
		}
	}
	/*
	public static HashMap<String, String> getCoalHashesMap() {
		HashMap<String,String> hashesMap = new HashMap<String,String>();
		hashesMap.put(MP1,"a80cc9089d01ba62fa465e70253a8ab4");
		hashesMap.put(MP2,"949a4197ac8fb97221f63da41f61c6b7");
		hashesMap.put(MP3,"69fd670cac701dc16d034fb5ebb17524");
		hashesMap.put(MP4,"10987f6f49a786637b045ba38e1cb78f");
		hashesMap.put(MP5,"4645cc530f4f309dc7be4eb1dffccab6");
		hashesMap.put(PATCH1,"f025e9b197bfa9e0ce24ca7aefc7b00f");
		hashesMap.put(PATCH2,"77c5584cff4726ad754cbecefa38adad");
		hashesMap.put(TESTPATCH,"c53a2ac7c3b6f62e76b3e529b7cc61e5");
		hashesMap.put(HEN_PR,"64ab5bae7ae4ad75108009d76c73389b");
		hashesMap.put(END,"a0f9f2acdba80acba100218f205e385e");
		hashesMap.put(EXP1,"3b9b37d842378e96038c17389dd63032");
		hashesMap.put(EXP2,"ba6f1055dff2cc63c72c34b59a2df9cb");
		hashesMap.put(EXP3,"b361c4bca1ac106dbb0c4b629e7c3022");
		hashesMap.put(EXP3B,"f4c66724f2cf26e4bbe3b62d9024b709");
		hashesMap.put(APP01,"d27098a14da986f4562bda557ed778cc");
		hashesMap.put(GUN01,"d05977324e5ef172e8d0f10ec664ab9f");
		hashesMap.put(GUN02,"6d7fa053fac1696c6b64ea20669db5c0");
		return hashesMap;
	}*/
}