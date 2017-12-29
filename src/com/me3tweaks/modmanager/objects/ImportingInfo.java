package com.me3tweaks.modmanager.objects;

public class ImportingInfo {
	@Override
	public String toString() {
		return "ImportingInfo [subPathToSearch=" + subPathToSearch + ", md5=" + md5 + "]";
	}

	private String subPathToSearch = "";
	private String md5;

	/**
	 * Copy Constructor
	 * 
	 * @param impinf
	 */
	public ImportingInfo(ImportingInfo impinf) {
		this.md5 = impinf.md5;
		this.subPathToSearch = impinf.subPathToSearch;
	}

	public ImportingInfo() {

	}

	public String getSubPathToSearch() {
		return subPathToSearch;
	}

	public void setSubPathToSearch(String subPathToSearch) {
		this.subPathToSearch = subPathToSearch;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
}
