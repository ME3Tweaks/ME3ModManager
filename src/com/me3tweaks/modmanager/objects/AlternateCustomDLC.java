package com.me3tweaks.modmanager.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class AlternateCustomDLC {

	//public static final String OPERATION_SUBSTITUTE = "OP_SUBSTITUTE"; //swap a file in a job
	public static final String OPERATION_ADD_CUSTOMDLC_JOB = "OP_ADD_CUSTOMDLC"; //do not install a file
	public static final String OPERATION_ADD_FILES_TO_CUSTOMDLC_FOLDER = "OP_ADD_FOLDERFILES_TO_CUSTOMDLC"; //install a file
	public static final String CONDITION_MANUAL = "COND_MANUAL"; //user must choose alt
	public static final String CONDITION_DLC_PRESENT = "COND_DLC_PRESENT"; //automatically choose alt if DLC listed is present
	public static final String CONDITION_DLC_NOT_PRESENT = "COND_DLC_NOT_PRESENT"; //automatically choose if DLC is not present
	public static final String CONDITION_ANY_DLC_NOT_PRESENT = "COND_ANY_DLC_NOT_PRESENT"; //multiple DLC, any of which are missing
	public static final String CONDITION_ALL_DLC_PRESENT = "COND_ALL_DLC_PRESENT";

	private boolean isValid = true;
	private String altDLC;
	private String destDLC;
	private String conditionalDLC;
	private String condition;
	private String description;
	private String operation;
	private String jobHeader;
	private ArrayList<String> conditionalDLCs = new ArrayList<String>();
	private String friendlyName;
	private boolean hasBeenChosen;

	public void setHasBeenChosen(boolean hasBeenChosen) {
		this.hasBeenChosen = hasBeenChosen;
	}

	/**
	 * Constructs a Alternate Custom DLC object. This is the original constructor introduced in Mod Manager 4.4, and only works with Custom DLC.
	 * @param altfileText String to parse
	 */
	public AlternateCustomDLC(String altfileText) {
		jobHeader = ModType.CUSTOMDLC;
		condition = ValueParserLib.getStringProperty(altfileText, "Condition", false);
		if (!condition.equals(CONDITION_MANUAL)) {
			conditionalDLC = ValueParserLib.getStringProperty(altfileText, "ConditionalDLC", false);
			if (condition.equals(CONDITION_ANY_DLC_NOT_PRESENT)) {
				parseConditionalDLC();
			}
		}
		altDLC = ValueParserLib.getStringProperty(altfileText, "ModAltDLC", false);
		description = ValueParserLib.getStringProperty(altfileText, "Description", true);
		operation = ValueParserLib.getStringProperty(altfileText, "ModOperation", false);
		destDLC = ValueParserLib.getStringProperty(altfileText,"ModDestDLC", false);
		friendlyName = ValueParserLib.getStringProperty(altfileText, "FriendlyName", true);
	}
	
/*	/**
	 * Constructs a alternate installation option for non-Custom DLC things.
	 * @param altfileText Text to parse
	 * @param jobHeader Job that this object targets
	 *//*
	public AlternateCustomDLC(String altfileText, String jobHeader) {
		condition = ValueParserLib.getStringProperty(altfileText, "Condition", false);
		if (!condition.equals(CONDITION_MANUAL)) {
			isValid = false; //Alternate Custom DLC targetting official DLC or basegame must be manually chosen.
			return;
		}
		operation = ValueParserLib.getStringProperty(altfileText, "ModOperation", false);
		if (!operation.equals(arg0))
		
		altDLC = ValueParserLib.getStringProperty(altfileText, "ModAltDLC", false);
		description = ValueParserLib.getStringProperty(altfileText, "Description", true);
		destDLC = ValueParserLib.getStringProperty(altfileText,"ModDestDLC", false);
		friendlyName = ValueParserLib.getStringProperty(altfileText, "FriendlyName", true);
	}*/
	
	/**
	 * Returns if this is a valid Alternate Custom DLC object. This value is only set when using non-custom dlc options (targetting official file replacements)
	 * @return true if valid, false if not valid
	 */
	public boolean isValid() {
		return isValid;
	}
	
	

	private void parseConditionalDLC() {
		String str = conditionalDLC.replaceAll("\\(", "");
		str = str.replaceAll("\\)", "");
		StringTokenizer strok = new StringTokenizer(str, ";");
		while (strok.hasMoreTokens()) {
			String dlc = strok.nextToken();
			conditionalDLCs.add(dlc);
			System.out.println("Read conditional DLC in multi dlc "+dlc);
		}
	}

	@Override
	public String toString() {
		return "AlternateCustomDLC [condition=" + condition + " on conditionalDLC=" + conditionalDLC+" ||| altDLC=" + altDLC + ", destDLC=" + destDLC + ", operation=" + operation + ", conditionalDLCs=" + conditionalDLCs + "]";
	}

	/**
	 * Copy constructor
	 * 
	 * @param alt
	 *            alternate file to copy
	 */
	public AlternateCustomDLC(AlternateCustomDLC alt) {
		altDLC = alt.altDLC;
		conditionalDLC = alt.conditionalDLC;
		condition = alt.condition;
		description = alt.description;
		operation = alt.operation;
		destDLC = alt.destDLC;
		for (String str: alt.conditionalDLCs){
			conditionalDLCs.add(str);
		}
		friendlyName = alt.friendlyName;
		hasBeenChosen = alt.hasBeenChosen;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAltDLC() {
		return altDLC;
	}

	public void setAltDLC(String altDLC) {
		this.altDLC = altDLC;
	}

	public String getConditionalDLC() {
		return conditionalDLC;
	}

	public void setTask(String task) {
		this.conditionalDLC = task;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * Verifies this alternate dlc specification has all the required info to
	 * do its task
	 * 
	 * @return true if usable, false otherwise
	 */
	public boolean isValidLocally(String modPath) {
		return true;
/*		try {
			if (!condition.equals(CONDITION_DLC_NOT_PRESENT) && !condition.equals(CONDITION_DLC_PRESENT) && !condition.equals(CONDITION_MANUAL)) {
				ModManager.debugLogger.writeError("Condition is not one of the allowed values: " + condition);
				return false;
			}
			ArrayList<String> officialHeaders = new ArrayList<String>(Arrays.asList(ModType.getDLCHeaderNameArray()));
			if (!officialHeaders.contains(conditionalDLC)) {
				File f = new File(modPath + conditionalDLC);
				if (f.exists() && f.isDirectory()) {
					ModManager.debugLogger.writeError("ConditionalDLC is listed as part of the custom dlc this mod will install: " + conditionalDLC
							+ ". On mod's first install this will have no effect, and on subsequent will change what is being installed.");
					return false;
				} else {
					if (!conditionalDLC.startsWith("DLC_")) {
						ModManager.debugLogger.writeError("ConditionalDLC is not an official header and does not start with DLC_: " + conditionalDLC + ".");
						return false;
					}
				}
			}
			File alternateFile = new File(modPath + altDLC);
			if (!alternateFile.exists() && !operation.equals(OPERATION_NOINSTALL)) {
				ModManager.debugLogger.writeError("Listed altfile doesn't exist: " + altDLC);
				return false;
			}

			File normalModFile = new File(modPath + modFile);
			if (!normalModFile.exists() && (operation.equals(OPERATION_SUBSTITUTE) || operation.equals(OPERATION_NOINSTALL))) {
				ModManager.debugLogger.writeError("Listed modfile (normal mod file) doesn't exist: " + normalModFile);
				return false;
			}
			return true;
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Exception validating alternate file:", e);
			return false;
		}
*/
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public ArrayList<String> getConditionalDLCList() {
		return conditionalDLCs;
	}
	public String getDestDLC() {
		return destDLC;
	}

	public void setDestDLC(String destDLC) {
		this.destDLC = destDLC;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public boolean hasBeenChoosen() {
		return hasBeenChosen;
	}
}
