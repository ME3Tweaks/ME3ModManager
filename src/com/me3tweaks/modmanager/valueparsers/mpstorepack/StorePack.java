package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.ArrayList;

public class StorePack {
	ArrayList<PackSlot> slotContents = new ArrayList<PackSlot>();
	
	public void addCardSlot(PackSlot packslot) {
		slotContents.add(packslot);
	}

	public String getContentsString() {
		StringBuilder sb = new StringBuilder();
		for (PackSlot slot : slotContents) {
			sb.append(slot);
			sb.append("\n");
		}
		return sb.toString();
	}
}
