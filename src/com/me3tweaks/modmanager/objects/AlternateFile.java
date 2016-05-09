package com.me3tweaks.modmanager.objects;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class AlternateFile {
	public static final String OPERATION_SUBSTITUTE = "OP_SUBSTITUTE"; //swap a file in a job
	public static final String OPERATION_NOINSTALL = "OP_INSTALL"; //do not install a file
	public static final String OPERATION_INSTALL = "OP_NOINSTALL"; //install a file
	
	public AlternateFile(String altfileText) {
		task = ValueParserLib.getStringProperty(altfileText, "Task", false);
		altFileFor = ValueParserLib.getStringProperty(altfileText, "File", false);
		altFilePath = ValueParserLib.getStringProperty(altfileText, "AltFile", false);
		condition = ValueParserLib.getStringProperty(altfileText, "Condition", false);
	}
	
	public String getAltFileFor() {
		return altFileFor;
	}
	public void setAltFileFor(String altFileFor) {
		this.altFileFor = altFileFor;
	}
	public String getAltFilePath() {
		return altFilePath;
	}
	public void setAltFilePath(String altFilePath) {
		this.altFilePath = altFilePath;
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

	private String altFileFor;
	private String altFilePath;
	private String task;
	private String condition;
}
