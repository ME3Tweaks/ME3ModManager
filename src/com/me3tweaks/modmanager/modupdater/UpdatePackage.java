package com.me3tweaks.modmanager.modupdater;

import java.util.ArrayList;

import com.me3tweaks.modmanager.Mod;

/**
 * Contains info about an update that can be executed (downloading new files/deleting files)
 * @author mjperez
 *
 */
public class UpdatePackage {
	private ArrayList<ManifestModFile> filesToDownload;
	private ArrayList<String> filesToDelete;
	private String serverFolderName;
	private Mod mod;
	private double version;
	
	public ArrayList<ManifestModFile> getFilesToDownload() {
		return filesToDownload;
	}
	public void setFilesToDownload(ArrayList<ManifestModFile> filesToDownload) {
		this.filesToDownload = filesToDownload;
	}
	public ArrayList<String> getFilesToDelete() {
		return filesToDelete;
	}
	public void setFilesToDelete(ArrayList<String> filesToDelete) {
		this.filesToDelete = filesToDelete;
	}
	public String getServerFolderName() {
		return serverFolderName;
	}
	public void setServerFolderName(String serverFolderName) {
		this.serverFolderName = serverFolderName;
	}
	public UpdatePackage(Mod mod, double version, ArrayList<ManifestModFile> filesToDownload, ArrayList<String> filesToDelete, String serverFolderName) {
		super();
		this.mod = mod;
		this.version = version;
		this.filesToDownload = filesToDownload;
		this.filesToDelete = filesToDelete;
		this.serverFolderName = serverFolderName;
	}
	
	public Mod getMod() {
		return mod;
	}
	public void setMod(Mod mod) {
		this.mod = mod;
	}
	public double getVersion() {
		return version;
	}
	
	public void setVersion(double version) {
		this.version = version;
	}
}
