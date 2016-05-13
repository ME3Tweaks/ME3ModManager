package com.me3tweaks.modmanager.objects;

public class CustomDLC implements Comparable<CustomDLC>{
	private MountFile mountFile;
	private String dlcName;
	public CustomDLC(MountFile mountFile, String dlcName) {
		super();
		this.mountFile = mountFile;
		this.dlcName = dlcName;
	}
	public MountFile getMountFile() {
		return mountFile;
	}
	public void setMountFile(MountFile mountFile) {
		this.mountFile = mountFile;
	}
	public String getDlcName() {
		return dlcName;
	}
	public void setDlcName(String dlcName) {
		this.dlcName = dlcName;
	}
	@Override
	public int compareTo(CustomDLC o) {
		return new Integer(mountFile.getMountPriority()).compareTo(o.getMountFile().getMountPriority());
	}
}
