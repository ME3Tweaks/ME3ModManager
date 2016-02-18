package com.me3tweaks.modmanager.modupdater;

import java.util.ArrayList;
import java.util.Comparator;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * Contains info about an update that can be executed (downloading new
 * files/deleting files)
 * 
 * @author mjperez
 *
 */
public class UpdatePackage {
	private ArrayList<ManifestModFile> filesToDownload;
	private ArrayList<String> filesToDelete;
	private String serverFolderName;
	private Mod mod;
	private double version;
	private boolean modmakerupdate;
	private String serverModName;

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

	/**
	 * Classic mod constructor for update package
	 * 
	 * @param mod
	 * @param version
	 *            Server version
	 * @param filesToDownload
	 * @param filesToDelete
	 * @param serverFolderName
	 */
	public UpdatePackage(Mod mod, double version, ArrayList<ManifestModFile> filesToDownload, ArrayList<String> filesToDelete, String serverFolderName) {
		super();
		this.mod = mod;
		this.version = version;
		this.filesToDownload = filesToDownload;
		this.filesToDelete = filesToDelete;
		this.serverFolderName = serverFolderName;
		this.modmakerupdate = false;
	}

	/**
	 * ModMaker update constructor
	 * 
	 * @param mod
	 *            Mod to update
	 * @param serverModName
	 *            Foldername where it will be placed (used if removing old
	 *            version)
	 * @param version
	 *            Server version Server version of mod
	 */
	public UpdatePackage(Mod mod, String serverModName, double version) {
		this.mod = mod;
		this.version = version;
		this.serverModName = serverModName;
		this.modmakerupdate = true;
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

	public boolean isModmakerupdate() {
		return modmakerupdate;
	}

	public void setModmakerupdate(boolean modmakerupdate) {
		this.modmakerupdate = modmakerupdate;
	}

	public String getServerModName() {
		return serverModName;
	}

	public void setServerModName(String serverModName) {
		this.serverModName = serverModName;
	}

	public void sortByLargestFirst() {
		filesToDownload.sort(new Comparator<ManifestModFile>() {

			@Override
			public int compare(ManifestModFile o1, ManifestModFile o2) {
				if (o1.getFilesize() > o2.getFilesize()) {
					return -1;
				}
				if (o1.getFilesize() < o2.getFilesize()) {
					return 1;
				} else
					return 0;
			}
		});
	}

	public String getUpdateSizeMB() {
		long size = 0;
		for (ManifestModFile mf : filesToDownload) {
			size += mf.getFilesize();
		}
		return ResourceUtils.humanReadableByteCount(size,true);
	}

}
