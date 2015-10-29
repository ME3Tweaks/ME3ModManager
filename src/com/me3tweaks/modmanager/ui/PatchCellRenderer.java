package com.me3tweaks.modmanager.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.me3tweaks.modmanager.objects.Patch;

public class PatchCellRenderer implements ListCellRenderer<Patch> {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	public Component getListCellRendererComponent(JList list, Patch value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		renderer.setText(value.getPatchName());
		return renderer;
	}
}
