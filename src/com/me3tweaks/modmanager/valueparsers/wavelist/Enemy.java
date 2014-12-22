package com.me3tweaks.modmanager.valueparsers.wavelist;

public class Enemy {
	String enemyname;
	int min, max, maxperwave;
	
	public Enemy(String str) {
		//System.out.println(str);
		String workingStr;
		//(EnemyType="WAVE_RPR_Husk", MinCount=2, MaxCount=2)
		int charIndex = str.indexOf('\"'); // first ", which is the lead into the name.
		workingStr = str.substring(charIndex+1);
		charIndex = workingStr.indexOf('\"'); // second " which is the end of the name. clip this to get what we want.
		enemyname = workingStr.substring(0, charIndex);
		
		//check for , to see if there is more.
		if (workingStr.indexOf(',') != -1) {
			workingStr = workingStr.substring(charIndex+3);//bypass " , and <space>
			charIndex = workingStr.indexOf('=');
			while (charIndex != -1){
				//more , means more values.
				String wavestat = workingStr.substring(0,charIndex);
				//System.out.println("Before update "+workingStr);
				workingStr = workingStr.substring(charIndex+1); //got wavestat name
				//System.out.println("After update "+workingStr);
				int wavestatvalue = 0;
				int cIndex = workingStr.indexOf(',');
				if (cIndex != -1) {
					//theres more
					wavestatvalue = Integer.parseInt(workingStr.substring(0,cIndex));
					charIndex = workingStr.indexOf(',');
					workingStr = workingStr.substring(charIndex+2);
				} else {
					wavestatvalue = Integer.parseInt(workingStr.substring(0,workingStr.length()-1));
				}
				//System.out.println(wavestat+": "+wavestatvalue);
				switch (wavestat) {
				case "MinCount":
					min = wavestatvalue;
					break;
				case "MaxCount":
					max = wavestatvalue;
					break;
				case "MaxPerWave":
					maxperwave = wavestatvalue;
					break;
				}
				charIndex = workingStr.indexOf('=');
			}
		}
	}
	
	public String toString(){
		String str = enemyname+", ";
		if (min == 0 && max == 0 && maxperwave == 0){
			return str+"baseenemy";
		}
		str += "min: "+min+" max: "+max+" maxperwave: "+maxperwave;
		return str;
	}

	public String createEnemyString() {
		StringBuilder str = new StringBuilder();
		str.append("(EnemyType=\"");
		str.append(enemyname);
		str.append("\"");
		if (min != 0) {
			str.append(", MinCount=");
			str.append(min);
		}
		if (max != 0) {
			str.append(", MaxCount=");
			str.append(max);
		}
		if (maxperwave != 0) {
			str.append(", MaxPerWave=");
			str.append(maxperwave);
		}
		str.append(")"); //end stat
		return str.toString();
	}
}
