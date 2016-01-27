package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class StorePack implements Comparable<StorePack> {
	private ArrayList<PackSlot> slotContents = new ArrayList<PackSlot>();
	private String packName;
	private PackMetadata metadata;
	private boolean isOfficial = false;

	public StorePack(String packname) {
		this.packName = packname;
		switch (packname) {
		case "equipjumbo":
		case "bronze":
		case "silver":
		case "gold":
		case "goldpremium":
		case "arsenal":
		case "reserves":
		case "starter":
			isOfficial = true;
			break;
		}

		//System.out.println(getHumanName(packname));
	}

	public boolean isOfficial() {
		return isOfficial;
	}

	public void setOfficial(boolean isOfficial) {
		this.isOfficial = isOfficial;
	}

	public void addCardSlot(PackSlot packslot) {
		slotContents.add(packslot);
	}

	public String getContentsString() {
		StringBuilder sb = new StringBuilder();
		for (PackSlot slot : slotContents) {
			sb.append(slot.getDisplayString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		sb.append(packName);
		int numCards = 0;
		for (PackSlot slot : slotContents) {
			numCards += slot.getNumCards();
		}
		sb.append(" (" + numCards + " card" + (numCards != 0 ? "s" : "") + ")\n");
		sb.append(getContentsString());
		return sb.toString();
	}

	public ArrayList<PackSlot> getSlotContents() {
		return slotContents;
	}

	public void setSlotContents(ArrayList<PackSlot> slotContents) {
		this.slotContents = slotContents;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public int getCost() {
		return metadata.getCost();
	}

	public String getPackHTML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<h2 id='pack_name_title' class=\"centered dark\">" + getHumanName() + "</h2>\n");
		sb.append("<h3 class=\"centered dark\">Internally known as " + packName + "</h3>\n");
		sb.append("<p class='centered dark'>" + getDescripton() + "</p>");
		sb.append("<p class='centered dark'>" + (getCost() > 0 ? "Costs " + getCost() + " Credits" : "Free")
				+ (metadata.getMaxPurchases() > 0 ? ", can be obtained " + metadata.getMaxPurchases() + " time" + (metadata.getMaxPurchases() != 1 ? "s" : "") : "") + "</p>");
		if (!isOfficial) {
			sb.append(
					"<p class='centered dark'>This pack is not normally obtainable (it may have been in the past). To access this pack, you need to use a mod with the Store Shows Everything MixIn.</p>");
		}
		sb.append("<hr class=\"dark_hr_center\">\n");

		TreeSet<RealCard> cards = new TreeSet<RealCard>();
		//TreeSet<RealCard> backupCards = new TreeSet<RealCard>();
		sb.append("<h3 class=\"centered\">Pack composition</h3>\n");
		int numCards = 0;
		for (PackSlot slot : slotContents) {
			numCards += slot.getNumCards();
		}
		sb.append("<p class='centered dark'>This pack awards " + numCards + " card" + (numCards != 1 ? "s" : "") + ".</p>");
		sb.append("<div class='pack_composition'>\n");
		sb.append("<div><canvas id='pack_pools_chart'></canvas></div>\n");
		sb.append("<div id='pack_composition_text'>\n");
		for (PackSlot slot : slotContents) {
			sb.append("<p>");
			sb.append("Draws " + slot.getNumCards() + " card" + (slot.getNumCards() != 1 ? "s" : "") + " from:");
			boolean first = true;
			for (SlotPool slotPool : slot.getMainPools()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				String name = slotPool.getPoolname().toLowerCase();
				sb.append(" <a href='/store_catalog/pools/" + name + "' title='" + name + " pool page'>");
				sb.append(name);
				sb.append("</a> ");
				sb.append(ValueParserLib.round(slotPool.getPoolweight() * 100, 2));
				sb.append("%");
			}
			if (slot.getBackupPool() != null) {
				String name = slot.getBackupPool().getPoolname();
				sb.append(" ,");
				sb.append(" <a href='/store_catalog/pools/" + name + "' title='" + name + " pool page'>");
				sb.append(name);
				sb.append("</a> (Backup)");
			}
			sb.append("</p>");

		}
		sb.append("</div>\n");
		sb.append("<div style='clear: both'></div>\n");
		sb.append("</div>\n");

		//MAIN POOLS
		for (PackSlot slot : slotContents) {
			for (SlotPool slotPool : slot.getMainPools()) {
				//these are only placeholder pools. we need to fetch the real ones
				CardPool realpool = CardParser.getPoolByName(slotPool.getPoolname()); //CONTENTS NOT WEIGHT
				for (PoolCard card : realpool.getPoolContents()) {
					RealCard realcard = CardParser.getRealCardFromPoolCard(card);
					//System.out.println(realcard);
					if (realcard != null) {
						RealCard cloneCard = new RealCard(realcard);
						if (!cards.contains(cloneCard)) {
							cards.add(cloneCard);
						}
						cloneCard.addPool(slotPool);
					}
					//	System.err.println("POOL CARD HAS NO STORE DEFINITION: "+card);
				}
			}
		}
		sb.append("<hr class=\"dark_hr_center\">\n");
		sb.append("<h3 class=\"centered\">Cards obtainable in this pack</h3>\n");
		sb.append("<div class='cardslist'>");
		sb.append("<div><input id='glowsearch' placeholder=\"Find a card...\"/></div>");
		sb.append("<h3 class='centered dark'>Main Cards</h3>");
		//print out tree - this is a set since pools may have overlapped.
		//System.out.println("STARTING HTML");
		for (RealCard card : cards) {
			//System.out.println(card);
			sb.append(card.getPackpageHTML(0));
		}
		//sb.append("</div>");


		
		TreeMap<RealCard, Integer> backupCards = new TreeMap<RealCard,Integer>();
		RealCard prevcard = null;
		//BACKUP POOL
		for (PackSlot slot : slotContents) {
			SlotPool backupPool = slot.getBackupPool();
			if (backupPool != null) {
				//these are only placeholder pools. we need to fetch the real ones
				CardPool realpool = CardParser.getPoolByName(backupPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					RealCard realcard = CardParser.getRealCardFromPoolCard(card);
					if (realcard != null) {
						RealCard cloneCard = new RealCard(realcard);
						if (backupCards.get(cloneCard) == null) {
							backupCards.put(cloneCard,1);
//							if (cloneCard.getUniqueName().contains("Warp")) {
//								System.out.println("["+packName+"]Adding backup card "+cloneCard);
//								prevcard = cloneCard; //debugging only
//							}
						} else {
							backupCards.put(cloneCard, new Integer(backupCards.get(cloneCard) + slot.getNumCards()));
						}
						cloneCard.addPool(backupPool);
					}
				}
			}
		}
		if (backupCards.size() > 0)

		{
			sb.append("<hr class=\"dark_hr_center\">");
			sb.append("<h3 class='centered dark'>Backup Cards</h3>");
			sb.append(
					"<p class='centered dark'>If the randomly chosen pool cannot drop any cards (it's empty, you can't get any more drops of all the contents, etc), the backup pool is chosen.</p>");
			for (Entry<RealCard,Integer> card : backupCards.entrySet()) {
				sb.append(card.getKey().getPackpageHTML(card.getValue()));
			}
		}

		sb.append("</div>");
		sb.append("<script>\n");
		sb.append("\tvar poolchartcanvas = document.getElementById(\"pack_pools_chart\").getContext(\"2d\");\n");

		HashMap<SlotPool, WeightWrapper> allSlotPools = new HashMap<SlotPool, WeightWrapper>();
		double entirepackweight = 0.0;
		for (PackSlot slot : slotContents) {
			for (SlotPool slotPool : slot.getMainPools()) {
				WeightWrapper storedWeightWrapper = allSlotPools.get(slotPool);
				if (storedWeightWrapper == null) {
					storedWeightWrapper = new WeightWrapper();
					allSlotPools.put(slotPool, storedWeightWrapper);
				}
				//System.out.println( slotPool.getPoolweight());
				storedWeightWrapper.weight += slotPool.getPoolweight()*(slot.getNumCards());
				entirepackweight += slotPool.getPoolweight()*(slot.getNumCards());
			}
		}

		sb.append("var data = [");
		boolean docomma = false;
		for (Entry<SlotPool, WeightWrapper> entry : allSlotPools.entrySet()) {
			if (docomma) {
				sb.append(",");
			} else {
				docomma = true;
			}
			SlotPool pool = entry.getKey();
			WeightWrapper totalweight = entry.getValue();
			Random ra = new Random();
			int r1, g1, b1, r2, g2, b2;
			r1 = ra.nextInt(255);
			g1 = ra.nextInt(255);
			b1 = ra.nextInt(255);
			r2 = ra.nextInt(255);
			g2 = ra.nextInt(255);
			b2 = ra.nextInt(255);
			sb.append("{");
			sb.append("value: " + ValueParserLib.round(totalweight.weight / entirepackweight * 100,2) + ",");
			sb.append("color: '" + String.format("#%02x%02x%02x", r1, g1, b1) + "',");
			sb.append("highlight: '" + String.format("#%02x%02x%02x", r2, g2, b2) + "',");
			sb.append("label: \"" + pool.getPoolname() + "\"");
			sb.append("}");
		}
		sb.append("]\n");
		sb.append("\tvar poolschart = new Chart(poolchartcanvas).Pie(data, {responsive: true});\n");
		sb.append("</script>");
		return sb.toString();

	}

	private String getDescripton() {
		return metadata.getDescription();
	}

	public String getHumanName() {
		String human = CardParser.tlkMap.get(metadata.getSrTitle());
		if (packName.startsWith("collectible") || packName.startsWith("collector") || packName.startsWith("giftpack") || packName.startsWith("commendationpack")
				|| packName.startsWith("weekly")|| packName.startsWith("loyalty")|| packName.startsWith("premiumcollectible") || packName.startsWith("customer")) {

			int packnum = 0;
			int index = packName.length()-1;
			while (Character.isDigit(packName.charAt(index))) {
				index--;
			}
			human += " " + packName.substring(index+1);
		}
		
		if (packName.equals("prophecy")) {
			human = "Commendation Pack (Prophecy)";
		}
		
		if (packName.equals("Lodestar2")) {
			human = "Commendation Pack (Lodestar Normal)";
		}
		
		if (packName.equals("Lodestar2")) {
			human = "Commendation Pack (Lodestar Insanity)";
		}
		
		if (packName.startsWith("FreeReckoning")){
			return "Gift Pack (Reckoning)";
		}
		
		if (packName.startsWith("FreeLodestar")){
			return "Gift Pack (LodeStar)";
		}

		return human;
		/*
		 * 
		 * 
		 * switch (packname) { case "starter": return "Starter"; case "trilogy":
		 * return "(Unreleased) Trilogy"; case "bronze": return "Recruit"; case
		 * "silver": return "Veteran"; case "gold": return "Spectre"; case
		 * "goldpremium": return "Premium Spectre"; case "goldjumbo": return
		 * "Jumbo Spectre"; case "equipjumbo": return "Jumbo Equipment"; case
		 * "collector11": return "HUMANNAME"; case "customer1": return
		 * "HUMANNAME"; case "customer2": return "HUMANNAME"; case "customer3":
		 * return "HUMANNAME"; case "n7equipment": return "HUMANNAME"; case
		 * "arsenal": return "Arsenal"; case "reserves": return "Reserves"; case
		 * "conmp5dlctest": return "HUMANNAME"; case "weekly1": return
		 * "HUMANNAME"; case "weekly2": return "HUMANNAME"; case "weekly3":
		 * return "HUMANNAME"; case "weekly4": return "HUMANNAME"; case
		 * "weekly5": return "HUMANNAME"; case "weekly6": return "HUMANNAME";
		 * case "victory16": return "HUMANNAME"; case "weekly7": return
		 * "HUMANNAME"; case "weekly8": return "HUMANNAME"; case "weekly9":
		 * return "HUMANNAME"; case "weekly10": return "HUMANNAME"; case
		 * "weekly11": return "HUMANNAME"; case "weekly12": return "HUMANNAME";
		 * case "weekly13": return "HUMANNAME"; case "weekly14": return
		 * "HUMANNAME"; case "weekly15": return "HUMANNAME"; case "weekly16":
		 * return "HUMANNAME"; case "weekly17": return "HUMANNAME"; case
		 * "FreeLodestar": return "HUMANNAME"; case "Lodestar1": return
		 * "HUMANNAME"; case "Lodestar2": return "HUMANNAME"; case
		 * "FreeReckoning": return "HUMANNAME"; case "prophecy": return
		 * "HUMANNAME"; case "bf3": return "HUMANNAME"; case "loyalty1": return
		 * "HUMANNAME"; case "loyalty2": return "HUMANNAME"; case "loyalty3":
		 * return "HUMANNAME"; case "loyalty4": return "HUMANNAME"; case
		 * "loyalty5": return "HUMANNAME"; case "avengerpack2": return
		 * "HUMANNAME"; case "predatorpack": return "HUMANNAME"; case
		 * "collectible0": return "HUMANNAME"; case "collectible1": return
		 * "HUMANNAME"; case "collectible2": return "HUMANNAME"; case
		 * "collectible3": return "HUMANNAME"; case "collectible4": return
		 * "HUMANNAME"; case "collectible5": return "HUMANNAME"; case
		 * "collectible6": return "HUMANNAME"; case "collectible7": return
		 * "HUMANNAME"; case "collectible8": return "HUMANNAME"; case
		 * "collectible9": return "HUMANNAME"; case "collectible10": return
		 * "HUMANNAME"; case "collectible11": return "HUMANNAME"; case
		 * "collectible12": return "HUMANNAME"; case "collectible13": return
		 * "HUMANNAME"; case "collectible14": return "HUMANNAME"; case
		 * "collectible15": return "HUMANNAME"; case "collectible16": return
		 * "HUMANNAME"; case "collectible17": return "HUMANNAME"; case
		 * "collectible18": return "HUMANNAME"; case "collectible19": return
		 * "HUMANNAME"; case "collectible20": return "HUMANNAME"; case
		 * "premiumcollectible0": return "HUMANNAME"; case
		 * "premiumcollectible1": return "HUMANNAME"; case
		 * "premiumcollectible2": return "HUMANNAME"; case
		 * "premiumcollectible4": return "HUMANNAME"; case
		 * "premiumcollectible5": return "HUMANNAME"; case "collector0": return
		 * "HUMANNAME"; case "collector1": return "HUMANNAME"; case
		 * "collector2": return "HUMANNAME"; case "collector3": return
		 * "HUMANNAME"; case "collector4": return "HUMANNAME"; case
		 * "collector5": return "HUMANNAME"; case "collector6": return
		 * "HUMANNAME"; case "collector7": return "HUMANNAME"; case
		 * "collector8": return "HUMANNAME"; case "collector9": return
		 * "HUMANNAME"; case "collector10": return "HUMANNAME"; default: return
		 * "case \"" + packname + "\":\n\treturn \"HUMANNAME\";";
		 */
	}

	public void setMetadata(PackMetadata metadata) {
		// TODO Auto-generated method stub
		this.metadata = metadata;
	}

	public boolean containsCard(RealCard findCard) {
		//MAIN POOLS
		for (PackSlot slot : slotContents) {
			for (SlotPool slotPool : slot.getMainPools()) {
				//these are only placeholder pools. we need to fetch the real ones
				CardPool realpool = CardParser.getPoolByName(slotPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					if (card.getUniqueName().equals(findCard.getUniqueName()) && (card.getPVIncrementBonus() == findCard.getPVIncrementBonus() || findCard.getCategoryName().equals("weapons"))) {
						if (findCard.getUseVersionIdx() == false || findCard.isCharCard) {
							return true;
						}
						//check for version IDX
						if (findCard.getVersionIdx() == card.getVersionIdx()) {
							return true;
						}
					}
				}
			}
		}

		//BACKUP POOL
		for (PackSlot slot : slotContents) {
			SlotPool backupPool = slot.getBackupPool();
			if (backupPool != null) {
				//these are only placeholder pools. we need to fetch the real ones
				CardPool realpool = CardParser.getPoolByName(backupPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					if (card.getUniqueName().equals(findCard.getUniqueName()) && (card.getPVIncrementBonus() == findCard.getPVIncrementBonus() || findCard.getCategoryName().equals("weapons"))) {
						if (findCard.getUseVersionIdx() == false || findCard.isCharCard) {
							return true;
						}
						//check for version IDX
						if (findCard.getVersionIdx() == card.getVersionIdx()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "StorePack [packName=" + packName + ", cost=" + getCost() + "]";
	}

	public String getSRPackName() {
		return metadata.getSrTitle() + ": " + getHumanName();
	}

	public String getDescription() {
		return metadata.getDescription();
	}

	@Override
	public int compareTo(StorePack other) {
		/*
		 * if (isOfficial) { System.out.println(packName + " is official");
		 * return -1; } if (isOfficial && !other.isOfficial) {
		 * System.out.println(packName + " is official, " + other.packName +
		 * " is not."); return 1; }
		 */
		return (getHumanName().compareToIgnoreCase(other.getHumanName()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isOfficial ? 1231 : 1237);
		result = prime * result + ((packName == null) ? 0 : packName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorePack other = (StorePack) obj;
		if (isOfficial != other.isOfficial)
			return false;
		if (packName == null) {
			if (other.packName != null)
				return false;
		} else if (!packName.equals(other.packName))
			return false;
		return true;
	}

}
