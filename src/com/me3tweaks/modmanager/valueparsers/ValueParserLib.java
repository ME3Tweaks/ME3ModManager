package com.me3tweaks.modmanager.valueparsers;

/**
 * ValueParserLib provides utility functions for things such as structs.
 * 
 * @author mgamerz
 *
 */
public class ValueParserLib {

	public static void main(String[] args) {
		String input = "(UniqueName=\"SFXGameContentDLC_CON_MP2.SFXGameEffect_MatchConsumable_Gear_GrenadeCapacity\", MaxCount = 5, Rarity=Rarity_Rare, bUseVersionIdx=false, VersionIdx = 0, Category = 4, GUICategory=EReinforcementGUICategory_MatchConsumableGear, GUIName=733576, GUIDescription=733577, GUIType=\"gearUpgrade\", GUITextureRef=\"GUI_MPImages_MP2.GrenadeAdd\")";
		System.out.println(getStringProperty(input, "Rarity", false));
		System.out.println(getIntProperty(input, "MaxCount"));
	}

	public static String getStringProperty(String inputString, String propertyName, boolean isQuoted) {
		int charIndex = inputString.indexOf(propertyName);
		if (charIndex > 0 && (inputString.charAt(charIndex-1) == '(' || inputString.charAt(charIndex-1) == ',' || inputString.charAt(charIndex-1) == '"' || inputString.charAt(charIndex-1) == ' ')) {
			//at least one instance was found.
			while (charIndex < inputString.length()) {
				String workingStr = inputString.substring(charIndex + propertyName.length());
				if (workingStr.charAt(0) == '=' || workingStr.charAt(1) == '=') { //next char, or after space char is =
					workingStr = workingStr.substring(workingStr.indexOf('=') + 1); //cut off =
					if (isQuoted) {
						workingStr = workingStr.substring(workingStr.indexOf('\"') + 1); //cut off " from quoted items.
					}
					//value is next.
					charIndex = 0;
					while (charIndex < workingStr.length()) {
						if (isQuoted) {
							if (workingStr.charAt(charIndex) == '\"') {
								return workingStr.substring(0, charIndex).trim();
							}
						} else {
							if (workingStr.charAt(charIndex) == ')' || workingStr.charAt(charIndex) == ',') {
								return workingStr.substring(0, charIndex).trim();
							}
						}
						charIndex++;
					}
					System.out.println("DID NOT FIND TERMINATING CHAR.");

					break;
				} else {
					System.out.println(workingStr);
					return "nextchars were not =.";
				}

				//charIndex++;
				//if (inputString.charAt(charIndex) == '')
			}
		} else {
			return null;
		}
		return "derp";
	}

	public static int getIntProperty(String inputString, String propertyName) {
		int charIndex = inputString.indexOf(propertyName);
		if (charIndex > 0 && (inputString.charAt(charIndex-1) == '(' || inputString.charAt(charIndex-1) == ',' || inputString.charAt(charIndex-1) == '"' || inputString.charAt(charIndex-1) == ' ')) {
			//at least one instance was found.
			while (charIndex < inputString.length()) {
				String workingStr = inputString.substring(charIndex + propertyName.length());
				if (workingStr.charAt(0) == '=' || workingStr.charAt(1) == '=') { //next char, or after space char is =
					workingStr = workingStr.substring(workingStr.indexOf('=') + 1); //cut off =
					//value is next.
					charIndex = 0;
					while (charIndex < workingStr.length()) {
						if (workingStr.charAt(charIndex) == ')' || workingStr.charAt(charIndex) == ',') {
							return Integer.parseInt(workingStr.substring(0, charIndex).trim());
						}
						charIndex++;
					}
					System.out.println("DID NOT FIND TERMINATING CHAR.");

					break;
				} else {
					System.out.println(workingStr);
					return -1;
				}

				//charIndex++;
				//if (inputString.charAt(charIndex) == '')
			}
		} else {
			return -1;
		}
		return -1;
	}

	public static double getFloatProperty(String inputString, String propertyName) {

		return 0;
	}

}
