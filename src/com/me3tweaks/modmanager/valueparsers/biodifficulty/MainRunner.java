package com.me3tweaks.modmanager.valueparsers.biodifficulty;


import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.IOException;
public class MainRunner {
	
	public static void main(String[] args) throws IOException {
		String input = new String(readAllBytes(get("C:/Users/Michael/Desktop/input.txt")));
		Category cat = new Category(input);
		String output = cat.createCategoryString();
		
		System.out.println("Input equals output? "+output.equals(input));
		//Category merge = new Category(mergeinput);
		//cat.merge(merge);
		//System.out.println(cat);
		System.out.println(input);
		System.out.println(cat.createCategoryString());
		diffString(input,output);
	}
	
	public static void diffString(String str1, String str2) {
		if (str1.length() != str2.length()) {
			System.out.println("Strings are not the same length: "+str1.length()+" vs " + str2.length());
			
			return;
		}
		for (int i = 0; i < str1.length(); i++) {
			if (str1.charAt(i) == str2.charAt(i)) {
				continue;
			} else {
				System.out.println("Difference at index "+i+", str1: "+ str1.charAt(i) +", str2: "+str2.charAt(i));
			}
		}
	}
}
