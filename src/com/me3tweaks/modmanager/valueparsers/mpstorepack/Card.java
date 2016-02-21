package com.me3tweaks.modmanager.valueparsers.mpstorepack;

public abstract class Card {
	public enum Rarity { //rarity is the background of the card.
		Common("Rarity_Common"), //blue
		Uncommon("Rarity_Uncommon"), //silver
		Rare("Rarity_Rare"), //gold
		UltraRare("Rarity_UltraRare"), //black
		Unused("Rarity_Unused") //red
		;

		private final String raritytext;

		/**
		 * @param text
		 */
		private Rarity(final String text) {
			this.raritytext = text;
		}

		@Override
		public String toString() {
			return raritytext;
		}
	}
}
