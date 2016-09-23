package com.me3tweaks.modmanager.objects;

import org.json.simple.JSONObject;

public class ThirdPartyModInfo {
	private String modname, modauthor, customDLCfolder, moddescription, modsite;

	public ThirdPartyModInfo(String modname, String modauthor, String customDLCfolder, String moddescription, String modsite) {
		super();
		this.modname = modname;
		this.modauthor = modauthor;
		this.customDLCfolder = customDLCfolder;
		this.moddescription = moddescription;
		this.modsite = modsite;
	}

	public ThirdPartyModInfo(String customdlcfolder, JSONObject modinfo) {
		this.customDLCfolder = customdlcfolder;
		this.modname = (String) modinfo.get("modname");
		this.modauthor = (String) modinfo.get("moddev");
		this.moddescription = (String) modinfo.get("moddesc");
		this.modsite = (String) modinfo.get("modsite");
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
}
