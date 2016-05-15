package com.me3tweaks.modmanager.objects;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Contains data that the DLC Injector can understand. It is typically passed as
 * a property container object. (object that contains properties)
 * 
 * @author FemShep
 *
 */
public class ModJob {
	//job types
	public static final int BASEGAME = 1;
	public static final int DLC = 0;
	public static final int CUSTOMDLC = 2;
	@Override
	public String toString() {
		return "ModJob [jobName=" + jobName + "]";
	}

	public boolean TESTPATCH = false; //testpatch flag for patch window
	private int jobType;
	String DLCFilePath;
	private String jobName, requirementText;
	private ArrayList<String> sourceFolders; //CUSTOMDLC (used only for writing desc file)
	private ArrayList<String> destFolders; //CUSTOMDLC (used only for writing desc file)
	public ArrayList<String> newFiles, filesToReplace, addFiles, addFilesTargets, removeFilesTargets;
	private String sourceDir;

	/**
	 * Holds many parameters that are required to inject files into a DLC Sfar
	 * file.
	 * 
	 * @param DLCFilePath
	 *            Path to the DLC Sfar file.
	 */
	public ModJob(String DLCFilePath, String jobName, String requirementText) {
		setJobType(DLC);
		this.setJobName(jobName);
		this.DLCFilePath = DLCFilePath;
		this.requirementText = requirementText;
		newFiles = new ArrayList<String>();
		filesToReplace = new ArrayList<String>();
		addFiles = new ArrayList<String>();
		addFilesTargets = new ArrayList<String>();
		removeFilesTargets = new ArrayList<String>();
	}

	public ArrayList<String> getFilesToAdd() {
		return addFiles;
	}

	public void setFilesToAdd(ArrayList<String> addFiles) {
		this.addFiles = addFiles;
	}

	public ArrayList<String> getFilesToAddTargets() {
		return addFilesTargets;
	}

	public void setAddFilesTargets(ArrayList<String> addFilesTargets) {
		this.addFilesTargets = addFilesTargets;
	}

	public ArrayList<String> getFilesToRemoveTargets() {
		return removeFilesTargets;
	}

	public void setRemoveFilesTargets(ArrayList<String> removeFilesTargets) {
		this.removeFilesTargets = removeFilesTargets;
	}

	/**
	 * Creates a basegame modjob. It doesn't need a path since it can be derived
	 * without the need for one.
	 * 
	 * Use the alternative constructors if you don't want a basegame job.
	 * 
	 * @param DLCFilePath
	 *            Path to the DLC Sfar file.
	 */
	public ModJob() {
		setJobType(BASEGAME);
		setJobName(ModType.BASEGAME);
		newFiles = new ArrayList<String>();
		filesToReplace = new ArrayList<String>();
		addFiles = new ArrayList<String>();
		addFilesTargets = new ArrayList<String>();
		removeFilesTargets = new ArrayList<String>();
	}

	/**
	 * Copy constructor
	 * 
	 * @param job
	 *            job to clone
	 */
	public ModJob(ModJob job) {
		TESTPATCH = job.TESTPATCH;
		jobType = job.jobType;
		DLCFilePath = job.DLCFilePath;
		jobName = job.jobName;
		requirementText = job.requirementText;
		sourceFolders = new ArrayList<String>();
		destFolders = new ArrayList<String>();

		if (job.sourceFolders != null) {
			for (String str : job.sourceFolders) {
				sourceFolders.add(str);
			}
		}
		if (job.destFolders != null) {
			for (String str : job.destFolders) {
				destFolders.add(str);
			}
		}
		newFiles = new ArrayList<String>();
		filesToReplace = new ArrayList<String>();
		addFiles = new ArrayList<String>();
		addFilesTargets = new ArrayList<String>();
		removeFilesTargets = new ArrayList<String>();

		for (String str : job.newFiles) {
			newFiles.add(str);
		}
		for (String str : job.filesToReplace) {
			filesToReplace.add(str);
		}
		for (String str : job.addFiles) {
			addFiles.add(str);
		}
		for (String str : job.addFilesTargets) {
			addFilesTargets.add(str);
		}
		for (String str : job.removeFilesTargets) {
			removeFilesTargets.add(str);
		}

		sourceDir = job.sourceDir;
	}

	public String getRequirementText() {
		return requirementText;
	}

	public void setRequirementText(String requirementText) {
		this.requirementText = requirementText;
	}

	public String getDLCFilePath() {
		return (getJobType() == BASEGAME) ? "Basegame" : DLCFilePath;
	}

	/**
	 * Gets the list of files that will be replacing the installed files
	 * 
	 * @return
	 */
	public ArrayList<String> getFilesToReplace() {
		return newFiles;
	}

