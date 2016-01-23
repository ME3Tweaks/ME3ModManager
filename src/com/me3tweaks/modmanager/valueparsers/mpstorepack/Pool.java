package com.me3tweaks.modmanager.valueparsers.mpstorepack;

import java.util.TreeSet;

import com.me3tweaks.modmanager.valueparsers.ValueParserLib;

public class Pool implements Comparable<Pool>{
	private String poolname;
	private double poolweight;
	private TreeSet<Card> poolContents;

	public Pool(String poolString) {
		poolname = ValueParserLib.getStringProperty(poolString, "PoolName", true);
		poolweight = ValueParserLib.getFloatProperty(poolString, "Weight");
		poolContents = new TreeSet<Card>();
	}

	public boolean removeCard(Card card) {
		return poolContents.remove(card);
	}

	public void addCard(Card card) {
		poolContents.add(card);
	}

	public TreeSet<Card> getPoolContents() {
		return poolContents;
	}

	public void setPoolContents(TreeSet<Card> poolContents) {
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

		return sb.toString();
	}

	@Override
	public String toString() {
		return "Pool [poolname=" + poolname + ", poolweight=" + poolweight + "]";
	}

	@Override
	public int compareTo(Pool other) {
		// compareTo should return < 0 if this is supposed to be
		// less than other, > 0 if this is supposed to be greater than 
		// other and 0 if they are supposed to be equal
		return getPoolname().compareTo(other.getPoolname());
	}

}
