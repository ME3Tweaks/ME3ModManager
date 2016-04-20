package com.me3tweaks.modmanager.objects;

import java.util.ArrayList;

/**
 * Contains list of valid and invalid mods
 * 
 * @author mgamerz
 *
 */
public class ModList {
	private ArrayList<Mod> validMods;
	private ArrayList<Mod> invalidMods;

	public ModList(ArrayList<Mod> validMods, ArrayList<Mod> invalidMods) {
		super();
		this.validMods = validMods;
		this.invalidMods = invalidMods;
	}

	public ArrayList<Mod> getValidMods() {
		return validMods;
	}

	public void setValidMods(ArrayList<Mod> validMods) {
		this.validMods = validMods;
	}

	public ArrayList<Mod> getInvalidMods() {
		return invalidMods;
	}

	public void setInvalidMods(ArrayList<Mod> invalidMods) {
		this.invalidMods = invalidMods;
	}
}
