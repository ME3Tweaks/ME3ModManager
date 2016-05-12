package com.me3tweaks.modmanager.modupdater;

/**
 * This object contains info about a <sourcefile> entry in the server's manifest
 * about a mod
 * 
 * @author mgamerz
 *
 */
public class ManifestModFile {
	private String relativePath;
	private String hash;
	private long size;
	private String lzmahash;
	private long lzmasize;
	private boolean sideloadOnly;

	public ManifestModFile(String relativePath, String svrHash, long svrSize, String svrCompressedHash, long svrCompressedSize) {
		super();
		this.relativePath = relativePath.replaceAll("\\\\", "/");
		this.hash = svrHash;
		this.size = svrSize;
		this.lzmahash = svrCompressedHash;
		this.lzmasize = svrCompressedSize;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getLzmahash() {
		return lzmahash;
	}

	public void setLzmahash(String lzmahash) {
		this.lzmahash = lzmahash;
	}

	public long getLzmasize() {
		return lzmasize;
	}

	public void setLzmasize(long lzmasize) {
		this.lzmasize = lzmasize;
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

	public boolean isSideloadOnly() {
		return sideloadOnly;
	}

	public void setSideloadOnly(boolean sideloadOnly) {
		this.sideloadOnly = sideloadOnly;
	}
}
