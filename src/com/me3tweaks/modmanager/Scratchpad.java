package com.me3tweaks.modmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Scratchpad {

	/**
	 * Scratchpad is anywhere to just write code i need to run
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String inputfile = "C:/users/michael/desktop/input.txt";
		try(BufferedReader br = new BufferedReader(new FileReader(new File(inputfile)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		    	int colIndex = line.indexOf(",");
		    	int codeOffset = line.indexOf("0x");
		    	int equOffset = line.indexOf("="); 
		    	
		    	System.out.println(" { "+line.substring(codeOffset, colIndex) + ", \""+line.substring(0,equOffset).trim()+"\" },");
		    }
		    // line is not visible here.
		}
	}

}
