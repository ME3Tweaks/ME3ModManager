package com.me3tweaks.modmanager.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class AlternateFile {
	public static final String OPERATION_SUBSTITUTE = "OP_SUBSTITUTE"; //swap a file in a job
	public static final String OPERATION_NOINSTALL = "OP_INSTALL"; //do not install a file
	public static final String OPERATION_INSTALL = "OP_NOINSTALL"; //install a file
	public static final String CONDITION_MANUAL = "COND_MANUAL"; //user must choose alt
	public static final String CONDITION_DLC_PRESENT = "COND_DLC_PRESENT"; //automatically choose alt if DLC listed is present
	public static final String CONDITION_DLC_NOT_PRESENT = "COND_DLC_NOT_PRESENT"; //automatically choose if DLC is not present

	private String modFile;
	private String altFile;
	private String conditionalDLC;
	private String condition;
	private String description;
	private String operation;

	public AlternateFile(String altfileText) {
		conditionalDLC = ValueParserLib.getStringProperty(altfileText, "ConditionalDLC", false);
		modFile = ValueParserLib.getStringProperty(altfileText, "ModFile", false);
		if (modFile.charAt(0) != '/') {
			modFile = "/" + modFile;
		}
		altFile = ValueParserLib.getStringProperty(altfileText, "ModAltFile", false);
		condition = ValueParserLib.getStringProperty(altfileText, "Condition", false);
		description = ValueParserLib.getStringProperty(altfileText, "Description", true);
		operation = ValueParserLib.getStringProperty(altfileText, "ModOperation", false);
	}

	/**
	 * Copy constructor
	 * 
	 * @param alt
	 *            alternate file to copy
	 */
	public AlternateFile(AlternateFile alt) {
		modFile = alt.modFile;
		altFile = alt.altFile;
		conditionalDLC = alt.conditionalDLC;
		condition = alt.condition;
		description = alt.description;
		operation = alt.operation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "AlternateFile [Applies to Task=" + conditionalDLC + ", Applies with condition=" + condition + ", Operation="+operation+", Normal file mod uses=" + conditionalDLC
				+ ", Alternate files to use=" + altFile + "]";
	}

	public String getModFile() {
		return modFile;
	}

	public void setModFile(String altFileFor) {
		this.modFile = altFileFor;
	}

	public String getAltFile() {
		return altFile;
	}

	public void setAltFile(String altFile) {
		this.altFile = altFile;
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
	 * Verifies this alternate file specification has all the required info to
	 * do its task
	 * 
	 * @return true if usable, false otherwise
	 */
	public boolean isValidLocally(String modPath) {
		try {
			if (!condition.equals(CONDITION_DLC_NOT_PRESENT) && !condition.equals(CONDITION_DLC_PRESENT) && !condition.equals(CONDITION_MANUAL)) {
				ModManager.debugLogger.writeError("Condition is not one of the allowed values: " + condition);
				return false;
			}
			if (!operation.equals(OPERATION_INSTALL) && !operation.equals(OPERATION_NOINSTALL) && !operation.equals(OPERATION_SUBSTITUTE)) {
				ModManager.debugLogger.writeError("Operation is not one of the allowed values: " + operation);
				return false;
			}
			ArrayList<String> officialHeaders = new ArrayList<String>(Arrays.asList(ModType.getDLCHeaderNameArray()));
			if (!officialHeaders.contains(conditionalDLC)) {
				File f = new File(modPath + conditionalDLC);
				if (f.exists() && f.isDirectory()) {
					ModManager.debugLogger.writeError("ConditionalDLC is listed as part of the custom dlc this mod will install: " + conditionalDLC +". On mod's first install this will have no effect, and on subsequent will change what is being installed.");
					return false;
				} else {
					if (!conditionalDLC.startsWith("DLC_")) {
						ModManager.debugLogger.writeError("ConditionalDLC is not an official header and does not start with DLC_: " + conditionalDLC +".");
						return false;
					}
				}
			}
			File alternateFile = new File(modPath + altFile);
			if (!alternateFile.exists()) {
				ModManager.debugLogger.writeError("Listed altfile doesn't exist: " + altFile);
				return false;
			}

			File normalModFile = new File(modPath + modFile);
			if (!normalModFile.exists()) {
				ModManager.debugLogger.writeError("Listed modfile (normal mod file) doesn't exist: " + normalModFile);
				return false;
			}
			return true;
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("Exception validating alternate file:", e);
			return false;
		}

	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

}
