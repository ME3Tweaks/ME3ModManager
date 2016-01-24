package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;
import java.util.TreeSet;

public class StorePack {
	private ArrayList<PackSlot> slotContents = new ArrayList<PackSlot>();
	private String packName;
	private int cost;
	private PackMetadata metadata;

	public StorePack(String packname) {
		this.packName = packname;
		//System.out.println(getHumanName(packname));
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

	public void setCost(int cost) {
		this.cost = cost;
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
		return cost;
	}

	public String getPackHTML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<h2 class=\"centered dark\">" + getHumanName() + "</h2>\n");
		sb.append("<h3 class=\"centered dark\">Internally known as " + packName + "</h3>\n");
		sb.append("<p class='centered dark'>"+getDescripton()+"</p>");
		sb.append("<p class='centered dark'>"+(cost > 0 ? "Costs "+cost+" Credits" : "Free") +(metadata.getMaxPurchases() > 0 ? ", can be obtained "+metadata.getMaxPurchases()+" time"+(metadata.getMaxPurchases() != 1 ? "s": "") :"")+"</p>");
		sb.append("<hr class=\"dark_hr_center\">\n");

		sb.append("<h3 class=\"centered\">Cards obtainable in this pack</h3>\n");
		TreeSet<RealCard> cards = new TreeSet<RealCard>();
		TreeSet<RealCard> backupCards = new TreeSet<RealCard>();

		//MAIN POOLS
		for (PackSlot slot : slotContents) {
			for (Pool slotPool : slot.getMainPools()) {
				//these are only placeholder pools. we need to fetch the real ones
				Pool realpool = CardParser.getPoolByName(slotPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					RealCard realcard = CardParser.getRealCardFromPoolCard(card);
					//System.out.println(realcard);
					if (realcard != null) {
						cards.add(realcard);
					} else {
					//	System.err.println("POOL CARD HAS NO STORE DEFINITION: "+card);
					}
				}
			}
		}

		sb.append("<div>");
		sb.append("<h3 class='centered dark'>Main Cards</h3>");

		//print out tree - this is a set since pools may have overlapped.
		//System.out.println("STARTING HTML");
		for (RealCard card : cards) {
			//System.out.println(card);
			sb.append(card.getCardHTML());
		}

		//BACKUP POOL
		for (PackSlot slot : slotContents) {
			Pool backupPool = slot.getBackupPool();
			if (backupPool != null) {
				//these are only placeholder pools. we need to fetch the real ones
				Pool realpool = CardParser.getPoolByName(backupPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					RealCard realcard = CardParser.getRealCardFromPoolCard(card);
					if (realcard != null) {
						backupCards.add(realcard);
					} else {
						//System.err.println("BACKUP POOL CARD HAS NO STORE DEFINITION: "+card);
					}
				}
			}
		}
		if (backupCards.size() > 0) {
			sb.append("<hr class=\"dark_hr_center\">");
			sb.append("<h3 class='centered dark'>Backup Cards</h3>");
			for (RealCard card : backupCards) {
				sb.append(card.getCardHTML());
			}
		}

		sb.append("</div>");
		return sb.toString();
	}

	private String getDescripton() {
		return metadata.getDescription();
	}

	public String getHumanName() {
		return CardParser.tlkMap.get(metadata.getSrTitle());
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
			for (Pool slotPool : slot.getMainPools()) {
				//these are only placeholder pools. we need to fetch the real ones
				Pool realpool = CardParser.getPoolByName(slotPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					if (card.getUniqueName().equals(findCard.getUniqueName())){
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
			Pool backupPool = slot.getBackupPool();
			if (backupPool != null) {
				//these are only placeholder pools. we need to fetch the real ones
				Pool realpool = CardParser.getPoolByName(backupPool.getPoolname());
				for (PoolCard card : realpool.getPoolContents()) {
					if (card.getUniqueName().equals(findCard.getUniqueName())){
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
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		return "StorePack [packName=" + packName + ", cost=" + cost + "]";
	}

	public String getSRPackName() {
		return metadata.getSrTitle() + ": "+getHumanName();
	}

	public String getDescription() {
		return metadata.getDescription();
	}

}
