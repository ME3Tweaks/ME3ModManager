package com.me3tweaks.modmanager.objects;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class AlternateFile {
	public static final String OPERATION_SUBSTITUTE = "OP_SUBSTITUTE"; //swap a file in a job
	public static final String OPERATION_NOINSTALL = "OP_INSTALL"; //do not install a file
	public static final String OPERATION_INSTALL = "OP_NOINSTALL"; //install a file
	private String description;
	private String operation;

	public AlternateFile(String altfileText) {
		task = ValueParserLib.getStringProperty(altfileText, "ModTask", false);
		modFile = ValueParserLib.getStringProperty(altfileText, "ModFile", false);
		altFile = ValueParserLib.getStringProperty(altfileText, "ModAltFile", false);
		condition = ValueParserLib.getStringProperty(altfileText, "Condition", false);
		description = ValueParserLib.getStringProperty(altfileText, "Description", true);
		operation = ValueParserLib.getStringProperty(altfileText, "ModOperation", false);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "AlternateFile [Applies to Task=" + task + ", Applies with condition=" + condition + ", Normal file mod uses=" + task
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

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public static final String CONDITION_MANUAL = "COND_MANUAL"; //user must choose alt
	public static final String CONDITION_DLC_PRESENT = "COND_DLC_PRESENT"; //automatically choose alt if DLC listed is present
	public static final String CONDITION_DLC_NOT_PRESENT = "COND_DLC_NOT_PRESENT"; //automatically choose if DLC is not present

	private String modFile;
	private String altFile;
	private String task;
	private String condition;

	/**
	 * Verifies this alternate file specification has all the required info to
	 * do its task
	 * 
	 * @return true if usable, false otherwise
	 */
	public boolean isValidLocally() {

		return false;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

}
