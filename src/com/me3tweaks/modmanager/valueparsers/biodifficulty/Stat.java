package com.me3tweaks.modmanager.valueparsers.biodifficulty;

public class Stat {
	String statname;
	StatRange statrange;
	
	public Stat(String str) {
		String workingStr;
		int charIndex = str.indexOf('\"'); // first ", which is the lead into the name.
		workingStr = str.substring(charIndex+1);
		charIndex = workingStr.indexOf('\"'); // second " which is the end of the name. clip this to get what we want.
		statname = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex+2);//bypass " and ,
		charIndex = workingStr.indexOf('='); //statrange start.
		workingStr = workingStr.substring(charIndex+1);
		
		charIndex = workingStr.indexOf('='); //x stat start
		workingStr = workingStr.substring(charIndex+1);
		
		charIndex = workingStr.indexOf(','); //x stat end
		String xstat = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex);
		
		charIndex = workingStr.indexOf('='); //y stat start
		workingStr = workingStr.substring(charIndex+1);
		charIndex = workingStr.indexOf(')'); //y stat end
		String ystat = workingStr.substring(0, charIndex);
		
		statrange = new StatRange(xstat,ystat);
	}
	
	public String toString(){
		String str = statname+" : "+statrange.toString();
		return str;
	}

	public String createStatString() {
		StringBuilder str = new StringBuilder();
		str.append("(StatName=\"");
		str.append(statname);
		str.append("\",");
		str.append(statrange.createStatString());
		str.append(")"); //end stat
		return str.toString();
	}
}
