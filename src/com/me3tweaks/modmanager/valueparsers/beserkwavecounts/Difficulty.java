package com.me3tweaks.modmanager.valueparsers.beserkwavecounts;

import com.me3tweaks.modmanager.ModManager;

/**
 * Represents a PossessedWave array property. It's identifier is the Difficulty string.
 * @author Michael
 *
 */
public class Difficulty {
	public String difficulty;
	public Waves waves;
	public Difficulty(String str) {
		//get difficulty
		String workingStr;
		int charIndex = str.indexOf('='); // first =, which gets us to (Difficulty=
		workingStr = str.substring(charIndex+1);
		charIndex = workingStr.indexOf(','); // , following DO_LevelX
		difficulty = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex);
		charIndex = workingStr.indexOf('=');
		workingStr = workingStr.substring(charIndex+1, workingStr.length() - 1); //get rid of final ) and Waves=
		//System.out.println("Remaining str to do: "+workingStr);
		waves = new Waves(workingStr);
		return;
	}
	
	public void merge(Difficulty mergeFrom) {
		for(int i = 0; i < waves.possessionwaves.length; i++) {
			waves.possessionwaves[i] = mergeFrom.waves.possessionwaves[i];
		}
	}
	
	public String createDifficultyString(){
		StringBuilder str = new StringBuilder();
		str.append("(Difficulty=\"");
		str.append(difficulty);
		str.append("\",Waves=(");
		str.append(waves.createWavesString());
		str.append(")"); //close categorydata, category
		return str.toString();
	}
	

	public String toString(){
		String str = difficulty;
		str+="\nPossessed in wave:\n";
		str+=waves.toString();
		return str;
	}

	public boolean matchIdentifiers(Difficulty importing) {
		ModManager.debugLogger.writeMessage("[PossessionWaves]Matching "+difficulty+" against "+importing.difficulty);
		return difficulty.equals(importing.difficulty);
	}
}
