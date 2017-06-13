package com.me3tweaks.modmanager.valueparsers.sharedassignment;

import com.me3tweaks.modmanager.ModManager;

/**
 * Represents a PossessedWave array property. It's identifier is the Difficulty
 * string.
 * 
 * @author Michael
 *
 */
public class SharedDifficulty {
	public String difficulty;

	public SharedDifficulty(String str) {
		//get difficulty
		String workingStr;
		int charIndex = str.indexOf('='); // first =, which gets us to (Difficulty=
		workingStr = str.substring(charIndex + 1);
		charIndex = workingStr.indexOf(','); // , following DO_LevelX
		difficulty = workingStr.substring(0, charIndex);
	}

	public String toString() {
		return "SharedDifficulty: " + difficulty;
	}

	public boolean matchIdentifiers(SharedDifficulty importing) {
		ModManager.debugLogger.writeMessageConditionally("[SharedDifficulty]Matching " + difficulty + " against " + importing.difficulty, ModManager.LOG_MODMAKER);
		return difficulty.equals(importing.difficulty);
	}
}
