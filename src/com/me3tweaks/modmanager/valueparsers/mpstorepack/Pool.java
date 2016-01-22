package com.me3tweaks.modmanager.valueparsers.mpstorepack;

public class Pool {
	private String poolname;
	private float poolweight;

	public Pool(String poolString) {
		//(PoolName="silverweapon",Weight=0.55)
		String workingStr;
		int charIndex = poolString.indexOf('"'); // first "
		workingStr = poolString.substring(charIndex + 1);
		charIndex = workingStr.indexOf('"'); // marks the end of pool name
		poolname = workingStr.substring(0, charIndex);
		workingStr.substring(charIndex);

		//WEIGHT IS OPTIONAL!
		charIndex = workingStr.indexOf("=");

		if (charIndex > 0) {
			workingStr = workingStr.substring(charIndex + 1);
			charIndex = workingStr.indexOf(')'); // marks the end of weight
			try {
				poolweight = Float.parseFloat(workingStr.substring(0, charIndex));
			} catch (NumberFormatException e) {
				System.err.println("Error reading weight as float: "+workingStr.substring(0, charIndex));
			}
		}
	}

	public String getPoolname() {
		return poolname;
	}

	public void setPoolname(String poolname) {
		this.poolname = poolname;
	}

	public float getPoolweight() {
		return poolweight;
	}

	public void setPoolweight(float poolweight) {
		this.poolweight = poolweight;
	}
	
	public String getPoolHTML(){
		StringBuilder sb = new StringBuilder();
		
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Pool [poolname=" + poolname + ", poolweight=" + poolweight + "]";
	}

}
