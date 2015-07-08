package com.me3tweaks.modmanager.modupdater;

/**
 * This object contains info about a <sourcefile> entry in the server's manifest about a mod
 * @author mgamerz
 *
 */
public class ManifestModFile {
	private String relativePath;
	private String hash;
	private long filesize;
	
	public ManifestModFile(String relativePath, String hash, long filesize) {
		super();
		this.relativePath = relativePath;
		this.hash = hash;
		this.filesize = filesize;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}
	
	
}
