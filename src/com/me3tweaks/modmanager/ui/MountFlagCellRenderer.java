package com.me3tweaks.modmanager.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.MountFlag;

public class MountFlagCellRenderer implements ListCellRenderer<MountFlag> {
	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	public Component getListCellRendererComponent(JList list, MountFlag value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value != null) {
			renderer.setText(value.getName() + " (" + value.getHexValue()+")");
		}
		return renderer;
	}
}
