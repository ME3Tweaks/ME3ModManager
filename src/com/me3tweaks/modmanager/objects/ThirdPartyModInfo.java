package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ThirdPartyModInfo {
	private String modname, modauthor, customDLCfolder, moddescription, modsite;
	private int mountpriority; //unsigned short
	private ArrayList<ImportingInfo> importingInfos;

	/**
	 * Copy constructor
	 * 
	 * @param modname
	 * @param modauthor
	 * @param customDLCfolder
	 * @param moddescription
	 * @param modsite
	 * @param mountpriority
	 */
	public ThirdPartyModInfo(ThirdPartyModInfo original) {
		super();
		this.modname = original.modname;
		this.modauthor = original.modauthor;
		this.customDLCfolder = original.customDLCfolder;
		this.moddescription = original.moddescription;
		this.modsite = original.modsite;
		this.mountpriority = original.mountpriority;
		this.importingInfos = new ArrayList<>();
		for (ImportingInfo impinf : original.importingInfos) {
			importingInfos.add(new ImportingInfo(impinf));
		}
	}

	public ThirdPartyModInfo(String customdlcfolder, JSONObject modinfo) {
		this.customDLCfolder = customdlcfolder;
		this.modname = (String) modinfo.get("modname");
		this.modauthor = (String) modinfo.get("moddev");
		this.moddescription = (String) modinfo.get("moddesc");
		this.modsite = (String) modinfo.get("modsite");
		String priority = (String) modinfo.get("mountpriority");
		this.mountpriority = Integer.parseInt(priority);
		this.importingInfos = new ArrayList<>();
		JSONArray importingInfo = (JSONArray) modinfo.get("importinginfo");
		if (importingInfo != null) {
			for (Object obj : importingInfo) {
				JSONObject jsonObj = (JSONObject) obj;
				ImportingInfo info = new ImportingInfo();
				info.setMd5((String) jsonObj.get("md5"));
				info.setSubPathToSearch((String) jsonObj.get("inarchivepathtosearch"));
				System.out.println(info);
			}
		}
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

	public int getMountPriority() {
		return mountpriority;
	}

	public void setMountpriority(int mountpriority) {
		this.mountpriority = mountpriority;
	}
}
