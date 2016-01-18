package com.me3tweaks.modmanager.valueparsers.mpstorepack;

public class UniqueName {
	private String uniqueName;
	private int versionIdx = -1;

	public UniqueName(String value) {
		//get uniquename
		String workingStr;
		int charIndex = value.indexOf('"'); // first "
		workingStr = value.substring(charIndex + 1);
		charIndex = workingStr.indexOf('"'); // marks the end of name
		uniqueName = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex);
		
		if (workingStr.indexOf("VersionIdx") > 0){
			workingStr = workingStr.substring(workingStr.indexOf("VersionIdx"));
			charIndex = value.indexOf('='); // first "
			workingStr = value.substring(charIndex + 1);
			
			versionIdx = Integer.parseInt(workingStr);
			
			
		}
	}

	public String toString() {
		return "UniqueName: " + uniqueName;
	}

	public boolean matchIdentifiers(UniqueName importing) {
		return uniqueName.equals(importing.uniqueName);
	}
}
