package com.me3tweaks.modmanager.valueparsers.id;

import com.me3tweaks.modmanager.ModManager;

public class ID {
	public int ID;

	public ID(String value) {
		//get id
		String workingStr;
		int charIndex = value.indexOf('='); // first =
		workingStr = value.substring(charIndex + 1);
		charIndex = workingStr.indexOf(','); // marks the end of ID
		try {
			ID = Integer.parseInt(workingStr.substring(0, charIndex));
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeError("ID IS NOT A NUMBER: " + workingStr.substring(0, charIndex));
		}
	}

	public String toString() {
		return "ID: " + ID;
	}

	public boolean matchIdentifiers(ID importing) {
		return ID == importing.ID;
	}
}
