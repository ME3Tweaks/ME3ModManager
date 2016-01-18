package com.me3tweaks.modmanager.valueparsers.mpstorepack;

public class Card {
	private UniqueName internalCardName;
	
	public Card(String uniqueName) {
		internalCardName = new UniqueName(uniqueName);
		System.out.println(internalCardName.getName());
		
	}

	public static String getHumanName(String uniqueName) {
		switch (uniqueName) {

		default:
			return "case \"" + uniqueName + "\":\n\treturn \"HUMANNAME\"";
		}
	}
}
