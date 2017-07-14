package com.me3tweaks.modmanager.objects;

public class PatchModBundle {
	Patch patch;
	Mod mod;
	public Patch getPatch() {
		return patch;
	}
	public void setPatch(Patch patch) {
		this.patch = patch;
	}
	public Mod getMod() {
		return mod;
	}
	public void setMod(Mod mod) {
		this.mod = mod;
	}
	public PatchModBundle(Patch patch, Mod mod) {
		super();
		this.patch = patch;
		this.mod = mod;
	}
}