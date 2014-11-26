package com.me3tweaks.modmanager.valueparsers.bioai;

import java.util.ArrayList;
import java.util.Stack;

import com.me3tweaks.modmanager.valueparsers.biodifficulty.Stat;


public class Range {
	double X, Y;
	public Range(String value) {
		//get name
		String workingStr;
		int charIndex = value.indexOf('='); // first =, marks start of X value
		workingStr = value.substring(charIndex+1); //start of X
		charIndex = workingStr.indexOf(','); // marks the end of X value
		X = Double.parseDouble(workingStr.substring(0, charIndex));
		workingStr = workingStr.substring(charIndex); //clip off all of X.
		
		charIndex = workingStr.indexOf('='); // second =, marks start of Y value
		workingStr = workingStr.substring(charIndex+1); //start of Y
		charIndex = workingStr.indexOf(')'); //end of Y value
		Y = Double.parseDouble(workingStr.substring(0, charIndex));		
	}

	/*public String toString(){
		String str = difficulty;
		str+="\nStats:\n";
		for (Enemy stat : enemies){
			str+=stat.toString()+"\n";
		}
		return str;
	}*/
	
	public String createWaveString() {
		StringBuilder str = new StringBuilder();
		str.append("(X=");
		str.append(X);
		str.append("f,Y=");
		str.append(Y);
		str.append("f)");
		return str.toString();
	}
}
