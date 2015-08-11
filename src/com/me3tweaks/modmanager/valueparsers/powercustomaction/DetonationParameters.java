package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.util.StringTokenizer;

public class DetonationParameters {
	String tableName;
	//defaults
	boolean blockedByObjects = true;
	boolean distancedSorted = true;
	boolean impactPlaceables = false;
	boolean impactDeadPawns = false;
	boolean impactFriends = false;
	double coneAngle = -1;
	int hitDirectionOffset = -1;
	
	public DetonationParameters(){
		
	}
	
	/**
	 * Creates a new Detonation Parameters object. If this throws an exception it is not a valid detonationobjects value.
	 * @param valueToParse
	 */
	public DetonationParameters(String tableName, String valueToParse) {
		//valueToParse = "(BlockedByObjects=true,DistancedSorted=true,ImpactDeadPawns=false,ImpactFriends=false,ImpactPlaceables=true)";
		String workingStr = valueToParse.substring(1,valueToParse.length()-1);
		StringTokenizer strok = new StringTokenizer(workingStr,",");
		while (strok.hasMoreTokens()){
			String assignment = strok.nextToken();
			int equalsIndex = assignment.indexOf('=');
			String var = assignment.substring(0,equalsIndex).trim();
			String val = assignment.substring(equalsIndex+1,assignment.length());
			switch(var){
			case "BlockedByObjects":
				blockedByObjects = Boolean.parseBoolean(val);
				break;
			case "DistanceSorted": //same thing, typical bioware.
				System.err.println("USING SPECIAL SPELLING: DISTANCESORTED");
			case "DistancedSorted":
				distancedSorted = Boolean.parseBoolean(val);
				break;
			case "ImpactPlaceables":
				impactPlaceables = Boolean.parseBoolean(val);
				break;
			case "ImpactDeadPawns":
				impactDeadPawns = Boolean.parseBoolean(val);
				break;
			case "ImpactFriends":
				impactFriends = Boolean.parseBoolean(val);
				break;
			case "ConeAngle":
				System.err.println("Found a coneangle");
				coneAngle = Double.parseDouble(val);
				break;
			case "HitDirectionOffset":
				System.err.println("Found a hitdirectionoffset");
				val = val.substring(val.indexOf('=')+1,val.indexOf(')'));
				
				break;
			default: 
				System.err.println("UNKNOWN DETONATION PARAMETER:" +var);
			}
		}
		this.tableName = tableName;
	}
	
	public String createVars(int tabs){
		StringBuilder sb = new StringBuilder();
		if (blockedByObjects);
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
		}
		
		return sb.toString();
	}

	public static boolean isDetonationParameters(String data) {
		if (data.contains("DistanceSorted") || data.contains("BlockedByObjects") || data.contains("ImpactPlaceables") || data.contains("ImpactDeadPawns") || data.contains("ImpactFriends") || data.contains("ConeAngle") || data.contains("HitDirectionOffset")) {
			return true;
		}
		return false;
	}
}
