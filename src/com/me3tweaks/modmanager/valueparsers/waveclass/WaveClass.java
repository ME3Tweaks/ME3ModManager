package com.me3tweaks.modmanager.valueparsers.waveclass;

import com.me3tweaks.modmanager.ModManager;

public class WaveClass {
	public String waveClassName;
	public int selectionWeight;
	
	public WaveClass(String value) {
		//get name
		String workingStr;
		int charIndex = value.indexOf('='); // first =
		workingStr = value.substring(charIndex+1);
		charIndex = workingStr.indexOf(','); // marks the end of wave class
		waveClassName = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex);
		charIndex = workingStr.indexOf('='); //selection weight =
		workingStr = workingStr.substring(charIndex+1); //trash everything before this
		
		charIndex = workingStr.indexOf(')'); //selection weight =
		try {
			selectionWeight = Integer.parseInt(workingStr.substring(0, charIndex));
		} catch (NumberFormatException e) {
			ModManager.debugLogger.writeError("SELECTION WEIGHT IS NOT A NUMBER: "+workingStr.substring(0, charIndex));
		}
	}

	public String toString(){
		return "WaveClass: "+waveClassName+", selection weight: "+selectionWeight;
	}


	public boolean matchIdentifiers(WaveClass importing) {
		return waveClassName.equals(importing.waveClassName);
	}
}
