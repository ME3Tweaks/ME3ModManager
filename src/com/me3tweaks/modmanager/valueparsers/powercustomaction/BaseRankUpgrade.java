package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class BaseRankUpgrade {
	double baseValue;
	String formula;
	HashMap<Integer, Double> rankBonuses;
	
	/**
	 * Creates a new Detonation Parameters object.
	 * @param valueToParse
	 */
	public BaseRankUpgrade(String tableName, String valueToParse) {
		rankBonuses = new HashMap<Integer, Double>();
		//valueToParse = "(BlockedByObjects=true,DistancedSorted=true,ImpactDeadPawns=false,ImpactFriends=false,ImpactPlaceables=true)";
		//remove bioware's shitty QA
		String workingStr = valueToParse.replaceAll(";", "");
		workingStr = workingStr.replaceAll(Pattern.quote("("), "");
		workingStr = workingStr.replaceAll(Pattern.quote(")"), "");
		/*while (workingStr.charAt(0) == '(' || workingStr.charAt(workingStr.length()) == ')') {
			if (workingStr.charAt(workingStr.length()) == ')'){
				
			}
			if (workingStr.charAt(0) == '(') {
				
			}
			
		}
		workingStr = workingStr.substring(1,workingStr.length()-1);*/
		StringTokenizer strok = new StringTokenizer(workingStr,",");
		//System.out.println("Parsing: "+workingStr);
		while (strok.hasMoreTokens()){
			String assignment = strok.nextToken();
			int equalsIndex = assignment.indexOf('=');
			String var = assignment.substring(0,equalsIndex).trim();
			int rankbonus = -1;
			if (var.contains("[")) {
				//its a rankbonus
				rankbonus = Integer.parseInt(var.substring(var.indexOf('[')+1, var.indexOf(']')));
			}
			String val = assignment.substring(equalsIndex+1,assignment.length());
			if (rankbonus >= 0) {
				rankBonuses.put(rankbonus, Double.parseDouble(val));
				continue;
			}
			
			switch(var){
			case "BaseValue":
				baseValue = Double.parseDouble(val);
				break;
			case "Formula": //same thing, typical bioware.
				formula = val;
				break;
			default: 
				System.err.println("UNKNOWN BASERANKUPGRADE PARAMETER:" +var);
			}
		}
	}
	
	public static boolean isRankBonusUpgrade(String input) {
		//input = "(BaseValue=0.075f,Formula=BonusIsHardValue,RankBonuses[2]=0.05f)";
		if (input.contains("BaseValue") || input.contains("formula") || input.contains("RankBonuses")) {
			return true;
		}
		return false;
	}
	
	public String createVars(int tabs){
		StringBuilder sb = new StringBuilder();
		/*if (blockedByObjects);
		{
			for (int i = 0; i < tabs; i++){ 
				sb.append("\t");
			}
			sb.append("public $mod_powers_");
			sb.append(tableName);
			sb.append("_detonationparameters_");
			sb.append("\n");
		}
		for (int i = 0; i < tabs; i++){ 
			sb.append("\t");
		}
		
		for (int i = 0; i < tabs; i++){ 
			sb.append("\t");
		}
		
		for (int i = 0; i < tabs; i++){ 
			sb.append("\t");
		}
		
		for (int i = 0; i < tabs; i++){ 
			sb.append("\t");
		}*/
		
		return sb.toString();
	}
}

