package com.me3tweaks.modmanager.valueparsers.possessionwaves;

import com.me3tweaks.modmanager.ModManager;

public class Waves {
	public boolean[] possessionwaves = new boolean[11];
	
	public Waves(String str) {
		System.out.println("Parsing possessionwaves: "+str);
		String workingStr = str;
		workingStr = workingStr.substring(1,workingStr.length()-1); //get rid of ( and ) on the outside
		int waveIndex = 0;
		int charIndex = 0;
		while ((charIndex = workingStr.indexOf(',')) != -1) {
			possessionwaves[waveIndex] = strToBoolean(workingStr.substring(0,charIndex));
			workingStr = workingStr.substring(charIndex+1);
			waveIndex++;
		}
	}
	
	private boolean strToBoolean(String bool){
		if (bool.equals("true")) {
			return true;
		}
		if (bool.equals("false")){
			return false;
		} else {
			ModManager.debugLogger.writeMessage("UNKNOWN STR TO BOOLEAN VALUE: "+bool+", defaulting to false");
			return false;
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 11; i++) {
			sb.append("Wave ");
			sb.append(i+1);
			sb.append(": ");
			sb.append((possessionwaves[i]) ? "Yes" : "No");
			sb.append("\n");
		}
		return sb.toString();
	}

	public String createWavesString() {
		System.out.println("BREAK");
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for (int i = 0; i <= possessionwaves.length; i++) {
			if (first) {
				first = false;
			} else {
				str.append(",");
			}
			str.append((possessionwaves[i]) ? "true" : "false");
		}
		return str.toString();
	}
}
