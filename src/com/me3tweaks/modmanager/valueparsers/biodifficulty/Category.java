package com.me3tweaks.modmanager.valueparsers.biodifficulty;

import java.util.ArrayList;

import com.me3tweaks.modmanager.ModManager;


public class Category {
	public String categoryname;
	ArrayList<Stat> stats;
	public Category(String value) {
		//get name
		String workingStr;
		int charIndex = value.indexOf('\"'); // first ", which is the lead into the name.
		workingStr = value.substring(charIndex+1);
		charIndex = workingStr.indexOf('\"'); // second " which is the end of the name. clip this to get what we want.
		categoryname = workingStr.substring(0, charIndex);
		workingStr = workingStr.substring(charIndex);
		charIndex = workingStr.indexOf('(');
		workingStr = workingStr.substring(charIndex+1); //start of stats array (removing the leading ( because of CategoryData = ( ).
		//Clip the ending two ))
		workingStr = workingStr.substring(0, workingStr.length()-2);
		
		//generate stats
		stats = new ArrayList<Stat>();
		
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
					stats.add(new Stat(workingStr.substring(0, charIndex)));
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
	
	public void merge(Category mergeFrom) {
		for (Stat stat : mergeFrom.stats){
			Stat statToUpdate = null;
			//merge that one into this one
			for (Stat localstat : stats) {
				//find our local stat that matches that one.
				if (localstat.statname.equals(stat.statname)) {
					statToUpdate = localstat;
					break;
				}
				//keep looping.
			}
			if (statToUpdate == null) {
				//error
				System.out.println("DIDN'T FIND STAT TO MERGE.");
				return;
			}
			System.out.println("Merging: "+stat.statname);
			statToUpdate.statrange = stat.statrange;
		}
	}
	
	public String createCategoryString(){
		StringBuilder str = new StringBuilder();
		str.append("(Category=\"");
		str.append(categoryname);
		str.append("\", CategoryData=(");
		boolean firststat = true;
		for (Stat stat : stats) {
			if (firststat) {
				firststat = false;
			} else {
				str.append(",");
			}
			str.append(stat.createStatString());
		}
		str.append("))"); //close categorydata, category
		return str.toString();
	}
	

	public String toString(){
		String str = categoryname;
		str+="\nStats:\n";
		for (Stat stat : stats){
			str+=stat.toString()+"\n";
		}
		return str;
	}

	public boolean matchIdentifiers(Category importing) {
		ModManager.debugLogger.writeMessage("Matching "+categoryname+" against "+importing.categoryname);
		return categoryname.equals(importing.categoryname);
	}
}
