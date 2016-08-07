package com.me3tweaks.modmanager.objects;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.ModManager;

public class InstalledASIMod {
	@Override
	public String toString() {
		return filename;
	}

	private String installedPath, filename, hash;

	public String getInstalledPath() {
		return installedPath;
	}

	public void setInstalledPath(String installedPath) {
		this.installedPath = installedPath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void deleteMod() {
		FileUtils.deleteQuietly(new File(installedPath));
		ModManager.debugLogger.writeMessage("Installed mod deleted "+installedPath);
	}
}
