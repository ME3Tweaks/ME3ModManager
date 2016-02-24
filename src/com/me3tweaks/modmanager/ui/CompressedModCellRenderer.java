package com.me3tweaks.modmanager.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.me3tweaks.modmanager.objects.CompressedMod;

public class CompressedModCellRenderer implements ListCellRenderer<CompressedMod> {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	public Component getListCellRendererComponent(JList list, CompressedMod value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		renderer.setText(value.getModName());
		return renderer;
	}
}
