package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;

/**
 * Details patches that need to be applied to a mod (during import of patches)
 * @author mgamerz
 *
 */
public class PendingPatchPack {
	private ArrayList<Patch> patches;
	private Mod mod;
	public ArrayList<Patch> getPatches() {
		return patches;
	}
	public void setPatches(ArrayList<Patch> patches) {
		this.patches = patches;
	}
	public Mod getMod() {
		return mod;
	}
	public void setMod(Mod mod) {
		this.mod = mod;
	}
	
}
