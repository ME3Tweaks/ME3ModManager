package com.me3tweaks.modmanager.objects;

import org.json.simple.JSONObject;

public class ThirdPartyModInfo {
	private String modname, modauthor, customDLCfolder, moddescription, modsite;
	private short mountpriority;
	/**
	 * Copy constructor
	 * @param modname
	 * @param modauthor
	 * @param customDLCfolder
	 * @param moddescription
	 * @param modsite
	 * @param mountpriority
	 */
	public ThirdPartyModInfo(String modname, String modauthor, String customDLCfolder, String moddescription, String modsite, short mountpriority) {
		super();
		this.modname = modname;
		this.modauthor = modauthor;
		this.customDLCfolder = customDLCfolder;
		this.moddescription = moddescription;
		this.modsite = modsite;
		this.mountpriority = mountpriority;
	}

	public ThirdPartyModInfo(String customdlcfolder, JSONObject modinfo) {
		this.customDLCfolder = customdlcfolder;
		this.modname = (String) modinfo.get("modname");
		this.modauthor = (String) modinfo.get("moddev");
		this.moddescription = (String) modinfo.get("moddesc");
		this.modsite = (String) modinfo.get("modsite");
		String priority = (String) modinfo.get("mountpriority");
		this.mountpriority = Short.parseShort(priority);
	}

	public String getModname() {
		return modname;
	}

	public void setModname(String modname) {
		this.modname = modname;
	}

	public String getModauthor() {
		return modauthor;
	}

	public void setModauthor(String modauthor) {
		this.modauthor = modauthor;
	}

	public String getCustomDLCfolder() {
		return customDLCfolder;
	}

	public void setCustomDLCfolder(String customDLCfolder) {
		this.customDLCfolder = customDLCfolder;
	}

	public String getModdescription() {
		return moddescription;
	}

	public void setModdescription(String moddescription) {
		this.moddescription = moddescription;
	}

	public String getModsite() {
		return modsite;
	}

	public void setModsite(String modsite) {
		this.modsite = modsite;
	}

	public short getMountPriority() {
		return mountpriority;
	}

	public void setMountpriority(short mountpriority) {
		this.mountpriority = mountpriority;
	}
}
