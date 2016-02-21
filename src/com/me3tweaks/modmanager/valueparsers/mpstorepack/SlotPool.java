package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.TreeSet;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class SlotPool implements Comparable<SlotPool> {
	private String poolname;
	private double poolweight;
	private TreeSet<PoolCard> poolContents;
	String inputStr;

	public SlotPool(String poolString) {
		poolname = ValueParserLib.getStringProperty(poolString, "PoolName", true);
		poolweight = ValueParserLib.getFloatProperty(poolString, "Weight");
		inputStr = poolString;
	}

	public boolean removeCard(PoolCard card) {
		return poolContents.remove(card);
	}

	public void addCard(PoolCard card) {
		poolContents.add(card);
	}

	public TreeSet<PoolCard> getPoolContents() {
		return poolContents;
	}

	public void setPoolContents(TreeSet<PoolCard> poolContents) {
		this.poolContents = poolContents;
	}

	public String getPoolname() {
		return poolname;
	}

	public void setPoolname(String poolname) {
		this.poolname = poolname;
	}

	public double getPoolweight() {
		return poolweight;
	}

	public void setPoolweight(double poolweight) {
		this.poolweight = poolweight;
	}

	public String getPoolHTML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<h2 class=\"centered dark\">Card Pool</h2>\n");
		sb.append("<h3 class=\"centered dark\">" + getPoolname() + "</h3>\n");
		sb.append("<p class=\"centered dark\">Slots in a store pack draw from a list of pools. Each slot can have independent pools from each other.</p>\n");
		sb.append("<hr class=\"dark_hr_center\">\n");
		sb.append("<div>");
		sb.append("<h3 class=\"centered\">Cards in this pool</h3>\n");

		//MAIN POOLS
		for (PoolCard pcard : poolContents) {
			//these are only placeholder pools. we need to fetch the real ones
			RealCard realcard = CardParser.getRealCardFromPoolCard(pcard);
			if (realcard != null) {
				sb.append(realcard.getCardpageHTML());
			} else {
				//	System.err.println("POOL CARD HAS NO STORE DEFINITION: "+card);
			}
		}

		sb.append("</div>");
		return sb.toString();

	}

	@Override
	public String toString() {
		return "Pool [poolname=" + poolname + ", poolweight=" + poolweight + "]";
	}

	@Override
	public int compareTo(SlotPool other) {
		// compareTo should return < 0 if this is supposed to be
		// less than other, > 0 if this is supposed to be greater than 
		// other and 0 if they are supposed to be equal
		return getPoolname().compareTo(other.getPoolname());
	}

}
