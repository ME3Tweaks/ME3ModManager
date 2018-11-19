package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;

public class CompressedMod {

	public int fileIndexInArchive;
	public Mod modDescMod; // this will fail to be valid because the files are not decompressed yet.
	public String modName, modDescription;
	public long filesize = 0;
	private String unofficialModDescString;
	private String customDLCFolderName = "";

	public String getCustomDLCFolderName() {
		return customDLCFolderName;
	}

	public void setCustomDLCFolderName(String customDLCFolderName) {
		this.customDLCFolderName = customDLCFolderName;
	}

	public void setFullyUnofficial(boolean fullyUnofficial) {
		this.fullyUnofficial = fullyUnofficial;
	}

	public String getUnofficialModDescString() {
		return unofficialModDescString;
	}

	public void setUnofficialModDescString(String unofficialModDescString) {
		this.unofficialModDescString = unofficialModDescString;
	}

	public ArrayList<String> fileList = new ArrayList<>();
	private String descLocationInArchive;
	private String manifestLocationInArchive;
	private boolean isOfficiallySupported = false;
	private String unofficialImportRoot;
	private boolean fullyUnofficial;

	public boolean isOfficiallySupported() {
		return isOfficiallySupported;
	}

	public void setOfficiallySupported(boolean isOfficiallySupported) {
		this.isOfficiallySupported = isOfficiallySupported;
	}

	public String getManifestLocationInArchive() {
		return manifestLocationInArchive;
	}

	public void setManifestLocationInArchive(String manifestLocationInArchive) {
		this.manifestLocationInArchive = manifestLocationInArchive;
	}

	public long getFilesize() {
		return filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public ArrayList<String> getFileList() {
		return fileList;
	}

	public void setFileList(ArrayList<String> fileList) {
		this.fileList = fileList;
	}

	public int getFileIndexInArchive() {
		return fileIndexInArchive;
	}

	public void setFileIndexInArchive(int fileIndexInArchive) {
		this.fileIndexInArchive = fileIndexInArchive;
	}

	public Mod getModDescMod() {
		return modDescMod;
	}

	public void setModDescMod(Mod modDescMod) {
		this.modDescMod = modDescMod;
	}

	public String getModName() {
		return modName;
	}

	public void setModName(String modName) {
		this.modName = modName;
	}

	public String getModDescription() {
		return modDescription;
	}

	public void setModDescription(String modDescription) {
		this.modDescription = modDescription;
	}

	public void setDescLocationInArchive(String descLocaitonInArchive) {
		this.descLocationInArchive = descLocaitonInArchive;
	}

	public String getDescLocationInArchive() {
		return descLocationInArchive;
	}

	public void setUnofficialImportRoot(String str) {
		this.unofficialImportRoot = str;
	}

	public String getUnofficialImportRoot() {
		return unofficialImportRoot;
	}

	public void setFullyUnofficial() {
		fullyUnofficial = true;
	}

	public boolean isFullyUnofficial() {
		return fullyUnofficial;
	}

	@Override
	public String toString() {
		return getModName();
	}

}
