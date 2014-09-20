package com.me3tweaks.modmanager;

import java.util.HashMap;

public class ModType {
	//DLC Header names
	protected static final String COAL = "COALESCED"; //Not an actual header, just used for marking
	protected static final String MP1 = "RESURGENCE";
	protected static final String MP2 = "REBELLION";
	protected static final String MP3 = "EARTH";
	protected static final String MP4 = "RETALIATION";
	protected static final String MP5 = "RECKONING";
	protected static final String PATCH1 = "PATCH1";
	protected static final String PATCH2 = "PATCH2";
	protected static final String TESTPATCH = "TESTPATCH";
	protected static final String HEN_PR = "FROM_ASHES";
	protected static final String END = "EXTENDED_CUT";
	protected static final String EXP1 = "LEVIATHAN";
	protected static final String EXP2 = "OMEGA";
	protected static final String EXP3 = "CITADEL";
	protected static final String EXP3B = "CITADEL_BASE";
	protected static final String APP01 = "APPEARANCE";
	protected static final String GUN01 = "FIREFIGHT";
	protected static final String GUN02 = "GROUNDSIDE";
	
	
	/*
	protected static final String COAL = "COALESCED";
	protected static final String MP1 = "RESURGENCE";
	protected static final String MP2 = "REBELLION";
	protected static final String MP3 = "EARTH";
	protected static final String MP4 = "RETALIATION";
	protected static final String MP5 = "RECKONING";
	protected static final String PATCH1 = "PATCH1";
	protected static final String PATCH2 = "PATCH2";
	protected static final String TESTPATCH = "TESTPATCH";
	protected static final String HEN_PR = "FROM_ASHES";
	protected static final String END = "EXTENDED_CUT";
	protected static final String EXP1 = "LEVIATHAN";
	protected static final String EXP2 = "OMEGA";
	protected static final String EXP3 = "CITADEL";
	protected static final String EXP3B = "CITADEL_BASE";
	protected static final String APP01 = "APPEARANCE";
	protected static final String GUN01 = "GROUNDSIDE";
	protected static final String GUN02 = "FIREFIGHT";*/

	protected static String[] getHeaderNameArray(){
		return new String[]{MP1,MP2,MP3,MP4,MP5,PATCH1,PATCH2,TESTPATCH,HEN_PR,END,EXP1,EXP2,EXP3,EXP3B,APP01,GUN01,GUN02};
	}
	
	/** Returns the subdirectory from biogame (no leading or trailing slashes) that corresponds to a DLC folder where the .sfar file should exist.
	 * @param modType Name of the DLC, as a ModType constant.
	 * @return String containing the subpath, or null if it doen't exist.
	 */
	protected static String getDLCPath(String modType){
		String subPath = null;
		//System.out.println("Getting DLC path for: "+modType);
		switch(modType){
		case MP1:
			return "DLC\\DLC_CON_MP1\\CookedPCConsole";
		case MP2:
			return "DLC\\DLC_CON_MP2\\CookedPCConsole";
		case MP3:
			return "DLC\\DLC_CON_MP3\\CookedPCConsole";
		case MP4:
			return "DLC\\DLC_CON_MP4\\CookedPCConsole";
		case MP5:
			return "DLC\\DLC_CON_MP5\\CookedPCConsole";
		case PATCH1:
			return "DLC\\DLC_UPD_Patch01\\CookedPCConsole";
		case PATCH2:
			return "DLC\\DLC_UPD_Patch02\\CookedPCConsole";
		case TESTPATCH: //This is a special case
			return "Patches\\PCConsole";
		case HEN_PR:
			return "DLC\\DLC_HEN_PR\\CookedPCConsole";
		case END:
			return "DLC\\DLC_CON_END\\CookedPCConsole";
		case EXP1:
			return "DLC\\DLC_EXP_Pack001\\CookedPCConsole";
		case EXP2:
			return "DLC\\DLC_EXP_Pack002\\CookedPCConsole";
		case EXP3:
			return "DLC\\DLC_EXP_Pack003\\CookedPCConsole";
		case EXP3B:
			return "DLC\\DLC_EXP_Pack003_Base\\CookedPCConsole";
		case APP01:
			return "DLC\\DLC_CON_APP01\\CookedPCConsole";
		case GUN01:
			return "DLC\\DLC_CON_GUN01\\CookedPCConsole";
		case GUN02:
			return "DLC\\DLC_CON_GUN02\\CookedPCConsole";
		}
		
		return subPath;
		
	}

	/** Returns a hashmap of names -> hashes of a DLC.
	 * @return HashMap with ModType.NAME mapped to that DLCs respective original hash value.
	 */
	public static HashMap<String, String> getHashesMap() {
		HashMap<String,String> dlcMap = new HashMap<String,String>();
		dlcMap.put(MP1,"a80cc9089d01ba62fa465e70253a8ab4");
		dlcMap.put(MP2,"949a4197ac8fb97221f63da41f61c6b7");
		dlcMap.put(MP3,"69fd670cac701dc16d034fb5ebb17524");
		dlcMap.put(MP4,"10987f6f49a786637b045ba38e1cb78f");
		dlcMap.put(MP5,"4645cc530f4f309dc7be4eb1dffccab6");
		dlcMap.put(PATCH1,"f025e9b197bfa9e0ce24ca7aefc7b00f");
		dlcMap.put(PATCH2,"77c5584cff4726ad754cbecefa38adad");
		dlcMap.put(TESTPATCH,"1");
		dlcMap.put(HEN_PR,"64ab5bae7ae4ad75108009d76c73389b");
		dlcMap.put(END,"a0f9f2acdba80acba100218f205e385e");
		dlcMap.put(EXP1,"3b9b37d842378e96038c17389dd63032");
		dlcMap.put(EXP2,"ba6f1055dff2cc63c72c34b59a2df9cb");
		dlcMap.put(EXP3,"b361c4bca1ac106dbb0c4b629e7c3022");
		dlcMap.put(EXP3B,"f4c66724f2cf26e4bbe3b62d9024b709");
		dlcMap.put(APP01,"d27098a14da986f4562bda557ed778cc");
		dlcMap.put(GUN01,"d05977324e5ef172e8d0f10ec664ab9f");
		dlcMap.put(GUN02,"6d7fa053fac1696c6b64ea20669db5c0");
		return dlcMap;
	}

	public static String[] getSPHeaderNameArray() {
		return new String[]{PATCH1,PATCH2,HEN_PR,END,EXP1,EXP2,EXP3,EXP3B,APP01,GUN01,GUN02};
	}
	
	public static String[] getMPHeaderNameArray(){
		return new String[]{MP1,MP2,MP3,MP4,MP5,PATCH1,PATCH2};

	}
}
