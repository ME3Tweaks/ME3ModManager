package com.me3tweaks.modmanager.objects;

public class ME3TweaksPatchPackage {
	private String patchname, patchdesc, patchdev, targetmodule, targetfile, patchurl, folder;
	private boolean finalizer;
	private long targetsize;
	private double patchver, targetversion;
	private int me3tweaksid;
	public String getPatchname() {
		return patchname;
	}
	public void setPatchname(String patchname) {
		this.patchname = patchname;
	}
	public String getPatchdesc() {
		return patchdesc;
	}
	public void setPatchdesc(String patchdesc) {
		this.patchdesc = patchdesc;
	}
	public String getPatchdev() {
		return patchdev;
	}
	public void setPatchdev(String patchdev) {
		this.patchdev = patchdev;
	}
	public String getTargetmodule() {
		return targetmodule;
	}
	public void setTargetmodule(String targetmodule) {
		this.targetmodule = targetmodule;
	}
	public String getTargetfile() {
		return targetfile;
	}
	public void setTargetfile(String targetfile) {
		this.targetfile = targetfile;
	}
	public String getPatchurl() {
		return patchurl;
	}
	public void setPatchurl(String patchurl) {
		this.patchurl = patchurl;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public boolean isFinalizer() {
		return finalizer;
	}
	public void setFinalizer(boolean finalizer) {
		this.finalizer = finalizer;
	}
	public long getTargetsize() {
		return targetsize;
	}
	public void setTargetsize(long targetsize) {
		this.targetsize = targetsize;
	}
	public double getPatchver() {
		return patchver;
	}
	public void setPatchver(double patchver) {
		this.patchver = patchver;
	}
	public double getTargetversion() {
		return targetversion;
	}
	public void setTargetversion(double targetversion) {
		this.targetversion = targetversion;
	}
	public int getMe3tweaksid() {
		return me3tweaksid;
	}
	public void setMe3tweaksid(int me3tweaksid) {
		this.me3tweaksid = me3tweaksid;
	}
}
