package com.me3tweaks.modmanager.valueparsers.biodifficulty;


public class StatRange {
	boolean isfloat = false;
	double floatx, floaty;
	int intx, inty;

	public StatRange(String xstat, String ystat) {
		if (xstat.contains("f")){
			this.floatx = Double.parseDouble(xstat.substring(0, xstat.length()-1));
			this.floaty = Double.parseDouble(ystat.substring(0, ystat.length()-1));
			isfloat = true;
		} else {
			this.intx = Integer.parseInt(xstat);
			this.inty = Integer.parseInt(ystat);
			isfloat = false;
		}
	}
	
	public String toString(){
		if (isfloat) {
			return floatx +" - "+floaty;
		} else {
			return intx +" - "+inty;
		}
	}

	public String createStatString() {
		StringBuilder str = new StringBuilder();
		str.append("StatRange=(X=");
		str.append((this.isfloat) ? this.floatx : this.intx);
		str.append("f,Y="); //append f, start Y
		str.append((this.isfloat) ? this.floaty : this.inty);
		str.append("f)"); //append f
		return str.toString();
	}
}
