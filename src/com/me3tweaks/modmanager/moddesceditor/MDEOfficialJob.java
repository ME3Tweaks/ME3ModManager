package com.me3tweaks.modmanager.moddesceditor;

public class MDEOfficialJob {
	private String rawHeader;
	private String rawNewFiles;
	private String rawReplaceFiles;
	private String rawAddFiles;
	private String rawAddTargetFiles;
	private String rawAddReadOnlyTargetFiles;
	private String rawRemoveFiles;
	private String rawRequirementText;
	private String rawFolder;

	public MDEOfficialJob(String rawHeader, String rawFolder, String rawNewFiles, String rawReplaceFiles, String rawAddFiles, String rawAddTargetFiles, String rawAddReadOnlyTargetFiles,
			String removeFiles, String rawRequirementText) {
		this.rawHeader = rawHeader;
		this.rawFolder = rawFolder;
		this.rawNewFiles = rawNewFiles;
		this.rawReplaceFiles = rawReplaceFiles;
		this.rawAddFiles = rawAddFiles;
		this.rawAddFiles = rawAddFiles;
		this.rawAddReadOnlyTargetFiles = rawAddReadOnlyTargetFiles;
		this.rawRemoveFiles = removeFiles;
		this.rawRequirementText = rawRequirementText;
	}

	public String getRawFolder() {
		return rawFolder;
	}

	public void setRawFolder(String rawFolder) {
		this.rawFolder = rawFolder;
	}

	public String getRawRequirementText() {
		return rawRequirementText;
	}

	public void setRawRequirementText(String rawRequirementText) {
		this.rawRequirementText = rawRequirementText;
	}

	public String getRawHeader() {
		return rawHeader;
	}

	public void setRawHeader(String rawHeader) {
		this.rawHeader = rawHeader;
	}

	public String getRawNewFiles() {
		return rawNewFiles;
	}

	public void setRawNewFiles(String rawNewFiles) {
		this.rawNewFiles = rawNewFiles;
	}

	public String getRawReplaceFiles() {
		return rawReplaceFiles;
	}

	public void setRawReplaceFiles(String rawReplaceFiles) {
		this.rawReplaceFiles = rawReplaceFiles;
	}

	public String getRawAddFiles() {
		return rawAddFiles;
	}

	public void setRawAddFiles(String rawAddFiles) {
		this.rawAddFiles = rawAddFiles;
	}

	public String getRawAddTargetFiles() {
		return rawAddTargetFiles;
	}

	public void setRawAddTargetFiles(String rawAddTargetFiles) {
		this.rawAddTargetFiles = rawAddTargetFiles;
	}

	public String getRawAddReadOnlyTargetFiles() {
		return rawAddReadOnlyTargetFiles;
	}

	public void setRawAddReadOnlyTargetFiles(String rawAddReadOnlyTargetFiles) {
		this.rawAddReadOnlyTargetFiles = rawAddReadOnlyTargetFiles;
	}

	public String getRawRemoveFiles() {
		return rawRemoveFiles;
	}

	public void setRawRemoveFiles(String rawRemoveFiles) {
		this.rawRemoveFiles = rawRemoveFiles;
	}
}
