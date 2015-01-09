package com.me3tweaks.modmanager.valueparsers.powercustomaction;

import java.util.StringTokenizer;

public class DetonationParameters {
	String tableName;
	boolean blockedByObjects;
	boolean distanceSorted;
	boolean impactPlaceables;
	boolean impactDeadPawns;
	boolean impactFriends;
	double coneAngle;
	int hitDirectionOffset;
	
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
				System.out.println("BlockedByObjects: "+val);
				blockedByObjects = Boolean.parseBoolean(val);
				System.out.println("evaled to "+blockedByObjects);
				break;
			case "DistancedSorted": //same thing, typical bioware.
			case "DistanceSorted":
				System.out.println("DistanceSorted: "+val);
				distanceSorted = Boolean.parseBoolean(val);
				System.out.println("evaled to "+distanceSorted);
				break;
			case "ImpactPlaceables":
				System.out.println("ImpactPlaceables: "+val);
				impactPlaceables = Boolean.parseBoolean(val);
				System.out.println("evaled to "+impactPlaceables);
				break;
			case "ImpactDeadPawns":
				System.out.println("ImpactDeadPawns: "+val);
				impactDeadPawns = Boolean.parseBoolean(val);
				System.out.println("evaled to "+impactDeadPawns);
				break;
			case "ImpactFriends":
				System.out.println("ImpactFriends: "+val);
				impactFriends = Boolean.parseBoolean(val);
				System.out.println("evaled to "+impactFriends);
				break;
			case "ConeAngle":
				coneAngle = Double.parseDouble(val);
				break;
			case "HitDirectionOffset":
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
