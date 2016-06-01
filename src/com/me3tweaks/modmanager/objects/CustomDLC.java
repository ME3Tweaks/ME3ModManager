package com.me3tweaks.modmanager.objects;

import java.util.Arrays;

import com.me3tweaks.modmanager.ModManager;

public class CustomDLC implements Comparable<CustomDLC> {
	@Override
	public String toString() {
		return dlcName;
	}

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
	
	public boolean isGUIMod() {
		return Arrays.asList(ModManager.KNOWN_GUI_CUSTOMDLC_MODS).contains(dlcName);
	}
}
