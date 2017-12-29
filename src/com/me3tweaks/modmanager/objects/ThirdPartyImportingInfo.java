package com.me3tweaks.modmanager.objects;

public class ThirdPartyImportingInfo {
	private String md5;
	private String inarchivepathtosearch;

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getInarchivepathtosearch() {
		return inarchivepathtosearch;
	}

	public void setInarchivepathtosearch(String inarchivepathtosearch) {
		this.inarchivepathtosearch = inarchivepathtosearch;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getSubidrectorydepth() {
		return subidrectorydepth;
	}

	public void setSubidrectorydepth(int subidrectorydepth) {
		this.subidrectorydepth = subidrectorydepth;
	}

	public ThirdPartyImportingInfo(String md5, String inarchivepathtosearch, String filename, int subidrectorydepth) {
		super();
		this.md5 = md5;
		this.inarchivepathtosearch = inarchivepathtosearch;
		this.filename = filename;
		this.subidrectorydepth = subidrectorydepth;
	}

	private String filename;
	private int subidrectorydepth;
}
