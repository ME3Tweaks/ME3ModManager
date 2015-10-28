package com.me3tweaks.modmanager.objects;

public class MountFlag {
	private String name;
	private byte value;

	public MountFlag(String name, int value) {
		this.name = name;
		this.value = (byte) value;
	}

	/**
	 * Returns the human-readable description of this mount flag
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the radix 10 version of this mount flag as a byte
	 * This byte is signed.
	 * 
	 * @return
	 */
	public byte getValue() {
		return value;
	}

	public void setValue(byte value) {
		this.value = value;
	}

	public String getHexValue() {
		return "0x" + Integer.toHexString(value);
	}

}
