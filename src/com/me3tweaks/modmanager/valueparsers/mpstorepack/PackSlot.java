package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackSlot {
	private String packname;
	private int numCards;
	private ArrayList<Pool> mainPools = new ArrayList<Pool>();
	private Pool backupPool;

	public PackSlot(String value) {
		//get Pack Name
		String workingStr;
		int charIndex = value.indexOf('"'); // first "
		workingStr = value.substring(charIndex + 1);
		charIndex = workingStr.indexOf('"'); // marks the end of pack name
		packname = workingStr.substring(0, charIndex);

		//Quantity
		charIndex = workingStr.indexOf("=");
		workingStr = workingStr.substring(charIndex + 1);
		charIndex = workingStr.indexOf(','); // marks the end of pack name
		try {
			numCards = Integer.parseInt(workingStr.substring(0, charIndex));
		} catch (NumberFormatException e) {
			System.err.println("Failed to parse number of cards as int: " + workingStr.substring(0, charIndex));
		}

		//Pools
		charIndex = workingStr.indexOf("=");
		workingStr = workingStr.substring(charIndex + 1);

		String matchingStr = workingStr;
		Matcher m = Pattern.compile("\\([a-zA-Z0-9=\",.]*\\)").matcher(workingStr);
		while (m.find()) {
			workingStr = matchingStr.substring(m.end());
			mainPools.add(new Pool(m.group()));
		}

		//Backup Pool, if any.
		System.out.println("Remaining text: " + workingStr);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(numCards + " card" + (numCards != 1 ? "s" : "") + " from one of the following pools:");
		for (Pool pool : mainPools) {
			sb.append("\n\t");
			sb.append(pool.getPoolname());
		}

		sb.append("\n\n");
		if (backupPool != null) {
			sb.append("If all the above pools are unavailable for use, the following backup pool is used:\n\t");
			sb.append(backupPool.getPoolname());
		}
		return sb.toString();
	}

	public String getPackname() {
		return packname;
	}

	public void setPackname(String packname) {
		this.packname = packname;
	}

	public int getNumCards() {
		return numCards;
	}

	public void setNumCards(int numCards) {
		this.numCards = numCards;
	}

	public ArrayList<Pool> getMainPools() {
		return mainPools;
	}

	public void setMainPools(ArrayList<Pool> mainPools) {
		this.mainPools = mainPools;
	}

	public Pool getBackupPool() {
		return backupPool;
	}

	public void setBackupPool(Pool backupPool) {
		this.backupPool = backupPool;
	}
}
