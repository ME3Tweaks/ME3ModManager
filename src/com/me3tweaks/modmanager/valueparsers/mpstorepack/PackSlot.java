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
	private ArrayList<SlotPool> mainPools = new ArrayList<SlotPool>();
	private SlotPool backupPool;

	public PackSlot(String value) {
		packname = ValueParserLib.getStringProperty(value, "PackName", true);
		numCards = ValueParserLib.getIntProperty(value, "Quantity");
		String backupPoolname = ValueParserLib.getStringProperty(value, "BackupPool", true);
		if (backupPoolname != null) {
			backupPool = new SlotPool("(PoolName=\"" + backupPoolname + "\")");
		}

		String workingStr = value.substring(value.indexOf("Pools=") + 6);

		//Pools
		String matchingStr = workingStr;
		Matcher m = Pattern.compile("\\([a-zA-Z0-9=\",.]*\\)").matcher(workingStr);
		while (m.find()) {
			workingStr = matchingStr.substring(m.end());
			mainPools.add(new SlotPool(m.group()));
		}
		
		if (mainPools.size() == 1) {
			mainPools.get(0).setPoolweight(1);
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
		for (SlotPool pool : mainPools) {
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

	public ArrayList<SlotPool> getMainPools() {
		return mainPools;
	}

	public void setMainPools(ArrayList<SlotPool> mainPools) {
		this.mainPools = mainPools;
	}

	public SlotPool getBackupPool() {
		return backupPool;
	}

	public void setBackupPool(SlotPool backupPool) {
		this.backupPool = backupPool;
	}
}
