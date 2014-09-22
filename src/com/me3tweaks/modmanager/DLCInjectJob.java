package com.me3tweaks.modmanager;

import java.io.File;
import java.util.ArrayList;

/** Contains data that the DLC Injector can understand.
 * It is typically passed as a property container object. (object that contains properties)
 * @author FemShep
 *
 */
public class DLCInjectJob {
	String DLCFilePath;
	ArrayList<String> newFiles;
	ArrayList<String> filesToReplace;
	
	/** Holds many parameters that are required to inject files into a DLC Sfar file.
	 * @param DLCFilePath Path to the DLC Sfar file.
	 */
	public DLCInjectJob(String DLCFilePath){
		this.DLCFilePath = DLCFilePath;
		newFiles = new ArrayList<String>();
		filesToReplace = new ArrayList<String>();
	}

	public String getDLCFilePath() {
		return DLCFilePath;
	}

	public String[] getNewFiles() {
		return newFiles.toArray(new String[newFiles.size()]);
	}

	public boolean addFileReplace(String newFile, String fileToReplace) {
		File file = new File(newFile);
		if (!file.exists()){
			if (ModManager.logging){
				ModManager.debugLogger.writeMessage("File marked to replace doesn't exist: "+newFile);
			}
			return false;
		}
		newFiles.add(newFile);
		filesToReplace.add(fileToReplace);
		return true;
	}

	public String[] getFilesToReplace() {
		return filesToReplace.toArray(new String[filesToReplace.size()]);
	}
}
