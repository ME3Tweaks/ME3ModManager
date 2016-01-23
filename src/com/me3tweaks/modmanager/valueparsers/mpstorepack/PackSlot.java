package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class PackSlot {
	private String packname;
	private int numCards;
	private ArrayList<Pool> mainPools = new ArrayList<Pool>();
	private Pool backupPool;

	public PackSlot(String value) {
		packname = ValueParserLib.getStringProperty(value, "PackName", true);
		numCards = ValueParserLib.getIntProperty(value, "Quantity");
		String backupPoolname = ValueParserLib.getStringProperty(value, "BackupPool", true);
		if (backupPoolname != null) {
			backupPool = new Pool("(PoolName=\"" + backupPoolname + "\")");
		}

		String workingStr = value.substring(value.indexOf("Pools=") + 6);

		//Pools
		String matchingStr = workingStr;
		Matcher m = Pattern.compile("\\([a-zA-Z0-9=\",.]*\\)").matcher(workingStr);
		while (m.find()) {
			workingStr = matchingStr.substring(m.end());
			mainPools.add(new Pool(m.group()));
		}

		//Backup Pool, if any.

	}

	@Override
	public String toString() {
		return "PackSlot [packname=" + packname + ", numCards=" + numCards + ", mainPools=" + mainPools + ", backupPool=" + backupPool + "]";
	}

	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		sb.append(numCards);
		sb.append(" card" + (numCards != 1 ? "s" : ""));
		sb.append(" from: ");
		for (Pool pool : mainPools) {
			sb.append("\n\t");
			sb.append(pool.getPoolname());
		}
		if (backupPool != null) {
			sb.append("\n\tBP: " + backupPool.getPoolname());
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
