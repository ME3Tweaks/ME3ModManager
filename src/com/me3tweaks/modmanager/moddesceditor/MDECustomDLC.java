package com.me3tweaks.modmanager.moddesceditor;

import javax.swing.*;
import java.util.AbstractMap.SimpleEntry;

public class MDECustomDLC {

	private JButton subtractButton;
	private SimpleEntry<String, String> pair;
	private JPanel custDLCLineItem;

	public MDECustomDLC(JPanel custDLCLineItem, JButton subtractButton, SimpleEntry<String, String> pair) {
		this.setSubtractButton(subtractButton);
		this.setCustDLCLineItem(custDLCLineItem);
		this.setPair(pair);
	}

	public JPanel getCustDLCLineItem() {
		return custDLCLineItem;
	}

	public void setCustDLCLineItem(JPanel custDLCLineItem) {
		this.custDLCLineItem = custDLCLineItem;
	}

	public SimpleEntry<String, String> getPair() {
		return pair;
	}

	public void setPair(SimpleEntry<String, String> pair) {
		this.pair = pair;
	}

	public JButton getSubtractButton() {
		return subtractButton;
	}

	public void setSubtractButton(JButton subtractButton) {
		this.subtractButton = subtractButton;
	}
}