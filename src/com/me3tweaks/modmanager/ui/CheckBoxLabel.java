package com.me3tweaks.modmanager.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicCheckBoxUI;

public class CheckBoxLabel extends JCheckBox {
	public CheckBoxLabel(String string) {
		super(string);
	}

	public void updateUI() {
		setUI(new CheckBoxLabelUI());
	}
}

class CheckBoxLabelUI extends BasicCheckBoxUI {
	public void installUI(JComponent c) {
		super.installUI(c);
		Icon i = super.getDefaultIcon();
		icon_ = new EmptyIcon();
	}

	public Icon getDefaultIcon() {
		return icon_;
	}

	private Icon icon_;
}

class EmptyIcon implements Icon {
	public EmptyIcon() {
		this(0, 0);
	}

	public EmptyIcon(int width, int height) {
		width_ = width;
		height_ = height;
	}

	public int getIconHeight() {
		return height_;
	}

	public int getIconWidth() {
		return width_;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	}

	private int width_;
	private int height_;
}