	/**
	 * Adds a matching set of files to mod. This is known as adding a task
	 * 
	 * @param newFile
	 *            Source file that will be injected (full file path)
	 * @param fileToReplace
	 *            File path in DLC or basegame that will be updated
	 * @return True if task was added OK, false if the source file does not
	 *         exist or duplicate files were added
	 */
	public boolean addFileReplace(String newFile, String fileToReplace) {
		File file = new File(newFile);
		if (!file.exists()) {
			ModManager.debugLogger.writeError("Source file doesn't exist: " + newFile);
			return false;
		}
		if (getJobType() == BASEGAME) {
			//check first char is \
			if (fileToReplace.charAt(0) != '\\') {
				if (fileToReplace.charAt(0) == '/') {
					StringBuffer sb = new StringBuffer(fileToReplace);
					sb.setCharAt(0, '/');
					fileToReplace = sb.toString();
				} else {
					fileToReplace = "\\" + fileToReplace;
				}
			}
			fileToReplace = ResourceUtils.getForwardSlashVersion(fileToReplace);
		} else {
			//its dlc
			if (fileToReplace.charAt(0) != '/') {
				fileToReplace = "/" + fileToReplace;
			}
		}

		if (newFiles.contains(newFile) || filesToReplace.contains(fileToReplace)) {
			ModManager.debugLogger.writeError("Adding duplicate source or target file for replacement: " + newFile + " or " + fileToReplace);
			return false;
		}

		newFiles.add(newFile);
		filesToReplace.add(fileToReplace);
		return true;
	}

	/**
	 * Gets the array of files that will be replaced
	 * 
	 * @return
	 */
	public ArrayList<String> getFilesToReplaceTargets() {
		return filesToReplace;
	}

	/*
	 * public String[] getFilesToReplace() { return filesToReplace.toArray(new
	 * String[filesToReplace.size()]); }
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((DLCFilePath == null) ? 0 : DLCFilePath.hashCode());
		result = prime * result + getJobType();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModJob other = (ModJob) obj;
		if (DLCFilePath == null) {
			if (other.DLCFilePath != null)
				return false;
		} else if (!DLCFilePath.equals(other.DLCFilePath))
			return false;
		if (getJobType() != other.getJobType())
			return false;
		return true;
	}

	/**
	 * Returns if this job has a PCConsoleTOC in it already
	 * 
	 * @return
	 */
	public boolean hasTOC() {
		for (String newFile : newFiles) {
			if (FilenameUtils.getName(newFile).equals("PCConsoleTOC.bin")) {
				return true;
			}
		}
		return false;
	}

	public int getJobType() {
		return jobType;
	}

	public void setJobType(int jobType) {
		this.jobType = jobType;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public ArrayList<String> getDestFolders() {
		return destFolders;
	}

	public void setDestFolders(ArrayList<String> destFolders) {
		this.destFolders = destFolders;
	}

	/**
	 * Adds a source file/target path pair to this job to add a new file to the
	 * basegame or DLC (packed or unpacked) when this job is processed
	 * 
	 * @param sourceFile
	 *            new file to add
	 * @param targetPath
	 *            path to place in DLC
	 * @return true if added, false otherwise
	 */
	public boolean addNewFileTask(String sourceFile, String targetPath) {
		File file = new File(sourceFile);
		if (!file.exists()) {
			ModManager.debugLogger.writeError("Source file doesn't exist: " + sourceFile);
			return false;
		}
		if (getJobType() == BASEGAME) {
			//check first char is \
			if (targetPath.charAt(0) != '\\') {
				targetPath = "\\" + targetPath;
			}
		} else {
			//its dlc
			if (targetPath.charAt(0) != '/') {
				targetPath = "/" + targetPath;
			}
		}

		addFiles.add(sourceFile);
		addFilesTargets.add(targetPath);
		return true;
	}

	/**
	 * Adds a target path to be removed when this job is processed.
	 * 
	 * @param targetPath
	 *            file to remove relative to this job
	 * @return true if added, false otherwise
	 */
	public boolean addRemoveFileTask(String targetPath) {
		if (getJobType() == BASEGAME) {
			//check first char is \
			if (targetPath.charAt(0) != '\\') {
				targetPath = "\\" + targetPath;
			}
		} else {
			//its dlc
			if (targetPath.charAt(0) != '/') {
				targetPath = "/" + targetPath;
			}
		}
		removeFilesTargets.add(targetPath);
		return true;
	}

	/**
	 * Sets this job's source direcotry. For example, if the moddir ini flag is
	 * set to MP (moddir = MP1), then this will set sourceDir to MP1.
	 * 
	 * @param sourceDir
	 *            Dir in the mod folder where this job will look for source
	 *            files in add/replace/add.
	 */
	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public ArrayList<String> getSourceFolders() {
		return sourceFolders;
	}

	public void setSourceFolders(ArrayList<String> sourceFolders) {
		this.sourceFolders = sourceFolders;
	}

	public String getSourceDir() {
		return sourceDir;
	}
}
