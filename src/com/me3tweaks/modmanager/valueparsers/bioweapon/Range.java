package com.me3tweaks.modmanager.valueparsers.bioweapon;

public class Range {
	double doubleX, doubleY;
	int intX, intY;
	boolean isInt = false;
	/**
	 * BioAI range value, in the form of (X=1.8f,Y=2.0f).
	 * @param value String to parse
	 */
	public Range(String value) {
		//get name
		String workingStr;
		int charIndex = value.indexOf('='); // first =, marks start of X value
		workingStr = value.substring(charIndex+1); //start of X
		charIndex = workingStr.indexOf(','); // marks the end of X value
		try {
			intX = Integer.parseInt(workingStr.substring(0, charIndex));
			isInt = true;
		} catch (NumberFormatException e) {
			isInt = false;
		}
		if (!isInt) {
			doubleX = Double.parseDouble(workingStr.substring(0, charIndex));
		}
		workingStr = workingStr.substring(charIndex); //clip off all of X.
		
		charIndex = workingStr.indexOf('='); // second =, marks start of Y value
		workingStr = workingStr.substring(charIndex+1); //start of Y
		charIndex = workingStr.indexOf(')'); //end of Y value
		if (isInt) {
			intY = Integer.parseInt(workingStr.substring(0, charIndex));
		} else {
			doubleY = Double.parseDouble(workingStr.substring(0, charIndex));
		}
				
	}
}
