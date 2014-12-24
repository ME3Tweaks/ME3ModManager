package com.me3tweaks.modmanager;

import java.io.File;
import java.util.ArrayList;

/** Contains data that the DLC Injector can understand.
 * It is typically passed as a property container object. (object that contains properties)
 * @author FemShep
 *
 */
public class ModJob {
	//job types
	protected static final int BASEGAME = 1;
	protected static final int DLC = 0;
	protected boolean TESTPATCH = false; //testpatch flag for patch window
	
	
	int modType;
	String DLCFilePath, jobName;
	ArrayList<String> newFiles;
	ArrayList<String> filesToReplace;
	
	/** Holds many parameters that are required to inject files into a DLC Sfar file.
	 * @param DLCFilePath Path to the DLC Sfar file.
	 */
	public ModJob(String DLCFilePath, String jobName){
		modType = DLC;
		this.jobName = jobName;
		this.DLCFilePath = DLCFilePath;
		newFiles = new ArrayList<String>();
		filesToReplace = new ArrayList<String>();
	}
	
	/** Creates a basegame modjob. It doesn't need a path since it can be derived without the need for one.
	 * @param DLCFilePath Path to the DLC Sfar file.
	 */
	public ModJob(){
		modType = BASEGAME;
		jobName = ModType.BASEGAME;
		newFiles = new ArrayList<String>();
		filesToReplace = new ArrayList<String>();
	}

	public String getDLCFilePath() {
		return (modType == BASEGAME) ? "Basegame" : DLCFilePath;
	}

	public String[] getNewFiles() {
		return newFiles.toArray(new String[newFiles.size()]);
	}

	/**
	 * Adds a matching set of files to add
	 * @param newFile Source file that will be injected
	 * @param fileToReplace File path in DLC or basegame that will be updated
	 * @return
	 */
	public boolean addFileReplace(String newFile, String fileToReplace) {
		File file = new File(newFile);
		if (!file.exists()){
			ModManager.debugLogger.writeMessage("Source file doesn't exist: "+newFile);
			return false;
		}
		newFiles.add(newFile);
		filesToReplace.add(fileToReplace);
		return true;
	}

	/**
	 * Gets the array of files that will be replaced
	 * @return
	 */
	public String[] getFilesToReplace() {
		return filesToReplace.toArray(new String[filesToReplace.size()]);
	}
}
