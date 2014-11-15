package com.me3tweaks.modmanager.valueparsers.wavelist;

import java.util.ArrayList;


public class Wave {
	public String difficulty;
	ArrayList<Enemy> enemies;
	public Wave(String value) {
		//get name
		String workingStr;
		int charIndex = value.indexOf('='); // first =, marks start of difficult string.,
		workingStr = value.substring(charIndex+1);
		charIndex = workingStr.indexOf(','); // marks the end of difficulty name
		difficulty = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex);
		charIndex = workingStr.indexOf('(');
		workingStr = workingStr.substring(charIndex+1); //start of stats array (removing the leading ( because of CategoryData = ( ).
		//Clip the ending two ))
		workingStr = workingStr.substring(0, workingStr.length()-2);
		
		//generate stats
		enemies = new ArrayList<Enemy>();
		
		charIndex = 0;
		int openBraces = 0;
		while (workingStr.length() > 0){
			if (workingStr.charAt(charIndex) == '(') {
				openBraces++;
				charIndex++;
				continue;
			}
			if (workingStr.charAt(charIndex) == ')') {
				openBraces--;
				charIndex++;
				if (openBraces <= 0) {
					//we finished one
					enemies.add(new Enemy(workingStr.substring(0, charIndex)));
					if (charIndex < workingStr.length()){
						workingStr = workingStr.substring(charIndex+1);
					} else {
						break; //we finished
					}
					charIndex = 0;
				}
				continue;
			}
			//its none of the above 2
			charIndex++;
		}
		//category finished.
	}

	public String toString(){
		String str = difficulty;
		str+="\nStats:\n";
		for (Enemy stat : enemies){
			str+=stat.toString()+"\n";
		}
		return str;
	}
	
	public String createWaveString() {
		StringBuilder str = new StringBuilder();
		str.append("(Difficulty=");
		str.append(difficulty);
		str.append(",Enemies=(");
		boolean firstEnemy = true;
		for (Enemy enemy : enemies) {
			if (firstEnemy) {
				firstEnemy = false;
			} else {
				str.append(",");
			}
			str.append(enemy.createEnemyString());
		}
		str.append("))"); //end stat
		return str.toString();
	}

	public boolean matchIdentifiers(Wave importing) {
		return difficulty.equals(importing.difficulty);
	}

	public String getBaseEnemy() {
		for (Enemy enemy : enemies) {
			if (enemy.min == 0 && enemy.max == 0 && enemy.maxperwave == 0){
				return enemy.enemyname;
			}
		}
		return null;
	}
}
