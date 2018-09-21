package com.me3tweaks.modmanager.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.ModManager;

public class MetaCMM {

	private String installationGUID = "";
	private String installedByModManagerBuild;
	private String modVersion;
	private String modName;

	public MetaCMM(File metacmmfile) {
		if (metacmmfile.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(metacmmfile))) {
				String line;
				if ((line = br.readLine()) != null) {
					//Name of installed mod, or name + version if old mod manager versions
					modName = line;
				}

				if ((line = br.readLine()) != null) {
					//Name of installed mod, or name + version if old mod manager versions
					modVersion = line;
				}

				if ((line = br.readLine()) != null) {
					//Name of installed mod, or name + version if old mod manager versions
					installedByModManagerBuild = line;
				}

				if ((line = br.readLine()) != null) {
					//Name of installed mod, or name + version if old mod manager versions
					installationGUID = line;
				}

			} catch (FileNotFoundException e) {
				modName= "_metacmm.txt not found";
			} catch (IOException e) {
				modName= "Could not read _metacmm.txt";

			}
		}
	}

	public String getInstallationGUID() {
		return installationGUID;
	}

	public String getInstalledByModManagerBuild() {
		return installedByModManagerBuild;
	}

	public String getModVersion() {
		return modVersion;
	}

	public String getModName() {
		return modName;
	}

	public String getDisplayString() {
		return modName + (modVersion != null ? " " + modVersion : "");
	}

	public static void writeMetaCMMFile(Mod mod, String destination) {
		try {
			ModManager.debugLogger.writeMessage("[METACMM]Writing custom DLC metadata file: " + destination);
			String metadatatext = mod.getModName();
			metadatatext += "\n"+mod.getVersion();
			metadatatext += "\n"+ModManager.BUILD_NUMBER;
			metadatatext += "\n"+ModManager.getGUID();
			
			FileUtils.writeStringToFile(new File(destination), metadatatext, StandardCharsets.UTF_8);
		} catch (IOException e) {
			ModManager.debugLogger.writeErrorWithException("[METACMM]Couldn't write custom dlc metadata file:", e);
		}
	}
}
