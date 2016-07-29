package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;
import java.util.Collections;

public class ASIUpdateGroup {
	private int groupID;
	private ArrayList<ASIMod> modVersions;
	public ASIUpdateGroup(int groupID) {
		super();
		this.groupID = groupID;
		modVersions = new ArrayList<>();
	}
	public int getGroupID() {
		return groupID;
	}
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public ArrayList<ASIMod> getModVersions() {
		return modVersions;
	}
	public void setModVersions(ArrayList<ASIMod> modVersion) {
		this.modVersions = modVersion;
	}
	public void addVersion(ASIMod mod) {
		modVersions.add(mod);
	}
	
	/**
	 * Sorts modVersions arraylist from highest to lowest
	 */
	public void sortVersions() {
		Collections.sort(modVersions);
	}
}
