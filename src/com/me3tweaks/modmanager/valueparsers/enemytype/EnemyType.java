package com.me3tweaks.modmanager.valueparsers.enemytype;

/**
 * Used for array properties: (EnemyType="X"... matching
 * This does not have output for this construct. ModMaker will require a full assignment of this value.
 * @author Michael
 *
 */
public class EnemyType {
	String enemyname;

	public EnemyType(String str) {
		//System.out.println(str);
		String workingStr;
		//(EnemyType="WAVE_RPR_Husk"
		int charIndex = str.indexOf('\"'); // first ", which is the lead into the name.
		workingStr = str.substring(charIndex + 1);
		charIndex = workingStr.indexOf('\"'); // second " which is the end of the name. clip this to get what we want.
		enemyname = workingStr.substring(0, charIndex);
	}

	public String toString() {
		return enemyname;
	}
	
	public boolean matchIdentifier(EnemyType other) {
		return enemyname.equals(other.enemyname);
	}
}
