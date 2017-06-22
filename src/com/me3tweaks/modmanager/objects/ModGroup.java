package com.me3tweaks.modmanager.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

/**
 * A modgroup is a list of moddesc files that can all be installed at the same
 * time in batch mode.
 * 
 * @author Mgamerz
 *
 */
public class ModGroup {
	@Override
	public String toString() {
		return modGroupName;
	}

	private ArrayList<String> descPaths;
	private String modGroupName;
	private String modGroupDescription;

	/**
	 * Used for loading an existing modgroup file.
	 * 
	 * @param modgroupFilePath
	 *            Path to a modgroup file.
	 */
	public ModGroup(String modgroupFilePath) {
		File file = new File(modgroupFilePath);
		if (file.exists()) {
			//parse
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				modGroupName = br.readLine();
				modGroupDescription = br.readLine();
				if (modGroupDescription!= null) {
					modGroupDescription = ResourceUtils.convertBrToNewline(modGroupDescription);
				}
				descPaths = new ArrayList<>();
				String line;
				while ((line = br.readLine()) != null) {
					// process the line.
					if (new File(ModManager.getModsDir() + line).exists()) {
						descPaths.add(line);
					}
				}
			} catch (FileNotFoundException e) {
				//This won't happen
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Error reading modgroup file:", e);
			}
		}
	}

	/**
	 * Used for constructing a new Mod Group.
	 * 
	 * @param name
	 *            Name of the mod group.
	 * @param description
	 *            Description of the mod group.
	 * @param mods
	 *            List of mods from that are part of the mod group.
	 */
	public ModGroup(String name, String description, ArrayList<Mod> mods) {

	}

	public void resolveDescsToMods() {

	}

	public ArrayList<String> getDescPaths() {
		return descPaths;
	}

	public String getModGroupName() {
		return modGroupName;
	}

	public String getModGroupDescription() {
		return modGroupDescription;
	}
}
